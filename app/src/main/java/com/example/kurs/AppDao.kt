package com.example.tipclik4

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AppDao {

    @Insert
    suspend fun insertUser(user: User)

    @Insert
    suspend fun insertOrder(order: Order)

    @Insert
    suspend fun insertRole(role: Role)

    @Insert
    suspend fun insertPaperType(paperType: PaperType)

    @Insert
    suspend fun insertPrintType(printType: PrintType)

    @Insert
    suspend fun insertOrderType(orderType: OrderType)

    @Update
    suspend fun updateOrder(order: Order)

    @Query("SELECT * FROM User WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM Role")
    suspend fun getAllRoles(): List<Role>

    @Delete
    suspend fun deleteOrder(order: Order)

    @Query("DELETE FROM `Order` WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: Long)

    @Query("SELECT * FROM PaperType")
    suspend fun getAllPaperTypes(): List<PaperType>

    @Query("SELECT * FROM PrintType")
    suspend fun getAllPrintTypes(): List<PrintType>

    @Query("SELECT * FROM OrderType")
    suspend fun getAllOrderTypes(): List<OrderType>

    @Query("SELECT * FROM PaperType WHERE name = :name")
    suspend fun getPaperTypeByName(name: String): PaperType?

    @Query("SELECT * FROM PrintType WHERE name = :name")
    suspend fun getPrintTypeByName(name: String): PrintType?

    @Query("SELECT * FROM OrderType WHERE name = :name")
    suspend fun getOrderTypeByName(name: String): OrderType?

    @Query("SELECT * FROM `Order` WHERE idUser = :userId")
    suspend fun getOrdersByUserId(userId: Long): List<Order>

    @Query("SELECT * FROM `Order` WHERE id = :orderId")
    suspend fun getOrderById(orderId: Long): Order?
    @Query("SELECT * FROM `Order`")
    suspend fun getAllOrders(): List<Order>
}