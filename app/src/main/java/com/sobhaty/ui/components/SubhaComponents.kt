package com.sobhaty.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sobhaty.model.ArabicFontFamily
import com.sobhaty.viewmodel.SubhaViewModel

@Composable
fun ColumnScope.TopControls(viewModel: SubhaViewModel) {
    var showDropdown by remember { mutableStateOf(false) }
    var showEditTarget by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 16.dp)) {
        Box {
            OutlinedButton(onClick = { showDropdown = true }) {
                Text(viewModel.dhikrList[viewModel.selectedIndex].name)
                if (viewModel.completedStates[viewModel.selectedIndex] == true) {
                    Icon(
                        Icons.Default.CheckCircle, 
                        null, 
                        tint = Color(0xFF4CAF50), 
                        modifier = Modifier.padding(start = 8.dp).size(20.dp)
                    )
                }
                Icon(Icons.Default.ArrowDropDown, null)
            }
            DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
                viewModel.dhikrList.forEachIndexed { index, dhikr ->
                    DropdownMenuItem(
                        text = { Text(dhikr.name) },
                        onClick = {
                            viewModel.selectedIndex = index
                            showDropdown = false
                        }
                    )
                }
            }
        }
        
        if (!showEditTarget) {
            TextButton(onClick = { showEditTarget = true }) {
                Text("الهدف: ${viewModel.currentTarget}")
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                var input by remember { mutableStateOf(viewModel.currentTarget.toString()) }
                OutlinedTextField(
                    value = input,
                    onValueChange = { newValue -> input = newValue.filter { c -> c.isDigit() } },
                    modifier = Modifier.width(100.dp).focusRequester(focusRequester),
                    singleLine = true,
                    label = { Text("الهدف") }
                )
                IconButton(onClick = {
                    viewModel.updateTarget(input.toIntOrNull() ?: 33)
                    showEditTarget = false
                }) { Icon(Icons.Default.Save, null) }
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
            }
        }
    }
}

@Composable
fun ColumnScope.DhikrDisplay(text: String) {
    Box(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp)
    ) {
        Text(
            text = text, 
            fontSize = 18.sp, 
            fontFamily = ArabicFontFamily,
            textAlign = TextAlign.Center, 
            lineHeight = 28.sp, 
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ColumnScope.CounterCircle(
    counter: Int, 
    target: Int, 
    isVisible: Boolean, 
    onIncrement: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = if (target > 0) counter.toFloat() / target else 0f, 
        label = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, 
                indication = null
            ) { onIncrement() },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress }, 
            modifier = Modifier.size(260.dp),
            color = if (counter >= target) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp, 
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        AnimatedContent(
            targetState = isVisible,
            transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(600)) },
            label = "counter_anim"
        ) { visible ->
            if (visible) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = counter.toString(), 
                        fontSize = 80.sp, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = if (counter >= target) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (counter >= target) "تم الإنجاز!" else "من $target", 
                        fontSize = 20.sp, 
                        color = if (counter >= target) Color(0xFF4CAF50) else MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                Text("انقر للتسبيح", color = MaterialTheme.colorScheme.outline, fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun ColumnScope.BottomActions(
    isCounterVisible: Boolean, 
    isFullscreen: Boolean, 
    onToggleVisibility: () -> Unit, 
    onToggleFullscreen: () -> Unit, 
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth(), 
        horizontalArrangement = Arrangement.SpaceEvenly, 
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggleVisibility) {
            Icon(
                if (isCounterVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, 
                null, 
                modifier = Modifier.size(28.dp)
            )
        }
        IconButton(onClick = onToggleFullscreen) {
            Icon(
                if (isFullscreen) Icons.Default.Close else Icons.Default.Fullscreen, 
                null, 
                modifier = Modifier.size(28.dp)
            )
        }
        AnimatedVisibility(
            visible = !isFullscreen,
            enter = fadeIn(tween(400)) + expandHorizontally(tween(400)),
            exit = fadeOut(tween(400)) + shrinkHorizontally(tween(400))
        ) {
            IconButton(onClick = onReset) { 
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(28.dp)) 
            }
        }
    }
}
