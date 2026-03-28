package com.payflow.ms_auth.controller;

import com.payflow.ms_auth.dto.AuthenticationDTO;
import com.payflow.ms_auth.dto.LoginResponseDTO;
import com.payflow.ms_auth.dto.UserDTO;
import com.payflow.ms_auth.model.User;
import com.payflow.ms_auth.repository.UserRepository;
import com.payflow.ms_auth.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<User> saveUser(@RequestBody @Valid UserDTO userDto) {
        var user = new User();
        BeanUtils.copyProperties(userDto, user);

        user.setPassword(passwordEncoder.encode(userDto.password()));

        return ResponseEntity.status(HttpStatus.CREATED).body(userRepository.save(user));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthenticationDTO data) {

        // 1. Criamos um "Ticket" com o email e a senha puros que o usuário digitou
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());

        // 2. O Manager vai pegar esse ticket, ir lá no AuthorizationService, buscar no banco, criptografar a senha digitada e comparar com o hash do banco. Tudo sozinho!
        var auth = this.authenticationManager.authenticate(usernamePassword);

        // 3. Se deu certo, geramos o Token JWT (A pulseira VIP!)
        var token = tokenService.generateToken((User) auth.getPrincipal());

        // 4. Devolvemos o token para o cliente
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}