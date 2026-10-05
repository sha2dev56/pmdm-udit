package com.example.burgershop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.burgershop.MainActivity.Producto
import com.example.burgershop.ui.theme.BurgerShopTheme
//activity principal
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContent {
            //MaterialTheme: aplica los colores
            //tipografias por defecto a todo lo que hay dentro
            MaterialTheme{
                //Surface es el lienzo de donde que ocupa la pantalla
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Catalogohamburgesas
                }
            }
        }
    }
    data class Producto (
            val nombre: String,
            val precio: String,
            val imanResId: Int //el indentificador de la imagen en el res/drawable
    )

    //DATOS DE PRUEBA
    //De momento viven aqui mismo en el codigo. no vienen de ningun servidor
    //ni base de datos

    val catalogohamburgesas = listOf(
        Producto(
            nombre = "Clásica con Queso",
            precio = "5.99€",
            imanResId = R.drawable.burger_clasica
        ),
        Producto(
            nombre = "Clásica Doble",
            precio = "7.99€",
            imanResId = R.drawable.burger_doble
        ),
        Producto(
            nombre = "Burger Barbacoa",
            precio = "8.99€",
            imanResId = R.drawable.burger_bbq
        ),
        Producto(
            nombre = "Burger Picante",
            precio = "6.99€",
            imanResId = R.drawable.burger_picante
        ),
        Producto(
            nombre = "Burger Pollo",
            precio = "7.99€",
            imanResId = R.drawable.burger_pollo
        ),
        Producto(
            nombre = "Burger Vegetariana",
            precio = "6.99€",
            imanResId = R.drawable.burger_vegetariana
        ),
    )
}







