package com.colaborai.colaborai.auth;

import com.colaborai.colaborai.auth.dto.LoginRequest;
import com.colaborai.colaborai.auth.dto.RegisterRequest;
import com.colaborai.colaborai.auth.dto.JwtResponse;
import com.colaborai.colaborai.auth.exception.InvalidCredentialsException;
//import com.colaborai.colaborai.entity.Role;
import com.colaborai.colaborai.entity.User;
import com.colaborai.colaborai.repository.RoleRepository;
import com.colaborai.colaborai.repository.UserRepository;
import com.colaborai.colaborai.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public JwtResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Collections.singleton(
                roleRepository.findByName("ROLE_USER").orElseThrow()
        ));
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getUsername());
        return new JwtResponse(token, user.getId(), user.getUsername());
    }

    public JwtResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);
        
        // Siempre ejecutar el encoder para evitar ataques de tiempo
        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            String token = jwtUtil.generateToken(user.getUsername());
            return new JwtResponse(token, user.getId(), user.getUsername());
        } else {
            // Ejecutar passwordEncoder incluso si el usuario no existe para evitar timing attacks
            if (user == null) {
                passwordEncoder.matches("dummy", "$2a$10$dummyHashToPreventTimingAttacks");
            }
            throw new InvalidCredentialsException();
        }
    }
}