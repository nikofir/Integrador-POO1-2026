# Memoria del Trabajo Integrador
## Sistema de Gestion Veterinaria (POO)

## 1. Introduccion y objetivo

El presente trabajo consiste en el desarrollo de una aplicacion de escritorio
para la gestion de una clinica veterinaria. El sistema permite administrar
clientes y sus mascotas, veterinarios con sus especialidades, un catalogo de
servicios (consultas medicas, peluqueria, guarderia por dia y aplicacion de
vacunas), y la reserva y seguimiento de turnos.

El objetivo academico es aplicar los conceptos de Programacion Orientada a
Objetos: encapsulamiento, herencia, polimorfismo, composicion, interfaces,
patrones de diseno y arquitectura en capas, sobre una aplicacion real con
persistencia y una interfaz grafica.

## 2. Alcance funcional

- **Clientes:** alta, consulta, modificacion y baja (con cantidad de mascotas).
- **Mascotas:** alta con ficha unica generada automaticamente, modificacion,
  activacion/inactivacion. Cada mascota pertenece a un unico cliente.
- **Veterinarios:** alta (matricula unica), modificacion, gestion de
  especialidades y baja.
- **Vacunas:** catalogo con nombre comercial unico, enfermedad que previene y
  periodicidad en meses.
- **Servicios:** catalogo de los cuatro tipos soportados, con precio base y
  duracion; la guarderia ademas tiene cupo diario y la aplicacion de vacunas
  referencia una vacuna del catalogo.
- **Turnos:** reserva, confirmacion, atencion, cancelacion, alta de servicios
  adicionales y carga de registros medicos sobre turnos atendidos.
- **Reporte de ingresos:** suma de los turnos atendidos en un rango de fechas.

## 3. Arquitectura en capas

La aplicacion se organiza en capas con dependencias dirigidas hacia el interior
(la presentacion conoce los servicios, los servicios conocen los repositorios,
y los repositorios conocen las entidades):

```
Presentacion (controller + FXML/DTOs)
      |
      v
Servicios de negocio (service)
      |
      v
Persistencia (repository + JPA/Hibernate)
      |
      v
Modelo de dominio (entity + validator)
```

### Patrones utilizados

- **Metodo de fabrica:** todas las entidades se construyen mediante
  `entidad.crear(...)` con constructores privados. La validacion se ejecuta
  dentro de la fabrica; una entidad invalida nunca llega a existir.
- **Repository:** cada entidad tiene su repositorio JPA; todos heredan de
  `BaseRepository` las operaciones comunes (buscar, listar, guardar).
- **Service:** encapsula las reglas de negocio que requieren consultar la base
  (unicidad, solapamientos, cupos, vigencia de vacunas).
- **DTO:** las capas superiores trabajan con objetos de transferencia
  (`ClienteDto`, `MascotaDto`, ...) que no exponen las entidades JPA.
- **Singleton perezoso:** `JpaUtil` construye una unica `EntityManagerFactory`
  y ofrece `enTransaccion(...)` para ejecutar operaciones atomica.
- **Maquina de estados:** el estado de un turno se modela como transiciones
  explicitas (`confirmar`, `atender`, `cancelar`) que validan el estado actual.

## 4. Modelo de dominio

### Entidades principales

| Entidad            | Descripcion                                                        |
|--------------------|--------------------------------------------------------------------|
| `Cliente`          | Datos de contacto; compone sus mascotas (`orphanRemoval`).         |
| `Mascota`          | Ficha unica `M-AAAA-NNNN`, especie, raza, fecha de nacimiento.     |
| `Veterinario`      | Matricula unica y conjunto de especialidades (minimo 1).           |
| `Vacuna`           | Nombre comercial unico, enfermedad prevenida y periodicidad.       |
| `Servicio`         | Abstracto; precio base y duracion.                                 |
| `Turno`            | Relaciona mascota + veterinario + lineas de servicio, con estado.  |
| `TurnoServicio`    | Linea del turno; guarda el precio historico del servicio.          |
| `RegistroMedico`   | Diagnostico y tratamiento de un turno-servicio atendido.           |

### Herencia

`Servicio` usa **tabla unica** (SINGLE_TABLE) con un discriminador
`tipo_servicio` y cuatro subclases:

- `ConsultaMedica` y `Peluqueria`: sin atributos adicionales.
- `GuarderiaDia`: agrega `cupo_maximo` (cupo diario).
- `AplicacionVacuna`: agrega la referencia a una `Vacuna`.

La herencia permite tratar polimorficamente a los servicios en el catalogo y
en las lineas de turno (por ejemplo, al sumar duraciones o precios).

### Composicion y relaciones

- `Cliente 1 -- 0..* Mascota` (composicion: la mascota vive con su cliente).
- `Turno 1 -- 0..* TurnoServicio` (composicion de lineas).
- `Turno * -- 1 Mascota` y `Turno * -- 1 Veterinario`.
- `TurnoServicio * -- 1 Servicio` y `TurnoServicio 1 -- 0..1 RegistroMedico`.

## 5. Reglas de negocio

Todas las reglas se validan ya sea en el dominio (fabricas y metodos) o en la
capa de servicios (cuando requieren consultas):

1. **Cliente:** DNI de 6 a 8 digitos unico; nombre y apellido solo letras
   (con tildes), espacios, puntos, apostrofos y guiones; email con formato
   valido; telefono numerico de 6 a 15 digitos.
