package ec.sergy.service;

import ec.sergy.model.LoginRequest;
import ec.sergy.model.RefreshTokenRequest;
import ec.sergy.model.RegisterRequest;
import ec.sergy.entity.User;
import ec.sergy.repository.UserRepository;
import ec.sergy.repository.UserRepositoryByRefreshToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;

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

    public ResponseEntity<Map<String, String>> register(RegisterRequest request) {
        Map<String, String> response = new HashMap<>();

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            response.put("message", "Email already exists");
            return ResponseEntity.badRequest().body(response);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);
        response.put("message", "User registered successfully");
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.ok(Map.of(
                    "message", "Invalid credentials"
            ));
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

        // Verificar si el token existe en la base de datos y está asociado a un usuario
        User user = userRepositoryByRefreshToken.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        // Validar el refresh token (verificar expiración y firma)
        if (!jwtService.isTokenValid(refreshToken, user.getEmail())) {
            throw new IllegalArgumentException("Expired or invalid refresh token");
        }

        // Generar un nuevo access token
        String newToken = jwtService.generateRefreshToken(
                user.getEmail()
        );

        // Actualiza en BD
        user.setRefreshToken(newToken);
        userRepository.save(user);

        // Retornar el nuevo token
        return ResponseEntity.ok(Map.of("refreshToken", newToken));
    }
}