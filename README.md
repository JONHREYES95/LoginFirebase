# LoginFirebase - Autenticación con Firebase y Jetpack Compose

## Introducción

LoginFirebase es un proyecto de ejemplo que demuestra cómo implementar autenticación de usuarios utilizando Firebase Authentication en una aplicación Android moderna desarrollada con Jetpack Compose. El objetivo principal es mostrar la integración de Firebase para el inicio de sesión y la gestión de usuarios, aprovechando las ventajas de Compose para la construcción de interfaces de usuario reactivas y declarativas.

## Características

- **Inicio de sesión con correo electrónico y contraseña** usando Firebase Authentication.
- **Interfaz moderna** desarrollada completamente con Jetpack Compose.
- **Gestión de estados** mediante ViewModel.
- **Navegación** entre pantallas de login y panel principal.
- **Validación de formularios** y manejo de errores de autenticación.
- **Compatibilidad con Material Design 3**.

## Tecnologías utilizadas

- **Kotlin**
- **Jetpack Compose**
- **Firebase Authentication**
- **AndroidX**
- **Material Design 3**
- **MVVM (Model-View-ViewModel)**

## Estructura del proyecto

app/
├── src/
│   ├── main/
│   │   ├── java/sv/edu/udb/login/
│   │   │   ├── MainActivity.kt
│   │   │   └── gui/
│   │   │       └── AuthViewModel.kt
│   │   └── res/
│   │       ├── layout/
│   │       ├── values/
│   │       └── drawable/
│   └── test/
├── build.gradle.kts
├── google-services.json
└── ...

## Configuración y ejecución

1. **Clona el repositorio:**
   ```bash
   git clone https://github.com/JONHREYES95/LoginFirebase.git

2. Abre el proyecto en Android Studio.
3. Agrega tu archivo google-services.json en la carpeta app/ (ya incluido si descargaste este repositorio).
4. Sincroniza el proyecto para descargar las dependencias.
5. Ejecuta la aplicación en un emulador o dispositivo físico.
## Notas importantes
- Asegúrate de tener configurado un proyecto en Firebase Console y habilitado el método de autenticación por correo y contraseña.
- El archivo google-services.json debe corresponder a tu propio proyecto de Firebase si deseas personalizar la autenticación.
## Capturas de pantalla
*****************
## Conclusiones
Este proyecto demuestra cómo integrar de manera sencilla y efectiva Firebase Authentication en una app Android moderna utilizando Jetpack Compose, siguiendo buenas prácticas de arquitectura y diseño de interfaces.

## Licencia
MIT License
