# Sistema de Gestion Veterinaria

Trabajo integrador de Programacion Orientada a Objetos (POO).

Aplicacion de escritorio JavaFX para la gestion de una clinica veterinaria:
clientes, mascotas, veterinarios, servicios (consultas, peluqueria, guarderia
por dia y aplicacion de vacunas) y turnos, con persistencia real en
PostgreSQL mediante JPA/Hibernate.

## Requisitos

- JDK 17 o superior (probado con Temurin JDK 25).
- Maven 3.9+.
- PostgreSQL (probado con PostgreSQL 18) o simplemente ejecutar los tests
  (que usan H2 en memoria, sin base instalada).

## Estructura del proyecto

```
src/main/java/com/veterinaria
├── entity/          Modelo de dominio (Cliente, Mascota, Veterinario,
│                    Servicio y subclases, Vacuna, Turno, RegistroMedico...)
├── validator/       Reglas de validacion de cada entidad
├── exception/       Jerarquia de excepciones del dominio
├── repository/      Acceso a datos (BaseRepository + 6 repositorios JPA)
├── service/         Servicios de negocio (6 servicios)
├── dto/             Objetos de transferencia para la interfaz
├── controller/      Controladores JavaFX (FXML)
├── util/            JpaUtil: EntityManagerFactory + transacciones
└── VeterinariaApp   Punto de entrada (Application)

src/main/resources
├── database.properties   Configuracion de conexion (PostgreSQL en ejecucion)
└── vistas/               Vistas FXML + hoja de estilos

src/test/java/com/veterinaria   135 tests (dominio + persistencia + servicios)

database/
├── crear_base_datos.sql   Crea la base veterinaria_db
└── seed_datos.sql         Datos de ejemplo (opcional)
```

## Compilacion y tests

```bash
mvn clean test
```

Los tests corren contra una base H2 en memoria (modo PostgreSQL) y no
requieren PostgreSQL instalado. Estado esperado: **135 tests, 0 fallos**.

## Configuracion de la base de datos

1. Crear la base de datos:

```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -h localhost -f database\crear_base_datos.sql
```

2. Editar `src/main/resources/database.properties` con las credenciales reales:

```properties
hibernate.connection.url=jdbc:postgresql://localhost:5432/veterinaria_db
hibernate.connection.username=postgres
hibernate.connection.password=TU_CONTRASENA
```

> El esquema (tablas) lo crea Hibernate automaticamente en la primera
> ejecucion (`hibernate.hbm2ddl.auto=update`).

3. (Opcional) Cargar datos de ejemplo despues de la primera ejecucion:

```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -h localhost -d veterinaria_db -f database\seed_datos.sql
```

## Ejecucion

```bash
mvn javafx:run
```

La ventana principal ofrece un menu lateral con las secciones: Clientes,
Mascotas, Veterinarios, Vacunas, Servicios, Turnos, Historial e Ingresos.

## Arquitectura

El proyecto sigue una arquitectura en capas:

- **Dominio** (`entity`): entidades JPA con fabricas, validadores propios y
  maquinas de estado (por ejemplo, `Turno`: PENDIENTE -> CONFIRMADO -> ATENDIDO).
- **Validacion** (`validator`): reglas aplicadas siempre antes de construir
  o modificar una entidad (ej. DNI de 6 a 8 digitos, ficha `M-AAAA-NNNN`).
- **Persistencia** (`repository`): repositorios JPA con consultas tipadas.
- **Negocio** (`service`): reglas que requieren la base (unicidad,
  solapamientos de turnos, cupo diario de guarderia, vigencia de vacunas).
- **Presentacion** (`controller` + FXML): interfaz JavaFX que consume DTOs.

### Reglas de negocio principales

- La ficha de la mascota es unica y se genera como `M-AAAA-NNNN`.
- La matricula del veterinario y el DNI del cliente son unicos.
- Un turno se reserva sobre fecha futura y no puede solaparse con otro turno
  del mismo veterinario ni de la misma mascota.
- La guarderia por dia respeta un cupo maximo diario.
- Una vacuna no se reaplica mientras la dosis anterior siga vigente.
- Un turno PENDIENTE se cancela cuando sea; uno CONFIRMADO solo con mas de
  24 hs de anticipacion.
- El registro medico solo se carga en turnos ATENDIDOS.

## Diagrama de clases

Ver `docs/diagrama_clases.mmd` (Mermaid). Puede renderizarse en
[mermaid.live](https://mermaid.live) o con la extension Mermaid del editor.

## Entregables academicos

- `docs/diagrama_clases.mmd`: diagrama de clases UML.
- `docs/decisiones_diseno.md`: documento de decisiones de diseno (max. 2 paginas).
- `docs/memoria.md`: memoria del trabajo (arquitectura, reglas, pruebas, manual).
