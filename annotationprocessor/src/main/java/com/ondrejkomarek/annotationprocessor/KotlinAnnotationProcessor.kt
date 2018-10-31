package com.ondrejkomarek.annotationprocessor

import com.ondrejkomarek.annotation.*
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import kotlin.reflect.KClass


@SupportedAnnotationTypes("*") //com.ondrejkomarek.annotation.JavaAnnotation
@SupportedSourceVersion(SourceVersion.RELEASE_7)
class KotlinAnnotationProcessor : AbstractProcessor() {
	val PREFERENCES_NAME = "\"KotlinAnnotationProcessorPreferences\""

	private var generated: Boolean = false

	private var cycle = 0

	@Synchronized
	override fun init(processingEnv: ProcessingEnvironment) {
		super.init(processingEnv)
	}

	companion object {
		const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
	}

	//NOTE kind of strange way how to get parameter and return type
	private fun resolveElementType(typeMirror: TypeMirror?): KClass<out Any>? {
		if(typeMirror == null) {
			throw Exception("There must be one argument for data setter.")
		}

		when(typeMirror.kind) {
			TypeKind.FLOAT -> {
				processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Type: FLOAT")
				return Float::class
			}
			TypeKind.INT -> {
				processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Type: INT")
				return Int::class
			}
			TypeKind.SHORT -> {
				processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Type: SHORT") // Java int and Kotlin Int is recognised as Short, so if we are using this type recognition, we can only support Int or short :/
				return Int::class
			}
			TypeKind.LONG -> {
				processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Type: LONG")// Kotlin e.g. Long is recognised well
				return Long::class
			}
			TypeKind.BOOLEAN -> {
				processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Type: BOOLEAN")
				return Boolean::class
			}
			TypeKind.DECLARED -> {
				processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Type: DECLARED")//String is recognised as this, hmm.
				return String::class
			}
			TypeKind.DOUBLE -> {
				processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Type: DOUBLE")
				throw Exception("Double not supported")
			}
			TypeKind.BYTE -> {
				processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Type: BYTE")
				throw Exception("Byte not supported")
			}
			TypeKind.CHAR -> {
				processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Type: CHAR")
				throw Exception("Char not supported")
			}
			TypeKind.ARRAY -> {
				processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Type: ARRAY")
				throw Exception("ARRAY not supported")
			}
			TypeKind.NULL -> {
				processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Type: NULL")
				throw Exception("NULL not supported")
			}
			TypeKind.VOID -> {
				processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Type: VOID")
				throw Exception("VOID not supported")
			}
			TypeKind.NONE -> {
				processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Type: NONE")
				throw Exception("There must be a type")
			}

		}

		return String::class
	}

	private fun processDaoMethodInfo(element: Element, annotationType: DaoAnnotationType, daoFunctions: HashMap<String, ArrayList<MethodInfo>>) {
		if(element is ExecutableElement) {
			val fileFunctions = ArrayList<MethodInfo>()

			if(daoFunctions.containsKey(element.enclosingElement.toString())) {
				fileFunctions.addAll(daoFunctions.getValue(element.enclosingElement.toString()))
			}

			//processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "\n\n processDaoMethodInfo")

			val returnType: KClass<out Any>? = if(annotationType == DaoAnnotationType.ANNOTATION_LOAD) {
				resolveElementType(element.returnType)
			} else {
				null
			}
			val parameterType: KClass<out Any>? = if(annotationType == DaoAnnotationType.ANNOTATION_SAVE) {
				resolveElementType(element.parameters.firstOrNull()?.asType())
			} else {
				null
			}


			val preferenceKey = when(annotationType) {
				DaoAnnotationType.ANNOTATION_LOAD -> element.getAnnotation(Load::class.java).preferenceKey
				DaoAnnotationType.ANNOTATION_SAVE -> element.getAnnotation(Save::class.java).preferenceKey
			}

			fileFunctions.add(MethodInfo(parameterType, returnType, element.simpleName.toString(), annotationType, preferenceKey))
			daoFunctions.set(element.enclosingElement.toString(), fileFunctions)
		}
	}

	override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {

		processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "${cycle++} - Kotlin annotation processing round started, supported: $supportedAnnotationTypes")

		testImmutability(Diagnostic.Kind.WARNING, roundEnvironment)
		//addImmutability(Diagnostic.Kind.WARNING, roundEnvironment)
		testImmutability(Diagnostic.Kind.WARNING, roundEnvironment)

