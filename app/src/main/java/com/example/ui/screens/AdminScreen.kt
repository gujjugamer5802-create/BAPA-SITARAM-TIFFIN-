package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PremiumGreen
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.TiffinViewModel

@Composable
fun AdminScreen(viewModel: TiffinViewModel) {
    val loggedIn by viewModel.isAdminLoggedIn.collectAsState()

    if (loggedIn) {
        AdminDashboardContent(viewModel = viewModel)
    } else {
        AdminSecureLoginForm(viewModel = viewModel)
    }
}

@Composable
fun AdminSecureLoginForm(viewModel: TiffinViewModel) {
    val isGujarati = viewModel.language.collectAsState().value == "GU"

    val inputEmail by viewModel.adminEmailInput.collectAsState()
    val inputPass by viewModel.adminPasswordInput.collectAsState()
    val inputOtp by viewModel.adminOtpInput.collectAsState()
    val showOtpBlock by viewModel.showOtpBlock.collectAsState()
    val paymentMsg by viewModel.paymentMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "Secure Security Lock",
            tint = RoyalGold,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = viewModel.t("admin_secure_login"),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = RoyalGold
        )
        Text(
            text = "OTP Code: 123456 | Credentials admin@bapasitaramtiffin.com // 123456",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!showOtpBlock) {
            // Email Input
            OutlinedTextField(
                value = inputEmail,
                onValueChange = { viewModel.adminEmailInput.value = it },
                label = { Text(viewModel.t("email")) },
                modifier = Modifier.fillMaxWidth().testTag("input_admin_email"),
                leadingIcon = { Icon(Icons.Default.Email, null) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            OutlinedTextField(
                value = inputPass,
                onValueChange = { viewModel.adminPasswordInput.value = it },
                label = { Text(viewModel.t("password")) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().testTag("input_admin_password"),
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Clickable Fingerprint Biometrics mock button
            IconButton(
                onClick = {
                    viewModel.adminEmailInput.value = viewModel.adminEmail.value
                    viewModel.adminPasswordInput.value = viewModel.adminPassword.value
                    viewModel.showOtpBlock.value = true
                    viewModel.paymentMessage.value = "Biometric credentials parsed. Please enter safety OTP 123456."
                },
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .border(1.dp, RoyalGold, CircleShape)
                    .testTag("admin_biometric_button")
            ) {
                Icon(Icons.Default.Fingerprint, "Biometric Login", tint = RoyalGold, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Simulate Biometrics", fontSize = 10.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.performAdminLogin() },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = Color.Black),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("admin_login_submit")
            ) {
                Text(viewModel.t("login_btn"), fontWeight = FontWeight.Bold)
            }
        } else {
            // OTP section
            OutlinedTextField(
                value = inputOtp,
                onValueChange = { viewModel.adminOtpInput.value = it },
                label = { Text(viewModel.t("otp")) },
                modifier = Modifier.fillMaxWidth().testTag("input_admin_otp"),
                leadingIcon = { Icon(Icons.Default.VpnKey, null) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.verifyAdminOtp(inputOtp) },
                colors = ButtonDefaults.buttonColors(containerColor = PremiumGreen, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("admin_otp_submit")
            ) {
                Text("VERIFY CODE ➔", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = { viewModel.showOtpBlock.value = false }) {
                Text("Change login credentials", color = RoyalGold)
            }
        }

        if (paymentMsg.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = paymentMsg,
                    color = Color.Green,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun AdminDashboardContent(viewModel: TiffinViewModel) {
    val ordersList by viewModel.orders.collectAsState()
    val menusList by viewModel.menus.collectAsState()

    val lunchM = menusList.find { it.mealType == "lunch" }
    val dinnerM = menusList.find { it.mealType == "dinner" }

    // Admin Inputs
    val amLunchVeg1 by viewModel.adminMenuLunchVeg1.collectAsState()
    val amLunchVeg2 by viewModel.adminMenuLunchVeg2.collectAsState()
    val amLunchKathol by viewModel.adminMenuLunchKathol.collectAsState()
    val amLunchSpecial by viewModel.adminMenuLunchSpecial.collectAsState()
    val amLunchRice by viewModel.adminMenuLunchRice.collectAsState()
    val amLunchSalad by viewModel.adminMenuLunchSalad.collectAsState()
    val amLunchPrice by viewModel.adminMenuLunchPrice.collectAsState()

    val amDinnerVeg1 by viewModel.adminMenuDinnerVeg1.collectAsState()
    val amDinnerVeg2 by viewModel.adminMenuDinnerVeg2.collectAsState()
    val amDinnerKathol by viewModel.adminMenuDinnerKathol.collectAsState()
    val amDinnerSpecial by viewModel.adminMenuDinnerSpecial.collectAsState()
    val amDinnerRice by viewModel.adminMenuDinnerRice.collectAsState()
    val amDinnerSalad by viewModel.adminMenuDinnerSalad.collectAsState()
    val amDinnerPrice by viewModel.adminMenuDinnerPrice.collectAsState()

    val updatedNotice = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BAPA SITARAM ADMIN PANEL",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = RoyalGold
                )
                Text(text = "Management Dashboard center.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            IconButton(
                onClick = { viewModel.logoutAdmin() },
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(Icons.Default.ExitToApp, "Logout", tint = Color.Red)
            }
        }

        // Stats Counter Grid Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminStatsCard(
                label = "Active Orders",
                value = ordersList.filter { it.status != "Delivered" && it.status != "Rejected" }.size.toString(),
                color = WarningOrange,
                modifier = Modifier.weight(1f)
            )
            AdminStatsCard(
                label = "Revenue Total",
                value = "₹" + ordersList.filter { it.isPaid }.sumOf { it.totalPrice }.toInt(),
                color = PremiumGreen,
                modifier = Modifier.weight(1.2f)
            )
        }

        // --- MENU MANAGEMENT ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x33D4AF37), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Daily Gujarati Menu Configuration",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = RoyalGold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Lunch Menu Inputs
                Column {
                    Text("LUNCH (બપોરના સમયે) ITEMS:", fontWeight = FontWeight.Bold, color = RoyalGold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amLunchVeg1,
                        onValueChange = { viewModel.adminMenuLunchVeg1.value = it },
                        label = { Text("Vegetable 1") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_lunch_veg1"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = amLunchVeg2,
                            onValueChange = { viewModel.adminMenuLunchVeg2.value = it },
                            label = { Text("Vegetable 2") },
                            modifier = Modifier.weight(1f).testTag("admin_lunch_veg2"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = amLunchKathol,
                            onValueChange = { viewModel.adminMenuLunchKathol.value = it },
                            label = { Text("Kathol") },
                            modifier = Modifier.weight(1f).testTag("admin_lunch_kathol"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = amLunchSpecial,
                            onValueChange = { viewModel.adminMenuLunchSpecial.value = it },
                            label = { Text("Kadhi / Dal") },
                            modifier = Modifier.weight(1f).testTag("admin_lunch_special"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = amLunchRice,
                            onValueChange = { viewModel.adminMenuLunchRice.value = it },
                            label = { Text("Rice Selection") },
                            modifier = Modifier.weight(1f).testTag("admin_lunch_rice"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = amLunchSalad,
                            onValueChange = { viewModel.adminMenuLunchSalad.value = it },
                            label = { Text("Salad Variety") },
                            modifier = Modifier.weight(1.5f).testTag("admin_lunch_salad"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = amLunchPrice,
                            onValueChange = { viewModel.adminMenuLunchPrice.value = it },
                            label = { Text("Price (₹)") },
                            modifier = Modifier.weight(1f).testTag("admin_lunch_price"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = Color(0x1AD4AF37))
                Spacer(modifier = Modifier.height(16.dp))

                // Dinner Menu Inputs
                Column {
                    Text("DINNER (સાંજના સમયે) ITEMS:", fontWeight = FontWeight.Bold, color = RoyalGold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amDinnerVeg1,
                        onValueChange = { viewModel.adminMenuDinnerVeg1.value = it },
                        label = { Text("Vegetable 1") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_dinner_veg1"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = amDinnerVeg2,
                            onValueChange = { viewModel.adminMenuDinnerVeg2.value = it },
                            label = { Text("Vegetable 2") },
                            modifier = Modifier.weight(1f).testTag("admin_dinner_veg2"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = amDinnerKathol,
                            onValueChange = { viewModel.adminMenuDinnerKathol.value = it },
                            label = { Text("Kathol") },
                            modifier = Modifier.weight(1f).testTag("admin_dinner_kathol"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = amDinnerSpecial,
                            onValueChange = { viewModel.adminMenuDinnerSpecial.value = it },
                            label = { Text("Kadhi / Dal") },
                            modifier = Modifier.weight(1f).testTag("admin_dinner_special"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = amDinnerRice,
                            onValueChange = { viewModel.adminMenuDinnerRice.value = it },
                            label = { Text("Rice / Khichdi") },
                            modifier = Modifier.weight(1f).testTag("admin_dinner_rice"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = amDinnerSalad,
                            onValueChange = { viewModel.adminMenuDinnerSalad.value = it },
                            label = { Text("Salad Variety") },
                            modifier = Modifier.weight(1.5f).testTag("admin_dinner_salad"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = amDinnerPrice,
                            onValueChange = { viewModel.adminMenuDinnerPrice.value = it },
                            label = { Text("Price (₹)") },
                            modifier = Modifier.weight(1f).testTag("admin_dinner_price"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                AnimatedVisibility(visible = updatedNotice.value.isNotEmpty()) {
                    Text(
                        text = updatedNotice.value,
                        color = PremiumGreen,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Button(
                    onClick = {
                        viewModel.updateDailyMenuFromAdmin()
                        updatedNotice.value = viewModel.t("update_success")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("admin_update_menu_submit")
                ) {
                    Text(viewModel.t("save_menu"), fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- ORDER ACTION MANAGER ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x33D4AF37), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Customer Active Orders Flow (" + ordersList.size + ")",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = RoyalGold
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (ordersList.isEmpty()) {
                    Text("No customer placing orders currently", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                } else {
                    ordersList.forEach { valOrder ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                                .testTag("admin_order_item_${valOrder.orderId}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = valOrder.orderId,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = RoyalGold
                                )
                                Text(
                                    text = valOrder.status,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = when (valOrder.status) {
                                        "Pending" -> WarningOrange
                                        "Preparing" -> RoyalGold
                                        "Out For Delivery" -> Color.Blue
                                        "Delivered" -> PremiumGreen
                                        else -> Color.Red
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "${valOrder.customerName} | +91 ${valOrder.customerPhone}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Roti Choice: ${valOrder.rotiChoice} | Qty: ${valOrder.quantity}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text(text = "Dest: ${valOrder.houseNo}, ${valOrder.buildingName}, ${valOrder.streetArea}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Charged: ₹${valOrder.totalPrice.toInt()} (${valOrder.paymentMethod})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            if (valOrder.paymentMethod == "UPI" && valOrder.utrNumber.isNotEmpty()) {
                                Text(text = "[Verified UTR: ${valOrder.utrNumber}]", color = PremiumGreen, style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace))
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Fast Status Triggers:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val states = listOf("Accepted", "Preparing", "Out For Delivery", "Delivered", "Rejected")
                                states.forEach { s ->
                                    Button(
                                        onClick = { viewModel.processOrderStatusChange(valOrder.orderId, s) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (valOrder.status == s) RoyalGold else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (valOrder.status == s) Color.Black else MaterialTheme.colorScheme.onBackground
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp).testTag("action_status_${valOrder.orderId}_$s"),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(s, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Payment confirmation trigger
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Is Paid:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Switch(
                                    checked = valOrder.isPaid,
                                    onCheckedChange = { viewModel.processOrderPaymentStatusChange(valOrder.orderId, it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PremiumGreen),
                                    modifier = Modifier.scale(0.8f).testTag("action_pay_switch_${valOrder.orderId}")
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

// Stats UI element
@Composable
fun AdminStatsCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, Color(0x1AD4AF37), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = label, fontSize = 11.sp, color = Color.Gray)
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}
