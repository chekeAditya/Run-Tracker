package com.androidtracker.runningtracker.viewmodels

import androidx.lifecycle.ViewModel
import com.androidtracker.runningtracker.repositories.MainRepository
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(val mainRepository: MainRepository) : ViewModel() {

}