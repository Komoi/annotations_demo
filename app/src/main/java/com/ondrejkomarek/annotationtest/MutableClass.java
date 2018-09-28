package com.ondrejkomarek.annotationtest;

import com.ondrejkomarek.annotation.ImmutableAnnotation;


@ImmutableAnnotation
public class MutableClass {
	private String name;

	public MutableClass( final String name ) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
