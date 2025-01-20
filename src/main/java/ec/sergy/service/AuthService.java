package ec.sergy.service;

import ec.sergy.model.LoginRequest;
import ec.sergy.model.RefreshTokenRequest;
import ec.sergy.model.RegisterRequest;
import ec.sergy.model.User;
import ec.sergy.repository.UserRepository;
import ec.sergy.repository.UserRepositoryByRefreshToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRepositoryByRefreshToken userRepositoryByRefreshToken;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       UserRepositoryByRefreshToken userRepositoryByRefreshToken) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userRepositoryByRefreshToken = userRepositoryByRefreshToken;
    }

    public ResponseEntity<String> register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    public ResponseEntity<?> authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            String token = jwtService.generateToken(user.getEmail(), Map.of("role", user.getRole()));
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    public ResponseEntity<?> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String accessToken = jwtService.generateToken(user.getEmail(), Map.of("role", user.getRole()));
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        // Guardar el Refresh Token en la base de datos
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }

    public ResponseEntity<?> refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        User user = userRepositoryByRefreshToken.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        String accessToken = jwtService.generateToken(user.getEmail(), Map.of("role", user.getRole()));

        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }
}