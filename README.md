# AppSportMate

Aplicación Android desarrollada como parte del TFG de DAM.  
El objetivo es conectar usuarios y ayuntamientos a través del deporte, permitiendo gestionar eventos, inscribirse y administrar plazas.

---

## 📌 Funcionalidades principales
- **Registro/Login con alias** (rol de Usuario o Ayuntamiento).
- **Gestión de deportes por ayuntamiento**: crear, editar, borrar eventos y controlar las plazas.
- **Inscripción de usuarios**: apuntarse o desapuntarse de un deporte.
- **Control de plazas** en tiempo real (se suman/restan automáticamente).
- **Gestión de inscritos**: el ayuntamiento puede ver y expulsar participantes.
- **Firestore y Firebase Auth** como backend principal.

---

## 🛠️ Tecnologías usadas
- Java (Android Studio)
- Firebase Authentication
- Firebase Firestore
- Material Design Components

---

## 📂 Estructura del proyecto
El código está organizado de forma modular:

com.nilson.appsportmate
├── adapters → RecyclerView.Adapters (eventos, usuarios, etc.)
├── data
│ ├── firebase → Clases de conexión y transacciones con Firestore
│ └── models → Modelos de datos (Usuario, Deporte, etc.)
├── ui → Activities (pantallas principales)
└── utils → Clases de utilidades (Preferencias, validaciones…)


---

## 🚀 Cómo ejecutar el proyecto
1. Clonar el repositorio
   ```bash
   git clone https://github.com/tu-repo/AppSportMate.git
Abrir en Android Studio (versión más reciente).

Conectar el proyecto a tu Firebase:

Crear un proyecto en Firebase.

Descargar el archivo google-services.json y colocarlo en la carpeta app/.

Sincronizar Gradle y ejecutar en un emulador o dispositivo físico.

👥 Equipo

Este proyecto ha sido desarrollado por:

Antonio
Jordy
Elio
Nilson



---

## 📄 .gitignore

```gitignore
# Gradle
.gradle/
build/
*/build/

# Local config
local.properties

# Android Studio
.idea/
*.iml
captures/

# Log/Temp files
*.log
*.tmp
*.temp

# Keystores (no compartir claves privadas)
*.jks
*.keystore

# Firebase
# (permitimos subir google-services.json para que todos trabajen igual)
# google-services.json

# OS files
.DS_Store
Thumbs.db

# 1. Inicializar repo (si no lo tienes)
git init

# 2. Añadir remoto (sustituye la URL por la de tu repo en GitHub)
git remote add origin https://github.com/TU-USUARIO/AppSportMate.git

# 3. Añadir todos los archivos (respetando el .gitignore)
git add .

# 4. Primer commit
git commit -m "Primer commit - AppSportMate TFG DAM"

# 5. Subir al repo remoto
git branch -M main
git push -u origin main
