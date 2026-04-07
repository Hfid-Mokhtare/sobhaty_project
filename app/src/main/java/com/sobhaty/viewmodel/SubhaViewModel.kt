package com.sobhaty.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sobhaty.api.SubhaApiService
import com.sobhaty.model.Thikr
import com.sobhaty.repository.SubhaRepository
import com.sobhaty.util.VibrationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SubhaViewModel(applicationContext: Context) : ViewModel() {
    private val repository = SubhaRepository(applicationContext)
    private val apiService = SubhaApiService.create()
    private var loadJob: Job? = null
    
    // Default local dhikr for offline use - Removed "المكيال الأوفى"
    private val localDhikr = listOf(
        Thikr(0, "الإستغفار", "رَبِّ اغْفِرْ لِي وَ تُبْ عَلَيَّ إِنَّكَ أَنْتَ التَّوَّابُ الرَّحِيمُ", 100),
        Thikr(2, "الكلمة الطيبة", "لَا إِلَهَ إِلَّا اللهُ", 100),
        Thikr(3, "التسبيح", "سُبْحَانَ اللهِ وَ بِحَمْدِهِ", 100),
        Thikr(4, "أدعية التحصين", "لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَ لَهُ الْحَمْدُ وَ هُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ", 100)
    )

    val dhikrList = mutableStateListOf<Thikr>().apply { addAll(localDhikr) }

    var selectedIndex by mutableIntStateOf(0)
    var counter by mutableIntStateOf(0)
    var currentTarget by mutableIntStateOf(33)
    val completedStates = mutableStateMapOf<Int, Boolean>()

    init {
        fetchRemoteAthkar()
        loadInitialState()
    }

    private fun loadInitialState() {
        viewModelScope.launch(Dispatchers.IO) {
            val lastIndex = repository.getInt(SubhaRepository.KEY_SELECTED_INDEX, 0).first()
            launch(Dispatchers.Main) {
                // Bounds check since we removed an item
                val indexToLoad = if (lastIndex < dhikrList.size) lastIndex else 0
                selectedIndex = indexToLoad
                loadData(indexToLoad)
            }
        }
    }

    private fun normalizeArabic(text: String): String {
        return text.replace("ال", "")
            .replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .trim()
    }

    private fun fetchRemoteAthkar() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getAthkar()
                if (response.isSuccessful) {
                    val remoteList = response.body()
                    if (remoteList != null) {
                        launch(Dispatchers.Main) {
                            val updatedList = localDhikr.toMutableList()
                            remoteList.forEach { remote ->
                                // Filter out "المكيال الأوفى" from remote as well if it matches the name
                                if (remote.category != "المكيال الأوفى") {
                                    val idx = updatedList.indexOfFirst { 
                                        normalizeArabic(it.category) == normalizeArabic(remote.category) || it.id == remote.id 
                                    }
                                    if (idx != -1) {
                                        updatedList[idx] = remote
                                    } else {
                                        updatedList.add(remote)
                                    }
                                }
                            }
                            
                            dhikrList.clear()
                            dhikrList.addAll(updatedList)
                            
                            if (selectedIndex < dhikrList.size) {
                                loadData(selectedIndex)
                            } else {
                                selectedIndex = 0
                                loadData(0)
                            }
                        }
                    }
                }
            } catch (e: Exception) { }
        }
    }

    fun selectDhikr(index: Int) {
        if (index < dhikrList.size) {
            selectedIndex = index
            viewModelScope.launch(Dispatchers.IO) {
                repository.saveInt(SubhaRepository.KEY_SELECTED_INDEX, index)
            }
            loadData(index)
        }
    }

    fun loadData(dhikrIdInList: Int) {
        if (dhikrIdInList >= dhikrList.size) return
        val dhikr = dhikrList[dhikrIdInList]
        loadJob?.cancel()
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            val targetKey = "target_${dhikr.id}"
            val counterKey = "counter_${dhikr.id}"
            
            val targetValue = repository.getInt(targetKey, dhikr.count).first()
            val counterValue = repository.getInt(counterKey, 0).first()
            
            launch(Dispatchers.Main) {
                currentTarget = targetValue
                counter = counterValue
                completedStates[dhikrIdInList] = counterValue >= targetValue
            }
        }
    }

    fun increment(context: Context, haptic: HapticFeedback? = null) {
        if (counter < currentTarget) {
            counter++
            if (counter >= currentTarget) {
                completedStates[selectedIndex] = true
                VibrationUtil.longVibrate(context)
            } else if (counter % 100 == 0) {
                VibrationUtil.doubleVibrate(context)
            } else {
                VibrationUtil.shortVibrate(context)
            }
            saveCurrentProgress()
        }
    }

    private fun saveCurrentProgress() {
        if (selectedIndex >= dhikrList.size) return
        val id = dhikrList[selectedIndex].id
        val currentCount = counter
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveInt("counter_$id", currentCount)
        }
    }

    fun updateTarget(newTarget: Int) {
        if (selectedIndex >= dhikrList.size) return
        currentTarget = newTarget
        val id = dhikrList[selectedIndex].id
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveInt("target_$id", newTarget)
        }
        completedStates[selectedIndex] = counter >= newTarget
    }

    fun reset() {
        counter = 0
        completedStates[selectedIndex] = false
        saveCurrentProgress()
    }
}
