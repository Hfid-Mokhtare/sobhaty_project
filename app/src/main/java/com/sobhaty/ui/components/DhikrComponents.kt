package com.sobhaty.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sobhaty.model.ArabicFontFamily
import com.sobhaty.model.Thikr

@Composable
fun DhikrPager(dhikr: Thikr) {
    val pages = mutableListOf<@Composable () -> Unit>()
    pages.add { DhikrContentBox(text = dhikr.text) }
    dhikr.verses?.forEach { verse ->
        val ayahStr = verse.displayAyahNumber
        val ayahInt = ayahStr.toDoubleOrNull()?.toInt()?.toString() ?: ayahStr
        pages.add { DhikrContentBox(text = verse.text, subText = "${verse.surah} - $ayahInt") }
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
                    // استخدام background مع Shape مباشرة لتجنب استخدام clip
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .width(width)
                            .height(6.dp)
                            .background(color, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun DhikrContentBox(text: String, subText: String? = null) {
    // استخدام Surface بدلاً من Box مع Modifier.clip لحل مشكلة المراجع
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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
}
