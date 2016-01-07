package com.exchange.ross.exchangeapp.db;

import android.accounts.Account;
import android.content.Context;

import com.exchange.ross.exchangeapp.APIs.WebService;
import com.exchange.ross.exchangeapp.core.entities.Event;
import com.exchange.ross.exchangeapp.db.dao.AccountDAO;

import java.util.ArrayList;

public class AccountsProxy {
	private String sectionName;
	private static AccountsProxy instance;
	private AccountDAO accountDAO;

    public static synchronized AccountsProxy sharedProxy() {
    if (instance == null)
            instance = new AccountsProxy();
        return instance;
    }
    
    private AccountsProxy() {
    	if(accountDAO == null) {
            accountDAO = new AccountDAO();
    	}
    }

    public ArrayList<WebService> getAllAccounts(Context context) {
        return (ArrayList<WebService>) accountDAO.getAll(context);
    }

    public void addAccount(WebService account) {
        accountDAO.save(account);
    }

    public void updateAccount(WebService account) {
        accountDAO.update(account);
    }

    public void removeAccount(WebService account) {
        accountDAO.delete(account);
    }

    public Boolean isUnique(String account) {
        return accountDAO.isUnique(account);
    }

}
