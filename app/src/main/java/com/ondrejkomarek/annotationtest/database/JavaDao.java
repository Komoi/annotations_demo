package com.ondrejkomarek.annotationtest.database;

import android.content.Context;

import com.ondrejkomarek.annotation.Dao;
import com.ondrejkomarek.annotation.Load;
import com.ondrejkomarek.annotation.Save;


@Dao
public interface JavaDao
{
	@Save(preferenceKey = "java_string_data")
	void setStringData(String value, Context context);

	@Load(preferenceKey = "java_string_data")
	String getStringData(Context context);

}