package com.sobhaty

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sobhaty.ui.theme.SobhatyTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// DataStore setup
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "subha_prefs")

data class Dhikr(val id: Int, val name: String, val fullText: String, val defaultTarget: Int)

// Font Provider for Google Fonts
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Amiri font gives a beautiful Uthmanic/Calligraphic feel
val AmiriFont = GoogleFont("Amiri")
val ArabicFontFamily = FontFamily(
    Font(googleFont = AmiriFont, fontProvider = provider)
)

class MainActivity : ComponentActivity() {
    
    private var onVolumeUpPressed: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SobhatyTheme {
                SubhaApp(
                    setVolumeUpCallback = { callback ->
                        onVolumeUpPressed = callback
                    }
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            onVolumeUpPressed?.invoke()
            return true 
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun SubhaApp(setVolumeUpCallback: (() -> Unit) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    
    val dhikrList = remember {
        listOf(
            Dhikr(0, "الإستغفار", "رَبِّ اغْفِرْ لِي وَ تُبْ عَلَيَّ إِنَّكَ أَنْتَ التَّوَّابُ الرَّحِيمُ", 100),
            Dhikr(1, "المكيال الأوفى", "اللَّهُمَّ صَلِّ عَلَى سَيِّدِنَا مُحَمَّدٍ النَّبِيِّ وَ عَلَى أَزْوَاجِهِ أُمَّهَاتِ الْمُؤْمِنِينَ وَ عَلَى ذُرِّيَّتِهِ وَ أَهْلِ بَيْتِهِ كَمَا صَلَّيْتَ عَلَى آلِ سَيِّدِنَا إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ", 10),
            Dhikr(2, "الكلمة الطيبة", "لَا إِلَهَ إِلَّا اللهُ", 100),
            Dhikr(3, "التسبيح", "سُبْحَانَ اللهِ وَ بِحَمْدِهِ", 100),
            Dhikr(4, "أدعية التحصين", "لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَ لَهُ الْحَمْدُ وَ هُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ", 100)
        )
    }

    var selectedIndex by remember { mutableStateOf(0) }
    var counter by remember { mutableStateOf(0) }
    var isCounterVisible by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }
    var showDropdown by remember { mutableStateOf(false) }
    
    val completedDhikrs = remember { mutableStateMapOf<Int, Boolean>() }
    val targetCounts = remember { mutableStateMapOf<Int, Int>() }
    val savedCounters = remember { mutableStateMapOf<Int, Int>() }
    
    LaunchedEffect(Unit) {
        dhikrList.forEach { dhikr ->
            val targetKey = intPreferencesKey("target_${dhikr.id}")
            val counterKey = intPreferencesKey("counter_${dhikr.id}")
            
            val preferences = context.dataStore.data.first()
            
            val savedTarget = preferences[targetKey] ?: dhikr.defaultTarget
            val savedCount = preferences[counterKey] ?: 0
            
            targetCounts[dhikr.id] = savedTarget
            savedCounters[dhikr.id] = savedCount
            
            if (savedCount >= savedTarget) {
                completedDhikrs[dhikr.id] = true
            }
        }
        counter = savedCounters[dhikrList[selectedIndex].id] ?: 0
    }
    
    val currentDhikr = dhikrList[selectedIndex]
    val currentTarget = targetCounts[currentDhikr.id] ?: currentDhikr.defaultTarget

    val incrementCounter = {
        if (counter < currentTarget) {
            counter++
            val newCount = counter
            savedCounters[currentDhikr.id] = newCount
            
            scope.launch {
                val key = intPreferencesKey("counter_${currentDhikr.id}")
                context.dataStore.edit { preferences ->
                    preferences[key] = newCount
                }
            }

            if (newCount == currentTarget) {
                completedDhikrs[currentDhikr.id] = true
                performLongVibration(context)
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    LaunchedEffect(currentDhikr, counter, currentTarget) {
        setVolumeUpCallback {
            incrementCounter()
        }
    }

    var showEditTarget by remember { mutableStateOf(false) }
    var targetInput by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val progress by animateFloatAsState(
        targetValue = if (currentTarget > 0) counter.toFloat() / currentTarget else 0f,
        label = "progress"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section
            if (!isFullscreen) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        OutlinedButton(onClick = { showDropdown = true }) {
                            Text(text = currentDhikr.name, fontSize = 18.sp)
                            if (completedDhikrs[currentDhikr.id] == true) {
                                Icon(
                                    Icons.Default.CheckCircle, 
                                    contentDescription = null, 
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.padding(start = 8.dp).size(20.dp)
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }
                        ) {
                            dhikrList.forEachIndexed { index, dhikr ->
                                DropdownMenuItem(
                                    text = { 
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(dhikr.name)
                                            if (completedDhikrs[dhikr.id] == true) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(
                                                    Icons.Default.Check, 
                                                    contentDescription = null, 
                                                    tint = Color(0xFF4CAF50)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedIndex = index
                                        counter = savedCounters[dhikr.id] ?: 0
                                        showDropdown = false
                                        showEditTarget = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!showEditTarget) {
                        TextButton(onClick = { 
                            targetInput = currentTarget.toString()
                            showEditTarget = true 
                        }) {
                            Text("تعديل الهدف الحالي: $currentTarget")
                        }
                    } else {
                        LaunchedEffect(showEditTarget) {
                            if (showEditTarget) focusRequester.requestFocus()
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            OutlinedTextField(
                                value = targetInput,
                                onValueChange = { targetInput = it.filter { char -> char.isDigit() } },
                                label = { Text("الهدف الجديد") },
                                modifier = Modifier.width(120.dp).focusRequester(focusRequester),
                                singleLine = true
                            )
                            IconButton(onClick = {
                                val newTarget = targetInput.toIntOrNull() ?: currentTarget
                                targetCounts[currentDhikr.id] = newTarget
                                scope.launch {
                                    val key = intPreferencesKey("target_${currentDhikr.id}")
                                    context.dataStore.edit { preferences -> preferences[key] = newTarget }
                                }
                                showEditTarget = false
                                if (counter < newTarget) completedDhikrs[currentDhikr.id] = false
                            }) {
                                Icon(Icons.Default.Save, contentDescription = "حفظ")
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(50.dp))
            }

            // Dhikr Full Text with Beautiful Arabic Font
            AnimatedVisibility(
                visible = isCounterVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = currentDhikr.fullText,
                    fontSize = 32.sp, // Larger font size for better calligraphic appearance
                    fontFamily = ArabicFontFamily, 
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 48.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .fillMaxWidth()
                )
            }

            // Counter in the center
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null 
                    ) {
                        incrementCounter()
                    },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(280.dp),
                    color = if (counter >= currentTarget) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp, 
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                if (isCounterVisible) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = counter.toString(),
                            fontSize = 100.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (counter >= currentTarget) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (counter >= currentTarget) "تم الإنجاز!" else "من $currentTarget",
                            fontSize = 24.sp,
                            color = if (counter >= currentTarget) Color(0xFF4CAF50) else MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    Text(
                        "انقر للتسبيح",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Bottom Buttons
            Row(
                modifier = Modifier.padding(bottom = 48.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isCounterVisible = !isCounterVisible }) {
                    Icon(
                        imageVector = if (isCounterVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "إخفاء العداد",
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(onClick = { isFullscreen = !isFullscreen }) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Default.Close else Icons.Default.Fullscreen,
                        contentDescription = "ملئ الشاشة",
                        modifier = Modifier.size(32.dp)
                    )
                }

                if (!isFullscreen) {
                    IconButton(onClick = { 
                        counter = 0 
                        savedCounters[currentDhikr.id] = 0
                        completedDhikrs[currentDhikr.id] = false
                        scope.launch {
                            val key = intPreferencesKey("counter_${currentDhikr.id}")
                            context.dataStore.edit { preferences ->
                                preferences[key] = 0
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "إعادة العداد",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

fun performLongVibration(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(500)
    }
}
