package com.example.shop.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
    }

    @Transactional
    public User register(String email, String rawPassword, String name){
        String normalizedEmail = email.trim().toLowerCase();

        if(userRepository.existsByEmail(normalizedEmail)){
            throw new EmailAlreadyUsedException(normalizedEmail);
        }

        String passwordHash = passwordEncoder.encode(rawPassword);

        User user = new User(normalizedEmail,passwordHash,name);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email){
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(()->new IllegalStateException("Authenticated user not found"));
    }
}
