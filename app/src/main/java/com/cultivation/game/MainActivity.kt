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
    val skillTreeLevels = remember { mutableStateListOf<Int>().apply {
        repeat(4) { ci -> repeat(9) { ni ->
            val old = when(ni) { 0 -> ci*3; 1 -> ci*3+1; 4 -> ci*3+2; else -> -1 }
            val legacy = if(old >= 0) prefs.getInt("skill_$old",0) else 0
            add(if(ni == 0) 1 else prefs.getInt("skill_tree_${ci}_$ni", if(legacy > 0) 1 else 0).coerceIn(0,1))
        } }
    } }
    var selectedWeapon by remember { mutableIntStateOf(prefs.getInt("weapon_type",0).coerceIn(0,3)) }
    var activeSkillNode by remember { mutableIntStateOf(prefs.getInt("active_skill_node",0).coerceIn(0,8)) }
    var activeSkillCooldown by remember { mutableIntStateOf(0) }
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
    val passiveSkillCount = if(chosenClass < 0) 0 else (0..8).count { skillTreeLevels[chosenClass*9+it] == 1 && !skillDefinitions[chosenClass][it].active }
    val weaponBranchBonus = if(chosenClass < 0) 0 else
        (if(skillTreeLevels[chosenClass*9+1] == 1) weaponNodeOneBonus[selectedWeapon] else 0) +
        (if(skillTreeLevels[chosenClass*9+4] == 1) weaponNodeTwoBonus[selectedWeapon] else 0)
    val classBonus = if(chosenClass < 0) 0 else classBaseAttack[chosenClass] + passiveSkillCount*4 + weaponAttackBonus[selectedWeapon] + weaponBranchBonus
    val activeSkillReady = chosenClass >= 0 && skillTreeLevels[chosenClass*9+activeSkillNode] == 1 && skillDefinitions[chosenClass][activeSkillNode].active && activeSkillCooldown == 0
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

    LaunchedEffect(qi, stones, stage, petLevel, bossLevel, enemyHp, wins, selectedTechnique, swordLevel, lotusLevel, foxLevel, chosenClass, skillPoints, skillTreeLevels.toList(), chosenSect, chosenAlliance, rareSkill, selectedWeapon, activeSkillNode) {
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
            .putInt("weapon_type", selectedWeapon)
            .putInt("active_skill_node", activeSkillNode)
            .also { editor -> skillTreeLevels.forEachIndexed { index, level -> editor.putInt("skill_tree_${index/9}_${index%9}", level) } }
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
                            val activeBonus = if(activeSkillReady) activeSkillDamage[chosenClass] else 0
                            val damage = 22 + stage*3 + petLevel*2 + techniqueDamage + classBonus + activeBonus
                            enemyHp = (enemyHp-damage).coerceAtLeast(0)
                            if(activeSkillReady) activeSkillCooldown = 2 else if(activeSkillCooldown > 0) activeSkillCooldown--
                            if (enemyHp == 0) {
                                val reward = 80 + bossLevel * 15 + stage * 5
                                stones += reward
                                wins++
                                skillPoints++
                                if (chosenClass >= 0 && rareSkill.isBlank() && Random.nextDouble() < 0.008) { rareSkill = rareArts[chosenClass]; message = "Редкое наследие пробудилось: $rareSkill" }
                                else message = "Победа! Получено $reward камней духа и 1 очко навыков."
                            } else {
                                message = if(activeSkillReady) skillDefinitions[chosenClass][activeSkillNode].name + " активирован! Общий урон: $damage." else "$activeTechniqueName нанесла $damage урона."
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

            GameCard("Боевой путь и оружие", if(chosenClass<0) "Выбери класс" else classNames[chosenClass]) {
                Text("Любой класс может использовать любое оружие. Два узла ветвей меняют эффект в зависимости от экипировки.", color=Color.LightGray)
                classNames.forEachIndexed { i,n ->
                    Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement=Arrangement.spacedBy(4.dp)) {
                        Text(n,color=Gold,fontWeight=FontWeight.Bold)
                        Text(classDescriptions[i],color=Color.LightGray,fontSize=12.sp)
                        Text(classStats[i],color=Jade,fontSize=11.sp)
                        OutlinedButton(onClick={chosenClass=i;if(skillTreeLevels[i*9+activeSkillNode]==0 || !skillDefinitions[i][activeSkillNode].active)activeSkillNode=0;message="Выбран класс: $n"},modifier=Modifier.fillMaxWidth()){Text(if(chosenClass==i)"Выбран" else "Выбрать класс")}
                    }
                }
                Text("Оружие — без классовых ограничений",color=Gold,fontWeight=FontWeight.Bold)
                weaponNames.forEachIndexed { i,n -> OutlinedButton(onClick={selectedWeapon=i;message="Экипировано: $n"},modifier=Modifier.fillMaxWidth()){Text(n+if(selectedWeapon==i)" · экипировано" else "")} }
                Text("Бонус атаки оружия: +${weaponAttackBonus[selectedWeapon]}",color=Jade)
                if(chosenClass>=0) {
                    Text("Очки навыков: $skillPoints · пассивных узлов: $passiveSkillCount",color=Gold,fontWeight=FontWeight.Bold)
                    SkillTreeSection("Начальный навык",chosenClass,listOf(0),skillTreeLevels,skillPoints,selectedWeapon,activeSkillNode,
                        onLearn={node->skillTreeLevels[chosenClass*9+node]=1;skillPoints--},
                        onSetActive={node->activeSkillNode=node})
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                            Text("Ветвь I · Напор",color=Jade,fontWeight=FontWeight.Bold)
                            SkillTreeSection("",chosenClass,listOf(1,2,3),skillTreeLevels,skillPoints,selectedWeapon,activeSkillNode,{node->skillTreeLevels[chosenClass*9+node]=1;skillPoints--},{node->activeSkillNode=node})
                        }
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                            Text("Ветвь II · Искусность",color=Jade,fontWeight=FontWeight.Bold)
                            SkillTreeSection("",chosenClass,listOf(4,5,6),skillTreeLevels,skillPoints,selectedWeapon,activeSkillNode,{node->skillTreeLevels[chosenClass*9+node]=1;skillPoints--},{node->activeSkillNode=node})
                        }
                    }
                    SkillTreeSection("Слияние ветвей",chosenClass,listOf(7,8),skillTreeLevels,skillPoints,selectedWeapon,activeSkillNode,
                        onLearn={node->skillTreeLevels[chosenClass*9+node]=1;skillPoints--},
                        onSetActive={node->activeSkillNode=node})
                    Text("Активный навык: ${skillDefinitions[chosenClass][activeSkillNode].name}. При готовности даёт дополнительный урон; перезарядка: $activeSkillCooldown",color=Gold,fontSize=12.sp)
                }
                Text(if(rareSkill.isBlank())"Редкое искусство: шанс выпадения за победу — 0,8%." else "Получено редкое искусство: $rareSkill",color=Gold)
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

