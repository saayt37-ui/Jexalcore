package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sku: String = "",
    val price: Double,
    val costPrice: Double = 0.0,
    val stockLevel: Int,
    val lowStockThreshold: Int = 5,
    val category: String = "General",
    val supplierName: String = "",
    val supplierEmail: String = ""
) {
    val isLowStock: Boolean
        get() = stockLevel <= lowStockThreshold

    fun formattedPrice(): String {
        return NumberFormat.getCurrencyInstance(Locale.US).format(price)
    }

    fun formattedCostPrice(): String {
        return NumberFormat.getCurrencyInstance(Locale.US).format(costPrice)
    }
}

@Entity(tableName = "sales_records")
data class SaleRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val productName: String,
    val quantitySold: Int,
    val unitPrice: Double,
    val costPrice: Double,
    val totalAmount: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun formattedTotalAmount(): String {
        return NumberFormat.getCurrencyInstance(Locale.US).format(totalAmount)
    }

    fun formattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
