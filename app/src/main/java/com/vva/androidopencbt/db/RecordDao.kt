package com.vva.androidopencbt.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecordDao {
    @Query(DbContract.Diary.GET_ALL_SQL)
    fun getAll(): LiveData<List<DbRecord>>

    @Query(DbContract.Diary.GET_RECORD_BY_ID)
    fun getById(id: Long): LiveData<DbRecord>

    @Update
    fun updateRecord(record: DbRecord)

    @Delete
    fun deleteRecord(record: DbRecord)
}