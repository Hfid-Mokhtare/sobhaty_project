package com.sobhaty.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    val pagerState = rememberPagerState(pageCount = { 4 })
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background.copy(alpha = 0.98f)).clickable(enabled = true, onClick = {})) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page -> OnboardingPage(page) }
            Row(Modifier.padding(bottom = 32.dp), horizontalArrangement = Arrangement.Center) {
                repeat(4) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val width by animateDpAsState(targetValue = if (isSelected) 24.dp else 8.dp, label = "")
                    Box(modifier = Modifier.padding(horizontal = 4.dp).clip(CircleShape).background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)).width(width).height(8.dp))
                }
            }
            Button(onClick = { viewModel.completeOnboarding() }, modifier = Modifier.padding(bottom = 64.dp).height(56.dp).width(220.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Text(text = if (pagerState.currentPage == 3) "فهمت، ابدأ" else "تخطى الشرح", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun OnboardingPage(page: Int) {
    if (page == 0) {
        Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(32.dp))
            val introText = buildAnnotatedString {
                append("الكيمياء الإلهية، والدواء والعلاج التي بها يطهر القلب، وهو مصب الإيمان وملتقى شعبه ومصدر نوره هو ذكر الله. قال الله تعالى:")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) { append("\n﴿أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ﴾") }
                append("\n(الرعد، 28)")
            }
            Text(text = introText, fontSize = 19.sp, fontFamily = ArabicFontFamily, textAlign = TextAlign.Center, lineHeight = 34.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "المنهاج النبوي تربية وتنظيماً وزحفاً، ص 146\nعبد السلام ياسين رحمه الله", fontSize = 11.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), fontStyle = FontStyle.Italic, lineHeight = 18.sp)
        }
    } else {
        val p = page - 1
        val data = when (p) {
            0 -> Triple(Icons.Default.TouchApp, "اضغط على الدائرة للعدّ", "استخدم الدائرة الكبيرة في المنتصف لزيادة العداد بسهولة")
            1 -> Triple(Icons.Default.VolumeUp, "العدّ بأزرار الصوت", "يمكنك استخدام أزرار رفع وخفض الصوت في هاتفك للعدّ أثناء التسبيح")
            else -> Triple(Icons.Default.Fullscreen, "وضع التركيز الكامل", "اضغط على زر ملء الشاشة لتجربة انغماس كاملة بعيداً عن المشتتات")
        }
        Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(180.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(imageVector = data.first, contentDescription = null, modifier = Modifier.size(90.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(48.dp))
            Text(text = data.second, fontSize = 22.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = data.third, fontSize = 15.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), lineHeight = 24.sp, fontFamily = ArabicFontFamily)
        }
    }
}
