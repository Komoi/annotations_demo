package com.ondrejkomarek.annotationtest.database

import com.ondrejkomarek.annotation.Dao
import com.ondrejkomarek.annotation.Load
import com.ondrejkomarek.annotation.Save

@Dao
interface FakeDao {

	//here we can have two annotations: save and load, we can check in annotation processor that e.g. save does not have return type. Annotaion can have string parameter which will be used as a key for shared preferences
	@Save
	fun setFakeData(value: String)

	@Load
	fun getFakeData(): String

}