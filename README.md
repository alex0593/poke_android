# Pokédex Android

Aplicación nativa en Kotlin y Jetpack Compose conectada a **https://pokedex-backend-kor1.onrender.com/**.

Estado de implementación y próximas iteraciones: [ROADMAP.md](ROADMAP.md). Auditoría de puntos de mejora: [AUDIT.md](AUDIT.md).

## Funciones

- Pokédex paginada, búsqueda por nombre y filtros combinados por tipo.
- Fichas con ilustraciones, estadísticas, habilidades y sprites.
- Catálogos de movimientos, habilidades, objetos y bayas; búsqueda sobre los resultados cargados.
- Registro, inicio de sesión, perfil, avatares y logros.
- Favoritos de las cinco categorías sincronizados con la cuenta del backend.
- Trivia libre para invitados y usuarios, aventura por regiones y ranking.
- Estados de carga, vacío, error y reintento; sesión caducada vuelve al modo invitado.

## Compilar

Necesitas JDK 17 o superior **con compilador**, Android SDK 36 y acceso a Maven/Google.

Configura `ANDROID_HOME` con la ruta de tu SDK (o `sdk.dir` en `local.properties`).

```sh
./gradlew assembleDebug
./gradlew testDebugUnitTest lintDebug
./gradlew connectedDebugAndroidTest # con emulador o dispositivo conectado
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Android mínimo: 7.0 (API 24).

La URL se define en `app/build.gradle.kts`, campo `BuildConfig.API_URL`. Android consume las rutas del backend directamente, sin el prefijo `/api` usado por el proxy web.

## Arquitectura

Compose → ViewModel / StateFlow → Repository → Retrofit / OkHttp.

Las funciones de red son suspendibles y se ejecutan con corrutinas ligadas al ViewModel. Kotlinx Serialization acepta campos adicionales y modelos del backend ya transformados. El repositorio conserva hasta 300 fichas en memoria; no hay descarga offline completa.

El JWT se cifra con una clave AES-GCM de Android Keystore antes de persistirlo en DataStore. La app no guarda contraseñas ni incluye credenciales. Las copias de seguridad están desactivadas. OkHttp agrega el token y solo registra datos básicos de solicitudes en debug, sin cuerpos ni encabezados de autenticación. Las escrituras no se reintentan automáticamente; las respuestas regionales conservan su UUID al reintentar manualmente.

Render puede tardar unos segundos en responder al arrancar; la app muestra la interfaz mientras carga y permite reintentar si una llamada supera 35 segundos. Los textos descriptivos de Pokémon y catálogos se muestran en el idioma que devuelve el backend (actualmente inglés); los controles están en español.

Las pruebas de contrato usan MockWebServer y no crean usuarios ni modifican producción. Las pruebas instrumentadas cubren interacción de fichas y persistencia cifrada de sesión.

## Créditos

Backend de referencia: [alex0593/pokedex](https://github.com/alex0593/pokedex). Datos e imágenes proceden de PokeAPI. Pokémon pertenece a Nintendo / Game Freak / The Pokémon Company; proyecto no oficial.
