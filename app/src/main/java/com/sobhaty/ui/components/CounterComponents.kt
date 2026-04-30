package com.sobhaty.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sobhaty.model.ArabicFontFamily
import com.sobhaty.viewmodel.SubhaViewModel
import kotlinx.coroutines.delay

@Composable
fun ColumnScope.CounterCircle(
    counter: Int, 
    target: Int, 
    isVisible: Boolean,
    isFullscreen: Boolean,
    isEditingTarget: Boolean,
    onIncrement: () -> Unit,
    onUpdateTarget: (Int) -> Unit,
    onToggleEditTarget: (Boolean) -> Unit,
    viewModel: SubhaViewModel,
    categoryName: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    val isHideMode = !isVisible
    val isFocusMode = isFullscreen
    
    val dynamicWeight = animateFloatAsState(targetValue = if (isHideMode || isFocusMode) 4f else 1.5f, label = "weight").value
    val animatedFontSize = animateFloatAsState(targetValue = if (isFocusMode) 115f else 100f, label = "font").value
    val hideModeScale = animateFloatAsState(targetValue = if (isHideMode || isFocusMode) 1.15f else 1f, label = "scale").value
    
    val scale by animateFloatAsState(
        targetValue = (if (isPressed) 0.94f else 1f) * hideModeScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "press_scale"
    )

    val progressValue = if (target > 0) counter.toFloat() / target else 0f
    val animatedProgress by animateFloatAsState(targetValue = progressValue, animationSpec = tween(350), label = "progress")

    Box(
        modifier = Modifier.fillMaxWidth().weight(dynamicWeight),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(if (!isHideMode && !isFocusMode) 285.dp else 315.dp).scale(scale),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = if (isPressed) 2.dp else 6.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .clickable(
                        enabled = !isEditingTarget,
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        onClick = onIncrement
                    )
                    .background(
                        Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress }, 
                    modifier = Modifier.size(if (!isHideMode && !isFocusMode) 260.dp else 290.dp),
                    color = if (counter >= target) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedVisibility(visible = !isHideMode) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = counter.toString(), 
                                fontSize = animatedFontSize.sp, 
                                fontWeight = FontWeight.Black, 
                                color = if (counter >= target) Color(0xFF4CAF50) else Color(0xFFA8E6A8),
                                letterSpacing = (-3).sp
                            )
                            
                            if (isEditingTarget) {
                                var textFieldValue by remember { 
                                    mutableStateOf(TextFieldValue(target.toString(), selection = TextRange(0, target.toString().length))) 
                                }
                                
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    TextField(
                                        value = textFieldValue,
                                        onValueChange = { newValue -> 
                                            textFieldValue = newValue.copy(text = newValue.text.filter { it.isDigit() }.take(4))
                                        },
                                        modifier = Modifier.width(100.dp).focusRequester(focusRequester),
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = {
                                            onUpdateTarget(textFieldValue.text.toIntOrNull() ?: 33)
                                            onToggleEditTarget(false)
                                            focusManager.clearFocus()
                                        }),
                                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), unfocusedIndicatorColor = Color.Transparent, cursorColor = MaterialTheme.colorScheme.primary)
                                    )
                                }
                                LaunchedEffect(Unit) { focusRequester.requestFocus() }
                            } else {
                                Text(text = "الهدف $target", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                            }
                        }
                    }
                    
                    AnimatedVisibility(visible = isHideMode && !isFocusMode) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = categoryName, fontSize = 22.sp, fontFamily = ArabicFontFamily, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
        
        VolumeKeyTooltip(viewModel)
    }
}

@Composable
fun BoxScope.VolumeKeyTooltip(viewModel: SubhaViewModel) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel.isOnboardingCompleted, viewModel.isTooltipShown) {
        if (viewModel.isOnboardingCompleted && !viewModel.isTooltipShown) {
            delay(1500)
            isVisible = true
            delay(3000)
            isVisible = false
            viewModel.markTooltipAsShown()
        }
    }
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = Modifier.align(Alignment.TopEnd).padding(top = 20.dp, end = 10.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(12.dp, 12.dp, 2.dp, 12.dp),
            shadowElevation = 6.dp
        ) {
            Text(
                text = "جرّب زر الصوت في هاتفك للعدّ",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
