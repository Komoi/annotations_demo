package com.ondrejkomarek.annotationtest.database

import com.ondrejkomarek.annotation.Dao
import com.ondrejkomarek.annotation.Load
import com.ondrejkomarek.annotation.Save

@Dao
interface FakeDao {

	@Save
	fun setFakeData(value: String)

	@Load
	fun getFakeData(): String

}