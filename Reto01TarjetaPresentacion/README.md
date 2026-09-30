# Reto 1 - Tarjeta de Presentación Profesional

**Módulo:** 0489 · Programación Multimedia y Dispositivos Móviles

**Autora:** Shaghayegh Asghari

**Tecnología:** Kotlin + Jetpack Compose (Android nativo)

**RA vinculado:** RA1 · Tecnologías de desarrollo para dispositivos móviles

## Qué es esta app

Una tarjeta de presentación digital (estilo Linktree) que muestra una foto de perfil, mi nombre, mi rol profesional y diferentes opciones para conocer mi perfil profesional y contactar conmigo.

La aplicación incluye:

- Foto de perfil.
- Nombre y rol profesional.
- Botón para acceder a mi perfil de GitHub.
- Botón para acceder a mi perfil de LinkedIn.
- Sección de contacto para posibles proyectos o colaboraciones.
- Botón `Contact me!` que abre la aplicación de correo.
- Mensaje, asunto y destinatario predefinidos en el correo.

## Objetivo del reto

Partir de un proyecto Android base y modificarlo para construir una aplicación funcional propia, aplicando los conceptos vistos en clase: estructura de un proyecto Kotlin, componentes visuales de Jetpack Compose, gestión de recursos (imágenes e icono), uso de `Intent` y `Uri` para interactuar con otras aplicaciones y control de versiones con Git.

## Componentes y conceptos utilizados

| Componente / concepto | Para qué se usa en esta app |
|---|---|
| `Column` | Organiza los elementos verticalmente (foto, nombre, rol, botones y contacto). |
| `Image` + `clip(CircleShape)` | Muestra la foto de perfil recortada en círculo. |
| `Text` | Muestra el nombre, rol profesional y mensaje de contacto. |
| `Spacer` | Añade separación entre los diferentes elementos. |
| `Button` | Permite acceder a GitHub, LinkedIn y al correo electrónico. |
| `Intent.ACTION_VIEW` | Abre los perfiles de GitHub y LinkedIn en el navegador. |
| `Intent.ACTION_SENDTO` | Abre la aplicación de correo para contactar conmigo. |
| `Uri.parse()` | Convierte las URLs y la dirección `mailto:` en una `Uri` que Android puede utilizar. |
| `LocalContext.current` | Permite obtener el contexto necesario para lanzar los `Intent`. |
| `res/drawable` | Carpeta donde se encuentra la imagen de perfil. |
| `res/mipmap-*` | Contiene el icono personalizado de la aplicación. |
| Image Asset Studio | Herramienta utilizada para crear y personalizar el icono de la app. |
| `strings.xml` (`app_name`) | Contiene el nombre visible de la aplicación bajo el icono. |
| Git | Se utiliza para controlar las versiones y registrar los cambios del proyecto. |

## Cómo ejecutar el proyecto

1. Clonar o abrir el proyecto en Android Studio.
2. Esperar a que sincronice Gradle.
3. Ejecutar (▶) sobre un emulador o un dispositivo Android real con la depuración USB activada.

## Qué he aprendido

- Cómo se estructura un proyecto Android/Kotlin con Jetpack Compose.
- Cómo crear una interfaz utilizando funciones `@Composable`.
- Cómo importar y organizar imágenes en `res/drawable`.
- Cómo usar `Column`, `Image`, `Text`, `Spacer` y `Button` para maquetar una pantalla.
- Cómo recortar una imagen en forma circular utilizando `clip(CircleShape)`.
- Cómo cambiar el icono de la app con Image Asset Studio, utilizando una capa de fondo y una capa de primer plano.
- Cómo cambiar el nombre visible de la app en `strings.xml`, sin modificar el nombre del proyecto.
- Qué es un `Intent` y cómo utilizarlo para lanzar acciones externas.
- La diferencia entre `Intent.ACTION_VIEW` y `Intent.ACTION_SENDTO`.
- Cómo utilizar `Uri.parse()` para convertir una URL o una dirección de correo en una `Uri`.
- Cómo abrir una URL externa desde un botón usando `Intent` + `Uri`.
- Cómo abrir un perfil de GitHub y LinkedIn desde la aplicación.
- Cómo abrir una aplicación de correo desde un botón.
- Cómo establecer un destinatario, asunto y mensaje predefinidos en un correo.
- Cómo utilizar Git para guardar y gestionar los cambios realizados en el proyecto.

## Dificultades y cómo las resolví

- Al principio, algunos componentes de Jetpack Compose como `Column`, `Image`, `Text` o `Button` no eran reconocidos porque faltaban algunos imports. Lo resolví utilizando `Alt + Enter` sobre cada elemento marcado en rojo para que Android Studio añadiera el import correspondiente.

- Tuve que organizar correctamente las funciones `@Composable` fuera de `onCreate()` para que `TarjetaPresentacion()` y `TarjetaPreview()` funcionaran correctamente.

- Al crear la imagen circular, tuve que utilizar `clip(CircleShape)` junto con `ContentScale.Crop` para conseguir que la fotografía se adaptara correctamente al círculo.

- Para los botones de GitHub y LinkedIn utilicé `Intent.ACTION_VIEW`, que permite abrir las URLs en el navegador.

- Para el botón `Contact me!` utilicé `Intent.ACTION_SENDTO` junto con `mailto:` para abrir la aplicación de correo.

- También añadí un asunto y un mensaje predefinido en el correo para facilitar el contacto profesional.

- Personalicé el icono de la aplicación mediante Image Asset Studio para sustituir el icono predeterminado de Android.

## Estructura del proyecto

```text
app/src/main/java/.../MainActivity.kt  → pantalla principal (Compose)

app/src/main/res/drawable/             → imagen de perfil

app/src/main/res/mipmap-*/             → icono personalizado de la app

app/src/main/res/values/strings.xml    → nombre visible de la app
