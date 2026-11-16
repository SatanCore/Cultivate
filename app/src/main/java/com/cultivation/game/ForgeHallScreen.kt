package com.cultivation.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ForgeGold = Color(0xFFE8B76D)
private val ForgeJade = Color(0xFF83D0B0)

@Composable
fun ForgeHallScreen(
    weaponName: String,
    weaponAttack: Int,
    bonusDamage: Int,
    temper: Int,
    ironOre: Int,
    stones: Int,
    onOpenWorkshop: () -> Unit,
    onClose: () -> Unit
) {
    val forgeAmbient = rememberInfiniteTransition(label = "forge-ambient")
    val forgeGlow by forgeAmbient.animateFloat(
        initialValue = .28f,
        targetValue = .78f,
        animationSpec = infiniteRepeatable(tween(1700), RepeatMode.Reverse),
        label = "forge-fire-glow"
    )
    val swordDrift by forgeAmbient.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2300), RepeatMode.Reverse),
        label = "forge-sword-drift"
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(218.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.abode_forge_scene),
                contentDescription = "Кузница Чэня",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier.matchParentSize().background(
                    Brush.radialGradient(
                        listOf(ForgeGold.copy(alpha = forgeGlow * .24f), Color.Transparent)
                    )
                )
            )
            Box(
                modifier = Modifier.matchParentSize().background(
                    Brush.verticalGradient(
                        listOf(Color(0x22101A2A), Color(0x44101825), Color(0xF0101825))
                    )
                )
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("МАСТЕРСКАЯ ЧЭНЯ", color = ForgeGold, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Text("Пламя закалки", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
                Text("Здесь сталь обретает волю, а оружие — силу духа", color = Color(0xFFD7E1E9), fontSize = 10.sp)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xE91A2A3B),
            shape = RoundedCornerShape(17.dp),
            border = BorderStroke(1.dp, ForgeGold.copy(alpha = .28f + forgeGlow * .30f))
        ) {
            Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("АКТИВНОЕ ОРУЖИЕ", color = ForgeGold, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.3.sp)
                Text(weaponName, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    ForgeStat("АТАКА", "+$weaponAttack", Modifier.weight(1f))
                    ForgeStat("ДОП. УРОН", "+$bonusDamage", Modifier.weight(1f))
                    ForgeStat("ЗАКАЛКА", "+$temper", Modifier.weight(1f))
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xCC142538),
            shape = RoundedCornerShape(15.dp),
            border = BorderStroke(1.dp, Color(0x557DA5BC))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painterResource(R.drawable.relic_sword), null, Modifier.size(38.dp).graphicsLayer { translationY = swordDrift.dp.toPx() })
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("ЗАПАС МАТЕРИАЛОВ", color = Color(0xFFB9C9D8), fontSize = 8.sp, fontWeight = FontWeight.Black)
                    Text("$ironOre ед. железной руды", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("$stones камней духа", color = ForgeJade, fontSize = 10.sp)
                }
            }
        }

        Text(
            "Чэнь сохранит свойства и историю твоего оружия. Каждая закалка повышает атаку, защиту и дополнительный урон.",
            color = Color(0xFFC2CEDA),
            fontSize = 11.sp
        )
        Button(
            onClick = onOpenWorkshop,
            modifier = Modifier.fillMaxWidth().height(49.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF895A32), contentColor = Color.White),
            shape = RoundedCornerShape(13.dp)
        ) {
            Text("ВОЙТИ В ГОРН · ЗАКАЛИТЬ ОРУЖИЕ", fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
        OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("ВЕРНУТЬСЯ НА ПЛОЩАДЬ", color = ForgeJade, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ForgeStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFF26384A),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 9.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, color = Color(0xFFB5C3D0), fontSize = 7.sp, fontWeight = FontWeight.Black)
            Text(value, color = ForgeJade, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
    }
}
