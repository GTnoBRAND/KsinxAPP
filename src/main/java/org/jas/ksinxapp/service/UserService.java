package org.jas.ksinxapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jas.ksinxapp.dtos.LoginRequest;
import org.jas.ksinxapp.dtos.LoginResponse;
import org.jas.ksinxapp.dtos.StudentRegistrationRequest;
import org.jas.ksinxapp.dtos.StudentResponse;
import org.jas.ksinxapp.jwt.JwtService;
import org.jas.ksinxapp.mappers.LoginMapper;
import org.jas.ksinxapp.mappers.UserMapper;
import org.jas.ksinxapp.model.User;
import org.jas.ksinxapp.model.UserRegisteredEvent;
import org.jas.ksinxapp.model.VerificationResult;
import org.jas.ksinxapp.model.VerificationToken;
import org.jas.ksinxapp.repo.UserRepo;
import org.jas.ksinxapp.repo.VerificationTokenRepository;
import org.jas.ksinxapp.security.UserPrincipal;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final LoginMapper loginMapper;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder  passwordEncoder;
    private final JwtService  jwtService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final ApplicationEventPublisher eventPublisher;
    

    @Transactional
    public StudentResponse registerStudent(StudentRegistrationRequest request) {

        //check if the email is already taken — notify the existing owner and return
        //a benign response so we don't leak which emails are registered
        if(userRepo.existsByEmail(request.email())){
            emailService.sendAccountExistingEmail(request.email());
            return new StudentResponse(null, request.email(), request.fullName(), User.Role.STUDENT.name());
        }

        //map the basic fields
        User newUser = userMapper.toEntity(request);

        //set the secure fields manually
        newUser.setRole(User.Role.STUDENT);

        //replace with Bcrypt later
        newUser.setPassword(passwordEncoder.encode(request.password()));

        //save to postgresql
        User savedUser = userRepo.save(newUser);

        issueVerificationToken(newUser);

        //return dto response
        return userMapper.toResponse(savedUser);

    }

    public LoginResponse login(LoginRequest request) {
        //Authenticates the user
        //triggers userDetailService and compares passwords using encoder
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (org.springframework.security.authentication.DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Please verify your email before logging in.");
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Invalid email or password.");
        }

        //if we reach here authentication was successful
        //get the principal and cast it to your custom class
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        //GENERATE THE TOKEN HERE
        // Use the email from the principal to create the ticket
        assert principal != null;
        String token = jwtService.generateToken(principal.getUser().getRole(), principal.getUser().getEmail());


        //extract the actual entity from your principal
        return loginMapper.loginResponse(principal.getUser(),  token);
    }

    @Transactional
    public StudentResponse getStudentById(Long id){
        User savedUser = userRepo.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentRegistrationRequest request) {
        User savedUser = userRepo.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        userMapper.updateEntityFromDto(request, savedUser);

        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public StudentResponse deleteStudentById(Long id){

        User savedUser = userRepo.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        userRepo.delete(savedUser);
        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public List<StudentResponse> getAllStudents(){

        return userRepo.findAll()
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public User getStudentByFullName(String fullName){
        User user = userRepo.findByFullName(fullName);
        if(user == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "student not found " + fullName);
        }

        return user;
    }

    @Transactional
    public StudentResponse updateUserRole(Long id, User.Role role) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() == User.Role.ADMIN && role != User.Role.ADMIN
                && userRepo.countByRole(User.Role.ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot demote the last remaining admin");
        }

        user.setRole(role);
        return userMapper.toResponse(user);
    }


    public void issueVerificationToken(User user){
        verificationTokenRepository.deleteByUser(user); //delete old token if any


        String token = UUID.randomUUID().toString();
        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUser(user);
        vt.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        verificationTokenRepository.save(vt);

//        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), token);
        //publish the event with ApplicationEventPublisher(the listener sends the email only if the transaction is commited)
        eventPublisher.publishEvent(
                new UserRegisteredEvent(user.getEmail(), user.getFullName(), vt.getToken())
        );
    }


    @Transactional
    public VerificationResult verify(String token){
        Optional<VerificationToken> maybe = verificationTokenRepository.findByToken(token);

        if(maybe.isEmpty()){
            return VerificationResult.INVALID;
        }

        VerificationToken vt = maybe.get();
        User user = vt.getUser();

        //already verified, but they clicked the link again
        if(user.isEnabled()){
            verificationTokenRepository.delete(vt);
            return VerificationResult.ALREADY_VERIFIED;
        }

        if(vt.isExpired()){
            verificationTokenRepository.delete(vt);
            return VerificationResult.EXPIRED;
        }

        user.setEnabled(true);
        userRepo.save(user);
        verificationTokenRepository.save(vt);
        return VerificationResult.SUCCESS;
    }

    @Transactional
    public void resendVerification(String email){
        // Silent on whether the email exists or is already verified
        userRepo.findByEmail(email)
                .filter(u->!u.isEnabled())
                .ifPresent(this::issueVerificationToken);
    }
}
