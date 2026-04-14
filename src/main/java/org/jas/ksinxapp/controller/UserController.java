package org.jas.ksinxapp.controller;

import jakarta.validation.Valid;
import org.jas.ksinxapp.dtos.LoginRequest;
import org.jas.ksinxapp.dtos.LoginResponse;
import org.jas.ksinxapp.dtos.StudentRegistrationRequest;
import org.jas.ksinxapp.dtos.StudentResponse;
import org.jas.ksinxapp.jwt.JwtService;
import org.jas.ksinxapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    //post request to register the user
    @PostMapping("/register")
    public ResponseEntity<StudentResponse> register(@Valid @RequestBody StudentRegistrationRequest request){

        StudentResponse response = userService.registerStudent(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAllRoles('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<StudentResponse> update(@PathVariable Long id ,@Valid @RequestBody StudentRegistrationRequest request){
        return  ResponseEntity.status(HttpStatus.CREATED).body(userService.updateStudent(id,request));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<StudentResponse> deleteStudent(@PathVariable Long id){
        return ResponseEntity.ok(userService.deleteStudentById(id));
    }

    @GetMapping("/all")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentResponse>> getAllStudents(){

        return ResponseEntity.ok(userService.getAllStudents());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request){
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/auth/token")
    public ResponseEntity<?> getToken(Authentication  authentication){
        if(authentication == null || !authentication.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated with Google");
        }

        // Spring identifies the user from the Google session
        String fullName = authentication.getName();

        //generate the token
        String token = jwtService.generateToken(fullName);

        //return it as a simple json
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

}
