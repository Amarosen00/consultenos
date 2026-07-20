-- ============================================================================
--  CONSÚLTENOS — Mesa de Ayuda B2B
--  Script 2: CONSULTAS POR HISTORIA DE USUARIO
--
--  Motor      : MySQL / MariaDB (XAMPP)
--  Requisito  : haber ejecutado antes '01_crear_bd_consultenos.sql'
--
--  PROPÓSITO:
--    Cada consulta corresponde a una historia de usuario (HU) del proyecto.
--    Sirven para (a) probar la base de datos y (b) como base de las sentencias
--    que se usarán desde Java con JDBC (PreparedStatement).
--
--  NOTA: donde aparece un valor fijo (por ejemplo, id_usuario = 1), en Java
--        se reemplaza por un parámetro '?' del PreparedStatement.
-- ============================================================================

USE consultenos_db;

-- ============================================================================
--  HU-12 — LOGIN CON ROLES DIFERENCIADOS
-- ============================================================================

-- Validar credenciales y obtener el rol del empleado que inicia sesión.
-- En Java: WHERE usuario = ? AND contrasena_hash = ?
SELECT e.id_empleado,
       e.nombre_completo,
       r.nombre_rol,
       e.id_grupo
FROM Empleado_Interno e
INNER JOIN Rol r ON e.id_rol = r.id_rol
WHERE e.usuario         = 'supervisor'
  AND e.contrasena_hash = 'hash_demo_super'
  AND e.estado_activo   = 1;

-- Listar todo el personal interno con su rol (pantalla de administración).
SELECT e.id_empleado,
       e.nombre_completo,
       r.nombre_rol,
       COALESCE(g.nombre_grupo, 'Sin grupo') AS grupo,
       CASE e.estado_activo WHEN 1 THEN 'Activo' ELSE 'Inactivo' END AS estado
FROM Empleado_Interno e
INNER JOIN Rol r             ON e.id_rol   = r.id_rol
LEFT  JOIN Grupo_Resolutor g ON e.id_grupo = g.id_grupo
ORDER BY r.nombre_rol, e.nombre_completo;

-- ============================================================================
--  HU-15 — FORMULARIO GUIADO: AUTOCOMPLETAR AL INGRESAR CÓDIGO DE USUARIO
-- ============================================================================

-- Al escribir el código de usuario, traer sus datos + sucursal + empresa.
-- En Java: WHERE u.codigo_usuario = ?
SELECT u.id_usuario,
       u.codigo_usuario,
       u.nombre_completo,
       u.telefono,
       s.id_sucursal,
       s.nombre_sucursal,
       s.direccion,
       ec.razon_social
FROM Usuario_Cliente u
INNER JOIN Sucursal        s  ON u.id_sucursal = s.id_sucursal
INNER JOIN Empresa_Cliente ec ON s.id_empresa  = ec.id_empresa
WHERE u.codigo_usuario = 'Mjopi001';

-- Dispositivos disponibles en la sucursal del usuario (combo del formulario).
-- En Java: WHERE d.id_sucursal = ?
SELECT d.id_dispositivo,
       d.numero_serie,
       d.tipo_hardware,
       d.modelo
FROM Dispositivo d
WHERE d.id_sucursal = 1
ORDER BY d.tipo_hardware;

-- ============================================================================
--  HU-01 / HU-02 — REGISTRO Y CLASIFICACIÓN DEL TICKET
-- ============================================================================

-- Insertar un ticket nuevo. La fecha y hora se capturan automáticamente.
-- En Java los 9 valores van como parámetros '?'.
INSERT INTO Ticket (id_usuario, id_dispositivo, id_ambito, id_estado,
                    id_agente_creador, prioridad, naturaleza,
                    lugar_fisico_exacto, descripcion_problema)
VALUES (1, 2, 4, 1, 2, 'Media', 'Logica', 'Oficina 305, tercer piso',
        'El equipo se reinicia solo al abrir el sistema de inventario.');

