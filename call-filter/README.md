# Filtro 400 · Bloqueo de llamadas con `CallScreeningService`

App Android (Kotlin) que rechaza automáticamente las llamadas entrantes cuyo número empieza por **400**, contemplando los formatos españoles `400…`, `+34 400…` y `0034 400…`. El filtrado lo hace el propio sistema operativo a través de `CallScreeningService` y el rol `ROLE_CALL_SCREENING`, sin servidor externo.

- **Vídeo de demostración (OneDrive):** `PEGAR_ENLACE_AQUÍ`
- **Lenguaje / IDE:** Kotlin · Android Studio
- **minSdk 29 (Android 10)** · targetSdk 35

## Funcionamiento

1. El usuario pulsa **Activar filtro** y Android muestra su diálogo para conceder a la app el rol de filtrado de llamadas (`ROLE_CALL_SCREENING`).
2. Con el rol concedido, cada vez que entra una llamada Telecom enlaza `CallFilterService` y llama a `onScreenCall()`.
3. El servicio lee el número (`callDetails.handle`), lo normaliza y aplica la regla.
4. Responde con `CallResponse`: si el número empieza por 400 la rechaza (`setDisallowCall(true)` + `setRejectCall(true)`); si no, la deja continuar con normalidad.
5. La interfaz muestra el estado del rol, un contador de llamadas bloqueadas por el servicio y un comprobador de números de prueba.

## Estructura

```
app/src/main/java/com/sha2dev/callfilter/
├── PhoneNumberFilter.kt    # Lógica pura: normalización + regla del prefijo 400
├── CallFilterService.kt    # CallScreeningService: decide y responde a cada llamada
├── MainActivity.kt         # UI mínima: solicitar rol, estado, contador, comprobador
└── BlockStats.kt           # Contador de llamadas bloqueadas (SharedPreferences)
app/src/test/.../PhoneNumberFilterTest.kt   # Tests unitarios de la lógica
```

## Decisiones técnicas

- **Lógica separada del servicio.** `PhoneNumberFilter` no depende de Android, así que se prueba con tests JVM rápidos, sin emulador, y el servicio queda reducido a leer el número y responder.
- **Normalización antes de comparar.** Se eliminan espacios, guiones, puntos y paréntesis. Si el número empieza por `+34` o `0034` se quita ese prefijo; así `+34 400 123 456`, `0034-400-123-456` y `400123456` dan el mismo resultado.
- **Números extranjeros protegidos.** `+44 400…`, `0044 400…` o `+1 400…` conservan su prefijo internacional al normalizarse, por lo que no se bloquean por error. Tampoco se bloquea un `400` que no esté al principio (`+34 91 400 12 34`).
- **Solo llamadas entrantes.** Se comprueba `callDirection == DIRECTION_INCOMING`; las salientes siempre se permiten.
- **Número oculto.** Si `handle` es `null`, la llamada continúa (no hay número que evaluar).
- **Rechazo transparente.** Se rechaza como si el usuario colgara pero se mantiene el registro de llamadas y la notificación de llamada perdida (`setSkipCallLog(false)`, `setSkipNotification(false)`), para que el usuario pueda ver qué se bloqueó.
- **Privacidad.** El log solo escribe el prefijo de 3 dígitos y el contador no guarda números. La app no pide ningún permiso peligroso: solo el rol.
- **Vistas XML + Material 3** en lugar de Compose, para una interfaz mínima con pocas dependencias.
- **No se trata `34400…` sin `+` ni `00`** como número español: sin indicador internacional es ambiguo, y Android entrega normalmente los números en formato internacional (`+34…`) o nacional.

## Cómo ejecutarlo

1. Abre la carpeta en Android Studio y espera a la sincronización de Gradle (puede sugerir actualizar versiones de AGP/Kotlin; es seguro aceptar).
2. Ejecuta la app en un dispositivo o emulador con Android 10 o superior y pulsa **Activar filtro**.
3. Para probar sin llamar desde otro teléfono, en el emulador:

```bash
adb emu gsm call 400123456        # debe bloquearse
adb emu gsm call +34400123456     # debe bloquearse
adb emu gsm call 0034400123456    # debe bloquearse
adb emu gsm call 612345678        # debe sonar con normalidad
adb emu gsm cancel 612345678      # colgar la llamada de prueba
```

   Para ver la decisión del servicio: `adb logcat -s CallFilterService`.

> En el emulador el número de prueba no está en contactos. En un teléfono real, haz la prueba con un número que no tengas guardado (ver pregunta 4).

### Tests unitarios

```bash
./gradlew testDebugUnitTest
```

También desde Android Studio: clic derecho sobre `PhoneNumberFilterTest` → *Run*.

