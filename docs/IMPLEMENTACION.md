# ESPECIFICACIÓN DE IMPLEMENTACIÓN — Sistema "Consúltenos"

> Documento de referencia de QUÉ construir. No reemplaza el `CLAUDE.md`;
> es un complemento que describe el alcance funcional pendiente y el estado actual.
> Stack: Java + Swing + NetBeans + MySQL/MariaDB (XAMPP) + JDBC.
>
> **Versión revisada:** se integraron HU-06 e HU-07 (historial por cliente y por
> dispositivo), que estaban comprometidas como prioridad ALTA pero no aparecían
> aterrizadas en una pantalla. También se aclararon la gestión del administrador
> (solo lectura), la interpretación de HU-05 y el uso de FlatLaf.

---

## REGLAS DEL PROYECTO (respetar siempre)

1. **Arquitectura en capas.** Nunca mezclar SQL con interfaz:
   - `modelo/` → clases de datos (solo atributos + get/set).
   - `dao/` → TODO el SQL. Cada DAO usa `Conexion.obtener()` y `PreparedStatement`.
   - `vista/` → formularios Swing. NO contienen SQL; llaman a los DAO.
   - `util/` → `Conexion.java` (ya existe, no modificar).
2. **Seguridad SQL:** siempre `PreparedStatement` con `?`, nunca concatenar strings en el SQL.
3. **Recursos:** usar `try-with-resources` para cerrar conexiones automáticamente.
4. **Idioma:** código y comentarios en español, sin tildes en comentarios de código.
5. **Comentarios explicativos:** el código debe estar comentado porque hay defensa oral.
6. **Nulos:** campos que pueden ser NULL en la BD (id_grupo, id_tecnico_asignado,
   fecha_cierre) usan `Integer`/comprobación con `rs.wasNull()`, no `int`.
7. **No romper lo que ya funciona:** `Conexion.java`, los 4 modelos y `EmpleadoDAO`
   ya están probados. Reutilizarlos, no reescribirlos.

---

## ESTADO ACTUAL

### YA IMPLEMENTADO Y PROBADO ✅
- `util/Conexion.java` — conexión JDBC (CONEXION EXITOSA confirmado)
- `modelo/Empleado.java`, `Ticket.java`, `Usuario.java`, `Dispositivo.java`
- `dao/EmpleadoDAO.java` con 3 métodos:
  - `login(usuario, contrasena)` → devuelve `Empleado` o `null`
  - `listarTecnicosPorAmbito(idAmbito)` → `List<Empleado>`
  - `listarTodos()` → `List<Empleado>`

### POR IMPLEMENTAR ⬜
Vistas (todas) + DAOs de tickets, usuarios y datos base + clases modelo faltantes.

---

## PANTALLAS A CONSTRUIR (vista/)

### 1. Login  `vista/Login.java`  — HU-12  [PRIORIDAD 1]
- Campos: usuario, contraseña (JPasswordField).
- Botón "Ingresar" → llama `EmpleadoDAO.login()`.
- Si es correcto: abre el MenuPrincipal pasando el `Empleado` logueado.
- Si es incorrecto: `JOptionPane` con "Usuario o contraseña incorrectos".
- Credenciales de prueba: `supervisor` / `hash_demo_super`.

### 2. Menú Principal  `vista/MenuPrincipal.java`  — HU-12
- Recibe el `Empleado` logueado y muestra su nombre y rol.
- Menú con botones/opciones que cambian **según el rol**:
  - Agente: Registrar ticket, Ver tickets.
  - Supervisor: todo lo anterior + Asignar, Reportes.
  - Técnico: Mis tickets asignados.
  - Administrador: todo + Gestión de personal/sucursales.
- **Nota (alcance):** la opción "Gestión de personal/sucursales" del administrador
  se implementa como **SOLO LECTURA** (listar personal con `EmpleadoDAO.listarTodos`
  y listar sucursales). Crear/editar/eliminar personal o sucursales queda FUERA de
  esta entrega, para no abrir alcance nuevo a estas alturas.

### 3. Registro de Ticket  `vista/RegistroTicket.java`  — HU-01, HU-02, HU-15
- Campo código de usuario → al salir del campo, autocompletar nombre,
  sucursal y empresa (usar `UsuarioDAO.buscarPorCodigo`).
