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
import org.jas.ksinxapp.repo.UserRepo;
import org.jas.ksinxapp.security.SecurityConfig;
import org.jas.ksinxapp.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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

    @Transactional
    public StudentResponse registerStudent(StudentRegistrationRequest request) {

        //check if the email is already taken
        if(userRepo.existsByEmail(request.email())){
            throw new RuntimeException("An account with that email already exists");
        }

        //map the basic fields
        User newUser = userMapper.toEntity(request);

        //set the secure fields manually
        newUser.setRole(User.Role.STUDENT);

        //replace with Bcrypt later
        newUser.setPassword(passwordEncoder.encode(request.password()));

        //save to postgresql
        User savedUser = userRepo.save(newUser);

        //return dto response
        return userMapper.toResponse(savedUser);

    }

    public LoginResponse login(LoginRequest request) {
        //Authenticates the user
        //triggers userDetailService and compares passwords using encoder
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        //if we reach here authentication was successful
        //get the principal and cast it to your custom class
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        //GENERATE THE TOKEN HERE
        // Use the username (fullName) from the principal to create the ticket
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
}
