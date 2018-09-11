package com.ondrejkomarek.annotationprocessor;


import com.ondrejkomarek.annotation.Annotation;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;


@SupportedAnnotationTypes( "*" )//com.ondrejkomarek.annotation.Annotation
@SupportedSourceVersion( SourceVersion.RELEASE_7 )
public class AnnotationProcessor extends AbstractProcessor// or implements Processor
{

	private static final String METHOD_PREFIX = "start";
	private static final ClassName classIntent = ClassName.get("android.content", "Intent");
	private static final ClassName classContext = ClassName.get("android.content", "Context");

	private Filer filer;
	private Messager messager;
	private Elements elements;
	private Map<String, String> activitiesWithPackage;


	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		for( final SourceVersion version: compiler.getSourceVersions() ) {
			System.out.println( version );
		}

		super.init(processingEnv); //TODO modify?
	}


/*	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return new HashSet<>();
	}

*/

	/*@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.RELEASE_8;//new ImmutableSet.of(Annotation.class.getCanonicalName());
	}
*/

	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment)
	{
		processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "annotation process started");
		for( final Element element: roundEnvironment.getElementsAnnotatedWith( Annotation.class ) )
		{
			if(element instanceof TypeElement)
			{
				final TypeElement typeElement = (TypeElement) element;

				for(final Element eclosedElement : typeElement.getEnclosedElements())
				{
					if(eclosedElement instanceof VariableElement)
					{
						final VariableElement variableElement = (VariableElement) eclosedElement;

						if(!variableElement.getModifiers().contains(Modifier.FINAL))
						{
							processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
									String.format("Class '%s' is annotated as @Immutable, but field '%s' is not declared as final",
											typeElement.getSimpleName(), variableElement.getSimpleName()
									)
							);
						}
					}
				}
			}
		}

			// Claiming that annotations have been processed by this processor
			return true;



		/*for (Element element : roundEnvironment.getElementsAnnotatedWith(Annotation.class)) {

			if (element.getKind() != ElementKind.CLASS) {
				return false;
			}

			TypeElement typeElement = (TypeElement) element;
			activitiesWithPackage.put(
					typeElement.getSimpleName().toString(),
					elements.getPackageOf(typeElement).getQualifiedName().toString());
		}



		 // 2- Generate a class

		TypeSpec.Builder navigatorClass = TypeSpec
				.classBuilder("Navigator")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL);

		for (Map.Entry<String, String> element : activitiesWithPackage.entrySet()) {
			String activityName = element.getKey();
			String packageName = element.getValue();
			ClassName activityClass = ClassName.get(packageName, activityName);
			MethodSpec intentMethod = MethodSpec
					.methodBuilder(METHOD_PREFIX + activityName)
					.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
					.returns(classIntent)
					.addParameter(classContext, "context")
					.addStatement("return new $T($L, $L)", classIntent, "context", activityClass + ".class")
					.build();
			navigatorClass.addMethod(intentMethod);
		}



		// 3- Write generated class to a file

		try
		{
			JavaFile.builder("com.annotationsample", navigatorClass.build()).build().writeTo(filer);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		return true;

		*/
	}
}