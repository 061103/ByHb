package com.zhou.biyongxposed;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class DatabaseHandler extends SQLiteOpenHelper {
        private static final String DATABASE_NAME="BiyongRedPacketDB";
        private static final String TABLE_NAME="biyongvalue";
        private static final int VERSION=1;
        private static final String KEY_ID="id";
        private static final String KEY_NAME="name";
        private static final String KEY_VALUE="value";
        private static final String KEY_STR="coincount";
        public DatabaseHandler(@Nullable Context context) {
            super(context, DATABASE_NAME, null, VERSION);
        }
        //建表语句
        private static final String CREATE_TABLE="create table "+TABLE_NAME+"("+KEY_ID+ " integer primary key autoincrement,"+ KEY_NAME +
                " not null,"+ KEY_VALUE +" integer primary key autoincrement,"+ KEY_STR + " not null);";
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
        public void addValue(Eventvalue name){
            SQLiteDatabase db=this.getWritableDatabase();
            ContentValues values=new ContentValues();
            values.put(KEY_ID,name.getType());
            values.put(KEY_NAME,name.getName());
            values.put(KEY_VALUE,name.getValue());
            values.put(KEY_STR,name.getCoincount());
            db.replace(TABLE_NAME, null, values);
            db.close();
        }
        //获取value
        public Eventvalue getValueResult(String name){
            SQLiteDatabase db=this.getWritableDatabase();
            Cursor cursor=db.query(TABLE_NAME,new String[]{KEY_ID,KEY_NAME,KEY_VALUE,KEY_STR},
                    KEY_NAME+"=?",new String[]{name},null,null,null,null);

            Eventvalue value=null;
            //注意返回结果有可能为空
            if(cursor.moveToFirst()){
                value=new Eventvalue(cursor.getInt(0),cursor.getString(1), cursor.getInt(2),cursor.getString(3));
            }
            return value;
        }
        //更新Value
        public int updateValue(Eventvalue name){
            SQLiteDatabase db=this.getWritableDatabase();
            ContentValues values=new ContentValues();
            values.put(KEY_ID,name.getType());
            values.put(KEY_NAME,name.getName());
            values.put(KEY_VALUE,name.getValue());
            values.put(KEY_STR,name.getCoincount());
            return db.update(TABLE_NAME,values,KEY_NAME+"=?",new String[]{String.valueOf(name.getValue())});
        }
        //删除Value
        public void deleteValue(Eventvalue name){
            SQLiteDatabase db=this.getWritableDatabase();
            db.delete(TABLE_NAME,KEY_NAME+"=?",new String[]{String.valueOf(name.getValue())});
            db.close();
        }
    }

