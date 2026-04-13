package com.sobhaty

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var showEditTarget by remember { mutableStateOf(false) }
    
    val view = LocalView.current
    val window = (context as? Activity)?.window

    // التحكم في وضع ملء الشاشة (Immersive Mode)
    LaunchedEffect(isFullscreen) {
        if (window != null) {
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            if (isFullscreen) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                windowInsetsController.systemBarsBehavior = 
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(if (isFullscreen) PaddingValues(0.dp) else innerPadding)
                .fillMaxSize()
                .clickable(
                    enabled = isFullscreen && !showEditTarget,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { viewModel.increment(context, haptic) }
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. الشريط العلوي
            AnimatedVisibility(
                visible = !isFullscreen,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                TopControls(viewModel = viewModel)
            }

            // 2. بطاقة الذكر
            AnimatedVisibility(
                visible = isCounterVisible && !isFullscreen,
                enter = fadeIn(tween(600)) + expandVertically(tween(600)),
                exit = fadeOut(tween(600)) + shrinkVertically(tween(600))
            ) {
                if (viewModel.selectedIndex < viewModel.dhikrList.size) {
                    DhikrPager(viewModel.dhikrList[viewModel.selectedIndex])
                }
            }

            // 3. العداد الملك
            val currentDhikrName = if (viewModel.selectedIndex < viewModel.dhikrList.size) {
                val name = viewModel.dhikrList[viewModel.selectedIndex].category
                if (name.contains("الصلاة على رسول الله")) "الصلاة على النبي ﷺ" else name
            } else ""

            CounterCircle(
                counter = viewModel.counter,
                target = viewModel.currentTarget,
                isVisible = isCounterVisible,
                isFullscreen = isFullscreen,
                isEditingTarget = showEditTarget,
                onIncrement = { viewModel.increment(context, haptic) },
                onUpdateTarget = { viewModel.updateTarget(it) },
                onToggleEditTarget = { showEditTarget = it },
                categoryName = currentDhikrName
            )

            // 4. أزرار التحكم السفلية
            BottomActions(
                isCounterVisible = isCounterVisible,
                isFullscreen = isFullscreen,
                isEditingTarget = showEditTarget, 
                onToggleVisibility = { isCounterVisible = !isCounterVisible },
                onToggleFullscreen = { isFullscreen = !isFullscreen },
                onReset = { viewModel.reset() },
                onEditTarget = { showEditTarget = !showEditTarget }
            )
        }
    }
}
