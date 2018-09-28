package com.ondrejkomarek.annotationprocessor;


import com.ondrejkomarek.annotation.ImmutableAnnotation;
import com.ondrejkomarek.annotation.JavaAnnotation;

/*
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
*/
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;


@SupportedAnnotationTypes("com.ondrejkomarek.annotation.*")
//com.ondrejkomarek.annotation.JavaAnnotation
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AnnotationProcessor extends AbstractProcessor// or implements Processor
{



	@Override
	public synchronized void init(ProcessingEnvironment processingEnv)
	{
		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		for(final SourceVersion version : compiler.getSourceVersions())
		{
			System.out.println(version);
		}

		super.init(processingEnv);


	}


	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment)
	{
		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Java annotation process started " + getSupportedAnnotationTypes());

		testImmutability(Diagnostic.Kind.WARNING, roundEnvironment);
		//addImmutability(Diagnostic.Kind.WARNING, roundEnvironment);
		testImmutability(Diagnostic.Kind.WARNING, roundEnvironment);

		// Claiming that annotations have been processed by this processor
		return true;
	}


	void testImmutability(javax.tools.Diagnostic.Kind logType, RoundEnvironment roundEnvironment)
	{
		for(final Element element : roundEnvironment.getElementsAnnotatedWith(JavaAnnotation.class))
		{
			if(element instanceof TypeElement)
			{
				final TypeElement typeElement = (TypeElement) element;

				for(final Element enclosedElement : typeElement.getEnclosedElements())
				{
					if(enclosedElement instanceof VariableElement)
					{
						final VariableElement variableElement = (VariableElement) enclosedElement;

						if(!variableElement.getModifiers().contains(Modifier.FINAL))
						{
							processingEnv.getMessager().printMessage(logType,
									String.format("Class '%s' is annotated as @Immutable, but field '%s' is not declared as final",
											typeElement.getSimpleName(), variableElement.getSimpleName()
									)
							);
						}
					}
				}
			}
		}
	}


/*	void addImmutability(javax.tools.Diagnostic.Kind logType, RoundEnvironment roundEnvironment)
	{
		final TreePathScanner<Object, CompilationUnitTree> scanner =
				new TreePathScanner<Object, CompilationUnitTree>()
				{
					@Override
					public Trees visitClass(final ClassTree classTree,
											final CompilationUnitTree unitTree)
					{

						if(unitTree instanceof JCTree.JCCompilationUnit)
						{
							final JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) unitTree;

							// Only process on files which have been compiled from source
							if(compilationUnit.sourcefile.getKind() == JavaFileObject.Kind.SOURCE)
							{
								compilationUnit.accept(new TreeTranslator()
								{
									@Override
									public void visitVarDef(final JCTree.JCVariableDecl tree)
									{
										super.visitVarDef(tree);

										if((tree.mods.flags & Flags.FINAL) == 0)
										{
											tree.mods.flags |= Flags.FINAL;
										}
									}
								});
							}
						}

						return trees;
					}
				};

		for(final Element element : roundEnvironment.getElementsAnnotatedWith(ImmutableAnnotation.class))
		{
			final TreePath path = trees.getPath(element);
			scanner.scan(path, path.getCompilationUnit());
		}
	}*/
}