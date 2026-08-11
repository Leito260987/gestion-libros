-- ===========================================================================
-- Esquema relacional - Biblioteca Universitaria (MySQL 8)
-- Referencia de entrega. En ejecucion, Hibernate gestiona el esquema
-- (ddl-auto) y DataInitializer inserta roles + admin de forma idempotente.
-- ===========================================================================

CREATE DATABASE IF NOT EXISTS biblioteca_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE biblioteca_db;

-- ---------- Rol ----------
CREATE TABLE IF NOT EXISTS rol (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL,
    CONSTRAINT uk_rol_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

-- ---------- Usuario ----------
CREATE TABLE IF NOT EXISTS usuario (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(80)  NOT NULL,
    apellido       VARCHAR(80)  NOT NULL,
    email          VARCHAR(120) NOT NULL,
    password       VARCHAR(100) NOT NULL,
    estado         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    fecha_registro DATETIME     NOT NULL,
    CONSTRAINT uk_usuario_email UNIQUE (email),
    INDEX idx_usuario_email (email),
    INDEX idx_usuario_estado (estado)
) ENGINE=InnoDB;

-- ---------- Usuario_Rol (N:M) ----------
CREATE TABLE IF NOT EXISTS usuario_rol (
    usuario_id BIGINT NOT NULL,
    rol_id     BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_rol_rol     FOREIGN KEY (rol_id)     REFERENCES rol (id)     ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------- Libro ----------
CREATE TABLE IF NOT EXISTS libro (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn                VARCHAR(20)  NOT NULL,
    titulo              VARCHAR(200) NOT NULL,
    autor               VARCHAR(150) NOT NULL,
    editorial           VARCHAR(150),
    categoria           VARCHAR(80),
    anio_publicacion    INT,
    cantidad_total      INT NOT NULL,
    cantidad_disponible INT NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    estado              VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    CONSTRAINT uk_libro_isbn UNIQUE (isbn),
    CONSTRAINT chk_libro_cantidades CHECK (cantidad_total >= 0
        AND cantidad_disponible >= 0 AND cantidad_disponible <= cantidad_total),
    INDEX idx_libro_isbn (isbn),
    INDEX idx_libro_titulo (titulo),
    INDEX idx_libro_autor (autor),
    INDEX idx_libro_categoria (categoria)
) ENGINE=InnoDB;

-- ---------- Prestamo ----------
CREATE TABLE IF NOT EXISTS prestamo (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id        BIGINT NOT NULL,
    libro_id          BIGINT NOT NULL,
    fecha_prestamo    DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    fecha_devolucion  DATE,
    estado            VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT fk_prestamo_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_prestamo_libro   FOREIGN KEY (libro_id)   REFERENCES libro (id),
    INDEX idx_prestamo_usuario (usuario_id),
    INDEX idx_prestamo_libro (libro_id),
    INDEX idx_prestamo_estado (estado),
    INDEX idx_prestamo_vencimiento (fecha_vencimiento)
) ENGINE=InnoDB;
