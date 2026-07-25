# CLAUDE.md — Proyecto "Consúltenos"

Guia de contexto para Claude Code. Lee esto antes de escribir o modificar codigo.

---

## 1. Que es este proyecto

**Consúltenos** es una mesa de ayuda B2B para soporte de hardware corporativo.
Es la Evaluacion 4 de "Taller de Desarrollo de Aplicaciones" — INACAP Calama 2026.
Cliente del caso: **ServiAndina SpA**. Entrega final: **25 de julio de 2026**.

**Restriccion clave del caso:** el ingreso de tickets es EXCLUSIVAMENTE telefonico.
Un agente registra la llamada. NO hay portal de autoservicio para el cliente.

Flujo: usuario del cliente llama -> agente registra ticket -> supervisor lo asigna
a un tecnico segun ambito -> tecnico resuelve -> supervisor consulta reportes.

---

## 2. Stack — NO cambiar

| Componente     | Herramienta                         |
|----------------|-------------------------------------|
| Lenguaje       | Java                                |
| Interfaz       | Java Swing (aplicacion de ESCRITORIO) |
| IDE            | Apache NetBeans                     |
| Base de datos  | MySQL / MariaDB via XAMPP           |
| Acceso a datos | JDBC (PreparedStatement)            |
| Admin BD       | DBeaver                             |
| Versiones      | Git / GitHub                        |

REGLA ABSOLUTA: este es un proyecto Java Swing de escritorio.
NO es una aplicacion web. No generes HTML, CSS ni JavaScript.
No propongas Spring, Maven, Node ni frameworks: se compila con el build.xml de NetBeans (Ant).

---

## 3. Base de datos — cual usar

USA UNICAMENTE el esquema definido en `sql/01_crear_bd_consultenos.sql`.
Base: `consultenos_db`. Son **13 tablas normalizadas (3FN)**:

- Catalogos: `Rol`, `Ambito`, `Estado_Ticket`, `Grupo_Resolutor`
- Personal interno: `Empleado_Interno`
- Cliente y activos: `Empresa_Cliente`, `Sucursal`, `Usuario_Cliente`, `Dispositivo`
- Nucleo: `Ticket`
- Apoyo: `Historial_Ticket`, `Alerta`, `Reporte`

NO uses ni menciones estos esquemas antiguos o de otros integrantes:
- El de la Evaluacion 3 (tablas EMPRESAS, USUARIOS, EMPLEADOS en mayuscula) — es la BD ANTIGUA.
- Una BD llamada `soporte_tickets` con tablas `tickets` / `historial_tickets` — es de otro
  compañero, en otro stack (web) y otro modelo. NO se usa.

Nombres de tablas y columnas: respeta EXACTAMENTE los del script 01 (singular, guion bajo).
Las consultas base por historia de usuario ya estan en `sql/02_consultas_consultenos.sql`;
usalas como punto de partida de cada DAO.

### Decisiones de diseño que hay que respetar (y que el alumno debe poder defender)
1. `Ticket` guarda solo `id_ambito`, NO `id_grupo`. El grupo resolutor se deduce por JOIN
   (Ticket -> Ambito -> Grupo_Resolutor -> Empleado_Interno). Evita redundancia derivable. Es 3FN.
2. Un solo `Empleado_Interno` + catalogo `Rol` (no una tabla por rol).
3. `Dispositivo` cuelga de `id_sucursal` (esta fisicamente en una sucursal).
4. Estado como FK a `Estado_Ticket`, no texto suelto.
5. `Ticket` usa `id_usuario` (quien reporta); la empresa se llega via Usuario -> Sucursal -> Empresa.

---

## 4. Estructura y patron de capas

