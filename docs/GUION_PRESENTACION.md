# GUIÓN DE PRESENTACIÓN — Consúltenos

> Documento vivo: se actualiza a medida que se completen/retesteen requerimientos.
> Última actualización: Prioridad 7 (pulido UI/UX) recién construida, pendiente
> de retest completo en NetBeans antes de la defensa.

---

## 0. Antes de la demo

1. XAMPP encendido (Apache + MySQL, ambos en verde).
2. Base `consultenos_db` creada (`sql/01_crear_bd_consultenos.sql` ya ejecutado).
3. NetBeans abierto con el proyecto, **Clean and Build** hecho al menos una vez
   (para que tome `lib/mysql-connector-j.jar` y `lib/flatlaf-3.5.4.jar`).
4. `Run Project` debe abrir `Login` directamente (no `Conexion`).

## 1. Credenciales de prueba (datos semilla del script SQL)

| Usuario      | Contraseña           | Rol           | Nota                                    |
|--------------|-----------------------|---------------|------------------------------------------|
| `admin`      | `hash_demo_admin`     | Administrador | Ve todo + Gestión de Personal            |
| `agente1`    | `hash_demo_agente1`   | Agente        | Registra tickets                         |
| `agente2`    | `hash_demo_agente2`   | Agente        | Registra tickets                         |
| `supervisor` | `hash_demo_super`     | Supervisor    | Ve todo menos Gestión de Personal        |
| `tecnico1`   | `hash_demo_tec1`      | Tecnico       | Cristofer Tito Condori, grupo Impresion  |
| `tecnico2`   | `hash_demo_tec2`      | Tecnico       | Luis Fernandez Rojas, grupo Redes        |
| `tecnico3`   | `hash_demo_tec3`      | Tecnico       | Camila Soto Vega, grupo Computadores     |

Códigos de usuario cliente para autocompletar en Registro de Ticket:
`Mjopi001`, `Jrome002`, `Asilv003`, `Pmuno004`.

Nota: como se ha ido probando la app, los IDs y estados de los tickets de
ejemplo pueden haber cambiado respecto al script original. Si un paso dice
"un ticket Abierto sin técnico" y no hay ninguno, usa Registrar Ticket para
crear uno nuevo antes de continuar.

---

## 2. Guión paso a paso (orden de flujo de negocio real)

### Paso 1 — Login y menú por rol (HU-12)
1. Entra como `agente1`. El menú debe mostrar solo: Registrar Ticket, Ver Tickets.
2. Cierra sesión, entra como `supervisor`. Debe sumar: Asignación de Técnicos,
   Reportes de Gestión, Historial (extra), Tickets Activos (extra).
3. Cierra sesión, entra como `tecnico1`. Debe mostrar SOLO: Mis Tickets Asignados.
4. Cierra sesión, entra como `admin`. Debe sumar además: Gestión de Personal.
5. **Punto de defensa:** el menú es la misma clase para todos (`MenuPrincipal`),
   pero arma botones distintos leyendo `empleado.getNombreRol()`.

### Paso 2 — Registrar Ticket (HU-01, HU-02, HU-15)
1. Como `agente1` → "Registrar Ticket".
2. Escribe un código válido (ej. `Mjopi001`) y presiona "Buscar" (o Enter).
   Debe autocompletar nombre, sucursal y empresa, y cargar el combo de
   dispositivos de esa sucursal.
3. Prueba también un código inválido: debe avisar "Codigo no encontrado" y
   mantener el resto del formulario bloqueado.
4. Completa ámbito, prioridad, naturaleza, lugar y descripción → "Registrar Ticket".
5. Debe mostrar el ID generado y limpiar el formulario para la siguiente llamada.
6. **Punto de defensa:** el ticket siempre nace en estado "Abierto"
   (`TicketDAO.insertar()` lo fija, no depende de lo que traiga el objeto).

### Paso 3 — Ver Tickets / filtros (HU-08, HU-16, HU-06, HU-07)
1. Como `supervisor` → "Ver Tickets".
2. Prueba el combo Estado, el combo Prioridad, el combo Sucursal y el campo
   de N° de serie (ej. `SN-IMP-00123`), solos y combinados.
3. **Punto de defensa:** el filtro más específico (dispositivo o sucursal)
   consulta la BD; estado/prioridad se refinan en memoria sobre ese
   resultado ya acotado — así se combinan sin complicar el DAO.
4. Doble clic en una fila → abre `DetalleTicket` con todos los datos (JOIN
   completo: empresa, sucursal, dispositivo, agente creador, técnico, etc.).

### Paso 4 — Asignación de Técnicos (HU-04, HU-05)
1. Como `supervisor` → "Asignación de Técnicos".
2. Selecciona un ticket Abierto sin técnico → el combo se llena con los
   técnicos de ESE ámbito (`EmpleadoDAO.listarTecnicosPorAmbito`).
