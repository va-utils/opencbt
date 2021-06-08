package com.vva.androidopencbt

import android.util.Log
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vva.androidopencbt.db.*
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordsDbMigrationTest {
    private lateinit var database: SupportSQLiteDatabase

    @JvmField
    @Rule
    val migrationTestHelper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            CbdDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrateFrom1To2() {
        database = migrationTestHelper.createDatabase("test_db", 1).apply {
            with(DbContract.Diary) {
                execSQL("INSERT INTO $TABLE_NAME($COLUMN_ID, $COLUMN_SITUATION, $COLUMN_DATETIME) VALUES(1, 'sit1', 1604339210420)")
//                execSQL("INSERT INTO $TABLE_NAME($COLUMN_ID, $COLUMN_SITUATION, $COLUMN_DATETIME) VALUES(1, 'sit2', 1604338884052)")
//                execSQL("INSERT INTO $TABLE_NAME($COLUMN_ID, $COLUMN_SITUATION, $COLUMN_DATETIME) VALUES(1, 'sit3', 1604330394053)")
//                execSQL("INSERT INTO $TABLE_NAME($COLUMN_ID, $COLUMN_SITUATION, $COLUMN_DATETIME) VALUES(1, 'sit4', 1604339210420)")
//                execSQL("INSERT INTO $TABLE_NAME($COLUMN_ID, $COLUMN_SITUATION, $COLUMN_DATETIME) VALUES(1, 'sit5', 1604339210420)")
            }
            close()
        }

        database = migrationTestHelper.runMigrationsAndValidate("test_db", 2, true, CbdDatabase.MIGRATION_1_2)

        val query = database.query("SELECT * FROM ${DbContract.Diary.TABLE_NAME}")
        assertTrue(query.moveToFirst())
        Log.d("test", query.count.toString())
        assertEquals("sit1", query.getString(query.getColumnIndex(DbContract.Diary.COLUMN_SITUATION)))
        assertEquals("", query.getString(query.getColumnIndex(DbContract.Diary.COLUMN_THOUGHTS)))
        assertEquals(1604339210420, query.getLong(query.getColumnIndex(DbContract.Diary.COLUMN_DATETIME)))
    }

    @Test
    fun migrateFrom2To3() {
        database = migrationTestHelper.createDatabase("test_db", 2).apply {
            with(DbContract.Diary) {
                execSQL("INSERT INTO $TABLE_NAME($COLUMN_ID, $COLUMN_SITUATION, $COLUMN_THOUGHTS, " +
                        "$COLUMN_RATIONAL, $COLUMN_EMOTIONS, $COLUMN_DISTORTIONS, $COLUMN_FEELINGS, $COLUMN_ACTIONS, " +
                        "$COLUMN_INTENSITY, $COLUMN_DATETIME) " +
                        "VALUES(1, 'sit1', 'th1', 'rat1', 'emo1', 'dis1', 'fee1', 'act1', 'int1', 1604339210420)")
            }
            close()
        }

        database = migrationTestHelper.runMigrationsAndValidate("test_db", 3, false, CbdDatabase.MIGRATION_2_3)

        val query = database.query("SELECT * FROM ${DbContract.Diary.TABLE_NAME}")
        assertTrue(query.moveToFirst())
        Log.d("test", query.count.toString())
        assertEquals("sit1", query.getString(query.getColumnIndex(DbContract.Diary.COLUMN_SITUATION)))
        assertEquals("th1", query.getString(query.getColumnIndex(DbContract.Diary.COLUMN_THOUGHTS)))
        assertEquals(1604339210420, query.getLong(query.getColumnIndex(DbContract.Diary.COLUMN_DATETIME)))
        assertEquals(1604339210420, query.getLong(query.getColumnIndex(DbContract.Diary.COLUMN_USER_DATETIME)))
    }
}