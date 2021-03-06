package com.zhou.biyongxposed;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BiyongRedPacketDB";
    private static final String TABLE_NAME = "biyongvalue";
    private static final int VERSION = 1;
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_VALUE = "value";
    private static final String KEY_STR = "coincount";

    DatabaseHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    //建表语句
    private static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + KEY_ID + " integer PRIMARY KEY autoincrement," + KEY_NAME +
            " not null," + KEY_VALUE + " integer," + KEY_STR + " not null);";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    //添加value
    void addValue(Eventvalue name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, name.getId());
        values.put(KEY_NAME, name.getName());
        values.put(KEY_VALUE, name.getValue());
        values.put(KEY_STR, name.getCoincount());
        db.replace(TABLE_NAME, null, values);
        db.close();
    }

    //按name获取value
    Eventvalue getNameResult(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{KEY_ID, KEY_NAME, KEY_VALUE, KEY_STR},
                KEY_NAME + "=?", new String[]{name}, null, null, null, null);

        Eventvalue value = null;
        //注意返回结果有可能为空
        if (cursor.moveToFirst()) {
            value = new Eventvalue(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3));
        }
        cursor.close();
        return value;
    }

    //按coincount获取value
    Eventvalue getStr(String coincount) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{KEY_ID, KEY_NAME, KEY_VALUE, KEY_STR},
                KEY_STR + "=?", new String[]{coincount}, null, null, null, null);

        Eventvalue value = null;
        //注意返回结果有可能为空
        if (cursor.moveToFirst()) {
            value = new Eventvalue(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3));
        }
        cursor.close();
        return value;
    }

    //按id获取value
    Eventvalue getIdResult(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{KEY_ID, KEY_NAME, KEY_VALUE, KEY_STR},
                KEY_ID + "=?", new String[]{id}, null, null, null, null);

        Eventvalue value = null;
        //注意返回结果有可能为空
        if (cursor.moveToFirst()) {
            value = new Eventvalue(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3));
        }
        cursor.close();
        return value;
    }

    //获取元素数量
    int getelementCounts() {
        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.close();
        return cursor.getCount();
    }

    //更新Value
    public int updateValue(Eventvalue name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, name.getId());
        values.put(KEY_NAME, name.getName());
        values.put(KEY_VALUE, name.getValue());
        values.put(KEY_STR, name.getCoincount());
        db.close();
        return db.update(TABLE_NAME, values, KEY_NAME + "=?", new String[]{String.valueOf(name.getName())});
    }

    //按NAME删除Value
    void deleteValue(Eventvalue name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_NAME + "=?", new String[]{String.valueOf(name.getName())});
        db.close();
    }

    //按coincount删除Value
    void deleteCoincount(Eventvalue coincount) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_STR + "=?", new String[]{String.valueOf(coincount.getCoincount())});
        db.close();
    }

    //删除数据库并把自增长设为0
    void deleteDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("update sqlite_sequence set seq=0 where name = 'biyongvalue'");
        db.execSQL("DELETE FROM biyongvalue");
        db.close();
    }

    //重排数据库
    List<Eventvalue> dbquery() {
        List<Eventvalue> ListData = new ArrayList<Eventvalue>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        Eventvalue elemnet = new Eventvalue();
                        elemnet.setId(Integer.parseInt(cursor.getString(0)));
                        elemnet.setName(cursor.getString(1));
                        elemnet.setValue(Integer.parseInt(cursor.getString(2)));
                        elemnet.setCoincount(cursor.getString(3));
                        ListData.add(elemnet);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            return ListData;
        }
    }

