package com.sobhaty.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.withContext

class SubhaViewModel(applicationContext: Context) : ViewModel() {
    private val repository = SubhaRepository(applicationContext)
    private val apiService = SubhaApiService.create()
    private var loadJob: Job? = null
    private var fetchJob: Job? = null
    private val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val baseAthkar = listOf(
        Thikr(1, "الإستغفار", "رَبِّ اغْفِرْ لِي وَتُبْ عَلَيَّ إِنَّكَ أَنْتَ التَّوَّابُ الرَّحِيمُ", 33),
        Thikr(2, "الصلاة على النبي ﷺ", "اللَّهُمَّ صَلِّ عَلَى سَيِّدِنَا مُحَمَّدٍ النَّبِيِّ وَأَزْواجِهِ أُمَّهَاتِ الْمُؤْمِنِينَ وَذُرِّيَّتِهِ وَأَهْلِ بَيْتِهِ كَمَا صَلَّيْتَ عَلَى آلِ سَيِّدِنَا إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ", 100),
        Thikr(3, "الكلمة الطيبة", "لَا إِلَهَ إِلَّا اللَّهُ", 1000),
        Thikr(4, "تسبيح", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", 100),
        Thikr(5, "أدعية التحصين", "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قديرٌ", 100)
    )

    val dhikrList = mutableStateListOf<Thikr>().apply { addAll(baseAthkar) }

    var selectedIndex by mutableIntStateOf(0)
    var counter by mutableIntStateOf(0)
    var currentTarget by mutableIntStateOf(33)
    val completedStates = mutableStateMapOf<Int, Boolean>()
    
    var isOnboardingCompleted by mutableStateOf(true)
    var isTooltipShown by mutableStateOf(true)
    var isBottomBarPulseShown by mutableStateOf(true)
    
    // وضع المظلم (Dark Mode)
    var isDarkMode by mutableStateOf(true)

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            fetchRemoteAthkar()
        }
    }

    init {
        loadInitialState()
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) { }
    }

    private fun loadInitialState() {
        viewModelScope.launch(Dispatchers.IO) {
            val lastIndex: Int = repository.getInt(SubhaRepository.KEY_SELECTED_INDEX, 0).first()
            val onboardingDone: Boolean = repository.getBoolean(SubhaRepository.KEY_ONBOARDING_COMPLETED, false).first()
            val tooltipDone: Boolean = repository.getBoolean(SubhaRepository.KEY_TOOLTIP_SHOWN, false).first()
            val pulseDone: Boolean = repository.getBoolean(SubhaRepository.KEY_BOTTOM_BAR_PULSE_SHOWN, false).first()
            val darkMode: Boolean = repository.getBoolean(SubhaRepository.KEY_DARK_MODE, true).first() // القيمة الافتراضية true
            
            withContext(Dispatchers.Main) {
                selectedIndex = if (lastIndex in dhikrList.indices) lastIndex else 0
                isOnboardingCompleted = onboardingDone
                isTooltipShown = tooltipDone
                isBottomBarPulseShown = pulseDone
                isDarkMode = darkMode
                loadData(selectedIndex)
                updateAllCompletionStates()
            }
        }
    }

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveBoolean(SubhaRepository.KEY_DARK_MODE, isDarkMode)
        }
    }

    fun completeOnboarding() {
        isOnboardingCompleted = true
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveBoolean(SubhaRepository.KEY_ONBOARDING_COMPLETED, true)
        }
    }

    fun markTooltipAsShown() {
        isTooltipShown = true
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveBoolean(SubhaRepository.KEY_TOOLTIP_SHOWN, true)
        }
    }

    fun markPulseAsShown() {
        isBottomBarPulseShown = true
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveBoolean(SubhaRepository.KEY_BOTTOM_BAR_PULSE_SHOWN, true)
        }
    }

    fun fetchRemoteAthkar() {
        if (fetchJob?.isActive == true) return
        fetchJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getAthkar()
                if (response.isSuccessful) {
                    val remoteList = response.body()
                    if (!remoteList.isNullOrEmpty()) {
                        withContext(Dispatchers.Main) {
                            dhikrList.clear()
                            dhikrList.addAll(remoteList)
                            if (selectedIndex !in dhikrList.indices) selectedIndex = 0
                            loadData(selectedIndex)
                            updateAllCompletionStates()
                        }
                    }
                }
            } catch (e: Exception) { }
        }
    }

    private fun updateAllCompletionStates() {
        val snapshot = dhikrList.toList()
        viewModelScope.launch(Dispatchers.IO) {
            snapshot.forEachIndexed { index, dhikr ->
                val targetKey = "target_${dhikr.id}"
                val counterKey = "counter_${dhikr.id}"
                val savedTarget: Int = repository.getInt(targetKey, dhikr.count).first()
                val savedCounter: Int = repository.getInt(counterKey, 0).first()
                withContext(Dispatchers.Main) {
                    completedStates[index] = savedCounter >= savedTarget
                }
            }
        }
    }

    fun selectDhikr(index: Int) {
        if (index in dhikrList.indices) {
            selectedIndex = index
            viewModelScope.launch(Dispatchers.IO) {
                repository.saveInt(SubhaRepository.KEY_SELECTED_INDEX, index)
            }
            loadData(index)
        }
    }

    fun loadData(index: Int) {
        if (index !in dhikrList.indices) return
        val dhikr = dhikrList[index]
        loadJob?.cancel()
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            val targetKey = "target_${dhikr.id}"
            val counterKey = "counter_${dhikr.id}"
            val targetValue: Int = repository.getInt(targetKey, dhikr.count).first()
            val counterValue: Int = repository.getInt(counterKey, 0).first()
            withContext(Dispatchers.Main) {
                currentTarget = targetValue
                counter = counterValue
                completedStates[index] = counterValue >= targetValue
            }
        }
    }

    fun increment(context: Context, haptic: HapticFeedback? = null) {
        if (counter < currentTarget) {
            counter++
            if (counter >= currentTarget) {
                completedStates[selectedIndex] = true
                VibrationUtil.longVibrate(context)
            } else {
                VibrationUtil.shortVibrate(context)
            }
            saveCurrentProgress()
        }
    }

    private fun saveCurrentProgress() {
        if (selectedIndex !in dhikrList.indices) return
        val id = dhikrList[selectedIndex].id
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveInt("counter_$id", counter)
        }
    }

    fun updateTarget(newTarget: Int) {
        if (selectedIndex !in dhikrList.indices) return
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
