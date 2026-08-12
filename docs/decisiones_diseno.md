# Documento de Decisiones de Diseño
## Sistema de Gestión Veterinaria

### 1. Clases definidas y por qué

- **`Cliente` y `Mascota`**: núcleo de la atención. `Mascota` guarda su ficha
  única (`M-AAAA-NNNN`, generada por el sistema), especie, raza y nacimiento.
- **`Veterinario`**: con matrícula única y un conjunto de especialidades
  (invariante mínimo 1). Las especialidades son un `enum` (valores fijos).
- **`Vacuna`**: catálogo con nombre comercial único, enfermedad que previene y
  periodicidad en meses.
- **`Servicio` (abstracta) con `ConsultaMedica`, `Peluqueria`,
  `GuarderiaDia` y `AplicacionVacuna`**: se eligió **herencia** porque cada
  tipo tiene comportamiento y datos propios (la guardería tiene cupo diario,
  la aplicación de vacuna referencia una vacuna) y todos comparten precio y
  duración. El polimorfismo permite tratar cualquier servicio igual al
  facturar o medir duraciones.
- **`Turno`**: agrega la mascota, el veterinario y las líneas de servicio;
  encapsula su **máquina de estados** (`PENDIENTE → CONFIRMADO → ATENDIDO`, con
  cancelación) y el cálculo de totales.
- **`TurnoServicio`**: línea de turno que **congela el precio** al momento de
  la reserva (precio histórico) y puede asociar un `RegistroMedico`.
- **`RegistroMedico`**: diagnóstico y tratamiento de una atención.
- **Enums**: `Especie`, `Especialidad`, `EstadoTurno`.
- **`exception`**: jerarquía propia (`ValidacionException`,
  `ReglaNegocioException`, `EntidadNoEncontradaException`,
  `PersistenciaException`).

### 2. Asociaciones y multiplicidades

- `Cliente 1 - 0..* Mascota` en **composición** (`orphanRemoval`): la mascota
  existe mientras exista su dueño y no se permite mascota sin cliente.
- `Turno 1 - 0..* TurnoServicio` en composición.
- `Turno * - 1 Mascota` y `Turno * - 1 Veterinario` (obligatorias).
- `TurnoServicio * - 1 Servicio` y `TurnoServicio 1 - 0..1 RegistroMedico`.
- `Veterinario 1 - 1..* Especialidad` (colección de valores).
- Los `fetch` se configuraron explícitamente (colecciones `LAZY`, referencias
  `EAGER`) y los `cascade` (`ALL`/`orphanRemoval`) sobre los agregados.

### 3. Dónde se ubicaron las reglas de negocio y por qué

Siguiendo la "regla de oro" del enunciado, las reglas que pueden vivir en el
dominio **están en las entidades**: fabricas que validan antes de construir
(`Cliente.crear`, `Turno.crear`), validadores por entidad (DNI, ficha,
matrícula, email), la máquina de estados (`confirmar/atender/cancelar` con el
plazo de 24 h) y la vigencia de vacunas (`Vacuna.estaVigente`). Las reglas que
**requieren consultas a la base** (solapamientos de turnos, cupo diario de
guardería, última aplicación de vacuna, unicidad) se implementan en la **capa
de servicios** (`TurnoService`, etc.), dentro de una única transacción.

### 4. Dificultades encontradas y su resolución

- **Solapamiento con duraciones**: se calcula en el servicio con la suma de
  duraciones del turno y una consulta previa de candidatos del veterinario y
  de la mascota (nunca se bordean solo por fecha/hora).
- **Herencia con tabla única**: las columnas de subclases (`cupo_maximo`,
  `vacuna_id`) debieron ser anulables para el resto de los servicios.
- **Ficha secuencial**: se resuelve con `MAX(ficha)` por año dentro de la
  misma transacción, evitando colisiones.
- **Transacciones**: `JpaUtil.enTransaccion` centraliza commit/rollback y
  cierre del `EntityManager`; los controladores nunca tocan JPA.

### 5. Funcionalidades implementadas y pendientes

**Implementadas**: clientes, mascotas (ficha automática, baja lógica),
veterinarios, vacunas, servicios (4 tipos), turnos (agenda, confirmar/atender/
cancelar, servicios, registro de consulta), historial médico por mascota con
filtros por tipo de servicio y rango de fechas, reporte de ingresos y control
de vacunaciones (alertas de vacunas vencidas o próximas a vencer en 30 días,
calculadas desde la última aplicación en turnos atendidos mediante
`Vacuna.proximaAplicacion` y `Vacuna.estadoAlerta`).

**Pendientes (sugeridas, no obligatorias)**: ninguna.

### 6. Configuración de la base de datos externa

Se usa PostgreSQL local. Crear la base con `database/crear_base_datos.sql` y
completar credenciales en `src/main/resources/database.properties`
(`hibernate.connection.url/username/password`). El esquema lo crea Hibernate
(`hbm2ddl.auto=update`). Datos de ejemplo opcionales: `database/seed_datos.sql`.
Los tests usan H2 en memoria (modo PostgreSQL), sin necesidad de servidor.
