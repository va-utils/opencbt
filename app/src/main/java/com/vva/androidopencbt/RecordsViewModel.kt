@file:Suppress("unused")

package com.vva.androidopencbt

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vva.androidopencbt.db.CbdDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordsViewModel @Inject constructor(private val db: CbdDatabase, prefs: SharedPreferences): ViewModel() {
    private val _recordsListUpdated = MutableLiveData<Boolean>()
    val recordsListUpdated: LiveData<Boolean>
        get() = _recordsListUpdated

    private val _isAuthenticated = MutableLiveData(!prefs.getBoolean("enable_pin_protection", false))
    val isAuthenticated:LiveData<Boolean>
        get() = _isAuthenticated

    private val _askChangesConfirm = MutableLiveData<Boolean?>()
    val askDetailsFragmentConfirm: LiveData<Boolean?>
        get() = _askChangesConfirm

    private val _isSelectionActive = MutableLiveData<Boolean>()
    val isSelectionActive: LiveData<Boolean>
        get() = _isSelectionActive

    fun askDetailsFragmentConfirmation() {
        _askChangesConfirm.value = true
    }

    fun detailsFragmentRollbackChanges() {
        _askChangesConfirm.value = false
    }

    fun detailsFragmentConfirmChangesCancel() {
        _askChangesConfirm.value = null
    }

    fun authSuccessful() {
        _isAuthenticated.value = true
    }

    fun deactivateSelection() {
        _isSelectionActive.value = false
    }

    fun activateSelection() {
        if (_isSelectionActive.value != true)
            _isSelectionActive.value = true
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            db.databaseDao.deleteRecord(
                    db.databaseDao.getRecordById(id)
            )
        }
    }
}