3. Asigna un técnico → debe pasar a "En progreso" y salir de la lista.
4. **Punto de defensa HU-05:** una app de escritorio no tiene notificación
   push real; la "notificación" es que el ticket aparece en la bandeja del
   técnico ("Mis Tickets Asignados") la próxima vez que la abra. El mensaje
   de confirmación lo dice explícitamente.

### Paso 5 — Resolver y cerrar (HU-03)
1. Cierra sesión, entra como el técnico dueño del ticket recién asignado.
2. "Mis Tickets Asignados" → debe aparecer autofiltrado a sus propios tickets.
3. Doble clic en el ticket "En progreso" → escribe un comentario de
   resolución → "Marcar como Resuelto".
4. Vuelve a abrir el mismo ticket (con cualquier rol) → botón "Cerrar Ticket"
   debe aparecer porque el estado es "Resuelto".
5. Revisa el historial de solo lectura al pie del detalle (opcional según
   el documento: esta pantalla lo MUESTRA, no lo genera).

### Paso 6 — Reportes de Gestión (HU-11)
1. Como `supervisor` o `admin` → "Reportes de Gestión".
2. Recorre las 4 pestañas: Por Estado, MTTR, Por Técnico, Por Ámbito.
3. **Punto de defensa:** las 4 consultas son exactamente las de
   `sql/02_consultas_consultenos.sql` sección HU-11, solo ejecutadas por JDBC.

### Paso 7 — Gestión de Personal (solo lectura)
1. Como `admin` → "Gestión de Personal".
2. Debe listar todo el personal interno.
3. **Punto de defensa:** reutiliza `EmpleadoDAO.listarTodos()`, que ya existía
   antes de este proyecto — no se agregó SQL nuevo para esta pantalla.

---

## 3. Funcionalidad EXTRA (fuera de las 12 HU comprometidas)

Mencionar solo si preguntan o si se quiere mostrar como valor agregado:

- **"Tickets Activos (extra)"** y **"Historial (extra)"**, visibles solo para
  Supervisor/Administrador. Equivalen a HU-13 (auditoría/escritura de
  historial), explícitamente marcada "fuera de alcance" en
  `docs/IMPLEMENTACION.md`. Se construyeron antes de conocer el documento y
  se mantuvieron por decisión explícita, claramente rotuladas como extra en
  el menú.
- Si el profesor pregunta "¿por qué esto no está en las 12 HU?": la
  respuesta corta es que se decidió dejarlas como bonus documentado, no
  como parte de la entrega comprometida.

---

## 4. Preguntas típicas de defensa oral (con respuesta corta)

- **¿Las contraseñas están hasheadas de verdad?** No — es un hash simulado
  para la demo (`contrasena_hash = ?` compara texto plano). Si se pidiera
  seguridad real, se cambiaría a `BCrypt.checkpw()`.
- **¿Por qué Ticket no guarda el grupo resolutor?** Se deduce por JOIN
  (Ticket → Ambito → Grupo_Resolutor → Empleado_Interno) para evitar
  redundancia derivable y respetar 3FN.
- **¿Por qué un solo Empleado_Interno y no una tabla por rol?** Rol es un
  catálogo (FK), no una tabla separada — más simple y normalizado.
- **¿Cómo se evita la inyección SQL?** Todo el SQL usa `PreparedStatement`
  con `?`, nunca concatenación de strings.
- **¿Por qué MenuPrincipal muestra botones distintos por rol?** Lee
  `empleado.getNombreRol()` y arma el sidebar condicionalmente — no hay
  lógica de permisos en la base, es solo la capa de vista decidiendo qué
  mostrar.

---

## 5. Estado de cobertura (HU comprometidas)

- [x] HU-01/02 Registro de ticket — RegistroTicket
- [x] HU-03 Detalle y flujo de estados — DetalleTicket
- [x] HU-04/05 Asignación — AsignacionTicket
- [x] HU-06/07 Historial por cliente/dispositivo — filtros en ListadoTickets
- [x] HU-08/16 Búsqueda y listado activos — ListadoTickets
- [x] HU-11 Reportes — Reportes
- [x] HU-12 Login con roles — Login + MenuPrincipal
- [x] HU-15 Autocompletado por código — RegistroTicket

Fuera de alcance (por diseño, no pendiente): HU-09, HU-10, HU-13, HU-14,
CRUD de personal/sucursales, exportación CSV/Excel.

## 6. Pendiente antes de la defensa

- [ ] Retest completo en NetBeans después del rediseño de Prioridad 7
  (sidebar + FlatLaf + EstilosUI) — confirmar que ninguna pantalla quedó
  rota visualmente o funcionalmente.
- [ ] Confirmar que `lib/flatlaf-3.5.4.jar` está bien enlazado en
  Properties → Libraries (mismo chequeo que se hizo con el conector JDBC).
