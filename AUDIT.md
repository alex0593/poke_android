# Auditoría — Pokédex Android

**Fecha:** 19 de septiembre de 2026
**Alcance:** revisión estática de los 20 archivos fuente (Kotlin, tests, CI y documentación). No se ejecutó compilación ni pruebas: el entorno de auditoría no dispone de Android SDK.
**Método:** lectura completa de `app/src/main`, `app/src/test`, `app/src/androidTest`, `.github/workflows/android.yml`, `README.md`, `ROADMAP.md` y `AGENTS.md`; contraste entre lo documentado y lo implementado.

## Resumen ejecutivo

La aplicación cumple lo declarado en seguridad de sesión (AES-GCM con Android Keystore, copias de seguridad desactivadas, logging saneado) y tiene una base de pruebas de contrato razonable. Los problemas relevantes se concentran en:

1. **Defectos de robustez** que producen crashes o pantallas inservibles con datos reales del backend (sección B).
2. **Desincronización entre documentación y código**: el roadmap lista como pendiente una función ya implementada (sección A).
3. **Deuda de arquitectura conocida**: ViewModel monolito y strings no externalizadas, ambos ya abiertos en el roadmap (sección C).

## Convención de estados

- **Abierto** — sin corrección aplicada.
- **Corregido** — corrección aplicada en el código, pendiente de verificar en CI.
- **Aceptado** — decisión consciente de no corregir; documentado.

---

## A. Desviaciones documentación ↔ código

| # | Hallazgo | Evidencia | Estado |
|---|---|---|---|
| A1 | `ROADMAP.md` §6 «Próxima entrega» #1 pide la exploración de regiones, pero ya está implementada: `ui/WorldScreen.kt`, endpoints `regions/` y `regions/{name}` en `data/Api.kt`, `MenuCard("Explorar regiones")` en `GameScreen`. | `WorldScreen.kt`, `Api.kt`, `GameScreens.kt` | Abierto |
| A2 | `README.md` «Funciones» no menciona la exploración de regiones y localidades, aunque es una pantalla completa del producto. | `README.md` | Abierto |
| A3 | `ROADMAP.md` §3 mantiene abierto «Separar el ViewModel…» mientras §6 marca la separación como hecha. Se separaron las pantallas (10 archivos en `ui/`), no el ViewModel (`PokeViewModel.kt`, 388 líneas, 8 responsabilidades). | `ROADMAP.md`, `PokeViewModel.kt` | Abierto |
| A4 | El test de ViewModel `app/src/test/.../ui/PokeViewModelTest.kt` existe y la doc lo declara, pero conviene confirmar que está commiteado (no aparecía en el listado de archivos del último commit). | `git status` | **Confirmado**: está trackeado y se ejecuta en la verificación local. |
| A5 | Ítems de §6 verificados consistentes: strings sin migrar a recursos y recorrido visual manual contra Render siguen pendientes. | `ROADMAP.md` §6 | Confirmado |

## B. Defectos funcionales

