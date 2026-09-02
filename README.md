# Consúltenos — Mesa de Ayuda B2B

Sistema de gestión de soporte técnico de hardware corporativo.

**Asignatura:** Taller de Desarrollo de Aplicaciones
**Institución:** INACAP Calama — 2026
**Cliente del caso:** ServiAndina SpA

---

## ¿Qué hace el sistema?

Consúltenos gestiona el ciclo completo de atención de una mesa de ayuda:
un usuario del cliente llama por teléfono reportando una falla de hardware,
un agente registra el ticket, un supervisor lo asigna a un técnico especialista,
el técnico lo resuelve, y el supervisor consulta reportes de gestión.

**Restricción de alcance:** el ingreso de tickets es exclusivamente telefónico
(no hay portal de autoservicio para el cliente).

---

## Tecnologías

| Componente | Herramienta |
|---|---|
| Lenguaje | Java |
| Interfaz | Java Swing |
| IDE | Apache NetBeans |
| Base de datos | MySQL / MariaDB (XAMPP) |
| Acceso a datos | JDBC |
| Administración BD | DBeaver |
| Control de versiones | Git / GitHub |

---

## Estructura del proyecto

```
consultenos/
├── src/
│   ├── modelo/    Clases que representan las entidades (Ticket, Usuario...)
│   ├── dao/       Acceso a datos: aquí viven las consultas SQL
│   ├── vista/     Formularios Swing (lo que ve el usuario)
│   └── util/      Utilidades comunes (Conexion.java)
├── sql/           Scripts de creación de la BD y consultas
├── docs/          Modelos de datos y documentación
└── lib/           Librerías externas (conector JDBC)
```

**¿Por qué esta separación?** Cada capa tiene una responsabilidad:
la vista no sabe SQL, el DAO no sabe de botones. Esto permite que varios
integrantes trabajen en paralelo sin pisarse, y hace el código mantenible.

---

## Instalación (hacer una vez por computador)

### 1. Requisitos previos

- JDK 17 o superior
- Apache NetBeans
- XAMPP (con MySQL)
- DBeaver (opcional, para ver la base de datos)

### 2. Crear la base de datos

1. Abrir XAMPP y encender **MySQL** (debe quedar en verde).
2. Abrir DBeaver y conectar a `localhost:3306` con usuario `root`.
3. Abrir el archivo `sql/01_crear_bd_consultenos.sql`.
4. Ejecutarlo completo (en DBeaver: `Alt + X`).
5. Verificar que aparezca la base `consultenos_db` con **13 tablas**.

### 3. Agregar el conector JDBC

El conector es el "traductor" entre Java y MySQL. Sin él, nada funciona.

1. Descargar **MySQL Connector/J** desde la web oficial de MySQL.
2. Copiar el archivo `.jar` a la carpeta `lib/` del proyecto.
3. En NetBeans: click derecho en el proyecto → *Properties* → *Libraries*
   → *Add JAR/Folder* → seleccionar el `.jar`.

### 4. Probar la conexión

1. Abrir `src/util/Conexion.java`.
2. Click derecho → **Run File**.
3. Si aparece `CONEXION EXITOSA`, todo está listo.

Si falla, el propio programa indica qué revisar.

---

## Trabajo en equipo con Git

### Primera vez (clonar el proyecto)

```bash
git clone https://github.com/USUARIO/consultenos.git
cd consultenos
```

### Flujo de trabajo diario

Trabajar siempre en una rama propia, nunca directo en `main`:

```bash
# 1. Traer los últimos cambios
git checkout main
git pull

# 2. Crear una rama para lo que voy a hacer
git checkout -b feature/login

# 3. Trabajar... y luego guardar los cambios
git add .
git commit -m "Agrega validacion de usuario en el login"

# 4. Subir la rama
git push -u origin feature/login
```

Después se crea un *Pull Request* en GitHub para integrar a `main`.

### Reglas del equipo

- **Nunca** trabajar directo en `main`.
- Un commit por cambio lógico, con mensaje que explique **qué** se hizo.
- Antes de empezar a trabajar, siempre `git pull`.
- No subir archivos compilados (ya está el `.gitignore` para eso).

---

## Estado del proyecto

| Hito | Descripción | Estado |
|---|---|---|
| 1 | Base de datos y ruteo de funciones | Completado |
| 2 | Construcción del sistema | Completado |
| 3 | Ejecución del guion y entrega | Completado |

---

## Equipo

- Peralta Campusano, Edson Denzel Amaro
- Buston Espíndola, Paolo Nicolás
- Cardenas Reynaga, Jhonatan Marco
- Jopia Lazo, Marco Antonio Alejandro
- Tito Condori, Cristofer Yigael
