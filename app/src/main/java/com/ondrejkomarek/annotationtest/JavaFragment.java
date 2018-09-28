package com.ondrejkomarek.annotationtest;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import com.ondrejkomarek.annotationtest.databinding.FragmentJavaBinding;

import org.alfonz.arch.AlfonzBindingFragment;

public class JavaFragment extends AlfonzBindingFragment<JavaViewModel, FragmentJavaBinding> implements JavaView
{

	FragmentJavaBinding binding = null;

	@Override
	public FragmentJavaBinding inflateBindingLayout(@NonNull LayoutInflater inflater)
	{
		binding = FragmentJavaBinding.inflate(inflater);
		return binding;
	}


	@Override
	public JavaViewModel setupViewModel()
	{
		return ViewModelProviders.of(this).get(JavaViewModel.class);
	}


	public void onOpenKotlinActivityClick()
	{
		startActivity(new Intent(getContext(), KotlinActivity.class));
		getActivity().finish();

	}
}