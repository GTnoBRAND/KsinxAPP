package org.jas.ksinxapp.service;

import jakarta.transaction.Transactional;
import org.jas.ksinxapp.dtos.StudentRegistrationRequest;
import org.jas.ksinxapp.dtos.StudentResponse;
import org.jas.ksinxapp.mappers.UserMapper;
import org.jas.ksinxapp.model.User;
import org.jas.ksinxapp.repo.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;

    public UserService(UserRepo userRepo, UserMapper userMapper) {
        this.userRepo = userRepo;
        this.userMapper = userMapper;
    }

    @Transactional
    public StudentResponse registerStudent(StudentRegistrationRequest request) {

        //check if the email is already taken
        if(userRepo.existsByEmail(request.email())){
            throw new RuntimeException("An account with that email already exists");
        }

        //map the basic fields
        User newUser = userMapper.toEntity(request);

        //set the secure fields manually
        newUser.setRole(User.Role.USER);

        //replace with Bcrypt later
        newUser.setPassword(request.password());

        //save to postgresql
        User savedUser = userRepo.save(newUser);

        //return dto response
        return userMapper.toResponse(savedUser);

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
}
