package com.ondrejkomarek.annotationtest.database;

import com.ondrejkomarek.annotation.Dao;
import com.ondrejkomarek.annotation.Load;
import com.ondrejkomarek.annotation.Save;


@Dao
public interface NewDao {

	/*
	Enable support for other than String data types, maybe even check
	 */
	@Save
	void setNeweData(String value); //TODO change to int and test

	@Load
	String getNewData();

}