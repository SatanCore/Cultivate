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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
            XianxiaAppTheme {
                MainGameEngine()
            }
        }
    }
}

@Composable
fun XianxiaAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF07090E),
            surface = Color(0xFF111420),
            primary = Color(0xFFFFD700),
            secondary = Color(0xFF00E5FF)
        ),
        content = content
    )
}

// ==========================================
// 1. КОСМОЛОГИЯ, КЛАССЫ И МОДЕЛИ
// ==========================================

enum class CultivatorClass(val title: String, val desc: String, val atkMod: Double, val defMod: Double, val hpMod: Double, val critMod: Double) {
    SWORD_SAINT("Бессмертный Мечник", "Пронзающие техники, высокий крит и летающие мечи", 1.5, 0.9, 1.0, 0.25),
    BODY_REFINER("Закалка Тела", "Непоколебимая защита, гигантский запас ОЗ и щиты", 0.9, 1.6, 1.8, 0.05),
    SPIRIT_WARLOCK("Духовный Маг", "Манипуляция стихиями, барьеры и взрывной урон", 1.6, 0.8, 0.9, 0.15),
    BLOOD_LORD("Кровавый Лорд", "Темный вампиризм Дао и поглощение жизненных сил", 1.2, 1.1, 1.2, 0.12)
}

enum class MortalWorld(val title: String) {
    MORTAL("Мир Смертных"),
    SPIRITUAL("Духовный Мир"),
    IMMORTAL("Небесный Мир")
}

enum class CultivationRealm(val title: String, val world: MortalWorld, val mult: Double, val tribulationBolts: Int) {
    QI_CONDENSATION("Конденсация Ци", MortalWorld.MORTAL, 1.0, 3),
    FOUNDATION("Возведение Основания", MortalWorld.MORTAL, 3.5, 4),
    CORE_FORMATION("Золотое Ядро", MortalWorld.MORTAL, 12.0, 5),
    NASCENT_SOUL("Зарождение Души", MortalWorld.SPIRITUAL, 45.0, 6),
    SOUL_TRANSFORM("Трансформация Духа", MortalWorld.SPIRITUAL, 180.0, 7),
    VOID_REFINING("Очищение Пустоты", MortalWorld.SPIRITUAL, 800.0, 8),
    MAHAYANA("Великая Колесница", MortalWorld.IMMORTAL, 3500.0, 9),
    ASCENSION("Истинный Бессмертный", MortalWorld.IMMORTAL, 15000.0, 12)
}

enum class ItemRarity(val title: String, val color: Color, val powerMultiplier: Double) {
    COMMON("Обычный", Color(0xFF9E9E9E), 1.0),
    SPIRITUAL("Духовный", Color(0xFF4CAF50), 2.5),
    EARTH("Земной", Color(0xFF2196F3), 6.0),
    HEAVEN("Небесный", Color(0xFFAB47BC), 16.0),
    IMMORTAL("Бессмертный", Color(0xFFFFB300), 45.0)
}

enum class EquipmentSlot(val title: String) {
    WEAPON("Оружие"),
    ARMOR("Мантия"),
    RING("Кольцо Духа"),
    BOOTS("Сапоги Ветра"),
    ARTIFACT("Артефакт"),
    PET("Зверь-Спутник")
}

data class Equipment(
    val id: String,
    val name: String,
    val slot: EquipmentSlot,
    val rarity: ItemRarity,
    val bonusAtk: Long,
    val bonusDef: Long,
    val bonusHp: Long,
    var enhanceLevel: Int = 0
)

data class BossEntity(
    val id: String,
    val name: String,
    val title: String,
    val hp: Long,
    val atk: Long,
    val rewardStones: Long,
    val dropRarity: ItemRarity
)

data class BotOpponent(
    val id: String,
    val name: String,
    val realm: CultivationRealm,
    val stage: Int,
    var combatPower: Long,
    val vip: Int,
    val avatarColor: Color,
    val title: String
)

// ==========================================
// 2. ГЛАВНОЕ СОСТОЯНИЕ ИГРЫ
// ==========================================

class GameState {
    var heroName by mutableStateOf("Хаос_Дао")
    var heroClass by mutableStateOf(CultivatorClass.SWORD_SAINT)
    var currentRealmIndex by mutableIntStateOf(0)
    var currentStage by mutableIntStateOf(1) // 1 - 9 стадии
    var currentQi by mutableLongStateOf(0L)
    var requiredQi by mutableLongStateOf(100L)

    // Валюта и ресурсы
    var spiritStones by mutableLongStateOf(25000L)
    var gachaSummonScrolls by mutableIntStateOf(20)
    var celestialJade by mutableLongStateOf(100L)
    var vipPoints by mutableIntStateOf(250)
    var vipLevel by mutableIntStateOf(2)

    // Дао-полярность: Праведность vs Демонизм
    var righteousPoints by mutableLongStateOf(150L)
    var demonicPoints by mutableLongStateOf(80L)

    // Экипировка
    val equippedItems = mutableStateMapOf<EquipmentSlot, Equipment>()
    val inventory = mutableStateListOf<Equipment>()

