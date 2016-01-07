// ISync.aidl
package com.exchange.ross.exchangeapp;

import com.exchange.ross.exchangeapp.IUpdateUIStart;
// Declare any non-default types here with import statements

interface ISync {

    //force sync
    void sync(String accountName);

    //UI update callback
    void attachUIUpdate(IUpdateUIStart uiUpdater);
}
