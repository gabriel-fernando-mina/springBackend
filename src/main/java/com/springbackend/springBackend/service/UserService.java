package com.springbackend.springBackend.service;

import com.springbackend.springBackend.dto.UserDTO;
import com.springbackend.springBackend.model.User;
import com.springbackend.springBackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MfaService mfaService; // Servicio para MFA (Multi-Factor Authentication)

    /**
     * Registra un nuevo usuario en la base de datos con la contraseña encriptada y configuración de MFA.
     *
     * @param userDTO Los detalles del usuario a registrar.
     * @return El usuario registrado.
     */
    public User registerUser(UserDTO userDTO) {
        // Validar si el username o email ya están en uso
        if (userRepository.existsByUsername(userDTO.getUsername()) || userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("El usuario o email ya están en uso.");
        }

        // Generar secreto MFA
        String mfaSecret = mfaService.generateSecret();

        // Crear el nuevo usuario
        User newUser = new User();
        newUser.setUsername(userDTO.getUsername());
        newUser.setEmail(userDTO.getEmail());
        newUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        newUser.setMfaSecret(mfaSecret); // Guardar el secreto MFA en la base de datos

        // Guardar el usuario
        userRepository.save(newUser);

        // Opción: Generar URL del código QR y devolverla
        String qrUrl = mfaService.generateQRUrl(userDTO.getUsername(), mfaSecret);

        System.out.println("Escanea este código QR para configurar MFA: " + qrUrl);

        return newUser;
    }

    /**
     * Carga un usuario por su nombre de usuario o email (para autenticación).
     *
     * @param usernameOrEmail El nombre de usuario o email.
     * @return Los detalles del usuario para Spring Security.
     * @throws UsernameNotFoundException Si el usuario no se encuentra.
     */
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        return userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        user.getAuthorities()))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    /**
     * Encuentra un usuario por su email o nombre de usuario.
     *
     * @param emailOrUsername El email o nombre de usuario.
     * @return El usuario encontrado, si existe.
     */
    public Optional<User> findByEmailOrUsername(String emailOrUsername) {
        return userRepository.findByUsername(emailOrUsername)
                .or(() -> userRepository.findByEmail(emailOrUsername));
    }

    /**
     * Encuentra un usuario por su nombre de usuario.
     *
     * @param username El nombre de usuario.
     * @return El usuario encontrado.
     * @throws UsernameNotFoundException Si el usuario no se encuentra.
     */
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    /**
     * Verifica si la contraseña proporcionada coincide con la almacenada.
     *
     * @param rawPassword    La contraseña proporcionada.
     * @param encodedPassword La contraseña encriptada almacenada.
     * @return true si las contraseñas coinciden, false de lo contrario.
     */
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}