- Combo de dispositivos de esa sucursal (`DispositivoDAO.listarPorSucursal`).
- Combo de ámbito (Impresion, Radiofrecuencia, Redes, Computadores, Telefonos).
- Combo de prioridad (Alta/Media/Baja) y naturaleza (Fisica/Logica).
- Campo lugar físico y descripción del problema.
- Botón "Registrar" → `TicketDAO.insertar()`. El estado inicial es "Abierto".

### 4. Listado de Tickets  `vista/ListadoTickets.java`  — HU-08, HU-16, HU-06, HU-07
- JTable con: id, fecha, estado, prioridad, ámbito, cliente/usuario, sucursal, técnico.
- Filtros combinables:
  - por **estado** (HU-08, HU-16),
  - por **prioridad** (HU-08),
  - por **cliente** (HU-06): filtra los tickets del cliente. Como hoy opera una sola
    empresa (ServiAndina), el filtro útil y demostrable es por **sucursal** o por
    **usuario que reporta** → usa `TicketDAO.listarPorCliente(...)`.
  - por **dispositivo** (HU-07): filtra por número de serie del equipo afectado
    → usa `TicketDAO.listarPorDispositivo(numeroSerie)`.
  - (opcional por rango de fecha).
- Doble clic en fila → abre el detalle del ticket.
- **Por qué aquí y no en pantallas nuevas:** HU-06 (historial por cliente) e HU-07
  (historial por dispositivo) son, en el fondo, el mismo listado con otro filtro.
  Se resuelven agregando estos dos criterios de filtrado a esta misma pantalla,
  evitando duplicar interfaz.

### 5. Asignación  `vista/AsignacionTicket.java`  — HU-04, HU-05
- Lista de tickets en estado "Abierto" sin técnico.
- Al seleccionar uno, combo con técnicos del ámbito del ticket
  (`EmpleadoDAO.listarTecnicosPorAmbito` — YA EXISTE).
- Botón "Asignar" → `TicketDAO.asignarTecnico()`; el ticket pasa a "En progreso".
- **Nota sobre HU-05 (notificación al técnico):** en una app de escritorio no hay
  notificación push real. La "notificación" se materializa en que el técnico ve el
  ticket recién asignado en su bandeja "Mis tickets asignados"
  (`TicketDAO.listarPorTecnico`). No se implementa correo ni aviso emergente.

### 6. Detalle / Resolución  `vista/DetalleTicket.java`  — HU-03
- Muestra todos los datos del ticket (con JOINs).
- Si el usuario es técnico y el ticket está "En progreso":
  campo comentario de resolución + botón "Marcar como Resuelto"
  (`TicketDAO.resolver()`, setea fecha_cierre y estado "Resuelto").
- Botón "Cerrar" para pasar de "Resuelto" a "Cerrado".
- **Opcional:** mostrar el historial del ticket (lista de eventos de
  `Historial_Ticket`) como tabla de solo lectura. Los datos ya vienen cargados por
  el script de poblamiento; esta pantalla los MUESTRA, no los genera (la escritura
  de auditoría es HU-13, fuera de alcance).

### 7. Reportes  `vista/Reportes.java`  — HU-11
- Conteo de tickets por estado.
- MTTR (tiempo promedio de resolución en horas).
- Tickets por técnico y por ámbito.
- (Consultas ya escritas en `sql/02_consultas_consultenos.sql`, sección HU-11.)

---

## DAOs A CONSTRUIR (dao/)

### `dao/TicketDAO.java`  [PRIORIDAD 2 — el más importante]
Métodos necesarios:
- `insertar(Ticket t)` → INSERT, devuelve el id generado (getGeneratedKeys).
- `listar(filtroEstado, filtroPrioridad)` → `List<Ticket>` con JOINs.
- `listarPorTecnico(idTecnico)` → `List<Ticket>` (bandeja del técnico).
- `listarAbiertosSinAsignar()` → `List<Ticket>` (para asignación).
- `buscarPorId(idTicket)` → `Ticket` con todos los JOINs (detalle).
- `asignarTecnico(idTicket, idTecnico)` → UPDATE, estado a "En progreso".
- `resolver(idTicket, comentario)` → UPDATE, estado "Resuelto" + fecha_cierre.
- `cambiarEstado(idTicket, nombreEstado)` → UPDATE genérico.
- `listarPorCliente(...)` → `List<Ticket>` (HU-06). Historial de tickets de un
  cliente. Ruta de JOIN: `Ticket → Usuario_Cliente → Sucursal → Empresa_Cliente`.
  Parametrizable por id de empresa, de sucursal o de usuario según lo que se filtre
  en la pantalla; con un solo cliente activo, el filtro por sucursal/usuario es el
  más demostrable.
