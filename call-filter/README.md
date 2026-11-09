# 📞 CallFilter — Aplicación Android para el filtrado de llamadas

**CallFilter** es una aplicación Android desarrollada en Kotlin que permite identificar, filtrar y bloquear llamadas telefónicas entrantes mediante reglas personalizadas.

El objetivo del proyecto es ofrecer al usuario un mayor control sobre las llamadas que recibe, permitiéndole configurar números y prefijos bloqueados, crear excepciones y consultar un registro de actividad desde una interfaz moderna e intuitiva.

La aplicación utiliza la API nativa `CallScreeningService` de Android para analizar las llamadas entrantes y decidir si deben permitirse o rechazarse.

---

## 🚀 Funcionalidades principales

### 🏠 1. Pantalla de inicio

La pantalla principal proporciona una visión general del estado de protección de la aplicación.

Incluye:
- Estado de activación del filtro de llamadas.
- Número de reglas configuradas y reglas activas.
- Resumen de llamadas revisadas y bloqueadas.
- Porcentaje de llamadas bloqueadas.
- Actividad reciente.
- Acceso directo al simulador y a los ajustes.

### 🚫 2. Gestión de reglas

La aplicación permite crear y administrar diferentes tipos de reglas para controlar las llamadas entrantes.

Se distinguen tres tipos:

- **Prefijos bloqueados:** rechaza cualquier número que comience por un prefijo determinado.
- **Números bloqueados:** rechaza números telefónicos concretos.
- **Lista blanca:** permite números específicos aunque coincidan con alguna regla de bloqueo de menor prioridad.

Además, permite:
- Añadir nuevas reglas.
- Editar y eliminar reglas existentes.
- Activar o desactivar reglas individualmente.
- Buscar números y prefijos.
- Configurar prioridades.
- Establecer franjas horarias para aplicar determinadas reglas.

### 📋 3. Registro de actividad

La aplicación mantiene un historial de las llamadas revisadas por el filtro.

Permite:
- Consultar llamadas bloqueadas y permitidas.
- Diferenciar las llamadas reales de las simulaciones.
- Filtrar los registros por categoría.
- Consultar la fecha y hora de cada evento.
- Identificar la regla que ha provocado el bloqueo.
- Consultar estadísticas de actividad.
- Eliminar el historial cuando se desee.

Por privacidad, los números se almacenan enmascarados en el registro.

### 🧪 4. Simulador de llamadas

Incluye un simulador para comprobar el funcionamiento de las reglas sin necesidad de recibir una llamada real.

Permite:
- Introducir un número telefónico.
- Seleccionar una hora para realizar la comprobación.
- Ejecutar el motor de filtrado.
- Consultar si la llamada sería bloqueada o permitida.
- Identificar la regla que ha provocado la decisión.
- Guardar el resultado en el historial como una prueba.

Esta funcionalidad facilita comprobar las reglas antes de utilizar el filtro con llamadas reales.

### ⚙️ 5. Ajustes y configuración

La pantalla de ajustes permite personalizar diferentes aspectos de la aplicación:

- Activar la protección mediante el permiso correspondiente de Android.
- Cambiar entre tema claro, oscuro o automático.
- Importar reglas desde archivos CSV.
- Exportar las reglas configuradas a CSV.
- Consultar el estado de protección.
- Gestionar y borrar el historial.

---

## 🛠️ Tecnologías utilizadas

| Tecnología | Utilización |
|---|---|
| Kotlin | Lenguaje principal de programación. |
| Android Studio | Entorno de desarrollo. |
| Jetpack Compose | Desarrollo de la interfaz gráfica. |
| Material 3 | Componentes y diseño visual. |
| CallScreeningService | Identificación y filtrado de llamadas entrantes. |
| SQLite | Almacenamiento local de reglas e historial. |
| SharedPreferences | Persistencia de preferencias de apariencia. |
| JUnit | Pruebas unitarias. |
| Gradle | Automatización de la compilación. |

### Requisitos técnicos

- Android 10 o superior (API 29).
- Android Studio.
- JDK 11 o compatible con la versión de Gradle utilizada.
- SDK de Android correspondiente al proyecto.

**Configuración actual:**
- `minSdk`: 29.
- `targetSdk`: 37.
- `compileSdk`: 37.
- `versionName`: 1.0.