2. **Mascota:** ficha unica con formato `M-AAAA-NNNN`, generada por el sistema
   (`M-2026-0001`, ...); la fecha de nacimiento no puede ser futura.
3. **Veterinario:** matricula numerica unica (hasta 10 caracteres) y al menos
   una especialidad; no puede quedarse sin especialidades.
4. **Vacuna:** nombre comercial unico; periodicidad mayor a cero.
5. **Turno (reserva):** la fecha/hora debe ser futura; la mascota debe estar
   activa; el turno no puede solaparse con otros del mismo veterinario ni de
   la misma mascota; la guarderia respeta el cupo diario; una vacuna no se
   reaplica mientras la dosis anterior siga vigente.
6. **Turno (estados):** `PENDIENTE -> CONFIRMADO`, `PENDIENTE -> CANCELADO`,
   `CONFIRMADO -> ATENDIDO` y `CONFIRMADO -> CANCELADO`. Un turno PENDIENTE se
   cancela cuando sea; uno CONFIRMADO solo con mas de 24 hs de anticipacion.
7. **Turno (servicios):** no se agregan ni quitan servicios a turnos en estado
   terminal; el precio se congela al reservar (precio historico).
8. **Registro medico:** solo puede cargarse en turnos ATENDIDOS, una vez por
   turno-servicio.

## 6. Capa de persistencia

- **JPA/Hibernate** sobre PostgreSQL (en las pruebas, H2 en modo PostgreSQL).
- `hibernate.hbm2ddl.auto=update`: el esquema se crea/ajusta en la primera
  ejecucion. Las restricciones de unicidad estan declaradas en las entidades
  (`uniqueConstraints`) como red de seguridad adicional a la validacion.
- Cada repositorio expone consultas tipadas especificas:
  `listarCandidatosSolapados`, `contarGuarderiaEnDia`, `ultimaAplicacionVacuna`,
  `sumarIngresos`, entre otras.
- `JpaUtil.enTransaccion(...)` centraliza la apertura de transaccion, el
  commit/rollback y el cierre del `EntityManager`.

## 7. Capa de servicios

Cada servicio valida la regla de negocio correspondiente y devuelve DTOs:

| Servicio            | Responsabilidades destacadas                                        |
|---------------------|---------------------------------------------------------------------|
| `ClienteService`    | Alta con DNI unico, listado con cantidad de mascotas, baja.         |
| `MascotaService`    | Generacion de ficha secuencial, alta/edicion, activar/inactivar.    |
| `VeterinarioService`| Matricula unica y reconciliacion de especialidades.                 |
| `VacunaService`     | Nombre comercial unico.                                             |
| `ServicioService`   | Creacion de los 4 tipos, edicion y cupo de guarderia.               |
| `TurnoService`      | Agenda (solapamientos, cupos, vigencia), estados, registros, ingresos. |

Por ejemplo, `TurnoService.crearTurno(...)` verifica en la misma transaccion:
mascota activa, fecha futura, ausencia de solapamientos del veterinario y de
la mascota, cupo de guarderia y vigencia de vacunas, antes de persistir.

## 8. Presentacion (JavaFX)

- Interfaz FXML con un menu lateral (`MainController`) que carga las vistas en
  un `StackPane`: clientes, mascotas, veterinarios, vacunas, servicios, turnos
  e ingresos.
- Cada vista tiene su controlador y sus DTOs; las operaciones de negocio se
  invocan contra los servicios dentro de un bloque que muestra los errores del
  dominio mediante dialogs (`Alertas`).
- Hoja de estilos `estilo.css` para una apariencia uniforme.
- `VeterinariaApp` carga `principal.fxml` y cierra la fabrica de persistencia
  al salir (`stop()`).

## 9. Pruebas

La suite tiene **135 tests** organizados en tres niveles:

1. **Dominio:** fabricas y validadores (datos validos, invalidos y limites).
2. **Persistencia:** repositorios contra H2 en memoria (modo PostgreSQL).
3. **Servicios:** reglas de negocio end-to-end (solapamientos, cupos,
   vigencia de vacunas, transiciones de estado, ingresos).

Los tests comparten una base H2 en memoria que se regenera por metodo,
garantizando aislamiento y repetibilidad.

## 10. Manual de usuario (resumen)

1. **Clientes:** boton *Nuevo cliente*; el listado muestra la cantidad de
   mascotas por cliente.
2. **Mascotas:** *Nueva mascota* pide el cliente y genera la ficha
   automaticamente; se pueden editar o inactivar.
3. **Veterinarios:** alta con matricula y una o mas especialidades.
4. **Vacunas:** alta de vacunas del catalogo.
5. **Servicios:** alta de cada tipo; para guarderia se pide el cupo y para
   vacunas la vacuna asociada.
6. **Turnos:** se selecciona dia, mascota, veterinario y uno o mas servicios.
   Botones para confirmar, atender, cancelar y registrar consulta.
7. **Ingresos:** rango de fechas y total de turnos atendidos.

## 11. Limitaciones y mejoras futuras

- La conexion a PostgreSQL se configura manualmente en `database.properties`;
  una mejora seria un asistente de configuracion o credenciales cifradas.
- No hay autenticacion ni roles de usuario.
- El reporte de ingresos es simple; podria ampliarse con graficos y filtros
  por veterinario o servicio.
- La numeracion de fichas asume reinicio anual; podria soportar configuracion.
- Pueden incorporarse notificaciones de turnos y recordatorios de vacunas.
