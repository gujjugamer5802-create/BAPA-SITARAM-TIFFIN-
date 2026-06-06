package com.example.data.dao

import androidx.room.*
import com.example.data.model.DailyMenu
import com.example.data.model.OrderEntity
import com.example.data.model.PaymentRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TiffinDao {

    // --- Menu Queries ---
    @Query("SELECT * FROM daily_menu")
    fun getAllMenuFlow(): Flow<List<DailyMenu>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenu(menu: DailyMenu)

    @Query("SELECT * FROM daily_menu WHERE mealType = :mealType LIMIT 1")
    suspend fun getMenuByMealType(mealType: String): DailyMenu?

    // --- Order Queries ---
    @Query("SELECT * FROM orders ORDER BY dateMillis DESC")
    fun getAllOrdersFlow(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("UPDATE orders SET isPaid = :isPaid WHERE orderId = :orderId")
    suspend fun updateOrderPaymentStatus(orderId: String, isPaid: Boolean)

    @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE customerPhone = :phone")
    fun getOrdersByCustomerFlow(phone: String): Flow<List<OrderEntity>>

    // --- Payment Record Queries ---
    @Query("SELECT * FROM payments ORDER BY dateMillis DESC")
    fun getAllPaymentsFlow(): Flow<List<PaymentRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentRecord)

    @Query("SELECT * FROM payments WHERE customerPhone = :phone ORDER BY dateMillis DESC")
    fun getPaymentsByCustomerFlow(phone: String): Flow<List<PaymentRecord>>
}
