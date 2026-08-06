-- =============================================================
-- Sistema de Gestion Veterinaria
-- Datos de ejemplo (seed)
-- =============================================================
--
-- Ejecutar DESPUES de la primera puesta en marcha de la aplicacion,
-- cuando Hibernate ya creo el esquema en veterinaria_db:
--
--   & "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -h localhost -d veterinaria_db -f database\seed_datos.sql
--
-- Los INSERT son idempotentes (ON CONFLICT DO NOTHING) y al final se
-- reajustan las secuencias para que la aplicacion pueda seguir creando
-- registros sin colisiones de identificadores.
-- =============================================================

BEGIN;

-- ------------------------------------------------------------------
-- Vacunas del catalogo
-- ------------------------------------------------------------------
INSERT INTO vacunas (id, nombre_comercial, enfermedad_prevenida, periodicidad_meses) VALUES
    (1, 'Nobivac Rabia', 'Rabia', 12),
    (2, 'Vanguard Plus', 'Parvovirus canino', 12),
    (3, 'Feligen', 'Leucemia felina', 12)
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------------
-- Servicios (herencia SINGLE_TABLE: tipo_servicio distingue la subclase)
-- ------------------------------------------------------------------
INSERT INTO servicios (id, tipo_servicio, nombre, precio_base, duracion_minutos, cupo_maximo, vacuna_id) VALUES
    (1, 'CONSULTA_MEDICA',    'Consulta general',               8000.00,  30, NULL, NULL),
    (2, 'CONSULTA_MEDICA',    'Consulta especializada',        12000.00,  45, NULL, NULL),
    (3, 'PELUQUERIA',         'Bano y corte',                  15000.00,  60, NULL, NULL),
    (4, 'GUARDERIA_DIA',      'Guarderia por dia',             18000.00, 480,   10, NULL),
    (5, 'APLICACION_VACUNA',  'Aplicacion antirrabica',         9000.00,  20, NULL, 1),
    (6, 'APLICACION_VACUNA',  'Aplicacion parvovirus',          9000.00,  20, NULL, 2)
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------------
-- Clientes
-- ------------------------------------------------------------------
INSERT INTO clientes (id, dni, nombre, apellido, telefono, email, domicilio) VALUES
    (1, '30111222', 'Ana Maria', 'Garcia',    '1155550101', 'ana.garcia@correo.com',   'Av. Siempre Viva 123'),
    (2, '28999111', 'Carlos',    'Perez',     '1144440202', 'carlos.perez@correo.com',  'Calle Falsa 456'),
    (3, '27111900', 'Lucia',     'Fernandez', '1133330303', 'lucia.fernandez@correo.com', NULL)
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------------
-- Veterinarios y sus especialidades (cada uno con al menos una)
-- ------------------------------------------------------------------
INSERT INTO veterinarios (id, matricula, nombre, apellido) VALUES
    (1, '1001', 'Laura',  'Martinez'),
    (2, '1002', 'Jorge',  'Sanchez'),
    (3, '1003', 'Maria',  'Lopez')
ON CONFLICT (id) DO NOTHING;

INSERT INTO veterinario_especialidades (veterinario_id, especialidad) VALUES
    (1, 'CLINICA_GENERAL'), (1, 'CARDIOLOGIA'),
    (2, 'CIRUGIA'),         (2, 'ODONTOLOGIA'),
    (3, 'DERMATOLOGIA'),    (3, 'NUTRICION')
ON CONFLICT (veterinario_id, especialidad) DO NOTHING;

