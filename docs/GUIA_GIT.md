# Guía de Git para el equipo

Guía práctica para trabajar el proyecto entre varios sin romper nada.

---

## Parte 1 — Crear el repositorio (solo lo hace UNA persona)

### 1. Crear el repo en GitHub

1. Entrar a github.com e iniciar sesión.
2. Botón **New** (o el `+` arriba a la derecha → *New repository*).
3. Configurar:
   - **Repository name:** `consultenos`
   - **Description:** Sistema de mesa de ayuda B2B — Taller de Desarrollo de Aplicaciones
   - **Visibility:** Private (o Public si el profesor lo pide)
   - **NO** marcar "Add a README" (ya tenemos uno)
4. Botón **Create repository**.

### 2. Subir el proyecto por primera vez

Abrir la terminal (o Git Bash) dentro de la carpeta del proyecto:

```bash
# Inicializar el repositorio local
git init

# Configurar quién eres (solo la primera vez en tu PC)
git config user.name "Tu Nombre"
git config user.email "tucorreo@ejemplo.com"

# Agregar todos los archivos
git add .

# Primer commit
git commit -m "Estructura inicial del proyecto y conexion a base de datos"

# Conectar con GitHub (reemplazar USUARIO por el tuyo)
git remote add origin https://github.com/USUARIO/consultenos.git

# Subir
git branch -M main
git push -u origin main
```

### 3. Invitar al equipo

En GitHub: **Settings** → **Collaborators** → **Add people**
→ agregar a los 4 compañeros por su usuario de GitHub.

---

## Parte 2 — Clonar el proyecto (lo hacen los DEMÁS)

```bash
git clone https://github.com/USUARIO/consultenos.git
cd consultenos
```

Luego abrir la carpeta desde NetBeans y agregar el `.jar` del conector.

---

## Parte 3 — El día a día

### El ciclo básico

Imagina que Git es como entregar un trabajo por partes:

```bash
# 1. ANTES de empezar: traer lo que hicieron los demás
git checkout main
git pull

# 2. Crear tu rama de trabajo
git checkout -b feature/registro-ticket

# 3. ... programar ...

# 4. Ver qué archivos cambiaste
git status

# 5. Preparar los cambios
git add .

# 6. Guardar con un mensaje claro
git commit -m "Agrega formulario de registro de ticket"

# 7. Subir tu rama
git push -u origin feature/registro-ticket
```

Después, en GitHub aparecerá un botón para crear un **Pull Request**.
Otro integrante lo revisa y lo integra a `main`.

### Nombres de rama sugeridos

| Tarea | Rama |
|---|---|
| Login | `feature/login` |
| Registro de ticket | `feature/registro-ticket` |
| Asignación | `feature/asignacion` |
| Listado/historial | `feature/listado` |
| Reportes | `feature/reportes` |
| Diseño de interfaz | `feature/ui` |

---

## Parte 4 — Problemas típicos

### "Me pide usuario y contraseña al hacer push"

GitHub ya no acepta contraseña normal. Hay que usar un **Personal Access Token**:

1. GitHub → foto de perfil → *Settings*
2. Abajo: *Developer settings* → *Personal access tokens* → *Tokens (classic)*
3. *Generate new token* → marcar el permiso **repo** → generar
4. Copiar el token y usarlo **como contraseña** al hacer push

### "Tengo un conflicto (merge conflict)"

Pasa cuando dos personas editan la misma línea del mismo archivo.
Git marca el archivo así:

```
<<<<<<< HEAD
código de main
=======
tu código
>>>>>>> tu-rama
```

Hay que **editar el archivo a mano**, dejar la versión correcta, borrar las
marcas `<<<`, `===`, `>>>`, y luego:

```bash
git add .
git commit -m "Resuelve conflicto"
```

**Cómo evitarlos:** que cada uno trabaje en archivos distintos, y hacer
`git pull` seguido.

### "Subí algo que no debía"

```bash
# Deshacer el último commit pero conservar los cambios
git reset --soft HEAD~1
```

### "Quiero descartar todos mis cambios locales"

```bash
git checkout -- .
```

Cuidado: esto borra lo que no hayas guardado con commit.

---

## Parte 5 — Reglas del equipo

1. **Nunca** trabajar directo en `main`. Siempre una rama.
2. Siempre `git pull` antes de empezar a programar.
3. Commits pequeños y frecuentes, mejor que uno gigante al final.
4. El mensaje del commit explica **qué** se hizo, no "cambios" o "arreglos".
5. Si algo se rompe, avisar al grupo antes de intentar arreglarlo solo.

---

## Comandos de rescate

| Situación | Comando |
|---|---|
| ¿En qué rama estoy? | `git branch` |
| ¿Qué cambié? | `git status` |
| Ver historial | `git log --oneline` |
| Volver a main | `git checkout main` |
| Traer cambios | `git pull` |
| Ver diferencias | `git diff` |
