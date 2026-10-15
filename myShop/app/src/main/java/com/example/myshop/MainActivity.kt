package com.example.myshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.myshop.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = TerracottaPrimary,
                    background = CreamBackground,
                    surface = Color.White
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CreamBackground
                ) {
                    PantallaPrincipal()
                }
            }
        }
    }
}


data class Producto(
    val nombre: String,
    val categoria: String,
    val precio: String,
    val imagenResId: Int
)

val catalogoMuebles = listOf(
    Producto(
        "Aparador Mid-Century",
        "Roble 1960",
        "1.450 €",
        R.drawable.aparadormadera
    ),
    Producto(
        "Sillón Club de Cuero",
        "Piel Teja",
        "980 €",
        R.drawable.silloncuero
    ),
    Producto(
        "Mesa Comedor Rústica",
        "Pino Recuperado",
        "1.200 €",
        R.drawable.mesacomedor
    ),
    Producto(
        "Alfombra Heriz Vintage",
        "Persia Noroccidental",
        "1.100 €",
        R.drawable.alfombra
    ),
    Producto(
        "Lámpara de Latón Pulido",
        "Diseño Industrial",
        "340 €",
        R.drawable.lamparavintage
    ),
    Producto(
        "Espejo Marco de Roble",
        "Tallado a Mano",
        "420 €",
        R.drawable.espejovintage
    )
)

//pantalla principal
@Composable
fun PantallaContenido(productos: List<Producto>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Encabezado Editorial
        Text(
            text = "Rustic House",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = DarkCharcoal
        )
        Spacer(modifier = Modifier.height(8.dp))

        Divider(
            color = SoftGray,
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Rejilla Adaptativa Directa de Productos
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(productos) { producto ->
                TarjetaProducto(producto)
            }
        }
    }
}

@Composable
fun TarjetaProducto(producto: Producto) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(SoftGray)
            ) {
                Image(
                    painter = painterResource(id = producto.imagenResId),
                    contentDescription = producto.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = producto.categoria.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = OliveAccent,
                    maxLines = 1,
                    overflow = Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = producto.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Serif,
                    color = DarkCharcoal,
                    maxLines = 1,
                    overflow = Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = producto.precio,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerracottaPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

//portada
@Composable
fun PantallaPrincipal() {
    var mostrarPortada by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1500)
        mostrarPortada = false
    }

    if (mostrarPortada) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Rustic House",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = DarkCharcoal
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Artesanía y Diseño",
                    fontSize = 14.sp,
                    color = MutedText,
                    letterSpacing = 2.sp
                )
            }
        }
    } else {
        PantallaContenido(productos = catalogoMuebles)
    }
}