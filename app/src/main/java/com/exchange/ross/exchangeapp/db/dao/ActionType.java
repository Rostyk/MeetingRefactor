package com.exchange.ross.exchangeapp.db.dao;

public enum ActionType {
    MUTE_SOUND(0), SAVE_RAPORT(1);

    final int actionType;

    private ActionType(int actionType){
        this.actionType = actionType;
    }

    public int getValue(){
        return this.actionType;
    }

};
