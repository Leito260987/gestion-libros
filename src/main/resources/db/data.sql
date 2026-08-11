-- ===========================================================================
-- Datos iniciales de referencia (MySQL).
-- Los roles y el admin tambien se crean automaticamente por DataInitializer;
-- este script sirve como documentacion y para cargas manuales de catalogo.
-- ===========================================================================
USE biblioteca_db;

-- Roles (idempotente)
INSERT INTO rol (nombre) SELECT 'ROLE_ADMIN'
    WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'ROLE_ADMIN');
INSERT INTO rol (nombre) SELECT 'ROLE_USER'
    WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'ROLE_USER');

-- Catalogo de ejemplo
INSERT INTO libro (isbn, titulo, autor, editorial, categoria, anio_publicacion, cantidad_total, cantidad_disponible, version, estado) VALUES
 ('9780134494166', 'Clean Architecture',              'Robert C. Martin', 'Prentice Hall', 'Ingenieria de Software', 2017, 5, 5, 0, 'DISPONIBLE'),
 ('9780132350884', 'Clean Code',                      'Robert C. Martin', 'Prentice Hall', 'Ingenieria de Software', 2008, 4, 4, 0, 'DISPONIBLE'),
 ('9780201633610', 'Design Patterns',                 'Erich Gamma',      'Addison-Wesley','Ingenieria de Software', 1994, 3, 3, 0, 'DISPONIBLE'),
 ('9781617294945', 'Spring in Action',                'Craig Walls',      'Manning',       'Java',                    2018, 6, 6, 0, 'DISPONIBLE'),
 ('9780596009205', 'Head First Design Patterns',      'Eric Freeman',     'O''Reilly',     'Java',                    2004, 2, 2, 0, 'DISPONIBLE');
