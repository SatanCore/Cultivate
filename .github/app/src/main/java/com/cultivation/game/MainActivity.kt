package com.cultivation.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = Color(0xFF07090E))) {
                CultivationGame()
            }
        }
    }
}

enum class Realm(val title: String, val mult: Double, val strikes: Int) {
    QI("Конденсация Ци", 1.0, 3),
    FOUNDATION("Основание Дао", 3.5, 4),
    CORE("Золотое Ядро", 12.0, 5),
    NASCENT("Зарождение Души", 45.0, 6),
    SOUL("Трансформация Духа", 180.0, 7),
    IMMORTAL("Истинный Бессмертный", 2500.0, 9)
}

enum class Slot(val title: String) { WEAPON("Оружие"), ARMOR("Доспех"), RING("Кольцо"), ARTIFACT("Артефакт") }

data class Item(val id: String, val name: String, val slot: Slot, val power: Long, val color: Color, var enhance: Int = 0)
data class Bot(val name: String, val realm: Realm, var power: Long, val vip: Int, val color: Color)
data class Boss(val name: String, val hp: Long, val rewardStones: Long, val lootName: String, val color: Color)

class GameEngine {
    var name by mutableStateOf("Хаос_Дао")
    var realmIdx by mutableIntStateOf(0)
    var stage by mutableIntStateOf(1)
    var qi by mutableLongStateOf(0L)
    var reqQi by mutableLongStateOf(80L)
    var stones by mutableLongStateOf(20000L)
    var scrolls by mutableIntStateOf(20)
    var vip by mutableIntStateOf(2)

    val equipped = mutableStateMapOf<Slot, Item>()
    val inventory = mutableStateListOf<Item>()
    val bots = mutableStateListOf<Bot>()

    init {
        equipped[Slot.WEAPON] = Item("w1", "Меч Зеленой Стали", Slot.WEAPON, 80L, Color(0xFF4CAF50), 1)
        equipped[Slot.ARMOR] = Item("a1", "Мантия Ученика", Slot.ARMOR, 50L, Color(0xFF9E9E9E), 0)

        val names = listOf("Патриарх Бай", "Владыка Ван", "Мечник Линь", "Демон Су", "Старейшина Гу")
        val realms = Realm.values()
        for (i in 1..15) {
            val isWhale = i <= 3
            val r = if (isWhale) realms[minOf(realms.size - 1, 2 + i)] else realms[maxOf(0, (15 - i) / 3)]
            bots.add(Bot("${names.random()} #$i", r, (r.mult * 2200 * (1.0 + (15 - i) * 0.1)).toLong(), if (isWhale) 12 else 1, if (isWhale) Color(0xFFFF5252) else Color(0xFF64B5F6)))
        }
        bots.sortByDescending { it.power }
    }

    val combatPower: Long
        get() {
            val r = Realm.values()[realmIdx]
            val eq = equipped.values.sumOf { (it.power * (1.0 + it.enhance * 0.25)).toLong() }
            return ((150L * (realmIdx + 1) * stage + eq + vip * 500L) * r.mult).toLong()
        }

    fun advance() {
        if (stage < 9) {
            stage++
            qi = 0L
            reqQi = (reqQi * 1.6).toLong()
        } else if (realmIdx < Realm.values().size - 1) {
            realmIdx++
            stage = 1
            qi = 0L
            reqQi = (reqQi * 2.4).toLong()
        }
    }
}

