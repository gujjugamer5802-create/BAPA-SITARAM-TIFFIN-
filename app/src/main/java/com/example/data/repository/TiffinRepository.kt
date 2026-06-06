package com.example.data.repository

import com.example.data.dao.TiffinDao
import com.example.data.model.DailyMenu
import com.example.data.model.OrderEntity
import com.example.data.model.PaymentRecord
import kotlinx.coroutines.flow.Flow

class TiffinRepository(private val tiffinDao: TiffinDao) {

    val allMenus: Flow<List<DailyMenu>> = tiffinDao.getAllMenuFlow()
    val allOrders: Flow<List<OrderEntity>> = tiffinDao.getAllOrdersFlow()
    val allPayments: Flow<List<PaymentRecord>> = tiffinDao.getAllPaymentsFlow()

    suspend fun insertMenu(menu: DailyMenu) {
        tiffinDao.insertMenu(menu)
    }

    suspend fun insertOrder(order: OrderEntity) {
        tiffinDao.insertOrder(order)
    }

    suspend fun updateOrderStatus(orderId: String, status: String) {
        if (status == "Delivered") {
            // Automatically mark order as paid if COD is delivered, or if it is marked delivered by default
            tiffinDao.updateOrderStatus(orderId, status)
            // Retrieve and auto balance if needed
            val order = tiffinDao.getOrderById(orderId)
            if (order != null && order.paymentMethod == "COD") {
                tiffinDao.updateOrderPaymentStatus(orderId, true)
            }
        } else {
            tiffinDao.updateOrderStatus(orderId, status)
        }
    }

    suspend fun updateOrderPaymentStatus(orderId: String, isPaid: Boolean) {
        tiffinDao.updateOrderPaymentStatus(orderId, isPaid)
    }

    suspend fun insertPayment(payment: PaymentRecord) {
        tiffinDao.insertPayment(payment)
    }

    fun getOrdersByCustomer(phone: String): Flow<List<OrderEntity>> {
        return tiffinDao.getOrdersByCustomerFlow(phone)
    }

    fun getPaymentsByCustomer(phone: String): Flow<List<PaymentRecord>> {
        return tiffinDao.getPaymentsByCustomerFlow(phone)
    }
}
