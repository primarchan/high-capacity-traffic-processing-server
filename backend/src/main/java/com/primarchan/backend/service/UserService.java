package com.primarchan.backend.service;

import com.primarchan.backend.dto.SignupUser;
import com.primarchan.backend.entity.User;
import com.primarchan.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(SignupUser signupUser) {
        User user = new User();
        user.setUsername(signupUser.getUsername());
        user.setPassword(passwordEncoder.encode(signupUser.getPassword()));
        user.setEmail(signupUser.getEmail());

        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