- `listarPorDispositivo(numeroSerie)` → `List<Ticket>` (HU-07). Historial de todos
  los tickets asociados a un equipo. JOIN `Ticket → Dispositivo` y filtro por
  `numero_serie`.

### `dao/UsuarioDAO.java`
- `buscarPorCodigo(codigo)` → `Usuario` con sucursal y empresa (JOIN).
- `listarPorSucursal(idSucursal)` → `List<Usuario>`.

### `dao/DispositivoDAO.java`
- `listarPorSucursal(idSucursal)` → `List<Dispositivo>`.
- `buscarPorSerie(numeroSerie)` → `Dispositivo` (apoyo a HU-07).

### `dao/CatalogoDAO.java` (para llenar los combos)
- `listarAmbitos()`, `listarEstados()`, `listarSucursales()`.
- Devuelven listas simples (id + nombre) para los JComboBox.

---

## CLASES MODELO FALTANTES (modelo/) — solo si se necesitan
Muchos catálogos se pueden manejar con id+nombre sin clase propia. Crear solo si aporta:
- `Sucursal.java`, `Empresa.java` (si el detalle lo requiere).
- `Reporte.java` (para la pantalla de reportes).
Las existentes (Empleado, Ticket, Usuario, Dispositivo) ya cubren lo esencial.

---

## MODELO DE DATOS (referencia rápida)

13 tablas. Relaciones clave:
- `Empresa_Cliente` 1─N `Sucursal` 1─N `Usuario_Cliente` 1─N `Ticket`
- `Sucursal` 1─N `Dispositivo` 1─N `Ticket`
- `Ambito` 1─N `Grupo_Resolutor` 1─N `Empleado_Interno`
- `Ambito` 1─N `Ticket`  (el grupo se deduce por JOIN, NO se guarda en Ticket)
- `Estado_Ticket` 1─N `Ticket`
- `Empleado_Interno` como agente_creador y como tecnico_asignado en `Ticket`
- `Rol` 1─N `Empleado_Interno`

**Estados del ticket:** Abierto → En progreso → Resuelto → Cerrado
**Roles:** Administrador, Agente, Supervisor, Tecnico

---

## UI / UX (HU-15) — mantener simple pero prolijo
- Paleta coherente: rojo institucional `#E30514`, navy `#1F3864`, grises de fondo.
- Sugerencia OPCIONAL: usar FlatLaf (look and feel moderno) con 2-3 líneas en el main.
  Ojo: FlatLaf es una **librería externa**; para usarla hay que agregar su `.jar` a
  `lib/` y a las librerías del proyecto en NetBeans, igual que se hizo con el conector
  JDBC. Si no se quiere otra dependencia, Swing estándar con la paleta de colores basta.
- Formularios con campos alineados y etiquetas claras.
- Botón principal destacado en cada pantalla.
- No buscar diseño avanzado; que se vea limpio y ordenado.

---

## ORDEN DE CONSTRUCCIÓN RECOMENDADO
1. Login + MenuPrincipal (con el EmpleadoDAO ya existente).
2. TicketDAO + CatalogoDAO + UsuarioDAO + DispositivoDAO.
3. RegistroTicket (usa todos los DAO anteriores).
4. ListadoTickets + DetalleTicket (incluye los filtros HU-06 y HU-07).
5. AsignacionTicket.
6. Reportes.
7. Pulido de UI/UX transversal.

## COBERTURA DE HISTORIAS DE USUARIO (prioridad ALTA)
Se implementan las 12 comprometidas: HU-01, HU-02, HU-03, HU-04, HU-05 (ver nota),
HU-06, HU-07, HU-08, HU-11, HU-12, HU-15, HU-16.

## FUERA DE ALCANCE (NO implementar)
- HU-09 (alertas), HU-10 (recordatorios), HU-13 (auditoría/escritura de historial),
  HU-14 (multi-cliente completo): diseñadas pero NO se implementan en esta entrega.
- Gestión (alta/edición/baja) de personal y sucursales: solo lectura (ver Menú Principal).
- Exportación a CSV/Excel y estadísticas semanales: descartadas.
