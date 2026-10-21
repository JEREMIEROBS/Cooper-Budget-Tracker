package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {
    fun getIcon(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "shopping_cart", "cart" -> Icons.Default.ShoppingCart
            "restaurant", "food", "dining" -> Icons.Default.Restaurant
            "home", "house", "rent" -> Icons.Default.Home
            "directions_car", "car", "transport" -> Icons.Default.DirectionsCar
            "bolt", "electricity", "utilities" -> Icons.Default.Bolt
            "movie", "entertainment" -> Icons.Default.Movie
            "favorite", "health" -> Icons.Default.Favorite
            "shopping_bag", "bag", "shopping" -> Icons.Default.ShoppingBag
            "payments", "salary" -> Icons.Default.Payments
            "work", "freelance" -> Icons.Default.Work
            "trending_up", "investments" -> Icons.Default.TrendingUp
            "account_balance_wallet", "wallet" -> Icons.Default.AccountBalanceWallet
            else -> Icons.Default.Category
        }
    }

    val availableIcons = listOf(
        "shopping_cart",
        "restaurant",
        "home",
        "directions_car",
        "bolt",
        "movie",
        "favorite",
        "shopping_bag",
        "payments",
        "work",
        "trending_up",
        "account_balance_wallet"
    )
}
