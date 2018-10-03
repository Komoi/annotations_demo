package com.ondrejkomarek.annotationtest.database;

import com.ondrejkomarek.annotation.Database;
import com.ondrejkomarek.annotationtest.database.base.BaseDatabase;


@Database
public abstract class NewDatabase extends BaseDatabase {
 public abstract NewDao getNewDao();
}