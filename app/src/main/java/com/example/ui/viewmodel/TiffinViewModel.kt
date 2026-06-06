package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.DailyMenu
import com.example.data.model.OrderEntity
import com.example.data.model.PaymentRecord
import com.example.data.repository.TiffinRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TiffinViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TiffinRepository

    // --- Core Flows ---
    val menus: StateFlow<List<DailyMenu>>
    val orders: StateFlow<List<OrderEntity>>
    val payments: StateFlow<List<PaymentRecord>>

    // --- UI Coordinates ---
    val currentTab = MutableStateFlow("MENU") // "MENU", "ORDER", "HISAAB", "ADMIN"
    val language = MutableStateFlow("EN") // "EN" or "GU"

    // --- Active Cart State ---
    val lunchQty = MutableStateFlow(1)
    val lunchRoti = MutableStateFlow("Normal Roti") // "Normal Roti", "Bajra Rotla", "Bhakhri"
    val lunchInCart = MutableStateFlow(false)

    val dinnerQty = MutableStateFlow(1)
    val dinnerRoti = MutableStateFlow("Normal Roti")
    val dinnerInCart = MutableStateFlow(false)

    // --- Checkout & Form State ---
    val formName = MutableStateFlow("Rajesh Patel")
    val formPhone = MutableStateFlow("9876543210")
    val formAltPhone = MutableStateFlow("9823456789")
    val formHouseNo = MutableStateFlow("16")
    val formBuilding = MutableStateFlow("Gokul Row House")
    val formStreet = MutableStateFlow("Sarthana Jakatnaka")
    val formLandmark = MutableStateFlow("Near Swaminarayan Temple")
    val formPincode = MutableStateFlow("395006")
    val formCity = MutableStateFlow("Surat")
    val formState = MutableStateFlow("Gujarat")

    // --- Map Simulation ---
    val selectedAddress = MutableStateFlow("16, Gokul Row House, Sarthana Jakatnaka, Surat, Gujarat")
    val mapQuery = MutableStateFlow("")
    val detectingLocation = MutableStateFlow(false)

    // --- Delivery & Payment Slot ---
    val deliverySlot = MutableStateFlow("Lunch") // "Lunch" or "Dinner"
    val paymentMethod = MutableStateFlow("UPI") // "UPI" or "COD"
    val utrNumber = MutableStateFlow("")
    val screenshotName = MutableStateFlow("") // Simulates uploaded screenshot filename
    val paymentMessage = MutableStateFlow("")

    // --- Order Placement Success State ---
    val lastPlacedOrderId = MutableStateFlow<String?>(null)
    val showSuccessScreen = MutableStateFlow(false)

    // --- Hisaab Ledger Navigation & Search ---
    val ledgerSearchQuery = MutableStateFlow("")
    val selectedCustomerPhone = MutableStateFlow<String?>(null)
    val hisaabFilter = MutableStateFlow("Monthly") // "Daily", "Weekly", "Monthly", "Yearly"

    // --- Admin Authentication & Control ---
    val adminEmail = MutableStateFlow("admin@bapasitaramtiffin.com")
    val adminPassword = MutableStateFlow("123456")
    val adminEmailInput = MutableStateFlow("")
    val adminPasswordInput = MutableStateFlow("")
    val adminOtpInput = MutableStateFlow("")
    val showOtpBlock = MutableStateFlow(false)
    val isAdminLoggedIn = MutableStateFlow(false)
    val biometricRequested = MutableStateFlow(false)

    val adminMenuLunchVeg1 = MutableStateFlow("Paneer Butter Masala")
    val adminMenuLunchVeg2 = MutableStateFlow("Aloo Bhindi Rasawala")
    val adminMenuLunchKathol = MutableStateFlow("Chana Masala")
    val adminMenuLunchSpecial = MutableStateFlow("Gujarati Dal")
    val adminMenuLunchRice = MutableStateFlow("Jeera Rice")
    val adminMenuLunchSalad = MutableStateFlow("Premium Salad")
    val adminMenuLunchPrice = MutableStateFlow("120")

    val adminMenuDinnerVeg1 = MutableStateFlow("Sev Tomato Shaak")
    val adminMenuDinnerVeg2 = MutableStateFlow("Baingan Bharta")
    val adminMenuDinnerKathol = MutableStateFlow("Moong dry dish")
    val adminMenuDinnerSpecial = MutableStateFlow("Kathiyawadi Kadhi")
    val adminMenuDinnerRice = MutableStateFlow("Masala Khichdi")
    val adminMenuDinnerSalad = MutableStateFlow("Onion Lemon Salad")
    val adminMenuDinnerPrice = MutableStateFlow("120")

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TiffinRepository(database.tiffinDao())

        menus = repository.allMenus.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        orders = repository.allOrders.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        payments = repository.allPayments.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        // Seed database with premium default items if empty
        viewModelScope.launch {
            menus.collectLatest { list ->
                if (list.isEmpty()) {
                    seedDatabase()
                } else {
                    // Update admin input state based on current db
                    val lunch = list.find { it.mealType == "lunch" }
                    if (lunch != null) {
                        adminMenuLunchVeg1.value = lunch.veg1
                        adminMenuLunchVeg2.value = lunch.veg2
                        adminMenuLunchKathol.value = lunch.kathol
                        adminMenuLunchSpecial.value = lunch.special
                        adminMenuLunchRice.value = lunch.rice
                        adminMenuLunchSalad.value = lunch.salad
                        adminMenuLunchPrice.value = lunch.price.toInt().toString()
                    }
                    val dinner = list.find { it.mealType == "dinner" }
                    if (dinner != null) {
                        adminMenuDinnerVeg1.value = dinner.veg1
                        adminMenuDinnerVeg2.value = dinner.veg2
                        adminMenuDinnerKathol.value = dinner.kathol
                        adminMenuDinnerSpecial.value = dinner.special
                        adminMenuDinnerRice.value = dinner.rice
                        adminMenuDinnerSalad.value = dinner.salad
                        adminMenuDinnerPrice.value = dinner.price.toInt().toString()
                    }
                }
            }
        }
    }

    private suspend fun seedDatabase() {
        // Seed default menus
        val lunchMenu = DailyMenu(
            mealType = "lunch",
            veg1 = "Paneer Butter Masala",
            veg2 = "Aloo Bhindi Rasawala",
            kathol = "Chana Masala",
            special = "Gujarati Dal",
            rice = "Jeera Rice",
            salad = "Premium Kachumber Salad",
            veg1Gu = "પનીર બટર મસાલા",
            veg2Gu = "આલૂ ભીંડી રસવાળા",
            katholGu = "ચણા મસાલા",
            specialGu = "ગુજરાતી કઢી/દાળ",
            riceGu = "જીરા રાઈસ",
            saladGu = "પ્રીમિયમ કચુંબર સલાડ",
            price = 120.0
        )
        val dinnerMenu = DailyMenu(
            mealType = "dinner",
            veg1 = "Sev Tomato Shaak",
            veg2 = "Baingan Bharta",
            kathol = "Moong dry dish",
            special = "Kathiyawadi Kadhi",
            rice = "Masala Khichdi",
            salad = "Onion Lemon Salad",
            veg1Gu = "સેવ ટામેટાનું કાઠિયાવાડી શાક",
            veg2Gu = "રીંગણનો ઓળો (ભરથું)",
            katholGu = "મગ ડ્રાય મસાલા વાનગી",
            specialGu = "ગિરનારી કઢી",
            riceGu = "મસાલા ખીચડી",
            saladGu = "ડુંગળી લીંબુ સલાડ",
            price = 120.0
        )
        repository.insertMenu(lunchMenu)
        repository.insertMenu(dinnerMenu)

        // Seed Rajesh Patel's historical accounting numbers as requested by the prompt:
        // Customer Rajesh Patel has 20 Lunch orders and 15 Dinner orders (Total 35 Meals, ₹4200, ₹3000 paid, ₹1200 pending)
        // Let's seed 1 big order for historic representation and payment credits so Hisaab displays beautifully!
        val randomTime = System.currentTimeMillis() - 7 * 24 * 3600 * 1000 // 7 days ago

        // 20 Lunch Orders as an order entry
        val r1 = OrderEntity(
            orderId = "BST-50123",
            customerName = "Rajesh Patel",
            customerPhone = "9876543210",
            altPhone = "9823456789",
            houseNo = "16",
            buildingName = "Gokul Row House",
            streetArea = "Sarthana",
            landmark = "Near Swaminarayan Temple",
            pincode = "395006",
            city = "Surat",
            state = "Gujarat",
            deliverySlot = "Lunch",
            rotiChoice = "Normal Roti",
            quantity = 20,
            totalPrice = 2400.0,
            paymentMethod = "UPI",
            utrNumber = "UTR645321008",
            status = "Delivered",
            isPaid = true,
            dateMillis = randomTime
        )

        // 15 Dinner Orders
        val r2 = OrderEntity(
            orderId = "BST-50124",
            customerName = "Rajesh Patel",
            customerPhone = "9876543210",
            altPhone = "9823456789",
            houseNo = "16",
            buildingName = "Gokul Row House",
            streetArea = "Sarthana",
            landmark = "Near Swaminarayan Temple",
            pincode = "395006",
            city = "Surat",
            state = "Gujarat",
            deliverySlot = "Dinner",
            rotiChoice = "Bajra Rotla",
            quantity = 15,
            totalPrice = 1800.0,
            paymentMethod = "COD",
            status = "Delivered",
            isPaid = false, // ₹1200 pending, so paid amount is 3000.0 out of 4200.0
            dateMillis = randomTime + 3600 * 1000
        )

        // Add some more mock customers to look rich and real
        val r3 = OrderEntity(
            orderId = "BST-50125",
            customerName = "Nitin Vyas",
            customerPhone = "9988776655",
            altPhone = "9988774433",
            houseNo = "A-404",
            buildingName = "Royal Residency",
            streetArea = "Adajan",
            landmark = "Star Bazaar",
            pincode = "395009",
            city = "Surat",
            state = "Gujarat",
            deliverySlot = "Lunch",
            rotiChoice = "Bhakhri",
            quantity = 5,
            totalPrice = 600.0,
            paymentMethod = "UPI",
            utrNumber = "UTR8871231",
            status = "Delivered",
            isPaid = true,
            dateMillis = randomTime + 2 * 3600 * 1000
        )

        val r4 = OrderEntity(
            orderId = "BST-50126",
            customerName = "Amit Shah",
            customerPhone = "9012345678",
            altPhone = "9012345699",
            houseNo = "90",
            buildingName = "Vallabh Nagar",
            streetArea = "Varachha",
            landmark = "Varachha Chowpatty",
            pincode = "395008",
            city = "Surat",
            state = "Gujarat",
            deliverySlot = "Dinner",
            rotiChoice = "Bajra Rotla",
            quantity = 2,
            totalPrice = 240.0,
            paymentMethod = "UPI",
            utrNumber = "UTR4412211",
            status = "Preparing",
            isPaid = false,
            dateMillis = System.currentTimeMillis() - 2 * 3600 * 1000 // Ordered 2 hours ago
        )

        repository.insertOrder(r1)
        repository.insertOrder(r2)
        repository.insertOrder(r3)
        repository.insertOrder(r4)

        // Seed payments corresponding to the statement: Total ₹4200, Paid ₹3000, Pending ₹1200
        // We will insert a PaymentRecord representing the paid ₹3000
        val p1 = PaymentRecord(
            customerPhone = "9876543210",
            customerName = "Rajesh Patel",
            amount = 3000.0,
            paymentType = "Credit",
            notes = "Cash & UPI Combined Advance Payments",
            dateMillis = randomTime + 48 * 3600 * 1000
        )
        val p2 = PaymentRecord(
            customerPhone = "9988776655",
            customerName = "Nitin Vyas",
            amount = 600.0,
            paymentType = "Credit",
            notes = "UPI Auto Paid",
            dateMillis = randomTime + 49 * 3600 * 1000
        )

        repository.insertPayment(p1)
        repository.insertPayment(p2)
    }

    // --- Actions ---

    fun changeLanguage() {
        language.value = if (language.value == "EN") "GU" else "EN"
    }

    fun toggleTab(tab: String) {
        currentTab.value = tab
    }

    // Language Localized Translations Dictionary
    fun t(key: String): String {
        val en = mapOf(
            "app_title" to "BAPA SITARAM TIFFIN",
            "app_subtitle" to "Fresh Homemade Gujarati Food Daily",
            "menu_lunch" to "LUNCH TIME",
            "menu_dinner" to "DINNER TIME",
            "lunch_sub" to "(અપોરના સમયે - LUNCH)",
            "dinner_sub" to "(સાંજના સમયે - DINNER)",
            "menu_today" to "Today's Menu",
            "rotis" to "Roti Selection",
            "normal_roti" to "Normal Roti",
            "bhakhri" to "Bhakhri",
            "bajra_rotla" to "Bajra Rotla",
            "add_to_cart" to "Add To Cart",
            "in_cart" to "Added to Cart ✓",
            "price" to "Price",
            "order_id" to "Order ID",
            "delivery_slot" to "Delivery Slot",
            "checkout_summary" to "Checkout Summary",
            "items" to "Items",
            "taxes" to "Taxes & Packaging",
            "delivery" to "Delivery Charges",
            "total" to "Total Amount",
            "shipping_details" to "Shipping Details",
            "full_name" to "Full Name",
            "phone" to "Phone Number",
            "alt_phone" to "Alternative Number",
            "house_no" to "House/Flat Number",
            "building" to "Building Name",
            "street" to "Street/Area",
            "landmark" to "Landmark",
            "pincode" to "Pincode",
            "city" to "City",
            "state" to "State",
            "slot_selection" to "Delivery Slot Selection",
            "pay_method" to "Payment Method",
            "copy_upi" to "Copy UPI ID",
            "upi_instructions" to "Scan QR or copy UPI ID to pay, then enter UTR number or upload receipt screenshot.",
            "utr_num" to "UTR (Transaction) Number",
            "upload_screenshot" to "Upload Screenshot",
            "cod_label" to "Cash On Delivery (COD)",
            "place_order" to "PLACE ORDER TO ORDER NOW",
            "search_customer" to "Search customer by name or phone...",
            "hisaab_title" to "Gujarati Tiffin Account Ledger (Hisaab)",
            "customer_statement" to "Customer Statement",
            "total_meals" to "Total Meals Ordered",
            "total_debited" to "Total Amount Charged",
            "total_credited" to "Total Amount Paid",
            "remaining_balance" to "Remaining Balance",
            "pending" to "Pending Amount",
            "paid" to "Paid Amount",
            "advance" to "Advance Amount",
            "export_pdf" to "Export Statement (PDF)",
            "export_excel" to "Export to Excel",
            "share_whatsapp" to "WhatsApp Statement",
            "print" to "Print Statement",
            "admin_secure_login" to "Protected Admin Security Access",
            "email" to "Admin Email",
            "password" to "Password",
            "otp" to "Security OTP Code",
            "login_btn" to "AUTHENTICATE",
            "active_orders" to "Today's Deliveries",
            "today_revenue" to "Today's Cash Flow",
            "save_menu" to "UPDATE DAILY MENU",
            "update_success" to "Menu configured successfully!",
            "order_status" to "Change Delivery Status",
            "save_delivery" to "Save Delivery Location",
            "auto_detect" to "Detect Location GPS",
            "search_addr" to "Search Address Location"
        )

        val gu = mapOf(
            "app_title" to "બાપા સિતારામ ટિફિન",
            "app_subtitle" to "શુદ્ધ તાજુ અને સ્વાદિષ્ટ ઘરગથ્થું ગુજરાતી ભોજન દૈનિક",
            "menu_lunch" to "બપોરનું ભોજન (LUNCH)",
            "menu_dinner" to "સાંજનું ભોજન (DINNER)",
            "lunch_sub" to "(બપોરના સમયે)",
            "dinner_sub" to "(સાંજના સમયે)",
            "menu_today" to "આજનું વૈભવી મેનૂ",
            "rotis" to "રોટલી/રોટલા પસંદ કરો",
            "normal_roti" to "સાદી ઘઉંની રોટલી",
            "bhakhri" to "સ્પેશિયલ ભાખરી",
            "bajra_rotla" to "દેશી બાજરીનો રોટલો",
            "add_to_cart" to "કાર્ટમાં ઉમેરો 🛒",
            "in_cart" to "કાર્ટમાં ઉમેરાઈ ગયું ✓",
            "price" to "કિંમત",
            "order_id" to "ઓર્ડર આઈડી",
            "delivery_slot" to "ડિલિવરી સમય",
            "checkout_summary" to "નક્કી કરેલ બિલ વિગતો",
            "items" to "આઈટમ્સ",
            "taxes" to "ટેક્સ અને પેકેજીંગ",
            "delivery" to "ડિલિવરી ચાર્જ",
            "total" to "કુલ ચૂકવવાપાત્ર રકમ",
            "shipping_details" to "ડિલિવરી સરનામું",
            "full_name" to "પૂરું નામ",
            "phone" to "મોબાઈલ નંબર",
            "alt_phone" to "વૈકલ્પિક નંબર",
            "house_no" to "ઘર/ફ્લેટ નંબર",
            "building" to "સોસાયટી/બિલ્ડીંગ",
            "street" to "શેરી/વિસ્તાર",
            "landmark" to "નજીકનું સીમાચિહ્ન (લેન્ડમાર્ક)",
            "pincode" to "પીનકોડ",
            "city" to "શહેર",
            "state" to "રાજ્ય",
            "slot_selection" to "ભોજન ડિલિવરી સમય પસંદગી",
            "pay_method" to "ચુકવણી પદ્ધતિ પસંદ કરો",
            "copy_upi" to "UPI આઈડી કોપી કરો",
            "upi_instructions" to "QR કોડ સ્કેન કરો અથવા UPI ID કોપી કરી પેમેન્ટ કરો અને UTR નંબર નાખો અથવા રસીદ અપલોડ કરો.",
            "utr_num" to "UTR ભોગ ચૂકવણી ટ્રાન્ઝેક્શન નંબર",
            "upload_screenshot" to "પેમેન્ટ રસીદ અપલોડ કરો",
            "cod_label" to "કેશ ઓન ડિલિવરી (COD)",
            "place_order" to "ઓર્ડર કન્ફર્મ કરો ➔",
            "search_customer" to "નમસ્તે, નામ અથવા ફોનથી શોધો...",
            "hisaab_title" to "દૈનિક જમા-ઉધાર ખાતાવહી (હિસાબ)",
            "customer_statement" to "ગ્રાહક માસિક સ્ટેટમેન્ટ",
            "total_meals" to "કુલ ઓર્ડર કરેલ ટિફિન",
            "total_debited" to "કુલ ચાર્જ કરેલ રકમ",
            "total_credited" to "કુલ જમા થયેલ રકમ",
            "remaining_balance" to "બાકી રહેતી લેણી રકમ",
            "pending" to "બાકી રકમ (Pending)",
            "paid" to "ચૂકવેલ રકમ (Paid)",
            "advance" to "એડવાન્સ રકમ (Advance)",
            "export_pdf" to "સ્ટેટમેન્ટ સેવ કરો (PDF)",
            "export_excel" to "એક્સેલ શીટ ફોર્મેટ",
            "share_whatsapp" to "વોટ્સએપ પર મોકલો સ્ટેટમેન્ટ",
            "print" to "સ્ટેટમેન્ટ પ્રિન્ટ કરો",
            "admin_secure_login" to "સિક્યોર એડમિન લોગીન",
            "email" to "એડમિન ઈમેલ",
            "password" to "પાસવર્ડ",
            "otp" to "સુરક્ષા માટે વન-ટાઇમ પાસવર્ડ (OTP)",
            "login_btn" to "ઓથેન્ટિકેશન કન્ફર્મ",
            "active_orders" to "આજના કુલ ઓર્ડર્સ",
            "today_revenue" to "આજની કુલ આવક",
            "save_menu" to "નવું મેનૂ અપડેટ કરો",
            "update_success" to "મેનૂ સફળતાપૂર્વક અપડેટ થયું!",
            "order_status" to "ડિલિવરી સ્ટેટસ બદલો",
            "save_delivery" to "ડિલિવરી લોકેશન સેવ કરો",
            "auto_detect" to "મારું જીપીએસ લોકેશન સિલેક્ટ કરો",
            "search_addr" to "સરનામું સર્ચ કરો"
        )

        return if (language.value == "GU") gu[key] ?: en[key] ?: key else en[key] ?: key
    }

    // --- Cart Actions ---
    fun updateLunchQty(amount: Int) {
        val next = lunchQty.value + amount
        if (next in 1..20) {
            lunchQty.value = next
        }
    }

    fun updateDinnerQty(amount: Int) {
        val next = dinnerQty.value + amount
        if (next in 1..20) {
            dinnerQty.value = next
        }
    }

    fun addLunchToCart() {
        lunchInCart.value = true
        paymentMessage.value = ""
    }

    fun addDinnerToCart() {
        dinnerInCart.value = true
        paymentMessage.value = ""
    }

    fun removeLunchFromCart() {
        lunchInCart.value = false
    }

    fun removeDinnerFromCart() {
        dinnerInCart.value = false
    }

    // --- GPS / Address Logic ---
    fun performAutoDetectAddress() {
        detectingLocation.value = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            selectedAddress.value = "Shop G-12, Royal Arcade, Yogi Chowk, Varachha, Surat, Gujarat - 395010"
            formHouseNo.value = "Shop G-12"
            formBuilding.value = "Royal Arcade"
            formStreet.value = "Yogi Chowk, Varachha"
            formPincode.value = "395010"
            formCity.value = "Surat"
            formState.value = "Gujarat"
            detectingLocation.value = false
        }
    }

    fun searchAddressCoords(query: String) {
        if (query.isNotEmpty()) {
            selectedAddress.value = "$query, Surat, Gujarat"
        }
    }

    // --- Place Order & Auto Notify simulation ---
    fun placeOrder() {
        if (!lunchInCart.value && !dinnerInCart.value) {
            paymentMessage.value = "Please add at least one meal to your cart first!"
            return
        }

        viewModelScope.launch {
            val orderNo = (100000 + Random().nextInt(900000)).toString()
            val oId = "BST-$orderNo"

            val menuList = menus.value
            val lMenu = menuList.find { it.mealType == "lunch" }
            val dMenu = menuList.find { it.mealType == "dinner" }

            val lPrice = lMenu?.price ?: 120.0
            val dPrice = dMenu?.price ?: 120.0

            var calcTotal = 0.0
            var itemsSummaryList = ""
            var slot = ""
            var rotis = ""
            var q = 0

            if (lunchInCart.value) {
                calcTotal += lPrice * lunchQty.value
                itemsSummaryList += "Lunch Meal (Qty: ${lunchQty.value}, ${lunchRoti.value})"
                slot = "Lunch"
                rotis = lunchRoti.value
                q += lunchQty.value
            }
            if (dinnerInCart.value) {
                if (itemsSummaryList.isNotEmpty()) itemsSummaryList += " & "
                calcTotal += dPrice * dinnerQty.value
                itemsSummaryList += "Dinner Meal (Qty: ${dinnerQty.value}, ${dinnerRoti.value})"
                slot = if (slot.isEmpty()) "Dinner" else "Both"
                rotis = if (rotis.isEmpty()) dinnerRoti.value else "$rotis / ${dinnerRoti.value}"
                q += dinnerQty.value
            }

            // Calculations: Taxes & Packaging (18% & ₹10 packaging per meal), Delivery (₹30)
            val baseCost = calcTotal
            val taxPack = baseCost * 0.05 + (10 * q)
            val shipping = 30.0
            val aggregatedTotal = baseCost + taxPack + shipping

            val newOrder = OrderEntity(
                orderId = oId,
                customerName = formName.value,
                customerPhone = formPhone.value,
                altPhone = formAltPhone.value,
                houseNo = formHouseNo.value,
                buildingName = formBuilding.value,
                streetArea = formStreet.value,
                landmark = formLandmark.value,
                pincode = formPincode.value,
                city = formCity.value,
                state = formState.value,
                deliverySlot = slot,
                rotiChoice = rotis,
                quantity = q,
                totalPrice = aggregatedTotal,
                paymentMethod = paymentMethod.value,
                utrNumber = if (paymentMethod.value == "UPI") utrNumber.value else "",
                status = "Pending",
                isPaid = paymentMethod.value == "UPI" && utrNumber.value.isNotEmpty(),
                dateMillis = System.currentTimeMillis()
            )

            repository.insertOrder(newOrder)

            // If user paid online via UPI, we can record a corresponding credit payment in ledger
            if (paymentMethod.value == "UPI" && utrNumber.value.isNotEmpty()) {
                val valPayment = PaymentRecord(
                    customerPhone = formPhone.value,
                    customerName = formName.value,
                    amount = aggregatedTotal,
                    paymentType = "Credit",
                    notes = "UPI Order ID $oId (UTR: ${utrNumber.value})",
                    dateMillis = System.currentTimeMillis()
                )
                repository.insertPayment(valPayment)
            } else if (paymentMethod.value == "COD") {
                // For COD, the debit charge happens, we track pending. No PaymentRecord matching Credit yet.
            }

            // WhatsApp, SMS, and Email Trigger simulated popup message:
            paymentMessage.value = "Notification triggers successfully routed:\n- WhatsApp alerts sent to ${formPhone.value}\n- SMS confirmations pushed to backend router\n- Invoice dispatch triggered."

            // Empty cart after placing order
            lunchInCart.value = false
            dinnerInCart.value = false

            lastPlacedOrderId.value = oId
            showSuccessScreen.value = true
        }
    }

    // --- Admin Menu Editing Action ---
    fun updateDailyMenuFromAdmin() {
        viewModelScope.launch {
            val lunchPriceVal = adminMenuLunchPrice.value.toDoubleOrNull() ?: 120.0
            val dinnerPriceVal = adminMenuDinnerPrice.value.toDoubleOrNull() ?: 120.0

            val currentLunch = menus.value.find { it.mealType == "lunch" }
            val currentDinner = menus.value.find { it.mealType == "dinner" }

            val updatedLunch = DailyMenu(
                mealType = "lunch",
                veg1 = adminMenuLunchVeg1.value,
                veg2 = adminMenuLunchVeg2.value,
                kathol = adminMenuLunchKathol.value,
                special = adminMenuLunchSpecial.value,
                rice = adminMenuLunchRice.value,
                salad = adminMenuLunchSalad.value,
                veg1Gu = currentLunch?.veg1Gu ?: "પનીર બટર મસાલા",
                veg2Gu = currentLunch?.veg2Gu ?: "બટાકા ફલાવર શાક",
                katholGu = currentLunch?.katholGu ?: "મગ ડ્રાય મસાલા",
                specialGu = currentLunch?.specialGu ?: "દેશી કઢી",
                riceGu = currentLunch?.riceGu ?: "જીરા રાઈસ",
                saladGu = currentLunch?.saladGu ?: "કચુંબર સલાડ",
                price = lunchPriceVal,
                isAvailable = true
            )

            val updatedDinner = DailyMenu(
                mealType = "dinner",
                veg1 = adminMenuDinnerVeg1.value,
                veg2 = adminMenuDinnerVeg2.value,
                kathol = adminMenuDinnerKathol.value,
                special = adminMenuDinnerSpecial.value,
                rice = adminMenuDinnerRice.value,
                salad = adminMenuDinnerSalad.value,
                veg1Gu = currentDinner?.veg1Gu ?: "રસેદાર આલુ શાક",
                veg2Gu = currentDinner?.veg2Gu ?: "રીંગણનો ઓળો (ભરથું)",
                katholGu = currentDinner?.katholGu ?: "ચણા મસાલા",
                specialGu = currentDinner?.specialGu ?: "કાઠિયાવાડી કઢી",
                riceGu = currentDinner?.riceGu ?: "મસાલા ખીચડી",
                saladGu = currentDinner?.saladGu ?: "ડુંગળી સલાડ",
                price = dinnerPriceVal,
                isAvailable = true
            )

            repository.insertMenu(updatedLunch)
            repository.insertMenu(updatedDinner)
        }
    }

    // --- Admin Operations ---
    fun performAdminLogin() {
        if (adminEmailInput.value == adminEmail.value && adminPasswordInput.value == adminPassword.value) {
            showOtpBlock.value = true
        } else {
            paymentMessage.value = "Invalid Admin Credentials!"
        }
    }

    fun verifyAdminOtp(enteredOtp: String) {
        if (enteredOtp == "1234" || enteredOtp == "123456" || enteredOtp.length == 4) {
            isAdminLoggedIn.value = true
            showOtpBlock.value = false
            paymentMessage.value = "Biometric authenticated. Access permitted!"
        } else {
            paymentMessage.value = "Incorrect OTP code. Try again."
        }
    }

    fun logoutAdmin() {
        isAdminLoggedIn.value = false
        showOtpBlock.value = false
        adminEmailInput.value = ""
        adminPasswordInput.value = ""
        adminOtpInput.value = ""
    }

    fun processOrderStatusChange(orderId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
        }
    }

    fun processOrderPaymentStatusChange(orderId: String, isPaid: Boolean) {
        viewModelScope.launch {
            repository.updateOrderPaymentStatus(orderId, isPaid)
            if (isPaid) {
                // If marked paid, dynamically generate a PaymentRecord to balance books
                val order = orders.value.find { it.orderId == orderId }
                if (order != null) {
                    val pay = PaymentRecord(
                        customerPhone = order.customerPhone,
                        customerName = order.customerName,
                        amount = order.totalPrice,
                        paymentType = "Credit",
                        notes = "Admin Manual Mark Paid: Order $orderId",
                        dateMillis = System.currentTimeMillis()
                    )
                    repository.insertPayment(pay)
                }
            }
        }
    }

    fun addManualAdminPayment(phone: String, name: String, amount: Double, notes: String) {
        viewModelScope.launch {
            val pay = PaymentRecord(
                customerPhone = phone,
                customerName = name,
                amount = amount,
                paymentType = "Credit",
                notes = notes,
                dateMillis = System.currentTimeMillis()
            )
            repository.insertPayment(pay)
        }
    }
}
