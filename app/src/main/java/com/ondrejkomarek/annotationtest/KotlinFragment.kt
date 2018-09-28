package com.ondrejkomarek.annotationtest

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.view.LayoutInflater
import com.ondrejkomarek.annotationtest.databinding.FragmentKotlinBinding
import org.alfonz.arch.AlfonzBindingFragment
interface KotlinView{
	fun onOpenJavaActivityClick()
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
}