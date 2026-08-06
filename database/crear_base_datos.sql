-- =============================================================
-- Sistema de Gestion Veterinaria
-- Creacion de la base de datos (PostgreSQL 18)
-- =============================================================
--
-- Ejecutar desde la raiz del proyecto:
--
--   & "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -h localhost -f database\crear_base_datos.sql
--
-- Se solicitara la contrasena del usuario postgres.
-- El esquema (tablas) lo crea Hibernate automaticamente al iniciar
-- la aplicacion (hibernate.hbm2ddl.auto=update).
-- =============================================================

SELECT 'CREATE DATABASE veterinaria_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'veterinaria_db')\gexec

\echo 'Base de datos veterinaria_db lista (si no existia, fue creada).'
