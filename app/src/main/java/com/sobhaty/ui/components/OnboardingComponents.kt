package com.sobhaty.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sobhaty.model.ArabicFontFamily
import com.sobhaty.viewmodel.SubhaViewModel

@Composable
fun OnboardingOverlay(viewModel: SubhaViewModel) {
    if (viewModel.isOnboardingCompleted) return
    
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.98f))
        .clickable(enabled = true, onClick = {}) 
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPage(page)
            }
            
            Row(
                Modifier.padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val width by animateDpAsState(targetValue = if (isSelected) 24.dp else 8.dp, label = "")
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f))
                            .width(width)
                            .height(8.dp)
                    )
                }
            }

            Button(
                onClick = { viewModel.completeOnboarding() },
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .height(56.dp)
                    .width(220.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (pagerState.currentPage == 2) "فهمت، ابدأ" else "التالي",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = ArabicFontFamily
                )
            }
        }
    }
}

@Composable
fun OnboardingPage(page: Int) {
    when (page) {
        0 -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                val introText = buildAnnotatedString {
                    append("الكيمياء الإلهية، والدواء والعلاج التي بها يطهر القلب، وهو مصب الإيمان وملتقى شعبه ومصدر نوره هو ذكر الله. قال الله تعالى: ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                        append("﴿أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ﴾")
                    }
                    append(" (الرعد، 28).")
                }

                Text(
                    text = introText,
                    fontSize = 19.sp,
                    fontFamily = ArabicFontFamily,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "المنهاج النبوي ص 146\nعبد السلام ياسين رحمه الله",
                    fontSize = 11.sp,
                    fontFamily = ArabicFontFamily,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontStyle = FontStyle.Italic,
                    lineHeight = 18.sp
                )
            }
        }
        1 -> {
            OnboardingStep(
                icon = Icons.Default.VolumeUp,
                title = "العدّ بأزرار الصوت",
                description = "يمكنك استخدام أزرار رفع وخفض الصوت في هاتفك للعدّ أثناء التسبيح دون الحاجة للمس الشاشة"
            )
        }
        else -> {
            OnboardingStep(
                icon = Icons.Default.Fullscreen,
                title = "وضع التركيز الكامل",
                description = "اضغط على زر ملء الشاشة لتجربة انغماس كاملة؛ حيث يختفي كل شيء ليبقى العداد وحده ملكاً للشاشة"
            )
        }
    }
}

@Composable
fun OnboardingStep(icon: ImageVector, title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(90.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            fontFamily = ArabicFontFamily,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = description,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            lineHeight = 24.sp,
            fontFamily = ArabicFontFamily
        )
    }
}
