package com.extrotarget.extroposv2.core.domain.commerce

/**
 * A shared enum for domain-agnostic status tracking across different industry lifecycles.
 */
enum class WorkflowStatus {
    // Shared / Core
    PENDING,
    PAID,
    CANCELLED,
    VOIDED,

    // F&B Specific
    OPEN,
    SENT,
    COOKING,
    READY,
    SERVED,

    // Service / Carwash Specific
    WAITING,
    IN_PROGRESS,
    COMPLETED,

    // Hospitality Specific
    BOOKED,
    CHECKED_IN,
    CHECKED_OUT
}
