package com.ondrejkomarek.annotationtest

import android.databinding.DataBindingUtil.setContentView
import android.os.Bundle
import android.util.Log
import com.ondrejkomarek.annotation.*
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
	}
}
