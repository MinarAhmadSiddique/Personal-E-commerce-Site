package com.example.shop.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(UserService userService,AuthenticationManager authenticationManager){
        this.userService=userService;
        this.authenticationManager=authenticationManager;
    }

    public static class SignupRequest{
        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Size(min = 0,max = 100)
        private String password;

        @NotBlank
        private String name;

        public String getEmail() {return email;}
        public void setEmail(String email) {this.email=email;}

        public String getPassword() {return password;}
        public void setPassword(String password) {this.password=password;}

        public String getName() {return name;}
        public void setName(String name) {this.name=name;}
    }

    public static class SigninRequest{
        @NotBlank
        @Email
        private String email;

        @NotBlank
        private String password;

        public String getEmail() {return email;}
        public void setEmail(String email) {this.email=email;}

        public String getPassword() {return password;}
        public void setPassword(String password) {this.password=password;}
    }

    public static class UserResponse{
        private Long id;
        private String email;
        private String name;
        private String role;

        public UserResponse(Long id,String email,String name,String role){
            this.id=id;
            this.email=email;
            this.name=name;
            this.role=role;
        }

        public static UserResponse from(User u){
            return new UserResponse(u.getId(),u.getEmail(),u.getName(),u.getRole().name());
        }

        public Long getId() {return id;}
        public String getEmail() {return email;}
        public String getName() {return name;}
        public String getRole() {return role;}
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest req){
        User user = userService.register(req.getEmail(),req.getPassword(),req.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PostMapping("/signin")
    public ResponseEntity<UserResponse> signin(@Valid @RequestBody SigninRequest req,HttpServletRequest request,HttpServletResponse response){
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail().trim().toLowerCase(),req.getPassword()));

        SecurityContext context =SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context,request,response);

        User user = userService.findByEmail(req.getEmail());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication auth){
        if(auth == null || !auth.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.findByEmail(auth.getName());
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