private data class SkillDefinition(val name:String,val description:String,val active:Boolean)
private val classNames=listOf("Мечник","Закалённый воин","Заклинатель стихий","Мистик духов")
private val classDescriptions=listOf("Физический универсал: точность, парирование и контратаки.","Мастер закалки тела: стойкость, мощные удары и сопротивление урону.","Маг разрушения: огонь, лёд и молнии, высокий урон и контроль.","Маг поддержки: духовные печати, ослабления и восстановление.")
private val classStats=listOf("Атака 8 · Защита 8 · Контроль 6","Атака 9 · Защита 10 · Контроль 4","Атака 10 · Защита 5 · Контроль 9","Атака 7 · Защита 8 · Контроль 10")
private val classBaseAttack=listOf(9,8,11,7)
private val activeSkillDamage=listOf(34,34,38,34)
private val weaponNames=listOf("Меч","Копьё","Посох","Боевые перчатки")
private val weaponAttackBonus=listOf(8,10,7,9)
private val weaponNodeOneBonus=listOf(3,5,2,4)
private val weaponNodeTwoBonus=listOf(4,2,5,3)
private val skillDefinitions=listOf(
 listOf(
  SkillDefinition("Основы меча","Активное: точный разрез; открывает обе ветви.",true),
  SkillDefinition("Шаг рассекающего ветра","Пассивное: быстрее находит брешь в защите.",false),
  SkillDefinition("Двойной разрез","Активное: серия быстрых ударов.",true),
  SkillDefinition("Парирование тысячи отражений","Пассивное: снижает получаемый урон.",false),
  SkillDefinition("Стойка горной стали","Пассивное: усиливает стойкость и ответные действия.",false),
  SkillDefinition("Контрудар без тени","Активное: мощный ответный удар.",true),
  SkillDefinition("Намерение совершенного клинка","Активное: концентрирует Ци в следующей атаке.",true),
  SkillDefinition("Единство двух намерений","Пассивное: соединяет точность и защиту.",false),
  SkillDefinition("Рассекающий небеса","Активное: высшее искусство мечника.",true)),
 listOf(
  SkillDefinition("Закалка плоти","Активное: укрепляет тело и наносит тяжёлый удар.",true),
  SkillDefinition("Железные сухожилия","Пассивное: усиливает физическую мощь.",false),
  SkillDefinition("Гора неколебима","Пассивное: повышает сопротивление и стойкость.",false),
  SkillDefinition("Сокрушающий кулак","Активное: пробивающий удар внутренней силой.",true),
  SkillDefinition("Кровь истинного дракона","Пассивное: увеличивает жизненную силу.",false),
  SkillDefinition("Несокрушимый покров","Активное: создаёт защитный покров.",true),
  SkillDefinition("Взрыв восьми меридианов","Активное: высвобождает телесную силу.",true),
  SkillDefinition("Тело алмазного владыки","Пассивное: соединяет защиту и мощь.",false),
  SkillDefinition("Нерушимое тело небес","Активное: высшее искусство закалки.",true)),
 listOf(
  SkillDefinition("Искра первозданного пламени","Активное: сгусток стихийной энергии.",true),
  SkillDefinition("Пылающие каналы","Пассивное: усиливает стихийный урон.",false),
  SkillDefinition("Ледяная печать","Активное: урон и сдерживание противника.",true),
  SkillDefinition("Громовой метеор","Активное: разряд молнии.",true),
  SkillDefinition("Сердце девяти стихий","Пассивное: повышает эффективность заклинаний.",false),
  SkillDefinition("Круг стихийной защиты","Пассивное: снижает опасность ответных атак.",false),
  SkillDefinition("Слияние огня и грома","Активное: комбинирует две стихии.",true),
  SkillDefinition("Гармония первоэлементов","Пассивное: объединяет обе ветви.",false),
  SkillDefinition("Бедствие девяти небес","Активное: высшее стихийное заклинание.",true)),
 listOf(
  SkillDefinition("Зов малого духа","Активное: призывает духа для атаки и поддержки.",true),
  SkillDefinition("Связь душ","Пассивное: усиливает духовные техники.",false),
  SkillDefinition("Печать истощения","Активное: ослабляет противника печатью.",true),
  SkillDefinition("Талисман защиты","Пассивное: укрепляет защиту и сопротивление.",false),
  SkillDefinition("Лунное очищение","Активное: восстанавливает силы и очищает.",true),
  SkillDefinition("Тысяча нитей судьбы","Пассивное: усиливает контроль.",false),
  SkillDefinition("Духовный резонанс","Активное: направляет силу спутника во врага.",true),
  SkillDefinition("Круг перерождения","Пассивное: соединяет поддержку и подавление.",false),
  SkillDefinition("Печать вечной души","Активное: высшее искусство мистика.",true))
)
private fun skillNodeUnlocked(ci:Int,node:Int,levels:List<Int>):Boolean {
 val b=ci*9
 return when(node){0->true;1,4->levels[b]==1;2->levels[b+1]==1;3->levels[b+2]==1;5->levels[b+4]==1;6->levels[b+5]==1;7->levels[b+3]==1&&levels[b+6]==1;8->levels[b+7]==1;else->false}
}
private fun weaponSkillDescription(ci:Int,node:Int,w:Int):String {
 val weapon=weaponNames[w]
 val effects=when(ci){
  0->if(node==1) listOf("точность разреза","пробивающий укол","поток Ци через заклинательный клинок","серию коротких ударов") else listOf("парирование клинком","отражение выпада древком","защитный круг Ци","контратаку руками")
  1->if(node==1) listOf("тяжёлый рубящий удар","сокрушительный выпад","концентрацию силы посохом","удар закалённым кулаком") else listOf("стойку с мечом","укреплённый хват древка","стойку с посохом","защиту предплечьями")
  2->if(node==1) listOf("огонь вдоль лезвия","пронзающий огненный поток","широкую область заклинания","стихийный удар касанием") else listOf("быстрое заклинание клинком","стихийный пробой","дальние печати","быструю магию ближнего боя")
  else->if(node==1) listOf("духовную энергию в клинке","духа по траектории выпада","широкую духовную печать","контактный зов духа") else listOf("духовное парирование","защитный контур древка","укреплённые печати","печать в серии ударов")
 }
 return weapon+" меняет навык: "+effects[w]+"."
}
@Composable
private fun SkillTreeSection(title:String,classIndex:Int,nodes:List<Int>,levels:List<Int>,points:Int,weapon:Int,activeNode:Int,onLearn:(Int)->Unit,onSetActive:(Int)->Unit){
 if(title.isNotBlank()) Text(title,color=Gold,fontWeight=FontWeight.Bold)
 nodes.forEach { node ->
  val ix=classIndex*9+node
  val learned=levels[ix]==1
  val def=skillDefinitions[classIndex][node]
  val desc=if(node==1||node==4) weaponSkillDescription(classIndex,node,weapon) else def.description
  Surface(color=if(learned) Color(0xFF29463F) else Color(0xFF24333B),shape=MaterialTheme.shapes.large){
   Column(Modifier.fillMaxWidth().padding(8.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
    Text((node+1).toString()+". "+def.name,color=Gold,fontWeight=FontWeight.Bold,fontSize=12.sp)
    Text(if(def.active)"АКТИВНЫЙ НАВЫК" else "ПАССИВНЫЙ НАВЫК",color=Jade,fontSize=10.sp)
    Text(desc,color=Color.LightGray,fontSize=11.sp)
    if(!learned) Button(onClick={onLearn(node)},enabled=skillNodeUnlocked(classIndex,node,levels)&&points>0,modifier=Modifier.fillMaxWidth()){Text(if(!skillNodeUnlocked(classIndex,node,levels))"Сначала изучи предыдущий узел" else "Изучить · 1 очко")}
    else if(def.active) OutlinedButton(onClick={onSetActive(node)},modifier=Modifier.fillMaxWidth()){Text(if(activeNode==node)"Назначен активным" else "Назначить активным")}
    else Text("Изучено",color=Jade,fontSize=10.sp)
   }
  }
 }
}

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
