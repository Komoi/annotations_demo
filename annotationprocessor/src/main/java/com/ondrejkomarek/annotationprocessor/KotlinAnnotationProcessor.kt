package com.ondrejkomarek.annotationprocessor

import com.ondrejkomarek.annotation.Database
import com.ondrejkomarek.annotation.KotlinClassAnnotation
import com.ondrejkomarek.annotation.Load
import com.ondrejkomarek.annotation.Save
import com.squareup.kotlinpoet.*
import sun.reflect.generics.tree.ReturnType
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
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

	private var cycle = 0

	@Synchronized
	override fun init(processingEnv: ProcessingEnvironment) {
		super.init(processingEnv)
		//trees = Trees.instance( processingEnv );

	}

	companion object {
		const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
	}


	override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {

		processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "${cycle++} - Kotlin annotation processing round started, supported: $supportedAnnotationTypes")

		/*testImmutability(Diagnostic.Kind.WARNING, roundEnvironment)
		//addImmutability(Diagnostic.Kind.WARNING, roundEnvironment)
		testImmutability(Diagnostic.Kind.WARNING, roundEnvironment)

		getInfo(Diagnostic.Kind.NOTE, roundEnvironment, KotlinClassAnnotation::class)
		getInfo(Diagnostic.Kind.NOTE, roundEnvironment, KotlinAnnotationClassAnnotation::class)
		getInfo(Diagnostic.Kind.NOTE, roundEnvironment, KotlinTypeAnnotation::class)
		getInfo(Diagnostic.Kind.NOTE, roundEnvironment, KotlinFieldAnnotation::class)
		getInfo(Diagnostic.Kind.NOTE, roundEnvironment, KotlinFunctionAnnotation::class)
		getInfo(Diagnostic.Kind.NOTE, roundEnvironment, KotlinValueParameterAnnotation::class)
		getInfo(Diagnostic.Kind.NOTE, roundEnvironment, KotlinConstructorAnnotation::class)
*/
		//just temp to get pack
		var tempPack = ""

		roundEnvironment.getElementsAnnotatedWith(KotlinClassAnnotation::class.java)
				.forEach {
					val className = it.simpleName.toString()
					val pack = processingEnv.elementUtils.getPackageOf(it).toString()
					tempPack = pack
					generateClass(className, pack)
				}


		//this is used to gather info about methods in classes and generate the file all at once during one for cycle
		// Elegant? LOL
		var daoFunctions = HashMap<String, HashMap<DaoAnnoatationTypes, ArrayList<String>>>()

		roundEnvironment.getElementsAnnotatedWith(Save::class.java)
				.forEach {
					//TODO make next few lines more nice and reusable
					if(it is ExecutableElement) {
						if(daoFunctions.containsKey(it.enclosingElement.toString())) {
							//Pair(DaoAnnoatationTypes.ANNOTATION_SAVE,
							val fileFunctionMap = daoFunctions.getValue(it.enclosingElement.toString())

							fileFunctionMap.get(DaoAnnoatationTypes.ANNOTATION_SAVE)?.let { saveArrayList ->
								saveArrayList.add(getSimpleMethodname(it.simpleName.toString()))
							}
									?: fileFunctionMap.set(DaoAnnoatationTypes.ANNOTATION_SAVE, arrayListOf(it.simpleName.toString())); daoFunctions.set(it.enclosingElement.toString(), fileFunctionMap) //TODO test if works properly - is it being saved?

							daoFunctions.set(it.enclosingElement.toString(), fileFunctionMap)
						} else {
							daoFunctions.set(it.enclosingElement.toString(), hashMapOf(Pair(DaoAnnoatationTypes.ANNOTATION_SAVE, arrayListOf(it.simpleName.toString()))))
						}
						processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "Save Element: ${getSimpleMethodname(it.simpleName.toString())}, enclosing class: ${getClassName(it.enclosingElement.toString())}, package: ${getPackageName(it.enclosingElement.toString())}")
					}
				}

		roundEnvironment.getElementsAnnotatedWith(Load::class.java)
				.forEach {
					if(it is ExecutableElement) {
						if(daoFunctions.containsKey(it.enclosingElement.toString())) {
							//Pair(DaoAnnoatationTypes.ANNOTATION_SAVE,
							val fileFunctionMap = daoFunctions.getValue(it.enclosingElement.toString())

							fileFunctionMap.get(DaoAnnoatationTypes.ANNOTATION_LOAD)?.let { saveArrayList ->
								saveArrayList.add(getSimpleMethodname(it.simpleName.toString()))
							}
									?: fileFunctionMap.set(DaoAnnoatationTypes.ANNOTATION_LOAD, arrayListOf(it.simpleName.toString())); daoFunctions.set(it.enclosingElement.toString(), fileFunctionMap) //TODO test if works properly - is it being saved?

							daoFunctions.set(it.enclosingElement.toString(), fileFunctionMap)
						} else {
							daoFunctions.set(it.enclosingElement.toString(), hashMapOf(Pair(DaoAnnoatationTypes.ANNOTATION_LOAD, arrayListOf(it.simpleName.toString()))))
						}
						processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "Save Element: ${getSimpleMethodname(it.simpleName.toString())}, enclosing class: ${getClassName(it.enclosingElement.toString())}, package: ${getPackageName(it.enclosingElement.toString())}")
					}
				}

		/*

		roundEnvironment.getElementsAnnotatedWith(Load::class.java)
				.forEach {
					if(it is ExecutableElement){
						if(daoFunctions.containsKey(Pair(DaoAnnoatationTypes.ANNOTATION_LOAD, it.enclosingElement.toString()))){
							val currentList = daoFunctions.getValue(Pair(DaoAnnoatationTypes.ANNOTATION_LOAD, it.enclosingElement.toString()))
							currentList.add(it.simpleName.toString())
							daoFunctions.set(Pair(DaoAnnoatationTypes.ANNOTATION_LOAD, it.enclosingElement.toString()), currentList)
						} else {
							daoFunctions.set(Pair(DaoAnnoatationTypes.ANNOTATION_LOAD, it.enclosingElement.toString()), arrayListOf(it.simpleName.toString()))
						}
						processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "Load Element: ${getSimpleMethodname(it.simpleName.toString())}, enclosing class: ${getClassName(it.enclosingElement.toString())}, package: ${getPackageName(it.enclosingElement.toString())}")
					}

				}
		 */

		//TODO get annotations, go trough them, generate e.g. Dao class based on what methods are annotated

		if(!generated) {
			generateTestFakeDaoClass(daoFunctions)
			generateTestFakeDatabseClass("FakeDatabase", tempPack, daoFunctions, roundEnvironment)
			generated = true
		}


		// Claiming that annotations have been processed by this processor
		return true
	}

	private fun getSimpleMethodname(name: String): String {
		return name.replace("[^a-zA-Z]", "")
	}

	private fun getClassName(name: String): String {
		return name.substring(name.lastIndexOf(".") + 1, name.length)
	}

	private fun getPackageName(name: String): String {
		return name.substring(0, name.lastIndexOf("."))
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

	//TODO pack and class name will be deprecated when finished
	private fun generateTestFakeDaoClass(files: HashMap<String, HashMap<DaoAnnoatationTypes, ArrayList<String>>>) { //TODO get class as whole package here, separate it in here to pack and class name
		for(fileContent in files) {
			val baseFakeDao = ClassName(getPackageName(fileContent.key), getClassName(fileContent.key)) //replace with more robust solution, move possibly to new package

			val fileName = "${getClassName(fileContent.key)}Implementation"
			val klass = TypeSpec.classBuilder(fileName)
					.addSuperinterface(baseFakeDao)

			for(fileMethodList in fileContent.value) {
				when {
					fileMethodList.key == DaoAnnoatationTypes.ANNOTATION_SAVE -> {
						for(method in fileMethodList.value){
							klass
									.addFunction(FunSpec.builder(getSimpleMethodname(method))
											//.returns(String::class)
											//TODO lets say that these methods can only work with String types - it would be yet another information I need to keep - or use MethodInfo class!!
											.addParameter(ParameterSpec.builder("value", String::class).build())
											.addStatement("return")
											.addModifiers(KModifier.OVERRIDE)
											.build())
						}

					}
					fileMethodList.key == DaoAnnoatationTypes.ANNOTATION_LOAD -> {
						for(method in fileMethodList.value){
							klass
									.addFunction(FunSpec.builder(getSimpleMethodname(method))
											.returns(String::class)
											//TODO lets say that these methods can only work with String types - it would be yet another information I need to keep - or use MethodInfo class!!
											.addStatement("return \"Fake data added by processor\"")
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

/*
		val file = FileSpec.builder(pack, fileName)
				.addType(TypeSpec.classBuilder(fileName)
						.addSuperinterface(baseFakeDao)
						//.superclass(baseDao)
						//.replaceAll("[^a-zA-Z]", "")
						.addFunction(FunSpec.builder("getFakeData")
								.returns(String::class)
								.addStatement("return \"Fake data added by processor\"")
								.addModifiers(KModifier.OVERRIDE)
								.build())
						.addFunction(FunSpec.builder("setFakeData")
								.returns(String::class)
								.addStatement("return \"Fake data added by processor\"")
								.addModifiers(KModifier.OVERRIDE)
								.build())
						.build())
				.build()
*/


	}

	private fun generateTestFakeDatabseClass(className: String, pack: String, files: HashMap<String, HashMap<DaoAnnoatationTypes, ArrayList<String>>>, roundEnvironment: RoundEnvironment) {
		//TODO we need to make sure that we successfully created dao object(s) to put it in database
		roundEnvironment.getElementsAnnotatedWith(Database::class.java)
				.forEach {

					val baseFakeDatabase = ClassName(getPackageName(it.toString()), getClassName(it.toString())) // TODO replace with more robust solution, move possibly to new package
					val fileName = "${getClassName(it.toString())}Implementation"

					val klass = TypeSpec.classBuilder(fileName)
							.superclass(baseFakeDatabase)

					//TODO return type of method must be same as some entry in files: HashMap... argument. If not, error must be thrown.
					// TODO If yes, I need to create new DatabaseImplementation class with implementation of getDao methods with appropriate return type

					for(file in files){
						for(databasegetDaoMethod in it.enclosedElements){

							if(databasegetDaoMethod is ExecutableElement) {//TODO what to do if it is not this type? Just ignore?
								processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "Dao Method Return type: ${databasegetDaoMethod.returnType.toString()}, ClassName: ${getClassName(file.key)}")

								if(getClassName(file.key) == getClassName(databasegetDaoMethod.returnType.toString())) { //THIS IS MATCH!, we can add method which will return this Type+Implementation
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




				/*	val file = FileSpec.builder(getPackageName(it.toString()), fileName)
							.addType(TypeSpec.classBuilder(fileName)
									.superclass(baseFakeDatabase)

									.addFunction(FunSpec.builder("getFakeDao")
											.addModifiers(KModifier.OVERRIDE)
											.returns(fakeDao)
											.addStatement("return %T()", fakeDao)
											.build())
									.build())
							.build()

							*/

					val file = FileSpec.builder(getPackageName(it.toString()), fileName)
							.addType(klass.build())
							.build()

					val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
					file.writeTo(File(kaptKotlinGeneratedDir, "$fileName.kt"))
				}





	}


	fun getInfo(logType: javax.tools.Diagnostic.Kind, roundEnvironment: RoundEnvironment, annotationClass: KClass<out kotlin.Annotation>) {

		for(element in roundEnvironment.getElementsAnnotatedWith(annotationClass.java)) {
			processingEnv.messager.printMessage(logType, "\n\nElement: ${element.simpleName}, Annotation: ${annotationClass.simpleName}")

			for(enclosedElement in element.getEnclosedElements()) {
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

class MethodInfo(){
	constructor(returnType: KClass<Any>,
				methodName: String): this(){
		this.returnType = returnType
		this.methodName = methodName
	}
	var returnType: KClass<out Any> = String::class
	var methodName: String = ""
}

enum class DaoAnnoatationTypes {
	ANNOTATION_SAVE,
	ANNOTATION_LOAD
}