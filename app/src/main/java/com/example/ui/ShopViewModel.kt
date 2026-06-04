package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Product
import com.example.data.SaleRecord
import com.example.data.ShopRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShopViewModel(private val repository: ShopRepository) : ViewModel() {

    // Main streams from Room
    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allSales: StateFlow<List<SaleRecord>> = repository.allSales
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Derived metric flows
    val totalRevenue: StateFlow<Double> = allSales.map { sales ->
        sales.sumOf { it.totalAmount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val totalProfit: StateFlow<Double> = allSales.map { sales ->
        sales.sumOf { it.totalAmount - (it.costPrice * it.quantitySold) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val totalSalesTransactions: StateFlow<Int> = allSales.map { sales ->
        sales.size
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Actions
    fun addProduct(
        name: String,
        sku: String,
        price: Double,
        costPrice: Double,
        stockLevel: Int,
        lowStockThreshold: Int,
        category: String,
        supplierName: String,
        supplierEmail: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val product = Product(
                name = name.trim(),
                sku = sku.trim(),
                price = price,
                costPrice = costPrice,
                stockLevel = stockLevel,
                lowStockThreshold = lowStockThreshold,
                category = category.trim().ifEmpty { "General" },
                supplierName = supplierName.trim(),
                supplierEmail = supplierEmail.trim()
            )
            repository.insertProduct(product)
            onComplete()
        }
    }

    fun updateProduct(product: Product, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertProduct(product)
            onComplete()
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun updateStock(product: Product, newStock: Int) {
        viewModelScope.launch {
            repository.insertProduct(product.copy(stockLevel = newStock))
        }
    }

    // Records a sale on an existing product, decrements inventory stock
    fun recordProductSale(
        product: Product,
        quantity: Int,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (product.stockLevel < quantity) {
                onFailure("Insufficient stock. Only ${product.stockLevel} units remaining.")
                return@launch
            }
            
            val success = repository.recordSale(
                productId = product.id,
                quantity = quantity,
                unitPrice = product.price,
                costPrice = product.costPrice,
                productName = product.name
            )
            if (success) {
                onSuccess()
            } else {
                onFailure("Record sale failed. Product might no longer exist.")
            }
        }
    }

    // Direct sale recording for custom item sales
    fun recordCustomSale(
        productName: String,
        quantity: Int,
        price: Double,
        costPrice: Double,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.recordCustomSale(
                productName = productName.trim().ifEmpty { "Custom Sale Item" },
                quantitySold = quantity,
                unitPrice = price,
                costPrice = costPrice
            )
            onComplete()
        }
    }

    // Populates fallback demo products so a new user can immediately interact with the system
    fun populateDemoInventory() {
        viewModelScope.launch {
            val demoItems = listOf(
                Product(name = "Premium Wireless Headphones", sku = "HD-992-W", price = 120.00, costPrice = 65.00, stockLevel = 15, lowStockThreshold = 4, category = "Electronics", supplierName = "SoundCorp", supplierEmail = "reorders@soundcorp.com"),
                Product(name = "Organic Green Tea Blend", sku = "TEA-ORG-GT", price = 14.50, costPrice = 6.00, stockLevel = 3, lowStockThreshold = 5, category = "Beverages", supplierName = "NatureLeaf Oils & Teas", supplierEmail = "sales@natureleaf.com"),
                Product(name = "Sleek Stainless Water Bottle", sku = "BTL-SST-24", price = 25.00, costPrice = 10.00, stockLevel = 22, lowStockThreshold = 8, category = "Accessories", supplierName = "EcoFlow", supplierEmail = "b2b@ecoflow.com"),
                Product(name = "Matte Black Mechanical Keyboard", sku = "KBD-MECH-BL", price = 85.00, costPrice = 40.00, stockLevel = 2, lowStockThreshold = 3, category = "Electronics", supplierName = "KeyTek Ltd", supplierEmail = "stock@keytek.com"),
                Product(name = "Scented Soy Wax Candle (Vanilla)", sku = "CDL-SOY-VN", price = 18.00, costPrice = 8.50, stockLevel = 4, lowStockThreshold = 5, category = "Home Decor", supplierName = "CozyHome Wholesalers", supplierEmail = "sales@cozyhome.com")
            )
            for (item in demoItems) {
                repository.insertProduct(item)
            }
        }
    }
}

class ShopViewModelFactory(private val repository: ShopRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShopViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShopViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