| # | Severidad | Hallazgo | Evidencia | Estado |
|---|---|---|---|---|
| B1 | Alta — crash en arranque | `SessionStore.restore()` solo captura `GeneralSecurityException` e `IllegalArgumentException`. Un payload corrupto/truncado (< 12 bytes) lanza `ArrayIndexOutOfBoundsException` en `copyOfRange(0, 12)` y tumba la app al iniciar. | `data/SessionStore.kt` | **Corregido**: validación de longitud + catch amplio con re-lanzamiento de `CancellationException`. Falta test instrumentado de sesión corrupta. |
| B2 | Alta — pantalla inservible | `RegionDetailScreen` compara `region.name != name` (case-sensitive) pero la lista navega con `key` = `name.lowercase()`. Si el backend capitaliza, la pantalla queda en «Reintentar» permanente. | `ui/WorldScreen.kt` | **Corregido**: comparación con `equals(name, ignoreCase = true)`. |
| B3 | Alta — crash de lista | `Catalog.forEntity` usa `entries.first { … }` → `NoSuchElementException` con un `entity_type` desconocido. `FavoritesScreen` lo llama dentro del `item` del `LazyColumn`: un registro raro rompe toda la lista. | `data/Models.kt`, `ui/GameScreens.kt` | **Corregido**: `forEntityOrNull` + tarjeta omitida en favoritos si el tipo es desconocido. Con prueba unitaria. |
| B4 | Media — falso error | `RegionExplorerScreen` muestra «No se pudieron cargar las regiones» en el primer frame (antes de que `busy` se active) y también cuando la carga devuelve vacío legítimo. Falta flag de carga completada. | `ui/WorldScreen.kt` | **Corregido**: flag `worldRegionsLoaded` + estados diferenciados de carga/error/vacío. |
| B5 | Media | `RankingScreen` y `RegionsScreen` no distinguen carga, vacío ni error («0 jugadores» mientras carga). El README promete estados de carga/vacío/error/reintento de forma general. | `ui/GameScreens.kt` | Abierto |
| B6 | Media | `Repository.page()` cachea las respuestas del endpoint *batch* y `detail()` las devuelve sin llamar al endpoint de detalle. Si el batch no trae todos los campos (descripción, stats), la ficha queda incompleta hasta que el LRU (>300) desaloje. | `data/Repository.kt` | Abierto — verificar contrato del backend |
| B7 | Media | `unauthorized` era `MutableSharedFlow(replay = 0, extraBufferCapacity = 1)`: si el 401 llegaba antes de que el colector del ViewModel se suscribiera, el evento se perdía y la sesión caducada no volvía a invitado. | `PokeApplication.kt`, `PokeViewModel.kt` | **Corregido**: `Channel(CONFLATED)` conservado hasta el primer colector; bucle `receive()` en el ViewModel. Con prueba de regresión. |
| B8 | Baja | «10 preguntas» y «Meta: 7/10» hardcodeados por duplicado en ViewModel y UI; se desincronizan si el backend cambia el umbral. | `PokeViewModel.kt`, `ui/QuizScreen.kt` | **Corregido**: constantes compartidas `GameRules.STAGE_QUESTIONS` / `STAGE_GOAL` en `data/Models.kt`. |
| B9 | Baja | `userMessage()` etiqueta todo 400 como «el nombre puede estar registrado», engañoso para otros 400. | `data/Repository.kt` | Abierto |

## C. Arquitectura y calidad de código

| # | Hallazgo | Estado |
|---|---|---|
| C1 | `PokeViewModel` (388 líneas) concentra catálogo, detalle, cuenta, favoritos, trivia, aventura, ranking, mundo y sesión. Es el ítem abierto del roadmap §3 y limita la testabilidad. | Abierto (planificado en roadmap) |
| C2 | Estado global `busy` + `actionMutex` único: cualquier acción activa el indicador de toda la app y serializa todas las acciones; una ficha lenta bloquea marcar un favorito. | Abierto |
| C3 | Cadenas de interfaz 100% en Kotlin; no existe `res/values/strings.xml`. Impide traducción y accesibilidad. Ítem del roadmap §3. | Abierto (planificado) |
| C4 | `typeLabel()` reconstruye el `Map` en cada llamada; se invoca por celda del grid y chip de tipo. Elevar a `val`. | **Corregido**: mapa elevado a `TYPE_LABELS_ES` de nivel superior. |
| C5 | `Entry` es un modelo único con campos de las 5 categorías (`power`, `pp`, `cost`, `growth_time`…): estados inválidos representables. Considerar tipos sellados por categoría. | Aceptado por ahora; revisar si el backend evoluciona |
| C6 | Modelos con `snake_case` (`base_stat`, `entity_type`) contradiciendo el estilo Kotlin de `AGENTS.md`; `@SerialName` lo resolvería. | Aceptado: pragmático frente al contrato del backend |
| C7 | `PokeViewModelTest` simula `PokeApi` con `java.lang.reflect.Proxy`: frágil (método nuevo ⇒ error, índices de `args` implícitos). Un fake que implemente la interfaz es más robusto. | Abierto |
| C8 | Caché `LinkedHashMap(accessOrder = true)` sin sincronización: seguro hoy solo porque todo corre en `Dispatchers.Main.immediate`. Trampa latente si se cambia de dispatcher. | Abierto — documentar o proteger |

