package com.zhou.biyongxposed;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME="BiyongRedPacketDB";
    private static final String TABLE_NAME="savevalue";
    private static final int VERSION=1;
    private static final String KEY_VALUE="value";
    private static final String KEY_NAME="name";
    public DatabaseHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }
    //建表语句
    private static final String CREATE_TABLE="create table "+TABLE_NAME+"("+KEY_NAME +" text not null,"+ KEY_VALUE +"integer primary key autoincrement);";
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
    public void addValue(eventvalue name){
        SQLiteDatabase db=this.getWritableDatabase();
        //使用ContentValues添加数据
        ContentValues values=new ContentValues();
        values.put(KEY_NAME,name.getName());
        values.put(KEY_VALUE,name.getValue());
        db.insert(TABLE_NAME, null, values);
        db.close();
    }
    //获取value
    public eventvalue getValue(String name){
        SQLiteDatabase db=this.getWritableDatabase();

        //Cursor对象返回查询结果
        Cursor cursor=db.query(TABLE_NAME,new String[]{KEY_NAME,KEY_VALUE},
                KEY_NAME+"=?",new String[]{name},null,null,null,null);

        eventvalue value=null;
        //注意返回结果有可能为空
        if(cursor.moveToFirst()){
            value=new eventvalue(cursor.getString(0), cursor.getInt(1));
        }
        return value;
    }
    //更新Value
    public int updateValue(eventvalue name){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(KEY_NAME,name.getName());
        values.put(KEY_VALUE,name.getValue());

        return db.update(TABLE_NAME,values,KEY_NAME+"=?",new String[]{String.valueOf(name.getValue())});
    }
    //删除Value
    public void deleteValue(eventvalue name){
        SQLiteDatabase db=this.getWritableDatabase();
        db.delete(TABLE_NAME,KEY_NAME+"=?",new String[]{String.valueOf(name.getValue())});
        db.close();
    }
}