    // Боты и Рейтинг
    val ladderBots = mutableStateListOf<BotOpponent>()

    // Инициализация стартовой экипировки и ботов
    init {
        equippedItems[EquipmentSlot.WEAPON] = Equipment("start_w", "Меч Зеленой Стали", EquipmentSlot.WEAPON, ItemRarity.COMMON, 50, 0, 100, 1)
        equippedItems[EquipmentSlot.ARMOR] = Equipment("start_a", "Одеяние Ученика", EquipmentSlot.ARMOR, ItemRarity.COMMON, 0, 30, 250, 0)

        // Заполняем агрессивных ботов
        val botTitles = listOf("Патриарх", "Старейшина Пика", "Мечник Клана", "Демонический Бог", "Бессмертный Владыка")
        val botNames = listOf("Бай Сяо", "Ван Линь", "Ли Цие", "Гу Чэнь", "Су Мин", "Хань Ли", "Е Фань")
        val realms = CultivationRealm.values()

        for (i in 1..25) {
            val isWhale = i <= 5
            val r = if (isWhale) realms[minOf(realms.size - 1, 4 + i % 3)] else realms[maxOf(0, (25 - i) / 5)]
            ladderBots.add(
                BotOpponent(
                    id = "bot_$i",
                    name = "${botTitles.random()} ${botNames.random()}",
                    realm = r,
                    stage = Random.nextInt(1, 10),
                    combatPower = (r.mult * 3000 * (1.0 + (25 - i) * 0.15)).toLong() + (if (isWhale) 100000L else 0L),
                    vip = if (isWhale) Random.nextInt(10, 16) else Random.nextInt(1, 5),
                    avatarColor = if (isWhale) Color(0xFFFF5252) else Color(0xFF64B5F6),
                    title = if (isWhale) "🔥 ТОП ДОНАТЕР" else "Соперник Дао"
                )
            )
        }
        ladderBots.sortByDescending { it.combatPower }
    }

    // Расчет боевых характеристик героя
    val totalAttack: Long
        get() {
            val base = 40.0 * (currentRealmIndex + 1) * currentStage * heroClass.atkMod
            val equip = equippedItems.values.sumOf { (it.bonusAtk * it.rarity.powerMultiplier * (1.0 + it.enhanceLevel * 0.2)).toLong() }
            val vipBonus = vipLevel * 150L
            return (base + equip + vipBonus).toLong()
        }

    val totalDefense: Long
        get() {
            val base = 25.0 * (currentRealmIndex + 1) * currentStage * heroClass.defMod
            val equip = equippedItems.values.sumOf { (it.bonusDef * it.rarity.powerMultiplier * (1.0 + it.enhanceLevel * 0.2)).toLong() }
            return (base + equip).toLong()
        }

    val totalHp: Long
        get() {
            val base = 300.0 * (currentRealmIndex + 1) * currentStage * heroClass.hpMod
            val equip = equippedItems.values.sumOf { (it.bonusHp * it.rarity.powerMultiplier * (1.0 + it.enhanceLevel * 0.2)).toLong() }
            return (base + equip).toLong()
        }

    val combatPower: Long
        get() {
            val currentRealm = CultivationRealm.values()[currentRealmIndex]
            val raw = (totalAttack * 4.0) + (totalDefense * 2.5) + (totalHp * 0.8)
            val realmMod = currentRealm.mult * (1.0 + currentStage * 0.12)
            val cosmeticBonus = 5000L
            return (raw * realmMod + cosmeticBonus).toLong()
        }

    fun addQi(amount: Long) {
        currentQi += amount
    }

    fun canBreakthrough(): Boolean = currentQi >= requiredQi

    fun advanceStage() {
        if (currentStage < 9) {
            currentStage++
            currentQi = 0L
            requiredQi = (requiredQi * 1.6).toLong()
        } else if (currentRealmIndex < CultivationRealm.values().size - 1) {
            currentRealmIndex++
            currentStage = 1
            currentQi = 0L
            requiredQi = (requiredQi * 2.5).toLong()
        }
    }
}

// ==========================================
// 3. ОСНОВНОЙ КОМПОНЕНТ ЭКРАНА
// ==========================================

