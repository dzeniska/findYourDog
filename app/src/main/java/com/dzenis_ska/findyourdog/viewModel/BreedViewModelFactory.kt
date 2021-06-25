package com.dzenis_ska.findyourdog.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dzenis_ska.findyourdog.Repository.Repository
import javax.inject.Inject


@Suppress("UNCHECKED_CAST")
class BreedViewModelFactory @Inject constructor (val repository: Repository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return BreedViewModel(repository) as T
    }
}