package com.paveuu.gust

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
                    Column (
                        modifier = Modifier.padding(contentPadding)
                    ) {
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
    val items = listOf("Home", "Search", "Profile")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp) // floating from sides
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()) // above nav bar
    ) {

        NavigationBar(
            modifier = Modifier
                .padding(16.dp) // 👈 space from all screen edges
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
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = {
                        when (item) {
                            "Home" -> Icon(Icons.Default.Home, contentDescription = item)
                            "Search" -> Icon(Icons.Default.Search, contentDescription = item)
                            "Profile" -> Icon(Icons.Default.Person, contentDescription = item)
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


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GustTheme {
        BottomNavBar()
    }
}