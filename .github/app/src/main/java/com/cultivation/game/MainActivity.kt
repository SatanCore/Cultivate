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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.*
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = Color(0xFF06070B))) {
                OvermortalCultivationEngine()
            }
        }
    }
}

// ==========================================
// 1. КОСМОЛОГИЯ, 16 СТУПЕНЕЙ И 4 МИРА
// ==========================================

enum class DaoWorld(val title: String) {
    MORTAL("Мир Смертных"),
    SPIRITUAL("Духовный Мир"),
    IMMORTAL("Небесный Мир"),
    CHAOS("Первозданный Хаос (Межмирье)")
}

enum class MajorRealm(val title: String, val powerBase: Double, val world: DaoWorld, val tribulationBolts: Int) {
    QI_REFINING("Конденсация Ци", 1.0, DaoWorld.MORTAL, 3),
    FOUNDATION("Возведение Основания", 4.2, DaoWorld.MORTAL, 4),
    CORE_FORMATION("Золотое Ядро", 18.0, DaoWorld.MORTAL, 5),
    NASCENT_SOUL("Зарождение Души", 80.0, DaoWorld.MORTAL, 6),
    SOUL_TRANSFORM("Трансформация Духа", 380.0, DaoWorld.MORTAL, 7),
    VOID_REFINING("Очищение Пустоты", 1600.0, DaoWorld.SPIRITUAL, 8),
    BODY_INTEGRATION("Слияние с Дао", 7500.0, DaoWorld.SPIRITUAL, 9),
    MAHAYANA("Великая Колесница", 35000.0, DaoWorld.SPIRITUAL, 9),
    TRIBULATION_CROSS("Преодоление Скорби", 160000.0, DaoWorld.SPIRITUAL, 10),
    NIRVANA("Перерождение Нирваны", 750000.0, DaoWorld.SPIRITUAL, 10),
    TRUE_IMMORTAL("Истинный Бессмертный", 3500000.0, DaoWorld.IMMORTAL, 12),
    GOLDEN_IMMORTAL("Золотой Бессмертный", 18000000.0, DaoWorld.IMMORTAL, 15),
    IMMORTAL_KING("Владыка Небес", 90000000.0, DaoWorld.IMMORTAL, 18),
    EMPEROR_DAO("Император Девяти Небес", 450000000.0, DaoWorld.IMMORTAL, 21),
    CHAOS_SOVEREIGN("Повелитель Хаоса", 2500000000.0, DaoWorld.CHAOS, 27),
    PRIMORDIAL_ANCESTOR("Первозданный Прародитель Дао", 15000000000.0, DaoWorld.CHAOS, 36)
}

enum class ItemGrade(val title: String, val color: Color, val borderGlow: Color) {
    MORTAL("Обычный", Color(0xFF8D99AE), Color(0xFF4A4E69)),
    SPIRIT("Духовный", Color(0xFF2EC4B6), Color(0xFF00F5D4)),
    EARTH("Земной", Color(0xFF3A86FF), Color(0xFF00B4D8)),
    HEAVEN("Небесный", Color(0xFF9D4EDD), Color(0xFFC77DFF)),
    IMMORTAL("Бессмертный", Color(0xFFFFB703), Color(0xFFFFD166)),
    CHAOS("Хаос", Color(0xFFFF0054), Color(0xFFFF5400))
}

enum class ItemCategory { WEAPON, ROBE, RING, TALISMAN, CURIO_CAULDRON, CURIO_MIRROR, PILL, SPIRIT_ROOT, DAO_FRUIT, CHEST }

data class RelicItem(
    val id: String,
    val name: String,
    val category: ItemCategory,
    val grade: ItemGrade,
    val power: Long,
    var refineLevel: Int = 0,
    val description: String = "",
    var count: Int = 1
)

data class SimulatedWhale(
    val name: String,
    val server: String,
    val realm: MajorRealm,
    val stage: Int,
    var power: Long,
    val vip: Int,
    val bannerTitle: String,
    val color: Color
)

// ==========================================
// 2. ДВИЖОК ПРОГРЕССИИ И ИГРОВОЕ СОСТОЯНИЕ
// ==========================================

class DaoMasterState {
    var heroName by mutableStateOf("Штормовой_Дао")
    var realmIndex by mutableIntStateOf(2) // Старт с Золотого Ядра
    var stage by mutableIntStateOf(3)      // Стадия 1..10
    var qiProgress by mutableFloatStateOf(0.42f)
    var aptitudeScore by mutableIntStateOf(100)

