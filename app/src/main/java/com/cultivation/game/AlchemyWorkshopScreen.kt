package com.cultivation.game

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AlchemyGold = Color(0xFFE8C887)
private val AlchemyJade = Color(0xFF8AD9C0)
private val AlchemyViolet = Color(0xFFC5A0F2)
private val AlchemyPanel = Color(0xE916263A)
private val AlchemyPanelLight = Color(0xE9223047)

@Composable
internal fun AlchemyWorkshopScreen(
    alchemyRank: Int,
    maxAlchemyRank: Int,
    spiritHerbs: Int,
    spiritWater: Int,
    qiPills: Int,
    bodyPills: Int,
    onBrewQi: () -> Unit,
    onBrewBody: () -> Unit,
    onUseQi: () -> Unit,
    onUseBody: () -> Unit,
    onUpgradeRank: () -> Unit
) {
    val safeRank = alchemyRank.coerceAtLeast(1)
    val safeMaxRank = maxAlchemyRank.coerceAtLeast(1)
    val qiEffect = 25 + safeRank * 5
    val bodyEffect = 15 + safeRank * 2
    val rankHerbs = safeRank * 4
    val rankWater = safeRank
    val canUpgrade = safeRank < safeMaxRank && spiritHerbs >= rankHerbs && spiritWater >= rankWater

    val transition = rememberInfiniteTransition(label = "alchemy-workshop")
    val pulse by transition.animateFloat(
        initialValue = 0.24f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alchemy-cauldron-pulse"
    )
    val runePulse by transition.animateFloat(
        initialValue = 0.32f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alchemy-rune-pulse"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF18243C), Color(0xFF34264C), Color(0xFF172B3D))
                    )
                )
                .border(1.dp, AlchemyViolet.copy(alpha = 0.66f), RoundedCornerShape(18.dp))
                .padding(horizontal = 13.dp, vertical = 9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        "ПАВИЛЬОН ДЕВЯТИ ТРАВ",
                        color = AlchemyViolet,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.3.sp
                    )
                    Text(
                        "Алхимическая мастерская",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Печь очищает духовные ингредиенты, а мастерство алхимика усиливает каждую готовую пилюлю.",
                        color = Color(0xFFD0D7E5),
                        fontSize = 9.sp,
                        lineHeight = 13.sp
                    )
                    Surface(
                        color = Color(0x663F315B),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AlchemyViolet.copy(alpha = 0.6f))
                    ) {
                        Text(
                            "РАНГ $safeRank / $safeMaxRank",
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            color = AlchemyGold,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Box(
                    modifier = Modifier.size(112.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val center = Offset(size.width * 0.5f, size.height * 0.53f)
                        val radius = size.minDimension * 0.43f
                        drawCircle(
                            Color(0xFFB77AF0).copy(alpha = 0.08f + pulse * 0.10f),
                            radius,
                            center
                        )
                        drawCircle(
                            AlchemyViolet.copy(alpha = 0.22f + runePulse * 0.42f),
                            radius * 0.88f,
                            center,
                            style = Stroke(width = 1.4.dp.toPx())
                        )
                        drawCircle(
                            AlchemyGold.copy(alpha = 0.24f + pulse * 0.28f),
                            radius * 0.65f,
                            center,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        for (i in 0..7) {
                            val angle = i * (Math.PI / 4.0)
                            val x = center.x + kotlin.math.cos(angle).toFloat() * radius * 0.92f
                            val y = center.y + kotlin.math.sin(angle).toFloat() * radius * 0.92f
                            drawCircle(
                                AlchemyGold.copy(alpha = 0.32f + runePulse * 0.48f),
                                if (i % 2 == 0) 2.6.dp.toPx() else 1.7.dp.toPx(),
                                Offset(x, y)
                            )
                        }
                    }
                    Image(
                        painter = painterResource(R.drawable.alchemy_cauldron_detail),
                        contentDescription = "Духовная алхимическая печь",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(104.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AlchemyResourceChip(
                iconRes = R.drawable.alchemy_herb_bundle,
                label = "Духовные травы",
                amount = spiritHerbs,
                accent = AlchemyJade,
                modifier = Modifier.weight(1f)
            )
            AlchemyResourceChip(
                iconRes = R.drawable.alchemy_spirit_water,
                label = "Духовная вода",
                amount = spiritWater,
                accent = Color(0xFF82D9EF),
                modifier = Modifier.weight(1f)
            )
            AlchemyResourceChip(
                iconRes = R.drawable.alchemy_qi_pill,
                label = "Пилюли Ци",
                amount = qiPills,
                accent = AlchemyJade,
                modifier = Modifier.weight(1f)
            )
            AlchemyResourceChip(
                iconRes = R.drawable.alchemy_body_pill,
                label = "Пилюли тела",
                amount = bodyPills,
                accent = AlchemyGold,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AlchemyRecipeCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.alchemy_qi_pill,
                title = "Пилюля сбора Ци",
                subtitle = "Чистая энергия для культивации",
                herbsCost = 6,
                waterCost = 2,
                herbsOwned = spiritHerbs,
                waterOwned = spiritWater,
                accent = AlchemyJade,
                enabled = spiritHerbs >= 6 && spiritWater >= 2,
                action = onBrewQi
            )
            AlchemyRecipeCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.alchemy_body_pill,
                title = "Пилюля крепкого тела",
                subtitle = "Постоянно укрепляет основу тела",
                herbsCost = 5,
                waterCost = 2,
                herbsOwned = spiritHerbs,
                waterOwned = spiritWater,
                accent = AlchemyGold,
                enabled = spiritHerbs >= 5 && spiritWater >= 2,
                action = onBrewBody
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AlchemyPanel,
            shape = RoundedCornerShape(15.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AlchemyGold.copy(alpha = 0.38f))
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    "СКЛАД ГОТОВЫХ ПИЛЮЛЬ",
                    color = AlchemyGold,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlchemyPillStockCard(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.alchemy_qi_pill,
                        title = "Сбор Ци",
                        count = qiPills,
                        effect = "+$qiEffect Ци",
                        accent = AlchemyJade,
                        actionLabel = "ПРИНЯТЬ",
                        enabled = qiPills > 0,
                        action = onUseQi
                    )
                    AlchemyPillStockCard(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.alchemy_body_pill,
                        title = "Крепкое тело",
                        count = bodyPills,
                        effect = "+$bodyEffect к здоровью навсегда",
                        accent = AlchemyGold,
                        actionLabel = "ПРИНЯТЬ",
                        enabled = bodyPills > 0,
                        action = onUseBody
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AlchemyPanelLight,
            shape = RoundedCornerShape(15.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AlchemyViolet.copy(alpha = 0.46f))
        ) {
            Column(
                modifier = Modifier.padding(11.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "МАСТЕРСТВО АЛХИМИКА",
                        color = AlchemyViolet,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "$safeRank / $safeMaxRank",
                        color = AlchemyGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                LinearProgressIndicator(
                    progress = { safeRank.toFloat() / safeMaxRank.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(8.dp)),
                    color = AlchemyViolet,
                    trackColor = Color(0xFF3A304F)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.alchemy_herb_bundle),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Text("Травы ×$rankHerbs", color = if (spiritHerbs >= rankHerbs) AlchemyJade else Color(0xFFE89A91), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Image(
                        painter = painterResource(R.drawable.alchemy_spirit_water),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Text("Вода ×$rankWater", color = if (spiritWater >= rankWater) AlchemyJade else Color(0xFFE89A91), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                }
                Button(
                    onClick = onUpgradeRank,
                    enabled = canUpgrade,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(11.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5C4B82),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF343444),
                        disabledContentColor = Color(0xFF9DA1AF)
                    )
                ) {
                    Text(
                        if (safeRank >= safeMaxRank) "ДОСТИГНУТ ПРЕДЕЛ РАНГА" else if (canUpgrade) "ПОВЫСИТЬ РАНГ АЛХИМИИ" else "НЕ ХВАТАЕТ ИНГРЕДИЕНТОВ",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.45.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AlchemyResourceChip(
    iconRes: Int,
    label: String,
    amount: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = AlchemyPanel,
        shape = RoundedCornerShape(11.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.34f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(27.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(label, color = Color(0xFFB9C8D7), fontSize = 7.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(amount.toString(), color = accent, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun AlchemyRecipeCard(
    modifier: Modifier,
    iconRes: Int,
    title: String,
    subtitle: String,
    herbsCost: Int,
    waterCost: Int,
    herbsOwned: Int,
    waterOwned: Int,
    accent: Color,
    enabled: Boolean,
    action: () -> Unit
) {
    Surface(
        modifier = modifier,
        color = AlchemyPanel,
        shape = RoundedCornerShape(15.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(9.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(49.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 2)
                    Text(subtitle, color = Color(0xFFB8C8D7), fontSize = 7.sp, lineHeight = 10.sp, maxLines = 2)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                AlchemyCostLabel("Травы ×$herbsCost", herbsOwned >= herbsCost, AlchemyJade)
                AlchemyCostLabel("Вода ×$waterCost", waterOwned >= waterCost, Color(0xFF82D9EF))
            }
            Button(
                onClick = action,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().height(35.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                shape = RoundedCornerShape(9.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF376F65),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF333E49),
                    disabledContentColor = Color(0xFF8996A2)
                )
            ) {
                Text(if (enabled) "СВАРИТЬ ПИЛЮЛЮ" else "НЕ ХВАТАЕТ РЕСУРСОВ", fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun AlchemyCostLabel(label: String, available: Boolean, accent: Color) {
    Surface(
        color = if (available) accent.copy(alpha = 0.12f) else Color(0xFF402F3C),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            color = if (available) accent else Color(0xFFE89A91),
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AlchemyPillStockCard(
    modifier: Modifier,
    iconRes: Int,
    title: String,
    count: Int,
    effect: String,
    accent: Color,
    actionLabel: String,
    enabled: Boolean,
    action: () -> Unit
) {
    Surface(
        modifier = modifier,
        color = Color(0xA91A2D40),
        shape = RoundedCornerShape(11.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(43.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(title, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                Text("В запасе: $count", color = accent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text(effect, color = Color(0xFFBAC8D5), fontSize = 7.sp, maxLines = 2)
            }
            OutlinedButton(
                onClick = action,
                enabled = enabled,
                modifier = Modifier.height(33.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(actionLabel, fontSize = 7.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
