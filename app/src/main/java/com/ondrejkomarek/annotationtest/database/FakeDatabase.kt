package com.ondrejkomarek.annotationtest.database

import com.ondrejkomarek.annotation.Database
import com.ondrejkomarek.database.BaseDatabase

@Database
abstract class FakeDatabase : BaseDatabase() {
	abstract fun getFakeDao(): FakeDao
}