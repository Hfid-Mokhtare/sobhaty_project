package com.sobhaty

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sobhaty.ui.components.BottomActions
import com.sobhaty.ui.components.CounterCircle
import com.sobhaty.ui.components.DhikrDisplay
import com.sobhaty.ui.components.TopControls
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

    LaunchedEffect(viewModel.selectedIndex) {
        viewModel.loadData(viewModel.selectedIndex)
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedVisibility(
                visible = !isFullscreen,
                enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                exit = fadeOut(tween(400)) + shrinkVertically(tween(400))
            ) {
                TopControls(viewModel)
            }

            AnimatedVisibility(
                visible = isCounterVisible,
                enter = fadeIn(tween(600)) + expandVertically(tween(600)),
                exit = fadeOut(tween(600)) + shrinkVertically(tween(600))
            ) {
                DhikrDisplay(viewModel.dhikrList[viewModel.selectedIndex].fullText)
            }

            CounterCircle(
                counter = viewModel.counter,
                target = viewModel.currentTarget,
                isVisible = isCounterVisible,
                onIncrement = { viewModel.increment(context, haptic) }
            )

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
