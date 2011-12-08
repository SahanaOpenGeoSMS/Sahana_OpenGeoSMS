/*******************************************************************************
 * Copyright 2011 Cai Fang, Ye
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.sahana.geosmser.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class WhiteListDBAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_NUMBER = "number";    
    
    private static final String DATABASE_NAME = "WhiteList";
    private static final String DATABASE_TABLE = "phone_numbers";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE =
        "create table phone_numbers (_id integer primary key autoincrement, name text not null, number text not null);";
        
    private final Context context; 
    
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public WhiteListDBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
        
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS titles");
            onCreate(db);
        }
    }    
    
    //---opens the database---
    public WhiteListDBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---    
    public void close() {
        DBHelper.close();
    }
    
    //---insert a title into the database---
    public long insertTitle(String name, String number) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_NUMBER, number);
        long l = db.insert(DATABASE_TABLE, null, initialValues);
        String s = String.valueOf(l);
        return l;
    }

    //---deletes a particular title---
    public boolean deleteTitle(long rowId) {
        return db.delete(DATABASE_TABLE, KEY_ROWID + 
                "=" + rowId, null) > 0;
    }

    //---retrieves all the titles---
    public Cursor getAllTitles() {
        return db.query(DATABASE_TABLE, new String[] {
                KEY_ROWID, 
                KEY_NAME,
                KEY_NUMBER}, 
                null, 
                null, 
                null, 
                null, 
                null);
    }

    //---retrieves a particular title---
    public Cursor getTitle(long rowId) throws SQLException {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE, new String[] {
                        KEY_ROWID,
                        KEY_NAME, 
                        KEY_NUMBER
                        }, 
                        KEY_ROWID + "=" + rowId, 
                        null,
                        null, 
                        null, 
                        null, 
                        null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    //---updates a title---
    public boolean updateTitle(long rowId, String name, String number) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        args.put(KEY_NUMBER, number);
        return db.update(DATABASE_TABLE, args, 
                         KEY_ROWID + "=" + rowId, null) > 0;
    }
}
