package com.ondrejkomarek.annotationprocessor

import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedAnnotationTypes("*") //com.ondrejkomarek.annotation.Annotation
@SupportedSourceVersion(SourceVersion.RELEASE_7)
class KotlinAnnotationProcessor : AbstractProcessor() {

	@Synchronized
	override fun init(processingEnv: ProcessingEnvironment) {
		super.init(processingEnv) //TODO modify
	}


	/*	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return new HashSet<>();
	}

*/

	/*	@Override
	public SourceVersion getSupportedSourceVersion() {
		return new ImmutableSet.of(Annotation.class.getCanonicalName());
	}
*/

	override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
		processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "KotlinAnnotationProcessor init")
		return true
	}
}