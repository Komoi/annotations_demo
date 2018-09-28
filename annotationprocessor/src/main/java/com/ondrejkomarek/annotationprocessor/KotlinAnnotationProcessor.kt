package com.ondrejkomarek.annotationprocessor

import com.ondrejkomarek.annotation.*
import com.squareup.kotlinpoet.*
import java.io.File

import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic
import kotlin.reflect.KClass


@SupportedAnnotationTypes("*") //com.ondrejkomarek.annotation.JavaAnnotation
@SupportedSourceVersion(SourceVersion.RELEASE_7)
class KotlinAnnotationProcessor : AbstractProcessor() {

	//private lateinit var trees: Trees
	private var generated: Boolean = false

	@Synchronized
	override fun init(processingEnv: ProcessingEnvironment) {
		super.init(processingEnv)
		//trees = Trees.instance( processingEnv );

	}

	companion object {
		const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
	}


	override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {

		processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Kotlin annotation process started, supported: $supportedAnnotationTypes")

		testImmutability(Diagnostic.Kind.WARNING, roundEnvironment)
		//addImmutability(Diagnostic.Kind.WARNING, roundEnvironment)
		testImmutability(Diagnostic.Kind.WARNING, roundEnvironment)

		getInfo(Diagnostic.Kind.NOTE, roundEnvironment, KotlinClassAnnotation::class)
		getInfo(Diagnostic.Kind.NOTE, roundEnvironment, KotlinAnnotationClassAnnotation::class)
		getInfo(Diagnostic.Kind.NOTE, roundEnvironment, KotlinTypeAnnotation::class)
		getInfo(Diagnostic.Kind.NOTE, roundEnvironment, KotlinFieldAnnotation::class)
		getInfo(Diagnostic.Kind.NOTE, roundEnvironment, KotlinFunctionAnnotation::class)
		getInfo(Diagnostic.Kind.NOTE, roundEnvironment, KotlinValueParameterAnnotation::class)
		getInfo(Diagnostic.Kind.NOTE, roundEnvironment, KotlinConstructorAnnotation::class)

		//just temp to get pack
		var tempPack = ""

		roundEnvironment.getElementsAnnotatedWith(KotlinClassAnnotation::class.java)
				.forEach {
					val className = it.simpleName.toString()
					val pack = processingEnv.elementUtils.getPackageOf(it).toString()
					tempPack = pack
					generateClass(className, pack)
				}


		if(!generated){
			generateTestFakeDaoClass("FakeDao", tempPack)
			generateTestFakeDatabseClass("FakeDatabase", tempPack)
			generated = true
		}


		// Claiming that annotations have been processed by this processor
		return true
	}



	private fun generateClass(className: String, pack: String) {
		val fileName = "Generated_$className"
		val file = FileSpec.builder(pack, fileName)
				.addType(TypeSpec.classBuilder(fileName)
						.addFunction(FunSpec.builder("getName")
								.addStatement("return \"World\"")
								.build())
						.build())
				.build()

		val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
		file.writeTo(File(kaptKotlinGeneratedDir, "$fileName.kt"))
	}


	private fun generateTestFakeDaoClass(className: String, pack: String) {
		val baseFakeDao = ClassName("${pack}.database", "FakeDao") //replace with more robust solution, move possibly to new package

		val fileName = "${className}Implementation"
		val file = FileSpec.builder(pack, fileName)
				.addType(TypeSpec.classBuilder(fileName)
						.addSuperinterface(baseFakeDao)
						//.superclass(baseDao)
						.addFunction(FunSpec.builder("getFakeData")
								.returns(String::class)
								.addStatement("return \"Fake data added by processor\"")
								.addModifiers(KModifier.OVERRIDE)
								.build())
						.build())
				.build()

		val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
		file.writeTo(File(kaptKotlinGeneratedDir, "$fileName.kt"))
	}

	private fun generateTestFakeDatabseClass(className: String, pack: String) {
		val fakeDao = ClassName(pack, "FakeDaoImplementation")
		val baseFakeDatabase = ClassName("${pack}.database", "FakeDatabase") //replace with more robust solution, move possibly to new package


		val fileName = "${className}Implementation"
		val file = FileSpec.builder(pack, fileName)
				.addType(TypeSpec.classBuilder(fileName)
						.superclass(baseFakeDatabase)
						.addFunction(FunSpec.builder("getFakeDao")
								.addModifiers(KModifier.OVERRIDE)
								.returns(fakeDao)
								.addStatement("return %T()", fakeDao)
								.build())
						.build())
				.build()

		val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
		file.writeTo(File(kaptKotlinGeneratedDir, "$fileName.kt"))
	}


	fun getInfo(logType: javax.tools.Diagnostic.Kind, roundEnvironment: RoundEnvironment, annotationClass: KClass<out kotlin.Annotation>) {

		for(element in roundEnvironment.getElementsAnnotatedWith(annotationClass.java)) {
			processingEnv.messager.printMessage(logType, "\n\nElement: ${element.simpleName}, Annotation: ${annotationClass.simpleName}")

			for(enclosedElement in element.getEnclosedElements())
			{
				processingEnv.messager.printMessage(logType, "Kind: ${enclosedElement.kind}")
				processingEnv.messager.printMessage(logType, "Name: ${enclosedElement.simpleName}")
			}
		}
	}

	fun testImmutability(logType: javax.tools.Diagnostic.Kind, roundEnvironment: RoundEnvironment) {
		for(element in roundEnvironment.getElementsAnnotatedWith(KotlinClassAnnotation::class.java)) {
			if(element is TypeElement) {

				for(eclosedElement in element.getEnclosedElements()) {
					if(eclosedElement is VariableElement) {
						val variableElement = eclosedElement

						if(!variableElement.modifiers.contains(Modifier.FINAL)) {
							processingEnv.messager.printMessage(logType,
									String.format("Class '%s' is annotated as @Immutable, but field '%s' is not declared as final",
											element.simpleName, variableElement.simpleName
									)
							)
						}
					}
				}
			}
		}
	}

	/*fun addImmutability(logType: javax.tools.Diagnostic.Kind, roundEnvironment: RoundEnvironment){


		val  scanner = object: TreePathScanner<Any, CompilationUnitTree>() {

			override fun visitClass(classTree: ClassTree, unitTree: CompilationUnitTree): Trees  {

				if (unitTree is JCTree.JCCompilationUnit) {
					if (unitTree.sourcefile.getKind() == JavaFileObject.Kind.SOURCE) {
						unitTree.accept( object: TreeTranslator() {
							override fun visitVarDef(tree: JCTree.JCVariableDecl) {
								super.visitVarDef( tree );

								if ( ( tree.mods.flags and Flags.FINAL.toLong() ) == 0L ) {
									tree.mods.flags = Flags.FINAL.toLong() or tree.mods.flags
								}
							}
						})
					}
				}

				return trees;
			}
		};

		for(element in roundEnvironment.getElementsAnnotatedWith(ImmutableAnnotation::class.java)) {
			val path = trees.getPath(element)
			scanner.scan(path, path.compilationUnit)
		}

	}*/

}