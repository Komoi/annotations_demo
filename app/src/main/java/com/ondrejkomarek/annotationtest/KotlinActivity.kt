package com.ondrejkomarek.annotationtest

import android.content.Context
import android.databinding.DataBindingUtil.setContentView
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import com.ondrejkomarek.annotation.*
import com.ondrejkomarek.annotationtest.database.FakeDatabase
import com.ondrejkomarek.annotationtest.database.NewDatabase
import com.ondrejkomarek.database.BaseDatabase
import org.alfonz.arch.AlfonzActivity

@KotlinClassAnnotation
//@JavaAnnotation
class KotlinActivity @KotlinConstructorAnnotation constructor() : AlfonzActivity() {

	@KotlinFieldAnnotation
	val mutableClass: @KotlinTypeAnnotation MutableClass =  MutableClass("Kotlin")

	val mutableClass2: @KotlinTypeAnnotation MutableClass =  MutableClass("Kotlin2")

	@KotlinFunctionAnnotation
	override fun onCreate(@KotlinValueParameterAnnotation savedInstanceState: Bundle?) {
		var localVar = ""
		Log.d("", localVar)
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_common)
		replaceFragment(KotlinFragment())


		Toast.makeText(baseContext, "Hello ${Generated_KotlinActivity().getName()}", Toast.LENGTH_LONG).show()

		BaseDatabase.getGeneratedDatabase<FakeDatabase>().getFakeDao().setFakeData(42L, baseContext)
		BaseDatabase.getGeneratedDatabase<NewDatabase>().getNewDao().setNewData("New data", baseContext)

		Toast.makeText(baseContext, "Fake data: ${(BaseDatabase.getGeneratedDatabase<FakeDatabase>()).getFakeDao().getFakeData(baseContext)}", Toast.LENGTH_LONG).show()
		Toast.makeText(baseContext, "New data: ${(BaseDatabase.getGeneratedDatabase<NewDatabase>()).getNewDao().getNewData(baseContext)}", Toast.LENGTH_LONG).show()



	}
}
