package com.sobhaty.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sobhaty.model.ArabicFontFamily
import com.sobhaty.viewmodel.SubhaViewModel

@Composable
fun ColumnScope.TopControls(viewModel: SubhaViewModel) {
    var showEditTarget by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 16.dp)) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(viewModel.dhikrList) { index, dhikr ->
                val isSelected = viewModel.selectedIndex == index
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectDhikr(index) },
                    label = { Text(dhikr.name) },
                    leadingIcon = if (viewModel.completedStates[index] == true) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        if (!showEditTarget) {
            TextButton(onClick = { showEditTarget = true }) {
                Text("الهدف: ${viewModel.currentTarget}", color = MaterialTheme.colorScheme.primary)
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
    isFullscreen: Boolean,
    onIncrement: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale_anim"
    )

    val progressValue = if (target > 0) counter.toFloat() / target else 0f
    val progress by animateFloatAsState(
        targetValue = progressValue, 
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // الشريط الخطي والنسبة المئوية (يظهر إذا كان العداد مرئياً أو في وضع ملء الشاشة)
        if (isVisible || isFullscreen) {
            Row(
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (counter >= target) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${(progressValue * 100).toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (counter >= target) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(scale)
                .shadow(elevation = 8.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    onClick = onIncrement
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress }, 
                modifier = Modifier.size(260.dp),
                color = if (counter >= target) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                strokeWidth = 6.dp, 
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp), 
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                }
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
