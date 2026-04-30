package com.sobhaty.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sobhaty.viewmodel.SubhaViewModel
import kotlinx.coroutines.delay

@Composable
fun TopControls(viewModel: SubhaViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // زر تبديل الوضع المظلم/الفاتح
            IconButton(
                onClick = { viewModel.toggleDarkMode() },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = if (viewModel.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Mode",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))

            LazyRow(
                modifier = Modifier.weight(1f),
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
}

@Composable
fun BottomActions(
    isCounterVisible: Boolean, 
    isFullscreen: Boolean, 
    isEditingTarget: Boolean,
    viewModel: SubhaViewModel,
    onToggleVisibility: () -> Unit, 
    onToggleFullscreen: () -> Unit, 
    onReset: () -> Unit,
    onEditTarget: () -> Unit
) {
    var showPulse by remember { mutableStateOf(false) }
    
    LaunchedEffect(viewModel.isOnboardingCompleted, viewModel.isBottomBarPulseShown) {
        if (viewModel.isOnboardingCompleted && !viewModel.isBottomBarPulseShown) {
            delay(1500)
            showPulse = true
            delay(5000)
            showPulse = false
            viewModel.markPulseAsShown()
        }
    }

    Column {
        AnimatedVisibility(visible = !isFullscreen) {
            HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        }
        Row(
            modifier = Modifier.padding(bottom = if (isFullscreen) 40.dp else 24.dp).fillMaxWidth().padding(vertical = 12.dp), 
            horizontalArrangement = Arrangement.SpaceEvenly, 
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = !isFullscreen) {
                BottomActionItem(icon = if (isCounterVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, label = if (isCounterVisible) "إخفاء" else "إظهار", onClick = onToggleVisibility)
            }
            AnimatedVisibility(visible = !isFullscreen) {
                BottomActionItem(icon = Icons.Default.Edit, label = "الهدف", onClick = onEditTarget, isHighlight = isEditingTarget)
            }
            BottomActionItem(
                icon = if (isFullscreen) Icons.Default.CloseFullscreen else Icons.Default.Fullscreen, 
                label = if (isFullscreen) "خروج" else "ملء", 
                onClick = onToggleFullscreen, 
                isHighlight = isFullscreen,
                hasPulse = showPulse
            )
            AnimatedVisibility(visible = !isFullscreen) {
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
    isHighlight: Boolean = false,
    hasPulse: Boolean = false
) {
    val bgColor by animateColorAsState(if (isHighlight) MaterialTheme.colorScheme.primary else Color.Transparent, label = "bgColor")
    val contentColor by animateColorAsState(if (isHighlight) Color.White else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), label = "contentColor")
    val borderColor by animateColorAsState(if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), label = "borderColor")

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(contentAlignment = Alignment.Center) {
        if (hasPulse) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(pulseScale)
                    .alpha(pulseAlpha)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        }

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
}
