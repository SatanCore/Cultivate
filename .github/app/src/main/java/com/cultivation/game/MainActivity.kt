package com.cultivation.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CultivationGameRoot()
        }
    }
}

enum class MortalWorld(val title: String) {
    MORTAL("Мир Смертных"),
    SPIRITUAL("Духовный Мир"),
    IMMORTAL("Небесный Мир")
}

enum class CultivationRealm(val title: String, val world: MortalWorld, val mult: Double) {
    QI_1("Конденсация Ци I", MortalWorld.MORTAL, 1.0),
    QI_2("Конденсация Ци II", MortalWorld.MORTAL, 1.5),
    FOUNDATION("Возведение Основания", MortalWorld.MORTAL, 4.0),
    GOLDEN_CORE("Золотое Ядро", MortalWorld.MORTAL, 15.0),
    NASCENT_SOUL("Зарождение Души", MortalWorld.SPIRITUAL, 60.0),
    SOUL_TRANSFORM("Трансформация Духа", MortalWorld.SPIRITUAL, 250.0),
    ASCENSION("Истинный Бессмертный", MortalWorld.IMMORTAL, 2000.0)
}

data class BotCultivator(
    val name: String,
    var power: Long,
    val realm: CultivationRealm,
    val vip: Int
)

@Composable
fun CultivationGameRoot() {
    var selectedTab by remember { mutableIntStateOf(0) }

    var playerStones by remember { mutableLongStateOf(5000L) }
    var gachaTickets by remember { mutableIntStateOf(10) }
    var vipLevel by remember { mutableIntStateOf(1) }
    var playerRealmIndex by remember { mutableIntStateOf(0) }
    val currentRealm = CultivationRealm.values()[playerRealmIndex]

    val basePower = (currentRealm.mult * 1250).toLong() + (vipLevel * 1000L)

    val bots = remember {
        mutableStateListOf(
            BotCultivator("Патриарх Бай (Бот)", 125000L, CultivationRealm.NASCENT_SOUL, 12),
            BotCultivator("Старейшина Вэй (Бот)", 45000L, CultivationRealm.GOLDEN_CORE, 8),
            BotCultivator("Демон Линь (Бот)", 18000L, CultivationRealm.FOUNDATION, 5),
            BotCultivator("Мечник Су (Бот)", 3200L, CultivationRealm.QI_2, 2)
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            bots.forEach { bot ->
                bot.power += Random.nextLong(50L, 300L)
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF141722)) {
                val items = listOf("Обитель", "Арена", "Гача", "Облики", "VIP Дар")
                items.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title, fontSize = 10.sp) },
                        icon = { Text(if (selectedTab == index) "⭐" else "🗡️") }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0C0E14))
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(currentRealm, basePower, playerStones, gachaTickets, vipLevel) {
                    if (playerRealmIndex < CultivationRealm.values().size - 1) {
                        playerRealmIndex++
                    }
                }
                1 -> ArenaScreen(basePower, bots)
                2 -> GachaScreen(gachaTickets, onPull = { gachaTickets-- })
                3 -> CosmeticsScreen()
                4 -> FreeDonationScreen { stones, tickets, vip ->
                    playerStones += stones
                    gachaTickets += tickets
                    vipLevel += vip
                }
            }
        }
    }
}

@Composable
fun HomeScreen(realm: CultivationRealm, power: Long, stones: Long, tickets: Int, vip: Int, onBreakthrough: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("ПУТЬ БЕССМЕРТИЯ: OVERMORTAL", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Текущий мир: ${realm.world.title}", color = Color.Cyan, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F2C))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Культиватор: Хаос_Дао (Вы)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Ступень: ${realm.title}", color = Color(0xFFFFB300))
                Text("Боевая Мощь (БМ): $power", color = Color.Green, fontSize = 14.sp)
                Text("VIP Статус: VIP $vip", color = Color.Magenta)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Духовные камни: $stones 💎 | Свитки призыва: $tickets 📜", color = Color.LightGray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onBreakthrough,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC67C00))
        ) {
            Text("СОВЕРШИТЬ НЕБЕСНЫЙ ПРОРЫВ", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ArenaScreen(playerPower: Long, bots: List<BotCultivator>) {
    var battleLog by remember { mutableStateOf("Выберите соперника для проверки меча.") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("АРЕНА СУДЬБЫ (РЕЙТИНГ МИРА)", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Ваша мощь: $playerPower БМ", color = Color.Green, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(8.dp))
        Text(battleLog, color = Color.White, fontSize = 12.sp, modifier = Modifier.background(Color(0xFF1E2230), RoundedCornerShape(4.dp)).padding(8.dp).fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(bots) { bot ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161A24))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${bot.name} [VIP ${bot.vip}]", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("БМ: ${bot.power} | ${bot.realm.title}", color = Color.Gray, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                battleLog = if (playerPower >= bot.power) {
                                    "⚔️ Вы сокрушили ${bot.name} с помощью тайных искусств!"
                                } else {
                                    "💀 ${bot.name} отразил вашу атаку и унизил ваш клан! Прокачайте БМ."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000))
                        ) {
                            Text("Дуэль", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GachaScreen(tickets: Int, onPull: () -> Unit) {
    var resultText by remember { mutableStateOf("Потяните свиток, чтобы получить божественный артефакт.") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("АЛТАРЬ ПРИЗЫВА СОКРОВИЩ", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Свитков в наличии: $tickets", color = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth().height(140.dp).background(Color(0xFF231E33), RoundedCornerShape(8.dp)).padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(resultText, color = Color.Cyan, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (tickets > 0) {
                    onPull()
                    val drops = listOf(
                        "✨ Меч Девяти Бессмертных [IMMORTAL]",
                        "📜 Свиток Пустоты [HEAVEN]",
                        "💊 Пилюля Дракона [EARTH]",
                        "🛡️ Железный Доспех [COMMON]"
                    )
                    resultText = "Выпало: ${drops.random()}"
                } else {
                    resultText = "Нет свитков! Заберите их в разделе 'VIP Дар'."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
        ) {
            Text("СДЕЛАТЬ ПРИЗЫВ (1 Свиток)")
        }
    }
}

@Composable
fun CosmeticsScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("ОБИТЕЛЬ ОБЛИКОВ", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("• Облик: Небесный Мечник (Активен) [+5000 БМ]", color = Color.White)
        Text("• Рамка: Драконий Оскал [+2000 БМ]", color = Color.White)
        Text("• Аура: Вспышка Пустоты [+3500 БМ]", color = Color.White)
    }
}

@Composable
fun FreeDonationScreen(onClaim: (stones: Long, tickets: Int, vip: Int) -> Unit) {
    var status by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("ТАЙНАЯ СОКРОВИЩНИЦА (БЕСПЛАТНЫЙ VIP)", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Все пакеты активируются моментально без списания реальных денег.", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(12.dp))

        if (status.isNotEmpty()) {
            Text(status, color = Color.Green, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                onClaim(100000L, 50, 2)
                status = "✅ Получен Набор Новичка (+100,000 Камней, +50 Свитков, +2 VIP)!"
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text("ЗАБРАТЬ: Набор Патриарха (999 ₽ БЕСПЛАТНО)")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                onClaim(10000000L, 300, 5)
                status = "👑 Активирован Сундук Владыки (+10,000,000 Камней, +300 Свитков, +5 VIP)!"
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
        ) {
            Text("ЗАБРАТЬ: Сундук Владыки (9 990 ₽ БЕСПЛАТНО)", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
