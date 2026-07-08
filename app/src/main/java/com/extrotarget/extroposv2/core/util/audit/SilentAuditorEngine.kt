package com.extrotarget.extroposv2.core.util.audit

import com.extrotarget.extroposv2.core.data.local.dao.ProductDao
import com.extrotarget.extroposv2.core.data.local.dao.SaleDao
import com.extrotarget.extroposv2.core.data.local.dao.reporting.ReportingDao
import com.extrotarget.extroposv2.core.network.ai.GeminiProvider
import com.extrotarget.extroposv2.core.util.security.SecurityManager
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SilentAuditorEngine @Inject constructor(
    private val geminiProvider: GeminiProvider,
    private val securityManager: SecurityManager,
    private val saleDao: SaleDao,
    private val productDao: ProductDao,
    private val reportingDao: ReportingDao
) {
    private suspend fun getModel() = geminiProvider.getModel(
        securityManager.getString(SecurityManager.KEY_GEMINI_API_KEY) ?: ""
    )

    /**
     * Use Case 1: Monitor reports and compare Daily sales vs Closing Sales (Shift)
     */
    suspend fun auditShiftDiscrepancy(shiftId: Long, salesSummary: String, closingData: String): String {
        val model = getModel()
        val prompt = """
            Analyze the following sales summary and closing data for Shift #$shiftId.
            Identify any discrepancies, missing cash, or suspicious patterns.
            
            Sales Summary:
            $salesSummary
            
            Closing Data:
            $closingData
            
            Provide a concise audit report.
        """.trimIndent()
        
        return try {
            val response = model.generateContent(prompt)
            response.text ?: "No insight generated."
        } catch (e: Exception) {
            "Audit failed: ${e.message}"
        }
    }

    /**
     * Use Case 2: Sales Performance Insight
     */
    suspend fun getSalesPerformanceInsight(periodData: String): String {
        val model = getModel()
        val prompt = "Analyze the following sales data for the period and provide performance insights, trends, and growth opportunities:\n$periodData"
        
        return try {
            val response = model.generateContent(prompt)
            response.text ?: "No performance insights available."
        } catch (e: Exception) {
            "Failed to analyze sales performance."
        }
    }

    /**
     * Use Case 3: Best Selling Products
     */
    suspend fun getBestSellersInsight(productStats: String): String {
        val model = getModel()
        val prompt = "Based on the following product sales statistics, identify the top performers and suggest cross-selling strategies:\n$productStats"
        
        return try {
            val response = model.generateContent(prompt)
            response.text ?: "No best-seller insights available."
        } catch (e: Exception) {
            "Failed to analyze best sellers."
        }
    }

    /**
     * Use Case 4: Assist to generate user required reports (e.g. Tender report)
     */
    suspend fun generateNaturalLanguageReport(request: String, rawData: String): String {
        val model = getModel()
        val prompt = """
            The user wants a report based on this request: "$request".
            Use the following raw data to generate a structured report with insights:
            $rawData
        """.trimIndent()

        return try {
            val response = model.generateContent(prompt)
            response.text ?: "Could not generate report."
        } catch (e: Exception) {
            "Report generation failed."
        }
    }

    /**
     * Use Case 5: Search Optimization & Product Finder
     */
    suspend fun getSemanticSearchSuggestions(query: String): List<String> {
        val model = getModel()
        // Limit context to relevant categories or top items to save tokens
        val products = productDao.getAllProducts().first().take(50).joinToString { it.name }
        val prompt = "Based on this product list: $products, suggest the most likely products the user is looking for when they type: '$query'. Return as a comma-separated list of product names ONLY."
        
        return try {
            val response = model.generateContent(prompt)
            response.text?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Use Case 6: Fix CSV Product Templates
     */
    suspend fun repairCsvTemplate(corruptedCsv: String, schema: String): String {
        val model = getModel()
        val prompt = """
            You are a data repair expert. Below is a CSV snippet for a POS product import that may contain errors.
            Fix any missing commas, unclosed quotes, misaligned columns, or data type errors based on this schema:
            $schema
            
            Corrupted Content:
            $corruptedCsv
            
            Return ONLY the valid CSV text without any explanation or markdown formatting.
        """.trimIndent()

        return try {
            val response = model.generateContent(prompt)
            response.text?.replace("```csv", "")?.replace("```", "")?.trim() ?: corruptedCsv
        } catch (e: Exception) {
            corruptedCsv
        }
    }

    /**
     * Use Case 7: Barcode Finder/Item Finder Optimization
     */
    suspend fun findSimilarProductByBarcode(barcode: String, catalogContext: String): String {
        val model = getModel()
        val prompt = """
            A user scanned a product with barcode/SKU: "$barcode", but it wasn't found in the local database.
            Based on the following catalog context and common retail patterns, what is this item likely to be? 
            Suggest a name, category, and estimated price if possible.
            
            Catalog Context:
            $catalogContext
        """.trimIndent()

        return try {
            val response = model.generateContent(prompt)
            response.text ?: "Item not found."
        } catch (e: Exception) {
            "Search failed."
        }
    }

    /**
     * Use Case 8: Stock Inventory Assistance
     */
    suspend fun getInventoryManagementAdvice(inventoryData: String): String {
        val model = getModel()
        val prompt = """
            Analyze the following inventory and stock movement data. 
            Identify items at risk of stockout, slow-moving items, and suggest reorder quantities based on velocity.
            
            Inventory Data:
            $inventoryData
        """.trimIndent()
        
        return try {
            val response = model.generateContent(prompt)
            response.text ?: "No inventory advice available."
        } catch (e: Exception) {
            "Failed to analyze inventory."
        }
    }
}
