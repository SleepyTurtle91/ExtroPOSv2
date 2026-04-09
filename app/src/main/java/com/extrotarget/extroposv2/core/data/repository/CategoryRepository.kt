package com.extrotarget.extroposv2.core.data.repository

import com.extrotarget.extroposv2.core.data.local.dao.CategoryDao
import com.extrotarget.extroposv2.core.data.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun getCategoryById(id: String): Category? = categoryDao.getCategoryById(id)

    suspend fun insertCategory(category: Category) = categoryDao.insertCategory(category)

    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
}