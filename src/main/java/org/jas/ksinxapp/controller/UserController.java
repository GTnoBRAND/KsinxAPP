package org.jas.ksinxapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jas.ksinxapp.RateLimit.RegistrationRateLimiter;
import org.jas.ksinxapp.dtos.*;
import org.jas.ksinxapp.jwt.JwtService;
import org.jas.ksinxapp.model.User;
import org.jas.ksinxapp.model.VerificationResult;
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
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final RegistrationRateLimiter rateLimiter;

    //post request to register the user
    @PostMapping("/register")
    public ResponseEntity<StudentResponse> register(@Valid @RequestBody StudentRegistrationRequest request,
                                                    HttpServletRequest http){

        String ip = http.getRemoteAddr();
        if(!rateLimiter.tryAcquire( ip, 10)){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        if(!rateLimiter.tryAcquire(request.email(), 3)){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

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

    //admin-only: change a user's role (ADMIN, TEACHER, STUDENT)
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponse> updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request){
        return ResponseEntity.ok(userService.updateUserRole(id, request.role()));
    }

    @GetMapping("/all")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentResponse>> getAllStudents(){

        return ResponseEntity.ok(userService.getAllStudents());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request){
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/auth/token")
    public ResponseEntity<?> getToken(Authentication  authentication){
        if(authentication == null || !authentication.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated with Google");
        }

        // Spring identifies the user from the Google session
        String fullName = authentication.getName();

        //get the user from db to get role and email
        User user = userService.getStudentByFullName(fullName);
        if(user == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        //generate the token
        String token = jwtService.generateToken(
                user.getRole(),
                user.getEmail());

        //return it as a simple json
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verify(@RequestParam String token){
        VerificationResult result = userService.verify(token);
        return switch (result){
            case SUCCESS -> ResponseEntity.ok(Map.of("Message", "Email verified. You can login now."));
            case ALREADY_VERIFIED -> ResponseEntity.ok(Map.of("Message", "Email already verified."));
            case EXPIRED -> ResponseEntity.status(HttpStatus.GONE).body(Map.of("Message", "Link expired. Request a new one!"));
            case INVALID -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("Message", "Invalid token."));
        };
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resend(
            @Valid @RequestBody ResendVerificationRequest request,
            HttpServletRequest http) {

        String ip = http.getRemoteAddr();
        if (!rateLimiter.tryAcquire("resend:ip:" + ip, 5)) {            // 5 per hour per IP
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        if (!rateLimiter.tryAcquire("resend:email:" + request.email(), 3)) {  // 3 per hour per email
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        userService.resendVerification(request.email());
        return ResponseEntity.accepted()
                .body(Map.of("Message", "If an account exists for this email, verification has been sent"));
    }
}
