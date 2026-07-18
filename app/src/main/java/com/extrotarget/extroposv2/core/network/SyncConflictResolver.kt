package com.extrotarget.extroposv2.core.network

import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.model.loyalty.Member
import com.extrotarget.extroposv2.core.data.model.Sale
import javax.inject.Inject
import javax.inject.Singleton

interface ConflictStrategy<T> {
    fun resolve(local: T, remote: T): T
}

@Singleton
class SyncConflictResolver @Inject constructor() {

    val productStrategy = object : ConflictStrategy<Product> {
        override fun resolve(local: Product, remote: Product): Product {
            // LWW (Last Write Wins) based on serverUpdatedAt or updatedAt
            val localTime = local.serverUpdatedAt ?: local.updatedAt
            val remoteTime = remote.serverUpdatedAt ?: remote.updatedAt
            
            return if (remoteTime >= localTime) {
                // Remote wins, but preserve some local-only state if needed
                remote.copy(stockQuantity = local.stockQuantity) // Stock handled separately by movement merge
            } else {
                local
            }
        }
    }

    val memberStrategy = object : ConflictStrategy<Member> {
        override fun resolve(local: Member, remote: Member): Member {
            val localTime = local.serverUpdatedAt ?: local.updatedAt
            val remoteTime = remote.serverUpdatedAt ?: remote.updatedAt
            
            return if (remoteTime >= localTime) remote else local
        }
    }

    val transactionStrategy = object : ConflictStrategy<Sale> {
        override fun resolve(local: Sale, remote: Sale): Sale {
            // Transactions are Immutable. remote should always win if it exists on server
            return remote
        }
    }
}