---

## 🧩 Estructura del proyecto

La aplicación está organizada en diferentes archivos Kotlin, separando la interfaz gráfica, la lógica de filtrado y el almacenamiento de datos.

```text
call-filter/
│
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/com/example/callfilter/
│       │   │   │
│       │   │   ├── MainActivity.kt
│       │   │   ├── HomeScreen.kt
│       │   │   ├── RulesScreen.kt
│       │   │   ├── LogScreen.kt
│       │   │   ├── SettingsScreen.kt
│       │   │   ├── TestScreen.kt
│       │   │   ├── Components.kt
│       │   │   ├── AppTheme.kt
│       │   │   ├── FilterDbHelper.kt
│       │   │   ├── FilterEngine.kt
│       │   │   ├── CallFilterService.kt
│       │   │   └── ...
│       │   │
│       │   ├── res/
│       │   │   ├── drawable/
│       │   │   ├── mipmap/
│       │   │   ├── values/
│       │   │   └── values-night/
│       │   │
│       │   └── AndroidManifest.xml
│       │
│       ├── test/
│       │   └── java/com/example/callfilter/
│       │       ├── CallFilterTest.kt
│       │       └── FilterEngineTest.kt
│       │
│       └── androidTest/
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── README.md
```

### Organización del código

- **MainActivity.kt:** punto de entrada de la aplicación, navegación entre pantallas y gestión del permiso de filtrado.
- **HomeScreen.kt:** pantalla principal y resumen del estado de protección.
- **RulesScreen.kt:** gestión de números, prefijos y excepciones.
- **LogScreen.kt:** visualización del historial de actividad.
- **SettingsScreen.kt:** configuración de la aplicación.
- **TestScreen.kt:** simulación de llamadas y comprobación de reglas.
- **Components.kt:** componentes reutilizables de la interfaz.
- **AppTheme.kt:** configuración visual y colores de la aplicación.
- **FilterDbHelper.kt:** gestión de la base de datos SQLite.
- **FilterEngine.kt:** motor encargado de evaluar las reglas.
- **CallFilterService.kt:** servicio Android que analiza las llamadas entrantes.

---

## ⚙️ Funcionamiento del filtrado de llamadas

El filtrado se realiza mediante el servicio `CallFilterService`, que hereda de la clase `CallScreeningService` proporcionada por Android.

El funcionamiento general es el siguiente:

1. El usuario activa la protección desde la aplicación.
2. Android solicita autorización mediante el rol `ROLE_CALL_SCREENING`.
3. Una vez concedido el rol, Android puede invocar el servicio cuando recibe una llamada que debe analizar.
4. El servicio obtiene el número telefónico y consulta las reglas activas almacenadas en SQLite.
5. El motor `FilterEngine` normaliza el número y evalúa las reglas teniendo en cuenta su prioridad y sus franjas horarias.
6. Si la regla seleccionada es de bloqueo, el servicio rechaza la llamada.
7. Si no existe ninguna regla coincidente o prevalece una regla de la lista blanca, la llamada se permite.
8. Finalmente, el resultado se registra en la base de datos local.

### Tipos de decisiones

| Situación | Resultado |
|---|---|
| Coincide con un prefijo bloqueado | Llamada rechazada |
| Coincide con un número bloqueado | Llamada rechazada |
| Coincide con una excepción permitida | Llamada permitida |
| No coincide con ninguna regla | Llamada permitida |
| La regla está desactivada | No se aplica |
| La regla está fuera de su horario | No se aplica |

Las reglas se evalúan según su prioridad. En caso de empate, la lista blanca tiene preferencia.

---

