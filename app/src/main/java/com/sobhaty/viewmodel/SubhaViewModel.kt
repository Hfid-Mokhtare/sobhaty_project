package com.sobhaty.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.ViewModel
import com.sobhaty.model.Dhikr
import com.sobhaty.repository.SubhaRepository
import com.sobhaty.util.VibrationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SubhaViewModel(applicationContext: Context) : ViewModel() {
    private val repository = SubhaRepository(applicationContext)
    
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

    fun loadData(dhikrId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val targetKey = "target_$dhikrId"
            val counterKey = "counter_$dhikrId"
            
            val targetValue = repository.getInt(targetKey, dhikrList[dhikrId].defaultTarget).first()
            val counterValue = repository.getInt(counterKey, 0).first()
            
            launch(Dispatchers.Main) {
                currentTarget = targetValue
                counter = counterValue
                completedStates[dhikrId] = counterValue >= targetValue
            }
        }
    }

    fun increment(context: Context, haptic: HapticFeedback?) {
        if (counter < currentTarget) {
            counter++
            if (counter == currentTarget) {
                completedStates[dhikrList[selectedIndex].id] = true
                VibrationUtil.longVibrate(context)
            } else {
                haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            saveCurrentProgress()
        }
    }

    private fun saveCurrentProgress() {
        val id = dhikrList[selectedIndex].id
        val currentCount = counter
        CoroutineScope(Dispatchers.IO).launch {
            repository.saveInt("counter_$id", currentCount)
        }
    }

    fun updateTarget(newTarget: Int) {
        currentTarget = newTarget
        val id = dhikrList[selectedIndex].id
        CoroutineScope(Dispatchers.IO).launch {
            repository.saveInt("target_$id", newTarget)
        }
        if (counter < newTarget) {
            completedStates[id] = false
        }
    }

    fun reset() {
        counter = 0
        val id = dhikrList[selectedIndex].id
        completedStates[id] = false
        saveCurrentProgress()
    }
}
