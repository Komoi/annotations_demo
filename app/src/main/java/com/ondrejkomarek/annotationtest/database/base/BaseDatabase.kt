package com.ondrejkomarek.annotationtest.database.base

import kotlin.reflect.KClass

open class BaseDatabase{
	//private lateinit var databaseClass: Class<*>

	companion object {
		// maybe think about moving this to new package so it is more like real thing
		fun <T: BaseDatabase>getGeneratedDatabase(klass : KClass<*>): T {
			//TODO calling expected generated classes here
			//get generated database object with generated implementation which implements Abstract class
			@SuppressWarnings("unchecked")
			val aClass = Class.forName( "${klass.qualifiedName}Implementation" )as Class<T>
			return aClass.newInstance()//handle error if not annotated class is passed


			/*
			Masterplan: Read @Database annotation and @Dao one. FOr every abstract function in @Database
			annotated abstract class there need to be some @Dao annotated abstract object.
			 If this is correct, generate database with implemented getDao*** method, which will get
			 appropriate generated Dao class, which will provide our mocked result. Maybe use 2 Daos.
			 */
		}
	}


}