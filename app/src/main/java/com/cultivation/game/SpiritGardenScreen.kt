package com.cultivation.game

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
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
    val selectedBed = remember { mutableIntStateOf(0) }

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
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF173341))
                ) {
                    Image(
                        painter = painterResource(R.drawable.abode_garden_sanctuary),
                        contentDescription = "Панорама сада духовных трав с горными террасами и павильоном",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        Modifier.matchParentSize().background(
                            Brush.verticalGradient(
                                listOf(Color(0x22101E35), Color(0x00101E35), Color(0xA50C1B27))
                            )
                        )
                    )
                    Canvas(Modifier.matchParentSize()) {
                        for (i in 0..25) {
                            val phase = (glow + i * .137f) % 1f
                            val x = size.width * ((i * 41 % 97) / 97f)
                            val y = size.height * (.14f + phase * .72f)
                            drawCircle(
                                if (i % 3 == 0) GardenGold.copy(alpha = .14f + glow * .22f)
                                else GardenJade.copy(alpha = .12f + glow * .18f),
                                if (i % 5 == 0) 2.2.dp.toPx() else 1.2.dp.toPx(),
                                Offset(x, y)
                            )
                        }
                    }
                    BoxWithConstraints(Modifier.matchParentSize()) {
                        GardenPlotHotspot(
                            title = "ЛУНОЛИСТ",
                            icon = R.drawable.garden_herb_moonleaf,
                            selected = selectedBed.intValue == 0,
                            modifier = Modifier.align(Alignment.TopStart)
                                .offset(x = maxWidth * .055f, y = maxHeight * .655f)
                        ) { selectedBed.intValue = 0 }
                        GardenPlotHotspot(
                            title = "ЛОТОС ЦИ",
                            icon = R.drawable.garden_herb_lotus,
                            selected = selectedBed.intValue == 1,
                            modifier = Modifier.align(Alignment.TopStart)
                                .offset(x = maxWidth * .385f, y = maxHeight * .655f)
                        ) { selectedBed.intValue = 1 }
                        GardenPlotHotspot(
                            title = "СОЛНЕЧНЫЙ КОРЕНЬ",
                            icon = R.drawable.garden_herb_sunbloom,
                            selected = selectedBed.intValue == 2,
                            modifier = Modifier.align(Alignment.TopStart)
                                .offset(x = maxWidth * .675f, y = maxHeight * .655f)
                        ) { selectedBed.intValue = 2 }
                    }
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                        color = Color(0xE60C1B27),
                        shape = RoundedCornerShape(9.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GardenGold.copy(alpha = .62f))
                    ) {
                        Text("ОБИТЕЛЬ · УР. $abodeLevel", modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), color = GardenGold, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        color = Color(0xE60C1B27),
                        shape = RoundedCornerShape(9.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GardenJade.copy(alpha = .55f))
                    ) {
                        Text("СВЯЩЕННЫЕ ГРЯДКИ · 3", modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), color = GardenJade, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
            GardenBedBadge(
                "ГРЯДКА I", "Лунолист", R.drawable.garden_herb_moonleaf, progress,
                selectedBed.intValue == 0, { selectedBed.intValue = 0 }, Modifier.weight(1f)
            )
            GardenBedBadge(
                "ГРЯДКА II", "Лотос Ци", R.drawable.garden_herb_lotus, progress,
                selectedBed.intValue == 1, { selectedBed.intValue = 1 }, Modifier.weight(1f)
            )
            GardenBedBadge(
                "ГРЯДКА III", "Солнечный корень", R.drawable.garden_herb_sunbloom, progress,
                selectedBed.intValue == 2, { selectedBed.intValue = 2 }, Modifier.weight(1f)
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xD9162939),
            shape = RoundedCornerShape(11.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GardenGold.copy(alpha = .24f))
        ) {
            Row(
                Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                val selectedPlant = when (selectedBed.intValue) {
                    0 -> R.drawable.garden_herb_moonleaf
                    1 -> R.drawable.garden_herb_lotus
                    else -> R.drawable.garden_herb_sunbloom
                }
                Image(painterResource(selectedPlant), contentDescription = null, modifier = Modifier.size(36.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        when (selectedBed.intValue) {
                            0 -> "Лунолист · прохладные тенистые грядки"
                            1 -> "Лотос Ци · цветок чистой духовной энергии"
                            else -> "Солнечный корень · тёплая сторона сада"
                        },
                        color = GardenGold, fontSize = 9.sp, fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Все три грядки входят в общий восьмичасовой цикл и пополняют запас духовных трав.",
                        color = Color(0xFFB8C9D4), fontSize = 8.sp, lineHeight = 11.sp
                    )
                }
            }
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
private fun GardenPlotHotspot(
    title: String,
    icon: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.size(width = 116.dp, height = 38.dp).clickable(onClick = onClick),
        color = if (selected) Color(0xF02A5145) else Color(0xD9142932),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 1.6.dp else 1.dp,
            if (selected) GardenGold.copy(alpha = .95f) else GardenJade.copy(alpha = .62f)
        ),
        shadowElevation = if (selected) 5.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(23.dp)
            )
            Text(
                title,
                color = if (selected) GardenGold else Color.White,
                fontSize = 6.5.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun GardenBedBadge(
    title: String,
    subtitle: String,
    icon: Int,
    progress: Float,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = if (selected) Color(0xEF29463F) else Color(0xE91B3038),
        shape = RoundedCornerShape(11.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 1.5.dp else 1.dp,
            if (selected) GardenGold.copy(alpha = .88f) else GardenJade.copy(alpha = .32f)
        )
    ) {
        Column(
            Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(31.dp))
            Text(title, color = if (selected) GardenGold else GardenJade, fontSize = 7.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = Color(0xFFC6D5D6), fontSize = 8.sp, maxLines = 1)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = if (selected) GardenGold else GardenJade,
                trackColor = Color(0xFF34464A)
            )
        }
    }
}
