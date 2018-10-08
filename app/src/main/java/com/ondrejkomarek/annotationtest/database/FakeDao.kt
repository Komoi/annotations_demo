package com.ondrejkomarek.annotationtest.database

import android.content.Context
import com.ondrejkomarek.annotation.Dao
import com.ondrejkomarek.annotation.Load
import com.ondrejkomarek.annotation.Save

@Dao
interface FakeDao {

	@Save("fake_data")
	fun setFakeData(value: Long, context: Context)

	@Load("fake_data")
	fun getFakeData(context: Context): Long

}