package com.ondrejkomarek.annotationtest.database

import android.content.Context
import com.ondrejkomarek.annotation.Dao
import com.ondrejkomarek.annotation.Load
import com.ondrejkomarek.annotation.Save

@Dao
interface KotlinDao {

	@Save("kotlin_long_data") //NOTE keys I am using for sharedprefs
	fun setLongData(value: Long, context: Context)

	@Load("kotlin_long_data")
	fun getLongData(context: Context): Long

}