-- ------------------------------------------------------------------
-- Mascotas
-- ------------------------------------------------------------------
INSERT INTO mascotas (id, ficha, especie, nombre, raza, fecha_nacimiento, activa, cliente_id) VALUES
    (1, 'M-2026-0001', 'PERRO', 'Rocky', 'Labrador',  '2020-03-15', TRUE,  1),
    (2, 'M-2026-0002', 'GATO',  'Michi', 'Siames',    '2022-07-01', TRUE,  1),
    (3, 'M-2026-0003', 'PERRO', 'Luna',  'Caniche',   '2021-11-20', TRUE,  2),
    (4, 'M-2026-0004', 'AVE',   'Piolin','Canario',   '2023-01-10', TRUE,  3),
    (5, 'M-2026-0005', 'ROEDOR','Coco',  'Hamster',   '2024-05-05', TRUE,  3),
    (6, 'M-2026-0006', 'GATO',  'Simba', 'Comun',     '2019-09-09', FALSE, 2)
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------------
-- Turnos
--   id 1,2,3,5: futuros (PENDIENTE / CONFIRMADO / CANCELADO)
--   id 4: pasado y ATENDIDO (genera ingreso y registro medico)
-- ------------------------------------------------------------------
INSERT INTO turnos (id, mascota_id, veterinario_id, estado, fecha_hora, fecha_creacion) VALUES
    (1, 1, 1, 'CONFIRMADO', ((now() + interval '2 days')::date + time '10:00'), now()),
    (2, 2, 1, 'PENDIENTE',  ((now() + interval '3 days')::date + time '11:30'), now()),
    (3, 3, 2, 'CONFIRMADO', ((now() + interval '1 days')::date + time '16:00'), now()),
    (4, 4, 1, 'ATENDIDO',   ((now() - interval '5 days')::date + time '09:00'), now() - interval '6 days'),
    (5, 5, 3, 'CANCELADO',  ((now() + interval '4 days')::date + time '15:00'), now())
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------------
-- Registros medicos (del turno 4, ya atendido)
-- ------------------------------------------------------------------
INSERT INTO registros_medicos (id, diagnostico, tratamiento, fecha) VALUES
    (1, 'Infeccion respiratoria leve', 'Antibiotico oral por 7 dias y reposo', now() - interval '5 days')
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------------
-- Lineas de turno (precio historico = instantanea del precio base)
-- ------------------------------------------------------------------
INSERT INTO turno_servicios (id, turno_id, servicio_id, precio_historico, registro_medico_id) VALUES
    (1, 1, 1, 8000.00, NULL),
    (2, 2, 3, 15000.00, NULL),
    (3, 3, 1, 8000.00, NULL),
    (4, 4, 1, 8000.00, 1),
    (5, 5, 6, 9000.00, NULL)
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------------
-- Reajuste de secuencias (columnas de identidad)
-- ------------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('vacunas', 'id'),               GREATEST((SELECT COALESCE(MAX(id), 0) FROM vacunas),               1));
SELECT setval(pg_get_serial_sequence('servicios', 'id'),             GREATEST((SELECT COALESCE(MAX(id), 0) FROM servicios),             1));
SELECT setval(pg_get_serial_sequence('clientes', 'id'),              GREATEST((SELECT COALESCE(MAX(id), 0) FROM clientes),              1));
SELECT setval(pg_get_serial_sequence('veterinarios', 'id'),          GREATEST((SELECT COALESCE(MAX(id), 0) FROM veterinarios),          1));
SELECT setval(pg_get_serial_sequence('mascotas', 'id'),              GREATEST((SELECT COALESCE(MAX(id), 0) FROM mascotas),              1));
SELECT setval(pg_get_serial_sequence('turnos', 'id'),                GREATEST((SELECT COALESCE(MAX(id), 0) FROM turnos),                1));
SELECT setval(pg_get_serial_sequence('registros_medicos', 'id'),     GREATEST((SELECT COALESCE(MAX(id), 0) FROM registros_medicos),     1));
SELECT setval(pg_get_serial_sequence('turno_servicios', 'id'),       GREATEST((SELECT COALESCE(MAX(id), 0) FROM turno_servicios),       1));

COMMIT;

\echo 'Datos de ejemplo cargados.'
