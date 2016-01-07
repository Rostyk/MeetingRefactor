package com.exchange.ross.exchangeapp.db.dao;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.exchange.ross.exchangeapp.APIs.ExchangeWebService;
import com.exchange.ross.exchangeapp.APIs.GoogleWebService;
import com.exchange.ross.exchangeapp.APIs.WebService;

import com.exchange.ross.exchangeapp.Utils.ApplicationContextProvider;
import com.exchange.ross.exchangeapp.db.DatabaseManager;
import com.exchange.ross.exchangeapp.db.WHDatabaseHelper;

public class AccountDAO implements BaseDAO<WebService> {
	
	private WHDatabaseHelper dbHelper;
	private SQLiteDatabase database;
	private String DATABASE_TABLE = "Accounts";

	public AccountDAO() {
	}
	
	public WebService getByID(Long ID) {
		return null;
	}
	public WebService save(WebService service) {
         initDB();
		 ContentValues cv = new ContentValues();
		 cv.put("url", service.getCredentials().getUrl());
		 cv.put("user", service.getCredentials().getUser());
		 cv.put("password", service.getCredentials().getPassword());
		 cv.put("domain",service.getCredentials().getDomain());
		 //cv.put("type",);
	     database.insert(DATABASE_TABLE, null, cv);
         closeDB();
	     return service;
	}
	
	public WebService update(WebService section) {
        initDB();
		delete(section);
		save(section);
        closeDB();
		return section;
	}
	
	public void delete(WebService service) {
        initDB();
		database.delete(DATABASE_TABLE, "user=" + "'" + service.getCredentials().getUser() + "'", null);
        closeDB();
    }
    
	public Iterable<WebService> getAll(Context context) {
        initDB();
		Cursor cursor = database.rawQuery("select * from " + DATABASE_TABLE,null);
		ArrayList<WebService> allAccounts = new ArrayList<WebService>();
		if (cursor.moveToFirst()) {
		            while (cursor.isAfterLast() == false) {
		            	
		            	//Parse url
		                String url = cursor.getString(cursor
		                        .getColumnIndex("url"));
		                
		                String user = cursor.getString(cursor
		                        .getColumnIndex("user"));

                        String domain = cursor.getString(cursor
                                .getColumnIndex("domain"));

                        String password = cursor.getString(cursor
                                .getColumnIndex("password"));

                        WebService service;
                        if(url.length() == 0) {
                            service = new GoogleWebService(url, user, password, domain, context, null);
                        }
                        else {
                            service = new ExchangeWebService(url, user, password, domain);
                        }
		                allAccounts.add(service);
                        cursor.moveToNext();
		            }
		        }
		cursor.close();
        closeDB();
		return allAccounts;
    
     }


    public Iterable<WebService> getAll() {
        initDB();
        Cursor cursor = database.rawQuery("select * from " + DATABASE_TABLE,null);
        ArrayList<WebService> allAccounts = new ArrayList<WebService>();
        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {

                //Parse url
                String url = cursor.getString(cursor
                        .getColumnIndex("url"));

                String user = cursor.getString(cursor
                        .getColumnIndex("user"));

                String domain = cursor.getString(cursor
                        .getColumnIndex("domain"));

                String password = cursor.getString(cursor
                        .getColumnIndex("password"));

                WebService service;
                if(url.length() == 0) {
                    service = new GoogleWebService(url, user, password, domain, ApplicationContextProvider.getContext(), null);
                }
                else {
                    service = new ExchangeWebService(url, user, password, domain);
                }
                allAccounts.add(service);
                cursor.moveToNext();
            }
        }
        cursor.close();
        closeDB();
        return allAccounts;

    }

    public Boolean isUnique(String account) {
        initDB();
        Cursor cursor = database.rawQuery("SELECT * FROM Accounts WHERE user = '" + account + "'", null);
        Boolean isUnique = (cursor.getCount() == 0);
        cursor.close();
        closeDB();
        return isUnique;
    }

    private void initDB() {
        database = DatabaseManager.getInstance().openDatabase();
    }
    private void closeDB() {
        //database.close();
    }
}