@Composable
fun CultivationGame() {
    val state = remember { GameEngine() }
    var tab by remember { mutableIntStateOf(0) }
    var tribOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            state.qi += 10L * (state.realmIdx + 1) * (1 + state.vip / 3)
            if (Random.nextDouble() < 0.25) {
                state.bots.random().power += Random.nextLong(100L, 600L)
                state.bots.sortByDescending { it.power }
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(color = Color(0xFF0F1320), modifier = Modifier.fillMaxWidth().border(0.5.dp, Color(0xFF232B40))) {
                Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(state.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Badge(containerColor = Color(0xFFD50000)) { Text("VIP ${state.vip}", color = Color.White, fontSize = 8.sp) }
                        }
                        val r = Realm.values()[state.realmIdx]
                        Text("${r.title} • Стадия ${state.stage}", color = Color(0xFFFFD700), fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("БОЕВАЯ МОЩЬ", color = Color.Gray, fontSize = 8.sp)
                        Text("${state.combatPower}", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0C0E16)) {
                listOf("Обитель" to "☯️", "Герой" to "🥋", "Арена" to "⚔️", "Боссы" to "👹", "Гача" to "🔮", "Донат" to "💎").forEachIndexed { i, item ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        label = { Text(item.first, fontSize = 8.sp) },
                        icon = { Text(item.second, fontSize = 14.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFFD700), selectedTextColor = Color(0xFFFFD700), indicatorColor = Color(0xFF1E2438))
                    )
                }
            }
        }
    ) { p ->
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF07090E)).padding(p)) {
            when (tab) {
                0 -> TabSanctuary(state) { tribOpen = true }
                1 -> TabHero(state)
                2 -> TabArena(state)
                3 -> TabBosses(state)
                4 -> TabGacha(state)
                5 -> TabStore(state)
            }
            if (tribOpen) TabTribulation(state) { tribOpen = false }
        }
    }
}