@Composable
fun MainGameEngine() {
    val state = remember { GameState() }
    var activeTab by remember { mutableIntStateOf(0) }
    var showTribulationDialog by remember { mutableStateOf(false) }

    // Фоновый цикл медитации: приток Ци каждую секунду
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            val baseGain = 15L * (state.currentRealmIndex + 1) * (1 + state.vipLevel / 2)
            state.addQi(baseGain)

            // Раз в 8 секунд боты немного увеличивают БМ
            if (Random.nextDouble() < 0.25) {
                val luckyBot = state.ladderBots.random()
                luckyBot.combatPower += Random.nextLong(200, 1500)
                state.ladderBots.sortByDescending { it.combatPower }
            }
        }
    }

    Scaffold(
        topBar = { TopCultivationHeader(state) },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0D101A)) {
                val navItems = listOf(
                    "Обитель" to "☯️",
                    "Герой" to "🥋",
                    "Арена" to "⚔️",
                    "Боссы" to "👹",
                    "Гача" to "🔮",
                    "Донат" to "💎"
                )
                navItems.forEachIndexed { idx, item ->
                    NavigationBarItem(
                        selected = activeTab == idx,
                        onClick = { activeTab = idx },
                        label = { Text(item.first, fontSize = 9.sp, fontWeight = if (activeTab == idx) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Text(item.second, fontSize = 16.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFFD700),
                            selectedTextColor = Color(0xFFFFD700),
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF1E2438)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF07090E))
                .padding(padding)
        ) {
            when (activeTab) {
                0 -> SanctuaryScreen(state, onTriggerTribulation = { showTribulationDialog = true })
                1 -> CharacterInventoryScreen(state)
                2 -> LiveArenaScreen(state)
                3 -> BossForgeScreen(state)
                4 -> MultiBannerGachaScreen(state)
                5 -> FreeWhaleStoreScreen(state)
            }

            if (showTribulationDialog) {
                TribulationModal(state, onDismiss = { showTribulationDialog = false })
            }
        }
    }
}

// ==========================================
// 4. ВЕРХНЯЯ СТАТУСНАЯ ПАНЕЛЬ ХАРАКТЕРИСТИК
// ==========================================

@Composable
fun TopCultivationHeader(state: GameState) {
    val currentRealm = CultivationRealm.values()[state.currentRealmIndex]

    Surface(
        color = Color(0xFF0F1320),
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth().border(0.5.dp, Color(0xFF232B40))
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xFFFFD700), Color(0xFF4A148C))))
                            .border(1.5.dp, Color(0xFFFFD700), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("仙", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(state.heroName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Badge(containerColor = Color(0xFFD50000)) {
                                Text("VIP ${state.vipLevel}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("${currentRealm.title} • Стадия ${state.currentStage}", color = Color(0xFFFFD700), fontSize = 11.sp)
                    }
                }

                // Боевая мощь
                Column(horizontalAlignment = Alignment.End) {
                    Text("БОЕВАЯ МОЩЬ", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${state.combatPower}",
                        color = Color(0xFF00E676),
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Панель ресурсов: Камни, Свитки, Нефрит
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ResourceBadge(icon = "💎", value = "${state.spiritStones}", label = "Камни")
                ResourceBadge(icon = "📜", value = "${state.gachaSummonScrolls}", label = "Свитки")
                ResourceBadge(icon = "🏮", value = currentRealm.world.title, label = "Мир")
            }
        }
    }
}

@Composable
fun ResourceBadge(icon: String, value: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color(0xFF151A2C), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(icon, fontSize = 11.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(value, color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// ==========================================
// 5. ЭКРАН 1: ОБИТЕЛЬ (ИНТЕРАКТИВНОЕ БАГУА)
// ==========================================

@Composable
fun SanctuaryScreen(state: GameState, onTriggerTribulation: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(20000, easing = LinearEasing), repeatMode = RepeatMode.Restart)
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "ТАИНСТВО МЕДИТАЦИИ ДАО",
            color = Color(0xFFFFD700),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            letterSpacing = 1.sp
        )
        Text(
            "Класс: ${state.heroClass.title} • Приток: +${15 * (state.currentRealmIndex + 1)} Ци/сек",
            color = Color.LightGray,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Графический холст (Canvas): Вращающаяся печать Багуа и сгустки энергии Ци
        Box(
            modifier = Modifier
                .size(230.dp)
                .clickable {
                    state.addQi(10L * (state.currentRealmIndex + 1))
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = (size.minDimension / 2) * pulseScale

                // Внешний круг барьера
                drawCircle(
                    brush = Brush.sweepGradient(listOf(Color(0xFF00E5FF), Color(0xFFFFD700), Color(0xFFD500F9), Color(0xFF00E5FF))),
                    radius = radius,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Вращающиеся кольца печати Дао
                drawCircle(
                    color = Color(0xFF263238),
                    radius = radius * 0.82f,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Рисуем лучи и триграммы
                val count = 8
                for (i in 0 until count) {
                    val angle = Math.toRadians((rotation + i * (360f / count)).toDouble())
                    val start = Offset(
                        (center.x + (radius * 0.65f) * cos(angle)).toFloat(),
                        (center.y + (radius * 0.65f) * sin(angle)).toFloat()
                    )
                    val end = Offset(
                        (center.x + (radius * 0.82f) * cos(angle)).toFloat(),
                        (center.y + (radius * 0.82f) * sin(angle)).toFloat()
                    )
                    drawLine(color = Color(0xFFFFD700), start = start, end = end, strokeWidth = 2.dp.toPx())
                }

                // Внутреннее свечение ауры героя
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFD700).copy(alpha = 0.35f), Color.Transparent),
                        center = center,
                        radius = radius * 0.6f
                    )
                )
            }

            // Иконка медитирующего мастера в центре
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🧘", fontSize = 48.sp)
                Text("Нажмите для Ци", color = Color(0xFF80D8FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Шкала накопления Ци к следующему прорыву
        val progress = (state.currentQi.toFloat() / state.req