-- Recuperar el id del ticket recién creado (Java: getGeneratedKeys()).
SELECT LAST_INSERT_ID() AS id_ticket_creado;

-- Combos para clasificar: ámbitos disponibles.
SELECT id_ambito, nombre_ambito FROM Ambito ORDER BY nombre_ambito;

-- ============================================================================
--  HU-04 — ASIGNACIÓN MANUAL POR SUPERVISOR
-- ============================================================================

-- Tickets abiertos esperando asignación (panel de despacho del supervisor).
SELECT t.id_ticket,
       t.fecha_hora_creacion,
       t.prioridad,
       a.nombre_ambito,
       u.nombre_completo AS reporta,
       s.nombre_sucursal,
       LEFT(t.descripcion_problema, 60) AS resumen
FROM Ticket t
INNER JOIN Estado_Ticket  e ON t.id_estado    = e.id_estado
INNER JOIN Ambito         a ON t.id_ambito    = a.id_ambito
INNER JOIN Usuario_Cliente u ON t.id_usuario  = u.id_usuario
INNER JOIN Sucursal       s ON u.id_sucursal  = s.id_sucursal
WHERE e.nombre_estado = 'Abierto'
ORDER BY FIELD(t.prioridad,'Alta','Media','Baja'), t.fecha_hora_creacion;

-- Técnicos disponibles del ámbito del ticket.
-- Aquí se ve la ruta Ticket → Ambito → Grupo_Resolutor → Empleado_Interno,
-- que es la razón por la que Ticket NO guarda id_grupo (evita redundancia).
-- En Java: WHERE t.id_ticket = ?
SELECT emp.id_empleado,
       emp.nombre_completo,
       g.nombre_grupo,
       a.nombre_ambito
FROM Ticket t
INNER JOIN Ambito           a   ON t.id_ambito  = a.id_ambito
INNER JOIN Grupo_Resolutor  g   ON g.id_ambito  = a.id_ambito
INNER JOIN Empleado_Interno emp ON emp.id_grupo = g.id_grupo
INNER JOIN Rol              r   ON emp.id_rol   = r.id_rol
WHERE t.id_ticket     = 1
  AND r.nombre_rol    = 'Tecnico'
  AND emp.estado_activo = 1;

-- Asignar el ticket al técnico y pasarlo a "En progreso".
-- En Java: SET id_tecnico_asignado = ? WHERE id_ticket = ?
UPDATE Ticket
SET id_tecnico_asignado = 5,
    id_estado = (SELECT id_estado FROM Estado_Ticket WHERE nombre_estado = 'En progreso')
WHERE id_ticket = 1;

-- ============================================================================
--  HU-05 / HU-16 — BANDEJA DEL TÉCNICO: TICKETS ACTIVOS
-- ============================================================================

-- Tickets asignados al técnico que inició sesión (Abierto o En progreso).
-- En Java: WHERE t.id_tecnico_asignado = ?
SELECT t.id_ticket,
       t.fecha_hora_creacion,
       t.prioridad,
       e.nombre_estado,
       a.nombre_ambito,
       s.nombre_sucursal,
       t.lugar_fisico_exacto,
       d.tipo_hardware,
       LEFT(t.descripcion_problema, 60) AS resumen
FROM Ticket t
INNER JOIN Estado_Ticket   e ON t.id_estado      = e.id_estado
INNER JOIN Ambito          a ON t.id_ambito      = a.id_ambito
INNER JOIN Usuario_Cliente u ON t.id_usuario     = u.id_usuario
INNER JOIN Sucursal        s ON u.id_sucursal    = s.id_sucursal
INNER JOIN Dispositivo     d ON t.id_dispositivo = d.id_dispositivo
WHERE t.id_tecnico_asignado = 5
  AND e.nombre_estado IN ('Abierto','En progreso')
ORDER BY FIELD(t.prioridad,'Alta','Media','Baja'), t.fecha_hora_creacion;

-- ============================================================================
--  HU-03 — FLUJO DE ESTADOS DEL TICKET
-- ============================================================================

