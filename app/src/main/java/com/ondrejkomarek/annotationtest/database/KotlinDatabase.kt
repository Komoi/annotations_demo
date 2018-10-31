package com.ondrejkomarek.annotationtest.database

import com.ondrejkomarek.annotation.Database
import com.ondrejkomarek.database.BaseDatabase

@Database
abstract class KotlinDatabase : BaseDatabase() {
	abstract fun getKotlinDao(): KotlinDao
}