# SportMate · AppSportMate 🏀🏃‍♂️⚽

Aplicación Android desarrollada como **Proyecto Final de Ciclo (TFG) – DAM**, orientada a fomentar la participación deportiva y la conexión entre **usuarios** y **ayuntamientos** mediante la gestión de eventos deportivos con control de plazas en tiempo real.

El proyecto resuelve un problema social claro: **facilitar el acceso al deporte**, combatir el sedentarismo y mejorar la organización de actividades deportivas locales mediante una app moderna, escalable y basada en la nube.

---

## 🎯 Objetivo del proyecto

Crear una aplicación móvil que permita a los **ayuntamientos** publicar deportes/eventos con un número limitado de plazas y a los **usuarios** inscribirse o darse de baja de forma dinámica, manteniendo siempre la coherencia de los datos y la seguridad de acceso.

---

## ✅ Funcionalidades principales

### 🔐 Autenticación y roles
- Registro e inicio de sesión mediante **alias + contraseña**.
- Gestión de roles:
  - **Usuario**
  - **Ayuntamiento**
- Autenticación segura con **Firebase Authentication**.
- Persistencia de sesión y control de acceso por rol.

### 🏛️ Funcionalidades de Ayuntamiento
- Crear, editar y eliminar deportes/eventos.
- Definir **plazas máximas** por evento.
- Ver listado de usuarios inscritos.
- Expulsar participantes de un evento.
- Control automático de plazas (suma/resta en tiempo real).

### 👤 Funcionalidades de Usuario
- Visualizar los deportes disponibles de su ayuntamiento.
- Apuntarse y desapuntarse de eventos.
- Visualizar eventos en los que está inscrito.
- Bloqueo automático si no hay plazas disponibles.

### 🔄 Lógica de negocio
- Sincronización en tiempo real con **Firestore**.
- Actualización automática de plazas disponibles.
- Filtrado de datos por **UID y rol**.
- Prevención de duplicados e inconsistencias.

---

## 🧠 Arquitectura y enfoque técnico

- Arquitectura modular y escalable.
- Separación clara de responsabilidades:
  - **UI**
  - **Datos**
  - **Lógica**
- CRUDs implementados **manualmente** (sin FirebaseUI) para tener control total.
- Código preparado para evolucionar hacia arquitecturas más avanzadas (MVVM).

---

## 🛠️ Tecnologías utilizadas

- Android Studio  
- Java  
- Firebase Authentication  
- Cloud Firestore  
- Firebase Storage  
- Material Design Components  
- Git & GitHub (control de versiones profesional)

---

## 📂 Estructura del proyecto
 ## com.nilson.appsportmate
## ├── adapters
## │ └── RecyclerView Adapters (usuarios, deportes, eventos)
## ├── data
## │ ├── firebase
## │ │ ├── FirebaseAuthManager
## │ │ ├── FirestoreManager
## │ │ └── FirebaseRefs
## │ └── models
## │ ├── Usuario
## │ ├── Deporte
## │ ├── Ayuntamiento
## │ └── Evento
## ├── ui
## │ ├── auth // Login y registro
## │ ├── usuario // Pantallas de usuario
## │ ├── ayuntamiento // Pantallas de ayuntamiento
## │ └── main // Pantalla principal y navegación
## └── utils
## ├── Constants
## ├── Validations
## └── Preferences

1. Clona el repositorio:
   ```bash
   git clone https://github.com/NilsonDevCode/SportMate.git
2. Abre el proyecto con Android Studio.

3. Crea un proyecto en Firebase:
   . Activa Authentication (Email/Password).
   . Activa Cloud Firestore.
   . (Opcional) Firebase Storage.

4. Descarga el archivo google-services.json y colócalo en: app/google-services.json
5. Sincroniza Gradle y ejecuta la app en un emulador o dispositivo físico.

🔒 Seguridad y buenas prácticas
- Acceso a datos restringido por UID.
- Separación clara de usuarios y ayuntamientos.
- Validaciones de formulario completas.
- Prevención de acciones no autorizadas.
- Código preparado para reglas de seguridad avanzadas en Firestore.


Código preparado para reglas de seguridad avanzadas en Firestore.
## 📊 Estado del proyecto

✔ Funcional y completo

✔ Evaluado y aprobado con calificación excelente

✔ Lógica de negocio sólida

✔ Arquitectura clara y mantenible

## 🔧 Futuras mejoras 
- Tests instrumentados
- Mejoras UI/UX
- Notificaciones push
- Optimización de rendimiento

## 👥 Autoría

Proyecto desarrollado inicialmente en equipo (4 personas).
Esta versión corresponde a una copia independiente, mantenida y evolucionada de forma personal.

Nilson (owner de esta versión)

Antonio

Jordy

Elio

## 📌 Aprendizajes clave

Desarrollo de una app Android completa desde cero.

Implementación real de autenticación y base de datos en la nube.

Gestión de roles y permisos.

Control de estados y sincronización en tiempo real.

Uso profesional de Git en proyectos colaborativos.

Organización de código a nivel profesional.

## 📄 Licencia

Este proyecto se distribuye con fines educativos y demostrativos.
Puedes usarlo como referencia respetando la autoría original.