-- Marcar un ticket como Resuelto. El comentario de resolución es obligatorio.
-- En Java: SET comentario_resolucion = ? WHERE id_ticket = ?
UPDATE Ticket
SET id_estado = (SELECT id_estado FROM Estado_Ticket WHERE nombre_estado = 'Resuelto'),
    comentario_resolucion = 'Se limpio el rodillo de arrastre y se recalibro la bandeja.',
    fecha_hora_cierre = CURRENT_TIMESTAMP
WHERE id_ticket = 1;

-- Cerrar un ticket resuelto (paso final del ciclo de vida).
UPDATE Ticket
SET id_estado = (SELECT id_estado FROM Estado_Ticket WHERE nombre_estado = 'Cerrado')
WHERE id_ticket = 1
  AND id_estado = (SELECT id_estado FROM Estado_Ticket WHERE nombre_estado = 'Resuelto');

-- Ver el detalle completo de un ticket (pantalla de detalle).
-- En Java: WHERE t.id_ticket = ?
SELECT t.id_ticket,
       ec.razon_social,
       s.nombre_sucursal,
       u.codigo_usuario,
       u.nombre_completo   AS reporta,
       d.numero_serie,
       d.tipo_hardware,
       d.modelo,
       a.nombre_ambito,
       t.naturaleza,
       t.prioridad,
       est.nombre_estado,
       t.lugar_fisico_exacto,
       t.descripcion_problema,
       t.comentario_resolucion,
       ag.nombre_completo   AS registrado_por,
       tec.nombre_completo  AS atendido_por,
       t.fecha_hora_creacion,
       t.fecha_hora_cierre
FROM Ticket t
INNER JOIN Usuario_Cliente  u   ON t.id_usuario          = u.id_usuario
INNER JOIN Sucursal         s   ON u.id_sucursal         = s.id_sucursal
INNER JOIN Empresa_Cliente  ec  ON s.id_empresa          = ec.id_empresa
INNER JOIN Dispositivo      d   ON t.id_dispositivo      = d.id_dispositivo
INNER JOIN Ambito           a   ON t.id_ambito           = a.id_ambito
INNER JOIN Estado_Ticket    est ON t.id_estado           = est.id_estado
INNER JOIN Empleado_Interno ag  ON t.id_agente_creador   = ag.id_empleado
LEFT  JOIN Empleado_Interno tec ON t.id_tecnico_asignado = tec.id_empleado
WHERE t.id_ticket = 3;

-- ============================================================================
--  HU-06 — HISTORIAL DE TICKETS POR CLIENTE
-- ============================================================================

-- Todos los tickets de una empresa cliente (cadena Empresa → Sucursal → Usuario).
-- En Java: WHERE ec.id_empresa = ?
SELECT t.id_ticket,
       t.fecha_hora_creacion,
       s.nombre_sucursal,
       u.nombre_completo AS reporta,
       a.nombre_ambito,
       est.nombre_estado,
       t.prioridad
FROM Ticket t
INNER JOIN Usuario_Cliente u   ON t.id_usuario  = u.id_usuario
INNER JOIN Sucursal        s   ON u.id_sucursal = s.id_sucursal
INNER JOIN Empresa_Cliente ec  ON s.id_empresa  = ec.id_empresa
INNER JOIN Ambito          a   ON t.id_ambito   = a.id_ambito
INNER JOIN Estado_Ticket   est ON t.id_estado   = est.id_estado
WHERE ec.id_empresa = 1
ORDER BY t.fecha_hora_creacion DESC;

-- Resumen de tickets por sucursal (para ver dónde se concentran las fallas).
SELECT s.nombre_sucursal,
       COUNT(t.id_ticket) AS total_tickets,
       SUM(CASE WHEN est.nombre_estado = 'Cerrado' THEN 1 ELSE 0 END) AS cerrados