    var spiritStones by mutableLongStateOf(850000L)
    var etherealJade by mutableLongStateOf(2500L)
    var summonTokens by mutableIntStateOf(35)
    var vipLevel by mutableIntStateOf(3)

    val equippedSlots = mutableStateMapOf<ItemCategory, RelicItem>()
    val bagItems = mutableStateListOf<RelicItem>()
    val worldBots = mutableStateListOf<SimulatedWhale>()

    init {
        equippedSlots[ItemCategory.WEAPON] = RelicItem("w1", "Меч Пронзающий Облака", ItemCategory.WEAPON, ItemGrade.EARTH, 850000L, 115, "Физическая атака +45%")
        equippedSlots[ItemCategory.ROBE] = RelicItem("r1", "Мантия Звездного Шелка", ItemCategory.ROBE, ItemGrade.SPIRIT, 420000L, 85, "Снижение входящего урона")
        equippedSlots[ItemCategory.RING] = RelicItem("rg1", "Перстень Бездны", ItemCategory.RING, ItemGrade.EARTH, 600000L, 90, "Увеличивает вместимость Ци")
        equippedSlots[ItemCategory.CURIO_CAULDRON] = RelicItem("c1", "Котел Куй Девяти Зорь", ItemCategory.CURIO_CAULDRON, ItemGrade.HEAVEN, 3200000L, 179, "Пассивно варит пилюли")
        equippedSlots[ItemCategory.CURIO_MIRROR] = RelicItem("m1", "Зеркало Инь-Ян", ItemCategory.CURIO_MIRROR, ItemGrade.IMMORTAL, 9500000L, 210, "Отражает атаки врагов")

        bagItems.add(RelicItem("p1", "Пилюля Возвышения Золотого Ядра", ItemCategory.PILL, ItemGrade.EARTH, 0L, 0, "+25% к текущей полосе Ци", 5))
        bagItems.add(RelicItem("rt1", "Тысячелетний Женьшень Небес", ItemCategory.SPIRIT_ROOT, ItemGrade.HEAVEN, 0L, 0, "Навсегда увеличивает потенциал корней +15", 3))
        bagItems.add(RelicItem("f1", "Плод Мудрости Бодхи", ItemCategory.DAO_FRUIT, ItemGrade.IMMORTAL, 0L, 0, "+50% Ци и благословение Дао", 2))
        bagItems.add(RelicItem("ch1", "Древний Ларь Владыки Драконов", ItemCategory.CHEST, ItemGrade.HEAVEN, 0L, 0, "Содержит редчайшие реликвии и руду", 4))

        val prefixes = listOf("Лорд", "Патриарх", "Демонический Меч", "Бессмертный", "Старейшина", "Суверен")
        val names = listOf("Ван Линь", "Бай Сяочунь", "Ли Цие", "Гу Чэньша", "Су Мин", "Хань Ли", "Сяо Ян", "Мэн Хао")
        val servers = listOf("S-101", "S-102", "S-088", "S-001 (Кросс)")

        for (i in 1..40) {
            val isWhale = i <= 6
            val rIdx = if (isWhale) minOf(MajorRealm.values().size - 1, 6 + i / 2) else maxOf(1, (40 - i) / 3)
            val r = MajorRealm.values()[rIdx]
            val pow = (r.powerBase * 150000L * (1.0 + (40 - i) * 0.12)).toLong() + (if (isWhale) 500000000L else 0L)
            worldBots.add(
                SimulatedWhale(
                    name = "${prefixes.random()} ${names.random()}",
                    server = servers.random(),
                    realm = r,
                    stage = Random.nextInt(1, 10),
                    power = pow,
                    vip = if (isWhale) Random.nextInt(12, 17) else Random.nextInt(2, 6),
                    bannerTitle = if (isWhale) "🏆 ВЛАДЫКА СЕРВЕРА" else "Культиватор Межмирья",
                    color = if (isWhale) Color(0xFFFF0054) else Color(0xFF00F5D4)
                )
            )
        }
        worldBots.sortByDescending { it.power }
    }

    val currentRealm: MajorRealm get() = MajorRealm.values()[realmIndex]

