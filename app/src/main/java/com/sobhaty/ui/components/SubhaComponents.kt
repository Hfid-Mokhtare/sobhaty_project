package com.sobhaty.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.sobhaty.model.Thikr
import com.sobhaty.viewmodel.SubhaViewModel

@Composable
fun TopControls(
    viewModel: SubhaViewModel
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 16.dp)) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(viewModel.dhikrList) { index, dhikr ->
                val isSelected = viewModel.selectedIndex == index
                val displayCategory = if (dhikr.category.contains("الصلاة على رسول الله")) "الصلاة على النبي ﷺ" else dhikr.category
                
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectDhikr(index) },
                    label = { Text(displayCategory, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    leadingIcon = if (viewModel.completedStates[index] == true) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(12.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    border = null,
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }
    }
}

@Composable
fun DhikrPager(dhikr: Thikr) {
    val pages = mutableListOf<@Composable () -> Unit>()
    pages.add { DhikrContentBox(text = dhikr.text) }
    dhikr.verses?.forEach { verse ->
        pages.add { DhikrContentBox(text = verse.text, subText = "${verse.surah} - ${verse.ayahNumber}") }
    }
    dhikr.hadith?.let { hadith ->
        pages.add { DhikrContentBox(text = hadith.text, subText = "حديث شريف - المصدر: ${hadith.source}") }
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 32.dp), 
            pageSpacing = 16.dp
        ) { pageIndex -> pages[pageIndex]() }
        
        if (pages.size > 1) {
            Row(
                Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val width by animateDpAsState(targetValue = if (isSelected) 18.dp else 6.dp, label = "")
                    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    Box(modifier = Modifier.padding(horizontal = 3.dp).clip(CircleShape).background(color).width(width).height(6.dp))
                }
            }
        }
    }
}

@Composable
fun DhikrContentBox(text: String, subText: String? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .shadow(0.5.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color.Transparent, spotColor = Color.Transparent)
            .border(BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)), shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Transparent)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text, 
                fontSize = 17.sp, 
                fontFamily = ArabicFontFamily,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center, 
                lineHeight = 26.sp, 
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
            
            if (subText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subText, 
                    fontSize = 10.sp, 
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), 
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            }
        }
    }
}

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
        modifier = Modifier
            .fillMaxWidth()
            .weight(dynamicWeight),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .size(if (!isHideMode && !isFocusMode) 285.dp else 315.dp)
                .scale(scale),
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
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        )
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
                    AnimatedVisibility(
                        visible = !isHideMode,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
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
                                        modifier = Modifier
                                            .width(100.dp)
                                            .focusRequester(focusRequester),
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(
                                            fontSize = 20.sp, 
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        ),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                onUpdateTarget(textFieldValue.text.toIntOrNull() ?: 33)
                                                onToggleEditTarget(false)
                                                focusManager.clearFocus()
                                            }
                                        ),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            unfocusedIndicatorColor = Color.Transparent,
                                            cursorColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                                LaunchedEffect(Unit) { focusRequester.requestFocus() }
                            } else {
                                Text(
                                    text = "الهدف $target", 
                                    fontSize = 14.sp, 
                                    fontWeight = FontWeight.Medium, 
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                    
                    AnimatedVisibility(
                        visible = isHideMode && !isFocusMode,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = categoryName, 
                                fontSize = 22.sp, 
                                fontFamily = ArabicFontFamily, 
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomActions(
    isCounterVisible: Boolean, 
    isFullscreen: Boolean, 
    isEditingTarget: Boolean,
    onToggleVisibility: () -> Unit, 
    onToggleFullscreen: () -> Unit, 
    onReset: () -> Unit,
    onEditTarget: () -> Unit
) {
    Column {
        AnimatedVisibility(visible = !isFullscreen, enter = fadeIn(), exit = fadeOut()) {
            HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        }
        
        Row(
            modifier = Modifier.padding(bottom = if (isFullscreen) 40.dp else 24.dp).fillMaxWidth().background(Color.Transparent).padding(vertical = 12.dp), 
            horizontalArrangement = Arrangement.SpaceEvenly, 
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = !isFullscreen, enter = fadeIn() + expandHorizontally(), exit = fadeOut() + shrinkHorizontally()) {
                BottomActionItem(icon = if (isCounterVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, label = if (isCounterVisible) "إخفاء" else "إظهار", onClick = onToggleVisibility)
            }

            AnimatedVisibility(visible = !isFullscreen, enter = fadeIn() + expandHorizontally(), exit = fadeOut() + shrinkHorizontally()) {
                BottomActionItem(
                    icon = Icons.Default.Edit, 
                    label = "الهدف", 
                    onClick = onEditTarget,
                    isHighlight = isEditingTarget
                )
            }

            BottomActionItem(
                icon = if (isFullscreen) Icons.Default.CloseFullscreen else Icons.Default.Fullscreen, 
                label = if (isFullscreen) "خروج" else "ملء", 
                onClick = onToggleFullscreen,
                isHighlight = isFullscreen
            )

            AnimatedVisibility(visible = !isFullscreen, enter = fadeIn() + expandHorizontally(), exit = fadeOut() + shrinkHorizontally()) {
                BottomActionItem(icon = Icons.Default.Refresh, label = "تصفير", onClick = onReset)
            }
        }
    }
}

@Composable
fun BottomActionItem(
    icon: ImageVector, 
    label: String, 
    onClick: () -> Unit,
    isHighlight: Boolean = false
) {
    val targetBgColor = if (isHighlight) MaterialTheme.colorScheme.primary else Color.Transparent
    val targetContentColor = if (isHighlight) Color.White else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    val targetBorderColor = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

    val bgColor by animateColorAsState(targetValue = targetBgColor, label = "bg")
    val contentColor by animateColorAsState(targetValue = targetContentColor, label = "content")
    val borderColor by animateColorAsState(targetValue = targetBorderColor, label = "border")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, 
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .border(0.5.dp, borderColor, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = contentColor)
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
    }
}
