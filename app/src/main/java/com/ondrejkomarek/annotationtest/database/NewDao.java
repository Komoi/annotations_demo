package com.ondrejkomarek.annotationtest.database;

import android.content.Context;

import com.ondrejkomarek.annotation.Dao;
import com.ondrejkomarek.annotation.Load;
import com.ondrejkomarek.annotation.Save;


@Dao
public interface NewDao {

	/*
	Enable support for other than String data types, maybe even check
	 */
	@Save(preferenceKey = "new_data")
	void setNewData(String value, Context context); //TODO change to int and test

	@Load(preferenceKey = "new_data")
	String getNewData(Context context);

}