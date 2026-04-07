package com.sobhaty.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sobhaty.model.Dhikr
import com.sobhaty.repository.SubhaRepository
import com.sobhaty.util.VibrationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SubhaViewModel(applicationContext: Context) : ViewModel() {
    private val repository = SubhaRepository(applicationContext)
    private var loadJob: Job? = null
    
    val dhikrList = listOf(
        Dhikr(0, "الإستغفار", "رَبِّ اغْفِرْ لِي وَ تُبْ عَلَيَّ إِنَّكَ أَنْتَ التَّوَّابُ الرَّحِيمُ", 100),
        Dhikr(1, "المكيال الأوفى", "اللَّهُمَّ صَلِّ عَلَى سَيِّدِنَا مُحَمَّدٍ النَّبِيِّ وَ عَلَى أَزْوَاجِهِ أُمَّهَاتِ الْمُؤْمِنِينَ وَ عَلَى ذُرِّيَّتِهِ وَ أَهْلِ بَيْتِهِ كَمَا صَلَّيْتَ عَلَى آلِ سَيِّدِنَا إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ", 10),
        Dhikr(2, "الكلمة الطيبة", "لَا إِلَهَ إِلَّا اللهُ", 100),
        Dhikr(3, "التسبيح", "سُبْحَانَ اللهِ وَ بِحَمْدِهِ", 100),
        Dhikr(4, "أدعية التحصين", "لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَ لَهُ الْحَمْدُ وَ هُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ", 100)
    )

    var selectedIndex by mutableIntStateOf(0)
    var counter by mutableIntStateOf(0)
    var currentTarget by mutableIntStateOf(33)
    val completedStates = mutableStateMapOf<Int, Boolean>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val lastIndex = repository.getInt(SubhaRepository.KEY_SELECTED_INDEX, 0).first()
            launch(Dispatchers.Main) {
                selectedIndex = lastIndex
                loadData(lastIndex)
            }
        }
    }

    fun selectDhikr(index: Int) {
        selectedIndex = index
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveInt(SubhaRepository.KEY_SELECTED_INDEX, index)
        }
        loadData(index)
    }

    fun loadData(dhikrId: Int) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            val targetValue = repository.getInt("target_$dhikrId", dhikrList[dhikrId].defaultTarget).first()
            val counterValue = repository.getInt("counter_$dhikrId", 0).first()
            
            launch(Dispatchers.Main) {
                currentTarget = targetValue
                counter = counterValue
                completedStates[dhikrId] = counterValue >= targetValue
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
        val id = dhikrList[selectedIndex].id
        val currentCount = counter
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveInt("counter_$id", currentCount)
        }
    }

    fun updateTarget(newTarget: Int) {
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
