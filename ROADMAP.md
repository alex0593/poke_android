# Roadmap — Pokédex Android

## 1. Base funcional — implementada

- [x] Proyecto Android nativo: Kotlin, Compose y Navigation Compose.
- [x] Retrofit + OkHttp + Kotlinx Serialization y corrutinas.
- [x] Conexión a `https://pokedex-backend-kor1.onrender.com/`.
- [x] Pokédex paginada, búsqueda con debounce y filtros combinados por tipo.
- [x] Detalles con estadísticas, habilidades, ilustraciones y sprites.
- [x] Catálogos de movimientos, habilidades, objetos y bayas.
- [x] Login/registro, perfil, avatares y logros.
- [x] JWT cifrado con Android Keystore y persistido en DataStore.
- [x] Favoritos remotos de las cinco categorías.
- [x] Trivia con siluetas y temporizador, aventura regional y ranking.
- [x] Manejo de errores y sesión caducada; reintento regional con UUID estable.
- [x] Pruebas de contratos HTTP y estados del ViewModel.
- [x] Flujo de CI para compilación, lint, pruebas y APK descargable.

## 2. Validación de la primera entrega

- [x] Compilación debug y pruebas unitarias locales.
- [x] Análisis Android Lint sin errores.
- [x] Cuatro pruebas instrumentadas de navegación, interfaz y sesión cifrada en emulador Android 15 (API 35), ejecutadas en GitHub Actions.
- [ ] Recorrido visual de la app contra Render.
- [x] Publicar el repositorio como público en GitHub: [alex0593/poke_android](https://github.com/alex0593/poke_android).
- [ ] Verificar los primeros resultados de CI tras publicar.
- [ ] Probar login/registro, favoritos y avance regional con una cuenta de pruebas dedicada. Las pruebas locales automatizadas no modifican producción.

**Criterio de cierre:** APK instalable, navegación y lectura de Render comprobadas, pruebas de CI verdes y enlace público disponible. Los recorridos con cuenta real se documentarán por separado.

## 3. Próxima iteración — experiencia y estabilidad

- [ ] Separar el ViewModel y las pantallas por funcionalidad; mantener los contratos y pruebas actuales.
- [ ] Incorporar Paging 3 para carga automática al desplazarse y recuperación por página.
- [ ] Búsqueda global en catálogos secundarios: requiere soporte de backend; actualmente se filtran los resultados cargados, como en la web.
- [ ] Mover cadenas de interfaz a recursos Android y completar traducciones de tipos/estadísticas. Los textos descriptivos actuales provienen del backend en inglés.
- [ ] Mejorar accesibilidad con TalkBack, tamaños de fuente grandes y orientación horizontal.
- [ ] Ajustar navegación y distribución para tablets y pantallas plegables.
- [ ] Pruebas en Android API 24, 35 y 36, incluyendo pérdida de red, rotación y recreación del proceso.
- [ ] Actualizar dependencias y SDK de forma controlada, comprobando compatibilidad y pruebas.

**Criterio de cierre:** flujos principales accesibles, sin pérdida de estado al recrearse y con pruebas en los niveles Android admitidos.

## 4. Datos offline y sincronización

- [ ] Persistir fichas consultadas mediante Room, con fecha de actualización y límite de almacenamiento.
- [ ] Mostrar datos guardados y su antigüedad cuando no haya conexión.
- [ ] Definir y probar la política de sincronización de favoritos antes de permitir cambios offline.
- [ ] Añadir idempotencia al guardado de trivia libre en el backend. El endpoint actual no admite UUID; no se reintenta automáticamente, pero un reintento manual tras una respuesta perdida puede duplicar el resultado.
- [ ] Evaluar un endpoint de identidad autenticada y renovación de tokens con el backend; actualmente el perfil se consulta por nombre de usuario y la sesión expirada requiere login.

**Criterio de cierre:** consulta offline útil, sincronización reproducible y ausencia de resultados duplicados por reintentos.

## 5. Distribución

- [ ] Preparar firma release fuera del repositorio y generar AAB.
- [ ] Revisar icono adaptativo, splash, capturas y ficha de distribución.
- [ ] Documentar política de privacidad y tratamiento de datos según el backend desplegado.
- [ ] Revisar atribuciones y condiciones de uso de imágenes/datos antes de distribuir en tiendas.
- [ ] Distribuir una beta y recoger fallos antes de una publicación estable.

**Criterio de cierre:** versión firmada reproducible, documentación de distribución completa y beta validada. El APK debug es para pruebas; no sustituye una versión de tienda.

## 6. Estado de la iteración móvil — 18 de septiembre de 2026

- [x] Separar pantallas, componentes comunes y tema para eliminar la duplicación de la UI.
- [x] Recuperar la compilación Kotlin y conservar los flujos de navegación existentes.
- [x] Añadir consulta de Pokémon al azar desde la pantalla principal.
- [x] Mejorar el arranque: interfaz inmediata, tipos y favoritos en paralelo, primera página de 12 elementos y timeout de 35 segundos.
- [x] Optimizar imágenes: sprites pequeños en tarjetas y trivia, caché de memoria y disco de 64 MB para Coil, e ilustraciones grandes solo en detalles.
- [x] Consultar y mostrar cadenas evolutivas desde `GET /evolutions/chain/{id}`.
- [x] Hacer navegables las especies relacionadas y las especies de una cadena evolutiva.
- [x] Añadir prueba de contrato para la respuesta anidada de evoluciones.
- [x] Ejecutar `assembleDebug`, `testDebugUnitTest` y `lintDebug` correctamente con JDK 17 y Android SDK 36.
- [x] Instalar el APK debug en el dispositivo Android conectado (API 34).
- [x] Ejecutar directamente las cuatro pruebas instrumentadas con `adb`; navegación, sesión cifrada y UI pasan.
- [ ] Completar el recorrido visual manual contra Render y capturar una evidencia de pantalla.

### Próxima entrega priorizada

1. Añadir una sección de exploración de regiones, localidades y encuentros usando los endpoints públicos de Render.
2. Añadir búsqueda global para movimientos, habilidades, objetos y bayas cuando el backend exponga búsqueda o un índice completo.
3. Incorporar Paging 3, restauración de posición y persistencia Room para consultas recientes.
4. Completar accesibilidad, traducciones de interfaz, orientación horizontal y pruebas en API 24, 35 y 36.
5. Validar registro, favoritos, avatar, progreso regional y ranking con una cuenta de pruebas dedicada.

**Estado al cierre de esta iteración:** la APK debug compila, las pruebas JVM, lint y las cuatro pruebas instrumentadas pasan; el APK está instalado en un dispositivo API 34 y la consulta de evoluciones ya está conectada. La exploración de regiones y la validación visual manual contra Render son los siguientes hitos verificables.
