# SportMate · AppSportMate 🏀🏃‍♂️⚽

<p align="center">
  <a href="#english">🇬🇧 English</a> |
  <a href="#español">🇪🇸 Español</a>
</p>

<br>

## English

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-blue?style=for-the-badge)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Testing](https://img.shields.io/badge/Testing-JUnit%20%7C%20Espresso%20%7C%20Mockito-success?style=for-the-badge)

> **Technical TL;DR**  
> Android app built in Java using Clean Architecture + MVVM and Firebase.  
> Supports official and private events, real-time seat control,  
> role-based access, testing, and a strong social focus.

<details>
<summary><b>📑 Contents</b></summary>

- [🎯 Project Goal](#-project-goal)
- [📸 Screenshots](#-screenshots-main-flows)
- [🎥 Demo](#-demo-real-time-seat-management)
- [✅ Core Features](#-core-features)
- [📂 Project Structure](#-project-structure)
- [🧩 Architecture and Technical Decisions](#-architecture-and-technical-decisions)
- [🔍 Testing Strategy & Tools](#-testing-strategy-tools)
- [🌍 Flexible Event Management](#-flexible-event-management)
- [🤝 Social Impact of the Project](#-social-impact-of-the-project)
- [🛠️ Technologies and Technical Stack](#-technologies-and-technical-stack)
- [🚀 Installation and Execution](#-installation-and-execution)
- [🔒 Security and Best Practices](#-security-and-best-practices)
- [📊 Project Status](#-project-status)
- [🔧 Future Improvements](#-future-improvements)
- [👥 Authorship](#-authorship)
- [📌 Key Learnings](#-key-learnings)
- [📄 License](#-license)

</details>


Android application developed as a **Final Degree Project (TFG) – DAM**, created to promote **sports participation**, **social inclusion**, and **connection between citizens and municipalities** through sports events with real-time seat management.

SportMate enables both **municipalities** and **users** to create and manage sports events within an **open, flexible, and social platform**, where anyone can practice sports, meet new people, and participate in activities without geographical, cultural, or social barriers.

The project addresses real-world issues such as **sedentary lifestyle**, **social isolation**, and **lack of community integration**, encouraging interaction between people of different ages, cultures, and backgrounds through sport as a shared element.


## 🎯 Project Goal

Develop a mobile application that allows:

- **Municipalities** to publish and manage official sports events with limited capacity.
- **Users** to **join existing events or create their own private events** anywhere.
- Enable sports participation even outside the user’s usual environment (travel, new cities, temporary stays).
- Build an **inclusive**, accessible, and socially driven sports community that promotes healthy habits and real human relationships.

All of this while maintaining a **robust architecture**, **data consistency**, and **secure role-based access control**.


## 📸 Screenshots (Main Flows)

<table align="center">
  <tr>
    <td align="center"><b>Login</b></td>
    <td align="center"><b>Event Details</b></td>
    <td align="center"><b>Profile / Home</b></td>
  </tr>
  <tr>
    <td><img src="docs/login.png" alt="Login screen" width="260"/></td>
    <td><img src="docs/eventos.png" alt="Event details with seat control" width="260"/></td>
    <td><img src="docs/perfil.png" alt="Profile and home screen" width="260"/></td>
  </tr>
</table>

<br/>




## 🎥 Demo (Real-Time Seat Management)

<p align="center">
  <img src="docs/plazas_realtime.gif" 
       alt="Real-time synchronization of available seats using Firebase Cloud Firestore" 
       width="380"/>
</p>

<p align="center">
  <sub>
    Live demo showing automatic seat updates when a user joins or leaves an event, powered by Cloud Firestore.
  </sub>
</p>



## ✅ Core Features
#### 🔐 Authentication & Roles
- Secure sign-up and login using **alias + password**.
- Role-based system:
  - **User**
  - **Municipality**
- Authentication handled by **Firebase Authentication**.
- Persistent sessions and **role-based access control**.

#### 🏛️ Municipality Capabilities
- Create, edit, and delete **official sports events**.
- Define and manage **maximum participant capacity**.
- View the list of **registered participants** per event.
- Remove participants and automatically **free seats**.
- Automatic **real-time seat synchronization**.
- Centralized sports event management for the local community.

#### 👤 User Capabilities
- Browse **official municipality events**.
- **Create private sports events** in any location.
- Join or leave both **official and private events**.
- View events the user is registered in.
- Participate in sports activities even while **traveling or outside their city**.
- Automatic registration blocking when **no seats are available**.
- Full freedom to organize or participate in sports activities.

#### 🔄 Business Logic & Data Integrity
- **Real-time synchronization** using Cloud Firestore.
- Automatic and consistent **seat availability management**.
- Visibility and permissions controlled by **UID and role**.
- Prevention of duplicates, inconsistent registrations, and invalid states.
- Guaranteed consistency between **events, users, and participants**.



## 📂 Project Structure
```
com.nilson.appsportmate
├── common
│ ├── datos.firebase // Shared Firebase configuration and utilities
│ ├── modelos // Shared models
│ └── utils // Constants, validations, and helpers
│
├── data
│ ├── local // Local data sources (if applicable)
│ ├── remote // Firebase / Firestore access
│ └── repository // Repository implementations
│
├── domain
│ ├── models // Domain models
│ ├── repository // Repository interfaces
│ └── usecase // Use cases (business logic)
│
├── di // Dependency injection
│
├── features // Feature-based modules
│ ├── townhall // Municipality features
│ └── user.ui // User features
│
├── ui
│ ├── auth // Login and registration
│ ├── splash // Splash screen
│ └── shared // Reusable components
│
├── App // Application class
└── MainActivity // Main activity
```


## 🧩 Architecture and Technical Decisions

The application is designed following **Clean Architecture + MVVM** with the goal of **separating responsibilities**, **improving maintainability**, and **facilitating project scalability**.

The business logic is **decoupled from the presentation layer**, which allows:

- Changing the data source (**Firebase, local, or mock**) without affecting the UI.
- Testing **use cases** in isolation.
- Maintaining code that is more **clean, predictable, and easy to evolve**.

The **MVVM architecture** is implemented across the entire application, with a clear separation of responsibilities:

- **UI**: Activities / Fragments (presentation)
- **ViewModel**: state management and presentation logic
- **Data**: repositories, Firebase, and models

All **CRUD operations are implemented manually** (without FirebaseUI) to maintain full control over business logic and data flows.

The organization by **layers** (`data`, `domain`, `ui`) and by **features** reflects a professional approach similar to that used in **real production projects**, preparing the application for future expansion without deep restructuring.



### 🔍 Testing Strategy & Tools
- **White Box Testing (Unit Testing):** 
  - Implemented using **JUnit 4** and **Mockito**.
  - Validation of the internal logic within **Use Cases** and **ViewModels**.
  - Use of **Mocks** to simulate Firebase responses and isolate business logic.
- **Black Box Testing (UI Testing):** 
  - Implemented using **Espresso**.
  - Functional validation of user flows (E2E).
  - Navigation control and UI state verification.

### 🎯 Test Scenarios (E2E and Unit)
- **Authentication Flows:** Login and Registration with valid/invalid data and network error handling (E2E with Espresso).
- **Real-time Slot Logic:** Validation that registration is only possible if slots are available, ensuring consistency in Firestore.
- **Form Validations:** Strict control of required fields, data formats, and user error messaging.
- **Session Persistence:** Verification of the entry flow based on authentication state and user role (User vs. Town Hall).

These tests focus mainly on **Login** and **Sign Up** flows, ensuring security and stability.



## 🌍 Flexible Event Management

SportMate enables **open and decentralized** event management, combining institutional organization with users’ personal initiative.

### 🧩 Supported Event Types
- **Official events**, created and managed by municipalities.
- **Private events**, freely created by users.
- Ability to join existing events or create new ones without geographical restrictions.

### 📍 Real Use and Social Reach

- Users can create or join events even when **outside their usual municipality**.
- The application is useful both locally and during travel, trips, or temporary stays.
- It facilitates spontaneous sports practice and human connection anywhere.

This approach transforms **SportMate** into a **social, inclusive, and scalable** platform designed for everyday use, not limited to a fixed context.



## 🤝 Social Impact of the Project

SportMate is an application with **real social impact**, designed to go beyond simple sports event organization.

- Reduces **sedentary lifestyle** by facilitating regular physical activity.
- Combats **social isolation** by promoting interaction between people with shared interests.
- Encourages **social integration** among people of different ages, cultures, and backgrounds.
- Strengthens **community cohesion**, using sport as a universal language.

The open and participatory approach of the application contributes to creating healthier, more inclusive, and socially connected environments, both locally and in broader contexts.



<a name="tech-section"></a>
## 🛠️ Technologies and Technical Stack
*   **Language:** Java (JDK 11/17) - *Focus on robustness and strong typing.*
*   **Android Jetpack Components:**
    *   **Navigation Component:** Flow management via `nav_graph.xml`.
    *   **ViewModel & LiveData:** Reactive architecture and state persistence across lifecycle changes.
    *   **Lifecycle:** Intelligent UI state management.
*   **Architecture:** Clean Architecture + MVVM (Strict separation of concerns).
*   **Backend & Cloud:** Firebase (Auth, Real-time Firestore, Storage).
*   **UI/UX:** Material Design Components (XML) with a focus on accessibility.
*   **DI (Dependency Injection):** Implemented in the `di/` layer for decoupling.


## 🚀 Installation and Execution

1. Clone the repository:
   ```bash
   git clone https://github.com/NilsonDevCode/SportMate.git
   ```
2. Open the project with Android Studio.
3. Create a project in Firebase:
- Enable Authentication (Email/Password).
- Enable Cloud Firestore.
- (Optional) Firebase Storage.
4. Download the google-services.json file and place it in:
`app/google-services.json`
5. Sync Gradle and run the app on an emulator or physical device.



## 🔒 Security and Best Practices
- Data access restricted by UID.
- Clear separation between users and municipalities.
- Complete form validations.
- Prevention of unauthorized actions.
- Code prepared for advanced Firestore security rules.

## 📊 Project Status
- ✔ Functional and complete
- ✔ Evaluated and approved with excellent grade
- ✔ Solid business logic
- ✔ Clear and maintainable architecture
- ✔ Software tests implemented (white-box and black-box)

## 🔧 Future Improvements
- Expanded instrumented testing
- UI/UX improvements
- Push notifications
- Performance optimization

## 👥 Authorship
Project initially developed as a team (4 members).
This version corresponds to an independent copy, personally maintained and evolved.

**Original team:** Antonio, Jordy, Elio, Nilson
**Maintenance and evolution (personal fork):** Nilson

## 📌 Key Learnings

*   **Jetpack Architecture in Java:** Professional implementation of **ViewModels, LiveData, and Navigation Component** to overcome traditional Android lifecycle limitations and ensure a reactive UI.
*   **Java Mastery and Design Patterns:** Utilization of Java to structure an application under **MVVM and Clean Architecture**, demonstrating a solid foundation in the **JVM** that facilitates a smooth transition to ecosystems like **Spring Boot**.
*   **Real-Time Data Management:** Design and implementation of **complex business logic without FirebaseUI**, maintaining full control over synchronization, slot consistency, and states in **Cloud Firestore**.
*   **Security and Access Control:** Advanced integration of **Firebase Authentication** with a **role-based management system (User/Town Hall)** and application-level permissions.
*   **Dependency Injection and Modularity:** Organized **modular code (data, domain, ui)** and decoupling, enhancing software maintainability and scalability through the `di/` layer.
*   **Software Quality and Testing:** Application of **white-box and black-box testing** methodologies on critical flows such as registration and event enrollment logic.
*   **Cloud Services Integration:** Multimedia file management and data persistence through the combined use of **Firebase Storage** and **Firestore**.
*   **Professional Workflow:** Advanced use of **Git and GitHub** in collaborative environments, managing branches, conflicts, and technical documentation efficiently.


## 📄 License
This project is distributed for educational and demonstrative purposes.
It may be used as a reference while respecting the original authorship.

<br>
<br>
<br>
<br>
<br>
<br>
<br>
<br>
<br>
<br>

## Español

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-blue?style=for-the-badge)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Testing](https://img.shields.io/badge/Testing-JUnit%20%7C%20Espresso%20%7C%20Mockito-success?style=for-the-badge)



> **TL;DR técnico**  
> App Android en Java con Clean Architecture + MVVM y Firebase,  
> eventos oficiales y privados, control de plazas en tiempo real,  
> roles, testing y enfoque social.

<details>
<summary><b>📑 Contenido</b></summary>

- [🎯 Objetivo del proyecto](#-objetivo-del-proyecto)
- [📸 Capturas](#-capturas-flujos-principales)
- [🎥 Demo](#-demo-plazas-en-tiempo-real)
- [✅ Funcionalidades](#-funcionalidades-principales)
- [📂 Estructura del proyecto](#-estructura-del-proyecto)
- [🧩 Arquitectura](#-arquitectura-y-decisiones-técnicas)
- [🧪 Testing](#-testing-y-calidad-del-software)
- [🌍 Gestión de eventos](#-gestión-flexible-de-eventos)
- [🤝 Impacto social](#-impacto-social-del-proyecto)
- [🛠️ Tecnologías](#-Tecnologías-y-Stack-Técnico)
- [🚀 Instalación](#-instalación-y-ejecución)
- [📊 Estado del proyecto](#-estado-del-proyecto)
- [🔧 Futuras mejoras](#-futuras-mejoras)
- [👥 Autoría](#-autoría)
- [📌 Aprendizajes](#-aprendizajes-clave)

</details>


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

## 📸 Capturas (flujos principales)

<table align="center">
  <tr>
    <td align="center"><b>Login</b></td>
    <td align="center"><b>Detalle del evento</b></td>
    <td align="center"><b>Perfil / Inicio</b></td>
  </tr>
  <tr>
    <td><img src="docs/login.png" alt="Pantalla de Login" width="260"/></td>
    <td><img src="docs/eventos.png" alt="Detalle del evento con plazas" width="260"/></td>
    <td><img src="docs/perfil.png" alt="Pantalla de Perfil e Inicio" width="260"/></td>
  </tr>
</table>

<br/>

## 🎥 Demo (plazas en tiempo real)

<p align="center">
  <img src="docs/plazas_realtime.gif" 
       alt="Sincronización de plazas en tiempo real con Firebase" 
       width="380"/>
</p>
<p align="center">
  <sub>
    Demo real de la sincronización automática de plazas al inscribirse o darse de baja de un evento, usando Cloud Firestore.
  </sub>
</p>



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

## 📂 Estructura del proyecto

```
com.nilson.appsportmate
├── common
│ ├── datos.firebase // Configuración y utilidades comunes de Firebase
│ ├── modelos // Modelos compartidos
│ └── utils // Constantes, validaciones y helpers
│
├── data
│ ├── local // Fuentes de datos locales (si aplica)
│ ├── remote // Acceso a Firebase / Firestore
│ └── repository // Implementaciones de repositorios
│
├── domain
│ ├── models // Modelos de dominio
│ ├── repository // Interfaces de repositorios
│ └── usecase // Casos de uso (lógica de negocio)
│
├── di // Inyección de dependencias
│
├── features // Módulos por funcionalidad
│ ├── townhall // Funcionalidades de ayuntamiento
│ └── user.ui // Funcionalidades de usuario
│
├── ui
│ ├── auth // Login y registro
│ ├── splash // Pantalla inicial
│ └── shared // Componentes reutilizables
│
├── App // Clase Application
└── MainActivity // Activity principal
```
## 🧩 Arquitectura y decisiones técnicas

La aplicación está diseñada siguiendo **Clean Architecture + MVVM** con el objetivo de **separar responsabilidades**, **mejorar la mantenibilidad** y **facilitar la escalabilidad** del proyecto.

La lógica de negocio se encuentra **desacoplada de la capa de presentación**, lo que permite:

- Cambiar la fuente de datos (**Firebase, local o mock**) sin afectar a la UI.
- Testear los **casos de uso** de forma aislada.
- Mantener un código más **limpio, predecible y fácil de evolucionar**.

La arquitectura **MVVM** está implementada en toda la aplicación, con una separación clara de responsabilidades:

- **UI**: Activities / Fragments (presentación)
- **ViewModel**: gestión de estado y lógica de presentación
- **Datos**: repositorios, Firebase y modelos

Los **CRUDs están implementados manualmente** (sin FirebaseUI) para tener control total sobre la lógica de negocio y los flujos de datos.

La organización por **capas** (`data`, `domain`, `ui`) y por **features** refleja un enfoque profesional, similar al utilizado en **proyectos reales de producción**, y prepara la aplicación para futuras ampliaciones sin necesidad de reestructuraciones profundas.

## 🧪 Testing y calidad del software

El proyecto incluye un plan de **pruebas de software reales**, diseñado para garantizar la fiabilidad de los procesos críticos y la integridad de los datos en Firebase.

### 🔍 Estrategia de Testing y Herramientas
- **Pruebas de Caja Blanca (Unit Testing):** 
  - Implementadas con **JUnit 4** y **Mockito**.
  - Validación de la lógica interna de los **Use Cases** y **ViewModels**.
  - Uso de **Mocks** para simular las respuestas de Firebase y aislar la lógica de negocio.
- **Pruebas de Caja Negra (UI Testing):** 
  - Implementadas con **Espresso**.
  - Validación funcional de flujos desde la perspectiva del usuario (E2E).
  - Control de navegación y estados de la interfaz.

### 🎯 Escenarios de prueba (E2E y Unitarios)
- **Flujos de Autenticación:** Login y Registro con datos válidos, inválidos y gestión de errores de red (E2E con Espresso).
- **Lógica de Plazas en Tiempo Real:** Validación de que la inscripción solo es posible si hay plazas disponibles, garantizando la consistencia en Firestore.
- **Validaciones de Formulario:** Control estricto de campos obligatorios, formatos de datos y gestión de mensajes de error al usuario.
- **Persistencia de Sesión:** Verificación del flujo de entrada según el estado de autenticación y el rol del usuario (Usuario vs. Ayuntamiento).

Estas pruebas se centran principalmente en los flujos de **Login** y **Sign Up**, garantizando seguridad y estabilidad.
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

## 🤝 Impacto social del proyecto

SportMate es una aplicación con **impacto social real**, diseñada para ir más allá de la simple organización de eventos deportivos.

- Reduce el **sedentarismo**, facilitando la práctica deportiva regular.
- Combate el **aislamiento social**, promoviendo la interacción entre personas con intereses comunes.
- Fomenta la **integración social** entre personas de distintas edades, culturas y contextos.
- Refuerza la **cohesión comunitaria**, utilizando el deporte como lenguaje universal.

El enfoque abierto y participativo de la aplicación contribuye a crear entornos más saludables, inclusivos y socialmente conectados, tanto a nivel local como en contextos más amplios.


## 🛠️ Tecnologías y Stack Técnico
*   **Lenguaje:** Java (JDK 11/17) - *Enfoque en robustez y tipado fuerte.*
*   **Android Jetpack Components:**
    *   **Navigation Component:** Gestión de flujos mediante `nav_graph.xml`.
    *   **ViewModel & LiveData:** Arquitectura reactiva y persistencia de estado ante cambios de ciclo de vida.
    *   **Lifecycle:** Gestión inteligente de estados de la UI.
*   **Arquitectura:** Clean Architecture + MVVM (Separación estricta de responsabilidades).
*   **Backend & Cloud:** Firebase (Auth, Firestore en tiempo real, Storage).
*   **UI/UX:** Material Design Components (XML) con enfoque en accesibilidad.
*   **DI (Inyección de Dependencias):** Implementada en la capa `di/` para desacoplamiento.


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

- **Equipo original:** Antonio, Jordy, Elio, Nilson  
- **Mantenimiento y evolución (fork personal):** Nilson  


## 📌 Aprendizajes clave

*   **Arquitectura Jetpack en Java:** Implementación profesional de **ViewModels, LiveData y Navigation Component** para solventar las limitaciones tradicionales del ciclo de vida de Android y garantizar una UI reactiva.
*   **Dominio de Java y Patrones de Diseño:** Uso de Java para estructurar una aplicación bajo **MVVM y Clean Architecture**, demostrando una base sólida en la **JVM** que facilita la transición hacia ecosistemas como **Spring Boot**.
*   **Gestión de Datos en Tiempo Real:** Diseño e implementación de **lógica de negocio compleja sin FirebaseUI**, manteniendo control total sobre la sincronización, consistencia de plazas y estados en **Cloud Firestore**.
*   **Seguridad y Control de Acceso:** Integración avanzada de **Firebase Authentication** con un sistema de **gestión de roles (Usuario/Ayuntamiento)** y permisos a nivel de aplicación.
*   **Inyección de Dependencias y Modularidad:** Organización de código **modular (data, domain, ui)** y desacoplado, facilitando el mantenimiento y la escalabilidad del software mediante la capa `di/`.
*   **Calidad de Software y Testing:** Aplicación de metodologías de **pruebas de caja blanca y caja negra** en flujos críticos como el registro y la lógica de inscripción de eventos.
*   **Integración de Servicios Cloud:** Gestión de archivos multimedia y persistencia de datos mediante el uso combinado de **Firebase Storage** y **Firestore**.
*   **Flujo de Trabajo Profesional:** Uso avanzado de **Git y GitHub** en entornos colaborativos, gestionando ramas, conflictos y documentación técnica de forma eficiente.

## 📄 Licencia

Este proyecto se distribuye con fines educativos y demostrativos.  
Puede utilizarse como referencia respetando la autoría original.

