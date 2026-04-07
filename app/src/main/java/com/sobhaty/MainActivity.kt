package com.sobhaty

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sobhaty.ui.components.*
import com.sobhaty.ui.theme.SobhatyTheme
import com.sobhaty.viewmodel.SubhaViewModel

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: SubhaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SubhaViewModel(applicationContext) as T
            }
        })[SubhaViewModel::class.java]

        setContent {
            SobhatyTheme { 
                SubhaApp(viewModel) 
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            viewModel.increment(this, null)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun SubhaApp(viewModel: SubhaViewModel) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var isFullscreen by remember { mutableStateOf(false) }
    var isCounterVisible by remember { mutableStateOf(true) }
    
    val view = LocalView.current
    val window = (context as? Activity)?.window

    // Handle Immersive Fullscreen Mode
    LaunchedEffect(isFullscreen) {
        if (window != null) {
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            if (isFullscreen) {
                // Hide status bar and navigation bar
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                windowInsetsController.systemBarsBehavior = 
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                // Show bars again
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(if (isFullscreen) PaddingValues(0.dp) else innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Section (Dhikr Chips)
            AnimatedVisibility(
                visible = !isFullscreen,
                enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                exit = fadeOut(tween(400)) + shrinkVertically(tween(400))
            ) {
                TopControls(viewModel)
            }

            // 2. Dhikr Content (Pager: Dhikr, Verse, Hadith)
            AnimatedVisibility(
                visible = isCounterVisible,
                enter = fadeIn(tween(600)) + expandVertically(tween(600)),
                exit = fadeOut(tween(600)) + shrinkVertically(tween(600))
            ) {
                DhikrPager(viewModel.dhikrList[viewModel.selectedIndex])
            }

            // 3. Central Counter
            CounterCircle(
                counter = viewModel.counter,
                target = viewModel.currentTarget,
                isVisible = isCounterVisible,
                isFullscreen = isFullscreen,
                onIncrement = { viewModel.increment(context, haptic) }
            )

            // 4. Bottom Navigation Labels
            BottomActions(
                isCounterVisible = isCounterVisible,
                isFullscreen = isFullscreen,
                onToggleVisibility = { isCounterVisible = !isCounterVisible },
                onToggleFullscreen = { isFullscreen = !isFullscreen },
                onReset = { viewModel.reset() }
            )
        }
    }
}