		getAnnotatedElementsInfo(Diagnostic.Kind.WARNING, roundEnvironment, KotlinClassAnnotation::class)
		getAnnotatedElementsInfo(Diagnostic.Kind.WARNING, roundEnvironment, KotlinAnnotationClassAnnotation::class)
		getAnnotatedElementsInfo(Diagnostic.Kind.WARNING, roundEnvironment, KotlinTypeAnnotation::class)
		getAnnotatedElementsInfo(Diagnostic.Kind.WARNING, roundEnvironment, KotlinFieldAnnotation::class)
		getAnnotatedElementsInfo(Diagnostic.Kind.WARNING, roundEnvironment, KotlinFunctionAnnotation::class)
		getAnnotatedElementsInfo(Diagnostic.Kind.WARNING, roundEnvironment, KotlinValueParameterAnnotation::class)
		getAnnotatedElementsInfo(Diagnostic.Kind.WARNING, roundEnvironment, KotlinConstructorAnnotation::class)


		roundEnvironment.getElementsAnnotatedWith(KotlinClassAnnotation::class.java)
				.forEach {
					val className = it.simpleName.toString()
					val pack = processingEnv.elementUtils.getPackageOf(it).toString()
					generateExampleClass(className, pack)
				}


		//NOTE this is used to gather info about methods in classes and generate the file all at once during one for cycle
		val daoFunctions = HashMap<String, ArrayList<MethodInfo>>()

		roundEnvironment.getElementsAnnotatedWith(Save::class.java)
				.forEach {
					processDaoMethodInfo(element = it, annotationType = DaoAnnotationType.ANNOTATION_SAVE, daoFunctions = daoFunctions)
				}

		roundEnvironment.getElementsAnnotatedWith(Load::class.java)
				.forEach {
					processDaoMethodInfo(element = it, annotationType = DaoAnnotationType.ANNOTATION_LOAD, daoFunctions = daoFunctions)
				}


		//NOTE generating e.g. Dao classes based on what methods are annotated
		if(!generated) {
			//processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "daoFiles size: ${daoFunctions.size}")

			generateDaoClasses(daoFunctions)
			generateDatabaseClasses(daoFunctions, roundEnvironment)
			generated = true
		}

