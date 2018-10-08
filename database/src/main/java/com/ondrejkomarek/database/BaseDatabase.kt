package com.ondrejkomarek.database


open class BaseDatabase{

	companion object {

		inline fun <reified T: BaseDatabase>getGeneratedDatabase(): T {

			//NOTE get generated database object with generated implementation which implements Abstract class
			@SuppressWarnings("unchecked")
			val aClass = Class.forName( "${T::class.qualifiedName}Implementation" )as Class<T>
			return aClass.newInstance()//error if not annotated class is passed


			/*NOTE
			Masterplan: Read @Database annotation and @Dao one. For every abstract getDao function in @Database
			annotated abstract class there need to be some @Dao annotated abstract object.
			 If this is correct, generate database with implemented getDao*** method, which will get
			 appropriate generated Dao class, which will has abstract get and set methods, which has generated code using preferences for saving and getting data.
			 */
		}
	}


}