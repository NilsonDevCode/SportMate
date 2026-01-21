# SportMate · AppSportMate 🏀🏃‍♂️⚽

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-blue?style=for-the-badge)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Firestore](https://img.shields.io/badge/Cloud%20Firestore-039BE5?style=for-the-badge&logo=firebase&logoColor=white)
![Testing](https://img.shields.io/badge/Testing-White%20%26%20Black%20Box-success?style=for-the-badge)


Aplicación Android desarrollada como **Proyecto Final de Ciclo (TFG) – DAM**, diseñada para fomentar la **participación deportiva**, la **inclusión social** y la **conexión entre personas y ayuntamientos** a través de eventos deportivos con control de plazas en tiempo real.

SportMate permite tanto a **ayuntamientos** como a **usuarios** crear y gestionar eventos deportivos, ofreciendo una plataforma **abierta, flexible y social**, donde cualquier persona puede practicar deporte, conocer gente y participar en actividades sin barreras geográficas, culturales o sociales.

El proyecto aborda problemas reales como el **sedentarismo**, el **aislamiento social** y la **falta de integración**, promoviendo la unión de personas de distintas edades, culturas y contextos mediante el deporte como elemento común.



## 🎯 Objetivo del proyecto

Desarrollar una aplicación móvil que permita:

- A los **ayuntamientos**, publicar y gestionar eventos deportivos oficiales con plazas limitadas.
- A los **usuarios**, **unirse a eventos existentes o crear sus propios eventos privados**, en cualquier lugar.
- Facilitar la práctica deportiva incluso fuera del entorno habitual del usuario (viajes, nuevas ciudades, estancias temporales).
- Crear una comunidad deportiva **inclusiva**, accesible y social, que fomente hábitos saludables y relaciones humanas reales.

Todo ello manteniendo una arquitectura sólida, datos coherentes y un control de acceso seguro basado en roles.

<table>
  <tr>
    <td align="center"><b>Login</b></td>
    <td align="center"><b>Detalle del Evento</b></td>
    <td align="center"><b>Inicio / Perfil</b></td>
  </tr>
  <tr>
    <td><img src="docs/login.png" width="250"/></td>
    <td><img src="docs/eventos.png" width="250"/></td>
    <td><img src="docs/perfil.png" width="250"/></td>
  </tr>
</table>

![Plazas en tiempo real](docs/plazas_realtime.gif)




## ✅ Funcionalidades principales

### 🔐 Autenticación y roles
- Registro e inicio de sesión mediante **alias + contraseña**.
- Gestión de roles:
  - **Usuario**
  - **Ayuntamiento**
- Autenticación segura con **Firebase Authentication**.
- Persistencia de sesión y control de acceso por rol.

### 🏛️ Funcionalidades de Ayuntamiento
- Crear, editar y eliminar **eventos deportivos oficiales**.
- Definir y gestionar el **número máximo de plazas** por evento.
- Visualizar el listado de **usuarios inscritos** en cada evento.
- Expulsar participantes y liberar plazas automáticamente.
- Control automático de plazas en **tiempo real** (suma/resta).
- Gestión centralizada de eventos deportivos para la comunidad local.

### 👤 Funcionalidades de Usuario
- Visualizar **eventos oficiales** creados por ayuntamientos.
- **Crear eventos deportivos privados** en cualquier ubicación.
- Unirse o darse de baja de eventos oficiales o privados.
- Visualizar los eventos en los que está inscrito.
- Buscar y participar en actividades deportivas incluso fuera de su entorno habitual (viajes, otras ciudades).
- Bloqueo automático de inscripción si no hay plazas disponibles.
- Libertad total para organizar o participar en actividades deportivas.


### 🔄 Lógica de negocio y control de datos
- Sincronización de datos en **tiempo real** mediante Cloud Firestore.
- Gestión automática y consistente de **plazas disponibles** en eventos.
- Control de acceso y visibilidad de la información según **UID y rol**.
- Prevención de duplicados, inscripciones inconsistentes y estados inválidos.
- Garantía de coherencia entre eventos, usuarios y participantes.



## 🌍 Gestión flexible de eventos

SportMate permite una gestión de eventos **abierta y descentralizada**, combinando la organización institucional con la iniciativa personal de los usuarios.

### 🧩 Tipos de eventos soportados
- **Eventos oficiales**, creados y gestionados por ayuntamientos.
- **Eventos privados**, creados libremente por los propios usuarios.
- Posibilidad de participar en eventos existentes o crear nuevos sin restricciones geográficas.



### 📍 Uso real y alcance social

- Los usuarios pueden crear o unirse a eventos aunque se encuentren **fuera de su ayuntamiento habitual**.
- La aplicación es útil tanto en el entorno local como en desplazamientos, viajes o estancias temporales.
- Facilita la práctica deportiva espontánea y la conexión entre personas en cualquier punto del territorio.

Este enfoque convierte a **SportMate** en una plataforma **social, inclusiva y escalable**, pensada para el uso cotidiano y no limitada a un contexto fijo.

## 🧠 Arquitectura y enfoque técnico

- Arquitectura **MVVM** implementada en toda la aplicación.
- Separación clara de responsabilidades:
  - **UI** (Activities / Fragments)
  - **ViewModel** (lógica de presentación y estado)
  - **Datos** (repositorios, Firebase, modelos)
- CRUDs implementados **manualmente** (sin FirebaseUI) para tener control total sobre la lógica.
- Arquitectura modular y escalable, facilitando mantenimiento, testing y futuras ampliaciones.




## 🧪 Testing y calidad del software

El proyecto incluye **pruebas de software reales**, centradas en garantizar la fiabilidad de los procesos críticos.

### 🔍 Tipos de pruebas implementadas
- **Pruebas de caja blanca**:
  - Validación de la lógica interna.
  - Control de flujos, condiciones y estados.
- **Pruebas de caja negra**:
  - Validación funcional desde la perspectiva del usuario.

### 🎯 Casos cubiertos
- Login correcto e incorrecto.
- Registro con datos válidos e inválidos.
- Validaciones de campos obligatorios.
- Gestión de errores y mensajes al usuario.

Estas pruebas se centran principalmente en los flujos de **Login** y **Sign Up**, garantizando seguridad y estabilidad.


## 🤝 Impacto social del proyecto

SportMate es una aplicación con **impacto social real**, diseñada para ir más allá de la simple organización de eventos deportivos.

- Reduce el **sedentarismo**, facilitando la práctica deportiva regular.
- Combate el **aislamiento social**, promoviendo la interacción entre personas con intereses comunes.
- Fomenta la **integración social** entre personas de distintas edades, culturas y contextos.
- Refuerza la **cohesión comunitaria**, utilizando el deporte como lenguaje universal.

El enfoque abierto y participativo de la aplicación contribuye a crear entornos más saludables, inclusivos y socialmente conectados, tanto a nivel local como en contextos más amplios.


## 🛠️ Tecnologías utilizadas

- Android Studio  
- Java  
- Firebase Authentication  
- Cloud Firestore  
- Firebase Storage  
- Material Design Components  
- Git & GitHub  


## 📂 Estructura del proyecto

```
com.nilson.appsportmate
├── adapters
│   └── RecyclerView adapters (usuarios, deportes, eventos)
├── data
│   ├── firebase
│   │   ├── FirebaseAuthManager
│   │   ├── FirestoreManager
│   │   └── FirebaseRefs
│   └── models
│       ├── Usuario
│       ├── Deporte
│       ├── Ayuntamiento
│       └── Evento
├── ui
│   ├── auth           // Login y registro
│   ├── usuario        // Pantallas de usuario
│   ├── ayuntamiento   // Pantallas de ayuntamiento
│   └── main           // Pantalla principal y navegación
└── utils
    ├── Constants
    ├── Validations
    └── Preferences
```


## 🚀 Instalación y ejecución

1. Clona el repositorio:
   ```bash
   git clone https://github.com/NilsonDevCode/SportMate.git
   ```
   
2. Abre el proyecto con **Android Studio**.

3. Crea un proyecto en **Firebase**:
   - Activa **Authentication (Email/Password)**.
   - Activa **Cloud Firestore**.
   - (Opcional) **Firebase Storage**.

4. Descarga el archivo `google-services.json` y colócalo en:
`app/google-services.json`

5. Sincroniza Gradle y ejecuta la app en un emulador o dispositivo físico.

## 🔒 Seguridad y buenas prácticas
- Acceso a datos restringido por UID.
- Separación clara de usuarios y ayuntamientos.
- Validaciones de formulario completas.
- Prevención de acciones no autorizadas.
- Código preparado para reglas de seguridad avanzadas en Firestore.

## 📊 Estado del proyecto

✔ Funcional y completo

✔ Evaluado y aprobado con calificación excelente

✔ Lógica de negocio sólida

✔ Arquitectura clara y mantenible

✔ Pruebas de software implementadas (caja blanca y caja negra)

## 🔧 Futuras mejoras 
- Ampliación de test instrumentados
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

- Desarrollo de una aplicación Android completa con **arquitectura MVVM**.
- Integración real de **Firebase Authentication**, **Cloud Firestore** y **Storage**.
- Implementación de **gestión de roles y permisos** a nivel de aplicación.
- Control de estado, sincronización de datos y consistencia en tiempo real.
- Diseño e implementación de **lógica de negocio compleja** sin FirebaseUI.
- Aplicación de **pruebas de software** (caja blanca y caja negra) en flujos críticos.
- Uso profesional de **Git y GitHub** en un entorno colaborativo.
- Organización de código modular, mantenible y escalable.

## 📄 Licencia

Este proyecto se distribuye con fines educativos y demostrativos.  
Puede utilizarse como referencia respetando la autoría original.