		// Claiming that annotations have been processed by this processor
		return true
	}

	private fun generateDaoClasses(files: HashMap<String, ArrayList<MethodInfo>>) {
		val contextType = ClassName("android.content", "Context") //need to define type like this because we can not import android classes here

		for(fileContent in files) {
			val daoInterface = ClassName(getPackageName(fileContent.key), getClassName(fileContent.key)) //replace with more robust solution, move possibly to new package

			//processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "daoFunctions size: ${fileContent.value.size}")

			val fileName = "${getClassName(fileContent.key)}Implementation"
			val klass = TypeSpec.classBuilder(fileName)
					.addSuperinterface(daoInterface)

			for(method in fileContent.value) {
				when {
					method.daoAnnotationType == DaoAnnotationType.ANNOTATION_SAVE -> {

						method.parameterType?.let { parameterType ->
							//processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "ANNOTATION_SAVE 2")

							klass
									.addFunction(FunSpec.builder(getSimpleMethodname(method.methodName))
											.addParameter(ParameterSpec.builder("value", parameterType).build())
											.addParameter(ParameterSpec.builder("context", contextType).build())
											.addStatement(generateSaveToPreferences(method.preferenceKey, method.parameterType.simpleName, "value"))
											.addStatement("return")
											.addModifiers(KModifier.OVERRIDE)
											.build())
						}
					}
					method.daoAnnotationType == DaoAnnotationType.ANNOTATION_LOAD -> {
						method.returnType?.let { returnType ->
							//processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "ANNOTATION_LOAD 2")

							klass
									.addFunction(FunSpec.builder(getSimpleMethodname(method.methodName))
											.returns(returnType)
											.addParameter(ParameterSpec.builder("context", contextType).build())
											.addStatement("return ${generateGetFromPreferences(method.preferenceKey, method.returnType.simpleName)}")
											.addModifiers(KModifier.OVERRIDE)
											.build())
						}
					}
				}
			}
			val file = FileSpec.builder(getPackageName(fileContent.key), fileName)
					.addType(klass.build())
					.build()


			val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
			file.writeTo(File(kaptKotlinGeneratedDir, "$fileName.kt"))
		}
	}


	private fun generateDatabaseClasses(files: HashMap<String, ArrayList<MethodInfo>>, roundEnvironment: RoundEnvironment) {
		//NOTE I am not checking that dao object(s) are successfully created to be used as parameter for getDao methods
		roundEnvironment.getElementsAnnotatedWith(Database::class.java)
				.forEach {

					val databaseAbstractClass = ClassName(getPackageName(it.toString()), getClassName(it.toString()))
					val fileName = "${getClassName(it.toString())}Implementation"

					val klass = TypeSpec.classBuilder(fileName)
							.superclass(databaseAbstractClass)

					// NOTE I need to create new DatabaseImplementation class with implementation of getDao methods with appropriate return types
					for(file in files) {
						for(databasegetDaoMethod in it.enclosedElements) {

							if(databasegetDaoMethod is ExecutableElement) {//NOTE process only functions
								//processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "Dao Method Return type: ${databasegetDaoMethod.returnType.toString()}, ClassName: ${getClassName(file.key)}")

								//NOTE return type of getDao method must be same as some entry in "files" argument.
								if(getClassName(file.key) == getClassName(databasegetDaoMethod.returnType.toString())) { //we can add method which will return this Type+Implementation
									val fakeDao = ClassName(getPackageName(it.toString()), "${getClassName(file.key)}Implementation")
									klass
											.addFunction(FunSpec.builder(getSimpleMethodname(databasegetDaoMethod.simpleName.toString()))
													.addModifiers(KModifier.OVERRIDE)
													.returns(fakeDao)
													.addStatement("return %T()", fakeDao)
													.build())

								}
							}
						}
					}

					//NOTE writing to file, finally
					val file = FileSpec.builder(getPackageName(it.toString()), fileName)
							.addType(klass.build())
							.build()

					val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
					file.writeTo(File(kaptKotlinGeneratedDir, "$fileName.kt"))
				}
	}


	//NOTE local helper methods
	private fun generateGetFromPreferences(key: String, type: String?): String {
		if(type == null) {
			throw Exception("Return type for Get functions must be set")
		}
		val defaultValue = when(type) {
			"String" -> "\"\""
			"Long" -> "0L"
			"Int" -> "0"
			"Float" -> "0.0f"
			"Boolean" -> "false"
			else -> ""
		}

		return "${generateGetPreferences()}.get${type}(\"$key\", $defaultValue)"
	}

	private fun generateSaveToPreferences(key: String, type: String?, value: String): String {
		if(type == null) {
			throw Exception("Parameter type for Set functions must be set")
		}
		return "${generateGetPreferences()}.edit().put${type}(\"$key\", $value).commit()"
	}

	private fun generateGetPreferences(): String = "context.getSharedPreferences($PREFERENCES_NAME, Context.MODE_PRIVATE)"


	private fun getSimpleMethodname(name: String): String {
		return name.replace("[^a-zA-Z]", "")
	}

	private fun getClassName(name: String): String {
		return name.substring(name.lastIndexOf(".") + 1, name.length)
	}

	private fun getPackageName(name: String): String {
		return name.substring(0, name.lastIndexOf("."))
	}


	//NOTE methods for other smaller examples
	private fun generateExampleClass(className: String, pack: String) {
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


	fun getAnnotatedElementsInfo(logType: javax.tools.Diagnostic.Kind, roundEnvironment: RoundEnvironment, annotationClass: KClass<out kotlin.Annotation>) {

		for(element in roundEnvironment.getElementsAnnotatedWith(annotationClass.java)) {
			processingEnv.messager.printMessage(logType, "\n\nElement: ${element.simpleName}, Annotation: ${annotationClass.simpleName}")

			for(enclosedElement in element.getEnclosedElements()) {
				processingEnv.messager.printMessage(logType, "Kind: ${enclosedElement.kind}")
				processingEnv.messager.printMessage(logType, "Name: ${enclosedElement.simpleName}")
			}
		}
	}

	//NOTE test by changing final word in MutableClass
	fun testImmutability(logType: javax.tools.Diagnostic.Kind, roundEnvironment: RoundEnvironment) {
		for(element in roundEnvironment.getElementsAnnotatedWith(ImmutableAnnotation::class.java)) {
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

	//NOTE classes like TreePathScanner can be imported, but they are actually missing in android -> com.sun... package is proprietary
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

class MethodInfo(
		val parameterType: KClass<out Any>?,
		val returnType: KClass<out Any>?,
		val methodName: String,
		val daoAnnotationType: DaoAnnotationType,
		val preferenceKey: String
)

enum class DaoAnnotationType {
	ANNOTATION_SAVE,
	ANNOTATION_LOAD
}