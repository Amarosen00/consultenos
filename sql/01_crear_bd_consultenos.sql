-- ============================================================================
--  CONSÚLTENOS — Mesa de Ayuda B2B para soporte de hardware corporativo
--  Script 1: CREACIÓN DE LA BASE DE DATOS + DATOS DE PRUEBA
--
--  Motor      : MySQL / MariaDB (XAMPP)
--  Herramienta: DBeaver
--  Proyecto   : Taller de Desarrollo de Aplicaciones — INACAP Calama 2026
--
--  INSTRUCCIONES:
--    1. Abrir DBeaver y conectar a su MySQL/MariaDB local (XAMPP encendido).
--    2. Abrir este archivo y ejecutarlo COMPLETO (Alt+X en DBeaver).
--    3. Verificar que aparezcan 13 tablas en el esquema 'consultenos_db'.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 0. CREACIÓN DEL ESQUEMA
-- ----------------------------------------------------------------------------
DROP DATABASE IF EXISTS consultenos_db;
CREATE DATABASE consultenos_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE consultenos_db;

-- ============================================================================
--  BLOQUE 1 — TABLAS DE CATÁLOGO Y PERSONAL INTERNO
-- ============================================================================

-- Catálogo de roles del personal interno.
-- Un solo Empleado_Interno + catálogo Rol (no tablas separadas por rol).
CREATE TABLE Rol (
    id_rol      INT AUTO_INCREMENT,
    nombre_rol  VARCHAR(30) NOT NULL,
    CONSTRAINT pk_rol        PRIMARY KEY (id_rol),
    CONSTRAINT uq_rol_nombre UNIQUE (nombre_rol)
) ENGINE=InnoDB;

-- Ámbitos técnicos del soporte (impresión, redes, radiofrecuencia, etc.)
CREATE TABLE Ambito (
    id_ambito     INT AUTO_INCREMENT,
    nombre_ambito VARCHAR(40) NOT NULL,
    CONSTRAINT pk_ambito        PRIMARY KEY (id_ambito),
    CONSTRAINT uq_ambito_nombre UNIQUE (nombre_ambito)
) ENGINE=InnoDB;

