package com.cultivation.game

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GardenGold = Color(0xFFE9CB8A)
private val GardenJade = Color(0xFF9EDB91)
private val GardenDeep = Color(0xFF102235)
private val GardenPanel = Color(0xE91A2B3B)

/**
 * Illustrated cultivation garden. Harvest timing and rewards remain owned by
 * CultivateApp; this screen only presents the existing state and action.
 */
@Composable
internal fun SpiritGardenScreen(
    abodeLevel: Int,
    spiritHerbs: Int,
    growth: Float,
    timeRemaining: String,
    ready: Boolean,
    harvestAmount: Int,
    onHarvest: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "spirit-garden")
    val breeze by transition.animateFloat(
        initialValue = -0.035f,
        targetValue = 0.035f,
        animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse),
        label = "garden-breeze"
    )
    val glow by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(tween(1700), RepeatMode.Reverse),
        label = "garden-glow"
    )
    val progress = growth.coerceIn(0f, 1f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = GardenPanel,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GardenJade.copy(alpha = .62f))
        ) {
            Column(modifier = Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("СВЯТИЛИЩЕ ЖИВЫХ ТРАВ", color = GardenJade, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
                        Text("Сад духовных трав", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
                        Text("Грядки впитывают Ци обители и выращивают ингредиенты для алхимической печи.", color = Color(0xFFC8D7E3), fontSize = 10.sp, lineHeight = 14.sp)
                    }
                    Box(
                        modifier = Modifier.size(58.dp).clip(RoundedCornerShape(15.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF284B42), Color(0xFF142D37))))
                            .border(1.dp, GardenJade.copy(alpha = .7f), RoundedCornerShape(15.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.alchemy_herb_bundle),
                            contentDescription = "Связка духовных трав",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(49.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth().height(158.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFF1B3B45), Color(0xFF182F35), Color(0xFF263B32))))
                ) {
                    Canvas(Modifier.matchParentSize()) {
                        val w = size.width
                        val h = size.height
                        drawCircle(Color(0xFFFFD98D).copy(alpha = .10f + glow * .09f), h * .34f, Offset(w * .78f, h * .22f))
                        drawCircle(Color(0xFFFFD98D).copy(alpha = .18f + glow * .22f), h * .23f, Offset(w * .78f, h * .22f), style = Stroke(width = 1.4.dp.toPx()))
                        drawPath(
                            Path().apply {
                                moveTo(0f, h * .72f)
                                cubicTo(w * .16f, h * .58f, w * .32f, h * .60f, w * .48f, h * .72f)
                                cubicTo(w * .64f, h * .84f, w * .84f, h * .80f, w, h * .66f)
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            },
                            Color(0xFF172A2C)
                        )
                        for (i in 0..22) {
                            val x = w * ((i * 41 % 97) / 97f)
                            val y = h * (.12f + (i * 23 % 68) / 100f)
                            drawCircle(
                                if (i % 3 == 0) GardenGold.copy(alpha = .20f + glow * .35f) else GardenJade.copy(alpha = .12f + glow * .22f),
                                if (i % 5 == 0) 2.5.dp.toPx() else 1.3.dp.toPx(),
                                Offset(x, y)
                            )
                        }
                        // Three raised soil beds anchor the illustrated plants.
                        for (i in 0..2) {
                            val cx = w * (.20f + i * .30f)
                            val cy = h * .79f
                            drawOval(Color(0xFF0D1C20).copy(alpha = .72f), topLeft = Offset(cx - w * .13f, cy - h * .045f), size = Size(w * .26f, h * .10f))
                            drawOval(Brush.verticalGradient(listOf(Color(0xFF806344), Color(0xFF3D3B2D))), topLeft = Offset(cx - w * .12f, cy - h * .075f), size = Size(w * .24f, h * .105f))
                            drawOval(Color(0xFFB78B5D).copy(alpha = .75f), topLeft = Offset(cx - w * .12f, cy - h * .075f), size = Size(w * .24f, h * .025f))
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        repeat(3) { index ->
                            GardenPlantArt(
                                modifier = Modifier.weight(1f).height(112.dp),
                                variant = index,
                                growth = progress,
                                sway = breeze,
                                glow = glow
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                        color = Color(0xD90C1B27),
                        shape = RoundedCornerShape(9.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GardenGold.copy(alpha = .5f))
                    ) {
                        Text("ОБИТЕЛЬ · УР. $abodeLevel", modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), color = GardenGold, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
            GardenBedBadge("ГРЯДКА I", "Молодые побеги", progress, Modifier.weight(1f))
            GardenBedBadge("ГРЯДКА II", "Духовный рост", progress, Modifier.weight(1f))
            GardenBedBadge("ГРЯДКА III", "Цветение Ци", progress, Modifier.weight(1f))
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xE9162939),
            shape = RoundedCornerShape(15.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GardenJade.copy(alpha = .38f))
        ) {
            Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (ready) "УРОЖАЙ СОЗРЕЛ" else "РОСТ ДУХОВНЫХ ТРАВ", color = if (ready) GardenGold else GardenJade, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text(if (ready) "ГОТОВО" else timeRemaining, color = Color(0xFFD7E5E8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { if (ready) 1f else progress },
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(8.dp)),
                    color = GardenJade,
                    trackColor = Color(0xFF31434A)
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Image(
                        painter = painterResource(R.drawable.alchemy_herb_bundle),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp)
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Духовные травы", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("В хранилище: $spiritHerbs · следующий сбор: +$harvestAmount", color = Color(0xFFB8C9D4), fontSize = 9.sp)
                    }
                }
                Button(
                    onClick = onHarvest,
                    enabled = ready,
                    modifier = Modifier.fillMaxWidth().height(47.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF527D55),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF34464A),
                        disabledContentColor = Color(0xFF91A5A5)
                    )
                ) {
                    Text(if (ready) "СОБРАТЬ УРОЖАЙ  ·  +$harvestAmount" else "ТРАВЫ ЕЩЁ РАСТУТ", fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = .4.sp)
                }
                Text("Урожайность повышается вместе с уровнем обители. Травы используются в алхимической мастерской.", color = Color(0xFFAFC2C7), fontSize = 9.sp, lineHeight = 13.sp)
            }
        }
    }
}

