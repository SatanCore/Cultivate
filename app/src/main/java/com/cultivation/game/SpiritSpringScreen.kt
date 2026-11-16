package com.cultivation.game

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SpringIce = Color(0xFF86DCEB)
private val SpringGold = Color(0xFFE9CE91)
private val SpringPanel = Color(0xE9162A3B)

@Composable
internal fun SpiritSpringScreen(
    abodeLevel: Int,
    spiritWater: Int,
    progress: Float,
    timeRemaining: String,
    ready: Boolean,
    collectAmount: Int,
    onCollect: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "spirit-spring")
    val shimmer by transition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(tween(2100), RepeatMode.Reverse),
        label = "spring-shimmer"
    )
    val safeProgress = progress.coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SpringPanel,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SpringIce.copy(alpha = .55f))
        ) {
            Column(modifier = Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("ИСТОЧНИК ЧИСТОЙ ЦИ", color = SpringIce, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
                        Text("Горный родник", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
                        Text("Вода стекает по древним камням, насыщаясь энергией духовных жил.", color = Color(0xFFC6D9E4), fontSize = 10.sp, lineHeight = 14.sp)
                    }
                    Surface(
                        color = Color(0xFF183A4B),
                        shape = RoundedCornerShape(11.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SpringIce.copy(alpha = .7f))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("УР.", color = SpringGold, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            Text("$abodeLevel", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(166.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF152B3D))) {
                    Image(
                        painter = painterResource(R.drawable.abode_spring_scene),
                        contentDescription = "Иллюстрация горного духовного родника",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier.matchParentSize().background(
                            Brush.verticalGradient(listOf(Color(0x44101E35), Color(0x11101E35), Color(0xBB102333)))
                        )
                    )
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd).padding(9.dp)
                            .clip(RoundedCornerShape(10.dp)).background(Color(0xD9112938))
                            .border(1.dp, SpringIce.copy(alpha = .55f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                    ) {
                        Text("ДУХОВНЫЙ ИСТОЧНИК", color = SpringIce, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                    Box(
                        modifier = Modifier.align(Alignment.BottomStart).padding(9.dp)
                            .clip(RoundedCornerShape(10.dp)).background(Color(0xD9112938))
                            .border(1.dp, SpringGold.copy(alpha = .45f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                    ) {
                        Text("ЧИСТОТА ВОДЫ  ·  ${(safeProgress * 100).toInt()}%", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier.align(Alignment.Center).size(72.dp)
                            .background(Color(0x2286DCEB), RoundedCornerShape(36.dp))
                            .border(1.dp, SpringIce.copy(alpha = shimmer), RoundedCornerShape(36.dp))
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SpringPanel,
            shape = RoundedCornerShape(15.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SpringIce.copy(alpha = .35f))
        ) {
            Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("ЗАПАС ДУХОВНОЙ ВОДЫ", color = SpringIce, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        Text("$spiritWater порций в хранилище", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("+$collectAmount", color = SpringGold, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
                LinearProgressIndicator(
                    progress = { if (ready) 1f else safeProgress },
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(8.dp)),
                    color = SpringIce,
                    trackColor = Color(0xFF344A59)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (ready) "Источник наполнен" else "Вода собирается: $timeRemaining", color = if (ready) SpringGold else Color(0xFFC4D7E1), fontSize = 10.sp, modifier = Modifier.weight(1f))
                    Text(if (ready) "ГОТОВО" else "ВОССТАНОВЛЕНИЕ", color = SpringIce, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
                Button(
                    onClick = onCollect,
                    enabled = ready,
                    modifier = Modifier.fillMaxWidth().height(47.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF286D7B),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF344A59),
                        disabledContentColor = Color(0xFF9CB3BD)
                    )
                ) {
                    Text(if (ready) "НАБРАТЬ ДУХОВНУЮ ВОДУ  ·  +$collectAmount" else "РОДНИК НАПОЛНЯЕТСЯ", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
