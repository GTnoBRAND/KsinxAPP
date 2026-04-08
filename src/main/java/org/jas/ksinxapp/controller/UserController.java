package org.jas.ksinxapp.controller;

import jakarta.validation.Valid;
import org.jas.ksinxapp.dtos.StudentRegistrationRequest;
import org.jas.ksinxapp.dtos.StudentResponse;
import org.jas.ksinxapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //post request to register the user
    @PostMapping("/register")
    public ResponseEntity<StudentResponse> register(@Valid @RequestBody StudentRegistrationRequest request){

        StudentResponse response = userService.registerStudent(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<StudentResponse> update(@PathVariable Long id ,@Valid @RequestBody StudentRegistrationRequest request){
        return  ResponseEntity.status(HttpStatus.CREATED).body(userService.updateStudent(id,request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<StudentResponse> deleteStudent(@PathVariable Long id){
        return ResponseEntity.ok(userService.deleteStudentById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<StudentResponse>> getAllStudents(){

        return ResponseEntity.ok(userService.getAllStudents());
    }

}