```
src/
  modelo/   Clases que representan entidades (Empleado, Ticket, Usuario, Dispositivo)  [HECHO]
  dao/      Acceso a datos: aqui vive TODO el SQL (EmpleadoDAO ya existe)              [PARCIAL]
  vista/    Formularios Swing (lo que ve el usuario)                                   [VACIO]
  util/     Conexion.java (conexion JDBC centralizada)                                 [HECHO]
sql/        01_crear_bd + 02_consultas
docs/       Modelo fisico draw.io, guias
```

Patron obligatorio para CADA funcionalidad nueva: **modelo + dao + vista**.
- La vista NO sabe SQL. El DAO NO sabe de botones. Respetalo siempre.
- Todo acceso a BD pasa por `util.Conexion.obtener()` y se cierra (usa try-with-resources).
- Todo SQL con parametros usa `PreparedStatement` con `?` (nunca concatenar strings — inyeccion SQL).
- El molde a imitar es `dao/EmpleadoDAO.java`. Copia su estilo (try-with-resources, mapeo
  fila -> objeto, uso de `wasNull()` para columnas nullable, metodo `main` de prueba).

---

## 5. Que falta (orden sugerido)

1. `vista/Login.java` — usa `EmpleadoDAO.login()` (ya funciona) y abre un menu segun el rol.
2. `dao/TicketDAO.java` — insertar, listar activos, buscar/filtrar, asignar tecnico, cambiar estado.
3. `vista/RegistroTicket.java` — con autocompletado al ingresar el codigo de usuario (HU-15).
4. `vista/Asignacion.java` — supervisor asigna tecnico segun el ambito del ticket (HU-04).
5. `vista/ListadoTickets.java` — tickets activos (HU-16).
6. `vista/Historial.java` — historial y busqueda con filtros por texto/estado/fecha (HU-06/07/08).
7. Reporte basico de gestion / MTTR (HU-11).
8. Menu principal / clase lanzadora por rol.

Prioriza el camino demostrable de punta a punta antes que pulir. Las HU de baja prioridad
(alertas, recordatorios, auditoria completa, multi-cliente) estan DISEÑADAS en la BD pero
NO se implementan; no las programes salvo que se pida.

---

## 6. Punto sensible del login

Hoy `EmpleadoDAO.login()` compara `contrasena_hash = ?` contra el texto plano que se escribe.
Funciona SOLO porque los datos demo guardan valores tipo `hash_demo_super` como si fueran hash.
NO afirmes que las contraseñas estan hasheadas: hoy es un hash simulado para la demo.
Si en algun momento se pide seguridad real, el login debe traer el hash por usuario y compararlo
con `BCrypt.checkpw()`, no con `=`. Manten el codigo y el discurso alineados con esto.

---

## 7. Convenciones de codigo

- Comentarios en ESPAÑOL, **sin tildes ni ñ** dentro de comentarios de codigo Java
  (evita problemas de encoding). El texto visible al usuario en la UI si puede llevar tildes.
- Codigo EXPLICADO: el alumno tiene defensa oral y el profesor pregunta el porque de cada cosa.
  Cada clase y cada metodo no trivial lleva un comentario que explique QUE hace y POR QUE se hizo asi.
  No entregues codigo que el alumno no pueda defender.
- Branding INACAP si se estiliza la UI: rojo #E30514, navy #1F3864, fuente Calibri/Tahoma. Sobrio.
- Trabaja paso a paso. Antes de generar mucho de golpe, confirma el enfoque.

---

## 8. Git

- Repo: github.com/Amarosen00/consultenos
- Ramas por funcionalidad (feature/login, feature/tickets, feature/listados). Nunca directo en main.
- `git pull` antes de empezar cada dia.


## Alcance funcional
El detalle de QUE construir (pantallas y metodos por DAO) esta en docs/IMPLEMENTACION.md.
Consultalo antes de construir cada modulo. Cubre las 12 historias de usuario de
prioridad alta, incluidas HU-06 (historial por cliente) y HU-07 (historial por
dispositivo), que se resuelven como filtros dentro de ListadoTickets.