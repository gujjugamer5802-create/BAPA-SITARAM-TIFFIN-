package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.HisaabScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.OrderScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RoyalGold
import com.example.ui.viewmodel.TiffinViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TiffinViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentTab by viewModel.currentTab.collectAsState()
                val activeLanguage by viewModel.language.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = "BAPA SITARAM",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = RoyalGold
                                    )
                                    Text(
                                        text = if (activeLanguage == "GU") "પ્રીમિયમ ટિફિન સર્વિસ" else "Premium Gujarati Tiffin",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                }
                            },
                            actions = {
                                // Elegant bilingual EN / ગુજરાતી toggle badge pill in top bar!
                                Row(
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, RoyalGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                        .clickable { viewModel.changeLanguage() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("language_toggle_button"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "Change Language",
                                        tint = RoyalGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (activeLanguage == "EN") "EN | ગુજરાતી" else "ગુજરાતી | EN",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.shadowUnderline()
                        )
                    },
                    bottomBar = {
                        // Custom glassmorphic bottom navigation bars
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            tonalElevation = 8.dp,
                            modifier = Modifier.testTag("app_bottom_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == "MENU",
                                onClick = { viewModel.toggleTab("MENU") },
                                icon = { Icon(Icons.Default.Restaurant, contentDescription = "Menu") },
                                label = { Text(viewModel.t("tab_menu"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = RoyalGold,
                                    indicatorColor = RoyalGold
                                ),
                                modifier = Modifier.testTag("tab_button_menu")
                            )

                            val lunchInCart by viewModel.lunchInCart.collectAsState()
                            val dinnerInCart by viewModel.dinnerInCart.collectAsState()
                            val showCartDot = lunchInCart || dinnerInCart

                            NavigationBarItem(
                                selected = currentTab == "ORDER",
                                onClick = { viewModel.toggleTab("ORDER") },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (showCartDot) {
                                                Badge(containerColor = RoyalGold) {
                                                    Text("!", color = Color.Black)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.ShoppingBag, contentDescription = "Order")
                                    }
                                },
                                label = { Text(viewModel.t("tab_order"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = RoyalGold,
                                    indicatorColor = RoyalGold
                                ),
                                modifier = Modifier.testTag("tab_button_order")
                            )

                            NavigationBarItem(
                                selected = currentTab == "HISAAB",
                                onClick = { viewModel.toggleTab("HISAAB") },
                                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Hisaab") },
                                label = { Text(viewModel.t("tab_hisaab"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = RoyalGold,
                                    indicatorColor = RoyalGold
                                ),
                                modifier = Modifier.testTag("tab_button_hisaab")
                            )

                            NavigationBarItem(
                                selected = currentTab == "ADMIN",
                                onClick = { viewModel.toggleTab("ADMIN") },
                                icon = { Icon(Icons.Default.Security, contentDescription = "Admin") },
                                label = { Text(viewModel.t("tab_admin"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = RoyalGold,
                                    indicatorColor = RoyalGold
                                ),
                                modifier = Modifier.testTag("tab_button_admin")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            "MENU" -> MenuScreen(viewModel = viewModel)
                            "ORDER" -> OrderScreen(viewModel = viewModel)
                            "HISAAB" -> HisaabScreen(viewModel = viewModel)
                            "ADMIN" -> AdminScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

// Custom decoration extension
fun Modifier.shadowUnderline(): Modifier = this.then(
    Modifier.drawBehind {
        drawLine(
            color = RoyalGold.copy(alpha = 0.2f),
            start = androidx.compose.ui.geometry.Offset(0f, size.height),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx()
        )
    }
)
