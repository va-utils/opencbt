@file:Suppress("unused")

package com.vva.androidopencbt

import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.vva.androidopencbt.db.CbdDatabase
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class RecordsViewModel(application: Application): AndroidViewModel(application) {
    private val db = CbdDatabase.getInstance(application)
    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)
    var recyclerViewState: Parcelable? = null

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

    fun listUpdated() {
        _recordsListUpdated.value = true
        viewModelScope.launch {
            delay(TimeUnit.SECONDS.toMillis(1))
            _recordsListUpdated.value = false
        }
    }

    fun restoreRecyclerView(rv: RecyclerView) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                delay(10)
            }
            rv.layoutManager?.onRestoreInstanceState(recyclerViewState)
        }
    }
}