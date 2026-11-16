package com.cultivation.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private val ShopGold = Color(0xFFE8C783)
private val ShopJade = Color(0xFF82D5B0)
private val ShopPanel = Color(0xEE142638)

@Composable
internal fun MeiShopScreen(
    stones: Int,
    shardCount: Int,
    onBuy: (Int) -> Unit,
    onClose: () -> Unit
) {
    val maxAffordable = (stones.coerceAtLeast(0) / 200)
    var quantity by remember(stones) { mutableIntStateOf(1) }
    val shownQuantity = quantity.coerceIn(1, maxAffordable.coerceAtLeast(1))
    val totalCost = shownQuantity * 200

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(146.dp)
                .clip(RoundedCornerShape(18.dp))
        ) {
            Image(
                painter = painterResource(R.drawable.shop_pavilion_interior),
                contentDescription = "Интерьер лавки духовных материалов в Лунной Гавани",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0x1806111B),
                                Color(0x5B0B1925),
                                Color(0xF20A1725)
                            )
                        )
                    )
            )
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(9.dp),
                color = Color(0xE9152A37),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, ShopGold.copy(alpha = .72f))
            ) {
                Text(
                    "ЛУННАЯ ГАВАНЬ",
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    color = ShopGold,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(13.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "ЛАВКА ДУХОВНЫХ РЕСУРСОВ",
                    color = ShopGold,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
                Text(
                    "Товары госпожи Мэй",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Редкие материалы для совершенствования Дао",
                    color = Color(0xFFD0DFE8),
                    fontSize = 9.sp
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ShopPanel,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ShopGold.copy(alpha = .38f)),
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.shop_mei_portrait),
                    contentDescription = "Портрет торговки Мэй",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, ShopGold.copy(alpha = .85f), CircleShape)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("МЭЙ · ХРАНИТЕЛЬНИЦА ЛАВКИ", color = ShopGold, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    Text("«Путник, ищешь силу? Начни с малого осколка. В нём тоже живёт Ци»", color = Color(0xFFE4EAF0), fontSize = 10.sp, lineHeight = 14.sp)
                    Text("Торговля материалами", color = ShopJade, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xF01A2C3B),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF6DB99D).copy(alpha = .58f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF315C62), Color(0xFF142538))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.shop_spirit_shard),
                        contentDescription = "Нефритовый осколок духа",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(76.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Осколок духа", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        Surface(color = Color(0xFF31584D), shape = RoundedCornerShape(6.dp)) {
                            Text("МАТЕРИАЛ", modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), color = ShopJade, fontSize = 7.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Text("Чистая крупица духовной энергии. Используется для развития техник после 20-го уровня.", color = Color(0xFFC7D4DF), fontSize = 9.sp, lineHeight = 13.sp)
                    Text("В твоей сумке: $shardCount", color = ShopJade, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("Цена за штуку · 200 камней духа", color = ShopGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xE9152232),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0x557C9AA9))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Количество", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Можно купить: $maxAffordable шт.", color = Color(0xFFAFC2D1), fontSize = 8.sp)
                }
                OutlinedButton(
                    onClick = { quantity = (shownQuantity - 1).coerceAtLeast(1) },
                    enabled = shownQuantity > 1,
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    shownQuantity.toString(),
                    modifier = Modifier.width(36.dp),
                    color = ShopGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                OutlinedButton(
                    onClick = { quantity = (shownQuantity + 1).coerceAtMost(maxAffordable) },
                    enabled = shownQuantity < maxAffordable,
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("К оплате", color = Color(0xFFCAD7E2), fontSize = 10.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text("$totalCost", color = ShopGold, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(5.dp))
            Text("КАМНЕЙ", color = Color(0xFFB6C6D2), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = { onBuy(shownQuantity) },
            enabled = stones >= totalCost,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3D7866),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF35424A),
                disabledContentColor = Color(0xFF9AA7AD)
            )
        ) {
            Text(
                if (stones >= totalCost) "ПРИОБРЕСТИ · $shownQuantity ШТ." else "НЕДОСТАТОЧНО КАМНЕЙ ДУХА",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = .5.sp
            )
        }
        TextButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.textButtonColors(contentColor = ShopJade)
        ) {
            Text("Вернуться на площадь", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