@Composable
fun TabSanctuary(state: GameEngine, onBreak: () -> Unit) {
    val trans = rememberInfiniteTransition()
    val rot by trans.animateFloat(0f, 360f, infiniteRepeatable(tween(18000, easing = LinearEasing)))
    val pulse by trans.animateFloat(0.96f, 1.04f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse))

    Column(modifier = Modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("ТАИНСТВО МЕДИТАЦИИ ДАО", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("Камни: ${state.stones} 💎 | Свитки: ${state.scrolls} 📜", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.size(190.dp).clickable { state.qi += 15L * (state.realmIdx + 1) }, contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val c = Offset(size.width / 2, size.height / 2)
                val rad = (size.minDimension / 2) * pulse
                drawCircle(Brush.sweepGradient(listOf(Color(0xFF00E5FF), Color(0xFFFFD700), Color(0xFF00E5FF))), radius = rad, style = Stroke(3.dp.toPx()))
                for (i in 0 until 8) {
                    val a = Math.toRadians((rot + i * 45).toDouble())
                    drawLine(Color(0xFFFFD700), Offset((c.x + rad * 0.7f * cos(a)).toFloat(), (c.y + rad * 0.7f * sin(a)).toFloat()), Offset((c.x + rad * 0.88f * cos(a)).toFloat(), (c.y + rad * 0.88f * sin(a)).toFloat()), 2.dp.toPx())
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🧘", fontSize = 40.sp)
                Text("Тап = +Ци", color = Color.Cyan, fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        val prg = (state.qi.toFloat() / state.reqQi.toFloat()).coerceIn(0f, 1f)
        Text("Ци: ${state.qi} / ${state.reqQi}", color = Color.White, fontSize = 11.sp)
        LinearProgressIndicator(progress = { prg }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = Color(0xFF00E5FF))
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBreak,
            enabled = state.qi >= state.reqQi,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), disabledContainerColor = Color(0xFF1E2434))
        ) {
            Text(if (state.qi >= state.reqQi) "⚡ НАЧАТЬ НЕБЕСНЫЙ ПРОРЫВ" else "НАКАПЛИВАЙТЕ ЦИ ДЛЯ ПРОРЫВА", color = if (state.qi >= state.reqQi) Color.Black else Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TabTribulation(state: GameEngine, onDismiss: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    val maxStep = Realm.values()[state.realmIdx].strikes
    var log by remember { mutableStateOf("Небесные молнии готовы обрушиться на вас!") }
    var ended by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (ended) onDismiss() },
        containerColor = Color(0xFF101322),
        title = { Text("⚡ СКОРБЬ НЕБЕС", color = Color(0xFFFFD700), fontSize = 15.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.fillMaxWidth().height(90.dp).background(Color(0xFF070810), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (step > 0) {
                            val path = Path().apply {
                                moveTo(size.width / 2, 0f)
                                lineTo(size.width * 0.46f, size.height * 0.4f)
                                lineTo(size.width * 0.54f, size.height * 0.7f)
                                lineTo(size.width / 2, size.height)
                            }
                            drawPath(path, Color(0xFF00E5FF), style = Stroke(3.dp.toPx()))
                        }
                    }
                    Text(if (ended) "ПРОРЫВ ЗАВЕРШЕН!" else "Разряд $step из $maxStep", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(log, color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (!ended) {
                    step++
                    log = "Молния №$step отражена золотым куполом!"
                    if (step >= maxStep) {
                        state.advance()
                        ended = true
                        log = "Успех! Ступень прорвана!"
                    }
                } else onDismiss()
            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))) {
                Text(if (ended) "Принять Дао" else "Отразить Удар", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun TabHero(state: GameEngine) {
    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Text("СНАРЯЖЕНИЕ КУЛЬТИВАТОРА", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Slot.values().forEach { slot ->
                val itm = state.equipped[slot]
                Box(modifier = Modifier.weight(1f).height(65.dp).background(Color(0xFF141826), RoundedCornerShape(6.dp)).border(1.dp, itm?.color ?: Color(0xFF263238), RoundedCornerShape(6.dp)).clickable {
                    itm?.let { if (state.stones >= 500) { state.stones -= 500; it.enhance++ } }
                }.padding(4.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(slot.title, color = Color.Gray, fontSize = 9.sp)
                        Text(itm?.name ?: "Пусто", color = itm?.color ?: Color.DarkGray, fontSize = 10.sp, maxLines = 1, fontWeight = FontWeight.Bold)
                        if (itm != null) Text("+${itm.enhance} (500💎)", color = Color(0xFFFFD700), fontSize = 8.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text("ПРОСТРАНСТВЕННЫЙ МЕШОК (${state.inventory.size}):", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(state.inventory) { itm ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF121624))) {
                    Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(itm.name, color = itm.color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Мощь: +${itm.power} | Слот: ${itm.slot.title}", color = Color.Gray, fontSize = 9.sp)
                        }
                        Button(onClick = {
                            val old = state.equipped[itm.slot]
                            state.equipped[itm.slot] = itm
                            state.inventory.remove(itm)
                            old?.let { state.inventory.add(it) }
                        }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) { Text("Надеть", fontSize = 10.sp) }
                    }
                }
            }
        }
    }
}

@Composable
fun TabArena(state: GameEngine) {
    var duelingBot by remember { mutableStateOf<Bot?>(null) }
    var resultText by remember { mutableStateOf("Бросьте вызов сильнейшим культиваторам!") }
    val trans = rememberInfiniteTransition()
    val swordX by trans.animateFloat(-1f, 1f, infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse))

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Text("РЕЙТИНГ АРЕНЫ МИРА", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(resultText, color = Color.White, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(8.dp))

        if (duelingBot != null) {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp).background(Color(0xFF131726), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cy = size.height / 2
                    drawCircle(Color(0xFF00E5FF), 16.dp.toPx(), Offset(size.width * 0.2f, cy))
                    drawCircle(duelingBot!!.color, 16.dp.toPx(), Offset(size.width * 0.8f, cy))
                    val sx = (size.width * 0.5f) + (size.width * 0.28f) * swordX
                    drawLine(Color(0xFFFFD700), Offset(sx - 10f, cy), Offset(sx + 10f, cy), 3.dp.toPx())
                }
                Text("⚔️ ДУЭЛЬ ⚔️", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(state.bots) { bot ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101422))) {
                    Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("${bot.name} [VIP ${bot.vip}]", color = bot.color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("БМ: ${bot.power} • ${bot.realm.title}", color = Color.Gray, fontSize = 9.sp)
                        }
                        Button(onClick = {
                            duelingBot = bot
                            resultText = if (state.combatPower >= bot.power) "🏆 Вы сокрушили ${bot.name}!" else "💀 ${bot.name} отразил вашу атаку!"
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                            Text("Вызов", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabBosses(state: GameEngine) {
    val bosses = remember {
        listOf(
            Boss("Древний Змей Бездны", 5000L, 2000L, "Змеиный Клинок", Color(0xFF4CAF50)),
            Boss("Огненный Цилинь", 35000L, 8000L, "Панцирь Цилиня", Color(0xFF2196F3)),
            Boss("Черный Дракон Пустоты", 200000L, 35000L, "Кольцо Бездны", Color(0xFFAB47BC)),
            Boss("Владыка Небес", 1500000L, 150000L, "Небесный Артефакт", Color(0xFFFFB300))
        )
    }
    var log by remember { mutableStateOf("Охотьтесь на чудовищ ради камней и лута!") }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Text("ОХОТА НА ВЕЛИКИХ ЧУДОВИЩ", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(log, color = Color.Green, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(bosses) { b ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF131726))) {
                    Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(b.name, color = b.color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("ОЗ: ${b.hp} | Награда: +${b.rewardStones}💎", color = Color.Gray, fontSize = 10.sp)
                        }
                        Button(onClick = {
                            if (state.combatPower >= b.hp / 2) {
                                state.stones += b.rewardStones
                                val drop = Item("d_${Random.nextInt(9999)}", b.lootName, Slot.values().random(), (b.hp * 0.08).toLong(), b.color)
                                state.inventory.add(drop)
                                log = "Победа над ${b.name}! Награда: ${b.rewardStones}💎 и ${drop.name}!"
                            } else log = "💀 Недостаточно мощи против ${b.name}!"
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315))) { Text("Битва", fontSize = 10.sp) }
                    }
                }
            }
        }
    }
}

@Composable
fun TabGacha(state: GameEngine) {
    var log by remember { mutableStateOf("Испытайте удачу в Алтаре Судьбы!") }
    val banners = listOf("Оружие", "Питомцы", "Доспехи", "Артефакты")
    var sel by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("АЛТАРЬ ПРИЗЫВА СОКРОВИЩ", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("Свитков в наличии: ${state.scrolls} 📜", color = Color.Cyan, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            banners.forEachIndexed { i, b ->
                Button(onClick = { sel = i }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (sel == i) Color(0xFF6A1B9A) else Color(0xFF171A28)), contentPadding = PaddingValues(2.dp)) {
                    Text(b, fontSize = 9.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color(0xFF171526), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Text(log, color = Color.White, textAlign = TextAlign.Center, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (state.scrolls >= 1) {
                state.scrolls--
                val colors = listOf(Color(0xFF9E9E9E), Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFFB300))
                val drop = Item("g_${Random.nextInt(9999)}", "Реликвия Судьбы", Slot.values().random(), Random.nextLong(150L, 1200L), colors.random())
                state.inventory.add(drop)
                log = "✨ Вы призвали: [${drop.name}] (${drop.slot.title}) мощи +${drop.power}!"
            } else log = "Нет свитков! Заберите в магазине Доната."
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))) {
            Text("Призыв (1 Свиток)", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TabStore(state: GameEngine) {
    var log by remember { mutableStateOf("") }
    val packs = listOf(
        Triple("Набор Наследника", "99 ₽ (БЕСПЛАТНО)", Pair(50000L, 20)),
        Triple("Сундук Патриарха", "990 ₽ (БЕСПЛАТНО)", Pair(300000L, 80)),
        Triple("Казна Императора", "4 990 ₽ (БЕСПЛАТНО)", Pair(2000000L, 250)),
        Triple("Сокровище Китов", "9 990 ₽ (БЕСПЛАТНО)", Pair(15000000L, 800))
    )

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Text("НЕБЕСНАЯ КАЗНА (БЕСПЛАТНЫЙ ДОНАТ)", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("Реальные деньги отключены. Забирайте ресурсы даром!", color = Color.Gray, fontSize = 10.sp)
        if (log.isNotEmpty()) Text(log, color = Color.Green, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(packs) { p ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF131726))) {
                    Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(p.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("+${p.third.first}💎 | +${p.third.second}📜", color = Color.Cyan, fontSize = 10.sp)
                        }
                        Button(onClick = {
                            state.stones += p.third.first
                            state.scrolls += p.third.second
                            state.vip = minOf(15, state.vip + 2)
                            log = "🎉 Получен ${p.first}! Уровень VIP вырос до ${state.vip}!"
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC67C00))) { Text(p.second, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}
// КОНЕЦ ФАЙЛА
