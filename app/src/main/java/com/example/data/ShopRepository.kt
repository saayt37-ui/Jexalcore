package com.example.data

import kotlinx.coroutines.flow.Flow

class ShopRepository(private val shopDao: ShopDao) {

    val allProducts: Flow<List<Product>> = shopDao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = shopDao.getLowStockProducts()
    val allSales: Flow<List<SaleRecord>> = shopDao.getAllSales()

    suspend fun getProductById(id: Int): Product? {
        return shopDao.getProductById(id)
    }

    suspend fun insertProduct(product: Product) {
        shopDao.insertProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        shopDao.deleteProduct(product)
    }

    suspend fun recordSale(
        productId: Int,
        quantity: Int,
        unitPrice: Double,
        costPrice: Double,
        productName: String
    ): Boolean {
        return shopDao.recordSaleAndUpdateStock(productId, quantity, unitPrice, costPrice, productName)
    }

    suspend fun recordCustomSale(
        productName: String,
        quantitySold: Int,
        unitPrice: Double,
        costPrice: Double
    ) {
        val customSale = SaleRecord(
            productId = -1, // Use -1 mapping for custom/unlisted product sales
            productName = productName,
            quantitySold = quantitySold,
            unitPrice = unitPrice,
            costPrice = costPrice,
            totalAmount = quantitySold * unitPrice
        )
        shopDao.recordCustomSale(customSale)
    }
}
