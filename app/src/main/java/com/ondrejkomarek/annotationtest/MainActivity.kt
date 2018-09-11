package com.ondrejkomarek.annotationtest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.ondrejkomarek.annotation.Annotation

@Annotation
class MainActivity : AppCompatActivity() {
	val mutableClass = MutableClass("Name1")

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
	}
}
