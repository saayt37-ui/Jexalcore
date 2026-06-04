package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Product
import com.example.data.SaleRecord
import com.example.ui.theme.AlertLowStock
import com.example.ui.theme.AlertLowStockContainer
import com.example.ui.theme.StableGreen
import com.example.ui.theme.WarningOrange
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDashboardScreen(viewModel: ShopViewModel, modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Core database states
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val salesRecords by viewModel.allSales.collectAsStateWithLifecycle()
    
    // Financial metrics states
    val totalRevenue by viewModel.totalRevenue.collectAsStateWithLifecycle()
    val totalProfit by viewModel.totalProfit.collectAsStateWithLifecycle()
    
    // Dialog control states
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showRecordSaleDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToEditStock by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Store Logo",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "StoreFlow",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Central Market Branch",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_navigation_bar"),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "Overview") },
                    label = { Text("Overview") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Inventory") },
                    label = { Text("Inventory") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Sales Log") },
                    label = { Text("Sales Log") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddProductDialog = true },
                    modifier = Modifier.testTag("add_product_fab"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product")
                }
            } else if (selectedTab == 0 || selectedTab == 2) {
                ExtendedFloatingActionButton(
                    onClick = { showRecordSaleDialog = true },
                    modifier = Modifier.testTag("record_sale_fab"),
                    icon = { Icon(Icons.Default.Add, contentDescription = "New Sale") },
                    text = { Text("Record Sale") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> DashboardTab(
                    products = products,
                    lowStockProducts = lowStockProducts,
                    totalRevenue = totalRevenue,
                    totalProfit = totalProfit,
                    salesCount = salesRecords.size,
                    onQuickRefill = { p -> viewModel.updateStock(p, p.stockLevel + 10) },
                    onNavigateToInventory = { selectedTab = 1 },
                    onPopulateDemoData = { viewModel.populateDemoInventory() },
                    onRecordSaleClick = { showRecordSaleDialog = true }
                )
                1 -> InventoryTab(
                    products = products,
                    onEditProduct = { p -> productToEdit = p },
                    onEditStock = { p -> productToEditStock = p },
                    onDeleteProduct = { p -> viewModel.deleteProduct(p) }
                )
                2 -> SalesHistoryTab(
                    salesRecords = salesRecords
                )
            }
        }
    }

    // Dialogs
    if (showAddProductDialog) {
        AddProductDialog(
            onDismiss = { showAddProductDialog = false },
            onConfirm = { name, sku, price, costPrice, stock, threshold, category, sName, sEmail ->
                viewModel.addProduct(
                    name = name,
                    sku = sku,
                    price = price,
                    costPrice = costPrice,
                    stockLevel = stock,
                    lowStockThreshold = threshold,
                    category = category,
                    supplierName = sName,
                    supplierEmail = sEmail
                )
                showAddProductDialog = false
            }
        )
    }

    if (showRecordSaleDialog) {
        RecordSaleDialog(
            products = products,
            onDismiss = { showRecordSaleDialog = false },
            onConfirm = { product, quantity ->
                viewModel.recordProductSale(
                    product = product,
                    quantity = quantity
                )
                showRecordSaleDialog = false
            },
            onConfirmCustom = { customName, quantity, salePrice, costPrice ->
                viewModel.recordCustomSale(
                    productName = customName,
                    quantity = quantity,
                    price = salePrice,
                    costPrice = costPrice
                )
                showRecordSaleDialog = false
            }
        )
    }

    productToEdit?.let { product ->
        EditProductDialog(
            product = product,
            onDismiss = { productToEdit = null },
            onConfirm = { updated ->
                viewModel.updateProduct(updated)
                productToEdit = null
            }
        )
    }

    productToEditStock?.let { product ->
        EditStockDialog(
            product = product,
            onDismiss = { productToEditStock = null },
            onConfirm = { newStock ->
                viewModel.updateStock(product, newStock)
                productToEditStock = null
            }
        )
    }
}

// ==========================================
// DB/OVERVIEW TAB DESIGN
// ==========================================
@Composable
fun DashboardTab(
    products: List<Product>,
    lowStockProducts: List<Product>,
    totalRevenue: Double,
    totalProfit: Double,
    salesCount: Int,
    onQuickRefill: (Product) -> Unit,
    onNavigateToInventory: () -> Unit,
    onPopulateDemoData: () -> Unit,
    onRecordSaleClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("overview_tab_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Store Health Checklist
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Store Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Keep track of active sales performance, monitor running stock shortages, and record incoming receipts securely.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    
                    if (products.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onPopulateDemoData,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Populate Sample Inventory")
                        }
                    }
                }
            }
        }

        // Financial KPI Indicators
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sales Metric
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("kpi_sales_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .height(112.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Total Sales",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "SALES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                "DAILY SALES",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = NumberFormat.getCurrencyInstance(Locale.US).format(totalRevenue),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Profit Metric
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("kpi_profit_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD0BCFF)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .height(112.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Total Profit",
                                tint = Color(0xFF381E72),
                                modifier = Modifier.size(20.dp)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF381E72).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "MARGIN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF381E72),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                "NET MARGINS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF381E72).copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = NumberFormat.getCurrencyInstance(Locale.US).format(totalProfit),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF381E72),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Automatic Low-Stock Alerts Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alerts",
                        tint = if (lowStockProducts.isNotEmpty()) AlertLowStock else StableGreen
                    )
                    Text(
                        text = "Low Stock Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (lowStockProducts.isNotEmpty()) AlertLowStockContainer else MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "${lowStockProducts.size} alert(s)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (lowStockProducts.isNotEmpty()) AlertLowStock else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (lowStockProducts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "All good",
                                tint = StableGreen,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                "All items adequately in stock!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            items(lowStockProducts, key = { it.id }) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("low_stock_item_card_${product.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, AlertLowStock.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = AlertLowStockContainer
                                ) {
                                    Text(
                                        text = "${product.stockLevel} left (Thresh: ${product.lowStockThreshold})",
                                        color = AlertLowStock,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                if (product.supplierName.isNotEmpty()) {
                                    Text(
                                        text = "Supplier: ${product.supplierName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        // Active Refill Assist Control
                        Button(
                            onClick = { onQuickRefill(product) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("quick_refill_btn_${product.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add 10 Stock",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("+10 Unit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Operations Panel
        item {
            Text(
                text = "Quick Services",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onRecordSaleClick() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "New Receipt",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "New Checkout",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToInventory() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "See Stock",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            "View Inventory",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// INVENTORY VIEW TAB DESIGN
// ==========================================
@Composable
fun InventoryTab(
    products: List<Product>,
    onEditProduct: (Product) -> Unit,
    onEditStock: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    // Grab distinct categories dynamically for styling filters
    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct().filter { it.isNotEmpty() }
    }

    // Advanced Local Filtering
    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { product ->
            val matchesSearch = product.name.contains(searchQuery, ignoreCase = true) ||
                    product.sku.contains(searchQuery, ignoreCase = true) ||
                    product.category.contains(searchQuery, ignoreCase = true)
            
            val matchesCategory = selectedCategory == "All" || product.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("inventory_view_tab")
    ) {
        // Search Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, SKU...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Search")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_search_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Horizontal list of filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // Inner List
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No matching products found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Confirm search keyword or click '+' below to record standard properties of a new product.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("inventory_product_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductRow(
                        product = product,
                        onEditDetails = { onEditProduct(product) },
                        onEditStock = { onEditStock(product) },
                        onDelete = { onDeleteProduct(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductRow(
    product: Product,
    onEditDetails: () -> Unit,
    onEditStock: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("product_row_card_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (product.isLowStock) AlertLowStock.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = product.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (product.sku.isNotEmpty()) {
                            Text(
                                text = "SKU: ${product.sku}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Pricing Info Right side
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = product.formattedPrice(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Cost: ${product.formattedCostPrice()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Adaptive Inventory Level Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val (pillColor, textColor, textLabel) = when {
                    product.stockLevel == 0 -> Triple(AlertLowStockContainer, AlertLowStock, "OUT OF STOCK")
                    product.isLowStock -> Triple(WarningOrange.copy(alpha = 0.2f), WarningOrange, "LOW STOCK")
                    else -> Triple(StableGreen.copy(alpha = 0.2f), StableGreen, "IN STOCK")
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = pillColor
                ) {
                    Text(
                        text = textLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "${product.stockLevel} unit(s) left",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (product.isLowStock) AlertLowStock else MaterialTheme.colorScheme.onSurface
                )
            }

            // Expanded Controls Segment
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (product.supplierName.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Supplier: ${product.supplierName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (product.supplierEmail.isNotEmpty()) {
                                Text(
                                    text = product.supplierEmail,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onEditStock,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_edit_stock_${product.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Edit Stock", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Adjust Stock", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onEditDetails,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_edit_details_${product.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Details", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit Details", fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag("btn_delete_product_${product.id}"),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = AlertLowStockContainer,
                                contentColor = AlertLowStock
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Product")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SALES HISTORY VIEW TAB DESIGN
// ==========================================
@Composable
fun SalesHistoryTab(
    salesRecords: List<SaleRecord>
) {
    if (salesRecords.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "No Sales",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "No sales recorded yet.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Perform a transaction by clicking the 'Record Sale' button to automatically log sales margins and decrease inventory stock.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Sales Transactions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("sales_records_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(salesRecords, key = { it.id }) { sale ->
                    SaleRecordRow(sale = sale)
                }
            }
        }
    }
}

@Composable
fun SaleRecordRow(sale: SaleRecord) {
    val margin = sale.totalAmount - (sale.costPrice * sale.quantitySold)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = sale.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${sale.quantitySold} unit(s) x " + NumberFormat.getCurrencyInstance(Locale.US).format(sale.unitPrice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = sale.formattedDate(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Margin breakdown & receipt total
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = sale.formattedTotalAmount(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = StableGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Margin: +" + NumberFormat.getCurrencyInstance(Locale.US).format(margin),
                        color = StableGreen,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// ADDING DIALOG FORM MODAL
// ==========================================
@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String, sku: String, price: Double, costPrice: Double,
        stock: Int, threshold: Int, category: String, sName: String, sEmail: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var costPriceStr by remember { mutableStateOf("") }
    var stockStr by remember { mutableStateOf("") }
    var thresholdStr by remember { mutableStateOf("5") }
    var category by remember { mutableStateOf("") }
    var supplierName by remember { mutableStateOf("") }
    var supplierEmail by remember { mutableStateOf("") }

    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        modifier = Modifier.testTag("add_product_dialog_container"),
        onDismissRequest = onDismiss,
        title = { Text("Add New Product") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; isError = false },
                        label = { Text("Product Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("field_product_name"),
                        isError = isError && name.isEmpty(),
                        singleLine = true
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text("SKU / Barcode") },
                            modifier = Modifier.weight(1f).testTag("field_product_sku"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            modifier = Modifier.weight(1f).testTag("field_product_category"),
                            placeholder = { Text("e.g. Snacks") },
                            singleLine = true
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Sale Price ($) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("field_product_price"),
                            isError = isError && priceStr.toDoubleOrNull() == null,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = costPriceStr,
                            onValueChange = { costPriceStr = it },
                            label = { Text("Cost Price ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("field_product_cost"),
                            singleLine = true
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = stockStr,
                            onValueChange = { stockStr = it },
                            label = { Text("Initial Stock *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("field_product_stock"),
                            isError = isError && stockStr.toIntOrNull() == null,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = thresholdStr,
                            onValueChange = { thresholdStr = it },
                            label = { Text("Low Alert Lim.") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("field_product_threshold"),
                            isError = isError && thresholdStr.toIntOrNull() == null,
                            singleLine = true
                        )
                    }
                }

                item {
                    Text(
                        "Supplier Information (Optional)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text("Supplier Name") },
                        modifier = Modifier.fillMaxWidth().testTag("field_supplier_name"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = supplierEmail,
                        onValueChange = { supplierEmail = it },
                        label = { Text("Supplier Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth().testTag("field_supplier_email"),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pDouble = priceStr.toDoubleOrNull()
                    val cDouble = costPriceStr.toDoubleOrNull() ?: 0.0
                    val sInt = stockStr.toIntOrNull()
                    val tInt = thresholdStr.toIntOrNull() ?: 5
                    
                    if (name.isNotEmpty() && pDouble != null && sInt != null) {
                        onConfirm(name, sku, pDouble, cDouble, sInt, tInt, category, supplierName, supplierEmail)
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier.testTag("btn_save_new_product")
            ) {
                Text("Save Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ==========================================
// RECORDING SALE / CHECKOUT COMPOSABLE
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordSaleDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (Product, Int) -> Unit,
    onConfirmCustom: (customName: String, quantity: Int, salePrice: Double, costPrice: Double) -> Unit
) {
    var isCustomSale by remember { mutableStateOf(false) }

    // Standard listed product selection state
    var selectedProductIndex by remember { mutableIntStateOf(-1) }
    var quantityStr by remember { mutableStateOf("1") }
    var selectedProductError by remember { mutableStateOf("") }

    // Custom sale input states
    var customName by remember { mutableStateOf("") }
    var customPriceStr by remember { mutableStateOf("") }
    var customCostStr by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Sales Transaction") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector Row (Listed vs Unlisted Custom Sale)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedAssistChip(
                        onClick = { isCustomSale = false },
                        label = { Text("In-Stock Product") },
                        colors = if (!isCustomSale) AssistChipDefaults.elevatedAssistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) else AssistChipDefaults.elevatedAssistChipColors()
                    )
                    ElevatedAssistChip(
                        onClick = { isCustomSale = true },
                        label = { Text("Unlisted/Custom Item") },
                        colors = if (isCustomSale) AssistChipDefaults.elevatedAssistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) else AssistChipDefaults.elevatedAssistChipColors()
                    )
                }

                if (!isCustomSale) {
                    if (products.isEmpty()) {
                        Text(
                            "Your inventory is currently empty. Record a custom/unlisted sale, or add regular products first from the Inventory tab.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        // Dropdown-style selectors or simple clickable rows
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        Text("Select Catalog Product:", style = MaterialTheme.typography.labelLarge)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            val selectedName = if (selectedProductIndex in products.indices) {
                                "${products[selectedProductIndex].name} (${products[selectedProductIndex].stockLevel} left)"
                            } else {
                                "Tap to select product"
                            }
                            
                            OutlinedCard(
                                onClick = { dropdownExpanded = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("catalog_product_picker_trigger")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .heightIn(max = 240.dp)
                            ) {
                                products.forEachIndexed { idx, product ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(product.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(
                                                    "${product.stockLevel} left",
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (product.isLowStock) AlertLowStock else StableGreen
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedProductIndex = idx
                                            dropdownExpanded = false
                                            selectedProductError = ""
                                        }
                                    )
                                }
                            }
                        }

                        if (selectedProductError.isNotEmpty()) {
                            Text(
                                text = selectedProductError,
                                color = AlertLowStock,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("Quantity Sold") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("field_sale_quantity"),
                            singleLine = true
                        )
                    }
                } else {
                    // Custom Product Sale Fields
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it; validationError = false },
                            label = { Text("Product name *") },
                            modifier = Modifier.fillMaxWidth().testTag("field_custom_name"),
                            isError = validationError && customName.isEmpty(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customPriceStr,
                                onValueChange = { customPriceStr = it; validationError = false },
                                label = { Text("Sale Price ($) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("field_custom_price"),
                                isError = validationError && customPriceStr.toDoubleOrNull() == null,
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = customCostStr,
                                onValueChange = { customCostStr = it },
                                label = { Text("Cost Price ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("field_custom_cost"),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it; validationError = false },
                            label = { Text("Quantity Sold *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("field_custom_quantity"),
                            isError = validationError && quantityStr.toIntOrNull() == null,
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityStr.toIntOrNull() ?: 1
                    
                    if (!isCustomSale) {
                        if (selectedProductIndex !in products.indices) {
                            selectedProductError = "Please select a product from the list."
                            return@Button
                        }
                        val selectedProduct = products[selectedProductIndex]
                        if (selectedProduct.stockLevel < qty) {
                            selectedProductError = "Cannot sell more than available. (${selectedProduct.stockLevel} in stock)"
                            return@Button
                        }
                        onConfirm(selectedProduct, qty)
                    } else {
                        val price = customPriceStr.toDoubleOrNull()
                        val cost = customCostStr.toDoubleOrNull() ?: 0.0
                        val rawQty = quantityStr.toIntOrNull()
                        
                        if (customName.isNotEmpty() && price != null && rawQty != null) {
                            onConfirmCustom(customName, rawQty, price, cost)
                        } else {
                            validationError = true
                        }
                    }
                },
                modifier = Modifier.testTag("btn_confirm_sale")
            ) {
                Text("Confirm Sale")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ==========================================
// STOCK QUICK ADJUST DIALOG
// ==========================================
@Composable
fun EditStockDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var stockStr by remember { mutableStateOf(product.stockLevel.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Stock: ${product.name}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Set the current physical stock level in the shop for validation:",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it; isError = false },
                    label = { Text("New Stock Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("field_edit_stock"),
                    isError = isError,
                    singleLine = true
                )
                if (isError) {
                    Text("Please enter a valid, positive integer", color = AlertLowStock, style = MaterialTheme.typography.bodySmall)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { stockStr = (stockStr.toIntOrNull()?.plus(10) ?: 10).toString() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+10 Unit")
                    }
                    Button(
                        onClick = { stockStr = ((stockStr.toIntOrNull()?.minus(10) ?: 0).coerceAtLeast(0)).toString() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("-10 Unit")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = stockStr.toIntOrNull()
                    if (amount != null && amount >= 0) {
                        onConfirm(amount)
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier.testTag("btn_save_stock_adjustment")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ==========================================
// FULL PRODUCT PROPERTY EDIT DIALOG
// ==========================================
@Composable
fun EditProductDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var sku by remember { mutableStateOf(product.sku) }
    var priceStr by remember { mutableStateOf(product.price.toString()) }
    var costPriceStr by remember { mutableStateOf(product.costPrice.toString()) }
    var thresholdStr by remember { mutableStateOf(product.lowStockThreshold.toString()) }
    var category by remember { mutableStateOf(product.category) }
    var supplierName by remember { mutableStateOf(product.supplierName) }
    var supplierEmail by remember { mutableStateOf(product.supplierEmail) }
    
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Product Attributes") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; isError = false },
                        label = { Text("Product Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_field_product_name"),
                        isError = isError && name.isEmpty(),
                        singleLine = true
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text("SKU / Barcode") },
                            modifier = Modifier.weight(1f).testTag("edit_field_product_sku"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            modifier = Modifier.weight(1f).testTag("edit_field_product_category"),
                            singleLine = true
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Sale Price ($) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("edit_field_product_price"),
                            isError = isError && priceStr.toDoubleOrNull() == null,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = costPriceStr,
                            onValueChange = { costPriceStr = it },
                            label = { Text("Cost Price ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("edit_field_product_cost"),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = thresholdStr,
                        onValueChange = { thresholdStr = it },
                        label = { Text("Low Alert Level") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("edit_field_product_threshold"),
                        isError = isError && thresholdStr.toIntOrNull() == null,
                        singleLine = true
                    )
                }

                item {
                    Text(
                        "Supplier Information",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text("Supplier Name") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_field_supplier_name"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = supplierEmail,
                        onValueChange = { supplierEmail = it },
                        label = { Text("Supplier Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth().testTag("edit_field_supplier_email"),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pDouble = priceStr.toDoubleOrNull()
                    val cDouble = costPriceStr.toDoubleOrNull() ?: 0.0
                    val tInt = thresholdStr.toIntOrNull()
                    
                    if (name.isNotEmpty() && pDouble != null && tInt != null) {
                        onConfirm(
                            product.copy(
                                name = name.trim(),
                                sku = sku.trim(),
                                price = pDouble,
                                costPrice = cDouble,
                                lowStockThreshold = tInt,
                                category = category.trim().ifEmpty { "General" },
                                supplierName = supplierName.trim(),
                                supplierEmail = supplierEmail.trim()
                            )
                        )
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier.testTag("btn_confirm_edit_product")
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
