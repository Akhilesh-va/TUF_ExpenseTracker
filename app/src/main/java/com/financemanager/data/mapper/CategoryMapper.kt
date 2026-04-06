package com.financemanager.data.mapper

import com.financemanager.data.local.entity.CategoryEntity
import com.financemanager.domain.model.Category
import com.financemanager.domain.model.TransactionType

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = TransactionType.fromStorage(type),
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = type.name,
)