## 📥 Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/sha2dev56/pmdm-udit.git
```

### 2. Abrir el proyecto

Abre Android Studio y selecciona la carpeta:

```text
pmdm-udit/call-filter
```

Espera a que finalice la sincronización de Gradle.

### 3. Ejecutar la aplicación

Conecta un dispositivo Android compatible o inicia un emulador con Android 10 o superior.

Pulsa **Run** desde Android Studio para instalar y ejecutar la aplicación.

### 4. Activar la protección

Al abrir la aplicación:

1. Accede a la pantalla de inicio.
2. Pulsa el botón de activación de la protección.
3. Android mostrará el diálogo del sistema para conceder el rol de filtrado de llamadas.
4. Acepta la solicitud.
5. Comprueba que el estado de protección aparece como activo.

**Importante:** instalar la aplicación no activa automáticamente el filtrado. Es necesario conceder el rol correspondiente desde Android.

---

## 🧪 Pruebas

El proyecto incluye pruebas unitarias para comprobar el comportamiento del motor de filtrado.

Se contemplan casos como:

- Bloqueo mediante prefijos.
- Bloqueo de números concretos.
- Normalización de números telefónicos.
- Aplicación de listas blancas.
- Comprobación de prioridades.
- Reglas desactivadas.
- Franjas horarias normales y nocturnas.
- Números ocultos o vacíos.
- Comprobación de horas válidas e inválidas.

### Ejecutar las pruebas

En Windows, desde PowerShell:

```powershell
.\gradlew.bat testDebugUnitTest
```

También se pueden ejecutar desde Android Studio haciendo clic derecho sobre las clases de pruebas y seleccionando **Run**.

El simulador integrado permite realizar comprobaciones adicionales sin necesidad de efectuar llamadas reales.

---

## 🔐 Privacidad y permisos

CallFilter está diseñada para realizar el filtrado de llamadas directamente en el dispositivo.

- Las reglas se almacenan localmente mediante SQLite.
- El historial se guarda en la base de datos del dispositivo.
- Los números se enmascaran antes de almacenarse en el registro.
- No es necesario utilizar un servidor externo para evaluar las reglas.
- El acceso al filtrado de llamadas se gestiona mediante el rol oficial de Android `ROLE_CALL_SCREENING`.

La aplicación declara el servicio de filtrado protegido por el permiso de sistema `BIND_SCREENING_SERVICE`.

El usuario mantiene el control sobre la activación de la protección y las reglas configuradas.

---

## 🔮 Posibles mejoras futuras

Algunas funcionalidades que podrían incorporarse en próximas versiones son:

- Incorporar estadísticas más avanzadas y gráficos.
- Añadir categorías y etiquetas personalizadas para las reglas.
- Permitir copias de seguridad y restauración de la configuración.
- Mejorar las opciones de importación y exportación.
- Incorporar notificaciones personalizables cuando se bloquee una llamada.
- Añadir más opciones de personalización visual.
- Ampliar las pruebas automatizadas de integración y de interfaz.

---

## 🧠 Reflexión crítica

### 1. ¿Qué es CallScreeningService y qué función cumple en Android?

`CallScreeningService` es una clase que proporciona Android para que las aplicaciones puedan analizar las llamadas telefónicas entrantes antes de que se establezca la comunicación.

En CallFilter utilizamos este servicio para recibir información sobre el número que está realizando la llamada y comprobar si coincide con alguna de las reglas configuradas por el usuario.

A partir del resultado del análisis, la aplicación puede decidir si permite o rechaza la llamada mediante la respuesta correspondiente del sistema Android.

Su principal ventaja es que permite realizar el filtrado utilizando los mecanismos oficiales de Android, sin tener que desarrollar un sistema propio de detección de llamadas.

### 2. ¿Por qué necesitamos solicitar el rol ROLE_CALL_SCREENING?

Android protege determinadas funciones del sistema para evitar que cualquier aplicación pueda controlar aspectos sensibles del dispositivo.

El rol `ROLE_CALL_SCREENING` permite que una aplicación sea reconocida como la encargada de analizar y filtrar las llamadas entrantes.

Por este motivo, CallFilter debe solicitar este rol al usuario mediante los mecanismos oficiales de Android.

Una vez concedido, el sistema puede invocar nuestro servicio `CallFilterService` cuando se recibe una llamada que debe analizarse.

Es importante destacar que instalar la aplicación no es suficiente: el usuario debe aceptar expresamente este rol para que el filtrado real pueda funcionar.

### 3. ¿Por qué no necesitamos un servidor externo para realizar el filtrado?

CallFilter realiza el procesamiento directamente en el dispositivo Android.

Las reglas se almacenan localmente mediante SQLite y el motor `FilterEngine` se encarga de comparar los números entrantes con las condiciones configuradas.

No necesitamos enviar los números telefónicos a un servidor externo para tomar decisiones, ya que toda la lógica de filtrado se ejecuta dentro de la propia aplicación.

Esto presenta varias ventajas:

- **Privacidad:** los números no necesitan enviarse a servicios externos.
- **Rapidez:** las comprobaciones se realizan localmente.
- **Disponibilidad:** el filtrado no depende de una conexión a Internet.
- **Autonomía:** el usuario mantiene sus reglas y datos en su propio dispositivo.

Por tanto, una arquitectura local resulta suficiente para las necesidades de este proyecto.

### 4. ¿Qué ocurre si el número telefónico está guardado en los contactos?

El comportamiento depende de las reglas de filtrado configuradas y de cómo Android proporcione la información de la llamada.

Que un número esté guardado en la agenda no significa automáticamente que deba permitirse. Si coincide con una regla de bloqueo, el resultado dependerá de las prioridades y excepciones que aplique el motor de filtrado.

En CallFilter, la lista blanca permite establecer excepciones para números que el usuario quiere recibir aunque coincidan con otras condiciones de bloqueo.

De esta manera, el usuario puede decidir qué números tienen prioridad, independientemente de que estén guardados o no en sus contactos.

### 5. ¿Qué diferencias existen entre comprobar un número mediante una función y bloquear realmente una llamada?

Son dos operaciones diferentes que no deben confundirse.

**Comprobar un número mediante una función:**

Consiste en ejecutar una función del motor `FilterEngine` que recibe un número y evalúa las reglas configuradas.

El resultado indica si ese número debería permitirse o bloquearse según las condiciones existentes.

Esta operación es útil para realizar pruebas, simular llamadas y comprobar que las reglas funcionan correctamente, pero por sí sola no afecta a ninguna llamada real.

**Bloquear realmente una llamada:**

Para bloquear una llamada real es necesario que Android invoque el servicio `CallScreeningService` de la aplicación y que este responda utilizando el mecanismo oficial de `CallResponse`.

En este caso, la decisión del motor de filtrado se comunica al sistema operativo, que es el encargado de aplicar el rechazo de la llamada.

Por tanto, la diferencia principal es que una función únicamente calcula una decisión, mientras que el servicio permite trasladar esa decisión al sistema Android para actuar sobre una llamada entrante real.

### 6. ¿Qué mejoras incorporarías en una futura versión?

Después de desarrollar CallFilter, considero que existen varias mejoras que podrían ampliar sus posibilidades y mejorar la experiencia del usuario.

Las principales serían:

- **Estadísticas avanzadas:** incorporar gráficos para visualizar la evolución de las llamadas bloqueadas y permitidas.
- **Gestión inteligente de contactos:** permitir crear reglas automáticas para contactos conocidos, números desconocidos o números privados.
- **Copias de seguridad:** añadir la posibilidad de guardar y restaurar las reglas y la configuración.
- **Notificaciones personalizadas:** avisar al usuario cuando se bloquee una llamada importante o se detecte un número sospechoso.
- **Mejoras en la interfaz:** incorporar más opciones de personalización y facilitar la navegación.
- **Pruebas adicionales:** ampliar las pruebas automatizadas para cubrir más situaciones reales y posibles errores.
- **Protección de datos:** mejorar las opciones de privacidad y permitir configurar cuánto tiempo se conserva el historial.

Como reflexión final, desarrollar CallFilter me ha permitido comprender mejor cómo Android gestiona las llamadas telefónicas, cómo funcionan los servicios del sistema y por qué es importante respetar los permisos y roles que proporciona la plataforma.

También he podido aplicar conocimientos de Kotlin, Jetpack Compose, SQLite y pruebas unitarias en una aplicación práctica, separando la interfaz gráfica de la lógica de negocio.

Considero que uno de los aspectos más importantes del proyecto ha sido comprobar que no basta con desarrollar una función que determine si un número debe bloquearse: también es necesario integrarla correctamente con los mecanismos del sistema operativo para que esa decisión tenga un efecto real.

## 👨‍💻 Autoría

Proyecto desarrollado como parte de la asignatura **Programación Multimedia y Dispositivos Móviles (PMDM)** del ciclo formativo de Desarrollo de Aplicaciones Multiplataforma (DAM).

**Repositorio:**  
https://github.com/sha2dev56/pmdm-udit/tree/main/call-filter

---

*CallFilter — Más control sobre tus llamadas, más tranquilidad en tu día a día.*