FROM Sucursal s
LEFT JOIN Usuario_Cliente u   ON u.id_sucursal = s.id_sucursal
LEFT JOIN Ticket          t   ON t.id_usuario  = u.id_usuario
LEFT JOIN Estado_Ticket   est ON t.id_estado   = est.id_estado
GROUP BY s.id_sucursal, s.nombre_sucursal
ORDER BY total_tickets DESC;

-- ============================================================================
--  HU-07 — HISTORIAL DE TICKETS POR DISPOSITIVO
-- ============================================================================

-- Historial completo de fallas de un equipo (por número de serie).
-- En Java: WHERE d.numero_serie = ?
SELECT t.id_ticket,
       t.fecha_hora_creacion,
       t.fecha_hora_cierre,
       a.nombre_ambito,
       t.naturaleza,
       est.nombre_estado,
       t.descripcion_problema,
       t.comentario_resolucion
FROM Ticket t
INNER JOIN Dispositivo   d   ON t.id_dispositivo = d.id_dispositivo
INNER JOIN Ambito        a   ON t.id_ambito      = a.id_ambito
INNER JOIN Estado_Ticket est ON t.id_estado      = est.id_estado
WHERE d.numero_serie = 'SN-IMP-00123'
ORDER BY t.fecha_hora_creacion DESC;

-- Equipos con más fallas (detección de patrones).
SELECT d.numero_serie,
       d.tipo_hardware,
       d.modelo,
       s.nombre_sucursal,
       COUNT(t.id_ticket) AS total_fallas
FROM Dispositivo d
INNER JOIN Sucursal s ON d.id_sucursal    = s.id_sucursal
LEFT  JOIN Ticket   t ON t.id_dispositivo = d.id_dispositivo
GROUP BY d.id_dispositivo, d.numero_serie, d.tipo_hardware, d.modelo, s.nombre_sucursal
HAVING total_fallas > 0
ORDER BY total_fallas DESC;

-- ============================================================================
--  HU-08 — BÚSQUEDA Y FILTRADO DE TICKETS
-- ============================================================================

-- Búsqueda con filtros combinados.
-- En Java se arma dinámicamente; los NULL simulan "sin filtro".
-- Patrón: (? IS NULL OR campo = ?)
SELECT t.id_ticket,
       t.fecha_hora_creacion,
       est.nombre_estado,
       t.prioridad,
       a.nombre_ambito,
       s.nombre_sucursal,
       COALESCE(tec.nombre_completo, 'Sin asignar') AS tecnico
FROM Ticket t
INNER JOIN Estado_Ticket    est ON t.id_estado           = est.id_estado
INNER JOIN Ambito           a   ON t.id_ambito           = a.id_ambito
INNER JOIN Usuario_Cliente  u   ON t.id_usuario          = u.id_usuario
INNER JOIN Sucursal         s   ON u.id_sucursal         = s.id_sucursal
LEFT  JOIN Empleado_Interno tec ON t.id_tecnico_asignado = tec.id_empleado
WHERE (est.nombre_estado = 'En progreso' OR 'En progreso' IS NULL)
  AND (t.prioridad       = 'Alta'        OR 'Alta'        IS NULL)
ORDER BY t.fecha_hora_creacion DESC;

-- Filtrar por rango de fechas.
-- En Java: BETWEEN ? AND ?
SELECT t.id_ticket,
       t.fecha_hora_creacion,
       est.nombre_estado,
       t.prioridad
FROM Ticket t
INNER JOIN Estado_Ticket est ON t.id_estado = est.id_estado
WHERE t.fecha_hora_creacion BETWEEN '2026-07-01 00:00:00' AND '2026-07-31 23:59:59'
ORDER BY t.fecha_hora_creacion DESC;

-- Búsqueda por texto en la descripción del problema.
-- En Java: WHERE t.descripcion_problema LIKE ?  (parámetro: '%texto%')
SELECT t.id_ticket,
       t.fecha_hora_creacion,
       LEFT(t.descripcion_problema, 80) AS resumen
FROM Ticket t
WHERE t.descripcion_problema LIKE '%impresora%'
ORDER BY t.fecha_hora_creacion DESC;

