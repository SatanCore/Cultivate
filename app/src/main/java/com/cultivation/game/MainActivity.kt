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
    var playerHp by remember { mutableIntStateOf(prefs.getInt("player_hp", 530).coerceAtLeast(0)) }
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
    var equippedWeaponId by remember { mutableStateOf(prefs.getString("equipped_weapon_id", "tempered_wood_blade") ?: "tempered_wood_blade") }
    var ownedWeaponIds by remember { mutableStateOf(prefs.getString("owned_weapon_ids", "tempered_wood_blade") ?: "tempered_wood_blade") }
    val ownedWeaponList = remember(ownedWeaponIds) { ownedWeaponIds.split("|").filter { it.isNotBlank() }.distinct() }
    val equippedWeapon = weaponCatalog.firstOrNull { it.id == equippedWeaponId && it.id in ownedWeaponList } ?: weaponCatalog.first()
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
    val playerMaxHp = 500 + stage * 30
    val defensiveNodes = listOf(listOf(3,4,7), listOf(2,4,7), listOf(5,7), listOf(3,5,7))
    val defensiveSkillCount = if(chosenClass < 0) 0 else defensiveNodes[chosenClass].count { skillTreeLevels[chosenClass*9+it] == 1 }
    val passiveSkillCount = if(chosenClass < 0) 0 else (0..8).count { skillTreeLevels[chosenClass*9+it] == 1 && !skillDefinitions[chosenClass][it].active && it !in defensiveNodes[chosenClass] }
    val classDefense = (if(chosenClass < 0) 0 else listOf(5,9,2,6)[chosenClass] + defensiveSkillCount*4) + equippedWeapon.defenseBonus
    val weaponBranchBonus = if(chosenClass < 0) 0 else
        (if(skillTreeLevels[chosenClass*9+1] == 1) weaponNodeOneBonus[selectedWeapon] else 0) +
        (if(skillTreeLevels[chosenClass*9+4] == 1) weaponNodeTwoBonus[selectedWeapon] else 0)
    val classBonus = (if(chosenClass < 0) 0 else classBaseAttack[chosenClass] + passiveSkillCount*4 + weaponAttackBonus[selectedWeapon] + weaponBranchBonus) + equippedWeapon.attackBonus + equippedWeapon.bonusDamage
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

    LaunchedEffect(qi, stones, stage, petLevel, bossLevel, enemyHp, playerHp, wins, selectedTechnique, swordLevel, lotusLevel, foxLevel, chosenClass, skillPoints, skillTreeLevels.toList(), chosenSect, chosenAlliance, rareSkill, selectedWeapon, activeSkillNode, equippedWeaponId, ownedWeaponIds) {
        prefs.edit()
            .putInt("qi", qi)
            .putInt("stones", stones)
            .putInt("stage", stage)
            .putInt("pet", petLevel)
            .putInt("boss_level", bossLevel)
            .putInt("enemy", enemyHp)
            .putInt("player_hp", playerHp)
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
            .putString("equipped_weapon_id", equippedWeaponId)
            .putString("owned_weapon_ids", ownedWeaponIds)
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
                Text("Твоё здоровье: ${playerHp.coerceAtMost(playerMaxHp)} / $playerMaxHp", color = Jade)
                LinearProgressIndicator(progress = { (playerHp.toFloat() / playerMaxHp).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth(), color = Jade)
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
                            if (equippedWeapon.healOnHit > 0) playerHp = (playerHp + equippedWeapon.healOnHit).coerceAtMost(playerMaxHp)
                            if(activeSkillReady) activeSkillCooldown = 2 else if(activeSkillCooldown > 0) activeSkillCooldown--
                            if (enemyHp > 0) {
                                var incoming = (12 + bossLevel * 3 - classDefense).coerceAtLeast(2)
                                if (activeSkillReady) {
                                    incoming = when(chosenClass) { 0 -> incoming * 55 / 100; 1 -> incoming * 25 / 100; 2 -> 0; else -> incoming * 70 / 100 }
                                    if (chosenClass == 3) playerHp = (playerHp + 45).coerceAtMost(playerMaxHp)
                                }
                                playerHp = (playerHp - incoming).coerceAtLeast(0)
                            }
                            if (enemyHp == 0) {
                                val reward = 80 + bossLevel * 15 + stage * 5
                                stones += reward
                                wins++
                                skillPoints++
                                val weaponDrop = rollWeaponDrop(stage, ownedWeaponList)
                                val rareSkillFound = chosenClass >= 0 && rareSkill.isBlank() && Random.nextDouble() < 0.008
                                val baseMessage = "Победа! Получено $reward камней духа и 1 очко навыков."
                                if (weaponDrop != null) {
                                    ownedWeaponIds = (ownedWeaponList + weaponDrop.id).distinct().joinToString("|")
                                    message = "Добыча: ${weaponDrop.name} · ${weaponDrop.rarity.title}! $baseMessage"
                                } else message = baseMessage
                                if (rareSkillFound) {
                                    rareSkill = rareArts[chosenClass]
                                    message += " Редкое наследие пробудилось: $rareSkill!"
                                }
                            } else if (playerHp <= 0) {
                                message = "Ты пал в бою. Восстановись за 40 камней духа, чтобы продолжить."
                            } else {
                                message = if(activeSkillReady) skillDefinitions[chosenClass][activeSkillNode].name + " активирован! Урон: $damage." else "$activeTechniqueName нанесла $damage урона."
                            }
                        }
                    },
                    enabled = enemyHp > 0 && playerHp > 0,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Удар потоком Ци") }

                if (playerHp <= 0) {
                    Button(onClick = { if (stones >= 40) { stones -= 40; playerHp = playerMaxHp; message = "Ты восстановил силы и готов вернуться на арену." } else message = "Нужно 40 камней духа для восстановления." }, modifier = Modifier.fillMaxWidth()) { Text("Восстановиться · 40 камней") }
                }
                Button(
                    onClick = {
                        if (enemyHp <= 0) {
                            bossLevel++
                            enemyHp = bossMaxHp(bossLevel)
                            playerHp = playerMaxHp
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
                Text("Экипировано: ${equippedWeapon.name}", color = weaponRarityColor(equippedWeapon.rarity), fontWeight = FontWeight.Bold)
                Text("${equippedWeapon.rarity.title} · Требование: ${stageName(equippedWeapon.requiredStage)} (ступень ${equippedWeapon.requiredStage})", color = Color.LightGray, fontSize = 12.sp)
                Text("Атака +${equippedWeapon.attackBonus} · защита +${equippedWeapon.defenseBonus} · доп. эффект: ${equippedWeapon.effectDescription}", color = Jade, fontSize = 12.sp)
                Text("Арсенал · ${ownedWeaponList.size} предметов", color = Gold, fontWeight = FontWeight.Bold)
                weaponCatalog.filter { it.id in ownedWeaponList }.forEach { item ->
                    Column(Modifier.fillMaxWidth().padding(6.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("${item.name} · ${item.rarity.title}", color = weaponRarityColor(item.rarity), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${weaponNames[item.type]} · ступень ${item.requiredStage} · атака +${item.attackBonus} · защита +${item.defenseBonus}", color = Color.LightGray, fontSize = 11.sp)
                        Text(item.effectDescription, color = Jade, fontSize = 11.sp)
                        OutlinedButton(onClick = { equippedWeaponId = item.id; selectedWeapon = item.type; message = "Экипировано: ${item.name}" }, enabled = stage >= item.requiredStage, modifier = Modifier.fillMaxWidth()) {
                            Text(if (equippedWeaponId == item.id) "Экипировано" else if (stage < item.requiredStage) "Требуется ступень ${item.requiredStage}" else "Экипировать")
                        }
                    }
                }
                val nextWeaponStage = listOf(1, 4, 7, 10, 13, 16).firstOrNull { it > stage }
                if (nextWeaponStage != null) {
                    val preview = weaponCatalog.first { it.requiredStage == nextWeaponStage }
                    Text("Следующий рубеж откроет оружие: ${preview.name} · ${preview.rarity.title} · атака +${preview.attackBonus}", color = Gold, fontSize = 12.sp)
                }
                Text("Бонус типа оружия: +${weaponAttackBonus[selectedWeapon]} · навыки ветвей учитывают тип экипировки", color = Jade, fontSize = 11.sp)
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

private enum class WeaponRarity(val title: String, val bonus: Int) {
    COMMON("Обычное", 0),
    UNCOMMON("Необычное", 3),
    RARE("Редкое", 7),
    EPIC("Эпическое", 12),
    LEGENDARY("Легендарное", 18),
    MYTHIC("Мифическое", 24)
}

private data class WeaponDefinition(
    val id: String,
    val name: String,
    val type: Int,
    val rarity: WeaponRarity,
    val requiredStage: Int,
    val attackBonus: Int,
    val defenseBonus: Int,
    val bonusDamage: Int,
    val healOnHit: Int,
    val effectDescription: String
)

private fun weapon(
    id: String, name: String, type: Int, rarity: WeaponRarity, requiredStage: Int,
    realmBase: Int, defense: Int = 0, extraDamage: Int = 0, healing: Int = 0, effect: String
) = WeaponDefinition(id, name, type, rarity, requiredStage, realmBase + rarity.bonus, defense, extraDamage, healing, effect)

private val weaponCatalog = listOf(
    weapon("tempered_wood_blade", "Тренировочный клинок", 0, WeaponRarity.COMMON, 1, 4, effect = "Надёжное оружие начинающего культиватора"),
    weapon("bamboo_spear", "Бамбуковое копьё", 1, WeaponRarity.UNCOMMON, 1, 4, extraDamage = 1, effect = "Уколы наносят немного больше урона"),
    weapon("herb_staff", "Посох травника", 2, WeaponRarity.RARE, 1, 4, healing = 1, effect = "Восстанавливает 1 здоровье при ударе"),
    weapon("stone_fists", "Каменные наручи", 3, WeaponRarity.EPIC, 1, 4, defense = 2, effect = "Укрепляют защиту владельца"),
    weapon("iron_short_sword", "Железный короткий меч", 0, WeaponRarity.LEGENDARY, 1, 4, extraDamage = 2, effect = "Сбалансированный клинок с усиленным ударом"),
    weapon("iron_bone_spear", "Костяное копьё", 1, WeaponRarity.MYTHIC, 1, 4, defense = 1, extraDamage = 2, effect = "Редкое оружие первой ступени"),

    weapon("qi_splitting_sword", "Меч Рассекающего Потока", 0, WeaponRarity.COMMON, 4, 36, extraDamage = 2, effect = "Резонирует с накопленной Ци"),
    weapon("azure_dragon_spear", "Копьё Лазурного Дракона", 1, WeaponRarity.UNCOMMON, 4, 36, extraDamage = 3, effect = "Усиливает пробивающие выпады"),
    weapon("cloud_pattern_staff", "Посох Облачных Узоров", 2, WeaponRarity.RARE, 4, 36, healing = 3, effect = "Постепенно возвращает здоровье"),
    weapon("mountain_guard_gauntlets", "Перчатки Горного Стража", 3, WeaponRarity.EPIC, 4, 36, defense = 5, effect = "Укрепляют тело и снижают ответный урон"),
    weapon("flame_twin_blades", "Парные Клинки Пылающего Ветра", 0, WeaponRarity.LEGENDARY, 4, 36, extraDamage = 6, effect = "Дополнительный урон быстрыми сериями"),
    weapon("jade_tide_lance", "Нефритовое Копьё Прилива", 1, WeaponRarity.MYTHIC, 4, 36, defense = 3, extraDamage = 5, effect = "Сочетает пробивание и защиту"),

    weapon("golden_core_edge", "Клинок Золотого Ядра", 0, WeaponRarity.COMMON, 7, 100, extraDamage = 5, effect = "Стабилизирует поток Ци в атаке"),
    weapon("thunderfall_spear", "Копьё Громового Падения", 1, WeaponRarity.UNCOMMON, 7, 100, extraDamage = 8, effect = "Удар сопровождается разрядом молнии"),
    weapon("nine_flame_staff", "Посох Девяти Пламен", 2, WeaponRarity.RARE, 7, 100, extraDamage = 7, healing = 3, effect = "Стихийный урон и небольшое исцеление"),
    weapon("adamant_gauntlets", "Адамантовые Рукавицы", 3, WeaponRarity.EPIC, 7, 100, defense = 12, effect = "Значительно повышают стойкость"),
    weapon("soul_carving_sword", "Меч Гравировки Души", 0, WeaponRarity.LEGENDARY, 7, 100, extraDamage = 12, effect = "Поражает не только тело, но и духовную оболочку"),
    weapon("star_river_spear", "Копьё Звёздной Реки", 1, WeaponRarity.MYTHIC, 7, 100, defense = 8, extraDamage = 10, effect = "Скрытая сила звёзд усиливает каждый выпад"),

    weapon("nascent_soul_saber", "Сабля Зарождающейся Души", 0, WeaponRarity.COMMON, 10, 230, extraDamage = 12, effect = "Настроена на духовное ядро владельца"),
    weapon("heaven_piercer", "Копьё Пронзающее Небеса", 1, WeaponRarity.UNCOMMON, 10, 230, extraDamage = 16, effect = "Пробивает укреплённую защиту"),
    weapon("spirit_sea_staff", "Посох Моря Духов", 2, WeaponRarity.RARE, 10, 230, healing = 8, effect = "Поглощает остаточную энергию и исцеляет"),
    weapon("dragonbone_gauntlets", "Перчатки Драконьей Кости", 3, WeaponRarity.EPIC, 10, 230, defense = 24, effect = "Тело становится твёрже драконьей чешуи"),
    weapon("void_echo_blade", "Клинок Эха Пустоты", 0, WeaponRarity.LEGENDARY, 10, 230, extraDamage = 25, effect = "Дополнительный урон эхом разреза"),
    weapon("phoenix_spear", "Копьё Возрождения Феникса", 1, WeaponRarity.MYTHIC, 10, 230, defense = 16, healing = 10, effect = "Возвращает часть жизненной силы"),

    weapon("immortal_sun_sword", "Меч Бессмертного Солнца", 0, WeaponRarity.COMMON, 13, 520, extraDamage = 28, effect = "Солнечная энергия усиливает рубящий удар"),
    weapon("sky_breaker_lance", "Копьё Разрушителя Небес", 1, WeaponRarity.UNCOMMON, 13, 520, extraDamage = 35, effect = "Сокрушительный пробивающий выпад"),
    weapon("moon_heart_staff", "Посох Лунного Сердца", 2, WeaponRarity.RARE, 13, 520, healing = 18, effect = "Лунная энергия восстанавливает здоровье"),
    weapon("titan_body_fists", "Кулаки Титанического Тела", 3, WeaponRarity.EPIC, 13, 520, defense = 48, effect = "Сила титана укрепляет защиту"),
    weapon("chaos_edge", "Клинок Первичного Хаоса", 0, WeaponRarity.LEGENDARY, 13, 520, extraDamage = 58, effect = "Хаотическая кромка наносит разрушительный урон"),
    weapon("world_pillar_spear", "Копьё Столпа Мира", 1, WeaponRarity.MYTHIC, 13, 520, defense = 35, extraDamage = 45, effect = "Стабилизирует тело и усиливает атаку"),

    weapon("void_sundering_blade", "Меч Рассекающий Пустоту", 0, WeaponRarity.COMMON, 16, 1150, extraDamage = 60, effect = "Разрезает пространство перед целью"),
    weapon("primordial_dragon_lance", "Копьё Первородного Дракона", 1, WeaponRarity.UNCOMMON, 16, 1150, extraDamage = 75, effect = "Пробуждает первородную драконью мощь"),
    weapon("chaos_oracle_staff", "Посох Оракула Хаоса", 2, WeaponRarity.RARE, 16, 1150, healing = 40, effect = "Преобразует хаос в жизненную энергию"),
    weapon("heavenly_titan_gauntlets", "Перчатки Небесного Титана", 3, WeaponRarity.EPIC, 16, 1150, defense = 100, effect = "Почти непробиваемая защита"),
    weapon("ancestor_soul_reaper", "Жнец Душ Прародителя", 0, WeaponRarity.LEGENDARY, 16, 1150, extraDamage = 130, effect = "Пожинает духовную силу поверженных врагов"),
    weapon("dao_origin_spear", "Копьё Истока Дао", 1, WeaponRarity.MYTHIC, 16, 1150, defense = 75, healing = 55, effect = "Соединяет атаку, защиту и восстановление")
)

private fun weaponRarityColor(rarity: WeaponRarity): Color = when (rarity) {
    WeaponRarity.COMMON -> Color(0xFFCFD4D8)
    WeaponRarity.UNCOMMON -> Color(0xFF78C6A3)
    WeaponRarity.RARE -> Color(0xFF68A9F5)
    WeaponRarity.EPIC -> Color(0xFFB58BE8)
    WeaponRarity.LEGENDARY -> Color(0xFFFFB65C)
    WeaponRarity.MYTHIC -> Color(0xFFFF718B)
}

private fun rollWeaponDrop(stage: Int, ownedIds: List<String>): WeaponDefinition? {
    val available = weaponCatalog.filter { it.requiredStage <= stage && it.id !in ownedIds }
    if (available.isEmpty()) return null
    val roll = Random.nextInt(1000)
    val rarity = when {
        roll < 400 -> WeaponRarity.COMMON
        roll < 660 -> WeaponRarity.UNCOMMON
        roll < 830 -> WeaponRarity.RARE
        roll < 930 -> WeaponRarity.EPIC
        roll < 985 -> WeaponRarity.LEGENDARY
        else -> WeaponRarity.MYTHIC
    }
    val sameRarity = available.filter { it.rarity == rarity }
    return (sameRarity.ifEmpty { available }).random()
}

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
    stage < 16 -> "Вознесение"
    stage < 19 -> "Преодоление пустоты"
    stage < 22 -> "Слияние с Дао"
    else -> "Бессмертный владыка"
}
