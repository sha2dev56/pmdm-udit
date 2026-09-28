package com.example.reto01_tarjetapresentacion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reto01_tarjetapresentacion.ui.theme.Reto01TarjetaPresentacionTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { //aplicamos el tema de colores del proyecto a todo lo que va dentro
            Reto01TarjetaPresentacionTheme {
                //surface = el espacio de fondo que ocupa toda la pantalla
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //aqui se llama al funcion, la que dibuja la tarjeta
                    TarjetaPresentacion()
                }
            }
        }
    }
}

@Composable
fun TarjetaPresentacion() {
    //localcontext: asi un Composable pide presrado el contexto de android
    // lo necesitamos para poder abrir el navegador desde el boton
    val context = LocalContext.current
    // Column: apila los elementos de arriba a abajo (flexbox vertical)
    Column(
        modifier = Modifier
            .fillMaxSize() //ocupa toda la pantalla
            .padding(16.dp), //margen para que nada toque los bordes
        horizontalAlignment = Alignment.CenterHorizontally, //centrar en el eje x y eje y
        verticalArrangement = Arrangement.Center
    ) {
        //image es la foto de perfil
        Image(
            painter = painterResource(id = R.drawable.foto_perfil),
            contentDescription = "Foto de perfil de usuario", //accesibilidad
            modifier = Modifier
                .size(150.dp) //tamaño fijo 150x150
                .clip(CircleShape), //la recorta en forma circular
            contentScale = ContentScale.Crop //rellena el circulo sin deformar la imagen
        )
        //hueco vacio entre la imagen y el texto
        Spacer(modifier = Modifier.height(24.dp))
        //nombre
        Text(
            text = "Shaghayegh Asghari",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
    //rol
        Text(
            text = "Desarrollador MERN & Docente DAM",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.secondary //color secundario del texto
        )

        //mayor espacio antes del boton

        Spacer(modifier = Modifier.height(24.dp))
        //boton enlace github
        Button(
            onClick = {
                //intent action_view: le decimos a android que queremos VER y el sistema decide que app usar (navegador etc..)
                //uri.parse convierte el texto de la url en el formato que android entiende
                //startActivity lanza esa accion
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/sha2dev56")
                )
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(0.8f) //ocupa el 80% de ancho de pantalla
        ) {
            Text(text = "Mi perfil de GitHub")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TarjetaPreview() {
    Reto01TarjetaPresentacionTheme {
        TarjetaPresentacion()
    }
}