    val patk: Long get() = ((currentRealm.powerBase * 125000L * stage) + equippedSlots.values.sumOf { it.power * 2 }).toLong()
    val matk: Long get() = ((currentRealm.powerBase * 195000L * stage) + equippedSlots.values.sumOf { (it.power * 2.8).toLong() }).toLong()
    val pdef: Long get() = (patk * 0.45).toLong()
    val mdef: Long get() = (matk * 0.52).toLong()
    val maxHp: Long get() = (patk * 18L) + 5000000L
    val maxMp: Long get() = (matk * 12L) + 3000000L

    val totalCombatPower: Long get() = patk + matk + pdef + mdef + (vipLevel * 25000000L)

    fun consumeItem(item: RelicItem) {
        when (item.category) {
            ItemCategory.PILL -> {
                qiProgress = minOf(1.0f, qiProgress + 0.25f)
            }
            ItemCategory.SPIRIT_ROOT -> {
                aptitudeScore += 15
            }
            ItemCategory.DAO_FRUIT -> {
                qiProgress = minOf(1.0f, qiProgress + 0.50f)
                aptitudeScore += 30
            }
            ItemCategory.CHEST -> {
                val drops = listOf(
                    RelicItem("drop_${Random.nextInt(9999)}", "Клинок Алого Пламени", ItemCategory.WEAPON, ItemGrade.HEAVEN, 4500000L, 120, "Оружие древнего дракона"),
                    RelicItem("drop_${Random.nextInt(9999)}", "Печать Птицы Феникс", ItemCategory.TALISMAN, ItemGrade.IMMORTAL, 12000000L, 150, "Божественная печать небес"),
                    RelicItem("drop_${Random.nextInt(9999)}", "Эссенция Первозданного Зверя", ItemCategory.DAO_FRUIT, ItemGrade.EARTH, 0L, 0, "+Потенциал Дао")
                )
                bagItems.add(drops.random())
                spiritStones += 150000L
            }
            else -> {}
        }

        if (item.count > 1) {
            val idx = bagItems.indexOf(item)
            if (idx != -1) bagItems[idx] = item.copy(count = item.count - 1)
        } else {
            bagItems.remove(item)
        }
    }

    fun performBreakthrough(): Boolean {
        if (qiProgress >= 1.0f) {
            if (stage < 10) {
                stage++
                qiProgress = 0.05f
            } else if (realmIndex < MajorRealm.values().size - 1) {
                realmIndex++
                stage = 1
                qiProgress = 0.02f
            }
            return true
        }
        return false
    }
}

fun formatNumberXianxia(value: Long): String {
    return when {
        value >= 1_000_000_000_000L -> String.format(Locale.US, "%.2fT", value / 1_000_000_000_000.0)
        value >= 1_000_000_000L -> String.format(Locale.US, "%.2fB", value / 1_000_000_000.0)
        value >= 1_000_000L -> String.format(Locale.US, "%.2fM", value / 1_000_000.0)
        value >= 1_000L -> String.format(Locale.US, "%.1fK", value / 1_000.0)
        else -> value.toString()
    }
}

// ==========================================
// 3. ПРОЦЕДУРНЫЕ ВЕКТОРНЫЕ ИКОНКИ (CANVAS)
// ==========================================

