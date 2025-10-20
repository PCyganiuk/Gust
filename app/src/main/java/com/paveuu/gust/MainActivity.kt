package com.paveuu.gust

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paveuu.gust.ui.theme.GustTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GustTheme {
                Scaffold (
                    bottomBar = { BottomNavBar() }
                ) {
                    contentPadding ->
                    Box (
                        modifier = Modifier.padding(contentPadding)
                    ) {
                        CarouselExample_MultiBrowse()
                        Text("Pawcio",
                            style = MaterialTheme.typography.titleLarge, // uses your font
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar() {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Start", "My data", "Config")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp) // floating from sides
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()) // above nav bar
    ) {

        NavigationBar(
            modifier = Modifier
                .padding(2.dp) // 👈 space from all screen edges
                .clip(RoundedCornerShape(24.dp)) // 👈 rounded corners
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    clip = false
                )
                .background(MaterialTheme.colorScheme.surface), // 👈 background for elevation contrast
            containerColor = MaterialTheme.colorScheme.primaryContainer, // 👈 better contrast color
            tonalElevation = 8.dp // 👈 adds Material-style elevation glow
        ){
            CarouselExample_MultiBrowse()
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = {
                        when (item) {
                            "Start" -> Icon(Icons.Default.Home, contentDescription = item)
                            "My data" -> Icon(Icons.Default.Person, contentDescription = item)
                            "Config" -> Icon(Icons.Default.Settings, contentDescription = item)
                        }
                    },
                    label = { Text(item) },
                    selected = selectedItem == index,
                    onClick = { selectedItem = index }
                )
            }
        }
    }
}


@Composable
fun CarouselExcerciseCards(){
    data class CarouselItem(
        // Everything is in seconds
        val id: Int,
        val numOfRounds: Int,
        val name: String,
        val breathCyclesInRound: Int,
        val secondsToHold: Int,
        val breathPacing: Int,
        val holdFor: Int,
        val holdAfter: Boolean, // True - inhale False - Exhale
        val duration: Int
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselExample_MultiBrowse() {
    data class CarouselItem(
        val id: Int,
        val title: String,
        val color: Color
    )

    val items = remember {
        listOf(
            CarouselItem(0, "Cupcake", Color(0xFFFFC1E3)),
            CarouselItem(1, "Donut", Color(0xFFFFD180)),
            CarouselItem(2, "Eclair", Color(0xFF80D8FF)),
            CarouselItem(3, "Froyo", Color(0xFFA7FFEB)),
            CarouselItem(4, "Gingerbread", Color(0xFFFF9E80))
        )
    }

    HorizontalMultiBrowseCarousel(
        state = rememberCarouselState { items.count() },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 16.dp),
        preferredItemWidth = 186.dp,
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { i ->
        val item = items[i]

        Card(
            modifier = Modifier
                .height(205.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Canvas drawable section
                Canvas(
                    modifier = Modifier
                        .size(120.dp)
                        .padding(top = 8.dp)
                ) {
                    // Example: Draw a circle and a triangle
                    drawCircle(
                        color = item.color,
                        radius = size.minDimension / 4,
                        center = center
                    )

                    drawPath(
                        path = Path().apply {
                            moveTo(size.width / 2, size.height / 4)
                            lineTo(size.width / 4, size.height * 3 / 4)
                            lineTo(size.width * 3 / 4, size.height * 3 / 4)
                            close()
                        },
                        color = Color.Black.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GustTheme {
        CarouselExample_MultiBrowse()
        BottomNavBar()
    }
}