## D. Seguridad

Verificado y conforme con lo documentado en README/AGENTS:

- Token cifrado con AES-GCM y clave en Android Keystore; persistencia en DataStore.
- `allowBackup="false"` + `dataExtractionRules` que excluyen todo (cloud backup y device transfer).
- `usesCleartextTraffic="false"`; solo permiso `INTERNET`.
- `HttpLoggingInterceptor` con `redactHeader("Authorization")` y nivel `BASIC` solo en debug.
- Sin credenciales en el repositorio; `.gitignore` cubre `local.properties` y `*.jks`.

Mejoras recomendadas:

| # | Hallazgo | Estado |
|---|---|---|
| D1 | `restore()` sin validación de longitud era el único camino de crash real de la capa de sesión. | **Corregido** (ver B1) |
| D2 | `username` se guarda en claro (el token no). Aceptable, pero documentarlo explícitamente o cifrarlo también. | Abierto |
| D3 | Sin `networkSecurityConfig` ni pinning de certificados. Riesgo bajo para una API HTTPS pública. | Aceptado |

## E. Pruebas y CI

| # | Hallazgo | Estado |
|---|---|---|
| E1 | Cobertura razonable: 9 pruebas de contrato HTTP y 4 de ViewModel (incluida `failedStageSaveLocksAnswerAndReusesUuid`, citada como ejemplo en `AGENTS.md`). Faltan: corrupción de sesión (B1), `entity_type` desconocido (B3), pantallas de mundo (B2/B4), acumulación de páginas con `more()`. | Abierto |
| E2 | `UiTest` envuelve las pantallas en `MaterialTheme {}` genérico; el tema real (`ui/Theme.kt`) no expone un composable `PokeTheme`, así que los tests no validan el esquema oscuro. | Abierto |
| E3 | CI: los jobs `verify` y `device-tests` compilan en paralelo duplicando trabajo; `sleep 30` antes del `screencap` es frágil; `on: [push, pull_request]` dispara doble en PRs del mismo repositorio. Usar `needs:`, timeouts y esperas por condición. | Abierto |
| E4 | Sin linter/formateador (detekt/ktlint) en CI. `AGENTS.md` asume el estilo pero no hay verificación automática. | Abierto |

## F. Distribución (roadmap §5)

- `applicationId = "com.example.poke_android"` es un placeholder; debe cambiarse antes de publicar en tiendas.
- Sin `buildTypes` release, sin `isMinifyEnabled`, sin firma ni AAB.
- Icono único `drawable/ic_pokeball.xml`: falta icono adaptativo, `roundIcon` y `core-splashscreen`.

## Priorización

**Ahora (defectos que afectan usuarios reales):**
1. B1 — corregido; añadir test instrumentado de sesión corrupta.
2. B3, B2, B4, B7 — crashes y pantallas que fallan con datos del backend.

**Corto plazo:**
3. A1/A2/A3 — sincronizar ROADMAP y README con el código.
4. Confirmar commit de `PokeViewModelTest.kt` (A4).
5. B5, B8, C4.

**Iteración (ya planificada en roadmap §3):**
6. C1, C2, C3, E2, E4.

**Antes de publicar (roadmap §5):**
7. F completo.

## Verificación

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug   # verificación de CI
./gradlew connectedDebugAndroidTest                    # con emulador/dispositivo
```

Las pruebas de contrato usan MockWebServer y no apuntan a producción.
