package com.universidad.biblioteca.config;

import com.universidad.biblioteca.entity.Rol;
import com.universidad.biblioteca.entity.Usuario;
import com.universidad.biblioteca.entity.enums.EstadoUsuario;
import com.universidad.biblioteca.entity.enums.RolNombre;
import com.universidad.biblioteca.repository.RolRepository;
import com.universidad.biblioteca.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Inicializa datos criticos de forma idempotente al arrancar: crea los roles
 * si no existen y un usuario ADMIN inicial solo si aun no hay ninguno.
 * La contrasena del admin proviene de variables de entorno (app.admin.*),
 * nunca hardcodeada en el codigo.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public DataInitializer(RolRepository rolRepository,
                           UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           AppProperties appProperties) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Rol rolAdmin = obtenerOCrearRol(RolNombre.ROLE_ADMIN);
        obtenerOCrearRol(RolNombre.ROLE_USER);
        crearAdminSiNoExiste(rolAdmin);
    }

    private Rol obtenerOCrearRol(RolNombre nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> rolRepository.save(new Rol(nombre)));
    }

    private void crearAdminSiNoExiste(Rol rolAdmin) {
        if (usuarioRepository.countByRoles_Nombre(RolNombre.ROLE_ADMIN) > 0) {
            return;
        }
        String email = appProperties.admin().email();
        Usuario admin = Usuario.builder()
                .nombre("Administrador")
                .apellido("Biblioteca")
                .email(email)
                .password(passwordEncoder.encode(appProperties.admin().password()))
                .estado(EstadoUsuario.ACTIVO)
                .roles(Set.of(rolAdmin))
                .build();
        usuarioRepository.save(admin);
        log.info("Usuario ADMIN inicial creado con email '{}'. Cambie la contrasena tras el primer acceso.", email);
    }
}
