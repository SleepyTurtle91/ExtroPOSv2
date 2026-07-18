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
    COMPLETED,

    // Retail / Purchase Order
    DRAFT,
    SUBMITTED,
    APPROVED,
    ORDERED,
    PARTIALLY_RECEIVED,
    RECEIVED,
    CLOSED,

    // F&B Specific
    OPEN,
    SENT,
    COOKING,
    READY,
    SERVED,

    // Service / Carwash Specific
    WAITING,
    IN_PROGRESS,

    // Hospitality Specific
    BOOKED,
    CHECKED_IN,
    CHECKED_OUT
}
