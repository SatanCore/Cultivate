package com.cultivation.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.clickable
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
private const val QI_INTERVAL_SECONDS = 600L
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
    val savedAbodeLevel = prefs.getInt("abode_level", 1).coerceIn(1, 20)
    val todayKey = System.currentTimeMillis() / 86_400_000L
    val dailyReset = prefs.getLong("daily_day", todayKey) != todayKey
    val restoredQi = (offlineSeconds / qiIntervalForAbode(savedAbodeLevel)).toInt()

    var qi by remember {
        mutableIntStateOf((prefs.getInt("qi", 0) + restoredQi).coerceAtMost(9999))
    }
    var stones by remember { mutableIntStateOf(prefs.getInt("stones", 120)) }
    var stage by remember { mutableIntStateOf(prefs.getInt("stage", 1).coerceAtLeast(1)) }
    var dailyWins by remember { mutableIntStateOf(if (dailyReset) 0 else prefs.getInt("daily_wins", 0)) }
    var dailyGardenCollected by remember { mutableStateOf(!dailyReset && prefs.getBoolean("daily_garden", false)) }
    var dailyMeditated by remember { mutableStateOf(!dailyReset && prefs.getBoolean("daily_meditated", false)) }
    var dailyClanDonated by remember { mutableStateOf(!dailyReset && prefs.getBoolean("daily_clan_donated", false)) }
    var dailyClaims by remember { mutableIntStateOf(if (dailyReset) 0 else prefs.getInt("daily_claims", 0)) }
    var clanReputation by remember { mutableIntStateOf(prefs.getInt("clan_reputation", 0).coerceAtLeast(0)) }
    var abodeLevel by remember { mutableIntStateOf(savedAbodeLevel) }
    var ironOre by remember { mutableIntStateOf(prefs.getInt("abode_iron_ore", 0).coerceAtLeast(0)) }
    var spiritHerbs by remember { mutableIntStateOf(prefs.getInt("abode_herbs", 0).coerceAtLeast(0)) }
    var spiritWater by remember { mutableIntStateOf(prefs.getInt("abode_water", 0).coerceAtLeast(0)) }
    var qiPills by remember { mutableIntStateOf(prefs.getInt("abode_qi_pills", 0).coerceAtLeast(0)) }
    var bodyPills by remember { mutableIntStateOf(prefs.getInt("abode_body_pills", 0).coerceAtLeast(0)) }
    var bodyPillBonus by remember { mutableIntStateOf(prefs.getInt("abode_body_bonus", 0).coerceAtLeast(0)) }
    var forgeTemper by remember { mutableIntStateOf(prefs.getInt("abode_forge_temper", 0).coerceAtLeast(0)) }
    var alchemyRank by remember { mutableIntStateOf(prefs.getInt("abode_alchemy_rank", 1).coerceIn(1, 20)) }
    var gardenReadyAt by remember { mutableLongStateOf(prefs.getLong("abode_garden_ready", 0L)) }
    var springReadyAt by remember { mutableLongStateOf(prefs.getLong("abode_spring_ready", 0L)) }
    var abodeSection by remember { mutableStateOf("sanctuary") }
    var ownedPetIds by remember { mutableStateOf(prefs.getString("owned_pet_ids", "jade_fox") ?: "jade_fox") }
    var activePetId by remember { mutableStateOf(prefs.getString("active_pet_id", "jade_fox") ?: "jade_fox") }
    val petLevels = remember { mutableStateListOf<Int>().apply { petCatalog.forEachIndexed { index, pet -> add(prefs.getInt("pet_level_${pet.id}", if (index == 0) prefs.getInt("pet", 1) else 1).coerceIn(1, 30)) } } }
    val petBondXp = remember { mutableStateListOf<Int>().apply { petCatalog.forEach { add(prefs.getInt("pet_xp_${it.id}", 0).coerceAtLeast(0)) } } }
    val petAscensions = remember { mutableStateListOf<Int>().apply { petCatalog.forEach { add(prefs.getInt("pet_ascension_${it.id}", 0).coerceIn(0, 5)) } } }
    var petEssence by remember { mutableIntStateOf(prefs.getInt("pet_essence", 0).coerceAtLeast(0)) }
    var petCores by remember { mutableIntStateOf(prefs.getInt("pet_cores", 0).coerceAtLeast(0)) }
    var petPity by remember { mutableIntStateOf(prefs.getInt("pet_pity", 0).coerceAtLeast(0)) }
    val safeActivePetId = if (activePetId in ownedPetIds.split("|")) activePetId else "jade_fox"
    val activePetIndex = petCatalog.indexOfFirst { it.id == safeActivePetId }.coerceAtLeast(0)
    val activePet = petCatalog[activePetIndex]
    val petLevel = petLevels[activePetIndex]
    val petXp = petBondXp[activePetIndex]
    val petAscension = petAscensions[activePetIndex]
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
    var selectedTownBuilding by remember { mutableStateOf("") }
    val pageScrollState = rememberScrollState()
    LaunchedEffect(selectedMenu, selectedTownBuilding) { pageScrollState.animateScrollTo(0) }
    var questClaimed by remember { mutableStateOf(prefs.getBoolean("town_quest_claimed", false)) }
    var storyChapter by remember { mutableIntStateOf(prefs.getInt("town_story_chapter", 0).coerceIn(0, 3)) }
    val skillMaterials = remember { mutableStateListOf<Int>().apply {
        val saved = (prefs.getString("skill_materials", "0,0,0,0,0,0") ?: "0,0,0,0,0,0").split(",").mapNotNull { it.toIntOrNull() }
        repeat(6) { add(saved.getOrElse(it) { 0 }.coerceAtLeast(0)) }
    } }
    var equippedWeaponId by remember { mutableStateOf(prefs.getString("equipped_weapon_id", "tempered_wood_blade") ?: "tempered_wood_blade") }
    var ownedWeaponIds by remember { mutableStateOf(prefs.getString("owned_weapon_ids", "tempered_wood_blade") ?: "tempered_wood_blade") }
    var ownedGearIds by remember { mutableStateOf(prefs.getString("owned_gear_ids", "") ?: "") }
    val gearSlots = remember { GearSlot.values().toList() }
    val ownedGearList = remember(ownedGearIds) { ownedGearIds.split("|").filter { it.isNotBlank() } }
    val ownedGear = remember(ownedGearIds) { ownedGearList.mapNotNull { resolveGearInstance(it) } }
    val equippedGearIds = remember { mutableStateListOf<String>().apply {
        gearSlots.forEach { slot -> add(prefs.getString("gear_${slot.name}", "") ?: "") }
    } }
    val equippedGear = remember(ownedGearIds, equippedGearIds.toList()) {
        gearSlots.mapIndexedNotNull { index, slot ->
            val encoded = equippedGearIds.getOrElse(index) { "" }
            if (encoded.isBlank()) null else resolveGearInstance(encoded)?.takeIf { encoded in ownedGearList }
        }
    }
    val ownedWeaponList = remember(ownedWeaponIds) { ownedWeaponIds.split("|").filter { it.isNotBlank() }.distinct() }
    val ownedWeapons = remember(ownedWeaponIds) { ownedWeaponList.mapNotNull { resolveWeaponInstance(it) } }
    val equippedWeapon = resolveWeaponInstance(equippedWeaponId)?.takeIf { equippedWeaponId in ownedWeaponList } ?: weaponCatalog.first()
    var activeSkillNode by remember { mutableIntStateOf(prefs.getInt("active_skill_node",0).coerceIn(0,8)) }
    val skillCooldowns = remember { mutableStateListOf<Int>().apply { repeat(9) { add(0) } } }
    var techniqueCooldown by remember { mutableIntStateOf(0) }
    var weaponCooldown by remember { mutableIntStateOf(0) }
    var autoBattle by remember { mutableStateOf(prefs.getBoolean("auto_battle", false)) }
    val rotationOrder = remember { mutableStateListOf<Int>().apply {
        val defaultSkills = if (chosenClass >= 0) (0..8).filter { skillTreeLevels[chosenClass*9+it] == 1 && skillDefinitions[chosenClass][it].active } else listOf(0)
        val defaults = defaultSkills + listOf(9, 10)
        val savedText = prefs.getString("battle_rotation_v2", null)
        val saved = savedText?.split(",")?.mapNotNull { it.toIntOrNull() }?.filter { it in 0..10 }?.distinct()
        addAll(saved ?: defaults)
        defaults.filter { it !in this }.forEach { add(it) }
    } }
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
    val maxAbodeLevel = abodeMaxLevel(stage)
    val maxAlchemyRank = alchemyMaxRank(stage)
    val classProfile = if (chosenClass >= 0) classCombatProfiles[chosenClass] else classCombatProfiles[0]
    val activeSetBonus = calculateGearSetBonus(equippedGear)
    val gearAttackTotal = equippedGear.sumOf { it.attackBonus } + activeSetBonus.attack
    val gearDefenseTotal = equippedGear.sumOf { it.defenseBonus } + activeSetBonus.defense
    val gearHpTotal = equippedGear.sumOf { it.hpBonus } + activeSetBonus.hp
    val gearSkillTotal = equippedGear.sumOf { it.skillBonus } + activeSetBonus.skill
    val factionIndex = chosenSect.coerceIn(0, 7)
    val factionAttack = if (stage < 7 && chosenSect >= 0) clanAttackBonuses[factionIndex] else 0
    val factionDefense = if (stage < 7 && chosenSect >= 0) clanDefenseBonuses[factionIndex] else 0
    val factionHp = if (stage < 7 && chosenSect >= 0) clanHpBonuses[factionIndex] else 0
    val petSynergy = petClassSynergy(activePet.id, chosenClass)
    val petAttack = activePet.attack + petLevel / 3 + petAscension * 3 + if (petSynergy) 4 else 0
    val petDefense = activePet.defense + petLevel / 4 + petAscension * 2 + if (petSynergy) 3 else 0
    val petSkill = activePet.skill + petAscension * 2 + if (petSynergy) 4 else 0
    val playerMaxHp = 500 + stage * 30 + classProfile.bonusHp + gearHpTotal + bodyPillBonus + activePet.hp + petLevel * 3 + factionHp + if (petSynergy) 20 else 0
    val defensiveNodes = listOf(listOf(3,4,7), listOf(2,4,7), listOf(5,7), listOf(3,5,7))
    val defensiveSkillCount = if(chosenClass < 0) 0 else defensiveNodes[chosenClass].count { skillTreeLevels[chosenClass*9+it] == 1 }
    val passiveSkillCount = if(chosenClass < 0) 0 else (0..8).count { skillTreeLevels[chosenClass*9+it] == 1 && !skillDefinitions[chosenClass][it].active && it !in defensiveNodes[chosenClass] }
    val classDefense = classProfile.defense + defensiveSkillCount*4 + equippedWeapon.defenseBonus + gearDefenseTotal + petDefense + factionDefense
    val weaponBranchBonus = if(chosenClass < 0) 0 else
        (if(skillTreeLevels[chosenClass*9+1] == 1) weaponNodeOneBonus[selectedWeapon] else 0) +
        (if(skillTreeLevels[chosenClass*9+4] == 1) weaponNodeTwoBonus[selectedWeapon] else 0)
    val classBonus = classProfile.attack + passiveSkillCount*4 + weaponAttackBonus[selectedWeapon] + weaponBranchBonus + equippedWeapon.attackBonus + equippedWeapon.bonusDamage + gearAttackTotal + gearSkillTotal + petAttack + petSkill + factionAttack
    val activeTechniqueLevel = when (selectedTechnique) {
        0 -> swordLevel
        1 -> lotusLevel
        else -> foxLevel
    }
    val activeTechniqueName = techniqueName(selectedTechnique)
    val techniqueDamageBase = when (selectedTechnique) {
        0 -> 18 + swordLevel.coerceAtMost(50) * 12
        1 -> 10 + lotusLevel.coerceAtMost(50) * 8
        else -> 14 + foxLevel.coerceAtMost(50) * 10
    }
    val techniqueDamage = if (activeTechniqueLevel >= 50) (techniqueDamageBase * 1.2).toInt() else techniqueDamageBase

    fun learnSkillNode(node: Int) {
        if (chosenClass < 0 || skillPoints <= 0) return
        if (!skillNodeUnlocked(chosenClass, node, skillTreeLevels, stage.coerceAtMost(20))) {
            message = "Навык пока закрыт: проверь уровень персонажа и предыдущие умения."
            return
        }
        skillTreeLevels[chosenClass * 9 + node] = 1
        skillPoints--
        if (skillDefinitions[chosenClass][node].active && node !in rotationOrder) {
            val insertAt = rotationOrder.indexOfFirst { it >= 9 }.let { if (it < 0) rotationOrder.size else it }
            rotationOrder.add(insertAt, node)
        }
        message = "Изучено умение: ${skillDefinitions[chosenClass][node].name}."
    }

    fun performCombatTurn() {
        if (enemyHp <= 0 || playerHp <= 0) return
        val nextSkillCooldowns = skillCooldowns.map { (it - 1).coerceAtLeast(0) }
        val techniqueCd = (techniqueCooldown - 1).coerceAtLeast(0)
        val weaponCd = (weaponCooldown - 1).coerceAtLeast(0)
        val action = rotationOrder.firstOrNull { candidate ->
            when {
                candidate in 0..8 -> chosenClass >= 0 &&
                    skillTreeLevels[chosenClass * 9 + candidate] == 1 &&
                    skillDefinitions[chosenClass][candidate].active &&
                    nextSkillCooldowns[candidate] == 0
                candidate == 9 -> techniqueCd == 0
                candidate == 10 -> weaponCd == 0 && equippedWeapon.hasActiveAbility
                else -> false
            }
        }
        val actionBonus = when {
            action != null && action in 0..8 -> activeSkillDamage[chosenClass] + classProfile.skillPower
            action == 9 -> techniqueDamage
            action == 10 -> 12 + equippedWeapon.bonusDamage * 2
            else -> techniqueDamage / 2
        }
        val petFinisherBonus = if (activePet.id == "ember_wolf" && enemyHp <= (enemyMaxHp * 0.30f).toInt()) (22 + stage * 3 + classBonus + actionBonus) / 7 else 0
        val rawDamage = 22 + stage * 3 + classBonus + actionBonus + petFinisherBonus
        val critical = Random.nextInt(100) < (classProfile.criticalChance + activePet.critical + if (petSynergy) 2 else 0).coerceAtMost(75)
        val damage = if (critical) (rawDamage * 1.5).toInt() else rawDamage
        enemyHp = (enemyHp - damage).coerceAtLeast(0)
        for (i in 0..8) skillCooldowns[i] = nextSkillCooldowns[i]
        if (action != null && action in 0..8) skillCooldowns[action] = 4
        techniqueCooldown = if (action == 9) 3 else techniqueCd
        weaponCooldown = if (action == 10) 6 else weaponCd
        val weaponHealing = equippedWeapon.healOnHit + activePet.healing + (if (chosenClass == 3) classProfile.healingBonus else 0) + if (petSynergy) 2 else 0
        if (weaponHealing > 0) playerHp = (playerHp + weaponHealing).coerceAtMost(playerMaxHp)
        if (enemyHp > 0) {
            var incoming = (12 + bossLevel * 3 - classDefense).coerceAtLeast(2)
            if (activePet.id == "stone_tortoise") incoming = (incoming * 80 / 100).coerceAtLeast(1)
            if (action != null && action in 0..8) {
                incoming = when (chosenClass) { 0 -> incoming * 55 / 100; 1 -> incoming * 25 / 100; 2 -> 0; else -> incoming * 70 / 100 }
                if (chosenClass == 3) playerHp = (playerHp + 45).coerceAtMost(playerMaxHp)
            }
            playerHp = (playerHp - incoming).coerceAtLeast(0)
        }
        if (enemyHp == 0) {
            autoBattle = false
            val reward = 80 + bossLevel * 15 + stage * 5
            stones += reward
            wins++
            dailyWins++
            qi = (qi + 5).coerceAtMost(9999)
            petBondXp[activePetIndex] = petBondXp[activePetIndex] + 15
            if (Random.nextInt(100) < 25) petEssence++
            if (Random.nextInt(100) < 4) petCores++
            skillPoints++
            val materialTier = ((bossLevel - 1) / 5).coerceIn(0, 5)
            skillMaterials[materialTier] = skillMaterials[materialTier] + 1
            val weaponDrop = rollWeaponDrop(stage, ownedWeaponList)
            val gearDrop = if (Random.nextInt(100) < 35) rollGearDrop(stage) else null
            val oreFound = 1 + (bossLevel / 5).coerceAtMost(3)
            ironOre += oreFound
            val rareSkillFound = chosenClass >= 0 && rareSkill.isBlank() && Random.nextDouble() < 0.008
            val weaponText = if (weaponDrop != null) {
                val encodedDrop = encodeWeaponInstance(weaponDrop)
                ownedWeaponIds = (ownedWeaponList + encodedDrop).joinToString("|")
                " Добыча: ${weaponDrop.name} · ${weaponDrop.rarity.title}."
            } else ""
            val gearText = if (gearDrop != null) {
                ownedGearIds = (ownedGearList + encodeGearInstance(gearDrop)).joinToString("|")
                " Экипировка: ${gearDrop.name} · ${gearDrop.rarity.title}."
            } else ""
            petPity++
            val petDropPool = petCatalog.filter { it.id !in ownedPetIds.split("|") && it.requiredBoss <= bossLevel && it.id != "moon_deer" && it.id != "river_koi" }
            val foundPet = if (petDropPool.isNotEmpty() && (Random.nextInt(100) < 5 || petPity >= 20)) petDropPool.random() else null
            val petDropText = if (foundPet != null) {
                ownedPetIds = (ownedPetIds.split("|").filter { it.isNotBlank() } + foundPet.id).distinct().joinToString("|")
                petPity = 0
                " Редкая находка! Приручён питомец: ${foundPet.name}."
            } else ""
            if (rareSkillFound) rareSkill = rareArts[chosenClass]
            message = "Победа! Получено $reward камней, 5 Ци, опыт связи питомца и железная руда ×$oreFound.$weaponText$gearText$petDropText" +
                if (rareSkillFound) " Редкое наследие: $rareSkill!" else ""
        } else if (playerHp <= 0) {
            autoBattle = false
            message = "Ты пал в бою. Восстановись за 40 камней духа, чтобы продолжить."
        } else {
            val actionName = when {
                action != null && action in 0..8 -> skillDefinitions[chosenClass][action].name
                action == 9 -> activeTechniqueName
                action == 10 -> "Способность оружия: ${equippedWeapon.name}"
                else -> "Обычная атака"
            }
            message = (if (critical) "КРИТИЧЕСКИЙ УДАР! " else "") + "$actionName · урон $damage."
        }
    }

    fun upgradeTechnique(level: Int, setLevel: (Int) -> Unit) {
        if (level >= 50) { message = "Техника достигла предела мастерства 50 и получила усиление +20%."; return }
        val targetLevel = level + 1
        if (level < 20) {
            val cost = techniqueUpgradeCost(level)
            if (stones >= cost) { stones -= cost; setLevel(targetLevel); message = "Мастерство повышено до $targetLevel." }
            else message = "Не хватает камней духа."
        } else {
            val tier = ((targetLevel - 21) / 5).coerceIn(0, 5)
            val amount = (((targetLevel - 21) % 5) + 1) * (tier + 1)
            if (skillMaterials[tier] >= amount) {
                skillMaterials[tier] = skillMaterials[tier] - amount
                setLevel(targetLevel)
                message = "Техника достигла $targetLevel уровня. Использовано: ${skillMaterialNames[tier]} ×$amount."
            } else message = "Не хватает ресурса: ${skillMaterialNames[tier]} ×$amount."
        }
    }

    LaunchedEffect(autoBattle, selectedMenu) {
        if (autoBattle && selectedMenu == 2) {
            while (autoBattle && selectedMenu == 2 && enemyHp > 0 && playerHp > 0) {
                delay(1000L)
                performCombatTurn()
            }
        }
    }

    LaunchedEffect(abodeLevel) {
        while (true) {
            delay(qiIntervalForAbode(abodeLevel) * 1000L)
            qi = (qi + 1).coerceAtMost(9999)
        }
    }

    LaunchedEffect(qi, stones, stage, abodeLevel, ironOre, spiritHerbs, spiritWater, qiPills, bodyPills, bodyPillBonus, forgeTemper, alchemyRank, gardenReadyAt, springReadyAt, ownedPetIds, activePetId, petLevels.toList(), petBondXp.toList(), petAscensions.toList(), petEssence, petCores, petPity, dailyWins, dailyGardenCollected, dailyMeditated, dailyClanDonated, dailyClaims, clanReputation, petLevel, bossLevel, enemyHp, playerHp, wins, selectedTechnique, swordLevel, lotusLevel, foxLevel, chosenClass, skillPoints, skillTreeLevels.toList(), chosenSect, chosenAlliance, rareSkill, selectedWeapon, activeSkillNode, equippedWeaponId, ownedWeaponIds, ownedGearIds, equippedGearIds.toList(), questClaimed, storyChapter, skillMaterials.toList(), autoBattle, rotationOrder.toList()) {
        prefs.edit()
            .putInt("qi", qi)
            .putInt("stones", stones)
            .putInt("stage", stage)
            .putString("owned_pet_ids", ownedPetIds)
            .putString("active_pet_id", activePetId)
            .putInt("pet_essence", petEssence)
            .putInt("pet_cores", petCores)
            .putInt("pet_pity", petPity)
            .also { editor -> petCatalog.forEachIndexed { index, pet ->
                editor.putInt("pet_level_${pet.id}", petLevels[index])
                editor.putInt("pet_xp_${pet.id}", petBondXp[index])
                editor.putInt("pet_ascension_${pet.id}", petAscensions[index])
            } }
            .putLong("daily_day", todayKey)
            .putInt("daily_wins", dailyWins)
            .putBoolean("daily_garden", dailyGardenCollected)
            .putBoolean("daily_meditated", dailyMeditated)
            .putBoolean("daily_clan_donated", dailyClanDonated)
            .putInt("daily_claims", dailyClaims)
            .putInt("clan_reputation", clanReputation)
            .putInt("abode_level", abodeLevel)
            .putInt("abode_iron_ore", ironOre)
            .putInt("abode_herbs", spiritHerbs)
            .putInt("abode_water", spiritWater)
            .putInt("abode_qi_pills", qiPills)
            .putInt("abode_body_pills", bodyPills)
            .putInt("abode_body_bonus", bodyPillBonus)
            .putInt("abode_forge_temper", forgeTemper)
            .putInt("abode_alchemy_rank", alchemyRank)
            .putLong("abode_garden_ready", gardenReadyAt)
            .putLong("abode_spring_ready", springReadyAt)
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
            .putString("owned_gear_ids", ownedGearIds)
            .also { editor -> gearSlots.forEachIndexed { index, slot -> editor.putString("gear_${slot.name}", equippedGearIds.getOrElse(index) { "" }) } }
            .putInt("active_skill_node", activeSkillNode)
            .putBoolean("town_quest_claimed", questClaimed)
            .putInt("town_story_chapter", storyChapter)
            .putString("skill_materials", skillMaterials.joinToString(","))
            .putBoolean("auto_battle", autoBattle)
            .putString("battle_rotation_v2", rotationOrder.joinToString(","))
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
            Modifier.fillMaxSize().background(Ink).verticalScroll(pageScrollState).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("ПУТЬ БЕССМЕРТНОГО", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Cultivate", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Text("${worldName(stage)} · Тихая сила рождается внутри.", color = Jade)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Ци", qi.toString(), Modifier.weight(1f))
                StatCard("Камни духа", stones.toString(), Modifier.weight(1f))
                StatCard("Победы", wins.toString(), Modifier.weight(1f))
            }

            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Город", "Путь", "Арена", "Персонаж", "Инвентарь", "Навыки", if (stage < 7) "Кланы" else "Секты", "Спутник", "Обитель").forEachIndexed { index, label ->
                    if (selectedMenu == index) Button(onClick = { selectedMenu = index; selectedTownBuilding = "" }) { Text(label) }
                    else OutlinedButton(onClick = { selectedMenu = index; selectedTownBuilding = "" }) { Text(label) }
                }
            }

            if (selectedMenu == 0) {
                GameCard("Городок Лунной Гавани", "Тихая долина у подножия духовных гор · ступень $stage") {
                    TownScene(stage, storyChapter)
                    Text("Главная площадь освещена фонарями Ци. Каждое здание — отдельное место, где живут торговцы, мастера и искатели приключений.", color = Color.LightGray)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TownBuilding("Лавка", "Расходники и материалы", "🏮", Modifier.weight(1f)) { selectedTownBuilding = "shop" }
                        TownBuilding("Аукцион", "Торги между искателями", "💎", Modifier.weight(1f)) { selectedTownBuilding = "auction" }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TownBuilding("Кузница", "Оружие и мастерство", "⚒️", Modifier.weight(1f)) { selectedTownBuilding = "forge" }
                        TownBuilding("Доска заданий", "Поручения и награды", "📜", Modifier.weight(1f)) { selectedTownBuilding = "quests" }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TownBuilding("Павильон культивации", "Медитация и прорыв", "☯️", Modifier.weight(1f)) { selectedMenu = 1 }
                        TownBuilding("Арена", "Испытания и добыча", "⚔️", Modifier.weight(1f)) { selectedMenu = 2 }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TownBuilding("Зал навыков", "Изучение боевых искусств", "✨", Modifier.weight(1f)) { selectedMenu = 5 }
                        TownBuilding(if (stage < 7) "Кланы" else "Секты", if (stage < 7) "Дома и союзы долин" else "Искусства и наставники", "🏯", Modifier.weight(1f)) { selectedMenu = 6 }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TownBuilding("Зверинец", "Поиск и воспитание питомцев", "🐾", Modifier.weight(1f)) { selectedMenu = 7 }
                        TownBuilding("Склад", "Экипировка и находки", "🎒", Modifier.weight(1f)) { selectedMenu = 4 }
                    }
                    TownBuilding("Личная обитель", "Медитация · кузница · алхимия", "🏡", Modifier.fillMaxWidth()) { selectedMenu = 8; abodeSection = "sanctuary" }
                }
                when (selectedTownBuilding) {
                    "shop" -> GameCard("Лавка Мэй", "Торговка материалов") {
                        Text("Мэй продаёт простые осколки духа для развития техник выше 20-го уровня.", color = Color.LightGray)
                        Text("Осколок духа: 200 камней · в наличии: ${skillMaterials[0]}", color = Jade)
                        Button(onClick = { if (stones >= 200) { stones -= 200; skillMaterials[0]++; message = "Куплен осколок духа." } else message = "Не хватает камней духа." }, enabled = stones >= 200, modifier = Modifier.fillMaxWidth()) { Text("Купить осколок · 200") }
                        OutlinedButton(onClick = { selectedTownBuilding = "" }) { Text("Назад на площадь") }
                    }
                    "auction" -> GameCard("Аукционный павильон", "Предложения странствующих культиваторов") {
                        val listings = weaponCatalog.filter { it.requiredStage <= stage }.sortedByDescending { weaponPowerScore(it) }.take(4)
                        listings.forEach { listing ->
                            val price = 250 + weaponPowerScore(listing) * 18
                            Text("${listing.name} · ${listing.rarity.title}", color = weaponRarityColor(listing.rarity), fontWeight = FontWeight.Bold)
                            Text("Рейтинг ${weaponPowerScore(listing)} · цена $price камней", color = Color.LightGray, fontSize = 11.sp)
                            OutlinedButton(onClick = {
                                if (stones >= price) { stones -= price; ownedWeaponIds = (ownedWeaponList + encodeWeaponInstance(listing)).joinToString("|"); message = "На аукционе приобретено: ${listing.name}." }
                                else message = "Недостаточно камней для ставки."
                            }, enabled = stones >= price, modifier = Modifier.fillMaxWidth()) { Text("Купить лот · $price") }
                        }
                        Divider(color = Color(0xFF3B4B4F))
                        Text("Редкие предметы экипировки", color = Gold, fontWeight = FontWeight.Bold)
                        gearCatalog.filter { it.requiredStage <= stage }.sortedByDescending { gearPowerScore(it) }.take(3).forEach { listing ->
                            val price = 350 + gearPowerScore(listing) * 16
                            Text("${listing.name} · ${listing.rarity.title}", color = weaponRarityColor(listing.rarity), fontWeight = FontWeight.Bold)
                            Text("${listing.slot.title} · набор «${gearSetNames[listing.setId]}» · рейтинг ${gearPowerScore(listing)} · $price камней", color = Color.LightGray, fontSize = 11.sp)
                            OutlinedButton(onClick = {
                                if (stones >= price) {
                                    stones -= price
                                    ownedGearIds = (ownedGearList + encodeGearInstance(listing)).joinToString("|")
                                    message = "На аукционе приобретено: ${listing.name}."
                                } else message = "Недостаточно камней для покупки."
                            }, enabled = stones >= price, modifier = Modifier.fillMaxWidth()) { Text("Купить экипировку · $price") }
                        }
                        OutlinedButton(onClick = { selectedTownBuilding = "" }) { Text("Назад на площадь") }
                    }
                    "forge" -> GameCard("Кузница Чэня", "Мастерская оружейника") {
                        Text("Чэнь изучает найденное оружие и помогает подготовить его к бою. Выбери предмет в инвентаре, чтобы сравнить характеристики и экипировать его.", color = Color.LightGray)
                        Button(onClick = { selectedMenu = 4; selectedTownBuilding = "" }, modifier = Modifier.fillMaxWidth()) { Text("Открыть склад оружия") }
                        OutlinedButton(onClick = { selectedTownBuilding = "" }) { Text("Назад на площадь") }
                    }
                    "event" -> GameCard("Юнь · посыльный ярмарки", "События и вести из соседних долин") {
                        Text("«На площади сегодня ярмарка духовных огней. Торговцы привезли редкие находки, а вечером у восточных ворот появится странствующий рассказчик».", color = Color.LightGray)
                        Text("Текущая история: глава ${storyChapter + 1} из 4", color = Gold)
                        OutlinedButton(onClick = { selectedTownBuilding = ""; message = "Юнь обещал сообщить, когда начнётся новое событие." }) { Text("Вернуться к жителям") }
                    }
                    "quests" -> GameCard("Доска поручений", "Старейшина Ли ждёт помощи") {
                        Text("Старейшина Ли: «Страж святилища снова тревожит долину. Одолей его хотя бы один раз и принеси весть о победе».", color = Color.LightGray)
                        Text("Поручение: победить стража · прогресс ${wins.coerceAtMost(1)}/1", color = Jade)
                        LinearProgressIndicator(progress = { if (wins > 0) 1f else 0f }, modifier = Modifier.fillMaxWidth(), color = Jade)
                        Button(onClick = {
                            if (wins > 0 && !questClaimed) { stones += 180; skillMaterials[0] += 2; questClaimed = true; storyChapter = 1; message = "Поручение выполнено! Получено 180 камней и 2 осколка духа." }
                            else if (questClaimed) message = "Старейшина уже наградил тебя. Скоро появится новое поручение."
                            else message = "Сначала одолей стража на арене."
                        }, enabled = wins > 0 && !questClaimed, modifier = Modifier.fillMaxWidth()) { Text(if (questClaimed) "Поручение выполнено" else "Получить награду") }
                        OutlinedButton(onClick = { selectedTownBuilding = "" }) { Text("Назад на площадь") }
                    }
                }
                GameCard("Жители Лунной Гавани", "Люди, у которых начинается твоя история") {
                    Text("Старейшина Ли · наставник долины", color = Gold, fontWeight = FontWeight.Bold)
                    Text(if (storyChapter == 0) "«Докажи, что способен защитить этот город. Тогда я расскажу, почему древние врата вновь подают знаки»." else "«Ты помог нашей долине. За северными воротами пробуждается древний путь — это только начало твоего странствия».", color = Color.LightGray)
                    OutlinedButton(onClick = { message = if (storyChapter == 0) "Старейшина Ли: начни с поручения на доске заданий." else "Старейшина Ли: северные врата откроются в следующей главе."; selectedTownBuilding = "quests" }, modifier = Modifier.fillMaxWidth()) { Text("Старейшина Ли · сюжет и задания") }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onClick = { selectedTownBuilding = "shop"; message = "Мэй: «Загляни в мою лавку, путник. У меня есть полезные осколки Ци»." }, modifier = Modifier.weight(1f)) { Text("Мэй · торговка") }
                        OutlinedButton(onClick = { selectedTownBuilding = "forge"; message = "Чэнь: «Покажи оружие и доспехи — подскажу, что лучше надеть»." }, modifier = Modifier.weight(1f)) { Text("Чэнь · кузнец") }
                    }
                    OutlinedButton(onClick = { selectedTownBuilding = "event"; message = "Юнь: «У меня свежие вести о событиях долины!»." }, modifier = Modifier.fillMaxWidth()) { Text("Юнь · посыльный событий") }
                }
                GameCard("Событие долины", "Ярмарка духовных огней") {
                    Text("Сегодня на площади проходит небольшая ярмарка. Забери разовый подарок для путника.", color = Color.LightGray)
                    Button(onClick = { if (prefs.getBoolean("lantern_event_claimed", false)) message = "Подарок ярмарки уже получен." else { stones += 75; prefs.edit().putBoolean("lantern_event_claimed", true).apply(); message = "За участие в ярмарке получено 75 камней духа." } }, modifier = Modifier.fillMaxWidth()) { Text("Получить подарок события · 75 камней") }
                }
            }

            if (selectedMenu == 8) {
                val currentWorld = worldName(stage)
                val abodeUpgradeCost = abodeLevel * 100
                val oreUpgradeCost = (abodeLevel - 1).coerceAtLeast(0)
                val nowMillis = System.currentTimeMillis()
                val gardenRemaining = formatRemainingTime(gardenReadyAt - nowMillis)
                val springRemaining = formatRemainingTime(springReadyAt - nowMillis)
                GameCard("Личная обитель", "Уровень $abodeLevel · $currentWorld · тихое место для роста") {
                    AbodeScene(abodeLevel, currentWorld)
                    Text("Небольшой двор в стороне от Лунной Гавани. Здесь ты можешь укрыться от суеты, собирать Ци и постепенно превращать скромное убежище в настоящую резиденцию культиватора.", color = Color.LightGray)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard("Железная руда", ironOre.toString(), Modifier.weight(1f))
                        StatCard("Травы", spiritHerbs.toString(), Modifier.weight(1f))
                        StatCard("Духовная вода", spiritWater.toString(), Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("sanctuary" to "Двор", "forge" to "Кузница", "alchemy" to "Алхимия", "garden" to "Сад духов", "worlds" to "Миры").forEach { (key, label) ->
                            if (abodeSection == key) Button(onClick = { abodeSection = key }) { Text(label) }
                            else OutlinedButton(onClick = { abodeSection = key }) { Text(label) }
                        }
                    }
                    when (abodeSection) {
                        "sanctuary" -> {
                            Text("Пещера уединения", color = Gold, fontWeight = FontWeight.Bold)
                            Text("Ци накапливается автоматически даже вне игры. Уровень обители ускоряет медитацию: сейчас одна единица Ци каждые ${qiIntervalForAbode(abodeLevel) / 60} мин. Прогресс сохраняется.", color = Color.LightGray, fontSize = 12.sp)
                            Button(onClick = { if (!dailyMeditated) { val gain = 25 + abodeLevel * 5; qi = (qi + gain).coerceAtMost(9999); dailyMeditated = true; message = "Медитация завершена: собрано $gain Ци. Возвращайся завтра." } else message = "Сегодня ты уже завершил основную медитацию. Продолжай пассивное накопление." }, modifier = Modifier.fillMaxWidth()) { Text(if (dailyMeditated) "Ежедневная медитация завершена" else "Провести медитацию · +${25 + abodeLevel * 5} Ци") }
                            Divider(color = Color(0xFF3B4B4F))
                            Text("Развитие обители", color = Gold, fontWeight = FontWeight.Bold)
                            Text(if (abodeLevel < maxAbodeLevel) "Следующий уровень: $abodeUpgradeCost камней духа и $oreUpgradeCost руды. Ускоряет пассивное накопление Ци и расширяет хозяйство. Предел текущего мира: $maxAbodeLevel." else "Обитель достигла предела текущего мира ($maxAbodeLevel). Следующий предел откроется после перехода в новый мир.", color = Color.LightGray, fontSize = 12.sp)
                            Button(onClick = {
                                if (abodeLevel >= maxAbodeLevel) message = "Для дальнейшего расширения обители потребуется сила следующего мира."
                                else if (stones >= abodeUpgradeCost && ironOre >= oreUpgradeCost) {
                                    stones -= abodeUpgradeCost; ironOre -= oreUpgradeCost; abodeLevel++
                                    message = "Обитель улучшена до $abodeLevel уровня. Потоки Ци стали плотнее."
                                } else message = "Не хватает ресурсов для улучшения обители."
                            }, enabled = abodeLevel < maxAbodeLevel && stones >= abodeUpgradeCost && ironOre >= oreUpgradeCost, modifier = Modifier.fillMaxWidth()) { Text(if (abodeLevel >= maxAbodeLevel) "Предел текущего мира" else "Улучшить обитель · $abodeUpgradeCost камней") }
                            Text("Склад обители · пилюли Ци: $qiPills · пилюли крепкого тела: $bodyPills · постоянная закалка: +$bodyPillBonus к здоровью", color = Jade, fontSize = 11.sp)
                        }
                        "forge" -> {
                            Text("Кузница у каменного склона", color = Gold, fontWeight = FontWeight.Bold)
                            Text("Пока здесь только простой горн и ручной молот. Чэнь помог наладить его работу: железная руда добывается в боях со стражами, а закалка усиливает надетое оружие, не уничтожая его историю и случайные свойства.", color = Color.LightGray, fontSize = 12.sp)
                            Text("Оружие: ${equippedWeapon.name} · заточка обители +$forgeTemper · атака +${equippedWeapon.attackBonus} · доп. урон +${equippedWeapon.bonusDamage}", color = Jade)
                            val temperCost = 60 + forgeTemper * 40
                            val temperOre = 1 + forgeTemper / 3
                            Button(onClick = {
                                if (ironOre >= temperOre && stones >= temperCost) {
                                    val oldItem = resolveWeaponInstance(equippedWeaponId) ?: weaponCatalog.first()
                                    val upgraded = oldItem.copy(attackBonus = oldItem.attackBonus + 2, defenseBonus = oldItem.defenseBonus + 1, bonusDamage = oldItem.bonusDamage + 1)
                                    val encodedOld = encodeWeaponInstance(oldItem)
                                    val encodedNew = encodeWeaponInstance(upgraded)
                                    ownedWeaponIds = ownedWeaponList.map { if (it == encodedOld) encodedNew else it }.let { if (encodedOld !in ownedWeaponList) it + encodedNew else it }.joinToString("|")
                                    equippedWeaponId = encodedNew
                                    ironOre -= temperOre; stones -= temperCost; forgeTemper++
                                    message = "Закалка завершена: атака +2, защита +1, дополнительный урон +1."
                                } else message = "Для закалки нужны $temperOre ед. руды и $temperCost камней духа."
                            }, enabled = ironOre >= temperOre && stones >= temperCost, modifier = Modifier.fillMaxWidth()) { Text("Закалить оружие · $temperOre руды · $temperCost камней") }
                            Text("Кузнечный навык: ${1 + forgeTemper / 3} · каждая третья закалка повышает мастерство кузницы.", color = Color.LightGray, fontSize = 11.sp)
                        }
                        "alchemy" -> {
                            Text("Алхимическая печь", color = Gold, fontWeight = FontWeight.Bold)
                            Text("Алхимия Смертного мира не творит чудес: простые травы и вода из родника превращаются в пилюли, помогающие пережить путь и ускорить накопление Ци.", color = Color.LightGray, fontSize = 12.sp)
                            Text("Ранг алхимии: $alchemyRank / $maxAlchemyRank · готовых пилюль Ци: $qiPills · пилюль закалки: $bodyPills", color = Jade)
                            Button(onClick = {
                                if (spiritHerbs >= 6 && spiritWater >= 2) { spiritHerbs -= 6; spiritWater -= 2; qiPills++; message = "Создана пилюля сбора Ци." }
                                else message = "Нужно 6 духовных трав и 2 порции духовной воды."
                            }, enabled = spiritHerbs >= 6 && spiritWater >= 2, modifier = Modifier.fillMaxWidth()) { Text("Сварить пилюлю Ци · 6 трав + 2 воды") }
                            Button(onClick = {
                                if (spiritHerbs >= 5 && spiritWater >= 2) { spiritHerbs -= 5; spiritWater -= 2; bodyPills++; message = "Создана пилюля крепкого тела." }
                                else message = "Нужно 5 духовных трав и 2 порции духовной воды."
                            }, enabled = spiritHerbs >= 5 && spiritWater >= 2, modifier = Modifier.fillMaxWidth()) { Text("Сварить пилюлю закалки · 5 трав + 2 вода") }
                            OutlinedButton(onClick = {
                                val qiPower = 25 + alchemyRank * 5
                                if (qiPills > 0) { qiPills--; qi = (qi + qiPower).coerceAtMost(9999); message = "Пилюля растворилась, даровав $qiPower Ци." }
                                else message = "В запасе нет пилюль сбора Ци."
                            }, modifier = Modifier.fillMaxWidth()) { Text("Принять пилюлю Ци · +${25 + alchemyRank * 5}") }
                            OutlinedButton(onClick = {
                                val bodyPower = 15 + alchemyRank * 2
                                if (bodyPills > 0) { bodyPills--; bodyPillBonus += bodyPower; playerHp = (playerHp + bodyPower).coerceAtMost(playerMaxHp + bodyPower); message = "Тело укреплено навсегда: максимальное здоровье +$bodyPower." }
                                else message = "В запасе нет пилюль закалки."
                            }, modifier = Modifier.fillMaxWidth()) { Text("Принять пилюлю закалки · здоровье +${15 + alchemyRank * 2} навсегда") }
                            if (alchemyRank < maxAlchemyRank && spiritHerbs >= alchemyRank * 4 && spiritWater >= alchemyRank) {
                                OutlinedButton(onClick = { spiritHerbs -= alchemyRank * 4; spiritWater -= alchemyRank; alchemyRank++; message = "Ранг алхимии повышен до $alchemyRank. Пилюли стали эффективнее." }, modifier = Modifier.fillMaxWidth()) { Text("Повысить ранг алхимии · травы ${alchemyRank * 4} + вода $alchemyRank") }
                            } else Text(if (alchemyRank >= maxAlchemyRank) "Достигнут предел алхимии текущего мира: $maxAlchemyRank." else "Для следующего ранга нужны ${alchemyRank * 4} трав и $alchemyRank порций воды.", color = Color.LightGray, fontSize = 11.sp)
                        }
                        "garden" -> {
                            Text("Сад духовных трав", color = Gold, fontWeight = FontWeight.Bold)
                            Text("На небольшом участке растут самые простые травы Смертного мира. Позже, в других мирах, сад сможет принимать семена с совершенно иной природой.", color = Color.LightGray, fontSize = 12.sp)
                            Text(if (gardenReadyAt <= nowMillis) "Урожай созрел: можно собрать травы." else "До следующего урожая примерно $gardenRemaining.", color = Jade)
                            Button(onClick = {
                                val now = System.currentTimeMillis()
                                if (now >= gardenReadyAt) { val harvest = 2 + abodeLevel; spiritHerbs += harvest; dailyGardenCollected = true; gardenReadyAt = now + 28_800_000L; if (Random.nextInt(100) < 3 && "moon_deer" !in ownedPetIds.split("|")) { ownedPetIds += "|moon_deer"; message = "Собрано трав: $harvest. Среди листьев появился лунный оленёнок — редкий питомец!" } else message = "Собрано духовных трав: $harvest. Следующий урожай примерно через 8 часов." }
                                else message = "Сад ещё растёт. Вернись позже."
                            }, enabled = gardenReadyAt <= nowMillis, modifier = Modifier.fillMaxWidth()) { Text("Собрать урожай · ${2 + abodeLevel} травы") }
                            Text("Урожайность растёт вместе с уровнем обители.", color = Color.LightGray, fontSize = 11.sp)
                        }
                        "worlds" -> {
                            Text("Путь через четыре мира", color = Gold, fontWeight = FontWeight.Bold)
                            worldEntries.forEachIndexed { index, entry ->
                                val unlocked = stage >= entry.second
                                Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("${index + 1}. ${entry.first}", color = if (unlocked) Jade else Gold, fontWeight = FontWeight.Bold)
                                    Text(entry.third, color = Color.LightGray, fontSize = 11.sp)
                                    Text(if (unlocked) "Доступен · текущая ступень $stage" else "Запечатан · требуется ступень ${entry.second}", color = if (unlocked) Jade else Color.Gray, fontSize = 10.sp)
                                }
                            }
                            Text("Сейчас ты лишь начинаешь путь. За пределами смертных земель существуют целые цивилизации, законы Дао и испытания, о которых жители Лунной Гавани даже не подозревают.", color = Gold, fontSize = 12.sp)
                        }
                    }
                }
                GameCard("Родник и хозяйство", "Небольшие источники ресурсов для ремесла") {
                    Text(if (springReadyAt <= nowMillis) "Родник наполнился духовной водой." else "Родник восстановится примерно через $springRemaining.", color = Color.LightGray, fontSize = 12.sp)
                    Button(onClick = {
                        val now = System.currentTimeMillis()
                        if (now >= springReadyAt) { spiritWater += 1 + abodeLevel / 3; springReadyAt = now + 28_800_000L; if (Random.nextInt(100) < 3 && "river_koi" !in ownedPetIds.split("|")) { ownedPetIds += "|river_koi"; message = "В роднике вспыхнула золотая чешуя! Ты приручил духовного карпа." } else message = "Набрана духовная вода из родника." }
                        else message = "Родник ещё не восстановился."
                    }, enabled = springReadyAt <= nowMillis, modifier = Modifier.fillMaxWidth()) { Text("Набрать духовную воду") }
                }
            }

            if (selectedMenu == 1) {
            GameCard("Культивация", "Ступень $stage · ${stageName(stage)}") {
                val breakthroughCost = cultivationQiCost(stage)
                val requiredBossRank = stage + 1
                Text(
                    "Путь рассчитан на недели, а не часы: пассивное накопление работает и вне игры до 8 часов. Для прорыва нужно $breakthroughCost Ци и победа над стражем ранга $requiredBossRank.",
                    color = Color.LightGray
                )
                LinearProgressIndicator(
                    progress = { (qi.toFloat() / breakthroughCost.coerceAtLeast(1)).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = Jade
                )
                Button(
                    onClick = {
                        if (qi >= breakthroughCost && bossLevel >= requiredBossRank) {
                            qi -= breakthroughCost
                            val oldWorld = worldName(stage)
                            stage++
                            skillPoints++
                            stones += 25
                            if (worldName(stage) != oldWorld) {
                                chosenSect = -1
                                chosenAlliance = -1
                                message = "Врата открылись! Ты покинул $oldWorld и вступил в ${worldName(stage)}. Старые клановые союзы остались позади — теперь предстоит искать секту."
                            } else message = "Прорыв свершился! Ты достиг ступени $stage в мире ${worldName(stage)}."
                        } else if (bossLevel < requiredBossRank) {
                            message = "Сначала одолей стража ранга $requiredBossRank, чтобы доказать готовность к следующей ступени."
                        } else {
                            message = "Недостаточно Ци. Продолжай медитировать и выполняй ежедневные поручения."
                        }
                    },
                    enabled = qi >= breakthroughCost && bossLevel >= requiredBossRank,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Совершить прорыв · $breakthroughCost Ци") }
            }
                GameCard("Ежедневный путь", "Занятия, которые поддерживают темп долгой культивации") {
                    Text("Победы сегодня: $dailyWins / 3 · сад собран: ${if (dailyGardenCollected) "да" else "нет"} · медитация: ${if (dailyMeditated) "завершена" else "не выполнена"}", color = Color.LightGray, fontSize = 12.sp)
                    val battleClaimed = dailyClaims and 1 != 0
                    Button(onClick = { if (dailyWins >= 3 && !battleClaimed) { qi += 60; stones += 100; dailyClaims = dailyClaims or 1; message = "Ежедневное испытание завершено: +60 Ци и 100 камней." } }, enabled = dailyWins >= 3 && !battleClaimed, modifier = Modifier.fillMaxWidth()) { Text(if (battleClaimed) "Испытание стражей · получено" else "Победить 3 раза · 60 Ци + 100 камней") }
                    val gardenClaimed = dailyClaims and 2 != 0
                    Button(onClick = { if (dailyGardenCollected && !gardenClaimed) { qi += 45; spiritHerbs += 2; dailyClaims = dailyClaims or 2; message = "За заботу о саде получено 45 Ци и 2 травы." } }, enabled = dailyGardenCollected && !gardenClaimed, modifier = Modifier.fillMaxWidth()) { Text(if (gardenClaimed) "Урожай дня · получено" else "Собрать сад · 45 Ци + 2 травы") }
                    val meditationClaimed = dailyClaims and 4 != 0
                    Button(onClick = { if (dailyMeditated && !meditationClaimed) { qi += 20; stones += 50; dailyClaims = dailyClaims or 4; message = "За ежедневную медитацию получено 20 Ци и 50 камней." } }, enabled = dailyMeditated && !meditationClaimed, modifier = Modifier.fillMaxWidth()) { Text(if (meditationClaimed) "Медитация дня · получено" else "Завершить медитацию · 20 Ци + 50 камней") }
                }
            }

            if (selectedMenu == 2) {
            GameCard("Арена испытаний", "Страж святилища · ранг $bossLevel") {
                BattleScene(autoBattle, message)
                Text("Твоё здоровье: ${playerHp.coerceAtMost(playerMaxHp)} / $playerMaxHp", color = Jade)
                LinearProgressIndicator(progress = { (playerHp.toFloat() / playerMaxHp).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth(), color = Jade)
                Text("Здоровье противника: $enemyHp / $enemyMaxHp", color = Color.LightGray)
                LinearProgressIndicator(
                    progress = { (enemyHp.toFloat() / enemyMaxHp).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFD77A68)
                )
                Button(
                    onClick = { performCombatTurn() },
                    enabled = enemyHp > 0 && playerHp > 0 && !autoBattle,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Сделать следующий ход") }
                Button(onClick = { autoBattle = !autoBattle }, enabled = enemyHp > 0 && playerHp > 0, modifier = Modifier.fillMaxWidth()) {
                    Text(if (autoBattle) "Остановить автобой" else "Наблюдать за боем · авто")
                }
                Text("Ротация: " + rotationOrder.joinToString(" → ") { action ->
                    when {
                        action in 0..8 && chosenClass >= 0 -> skillDefinitions[chosenClass][action].name
                        action == 9 -> "Техника"
                        action == 10 -> "Оружие"
                        else -> "Навык"
                    }
                }, color = Gold, fontSize = 11.sp)
                Text("Перезарядка · выбранный навык ${skillCooldowns[activeSkillNode]} · техника $techniqueCooldown · оружие $weaponCooldown", color = Color.LightGray, fontSize = 11.sp)

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

            if (selectedMenu == 3) GameCard("Персонаж", if (chosenClass < 0) "Выбери свой путь культивации" else classNames[chosenClass]) {
                Text("Характеристики и экипировка находятся здесь. Коллекция предметов — в отдельном инвентаре.", color = Color.LightGray)
                Text("Атака с экипировкой: +$classBonus · защита: +$classDefense · здоровье: $playerMaxHp", color = Jade, fontWeight = FontWeight.Bold)
                gearSlots.forEachIndexed { index, slot ->
                    val item = equippedGear.firstOrNull { it.slot == slot }
                    Text("${slot.title}: ${item?.name ?: "Пусто"}", color = if (item == null) Color.LightGray else weaponRarityColor(item.rarity), fontSize = 12.sp)
                }
                Text(activeSetBonus.description.ifBlank { "Бонус комплекта: не активен · собери 2 или 4 предмета одного набора." }, color = Gold, fontSize = 11.sp)
                classNames.forEachIndexed { i, n ->
                    val profile = classCombatProfiles[i]
                    Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(n, color = Gold, fontWeight = FontWeight.Bold)
                        Text(classDescriptions[i], color = Color.LightGray, fontSize = 12.sp)
                        Text("Атака +${profile.attack} · Защита +${profile.defense} · Здоровье +${profile.bonusHp} · Крит. шанс ${profile.criticalChance}%", color = Jade, fontSize = 11.sp)
                        Text("Сила техник +${profile.skillPower} · Бонус лечения +${profile.healingBonus}", color = Jade, fontSize = 11.sp)
                        OutlinedButton(onClick = {
                            chosenClass = i
                            rotationOrder.clear()
                            (0..8).filter { skillTreeLevels[i*9+it] == 1 && skillDefinitions[i][it].active }.forEach { rotationOrder.add(it) }
                            rotationOrder.add(9)
                            rotationOrder.add(10)
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

            if (selectedMenu == 4) GameCard("Инвентарь", "Оружие: ${ownedWeapons.size} · броня и артефакты: ${ownedGear.size}") {
                Text("Оружие, броня, шлемы, кольца и артефакты хранятся отдельно от надетых предметов. Каждый экземпляр получает собственный разброс характеристик.", color = Color.LightGray, fontSize = 12.sp)
                ownedWeapons.sortedByDescending { weaponPowerScore(it) }.forEach { item ->
                    Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${item.name} · ${item.rarity.title}", color = weaponRarityColor(item.rarity), fontWeight = FontWeight.Bold)
                        Text("${weaponNames[item.type]} · ${stageName(item.requiredStage)} · ступень ${item.requiredStage}", color = Color.LightGray, fontSize = 11.sp)
                        Text("Рейтинг ${weaponPowerScore(item)} · атака +${item.attackBonus} · доп. урон +${item.bonusDamage} · защита +${item.defenseBonus} · лечение +${item.healOnHit}", color = Jade, fontSize = 11.sp)
                        Text(item.effectDescription, color = Color.LightGray, fontSize = 11.sp)
                        val powerDifference = weaponPowerScore(item) - weaponPowerScore(equippedWeapon)
                        Text(
                            when {
                                encodeWeaponInstance(item) == encodeWeaponInstance(equippedWeapon) -> "Сейчас надето"
                                powerDifference > 0 -> "Рейтинг на +$powerDifference выше текущего оружия"
                                powerDifference < 0 -> "Рейтинг на ${-powerDifference} ниже текущего оружия"
                                else -> "Рейтинг равен текущему оружию"
                            },
                            color = if (powerDifference > 0) Jade else Color.LightGray,
                            fontSize = 11.sp
                        )
                        OutlinedButton(
                            onClick = { equippedWeaponId = encodeWeaponInstance(item); selectedWeapon = item.type; message = "Экипировано: ${item.name}" },
                            enabled = stage >= item.requiredStage,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (equippedWeaponId == encodeWeaponInstance(item)) "Надето" else if (stage < item.requiredStage) "Откроется на ступени ${item.requiredStage}" else "Экипировать")
                        }
                    }
                }
                Divider(color = Color(0xFF3B4B4F))
                Text("Броня, шлемы, кольца и артефакты", color = Gold, fontWeight = FontWeight.Bold)
                Text(activeSetBonus.description.ifBlank { "Комплектные бонусы: 2 предмета дают малый бонус, 4 предмета — полный." }, color = Jade, fontSize = 11.sp)
                ownedGear.sortedByDescending { gearPowerScore(it) }.forEach { item ->
                    Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${item.name} · ${item.rarity.title}", color = weaponRarityColor(item.rarity), fontWeight = FontWeight.Bold)
                        Text("${item.slot.title} · ${stageName(item.requiredStage)} · набор «${gearSetNames[item.setId] ?: item.setId}»", color = Color.LightGray, fontSize = 11.sp)
                        Text("Рейтинг ${gearPowerScore(item)} · атака +${item.attackBonus} · защита +${item.defenseBonus} · здоровье +${item.hpBonus} · сила техник +${item.skillBonus}", color = Jade, fontSize = 11.sp)
                        Text(item.effectDescription, color = Color.LightGray, fontSize = 11.sp)
                        val currentSlotItem = equippedGear.firstOrNull { it.slot == item.slot }
                        val comparison = if (currentSlotItem == null) "Слот свободен" else {
                            val delta = gearPowerScore(item) - gearPowerScore(currentSlotItem)
                            when { delta > 0 -> "Сильнее надетого предмета на $delta"; delta < 0 -> "Слабее надетого предмета на ${-delta}"; else -> "Рейтинг равен надетому предмету" }
                        }
                        Text(comparison, color = if (currentSlotItem == null || gearPowerScore(item) >= gearPowerScore(currentSlotItem)) Jade else Color.LightGray, fontSize = 11.sp)
                        val equipped = equippedGearIds.getOrElse(item.slot.ordinal) { "" } == encodeGearInstance(item)
                        OutlinedButton(
                            onClick = { equippedGearIds[item.slot.ordinal] = encodeGearInstance(item); message = "Экипировано: ${item.name}" },
                            enabled = stage >= item.requiredStage,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (equipped) "Надето" else if (stage < item.requiredStage) "Откроется на ступени ${item.requiredStage}" else "Надеть · ${item.slot.title}") }
                    }
                }
                val nextWeaponStage = listOf(1, 4, 7, 10, 13, 16).firstOrNull { it > stage }
                if (nextWeaponStage != null) {
                    val preview = weaponCatalog.first { it.requiredStage == nextWeaponStage }
                    Text("Следующий рубеж: ${preview.name} · ${preview.rarity.title}", color = Gold, fontSize = 12.sp)
                }
            }

            if (selectedMenu == 5) {
                GameCard("Боевые техники", "Выбранная техника: $activeTechniqueName · мастерство $activeTechniqueLevel") {
                    Text("Уровни 1–20 повышаются за камни духа. Начиная с 21-го уровня нужны материалы; каждые пять уровней открывается следующий, более редкий ресурс. На 50-м уровне техника получает дополнительный эффект +20%.", color = Color.LightGray)
                    Text("Материалы: " + skillMaterialNames.indices.joinToString(" · ") { "${skillMaterialNames[it]}: ${skillMaterials[it]}" }, color = Jade, fontSize = 11.sp)
                    TechniqueRow("Меч рассекающей Ци", "Сильный прямой удар", selectedTechnique == 0, swordLevel, techniqueUpgradeCost(swordLevel), stones,
                        onSelect = { selectedTechnique = 0 },
                        onUpgrade = { upgradeTechnique(swordLevel) { swordLevel = it } }
                    )
                    TechniqueRow("Нефритовый лотос", "Устойчивая техника духовной энергии", selectedTechnique == 1, lotusLevel, techniqueUpgradeCost(lotusLevel), stones,
                        onSelect = { selectedTechnique = 1 },
                        onUpgrade = { upgradeTechnique(lotusLevel) { lotusLevel = it } }
                    )
                    TechniqueRow("Коготь лиса", "Быстрая атака в союзе со спутником", selectedTechnique == 2, foxLevel, techniqueUpgradeCost(foxLevel), stones,
                        onSelect = { selectedTechnique = 2 },
                        onUpgrade = { upgradeTechnique(foxLevel) { foxLevel = it } }
                    )
                    Text("Дополнительный урон выбранной техники: +$techniqueDamage", color = Gold, fontWeight = FontWeight.Bold)
                }
                GameCard("Ротация автобоя", "Порядок автоматического применения навыков, техник и оружия") {
                    Text("Все изученные активные навыки добавляются в очередь автоматически. Меняй приоритет стрелками: система применяет первое готовое действие.", color = Color.LightGray, fontSize = 11.sp)
                    rotationOrder.forEachIndexed { index, action ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            val actionLabel = when {
                                action in 0..8 && chosenClass >= 0 -> skillDefinitions[chosenClass][action].name + " · КД 4 сек"
                                action == 9 -> "Техника · КД 3 сек"
                                action == 10 -> "Активация оружия · КД 6 сек"
                                else -> "Навык · КД 4 сек"
                            }
                            Text(actionLabel, color = Gold, modifier = Modifier.weight(1f), fontSize = 11.sp)
                            OutlinedButton(onClick = { if (index > 0) { val v = rotationOrder[index-1]; rotationOrder[index-1] = rotationOrder[index]; rotationOrder[index] = v } }, enabled = index > 0) { Text("↑") }
                            OutlinedButton(onClick = { if (index < rotationOrder.lastIndex) { val v = rotationOrder[index+1]; rotationOrder[index+1] = rotationOrder[index]; rotationOrder[index] = v } }, enabled = index < rotationOrder.lastIndex) { Text("↓") }
                        }
                    }
                }
                GameCard("Древо навыков", if (chosenClass < 0) "Сначала выбери класс в меню персонажа" else classNames[chosenClass]) {
                    if (chosenClass >= 0) {
                        Text("Очки навыков: $skillPoints · пассивных узлов: $passiveSkillCount", color = Gold, fontWeight = FontWeight.Bold)
                        SkillTreeSection("Начальный навык", chosenClass, listOf(0), skillTreeLevels, skillPoints, selectedWeapon, activeSkillNode, stage.coerceAtMost(20),
                            onLearn = { node -> learnSkillNode(node) },
                            onSetActive = { node -> activeSkillNode = node })
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Ветвь I · Напор", color = Jade, fontWeight = FontWeight.Bold)
                                SkillTreeSection("", chosenClass, listOf(1,2,3), skillTreeLevels, skillPoints, selectedWeapon, activeSkillNode, stage.coerceAtMost(20),
                                    { node -> learnSkillNode(node) }, { node -> activeSkillNode = node })
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Ветвь II · Искусность", color = Jade, fontWeight = FontWeight.Bold)
                                SkillTreeSection("", chosenClass, listOf(4,5,6), skillTreeLevels, skillPoints, selectedWeapon, activeSkillNode, stage.coerceAtMost(20),
                                    { node -> learnSkillNode(node) }, { node -> activeSkillNode = node })
                            }
                        }
                        SkillTreeSection("Слияние ветвей", chosenClass, listOf(7,8), skillTreeLevels, skillPoints, selectedWeapon, activeSkillNode, stage.coerceAtMost(20),
                            onLearn = { node -> learnSkillNode(node) },
                            onSetActive = { node -> activeSkillNode = node })
                        Text("Активный навык: ${skillDefinitions[chosenClass][activeSkillNode].name} · перезарядка: ${skillCooldowns[activeSkillNode]}", color = Gold, fontSize = 12.sp)
                    }
                    Text(if (rareSkill.isBlank()) "Редкое искусство: шанс выпадения за победу — 0,8%." else "Получено редкое искусство: $rareSkill", color = Gold)
                }
            }

            if (selectedMenu == 6) GameCard(if (stage < 7) "Кланы Смертного мира" else "Секты Духовного мира", if (stage < 7) "Родовые дома, торговые союзы и защитники долин" else "Праведный и Злой пути · новый уровень влияния") {
                Text(if (stage < 7) "В Смертном мире нет сект бессмертных. Здесь власть держат кланы и союзы: они защищают дороги, торгуют ресурсами и обучают простым искусствам." else "За пределами смертных земель культиваторы объединяются в секты, где передают наследия и изучают законы Дао.", color = Color.LightGray, fontSize = 12.sp)
                (if (stage < 7) listOf("Союз долин", "Вольные дома") else listOf("Праведный альянс", "Злой альянс")).forEachIndexed { index, label ->
                    OutlinedButton(onClick = { chosenAlliance = index; val alignment = if (stage < 7) factionAlliance else sectAlliance; if (chosenSect >= 0 && alignment[chosenSect] != index) chosenSect = -1; message = "Выбран $label" }, modifier = Modifier.fillMaxWidth()) { Text(label + if (chosenAlliance == index) " · выбран" else "") }
                }
                val factionNames = if (stage < 7) clanNames else sectNames
                val factionLore = if (stage < 7) clanLore else sectLore
                val factionAlliances = if (stage < 7) factionAlliance else sectAlliance
                factionNames.forEachIndexed { index, sect ->
                    if (chosenAlliance < 0 || factionAlliances[index] == chosenAlliance) {
                        Column(Modifier.fillMaxWidth().padding(8.dp)) {
                            Text(sect, color = Gold, fontWeight = FontWeight.Bold)
                            Text(factionLore[index], color = Color.LightGray, fontSize = 12.sp)
                            Text("Бонус союза: атака +${clanAttackBonuses[index]} · защита +${clanDefenseBonuses[index]} · здоровье +${clanHpBonuses[index]}", color = Jade, fontSize = 11.sp)
                            OutlinedButton(onClick = { chosenSect = index; message = if (stage < 7) "Ты вступил в клан «$sect»." else "Ты вступил в секту «$sect»." }, enabled = chosenAlliance == factionAlliances[index], modifier = Modifier.fillMaxWidth()) { Text(if (chosenSect == index) if (stage < 7) "Твой клан" else "Твоя секта" else "Вступить") }
                        }
                    }
                }
                Text("Репутация союза: $clanReputation · ежедневный вклад помогает заслужить доверие старейшин.", color = Gold, fontSize = 11.sp)
                Button(onClick = {
                    if (chosenSect < 0) message = "Сначала вступи в клан или секту."
                    else if (!dailyClanDonated && stones >= 50) { stones -= 50; qi += 35; clanReputation++; dailyClanDonated = true; message = "Вклад принят. +35 Ци и 1 очко репутации." }
                    else if (dailyClanDonated) message = "Сегодня ты уже внёс вклад."
                    else message = "Для вклада нужно 50 камней духа."
                }, enabled = chosenSect >= 0 && !dailyClanDonated && stones >= 50, modifier = Modifier.fillMaxWidth()) { Text(if (chosenSect < 0) "Сначала вступи в клан / секту" else if (dailyClanDonated) "Вклад сегодня внесён" else "Внести ежедневный вклад · 50 камней") }
            }

            if (selectedMenu == 7) GameCard("Духовный зверинец", "Коллекция питомцев · активен: ${activePet.name}") {
                Text("Каждый зверь меняет стиль боя. Опыт связи получает только активный питомец за победы. Развитие намеренно сложное: нужны опыт, эссенция зверя и ядра для ключевых рубежей.", color = Color.LightGray, fontSize = 12.sp)
                Text("Эссенция: $petEssence · ядра зверя: $petCores · поиск редкого зверя: $petPity / 20 побед", color = Jade, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("Активен: ${activePet.symbol} ${activePet.name} · уровень $petLevel / 30 · опыт связи $petXp · пробуждение $petAscension / 5", color = Gold)
                Text("Бонусы: атака +$petAttack · защита +$petDefense · здоровье +${activePet.hp + petLevel * 3 + if (petSynergy) 20 else 0} · техника +$petSkill · крит +${activePet.critical + if (petSynergy) 2 else 0}% · лечение +${activePet.healing + if (petSynergy) 2 else 0}", color = Jade, fontSize = 11.sp)
                Text(if (petSynergy) "Резонанс класса: активен · питомец усиливает твой путь" else "Резонанс класса: нет · выбери подходящий класс для полного раскрытия питомца", color = if (petSynergy) Gold else Color.Gray, fontSize = 10.sp)
                Text("Особенность спутника: ${petCombatTrait(activePet.id)}", color = Color.LightGray, fontSize = 10.sp)
                val xpCost = 100 + petLevel * 75
                val essenceCost = 1 + petLevel / 5
                val coreCost = if ((petLevel + 1) % 5 == 0) 1 else 0
                Button(onClick = {
                    if (petLevel < 30 && petXp >= xpCost && petEssence >= essenceCost && petCores >= coreCost) {
                        petBondXp[activePetIndex] = petXp - xpCost
                        petEssence -= essenceCost
                        petCores -= coreCost
                        petLevels[activePetIndex] = petLevel + 1
                        message = "${activePet.name} достиг ${petLevel + 1} уровня связи."
                    } else message = "Нужно $xpCost опыта связи, $essenceCost эссенции и $coreCost ядер зверя."
                }, enabled = petLevel < 30 && petXp >= xpCost && petEssence >= essenceCost && petCores >= coreCost, modifier = Modifier.fillMaxWidth()) {
                    Text(if (petLevel >= 30) "Предел уровня · требуется пробуждение" else "Укрепить связь · $xpCost XP · $essenceCost эссенции · $coreCost ядер")
                }
                if (petLevel >= 30 && petAscension < 5) {
                    Button(onClick = {
                        if (petEssence >= 20 && petCores >= 5) { petEssence -= 20; petCores -= 5; petAscensions[activePetIndex] = petAscension + 1; message = "${activePet.name} пробудил новую ступень силы!" }
                        else message = "Пробуждение требует 20 эссенции и 5 ядер зверя."
                    }, enabled = petEssence >= 20 && petCores >= 5, modifier = Modifier.fillMaxWidth()) { Text("Пробудить питомца · 20 эссенции + 5 ядер") }
                }
                petCatalog.forEachIndexed { index, pet ->
                    val owned = pet.id in ownedPetIds.split("|")
                    val isActive = pet.id == activePetId
                    Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${pet.symbol} ${pet.name} · ${pet.role}" + if (isActive) " · активен" else "", color = if (owned) Gold else Color.Gray, fontWeight = FontWeight.Bold)
                        Text(pet.description, color = Color.LightGray, fontSize = 11.sp)
                        Text("Сильная сторона: ${pet.strength} · слабость: ${pet.weakness}", color = Jade, fontSize = 10.sp)
                        Text("Подходит: ${pet.bestClass} · получение: ${pet.source}", color = Color.LightGray, fontSize = 10.sp)
                        if (owned) {
                            Text("Уровень ${petLevels[index]} · опыт ${petBondXp[index]} · пробуждение ${petAscensions[index]}/5", color = Jade, fontSize = 10.sp)
                            OutlinedButton(onClick = { activePetId = pet.id; message = "${pet.name} теперь твой спутник." }, enabled = !isActive, modifier = Modifier.fillMaxWidth()) { Text(if (isActive) "Выбран" else "Выбрать питомца") }
                        } else Text("Не приручён · ${pet.dropHint}", color = Color.Gray, fontSize = 10.sp)
                    }
                    Divider(color = Color(0xFF3B4B4F))
                }
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
            Text("$description · уровень $level / 50", color = Color.LightGray, fontSize = 12.sp)
            if (level == 20) Text("Достигнут рубеж: дальше нужны редкие материалы.", color = Gold, fontSize = 11.sp)
            if (level >= 50) Text("Предел достигнут · дополнительный эффект +20% к урону", color = Jade, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onSelect, modifier = Modifier.weight(1f)) { Text("Выбрать") }
                Button(onClick = onUpgrade, enabled = stones >= cost && level < 50, modifier = Modifier.weight(1f)) { Text(if(level >= 50) "Максимум" else if(level >= 20) "Улучшить · ресурс" else "Улучшить · $cost") }
            }
        }
    }
}

