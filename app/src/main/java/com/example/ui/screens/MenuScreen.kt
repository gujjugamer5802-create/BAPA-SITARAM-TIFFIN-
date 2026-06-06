package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyMenu
import com.example.ui.theme.PremiumGreen
import com.example.ui.theme.RoyalGold
import com.example.ui.viewmodel.TiffinViewModel

@Composable
fun MenuScreen(viewModel: TiffinViewModel) {
    val menusState by viewModel.menus.collectAsState()
    val isGujarati = viewModel.language.collectAsState().value == "GU"

    val lunchQty by viewModel.lunchQty.collectAsState()
    val lunchRoti by viewModel.lunchRoti.collectAsState()
    val lunchInCart by viewModel.lunchInCart.collectAsState()

    val dinnerQty by viewModel.dinnerQty.collectAsState()
    val dinnerRoti by viewModel.dinnerRoti.collectAsState()
    val dinnerInCart by viewModel.dinnerInCart.collectAsState()

    val lunch = menusState.find { it.mealType == "lunch" }
    val dinner = menusState.find { it.mealType == "dinner" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero Branding Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_card_banner"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = viewModel.t("app_title"),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = RoyalGold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.t("app_subtitle"),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = RoyalGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "4.9 (420+ Reviews)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "• Pure Gujarati Shadh",
                                style = MaterialTheme.typography.labelSmall,
                                color = RoyalGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- LUNCH TIME SECTION ---
        item {
            MealCard(
                title = viewModel.t("menu_lunch"),
                subtitle = viewModel.t("lunch_sub"),
                menu = lunch,
                isGujarati = isGujarati,
                qty = lunchQty,
                selectedRoti = lunchRoti,
                isInCart = lunchInCart,
                isLunch = true,
                onQtyChange = { viewModel.updateLunchQty(it) },
                onRotiSelect = { viewModel.lunchRoti.value = it },
                onAddToCart = { viewModel.addLunchToCart() },
                onRemoveFromCart = { viewModel.removeLunchFromCart() },
                tHelper = { viewModel.t(it) }
            )
        }

        // --- DINNER TIME SECTION ---
        item {
            MealCard(
                title = viewModel.t("menu_dinner"),
                subtitle = viewModel.t("dinner_sub"),
                menu = dinner,
                isGujarati = isGujarati,
                qty = dinnerQty,
                selectedRoti = dinnerRoti,
                isInCart = dinnerInCart,
                isLunch = false,
                onQtyChange = { viewModel.updateDinnerQty(it) },
                onRotiSelect = { viewModel.dinnerRoti.value = it },
                onAddToCart = { viewModel.addDinnerToCart() },
                onRemoveFromCart = { viewModel.removeDinnerFromCart() },
                tHelper = { viewModel.t(it) }
            )
        }
    }
}

