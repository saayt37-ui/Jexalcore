package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE stockLevel <= lowStockThreshold ORDER BY stockLevel ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET stockLevel = :newStock WHERE id = :productId")
    suspend fun updateProductStock(productId: Int, newStock: Int)

    @Query("SELECT * FROM sales_records ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleRecord(sale: SaleRecord)

    @Transaction
    suspend fun recordSaleAndUpdateStock(
        productId: Int,
        quantity: Int,
        unitPrice: Double,
        costPrice: Double,
        productName: String
    ): Boolean {
        val product = getProductById(productId) ?: return false
        if (product.stockLevel < quantity) return false // Prevent selling more than we have
        
        val newStock = product.stockLevel - quantity
        updateProductStock(productId, newStock)
        
        val sale = SaleRecord(
            productId = productId,
            productName = productName,
            quantitySold = quantity,
            unitPrice = unitPrice,
            costPrice = costPrice,
            totalAmount = quantity * unitPrice
        )
        insertSaleRecord(sale)
        return true
    }

    // Direct sale recording for custom item sales (e.g. unlisted products)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordCustomSale(sale: SaleRecord)
}
