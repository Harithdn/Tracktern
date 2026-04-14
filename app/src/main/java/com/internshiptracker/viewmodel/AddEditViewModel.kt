package com.internshiptracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.internshiptracker.data.repository.ApplicationRepository
import com.internshiptracker.domain.model.ApplicationStatus
import com.internshiptracker.domain.model.InternshipApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel for the Add / Edit application form.
 * Holds all form field state so configuration changes don't lose input.
 */
@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val repository: ApplicationRepository
) : ViewModel() {

    // ── Form fields ───────────────────────────────────────────────────────
    val companyName  = MutableStateFlow("")
    val role         = MutableStateFlow("")
    val status       = MutableStateFlow(ApplicationStatus.APPLIED)
    val dateApplied  = MutableStateFlow(LocalDate.now())
    val notes        = MutableStateFlow("")
    val salaryRange  = MutableStateFlow("")
    val location     = MutableStateFlow("")
    val applicationUrl = MutableStateFlow("")
    val contactName  = MutableStateFlow("")
    val contactEmail = MutableStateFlow("")

    private val _saveResult = MutableStateFlow<SaveResult?>(null)
    val saveResult: StateFlow<SaveResult?> = _saveResult.asStateFlow()

    private var editingId: Long? = null

    /** Load an existing application into the form fields for editing. */
    fun loadApplication(id: Long) {
        viewModelScope.launch {
            repository.getById(id)?.let { app ->
                editingId = id
                companyName.value    = app.companyName
                role.value           = app.role
                status.value         = app.status
                dateApplied.value    = app.dateApplied
                notes.value          = app.notes
                salaryRange.value    = app.salaryRange
                location.value       = app.location
                applicationUrl.value = app.applicationUrl
                contactName.value    = app.contactName
                contactEmail.value   = app.contactEmail
            }
        }
    }

    /** Reset all form fields to defaults (for fresh add). */
    fun resetForm() {
        editingId        = null
        companyName.value    = ""
        role.value           = ""
        status.value         = ApplicationStatus.APPLIED
        dateApplied.value    = LocalDate.now()
        notes.value          = ""
        salaryRange.value    = ""
        location.value       = ""
        applicationUrl.value = ""
        contactName.value    = ""
        contactEmail.value   = ""
        _saveResult.value    = null
    }

    /** Validate inputs and save to Room database. */
    fun saveApplication() {
        if (companyName.value.isBlank()) {
            _saveResult.value = SaveResult.Error("Company name is required")
            return
        }
        if (role.value.isBlank()) {
            _saveResult.value = SaveResult.Error("Role / position is required")
            return
        }

        viewModelScope.launch {
            val application = InternshipApplication(
                id             = editingId ?: 0L,
                companyName    = companyName.value.trim(),
                role           = role.value.trim(),
                status         = status.value,
                dateApplied    = dateApplied.value,
                notes          = notes.value.trim(),
                salaryRange    = salaryRange.value.trim(),
                location       = location.value.trim(),
                applicationUrl = applicationUrl.value.trim(),
                contactName    = contactName.value.trim(),
                contactEmail   = contactEmail.value.trim(),
                updatedAt      = LocalDateTime.now()
            )

            val savedId = if (editingId != null) {
                repository.updateApplication(application)
                editingId!!
            } else {
                repository.addApplication(application)
            }

            _saveResult.value = SaveResult.Success(savedId)
        }
    }

    /** Delete the currently-editing application from the database. */
    fun delete() {
        val id = editingId ?: return
        viewModelScope.launch {
            repository.deleteById(id)
            _saveResult.value = SaveResult.Deleted
        }
    }
}

sealed class SaveResult {
    data class Success(val id: Long) : SaveResult()
    data class Error(val message: String) : SaveResult()
    object Deleted : SaveResult()
}
