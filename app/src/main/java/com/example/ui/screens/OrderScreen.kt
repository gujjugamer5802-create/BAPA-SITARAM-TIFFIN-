package com.example.ui.screens

import android.widget.Space
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PremiumGreen
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.TiffinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(viewModel: TiffinViewModel) {
    val showSuccess by viewModel.showSuccessScreen.collectAsState()
    val lastOId by viewModel.lastPlacedOrderId.collectAsState()

    val lunchInCart by viewModel.lunchInCart.collectAsState()
    val dinnerInCart by viewModel.dinnerInCart.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (showSuccess && lastOId != null) {
            OrderSuccessScreen(
                orderId = lastOId!!,
                viewModel = viewModel,
                onDismiss = {
                    viewModel.showSuccessScreen.value = false
                    viewModel.lastPlacedOrderId.value = null
                    viewModel.toggleTab("MENU")
                }
            )
        } else {
            ActiveCartAndCheckoutContent(viewModel = viewModel)
        }
    }
}

@Composable
fun ActiveCartAndCheckoutContent(viewModel: TiffinViewModel) {
    val isGujarati = viewModel.language.collectAsState().value == "GU"
    val clipboardManager = LocalClipboardManager.current

    val lunchInCart by viewModel.lunchInCart.collectAsState()
    val dinnerInCart by viewModel.dinnerInCart.collectAsState()
    val lunchQty by viewModel.lunchQty.collectAsState()
    val dinnerQty by viewModel.dinnerQty.collectAsState()
    val lunchRoti by viewModel.lunchRoti.collectAsState()
    val dinnerRoti by viewModel.dinnerRoti.collectAsState()

    val formName by viewModel.formName.collectAsState()
    val formPhone by viewModel.formPhone.collectAsState()
    val formAltPhone by viewModel.formAltPhone.collectAsState()
    val formHouseNo by viewModel.formHouseNo.collectAsState()
    val formBuilding by viewModel.formBuilding.collectAsState()
    val formStreet by viewModel.formStreet.collectAsState()
    val formLandmark by viewModel.formLandmark.collectAsState()
    val formPincode by viewModel.formPincode.collectAsState()
    val formCity by viewModel.formCity.collectAsState()
    val formState by viewModel.formState.collectAsState()

    val mapAddress by viewModel.selectedAddress.collectAsState()
    val detectingLoc by viewModel.detectingLocation.collectAsState()
    val userMapQuery by viewModel.mapQuery.collectAsState()

    val slotSelection by viewModel.deliverySlot.collectAsState()
    val payMethod by viewModel.paymentMethod.collectAsState()
    val enteredUtr by viewModel.utrNumber.collectAsState()
    val imgScreenshot by viewModel.screenshotName.collectAsState()
    val paymentMsg by viewModel.paymentMessage.collectAsState()

    // Price details calculation
    val menusList by viewModel.menus.collectAsState()
    val lPrice = menusList.find { it.mealType == "lunch" }?.price ?: 120.0
    val dPrice = menusList.find { it.mealType == "dinner" }?.price ?: 120.0

    var subTotal = 0.0
    var qCount = 0
    if (lunchInCart) {
        subTotal += lPrice * lunchQty
        qCount += lunchQty
    }
    if (dinnerInCart) {
        subTotal += dPrice * dinnerQty
        qCount += dinnerQty
    }

    // Calculations: Taxes & Packaging (5% + ₹10 per meal packing), Delivery (₹30)
    val taxAmt = subTotal * 0.05 + (10 * qCount)
    val deliveryChg = if (qCount > 0) 30.0 else 0.0
    val totalBillAmount = if (qCount > 0) subTotal + taxAmt + deliveryChg else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 1. Cart Summary Screen ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x33D4AF37), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.t("cart"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = RoyalGold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!lunchInCart && !dinnerInCart) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Empty",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isGujarati) "કાર્ટ ખાલી છે! કૃપા કરીને મેનૂમાંથી ભોજન પસંદ કરો." else "Cart is empty! Select meals from Menu tab.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        if (lunchInCart) {
                            CartItemRow(
                                title = viewModel.t("menu_lunch"),
                                qty = lunchQty,
                                roti = lunchRoti,
                                price = lPrice,
                                isLunch = true,
                                viewModel = viewModel
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        if (dinnerInCart) {
                            if (lunchInCart) Divider(color = Color(0x1AD4AF37), modifier = Modifier.padding(bottom = 12.dp))
                            CartItemRow(
                                title = viewModel.t("menu_dinner"),
                                qty = dinnerQty,
                                roti = dinnerRoti,
                                price = dPrice,
                                isLunch = false,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }

        // Only show form and details if items are present
        if (lunchInCart || dinnerInCart) {
            // --- 2. Google Maps Location Section ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0x33D4AF37), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "1. Delivery Location",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = RoyalGold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Stylized Map Drawing Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0x22D4AF37), RoundedCornerShape(12.dp))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw map roads/background curves representing Varachha/Adajan Surat
                                drawRect(color = Color(0xFFE8F5E9))

                                // Grid Roads
                                val roadColor = Color(0xFFFFFFFF)
                                val roadWidth = 24f
                                drawLine(roadColor, Offset(0f, 150f), Offset(size.width, 150f), strokeWidth = roadWidth)
                                drawLine(roadColor, Offset(0f, 320f), Offset(size.width, 320f), strokeWidth = roadWidth)
                                drawLine(roadColor, Offset(400f, 0f), Offset(400f, size.height), strokeWidth = roadWidth)

                                // River Tapi Representation (blue curve)
                                val tPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(0f, 50f)
                                    quadraticTo(size.width / 2, 100f, size.width, 30f)
                                }
                                drawPath(
                                    path = tPath,
                                    color = Color(0xFFBBDEFB),
                                    style = Stroke(width = 40f)
                                )

                                // Royal Map Pin Location
                                drawCircle(
                                    color = RoyalGold,
                                    radius = 16f,
                                    center = Offset(400f, 150f)
                                )
                                drawCircle(
                                    color = Color.Black,
                                    radius = 6f,
                                    center = Offset(400f, 150f)
                                )
                            }

                            // GPS Auto detect overlays
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    OutlinedTextField(
                                        value = userMapQuery,
                                        onValueChange = {
                                            viewModel.mapQuery.value = it
                                            viewModel.searchAddressCoords(it)
                                        },
                                        placeholder = { Text("Search location...", fontSize = 11.sp) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp)),
                                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                                        trailingIcon = { Icon(Icons.Default.Search, "Search", modifier = Modifier.size(16.dp)) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = RoyalGold,
                                            unfocusedBorderColor = Color.LightGray
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { viewModel.performAutoDetectAddress() },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("auto_detect_location_gps")
                                    ) {
                                        if (detectingLoc) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black)
                                        } else {
                                            Icon(Icons.Default.MyLocation, "GPS", modifier = Modifier.size(16.dp), tint = Color.Black)
                                        }
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = mapAddress,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- 3. Customer Information Form ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0x33D4AF37), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "2. " + viewModel.t("shipping_details"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = RoyalGold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = formName,
                            onValueChange = { viewModel.formName.value = it },
                            label = { Text(viewModel.t("full_name")) },
                            modifier = Modifier.fillMaxWidth().testTag("input_full_name"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = formPhone,
                                onValueChange = { viewModel.formPhone.value = it },
                                label = { Text(viewModel.t("phone")) },
                                modifier = Modifier.weight(1f).testTag("input_phone"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = formAltPhone,
                                onValueChange = { viewModel.formAltPhone.value = it },
                                label = { Text(viewModel.t("alt_phone")) },
                                modifier = Modifier.weight(1f).testTag("input_alt_phone"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = formHouseNo,
                                onValueChange = { viewModel.formHouseNo.value = it },
                                label = { Text(viewModel.t("house_no")) },
                                modifier = Modifier.weight(1f).testTag("input_house_no"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = formBuilding,
                                onValueChange = { viewModel.formBuilding.value = it },
                                label = { Text(viewModel.t("building")) },
                                modifier = Modifier.weight(1.5f).testTag("input_building"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = formStreet,
                            onValueChange = { viewModel.formStreet.value = it },
                            label = { Text(viewModel.t("street")) },
                            modifier = Modifier.fillMaxWidth().testTag("input_street"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = formLandmark,
                            onValueChange = { viewModel.formLandmark.value = it },
                            label = { Text(viewModel.t("landmark")) },
                            modifier = Modifier.fillMaxWidth().testTag("input_landmark"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = formPincode,
                                onValueChange = { viewModel.formPincode.value = it },
                                label = { Text(viewModel.t("pincode")) },
                                modifier = Modifier.weight(1f).testTag("input_pincode"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = formCity,
                                onValueChange = { viewModel.formCity.value = it },
                                label = { Text(viewModel.t("city")) },
                                modifier = Modifier.weight(1f).testTag("input_city"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                            )
                        }
                    }
                }
            }

            // --- 4. Delivery Slot Selection ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0x33D4AF37), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "3. " + viewModel.t("slot_selection"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = RoyalGold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val slotList = listOf("Lunch" to "Lunch (બપોરે ૧૨:૦૦ થી ૨:૦૦)", "Dinner" to "Dinner (સાંજે ૭:૦૦ થી ૯:૦૦)")
                        slotList.forEach { pair ->
                            val isSel = slotSelection == pair.first
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) Color(0x0AD4AF37) else Color.Transparent)
                                    .clickable { viewModel.deliverySlot.value = pair.first }
                                    .padding(vertical = 12.dp, horizontal = 8.dp)
                                    .testTag("slot_option_${pair.first}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSel,
                                    onClick = { viewModel.deliverySlot.value = pair.first },
                                    colors = RadioButtonDefaults.colors(selectedColor = RoyalGold)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = pair.second, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }

            // --- 5. Payment Methods & Verification Details ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0x33D4AF37), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "4. " + viewModel.t("pay_method"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = RoyalGold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.paymentMethod.value = "UPI" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (payMethod == "UPI") RoyalGold else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (payMethod == "UPI") Color.Black else MaterialTheme.colorScheme.onBackground
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(50.dp).testTag("select_pay_upi")
                            ) {
                                Icon(Icons.Default.QrCodeScanner, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("UPI Payment")
                            }

                            Button(
                                onClick = { viewModel.paymentMethod.value = "COD" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (payMethod == "COD") RoyalGold else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (payMethod == "COD") Color.Black else MaterialTheme.colorScheme.onBackground
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(50.dp).testTag("select_pay_cod")
                            ) {
                                Icon(Icons.Default.CurrencyRupee, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cash On Dev")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (payMethod == "UPI") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = viewModel.t("upi_instructions"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // UPI ID Copy bar
                                val upiId = "bapasitaramtiffin@yesbank"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, Color(0x1AD4AF37), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = upiId,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = RoyalGold
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(upiId))
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, "Copy", modifier = Modifier.size(18.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Compose QR Code simulation drawing inside canvas
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(120.dp)) {
                                        // Draw simulated QR Code grid blocks
                                        val squareSize = size.width / 4
                                        val strokeW = 4f

                                        // Outer bounds
                                        drawRect(Color.White)
                                        drawRect(Color.Black, style = Stroke(width = strokeW * 2))

                                        // QR Corners finder patterns
                                        drawRect(Color.Black, Offset(0f, 0f), androidx.compose.ui.geometry.Size(squareSize, squareSize))
                                        drawRect(Color.White, Offset(strokeW, strokeW), androidx.compose.ui.geometry.Size(squareSize - strokeW*2, squareSize - strokeW*2))
                                        drawRect(Color.Black, Offset(strokeW*2, strokeW*2), androidx.compose.ui.geometry.Size(squareSize - strokeW*4, squareSize - strokeW*4))

                                        drawRect(Color.Black, Offset(size.width - squareSize, 0f), androidx.compose.ui.geometry.Size(squareSize, squareSize))
                                        drawRect(Color.Black, Offset(0f, size.height - squareSize), androidx.compose.ui.geometry.Size(squareSize, squareSize))

                                        // Mock QR modules
                                        drawRect(Color.Black, Offset(squareSize*1.5f, squareSize), androidx.compose.ui.geometry.Size(30f, 30f))
                                        drawRect(Color.Black, Offset(squareSize*2f, squareSize*1.8f), androidx.compose.ui.geometry.Size(40f, 20f))
                                        drawRect(Color.Black, Offset(squareSize*1.1f, squareSize*2.2f), androidx.compose.ui.geometry.Size(20f, 50f))
                                        drawRect(Color.Black, Offset(squareSize*2.2f, squareSize*2.5f), androidx.compose.ui.geometry.Size(35f, 35f))
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Open in payment apps row
                                Text(
                                    text = "Pay using external app:",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    val apps = listOf("GooglePay", "PhonePe", "Paytm", "BHIM")
                                    apps.forEach { appName ->
                                        Button(
                                            onClick = { viewModel.paymentMessage.value = "Opening $appName Secure Link API Gateway..." },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text(appName, fontSize = 9.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // UTR input
                                OutlinedTextField(
                                    value = enteredUtr,
                                    onValueChange = { viewModel.utrNumber.value = it },
                                    label = { Text(viewModel.t("utr_num")) },
                                    modifier = Modifier.fillMaxWidth().testTag("input_utr_number"),
                                    leadingIcon = { Icon(Icons.Default.Tag, null) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Screenshot Simulate Upload Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.screenshotName.value = "screenshot_BST_${System.currentTimeMillis() % 10000}.png"
                                            viewModel.paymentMessage.value = "Payment slip compressed, verified, WebP auto-converted & cached!"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                                        modifier = Modifier.testTag("upload_screenshot_btn")
                                    ) {
                                        Icon(Icons.Default.Upload, null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(viewModel.t("upload_screenshot"), color = Color.Black, fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = if (imgScreenshot.isEmpty()) "No slip selected" else imgScreenshot,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (imgScreenshot.isEmpty()) Color.Gray else PremiumGreen,
                                        maxLines = 1
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x0D2E7D32), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.VerifiedUser, "Secure", tint = PremiumGreen)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Pay cash (₹${totalBillAmount.toInt()}) on delivery at your doorstep.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PremiumGreen
                                )
                            }
                        }
                    }
                }
            }

            // --- 6. Checkout Summary Section ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0x33D4AF37), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = viewModel.t("checkout_summary"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = RoyalGold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(viewModel.t("items"), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                            Text("₹${subTotal.toInt()}")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(viewModel.t("taxes"), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                            Text("₹${taxAmt.toInt()}")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(viewModel.t("delivery"), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                            Text("₹${deliveryChg.toInt()}")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0x1AD4AF37))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                viewModel.t("total"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            Text(
                                "₹${totalBillAmount.toInt()}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = RoyalGold)
                            )
                        }
                    }
                }
            }

            // Real-time backend system output logs
            if (paymentMsg.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
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

            // --- 7. Place Order Button ---
            item {
                Button(
                    onClick = { viewModel.placeOrder() },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("submit_checkout_order")
                ) {
                    Text(
                        text = viewModel.t("place_order"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun CartItemRow(
    title: String,
    qty: Int,
    roti: String,
    price: Double,
    isLunch: Boolean,
    viewModel: TiffinViewModel
) {
    val enRoti = when (roti) {
        "Normal Roti" -> viewModel.t("normal_roti")
        "Bajra Rotla" -> viewModel.t("bajra_rotla")
        "Bhakhri" -> viewModel.t("bhakhri")
        else -> roti
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.5f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Roti Pref: $enRoti",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Text(
                text = "Qty: $qty x ₹${price.toInt()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "₹${(qty * price).toInt()}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RoyalGold),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(
                onClick = {
                    if (isLunch) viewModel.removeLunchFromCart() else viewModel.removeDinnerFromCart()
                },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("remove_from_cart_${if (isLunch) "lunch" else "dinner"}")
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun OrderSuccessScreen(
    orderId: String,
    viewModel: TiffinViewModel,
    onDismiss: () -> Unit
) {
    val isGujarati = viewModel.language.collectAsState().value == "GU"
    val systemResponseMsg by viewModel.paymentMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Golden Icon representation
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = PremiumGreen,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isGujarati) "ઓર્ડર સફળતાપૂર્વક સબમિટ થયો!" else "Thank You For Ordering With Bapa Sitaram Tiffin",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = RoyalGold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your pure home-cooked Gujarati food preparation will begin shortly.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0x33D4AF37), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = viewModel.t("order_id"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = orderId,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                    color = RoyalGold
                )
            }
        }

        if (systemResponseMsg.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = systemResponseMsg,
                    color = Color.Green,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = Color.Black),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("dismiss_success_screen")
        ) {
            Text("Go Back Menu", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}