-- Cada grupo resolutor cubre UN ámbito técnico.
CREATE TABLE Grupo_Resolutor (
    id_grupo     INT AUTO_INCREMENT,
    id_ambito    INT NOT NULL,
    nombre_grupo VARCHAR(60) NOT NULL,
    CONSTRAINT pk_grupo        PRIMARY KEY (id_grupo),
    CONSTRAINT fk_grupo_ambito FOREIGN KEY (id_ambito)
        REFERENCES Ambito (id_ambito)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Personal interno de Consúltenos (administrador, agente, supervisor, técnico).
-- id_grupo es NULL porque solo los técnicos pertenecen a un grupo resolutor.
-- La contraseña se almacena como hash, nunca en texto plano.
CREATE TABLE Empleado_Interno (
    id_empleado     INT AUTO_INCREMENT,
    id_rol          INT NOT NULL,
    id_grupo        INT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    usuario         VARCHAR(30)  NOT NULL,
    contrasena_hash VARCHAR(255) NOT NULL,
    estado_activo   TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_empleado        PRIMARY KEY (id_empleado),
    CONSTRAINT uq_empleado_usuario UNIQUE (usuario),
    CONSTRAINT fk_empleado_rol    FOREIGN KEY (id_rol)
        REFERENCES Rol (id_rol)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_empleado_grupo  FOREIGN KEY (id_grupo)
        REFERENCES Grupo_Resolutor (id_grupo)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
--  BLOQUE 2 — CLIENTE Y SUS ACTIVOS
-- ============================================================================

-- Empresa cliente. Hoy opera ServiAndina, pero el modelo soporta multi-cliente.
CREATE TABLE Empresa_Cliente (
    id_empresa   INT AUTO_INCREMENT,
    rut_empresa  VARCHAR(12)  NOT NULL,
    razon_social VARCHAR(150) NOT NULL,
    CONSTRAINT pk_empresa     PRIMARY KEY (id_empresa),
    CONSTRAINT uq_empresa_rut UNIQUE (rut_empresa)
) ENGINE=InnoDB;

-- Sucursales de cada empresa cliente.
CREATE TABLE Sucursal (
    id_sucursal     INT AUTO_INCREMENT,
    id_empresa      INT NOT NULL,
    nombre_sucursal VARCHAR(100) NOT NULL,
    direccion       VARCHAR(200) NULL,
    CONSTRAINT pk_sucursal         PRIMARY KEY (id_sucursal),
    CONSTRAINT fk_sucursal_empresa FOREIGN KEY (id_empresa)
        REFERENCES Empresa_Cliente (id_empresa)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Trabajadores del cliente que reportan incidentes por teléfono.
-- codigo_usuario lo genera el sistema (ej. Mjopi001).
CREATE TABLE Usuario_Cliente (
    id_usuario      INT AUTO_INCREMENT,
    id_sucursal     INT NOT NULL,
    codigo_usuario  VARCHAR(10)  NOT NULL,
    rut             VARCHAR(12)  NOT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    telefono        VARCHAR(20)  NULL,
    correo          VARCHAR(120) NULL,
    CONSTRAINT pk_usuario          PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuario_codigo   UNIQUE (codigo_usuario),
    CONSTRAINT uq_usuario_rut      UNIQUE (rut),
    CONSTRAINT fk_usuario_sucursal FOREIGN KEY (id_sucursal)
        REFERENCES Sucursal (id_sucursal)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Hardware corporativo. Está físicamente en una sucursal.
CREATE TABLE Dispositivo (
    id_dispositivo INT AUTO_INCREMENT,
    id_sucursal    INT NOT NULL,
    numero_serie   VARCHAR(50) NOT NULL,
    tipo_hardware  VARCHAR(50) NOT NULL,
    modelo         VARCHAR(80) NULL,
    CONSTRAINT pk_dispositivo          PRIMARY KEY (id_dispositivo),
    CONSTRAINT uq_dispositivo_serie    UNIQUE (numero_serie),
    CONSTRAINT fk_dispositivo_sucursal FOREIGN KEY (id_sucursal)
        REFERENCES Sucursal (id_sucursal)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
--  BLOQUE 3 — NÚCLEO DEL TICKET
-- ============================================================================

-- Catálogo de estados: Abierto → En progreso → Resuelto → Cerrado.
CREATE TABLE Estado_Ticket (
    id_estado     INT AUTO_INCREMENT,
    nombre_estado VARCHAR(20) NOT NULL,
    CONSTRAINT pk_estado        PRIMARY KEY (id_estado),
    CONSTRAINT uq_estado_nombre UNIQUE (nombre_estado)
) ENGINE=InnoDB;

-- Tabla central del sistema.
-- NOTA DE DISEÑO: Ticket guarda solo id_ambito, NO id_grupo. El grupo resolutor
-- se deduce por JOIN (Ticket → Ambito → Grupo_Resolutor), evitando redundancia
-- derivable y datos contradictorios. Respeta 3FN.
CREATE TABLE Ticket (
    id_ticket             INT AUTO_INCREMENT,
    id_usuario            INT NOT NULL,
    id_dispositivo        INT NOT NULL,
    id_ambito             INT NOT NULL,
    id_estado             INT NOT NULL,
    id_agente_creador     INT NOT NULL,
    id_tecnico_asignado   INT NULL,
    fecha_hora_creacion   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_hora_cierre     DATETIME NULL,
    prioridad             ENUM('Alta','Media','Baja')  NOT NULL,
    naturaleza            ENUM('Fisica','Logica')      NOT NULL,
    lugar_fisico_exacto   VARCHAR(200) NOT NULL,
    descripcion_problema  TEXT NOT NULL,
    comentario_resolucion TEXT NULL,
    CONSTRAINT pk_ticket             PRIMARY KEY (id_ticket),
    CONSTRAINT fk_ticket_usuario     FOREIGN KEY (id_usuario)
        REFERENCES Usuario_Cliente (id_usuario)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_ticket_dispositivo FOREIGN KEY (id_dispositivo)
        REFERENCES Dispositivo (id_dispositivo)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_ticket_ambito      FOREIGN KEY (id_ambito)
        REFERENCES Ambito (id_ambito)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_ticket_estado      FOREIGN KEY (id_estado)
        REFERENCES Estado_Ticket (id_estado)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_ticket_agente      FOREIGN KEY (id_agente_creador)
        REFERENCES Empleado_Interno (id_empleado)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_ticket_tecnico     FOREIGN KEY (id_tecnico_asignado)
        REFERENCES Empleado_Interno (id_empleado)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Índices para las consultas más frecuentes (búsqueda y filtrado, HU-08).
CREATE INDEX idx_ticket_estado   ON Ticket (id_estado);
CREATE INDEX idx_ticket_tecnico  ON Ticket (id_tecnico_asignado);
CREATE INDEX idx_ticket_fecha    ON Ticket (fecha_hora_creacion);
CREATE INDEX idx_ticket_prioridad ON Ticket (prioridad);

-- ============================================================================
--  BLOQUE 4 — TABLAS DE APOYO (baja prioridad)
-- ============================================================================

-- Auditoría: registra cada hito del ciclo de vida del ticket (HU-13).
CREATE TABLE Historial_Ticket (
    id_historial      INT AUTO_INCREMENT,
    id_ticket         INT NOT NULL,
    id_empleado       INT NOT NULL,
    fecha_hora_accion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accion            VARCHAR(150) NOT NULL,
    CONSTRAINT pk_historial          PRIMARY KEY (id_historial),
    CONSTRAINT fk_historial_ticket   FOREIGN KEY (id_ticket)
        REFERENCES Ticket (id_ticket)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_historial_empleado FOREIGN KEY (id_empleado)
        REFERENCES Empleado_Interno (id_empleado)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Alertas por tickets atrasados (HU-09).
CREATE TABLE Alerta (
    id_alerta      INT AUTO_INCREMENT,
    id_ticket      INT NOT NULL,
    tipo_alerta    VARCHAR(40) NOT NULL,
    fecha_generada DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_alerta  ENUM('Activa','Atendida') NOT NULL DEFAULT 'Activa',
    CONSTRAINT pk_alerta        PRIMARY KEY (id_alerta),
    CONSTRAINT fk_alerta_ticket FOREIGN KEY (id_ticket)
        REFERENCES Ticket (id_ticket)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Reportes de gestión consolidados por periodo (HU-11).
CREATE TABLE Reporte (
    id_reporte                 INT AUTO_INCREMENT,
    periodo                    VARCHAR(20) NOT NULL,
    tickets_abiertos           INT NOT NULL DEFAULT 0,
    tickets_cerrados           INT NOT NULL DEFAULT 0,
    tiempo_promedio_resolucion DECIMAL(6,2) NULL,
    CONSTRAINT pk_reporte PRIMARY KEY (id_reporte)
) ENGINE=InnoDB;

-- ============================================================================
--  BLOQUE 5 — DATOS DE PRUEBA
-- ============================================================================

-- --- Catálogos ---
INSERT INTO Rol (nombre_rol) VALUES
    ('Administrador'),
    ('Agente'),
    ('Supervisor'),
    ('Tecnico');

INSERT INTO Ambito (nombre_ambito) VALUES
    ('Impresion'),
    ('Radiofrecuencia'),
    ('Redes'),
    ('Computadores'),
    ('Telefonos');

INSERT INTO Estado_Ticket (nombre_estado) VALUES
    ('Abierto'),
    ('En progreso'),
    ('Resuelto'),
    ('Cerrado');

-- --- Grupos resolutores (uno por ámbito) ---
INSERT INTO Grupo_Resolutor (id_ambito, nombre_grupo) VALUES
    (1, 'Especialistas en Impresion'),
    (2, 'Especialistas en Radiofrecuencia'),
    (3, 'Especialistas en Redes'),
    (4, 'Especialistas en Computadores'),
    (5, 'Especialistas en Telefonia');

-- --- Personal interno ---
-- NOTA: las contraseñas aquí son hashes de ejemplo. En la aplicación Java
-- se debe generar el hash real (por ejemplo con BCrypt) antes de insertar.
INSERT INTO Empleado_Interno (id_rol, id_grupo, nombre_completo, usuario, contrasena_hash) VALUES
    (1, NULL, 'Edson Peralta Campusano',   'admin',     'hash_demo_admin'),
    (2, NULL, 'Paolo Buston Espindola',    'agente1',   'hash_demo_agente1'),
    (2, NULL, 'Jhonatan Cardenas Reynaga', 'agente2',   'hash_demo_agente2'),
    (3, NULL, 'Marco Jopia Lazo',          'supervisor','hash_demo_super'),
    (4, 1,    'Cristofer Tito Condori',    'tecnico1',  'hash_demo_tec1'),
    (4, 3,    'Luis Fernandez Rojas',      'tecnico2',  'hash_demo_tec2'),
    (4, 4,    'Camila Soto Vega',          'tecnico3',  'hash_demo_tec3');

-- --- Cliente y sus activos ---
INSERT INTO Empresa_Cliente (rut_empresa, razon_social) VALUES
    ('76.543.210-8', 'ServiAndina SpA');

INSERT INTO Sucursal (id_empresa, nombre_sucursal, direccion) VALUES
    (1, 'Casa Matriz Calama',   'Av. Balmaceda 2340, Calama'),
    (1, 'Sucursal Antofagasta', 'Av. Grecia 1180, Antofagasta'),
    (1, 'Faena Chuquicamata',   'Camino a Chuquicamata s/n');

INSERT INTO Usuario_Cliente (id_sucursal, codigo_usuario, rut, nombre_completo, telefono, correo) VALUES
    (1, 'Mjopi001', '18.234.567-9', 'Maria Jose Pizarro',   '+56912345678', 'mpizarro@serviandina.cl'),
    (1, 'Jrome002', '17.111.222-3', 'Jorge Romero Diaz',    '+56987654321', 'jromero@serviandina.cl'),
    (2, 'Asilv003', '19.876.543-2', 'Ana Silva Contreras',  '+56911223344', 'asilva@serviandina.cl'),
    (3, 'Pmuno004', '16.555.444-1', 'Pedro Munoz Tapia',    '+56955667788', 'pmunoz@serviandina.cl');

INSERT INTO Dispositivo (id_sucursal, numero_serie, tipo_hardware, modelo) VALUES
    (1, 'SN-IMP-00123', 'Impresora',      'HP LaserJet Pro M404'),
    (1, 'SN-PC-00456',  'Computador',     'Dell OptiPlex 7090'),
    (1, 'SN-TEL-00789', 'Telefono IP',    'Cisco IP Phone 7841'),
    (2, 'SN-RF-00321',  'Lector RF',      'Zebra MC3300'),
    (2, 'SN-PC-00654',  'Computador',     'Lenovo ThinkCentre M70'),
    (3, 'SN-SW-00987',  'Switch de red',  'Cisco Catalyst 2960'),
    (3, 'SN-IMP-00555', 'Impresora',      'Epson EcoTank L3250');

-- --- Tickets de ejemplo (cubren los 4 estados del ciclo de vida) ---
-- Ticket 1: Abierto, sin técnico asignado aún
INSERT INTO Ticket (id_usuario, id_dispositivo, id_ambito, id_estado, id_agente_creador,
                    id_tecnico_asignado, prioridad, naturaleza, lugar_fisico_exacto,
                    descripcion_problema)
VALUES (1, 1, 1, 1, 2, NULL, 'Alta', 'Fisica', 'Oficina 203, segundo piso',
        'La impresora no toma papel de la bandeja principal y muestra atasco constante.');

-- Ticket 2: En progreso, asignado al técnico de impresión
INSERT INTO Ticket (id_usuario, id_dispositivo, id_ambito, id_estado, id_agente_creador,
                    id_tecnico_asignado, prioridad, naturaleza, lugar_fisico_exacto,
                    descripcion_problema)
VALUES (2, 7, 1, 2, 2, 5, 'Media', 'Fisica', 'Bodega faena, sector norte',
        'La impresora imprime con lineas horizontales en toda la hoja.');

-- Ticket 3: Resuelto (con comentario de resolución y fecha de cierre)
INSERT INTO Ticket (id_usuario, id_dispositivo, id_ambito, id_estado, id_agente_creador,
                    id_tecnico_asignado, fecha_hora_creacion, fecha_hora_cierre,
                    prioridad, naturaleza, lugar_fisico_exacto,
                    descripcion_problema, comentario_resolucion)
VALUES (3, 5, 4, 3, 3, 7, '2026-07-10 09:15:00', '2026-07-10 14:30:00',
        'Alta', 'Logica', 'Sala de ventas, puesto 4',
        'El computador no inicia sesion en el dominio corporativo.',
        'Se restablecio la cuenta de equipo en el dominio y se reinicio el servicio de red.');

-- Ticket 4: Cerrado
INSERT INTO Ticket (id_usuario, id_dispositivo, id_ambito, id_estado, id_agente_creador,
                    id_tecnico_asignado, fecha_hora_creacion, fecha_hora_cierre,
                    prioridad, naturaleza, lugar_fisico_exacto,
                    descripcion_problema, comentario_resolucion)
VALUES (4, 6, 3, 4, 2, 6, '2026-07-08 11:00:00', '2026-07-08 16:45:00',
        'Alta', 'Fisica', 'Sala de servidores, rack 2',
        'El switch principal pierde conectividad de forma intermitente.',
        'Se reemplazo cable de red defectuoso en puerto 12 y se verifico enlace estable.');

-- Ticket 5: En progreso, radiofrecuencia
INSERT INTO Ticket (id_usuario, id_dispositivo, id_ambito, id_estado, id_agente_creador,
                    id_tecnico_asignado, prioridad, naturaleza, lugar_fisico_exacto,
                    descripcion_problema)
VALUES (3, 4, 2, 2, 3, 5, 'Baja', 'Logica', 'Patio de despacho',
        'El lector de radiofrecuencia no sincroniza con el sistema de inventario.');

-- --- Historial de auditoría de ejemplo ---
INSERT INTO Historial_Ticket (id_ticket, id_empleado, accion) VALUES
    (1, 2, 'Ticket creado via llamada telefonica'),
    (2, 2, 'Ticket creado via llamada telefonica'),
    (2, 4, 'Ticket asignado al tecnico Cristofer Tito'),
    (3, 3, 'Ticket creado via llamada telefonica'),
    (3, 4, 'Ticket asignado a la tecnico Camila Soto'),
    (3, 7, 'Ticket marcado como Resuelto');

-- --- Alerta de ejemplo (ticket atrasado) ---
INSERT INTO Alerta (id_ticket, tipo_alerta) VALUES
    (1, 'Ticket sin asignar por mas de 4 horas');

-- --- Reporte de ejemplo ---
INSERT INTO Reporte (periodo, tickets_abiertos, tickets_cerrados, tiempo_promedio_resolucion) VALUES
    ('Julio 2026', 3, 2, 5.30);

-- ============================================================================
--  BLOQUE 6 — VERIFICACIÓN
-- ============================================================================

-- Confirmar que se crearon las 13 tablas
SELECT 'Tablas creadas:' AS Verificacion, COUNT(*) AS Total
FROM information_schema.tables
WHERE table_schema = 'consultenos_db';

-- Confirmar conteo de registros por tabla
SELECT 'Rol'              AS Tabla, COUNT(*) AS Registros FROM Rol
UNION ALL SELECT 'Ambito',           COUNT(*) FROM Ambito
UNION ALL SELECT 'Grupo_Resolutor',  COUNT(*) FROM Grupo_Resolutor
UNION ALL SELECT 'Empleado_Interno', COUNT(*) FROM Empleado_Interno
UNION ALL SELECT 'Empresa_Cliente',  COUNT(*) FROM Empresa_Cliente
UNION ALL SELECT 'Sucursal',         COUNT(*) FROM Sucursal
UNION ALL SELECT 'Usuario_Cliente',  COUNT(*) FROM Usuario_Cliente
UNION ALL SELECT 'Dispositivo',      COUNT(*) FROM Dispositivo
UNION ALL SELECT 'Estado_Ticket',    COUNT(*) FROM Estado_Ticket
UNION ALL SELECT 'Ticket',           COUNT(*) FROM Ticket
UNION ALL SELECT 'Historial_Ticket', COUNT(*) FROM Historial_Ticket
UNION ALL SELECT 'Alerta',           COUNT(*) FROM Alerta
UNION ALL SELECT 'Reporte',          COUNT(*) FROM Reporte;

-- ============================================================================
--  FIN DEL SCRIPT DE CREACIÓN
-- ============================================================================
