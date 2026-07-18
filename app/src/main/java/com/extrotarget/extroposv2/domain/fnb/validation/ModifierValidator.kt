package com.extrotarget.extroposv2.domain.fnb.validation

import com.extrotarget.extroposv2.domain.fnb.model.ModifierGroup
import com.extrotarget.extroposv2.domain.fnb.model.ModifierOption

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(
        val code: ValidationErrorCode,
        val required: Int,
        val selected: Int
    ) : ValidationResult()
}

enum class ValidationErrorCode {
    MIN_SELECTION_NOT_MET,
    MAX_SELECTION_EXCEEDED
}

class ModifierValidator {
    fun validate(
        group: ModifierGroup,
        selected: List<ModifierOption>
    ): ValidationResult {
        val selectedCount = selected.size
        
        return when {
            selectedCount < group.minSelect -> {
                ValidationResult.Error(
                    code = ValidationErrorCode.MIN_SELECTION_NOT_MET,
                    required = group.minSelect,
                    selected = selectedCount
                )
            }
            group.maxSelect in 1 until selectedCount -> {
                ValidationResult.Error(
                    code = ValidationErrorCode.MAX_SELECTION_EXCEEDED,
                    required = group.maxSelect,
                    selected = selectedCount
                )
            }
            else -> ValidationResult.Success
        }
    }
}
