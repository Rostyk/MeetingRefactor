package com.exchange.ross.exchangeapp.db.dao;

public interface BaseDAO<T> {
	    T save(T type);
	    T update(T type);
	    void delete(T type);
	    Iterable <T> getAll();
}