## Pruebas y evidencias

> Sustituir cada marcador por la captura real (guardar en `docs/img/`).

| Evidencia | Captura |
|---|---|
| Interfaz con el filtro inactivo | `![inactivo](docs/img/01-inactivo.png)` |
| Diálogo del sistema para conceder el rol | `![rol](docs/img/02-rol.png)` |
| Interfaz con el filtro activo | `![activo](docs/img/03-activo.png)` |
| Llamada desde 400… bloqueada + contador incrementado | `![bloqueo](docs/img/04-bloqueo.png)` |
| Llamada desde un número normal que sí suena | `![permitida](docs/img/05-permitida.png)` |
| Salida de logcat (`CallFilterService`) | `![logcat](docs/img/06-logcat.png)` |
| Tests unitarios en verde | `![tests](docs/img/07-tests.png)` |

Casos cubiertos por los tests unitarios: `400…` sin prefijo, `+34`, `0034`, con espacios/guiones/puntos/paréntesis, solo el prefijo, móviles y fijos normales, `400` en medio del número, números extranjeros con `400`, prefijos parciales (`40`, `4`, `+34`), `null`, vacío y texto.

## Reflexión crítica

**1. ¿Qué es `CallScreeningService` y qué función cumple en Android?**
Es un servicio de Telecom que permite a una app inspeccionar las llamadas nuevas y responder al sistema, antes de que suenen, si deben permitirse, silenciarse, rechazarse, ocultarse del registro o no notificarse. Es la API oficial para apps de bloqueo de spam e identificación de llamadas, y desde Android 10 no requiere ser la app de teléfono por defecto ni permisos como `READ_CALL_LOG`.

**2. ¿Por qué necesitamos solicitar el rol `ROLE_CALL_SCREENING`?**
Porque filtrar llamadas es una capacidad sensible (puede impedir que el usuario reciba llamadas) y Android no deja que cualquier app la tenga. El sistema de roles hace que el usuario conceda explícitamente esa función a una única app, mediante un diálogo del propio sistema. Sin el rol, Telecom no enlaza el servicio aunque esté declarado en el manifiesto.

**3. ¿Por qué no necesitamos un servidor externo para realizar el filtrado?**
Porque la regla es local y determinista: basta con mirar el prefijo del número, que Telecom entrega al servicio en el propio dispositivo. No hay que consultar listas negras ni reputación. Esto da respuesta inmediata (la decisión debe llegar rápido, antes de que suene el teléfono), funciona sin conexión y no envía los números de las llamadas a terceros, lo que mejora la privacidad. Un servidor solo haría falta para reglas dinámicas, como una base de datos de spam.

**4. ¿Qué ocurre si el número telefónico está guardado en los contactos?**
Según la documentación oficial de Android, `onScreenCall()` se invoca para llamadas de números que **no** están en la lista de contactos del usuario. Por tanto, un número que empiece por 400 pero esté guardado en contactos no pasaría por el filtro y sonaría con normalidad. Es una decisión de diseño de la plataforma: se asume que lo que el usuario guardó es deseado. Conviene tenerlo en cuenta al hacer pruebas con un teléfono real.

**5. ¿Qué diferencias existen entre comprobar un número mediante una función y bloquear realmente una llamada?**
La función (`shouldBlock`, el comprobador de la interfaz) solo contesta *sí o no* a un texto que escribe el usuario; no interviene en nada. Bloquear de verdad exige estar integrado con Telecom: tener el rol concedido, que el sistema enlace el servicio ante una llamada real, recibir el número en el formato que entregue Android, responder dentro del plazo con un `CallResponse`, y aceptar las reglas de la plataforma (como la excepción de contactos). Por eso los tests unitarios validan la regla, pero solo una llamada real o simulada con el emulador demuestra el bloqueo. Además, hay casos que la función no ve: las llamadas con número oculto no se pueden evaluar, y algunos fabricantes añaden ajustes propios (ahorro de batería, gestores de llamadas) que pueden afectar al comportamiento.

**6. ¿Qué mejoras incorporaría en una futura versión?**
- Lista de prefijos configurable por el usuario (y lista blanca de excepciones) en lugar de un `400` fijo.
- Historial de llamadas bloqueadas con fecha, consultable desde la app (con opción de borrarlo).
- Opción de elegir entre rechazar, silenciar o dejar sonar sin notificación.
- Normalización con una librería de números (por ejemplo libphonenumber) para soportar otros países y formatos.
- Notificación al bloquear, tests instrumentados con un `Call.Details` simulado y pruebas de interfaz.
- Pantalla de ayuda que explique la excepción de contactos y las llamadas con número oculto.
