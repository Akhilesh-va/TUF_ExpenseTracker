package com.financemanager.data.local.database

import com.financemanager.data.local.entity.CategoryEntity

object CategorySeeds {
    val All: List<CategoryEntity> = listOf(
        CategoryEntity("expense_food", "Food", "\uD83C\uDF54", 0xFFFF6B6B, "EXPENSE"),
        CategoryEntity("expense_transport", "Transport", "\uD83D\uDE97", 0xFFFFA726, "EXPENSE"),
        CategoryEntity("expense_shopping", "Shopping", "\uD83D\uDECD", 0xFFAB47BC, "EXPENSE"),
        CategoryEntity("expense_health", "Health", "\uD83D\uDC8A", 0xFF26A69A, "EXPENSE"),
        CategoryEntity("expense_entertainment", "Entertainment", "\uD83C\uDFAC", 0xFF42A5F5, "EXPENSE"),
        CategoryEntity("expense_bills", "Bills", "\uD83E\uDDFE", 0xFF78909C, "EXPENSE"),
        CategoryEntity("expense_education", "Education", "\uD83D\uDCDA", 0xFF7E57C2, "EXPENSE"),
        CategoryEntity("expense_other", "Other", "\u2795", 0xFF90A4AE, "EXPENSE"),
        CategoryEntity("income_salary", "Salary", "\uD83D\uDCBC", 0xFF66BB6A, "INCOME"),
        CategoryEntity("income_freelance", "Freelance", "\uD83D\uDCBB", 0xFF29B6F6, "INCOME"),
        CategoryEntity("income_investment", "Investment", "\uD83D\uDCC8", 0xFFFFCA28, "INCOME"),
        CategoryEntity("income_gift", "Gift", "\uD83C\uDF81", 0xFFEC407A, "INCOME"),
        CategoryEntity("income_other", "Other", "\u2795", 0xFF8D6E63, "INCOME"),
    )
}
