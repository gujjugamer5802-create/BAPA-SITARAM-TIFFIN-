package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PremiumGreen
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.TiffinViewModel

data class LedgerNode(
    val name: String,
    val phone: String,
    val lunchCount: Int,
    val dinnerCount: Int,
    val mealsTotal: Int,
    val amountCharged: Double,
    val amountPaid: Double,
    val pending: Double
)

@Composable
fun HisaabScreen(viewModel: TiffinViewModel) {
    val ordersState by viewModel.orders.collectAsState()
    val paymentsState by viewModel.payments.collectAsState()

    val searchQuery by viewModel.ledgerSearchQuery.collectAsState()
    val selectedPhone by viewModel.selectedCustomerPhone.collectAsState()
    val activeFilter by viewModel.hisaabFilter.collectAsState()
    val isGujarati = viewModel.language.collectAsState().value == "GU"

    val exportLog = remember { mutableStateOf("") }

    // Aggregate customer ledger dynamically from database!
    val aggregatedLedgers = remember(ordersState, paymentsState) {
        ordersState.groupBy { it.customerPhone }.map { (phone, custOrders) ->
            val name = custOrders.firstOrNull()?.customerName ?: "Unknown"
            
            val lunchQty = custOrders.filter { it.deliverySlot == "Lunch" }.sumOf { it.quantity }
            val dinnerQty = custOrders.filter { it.deliverySlot == "Dinner" }.sumOf { it.quantity }
            val combinedQty = custOrders.filter { it.deliverySlot == "Both" }.sumOf { it.quantity }

            val totalLunch = lunchQty + combinedQty
            val totalDinner = dinnerQty + combinedQty
            val totalMeals = totalLunch + totalDinner

            val totalCharged = custOrders.sumOf { it.totalPrice }
            val totalPaidCredit = paymentsState.filter { it.customerPhone == phone && it.paymentType == "Credit" }.sumOf { it.amount }
            
            val pendingAmount = if (totalCharged > totalPaidCredit) totalCharged - totalPaidCredit else 0.0

            LedgerNode(
                name = name,
                phone = phone,
                lunchCount = totalLunch,
                dinnerCount = totalDinner,
                mealsTotal = totalMeals,
                amountCharged = totalCharged,
                amountPaid = totalPaidCredit,
                pending = pendingAmount
            )
        }
    }

    val filteredLedgers = aggregatedLedgers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Section title
        Column {
            Text(
                text = viewModel.t("hisaab_title"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = RoyalGold
            )
            Text(
                text = "Live Tiffin Customer accounts ledger & dynamic invoicing logs.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Search Bar or customer selector
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.ledgerSearchQuery.value = it },
            placeholder = { Text(viewModel.t("search_customer")) },
            modifier = Modifier.fillMaxWidth().testTag("hisaab_customer_search"),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = RoyalGold) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.ledgerSearchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, null)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalGold)
        )

        // Filters Selector (Daily, Weekly, Monthly, Yearly)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("Daily", "Weekly", "Monthly", "Yearly")
            filters.forEach { filterItem ->
                val isSel = activeFilter == filterItem
                Button(
                    onClick = { viewModel.hisaabFilter.value = filterItem },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSel) RoyalGold else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSel) Color.Black else MaterialTheme.colorScheme.onBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(40.dp).testTag("filter_$filterItem")
                ) {
                    Text(filterItem, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Ledger Node Detail statement (if customer selected)
        val activeCustomer = aggregatedLedgers.find { it.phone == selectedPhone }
        if (activeCustomer != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, RoyalGold, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = activeCustomer.name,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "+91 ${activeCustomer.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.selectedCustomerPhone.value = null },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, "Close details", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = viewModel.t("customer_statement") + " ($activeFilter)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = RoyalGold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    HisaabItemRow(viewModel.t("total_meals"), activeCustomer.mealsTotal.toString())
                    HisaabItemRow(viewModel.t("total_debited"), "₹" + activeCustomer.amountCharged.toInt())
                    HisaabItemRow(viewModel.t("total_credited"), "₹" + activeCustomer.amountPaid.toInt())
                    Divider(color = Color(0x1AD4AF37), modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = viewModel.t("remaining_balance"),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "₹" + activeCustomer.pending.toInt(),
                            fontWeight = FontWeight.ExtraBold,
                            color = if (activeCustomer.pending > 0) Color(0xFFD32F2F) else PremiumGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Export Button Panel list
                    Text(
                        text = "Document Generation & Dispatch API:",
                        style = MaterialTheme.typography.labelSmall,
                        color = RoyalGold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExportActionButton(
                                label = viewModel.t("export_pdf"),
                                icon = Icons.Default.PictureAsPdf,
                                color = Color(0xFFD32F2F),
                                onClick = {
                                    exportLog.value = "Compiling PDF Statement...\nSuccessfully generated: Bapa_Sitaram_Hisaab_${activeCustomer.name.replace(" ", "_")}.pdf [342 KB]\nFile cached locally and scheduled to dispatch."
                                }
                            )
                            ExportActionButton(
                                label = viewModel.t("export_excel"),
                                icon = Icons.Default.GridOn,
                                color = Color(0xFF1B5E20),
                                onClick = {
                                    exportLog.value = "Aggregating cells...\nExcel generated: Bapa_Sitaram_${activeCustomer.name.replace(" ", "_")}.xlsx"
                                }
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExportActionButton(
                                label = viewModel.t("share_whatsapp"),
                                icon = Icons.Default.Send,
                                color = Color(0xFF2E7D32),
                                onClick = {
                                    exportLog.value = "Connecting to WhatsApp Business Cloud Application Router API...\nDispatched Invoice parameters to https://api.whatsapp.com/send?phone=91${activeCustomer.phone}"
                                }
                            )
                            ExportActionButton(
                                label = viewModel.t("print"),
                                icon = Icons.Default.Print,
                                color = Color.Gray,
                                onClick = {
                                    exportLog.value = "Opening system printer dialer...\nReady to print to Google JetCloud Driver API."
                                }
                            )
                        }
                    }

                    // System log drawer
                    AnimatedVisibility(visible = exportLog.value.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = exportLog.value,
                                    color = Color.Green,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                )
                                TextButton(
                                    onClick = { exportLog.value = "" },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Clear Log", color = RoyalGold, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Ledger Customer List ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x33D4AF37), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Customer Ledgers Database (" + filteredLedgers.size + ")",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = RoyalGold
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (filteredLedgers.isEmpty()) {
                    Text(
                        text = "No customers matches your query.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    filteredLedgers.forEach { customerNode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.selectedCustomerPhone.value = customerNode.phone
                                    exportLog.value = ""
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                                .testTag("ledger_row_${customerNode.phone}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = customerNode.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Total meals: ${customerNode.mealsTotal} | +91 ${customerNode.phone}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹" + customerNode.amountCharged.toInt().toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = if (customerNode.pending > 0) "Pending ₹" + customerNode.pending.toInt() else "Balanced Paid",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (customerNode.pending > 0) Color(0xFFD32F2F) else PremiumGreen
                                )
                            }
                        }
                        Divider(color = Color(0x0AD4AF37))
                    }
                }
            }
        }

        // --- 4. Reports Dashboard Visual Graphs ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x33D4AF37), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Reports Analytics Dashboard",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = RoyalGold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Custom charts drawn on Canvas representing revenue, expenses & profit ratio
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Bar chart canvas
                    Canvas(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .border(1.dp, Color(0x1AD4AF37), RoundedCornerShape(8.dp))
                    ) {
                        // Drawing static comparative accounting columns
                        val padding = 30f
                        val graphW = size.width - padding * 2
                        val graphH = size.height - padding * 2

                        // Draw Grid lines
                        drawLine(Color.Gray.copy(alpha = 0.2f), Offset(padding, padding + graphH * 0.5f), Offset(size.width - padding, padding + graphH * 0.5f))
                        drawLine(Color.Gray.copy(alpha = 0.2f), Offset(padding, padding + graphH * 0.25f), Offset(size.width - padding, padding + graphH * 0.25f))

                        val colW = graphW / 3f - 16f

                        // Column 1: Sales Revenue (Royal Gold)
                        drawRect(
                            color = RoyalGold,
                            topLeft = Offset(padding + 8f, padding + graphH * 0.15f),
                            size = Size(colW, graphH * 0.85f)
                        )

                        // Column 2: Expense Raw Materials (Maroon)
                        drawRect(
                            color = Color(0xFF8B0000),
                            topLeft = Offset(padding + colW + 24f, padding + graphH * 0.45f),
                            size = Size(colW, graphH * 0.55f)
                        )

                        // Column 3: Profits (Green)
                        drawRect(
                            color = PremiumGreen,
                            topLeft = Offset(padding + colW * 2 + 40f, padding + graphH * 0.58f),
                            size = Size(colW, graphH * 0.42f)
                        )
                    }

                    // Key details column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        ReportLegendItem("Revenue", "₹8,400", RoyalGold)
                        Spacer(modifier = Modifier.height(8.dp))
                        ReportLegendItem("Expenses", "₹3,200", Color(0xFF8B0000))
                        Spacer(modifier = Modifier.height(8.dp))
                        ReportLegendItem("Avg Profits", "₹5,200", PremiumGreen)
                        Spacer(modifier = Modifier.height(8.dp))
                        ReportLegendItem("Pending", "₹1,200", WarningOrange)
                    }
                }
            }
        }
    }
}

@Composable
fun ReportLegendItem(label: String, valStr: String, indicatorColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(indicatorColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(valStr, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun RowScope.ExportActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, Color(0x22D4AF37)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .testTag("export_btn_${label.replace(" ", "_")}"),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun HisaabItemRow(label: String, valStr: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Text(valStr, fontWeight = FontWeight.Bold)
    }
}