@Composable
fun ProceduralItemIcon(category: ItemCategory, grade: ItemGrade, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF10131E))
            .border(1.5.dp, grade.borderGlow, RoundedCornerShape(8.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2, h / 2)

            drawCircle(
                brush = Brush.radialGradient(listOf(grade.color.copy(alpha = 0.45f), Color.Transparent), center, w * 0.6f),
                radius = w * 0.5f
            )

            when (category) {
                ItemCategory.WEAPON -> {
                    drawLine(Color.White, Offset(w * 0.5f, h * 0.15f), Offset(w * 0.5f, h * 0.72f), strokeWidth = 3.dp.toPx())
                    drawLine(grade.borderGlow, Offset(w * 0.3f, h * 0.62f), Offset(w * 0.7f, h * 0.62f), strokeWidth = 2.dp.toPx())
                    drawLine(Color(0xFFFFD166), Offset(w * 0.5f, h * 0.72f), Offset(w * 0.5f, h * 0.88f), strokeWidth = 2.5.dp.toPx())
                    drawCircle(grade.borderGlow, 3.dp.toPx(), Offset(w * 0.5f, h * 0.15f))
                }
                ItemCategory.ROBE -> {
                    val path = Path().apply {
                        moveTo(w * 0.3f, h * 0.2f)
                        lineTo(w * 0.7f, h * 0.2f)
                        lineTo(w * 0.85f, h * 0.85f)
                        lineTo(w * 0.5f, h * 0.75f)
                        lineTo(w * 0.15f, h * 0.85f)
                        close()
                    }
                    drawPath(path, Brush.verticalGradient(listOf(grade.color, Color(0xFF0F172A))))
                    drawPath(path, grade.borderGlow, style = Stroke(1.5.dp.toPx()))
                }
                ItemCategory.CURIO_CAULDRON -> {
                    drawCircle(grade.color.copy(alpha = 0.7f), w * 0.28f, center)
                    drawLine(grade.borderGlow, Offset(w * 0.25f, h * 0.75f), Offset(w * 0.2f, h * 0.9f), 2.5.dp.toPx())
                    drawLine(grade.borderGlow, Offset(w * 0.75f, h * 0.75f), Offset(w * 0.8f, h * 0.9f), 2.5.dp.toPx())
                    drawArc(grade.borderGlow, 0f, 180f, false, Offset(w * 0.2f, h * 0.2f), Size(w * 0.6f, h * 0.25f), style = Stroke(2.dp.toPx()))
                }
                ItemCategory.CURIO_MIRROR, ItemCategory.RING -> {
                    drawCircle(grade.borderGlow, w * 0.32f, center, style = Stroke(2.5.dp.toPx()))
                    drawCircle(Color.White, w * 0.15f, center)
                }
                ItemCategory.PILL -> {
                    drawCircle(Brush.radialGradient(listOf(Color.White, grade.color)), w * 0.22f, center)
                    drawArc(grade.borderGlow, 45f, 270f, false, Offset(w * 0.15f, h * 0.25f), Size(w * 0.7f, h * 0.5f), style = Stroke(1.5.dp.toPx()))
                }
                ItemCategory.SPIRIT_ROOT, ItemCategory.DAO_FRUIT -> {
                    drawCircle(grade.color, w * 0.24f, Offset(w * 0.5f, h * 0.55f))
                    drawLine(Color(0xFF00F5D4), Offset(w * 0.5f, h * 0.55f), Offset(w * 0.5f, h * 0.2f), 2.dp.toPx())
                    drawCircle(Color(0xFF00F5D4), 3.dp.toPx(), Offset(w * 0.65f, h * 0.25f))
                }
                ItemCategory.CHEST, ItemCategory.TALISMAN -> {
                    drawRect(grade.color, Offset(w * 0.2f, h * 0.35f), Size(w * 0.6f, h * 0.45f))
                    drawRect(grade.borderGlow, Offset(w * 0.2f, h * 0.35f), Size(w * 0.6f, h * 0.45f), style = Stroke(1.5.dp.toPx()))
                    drawLine(Color(0xFFFFD166), Offset(w * 0.2f, h * 0.55f), Offset(w * 0.8f, h * 0.55f), 2.dp.toPx())
                    drawCircle(Color.White, 3.dp.toPx(), Offset(w * 0.5f, h * 0.55f))
                }
            }
        }
    }
}

// ==========================================
// 4. ГЛАВНЫЙ ЭКРАН И НАВИГАЦИОННОЕ ЯДРО
// ==========================================

@Composable
fun OvermortalCultivationEngine() {
    val state = remember { DaoMasterState() }
    var activeTab by remember { mutableIntStateOf(0) }
    var tribulationOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            val gain = 0.003f + (state.aptitudeScore * 0.00005f)
            state.qiProgress = minOf(1.0f, state.qiProgress + gain)

            if (Random.nextDouble() < 0.35) {
                val b = state.worldBots.random()
                b.power += Random.nextLong(150000L, 1200000L)
                state.worldBots.sortByDescending { it.power }
            }
        }
    }

    Scaffold(
        topBar = { OvermortalTopBar(state) },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0C0E17)) {
                val tabs = listOf(
                    "Персонаж" to "🥋",
                    "Обитель" to "☯️",
                    "Сундуки" to "🎁",
                    "Межмирье" to "⚔️",
                    "VIP Казна" to "💎"
                )
                tabs.forEachIndexed { idx, item ->
                    NavigationBarItem(
                        selected = activeTab == idx,
                        onClick = { activeTab = idx },
                        label = { Text(item.first, fontSize = 9.sp, fontWeight = if (activeTab == idx) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Text(item.second, fontSize = 15.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFFD166),
                            selectedTextColor = Color(0xFFFFD166),
                            indicatorColor = Color(0xFF1C2237)
                        )
                    )
                }
            }
        }
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF06070B))
                .padding(pad)
        ) {
            when (activeTab) {
                0 -> HeroOverviewScreen(state)
                1 -> AbodeMeditationScreen(state, onStartTribulation = { tribulationOpen = true })
                2 -> ChestAlchemyScreen(state)
                3 -> CrossWorldWarScreen(state)
                4 -> FreeVipTreasuryScreen(state)
            }

            if (tribulationOpen) {
                TribulationModalDialog(state, onDismiss = { tribulationOpen = false })
            }
        }
    }
}

