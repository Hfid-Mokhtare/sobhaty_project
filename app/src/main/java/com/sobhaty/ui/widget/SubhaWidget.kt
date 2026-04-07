package com.sobhaty.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.sobhaty.MainActivity
import com.sobhaty.repository.SubhaRepository

class SubhaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = SubhaRepository(context)
        
        provideContent {
            // Get the current selected index to show relevant data in the widget
            val selectedIndex by repository.getInt(SubhaRepository.KEY_SELECTED_INDEX, 0).collectAsState(initial = 0)
            
            // Fetch counter and target for the selected dhikr
            val counter by repository.getInt("counter_$selectedIndex", 0).collectAsState(initial = 0)
            val target by repository.getInt("target_$selectedIndex", 33).collectAsState(initial = 33)

            SubhaWidgetContent(counter, target)
        }
    }

    @Composable
    private fun SubhaWidgetContent(counter: Int, target: Int) {
        val propheticGreen = Color(0xFF2E7D32)
        val white = Color.White
        
        val backgroundProvider = ColorProvider(day = propheticGreen, night = propheticGreen)
        val textProvider = ColorProvider(day = white, night = white)
        val semiTextProvider = ColorProvider(day = white.copy(alpha = 0.7f), night = white.copy(alpha = 0.7f))
        
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundProvider)
                .padding(8.dp)
                .clickable(actionStartActivity<MainActivity>()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "سبحتي",
                style = TextStyle(
                    color = textProvider,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = counter.toString(),
                style = TextStyle(
                    color = textProvider,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "من $target",
                style = TextStyle(
                    color = semiTextProvider,
                    fontSize = 12.sp
                )
            )
        }
    }
}
