package com.vva.androidopencbt.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RecordDao {
    @Query(DbContract.Diary.GET_ALL_SQL)
    fun getAll(): LiveData<List<DbRecord>>

    @Query(DbContract.Diary.GET_ALL_SQL_ORDERED)
    fun getAllOrdered(order : Int) : LiveData<List<DbRecord>>

    @Query(DbContract.Diary.GET_RECORD_BY_ID)
    fun getById(id: Long): LiveData<DbRecord>

    @Query(DbContract.Diary.GET_RECORD_BY_ID)
    fun getRecordById(id: Long): DbRecord

    @Update
    fun updateRecord(record: DbRecord)

    @Delete
    fun deleteRecord(record: DbRecord)

    @Insert
    fun addRecord(dbRecord: DbRecord)
}