@Composable
private fun GardenBedBadge(title: String, subtitle: String, progress: Float, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xE91B3038),
        shape = RoundedCornerShape(11.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GardenJade.copy(alpha = .32f))
    ) {
        Column(Modifier.padding(horizontal = 7.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = GardenGold, fontSize = 7.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = Color(0xFFC6D5D6), fontSize = 8.sp, maxLines = 1)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(3.dp), color = GardenJade, trackColor = Color(0xFF34464A))
        }
    }
}

@Composable
private fun GardenPlantArt(
    modifier: Modifier = Modifier,
    variant: Int,
    growth: Float,
    sway: Float,
    glow: Float
) {
    Canvas(modifier) {
        val base = Offset(size.width * .5f, size.height * .86f)
        val height = size.height * (.26f + growth * .42f)
        val top = Offset(base.x + sway * size.width, base.y - height)
        val stem = Color(0xFF75B66E)
        val leaf = when (variant) {
            0 -> Color(0xFF9BD77C)
            1 -> Color(0xFF78C99B)
            else -> Color(0xFFB8D879)
        }
        drawLine(stem, base, top, strokeWidth = 3.dp.toPx())
        for (side in -1..1) {
            if (side == 0) continue
            val y = base.y - height * (if (side < 0) .48f else .66f)
            val x = base.x + side * size.width * (.22f + growth * .08f)
            drawOval(
                Brush.linearGradient(listOf(leaf.copy(alpha = .85f), Color(0xFF315F45))),
                topLeft = Offset(x - size.width * .15f, y - size.height * .09f),
                size = Size(size.width * .30f, size.height * .18f)
            )
            drawLine(Color(0xFFC7E8A0).copy(alpha = .72f), Offset(x - side * size.width * .10f, y), Offset(x + side * size.width * .10f, y - size.height * .01f), strokeWidth = 1.dp.toPx())
        }
        if (growth > .38f) {
            val flower = when (variant) {
                0 -> Color(0xFFE5B6E9)
                1 -> Color(0xFF9BE2D6)
                else -> Color(0xFFFFD98C)
            }
            val flowerCenter = Offset(top.x, top.y - size.height * .035f)
            val radius = size.minDimension * (.035f + growth * .018f)
            for (petal in 0..5) {
                val angle = petal * (Math.PI / 3.0)
                drawCircle(flower.copy(alpha = .82f), radius, Offset(flowerCenter.x + kotlin.math.cos(angle).toFloat() * radius * 1.25f, flowerCenter.y + kotlin.math.sin(angle).toFloat() * radius * 1.25f))
            }
            drawCircle(GardenGold.copy(alpha = .8f + glow * .2f), radius * .58f, flowerCenter)
        }
        drawCircle(Color(0xFFFFD98C).copy(alpha = .16f + glow * .28f), size.minDimension * .13f, top)
    }
}
