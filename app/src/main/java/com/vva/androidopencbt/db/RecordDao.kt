package com.vva.androidopencbt.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.joda.time.DateTime

@Dao
@TypeConverters(Converters::class)
interface RecordDao {
    @Query(DbContract.Diary.GET_ALL_SQL)
    fun getAll(): LiveData<List<DbRecord>>

    @Query(DbContract.Diary.GET_ALL_SQL)
    fun getAllList(): List<DbRecord>

    @Query(DbContract.Diary.GET_ALL_SQL_ORDERED)
    fun getAllOrdered(order : Int) : LiveData<List<DbRecord>>

    @Query(DbContract.Diary.GET_RECORD_BY_ID)
    fun getById(id: Long): LiveData<DbRecord>

    @Query(DbContract.Diary.GET_RECORD_BY_ID)
    fun getRecordById(id: Long): DbRecord

    @Query(DbContract.Diary.GET_RECORD_BY_ID)
    fun getRecordLiveDataById(id: Long): LiveData<DbRecord>

    @Query(DbContract.Diary.GET_AVERAGE_INTENSITY)
    fun getAverageIntensity() : LiveData<Double>

    @Query(DbContract.Diary.GET_ALL_COUNT)
    fun getAllCount() : LiveData<Int>

    @Query(DbContract.Diary.GET_MIN_DATE)
    fun getOldestDate() : LiveData<DateTime>

    @Query(DbContract.Diary.GET_MAX_DATE)
    fun getLatestDate() : LiveData<DateTime>

    @Query(DbContract.Diary.GET_DISTORTION_COL)
    fun getDistList() : List<Int>

    @Query(DbContract.Diary.GET_DATETIME_COL)
    fun getDateTimeList() : List<Long>

    @Query(DbContract.Diary.GET_RECORDS_FOR_PERIOD)
    fun getRecordsForPeriod (t1 : DateTime, t2 : DateTime) : List<DbRecord>

    @Query(DbContract.Diary.DELETE_BY_ID)
    fun deleteById(id: Long)

    @Update
    fun updateRecord(record: DbRecord)

    @Delete
    fun deleteRecord(record: DbRecord)

    @Insert
    fun addRecord(dbRecord: DbRecord): Long
}