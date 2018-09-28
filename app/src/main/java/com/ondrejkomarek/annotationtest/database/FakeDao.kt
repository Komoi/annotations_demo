package com.ondrejkomarek.annotationtest.database

import com.ondrejkomarek.annotation.Dao
import com.ondrejkomarek.annotation.Data
import com.ondrejkomarek.annotation.KotlinFunctionAnnotation

@Dao
interface FakeDao {

	@Data
	fun getFakeData(): String

}