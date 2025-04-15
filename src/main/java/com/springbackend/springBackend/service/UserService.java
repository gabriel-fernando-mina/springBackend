package com.springbackend.springBackend.service;

import com.springbackend.springBackend.dto.UserDTO;
import com.springbackend.springBackend.mapper.UserMapper;
import com.springbackend.springBackend.model.RoleType;
import com.springbackend.springBackend.model.User;
import com.springbackend.springBackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Registra un nuevo usuario en la base de datos.
     *
     * @param userDTO Los datos del usuario proporcionados para el registro.
     * @return El objeto User recién registrado.
     */
    public User registerUser(UserDTO userDTO) {
        if (userRepository.findByUsernameOrEmail(userDTO.getUsername(), userDTO.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario o email ya está en uso.");
        }

        // Mapear UserDTO a User
        User user = UserMapper.toEntity(userDTO);

        // Establecer el rol a USER por defecto
        user.setRole(RoleType.USER);

        // Codificar la contraseña antes de guardar
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        // Guardar el usuario en la base de datos
        User savedUser = userRepository.save(user);
        logger.info("Usuario registrado con éxito: {}", savedUser.getUsername());
        return savedUser;
    }

    /**
     * Obtiene un usuario por nombre de usuario o correo electrónico.
     *
     * @param usernameOrEmail El nombre de usuario o correo electrónico.
     * @return El usuario encontrado.
     */
    public User findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    /**
     * Autentica al usuario verificando su nombre de usuario/correo y contraseña.
     *
     * @param usernameOrEmail El nombre de usuario o correo electrónico.
     * @param password        La contraseña en texto plano.
     * @return El usuario autenticado.
     */
    public User authenticate(String usernameOrEmail, String password) {
        User user = findByUsernameOrEmail(usernameOrEmail);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        logger.info("Usuario autenticado con éxito: {}", user.getUsername());
        return user;
    }

    /**
     * Implementación de la interfaz UserDetailsService para cargar un usuario por nombre de usuario o email.
     *
     * @param usernameOrEmail El nombre de usuario o email.
     * @return Un objeto UserDetails para Spring Security.
     * @throws UsernameNotFoundException Si el usuario no es encontrado.
     */

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el nombre o email: " + usernameOrEmail));

        // Construir y retornar un objeto UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name()) // Asegúrate de mapear correctamente los roles
                .build();
    }

}