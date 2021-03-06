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

import com.sahana.geosmser.WhereToMeet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class QueryRepliedIDDBAdapter {
    public static final String KEY_SMSID = "repliedID";
    
    private static final String DATABASE_NAME = "QueryRepliedDB";
    private static final String DATABASE_TABLE = "QueryRepliedIDTable";
    private static final int DATABASE_VERSION = 1;
    
    private static final String DATABASE_CREATE
    = "create table QueryRepliedIDTable (_id integer primary key autoincrement, repliedID integer);";
    
    private Context baseContext;
    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDatabase;
    
    public QueryRepliedIDDBAdapter(Context context) {
        baseContext = context;
        mDBHelper = new DatabaseHelper(baseContext);
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
    
    public QueryRepliedIDDBAdapter open() throws SQLException {
        mDatabase = mDBHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDBHelper.close();
    }
    
    public long insertItem(int smsID) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_SMSID, smsID);
        long rowID = mDatabase.insert(DATABASE_TABLE, null, initialValues);
        return rowID;
    }
    
    /** Search item by SMSID */
    public boolean searchItem(int smsID) {
        boolean repeatFlag = false;
        Cursor cursor = mDatabase.query(DATABASE_TABLE, new String[] {WhiteListDBAdapter.KEY_ROWID, KEY_SMSID},
                null, null, null, null, null);
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                int restoreSMSID = cursor.getInt(1);
                if (restoreSMSID == smsID) repeatFlag = true;
            } while (cursor.moveToNext());
        }
        return repeatFlag;
    }

    public boolean deleteItem(long rowID) {
        return mDatabase.delete(DATABASE_TABLE, WhiteListDBAdapter.KEY_ROWID + 
                "=" + rowID, null) > 0;
    }

    public Cursor getAllItems() {
        return mDatabase.query(DATABASE_TABLE,
                new String[] { WhiteListDBAdapter.KEY_ROWID, KEY_SMSID}, null, null, null, null, null);
    }

    public Cursor getItem(long rowID) throws SQLException {
        Cursor mCursor =
            mDatabase.query(true, DATABASE_TABLE, new String[] {WhiteListDBAdapter.KEY_ROWID, KEY_SMSID}, 
                    KEY_SMSID + "=" + rowID, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateItem(long rowID, int smsID) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SMSID, smsID);
        return mDatabase.update(DATABASE_TABLE, contentValues, KEY_SMSID + "=" + rowID, null) > 0 ? true : false;
    }
}