-- ============================================================================
--  HU-11 — REPORTES BÁSICOS DE GESTIÓN
-- ============================================================================

-- Conteo de tickets por estado (tarjetas del panel de reportes).
SELECT est.nombre_estado,
       COUNT(t.id_ticket) AS cantidad
FROM Estado_Ticket est
LEFT JOIN Ticket t ON t.id_estado = est.id_estado
GROUP BY est.id_estado, est.nombre_estado
ORDER BY est.id_estado;

-- MTTR: tiempo promedio de resolución en horas (métrica clave).
SELECT ROUND(AVG(TIMESTAMPDIFF(MINUTE, t.fecha_hora_creacion, t.fecha_hora_cierre)) / 60, 2)
           AS horas_promedio_resolucion,
       COUNT(*) AS tickets_considerados
FROM Ticket t
WHERE t.fecha_hora_cierre IS NOT NULL;

-- Reporte consolidado del periodo (abiertos, cerrados y MTTR juntos).
SELECT 'Julio 2026' AS periodo,
       SUM(CASE WHEN est.nombre_estado IN ('Abierto','En progreso') THEN 1 ELSE 0 END)
           AS tickets_abiertos,
       SUM(CASE WHEN est.nombre_estado IN ('Resuelto','Cerrado')    THEN 1 ELSE 0 END)
           AS tickets_cerrados,
       ROUND(AVG(TIMESTAMPDIFF(MINUTE, t.fecha_hora_creacion, t.fecha_hora_cierre)) / 60, 2)
           AS tiempo_promedio_horas
FROM Ticket t
INNER JOIN Estado_Ticket est ON t.id_estado = est.id_estado
WHERE t.fecha_hora_creacion BETWEEN '2026-07-01' AND '2026-07-31 23:59:59';

-- Carga de trabajo por técnico.
SELECT emp.nombre_completo AS tecnico,
       g.nombre_grupo,
       COUNT(t.id_ticket)  AS tickets_asignados,
       SUM(CASE WHEN est.nombre_estado IN ('Resuelto','Cerrado') THEN 1 ELSE 0 END) AS resueltos
FROM Empleado_Interno emp
INNER JOIN Rol             r   ON emp.id_rol = r.id_rol
LEFT  JOIN Grupo_Resolutor g   ON emp.id_grupo = g.id_grupo
LEFT  JOIN Ticket          t   ON t.id_tecnico_asignado = emp.id_empleado
LEFT  JOIN Estado_Ticket   est ON t.id_estado = est.id_estado
WHERE r.nombre_rol = 'Tecnico'
GROUP BY emp.id_empleado, emp.nombre_completo, g.nombre_grupo
ORDER BY tickets_asignados DESC;

-- Tickets por ámbito técnico (dónde se concentra la demanda).
SELECT a.nombre_ambito,
       COUNT(t.id_ticket) AS total
FROM Ambito a
LEFT JOIN Ticket t ON t.id_ambito = a.id_ambito
GROUP BY a.id_ambito, a.nombre_ambito
ORDER BY total DESC;

-- ============================================================================
--  HU-14 — GESTIÓN DE EMPRESAS Y SUCURSALES (baja prioridad)
-- ============================================================================

-- Registrar una sucursal nueva.
-- En Java: VALUES (?, ?, ?)
INSERT INTO Sucursal (id_empresa, nombre_sucursal, direccion)
VALUES (1, 'Sucursal El Loa', 'Av. Granaderos 1520, Calama');

-- Listar empresas con su cantidad de sucursales y usuarios.
SELECT ec.razon_social,
       ec.rut_empresa,
       COUNT(DISTINCT s.id_sucursal) AS sucursales,
       COUNT(DISTINCT u.id_usuario)  AS usuarios
FROM Empresa_Cliente ec
LEFT JOIN Sucursal        s ON s.id_empresa  = ec.id_empresa
LEFT JOIN Usuario_Cliente u ON u.id_sucursal = s.id_sucursal
GROUP BY ec.id_empresa, ec.razon_social, ec.rut_empresa;

