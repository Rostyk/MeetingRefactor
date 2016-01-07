package com.exchange.ross.exchangeapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WHDatabaseHelper extends SQLiteOpenHelper
{
    private static WHDatabaseHelper instance;
    public WHDatabaseHelper(Context context) {
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    private static final String DATABASE_NAME = "WHDB";
    private static final int DATABASE_VERSION = 2;
    // Database creation sql statement
    private static final String DATABASE_CREATE1 = "CREATE TABLE IF NOT EXISTS Events (_id VARCHAR PRIMARY KEY, subject VARCHAR, location VARCHAR, required_attendees VARCHAR, optional_attendees VARCHAR, calendarName, start_date VARCHAR, end_date VARCHAR, body VARCHAR, account VARCHAR, modified VARCHAR, busy INTEGER, all_day INTEGER, mute INTEGER)";
    private static final String DATABASE_CREATE2 = "CREATE TABLE IF NOT EXISTS Accounts (url VARCHAR, user VARCHAR, password VARCHAR, domain VARCHAR, token VARCHAR)";

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE1);
        database.execSQL(DATABASE_CREATE2);
    }

    // Method is called during an upgrade of the database,
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
            int newVersion) {
        Log.w(WHDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS Events");
        database.execSQL("DROP TABLE IF EXISTS Accounts");
        onCreate(database);
    }

    public void clear() {
        getWritableDatabase().execSQL("DELETE FROM Events");
        getWritableDatabase().execSQL("DELETE FROM Accounts");
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }
}
