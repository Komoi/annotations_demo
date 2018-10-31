package com.ondrejkomarek.annotationtest

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast

import com.ondrejkomarek.annotationtest.database.KotlinDatabase
import com.ondrejkomarek.annotationtest.database.JavaDatabase
import com.ondrejkomarek.annotationtest.databinding.FragmentKotlinBinding
import com.ondrejkomarek.database.BaseDatabase
import org.alfonz.arch.AlfonzBindingFragment
interface KotlinView{
	fun onOpenJavaActivityClick()
	fun onShowValuesClick()
	fun onSaveFloatValueClick()
	fun onSaveStringValueClick()
}

class KotlinFragment: AlfonzBindingFragment<KotlinViewModel, FragmentKotlinBinding>(), KotlinView {


	lateinit var binding: FragmentKotlinBinding

	override fun inflateBindingLayout(inflater: LayoutInflater): FragmentKotlinBinding {
		binding = FragmentKotlinBinding.inflate(inflater)
		return binding
	}

	override fun setupViewModel(): KotlinViewModel {
		 return ViewModelProviders.of(this).get(KotlinViewModel::class.java)
	}

	override fun onOpenJavaActivityClick() {
		startActivity(Intent(context, JavaActivity::class.java))
		activity?.finish()
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		Toast.makeText(context, "Hello ${Generated_KotlinActivity().getName()}", Toast.LENGTH_LONG).show()
		return super.onCreateView(inflater, container, savedInstanceState)!!
	}

	override fun onShowValuesClick() {
		Toast.makeText(context, "Long data: ${(BaseDatabase.getGeneratedDatabase<KotlinDatabase>()).getKotlinDao().getLongData(context!!)}", Toast.LENGTH_LONG).show()
		Toast.makeText(context, "String data: ${(BaseDatabase.getGeneratedDatabase<JavaDatabase>()).getJavaDao().getStringData(context!!)}", Toast.LENGTH_LONG).show()
	}

	override fun onSaveFloatValueClick() {
		BaseDatabase.getGeneratedDatabase<KotlinDatabase>().getKotlinDao().setLongData(binding.floatEdittext.text.toString().toLong(), context!!)
		binding.floatEdittext.setText("")
	}

	override fun onSaveStringValueClick() {
		BaseDatabase.getGeneratedDatabase<JavaDatabase>().getJavaDao().setStringData(binding.stringEdittext.text.toString(), context!!)
		binding.stringEdittext.setText("")
	}
}