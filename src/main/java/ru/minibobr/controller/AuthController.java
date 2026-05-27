package ru.minibobr.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import ru.minibobr.dto.AuthResponse;
import ru.minibobr.dto.LoginRequest;
import ru.minibobr.dto.RegisterRequest;
import ru.minibobr.models.User;
import ru.minibobr.service.AuthService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        boolean success = authService.register(request.getUsername(), request.getPassword());
        if (success) {
            return ResponseEntity.ok(new AuthResponse(true, "Registration successful", null));
        } else {
            return ResponseEntity.badRequest().body(new AuthResponse(false, "Username already exists", null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        Optional<User> userOpt = authService.authenticate(request.getUsername(), request.getPassword());
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getUsername(), null, List.of(() -> "ROLE_USER")
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());
            session.setAttribute("user", user);

            return ResponseEntity.ok(new AuthResponse(true, "Login successful", session.getId()));
        } else {
            return ResponseEntity.status(401).body(new AuthResponse(false, "Invalid credentials", null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(new AuthResponse(true, "Logout successful", null));
    }

    @GetMapping("/check")
    public ResponseEntity<AuthResponse> checkAuth(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return ResponseEntity.ok(new AuthResponse(true, "Authenticated", session.getId()));
        } else {
            return ResponseEntity.status(401).body(new AuthResponse(false, "Not authenticated", null));
        }
    }
}


