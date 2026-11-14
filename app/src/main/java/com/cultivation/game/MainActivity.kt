package com.cultivation.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
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
    var selectedMenu by remember { mutableIntStateOf(0) }
    var equippedWeaponId by remember { mutableStateOf(prefs.getString("equipped_weapon_id", "tempered_wood_blade") ?: "tempered_wood_blade") }
    var ownedWeaponIds by remember { mutableStateOf(prefs.getString("owned_weapon_ids", "tempered_wood_blade") ?: "tempered_wood_blade") }
    val ownedWeaponList = remember(ownedWeaponIds) { ownedWeaponIds.split("|").filter { it.isNotBlank() }.distinct() }
    val ownedWeapons = remember(ownedWeaponIds) { ownedWeaponList.mapNotNull { resolveWeaponInstance(it) } }
    val equippedWeapon = resolveWeaponInstance(equippedWeaponId)?.takeIf { equippedWeaponId in ownedWeaponList } ?: weaponCatalog.first()
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
    val classProfile = if (chosenClass >= 0) classCombatProfiles[chosenClass] else classCombatProfiles[0]
    val playerMaxHp = 500 + stage * 30 + classProfile.bonusHp
    val defensiveNodes = listOf(listOf(3,4,7), listOf(2,4,7), listOf(5,7), listOf(3,5,7))
    val defensiveSkillCount = if(chosenClass < 0) 0 else defensiveNodes[chosenClass].count { skillTreeLevels[chosenClass*9+it] == 1 }
    val passiveSkillCount = if(chosenClass < 0) 0 else (0..8).count { skillTreeLevels[chosenClass*9+it] == 1 && !skillDefinitions[chosenClass][it].active && it !in defensiveNodes[chosenClass] }
    val classDefense = classProfile.defense + defensiveSkillCount*4 + equippedWeapon.defenseBonus
    val weaponBranchBonus = if(chosenClass < 0) 0 else
        (if(skillTreeLevels[chosenClass*9+1] == 1) weaponNodeOneBonus[selectedWeapon] else 0) +
        (if(skillTreeLevels[chosenClass*9+4] == 1) weaponNodeTwoBonus[selectedWeapon] else 0)
    val classBonus = classProfile.attack + passiveSkillCount*4 + weaponAttackBonus[selectedWeapon] + weaponBranchBonus + equippedWeapon.attackBonus + equippedWeapon.bonusDamage
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

            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Путь", "Арена", "Персонаж", "Инвентарь", "Навыки", "Секты", "Спутник").forEachIndexed { index, label ->
                    if (selectedMenu == index) Button(onClick = { selectedMenu = index }) { Text(label) }
                    else OutlinedButton(onClick = { selectedMenu = index }) { Text(label) }
                }
            }

            if (selectedMenu == 0 || selectedMenu == 1) {
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
                            val rawDamage = 22 + stage*3 + petLevel*2 + techniqueDamage + classBonus + activeBonus + classProfile.skillPower
                            val critical = Random.nextInt(100) < classProfile.criticalChance
                            val damage = if (critical) (rawDamage * 1.5).toInt() else rawDamage
                            enemyHp = (enemyHp-damage).coerceAtLeast(0)
                            val weaponHealing = equippedWeapon.healOnHit + if (chosenClass == 3) classProfile.healingBonus else 0
                            if (weaponHealing > 0) playerHp = (playerHp + weaponHealing).coerceAtMost(playerMaxHp)
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
                                    val encodedDrop = encodeWeaponInstance(weaponDrop)
                                    ownedWeaponIds = (ownedWeaponList + encodedDrop).distinct().joinToString("|")
                                    message = "Добыча: ${weaponDrop.name} · ${weaponDrop.rarity.title}! $baseMessage"
                                } else message = baseMessage
                                if (rareSkillFound) {
                                    rareSkill = rareArts[chosenClass]
                                    message += " Редкое наследие пробудилось: $rareSkill!"
                                }
                            } else if (playerHp <= 0) {
                                message = "Ты пал в бою. Восстановись за 40 камней духа, чтобы продолжить."
                            } else {
                                message = (if (critical) "КРИТИЧЕСКИЙ УДАР! " else "") + (if(activeSkillReady) skillDefinitions[chosenClass][activeSkillNode].name + " активирован! Урон: $damage." else "$activeTechniqueName нанесла $damage урона.")
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

            }

            if (selectedMenu == 2) GameCard("Персонаж", if (chosenClass < 0) "Выбери свой путь культивации" else classNames[chosenClass]) {
                Text("Характеристики и экипировка находятся здесь. Коллекция предметов — в отдельном инвентаре.", color = Color.LightGray)
                classNames.forEachIndexed { i, n ->
                    val profile = classCombatProfiles[i]
                    Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(n, color = Gold, fontWeight = FontWeight.Bold)
                        Text(classDescriptions[i], color = Color.LightGray, fontSize = 12.sp)
                        Text("Атака +${profile.attack} · Защита +${profile.defense} · Здоровье +${profile.bonusHp} · Крит. шанс ${profile.criticalChance}%", color = Jade, fontSize = 11.sp)
                        Text("Сила техник +${profile.skillPower} · Бонус лечения +${profile.healingBonus}", color = Jade, fontSize = 11.sp)
                        OutlinedButton(onClick = {
                            chosenClass = i
                            if (skillTreeLevels[i*9+activeSkillNode] == 0 || !skillDefinitions[i][activeSkillNode].active) activeSkillNode = 0
                            playerHp = playerHp.coerceAtMost(500 + stage*30 + profile.bonusHp)
                            message = "Выбран класс: $n"
                        }, modifier = Modifier.fillMaxWidth()) { Text(if (chosenClass == i) "Текущий класс" else "Выбрать класс") }
                    }
                }
                Text("Снаряжение персонажа", color = Gold, fontWeight = FontWeight.Bold)
                Text("Слот: основное оружие", color = Color.LightGray, fontSize = 11.sp)
                Text(equippedWeapon.name, color = weaponRarityColor(equippedWeapon.rarity), fontWeight = FontWeight.Bold)
                Text("${equippedWeapon.rarity.title} · ${weaponNames[equippedWeapon.type]} · требуемая ступень ${equippedWeapon.requiredStage}", color = Color.LightGray, fontSize = 12.sp)
                Text("Атака +${equippedWeapon.attackBonus} · доп. урон +${equippedWeapon.bonusDamage} · защита +${equippedWeapon.defenseBonus} · лечение +${equippedWeapon.healOnHit}", color = Jade, fontSize = 12.sp)
                Text("Боевые показатели: атака ${classBonus} · защита ${classDefense} · здоровье ${playerMaxHp}", color = Gold, fontSize = 12.sp)
                Text("Для смены экипировки перейди в «Инвентарь» и выбери предмет.", color = Color.LightGray, fontSize = 11.sp)
            }

            if (selectedMenu == 3) GameCard("Инвентарь", "Оружие · найдено предметов: ${ownedWeapons.size}") {
                Text("Предметы здесь хранятся отдельно от экипировки. Каждый выпавший экземпляр получает собственный разброс характеристик.", color = Color.LightGray, fontSize = 12.sp)
                ownedWeapons.sortedByDescending { weaponPowerScore(it) }.forEach { item ->
                    Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${item.name} · ${item.rarity.title}", color = weaponRarityColor(item.rarity), fontWeight = FontWeight.Bold)
                        Text("${weaponNames[item.type]} · ${stageName(item.requiredStage)} · ступень ${item.requiredStage}", color = Color.LightGray, fontSize = 11.sp)
                        Text("Рейтинг ${weaponPowerScore(item)} · атака +${item.attackBonus} · доп. урон +${item.bonusDamage} · защита +${item.defenseBonus} · лечение +${item.healOnHit}", color = Jade, fontSize = 11.sp)
                        Text(item.effectDescription, color = Color.LightGray, fontSize = 11.sp)
                        OutlinedButton(
                            onClick = { equippedWeaponId = encodeWeaponInstance(item); selectedWeapon = item.type; message = "Экипировано: ${item.name}" },
                            enabled = stage >= item.requiredStage,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (equippedWeaponId == encodeWeaponInstance(item)) "Надето" else if (stage < item.requiredStage) "Откроется на ступени ${item.requiredStage}" else "Экипировать")
                        }
                    }
                }
                val nextWeaponStage = listOf(1, 4, 7, 10, 13, 16).firstOrNull { it > stage }
                if (nextWeaponStage != null) {
                    val preview = weaponCatalog.first { it.requiredStage == nextWeaponStage }
                    Text("Следующий рубеж: ${preview.name} · ${preview.rarity.title}", color = Gold, fontSize = 12.sp)
                }
            }

            if (selectedMenu == 4) {
                GameCard("Боевые техники", "Выбранная техника: $activeTechniqueName · мастерство $activeTechniqueLevel") {
                    Text("Техника усиливает каждый удар на арене. Развивай её за камни духа.", color = Color.LightGray)
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
                GameCard("Древо навыков", if (chosenClass < 0) "Сначала выбери класс в меню персонажа" else classNames[chosenClass]) {
                    if (chosenClass >= 0) {
                        Text("Очки навыков: $skillPoints · пассивных узлов: $passiveSkillCount", color = Gold, fontWeight = FontWeight.Bold)
                        SkillTreeSection("Начальный навык", chosenClass, listOf(0), skillTreeLevels, skillPoints, selectedWeapon, activeSkillNode,
                            onLearn = { node -> skillTreeLevels[chosenClass*9+node] = 1; skillPoints-- },
                            onSetActive = { node -> activeSkillNode = node })
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Ветвь I · Напор", color = Jade, fontWeight = FontWeight.Bold)
                                SkillTreeSection("", chosenClass, listOf(1,2,3), skillTreeLevels, skillPoints, selectedWeapon, activeSkillNode,
                                    { node -> skillTreeLevels[chosenClass*9+node] = 1; skillPoints-- }, { node -> activeSkillNode = node })
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Ветвь II · Искусность", color = Jade, fontWeight = FontWeight.Bold)
                                SkillTreeSection("", chosenClass, listOf(4,5,6), skillTreeLevels, skillPoints, selectedWeapon, activeSkillNode,
                                    { node -> skillTreeLevels[chosenClass*9+node] = 1; skillPoints-- }, { node -> activeSkillNode = node })
                            }
                        }
                        SkillTreeSection("Слияние ветвей", chosenClass, listOf(7,8), skillTreeLevels, skillPoints, selectedWeapon, activeSkillNode,
                            onLearn = { node -> skillTreeLevels[chosenClass*9+node] = 1; skillPoints-- },
                            onSetActive = { node -> activeSkillNode = node })
                        Text("Активный навык: ${skillDefinitions[chosenClass][activeSkillNode].name} · перезарядка: $activeSkillCooldown", color = Gold, fontSize = 12.sp)
                    }
                    Text(if (rareSkill.isBlank()) "Редкое искусство: шанс выпадения за победу — 0,8%." else "Получено редкое искусство: $rareSkill", color = Gold)
                }
            }

            if (selectedMenu == 5) GameCard("Секты и альянсы", "Праведный и Злой пути") {
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

            if (selectedMenu == 6) GameCard("Духовный спутник", "Нефритовый лис · уровень $petLevel") {
                Text("Спутник усиливает атаки на арене. Следующий уровень стоит ${petLevel * 60} камней духа.", color = Color.LightGray)
                Button(
                    onClick = {
                        val cost = petLevel * 60
                        if (stones >= cost) { stones -= cost; petLevel++; message = "Нефритовый лис достиг $petLevel уровня. Сила атак увеличена!" }
                        else message = "Не хватает камней духа."
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Улучшить спутника") }
            }