@Composable
private fun TownScene(stage: Int, storyChapter: Int) {
    val transition = rememberInfiniteTransition(label = "living-valley")
    val cloudDrift by transition.animateFloat(-0.08f, 0.08f, infiniteRepeatable(tween(18000), RepeatMode.Reverse), label = "cloud-drift")
    val lanternPulse by transition.animateFloat(0.42f, 1f, infiniteRepeatable(tween(1350), RepeatMode.Reverse), label = "lantern-pulse")
    val mistDrift by transition.animateFloat(0.12f, 0.34f, infiniteRepeatable(tween(7000), RepeatMode.Reverse), label = "mist-drift")
    val world = when { stage >= 19 -> 3; stage >= 13 -> 2; stage >= 7 -> 1; else -> 0 }
    val palette = when (world) {
        1 -> listOf(Color(0xFF101E36), Color(0xFF244C58), Color(0xFF172C36))
        2 -> listOf(Color(0xFF211B42), Color(0xFF51406F), Color(0xFF252D48))
        3 -> listOf(Color(0xFF160F2B), Color(0xFF3A285A), Color(0xFF171B34))
        else -> listOf(Color(0xFF101D35), Color(0xFF31545A), Color(0xFF182D36))
    }
    Canvas(Modifier.fillMaxWidth().height(342.dp)) {
        val w = size.width; val h = size.height
        fun p(x: Float, y: Float) = Offset(w * x, h * y)
        drawRect(brush = Brush.verticalGradient(palette))
        for (i in 0..42) {
            val x = ((i * 73) % 997) / 997f
            val y = ((i * 37) % 430) / 1000f
            val alpha = 0.18f + ((i * 19) % 60) / 100f
            drawCircle(Color(0xFFDCEBFF).copy(alpha = alpha), if (i % 7 == 0) 2.4f else 1.2f, p(x, y))
        }
        drawCircle(Color(0xFFFFE6B8).copy(alpha = 0.10f), w * 0.105f, p(0.82f, 0.17f))
        drawCircle(Color(0xFFFFEAC8).copy(alpha = 0.88f), w * 0.061f, p(0.82f, 0.17f))
        drawCircle(palette[0], w * 0.052f, p(0.846f, 0.145f))
        val far = Path().apply {
            moveTo(0f,h*.54f); lineTo(w*.10f,h*.31f); lineTo(w*.19f,h*.46f); lineTo(w*.34f,h*.20f)
            lineTo(w*.48f,h*.48f); lineTo(w*.62f,h*.25f); lineTo(w*.75f,h*.47f); lineTo(w*.91f,h*.22f)
            lineTo(w,h*.43f); lineTo(w,h*.76f); lineTo(0f,h*.76f); close()
        }
        drawPath(far, Color(0xFF273B50).copy(alpha=.92f))
        val mid = Path().apply {
            moveTo(0f,h*.63f); lineTo(w*.12f,h*.43f); lineTo(w*.23f,h*.60f); lineTo(w*.39f,h*.37f)
            lineTo(w*.55f,h*.62f); lineTo(w*.70f,h*.40f); lineTo(w*.86f,h*.61f); lineTo(w,h*.44f)
            lineTo(w,h); lineTo(0f,h); close()
        }
        drawPath(mid, Color(0xFF1D3D43))
        drawPath(Path().apply {
            moveTo(w*.62f,h*.43f); cubicTo(w*.60f,h*.57f,w*.69f,h*.62f,w*.66f,h*.78f)
            lineTo(w*.72f,h*.78f); cubicTo(w*.75f,h*.61f,w*.67f,h*.55f,w*.70f,h*.42f); close()
        }, Color(0xFF8CC8C2).copy(alpha=.45f))
        drawPath(Path().apply {
            moveTo(w*.68f,h*.77f); cubicTo(w*.62f,h*.82f,w*.78f,h*.87f,w*.54f,h)
            lineTo(w*.93f,h); cubicTo(w*.81f,h*.89f,w*.88f,h*.84f,w*.72f,h*.77f); close()
        }, Color(0xFF4D9DA0).copy(alpha=.52f))
        drawRect(Color(0xFF243E37), Offset(0f,h*.70f), Size(w,h*.30f))
        drawPath(Path().apply {
            moveTo(w*.39f,h); cubicTo(w*.41f,h*.87f,w*.54f,h*.86f,w*.53f,h*.72f)
            lineTo(w*.50f,h*.66f); lineTo(w*.61f,h*.66f); lineTo(w*.59f,h*.74f)
            cubicTo(w*.61f,h*.87f,w*.72f,h*.91f,w*.70f,h); close()
        }, Color(0xFF927D62).copy(alpha=.86f))
        fun pavilion(cx: Float, cy: Float, scale: Float, wall: Color, roof: Color, glow: Float) {
            val bw = w * .105f * scale; val bh = h * .145f * scale
            drawRoundRect(Color(0xFF171F29).copy(alpha=.45f), Offset(cx-bw*.60f,cy+bh*.76f), Size(bw*1.2f,bh*.18f), androidx.compose.ui.geometry.CornerRadius(10f))
            drawRect(wall, Offset(cx-bw/2,cy), Size(bw,bh))
            drawRect(Color(0xFF4C332B), Offset(cx-bw*.105f,cy+bh*.42f), Size(bw*.21f,bh*.58f))
            for (side in listOf(-1f,1f)) drawRoundRect(Color(0xFFFFD99A).copy(alpha=glow), Offset(cx+side*bw*.27f-bw*.07f,cy+bh*.28f), Size(bw*.14f,bh*.18f), androidx.compose.ui.geometry.CornerRadius(3f))
            val r = Path().apply {
                moveTo(cx-bw*.68f,cy+bh*.12f); lineTo(cx,cy-bh*.52f); lineTo(cx+bw*.68f,cy+bh*.12f)
                lineTo(cx+bw*.52f,cy+bh*.18f); lineTo(cx,cy-bh*.28f); lineTo(cx-bw*.52f,cy+bh*.18f); close()
            }
            drawPath(r, roof)
        }
        pavilion(w*.17f,h*.57f,1.08f,Color(0xFF9C8067),Color(0xFF754B45),lanternPulse)
        pavilion(w*.38f,h*.49f,1.35f,Color(0xFFC19A70),Color(0xFF9A6046),lanternPulse)
        pavilion(w*.82f,h*.57f,1.12f,Color(0xFF887A83),Color(0xFF554765),lanternPulse)
        pavilion(w*.96f,h*.66f,.72f,Color(0xFF8D795F),Color(0xFF655044),lanternPulse)
        drawRoundRect(Color(0xFF493E3A), Offset(w*.70f,h*.50f), Size(w*.10f,h*.23f), androidx.compose.ui.geometry.CornerRadius(7f))
        drawRoundRect(Color(0xFF17252B), Offset(w*.724f,h*.55f), Size(w*.052f,h*.18f), androidx.compose.ui.geometry.CornerRadius(22f))
        drawCircle(Color(0xFF82D7C0).copy(alpha=if(storyChapter>0) lanternPulse else .22f), w*.022f, p(.75f,.59f))
        fun pine(x: Float, y: Float, s: Float) {
            drawRect(Color(0xFF49392F), Offset(w*x-w*.004f,h*y), Size(w*.008f,h*.13f*s))
            for (j in 0..2) {
                val yy=h*y-h*.045f*s*j
                val path=Path().apply{moveTo(w*x,yy-h*.12f*s);lineTo(w*x-w*.055f*s,yy+h*.025f*s);lineTo(w*x+w*.055f*s,yy+h*.025f*s);close()}
                drawPath(path,if(j==0) Color(0xFF183C36) else Color(0xFF205044))
            }
        }
        pine(.035f,.72f,1.2f); pine(.11f,.79f,.8f); pine(.91f,.75f,1.15f); pine(.98f,.83f,.82f)
        for (i in 0..5) {
            val x=.08f+i*.16f; val y=if(i%2==0) .69f else .65f
            drawLine(Color(0xFF5B4939),p(x,y-.10f),p(x,y),2.5f)
            drawCircle(Color(0xFFFFD18B).copy(alpha=.12f*lanternPulse),w*.035f,p(x,y))
            drawRoundRect(Color(0xFFFFD18B).copy(alpha=lanternPulse),Offset(w*x-w*.006f,h*y),Size(w*.012f,h*.035f),androidx.compose.ui.geometry.CornerRadius(4f))
        }
        val hx=w*.48f; val hy=h*.79f
        drawCircle(Color(0xFFE9C9A7),w*.012f,Offset(hx,hy))
        drawPath(Path().apply{moveTo(hx-w*.018f,hy+h*.012f);lineTo(hx+w*.018f,hy+h*.012f);lineTo(hx+w*.029f,hy+h*.13f);lineTo(hx,hy+h*.17f);lineTo(hx-w*.03f,hy+h*.13f);close()},Color(0xFF4C8D87))
        drawLine(Color(0xFFD9C7A7),Offset(hx+w*.018f,hy+h*.04f),Offset(hx+w*.065f,hy-h*.04f),3.2f)
        drawLine(Color(0xFFC5D9DE),Offset(hx+w*.065f,hy-h*.04f),Offset(hx+w*.073f,hy-h*.075f),2f)
        drawRect(Color(0xFFB5D5CE).copy(alpha=mistDrift*.16f),Offset(w*cloudDrift,h*.60f),Size(w*.72f,h*.09f))
        drawRect(Color(0xFFB5D5CE).copy(alpha=mistDrift*.11f),Offset(w*(.25f-cloudDrift),h*.73f),Size(w*.68f,h*.07f))
        drawRect(brush=Brush.verticalGradient(0f to Color.Transparent,.72f to Color.Transparent,1f to Color(0xCC101820)))
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
        Text("ЛУННАЯ ГАВАНЬ  ·  \${worldName(stage).uppercase()}", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Text("СТУПЕНЬ $stage", color = Jade, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
@Composable
private fun AbodeScene(level: Int, world: String) {
    val transition = rememberInfiniteTransition(label = "abode-breath")
    val glow by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(tween(2200), repeatMode = RepeatMode.Reverse),
        label = "spring-glow"
    )
    Canvas(Modifier.fillMaxWidth().height(190.dp)) {
        val w = size.width
        val h = size.height
        drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF182B3A), Color(0xFF40564A), Color(0xFF24362F))))
        val ridge = Path().apply {
            moveTo(0f, h * 0.62f); lineTo(w * 0.18f, h * 0.22f); lineTo(w * 0.34f, h * 0.61f)
            lineTo(w * 0.55f, h * 0.18f); lineTo(w * 0.78f, h * 0.58f); lineTo(w, h * 0.28f)
            lineTo(w, h); lineTo(0f, h); close()
        }
        drawPath(ridge, Color(0xFF293E3B))
        drawRect(Color(0xFF526247), Offset(0f, h * 0.72f), Size(w, h * 0.28f))
        drawRoundRect(Color(0xFF9B7956), Offset(w * 0.12f, h * 0.48f), Size(w * 0.27f, h * 0.30f))
        val roof = Path().apply {
            moveTo(w * 0.08f, h * 0.51f); lineTo(w * 0.25f, h * 0.30f); lineTo(w * 0.43f, h * 0.51f); close()
        }
        drawPath(roof, Color(0xFF624638))
        drawRect(Color(0xFF4B332A), Offset(w * 0.22f, h * 0.61f), Size(w * 0.06f, h * 0.17f))
        drawRect(Color(0xFFFFD38B).copy(alpha = glow), Offset(w * 0.15f, h * 0.57f), Size(w * 0.045f, h * 0.055f))
        drawRoundRect(Color(0xFF18252B), Offset(w * 0.59f, h * 0.48f), Size(w * 0.23f, h * 0.29f))
        val cave = Path().apply {
            moveTo(w * 0.56f, h * 0.51f); lineTo(w * 0.70f, h * 0.30f); lineTo(w * 0.85f, h * 0.51f); close()
        }
        drawPath(cave, Color(0xFF46504B))
        drawCircle(Color(0xFF8ED9B2).copy(alpha = glow), 12f, Offset(w * 0.70f, h * 0.63f))
        drawCircle(Color(0xFF8ED9B2).copy(alpha = glow * 0.20f), 26f, Offset(w * 0.70f, h * 0.63f))
        for (i in 0..4) {
            val x = w * (0.43f + i * 0.045f)
            drawLine(Color(0xFF8CA36A), Offset(x, h * 0.78f), Offset(x - 3f, h * 0.68f), 3f)
            drawCircle(Color(0xFF9FC47A), 4f, Offset(x - 3f, h * 0.68f))
        }
        drawLine(Color(0xFF91B8A4).copy(alpha = glow), Offset(w * 0.03f, h * 0.89f), Offset(w * 0.97f, h * 0.89f), 3f)
    }
    Text("Убежище Смертного мира · уровень $level · $world", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun BattleScene(autoBattle: Boolean, actionText: String) {
    val transition=rememberInfiniteTransition(label="cinematic-battle")
    val pulse by transition.animateFloat(.25f,.95f,infiniteRepeatable(tween(850),RepeatMode.Reverse),label="core-pulse")
    val sweep by transition.animateFloat(-.12f,.12f,infiniteRepeatable(tween(if(autoBattle)520 else 1500),RepeatMode.Reverse),label="energy-sweep")
    val aura by transition.animateFloat(.7f,1.18f,infiniteRepeatable(tween(1100),RepeatMode.Reverse),label="aura")
    Canvas(Modifier.fillMaxWidth().height(252.dp)) {
        val w=size.width; val h=size.height
        drawRect(brush=Brush.verticalGradient(listOf(Color(0xFF11192B),Color(0xFF3B2943),Color(0xFF172B31))))
        drawCircle(Color(0xFFFFDCA8).copy(alpha=.7f),w*.055f,Offset(w*.82f,h*.18f))
        val ridge=Path().apply{moveTo(0f,h*.65f);lineTo(w*.17f,h*.34f);lineTo(w*.31f,h*.62f);lineTo(w*.55f,h*.28f);lineTo(w*.74f,h*.61f);lineTo(w*.91f,h*.37f);lineTo(w,h*.58f);lineTo(w,h);lineTo(0f,h);close()}
        drawPath(ridge,Color(0xFF202E3B))
        drawRect(Color(0xFF263B35),Offset(0f,h*.79f),Size(w,h*.21f))
        drawLine(Color(0xFFB08A60),Offset(0f,h*.79f),Offset(w,h*.79f),3f)
        drawCircle(Color(0xFF83D7C7).copy(alpha=.18f),w*.19f,Offset(w*.49f,h*.78f))
        drawCircle(Color(0xFF9ADFD1).copy(alpha=.48f),w*.14f,Offset(w*.49f,h*.78f),style=androidx.compose.ui.graphics.drawscope.Stroke(width=2f))
        for(i in 0..7){val angle=i*.785f;drawCircle(Color(0xFFE5BD72).copy(alpha=.72f),3f,Offset(w*.49f+kotlin.math.cos(angle)*w*.14f,h*.78f+kotlin.math.sin(angle)*w*.14f))}
        val hx=w*.23f; val hy=h*.43f
        drawCircle(Color(0xFFE8C4A0),w*.027f,Offset(hx,hy))
        drawCircle(Color(0xFF20202A),w*.03f,Offset(hx,hy-w*.012f))
        drawPath(Path().apply{moveTo(hx-w*.045f,hy+h*.04f);lineTo(hx+w*.045f,hy+h*.04f);lineTo(hx+w*.075f,h*.80f);lineTo(hx,h*.85f);lineTo(hx-w*.07f,h*.80f);close()},Color(0xFF397F7B))
        drawPath(Path().apply{moveTo(hx-w*.04f,hy+h*.05f);lineTo(hx-w*.11f,h*.65f);lineTo(hx-w*.08f,h*.68f);lineTo(hx+w*.005f,hy+h*.14f);close()},Color(0xFFB6D4CF))
        drawLine(Color(0xFFD8C7A6),Offset(hx+w*.03f,hy+h*.10f),Offset(hx+w*.16f,hy-h*.12f),5f)
        drawLine(Color(0xFFB9D8E0),Offset(hx+w*.16f,hy-h*.12f),Offset(hx+w*.19f,hy-h*.20f),3f)
        val ex=w*.78f; val ey=h*.43f
        drawCircle(Color(0xFFB98E7D),w*.045f,Offset(ex,ey))
        drawLine(Color(0xFF6E4A52),Offset(ex-w*.025f,ey-w*.025f),Offset(ex-w*.06f,ey-w*.09f),5f)
        drawLine(Color(0xFF6E4A52),Offset(ex+w*.025f,ey-w*.025f),Offset(ex+w*.06f,ey-w*.09f),5f)
        drawPath(Path().apply{moveTo(ex-w*.09f,ey+h*.06f);lineTo(ex+w*.09f,ey+h*.06f);lineTo(ex+w*.13f,h*.83f);lineTo(ex,h*.87f);lineTo(ex-w*.13f,h*.83f);close()},Color(0xFF754650))
        drawLine(Color(0xFFB66F67),Offset(ex-w*.07f,ey+h*.12f),Offset(ex-w*.17f,h*.70f),9f)
        val orb=Offset(w*(.38f+sweep),h*(.42f+sweep*.25f))
        drawCircle(Color(0xFF72E5D2).copy(alpha=.13f),w*.12f*aura,orb)
        drawCircle(Color(0xFF9BFFE9).copy(alpha=pulse),w*.035f,orb)
        drawLine(Color(0xFF8DF2D8).copy(alpha=pulse),Offset(hx+w*.17f,hy-h*.02f),orb,4f)
        for(i in 0..10){val x=orb.x+(i-5)*w*.018f;val y=orb.y+((i%3)-1)*h*.035f;drawCircle(Color(0xFFFFD58A).copy(alpha=pulse*.75f),2.4f,Offset(x,y))}
        drawRect(brush=Brush.verticalGradient(0f to Color.Transparent,.75f to Color.Transparent,1f to Color(0x99101820)))
    }
    Text(if(autoBattle)"БОЙ · ТЕХНИКИ И ЭФФЕКТЫ АКТИВНЫ" else actionText,color=if(autoBattle) Jade else Color.LightGray,fontSize=11.sp,fontWeight=FontWeight.Bold)
}
@Composable
private fun TownBuilding(name: String, description: String, symbol: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier.heightIn(min = 104.dp).clickable(onClick = onClick), color = Color(0xFF1B3035), shape = MaterialTheme.shapes.large, border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.42f)), shadowElevation = 7.dp) {
        Box {
            Canvas(Modifier.matchParentSize()) {
                drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF31504A).copy(alpha=.55f), Color(0xFF17262D))))
                drawCircle(Color(0xFFE5BD72).copy(alpha=.08f), size.minDimension*.48f, Offset(size.width*.5f,size.height*.18f))
            }
            Column(Modifier.fillMaxWidth().padding(horizontal=8.dp, vertical=9.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(symbol, fontSize = 31.sp)
                Text(name, color = Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(description, color = Color(0xFFB5C9C0), fontSize = 9.sp, lineHeight = 12.sp)
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
private data class ClassCombatProfile(val attack: Int, val defense: Int, val bonusHp: Int, val criticalChance: Int, val skillPower: Int, val healingBonus: Int)
private val classCombatProfiles = listOf(
    ClassCombatProfile(14, 6, 45, 12, 2, 0),
    ClassCombatProfile(9, 15, 220, 3, 0, 0),
    ClassCombatProfile(18, 3, 20, 7, 16, 0),
    ClassCombatProfile(10, 8, 95, 5, 5, 28)
)
private val classNames=listOf("Мечник","Закалённый воин","Заклинатель стихий","Мистик духов")
private val classDescriptions=listOf("Физический универсал: точность, парирование и контратаки.","Мастер закалки тела: стойкость, мощные удары и сопротивление урону.","Маг разрушения: огонь, лёд и молнии, высокий урон и контроль.","Маг поддержки: духовные печати, ослабления и восстановление.")
private val classStats=listOf("Атака 8 · Защита 8 · Контроль 6","Атака 9 · Защита 10 · Контроль 4","Атака 10 · Защита 5 · Контроль 9","Атака 7 · Защита 8 · Контроль 10")
private val classBaseAttack=listOf(9,8,11,7)
private val activeSkillDamage=listOf(34,34,38,34)
private val weaponNames=listOf("Меч","Копьё","Посох","Боевые перчатки")
private val weaponAttackBonus=listOf(8,10,7,9)
private val weaponNodeOneBonus=listOf(3,5,2,4)
private val weaponNodeTwoBonus=listOf(4,2,5,3)

private enum class WeaponRarity(val title: String, val realmGapShare: Double, val rollSpread: Double) {
    COMMON("Обычное", 0.00, 0.10),
    UNCOMMON("Необычное", 0.16, 0.12),
    RARE("Редкое", 0.34, 0.15),
    EPIC("Эпическое", 0.58, 0.18),
    LEGENDARY("Легендарное", 0.88, 0.23),
    MYTHIC("Мифическое", 1.08, 0.28)
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
    val effectDescription: String,
    val hasActiveAbility: Boolean = rarity == WeaponRarity.EPIC || rarity == WeaponRarity.LEGENDARY || rarity == WeaponRarity.MYTHIC
)

private fun weapon(
    id: String, name: String, type: Int, rarity: WeaponRarity, requiredStage: Int,
    realmBase: Int, defense: Int = 0, extraDamage: Int = 0, healing: Int = 0, effect: String
): WeaponDefinition {
    val nextRealmBase = when (requiredStage) { 1 -> 36; 4 -> 100; 7 -> 230; 10 -> 520; 13 -> 1150; else -> 2600 }
    val rarityPower = ((nextRealmBase - realmBase).coerceAtLeast(0) * rarity.realmGapShare).toInt()
    return WeaponDefinition(id, name, type, rarity, requiredStage, realmBase + rarityPower, defense, extraDamage, healing, effect)
}

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

private enum class GearSlot(val title: String) {
    HELMET("Шлем"), ARMOR("Броня"), RING("Кольцо"), ARTIFACT("Артефакт")
}

private data class GearDefinition(
    val id: String,
    val name: String,
    val slot: GearSlot,
    val rarity: WeaponRarity,
    val requiredStage: Int,
    val attackBonus: Int,
    val defenseBonus: Int,
    val hpBonus: Int,
    val skillBonus: Int,
    val setId: String,
    val effectDescription: String
)

private data class GearSetBonus(
    val attack: Int = 0,
    val defense: Int = 0,
    val hp: Int = 0,
    val skill: Int = 0,
    val description: String = ""
)

private val gearSetNames = mapOf(
    "moon_guard" to "Страж Луны",
    "jade_path" to "Нефритовый путь",
    "sky_oath" to "Клятва Небес",
    "immortal_soul" to "Душа Бессмертного",
    "void_walker" to "Странник Пустоты",
    "chaos_origin" to "Первородный Хаос"
)

private val gearCatalog = listOf(
    GearDefinition("moon_helm", "Шлем ночного дозора", GearSlot.HELMET, WeaponRarity.COMMON, 1, 1, 2, 18, 0, "moon_guard", "Укрепляет голову и разум"),
    GearDefinition("moon_armor", "Доспех лунного стража", GearSlot.ARMOR, WeaponRarity.UNCOMMON, 1, 1, 4, 28, 0, "moon_guard", "Лёгкая защита начинающего культиватора"),
    GearDefinition("moon_ring", "Кольцо тихой гавани", GearSlot.RING, WeaponRarity.RARE, 1, 2, 1, 12, 2, "moon_guard", "Стабилизирует поток Ци"),
    GearDefinition("moon_charm", "Талисман лунного света", GearSlot.ARTIFACT, WeaponRarity.EPIC, 1, 3, 2, 20, 3, "moon_guard", "Сохраняет часть духовной энергии"),
    GearDefinition("jade_helm", "Нефритовый венец", GearSlot.HELMET, WeaponRarity.UNCOMMON, 4, 3, 4, 42, 2, "jade_path", "Усиливает концентрацию"),
    GearDefinition("jade_armor", "Мантия нефритовых жил", GearSlot.ARMOR, WeaponRarity.RARE, 4, 2, 7, 58, 2, "jade_path", "Защищает меридианы"),
    GearDefinition("jade_ring", "Перстень зелёного пламени", GearSlot.RING, WeaponRarity.EPIC, 4, 5, 3, 34, 5, "jade_path", "Преобразует Ци в силу"),
    GearDefinition("jade_artifact", "Печать нефритового дракона", GearSlot.ARTIFACT, WeaponRarity.LEGENDARY, 4, 7, 5, 48, 7, "jade_path", "Пробуждает драконью энергию"),
    GearDefinition("sky_helm", "Корона небесного грома", GearSlot.HELMET, WeaponRarity.RARE, 7, 6, 7, 78, 4, "sky_oath", "Удерживает грозовую Ци"),
    GearDefinition("sky_armor", "Броня семи облаков", GearSlot.ARMOR, WeaponRarity.EPIC, 7, 5, 11, 110, 4, "sky_oath", "Рассекает встречные потоки"),
    GearDefinition("sky_ring", "Кольцо громового сердца", GearSlot.RING, WeaponRarity.LEGENDARY, 7, 9, 5, 66, 8, "sky_oath", "Ускоряет боевые техники"),
    GearDefinition("sky_artifact", "Зеркало небесного суда", GearSlot.ARTIFACT, WeaponRarity.MYTHIC, 7, 12, 8, 92, 10, "sky_oath", "Отражает часть духовного урона"),
    GearDefinition("soul_helm", "Венец бессмертной души", GearSlot.HELMET, WeaponRarity.EPIC, 10, 10, 12, 135, 7, "immortal_soul", "Защищает душу от распада"),
    GearDefinition("soul_armor", "Панцирь вечного пути", GearSlot.ARMOR, WeaponRarity.LEGENDARY, 10, 8, 17, 180, 8, "immortal_soul", "Сковывает тело истинной Ци"),
    GearDefinition("soul_ring", "Кольцо вечного источника", GearSlot.RING, WeaponRarity.MYTHIC, 10, 15, 10, 110, 13, "immortal_soul", "Поддерживает поток бессмертия"),
    GearDefinition("soul_artifact", "Сердце бессмертного дерева", GearSlot.ARTIFACT, WeaponRarity.MYTHIC, 10, 18, 12, 155, 16, "immortal_soul", "Возвращает жизненную энергию"),
    GearDefinition("void_helm", "Шлем беззвёздной пустоты", GearSlot.HELMET, WeaponRarity.LEGENDARY, 13, 16, 19, 205, 12, "void_walker", "Скрывает присутствие в пустоте"),
    GearDefinition("void_armor", "Доспех пограничного мира", GearSlot.ARMOR, WeaponRarity.MYTHIC, 13, 14, 25, 270, 14, "void_walker", "Сдерживает разрывы пространства"),
    GearDefinition("void_ring", "Кольцо последней грани", GearSlot.RING, WeaponRarity.MYTHIC, 13, 23, 15, 170, 20, "void_walker", "Усиливает волю культиватора"),
    GearDefinition("void_artifact", "Осколок вечной пустоты", GearSlot.ARTIFACT, WeaponRarity.MYTHIC, 13, 27, 18, 230, 24, "void_walker", "Поглощает энергию противника"),
    GearDefinition("chaos_helm", "Диадема первичного хаоса", GearSlot.HELMET, WeaponRarity.MYTHIC, 16, 24, 28, 300, 18, "chaos_origin", "Удерживает хаотическое сознание"),
    GearDefinition("chaos_armor", "Облачение прародителя", GearSlot.ARMOR, WeaponRarity.MYTHIC, 16, 22, 36, 390, 20, "chaos_origin", "Сшито из материи первозданного мира"),
    GearDefinition("chaos_ring", "Перстень нулевого Дао", GearSlot.RING, WeaponRarity.MYTHIC, 16, 34, 22, 245, 29, "chaos_origin", "Подчиняет потоки первоосновы"),
    GearDefinition("chaos_artifact", "Сердце начала и конца", GearSlot.ARTIFACT, WeaponRarity.MYTHIC, 16, 40, 27, 330, 34, "chaos_origin", "Содержит искру первого мира")
)

private fun encodeGearInstance(item: GearDefinition): String =
    listOf(item.id, item.attackBonus, item.defenseBonus, item.hpBonus, item.skillBonus).joinToString("~")

private fun resolveGearInstance(encoded: String): GearDefinition? {
    val parts = encoded.split("~")
    val base = gearCatalog.firstOrNull { it.id == parts.firstOrNull() } ?: return null
    if (parts.size < 5) return base
    return base.copy(
        attackBonus = parts[1].toIntOrNull() ?: base.attackBonus,
        defenseBonus = parts[2].toIntOrNull() ?: base.defenseBonus,
        hpBonus = parts[3].toIntOrNull() ?: base.hpBonus,
        skillBonus = parts[4].toIntOrNull() ?: base.skillBonus
    )
}

private fun gearPowerScore(item: GearDefinition): Int =
    item.attackBonus + item.defenseBonus * 2 + item.hpBonus / 20 + item.skillBonus * 2

private fun calculateGearSetBonus(items: List<GearDefinition>): GearSetBonus {
    var attack = 0
    var defense = 0
    var hp = 0
    var skill = 0
    val active = mutableListOf<String>()
    items.groupingBy { it.setId }.eachCount().forEach { (setId, count) ->
        val name = gearSetNames[setId] ?: setId
        if (count >= 2) {
            attack += 8
            defense += 8
            hp += 80
            active.add("$name · 2/4")
        }
        if (count >= 4) {
            attack += 12
            defense += 12
            hp += 120
            skill += 8
            active.remove("$name · 2/4")
            active.add("$name · полный комплект")
        }
    }
    val description = if (active.isEmpty()) "" else "Бонус комплекта: " + active.joinToString(" · ") +
        " · атака +$attack · защита +$defense · здоровье +$hp" + if (skill > 0) " · сила техник +$skill" else ""
    return GearSetBonus(attack, defense, hp, skill, description)
}

private fun rollGearDrop(stage: Int): GearDefinition? {
    val available = gearCatalog.filter { it.requiredStage <= stage }
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
    val base = available.filter { it.rarity == rarity }.ifEmpty { available }.random()
    val spread = base.rarity.rollSpread
    return base.copy(
        attackBonus = (base.attackBonus * Random.nextDouble(1.0 - spread, 1.0 + spread)).toInt().coerceAtLeast(0),
        defenseBonus = (base.defenseBonus * Random.nextDouble(1.0 - spread, 1.0 + spread)).toInt().coerceAtLeast(0),
        hpBonus = (base.hpBonus * Random.nextDouble(1.0 - spread, 1.0 + spread)).toInt().coerceAtLeast(1),
        skillBonus = (base.skillBonus * Random.nextDouble(1.0 - spread, 1.0 + spread)).toInt().coerceAtLeast(0)
    )
}

private fun weaponRarityColor(rarity: WeaponRarity): Color = when (rarity) {
    WeaponRarity.COMMON -> Color(0xFFCFD4D8)
    WeaponRarity.UNCOMMON -> Color(0xFF78C6A3)
    WeaponRarity.RARE -> Color(0xFF68A9F5)
    WeaponRarity.EPIC -> Color(0xFFB58BE8)
    WeaponRarity.LEGENDARY -> Color(0xFFFFB65C)
    WeaponRarity.MYTHIC -> Color(0xFFFF718B)
}

private fun encodeWeaponInstance(item: WeaponDefinition): String =
    listOf(item.id, item.attackBonus, item.defenseBonus, item.bonusDamage, item.healOnHit).joinToString("~")

private fun resolveWeaponInstance(encoded: String): WeaponDefinition? {
    val parts = encoded.split("~")
    val base = weaponCatalog.firstOrNull { it.id == parts.firstOrNull() } ?: return null
    if (parts.size < 5) return base
    return base.copy(
        attackBonus = parts[1].toIntOrNull() ?: base.attackBonus,
        defenseBonus = parts[2].toIntOrNull() ?: base.defenseBonus,
        bonusDamage = parts[3].toIntOrNull() ?: base.bonusDamage,
        healOnHit = parts[4].toIntOrNull() ?: base.healOnHit
    )
}

private fun weaponPowerScore(item: WeaponDefinition): Int =
    item.attackBonus + item.bonusDamage + item.defenseBonus * 2 + item.healOnHit * 2

private fun rollWeaponDrop(stage: Int, ownedIds: List<String>): WeaponDefinition? {
    // Повторные экземпляры разрешены: один и тот же клинок может выпасть с иным набором характеристик.
    val available = weaponCatalog.filter { it.requiredStage <= stage }
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
    val base = (sameRarity.ifEmpty { available }).random()
    val spread = base.rarity.rollSpread
    return base.copy(
        attackBonus = (base.attackBonus * Random.nextDouble(1.0 - spread, 1.0 + spread)).toInt().coerceAtLeast(1),
        defenseBonus = (base.defenseBonus * Random.nextDouble(0.85, 1.25)).toInt().coerceAtLeast(0),
        bonusDamage = (base.bonusDamage * Random.nextDouble(0.85, 1.35)).toInt().coerceAtLeast(0),
        healOnHit = (base.healOnHit * Random.nextDouble(0.85, 1.30)).toInt().coerceAtLeast(0)
    )
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
private fun skillNodeUnlocked(ci:Int,node:Int,levels:List<Int>,characterLevel:Int=20):Boolean {
 val b=ci*9
 val requiredLevel = when(node) { 0 -> 1; 1,4 -> 3; 2,5 -> 6; 3,6 -> 9; 7 -> 13; 8 -> 18; else -> 1 }
 val branchReady = when(node){0->true;1,4->levels[b]==1;2->levels[b+1]==1;3->levels[b+2]==1;5->levels[b+4]==1;6->levels[b+5]==1;7->levels[b+3]==1&&levels[b+6]==1;8->levels[b+7]==1;else->false}
 return branchReady && characterLevel >= requiredLevel
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
private fun SkillTreeSection(title:String,classIndex:Int,nodes:List<Int>,levels:List<Int>,points:Int,weapon:Int,activeNode:Int,characterLevel:Int,onLearn:(Int)->Unit,onSetActive:(Int)->Unit){
 if(title.isNotBlank()) Text(title,color=Gold,fontWeight=FontWeight.Bold)
 nodes.forEach { node ->
  val ix=classIndex*9+node
  val learned=levels[ix]==1
  val requiredLevel = when(node) { 0 -> 1; 1,4 -> 3; 2,5 -> 6; 3,6 -> 9; 7 -> 13; 8 -> 18; else -> 1 }
  val unlocked = skillNodeUnlocked(classIndex,node,levels,characterLevel)
  val def=skillDefinitions[classIndex][node]
  val desc=if(node==1||node==4) weaponSkillDescription(classIndex,node,weapon) else def.description
  Surface(color=if(learned) Color(0xFF29463F) else Color(0xFF24333B),shape=MaterialTheme.shapes.large){
   Column(Modifier.fillMaxWidth().padding(8.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
    Text((node+1).toString()+". "+def.name,color=Gold,fontWeight=FontWeight.Bold,fontSize=12.sp)
    Text(if(def.active)"АКТИВНЫЙ НАВЫК" else "ПАССИВНЫЙ НАВЫК",color=Jade,fontSize=10.sp)
    Text(desc,color=Color.LightGray,fontSize=11.sp)
    if(!learned) Text("Требуемый уровень персонажа: $requiredLevel" + if(!unlocked && characterLevel < requiredLevel) " · сейчас $characterLevel" else "",color=Color(0xFFFFC978),fontSize=10.sp)
    if(!learned) Button(onClick={onLearn(node)},enabled=unlocked&&points>0,modifier=Modifier.fillMaxWidth()){Text(if(characterLevel < requiredLevel) "Откроется на уровне $requiredLevel" else if(!unlocked) "Сначала изучи связанные навыки" else "Изучить · 1 очко")}
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

private val skillMaterialNames = listOf("Осколок духа", "Нефритовая эссенция", "Сердце зверя", "Кристалл души", "Печать бессмертия", "Ядро хаоса")
private fun techniqueUpgradeCost(level: Int): Int = if (level >= 20) 0 else level * 100

private fun techniqueName(index: Int): String = when (index) {
    0 -> "Меч рассекающей Ци"
    1 -> "Нефритовый лотос"
    else -> "Коготь лиса"
}

private fun bossMaxHp(level: Int): Int = 100 + (level - 1) * 35

private fun abodeMaxLevel(stage: Int): Int = when {
    stage >= 19 -> 20
    stage >= 13 -> 15
    stage >= 7 -> 10
    else -> 5
}

private fun alchemyMaxRank(stage: Int): Int = abodeMaxLevel(stage)

private fun petCombatTrait(petId: String): String = when (petId) {
    "jade_fox" -> "техники получают дополнительную силу"
    "stone_tortoise" -> "получаемый урон снижается на 20%"
    "thunder_hawk" -> "повышенный шанс критического удара"
    "ember_wolf" -> "добивающий урон +14% по ослабленному противнику"
    "moon_deer" -> "восстанавливает здоровье каждый ход"
    "iron_ape" -> "сбалансированная атака и стойкость"
    "river_koi" -> "усиленное восстановление и духовные техники"
    else -> "неизвестная особенность"
}

private fun petClassSynergy(petId: String, classIndex: Int): Boolean = when (petId) {
    "jade_fox" -> classIndex == 0 || classIndex == 3
    "stone_tortoise" -> classIndex == 1
    "thunder_hawk" -> classIndex == 0 || classIndex == 2
    "ember_wolf" -> classIndex == 0
    "moon_deer" -> classIndex == 1 || classIndex == 3
    "iron_ape" -> classIndex == 1
    "river_koi" -> classIndex == 2 || classIndex == 3
    else -> false
}

private fun formatRemainingTime(milliseconds: Long): String {
    val totalMinutes = (milliseconds.coerceAtLeast(0L) + 59_999L) / 60_000L
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return if (hours > 0) "${hours} ч. ${minutes} мин." else "${minutes} мин."
}

private fun qiIntervalForAbode(level: Int): Long = (QI_INTERVAL_SECONDS - (level - 1) * 30L).coerceAtLeast(300L)
private fun cultivationQiCost(stage: Int): Int = when (stage) {
    1 -> 180
    2 -> 240
    3 -> 300
    4 -> 380
    5 -> 470
    6 -> 570
    else -> 650 + (stage - 7).coerceAtLeast(0) * 160
}

private val clanNames = listOf("Клан Железной Горы", "Дом Красного Клёна", "Союз Травников", "Клан Тихой Реки", "Дом Пепельного Волка", "Стражи Перевала", "Торговый союз Нефритовой монеты", "Род Лунной Гавани")
private val clanLore = listOf("Кузнецы и бойцы передней линии. Учат стойкости и тяжёлым ударам.", "Воители с сильными клинками и честью рода. Предпочитают прямой бой.", "Знатоки трав и настоек. Помогают выживать и готовить пилюли.", "Спокойные защитники, мастера обороны и восстановления.", "Охотники и следопыты. Сильны в преследовании и критических атаках.", "Опытные стражи дорог. Снижают опасность ответных ударов.", "Торговцы и собиратели. Ценят ресурсы и ремесло.", "Старый род долины. Сбалансированная подготовка новичков.")
private val clanAttackBonuses = listOf(2, 4, 1, 1, 5, 2, 1, 2)
private val clanDefenseBonuses = listOf(5, 2, 1, 4, 1, 5, 1, 2)
private val clanHpBonuses = listOf(25, 10, 20, 35, 5, 30, 15, 20)
private val factionAlliance = listOf(0, 1, 0, 0, 1, 0, 1, 0)

private data class PetDefinition(
    val id: String, val name: String, val symbol: String, val role: String,
    val description: String, val strength: String, val weakness: String,
    val bestClass: String, val source: String, val dropHint: String, val requiredBoss: Int,
    val attack: Int, val defense: Int, val hp: Int, val skill: Int, val critical: Int, val healing: Int
)
private val petCatalog = listOf(
    PetDefinition("jade_fox", "Нефритовый лис", "🦊", "хитрый дуэлянт", "Усиливает техники и помогает наносить стабильный урон.", "техники и гибкость", "мало здоровья", "Мистик духов / Мечник", "начальный спутник", "выдаётся в начале пути", 1, 5, 1, 20, 5, 2, 1),
    PetDefinition("stone_tortoise", "Каменная черепаха", "🐢", "страж", "Принимает часть натиска врага и укрепляет владельца.", "защита и здоровье", "низкий урон", "Закалённый воин", "стражи горного перевала", "добыча со стражей ранга 2+", 2, 1, 9, 90, 0, 0, 0),
    PetDefinition("thunder_hawk", "Громовой ястреб", "🦅", "скоростной атакующий", "Бьёт стремительно и чаще наносит критические удары.", "атака и крит", "слабая защита", "Заклинатель стихий / Мечник", "грозовые стражи", "добыча со стражей ранга 4+", 4, 10, 0, 12, 3, 7, 0),
    PetDefinition("ember_wolf", "Пепельный волк", "🐺", "охотник", "Разрывает цель сериями атак, но почти не помогает в обороне.", "сильный урон", "низкая выживаемость", "Мечник", "элитные стражи", "добыча со стражей ранга 6+", 6, 13, 1, 5, 2, 4, 0),
    PetDefinition("moon_deer", "Лунный олень", "🦌", "целитель", "Возвращает владельцу здоровье и поддерживает долгие сражения.", "лечение и здоровье", "низкий урон", "Мистик духов / Закалённый воин", "редкая находка в саду духовных трав", "шанс 3% при сборе урожая", 99, 1, 4, 65, 2, 0, 8),
    PetDefinition("iron_ape", "Железная обезьяна", "🐒", "боец передней линии", "Сочетает мощные удары с крепкой защитой.", "баланс атаки и защиты", "нет выраженного уклона в магию", "Закалённый воин", "стражи древней заставы", "добыча со стражей ранга 8+", 8, 8, 6, 45, 1, 1, 0),
    PetDefinition("river_koi", "Карп тихой реки", "🐟", "духовный помощник", "Усиливает духовные техники и помогает восстанавливать силы.", "техника и лечение", "низкая физическая атака", "Мистик духов / Заклинатель стихий", "редкая встреча в роднике", "шанс 3% при сборе духовной воды", 99, 1, 3, 38, 8, 0, 5)
)

private val worldEntries = listOf(
    Triple("Смертный мир", 1, "Мир простых людей, горных долин и первых искателей Дао. Здесь всё начинается с тела, дыхания и крупицы Ци."),
    Triple("Духовный мир", 7, "Земли, где духовная энергия наполняет реки и леса, а секты хранят искусства древних мастеров."),
    Triple("Бессмертный мир", 13, "Обители бессмертных, небесные города и испытания, меняющие саму природу души."),
    Triple("Мир Хаоса", 19, "Первозданная область за пределами привычных законов. Даже само пространство здесь подчиняется Дао.")
)
private fun worldName(stage: Int): String = when {
    stage >= 19 -> "Мир Хаоса"
    stage >= 13 -> "Бессмертный мир"
    stage >= 7 -> "Духовный мир"
    else -> "Смертный мир"
}

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