@Composable
fun OvermortalTopBar(state: DaoMasterState) {
    Surface(
        color = Color(0xFF0E111C),
        modifier = Modifier.fillMaxWidth().border(0.5.dp, Color(0xFF1F263D))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xFFFF0054), Color(0xFF3A86FF))))
                            .border(1.5.dp, Color(0xFFFFD166), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("道", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(state.heroName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Badge(containerColor = Color(0xFFFF0054)) {
                                Text("VIP ${state.vipLevel}", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("${state.currentRealm.title} (G${state.stage})", color = Color(0xFFFFD166), fontSize = 10.sp)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("БОЕВАЯ МОЩЬ", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(
                        formatNumberXianxia(state.totalCombatPower),
                        color = Color(0xFF00F5D4),
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("💎 ${formatNumberXianxia(state.spiritStones)}", color = Color.LightGray, fontSize = 10.sp)
                Text("🏮 Нефрит: ${state.etherealJade}", color = Color(0xFF00F5D4), fontSize = 10.sp)
                Text("📜 Свитки: ${state.summonTokens}", color = Color(0xFFFFD166), fontSize = 10.sp)
                Text("🌌 ${state.currentRealm.world.title}", color = Color(0xFFC77DFF), fontSize = 10.sp)
            }
        }
    }
}

// ==========================================
// 5. ЭКРАН 1: ГЕРОЙ, РЕЛИКВИИ И ХАРАКТЕРИСТИКИ
// ==========================================

@Composable
fun HeroOverviewScreen(state: DaoMasterState) {
    var selectedToEnhance by remember { mutableStateOf<RelicItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(0.26f)) {
                EquipSlotCard(state.equippedSlots[ItemCategory.WEAPON], "Оружие") { selectedToEnhance = state.equippedSlots[ItemCategory.WEAPON] }
                EquipSlotCard(state.equippedSlots[ItemCategory.ROBE], "Мантия") { selectedToEnhance = state.equippedSlots[ItemCategory.ROBE] }
                EquipSlotCard(state.equippedSlots[ItemCategory.RING], "Перстень") { selectedToEnhance = state.equippedSlots[ItemCategory.RING] }
            }

            Box(
                modifier = Modifier
                    .weight(0.48f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2
                    val cy = size.height * 0.48f

                    drawCircle(
                        brush = Brush.radialGradient(listOf(Color(0xFFFF0054).copy(alpha = 0.25f), Color.Transparent), Offset(cx, cy), size.width * 0.6f),
                        radius = size.width * 0.55f
                    )
                    drawOval(Color(0xFF00F5D4).copy(alpha = 0.4f), Offset(cx - size.width * 0.4f, size.height * 0.85f), Size(size.width * 0.8f, 20.dp.toPx()), style = Stroke(2.dp.toPx()))

                    val robePath = Path().apply {
                        moveTo(cx, cy - 60f)
                        lineTo(cx + 45f, cy + 120f)
                        lineTo(cx + 65f, cy + 190f)
                        lineTo(cx - 65f, cy + 190f)
                        lineTo(cx - 45f, cy + 120f)
                        close()
                    }
                    drawPath(robePath, Color(0xFF141724))
                    drawPath(robePath, Color(0xFFFFD166), style = Stroke(1.5.dp.toPx()))

                    drawLine(Color(0xFF00F5D4), Offset(cx + 55f, cy - 90f), Offset(cx + 35f, cy + 60f), 3.dp.toPx())
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.BottomCenter)) {
                    Text("«${state.heroName}»", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Секта Небесного Меча", color = Color(0xFFFFD166), fontSize = 10.sp)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(0.26f)) {
                EquipSlotCard(state.equippedSlots[ItemCategory.CURIO_CAULDRON], "Котел Дао") { selectedToEnhance = state.equippedSlots[ItemCategory.CURIO_CAULDRON] }
                EquipSlotCard(state.equippedSlots[ItemCategory.CURIO_MIRROR], "Зеркало") { selectedToEnhance = state.equippedSlots[ItemCategory.CURIO_MIRROR] }
                EquipSlotCard(state.equippedSlots[ItemCategory.TALISMAN], "Талисман") { selectedToEnhance = state.equippedSlots[ItemCategory.TALISMAN] }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("HP: ${formatNumberXianxia(state.maxHp)}", color = Color(0xFFFF4D6D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("MP: ${formatNumberXianxia(state.maxMp)}", color = Color(0xFF38B000), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(2.dp))
            LinearProgressIndicator(progress = { 1.0f }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = Color(0xFFFF4D6D))
            Spacer(modifier = Modifier.height(2.dp))
            LinearProgressIndicator(progress = { 0.85f }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = Color(0xFF3A86FF))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F121F)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem("ФИЗ.АТК", formatNumberXianxia(state.patk), Color(0xFFFF5400))
                StatItem("МАГ.АТК", formatNumberXianxia(state.matk), Color(0xFF7209B7))
                StatItem("ФИЗ.ЗАЩ", formatNumberXianxia(state.pdef), Color(0xFF4CC9F0))
                StatItem("МАГ.ЗАЩ", formatNumberXianxia(state.mdef), Color(0xFF4895EF))
            }
        }

        selectedToEnhance?.let { item ->
            val cost = (item.refineLevel + 1) * 25000L
            AlertDialog(
                onDismissRequest = { selectedToEnhance = null },
                containerColor = Color(0xFF111422),
                title = { Text("ЗАКАЛКА РЕЛИКВИИ", color = Color(0xFFFFD166), fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(item.name, color = item.grade.color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Уровень закалки: +${item.refineLevel}", color = Color.White, fontSize = 11.sp)
                        Text("Стоимость: ${formatNumberXianxia(cost)} 💎", color = Color(0xFF00F5D4), fontSize = 11.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (state.spiritStones >= cost) {
                                state.spiritStones -= cost
                                item.refineLevel++
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB703))
                    ) {
                        Text("Закалить", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedToEnhance = null }) { Text("Закрыть", color = Color.Gray) }
                }
            )
        }
    }
}

@Composable
fun EquipSlotCard(item: RelicItem?, placeholder: String, onClick: () -> Unit) {
    if (item != null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
            Box(modifier = Modifier.size(54.dp)) {
                ProceduralItemIcon(category = item.category, grade = item.grade, modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(Color(0xFF0A0D18), RoundedCornerShape(4.dp))
                        .padding(horizontal = 2.dp)
                ) {
                    Text("+${item.refineLevel}", color = Color(0xFFFFD166), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(item.name.take(8), color = item.grade.color, fontSize = 9.sp, maxLines = 1)
        }
    } else {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(Color(0xFF0D101C), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1E2338), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(placeholder.take(4), color = Color.DarkGray, fontSize = 8.sp)
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

// ==========================================
// 6. ЭКРАН 2: ОБИТЕЛЬ МЕДИТАЦИИ (CULTIVATION)
// ==========================================

@Composable
fun AbodeMeditationScreen(state: DaoMasterState, onStartTribulation: () -> Unit) {
    val trans = rememberInfiniteTransition()
    val auraFloat by trans.animateFloat(0.95f, 1.05f, infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ТАИНСТВО ОБИТЕЛИ (МЕДИТАЦИЯ ДАО)", color = Color(0xFFFFD166), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("Потенциал корней: ${state.aptitudeScore} • Скорость сбора Ци: +${(10 + state.aptitudeScore / 5)}/сек", color = Color.Gray, fontSize = 10.sp)

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF090C16)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val cy = size.height * 0.6f

                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0xFF00F5D4).copy(alpha = 0.35f), Color.Transparent), Offset(cx, cy - 70f), size.width * 0.5f),
                    radius = size.width * 0.45f * auraFloat
                )

                drawOval(Color(0xFF2EC4B6), Offset(cx - 75f, cy + 30f), Size(150f, 35f), style = Stroke(2.dp.toPx()))
                drawOval(Color(0xFF101726), Offset(cx - 70f, cy + 32f), Size(140f, 30f))

                drawCircle(Color(0xFF00F5D4), 14f, Offset(cx, cy - 25f))
                val bodyPath = Path().apply {
                    moveTo(cx, cy - 10f)
                    lineTo(cx + 30f, cy + 28f)
                    lineTo(cx - 30f, cy + 28f)
                    close()
                }
                drawPath(bodyPath, Color(0xFF1E2538))
                drawPath(bodyPath, Color(0xFFFFD166), style = Stroke(1.5.dp.toPx()))
            }

            Text("🧘", fontSize = 48.sp, modifier = Modifier.offset(y = (-8).dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("Прогресс слияния с Ци: ${(state.qiProgress * 100).toInt()}%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { state.qiProgress },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
            color = Color(0xFF00F5D4),
            trackColor = Color(0xFF151B2B)
        )

        Spacer(modifier = Modifier.height(14.dp))

        val canBreak = state.qiProgress >= 1.0f
        Button(
            onClick = {
                if (canBreak) onStartTribulation()
            },
            enabled = canBreak,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166), disabledContainerColor = Color(0xFF1C2234))
        ) {
            Text(
                if (canBreak) "⚡ СОВЕРШИТЬ НЕБЕСНЫЙ ПРОРЫВ" else "ПРИМЕНИТЕ ПИЛЮЛИ ДЛЯ УСКОРЕНИЯ",
                color = if (canBreak) Color.Black else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text("Медитация происходит пассивно. Для ускорения используйте вкладку 'Сундуки'", color = Color.Gray, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

// Модальное окно Небесной Скорби с молниями
@Composable
fun TribulationModalDialog(state: DaoMasterState, onDismiss: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    val maxStep = state.currentRealm.tribulationBolts
    var log by remember { mutableStateOf("Небесные молнии готовы обрушиться на вас!") }
    var ended by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (ended) onDismiss() },
        containerColor = Color(0xFF101322),
        title = { Text("⚡ СКОРБЬ НЕБЕС", color = Color(0xFFFFD166), fontSize = 15.sp, fontWeight = FontWeight.Bold) },
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
                    log = "Молния №$step отражена золотым телом Дао!"
                    if (step >= maxStep) {
                        state.performBreakthrough()
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

// ==========================================
// 7. ЭКРАН 3: СУНДУКИ, ПИЛЮЛИ И ПЛОДЫ
// ==========================================

@Composable
fun ChestAlchemyScreen(state: DaoMasterState) {
    var actionNotice by remember { mutableStateOf("Открывайте сундуки боссов и вкушайте плоды Дао.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text("СОКРОВИЩНИЦА, ПИЛЮЛИ И ЛАРИ", color = Color(0xFFFFD166), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(actionNotice, color = Color(0xFF00F5D4), fontSize = 10.sp)

        Spacer(modifier = Modifier.height(10.dp))

        if (state.bagItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFF0C0F1A), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Рюкзак пуст. Выбивайте лари в Межмирье!", color = Color.Gray, fontSize = 11.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.bagItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111422)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(46.dp)) {
                                    ProceduralItemIcon(item.category, item.grade, Modifier.fillMaxSize())
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("${item.name} (${item.count}x)", color = item.grade.color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(item.description, color = Color.LightGray, fontSize = 9.sp)
                                    Text("[${item.grade.title}] • ${item.category.name}", color = Color.Gray, fontSize = 8.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    state.consumeItem(item)
                                    actionNotice = "Использован предмет: [${item.name}]!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (item.category == ItemCategory.CHEST) Color(0xFFFF0054) else Color(0xFF2EC4B6)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(if (item.category == ItemCategory.CHEST) "Открыть" else "Вкусить", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val chest = RelicItem("ch_${Random.nextInt(9999)}", "Сундук Древнего Демона", ItemCategory.CHEST, ItemGrade.EARTH, 0L, 0, "Содержит пилюли прорыва и духовную руду", 1)
                state.bagItems.add(chest)
                actionNotice = "⚔️ Вы сокрушили чудовище бездны и добыли [${chest.name}]!"
            },
            modifier = Modifier.fillMaxWidth().height(42.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A86FF))
        ) {
            Text("⚔️ Отправиться на охоту за Сундуком", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 8. ЭКРАН 4: МЕЖМИРОВЫЕ ВОЙНЫ И ЖИВЫЕ БОТЫ
// ==========================================

@Composable
fun CrossWorldWarScreen(state: DaoMasterState) {
    var warLog by remember { mutableStateOf("Межмировой турнир открыт! Топ-киты серверов захватили жилы.") }
    var duelingBot by remember { mutableStateOf<SimulatedWhale?>(null) }
    val trans = rememberInfiniteTransition()
    val swordX by trans.animateFloat(-1f, 1f, infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text("МЕЖМИРОВОЙ ТУРНИР (CROSS-SERVER DAO WAR)", color = Color(0xFFFFD166), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(warLog, color = Color(0xFF00F5D4), fontSize = 10.sp)

        Spacer(modifier = Modifier.height(10.dp))

        if (duelingBot != null) {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp).background(Color(0xFF131726), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cy = size.height / 2
                    drawCircle(Color(0xFF00F5D4), 16.dp.toPx(), Offset(size.width * 0.2f, cy))
                    drawCircle(duelingBot!!.color, 16.dp.toPx(), Offset(size.width * 0.8f, cy))
                    val sx = (size.width * 0.5f) + (size.width * 0.28f) * swordX
                    drawLine(Color(0xFFFFD166), Offset(sx - 10f, cy), Offset(sx + 10f, cy), 3.dp.toPx())
                }
                Text("⚔️ ДУЭЛЬ МЕЖМИРЬЯ ⚔️", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(state.worldBots) { bot ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F121E)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(bot.name, color = bot.color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge(containerColor = Color(0xFF4A148C)) {
                                    Text(bot.server, color = Color.White, fontSize = 7.sp)
                                }
                            }
                            Text("${bot.realm.title} • Мощь: ${formatNumberXianxia(bot.power)}", color = Color.Gray, fontSize = 9.sp)
                        }

                        Button(
                            onClick = {
                                duelingBot = bot
                                if (state.totalCombatPower >= bot.power) {
                                    state.spiritStones += 250000L
                                    warLog = "🏆 Вы победили ${bot.name} с сервера ${bot.server}! Захвачено 250,000 💎!"
                                } else {
                                    warLog = "💀 ${bot.name} сокрушил вашу проекцию волей Небес!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1121F)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Вызов", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. ЭКРАН 5: БЕСПЛАТНАЯ VIP КАЗНА
// ==========================================

@Composable
fun FreeVipTreasuryScreen(state: DaoMasterState) {
    var treasuryLog by remember { mutableStateOf("") }
    val packs = listOf(
        Triple("Дар Патриарха Дао", "990 ₽ (БЕСПЛАТНО)", Pair(5000000L, 50)),
        Triple("Казна Императора Девяти Небес", "4 990 ₽ (БЕСПЛАТНО)", Pair(50000000L, 250)),
        Triple("Сверхсокровище Первозданного Кибера", "9 990 ₽ (БЕСПЛАТНО)", Pair(500000000L, 1000))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text("НЕБЕСНАЯ КАЗНА (БЕСПЛАТНЫЙ VIP ДЛЯ ВАС)", color = Color(0xFFFFD166), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("Реальные платежи отключены. Получайте ресурсы китов бесплатно.", color = Color.Gray, fontSize = 10.sp)
        if (treasuryLog.isNotEmpty()) Text(treasuryLog, color = Color.Green, fontSize = 10.sp)

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(packs) { p ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111524)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(p.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Badge(containerColor = Color(0xFFFF0054)) { Text("-100%", color = Color.White) }
                        }
                        Text("• Камни: +${formatNumberXianxia(p.third.first)} 💎 | Свитки: +${p.third.second} 📜", color = Color(0xFF00F5D4), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                state.spiritStones += p.third.first
                                state.summonTokens += p.third.second
                                state.vipLevel = minOf(18, state.vipLevel + 3)
                                state.bagItems.add(RelicItem("vip_ch_${Random.nextInt(9999)}", "Императорский Сундук Хаоса", ItemCategory.CHEST, ItemGrade.CHAOS, 0L, 0, "Содержит бессмертные реликвии"))
                                treasuryLog = "🎉 Получен ${p.first}! Уровень VIP вырос до ${state.vipLevel}!"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB703))
                        ) {
                            Text("ЗАБРАТЬ: ${p.second}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
// КОНЕЦ ФАЙЛА
