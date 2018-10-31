package com.ondrejkomarek.annotationtest;

import android.os.Bundle;

import com.ondrejkomarek.annotation.JavaAnnotation;
import com.ondrejkomarek.annotation.KotlinClassAnnotation;

import org.alfonz.arch.AlfonzActivity;

//@KotlinClassAnnotation
@JavaAnnotation
public class JavaActivity extends AlfonzActivity
{
	 final MutableClass mutableClass = new MutableClass("Java");

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_common);
		replaceFragment(new JavaFragment());
	}
}