@Composable
fun MealCard(
    title: String,
    subtitle: String,
    menu: DailyMenu?,
    isGujarati: Boolean,
    qty: Int,
    selectedRoti: String,
    isInCart: Boolean,
    isLunch: Boolean,
    onQtyChange: (Int) -> Unit,
    onRotiSelect: (String) -> Unit,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit,
    tHelper: (String) -> String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x33D4AF37), RoundedCornerShape(24.dp))
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column {
            // Styled Gujarati Thali Drawing Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1C1C1C),
                                Color(0xFF121212)
                            )
                        )
                    )
            ) {
                // Vector Thali Canvas Drawing
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    // The main golden brass thali plate border
                    drawCircle(
                        color = RoyalGold,
                        radius = 85.dp.toPx(),
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0x1AD4AF37),
                        radius = 80.dp.toPx(),
                        center = center
                    )

                    // Draw little bowls in a circle inside the thali
                    val bowlRadius = 18.dp.toPx()
                    val bowlDistance = 55.dp.toPx()
                    val bowlColors = listOf(
                        Color(0xFF8B0000), // Sabji 1 (Paneer or Sev Tomato)
                        Color(0xFFE6A100), // Sabji 2 / Kathol
                        Color(0xFFD4AF37), // Dal / Kadhi
                        Color(0xFFF1F0E8), // Rice / Khichdi
                        Color(0xFF2E7D32)  // Salad
                    )

                    for (i in 0 until 5) {
                        val angle = (i * 72 * Math.PI / 180).toFloat()
                        val bowlCenter = Offset(
                            center.x + bowlDistance * kotlin.math.cos(angle),
                            center.y + bowlDistance * kotlin.math.sin(angle)
                        )
                        // Bowl rim
                        drawCircle(
                            color = Color(0xFFE5C158),
                            radius = bowlRadius,
                            center = bowlCenter,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        // Food inside bowl
                        drawCircle(
                            color = bowlColors[i],
                            radius = bowlRadius - 2.dp.toPx(),
                            center = bowlCenter
                        )
                    }

                    // Rotis in the center
                    drawCircle(
                        color = Color(0xFFD2B48C),
                        radius = 24.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFF8B5A2B),
                        radius = 24.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Header Labels Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = RoyalGold,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isLunch) "LUNCH TIME" else "DINNER TIME",
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Surface(
                            color = if (menu?.isAvailable == true) PremiumGreen else Color.Gray,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (menu?.isAvailable == true) "FRESH TODAY" else "UNAVAILABLE",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = RoyalGold
                            )
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }

            // Menu Items List
            if (menu != null) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = tHelper("menu_today"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val veg1Text = if (isGujarati) menu.veg1Gu else menu.veg1
                    val veg2Text = if (isGujarati) menu.veg2Gu else menu.veg2
                    val katholText = if (isGujarati) menu.katholGu else menu.kathol
                    val specialText = if (isGujarati) menu.specialGu else menu.special
                    val riceText = if (isGujarati) menu.riceGu else menu.rice
                    val saladText = if (isGujarati) menu.saladGu else menu.salad

                    val items = listOf(
                        "• $veg1Text" to "Veg 1",
                        "• $veg2Text" to "Veg 2",
                        "• $katholText" to "Kathol",
                        "• $specialText" to "Dal/Kadhi",
                        "• $riceText" to "Rice/Khichdi",
                        "• $saladText" to "Salad"
                    )

                    // Render nicely inside 2 columns
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            items.take(3).forEach {
                                Text(
                                    text = it.first,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            items.skip(3).forEach {
                                Text(
                                    text = it.first,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0x1AD4AF37))

                    // Roti Selections
                    Text(
                        text = tHelper("rotis"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val rotisList = listOf(
                        "Normal Roti" to tHelper("normal_roti"),
                        "Bajra Rotla" to tHelper("bajra_rotla"),
                        "Bhakhri" to tHelper("bhakhri")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rotisList.forEach { pair ->
                            val isSelected = selectedRoti == pair.first
                            Button(
                                onClick = { onRotiSelect(pair.first) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) RoyalGold else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onBackground
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("${if (isLunch) "lunch" else "dinner"}_roti_${pair.first.replace(" ", "_")}"),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = pair.second,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quantity and Bottom Control Panel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = tHelper("price"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "₹${menu.price.toInt()}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalGold
                                )
                            )
                        }

                        // Quantity selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = { onQtyChange(-1) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("${if (isLunch) "lunch" else "dinner"}_qty_minus")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }
                            Text(
                                text = qty.toString(),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            IconButton(
                                onClick = { onQtyChange(1) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("${if (isLunch) "lunch" else "dinner"}_qty_plus")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }
                        }

                        // Add to Cart
                        Button(
                            onClick = {
                                if (isInCart) {
                                    onRemoveFromCart()
                                } else {
                                    onAddToCart()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isInCart) PremiumGreen else RoyalGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("${if (isLunch) "lunch" else "dinner"}_cart_button")
                        ) {
                            Text(
                                text = if (isInCart) tHelper("in_cart") else tHelper("add_to_cart"),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isInCart) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper expansion list skipping
fun <T> List<T>.skip(n: Int): List<T> = if (this.size > n) this.subList(n, this.size) else emptyList()
