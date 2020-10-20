package com.vva.androidopencbt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class DateBaseAdapter
{
    DateBaseHelper dbHelper;
    SQLiteDatabase db;

    public DateBaseAdapter(Context context)
    {
        dbHelper = new DateBaseHelper(context.getApplicationContext());
    }

    public void open()
    {
        db = dbHelper.getWritableDatabase();
        //return this;
    }

    public void close()
    {
        dbHelper.close();
    }

    private Cursor getAllEntries(int orderBy ,boolean showOld)
    {
        String[] clms = new String[10];
        clms[0] = DateBaseHelper.COLUMN_ID;
        clms[1] = DateBaseHelper.COLUMN_SITUATION;
        clms[2] = DateBaseHelper.COLUMN_THOUGHTS;
        clms[3] = DateBaseHelper.COLUMN_RATIONAL;
        clms[4] = DateBaseHelper.COLUMN_EMOTIONS;
        clms[5] = DateBaseHelper.COLUMN_FEELINGS;
        clms[6] = DateBaseHelper.COLUMN_ACTIONS;
        clms[7] = DateBaseHelper.COLUMN_INTENSITY;
        clms[8] = DateBaseHelper.COLUMN_DISTORTIONS;
        clms[9] = DateBaseHelper.COLUMN_DATETIME;
        return db.query(DateBaseHelper.TABLE,clms,null,null,null,null,null);
    }

    public List<Record> getRecords(int orderBy, boolean showOlds)
    {
        ArrayList<Record> records = new ArrayList<>();
        Cursor cursor = getAllEntries(orderBy, showOlds);
        if(cursor.moveToFirst()) {
            do
            {
                int id = cursor.getInt(cursor.getColumnIndex(DateBaseHelper.COLUMN_ID));
                String situation = cursor.getString(cursor.getColumnIndex(DateBaseHelper.COLUMN_SITUATION));
                String thoughts = cursor.getString(cursor.getColumnIndex(DateBaseHelper.COLUMN_THOUGHTS));
                String rational = cursor.getString(cursor.getColumnIndex(DateBaseHelper.COLUMN_RATIONAL));
                String emotion = cursor.getString(cursor.getColumnIndex(DateBaseHelper.COLUMN_EMOTIONS));
                String feelings = cursor.getString(cursor.getColumnIndex(DateBaseHelper.COLUMN_FEELINGS));
                String actions = cursor.getString(cursor.getColumnIndex(DateBaseHelper.COLUMN_ACTIONS));
                short intensity = cursor.getShort(cursor.getColumnIndex(DateBaseHelper.COLUMN_INTENSITY));
                short distortions = cursor.getShort(cursor.getColumnIndex(DateBaseHelper.COLUMN_DISTORTIONS)); //тут
                long date = cursor.getLong(cursor.getColumnIndex(DateBaseHelper.COLUMN_DATETIME));
                records.add(new Record(id, situation, thoughts, rational, emotion, feelings, actions, intensity, distortions,date));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return records;
    }

    public long count()
    {
        return DatabaseUtils.queryNumEntries(db, dbHelper.TABLE);
    }

    public Record getEvent(long id)
    {
        Record record = null;
        String query = String.format("SELECT * FROM %s WHERE %s=?", DateBaseHelper.TABLE, DateBaseHelper.COLUMN_ID);
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        if(cursor.moveToFirst())
        {
            String situation = cursor.getString(cursor.getColumnIndex(DateBaseHelper.COLUMN_SITUATION));
            String thoughts = cursor.getString(cursor.getColumnIndex(DateBaseHelper.COLUMN_THOUGHTS));
            String rational = cursor.getString(cursor.getColumnIndex(DateBaseHelper.COLUMN_RATIONAL));
            String emotion = cursor.getString(cursor.getColumnIndex(DateBaseHelper.COLUMN_EMOTIONS));
            String feelings = cursor.getString(cursor.getColumnIndex(DateBaseHelper.COLUMN_FEELINGS));
            String actions = cursor.getString(cursor.getColumnIndex(DateBaseHelper.COLUMN_ACTIONS));
            short intensity = (short)cursor.getInt(cursor.getColumnIndex(DateBaseHelper.COLUMN_INTENSITY));
            short distortions = (short)cursor.getInt(cursor.getColumnIndex(DateBaseHelper.COLUMN_DISTORTIONS));
            long date = cursor.getLong(cursor.getColumnIndex(DateBaseHelper.COLUMN_DATETIME));
            record = new Record(id, situation, thoughts, rational, emotion, feelings, actions, intensity, distortions,date);
        }
        cursor.close();
        return record;
    }

    public long insert(Record record)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DateBaseHelper.COLUMN_SITUATION,record.getSituation());
        contentValues.put(DateBaseHelper.COLUMN_THOUGHTS,record.getThought());
        contentValues.put(DateBaseHelper.COLUMN_RATIONAL,record.getRational());
        contentValues.put(DateBaseHelper.COLUMN_EMOTIONS,record.getEmotion());
        contentValues.put(DateBaseHelper.COLUMN_FEELINGS,record.getFeelings());
        contentValues.put(DateBaseHelper.COLUMN_ACTIONS,record.getActions());
        contentValues.put(DateBaseHelper.COLUMN_INTENSITY,record.getIntensity());
        contentValues.put(DateBaseHelper.COLUMN_DISTORTIONS,record.getDistortionsValue());
        contentValues.put(DateBaseHelper.COLUMN_DATETIME,record.getDateTime().getTime());
        return db.insert(DateBaseHelper.TABLE,null,contentValues);
    }

    public long delete(long id)
    {
        String whereClause = "_id = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        return db.delete(DateBaseHelper.TABLE, whereClause, whereArgs);
    }

    public long update(Record record)
    {
        String whereClause = DateBaseHelper.COLUMN_ID+"="+record.getId();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DateBaseHelper.COLUMN_SITUATION, record.getSituation());
        contentValues.put(DateBaseHelper.COLUMN_THOUGHTS,record.getThought());
        contentValues.put(DateBaseHelper.COLUMN_RATIONAL,record.getRational());
        contentValues.put(DateBaseHelper.COLUMN_EMOTIONS,record.getEmotion());
        contentValues.put(DateBaseHelper.COLUMN_FEELINGS,record.getFeelings());
        contentValues.put(DateBaseHelper.COLUMN_ACTIONS,record.getActions());
        contentValues.put(DateBaseHelper.COLUMN_INTENSITY,record.getIntensity());
        contentValues.put(DateBaseHelper.COLUMN_DISTORTIONS,record.getDistortionsValue());
        contentValues.put(DateBaseHelper.COLUMN_DATETIME,record.getDateTime().getTime());
        return db.update(DateBaseHelper.TABLE,contentValues,whereClause,null);
    }


}
