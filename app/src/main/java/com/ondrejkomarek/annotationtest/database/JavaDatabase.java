package com.ondrejkomarek.annotationtest.database;

import com.ondrejkomarek.annotation.Database;
import com.ondrejkomarek.database.BaseDatabase;


@Database
public abstract class JavaDatabase extends BaseDatabase
{
 public abstract JavaDao getJavaDao();
}