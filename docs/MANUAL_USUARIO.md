# MANUAL DE USUARIO — Consúltenos

Mesa de ayuda para soporte de hardware corporativo de ServiAndina SpA.

Este manual explica cómo usar el programa. No requiere conocimientos
técnicos: está pensado para el personal que usa la aplicación día a día
(agentes, supervisores, técnicos y administradores).

---

## Índice

1. [¿Qué es Consúltenos?](#1-qué-es-consúltenos)
2. [Iniciar sesión](#2-iniciar-sesión)
3. [El menú principal](#3-el-menú-principal)
4. [Registrar un ticket (Agente)](#4-registrar-un-ticket-agente)
5. [Ver Tickets y filtrar (Agente / Supervisor / Administrador)](#5-ver-tickets-y-filtrar-agente--supervisor--administrador)
6. [Detalle de un ticket](#6-detalle-de-un-ticket)
7. [Asignar un técnico (Supervisor / Administrador)](#7-asignar-un-técnico-supervisor--administrador)
8. [Mis Tickets Asignados y resolución (Técnico)](#8-mis-tickets-asignados-y-resolución-técnico)
9. [Reportes de Gestión (Supervisor / Administrador)](#9-reportes-de-gestión-supervisor--administrador)
10. [Gestión de Personal (Administrador)](#10-gestión-de-personal-administrador)
11. [Funciones adicionales](#11-funciones-adicionales)
12. [El ciclo de vida de un ticket](#12-el-ciclo-de-vida-de-un-ticket)
13. [Preguntas frecuentes](#13-preguntas-frecuentes)

---

## 1. ¿Qué es Consúltenos?

Consúltenos es el sistema interno donde el personal de soporte registra,
asigna, resuelve y hace seguimiento a los problemas de hardware que
reportan por teléfono los trabajadores de las empresas clientes.

El ingreso de tickets es **exclusivamente telefónico**: el cliente llama,
un agente contesta y registra el problema en el sistema. El cliente no
tiene acceso directo a la aplicación.

Flujo general:

```
Cliente llama → Agente registra el ticket → Supervisor asigna un técnico
→ Técnico resuelve → Supervisor revisa reportes de gestión
```

Cada persona que usa el sistema tiene un **rol** (Agente, Supervisor,
Técnico o Administrador), y el menú le muestra solo las opciones que le
corresponden a ese rol.

---

## 2. Iniciar sesión

Al abrir la aplicación aparece la pantalla de ingreso:

1. Escribe tu **usuario** y **contraseña**.
2. Presiona **Ingresar** (o Enter en cualquiera de los dos campos).
3. Si los datos son correctos, se abre el menú principal según tu rol.
4. Si te equivocas, aparece el mensaje "Usuario o contraseña incorrectos".

Si cierras la ventana de ingreso sin entrar, la aplicación se cierra.

---

## 3. El menú principal

Al ingresar ves una ventana con:

- Un **panel lateral** (a la izquierda) con las opciones disponibles para
  tu rol, y el botón **Cerrar sesión** al final.
- Una **cabecera** (arriba) con la fecha de hoy y tus datos (nombre y rol).
- Un **panel de inicio** con tarjetas que muestran cuántos tickets hay
  hoy en cada estado (Abierto, En progreso, Resuelto, Cerrado).

Cada opción del panel lateral abre su propia ventana. Puedes tener varias
ventanas abiertas al mismo tiempo; si vuelves a hacer clic en una opción
que ya tienes abierta, esa ventana se trae al frente en vez de abrir otra
igual.

Las opciones que ves dependen de tu rol:

| Rol            | Opciones disponibles |
|----------------|------------------------|
| Agente         | Registrar Ticket, Ver Tickets |
| Supervisor     | Registrar Ticket, Ver Tickets, Asignación de Técnicos, Reportes de Gestión, más las funciones adicionales (sección 11) |
| Técnico        | Mis Tickets Asignados |
| Administrador  | Todo lo del Supervisor, más Gestión de Personal |

---

## 4. Registrar un ticket (Agente)

Se usa cuando un cliente llama para reportar un problema.

1. Abre **Registrar Ticket**.
2. Escribe el **código de usuario** del cliente que llama (por ejemplo
   `Mjopi001`) y presiona **Buscar** (o Enter).
   - Si el código existe, se completan automáticamente su nombre,
     sucursal y empresa, y se llena la lista de dispositivos de esa
     sucursal.
   - Si el código no existe, aparece "Código no encontrado" y el resto
     del formulario permanece bloqueado hasta que ingreses un código
     válido.
3. Selecciona el **dispositivo afectado** de la lista.
4. Selecciona el **ámbito** del problema (Impresión, Redes, Radiofrecuencia,
   Computadores, Teléfonos).
5. Selecciona la **prioridad** (Alta, Media, Baja) y la **naturaleza**
   (Física o Lógica).
6. Escribe el **lugar físico exacto** (ej. "Oficina 203, segundo piso") y
   una **descripción del problema**.
7. Presiona **Registrar Ticket**.

El sistema confirma con el número de ticket generado. El ticket queda
creado en estado **Abierto**, y el formulario se limpia automáticamente
para registrar la siguiente llamada.

---

## 5. Ver Tickets y filtrar (Agente / Supervisor / Administrador)

Muestra todos los tickets, con filtros para encontrar lo que buscas:

- **Estado**: Todos, Abierto, En progreso, Resuelto o Cerrado.
- **Prioridad**: Todas, Alta, Media o Baja.
- **Sucursal**: Todas, o una sucursal específica.
- **N° de serie del dispositivo**: para ver el historial de un equipo
  puntual (ej. `SN-IMP-00123`).

Puedes combinar los filtros. Presiona **Buscar** para aplicar, o
**Limpiar filtros** para volver a ver todo.

La tabla usa colores para que identifiques rápido:
- **Prioridad**: rojo (Alta), amarillo (Media), verde (Baja).
- **Estado**: naranjo (Abierto), azul (En progreso), morado (Resuelto),
  gris (Cerrado).

**Doble clic en cualquier fila** abre el detalle completo de ese ticket.

---

## 6. Detalle de un ticket

Muestra toda la información del ticket: empresa, sucursal, quién reporta,
dispositivo, ámbito, prioridad, estado, quién lo registró, quién lo
atiende, fechas, descripción del problema y comentario de resolución (si
ya tiene uno).

Al final se muestra el **historial de eventos** del ticket (por ejemplo,
cuándo se asignó, cuándo se resolvió).

Según tu rol y el estado del ticket, puede aparecer:

- **Marcar como Resuelto** (solo si eres el técnico asignado y el ticket
  está "En progreso"): escribe el comentario de resolución y confirma.
- **Cerrar Ticket** (si el ticket ya está "Resuelto").

---

## 7. Asignar un técnico (Supervisor / Administrador)

1. Abre **Asignación de Técnicos**. Se listan los tickets **Abierto** que
   todavía no tienen técnico asignado.
2. Selecciona un ticket de la tabla. El sistema busca automáticamente los
   técnicos disponibles para el **ámbito** de ese ticket.
   - Si no hay ningún técnico disponible para ese ámbito, se te avisa y
     no puedes continuar con ese ticket.
3. Elige un técnico de la lista y presiona **Asignar Técnico**.

El ticket pasa a estado **En progreso** y desaparece de esta lista (ya
no está "sin asignar"). El técnico verá el ticket en su bandeja **Mis
Tickets Asignados** la próxima vez que la abra — el sistema no envía
correos ni notificaciones emergentes.

---

## 8. Mis Tickets Asignados y resolución (Técnico)

Muestra únicamente los tickets que tienes asignados y que siguen activos
(Abierto o En progreso).

Para resolver un ticket:

1. Haz doble clic en el ticket para abrir su detalle.
2. Escribe el **comentario de resolución** (obligatorio) explicando qué
   se hizo.
3. Presiona **Marcar como Resuelto**.

El ticket pasa a estado **Resuelto** y sale de tu bandeja.

---

## 9. Reportes de Gestión (Supervisor / Administrador)

Cuatro pestañas con información de gestión, todas de solo lectura:

- **Por Estado**: cuántos tickets hay actualmente en cada estado.
- **MTTR**: tiempo promedio (en horas) que toma resolver un ticket, y
  cuántos tickets se están considerando en ese cálculo.
- **Por Técnico**: cuántos tickets tiene asignados cada técnico y cuántos
  ha resuelto.
- **Por Ámbito**: en qué ámbito técnico se concentran más tickets.

Presiona **Actualizar reportes** para refrescar los datos.

---

## 10. Gestión de Personal (Administrador)

Lista de solo lectura de todo el personal interno: nombre, usuario, rol y
si está activo. No permite crear, editar ni eliminar personal desde aquí.

---

## 11. Funciones adicionales

El menú de Supervisor y Administrador incluye dos opciones marcadas como
**"(extra)"**:

- **Historial (extra)**: búsqueda libre sobre el registro de auditoría
  del sistema (por texto, estado o rango de fechas).
- **Tickets Activos (extra)**: una vista rápida de los tickets Abierto/En
  progreso con cambio de estado directo desde la misma tabla.

Son funciones adicionales que no forman parte del set de funcionalidades
comprometidas para esta entrega, pero quedaron disponibles como valor
agregado.

---

## 12. El ciclo de vida de un ticket

```
Abierto → En progreso → Resuelto → Cerrado
```

- **Abierto**: recién registrado, todavía sin técnico asignado.
- **En progreso**: ya tiene un técnico asignado, está siendo atendido.
- **Resuelto**: el técnico terminó y dejó su comentario de resolución.
- **Cerrado**: paso final, confirma que el caso quedó totalmente cerrado.

---

## 13. Preguntas frecuentes

**¿Qué hago si el código de usuario no se encuentra al registrar un ticket?**
Verifica que el código esté bien escrito (es sensible a mayúsculas/minúsculas
en algunos casos). Si el cliente es nuevo, debe ser dado de alta primero en
el sistema (fuera del alcance de esta aplicación por ahora).

**¿Por qué no aparece ningún técnico al intentar asignar un ticket?**
Significa que no hay técnicos activos configurados para el ámbito de ese
ticket. Debe revisarlo un administrador.

**¿Por qué no puedo cerrar un ticket que acabo de resolver?**
El botón "Cerrar Ticket" solo aparece cuando el ticket está en estado
Resuelto — si lo acabas de marcar como resuelto, cierra y vuelve a abrir
el detalle para verlo.

**¿Puedo tener varias ventanas abiertas a la vez?**
Sí. Puedes tener, por ejemplo, el Listado de Tickets y el detalle de dos
tickets distintos abiertos al mismo tiempo. Si intentas abrir una ventana
que ya está abierta, el sistema la trae al frente en vez de abrir otra.

**Olvidé mi usuario o contraseña, ¿qué hago?**
Contacta a un Administrador para que verifique tus credenciales en
Gestión de Personal.
