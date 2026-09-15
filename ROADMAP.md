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
- [ ] Pruebas instrumentadas de interfaz y sesión cifrada en emulador.
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
