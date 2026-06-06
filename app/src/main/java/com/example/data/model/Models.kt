package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_menu")
data class DailyMenu(
    @PrimaryKey val mealType: String, // "lunch" or "dinner"
    val veg1: String,
    val veg2: String,
    val kathol: String,
    val special: String, // Dal / Kadhi
    val rice: String, // Rice / Khichdi
    val salad: String,
    val veg1Gu: String,
    val veg2Gu: String,
    val katholGu: String,
    val specialGu: String,
    val riceGu: String,
    val saladGu: String,
    val price: Double,
    val isAvailable: Boolean = true,
    val bannerUrl: String = ""
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String, // BST-XXXXXX
    val customerName: String,
    val customerPhone: String,
    val altPhone: String,
    val houseNo: String,
    val buildingName: String,
    val streetArea: String,
    val landmark: String,
    val pincode: String,
    val city: String,
    val state: String,
    val deliverySlot: String, // "Lunch" or "Dinner"
    val rotiChoice: String, // "Normal Roti", "Bajra Rotla", "Bhakhri"
    val quantity: Int,
    val totalPrice: Double,
    val paymentMethod: String, // "UPI" or "COD"
    val utrNumber: String = "",
    val status: String, // "Pending", "Accepted", "Preparing", "Out For Delivery", "Delivered", "Rejected"
    val isPaid: Boolean = false,
    val dateMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerPhone: String,
    val customerName: String,
    val amount: Double,
    val paymentType: String, // "Credit" (received) or "Debit" (charged)
    val notes: String = "",
    val dateMillis: Long = System.currentTimeMillis()
)
