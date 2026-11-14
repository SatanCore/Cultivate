package com.cultivation.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private val Ink = Color(0xFF101820)
private val Panel = Color(0xFF1B2A32)
private val Gold = Color(0xFFE5BD72)
private val Jade = Color(0xFF78C6A3)
private const val QI_INTERVAL_SECONDS = 5L
private const val OFFLINE_CAP_SECONDS = 8L * 60L * 60L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CultivateApp() }
    }
}

@Composable
private fun CultivateApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("cultivate_save", 0) }
    val now = remember { System.currentTimeMillis() }
    val previousVisit = remember { prefs.getLong("last_seen", now) }
    val offlineSeconds = ((now - previousVisit) / 1000L).coerceIn(0L, OFFLINE_CAP_SECONDS)
    val restoredQi = (offlineSeconds / QI_INTERVAL_SECONDS).toInt()

    var qi by remember {
        mutableIntStateOf((prefs.getInt("qi", 0) + restoredQi).coerceAtMost(9999))
    }
    var stones by remember { mutableIntStateOf(prefs.getInt("stones", 120)) }
    var stage by remember { mutableIntStateOf(prefs.getInt("stage", 1).coerceAtLeast(1)) }
    var petLevel by remember { mutableIntStateOf(prefs.getInt("pet", 1).coerceAtLeast(1)) }
    var bossLevel by remember { mutableIntStateOf(prefs.getInt("boss_level", 1).coerceAtLeast(1)) }
    var enemyHp by remember { mutableIntStateOf(prefs.getInt("enemy", 100).coerceAtLeast(0)) }
    var wins by remember { mutableIntStateOf(prefs.getInt("wins", 0).coerceAtLeast(0)) }
    var selectedTechnique by remember { mutableIntStateOf(prefs.getInt("selected_technique", 0).coerceIn(0, 2)) }
    var swordLevel by remember { mutableIntStateOf(prefs.getInt("tech_sword", 1).coerceAtLeast(1)) }
    var lotusLevel by remember { mutableIntStateOf(prefs.getInt("tech_lotus", 1).coerceAtLeast(1)) }
    var foxLevel by remember { mutableIntStateOf(prefs.getInt("tech_fox", 1).coerceAtLeast(1)) }
    var chosenClass by remember { mutableIntStateOf(prefs.getInt("chosen_class", -1).coerceIn(-1, 3)) }
    var skillPoints by remember { mutableIntStateOf(prefs.getInt("skill_points", 0).coerceAtLeast(0)) }
    val skillLevels = remember { mutableStateListOf<Int>().apply { repeat(12) { add(prefs.getInt("skill_$it", 0).coerceIn(0, 5)) } } }
    var chosenSect by remember { mutableIntStateOf(prefs.getInt("chosen_sect", -1).coerceIn(-1, 7)) }
    var chosenAlliance by remember { mutableIntStateOf(prefs.getInt("chosen_alliance", -1).coerceIn(-1, 1)) }
    var rareSkill by remember { mutableStateOf(prefs.getString("rare_skill", "") ?: "") }
    var message by remember {
        mutableStateOf(
            if (restoredQi > 0) "За время отсутствия восстановлено $restoredQi Ци. Путь продолжается!"
            else "Сосредоточься. Путь бессмертного начинается с одного вдоха."
        )
    }

    val enemyMaxHp = bossMaxHp(bossLevel)
    val classBonus = if (chosenClass < 0) 0 else 10 + (chosenClass * 3 until chosenClass * 3 + 3).sumOf { skillLevels[it] } * 5
    val activeTechniqueLevel = when (selectedTechnique) {
        0 -> swordLevel
        1 -> lotusLevel
        else -> foxLevel
    }
    val activeTechniqueName = techniqueName(selectedTechnique)
    val techniqueDamage = when (selectedTechnique) {
        0 -> 18 + swordLevel * 12
        1 -> 10 + lotusLevel * 8
        else -> 14 + foxLevel * 10
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(QI_INTERVAL_SECONDS * 1000L)
            qi = (qi + 1).coerceAtMost(9999)
        }
    }

    LaunchedEffect(qi, stones, stage, petLevel, bossLevel, enemyHp, wins, selectedTechnique, swordLevel, lotusLevel, foxLevel, chosenClass, skillPoints, skillLevels.toList(), chosenSect, chosenAlliance, rareSkill) {
        prefs.edit()
            .putInt("qi", qi)
            .putInt("stones", stones)
            .putInt("stage", stage)
            .putInt("pet", petLevel)
            .putInt("boss_level", bossLevel)
            .putInt("enemy", enemyHp)
            .putInt("wins", wins)
            .putInt("selected_technique", selectedTechnique)
            .putInt("tech_sword", swordLevel)
            .putInt("tech_lotus", lotusLevel)
            .putInt("tech_fox", foxLevel)
            .putInt("chosen_class", chosenClass)
            .putInt("skill_points", skillPoints)
            .putInt("chosen_sect", chosenSect)
            .putInt("chosen_alliance", chosenAlliance)
            .putString("rare_skill", rareSkill)
            .also { editor -> skillLevels.forEachIndexed { index, level -> editor.putInt("skill_$index", level) } }
            .putLong("last_seen", System.currentTimeMillis())
            .apply()
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Gold,
            secondary = Jade,
            background = Ink,
            surface = Panel,
            onBackground = Color(0xFFF0E8D8),
            onSurface = Color(0xFFF0E8D8)
        )
    ) {
        Column(
            Modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState()).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("ПУТЬ БЕССМЕРТНОГО", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Cultivate", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Text("Тихая сила рождается внутри.", color = Jade)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Ци", qi.toString(), Modifier.weight(1f))
                StatCard("Камни духа", stones.toString(), Modifier.weight(1f))
                StatCard("Победы", wins.toString(), Modifier.weight(1f))
            }

            GameCard("Культивация", "Ступень $stage · ${stageName(stage)}") {
                val breakthroughCost = stage * 35
                Text(
                    "Ци накапливается даже в твоё отсутствие — до 8 часов. Для прорыва требуется $breakthroughCost Ци.",
                    color = Color.LightGray
                )
                LinearProgressIndicator(
                    progress = { (qi.toFloat() / breakthroughCost.coerceAtLeast(1)).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = Jade
                )
                Button(
                    onClick = {
                        if (qi >= breakthroughCost) {
                            qi -= breakthroughCost
                            stage++
                            skillPoints++
                            stones += 25
                            message = "Прорыв свершился! Ты достиг ступени $stage."
                        } else {
                            message = "Недостаточно Ци. Продолжай медитировать."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Совершить прорыв") }
            }

            GameCard("Арена испытаний", "Страж святилища · ранг $bossLevel") {
                Text("Здоровье противника: $enemyHp / $enemyMaxHp", color = Color.LightGray)
                LinearProgressIndicator(
                    progress = { (enemyHp.toFloat() / enemyMaxHp).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFD77A68)
                )
                Button(
                    onClick = {
                        if (enemyHp <= 0) {
                            message = "Страж повержен. Начни следующий бой."
                        } else {
                            val damage = 22 + stage * 3 + petLevel * 2 + techniqueDamage + classBonus
                            enemyHp = (enemyHp - damage).coerceAtLeast(0)
                            if (enemyHp == 0) {
                                val reward = 80 + bossLevel * 15 + stage * 5
                                stones += reward
                                wins++
                                skillPoints++
                                if (chosenClass >= 0 && rareSkill.isBlank() && Random.nextDouble() < 0.008) { rareSkill = rareArts[chosenClass]; message = "Редкое наследие пробудилось: $rareSkill" }
                                else message = "Победа! Получено $reward камней духа и 1 очко навыков."
                            } else {
                                message = "$activeTechniqueName нанесла $damage урона."
                            }
                        }
                    },
                    enabled = enemyHp > 0,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Удар потоком Ци") }

                Button(
                    onClick = {
                        if (enemyHp <= 0) {
                            bossLevel++
                            enemyHp = bossMaxHp(bossLevel)
                            message = "На арену вышел страж ранга $bossLevel. Будь осторожен!"
                        } else {
                            message = "Сначала одолей текущего стража."
                        }
                    },
                    enabled = enemyHp <= 0,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Вызвать следующего стража") }
            }

            GameCard("Боевые техники", "Выбранная техника: $activeTechniqueName · мастерство $activeTechniqueLevel") {
                Text("Техника усиливает каждый удар на арене. Выбери стиль боя и развивай его за камни духа.", color = Color.LightGray)
                TechniqueRow("Меч рассекающей Ци", "Сильный прямой удар", selectedTechnique == 0, swordLevel, techniqueUpgradeCost(swordLevel), stones,
    onSelect = { selectedTechnique = 0 },
    onUpgrade = { val cost = techniqueUpgradeCost(swordLevel); if (stones >= cost) { stones -= cost; swordLevel++; message = "Мастерство меча повышено до $swordLevel." } else message = "Не хватает камней духа для улучшения." }
)
TechniqueRow("Нефритовый лотос", "Устойчивая техника духовной энергии", selectedTechnique == 1, lotusLevel, techniqueUpgradeCost(lotusLevel), stones,
    onSelect = { selectedTechnique = 1 },
    onUpgrade = { val cost = techniqueUpgradeCost(lotusLevel); if (stones >= cost) { stones -= cost; lotusLevel++; message = "Мастерство лотоса повышено до $lotusLevel." } else message = "Не хватает камней духа для улучшения." }
)
TechniqueRow("Коготь лиса", "Быстрая атака в союзе со спутником", selectedTechnique == 2, foxLevel, techniqueUpgradeCost(foxLevel), stones,
    onSelect = { selectedTechnique = 2 },
    onUpgrade = { val cost = techniqueUpgradeCost(foxLevel); if (stones >= cost) { stones -= cost; foxLevel++; message = "Мастерство когтя лиса повышено до $foxLevel." } else message = "Не хватает камней духа для улучшения." }
)
                                Text("Дополнительный урон выбранной техники: +$techniqueDamage", color = Gold, fontWeight = FontWeight.Bold)
            }

            GameCard("Классы и ветки навыков", if (chosenClass < 0) "Выбери свой путь" else classNames[chosenClass]) {
                Text("Очки навыков: $skillPoints · бонус урона: +$classBonus", color = Gold)
                classNames.forEachIndexed { index, name ->
                    Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(name, color = Gold, fontWeight = FontWeight.Bold)
                        Text(classDescriptions[index], color = Color.LightGray, fontSize = 12.sp)
                        Text(classStats[index], color = Jade, fontSize = 11.sp)
                        OutlinedButton(onClick = { chosenClass = index; message = "Выбран класс: $name" }, modifier = Modifier.fillMaxWidth()) { Text(if (chosenClass == index) "Выбран" else "Выбрать") }
                    }
                }
                if (chosenClass >= 0) repeat(3) { slot ->
                    val skillIndex = chosenClass * 3 + slot
                    Text(skillNames[chosenClass][slot] + " · " + skillLevels[skillIndex] + "/5", color = Jade, fontWeight = FontWeight.Bold)
                    Text(skillDescriptions[chosenClass][slot], color = Color.LightGray, fontSize = 12.sp)
                    Button(onClick = { if (skillPoints > 0 && skillLevels[skillIndex] < 5) { skillPoints--; skillLevels[skillIndex] = skillLevels[skillIndex] + 1; message = "Навык улучшен." } }, enabled = skillPoints > 0 && skillLevels[skillIndex] < 5, modifier = Modifier.fillMaxWidth()) { Text("Изучить / улучшить · 1 очко") }
                }
                Text(if (rareSkill.isBlank()) "Редкое искусство: шанс выпадения за победу над боссом — 0,8%." else "Получено редкое искусство: $rareSkill", color = Gold)
            }

            GameCard("Секты и альянсы", "Праведный и Злой пути") {
                listOf("Праведный альянс", "Злой альянс").forEachIndexed { index, label ->
                    OutlinedButton(onClick = { chosenAlliance = index; if (chosenSect >= 0 && sectAlliance[chosenSect] != index) chosenSect = -1; message = "Выбран $label" }, modifier = Modifier.fillMaxWidth()) { Text(label + if (chosenAlliance == index) " · выбран" else "") }
                }
                sectNames.forEachIndexed { index, sect ->
                    if (chosenAlliance < 0 || sectAlliance[index] == chosenAlliance) {
                        Column(Modifier.fillMaxWidth().padding(8.dp)) {
                            Text(sect, color = Gold, fontWeight = FontWeight.Bold)
                            Text(sectLore[index], color = Color.LightGray, fontSize = 12.sp)
                            OutlinedButton(onClick = { chosenSect = index; message = "Ты вступил в $sect" }, enabled = chosenAlliance == sectAlliance[index], modifier = Modifier.fillMaxWidth()) { Text(if (chosenSect == index) "Твоя секта" else "Вступить") }
                        }
                    }
                }
            }

            GameCard("Духовный спутник", "Нефритовый лис · уровень $petLevel") {
                Text(
                    "Спутник усиливает атаки на арене. Следующий уровень стоит ${petLevel * 60} камней духа.",
                    color = Color.LightGray
                )
                Button(
                    onClick = {
                        val cost = petLevel * 60
                        if (stones >= cost) {
                            stones -= cost
                            petLevel++
                            message = "Нефритовый лис достиг $petLevel уровня. Сила атак увеличена!"
                        } else {
                            message = "Не хватает камней духа."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Улучшить спутника") }
            }

            Text(message, color = Gold, fontSize = 14.sp)
            Text("Прогресс сохраняется автоматически на этом устройстве.", color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
private fun TechniqueRow(
    name: String,
    description: String,
    selected: Boolean,
    level: Int,
    cost: Int,
    stones: Int,
    onSelect: () -> Unit,
    onUpgrade: () -> Unit
) {
    Surface(color = if (selected) Color(0xFF29463F) else Color(0xFF24333B), shape = MaterialTheme.shapes.large) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(name + if (selected) " · выбрана" else "", color = Gold, fontWeight = FontWeight.Bold)
            Text("$description · уровень $level", color = Color.LightGray, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onSelect, modifier = Modifier.weight(1f)) { Text("Выбрать") }
                Button(onClick = onUpgrade, enabled = stones >= cost, modifier = Modifier.weight(1f)) { Text("Улучшить · $cost") }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, color = Panel, shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color.LightGray, fontSize = 10.sp)
        }
    }
}

@Composable
private fun GameCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(color = Panel, shape = MaterialTheme.shapes.extraLarge) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, color = Gold, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Jade, fontSize = 13.sp)
            content()
        }
    }
}

private val classNames = listOf("Мечник", "Копейщик", "Заклинатель стихий", "Мистик духов")
private val classDescriptions = listOf("Универсальный физический боец: точность, парирование и стабильный урон.", "Физический боец прорыва: дистанция, пробивание брони и сильные выпады.", "Маг разрушения: огонь, лёд и молнии, высокий урон и контроль.", "Маг поддержки: духовные печати, ослабления врага и защитные искусства.")
private val classStats = listOf("Атака 8 · Защита 7 · Контроль 5", "Атака 10 · Защита 5 · Контроль 5", "Атака 10 · Защита 4 · Контроль 8", "Атака 6 · Защита 8 · Контроль 9")
private val skillNames = listOf(listOf("Рассекающий поток", "Стойка горной стали", "Ответный разрез"), listOf("Пронзающее копьё", "Шаг ветра", "Слом брони"), listOf("Пылающий метеор", "Ледяная печать", "Громовой резонанс"), listOf("Духовная связь", "Печать истощения", "Защитный талисман"))
private val skillDescriptions = listOf(listOf("Усиливает мечные атаки.", "Повышает стойкость.", "Увеличивает ответный урон."), listOf("Усиливает пробивание.", "Повышает скорость.", "Ослабляет защиту врага."), listOf("Увеличивает огненный урон.", "Усиливает контроль.", "Увеличивает силу молнии."), listOf("Усиливает духовные техники.", "Ослабляет врага.", "Повышает защиту."))
private val rareArts = listOf("Рассекающий небеса", "Драконий пронзающий удар", "Гром девяти небес", "Печать вечной души")
private val sectNames = listOf("Секта Кровавого Меча", "Секта Тёмного Пламени", "Дворец Ядовитой Луны", "Павильон Призрачного Клинка", "Секта Лёгкого Копья", "Дворец Нефритового Пика", "Храм Золотого Лотоса", "Обитель Небесного Грома")
private val sectAlliance = listOf(1, 1, 1, 1, 0, 0, 0, 0)
private val sectLore = listOf("Кровавые мечники усиливают атаки ценой риска.", "Запретные огненные искусства и взрывной урон.", "Яды, проклятия и постепенное истощение противника.", "Скрытность, критические удары и внезапные атаки.", "Стремительные выпады и контроль дистанции.", "Защита, внутренняя энергия и прочность тела.", "Духовные печати, очищение и поддержка.", "Молнии, скорость и подавление противника.")

private fun techniqueUpgradeCost(level: Int): Int = level * 100

private fun techniqueName(index: Int): String = when (index) {
    0 -> "Меч рассекающей Ци"
    1 -> "Нефритовый лотос"
    else -> "Коготь лиса"
}

private fun bossMaxHp(level: Int): Int = 100 + (level - 1) * 35

private fun stageName(stage: Int): String = when {
    stage < 4 -> "Закалка тела"
    stage < 7 -> "Сбор Ци"
    stage < 10 -> "Золотое ядро"
    stage < 13 -> "Зарождение души"
    else -> "Вознесение"
}
