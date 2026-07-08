package com.extrotarget.extroposv2.ui.components.ai

import androidx.lifecycle.ViewModel
import com.extrotarget.extroposv2.core.util.audit.SilentAuditorEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    val auditorEngine: SilentAuditorEngine
) : ViewModel()