-- ============================================================================
--  HU-13 — AUDITORÍA DE CAMBIOS (baja prioridad)
-- ============================================================================

-- Registrar una acción en el historial.
-- En Java: VALUES (?, ?, ?)
INSERT INTO Historial_Ticket (id_ticket, id_empleado, accion)
VALUES (1, 4, 'Ticket reasignado al tecnico Luis Fernandez');

-- Trazabilidad completa de un ticket.
-- En Java: WHERE h.id_ticket = ?
SELECT h.fecha_hora_accion,
       emp.nombre_completo AS responsable,
       r.nombre_rol,
       h.accion
FROM Historial_Ticket h
INNER JOIN Empleado_Interno emp ON h.id_empleado = emp.id_empleado
INNER JOIN Rol              r   ON emp.id_rol    = r.id_rol
WHERE h.id_ticket = 3
ORDER BY h.fecha_hora_accion;

-- ============================================================================
--  HU-09 / HU-10 — ALERTAS Y RECORDATORIOS (baja prioridad)
-- ============================================================================

-- Tickets abiertos sin técnico asignado (recordatorio al supervisor).
SELECT t.id_ticket,
       t.fecha_hora_creacion,
       TIMESTAMPDIFF(HOUR, t.fecha_hora_creacion, NOW()) AS horas_sin_asignar,
       t.prioridad
FROM Ticket t
INNER JOIN Estado_Ticket est ON t.id_estado = est.id_estado
WHERE t.id_tecnico_asignado IS NULL
  AND est.nombre_estado = 'Abierto'
ORDER BY horas_sin_asignar DESC;

-- Generar alertas para tickets que exceden el umbral definido (ejemplo: 4 horas).
-- El umbral (SLA) debe definirse como regla de negocio.
INSERT INTO Alerta (id_ticket, tipo_alerta)
SELECT t.id_ticket, 'Ticket abierto por mas de 4 horas'
FROM Ticket t
INNER JOIN Estado_Ticket est ON t.id_estado = est.id_estado
WHERE est.nombre_estado = 'Abierto'
  AND TIMESTAMPDIFF(HOUR, t.fecha_hora_creacion, NOW()) > 4
  AND NOT EXISTS (
      SELECT 1 FROM Alerta al
      WHERE al.id_ticket = t.id_ticket
        AND al.estado_alerta = 'Activa'
  );

-- Alertas activas.
SELECT al.id_alerta,
       al.id_ticket,
       al.tipo_alerta,
       al.fecha_generada,
       t.prioridad
FROM Alerta al
INNER JOIN Ticket t ON al.id_ticket = t.id_ticket
WHERE al.estado_alerta = 'Activa'
ORDER BY al.fecha_generada DESC;

-- ============================================================================
--  CONSULTAS DE VERIFICACIÓN DE INTEGRIDAD
-- ============================================================================

-- Ver todas las claves foráneas definidas (útil para la defensa).
SELECT TABLE_NAME          AS tabla,
       COLUMN_NAME         AS columna,
       CONSTRAINT_NAME     AS restriccion,
       REFERENCED_TABLE_NAME  AS referencia_tabla,
       REFERENCED_COLUMN_NAME AS referencia_columna
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'consultenos_db'
  AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME, COLUMN_NAME;

-- Probar que la integridad referencial funciona.
-- Esta sentencia DEBE fallar con error de FK (ámbito 99 no existe).
-- Descomentar solo para demostrar la validación:
-- INSERT INTO Ticket (id_usuario, id_dispositivo, id_ambito, id_estado,
--                     id_agente_creador, prioridad, naturaleza,
--                     lugar_fisico_exacto, descripcion_problema)
-- VALUES (1, 1, 99, 1, 2, 'Alta', 'Fisica', 'Prueba', 'Prueba de integridad');

-- ============================================================================
--  FIN DEL SCRIPT DE CONSULTAS
-- ============================================================================
