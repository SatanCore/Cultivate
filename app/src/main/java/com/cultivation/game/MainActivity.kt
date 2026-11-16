package com.cultivation.game

import android.os.Bundle
import android.os.Build
import android.view.View
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
        // Keep Android system bars visible for a reliable first launch across devices.
        setContent { CultivateApp() }
    }
}

@Composable
private fun CharacterCreationScreen(
    selectedClass: Int,
    onClassSelected: (Int) -> Unit,
    gender: String,
    onGenderSelected: (String) -> Unit,
    nickname: String,
    onNicknameChanged: (String) -> Unit,
    onConfirm: () -> Unit
) {
    val validNickname = nickname.trim().length in 3..16 &&
        nickname.trim().all { it.isLetterOrDigit() || it == '_' }
    val portraits = listOf(
        if (gender == "female") R.drawable.blademaster_female else R.drawable.blademaster_male,
        if (gender == "female") R.drawable.ironbody_female else R.drawable.ironbody_male,
        if (gender == "female") R.drawable.elementalist_female else R.drawable.elementalist_male,
        if (gender == "female") R.drawable.spiritmystic_female else R.drawable.spiritmystic_male
    )
    BoxWithConstraints(
        Modifier.fillMaxSize().background(Color(0xFF081426))
    ) {
        Image(
            painter = painterResource(R.drawable.world_mortal_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xDD07111F), Color(0xEE0B1728), Color(0xF5081220)))))
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("СОЗДАНИЕ ПЕРВОГО ГЕРОЯ", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Text("Выбери свой путь", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
            Text("Этот выбор определит твоё путешествие по мирам Дао.", color = Color(0xFFC4D1DF), fontSize = 11.sp)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xDD14243A),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, Color(0x887F9BB8))
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("01 · КЛАСС", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                    for (row in 0..1) {
                        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            for (column in 0..1) {
                                val index = row * 2 + column
                                val selected = selectedClass == index
                                Surface(
                                    modifier = Modifier.weight(1f).clickable { onClassSelected(index) },
                                    color = if (selected) Color(0xFF254B4A) else Color(0xFF17283E),
                                    shape = RoundedCornerShape(15.dp),
                                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Jade else Color(0x667F9BB8))
                                ) {
                                    Column(
                                        Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(portraits[index]),
                                            contentDescription = classNames[index],
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxWidth().height(142.dp).graphicsLayer(shape = RoundedCornerShape(10.dp), clip = true)
                                        )
                                        Text(classNames[index], color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(classDescriptions[index], color = Color(0xFFB9C9D8), fontSize = 8.sp, minLines = 2)
                                        Text(if (selected) "ВЫБРАНО" else "ВЫБРАТЬ", color = if (selected) Jade else Gold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xDD14243A),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, Color(0x887F9BB8))
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("02 · ОБЛИК", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        listOf("male" to "МУЖСКОЙ", "female" to "ЖЕНСКИЙ").forEach { (value, label) ->
                            val selected = gender == value
                            Surface(
                                modifier = Modifier.weight(1f).clickable { onGenderSelected(value) },
                                color = if (selected) Color(0xFF254B4A) else Color(0xFF17283E),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Jade else Color(0x667F9BB8))
                            ) {
                                Text(label, Modifier.padding(vertical = 14.dp), color = if (selected) Jade else Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    Text("03 · ИМЯ ПЕРСОНАЖА", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { value -> onNicknameChanged(value.take(16).filter { it.isLetterOrDigit() || it == '_' }) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Твой никнейм") },
                        placeholder = { Text("Например, Нефритовый_Лис") },
                        supportingText = { Text("От 3 до 16 символов: буквы, цифры и _") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Jade,
                            unfocusedBorderColor = Color(0x667F9BB8),
                            focusedLabelColor = Jade,
                            cursorColor = Jade
                        )
                    )
                    Button(
                        onClick = onConfirm,
                        enabled = validNickname,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF806039), contentColor = Color.White, disabledContainerColor = Color(0xFF394454))
                    ) {
                        Text("ЗАКРЕПИТЬ ГЕРОЯ И НАЧАТЬ ПУТЬ", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
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
    var todayKey by remember { mutableLongStateOf(System.currentTimeMillis() / 86_400_000L) }
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
    var selectedWorldIndex by remember { mutableIntStateOf(prefs.getInt("selected_world_index", worldIndexForStage(stage)).coerceIn(0, 3)) }
    var explorationEnergy by remember { mutableIntStateOf(if (dailyReset) 6 else prefs.getInt("world_exploration_energy", 6).coerceIn(0, 6)) }
    val explorationProgress = remember {
        mutableStateListOf<Int>().apply {
            val saved = (prefs.getString("world_exploration_progress", "") ?: "")
                .split(",").mapNotNull { it.toIntOrNull() }
            repeat(worldLocationCatalog.size) { add(saved.getOrElse(it) { 0 }.coerceIn(0, 3)) }
        }
    }
    val worldFragments = remember {
        mutableStateListOf<Int>().apply {
            val saved = (prefs.getString("world_fragments", "0,0,0,0") ?: "0,0,0,0")
                .split(",").mapNotNull { it.toIntOrNull() }
            repeat(4) { add(saved.getOrElse(it) { 0 }.coerceAtLeast(0)) }
        }
    }
    var defeatedWorldBossIds by remember { mutableStateOf(prefs.getString("world_bosses_defeated", "") ?: "") }
    var pendingWorldBossId by remember { mutableStateOf(prefs.getString("pending_world_boss", "") ?: "") }
    var pendingWorldEncounterId by remember { mutableStateOf(prefs.getString("pending_world_encounter", "") ?: "") }
    var worldBossTurnCount by remember { mutableIntStateOf(prefs.getInt("world_boss_turn_count", 0).coerceAtLeast(0)) }
    var guardPrepared by remember { mutableStateOf(prefs.getBoolean("battle_guard_prepared", false)) }
    var lootRevealVisible by remember { mutableStateOf(prefs.getBoolean("battle_loot_visible", false)) }
    var lastLootPayload by remember { mutableStateOf(prefs.getString("battle_loot_payload", "") ?: "") }
    var selectedLocationId by remember { mutableStateOf(prefs.getString("world_selected_location", "mortal_valley") ?: "mortal_valley") }
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
    var playerNickname by remember { mutableStateOf(prefs.getString("player_nickname", "") ?: "") }
    var playerGender by remember { mutableStateOf(prefs.getString("player_gender", "male") ?: "male") }
    var creationClass by remember { mutableIntStateOf(prefs.getInt("creation_class_draft", 0).coerceIn(0, 3)) }
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
    var showExitDialog by remember { mutableStateOf(false) }
    var showMoreNavigation by remember { mutableStateOf(false) }
    var inventoryFilter by remember { mutableIntStateOf(0) }
    var petFilter by remember { mutableIntStateOf(0) }
    var selectedPetDetailIndex by remember { mutableIntStateOf(-1) }
    var showSkillNodeDialog by remember { mutableStateOf(false) }
    var selectedTownBuilding by remember { mutableStateOf("") }
    val pageScrollState = rememberScrollState()
    val pageEnterTransition = remember { Animatable(1f) }
    LaunchedEffect(selectedMenu, selectedTownBuilding) {
        pageScrollState.animateScrollTo(0)
        pageEnterTransition.snapTo(0.965f)
        pageEnterTransition.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 260,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
        )
    }
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
    var inspectedSkillNode by remember { mutableIntStateOf(0) }
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

    val activeWorldBoss = worldLocationCatalog.firstOrNull { it.id == pendingWorldBossId && it.isBoss }
    val activeWorldEncounter = worldLocationCatalog.firstOrNull { it.id == pendingWorldEncounterId && !it.isBoss }
    val activeWorldOpponent = activeWorldBoss ?: activeWorldEncounter
    val enemyMaxHp = activeWorldEncounter?.let { worldEncounterMaxHp(it) } ?: bossMaxHp(bossLevel)
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

    fun performCombatTurn(requestedAction: Int? = null) {
        if (enemyHp <= 0 || playerHp <= 0) return
        val nextSkillCooldowns = skillCooldowns.map { (it - 1).coerceAtLeast(0) }
        val techniqueCd = (techniqueCooldown - 1).coerceAtLeast(0)
        val weaponCd = (weaponCooldown - 1).coerceAtLeast(0)
        val selectedManualAction = requestedAction?.takeIf { candidate ->
            chosenClass in 0..3 && candidate in 0..8 &&
                skillTreeLevels[chosenClass * 9 + candidate] == 1 &&
                skillDefinitions[chosenClass][candidate].active &&
                nextSkillCooldowns[candidate] == 0
        }
        if (requestedAction != null && selectedManualAction == null) return
        val action = selectedManualAction ?: if (requestedAction == null) rotationOrder.firstOrNull { candidate ->
            when {
                candidate in 0..8 -> chosenClass >= 0 &&
                    skillTreeLevels[chosenClass * 9 + candidate] == 1 &&
                    skillDefinitions[chosenClass][candidate].active &&
                    nextSkillCooldowns[candidate] == 0
                candidate == 9 -> techniqueCd == 0
                candidate == 10 -> weaponCd == 0 && equippedWeapon.hasActiveAbility
                else -> false
            }
        } else null
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
        var worldBossAbilityMessage = ""
        if (enemyHp > 0) {
            val nextWorldBossTurn = if (activeWorldOpponent != null) worldBossTurnCount + 1 else 0
            val specialStrike = activeWorldBoss != null && nextWorldBossTurn % 3 == 0
            val eliteStrike = activeWorldEncounter?.isElite == true && nextWorldBossTurn % 3 == 0
            val specialBonus = when (activeWorldBoss?.worldIndex) {
                0 -> 6 + bossLevel
                1 -> 8 + bossLevel
                2 -> 10 + bossLevel * 2
                3 -> 14 + bossLevel * 2
                else -> 0
            }
            val abilityName = activeWorldBoss?.let { worldBossAbilityName(it.worldIndex) }.orEmpty()
            val eliteBonus = if (eliteStrike) activeWorldEncounter?.enemyDamageBonus ?: 0 else 0
            val encounterBaseDamage = activeWorldEncounter?.let { 8 + it.recommendedPower / 90 + it.requiredStage * 2 } ?: (12 + bossLevel * 3)
            var incoming = (encounterBaseDamage + (if (specialStrike) specialBonus else 0) + eliteBonus - classDefense).coerceAtLeast(2)
            if (activePet.id == "stone_tortoise") incoming = (incoming * 80 / 100).coerceAtLeast(1)
            val usedGuard = guardPrepared
            if (usedGuard) {
                incoming = (incoming * 45 / 100).coerceAtLeast(1)
                guardPrepared = false
            } else if (action != null && action in 0..8) {
                incoming = when (chosenClass) { 0 -> incoming * 55 / 100; 1 -> incoming * 25 / 100; 2 -> 0; else -> incoming * 70 / 100 }
                if (chosenClass == 3) playerHp = (playerHp + 45).coerceAtMost(playerMaxHp)
            }
            playerHp = (playerHp - incoming).coerceAtLeast(0)
            if (activeWorldOpponent != null) worldBossTurnCount = nextWorldBossTurn
            if (usedGuard) worldBossAbilityMessage = "Защитная стойка смягчила ответный удар."
            if (specialStrike) worldBossAbilityMessage = listOf(worldBossAbilityMessage, "$abilityName · дополнительный урон +$specialBonus.").filter { it.isNotBlank() }.joinToString(" ")
            if (eliteStrike) worldBossAbilityMessage = listOf(worldBossAbilityMessage, "${activeWorldEncounter?.eliteAbilityName ?: "Элитный натиск"} · дополнительный урон +$eliteBonus.").filter { it.isNotBlank() }.joinToString(" ")
        }
        if (enemyHp == 0) {
            autoBattle = false
            guardPrepared = false
            val encounterRank = activeWorldEncounter?.requiredStage ?: bossLevel
            val reward = if (activeWorldEncounter != null) 20 + encounterRank * 7 else 80 + bossLevel * 15 + stage * 5
            stones += reward
            wins++
            dailyWins++
            qi = (qi + 5).coerceAtMost(9999)
            petBondXp[activePetIndex] = petBondXp[activePetIndex] + 15
            val essenceFound = Random.nextInt(100) < 25
            if (essenceFound) petEssence++
            val petCoreFound = Random.nextInt(100) < 4
            if (petCoreFound) petCores++
            skillPoints++
            val materialTier = ((encounterRank - 1) / 5).coerceIn(0, 5)
            skillMaterials[materialTier] = skillMaterials[materialTier] + 1
            val lootItems = mutableListOf(
                LootRevealItem("currency", "artifact", "Камни духа", "+$reward", "COMMON"),
                LootRevealItem("currency", "qi", "Ци", "+5", "UNCOMMON"),
                LootRevealItem("pet_xp", "pet_fox", "Опыт связи питомца", "+15", "UNCOMMON"),
                LootRevealItem("skill_point", "skills", "Очко навыка", "+1", "RARE"),
                LootRevealItem("material", "artifact", skillMaterialNames[materialTier], "+1", "RARE")
            )
            if (essenceFound) lootItems += LootRevealItem("pet_essence", "lotus", "Эссенция питомца", "+1", "RARE")
            if (petCoreFound) lootItems += LootRevealItem("pet_core", "artifact", "Ядро питомца", "+1", "EPIC")
            val weaponDrop = rollWeaponDrop(stage, ownedWeaponList)
            val gearDrop = if (Random.nextInt(100) < 35) rollGearDrop(stage) else null
            val oreFound = if (activeWorldEncounter != null) 0 else 1 + (bossLevel / 5).coerceAtMost(3)
            ironOre += oreFound
            val rareSkillFound = chosenClass >= 0 && rareSkill.isBlank() && Random.nextDouble() < 0.008
            val weaponText = if (weaponDrop != null) {
                val encodedDrop = encodeWeaponInstance(weaponDrop)
                ownedWeaponIds = (ownedWeaponList + encodedDrop).joinToString("|")
                lootItems += LootRevealItem("weapon", weaponDrop.type.toString(), weaponDrop.name, weaponDrop.rarity.title, weaponDrop.rarity.name)
                " Добыча: ${weaponDrop.name} · ${weaponDrop.rarity.title}."
            } else ""
            val gearText = if (gearDrop != null) {
                ownedGearIds = (ownedGearList + encodeGearInstance(gearDrop)).joinToString("|")
                lootItems += LootRevealItem("gear", gearDrop.slot.name, gearDrop.name, gearDrop.rarity.title, gearDrop.rarity.name)
                " Экипировка: ${gearDrop.name} · ${gearDrop.rarity.title}."
            } else ""
            petPity++
            val petDropPool = petCatalog.filter { it.id !in ownedPetIds.split("|") && it.requiredBoss <= encounterRank && it.id != "moon_deer" && it.id != "river_koi" }
            val foundPet = if (petDropPool.isNotEmpty() && (Random.nextInt(100) < 5 || petPity >= 20)) petDropPool.random() else null
            val petDropText = if (foundPet != null) {
                ownedPetIds = (ownedPetIds.split("|").filter { it.isNotBlank() } + foundPet.id).distinct().joinToString("|")
                petPity = 0
                lootItems += LootRevealItem("pet", foundPet.id, foundPet.name, "Новый спутник", "EPIC")
                " Редкая находка! Приручён питомец: ${foundPet.name}."
            } else ""
            if (rareSkillFound) {
                rareSkill = rareArts[chosenClass]
                lootItems += LootRevealItem("skill", "skills", rareSkill, "Редкое наследие", "MYTHIC")
            }
            if (oreFound > 0) lootItems += LootRevealItem("resource", "equipment", "Железная руда", "×$oreFound", "COMMON")
            val oreText = if (oreFound > 0) " и железная руда ×$oreFound" else ""
            message = "Победа! Получено $reward камней, 5 Ци и опыт связи питомца$oreText.$weaponText$gearText$petDropText" +
                if (rareSkillFound) " Редкое наследие: $rareSkill!" else ""
            if (pendingWorldBossId.isNotBlank()) {
                val defeated = defeatedWorldBossIds.split("|").filter { it.isNotBlank() }
                val worldBoss = worldLocationCatalog.firstOrNull { it.id == pendingWorldBossId && it.isBoss }
                if (worldBoss != null && worldBoss.id !in defeated) {
                    defeatedWorldBossIds = (defeated + worldBoss.id).distinct().joinToString("|")
                    worldFragments[worldBoss.worldIndex] = worldFragments[worldBoss.worldIndex] + 5
                    lootItems += LootRevealItem("fragment", "portal", "Фрагменты мира", "+5", "EPIC")
                    message += " Хранитель ${worldBoss.title} повержен: получено 5 фрагментов мира."
                }
                pendingWorldBossId = ""
                worldBossTurnCount = 0
            } else if (pendingWorldEncounterId.isNotBlank()) {
                val location = activeWorldEncounter
                if (location != null) {
                    val locationIndex = worldLocationCatalog.indexOfFirst { it.id == location.id }
                    if (locationIndex >= 0) {
                        explorationProgress[locationIndex] = (explorationProgress[locationIndex] + 1).coerceAtMost(location.progressGoal)
                    }
                    stones += location.stoneReward
                    qi = (qi + location.qiReward).coerceAtMost(9999)
                    when (location.resourceKind) {
                        "ore" -> ironOre += location.resourceAmount
                        "herbs" -> spiritHerbs += location.resourceAmount
                        "water" -> spiritWater += location.resourceAmount
                        "material" -> {
                            val tier = location.materialTier.coerceIn(0, skillMaterials.lastIndex)
                            skillMaterials[tier] = skillMaterials[tier] + location.resourceAmount
                        }
                    }
                    if (location.isElite) {
                        val eliteTier = if (location.materialTier >= 0) location.materialTier.coerceIn(0, skillMaterials.lastIndex)
                            else ((location.requiredStage - 1) / 5).coerceIn(0, skillMaterials.lastIndex)
                        skillMaterials[eliteTier] = skillMaterials[eliteTier] + 1
                        lootItems += LootRevealItem("material", "artifact", skillMaterialNames[eliteTier], "+1 · элита", "EPIC")
                    }
                    worldFragments[location.worldIndex] = worldFragments[location.worldIndex] + 1
                    val resourceIconKey = when (location.resourceKind) {
                        "ore" -> "equipment"
                        "herbs" -> "garden"
                        "water" -> "ice"
                        else -> "artifact"
                    }
                    lootItems += LootRevealItem("resource", resourceIconKey, location.resourceLabel, "×${location.resourceAmount}", if (location.isElite) "EPIC" else "UNCOMMON")
                    lootItems += LootRevealItem("currency", "artifact", "Камни духа", "+${location.stoneReward}", "COMMON")
                    lootItems += LootRevealItem("currency", "qi", "Ци", "+${location.qiReward}", "UNCOMMON")
                    lootItems += LootRevealItem("fragment", "portal", "Фрагмент мира", "+1", "EPIC")
                    val completedProgress = explorationProgress.getOrElse(locationIndex) { 0 }
                    message += " Экспедиция «${location.title}»: ${location.resourceLabel} ×${location.resourceAmount}, +${location.stoneReward} камней, +${location.qiReward} Ци. Прогресс ${completedProgress}/${location.progressGoal}."
                }
                pendingWorldEncounterId = ""
                worldBossTurnCount = 0
                selectedMenu = 9
            }
            lastLootPayload = encodeLootReveal(lootItems)
            lootRevealVisible = lootItems.isNotEmpty()
        } else if (playerHp <= 0) {
            autoBattle = false
            guardPrepared = false
            message = "Ты пал в бою. Восстановись за 40 камней духа, чтобы продолжить." + if (worldBossAbilityMessage.isNotBlank()) " $worldBossAbilityMessage" else ""
        } else {
            val actionName = when {
                action != null && action in 0..8 -> skillDefinitions[chosenClass][action].name
                action == 9 -> activeTechniqueName
                action == 10 -> "Способность оружия: ${equippedWeapon.name}"
                else -> "Обычная атака"
            }
            message = (if (critical) "КРИТИЧЕСКИЙ УДАР! " else "") + "$actionName · урон $damage." + if (worldBossAbilityMessage.isNotBlank()) " $worldBossAbilityMessage" else ""
        }
    }

    fun exploreWorldLocation(locationId: String) {
        val location = worldLocationCatalog.firstOrNull { it.id == locationId } ?: return
        val pendingBattleId = pendingWorldBossId.ifBlank { pendingWorldEncounterId }
        if (pendingBattleId.isNotBlank()) {
            if (pendingBattleId == location.id) {
                selectedMenu = 2
                message = "Сражение с «${location.title}» ещё не завершено. Продолжай бой на арене."
            } else {
                message = "Сначала заверши начатое сражение."
            }
            return
        }
        if (stage < location.requiredStage) {
            message = "Путь запечатан. Для территории «${location.title}» нужна ступень ${location.requiredStage}."
            return
        }
        if (location.isBoss) {
            if (location.id in defeatedWorldBossIds.split("|")) {
                message = "Хранитель этой территории уже повержен."
                return
            }
            val prerequisites = worldLocationCatalog.filter { it.worldIndex == location.worldIndex && !it.isBoss }
            val allExplored = prerequisites.all { prerequisite ->
                val index = worldLocationCatalog.indexOfFirst { it.id == prerequisite.id }
                explorationProgress.getOrElse(index) { 0 } >= prerequisite.progressGoal
            }
            if (!allExplored) {
                message = "Сначала заверши исследование всех трёх территорий этого мира."
                return
            }
            if (explorationEnergy < 3) {
                message = "Для вызова хранителя нужны 3 единицы энергии экспедиции."
                return
            }
            explorationEnergy -= 3
            lootRevealVisible = false
            lastLootPayload = ""
            pendingWorldEncounterId = ""
            pendingWorldBossId = location.id
            worldBossTurnCount = 0
            guardPrepared = false
            bossLevel = maxOf(bossLevel, location.bossRank)
            enemyHp = bossMaxHp(bossLevel)
            playerHp = playerMaxHp
            autoBattle = false
            selectedMenu = 2
            message = "Ты призвал хранителя «${location.title}». Победи его на арене, чтобы получить фрагменты мира."
            return
        }

        val index = worldLocationCatalog.indexOfFirst { it.id == location.id }
        if (index < 0) return
        if (explorationProgress[index] >= location.progressGoal) {
            message = "Территория «${location.title}» уже полностью исследована."
            return
        }
        if (explorationEnergy <= 0) {
            message = "Энергия экспедиции исчерпана. Она восстановится при следующем ежедневном обновлении."
            return
        }

        explorationEnergy--
        lootRevealVisible = false
        lastLootPayload = ""
        pendingWorldEncounterId = location.id
        worldBossTurnCount = 0
        guardPrepared = false
        enemyHp = worldEncounterMaxHp(location)
        playerHp = playerMaxHp
        autoBattle = false
        selectedMenu = 2
        message = if (location.isElite) {
            "Элитный противник «${location.enemyName}» преградил путь! Его особая техника: ${location.eliteAbilityName}."
        } else {
            "На территории «${location.title}» появился противник: ${location.enemyName}. Победи его, чтобы завершить часть исследования."
        }
    }

    fun exchangeWorldFragments(worldIndex: Int) {
        if (worldIndex !in 0..3) return
        if (worldFragments[worldIndex] < 5) {
            message = "Для обмена нужно 5 фрагментов этого мира."
            return
        }
        val materialTier = when (worldIndex) {
            0 -> 0
            1 -> 1
            2 -> 3
            else -> 5
        }
        worldFragments[worldIndex] -= 5
        skillMaterials[materialTier] = skillMaterials[materialTier] + 1
        message = "Пять фрагментов ${daoWorldCatalog[worldIndex].title} обменяны на ${skillMaterialNames[materialTier]}."
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

    LaunchedEffect(stage) {
        val unlockedWorldIndex = worldIndexForStage(stage)
        if (selectedWorldIndex < unlockedWorldIndex) selectedWorldIndex = unlockedWorldIndex
    }

    LaunchedEffect(pendingWorldBossId, pendingWorldEncounterId, enemyHp, playerHp) {
        if ((pendingWorldBossId.isBlank() && pendingWorldEncounterId.isBlank()) || enemyHp <= 0 || playerHp <= 0) {
            guardPrepared = false
        }
    }

    LaunchedEffect(selectedWorldIndex) {
        if (worldLocationCatalog.none { it.id == selectedLocationId && it.worldIndex == selectedWorldIndex }) {
            selectedLocationId = worldLocationCatalog.firstOrNull { it.worldIndex == selectedWorldIndex }?.id ?: "mortal_valley"
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            val currentDayKey = System.currentTimeMillis() / 86_400_000L
            if (currentDayKey != todayKey) {
                todayKey = currentDayKey
                dailyWins = 0
                dailyGardenCollected = false
                dailyMeditated = false
                dailyClanDonated = false
                dailyClaims = 0
                explorationEnergy = 6
                message = "Наступил новый день. Энергия экспедиций и ежедневные поручения восстановлены."
            }
        }
    }

    LaunchedEffect(qi, stones, stage, selectedWorldIndex, todayKey, explorationEnergy, explorationProgress.toList(), worldFragments.toList(), defeatedWorldBossIds, pendingWorldBossId, pendingWorldEncounterId, worldBossTurnCount, guardPrepared, lootRevealVisible, lastLootPayload, selectedLocationId, abodeLevel, ironOre, spiritHerbs, spiritWater, qiPills, bodyPills, bodyPillBonus, forgeTemper, alchemyRank, gardenReadyAt, springReadyAt, ownedPetIds, activePetId, petLevels.toList(), petBondXp.toList(), petAscensions.toList(), petEssence, petCores, petPity, dailyWins, dailyGardenCollected, dailyMeditated, dailyClanDonated, dailyClaims, clanReputation, petLevel, bossLevel, enemyHp, playerHp, wins, selectedTechnique, swordLevel, lotusLevel, foxLevel, chosenClass, skillPoints, skillTreeLevels.toList(), chosenSect, chosenAlliance, rareSkill, playerNickname, playerGender, creationClass, selectedWeapon, activeSkillNode, equippedWeaponId, ownedWeaponIds, ownedGearIds, equippedGearIds.toList(), questClaimed, storyChapter, skillMaterials.toList(), autoBattle, rotationOrder.toList()) {
        prefs.edit()
            .putInt("qi", qi)
            .putInt("stones", stones)
            .putInt("stage", stage)
            .putInt("selected_world_index", selectedWorldIndex)
            .putInt("world_exploration_energy", explorationEnergy)
            .putString("world_exploration_progress", explorationProgress.joinToString(","))
            .putString("world_fragments", worldFragments.joinToString(","))
            .putString("world_bosses_defeated", defeatedWorldBossIds)
            .putString("pending_world_boss", pendingWorldBossId)
            .putString("pending_world_encounter", pendingWorldEncounterId)
            .putInt("world_boss_turn_count", worldBossTurnCount)
            .putBoolean("battle_guard_prepared", guardPrepared)
            .putBoolean("battle_loot_visible", lootRevealVisible)
            .putString("battle_loot_payload", lastLootPayload)
            .putString("world_selected_location", selectedLocationId)
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
            .putString("player_nickname", playerNickname)
            .putString("player_gender", playerGender)
            .putInt("creation_class_draft", creationClass)
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

    if (chosenClass < 0) {
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
            CharacterCreationScreen(
                selectedClass = creationClass,
                onClassSelected = { creationClass = it },
                gender = playerGender,
                onGenderSelected = { playerGender = it },
                nickname = playerNickname,
                onNicknameChanged = { playerNickname = it },
                onConfirm = {
                    chosenClass = creationClass.coerceIn(0, 3)
                    playerNickname = playerNickname.trim()
                    message = "Путь выбран. Добро пожаловать, $playerNickname!"
                }
            )
        }
        return
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
        BackHandler {
            when {
                showMoreNavigation -> showMoreNavigation = false
                selectedTownBuilding.isNotBlank() -> selectedTownBuilding = ""
                selectedMenu != 0 -> { selectedMenu = 0; selectedTownBuilding = "" }
                else -> showExitDialog = true
            }
        }
        if (showMoreNavigation) {
            MoreNavigationDialog(
                selectedMenu = selectedMenu,
                stage = stage,
                onNavigate = { target ->
                    selectedMenu = target
                    selectedTownBuilding = ""
                    showMoreNavigation = false
                },
                onDismiss = { showMoreNavigation = false }
            )
        }
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Покинуть мир Дао?") },
                text = { Text("Игра сохранит прогресс. Действительно выйти из Cultivate?") },
                confirmButton = {
                    TextButton(onClick = { showExitDialog = false; (context as? android.app.Activity)?.finish() }) {
                        Text("Выйти")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) { Text("Остаться") }
                },
                containerColor = Color(0xFF1B2A32),
                titleContentColor = Gold,
                textContentColor = Color(0xFFE0E6EF)
            )
        }
        if (selectedMenu == 0) {
            Box(
                Modifier.fillMaxSize().graphicsLayer {
                    alpha = pageEnterTransition.value
                    scaleX = pageEnterTransition.value
                    scaleY = pageEnterTransition.value
                }
            ) {
                CinematicHomeScreen(
                    stage = stage, qi = qi, stones = stones, wins = wins,
                    storyChapter = storyChapter, combatPower = classBonus + classDefense + playerMaxHp,
                    playerNickname = playerNickname, playerGender = playerGender, classIndex = chosenClass,
                    onNavigate = { target -> selectedMenu = target; selectedTownBuilding = "" },
                    onBuilding = { building ->
                        if (building == "alchemy") {
                            abodeSection = "alchemy"
                            selectedMenu = 8
                            selectedTownBuilding = ""
                        } else {
                            selectedTownBuilding = building
                        }
                    }
                )
                if (selectedTownBuilding.isNotBlank()) {
                    Column(
                        Modifier.align(Alignment.BottomCenter)
                            .padding(start = 12.dp, end = 12.dp, bottom = 78.dp)
                            .fillMaxWidth()
                            .heightIn(max = 430.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth()
                                .background(Color(0xEE142238), MaterialTheme.shapes.large)
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ЛУННАЯ ГАВАНЬ  ·  ${selectedTownBuilding.uppercase()}", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            TextButton(onClick = { selectedTownBuilding = "" }) { Text("ЗАКРЫТЬ", color = Jade, fontSize = 10.sp) }
                        }
                when (selectedTownBuilding) {
                    "shop" -> MeiShopScreen(
                        stones = stones,
                        shardCount = skillMaterials[0],
                        onBuy = { quantity ->
                            val totalCost = quantity * 200
                            if (quantity > 0 && stones >= totalCost) {
                                stones -= totalCost
                                skillMaterials[0] += quantity
                                message = if (quantity == 1) {
                                    "Куплен осколок духа."
                                } else {
                                    "Куплено осколков духа: $quantity."
                                }
                            } else {
                                message = "Недостаточно камней духа."
                            }
                        },
                        onClose = { selectedTownBuilding = "" }
                    )
                    "auction" -> AuctionHallScreen(
                        stage = stage,
                        stones = stones,
                        onBuyWeapon = { listing, price ->
                            if (stones >= price) {
                                stones -= price
                                ownedWeaponIds = (ownedWeaponList + encodeWeaponInstance(listing)).joinToString("|")
                                message = "На аукционе приобретено: ${listing.name}."
                            } else message = "Недостаточно камней для ставки."
                        },
                        onBuyGear = { listing, price ->
                            if (stones >= price) {
                                stones -= price
                                ownedGearIds = (ownedGearList + encodeGearInstance(listing)).joinToString("|")
                                message = "На аукционе приобретено: ${listing.name}."
                            } else message = "Недостаточно камней для покупки."
                        },
                        onClose = { selectedTownBuilding = "" }
                    )
                    "forge" -> ForgeHallScreen(
                        weaponName = equippedWeapon.name,
                        weaponAttack = equippedWeapon.attackBonus,
                        bonusDamage = equippedWeapon.bonusDamage,
                        temper = forgeTemper,
                        ironOre = ironOre,
                        stones = stones,
                        onOpenWorkshop = {
                            abodeSection = "forge"
                            selectedMenu = 8
                            selectedTownBuilding = ""
                        },
                        onClose = { selectedTownBuilding = "" }
                    )
                    "event" -> GameCard("Юнь · посыльный ярмарки", "События и вести из соседних долин") {
                        Text("«На площади сегодня ярмарка духовных огней. Торговцы привезли редкие находки, а вечером у восточных ворот появится странствующий рассказчик».", color = Color.LightGray)
                        Text("Текущая история: глава ${storyChapter + 1} из 4", color = Gold)
                        Text("Награда участника ярмарки", color = Jade, fontWeight = FontWeight.Bold)
                        Button(onClick = {
                            if (prefs.getBoolean("lantern_event_claimed", false)) message = "Подарок ярмарки уже получен."
                            else {
                                stones += 75
                                prefs.edit().putBoolean("lantern_event_claimed", true).apply()
                                message = "За участие в ярмарке получено 75 камней духа."
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text(if (prefs.getBoolean("lantern_event_claimed", false)) "Подарок получен" else "Получить подарок · 75 камней") }
                        OutlinedButton(onClick = { selectedTownBuilding = "" }) { Text("Вернуться на площадь") }
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
                    }
                }
            }
        } else {
        Column(
            Modifier.fillMaxSize().background(Ink).graphicsLayer {
                alpha = pageEnterTransition.value
                scaleX = pageEnterTransition.value
                scaleY = pageEnterTransition.value
            }
        ) {
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(pageScrollState).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("CULTIVATE  ·  ПУТЬ ДАО", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text("${worldName(stage)} · ${stageName(stage)}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    HomeResource("qi", "Ци", qi.toString())
                    HomeResource("chest", "Камни", stones.toString())
                    HomeResource("arena", "Победы", wins.toString())
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    when (selectedMenu) {
                        1 -> "ПУТЬ КУЛЬТИВАЦИИ"
                        2 -> "АРЕНА И ИСПЫТАНИЯ"
                        3 -> "ПЕРСОНАЖ"
                        4 -> "ХРАНИЛИЩЕ СОКРОВИЩ"
                        5 -> "БОЕВЫЕ ТЕХНИКИ"
                        6 -> if (stage < 7) "КЛАНЫ" else "СЕКТЫ"
                        7 -> "ДУХОВНЫЙ ЗВЕРИНЕЦ"
                        8 -> "ЛИЧНАЯ ОБИТЕЛЬ"
                        9 -> "ЧЕТЫРЕ МИРА ДАО"
                        else -> "ЛУННАЯ ГАВАНЬ"
                    },
                    color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Black
                )
                TextButton(onClick = { selectedMenu = 0; selectedTownBuilding = "" }) {
                    Text("‹  В ГОРОД", color = Jade, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                    AbodeScene(abodeLevel, currentWorld, abodeSection) { selected -> abodeSection = selected }
                    if (abodeSection != "alchemy") {
                        Text("Небольшой двор в стороне от Лунной Гавани. Здесь ты можешь укрыться от суеты, собирать Ци и постепенно превращать скромное убежище в настоящую резиденцию культиватора.", color = Color.LightGray)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatCard("Железная руда", ironOre.toString(), Modifier.weight(1f))
                            StatCard("Травы", spiritHerbs.toString(), Modifier.weight(1f))
                            StatCard("Духовная вода", spiritWater.toString(), Modifier.weight(1f))
                        }
                    }
                    when (abodeSection) {
                        "sanctuary" -> {
                            Box(
                                Modifier.fillMaxWidth().height(148.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, Jade.copy(alpha = 0.62f), RoundedCornerShape(16.dp))
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.abode_meditation_scene),
                                    contentDescription = "Пещера уединения для медитации",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    Modifier.fillMaxSize().background(
                                        Brush.verticalGradient(
                                            listOf(Color(0x22101E35), Color(0xCC081522), Color(0xF0081522))
                                        )
                                    )
                                )
                                Column(
                                    Modifier.align(Alignment.BottomStart).padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text("ПЕЩЕРА УЕДИНЕНИЯ", color = Jade, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                                    Text("Место тихой медитации", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                }
                            }
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
                        "alchemy" -> AlchemyWorkshopScreen(
                            alchemyRank = alchemyRank,
                            maxAlchemyRank = maxAlchemyRank,
                            spiritHerbs = spiritHerbs,
                            spiritWater = spiritWater,
                            qiPills = qiPills,
                            bodyPills = bodyPills,
                            onBrewQi = {
                                if (spiritHerbs >= 6 && spiritWater >= 2) {
                                    spiritHerbs -= 6
                                    spiritWater -= 2
                                    qiPills++
                                    message = "Создана пилюля сбора Ци."
                                } else message = "Нужно 6 духовных трав и 2 порции духовной воды."
                            },
                            onBrewBody = {
                                if (spiritHerbs >= 5 && spiritWater >= 2) {
                                    spiritHerbs -= 5
                                    spiritWater -= 2
                                    bodyPills++
                                    message = "Создана пилюля крепкого тела."
                                } else message = "Нужно 5 духовных трав и 2 порции духовной воды."
                            },
                            onUseQi = {
                                val qiPower = 25 + alchemyRank * 5
                                if (qiPills > 0) {
                                    qiPills--
                                    qi = (qi + qiPower).coerceAtMost(9999)
                                    message = "Пилюля растворилась, даровав $qiPower Ци."
                                } else message = "В запасе нет пилюль сбора Ци."
                            },
                            onUseBody = {
                                val bodyPower = 15 + alchemyRank * 2
                                if (bodyPills > 0) {
                                    bodyPills--
                                    bodyPillBonus += bodyPower
                                    playerHp = (playerHp + bodyPower).coerceAtMost(playerMaxHp + bodyPower)
                                    message = "Тело укреплено навсегда: максимальное здоровье +$bodyPower."
                                } else message = "В запасе нет пилюль закалки."
                            },
                            onUpgradeRank = {
                                if (alchemyRank < maxAlchemyRank &&
                                    spiritHerbs >= alchemyRank * 4 &&
                                    spiritWater >= alchemyRank
                                ) {
                                    spiritHerbs -= alchemyRank * 4
                                    spiritWater -= alchemyRank
                                    alchemyRank++
                                    message = "Ранг алхимии повышен до $alchemyRank. Пилюли стали эффективнее."
                                } else message = "Для следующего ранга нужны ${alchemyRank * 4} трав и $alchemyRank порций воды."
                            }
                        )
                        "garden" -> {
                            val gardenReady = gardenReadyAt <= nowMillis
                            val gardenGrowth = if (gardenReady) 1f else
                                (1f - ((gardenReadyAt - nowMillis) / 28_800_000f)).coerceIn(0f, 1f)
                            SpiritGardenScreen(
                                abodeLevel = abodeLevel,
                                spiritHerbs = spiritHerbs,
                                growth = gardenGrowth,
                                timeRemaining = gardenRemaining,
                                ready = gardenReady,
                                harvestAmount = 2 + abodeLevel,
                                onHarvest = {
                                    val now = System.currentTimeMillis()
                                    if (now >= gardenReadyAt) {
                                        val harvest = 2 + abodeLevel
                                        spiritHerbs += harvest
                                        dailyGardenCollected = true
                                        gardenReadyAt = now + 28_800_000L
                                        if (Random.nextInt(100) < 3 && "moon_deer" !in ownedPetIds.split("|")) {
                                            ownedPetIds += "|moon_deer"
                                            message = "Собрано трав: $harvest. Среди листьев появился лунный оленёнок — редкий питомец!"
                                        } else {
                                            message = "Собрано духовных трав: $harvest. Следующий урожай примерно через 8 часов."
                                        }
                                    } else {
                                        message = "Сад ещё растёт. Вернись позже."
                                    }
                                }
                            )
                        }
                        "spring" -> {
                            val springReady = springReadyAt <= nowMillis
                            val springProgress = if (springReady) 1f else
                                (1f - ((springReadyAt - nowMillis) / 28_800_000f)).coerceIn(0f, 1f)
                            SpiritSpringScreen(
                                abodeLevel = abodeLevel,
                                spiritWater = spiritWater,
                                progress = springProgress,
                                timeRemaining = springRemaining,
                                ready = springReady,
                                collectAmount = 1 + abodeLevel / 3,
                                onCollect = {
                                    val now = System.currentTimeMillis()
                                    if (now >= springReadyAt) {
                                        spiritWater += 1 + abodeLevel / 3
                                        springReadyAt = now + 28_800_000L
                                        if (Random.nextInt(100) < 3 && "river_koi" !in ownedPetIds.split("|")) {
                                            ownedPetIds += "|river_koi"
                                            message = "В роднике вспыхнула золотая чешуя! Ты приручил духовного карпа."
                                        } else {
                                            message = "Набрана духовная вода из родника."
                                        }
                                    } else {
                                        message = "Родник ещё не восстановился."
                                    }
                                }
                            )
                        }
                        "worlds" -> {
                            Text("Четыре мира Дао", color = Gold, fontWeight = FontWeight.Bold)
                            Text("Каждая ступень открывает новую область культивации. Коснись иллюстрации, чтобы рассмотреть мир.", color = Color(0xFFC6D2E2), fontSize = 11.sp)
                            worldEntries.forEachIndexed { index, entry ->
                                val unlocked = stage >= entry.second
                                WorldIllustratedCard(
                                    index = index,
                                    title = entry.first,
                                    description = entry.third,
                                    requiredStage = entry.second,
                                    currentStage = stage,
                                    unlocked = unlocked,
                                    selected = selectedWorldIndex == index,
                                    onClick = {
                                        selectedWorldIndex = index
                                        message = if (unlocked) "Ты созерцаешь земли: ${entry.first}." else "Путь запечатан. Для входа нужна ступень ${entry.second}."
                                    }
                                )
                            }
                            val safeWorldIndex = selectedWorldIndex.coerceIn(0, worldEntries.lastIndex)
                            val selectedEntry = worldEntries[safeWorldIndex]
                            val selectedUnlocked = stage >= selectedEntry.second
                            val currentWorldIndex = worldIndexForStage(stage)
                            val nextWorld = worldEntries.getOrNull(safeWorldIndex + 1)
                            val worldProgress = when {
                                !selectedUnlocked -> (stage.toFloat() / selectedEntry.second).coerceIn(0f, 1f)
                                safeWorldIndex == currentWorldIndex && nextWorld != null ->
                                    ((stage - selectedEntry.second).toFloat() / (nextWorld.second - selectedEntry.second)).coerceIn(0f, 1f)
                                else -> 1f
                            }
                            Surface(
                                color = Color(0xE8172942),
                                shape = MaterialTheme.shapes.extraLarge,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedUnlocked) Jade.copy(alpha = .72f) else Gold.copy(alpha = .5f))
                            ) {
                                Column(
                                    Modifier.fillMaxWidth().padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(9.dp)
                                ) {
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        RpgIcon(if (selectedUnlocked) "portal" else "worlds", Modifier.size(38.dp))
                                        Spacer(Modifier.width(10.dp))
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                when {
                                                    !selectedUnlocked -> "Печать мира не разрушена"
                                                    safeWorldIndex == currentWorldIndex -> "ТВОЙ ТЕКУЩИЙ МИР"
                                                    else -> "ВРАТА МИРА ОТКРЫТЫ"
                                                },
                                                color = if (selectedUnlocked) Jade else Gold,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(selectedEntry.first, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                if (selectedUnlocked) "Твоя ступень: $stage · вход разрешён"
                                                else "Требуется ступень ${selectedEntry.second} · сейчас $stage",
                                                color = Color(0xFFD1DCEB),
                                                fontSize = 10.sp
                                            )
                                        }
                                        if (!selectedUnlocked) RpgIcon("lock", Modifier.size(27.dp))
                                    }
                                    LinearProgressIndicator(
                                        progress = { worldProgress },
                                        modifier = Modifier.fillMaxWidth().height(5.dp),
                                        color = if (selectedUnlocked) Jade else Gold,
                                        trackColor = Color(0xFF344458)
                                    )
                                    Text(
                                        if (safeWorldIndex == currentWorldIndex && nextWorld != null)
                                            "До следующих врат: ступень ${nextWorld.second}"
                                        else if (selectedUnlocked) "Доступные пути этого мира"
                                        else "Прогресс приближения к печати",
                                        color = Color(0xFFC9D6E5),
                                        fontSize = 9.sp
                                    )
                                    Row(
                                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        worldPortalDestinations[safeWorldIndex].forEach { destination ->
                                            WorldPortalChip(destination, safeWorldIndex == currentWorldIndex) {
                                                if (selectedUnlocked) {
                                                    selectedMenu = destination.menu
                                                    selectedTownBuilding = ""
                                                    if (destination.menu == 8) abodeSection = "sanctuary"
                                                    message = "Врата открыты: ${destination.title}."
                                                } else {
                                                    message = "Печать удерживает путь. Требуется ступень ${selectedEntry.second}."
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (selectedMenu == 1) {
                val breakthroughCost = cultivationQiCost(stage)
                val requiredBossRank = stage + 1
                CultivationPanel(
                    stage = stage,
                    qi = qi,
                    cost = breakthroughCost,
                    bossRank = requiredBossRank,
                    bossLevel = bossLevel,
                    classIndex = chosenClass, playerGender = playerGender, playerNickname = playerNickname,
                    onBreakthrough = {
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
                    }
                )
                GameCard("Ежедневный путь", "Испытания, сад духов и медитация") {
                    val battleClaimed = dailyClaims and 1 != 0
                    DailyTaskCard(
                        title = "Испытание стражей",
                        detail = "Победы сегодня: $dailyWins / 3",
                        reward = "+60 Ци · +100 камней",
                        iconKey = "arena",
                        claimed = battleClaimed,
                        available = dailyWins >= 3 && !battleClaimed,
                        onClaim = {
                            if (dailyWins >= 3 && !battleClaimed) {
                                qi += 60
                                stones += 100
                                dailyClaims = dailyClaims or 1
                                message = "Ежедневное испытание завершено: +60 Ци и 100 камней."
                            }
                        }
                    )
                    val gardenClaimed = dailyClaims and 2 != 0
                    DailyTaskCard(
                        title = "Сад духовных трав",
                        detail = if (dailyGardenCollected) "Урожай собран" else "Урожай ещё не готов",
                        reward = "+45 Ци · +2 травы",
                        iconKey = "garden",
                        claimed = gardenClaimed,
                        available = dailyGardenCollected && !gardenClaimed,
                        onClaim = {
                            if (dailyGardenCollected && !gardenClaimed) {
                                qi += 45
                                spiritHerbs += 2
                                dailyClaims = dailyClaims or 2
                                message = "За заботу о саде получено 45 Ци и 2 травы."
                            }
                        }
                    )
                    val meditationClaimed = dailyClaims and 4 != 0
                    DailyTaskCard(
                        title = "Медитация у источника",
                        detail = if (dailyMeditated) "Практика завершена" else "Сосредоточься на дыхании Ци",
                        reward = "+20 Ци · +50 камней",
                        iconKey = "cultivation",
                        claimed = meditationClaimed,
                        available = dailyMeditated && !meditationClaimed,
                        onClaim = {
                            if (dailyMeditated && !meditationClaimed) {
                                qi += 20
                                stones += 50
                                dailyClaims = dailyClaims or 4
                                message = "За ежедневную медитацию получено 20 Ци и 50 камней."
                            }
                        }
                    )
                }
            }

            if (selectedMenu == 9) {
                WorldExplorationScreen(
                    stage = stage,
                    combatPower = classBonus + classDefense + playerMaxHp,
                    classIndex = chosenClass, playerGender = playerGender, playerNickname = playerNickname,
                    selectedWorldIndex = selectedWorldIndex,
                    onSelectWorld = { index ->
                        selectedWorldIndex = index
                        selectedLocationId = worldLocationCatalog.firstOrNull { it.worldIndex == index }?.id ?: "mortal_valley"
                        message = "Открыта карта экспедиций: ${daoWorldCatalog[index].title}."
                    },
                    explorationEnergy = explorationEnergy,
                    locationProgress = explorationProgress,
                    worldFragments = worldFragments,
                    defeatedBossIds = defeatedWorldBossIds.split("|").filter { it.isNotBlank() }.toSet(),
                    pendingBossId = pendingWorldBossId,
                    pendingEncounterId = pendingWorldEncounterId,
                    statusMessage = message,
                    selectedLocationId = selectedLocationId,
                    onSelectLocation = { selectedLocationId = it },
                    onExplore = { exploreWorldLocation(it) },
                    onExchangeFragments = { exchangeWorldFragments(it) }
                )
                if (lootRevealVisible && lastLootPayload.isNotBlank()) {
                    BattleLootReveal(
                        payload = lastLootPayload,
                        onDismiss = {
                            lootRevealVisible = false
                            lastLootPayload = ""
                        },
                        onOpenItem = { item ->
                            lootRevealVisible = false
                            lastLootPayload = ""
                            selectedMenu = when (item.kind) {
                                "weapon", "gear", "currency" -> 4
                                "pet", "pet_xp", "pet_essence", "pet_core" -> 7
                                "skill", "skill_point", "material" -> 5
                                "fragment" -> 9
                                "resource" -> 8
                                else -> 4
                            }
                        }
                    )
                }
            }

            if (selectedMenu == 2) {
                val incomingAbilityName = activeWorldBoss?.let { worldBossAbilityName(it.worldIndex) }
                    ?: activeWorldEncounter?.takeIf { it.isElite }?.eliteAbilityName.orEmpty()
                // The enemy's third response is telegraphed before the player's third action.
                val telegraphActive = activeWorldOpponent != null && incomingAbilityName.isNotBlank() &&
                    enemyHp > 0 && playerHp > 0 && (worldBossTurnCount + 1) % 3 == 0
                val arenaBackgroundRes = activeWorldOpponent?.let { daoWorldCatalog[it.worldIndex].backgroundRes } ?: R.drawable.lunar_harbor_bg
                val enemyAccent = activeWorldOpponent?.let { daoWorldCatalog[it.worldIndex].accent } ?: Color(0xFFE47A73)
                val arenaTitle = activeWorldBoss?.title ?: activeWorldEncounter?.enemyName ?: "Страж святилища · ранг $bossLevel"
                GameCard("Арена испытаний", arenaTitle) {
                    BattleScene(
                        autoBattle = autoBattle,
                        actionText = message,
                        bossLevel = bossLevel,
                        playerName = playerNickname.ifBlank { classNames[chosenClass] },
                        playerHp = playerHp,
                        playerMaxHp = playerMaxHp,
                        enemyHp = enemyHp,
                        enemyMaxHp = enemyMaxHp,
                        playerPortraitRes = when (chosenClass) {
                            1 -> R.drawable.class_portrait_ironbody
                            2 -> R.drawable.class_portrait_elementalist
                            3 -> R.drawable.class_portrait_spiritmystic
                            else -> R.drawable.class_portrait_blademaster
                        },
                        heroArtworkRes = when (chosenClass) {
                            0 -> if (playerGender == "female") R.drawable.blademaster_female else R.drawable.blademaster_male
                            1 -> if (playerGender == "female") R.drawable.ironbody_female else R.drawable.ironbody_male
                            2 -> if (playerGender == "female") R.drawable.elementalist_female else R.drawable.elementalist_male
                            else -> if (playerGender == "female") R.drawable.spiritmystic_female else R.drawable.spiritmystic_male
                        },
                        petName = activePet.name,
                        petPortraitRes = petPortraitResource(activePet.id),
                        enemyPortraitRes = activeWorldBoss?.battlePortraitRes ?: activeWorldEncounter?.enemyPortraitRes ?: R.drawable.boss_mountain_lord,
                        arenaBackgroundRes = arenaBackgroundRes,
                        enemyAccent = enemyAccent,
                        enemyTitle = activeWorldBoss?.title ?: activeWorldEncounter?.enemyName.orEmpty(),
                        incomingAbilityName = incomingAbilityName,
                        telegraphActive = telegraphActive,
                        guardPrepared = guardPrepared,
                        telegraphAccent = enemyAccent
                    )

                    if (lootRevealVisible && lastLootPayload.isNotBlank()) {
                        BattleLootReveal(
                            payload = lastLootPayload,
                            onDismiss = {
                                lootRevealVisible = false
                                lastLootPayload = ""
                            },
                            onOpenItem = { item ->
                                lootRevealVisible = false
                                lastLootPayload = ""
                                selectedMenu = when (item.kind) {
                                    "weapon", "gear", "currency" -> 4
                                    "pet", "pet_xp", "pet_essence", "pet_core" -> 7
                                    "skill", "skill_point", "material" -> 5
                                    "fragment" -> 9
                                    "resource" -> 8
                                    else -> 4
                                }
                            }
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BattleActionButton(
                            modifier = Modifier.weight(1f),
                            icon = "arena",
                            label = "УДАР",
                            enabled = enemyHp > 0 && playerHp > 0 && !autoBattle
                        ) { performCombatTurn() }
                        BattleActionButton(
                            modifier = Modifier.weight(1f),
                            icon = if (autoBattle) "check" else "skills",
                            label = if (autoBattle) "АВТО: ВКЛ" else "АВТОБОЙ",
                            enabled = enemyHp > 0 && playerHp > 0,
                            selected = autoBattle
                        ) { autoBattle = !autoBattle }
                    }

                    if (telegraphActive || guardPrepared) {
                        BattleActionButton(
                            modifier = Modifier.fillMaxWidth(),
                            icon = "shield",
                            label = if (guardPrepared) "ЗАЩИТА ГОТОВА · ОТМЕНИТЬ" else "ПРИНЯТЬ ЗАЩИТНУЮ СТОЙКУ",
                            enabled = enemyHp > 0 && playerHp > 0 && !autoBattle,
                            selected = guardPrepared
                        ) {
                            guardPrepared = !guardPrepared
                            message = if (guardPrepared) {
                                "Ты принимаешь защитную стойку. Следующий ответный удар будет ослаблен."
                            } else {
                                "Защитная стойка отменена. Можно полагаться на активную технику."
                            }
                        }
                    }

                    if (chosenClass in 0..3) {
                        val availableCombatSkills = (0..8).filter { node ->
                            skillTreeLevels[chosenClass * 9 + node] == 1 &&
                                skillDefinitions[chosenClass][node].active
                        }
                        if (availableCombatSkills.isNotEmpty()) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ИСКУССТВА БОЯ", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                Spacer(Modifier.weight(1f))
                                Text("НАЖМИ ДЛЯ ПРИМЕНЕНИЯ", color = Jade, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                availableCombatSkills.forEach { node ->
                                    BattleSkillCard(
                                        name = skillDefinitions[chosenClass][node].name,
                                        iconRes = battleSkillDrawable(chosenClass),
                                        cooldown = skillCooldowns[node],
                                        enabled = enemyHp > 0 && playerHp > 0 && !autoBattle && skillCooldowns[node] == 0,
                                        selected = activeSkillNode == node
                                    ) {
                                        performCombatTurn(node)
                                    }
                                }
                            }
                        }
                    } else {
                        Surface(
                            color = Color(0xCC17243A),
                            shape = MaterialTheme.shapes.large,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = .45f))
                        ) {
                            Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                RpgIcon("skills", Modifier.size(28.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Выбери класс и изучи активные техники, чтобы открывать ручное применение в бою.", color = Color(0xFFD6E0EC), fontSize = 9.sp)
                            }
                        }
                    }

                    if (playerHp <= 0) {
                        BattleActionButton(
                            modifier = Modifier.fillMaxWidth(),
                            icon = "health",
                            label = "ВОССТАНОВИТЬСЯ · 40 КАМНЕЙ",
                            enabled = true
                        ) {
                            if (stones >= 40) {
                                stones -= 40
                                playerHp = playerMaxHp
                                message = "Ты восстановил силы и готов вернуться на арену."
                            } else {
                                message = "Нужно 40 камней духа для восстановления."
                            }
                        }
                    }

                    BattleActionButton(
                        modifier = Modifier.fillMaxWidth(),
                        icon = "portal",
                        label = "ВЫЗВАТЬ СЛЕДУЮЩЕГО СТРАЖА",
                        enabled = enemyHp <= 0
                    ) {
                        if (enemyHp <= 0) {
                            bossLevel++
                            guardPrepared = false
                            lootRevealVisible = false
                            lastLootPayload = ""
                            enemyHp = bossMaxHp(bossLevel)
                            playerHp = playerMaxHp
                            message = "На арену вышел страж ранга $bossLevel. Будь осторожен!"
                        } else {
                            message = "Сначала одолей текущего стража."
                        }
                    }

                    Surface(
                        color = Color(0xCC17243A),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(Modifier.fillMaxWidth().padding(9.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("РОТАЦИЯ БОЯ", color = Gold, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            Text(
                                rotationOrder.joinToString("  ›  ") { action ->
                                    when {
                                        action in 0..8 && chosenClass >= 0 -> skillDefinitions[chosenClass][action].name
                                        action == 9 -> "Техника"
                                        action == 10 -> "Оружие"
                                        else -> "Навык"
                                    }
                                },
                                color = Color(0xFFE3D8C2),
                                fontSize = 9.sp,
                                maxLines = 2
                            )
                            Text(
                                "Перезарядка · навык ${skillCooldowns[activeSkillNode]} · техника $techniqueCooldown · оружие $weaponCooldown",
                                color = Color(0xFFAEBBCB),
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }

            if (selectedMenu == 3) {
                CharacterArmoryScreen(
                    stage = stage,
                    playerNickname = playerNickname,
                    playerGender = playerGender,
                    classIndex = chosenClass,
                    attack = classBonus,
                    defense = classDefense,
                    currentHp = playerHp,
                    maxHp = playerMaxHp,
                    equippedWeapon = equippedWeapon,
                    equippedGear = equippedGear,
                    setBonus = activeSetBonus,
                    onOpenWeapons = {
                        inventoryFilter = 1
                        selectedTownBuilding = ""
                        selectedMenu = 4
                    },
                    onOpenGear = {
                        inventoryFilter = 2
                        selectedTownBuilding = ""
                        selectedMenu = 4
                    },
                    onSelectClass = { i ->
                        chosenClass = i
                        inspectedSkillNode = 0
                        rotationOrder.clear()
                        (0..8).filter { skillTreeLevels[i * 9 + it] == 1 && skillDefinitions[i][it].active }
                            .forEach { rotationOrder.add(it) }
                        rotationOrder.add(9)
                        rotationOrder.add(10)
                        if (skillTreeLevels[i * 9 + activeSkillNode] == 0 || !skillDefinitions[i][activeSkillNode].active) {
                            activeSkillNode = 0
                        }
                        playerHp = playerHp.coerceAtMost(500 + stage * 30 + classCombatProfiles[i].bonusHp)
                        message = "Выбран класс: ${classNames[i]}"
                    }
                )
            }

            if (selectedMenu == 4) {
                TreasuryVaultScreen(
                    stage = stage,
                    classIndex = chosenClass,
                    weapons = ownedWeapons,
                    gear = ownedGear,
                    equippedWeapon = equippedWeapon,
                    equippedGear = equippedGear,
                    setBonus = activeSetBonus,
                    filter = inventoryFilter,
                    onFilterChange = { inventoryFilter = it },
                    onEquipWeapon = { item ->
                        equippedWeaponId = encodeWeaponInstance(item)
                        selectedWeapon = item.type
                        message = "Экипировано: ${item.name}"
                    },
                    onEquipGear = { item ->
                        equippedGearIds[item.slot.ordinal] = encodeGearInstance(item)
                        message = "Экипировано: ${item.name}"
                    }
                )
            }

            if (selectedMenu == 5) {
                GameCard("Арсенал боевых искусств", "Выбери духовную технику и пробуди её силу") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                        RpgIcon("qi", Modifier.size(25.dp))
                        Column(Modifier.weight(1f)) {
                            Text("МАСТЕРСТВО ТЕХНИКИ", color = Gold, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            Text("$activeTechniqueName · $activeTechniqueLevel / 50", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Surface(color = Color(0xFF183B36), shape = CircleShape, border = BorderStroke(1.dp, Jade.copy(alpha = .65f))) {
                            Text("+$techniqueDamage УРОН", Modifier.padding(horizontal = 8.dp, vertical = 6.dp), color = Jade, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Text("Материалы пробуждения", color = Color(0xFFD5DFE8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        skillMaterialNames.indices.forEach { materialIndex ->
                            Surface(Modifier.weight(1f), color = Color(0xFF142333), shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, Color(0x334E718E))) {
                                Column(Modifier.padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    RpgIcon("potion", Modifier.size(21.dp))
                                    Text("×${skillMaterials[materialIndex]}", color = Jade, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                    TechniqueRow("Меч рассекающей Ци", "Сильный прямой удар", "weapon", selectedTechnique == 0, swordLevel, techniqueUpgradeCost(swordLevel), stones,
                        onSelect = { selectedTechnique = 0 },
                        onUpgrade = { upgradeTechnique(swordLevel) { swordLevel = it } }
                    )
                    TechniqueRow("Нефритовый лотос", "Устойчивая техника духовной энергии", "lotus", selectedTechnique == 1, lotusLevel, techniqueUpgradeCost(lotusLevel), stones,
                        onSelect = { selectedTechnique = 1 },
                        onUpgrade = { upgradeTechnique(lotusLevel) { lotusLevel = it } }
                    )
                    TechniqueRow("Коготь лиса", "Быстрая атака в союзе со спутником", "pet_fox", selectedTechnique == 2, foxLevel, techniqueUpgradeCost(foxLevel), stones,
                        onSelect = { selectedTechnique = 2 },
                        onUpgrade = { upgradeTechnique(foxLevel) { foxLevel = it } }
                    )
                    Text("Дополнительный урон выбранной техники: +$techniqueDamage", color = Gold, fontWeight = FontWeight.Bold)
                }
                GameCard("Тактика автоматического боя", "Приоритет применения боевых действий") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf("ПРИОРИТЕТ", "ПЕРЕЗАРЯДКА", "АВТОБОЙ").forEachIndexed { index, label ->
                            Surface(Modifier.weight(1f), color = Color(0xFF172839), shape = MaterialTheme.shapes.small) {
                                Text(label, Modifier.padding(vertical = 6.dp), color = if (index == 0) Gold else Color(0xFF9DB0C3), fontSize = 7.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                    rotationOrder.forEachIndexed { index, action ->
                        val actionLabel = when {
                            action in 0..8 && chosenClass >= 0 -> skillDefinitions[chosenClass][action].name
                            action == 9 -> "Духовная техника"
                            action == 10 -> "Пробуждение оружия"
                            else -> "Боевой навык"
                        }
                        val iconKey = when (action) {
                            9 -> when (selectedTechnique) { 0 -> "weapon"; 1 -> "lotus"; else -> "pet_fox" }
                            10 -> "weapon"
                            else -> if (chosenClass == 1) "shield" else if (chosenClass == 2) "magic" else "skill"
                        }
                        Surface(
                            color = if (index == 0) Color(0xFF294238) else Color(0xE9162636),
                            shape = MaterialTheme.shapes.large,
                            border = BorderStroke(1.dp, if (index == 0) Gold.copy(alpha = .75f) else Color(0x335E7891))
                        ) {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("${index + 1}", color = if (index == 0) Gold else Color(0xFF8094A9), fontSize = 11.sp, fontWeight = FontWeight.Black)
                                RpgIcon(iconKey, Modifier.size(34.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(actionLabel, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(if (action == 9) "ПЕРЕЗАРЯДКА · 3 СЕК" else if (action == 10) "ПЕРЕЗАРЯДКА · 6 СЕК" else "ПЕРЕЗАРЯДКА · 4 СЕК", color = Color(0xFF9FB1C3), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    IconButton(onClick = { if (index > 0) { val v = rotationOrder[index-1]; rotationOrder[index-1] = rotationOrder[index]; rotationOrder[index] = v } }, enabled = index > 0, modifier = Modifier.size(27.dp)) {
                                        Text("▲", color = if (index > 0) Gold else Color(0xFF526173), fontSize = 10.sp)
                                    }
                                    IconButton(onClick = { if (index < rotationOrder.lastIndex) { val v = rotationOrder[index+1]; rotationOrder[index+1] = rotationOrder[index]; rotationOrder[index] = v } }, enabled = index < rotationOrder.lastIndex, modifier = Modifier.size(27.dp)) {
                                        Text("▼", color = if (index < rotationOrder.lastIndex) Gold else Color(0xFF526173), fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                GameCard("Зал боевых искусств", if (chosenClass < 0) "Сначала выбери класс в меню персонажа" else "Наследие · ${classNames[chosenClass]}") {
                    if (chosenClass >= 0) {
                        val learnedForClass = (0..8).count { skillTreeLevels[chosenClass * 9 + it] == 1 }
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(battleSkillDrawable(chosenClass)),
                                contentDescription = "Знак наследия класса ${classNames[chosenClass]}",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(66.dp)
                            )
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("НАСЛЕДИЕ КЛАССА", color = Gold, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.3.sp)
                                Text(classNames[chosenClass], color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                                Text("$learnedForClass / 9 узлов освоено · ${classDescriptions[chosenClass]}", color = Color(0xFFB9C9D8), fontSize = 8.sp, lineHeight = 11.sp)
                            }
                            Surface(
                                color = Color(0xFF193D38),
                                shape = RoundedCornerShape(13.dp),
                                border = BorderStroke(1.dp, Jade.copy(alpha = .7f))
                            ) {
                                Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$skillPoints", color = Color(0xFFFFE4A8), fontSize = 19.sp, fontWeight = FontWeight.Black)
                                    Text("ОЧКИ", color = Jade, fontSize = 7.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        Text("ПУТЬ РАЗВИТИЯ · ВЫБЕРИ УЗЕЛ, ЧТОБЫ УЗНАТЬ ЕГО СИЛУ", color = Color(0xFFB8C7D8), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("ДРЕВО НАСЛЕДИЯ · 9 ТЕХНИК", color = Color(0xFFB8C7D8), fontSize = 8.sp, fontWeight = FontWeight.Black)
                        (0..8).chunked(3).forEach { nodeRow ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                nodeRow.forEach { node ->
                                    SkillQuickTile(
                                        modifier = Modifier.weight(1f),
                                        classIndex = chosenClass,
                                        node = node,
                                        levels = skillTreeLevels,
                                        points = skillPoints,
                                        activeNode = activeSkillNode,
                                        characterLevel = stage.coerceAtMost(20),
                                        onClick = {
                                            inspectedSkillNode = node
                                            showSkillNodeDialog = true
                                        }
                                    )
                                }
                            }
                        }
                        Text(
                            "Боевой слот: ${skillDefinitions[chosenClass][activeSkillNode].name} · перезарядка ${skillCooldowns[activeSkillNode]} хода",
                            color = Gold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (showSkillNodeDialog) {
                            Dialog(onDismissRequest = { showSkillNodeDialog = false }) {
                                Surface(
                                    color = Color(0xFF101D2D),
                                    shape = RoundedCornerShape(22.dp),
                                    border = BorderStroke(1.dp, Gold.copy(alpha = .75f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                            TextButton(onClick = { showSkillNodeDialog = false }) { Text("ЗАКРЫТЬ", color = Jade) }
                                        }
                                        SkillNodeDetailCard(
                                            classIndex = chosenClass,
                                            node = inspectedSkillNode,
                                            levels = skillTreeLevels,
                                            points = skillPoints,
                                            weapon = selectedWeapon,
                                            activeNode = activeSkillNode,
                                            characterLevel = stage.coerceAtMost(20),
                                            onLearn = { learnSkillNode(inspectedSkillNode) },
                                            onSetActive = {
                                                activeSkillNode = inspectedSkillNode
                                                rotationOrder.remove(inspectedSkillNode)
                                                rotationOrder.add(0, inspectedSkillNode)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text("Выбери одно из четырёх наследий в зале персонажа. Каждое открывает собственное древо из девяти техник и пассивных искусств.", color = Color(0xFFD2DCEB), fontSize = 11.sp)
                    }
                    Text(if (rareSkill.isBlank()) "Редкое искусство: шанс выпадения за победу — 0,8%." else "Получено редкое искусство: $rareSkill", color = Gold)
                }
            }

            if (selectedMenu == 6) {
                val isMortalClan = stage < 7
                val factionNames = if (isMortalClan) clanNames else sectNames
                val factionLore = if (isMortalClan) clanLore else sectLore
                val factionAlliances = if (isMortalClan) factionAlliance else sectAlliance
                SectHallScreen(
                    stage = stage, selectedAlliance = chosenAlliance, chosenSect = chosenSect,
                    reputation = clanReputation, dailyDonated = dailyClanDonated, stones = stones,
                    factionNames = factionNames, factionLore = factionLore, factionAlliances = factionAlliances,
                    attackBonuses = clanAttackBonuses, defenseBonuses = clanDefenseBonuses, hpBonuses = clanHpBonuses,
                    onAllianceSelected = { index ->
                        chosenAlliance = index
                        if (index >= 0 && chosenSect >= 0 && factionAlliances[chosenSect] != index) chosenSect = -1
                        message = if (index < 0) "Открыт обзор всех объединений." else "Выбран путь: ${if (isMortalClan) listOf("Союз долин", "Вольные дома")[index] else listOf("Праведный альянс", "Злой альянс")[index]}."
                    },
                    onJoinFaction = { index ->
                        chosenSect = index
                        message = if (isMortalClan) "Ты вступил в клан «${factionNames[index]}»." else "Ты вступил в секту «${factionNames[index]}»."
                    },
                    onDonate = {
                        if (chosenSect < 0) message = "Сначала вступи в клан или секту."
                        else if (!dailyClanDonated && stones >= 50) {
                            stones -= 50; qi += 35; clanReputation++; dailyClanDonated = true
                            message = "Вклад принят. +35 Ци и 1 очко репутации."
                        } else if (dailyClanDonated) message = "Сегодня ты уже внёс вклад."
                        else message = "Для вклада нужно 50 камней духа."
                    }
                )
            }

            if (selectedMenu == 7) GameCard("Духовный зверинец", "Активный спутник · " + activePet.name) {
                Text("Собирай древних зверей, открывай их резонанс с классом и укрепляй связь в личном святилище.", color = Color(0xFFD2DCEB), fontSize = 11.sp)
                PetCompanionArt(activePet)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PetResourceBadge("qi", "Эссенция", petEssence.toString(), Modifier.weight(1f))
                    PetResourceBadge("lotus", "Ядра", petCores.toString(), Modifier.weight(1f))
                    PetResourceBadge("chest", "Поиск", petPity.toString() + " / 20", Modifier.weight(1f))
                }
                val xpCost = 100 + petLevel * 75
                val essenceCost = 1 + petLevel / 5
                val coreCost = if ((petLevel + 1) % 5 == 0) 1 else 0
                PetActiveShowcase(
                    pet = activePet,
                    level = petLevel,
                    bondXp = petXp,
                    ascension = petAscension,
                    attack = petAttack,
                    defense = petDefense,
                    hp = activePet.hp + petLevel * 3 + if (petSynergy) 20 else 0,
                    skill = petSkill,
                    synergy = petSynergy,
                    xpCost = xpCost
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        InventoryActionButton(
                            label = if (petLevel >= 30) "МАКС. УРОВЕНЬ" else "УКРЕПИТЬ СВЯЗЬ",
                            selected = false,
                            enabled = petLevel < 30 && petXp >= xpCost && petEssence >= essenceCost && petCores >= coreCost,
                            onClick = {
                                if (petLevel < 30 && petXp >= xpCost && petEssence >= essenceCost && petCores >= coreCost) {
                                    petBondXp[activePetIndex] = petXp - xpCost
                                    petEssence -= essenceCost
                                    petCores -= coreCost
                                    petLevels[activePetIndex] = petLevel + 1
                                    message = activePet.name + " достиг " + (petLevel + 1) + " уровня связи."
                                } else message = "Нужно " + xpCost + " опыта связи, " + essenceCost + " эссенции и " + coreCost + " ядер зверя."
                            }
                        )
                    }
                    if (petLevel >= 30 && petAscension < 5) {
                        Box(Modifier.weight(1f)) {
                            InventoryActionButton(
                                label = "ПРОБУЖДЕНИЕ",
                                selected = true,
                                enabled = petEssence >= 20 && petCores >= 5,
                                onClick = {
                                    if (petEssence >= 20 && petCores >= 5) {
                                        petEssence -= 20
                                        petCores -= 5
                                        petAscensions[activePetIndex] = petAscension + 1
                                        message = activePet.name + " пробудил новую ступень силы!"
                                    } else message = "Пробуждение требует 20 эссенции и 5 ядер зверя."
                                }
                            )
                        }
                    }
                }
                val ownedPetIdsSet = ownedPetIds.split("|").filter { it.isNotBlank() }.toSet()
                val ownedPetCount = petCatalog.count { it.id in ownedPetIdsSet }
                InventorySectionHeading("Бестиарий духовных зверей", "${ownedPetCount} / ${petCatalog.size} приручено", "pets")
                LinearProgressIndicator(
                    progress = { ownedPetCount.toFloat() / petCatalog.size.coerceAtLeast(1) },
                    modifier = Modifier.fillMaxWidth().height(5.dp),
                    color = Jade,
                    trackColor = Color(0xFF34445A)
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    InventoryCategoryTab("ВСЕ · ${petCatalog.size}", "pets", petFilter == 0, Modifier.weight(1f)) { petFilter = 0 }
                    InventoryCategoryTab("МОИ · $ownedPetCount", "check", petFilter == 1, Modifier.weight(1f)) { petFilter = 1 }
                    InventoryCategoryTab("ПОИСК · ${petCatalog.size - ownedPetCount}", "chest", petFilter == 2, Modifier.weight(1f)) { petFilter = 2 }
                }
                val visiblePetEntries = petCatalog.withIndex().filter { (_, pet) ->
                    when (petFilter) {
                        1 -> pet.id in ownedPetIdsSet
                        2 -> pet.id !in ownedPetIdsSet
                        else -> true
                    }
                }
                visiblePetEntries.chunked(3).forEach { petRow ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        petRow.forEach { (index, pet) ->
                            val owned = pet.id in ownedPetIdsSet
                            val isActive = pet.id == activePetId
                            PetGalleryTile(
                                modifier = Modifier.weight(1f),
                                pet = pet,
                                owned = owned,
                                active = isActive,
                                level = petLevels[index],
                                onClick = { selectedPetDetailIndex = index }
                            )
                        }
                        repeat(3 - petRow.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
                if (selectedPetDetailIndex in petCatalog.indices) {
                    val detailIndex = selectedPetDetailIndex
                    val detailPet = petCatalog[detailIndex]
                    val detailOwned = detailPet.id in ownedPetIdsSet
                    Dialog(onDismissRequest = { selectedPetDetailIndex = -1 }) {
                        Surface(
                            color = Color(0xFF101D2D),
                            shape = RoundedCornerShape(22.dp),
                            border = BorderStroke(1.dp, petRarityColor(detailPet.id).copy(alpha = .85f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { selectedPetDetailIndex = -1 }) { Text("ЗАКРЫТЬ", color = Jade) }
                                }
                                PetCompanionCard(
                                    pet = detailPet,
                                    owned = detailOwned,
                                    active = detailPet.id == activePetId,
                                    level = petLevels[detailIndex],
                                    bondXp = petBondXp[detailIndex],
                                    ascension = petAscensions[detailIndex],
                                    onSelect = {
                                        if (detailOwned) {
                                            activePetId = detailPet.id
                                            message = detailPet.name + " теперь твой спутник."
                                        } else {
                                            message = "След: ${detailPet.dropHint}."
                                        }
                                        selectedPetDetailIndex = -1
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Text(message, color = Gold, fontSize = 14.sp)
            Text("Прогресс сохраняется автоматически на этом устройстве.", color = Color.Gray, fontSize = 11.sp)
        }
        CultivateBottomNavigation(
            selectedMenu = selectedMenu,
            onNavigate = { target ->
                selectedMenu = target
                selectedTownBuilding = ""
                showMoreNavigation = false
                if (target == 8 && abodeSection.isBlank()) abodeSection = "sanctuary"
            },
            onMore = { showMoreNavigation = true }
        )
        }
    }
}

}

@Composable
private fun CinematicHomeScreen(
    stage: Int, qi: Int, stones: Int, wins: Int, storyChapter: Int, combatPower: Int,
    playerNickname: String, playerGender: String, classIndex: Int,
    onNavigate: (Int) -> Unit, onBuilding: (String) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFF081426))) {
        Image(
            painter = painterResource(R.drawable.lunar_harbor_bg),
            contentDescription = "Лунная Гавань — главная игровая локация",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            0f to Color(0xAA07101F), .22f to Color(0x1807101F),
            .68f to Color(0x1207101F), 1f to Color(0xE6081020)
        )))
        val glow = rememberInfiniteTransition(label = "home-lanterns")
        val pulse by glow.animateFloat(.72f, 1f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "home-pulse")
        Canvas(Modifier.fillMaxSize()) {
            for (i in 0..34) {
                val x = size.width * (((i * 73) % 97) / 97f)
                val y = size.height * (.12f + ((i * 29) % 78) / 100f)
                drawCircle(Color(0xFFFFD99B).copy(alpha = pulse * .30f), if (i % 5 == 0) 2.8f else 1.4f, Offset(x, y))
            }
        }

        Row(
            Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top
        ) {
            Surface(color = Color(0xDD111B30), shape = MaterialTheme.shapes.extraLarge, tonalElevation = 8.dp) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFF293A59), shape = MaterialTheme.shapes.extraLarge) {
                        val safeClassIndex = classIndex.coerceIn(0, classNames.lastIndex)
                        val heroPortrait = when (safeClassIndex) {
                            0 -> if (playerGender == "female") R.drawable.blademaster_female else R.drawable.blademaster_male
                            1 -> if (playerGender == "female") R.drawable.ironbody_female else R.drawable.ironbody_male
                            2 -> if (playerGender == "female") R.drawable.elementalist_female else R.drawable.elementalist_male
                            else -> if (playerGender == "female") R.drawable.spiritmystic_female else R.drawable.spiritmystic_male
                        }
                        Image(
                            painter = painterResource(heroPortrait),
                            contentDescription = "Персонаж " + playerNickname.ifBlank { classNames[safeClassIndex] },
                            modifier = Modifier.size(42.dp).clip(CircleShape)
                                .border(1.dp, Gold.copy(alpha = .9f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.width(9.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text("Странник Лунной Гавани", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${worldName(stage)} · ${stageName(stage)}", color = Color(0xFFE8D3A6), fontSize = 10.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            RpgIcon("critical", Modifier.size(15.dp))
                            Text("Боевая мощь  ${combatPower.coerceAtLeast(0)}", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                HomeResource("qi", "Ци", qi.toString())
                HomeResource("chest", "Камни", stones.toString())
                HomeResource("arena", "Победы", wins.toString())
            }
        }

        Surface(
            Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 76.dp).width(154.dp)
                .clickable { onBuilding("quests") },
            color = Color(0xD9122033),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0x66D9B875)),
            tonalElevation = 7.dp
        ) {
            Column(Modifier.padding(horizontal = 9.dp, vertical = 7.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("ГЛАВА ${storyChapter + 1} · СЮЖЕТ", color = Gold, fontSize = 8.sp, fontWeight = FontWeight.Black)
                Text("Тень в долине", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Победи стража · ${wins.coerceAtMost(1)}/1", color = Color(0xFFE4E8F0), fontSize = 8.sp)
                LinearProgressIndicator(
                    progress = { wins.coerceAtMost(1).toFloat() },
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = Jade, trackColor = Color(0xFF43516A)
                )
            }
        }

        HomeHotspot("Клан", "Союз и развитие", "clan", Modifier.align(Alignment.Center).offset(x = -maxWidth * .30f, y = -maxHeight * .24f)) { onNavigate(6) }
        HomeHotspot("Обитель", "Культивация · ремёсла", "abode", Modifier.align(Alignment.Center).offset(y = -maxHeight * .27f)) { onNavigate(8) }
        HomeHotspot("Башня испытаний", "Этажи и награды", "tower", Modifier.align(Alignment.Center).offset(x = maxWidth * .30f, y = -maxHeight * .22f)) { onNavigate(2) }
        HomeHotspot("Алхимия", "Пилюли и эликсиры", "alchemy", Modifier.align(Alignment.Center).offset(x = -maxWidth * .30f, y = maxHeight * .03f)) { onBuilding("alchemy") }
        HomeHotspot("Кузница", "Ковка и усиление", "forge", Modifier.align(Alignment.Center).offset(y = maxHeight * .02f)) { onBuilding("forge") }
        HomeHotspot("Арена", "Поединки и добыча", "arena", Modifier.align(Alignment.Center).offset(x = maxWidth * .30f, y = maxHeight * .04f)) { onNavigate(2) }
        HomeHotspot("Аукцион", "Торговля и редкости", "auction", Modifier.align(Alignment.Center).offset(x = -maxWidth * .30f, y = maxHeight * .29f)) { onBuilding("auction") }
        HomeHotspot("Доска заданий", "Поручения долины", "quests", Modifier.align(Alignment.Center).offset(y = maxHeight * .28f)) { onBuilding("quests") }
        HomeHotspot("Лавка", "Предметы и ресурсы", "shop", Modifier.align(Alignment.Center).offset(x = maxWidth * .30f, y = maxHeight * .29f)) { onBuilding("shop") }
        HomeHotspot("Ярмарка", "Событие долины", "quests", Modifier.align(Alignment.TopEnd).padding(top = 78.dp, end = 12.dp)) { onBuilding("event") }

        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xF20A1325))))
                .padding(horizontal = 6.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically
        ) {
            HomeNavItem("Культивация", "cultivation", 1, onNavigate)
            HomeNavItem("Персонаж", "character", 3, onNavigate)
            HomeNavItem("Снаряжение", "equipment", 4, onNavigate)
            HomeNavItem("Навыки", "skills", 5, onNavigate)
            HomeNavItem("Питомцы", "pets", 7, onNavigate)
            HomeNavItem("Инвентарь", "inventory", 4, onNavigate)
            HomeNavItem("Миры", "worlds", 9, onNavigate)
        }
    }
}

@Composable
private fun HomeResource(symbol: String, label: String, value: String) {
    Surface(color = Color(0xD9152136), shape = MaterialTheme.shapes.large) {
        Row(Modifier.padding(horizontal = 7.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            RpgIcon(symbol, Modifier.size(25.dp))
            Spacer(Modifier.width(5.dp))
            Column {
                Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(label, color = Color(0xFFBFC9D8), fontSize = 8.sp)
            }
        }
    }
}

@Composable
private fun HomeHotspot(title: String, subtitle: String, symbol: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "home-hotspot-$symbol")
    val pulse by transition.animateFloat(
        initialValue = .22f,
        targetValue = .58f,
        animationSpec = infiniteRepeatable(tween(1900), RepeatMode.Reverse),
        label = "home-hotspot-glow-$symbol"
    )
    val drift by transition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(tween(2600, easing = androidx.compose.animation.core.FastOutSlowInEasing), RepeatMode.Reverse),
        label = "home-hotspot-drift-$symbol"
    )
    val breathe by transition.animateFloat(
        initialValue = .975f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(tween(2100), RepeatMode.Reverse),
        label = "home-hotspot-breathe-$symbol"
    )
    Box(
        modifier = modifier.width(112.dp).height(126.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width * .5f, size.height * .43f)
            drawCircle(
                Brush.radialGradient(
                    listOf(
                        Color(0x6688D9C3).copy(alpha = pulse),
                        Color(0x2289C9D2).copy(alpha = pulse),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension * .62f
                ),
                radius = size.minDimension * .62f,
                center = center
            )
            drawOval(
                Brush.radialGradient(
                    listOf(Color(0x5578C9B1), Color(0x2278C9B1), Color.Transparent)
                ),
                topLeft = Offset(size.width * .12f, size.height * .66f),
                size = Size(size.width * .76f, size.height * .17f)
            )
            drawArc(
                color = Gold.copy(alpha = .22f + pulse * .18f),
                startAngle = 205f,
                sweepAngle = 130f,
                useCenter = false,
                topLeft = Offset(size.width * .18f, size.height * .09f),
                size = Size(size.width * .64f, size.height * .64f),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        Column(
            Modifier.fillMaxSize().padding(horizontal = 1.dp, vertical = 1.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(townBuildingResource(symbol)),
                contentDescription = "$title — $subtitle",
                modifier = Modifier.width(108.dp).height(88.dp).graphicsLayer {
                    translationY = drift.dp.toPx()
                    scaleX = breathe
                    scaleY = breathe
                },
                contentScale = ContentScale.Fit
            )
            Text(
                title,
                color = Color(0xFFFFE8B5),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun CultivateBottomNavigation(
    selectedMenu: Int,
    onNavigate: (Int) -> Unit,
    onMore: () -> Unit
) {
    val primary = listOf(
        Triple("Путь", "cultivation", 1),
        Triple("Бой", "arena", 2),
        Triple("Герой", "character", 3),
        Triple("Сумка", "inventory", 4)
    )
    Surface(
        color = Color(0xF20B1728),
        tonalElevation = 10.dp,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, Color(0x334E718E))
    ) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().height(66.dp).padding(horizontal = 5.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            primary.forEach { (label, icon, target) ->
                val selected = selectedMenu == target
                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.14f else 1f,
                    animationSpec = tween(220),
                    label = "bottom-nav-scale-$icon"
                )
                Column(
                    Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(13.dp))
                        .background(if (selected) Color(0xFF25443F) else Color.Transparent)
                        .clickable { onNavigate(target) }.padding(vertical = 3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(Modifier.size(29.dp), contentAlignment = Alignment.Center) {
                        if (selected) {
                            Canvas(Modifier.fillMaxSize()) {
                                drawCircle(
                                    Brush.radialGradient(listOf(Jade.copy(alpha = .34f), Color.Transparent)),
                                    radius = size.minDimension * .5f
                                )
                                drawArc(
                                    color = Gold.copy(alpha = .78f),
                                    startAngle = 205f,
                                    sweepAngle = 130f,
                                    useCenter = false,
                                    topLeft = Offset(size.width * .12f, size.height * .12f),
                                    size = Size(size.width * .76f, size.height * .76f),
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            }
                        }
                        RpgIcon(icon, Modifier.size(25.dp).graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        })
                    }
                    Text(label, color = if (selected) Gold else Color(0xFFB9C7D8), fontSize = 8.sp, fontWeight = if (selected) FontWeight.Black else FontWeight.Medium)
                }
            }
            val moreSelected = selectedMenu in listOf(5, 6, 7, 8, 9)
            val moreScale by animateFloatAsState(
                targetValue = if (moreSelected) 1.12f else 1f,
                animationSpec = tween(220),
                label = "bottom-nav-more-scale"
            )
            Column(
                Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(13.dp))
                    .background(if (moreSelected) Color(0xFF25443F) else Color.Transparent)
                    .clickable(onClick = onMore).padding(vertical = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                RpgIcon("menu", Modifier.size(25.dp).graphicsLayer {
                    scaleX = moreScale
                    scaleY = moreScale
                })
                Text("Ещё", color = if (moreSelected) Gold else Color(0xFFB9C7D8), fontSize = 8.sp, fontWeight = if (moreSelected) FontWeight.Black else FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun MoreNavigationDialog(
    selectedMenu: Int,
    stage: Int,
    onNavigate: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val destinations = listOf(
        Triple("Боевые искусства", "skills", 5),
        Triple(if (stage < 7) "Клан" else "Секта", "clan", 6),
        Triple("Духовные звери", "pets", 7),
        Triple("Личная обитель", "abode", 8),
        Triple("Экспедиции и миры", "worlds", 9)
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color(0xFF101D2D),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Gold.copy(alpha = .65f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("МИР ДАО", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Text("Дополнительные разделы", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
                destinations.forEach { (label, icon, target) ->
                    val selected = selectedMenu == target
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigate(target) },
                        color = if (selected) Color(0xFF25443F) else Color(0xFF192A3F),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (selected) Jade else Color(0x335E7891))
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                            RpgIcon(icon, Modifier.size(29.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(label, Modifier.weight(1f), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(if (selected) "ОТКРЫТО" else "›", color = if (selected) Jade else Gold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("ЗАКРЫТЬ", color = Jade) }
            }
        }
    }
}

@Composable
private fun HomeNavItem(title: String, symbol: String, target: Int, onNavigate: (Int) -> Unit) {
    val transition = rememberInfiniteTransition(label = "home-nav-$symbol")
    val breathe by transition.animateFloat(
        initialValue = .96f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500 + target * 65, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "home-nav-breathe-$symbol"
    )
    val halo by transition.animateFloat(
        initialValue = .16f,
        targetValue = .42f,
        animationSpec = infiniteRepeatable(tween(1500 + target * 65), RepeatMode.Reverse),
        label = "home-nav-halo-$symbol"
    )
    Column(
        Modifier.clickable { onNavigate(target) }.padding(horizontal = 3.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF8EDFC4).copy(alpha = halo), Color.Transparent)
                    ),
                    radius = size.minDimension * .49f
                )
                drawArc(
                    color = Gold.copy(alpha = .22f + halo * .55f),
                    startAngle = 205f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(size.width * .12f, size.height * .12f),
                    size = Size(size.width * .76f, size.height * .76f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            RpgIcon(
                symbol,
                Modifier.size(29.dp).graphicsLayer {
                    scaleX = breathe
                    scaleY = breathe
                }
            )
        }
        Text(title, color = Color(0xFFE8D9B9), fontSize = 8.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
private fun CultivationPanel(
    stage: Int, qi: Int, cost: Int, bossRank: Int, bossLevel: Int,
    classIndex: Int, playerGender: String, playerNickname: String,
    onBreakthrough: () -> Unit
) {
    val progress = (qi.toFloat() / cost.coerceAtLeast(1)).coerceIn(0f, 1f)
    val worldIndex = when { stage >= 19 -> 3; stage >= 13 -> 2; stage >= 7 -> 1; else -> 0 }
    val background = when (worldIndex) { 0 -> R.drawable.world_mortal_bg; 1 -> R.drawable.world_spiritual_bg; 2 -> R.drawable.world_immortal_bg; else -> R.drawable.world_chaos_bg }
    val transition = rememberInfiniteTransition(label = "cultivation-sanctuary")
    val auraFrames = remember(worldIndex) {
        if (worldIndex >= 2) listOf(R.drawable.cultivation_aura_void_00, R.drawable.cultivation_aura_void_01, R.drawable.cultivation_aura_void_02, R.drawable.cultivation_aura_void_03, R.drawable.cultivation_aura_void_04, R.drawable.cultivation_aura_void_05, R.drawable.cultivation_aura_void_06, R.drawable.cultivation_aura_void_07, R.drawable.cultivation_aura_void_08, R.drawable.cultivation_aura_void_09, R.drawable.cultivation_aura_void_10, R.drawable.cultivation_aura_void_11) else listOf(R.drawable.cultivation_aura_gold_00, R.drawable.cultivation_aura_gold_01, R.drawable.cultivation_aura_gold_02, R.drawable.cultivation_aura_gold_03, R.drawable.cultivation_aura_gold_04, R.drawable.cultivation_aura_gold_05, R.drawable.cultivation_aura_gold_06, R.drawable.cultivation_aura_gold_07, R.drawable.cultivation_aura_gold_08, R.drawable.cultivation_aura_gold_09, R.drawable.cultivation_aura_gold_10, R.drawable.cultivation_aura_gold_11)
    }
    var auraFrame by remember(worldIndex) { mutableStateOf(0) }
    LaunchedEffect(worldIndex) {
        while (true) {
            delay(95L)
            auraFrame = (auraFrame + 1) % auraFrames.size
        }
    }
    var previousStage by remember { mutableStateOf(stage) }
    var breakthroughVisible by remember { mutableStateOf(false) }
    val breakthroughPulse = remember { Animatable(0f) }
    LaunchedEffect(stage) {
        if (stage > previousStage) {
            previousStage = stage
            breakthroughVisible = true
            breakthroughPulse.snapTo(1f)
            breakthroughPulse.animateTo(0f, tween(1600))
            delay(250L)
            breakthroughVisible = false
        } else {
            previousStage = stage
        }
    }
    val pulse by transition.animateFloat(.82f, 1.12f, infiniteRepeatable(tween(2100), RepeatMode.Reverse), label = "core-pulse")
    val rotation by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(26000), RepeatMode.Restart), label = "meridian-rotation")
    val flow by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(3200), RepeatMode.Restart), label = "qi-flow")
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.fillMaxWidth().height(286.dp), contentAlignment = Alignment.Center) {
            Image(painter = painterResource(background), contentDescription = "Пейзаж " + worldName(stage), modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xB9081423), Color(0x54101B2D), Color(0xE9081423)))))
            Image(
                painter = painterResource(auraFrames[auraFrame]),
                contentDescription = null,
                modifier = Modifier.align(Alignment.Center).size(248.dp).graphicsLayer(alpha = .78f),
                contentScale = ContentScale.Fit
            )
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width * .5f, size.height * .53f)
                val radius = size.minDimension * .34f
                drawCircle(Brush.radialGradient(listOf(Color(0x6659D8B4), Color(0x223D9DAB), Color.Transparent), center, radius * 1.8f), radius * 1.8f, center)
                drawCircle(Color(0x99E5BD72), radius, center, style = Stroke(1.4.dp.toPx()))
                drawCircle(Color(0x9978C6A3), radius * .82f, center, style = Stroke(1.dp.toPx()))
                drawCircle(Color(0x66E5BD72), radius * .61f, center, style = Stroke(1.dp.toPx()))
                drawCircle(Color(0x6678C6A3), radius * .43f, center, style = Stroke(1.dp.toPx()))
                for (i in 0..7) {
                    val angle = (i * 45.0 + rotation) * (Math.PI / 180.0)
                    val inner = radius * .68f
                    val outer = radius * 1.04f
                    val p1 = Offset(center.x + kotlin.math.cos(angle).toFloat() * inner, center.y + kotlin.math.sin(angle).toFloat() * inner)
                    val p2 = Offset(center.x + kotlin.math.cos(angle).toFloat() * outer, center.y + kotlin.math.sin(angle).toFloat() * outer)
                    drawLine(Color(0x8878C6A3), p1, p2, 1.dp.toPx())
                    drawCircle(Color(0xCCFFE3A0), 2.2.dp.toPx(), p2)
                }
                drawArc(Color(0xFFE5BD72), -90f, 360f * progress, false, Offset(center.x - radius, center.y - radius), Size(radius * 2f, radius * 2f), style = Stroke(4.dp.toPx()))
                for (i in 0..11) {
                    val phase = (i / 12f + flow) % 1f
                    val angle = (phase * 360f - 90f) * (Math.PI / 180.0)
                    val orbit = radius * (.48f + (i % 3) * .12f)
                    val point = Offset(center.x + kotlin.math.cos(angle).toFloat() * orbit, center.y + kotlin.math.sin(angle).toFloat() * orbit)
                    drawCircle(Color(0x9978E3C0).copy(alpha = .35f + .5f * pulse), (1.5f + (i % 3) * .6f).dp.toPx(), point)
                }
                drawCircle(Color(0x55FFE3A0), radius * .13f * pulse, center)
            }
            Column(Modifier.align(Alignment.TopCenter).padding(top = 13.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(worldName(stage).uppercase(), color = Color(0xFFFFD995), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Text("СВЯЩЕННОЕ МЕСТО КУЛЬТИВАЦИИ", color = Color(0xFFD9E7E8), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
            }
            val safeClassIndex = classIndex.coerceIn(0, classNames.lastIndex)
            val cultivatorPortrait = when (safeClassIndex) {
                0 -> if (playerGender == "female") R.drawable.blademaster_female else R.drawable.blademaster_male
                1 -> if (playerGender == "female") R.drawable.ironbody_female else R.drawable.ironbody_male
                2 -> if (playerGender == "female") R.drawable.elementalist_female else R.drawable.elementalist_male
                else -> if (playerGender == "female") R.drawable.spiritmystic_female else R.drawable.spiritmystic_male
            }
            Image(
                painter = painterResource(cultivatorPortrait),
                contentDescription = "Медитирующий персонаж " + playerNickname.ifBlank { classNames[safeClassIndex] },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 57.dp)
                    .width(105.dp).height(166.dp).graphicsLayer(alpha = .94f),
                contentScale = ContentScale.Fit
            )
            Box(Modifier.align(Alignment.Center).size(116.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(Brush.radialGradient(listOf(Color(0xAA79E2C0), Color(0x4478C6A3), Color.Transparent)), size.minDimension * .49f * pulse, c)
                    drawCircle(Color(0xCCFFE3A0).copy(alpha = .75f), size.minDimension * .39f, c, style = Stroke(1.5.dp.toPx()))
                    drawCircle(Color(0x8878C6A3), size.minDimension * .31f, c, style = Stroke(1.dp.toPx()))
                }
                RpgIcon("lotus", Modifier.size(82.dp))
            }
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("СТУПЕНЬ $stage", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
                        Text(stageName(stage), color = Color(0xFF9CE0C5), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("$qi / $cost Ци", color = Color(0xFFFFE5AE), fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(7.dp), color = Color(0xFF8BE0BC), trackColor = Color(0x88445468))
            }
            if (breakthroughVisible) {
                val flashAlpha = breakthroughPulse.value.coerceIn(0f, 1f)
                Box(Modifier.fillMaxSize().background(Color(0xFFBDEFD9).copy(alpha = flashAlpha * .18f)))
                Image(
                    painter = painterResource(R.drawable.cultivation_ascension_rays),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(230.dp)
                        .graphicsLayer(alpha = flashAlpha, scaleX = .82f + .28f * flashAlpha, scaleY = .82f + .28f * flashAlpha),
                    contentScale = ContentScale.Fit
                )
                Image(
                    painter = painterResource(R.drawable.cultivation_breakthrough_ascension),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(172.dp)
                        .graphicsLayer(alpha = flashAlpha, scaleX = .76f + .24f * flashAlpha, scaleY = .76f + .24f * flashAlpha),
                    contentScale = ContentScale.Fit
                )
                Image(
                    painter = painterResource(R.drawable.cultivation_event_breakthrough),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 38.dp)
                        .width(244.dp).height(76.dp).graphicsLayer(alpha = flashAlpha),
                    contentScale = ContentScale.Fit
                )
                Surface(
                    modifier = Modifier.align(Alignment.Center).padding(top = 112.dp),
                    color = Color(0xEE10243A),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5BD72).copy(alpha = flashAlpha)),
                    shadowElevation = 8.dp
                ) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ПРОРЫВ СВЕРШЁН", color = Color(0xFFFFD995), fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Text("${worldName(stage)} · ${stageName(stage)}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xE9192A3D), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, Color(0x5578C6A3)), tonalElevation = 5.dp) {
            Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        RpgIcon("lotus", Modifier.size(25.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text("ВНУТРЕННЕЕ ЯДРО", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Text("Поток духовной энергии", color = Color(0xFFDCE7F0), fontSize = 10.sp)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${(progress * 100).toInt()}%", color = Jade, fontSize = 17.sp, fontWeight = FontWeight.Black)
                        Text("НАКОПЛЕНИЕ", color = Color(0xFF9EADBD), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RpgIcon("tower", Modifier.size(23.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text("Испытание стража", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Требуется ранг $bossRank · Побеждён ранг $bossLevel", color = if (bossLevel >= bossRank) Jade else Color(0xFFFFC978), fontSize = 9.sp)
                    }
                    RpgIcon(if (bossLevel >= bossRank) "check" else "lock", Modifier.size(19.dp))
                }
                InventoryActionButton(label = if (qi < cost) "НАКОПИТЬ ЕЩЁ ${cost - qi} ЦИ" else if (bossLevel < bossRank) "ОДОЛЕТЬ СТРАЖА · РАНГ $bossRank" else "СОВЕРШИТЬ ПРОРЫВ", selected = false, enabled = qi >= cost && bossLevel >= bossRank, onClick = onBreakthrough)
            }
        }
    }
}
@Composable
private fun DailyTaskCard(
    title: String,
    detail: String,
    reward: String,
    iconKey: String,
    claimed: Boolean,
    available: Boolean,
    onClaim: () -> Unit
) {
    Surface(
        color = if (claimed) Color(0xCC1F403B) else Color(0xCC1B2D45),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 4.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RpgIcon(iconKey, Modifier.size(48.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = if (claimed) Jade else Gold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                Text(detail, color = Color(0xFFD1DCEB), fontSize = 9.sp)
                Text(reward, color = Color(0xFF9BDCC5), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Box(Modifier.width(124.dp)) {
                InventoryActionButton(
                    label = if (claimed) "ПОЛУЧЕНО" else if (available) "ЗАБРАТЬ" else "В ПРОЦЕССЕ",
                    selected = claimed,
                    enabled = available,
                    onClick = onClaim
                )
            }
        }
    }
}

@Composable
private fun CharacterArmoryScreen(
    stage: Int,
    playerNickname: String,
    playerGender: String,
    classIndex: Int,
    attack: Int,
    defense: Int,
    currentHp: Int,
    maxHp: Int,
    equippedWeapon: WeaponDefinition,
    equippedGear: List<GearDefinition>,
    setBonus: GearSetBonus,
    onOpenWeapons: () -> Unit,
    onOpenGear: () -> Unit,
    onSelectClass: (Int) -> Unit
) {
    val selected = classIndex.coerceIn(0, classNames.lastIndex)
    val profile = classCombatProfiles[selected]
    val portrait = when (selected) {
        0 -> R.drawable.hero_full_blademaster
        1 -> R.drawable.hero_full_ironbody
        2 -> R.drawable.hero_full_elementalist
        else -> R.drawable.hero_full_spiritmystic
    }
    val gearBySlot = equippedGear.associateBy { it.slot }
    val hpProgress = if (maxHp <= 0) 0f else (currentHp.toFloat() / maxHp).coerceIn(0f, 1f)
    val aura = rememberInfiniteTransition(label = "character-armory-aura")
    val auraPulse by aura.animateFloat(
        initialValue = .88f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1900), RepeatMode.Reverse),
        label = "character-armory-pulse"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            color = Color(0xEE17263D),
            shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(1.5.dp, Gold.copy(alpha = .82f)),
            tonalElevation = 10.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RpgIcon("character", Modifier.size(34.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("ЗАЛ ВОСХОЖДЕНИЯ", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    Text(classNames[selected], color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text("${worldName(stage)} · ${stageName(stage)} · ступень $stage", color = Jade, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Surface(
                    color = Color(0xFF594329),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, Color(0xFFFFD98C))
                ) {
                    Text("БОЕВОЙ ДУХ", Modifier.padding(horizontal = 8.dp, vertical = 6.dp), color = Color(0xFFFFE5A5), fontSize = 7.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Surface(
            color = Color(0xE9101C30),
            shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(1.dp, Color(0x887E9BB8)),
            tonalElevation = 7.dp
        ) {
            Row(
                Modifier.fillMaxWidth().height(258.dp).padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    Modifier.weight(.25f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    CharacterEquipmentSlot(
                        "ОРУЖИЕ",
                        equippedWeapon.name,
                        weaponDrawable(equippedWeapon.type),
                        weaponRarityColor(equippedWeapon.rarity),
                        Modifier.weight(1f),
                        rarityFrameResource(equippedWeapon.rarity),
                        onOpenWeapons
                    )
                    CharacterEquipmentSlot(
                        "ШЛЕМ",
                        gearBySlot[GearSlot.HELMET]?.name ?: "Пусто",
                        gearDrawable(GearSlot.HELMET),
                        gearBySlot[GearSlot.HELMET]?.let { weaponRarityColor(it.rarity) } ?: Color(0xFF66758B),
                        Modifier.weight(1f),
                        gearBySlot[GearSlot.HELMET]?.let { rarityFrameResource(it.rarity) } ?: R.drawable.ui_rarity_frame_01,
                        onOpenGear
                    )
                    CharacterEquipmentSlot(
                        "БРОНЯ",
                        gearBySlot[GearSlot.ARMOR]?.name ?: "Пусто",
                        gearDrawable(GearSlot.ARMOR),
                        gearBySlot[GearSlot.ARMOR]?.let { weaponRarityColor(it.rarity) } ?: Color(0xFF66758B),
                        Modifier.weight(1f),
                        gearBySlot[GearSlot.ARMOR]?.let { rarityFrameResource(it.rarity) } ?: R.drawable.ui_rarity_frame_01,
                        onOpenGear
                    )
                }

                Box(
                    Modifier.weight(.50f).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.character_halo_frame),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize().graphicsLayer {
                            scaleX = auraPulse
                            scaleY = auraPulse
                            alpha = .72f
                        }
                    )
                    Surface(
                        modifier = Modifier.fillMaxHeight(.94f).aspectRatio(2f / 3f),
                        color = Color(0xFF111A2B),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, Gold.copy(alpha = .88f)),
                        tonalElevation = 8.dp,
                        shadowElevation = 7.dp
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(portrait),
                                contentDescription = "Полноценный образ класса: ${classNames[selected]}",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize().padding(horizontal = 3.dp, vertical = 2.dp)
                            )
                            Box(
                                Modifier.align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color(0xDD081321))
                                        )
                                    )
                            )
                            Surface(
                                modifier = Modifier.align(Alignment.TopCenter).padding(top = 7.dp),
                                color = Color(0xD90B1728),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, Gold.copy(alpha = .72f))
                            ) {
                                Column(
                                    Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    Text(
                                        playerNickname.ifBlank { "БЕЗЫМЯННЫЙ КУЛЬТИВАТОР" },
                                        color = Color(0xFFFFE8B4),
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1
                                    )
                                    Text(
                                        classNames[selected].uppercase(),
                                        color = Jade,
                                        fontSize = 6.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        color = Color(0xF01A2A40),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Gold.copy(alpha = .92f)),
                        shadowElevation = 5.dp
                    ) {
                        Text(
                            "УР. $stage",
                            Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                            color = Color(0xFFFFE4A0),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Column(
                    Modifier.weight(.25f).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    CharacterEquipmentSlot(
                        "КОЛЬЦО",
                        gearBySlot[GearSlot.RING]?.name ?: "Пусто",
                        gearDrawable(GearSlot.RING),
                        gearBySlot[GearSlot.RING]?.let { weaponRarityColor(it.rarity) } ?: Color(0xFF66758B),
                        Modifier.fillMaxWidth().weight(1f),
                        gearBySlot[GearSlot.RING]?.let { rarityFrameResource(it.rarity) } ?: R.drawable.ui_rarity_frame_01,
                        onOpenGear
                    )
                    Spacer(Modifier.weight(.35f))
                    CharacterEquipmentSlot(
                        "АРТЕФАКТ",
                        gearBySlot[GearSlot.ARTIFACT]?.name ?: "Пусто",
                        gearDrawable(GearSlot.ARTIFACT),
                        gearBySlot[GearSlot.ARTIFACT]?.let { weaponRarityColor(it.rarity) } ?: Color(0xFF66758B),
                        Modifier.fillMaxWidth().weight(1f),
                        gearBySlot[GearSlot.ARTIFACT]?.let { rarityFrameResource(it.rarity) } ?: R.drawable.ui_rarity_frame_01,
                        onOpenGear
                    )
                }
            }
        }

        Surface(
            color = Color(0xD91B2B42),
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.dp, Color(0x887F9BB8))
        ) {
            Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RpgIcon("health", Modifier.size(20.dp))
                    Text("ЖИЗНЕННАЯ СИЛА", color = Color(0xFFFFD99A), fontSize = 8.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.weight(1f))
                    Text("$currentHp / $maxHp", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { hpProgress },
                    modifier = Modifier.fillMaxWidth().height(7.dp),
                    color = if (hpProgress < .3f) Color(0xFFE17B69) else Jade,
                    trackColor = Color(0xFF35445A)
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    CharacterStatCard("weapon", "АТАКА", attack.toString(), Modifier.weight(1f))
                    CharacterStatCard("shield", "ЗАЩИТА", defense.toString(), Modifier.weight(1f))
                    CharacterStatCard("health", "ЗАПАС HP", maxHp.toString(), Modifier.weight(1f))
                    CharacterStatCard("critical", "КРИТ", "${profile.criticalChance}%", Modifier.weight(1f))
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenGear),
            color = Color(0xE522344A),
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.dp, Gold.copy(alpha = .62f))
        ) {
            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                RpgIcon("equipment", Modifier.size(32.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("НАСЛЕДИЕ СНАРЯЖЕНИЯ", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    Text(
                        setBonus.description.ifBlank { "Собери части одного набора, чтобы пробудить общий эффект" },
                        color = Color(0xFFD5DFEC),
                        fontSize = 9.sp,
                        maxLines = 2
                    )
                    Text(
                        "Бонусы комплекта · атака +${setBonus.attack} · защита +${setBonus.defense} · здоровье +${setBonus.hp}",
                        color = Jade,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                RpgIcon("chevron-right", Modifier.size(22.dp))
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xE522344A),
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.dp, Gold.copy(alpha = .62f))
        ) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("ПУТЬ ЗАКРЕПЛЁН", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                Text(playerNickname, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(classNames[selected] + " · " + if (playerGender == "female") "Женский облик" else "Мужской облик", color = Jade, fontSize = 10.sp)
                Text("Класс и облик выбираются при создании персонажа и не меняются в пути.", color = Color(0xFFB7C5D6), fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun CharacterEquipmentOverlay(
    weapon: WeaponDefinition,
    gearBySlot: Map<GearSlot, GearDefinition>,
    modifier: Modifier = Modifier
) {
    val armorColor = gearBySlot[GearSlot.ARMOR]?.let { weaponRarityColor(it.rarity) }
    val helmetColor = gearBySlot[GearSlot.HELMET]?.let { weaponRarityColor(it.rarity) }
    val ringColor = gearBySlot[GearSlot.RING]?.let { weaponRarityColor(it.rarity) }
    val artifactColor = gearBySlot[GearSlot.ARTIFACT]?.let { weaponRarityColor(it.rarity) }
    val weaponColor = weaponRarityColor(weapon.rarity)
    val transition = rememberInfiniteTransition(label = "equipped-relic-glow")
    val glow by transition.animateFloat(
        initialValue = .48f,
        targetValue = .92f,
        animationSpec = infiniteRepeatable(tween(1250), RepeatMode.Reverse),
        label = "equipped-relic-pulse"
    )

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        fun point(x: Float, y: Float) = Offset(w * x, h * y)
        val thin = 1.15.dp.toPx()
        val bold = 2.1.dp.toPx()
        val gold = Color(0xFFFFE2A0)

        // Оружие рисуется отдельным слоем: его форма и цвет зависят от реально надетого предмета.
        when (weapon.type) {
            0 -> {
                val blade = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * .805f, h * .245f)
                    lineTo(w * .875f, h * .285f)
                    lineTo(w * .825f, h * .665f)
                    lineTo(w * .785f, h * .625f)
                    close()
                }
                drawPath(blade, Brush.linearGradient(listOf(Color.White, weaponColor, Color(0xFF34445B)), point(.79f,.25f), point(.87f,.68f)))
                drawPath(blade, gold.copy(alpha = glow), style = androidx.compose.ui.graphics.drawscope.Stroke(thin))
                drawLine(gold, point(.765f,.63f), point(.855f,.70f), bold)
                drawLine(Color(0xFF9A633E), point(.815f,.67f), point(.765f,.79f), bold * 2.2f)
            }
            1 -> {
                drawLine(Color(0xFF9D7145), point(.845f,.28f), point(.805f,.78f), bold * 1.5f)
                val spear = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w*.845f,h*.20f); lineTo(w*.895f,h*.30f); lineTo(w*.84f,h*.39f); lineTo(w*.82f,h*.31f); close()
                }
                drawPath(spear, Brush.linearGradient(listOf(Color.White, weaponColor, Color(0xFF76513B)), point(.82f,.2f), point(.9f,.4f)))
                drawPath(spear, gold.copy(alpha = glow), style = androidx.compose.ui.graphics.drawscope.Stroke(thin))
                drawLine(gold, point(.79f,.66f), point(.86f,.68f), bold)
            }
            2 -> {
                drawLine(Color(0xFF9C704A), point(.83f,.30f), point(.79f,.79f), bold * 2.2f)
                drawCircle(weaponColor.copy(alpha = .24f * glow), w*.07f, point(.84f,.25f))
                drawCircle(weaponColor, w*.045f, point(.84f,.25f))
                drawCircle(gold, w*.045f, point(.84f,.25f), style = androidx.compose.ui.graphics.drawscope.Stroke(bold))
                drawLine(gold, point(.79f,.79f), point(.86f,.79f), bold)
            }
            else -> {
                val fist = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w*.77f,h*.48f); lineTo(w*.88f,h*.45f); lineTo(w*.94f,h*.53f)
                    lineTo(w*.91f,h*.68f); lineTo(w*.82f,h*.72f); lineTo(w*.76f,h*.63f); close()
                }
                drawPath(fist, Brush.linearGradient(listOf(weaponColor, Color(0xFF263B50), weaponColor.copy(alpha=.8f)), point(.77f,.46f), point(.93f,.7f)))
                drawPath(fist, gold.copy(alpha=glow), style=androidx.compose.ui.graphics.drawscope.Stroke(bold))
                drawCircle(Color.White.copy(alpha=.7f*glow), w*.018f, point(.85f,.55f))
            }
        }

        // Нагрудник и наплечники появляются только при наличии надетой брони.
        armorColor?.let { color ->
            val leftShoulder = androidx.compose.ui.graphics.Path().apply {
                moveTo(w*.27f,h*.365f); lineTo(w*.405f,h*.355f); lineTo(w*.445f,h*.455f)
                lineTo(w*.385f,h*.515f); lineTo(w*.295f,h*.475f); close()
            }
            val rightShoulder = androidx.compose.ui.graphics.Path().apply {
                moveTo(w*.595f,h*.355f); lineTo(w*.73f,h*.365f); lineTo(w*.705f,h*.475f)
                lineTo(w*.615f,h*.515f); lineTo(w*.555f,h*.455f); close()
            }
            val chest = androidx.compose.ui.graphics.Path().apply {
                moveTo(w*.385f,h*.405f); lineTo(w*.5f,h*.445f); lineTo(w*.615f,h*.405f)
                lineTo(w*.675f,h*.565f); lineTo(w*.61f,h*.665f); lineTo(w*.5f,h*.705f)
                lineTo(w*.39f,h*.665f); lineTo(w*.325f,h*.565f); close()
            }
            val armorBrush = Brush.linearGradient(
                listOf(color.copy(alpha=.92f), Color(0xFF18283B).copy(alpha=.96f), color.copy(alpha=.78f)),
                point(.30f,.38f), point(.70f,.68f)
            )
            drawPath(leftShoulder, armorBrush)
            drawPath(rightShoulder, armorBrush)
            drawPath(chest, armorBrush)
            drawPath(leftShoulder, gold.copy(alpha=glow), style=androidx.compose.ui.graphics.drawscope.Stroke(bold))
            drawPath(rightShoulder, gold.copy(alpha=glow), style=androidx.compose.ui.graphics.drawscope.Stroke(bold))
            drawPath(chest, gold.copy(alpha=glow), style=androidx.compose.ui.graphics.drawscope.Stroke(bold))
            drawLine(gold.copy(alpha=.8f), point(.5f,.46f), point(.5f,.66f), thin)
            drawCircle(Color(0xFF10243A), w*.047f, point(.5f,.535f))
            drawCircle(color, w*.033f, point(.5f,.535f))
            drawCircle(Color.White.copy(alpha=.75f*glow), w*.012f, point(.49f,.52f))
        }

        // Шлем меняет силуэт над головой, не перекрывая лицо персонажа.
        helmetColor?.let { color ->
            val crown = androidx.compose.ui.graphics.Path().apply {
                moveTo(w*.365f,h*.165f); lineTo(w*.405f,h*.055f); lineTo(w*.465f,h*.105f)
                lineTo(w*.5f,h*.025f); lineTo(w*.535f,h*.105f); lineTo(w*.595f,h*.055f)
                lineTo(w*.635f,h*.165f); lineTo(w*.59f,h*.205f); lineTo(w*.41f,h*.205f); close()
            }
            drawPath(crown, Brush.linearGradient(listOf(color, Color(0xFF19263A), color.copy(alpha=.88f)), point(.36f,.05f), point(.64f,.21f)))
            drawPath(crown, gold.copy(alpha=glow), style=androidx.compose.ui.graphics.drawscope.Stroke(bold))
            drawCircle(color, w*.027f, point(.5f,.145f))
            drawCircle(gold, w*.027f, point(.5f,.145f), style=androidx.compose.ui.graphics.drawscope.Stroke(thin))
        }

        // Кольцо и артефакт показываются как отдельные магические детали надетых предметов.
        ringColor?.let { color ->
            drawCircle(color.copy(alpha=.18f*glow), w*.055f, point(.295f,.585f))
            drawCircle(color, w*.035f, point(.295f,.585f), style=androidx.compose.ui.graphics.drawscope.Stroke(bold))
            drawCircle(gold.copy(alpha=glow), w*.012f, point(.295f,.585f))
        }
        artifactColor?.let { color ->
            val center = point(.69f,.425f)
            drawCircle(color.copy(alpha=.17f*glow), w*.075f, center)
            drawCircle(color.copy(alpha=.9f), w*.055f, center, style=androidx.compose.ui.graphics.drawscope.Stroke(bold))
            drawCircle(gold.copy(alpha=glow), w*.032f, center, style=androidx.compose.ui.graphics.drawscope.Stroke(thin))
            drawLine(gold.copy(alpha=glow), point(.69f,.34f), point(.69f,.51f), thin)
            drawLine(gold.copy(alpha=glow), point(.625f,.425f), point(.755f,.425f), thin)
            drawCircle(Color.White.copy(alpha=.85f*glow), w*.012f, center)
        }
    }
}

@Composable
private fun CharacterEquipmentSlot(
    title: String,
    itemName: String,
    iconRes: Int,
    rarityColor: Color,
    modifier: Modifier = Modifier,
    frameResource: Int = R.drawable.ui_rarity_frame_01,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        color = Color(0xE91A2A40),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, rarityColor.copy(alpha = .82f)),
        tonalElevation = 4.dp,
        shadowElevation = 3.dp
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 2.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(39.dp)
            )
            Image(
                painter = painterResource(frameResource),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(43.dp).offset(y = (-38).dp)
            )
            Text(title, color = Gold, fontSize = 8.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(
                itemName,
                color = if (itemName == "Пусто") Color(0xFF9BA9BB) else rarityColor,
                fontSize = 6.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                lineHeight = 8.sp
            )
        }
    }
}

@Composable
private fun CharacterStatCard(icon: String, label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFF14243A),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, Color(0x557D9AB7))
    ) {
        Column(
            Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            RpgIcon(icon, Modifier.size(20.dp))
            Text(label, color = Color(0xFFB8C6D8), fontSize = 6.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(value, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
    }
}

@Composable
private fun CharacterClassTile(
    name: String,
    portraitRes: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.width(150.dp).clickable(onClick = onClick),
        color = if (selected) Color(0xE13D4C5A) else Color(0xD916263B),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Gold else Color(0x665E7897)),
        tonalElevation = if (selected) 8.dp else 2.dp
    ) {
        Column(
            Modifier.padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Image(
                painter = painterResource(portraitRes),
                contentDescription = name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().height(168.dp)
            )
            Text(
                name,
                color = if (selected) Color(0xFFFFE5A5) else Color(0xFFD5DFEC),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                minLines = 2
            )
            Text(
                if (selected) "ТЕКУЩИЙ ПУТЬ" else "ВЫБРАТЬ",
                color = if (selected) Jade else Color(0xFFC5A46B),
                fontSize = 7.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun InventoryActionButton(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().heightIn(min = 36.dp).clickable(enabled = enabled, onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = when {
            !enabled -> Color(0xFF293140)
            selected -> Color(0xFF315C51)
            else -> Color(0xFF2B4261)
        },
        tonalElevation = 4.dp
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RpgIcon(if (selected) "check" else if (enabled) "plus" else "lock", Modifier.size(24.dp))
            Spacer(Modifier.width(5.dp))
            Text(label, color = if (enabled) Color(0xFFFFE7B0) else Color(0xFF9DA9BA), fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
    }
}

private fun weaponDrawable(type: Int): Int = when (type) {
    0 -> R.drawable.relic_sword
    1 -> R.drawable.relic_spear
    2 -> R.drawable.relic_staff
    else -> R.drawable.relic_gauntlet
}

private fun gearDrawable(slot: GearSlot): Int = when (slot) {
    GearSlot.HELMET -> R.drawable.relic_helmet
    GearSlot.ARMOR -> R.drawable.relic_armor
    GearSlot.RING -> R.drawable.relic_ring
    GearSlot.ARTIFACT -> R.drawable.relic_artifact
}

@Composable
private fun RelicArtworkIcon(
    resourceId: Int,
    rarityColor: Color,
    modifier: Modifier = Modifier,
    frameResource: Int = R.drawable.ui_rarity_frame_02
) {
    val frameTransition = rememberInfiniteTransition(label = "relic-rarity-frame")
    val frameGlow by frameTransition.animateFloat(
        initialValue = .72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1900), RepeatMode.Reverse),
        label = "relic-rarity-frame-glow"
    )
    Surface(
        modifier = modifier,
        color = Color(0xFF101A2B),
        shape = CircleShape,
        border = BorderStroke(2.dp, rarityColor.copy(alpha = .58f + frameGlow * .38f)),
        tonalElevation = 7.dp,
        shadowElevation = 5.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(rarityColor.copy(alpha = .40f), Color(0xFF17243A), Color(0xFF09111F)),
                        center = Offset(size.width * .5f, size.height * .44f)
                    )
                )
                drawCircle(Gold.copy(alpha = .48f + frameGlow * .34f), radius = size.minDimension * .43f, style = Stroke(width = 1.dp.toPx()))
                drawCircle(rarityColor.copy(alpha = .34f + frameGlow * .38f), radius = size.minDimension * .34f, style = Stroke(width = 1.dp.toPx()))
            }
            Image(
                painter = painterResource(resourceId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(8.dp),
                contentScale = ContentScale.Fit
            )
            Image(
                painter = painterResource(frameResource),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(1.dp).graphicsLayer {
                    alpha = .86f + frameGlow * .14f
                },
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun BattleLootReveal(
    payload: String,
    onDismiss: () -> Unit,
    onOpenItem: (LootRevealItem) -> Unit
) {
    val items = remember(payload) { decodeLootReveal(payload) }
    if (items.isEmpty()) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xF0142238),
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.5.dp, Gold.copy(alpha = .82f)),
        tonalElevation = 9.dp,
        shadowElevation = 7.dp
    ) {
        Column(
            Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RelicArtworkIcon(R.drawable.relic_artifact, Gold, Modifier.size(42.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("СОКРОВИЩА ПОБЕДЫ", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text("Награды уже зачислены. Нажми на предмет, чтобы открыть его раздел.", color = Color(0xFFD5E0ED), fontSize = 8.sp, maxLines = 2)
                }
                Surface(
                    color = Color(0xFF24483F),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Jade.copy(alpha = .8f))
                ) {
                    Text("✓", Modifier.padding(horizontal = 8.dp, vertical = 5.dp), color = Jade, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                items.forEach { item ->
                    LootRevealTile(item = item, onClick = { onOpenItem(item) })
                }
            }
            InventoryActionButton(
                label = "ЗАКРЫТЬ СОКРОВИЩНИЦУ",
                selected = true,
                enabled = true,
                onClick = onDismiss
            )
        }
    }
}

@Composable
private fun LootRevealTile(item: LootRevealItem, onClick: () -> Unit) {
    val rarityColor = lootRarityColor(item.rarity)
    Surface(
        modifier = Modifier.width(104.dp).height(126.dp).clickable(onClick = onClick),
        color = Color(0xFF13233A),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.5.dp, rarityColor.copy(alpha = .9f)),
        tonalElevation = 4.dp
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LootRevealIcon(item, rarityColor)
            Text(
                item.title,
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                minLines = 2,
                lineHeight = 10.sp
            )
            Text(
                item.detail,
                color = rarityColor,
                fontSize = 7.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LootRevealIcon(item: LootRevealItem, rarityColor: Color) {
    val resource = when (item.kind) {
        "weapon" -> weaponDrawable(item.key.toIntOrNull() ?: 0)
        "gear" -> gearDrawable(GearSlot.values().firstOrNull { it.name == item.key } ?: GearSlot.ARTIFACT)
        "pet" -> petPortraitResource(item.key)
        else -> null
    }
    if (resource != null) {
        RelicArtworkIcon(resource, rarityColor, Modifier.size(48.dp))
    } else {
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    Brush.radialGradient(listOf(rarityColor.copy(alpha = .42f), Color(0xFF17243A), Color(0xFF09111F)))
                )
                drawCircle(rarityColor.copy(alpha = .75f), size.minDimension * .44f, style = Stroke(width = 1.dp.toPx()))
            }
            RpgIcon(item.key, Modifier.fillMaxSize().padding(6.dp))
        }
    }
}

@Composable
private fun TreasuryVaultScreen(
    stage: Int,
    classIndex: Int,
    weapons: List<WeaponDefinition>,
    gear: List<GearDefinition>,
    equippedWeapon: WeaponDefinition,
    equippedGear: List<GearDefinition>,
    setBonus: GearSetBonus,
    filter: Int,
    onFilterChange: (Int) -> Unit,
    onEquipWeapon: (WeaponDefinition) -> Unit,
    onEquipGear: (GearDefinition) -> Unit
) {
    val visibleWeapons = if (filter == 2) emptyList() else weapons.sortedByDescending { weaponPowerScore(it) }
    val visibleGear = if (filter == 1) emptyList() else gear.sortedByDescending { gearPowerScore(it) }
    var selectedWeapon by remember { mutableStateOf<WeaponDefinition?>(null) }
    var selectedGear by remember { mutableStateOf<GearDefinition?>(null) }
    val equippedGearBySlot = equippedGear.associateBy { it.slot }
    val nextWeaponStage = listOf(1, 4, 7, 10, 13, 16).firstOrNull { it > stage }
    val totalItems = visibleWeapons.size + visibleGear.size

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TreasuryVaultHero(totalItems = weapons.size + gear.size, equippedItems = 1 + equippedGear.size, classIndex = classIndex)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            InventoryCategoryTab("ВСЁ · ${weapons.size + gear.size}", "inventory", filter == 0, Modifier.weight(1f)) { onFilterChange(0) }
            InventoryCategoryTab("ОРУЖИЕ · ${weapons.size}", "weapon", filter == 1, Modifier.weight(1f)) { onFilterChange(1) }
            InventoryCategoryTab("БРОНЯ · ${gear.size}", "equipment", filter == 2, Modifier.weight(1f)) { onFilterChange(2) }
        }
        InventoryLoadoutRow(weapon = equippedWeapon, equippedGear = equippedGear)
        if (totalItems == 0) {
            Surface(Modifier.fillMaxWidth(), color = Color(0xDD17263A), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Gold.copy(alpha = .42f))) {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Image(painterResource(R.drawable.treasury_vault_sigil), null, Modifier.size(62.dp))
                    Text("СОКРОВИЩНИЦА ПОКА ПУСТА", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text("Побеждай стражей и исследуй миры, чтобы находить новые реликвии.", color = Color(0xFFC5D1DF), fontSize = 9.sp)
                }
            }
        }
        if (visibleWeapons.isNotEmpty()) {
            InventorySectionHeading("Оружейная галерея", "${visibleWeapons.size} предметов · нажми для подробностей", "weapon")
            visibleWeapons.chunked(4).forEach { rowItems ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rowItems.forEach { item ->
                        val equipped = encodeWeaponInstance(item) == encodeWeaponInstance(equippedWeapon)
                        TreasuryRelicTile(
                            modifier = Modifier.weight(1f),
                            title = item.name,
                            subtitle = "${item.rarity.title} · ${weaponPowerScore(item)}",
                            resourceId = weaponDrawable(item.type),
                            rarityColor = weaponRarityColor(item.rarity),
                            frameResource = rarityFrameResource(item.rarity),
                            selected = selectedWeapon?.id == item.id,
                            equipped = equipped,
                            onClick = { selectedWeapon = item; selectedGear = null }
                        )
                    }
                    repeat(4 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        if (visibleGear.isNotEmpty()) {
            InventorySectionHeading("Доспехи и артефакты", "${visibleGear.size} предметов · нажми для подробностей", "equipment")
            visibleGear.chunked(4).forEach { rowItems ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rowItems.forEach { item ->
                        val equipped = equippedGearBySlot[item.slot]?.let { encodeGearInstance(it) == encodeGearInstance(item) } == true
                        TreasuryRelicTile(
                            modifier = Modifier.weight(1f),
                            title = item.name,
                            subtitle = "${item.rarity.title} · ${gearPowerScore(item)}",
                            resourceId = gearDrawable(item.slot),
                            rarityColor = weaponRarityColor(item.rarity),
                            frameResource = rarityFrameResource(item.rarity),
                            selected = selectedGear?.id == item.id,
                            equipped = equipped,
                            onClick = { selectedGear = item; selectedWeapon = null }
                        )
                    }
                    repeat(4 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        if (nextWeaponStage != null && filter != 2) {
            val preview = weaponCatalog.first { it.requiredStage == nextWeaponStage }
            InventoryNextDropCard(preview.name, preview.rarity.title, stageName(nextWeaponStage), weaponRarityColor(preview.rarity))
        }
    }

    selectedWeapon?.let { item ->
        val delta = weaponPowerScore(item) - weaponPowerScore(equippedWeapon)
        val equipped = encodeWeaponInstance(item) == encodeWeaponInstance(equippedWeapon)
        val comparison = when {
            equipped -> "Эта реликвия уже находится в активном комплекте"
            delta > 0 -> "Сильнее текущего оружия на $delta очков"
            delta < 0 -> "Уступает текущему оружию на ${-delta} очков"
            else -> "Рейтинг совпадает с текущим оружием"
        }
        val weaponDetailReveal = remember(item.id) { Animatable(0f) }
        LaunchedEffect(item.id) {
            weaponDetailReveal.animateTo(1f, tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        }
        Dialog(onDismissRequest = { selectedWeapon = null }) {
            Surface(
                Modifier.fillMaxWidth().graphicsLayer {
                    val reveal = weaponDetailReveal.value
                    scaleX = .94f + reveal * .06f
                    scaleY = .94f + reveal * .06f
                    alpha = .55f + reveal * .45f
                },
                color = Color(0xFF14243A),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.5.dp, weaponRarityColor(item.rarity))
            ) {
                Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ПРЕДМЕТ", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        TextButton(onClick = { selectedWeapon = null }) { Text("ЗАКРЫТЬ") }
                    }
                    TreasuryWeaponDetail(
                        item = item,
                        score = weaponPowerScore(item),
                        rarityColor = weaponRarityColor(item.rarity),
                        comparison = comparison,
                        powerDelta = delta,
                        equipped = equipped,
                        canEquip = stage >= item.requiredStage,
                        onEquip = { onEquipWeapon(item); selectedWeapon = null }
                    )
                }
            }
        }
    }
    selectedGear?.let { item ->
        val current = equippedGearBySlot[item.slot]
        val delta = if (current == null) gearPowerScore(item) else gearPowerScore(item) - gearPowerScore(current)
        val equipped = current?.let { encodeGearInstance(it) == encodeGearInstance(item) } == true
        val comparison = when {
            equipped -> "Эта реликвия уже надета"
            current == null -> "Слот свободен · предмет готов к экипировке"
            delta > 0 -> "Сильнее надетого предмета на $delta очков"
            delta < 0 -> "Уступает надетому предмету на ${-delta} очков"
            else -> "Рейтинг совпадает с надетой реликвией"
        }
        val gearDetailReveal = remember(item.id) { Animatable(0f) }
        LaunchedEffect(item.id) {
            gearDetailReveal.animateTo(1f, tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        }
        Dialog(onDismissRequest = { selectedGear = null }) {
            Surface(
                Modifier.fillMaxWidth().graphicsLayer {
                    val reveal = gearDetailReveal.value
                    scaleX = .94f + reveal * .06f
                    scaleY = .94f + reveal * .06f
                    alpha = .55f + reveal * .45f
                },
                color = Color(0xFF14243A),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.5.dp, weaponRarityColor(item.rarity))
            ) {
                Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ПРЕДМЕТ", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        TextButton(onClick = { selectedGear = null }) { Text("ЗАКРЫТЬ") }
                    }
                    TreasuryGearDetail(
                        item = item,
                        score = gearPowerScore(item),
                        rarityColor = weaponRarityColor(item.rarity),
                        setName = gearSetNames[item.setId] ?: item.setId,
                        comparison = comparison,
                        powerDelta = if (current == null) null else delta,
                        equipped = equipped,
                        canEquip = stage >= item.requiredStage,
                        onEquip = { onEquipGear(item); selectedGear = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun TreasuryVaultHero(totalItems: Int, equippedItems: Int, classIndex: Int) {
    val transition = rememberInfiniteTransition(label = "treasury-vault-glow")
    val pulse by transition.animateFloat(
        initialValue = .94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(2300), RepeatMode.Reverse),
        label = "treasury-vault-pulse"
    )
    Box(
        Modifier.fillMaxWidth().height(166.dp).clip(RoundedCornerShape(20.dp))
    ) {
        Image(
            painter = painterResource(R.drawable.lunar_harbor_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xAA0B1729), Color(0xCC1D3B4B), Color(0xEE121B31)))))
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.linearGradient(listOf(Color(0x660B1729), Color(0x551D3B4B), Color(0x77121B31))))
            drawCircle(
                Brush.radialGradient(listOf(Color(0x77E9BD70), Color(0x2278C6A3), Color.Transparent)),
                radius = size.height * .88f,
                center = Offset(size.width * .82f, size.height * .48f)
            )
            for (index in 0..8) {
                val x = size.width * (0.48f + index * .065f)
                drawLine(Color(0x227BC7B0), Offset(x, 0f), Offset(x - size.height * .32f, size.height), size.width * .0018f)
            }
            drawCircle(Color(0x66E7C27A), size.height * .42f, Offset(size.width * .82f, size.height * .5f), style = Stroke(width = 1.2.dp.toPx()))
            drawCircle(Color(0x3378C6A3), size.height * .34f, Offset(size.width * .82f, size.height * .5f), style = Stroke(width = 1.dp.toPx()))
        }
        Box(
            Modifier.align(Alignment.CenterEnd).padding(end = 8.dp).size(width = 126.dp, height = 158.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.character_halo_frame),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                    alpha = .94f
                }
            )
            Image(
                painter = painterResource(classHeroResource(classIndex)),
                contentDescription = "Персонаж: ${classNames[classIndex.coerceIn(0, 3)]}",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(horizontal = 7.dp, vertical = 5.dp)
            )
            Image(
                painter = painterResource(classSigilResource(classIndex)),
                contentDescription = "Эмблема класса",
                contentScale = ContentScale.Fit,
                modifier = Modifier.align(Alignment.BottomEnd).size(39.dp).offset(x = 2.dp, y = 2.dp)
            )
        }
        Column(
            Modifier.align(Alignment.CenterStart).fillMaxWidth(.67f).padding(start = 15.dp, end = 4.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text("СОКРОВИЩНИЦА ЛУННОЙ ГАВАНИ", color = Gold, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
            Text("Зал реликвий", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
            Text("Оружие, доспехи и наследие древних мастеров", color = Color(0xFFD0DCE8), fontSize = 9.sp, maxLines = 2)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                VaultCounter("РЕЛИКВИИ", totalItems.toString())
                VaultCounter("НАДЕТО", equippedItems.toString())
            }
        }
    }
}

private fun classHeroResource(classIndex: Int): Int = when (classIndex.coerceIn(0, 3)) {
    0 -> R.drawable.hero_full_blademaster
    1 -> R.drawable.hero_full_ironbody
    2 -> R.drawable.hero_full_elementalist
    else -> R.drawable.hero_full_spiritmystic
}

private fun classSigilResource(classIndex: Int): Int = when (classIndex.coerceIn(0, 3)) {
    0 -> R.drawable.class_sigil_blademaster
    1 -> R.drawable.class_sigil_ironbody
    2 -> R.drawable.class_sigil_elementalist
    else -> R.drawable.class_sigil_spiritmystic
}

@Composable
private fun VaultCounter(label: String, value: String) {
    Surface(
        color = Color(0xAA0A1524),
        shape = RoundedCornerShape(7.dp),
        border = BorderStroke(1.dp, Gold.copy(alpha = .45f))
    ) {
        Row(Modifier.padding(horizontal = 7.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = Color(0xFFB6C7D6), fontSize = 6.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Gold, fontSize = 8.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun TreasuryRelicTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    resourceId: Int,
    rarityColor: Color,
    frameResource: Int = R.drawable.ui_rarity_frame_02,
    selected: Boolean,
    equipped: Boolean,
    onClick: () -> Unit
) {
    val selectionProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(
            durationMillis = 240,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "relic-selection"
    )
    Surface(
        modifier = modifier.height(158.dp).graphicsLayer {
            val scale = 1f + selectionProgress * .018f
            scaleX = scale
            scaleY = scale
        }.clickable(onClick = onClick),
        color = if (selected) Color(0xEE29485A) else Color(0xE9142236),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp + selectionProgress.dp,
            Gold.copy(alpha = .55f + selectionProgress * .45f)
        ),
        tonalElevation = 2.dp + 5.dp * selectionProgress,
        shadowElevation = 1.dp + 5.dp * selectionProgress
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier.fillMaxSize()
                    .background(
                        Gold.copy(alpha = selectionProgress * .075f),
                        RoundedCornerShape(14.dp)
                    )
            )
            Column(
                Modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RelicArtworkIcon(
                    resourceId,
                    rarityColor,
                    Modifier.size(76.dp).graphicsLayer {
                        val scale = 1f + selectionProgress * .035f
                        scaleX = scale
                        scaleY = scale
                    },
                    frameResource
                )
                Text(title, color = if (selected) Color.White else Color(0xFFE2E8F0), fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 2, minLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 10.sp)
                Text(subtitle, color = rarityColor, fontSize = 7.sp, fontWeight = FontWeight.Black, maxLines = 1)
            }
            if (selected) {
                Surface(
                    Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp),
                    color = Color(0xEE6B512C),
                    shape = RoundedCornerShape(5.dp),
                    border = BorderStroke(1.dp, Gold.copy(alpha = .75f))
                ) {
                    Text("✦ ВЫБРАНО", Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFFFFE8B7), fontSize = 6.sp, fontWeight = FontWeight.Black)
                }
            }
            if (equipped) {
                Surface(
                    Modifier.align(Alignment.TopEnd).padding(4.dp),
                    color = Color(0xEE315C4E),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text("НАДЕТО", Modifier.padding(horizontal = 4.dp, vertical = 2.dp), color = Color.White, fontSize = 6.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun InventoryPowerComparison(score: Int, delta: Int?, equipped: Boolean) {
    val deltaColor = when {
        equipped -> Jade
        delta == null -> Gold
        delta > 0 -> Jade
        delta < 0 -> Color(0xFFFF8B83)
        else -> Color(0xFFB8C7D8)
    }
    val deltaLabel = when {
        equipped -> "АКТИВНО"
        delta == null -> "НОВЫЙ СЛОТ"
        delta > 0 -> "+$delta"
        delta < 0 -> "$delta"
        else -> "БЕЗ ИЗМЕНЕНИЙ"
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xAA0B1728),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, deltaColor.copy(alpha = .58f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("СИЛА РЕЛИКВИИ", color = Color(0xFF9FB2C8), fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text(score.toString(), color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("СРАВНЕНИЕ С ЭКИПИРОВКОЙ", color = Color(0xFF9FB2C8), fontSize = 7.sp, fontWeight = FontWeight.Black)
                Text(deltaLabel, color = deltaColor, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun RelicDetailShowcase(resourceId: Int, rarityColor: Color, frameResource: Int) {
    Box(
        Modifier.size(122.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width * .5f, size.height * .5f)
            val radius = size.minDimension * .48f
            drawCircle(
                Brush.radialGradient(
                    listOf(rarityColor.copy(alpha = .38f), rarityColor.copy(alpha = .12f), Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            drawCircle(
                rarityColor.copy(alpha = .72f),
                radius = size.minDimension * .455f,
                center = center,
                style = Stroke(width = 1.6.dp.toPx())
            )
            drawCircle(
                Gold.copy(alpha = .48f),
                radius = size.minDimension * .405f,
                center = center,
                style = Stroke(width = .8.dp.toPx())
            )
            for (index in 0 until 8) {
                val angle = index * (Math.PI / 4.0)
                val inner = size.minDimension * .43f
                val outer = size.minDimension * .48f
                val start = Offset(center.x + kotlin.math.cos(angle).toFloat() * inner, center.y + kotlin.math.sin(angle).toFloat() * inner)
                val end = Offset(center.x + kotlin.math.cos(angle).toFloat() * outer, center.y + kotlin.math.sin(angle).toFloat() * outer)
                drawLine(rarityColor.copy(alpha = .78f), start, end, 1.2.dp.toPx())
            }
        }
        RelicArtworkIcon(resourceId, rarityColor, Modifier.size(100.dp), frameResource)
    }
}

@Composable
private fun TreasuryWeaponDetail(
    item: WeaponDefinition,
    score: Int,
    rarityColor: Color,
    comparison: String,
    powerDelta: Int?,
    equipped: Boolean,
    canEquip: Boolean,
    onEquip: () -> Unit
) {
    Surface(
        Modifier.fillMaxWidth(),
        color = Color(0xF0192B42),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, rarityColor.copy(alpha = .82f)),
        tonalElevation = 8.dp,
        shadowElevation = 5.dp
    ) {
        Column(Modifier.fillMaxWidth().padding(11.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RelicDetailShowcase(weaponDrawable(item.type), rarityColor, rarityFrameResource(item.rarity))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("ИЗБРАННАЯ РЕЛИКВИЯ", color = Gold, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    Text(item.name, color = rarityColor, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 2)
                    Text("${weaponNames[item.type]} · ${stageName(item.requiredStage)}", color = Color(0xFFD4E0EC), fontSize = 9.sp)
                    Text("${item.rarity.title.uppercase()} · РЕЙТИНГ $score", color = Gold, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(item.effectDescription, color = Color(0xFFD3DFE9), fontSize = 10.sp)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                ClassMetric("weapon", "+${item.attackBonus}")
                ClassMetric("critical", "+${item.bonusDamage}")
                ClassMetric("shield", "+${item.defenseBonus}")
                ClassMetric("health", "+${item.healOnHit}")
            }
            InventoryPowerComparison(score = score, delta = powerDelta, equipped = equipped)
            Text(comparison, color = if (equipped || comparison.startsWith("Сильнее")) Jade else Color(0xFFFFCF9B), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            InventoryActionButton(
                label = when { equipped -> "УЖЕ ЭКИПИРОВАНО"; !canEquip -> "ТРЕБУЕТСЯ СТУПЕНЬ ${item.requiredStage}"; else -> "ЭКИПИРОВАТЬ РЕЛИКВИЮ" },
                selected = equipped,
                enabled = canEquip && !equipped,
                onClick = onEquip
            )
        }
    }
}

@Composable
private fun TreasuryGearDetail(
    item: GearDefinition,
    score: Int,
    rarityColor: Color,
    setName: String,
    comparison: String,
    powerDelta: Int?,
    equipped: Boolean,
    canEquip: Boolean,
    onEquip: () -> Unit
) {
    Surface(
        Modifier.fillMaxWidth(),
        color = Color(0xF0192B42),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, rarityColor.copy(alpha = .82f)),
        tonalElevation = 8.dp,
        shadowElevation = 5.dp
    ) {
        Column(Modifier.fillMaxWidth().padding(11.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RelicDetailShowcase(gearDrawable(item.slot), rarityColor, rarityFrameResource(item.rarity))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("ИЗБРАННАЯ ЧАСТЬ КОМПЛЕКТА", color = Gold, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    Text(item.name, color = rarityColor, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 2)
                    Text("${item.slot.title} · ${stageName(item.requiredStage)}", color = Color(0xFFD4E0EC), fontSize = 9.sp)
                    Text("$setName · РЕЙТИНГ $score", color = Jade, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(item.effectDescription, color = Color(0xFFD3DFE9), fontSize = 10.sp)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                ClassMetric("weapon", "+${item.attackBonus}")
                ClassMetric("shield", "+${item.defenseBonus}")
                ClassMetric("health", "+${item.hpBonus}")
                ClassMetric("skills", "+${item.skillBonus}")
            }
            InventoryPowerComparison(score = score, delta = powerDelta, equipped = equipped)
            Text(comparison, color = if (equipped || comparison.startsWith("Сильнее")) Jade else Color(0xFFFFCF9B), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            InventoryActionButton(
                label = when { equipped -> "УЖЕ НАДЕТО"; !canEquip -> "ТРЕБУЕТСЯ СТУПЕНЬ ${item.requiredStage}"; else -> "НАДЕТЬ РЕЛИКВИЮ" },
                selected = equipped,
                enabled = canEquip && !equipped,
                onClick = onEquip
            )
        }
    }
}

private fun lootRarityColor(rarity: String): Color = when (rarity.uppercase()) {
    "UNCOMMON" -> Color(0xFF77C9A6)
    "RARE" -> Color(0xFF70BCEB)
    "EPIC" -> Color(0xFFB78BFF)
    "LEGENDARY" -> Color(0xFFFFC76F)
    "MYTHIC" -> Color(0xFFFF80C7)
    else -> Color(0xFFC1CBD8)
}

@Composable
private fun InventoryLoadoutRow(weapon: WeaponDefinition, equippedGear: List<GearDefinition>) {
    Surface(
        color = Color(0xE9111F32),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0x886D89A6)),
        tonalElevation = 4.dp
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RpgIcon("equipment", Modifier.size(19.dp))
                Spacer(Modifier.width(6.dp))
                Text("АКТИВНАЯ ЭКИПИРОВКА", color = Gold, fontSize = 8.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.weight(1f))
                Text("${1 + equippedGear.size}/5", color = Jade, fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                InventoryLoadoutSlot(
                    "ОРУЖИЕ", weapon.name, weaponDrawable(weapon.type),
                    weaponRarityColor(weapon.rarity), Modifier.weight(1f),
                    rarityFrameResource(weapon.rarity)
                )
                GearSlot.values().forEach { slot ->
                    val item = equippedGear.firstOrNull { it.slot == slot }
                    InventoryLoadoutSlot(
                        slot.title.uppercase(),
                        item?.name ?: "Пусто",
                        gearDrawable(slot),
                        item?.let { weaponRarityColor(it.rarity) } ?: Color(0xFF65758A),
                        Modifier.weight(1f),
                        item?.let { rarityFrameResource(it.rarity) } ?: R.drawable.ui_rarity_frame_01
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryLoadoutSlot(
    label: String,
    itemName: String,
    icon: Int,
    tint: Color,
    modifier: Modifier = Modifier,
    frameResource: Int = R.drawable.ui_rarity_frame_01
) {
    Surface(
        modifier = modifier.height(104.dp),
        color = Color(0xFF101C30),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, tint.copy(alpha = .78f)),
        tonalElevation = 2.dp
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 2.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            RelicArtworkIcon(icon, tint, Modifier.size(52.dp), frameResource)
            Text(
                label, color = Gold, fontSize = 6.sp,
                fontWeight = FontWeight.Black, maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                itemName, color = Color(0xFFD8E0EC), fontSize = 6.sp,
                maxLines = 2, minLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 7.sp
            )
        }
    }
}

@Composable
private fun InventoryCategoryTab(label: String, icon: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val selectionProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(
            durationMillis = 220,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "inventory-tab-selection"
    )
    Surface(
        modifier = modifier.heightIn(min = 42.dp).graphicsLayer {
            val scale = 1f + selectionProgress * .025f
            scaleX = scale
            scaleY = scale
        }.clickable(onClick = onClick),
        color = androidx.compose.ui.graphics.lerp(Color(0xFF17263A), Color(0xFF71502C), selectionProgress),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            1.dp + 1.dp * selectionProgress,
            Gold.copy(alpha = .42f + selectionProgress * .58f)
        ),
        tonalElevation = 1.dp + 5.dp * selectionProgress,
        shadowElevation = 1.dp + 2.dp * selectionProgress
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            RpgIcon(
                icon,
                Modifier.size(22.dp).graphicsLayer {
                    val iconScale = 1f + selectionProgress * .12f
                    scaleX = iconScale
                    scaleY = iconScale
                    alpha = .78f + selectionProgress * .22f
                }
            )
            Text(
                label,
                color = androidx.compose.ui.graphics.lerp(Color(0xFFC0CCDB), Color(0xFFFFE7B4), selectionProgress),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
            Box(
                Modifier.width(18.dp + 12.dp * selectionProgress)
                    .height(2.dp)
                    .background(Gold.copy(alpha = selectionProgress), RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
private fun InventorySectionHeading(title: String, count: String, icon: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RpgIcon(icon, Modifier.size(30.dp))
        Spacer(Modifier.width(7.dp))
        Text(title, color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.weight(1f))
        Text(count, color = Jade, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InventoryNextDropCard(name: String, rarity: String, realm: String, rarityColor: Color) {
    Surface(
        color = Color(0xFF111E31),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, Gold.copy(alpha = .55f))
    ) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            RpgIcon("chest", Modifier.size(34.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("СЛЕДУЮЩАЯ РЕЛИКВИЯ", color = Jade, fontSize = 8.sp, fontWeight = FontWeight.Black)
                Text(name, color = rarityColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("$realm · $rarity", color = Color(0xFFB8C6D8), fontSize = 9.sp)
            }
            RpgIcon("portal", Modifier.size(28.dp))
        }
    }
}

@Composable
private fun WeaponInventoryCard(
    item: WeaponDefinition,
    weaponTypeName: String,
    score: Int,
    rarityColor: Color,
    comparison: String,
    comparisonPositive: Boolean,
    equipped: Boolean,
    canEquip: Boolean,
    onEquip: () -> Unit
) {
    Surface(
        color = if (equipped) Color(0xE52A4054) else Color(0xD918273C),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(if (equipped) 2.dp else 1.dp, if (equipped) Gold else rarityColor.copy(alpha = .48f)),
        tonalElevation = if (equipped) 8.dp else 3.dp,
        shadowElevation = 5.dp
    ) {
        Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RelicArtworkIcon(weaponDrawable(item.type), rarityColor, Modifier.size(90.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(item.name, color = rarityColor, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 2)
                    Text("$weaponTypeName · ${stageName(item.requiredStage)} · ступень ${item.requiredStage}", color = Color(0xFFD1DCEB), fontSize = 9.sp)
                    Text(item.effectDescription, color = Color(0xFFB8C6D8), fontSize = 9.sp, maxLines = 2)
                    Text("РЕЙТИНГ $score · ${item.rarity.title.uppercase()}", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
                if (equipped) RpgIcon("check", Modifier.size(23.dp))
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClassMetric("weapon", "+${item.attackBonus}")
                    ClassMetric("critical", "+${item.bonusDamage}")
                    ClassMetric("shield", "+${item.defenseBonus}")
                    ClassMetric("health", "+${item.healOnHit}")
                }
                Spacer(Modifier.width(7.dp))
                Box(Modifier.width(112.dp)) {
                    InventoryActionButton(
                        label = when { equipped -> "НАДЕТО"; !canEquip -> "СТУПЕНЬ ${item.requiredStage}"; else -> "ЭКИПИРОВАТЬ" },
                        selected = equipped,
                        enabled = canEquip && !equipped,
                        onClick = onEquip
                    )
                }
            }
            Text(comparison, color = if (equipped || comparisonPositive) Jade else Color.LightGray, fontSize = 9.sp, maxLines = 2)
        }
    }
}

@Composable
private fun GearInventoryCard(
    item: GearDefinition,
    score: Int,
    rarityColor: Color,
    setName: String,
    comparison: String,
    comparisonPositive: Boolean,
    equipped: Boolean,
    canEquip: Boolean,
    onEquip: () -> Unit
) {
    Surface(
        color = if (equipped) Color(0xE52A4054) else Color(0xD918273C),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(if (equipped) 2.dp else 1.dp, if (equipped) Gold else rarityColor.copy(alpha = .48f)),
        tonalElevation = if (equipped) 8.dp else 3.dp,
        shadowElevation = 5.dp
    ) {
        Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RelicArtworkIcon(gearDrawable(item.slot), rarityColor, Modifier.size(90.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(item.name, color = rarityColor, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 2)
                    Text("${item.slot.title} · ${stageName(item.requiredStage)}", color = Color(0xFFD1DCEB), fontSize = 9.sp)
                    Text("Комплект: $setName", color = Jade, fontSize = 9.sp, maxLines = 1)
                    Text(item.effectDescription, color = Color(0xFFB8C6D8), fontSize = 9.sp, maxLines = 2)
                    Text("РЕЙТИНГ $score · ${item.rarity.title.uppercase()}", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
                if (equipped) RpgIcon("check", Modifier.size(23.dp))
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClassMetric("weapon", "+${item.attackBonus}")
                    ClassMetric("shield", "+${item.defenseBonus}")
                    ClassMetric("health", "+${item.hpBonus}")
                    ClassMetric("skills", "+${item.skillBonus}")
                }
                Spacer(Modifier.width(7.dp))
                Box(Modifier.width(112.dp)) {
                    InventoryActionButton(
                        label = when { equipped -> "НАДЕТО"; !canEquip -> "СТУПЕНЬ ${item.requiredStage}"; else -> "НАДЕТЬ" },
                        selected = equipped,
                        enabled = canEquip && !equipped,
                        onClick = onEquip
                    )
                }
            }
            Text(comparison, color = if (equipped || comparisonPositive) Jade else Color.LightGray, fontSize = 9.sp, maxLines = 2)
        }
    }
}

@Composable
private fun PetCollectionTile(
    pet: PetDefinition,
    owned: Boolean,
    active: Boolean,
    level: Int,
    bondXp: Int,
    onClick: () -> Unit
) {
    val rarity = petRarityColor(pet.id)
    val bondCost = 100 + level * 75
    val progress = if (level >= 30) 1f else (bondXp.toFloat() / bondCost.coerceAtLeast(1)).coerceIn(0f, 1f)
    Surface(
        modifier = Modifier.fillMaxWidth().height(174.dp).clickable(onClick = onClick),
        color = when {
            active -> Color(0xE52A494A)
            owned -> Color(0xE51A2C43)
            else -> Color(0xC9162031)
        },
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(
            if (active) 2.dp else 1.dp,
            when {
                active -> rarity
                owned -> rarity.copy(alpha = .55f)
                else -> Color(0xFF69768A).copy(alpha = .42f)
            }
        ),
        tonalElevation = if (active) 8.dp else 3.dp,
        shadowElevation = if (active) 7.dp else 2.dp
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (active) "СПУТНИК" else if (owned) "ПРИРУЧЁН" else "НЕ НАЙДЕН",
                    color = if (active) Jade else if (owned) rarity else Color(0xFF9BA8B8),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.weight(1f))
                RpgIcon(if (active) "check" else if (owned) "plus" else "lock", Modifier.size(15.dp))
            }
            PetPortraitMedallion(pet.id, 76.dp, active)
            Text(
                pet.name,
                color = if (owned) Color(0xFFFFE5B0) else Color(0xFFB5C0CF),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
            Text(
                petRarityName(pet.id).replace(" СПУТНИК", ""),
                color = if (owned) rarity else Color(0xFF8794A6),
                fontSize = 6.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            if (owned) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(.92f).height(3.dp),
                    color = if (active) Jade else rarity,
                    trackColor = Color(0xFF354359)
                )
                Text(
                    "СВЯЗЬ · УР. $level/30",
                    color = if (active) Jade else Color(0xFFD4DDE8),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    pet.dropHint,
                    color = Color(0xFFE1C58D),
                    fontSize = 7.sp,
                    maxLines = 2,
                    lineHeight = 8.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PetCompanionCard(
    pet: PetDefinition,
    owned: Boolean,
    active: Boolean,
    level: Int,
    bondXp: Int,
    ascension: Int,
    onSelect: () -> Unit
) {
    val rarity = petRarityColor(pet.id)
    val nextBondCost = 100 + level * 75
    val bondProgress = if (level >= 30) 1f else (bondXp.toFloat() / nextBondCost.coerceAtLeast(1)).coerceIn(0f, 1f)
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
        color = when {
            active -> Color(0xE52B4A4C)
            owned -> Color(0xE51A2C43)
            else -> Color(0xC9162031)
        },
        shape = MaterialTheme.shapes.extraLarge,
        border = androidx.compose.foundation.BorderStroke(
            if (active) 2.dp else 1.dp,
            when { active -> rarity; owned -> rarity.copy(alpha = .48f); else -> Color(0xFF566174).copy(alpha = .45f) }
        ),
        tonalElevation = if (active) 10.dp else 4.dp,
        shadowElevation = if (active) 8.dp else 3.dp
    ) {
        Row(Modifier.fillMaxWidth().padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
            PetPortraitMedallion(pet.id, 110.dp, active)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        pet.name,
                        color = if (owned) Color(0xFFFFE5B0) else Color(0xFFB3BDCA),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Surface(color = rarity.copy(alpha = if (owned) .20f else .10f), shape = MaterialTheme.shapes.small) {
                        Text(petRarityName(pet.id), Modifier.padding(horizontal = 5.dp, vertical = 2.dp), color = if (owned) rarity else Color(0xFF929BAB), fontSize = 7.sp, fontWeight = FontWeight.Black)
                    }
                }
                Text(pet.role.uppercase(), color = Color(0xFFD5C59F), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text(pet.description, color = Color(0xFFD1DCEB), fontSize = 9.sp, maxLines = 2, lineHeight = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    ClassMetric("weapon", pet.attack.toString())
                    ClassMetric("shield", pet.defense.toString())
                    ClassMetric("health", pet.hp.toString())
                    ClassMetric("skills", pet.skill.toString())
                }
                if (owned) {
                    LinearProgressIndicator(
                        progress = { bondProgress },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = if (active) Jade else rarity,
                        trackColor = Color(0xFF354359)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("УР. " + level + "/30", color = Jade, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.width(7.dp))
                        Text("СВЯЗЬ " + bondXp + " XP", color = Color(0xFFD1DCEB), fontSize = 8.sp)
                        Spacer(Modifier.weight(1f))
                        Text("✦".repeat(ascension) + "·".repeat(5 - ascension), color = Color(0xFFFFD47F), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                } else {
                    Text("ИСТОЧНИК: " + pet.dropHint, color = Color(0xFFFFC978), fontSize = 8.sp, maxLines = 2)
                }
                Text(if (owned) pet.bestClass else "РЕЗОНАНС: " + pet.bestClass, color = Color(0xFFB9C6DC), fontSize = 8.sp, maxLines = 1)
            }
            Spacer(Modifier.width(7.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                RpgIcon(when { active -> "check"; owned -> "plus"; else -> "lock" }, Modifier.size(30.dp))
                Text(
                    when { active -> "АКТИВЕН"; owned -> "ВЫБРАТЬ"; else -> "ЗАКРЫТ" },
                    color = when { active -> Jade; owned -> Color(0xFFFFD58A); else -> Color(0xFF8E99A9) },
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun PetPortraitMedallion(
    petId: String,
    size: androidx.compose.ui.unit.Dp,
    active: Boolean = false
) {
    val tint = petRarityColor(petId)
    val transition = rememberInfiniteTransition(label = "pet-medallion-" + petId)
    val pulse by transition.animateFloat(
        if (active) .94f else .97f,
        if (active) 1.06f else 1.02f,
        infiniteRepeatable(tween(if (active) 1500 else 2200), RepeatMode.Reverse),
        label = "pet-medallion-pulse-" + petId
    )
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.pet_sanctuary_seal),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = pulse; scaleY = pulse }
        )
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension * .43f
            drawCircle(
                Brush.radialGradient(listOf(tint.copy(alpha = if (active) .28f else .16f), Color.Transparent)),
                radius,
                center
            )
            drawCircle(
                tint.copy(alpha = if (active) .85f else .45f),
                radius * .91f,
                center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = if (active) 2.3.dp.toPx() else 1.4.dp.toPx())
            )
            for (i in 0..7) {
                val angle = i * (Math.PI / 4.0)
                val x = center.x + kotlin.math.cos(angle).toFloat() * radius * .91f
                val y = center.y + kotlin.math.sin(angle).toFloat() * radius * .91f
                drawCircle(Color(0xFFFFE5A5).copy(alpha = if (active) .9f else .55f), if (active) 2.1.dp.toPx() else 1.4.dp.toPx(), Offset(x, y))
            }
        }
        Image(
            painter = painterResource(id = petPortraitResource(petId)),
            contentDescription = petId,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(.80f)
        )
    }
}

@Composable
private fun PetActiveShowcase(
    pet: PetDefinition,
    level: Int,
    bondXp: Int,
    ascension: Int,
    attack: Int,
    defense: Int,
    hp: Int,
    skill: Int,
    synergy: Boolean,
    xpCost: Int
) {
    val rarity = petRarityColor(pet.id)
    val progress = if (level >= 30) 1f else (bondXp.toFloat() / xpCost.coerceAtLeast(1)).coerceIn(0f, 1f)
    Surface(
        color = Color(0xEE12223A),
        shape = MaterialTheme.shapes.extraLarge,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, rarity.copy(alpha = .8f)),
        tonalElevation = 10.dp,
        shadowElevation = 8.dp
    ) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            PetPortraitMedallion(pet.id, 136.dp, true)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(petRarityName(pet.id), color = rarity, fontSize = 8.sp, fontWeight = FontWeight.Black)
                Text(pet.name, color = Color(0xFFFFE5B0), fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 2)
                Text(pet.role, color = Color(0xFFD5C59F), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    ClassMetric("weapon", "+" + attack)
                    ClassMetric("shield", "+" + defense)
                    ClassMetric("health", "+" + hp)
                    ClassMetric("skills", "+" + skill)
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = if (level >= 30) Color(0xFFFFD47F) else Jade,
                    trackColor = Color(0xFF354359)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("СВЯЗЬ · УР. " + level + "/30", color = Jade, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.weight(1f))
                    Text("✦".repeat(ascension) + "·".repeat(5 - ascension), color = Color(0xFFFFD47F), fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
                Surface(color = if (synergy) Color(0xFF254D47) else Color(0xFF30394B), shape = MaterialTheme.shapes.small) {
                    Text(
                        if (synergy) "РЕЗОНАНС КЛАССА · АКТИВЕН" else "РЕЗОНАНС · " + pet.bestClass,
                        Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        color = if (synergy) Color(0xFFA6E9CD) else Color(0xFFB9C6D8),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text("ОСОБЕННОСТЬ: " + petCombatTrait(pet.id), color = Color(0xFFD0D9E5), fontSize = 8.sp, maxLines = 2)
            }
        }
    }
}

@Composable
private fun PetResourceBadge(icon: String, label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xD91B2B42),
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55D7B56F))
    ) {
        Row(Modifier.padding(horizontal = 7.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            RpgIcon(icon, Modifier.size(24.dp))
            Spacer(Modifier.width(5.dp))
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(label.uppercase(), color = Color(0xFFBFC9D8), fontSize = 7.sp, fontWeight = FontWeight.Black)
                Text(value, color = Color(0xFFFFE5B0), fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

private fun petRarityName(petId: String): String = when (petId) {
    "jade_fox", "stone_tortoise" -> "РЕДКИЙ СПУТНИК"
    "thunder_hawk", "ember_wolf", "iron_ape" -> "ЭПИЧЕСКИЙ СПУТНИК"
    "moon_deer", "river_koi" -> "ЛЕГЕНДАРНЫЙ СПУТНИК"
    else -> "ДУХОВНЫЙ СПУТНИК"
}

private fun petRarityColor(petId: String): Color = when (petId) {
    "jade_fox" -> Color(0xFF83D8BD)
    "stone_tortoise" -> Color(0xFF9EC7A5)
    "thunder_hawk" -> Color(0xFF83CFFF)
    "ember_wolf" -> Color(0xFFFFA36F)
    "moon_deer" -> Color(0xFFE3C6FF)
    "iron_ape" -> Color(0xFFE5BD72)
    "river_koi" -> Color(0xFFB8A4FF)
    else -> Color(0xFFE5BD72)
}


@Composable
private fun ClassPortraitCard(
    name: String,
    description: String,
    profile: ClassCombatProfile,
    portraitRes: Int,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val aura = rememberInfiniteTransition(label = "class-portrait-$name")
    val glow by aura.animateFloat(
        initialValue = .34f,
        targetValue = .72f,
        animationSpec = infiniteRepeatable(tween(1700), RepeatMode.Reverse),
        label = "class-portrait-glow"
    )
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
        color = if (selected) Color(0xE52B405A) else Color(0xD916253A),
        shape = MaterialTheme.shapes.extraLarge,
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) Gold.copy(alpha = .75f + glow * .25f) else Color(0x665B7898)
        ),
        tonalElevation = if (selected) 10.dp else 4.dp,
        shadowElevation = if (selected) 8.dp else 3.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.width(126.dp).height(154.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                (if (selected) Color(0xFF8ED8E8) else Color(0xFF607DA8)).copy(alpha = .48f + glow * .16f),
                                Color(0xFF172943),
                                Color(0xFF0B1423),
                                Color.Transparent
                            )
                        ),
                        MaterialTheme.shapes.large
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val center = Offset(size.width * .5f, size.height * .5f)
                    drawCircle(
                        Brush.radialGradient(
                            listOf(
                                (if (selected) Color(0x667DD8D1) else Color(0x444D83B5)),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension * .66f
                        ),
                        radius = size.minDimension * .66f,
                        center = center
                    )
                    drawCircle(
                        if (selected) Gold.copy(alpha = .72f + glow * .25f) else Color(0x665B7898),
                        radius = size.minDimension * .465f,
                        center = center,
                        style = Stroke(width = 1.4.dp.toPx())
                    )
                    drawCircle(
                        Color(0x667FC7D4),
                        radius = size.minDimension * .435f,
                        center = center,
                        style = Stroke(width = .7.dp.toPx())
                    )
                }
                Image(
                    painter = painterResource(id = portraitRes),
                    contentDescription = "$name — иллюстрированный образ класса",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(2.dp)
                )
                if (selected) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 5.dp),
                        color = Color(0xE52B594F),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Jade.copy(alpha = .9f))
                    ) {
                        Text("ПУТЬ ИЗБРАН", Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        name,
                        color = if (selected) Color(0xFFFFE4A0) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (selected) {
                        Spacer(Modifier.width(6.dp))
                        Text("ПУТЬ ВЫБРАН", color = Jade, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }
                Text(description, color = Color(0xFFD1DCEB), fontSize = 10.sp, maxLines = 3)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClassMetric("weapon", profile.attack.toString())
                    ClassMetric("shield", profile.defense.toString())
                    ClassMetric("health", profile.bonusHp.toString())
                    ClassMetric("critical", "${profile.criticalChance}%")
                }
                Text(
                    "Техники +${profile.skillPower}  ·  лечение +${profile.healingBonus}",
                    color = Color(0xFF9BDCC5),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RpgIcon(if (selected) "check" else "plus", Modifier.size(19.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (selected) "ТЕКУЩИЙ ПУТЬ" else "ВЫБРАТЬ ПУТЬ",
                        color = if (selected) Jade else Color(0xFFE7C98E),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassMetric(icon: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        RpgIcon(icon, Modifier.size(17.dp))
        Text(value, color = Color(0xFFF4E8D0), fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RpgIcon(kind: String, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val c = Offset(w * .5f, h * .5f)
        val unit = minOf(w, h)
        val goldBright = Color(0xFFFFE5A5)
        val goldDark = Color(0xFFB7833D)
        val blue = Color(0xFF78C9E9)
        val purple = Color(0xFFB58BFF)
        drawCircle(Brush.radialGradient(listOf(Color(0xFF344B70), Color(0xFF111A30))), unit * .47f, c)
        drawCircle(goldDark, unit * .46f, c, style = androidx.compose.ui.graphics.drawscope.Stroke(unit * .045f))
        drawCircle(goldBright.copy(alpha = .7f), unit * .40f, c, style = androidx.compose.ui.graphics.drawscope.Stroke(unit * .012f))
        val p = Path()
        val sw = unit * .055f
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = sw, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
        fun line(x1: Float, y1: Float, x2: Float, y2: Float, color: Color = goldBright, width: Float = sw) {
            drawLine(color, Offset(w*x1,h*y1), Offset(w*x2,h*y2), width)
        }
        fun circle(x: Float, y: Float, r: Float, color: Color = goldBright, fill: Boolean = false) {
            drawCircle(color, unit*r, Offset(w*x,h*y), style = if(fill) androidx.compose.ui.graphics.drawscope.Fill else stroke)
        }
        when (kind) {
            "garden" -> {
                p.moveTo(w*.5f,h*.77f); p.cubicTo(w*.18f,h*.58f,w*.28f,h*.31f,w*.5f,h*.43f); p.cubicTo(w*.72f,h*.31f,w*.82f,h*.58f,w*.5f,h*.77f); drawPath(p,Color(0xFF85C99C),style=stroke)
                line(.5f,.76f,.5f,.32f,goldBright,sw*.7f); line(.5f,.58f,.34f,.46f,goldBright,sw*.55f); line(.5f,.58f,.66f,.46f,goldBright,sw*.55f)
                circle(.5f,.28f,.055f,Color(0xFFFFD98C),true)
            }
            "ice" -> {
                line(.5f,.20f,.5f,.80f,blue,sw)
                line(.24f,.34f,.76f,.66f,blue,sw)
                line(.24f,.66f,.76f,.34f,blue,sw)
                line(.5f,.20f,.40f,.31f,blue,sw*.7f); line(.5f,.20f,.60f,.31f,blue,sw*.7f)
                line(.5f,.80f,.40f,.69f,blue,sw*.7f); line(.5f,.80f,.60f,.69f,blue,sw*.7f)
                line(.24f,.34f,.38f,.35f,blue,sw*.7f); line(.24f,.34f,.30f,.47f,blue,sw*.7f)
                line(.76f,.66f,.62f,.65f,blue,sw*.7f); line(.76f,.66f,.70f,.53f,blue,sw*.7f)
                circle(.5f,.5f,.035f,goldBright,true)
            }
            "lightning" -> {
                p.moveTo(w*.57f,h*.14f); p.lineTo(w*.30f,h*.54f); p.lineTo(w*.48f,h*.54f); p.lineTo(w*.39f,h*.84f); p.lineTo(w*.72f,h*.42f); p.lineTo(w*.53f,h*.42f); p.close()
                drawPath(p,Color(0xFFFFD36F),style=stroke)
            }
            "lotus" -> {
                p.moveTo(w*.5f,h*.75f); p.cubicTo(w*.18f,h*.55f,w*.25f,h*.32f,w*.5f,h*.43f); p.cubicTo(w*.75f,h*.32f,w*.82f,h*.55f,w*.5f,h*.75f); drawPath(p,Color(0xFF83CDB6),style=stroke)
                p.moveTo(w*.5f,h*.70f); p.cubicTo(w*.30f,h*.50f,w*.42f,h*.23f,w*.5f,h*.18f); p.cubicTo(w*.58f,h*.23f,w*.70f,h*.50f,w*.5f,h*.70f); drawPath(p,purple,style=stroke)
                line(.22f,.78f,.78f,.78f,goldBright,sw*.8f)
            }
            "spear" -> {
                line(.5f,.16f,.5f,.84f,goldBright,sw*1.2f)
                p.moveTo(w*.5f,h*.13f); p.lineTo(w*.70f,h*.39f); p.lineTo(w*.5f,h*.33f); p.lineTo(w*.30f,h*.39f); p.close(); drawPath(p,blue,style=stroke)
                line(.36f,.65f,.64f,.65f,purple,sw)
            }
            "staff" -> {
                line(.5f,.28f,.5f,.83f,goldBright,sw*1.4f)
                drawCircle(purple,unit*.17f,Offset(w*.5f,h*.25f),style=stroke)
                drawCircle(blue,unit*.08f,Offset(w*.5f,h*.25f))
                line(.36f,.75f,.64f,.75f,goldBright,sw)
            }
            "gauntlet" -> {
                p.moveTo(w*.30f,h*.70f); p.lineTo(w*.25f,h*.48f); p.lineTo(w*.34f,h*.38f); p.lineTo(w*.40f,h*.18f); p.lineTo(w*.51f,h*.34f); p.lineTo(w*.60f,h*.20f); p.lineTo(w*.68f,h*.39f); p.lineTo(w*.76f,h*.46f); p.lineTo(w*.68f,h*.74f); p.close()
                drawPath(p,blue,style=stroke); line(.35f,.57f,.65f,.57f,goldBright,sw)
            }
            "helmet" -> {
                p.moveTo(w*.25f,h*.62f); p.lineTo(w*.25f,h*.43f); p.cubicTo(w*.25f,h*.16f,w*.75f,h*.16f,w*.75f,h*.43f); p.lineTo(w*.75f,h*.62f); p.lineTo(w*.62f,h*.62f); p.lineTo(w*.60f,h*.49f); p.lineTo(w*.40f,h*.49f); p.lineTo(w*.38f,h*.62f); p.close()
                drawPath(p,blue,style=stroke); line(.25f,.64f,.75f,.64f,goldBright,sw*1.3f)
            }
            "armor" -> {
                p.moveTo(w*.32f,h*.24f); p.lineTo(w*.43f,h*.17f); p.lineTo(w*.5f,h*.27f); p.lineTo(w*.57f,h*.17f); p.lineTo(w*.68f,h*.24f); p.lineTo(w*.78f,h*.39f); p.lineTo(w*.66f,h*.48f); p.lineTo(w*.63f,h*.78f); p.lineTo(w*.37f,h*.78f); p.lineTo(w*.34f,h*.48f); p.lineTo(w*.22f,h*.39f); p.close()
                drawPath(p,blue,style=stroke); line(.5f,.28f,.5f,.73f,goldBright,sw*.8f)
            }
            "ring" -> {
                drawCircle(goldBright,unit*.27f,c,style=stroke); drawCircle(blue,unit*.17f,c,style=stroke)
                p.moveTo(w*.5f,h*.18f); p.lineTo(w*.62f,h*.34f); p.lineTo(w*.5f,h*.47f); p.lineTo(w*.38f,h*.34f); p.close(); drawPath(p,purple,style=stroke)
            }
            "artifact" -> {
                p.moveTo(w*.5f,h*.16f); p.lineTo(w*.75f,h*.5f); p.lineTo(w*.5f,h*.84f); p.lineTo(w*.25f,h*.5f); p.close(); drawPath(p,purple,style=stroke)
                drawCircle(blue,unit*.12f,c); drawCircle(goldBright,unit*.045f,c)
                line(.5f,.16f,.5f,.84f,goldBright,sw*.55f); line(.25f,.5f,.75f,.5f,goldBright,sw*.55f)
            }
            "blademaster" -> {
                p.moveTo(w*.49f,h*.13f); p.lineTo(w*.64f,h*.54f); p.lineTo(w*.52f,h*.68f); p.lineTo(w*.36f,h*.54f); p.close()
                drawPath(p, goldBright.copy(alpha=.92f))
                line(.5f,.64f,.5f,.83f,blue,sw*1.2f)
                line(.35f,.57f,.65f,.57f,goldBright,sw*1.5f)
                line(.25f,.72f,.75f,.25f,purple,sw*.8f)
                circle(.5f,.57f,.045f,blue,true)
            }
            "ironbody" -> {
                p.moveTo(w*.5f,h*.14f); p.cubicTo(w*.64f,h*.23f,w*.75f,h*.25f,w*.76f,h*.32f)
                p.lineTo(w*.72f,h*.61f); p.cubicTo(w*.68f,h*.73f,w*.57f,h*.81f,w*.5f,h*.85f)
                p.cubicTo(w*.43f,h*.81f,w*.32f,h*.73f,w*.28f,h*.61f); p.lineTo(w*.24f,h*.32f); p.cubicTo(w*.25f,h*.25f,w*.36f,h*.23f,w*.5f,h*.14f)
                drawPath(p, goldBright, style=stroke)
                p.moveTo(w*.36f,h*.44f); p.lineTo(w*.44f,h*.38f); p.lineTo(w*.55f,h*.40f); p.lineTo(w*.65f,h*.47f); p.lineTo(w*.59f,h*.57f); p.lineTo(w*.49f,h*.54f); p.lineTo(w*.43f,h*.62f); p.lineTo(w*.34f,h*.55f); p.close()
                drawPath(p, blue.copy(alpha=.85f), style=stroke)
            }
            "elementalist" -> {
                p.moveTo(w*.28f,h*.68f); p.cubicTo(w*.13f,h*.48f,w*.30f,h*.35f,w*.36f,h*.22f); p.cubicTo(w*.48f,h*.39f,w*.50f,h*.55f,w*.39f,h*.69f); p.close()
                drawPath(p, Color(0xFFFFA45D), style=stroke)
                p.moveTo(w*.58f,h*.22f); p.lineTo(w*.75f,h*.48f); p.lineTo(w*.58f,h*.73f); p.lineTo(w*.43f,h*.48f); p.close()
                drawPath(p, blue, style=stroke)
                line(.62f,.31f,.50f,.52f,purple,sw*1.4f); line(.50f,.52f,.64f,.52f,purple,sw*1.4f); line(.64f,.52f,.53f,.73f,purple,sw*1.4f)
                circle(.30f,.75f,.035f,goldBright,true)
            }
            "spiritmystic" -> {
                p.moveTo(w*.62f,h*.18f); p.cubicTo(w*.34f,h*.18f,w*.25f,h*.43f,w*.37f,h*.65f); p.cubicTo(w*.48f,h*.83f,w*.73f,h*.72f,w*.77f,h*.51f); p.cubicTo(w*.66f,h*.65f,w*.48f,h*.62f,w*.42f,h*.48f); p.cubicTo(w*.35f,h*.34f,w*.47f,h*.22f,w*.62f,h*.18f)
                drawPath(p,purple.copy(alpha=.95f),style=stroke)
                p.moveTo(w*.5f,h*.37f); p.cubicTo(w*.66f,h*.43f,w*.68f,h*.61f,w*.5f,h*.69f); p.cubicTo(w*.32f,h*.61f,w*.34f,h*.43f,w*.5f,h*.37f); drawPath(p,blue,style=stroke)
                circle(.5f,.53f,.045f,goldBright,true)
            }
            "health" -> {
                p.moveTo(w*.5f,h*.77f); p.cubicTo(w*.36f,h*.63f,w*.18f,h*.48f,w*.24f,h*.34f); p.cubicTo(w*.29f,h*.20f,w*.45f,h*.24f,w*.5f,h*.36f); p.cubicTo(w*.55f,h*.24f,w*.71f,h*.20f,w*.76f,h*.34f); p.cubicTo(w*.82f,h*.48f,w*.64f,h*.63f,w*.5f,h*.77f)
                drawPath(p,Color(0xFFFF91A7),style=stroke)
            }
            "shield" -> {
                p.moveTo(w*.5f,h*.17f); p.lineTo(w*.75f,h*.29f); p.lineTo(w*.70f,h*.61f); p.lineTo(w*.5f,h*.80f); p.lineTo(w*.30f,h*.61f); p.lineTo(w*.25f,h*.29f); p.close()
                drawPath(p,blue,style=stroke); line(.5f,.30f,.5f,.65f,goldBright,sw*.8f)
            }
            "critical" -> {
                p.moveTo(w*.5f,h*.15f); p.lineTo(w*.58f,h*.39f); p.lineTo(w*.82f,h*.5f); p.lineTo(w*.58f,h*.59f); p.lineTo(w*.5f,h*.84f); p.lineTo(w*.42f,h*.59f); p.lineTo(w*.18f,h*.5f); p.lineTo(w*.42f,h*.39f); p.close()
                drawPath(p,Color(0xFFFFC46F),style=stroke)
            }
            "check" -> {
                line(.25f,.52f,.43f,.70f,Jade,sw*1.7f); line(.43f,.70f,.77f,.29f,Jade,sw*1.7f)
            }
            "plus" -> {
                line(.5f,.25f,.5f,.75f,goldBright,sw*1.5f); line(.25f,.5f,.75f,.5f,goldBright,sw*1.5f)
            }
            "cultivation", "qi" -> {
                p.moveTo(w*.5f,h*.78f); p.cubicTo(w*.18f,h*.62f,w*.28f,h*.28f,w*.5f,h*.20f)
                p.cubicTo(w*.72f,h*.28f,w*.82f,h*.62f,w*.5f,h*.78f); p.close()
                drawPath(p, blue.copy(alpha=.9f), style=stroke)
                line(.5f,.69f,.5f,.32f, goldBright, sw*.75f)
                line(.5f,.53f,.36f,.42f, goldBright, sw*.65f); line(.5f,.53f,.64f,.42f,goldBright,sw*.65f)
                circle(.5f,.28f,.035f,blue,true)
            }
            "character" -> {
                circle(.5f,.35f,.13f); p.moveTo(w*.25f,h*.78f); p.cubicTo(w*.27f,h*.55f,w*.73f,h*.55f,w*.75f,h*.78f); drawPath(p,goldBright,style=stroke)
                line(.32f,.64f,.68f,.64f,blue,sw*.7f)
            }
            "equipment", "weapon" -> {
                line(.28f,.72f,.72f,.25f); line(.23f,.66f,.34f,.77f); line(.62f,.20f,.78f,.36f)
                p.moveTo(w*.61f,h*.21f); p.lineTo(w*.76f,h*.16f); p.lineTo(w*.80f,h*.31f); drawPath(p,blue,style=stroke)
            }
            "skills", "fire" -> {
                p.moveTo(w*.5f,h*.16f); p.cubicTo(w*.68f,h*.37f,w*.79f,h*.52f,w*.65f,h*.70f); p.cubicTo(w*.55f,h*.85f,w*.27f,h*.76f,w*.28f,h*.55f); p.cubicTo(w*.28f,h*.43f,w*.43f,h*.31f,w*.5f,h*.16f); drawPath(p,Color(0xFFFFB15D),style=stroke)
                p.moveTo(w*.5f,h*.43f); p.cubicTo(w*.65f,h*.58f,w*.56f,h*.72f,w*.46f,h*.69f); drawPath(p,goldBright,style=stroke)
            }
            "pet_fox" -> {
                val ears = Path().apply { moveTo(w*.25f,h*.44f); lineTo(w*.27f,h*.18f); lineTo(w*.45f,h*.34f); lineTo(w*.55f,h*.34f); lineTo(w*.73f,h*.18f); lineTo(w*.75f,h*.44f); cubicTo(w*.78f,h*.72f,w*.22f,h*.72f,w*.25f,h*.44f); close() }
                drawPath(ears, Color(0xFFE2A15F)); drawPath(ears, goldBright, style=stroke)
                p.moveTo(w*.39f,h*.52f); p.lineTo(w*.5f,h*.64f); p.lineTo(w*.61f,h*.52f); p.close(); drawPath(p,Color(0xFFFFE4C2))
                circle(.40f,.48f,.035f,Color(0xFF1A2030),true); circle(.60f,.48f,.035f,Color(0xFF1A2030),true); circle(.5f,.61f,.025f,purple,true)
            }
            "pet_tortoise" -> {
                drawOval(Color(0xFF5F9A80),Offset(w*.26f,h*.32f),Size(w*.48f,h*.42f))
                drawOval(goldBright,Offset(w*.26f,h*.32f),Size(w*.48f,h*.42f),style=stroke)
                drawCircle(Color(0xFF7FB7A0),unit*.12f,Offset(w*.76f,h*.52f))
                for(x in listOf(.30f,.70f)) for(y in listOf(.36f,.68f)) drawRoundRect(Color(0xFF638E75),Offset(w*x,h*y),Size(unit*.14f,unit*.12f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(unit*.04f))
                line(.40f,.43f,.60f,.62f,goldBright,sw*.55f); line(.60f,.43f,.40f,.62f,goldBright,sw*.55f)
                circle(.79f,.49f,.018f,Color(0xFF111827),true)
            }
            "pet_hawk" -> {
                p.moveTo(w*.5f,h*.72f); p.cubicTo(w*.18f,h*.62f,w*.16f,h*.36f,w*.08f,h*.22f); p.cubicTo(w*.32f,h*.27f,w*.42f,h*.37f,w*.5f,h*.47f); p.cubicTo(w*.58f,h*.37f,w*.68f,h*.27f,w*.92f,h*.22f); p.cubicTo(w*.84f,h*.36f,w*.82f,h*.62f,w*.5f,h*.72f); p.close(); drawPath(p,blue,style=stroke)
                line(.5f,.33f,.5f,.77f,goldBright,sw*.8f); line(.5f,.47f,.35f,.34f,goldBright,sw*.65f); line(.5f,.47f,.65f,.34f,goldBright,sw*.65f)
                p.moveTo(w*.5f,h*.35f); p.lineTo(w*.62f,h*.43f); p.lineTo(w*.52f,h*.50f); p.close(); drawPath(p,Color(0xFFFFC46F))
                circle(.47f,.38f,.018f,Color(0xFF101827),true)
            }
            "pet_wolf" -> {
                p.moveTo(w*.27f,h*.42f); p.lineTo(w*.22f,h*.17f); p.lineTo(w*.44f,h*.32f); p.cubicTo(w*.5f,h*.28f,w*.56f,h*.28f,w*.62f,h*.32f); p.lineTo(w*.78f,h*.17f); p.lineTo(w*.73f,h*.43f); p.cubicTo(w*.80f,h*.67f,w*.65f,h*.78f,w*.5f,h*.78f); p.cubicTo(w*.35f,h*.78f,w*.20f,h*.67f,w*.27f,h*.42f); p.close(); drawPath(p,Color(0xFF8C9CB0),style=stroke)
                circle(.40f,.48f,.035f,Color(0xFFFFC46F),true); circle(.60f,.48f,.035f,Color(0xFFFFC46F),true)
                p.moveTo(w*.42f,h*.58f); p.lineTo(w*.5f,h*.68f); p.lineTo(w*.58f,h*.58f); p.close(); drawPath(p,Color(0xFF27354A),style=stroke)
            }
            "pet_deer" -> {
                p.moveTo(w*.36f,h*.40f); p.cubicTo(w*.31f,h*.22f,w*.39f,h*.16f,w*.5f,h*.30f); p.cubicTo(w*.61f,h*.16f,w*.69f,h*.22f,w*.64f,h*.40f); p.cubicTo(w*.74f,h*.70f,w*.26f,h*.70f,w*.36f,h*.40f); p.close(); drawPath(p,Color(0xFFB98B65),style=stroke)
                line(.39f,.36f,.25f,.16f,goldBright); line(.25f,.16f,.18f,.24f,goldBright); line(.25f,.16f,.30f,.09f,goldBright)
                line(.61f,.36f,.75f,.16f,goldBright); line(.75f,.16f,.82f,.24f,goldBright); line(.75f,.16f,.70f,.09f,goldBright)
                circle(.42f,.49f,.022f,Color(0xFF1B2231),true); circle(.58f,.49f,.022f,Color(0xFF1B2231),true)
            }
            "pet_ape" -> {
                drawCircle(Color(0xFF9A6B52),unit*.28f,c)
                drawCircle(Color(0xFFC48B62),unit*.19f,Offset(w*.5f,h*.53f))
                drawCircle(Color(0xFFB97A58),unit*.09f,Offset(w*.22f,h*.48f)); drawCircle(Color(0xFFB97A58),unit*.09f,Offset(w*.78f,h*.48f))
                drawCircle(Color(0xFF1A2030),unit*.025f,Offset(w*.42f,h*.44f)); drawCircle(Color(0xFF1A2030),unit*.025f,Offset(w*.58f,h*.44f))
                line(.42f,.60f,.58f,.60f,goldBright,sw*.8f)
            }
            "pet_koi" -> {
                drawOval(Color(0xFFE6A65D),Offset(w*.23f,h*.34f),Size(w*.48f,h*.32f))
                p.moveTo(w*.68f,h*.50f); p.lineTo(w*.87f,h*.30f); p.lineTo(w*.87f,h*.70f); p.close(); drawPath(p,Color(0xFFE6A65D),style=stroke)
                p.moveTo(w*.42f,h*.35f); p.lineTo(w*.55f,h*.22f); p.lineTo(w*.61f,h*.39f); p.close(); drawPath(p,blue,style=stroke)
                p.moveTo(w*.42f,h*.65f); p.lineTo(w*.55f,h*.78f); p.lineTo(w*.61f,h*.61f); p.close(); drawPath(p,purple,style=stroke)
                for(i in 0..3) circle(.34f+i*.07f,.45f,.018f,Color(0xFFFAE7C5),true)
                circle(.30f,.46f,.02f,Color(0xFF1A2030),true)
            }
            "lock" -> {
                p.moveTo(w*.29f,h*.47f); p.lineTo(w*.71f,h*.47f); p.lineTo(w*.71f,h*.78f); p.lineTo(w*.29f,h*.78f); p.close(); drawPath(p,goldBright,style=stroke)
                p.moveTo(w*.36f,h*.47f); p.lineTo(w*.36f,h*.36f); p.cubicTo(w*.36f,h*.18f,w*.64f,h*.18f,w*.64f,h*.36f); p.lineTo(w*.64f,h*.47f); drawPath(p,blue,style=stroke)
                circle(.5f,.61f,.035f,purple,true)
            }
            "pets", "fox", "deer" -> {
                p.moveTo(w*.29f,h*.37f); p.lineTo(w*.25f,h*.17f); p.lineTo(w*.43f,h*.29f); p.cubicTo(w*.4767f,h*.2567f,w*.5233f,h*.2567f,w*.57f,h*.29f); p.lineTo(w*.75f,h*.17f); p.lineTo(w*.71f,h*.39f); p.cubicTo(w*.7967f,h*.5633f,w*.7133f,h*.6867f,w*.5f,h*.76f); p.cubicTo(w*.2867f,h*.6867f,w*.3633f,h*.5567f,w*.29f,h*.37f); drawPath(p,purple,style=stroke)
                circle(.41f,.49f,.025f,goldBright,true); circle(.59f,.49f,.025f,goldBright,true)
            }
            "inventory", "chest" -> {
                p.moveTo(w*.24f,h*.39f); p.lineTo(w*.76f,h*.39f); p.lineTo(w*.73f,h*.74f); p.lineTo(w*.27f,h*.74f); p.close(); drawPath(p,goldBright,style=stroke)
                p.moveTo(w*.24f,h*.39f); p.cubicTo(w*.4133f,h*.2433f,w*.5867f,h*.2433f,w*.76f,h*.39f); drawPath(p,blue,style=stroke)
                line(.5f,.4f,.5f,.72f); circle(.5f,.54f,.04f,blue)
            }
            "worlds", "portal" -> {
                drawCircle(purple,unit*.27f,c,style=stroke); drawCircle(blue,unit*.18f,c,style=stroke)
                p.moveTo(w*.5f,h*.26f); p.cubicTo(w*.25f,h*.4f,w*.72f,h*.55f,w*.5f,h*.74f); drawPath(p,goldBright,style=stroke)
            }
            "clan", "banner" -> {
                line(.34f,.18f,.34f,.82f); p.moveTo(w*.36f,h*.22f); p.lineTo(w*.76f,h*.31f); p.lineTo(w*.36f,h*.52f); p.close(); drawPath(p,goldBright,style=stroke)
                circle(.34f,.18f,.04f,blue,true)
            }
            "abode", "temple" -> {
                p.moveTo(w*.19f,h*.42f); p.lineTo(w*.5f,h*.20f); p.lineTo(w*.81f,h*.42f); p.close(); drawPath(p,goldBright,style=stroke)
                line(.27f,.45f,.73f,.45f); line(.32f,.47f,.32f,.72f); line(.68f,.47f,.68f,.72f); line(.25f,.75f,.75f,.75f,blue)
            }
            "alchemy", "cauldron" -> {
                p.moveTo(w*.25f,h*.42f); p.cubicTo(w*.4167f,h*.36f,w*.5833f,h*.36f,w*.75f,h*.42f); p.lineTo(w*.68f,h*.69f); p.cubicTo(w*.56f,h*.7833f,w*.44f,h*.7833f,w*.32f,h*.69f); p.close(); drawPath(p,goldBright,style=stroke)
                p.moveTo(w*.42f,h*.32f); p.cubicTo(w*.3467f,h*.22f,w*.4267f,h*.1567f,w*.45f,h*.13f); p.moveTo(w*.57f,h*.32f); p.cubicTo(w*.65f,h*.22f,w*.6033f,h*.15f,w*.56f,h*.11f); drawPath(p,purple,style=stroke)
            }
            "forge", "anvil" -> {
                p.moveTo(w*.24f,h*.42f); p.lineTo(w*.76f,h*.42f); p.lineTo(w*.64f,h*.56f); p.lineTo(w*.57f,h*.76f); p.lineTo(w*.4f,h*.76f); p.lineTo(w*.36f,h*.56f); p.close(); drawPath(p,goldBright,style=stroke)
                line(.5f,.18f,.5f,.35f,Color(0xFFFFA45D)); line(.39f,.27f,.61f,.27f,Color(0xFFFFA45D))
            }
            "arena", "sword" -> {
                line(.27f,.72f,.72f,.25f); line(.23f,.61f,.39f,.77f); line(.61f,.20f,.79f,.38f,blue)
                line(.26f,.27f,.72f,.73f,purple); line(.20f,.33f,.34f,.19f,purple); line(.66f,.80f,.80f,.66f,purple)
            }
            "auction", "scales" -> {
                line(.5f,.22f,.5f,.75f); line(.3f,.35f,.7f,.35f); line(.34f,.35f,.24f,.58f); line(.34f,.35f,.44f,.58f); line(.66f,.35f,.56f,.58f); line(.66f,.35f,.76f,.58f); line(.33f,.75f,.67f,.75f)
                p.moveTo(w*.17f,h*.59f); p.cubicTo(w*.2833f,h*.67f,w*.3967f,h*.67f,w*.51f,h*.59f); p.moveTo(w*.49f,h*.59f); p.cubicTo(w*.6033f,h*.67f,w*.7167f,h*.67f,w*.83f,h*.59f); drawPath(p,goldBright,style=stroke)
            }
            "quests", "scroll" -> {
                p.moveTo(w*.31f,h*.2f); p.lineTo(w*.68f,h*.2f); p.lineTo(w*.68f,h*.75f); p.lineTo(w*.31f,h*.75f); p.close(); drawPath(p,goldBright,style=stroke)
                line(.39f,.37f,.61f,.37f,blue); line(.39f,.49f,.61f,.49f); line(.39f,.61f,.57f,.61f)
            }
            "shop" -> {
                p.moveTo(w*.25f,h*.4f); p.lineTo(w*.75f,h*.4f); p.lineTo(w*.69f,h*.75f); p.lineTo(w*.31f,h*.75f); p.close(); drawPath(p,goldBright,style=stroke)
                p.moveTo(w*.35f,h*.4f); p.cubicTo(w*.35f,h*.2533f,w*.4f,h*.18f,w*.5f,h*.18f); p.cubicTo(w*.6f,h*.18f,w*.65f,h*.2533f,w*.65f,h*.4f); drawPath(p,blue,style=stroke)
            }
            "tower" -> {
                line(.5f,.18f,.5f,.79f); line(.31f,.34f,.69f,.34f); line(.36f,.52f,.64f,.52f); line(.27f,.77f,.73f,.77f)
                p.moveTo(w*.31f,h*.34f); p.lineTo(w*.5f,h*.2f); p.lineTo(w*.69f,h*.34f); drawPath(p,goldBright,style=stroke)
            }
            else -> {
                circle(.5f,.5f,.2f,blue); line(.5f,.25f,.5f,.75f); line(.25f,.5f,.75f,.5f)
            }
        }
        drawCircle(goldBright.copy(alpha=.8f), unit*.025f, Offset(w*.78f,h*.20f))
    }
}

@Composable
private fun TechniqueRow(
    name: String,
    description: String,
    iconKey: String,
    selected: Boolean,
    level: Int,
    cost: Int,
    stones: Int,
    onSelect: () -> Unit,
    onUpgrade: () -> Unit
) {
    val progress = (level.coerceIn(0, 50) / 50f)
    val accent = if (selected) Gold else Color(0xFF71869C)
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
        color = if (selected) Color(0xFF263F46) else Color(0xE91A2938),
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(if (selected) 1.6.dp else 1.dp, accent.copy(alpha = if (selected) .95f else .42f)),
        tonalElevation = if (selected) 8.dp else 2.dp,
        shadowElevation = if (selected) 7.dp else 1.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(Modifier.size(70.dp), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF101B2B),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, accent.copy(alpha = .72f))
                ) {}
                RpgIcon(iconKey, Modifier.size(57.dp))
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    color = accent,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color(0xFF101820))
                ) {
                    Text("$level", Modifier.padding(horizontal = 6.dp, vertical = 3.dp), color = Color(0xFF101820), fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, color = if (selected) Gold else Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    if (selected) Text("АКТИВНА", color = Jade, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
                Text(description, color = Color(0xFFB8C7D5), fontSize = 9.sp, maxLines = 2)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.weight(1f).height(5.dp),
                        color = accent,
                        trackColor = Color(0xFF101820)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text("$level / 50", color = Color(0xFFD5DFE8), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = Color(0xFF111F2D), shape = CircleShape) {
                        Text(if (level >= 20) "РЕДКИЙ РЕСУРС" else "✦ $cost КАМНЕЙ", Modifier.padding(horizontal = 7.dp, vertical = 4.dp), color = Gold, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = onUpgrade,
                        enabled = stones >= cost && level < 50,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(29.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF80623B), contentColor = Color.White, disabledContainerColor = Color(0xFF35404B))
                    ) {
                        Text(if (level >= 50) "МАКС." else "УСИЛИТЬ", fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }
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
    val tint = when (world) {
        1 -> Color(0xFF426B9B).copy(alpha = .15f)
        2 -> Color(0xFF7952A6).copy(alpha = .20f)
        3 -> Color(0xFF9A5BC1).copy(alpha = .24f)
        else -> Color(0xFF173F50).copy(alpha = .08f)
    }
    Box(Modifier.fillMaxWidth().height(248.dp)) {
        Image(
            painter = painterResource(id = R.drawable.lunar_harbor_bg),
            contentDescription = "Лунная Гавань — панорама игрового мира",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().graphicsLayer {
                val zoom = 1.015f + lanternPulse * .012f
                scaleX = zoom
                scaleY = zoom
                translationX = cloudDrift * 28f
            }
        )
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(tint)
            drawOval(Color(0xFFB9E9E5).copy(alpha = mistDrift * .18f), Offset(w * (.02f + cloudDrift), h * .62f), Size(w * .76f, h * .11f))
            drawOval(Color(0xFFB9E9E5).copy(alpha = mistDrift * .12f), Offset(w * (.28f - cloudDrift), h * .76f), Size(w * .64f, h * .08f))
            for (i in 0..22) {
                val x = ((i * .137f + cloudDrift + 1f) % 1f) * w
                val y = h * (.22f + ((i * 37) % 65) / 100f) + mistDrift * h * .10f
                val radius = if (i % 4 == 0) 3.2f else 1.8f
                val color = if (i % 3 == 0) Color(0xFFFFC8DE) else Color(0xFFB9F7E7)
                drawCircle(color.copy(alpha = .30f + lanternPulse * .32f), radius, Offset(x, y))
            }
            drawRect(brush = Brush.verticalGradient(0f to Color(0x220B1422), .55f to Color.Transparent, 1f to Color(0x99101820)))
            if (storyChapter > 0) drawCircle(Color(0xFF9CFFE6).copy(alpha = lanternPulse * .12f), w * .12f, Offset(w * .78f, h * .34f))
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("ЛУННАЯ ГАВАНЬ  ·  ${worldName(stage).uppercase()}", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Text("СТУПЕНЬ $stage", color = Jade, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

private fun factionEmblemResource(index: Int): Int = when (index) {
    0 -> R.drawable.faction_crest_mountain
    1 -> R.drawable.faction_crest_maple
    2 -> R.drawable.faction_crest_lotus
    3 -> R.drawable.faction_crest_river
    4 -> R.drawable.faction_crest_wolf
    5 -> R.drawable.faction_crest_gate
    6 -> R.drawable.faction_crest_sword
    else -> R.drawable.faction_crest_flame
}

@Composable
private fun SectHallScreen(
    stage: Int, selectedAlliance: Int, chosenSect: Int, reputation: Int, dailyDonated: Boolean, stones: Int,
    factionNames: List<String>, factionLore: List<String>, factionAlliances: List<Int>,
    attackBonuses: List<Int>, defenseBonuses: List<Int>, hpBonuses: List<Int>,
    onAllianceSelected: (Int) -> Unit, onJoinFaction: (Int) -> Unit, onDonate: () -> Unit
) {
    val mortal = stage < 7
    val allianceNames = if (mortal) listOf("СОЮЗ ДОЛИН", "ВОЛЬНЫЕ ДОМА") else listOf("ПРАВЕДНЫЙ ПУТЬ", "ТЁМНЫЙ ПУТЬ")
    val ambient = rememberInfiniteTransition(label = "sect-hall-ambient")
    val lanternPulse by ambient.animateFloat(.38f, .92f, infiniteRepeatable(tween(1900), RepeatMode.Reverse), label = "sect-hall-pulse")
    val visibleFactions = factionNames.indices.filter { selectedAlliance < 0 || factionAlliances.getOrElse(it) { 0 } == selectedAlliance }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.fillMaxWidth().height(194.dp).clip(RoundedCornerShape(22.dp))) {
            Image(painterResource(R.drawable.sect_hall_scene), if (mortal) "Зал кланов Лунной Гавани" else "Зал духовных сект", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color(0xAA071321), .42f to Color(0x24101B2A), 1f to Color(0xF20A1423))))
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width; val h = size.height
                drawCircle(Color(0xFFE5BD72).copy(alpha = .10f + lanternPulse * .08f), w * .20f, Offset(w * .79f, h * .36f))
                drawCircle(Color(0xFF8DDCC1).copy(alpha = .20f + lanternPulse * .22f), w * .10f, Offset(w * .79f, h * .36f), style = Stroke(2.dp.toPx()))
                for (i in 0..25) {
                    val x = w * (((i * 41) % 97) / 97f); val y = h * (.12f + ((i * 31) % 78) / 100f)
                    drawCircle((if (i % 3 == 0) Color(0xFFFFD995) else Color(0xFF8DDEC5)).copy(alpha = .12f + lanternPulse * .24f), if (i % 6 == 0) 2.8f else 1.35f, Offset(x, y))
                }
                drawLine(Brush.horizontalGradient(listOf(Color.Transparent, Color(0xFFD9B875).copy(alpha = .58f), Color.Transparent)), Offset(w * .06f, h * .74f), Offset(w * .94f, h * .74f), 1.4.dp.toPx())
            }
            Column(Modifier.align(Alignment.TopStart).padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(if (mortal) "ЗАЛ КЛАНОВ" else "ЗАЛ ДУХОВНЫХ СЕКТ", color = Color(0xFFFFD995), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
                Text(if (mortal) "Клятвы долины и наследие родов" else "Праведный и тёмный пути Дао", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
                Text("${worldName(stage)} · ступень $stage", color = Color(0xFFD3E1E8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Row(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(13.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("РЕПУТАЦИЯ", color = Color(0xFFB9C9D8), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(reputation.toString(), color = Color(0xFFFFE3A4), fontSize = 23.sp, fontWeight = FontWeight.Black)
                }
                Surface(color = Color(0xDD142B35), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Jade.copy(alpha = .72f))) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RpgIcon("clan", Modifier.size(23.dp))
                        Text(if (chosenSect >= 0) "СОЮЗ ЗАКЛЮЧЁН" else "ИЩИ ПОКРОВИТЕЛЯ", color = Jade, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        Surface(Modifier.fillMaxWidth(), color = Color(0xE9162738), shape = RoundedCornerShape(15.dp), border = BorderStroke(1.dp, Color(0x5578C6A3))) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("КАМНИ ДУХА", color = Color(0xFF9FB4C6), fontSize = 7.sp, fontWeight = FontWeight.Black)
                    Text(stones.toString(), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("ТВОЁ ОБЪЕДИНЕНИЕ", color = Color(0xFF9FB4C6), fontSize = 7.sp, fontWeight = FontWeight.Black)
                    Text(if (chosenSect in factionNames.indices) factionNames[chosenSect] else "Пока не выбрано", color = if (chosenSect >= 0) Gold else Color(0xFFB8C7D5), fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }

        Text("ВЫБЕРИ СТОРОНУ", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.3.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SectAllianceTab("ВСЕ ПУТИ", "8 наследий", selectedAlliance == -1, Modifier.weight(.78f)) { onAllianceSelected(-1) }
            SectAllianceTab(allianceNames[0], "Союз света", selectedAlliance == 0, Modifier.weight(1f)) { onAllianceSelected(0) }
            SectAllianceTab(allianceNames[1], "Второй союз", selectedAlliance == 1, Modifier.weight(1f)) { onAllianceSelected(1) }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("НАСЛЕДИЯ И ОБИТЕЛИ", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text("${visibleFactions.size} найдено", color = Color(0xFF9FB3C4), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
        visibleFactions.forEach { index ->
            SectFactionCard(
                index, factionNames[index], factionLore.getOrElse(index) { "" },
                attackBonuses.getOrElse(index) { 0 }, defenseBonuses.getOrElse(index) { 0 }, hpBonuses.getOrElse(index) { 0 },
                chosenSect == index, selectedAlliance == factionAlliances.getOrElse(index) { 0 }
            ) { onJoinFaction(index) }
        }

        Surface(Modifier.fillMaxWidth(), color = Color(0xE91B2D3C), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, Color(0x66D9B875))) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Image(painterResource(R.drawable.town_building_skills), "Знак ежедневного вклада", Modifier.size(58.dp), contentScale = ContentScale.Fit)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("ЕЖЕДНЕВНАЯ ПРИСЯГА", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    Text("50 камней духа · +35 Ци · +1 репутация", color = Color(0xFFD3DFE8), fontSize = 9.sp)
                    Text(when { chosenSect < 0 -> "Сначала выбери своё объединение"; dailyDonated -> "Сегодняшний вклад уже принят"; stones < 50 -> "Не хватает ${50 - stones} камней духа"; else -> "Старейшины ждут твоего вклада" }, color = if (dailyDonated) Jade else Color(0xFFB9C8D7), fontSize = 8.sp)
                }
                Button(onClick = onDonate, enabled = chosenSect >= 0 && !dailyDonated && stones >= 50, modifier = Modifier.height(38.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF806039), contentColor = Color.White, disabledContainerColor = Color(0xFF35404B))) {
                    Text(if (dailyDonated) "ВНЕСЕНО" else "ВНЕСТИ", fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun SectAllianceTab(title: String, subtitle: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier.clickable(onClick = onClick), color = if (selected) Color(0xFF294A43) else Color(0xE9162738), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, if (selected) Gold.copy(alpha = .85f) else Color(0x445F8293))) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = if (selected) Color(0xFFFFE5AE) else Color(0xFFD2DDE6), fontSize = 7.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(subtitle, color = if (selected) Jade else Color(0xFF91A5B6), fontSize = 6.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectFactionCard(
    index: Int, name: String, lore: String, attack: Int, defense: Int, hp: Int,
    joined: Boolean, allianceSelected: Boolean, onJoin: () -> Unit
) {
    Surface(Modifier.fillMaxWidth(), color = if (joined) Color(0xE92A453F) else Color(0xE9182939), shape = RoundedCornerShape(18.dp), border = BorderStroke(if (joined) 1.8.dp else 1.dp, if (joined) Gold.copy(alpha = .9f) else Color(0x556D879B)), tonalElevation = if (joined) 7.dp else 2.dp, shadowElevation = if (joined) 6.dp else 1.dp) {
        Column(Modifier.fillMaxWidth().padding(11.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(76.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) { drawCircle(Brush.radialGradient(listOf(if (joined) Color(0x668BE0BC) else Color(0x334E7E91), Color.Transparent)), size.minDimension * .5f) }
                    Image(painterResource(factionEmblemResource(index)), "Герб: $name", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("НАСЛЕДИЕ ${index + 1}", color = if (joined) Gold else Jade, fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = .8.sp)
                    Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Text(lore, color = Color(0xFFB9C8D7), fontSize = 8.sp, lineHeight = 11.sp)
                }
                if (joined) Surface(color = Color(0xFF173E36), shape = RoundedCornerShape(9.dp), border = BorderStroke(1.dp, Jade.copy(alpha = .8f))) {
                    Text("ТВОЙ ПУТЬ", Modifier.padding(horizontal = 7.dp, vertical = 5.dp), color = Jade, fontSize = 7.sp, fontWeight = FontWeight.Black)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                SectBonusChip("АТАКА", "+$attack", Color(0xFFFFC27D), Modifier.weight(1f))
                SectBonusChip("ЗАЩИТА", "+$defense", Color(0xFF8EDBC1), Modifier.weight(1f))
                SectBonusChip("ЗДОРОВЬЕ", "+$hp", Color(0xFFFF9C9C), Modifier.weight(1f))
            }
            Button(onClick = onJoin, enabled = allianceSelected && !joined, modifier = Modifier.fillMaxWidth().height(36.dp), contentPadding = PaddingValues(vertical = 0.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF735B3A), contentColor = Color.White, disabledContainerColor = if (joined) Color(0xFF285246) else Color(0xFF35404B))) {
                Text(when { joined -> "ТВОЁ ОБЪЕДИНЕНИЕ"; !allianceSelected -> "ВЫБЕРИ СТОРОНУ ДЛЯ ВСТУПЛЕНИЯ"; else -> "ПРИНЯТЬ НАСЛЕДИЕ" }, fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun SectBonusChip(title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(modifier, color = Color(0xFF101D2B), shape = RoundedCornerShape(9.dp), border = BorderStroke(1.dp, accent.copy(alpha = .32f))) {
        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 3.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, color = Color(0xFF9FB0C0), fontSize = 6.sp, fontWeight = FontWeight.Black)
            Text(value, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun AbodeScene(
    level: Int,
    world: String,
    section: String,
    onSectionSelected: (String) -> Unit
) {
    val transition = rememberInfiniteTransition(label = "abode-estate-map")
    val pulse by transition.animateFloat(
        initialValue = 0.24f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(tween(1900), repeatMode = RepeatMode.Reverse),
        label = "abode-map-spirit-pulse"
    )
    val sectionTitle = when (section) {
        "forge" -> "КУЗНИЦА"
        "alchemy" -> "АЛХИМИЧЕСКИЙ ПАВИЛЬОН"
        "garden" -> "САД ДУХОВНЫХ ТРАВ"
        "spring" -> "РОДНИК ЧИСТОЙ ЦИ"
        "worlds" -> "ВРАТА ЧЕТЫРЁХ МИРОВ"
        else -> "ПЕЩЕРА МЕДИТАЦИИ"
    }
    val accent = when (section) {
        "forge" -> Color(0xFFFFB56E)
        "alchemy" -> Color(0xFFC2A0FF)
        "garden" -> Color(0xFF9EDB91)
        "spring" -> Color(0xFF75DDE8)
        "worlds" -> Color(0xFFB8A5F4)
        else -> Jade
    }

    Surface(
        modifier = Modifier.fillMaxWidth().height(286.dp),
        color = Color(0xFF101B2B),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Gold.copy(alpha = .78f)),
        shadowElevation = 7.dp
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.abode_estate_map),
                contentDescription = "Общая карта личной обители: павильон, кузница, алхимическая мастерская, сад, родник и врата миров",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color(0xAA0B1827),
                        .28f to Color(0x22101820),
                        .72f to Color(0x33101820),
                        1f to Color(0xCC0B1723)
                    )
                )
            )
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w * .5f, h * .49f)
                drawCircle(
                    Brush.radialGradient(
                        listOf(accent.copy(alpha = .12f + pulse * .12f), Color.Transparent),
                        center,
                        w * .34f
                    ),
                    w * .34f,
                    center
                )
                for (i in 0..29) {
                    val phase = (pulse + i * .173f) % 1f
                    val x = w * (((i * 43) % 97) / 97f)
                    val y = h * (.12f + phase * .72f)
                    drawCircle(
                        if (i % 4 == 0) accent.copy(alpha = .16f + pulse * .18f)
                        else Color(0xFFFFE0A0).copy(alpha = .10f + pulse * .13f),
                        if (i % 6 == 0) 2.4.dp.toPx() else 1.2.dp.toPx(),
                        Offset(x, y)
                    )
                }
            }

            Row(
                Modifier.align(Alignment.TopStart).fillMaxWidth().padding(9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = Color(0xE8101D2B),
                    shape = RoundedCornerShape(11.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = .7f))
                ) {
                    Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text("ТЕРРИТОРИЯ КУЛЬТИВАТОРА", color = accent, fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Text("Личная обитель", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                }
                Surface(
                    color = Color(0xE8101D2B),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Gold.copy(alpha = .66f))
                ) {
                    Text(
                        "УР. $level  ·  $world",
                        color = Gold,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp)
                    )
                }
            }

            BoxWithConstraints(Modifier.fillMaxSize()) {
                val left = maxWidth * .025f
                val middle = maxWidth * .37f
                val right = maxWidth * .715f
                AbodeMapHotspot(
                    title = "Кузница",
                    subtitle = "Закалка снаряжения",
                    symbol = "forge",
                    selected = section == "forge",
                    modifier = Modifier.align(Alignment.TopStart).offset(x = left, y = 43.dp)
                ) { onSectionSelected("forge") }
                AbodeMapHotspot(
                    title = "Медитация",
                    subtitle = "Пещера уединения",
                    symbol = "sanctuary",
                    selected = section == "sanctuary",
                    modifier = Modifier.align(Alignment.TopStart).offset(x = middle, y = 43.dp)
                ) { onSectionSelected("sanctuary") }
                AbodeMapHotspot(
                    title = "Алхимия",
                    subtitle = "Печь Девяти трав",
                    symbol = "alchemy",
                    selected = section == "alchemy",
                    modifier = Modifier.align(Alignment.TopStart).offset(x = right, y = 43.dp)
                ) { onSectionSelected("alchemy") }
                AbodeMapHotspot(
                    title = "Сад",
                    subtitle = "Духовные травы",
                    symbol = "garden",
                    selected = section == "garden",
                    modifier = Modifier.align(Alignment.TopStart).offset(x = left, y = 132.dp)
                ) { onSectionSelected("garden") }
                AbodeMapHotspot(
                    title = "Миры Дао",
                    subtitle = "Переходы между мирами",
                    symbol = "worlds",
                    selected = section == "worlds",
                    modifier = Modifier.align(Alignment.TopStart).offset(x = middle, y = 132.dp)
                ) { onSectionSelected("worlds") }
                AbodeMapHotspot(
                    title = "Родник",
                    subtitle = "Сбор духовной воды",
                    symbol = "spring",
                    selected = section == "spring",
                    modifier = Modifier.align(Alignment.TopStart).offset(x = right, y = 132.dp)
                ) { onSectionSelected("spring") }
            }

            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 7.dp),
                color = Color(0xE8101C2B),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, accent.copy(alpha = .5f))
            ) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(Modifier.size(6.dp).background(accent, CircleShape))
                    Text(
                        "$sectionTitle  ·  коснись здания, чтобы войти",
                        color = Color(0xFFE8E4D8),
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    Text(
        "Обитель · уровень $level · $world",
        color = Gold,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun AbodeMapHotspot(
    title: String,
    subtitle: String,
    symbol: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "abode-map-node-$symbol")
    val pulse by transition.animateFloat(
        initialValue = .24f,
        targetValue = .72f,
        animationSpec = infiniteRepeatable(tween(1600), repeatMode = RepeatMode.Reverse),
        label = "abode-map-node-pulse-$symbol"
    )
    val borderColor = if (selected) Gold else Color(0xFFB8C6C6).copy(alpha = .62f)
    Surface(
        modifier = modifier.width(74.dp).height(76.dp).clickable(onClick = onClick),
        color = if (selected) Color(0xF02C4B43) else Color(0xE9162734),
        shape = RoundedCornerShape(13.dp),
        border = BorderStroke(if (selected) 1.8.dp else 1.dp, borderColor),
        tonalElevation = if (selected) 7.dp else 3.dp,
        shadowElevation = if (selected) 8.dp else 4.dp
    ) {
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.matchParentSize()) {
                val center = Offset(size.width * .5f, size.height * .43f)
                drawCircle(
                    Brush.radialGradient(
                        listOf(
                            (if (selected) Gold else Color(0xFF79D7C5)).copy(alpha = pulse * .46f),
                            Color.Transparent
                        ),
                        center,
                        size.minDimension * .62f
                    ),
                    size.minDimension * .62f,
                    center
                )
                if (selected) {
                    drawCircle(
                        Gold.copy(alpha = .28f + pulse * .22f),
                        size.minDimension * .39f,
                        center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
            Column(
                Modifier.fillMaxSize().padding(horizontal = 3.dp, vertical = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(townBuildingResource(symbol)),
                    contentDescription = "$title — $subtitle",
                    modifier = Modifier.size(43.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    title,
                    color = if (selected) Gold else Color(0xFFF1E7D0),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun BattleScene(
    autoBattle: Boolean,
    actionText: String,
    bossLevel: Int,
    playerName: String,
    playerHp: Int,
    playerMaxHp: Int,
    enemyHp: Int,
    enemyMaxHp: Int,
    playerPortraitRes: Int,
    heroArtworkRes: Int,
    petName: String,
    petPortraitRes: Int,
    enemyPortraitRes: Int,
    arenaBackgroundRes: Int,
    enemyAccent: Color,
    enemyTitle: String,
    incomingAbilityName: String,
    telegraphActive: Boolean,
    guardPrepared: Boolean,
    telegraphAccent: Color
) {
    val transition = rememberInfiniteTransition(label = "cinematic-battle")
    val heroBreath by transition.animateFloat(
        -1f, 1f,
        infiniteRepeatable(tween(1750), RepeatMode.Reverse),
        label = "battle-hero-breath"
    )
    val enemyHover by transition.animateFloat(
        -1f, 1f,
        infiniteRepeatable(tween(1450), RepeatMode.Reverse),
        label = "battle-enemy-hover"
    )
    val petHover by transition.animateFloat(
        -1f, 1f,
        infiniteRepeatable(tween(2050), RepeatMode.Reverse),
        label = "battle-pet-hover"
    )
    val runeRotation by transition.animateFloat(
        0f, 360f,
        infiniteRepeatable(
            tween(32000, easing = androidx.compose.animation.core.LinearEasing),
            RepeatMode.Restart
        ),
        label = "battle-arena-rune-rotation"
    )
    val impactProgress = remember { Animatable(1f) }
    val strikeLunge = remember { Animatable(0f) }
    val arenaShake = remember { Animatable(0f) }
    val playerHitProgress = remember { Animatable(1f) }
    val guardImpactProgress = remember { Animatable(1f) }
    var previousPlayerHp by remember { mutableIntStateOf(playerHp) }
    val enemyWindup by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(430), RepeatMode.Reverse),
        label = "battle-enemy-telegraph-windup"
    )
    val guardImpact = actionText.contains("Защитная стойка смягчила", ignoreCase = true)
    LaunchedEffect(playerHp) {
        if (playerHp < previousPlayerHp) {
            playerHitProgress.snapTo(0f)
            playerHitProgress.animateTo(1f, tween(440))
        }
        previousPlayerHp = playerHp
    }
    LaunchedEffect(actionText) {
        if (guardImpact) {
            guardImpactProgress.snapTo(0f)
            guardImpactProgress.animateTo(1f, tween(520))
        } else if (!guardPrepared) {
            guardImpactProgress.snapTo(1f)
        }
    }
    val combatAction = actionText.contains("урон", ignoreCase = true) ||
        actionText.contains("КРИТИЧЕСКИЙ УДАР", ignoreCase = true) ||
        actionText.contains("Рёв Каменного Тигра", ignoreCase = true) ||
        actionText.contains("Корни Нефритового Пика", ignoreCase = true) ||
        actionText.contains("Приговор Небесного Дракона", ignoreCase = true) ||
        actionText.contains("Разлом Пустоты", ignoreCase = true)
    val impactRes = when {
        actionText.contains("Разлом Пустоты", ignoreCase = true) -> R.drawable.battle_impact_chaos
        actionText.contains("Корни Нефритового Пика", ignoreCase = true) -> R.drawable.battle_impact_spirit
        actionText.contains("Приговор Небесного Дракона", ignoreCase = true) -> R.drawable.battle_impact_critical
        actionText.contains("Рёв Каменного Тигра", ignoreCase = true) -> R.drawable.battle_impact_iron
        actionText.contains("КРИТИЧЕСКИЙ УДАР", ignoreCase = true) -> R.drawable.battle_impact_critical
        playerPortraitRes == R.drawable.class_portrait_ironbody -> R.drawable.battle_impact_iron
        playerPortraitRes == R.drawable.class_portrait_elementalist -> R.drawable.battle_impact_elemental
        playerPortraitRes == R.drawable.class_portrait_spiritmystic -> R.drawable.battle_impact_spirit
        else -> R.drawable.battle_impact_blade
    }
    val impactDamage = Regex("урон\\s+(\\d+)").find(actionText)?.groupValues?.getOrNull(1).orEmpty()
    val signatureRes = when (playerPortraitRes) {
        R.drawable.class_portrait_ironbody -> R.drawable.battle_signature_iron
        R.drawable.class_portrait_elementalist -> R.drawable.battle_signature_elemental
        R.drawable.class_portrait_spiritmystic -> R.drawable.battle_signature_spirit
        else -> R.drawable.battle_signature_blade
    }
    LaunchedEffect(actionText, enemyHp, playerHp) {
        if (combatAction) {
            impactProgress.snapTo(0f)
            strikeLunge.snapTo(0f)
            arenaShake.snapTo(0f)
            launch {
                impactProgress.animateTo(1f, tween(620))
            }
            launch {
                strikeLunge.animateTo(1f, tween(125))
                strikeLunge.animateTo(0f, tween(255))
            }
            launch {
                arenaShake.animateTo(1f, tween(55))
                arenaShake.animateTo(0f, tween(230))
            }
        } else {
            impactProgress.snapTo(1f)
            strikeLunge.snapTo(0f)
            arenaShake.snapTo(0f)
        }
    }
    val pulse by transition.animateFloat(
        .38f, 1f,
        infiniteRepeatable(tween(if (autoBattle) 520 else 1050), RepeatMode.Reverse),
        label = "battle-core-pulse"
    )
    val sweep by transition.animateFloat(
        -.10f, .10f,
        infiniteRepeatable(tween(if (autoBattle) 650 else 1700), RepeatMode.Reverse),
        label = "battle-energy-sweep"
    )
    val camera by transition.animateFloat(
        1f, 1.035f,
        infiniteRepeatable(tween(4200), RepeatMode.Reverse),
        label = "battle-camera"
    )
    val playerRatio = (playerHp.toFloat() / playerMaxHp.coerceAtLeast(1)).coerceIn(0f, 1f)
    val enemyRatio = (enemyHp.toFloat() / enemyMaxHp.coerceAtLeast(1)).coerceIn(0f, 1f)
    val bossPhase = when {
        enemyRatio <= .33f -> 3
        enemyRatio <= .66f -> 2
        else -> 1
    }
    val phaseAccent = when (bossPhase) {
        3 -> Color(0xFFC38AFF)
        2 -> Color(0xFFFF986F)
        else -> enemyAccent
    }
    val phaseTitle = when (bossPhase) {
        3 -> "ПРЕДЕЛ · ПРОБУЖДЕНИЕ ЯДРА"
        2 -> "НАТИСК · РАСКРЫТИЕ СИЛЫ"
        else -> "ИСПЫТАНИЕ · ИЗУЧЕНИЕ ПРОТИВНИКА"
    }
    val phasePulse by transition.animateFloat(
        .96f, 1.045f,
        infiniteRepeatable(tween(if (bossPhase == 3) 420 else 1050), RepeatMode.Reverse),
        label = "battle-boss-phase-pulse"
    )
    val bossName = enemyTitle.ifBlank {
        when {
            bossLevel >= 20 -> "Владыка Разлома"
            bossLevel >= 10 -> "Демон Горного Пика"
            bossLevel >= 5 -> "Древний Страж"
            else -> "Страж Святилища"
        }
    }

    Box(
        Modifier.fillMaxWidth().height(326.dp).graphicsLayer {
            val kick = arenaShake.value
            translationX = kotlin.math.sin((kick * 42f).toDouble()).toFloat() * kick * 8f
            translationY = kotlin.math.cos((kick * 31f).toDouble()).toFloat() * kick * 2.5f
        }
    ) {
        Image(
            painter = painterResource(id = arenaBackgroundRes),
            contentDescription = "Лунная арена у горного святилища",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().graphicsLayer {
                scaleX = camera
                scaleY = camera
            }
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color(0xD0091428),
                    .30f to Color(0x44101820),
                    .72f to Color(0x22101820),
                    1f to Color(0xF20A1220)
                )
            )
        )
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w * .5f, h * .78f)
            drawCircle(Color(0xFF7EE5D3).copy(alpha = .16f * pulse), w * .22f, center)
            drawCircle(
                Color(0xFFB8F5E8).copy(alpha = .58f * pulse),
                w * .17f,
                center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.2f)
            )
            drawCircle(
                Color(0xFFE5BD72).copy(alpha = .68f),
                w * .125f,
                center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
            )
            for (i in 0..11) {
                val angle = i * .523599f
                val radius = w * if (i % 3 == 0) .17f else .125f
                val x = center.x + kotlin.math.cos(angle) * radius
                val y = center.y + kotlin.math.sin(angle) * radius * .55f
                drawCircle(Color(0xFFFFD993).copy(alpha = .55f + pulse * .25f), if (i % 3 == 0) 3.2f else 1.8f, Offset(x, y))
            }
            val clash = Offset(w * (.50f + sweep), h * (.49f - sweep * .25f))
            drawLine(
                brush = Brush.linearGradient(
                    listOf(Color.Transparent, Color(0xFF7EE9E0).copy(alpha = .88f * pulse), Color(0xFFFFD58B).copy(alpha = .72f * pulse), Color.Transparent)
                ),
                start = Offset(w * .26f, h * .48f),
                end = Offset(w * .76f, h * .47f),
                strokeWidth = 4.5f + pulse * 2.5f
            )
            drawCircle(Color(0xFF9BFFE9).copy(alpha = .22f * pulse), w * .105f, clash)
            drawCircle(Color(0xFFFFE5A5).copy(alpha = .78f * pulse), w * .025f, clash)
            for (i in 0..18) {
                val x = w * (((i * 47) % 97) / 97f)
                val y = h * (.26f + ((i * 31) % 60) / 100f)
                drawCircle(
                    if (i % 2 == 0) Color(0xFF8DE5E0).copy(alpha = .25f + pulse * .45f)
                    else Color(0xFFFFD58A).copy(alpha = .25f + pulse * .35f),
                    if (i % 4 == 0) 2.8f else 1.4f,
                    Offset(x, y)
                )
            }
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Transparent,
                    .70f to Color.Transparent,
                    1f to Color(0xE80A1220)
                )
            )
        }

        Image(
            painter = painterResource(R.drawable.battle_arena_rune),
            contentDescription = "Древняя печать боевой арены",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(246.dp)
                .graphicsLayer {
                    rotationZ = runeRotation
                    alpha = .16f + pulse * .075f
                    scaleX = phasePulse
                    scaleY = phasePulse
                }
        )

        Row(
            Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Top
        ) {
            BattleHealthPanel(
                modifier = Modifier.weight(1f),
                title = "СТРАННИК",
                subtitle = playerName,
                current = playerHp,
                maximum = playerMaxHp,
                ratio = playerRatio,
                barColor = Jade,
                isDefeated = playerHp <= 0
            )
            BattleHealthPanel(
                modifier = Modifier.weight(1f),
                title = "СТРАЖ · РАНГ $bossLevel",
                subtitle = bossName,
                current = enemyHp,
                maximum = enemyMaxHp,
                ratio = enemyRatio,
                barColor = enemyAccent,
                isDefeated = enemyHp <= 0
            )
        }

        if (!telegraphActive && enemyHp > 0 && playerHp > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 62.dp)
                    .graphicsLayer {
                        scaleX = phasePulse
                        scaleY = phasePulse
                    },
                color = Color(0xE8101A2B),
                shape = MaterialTheme.shapes.extraLarge,
                border = BorderStroke(1.dp, phaseAccent.copy(alpha = .82f)),
                shadowElevation = 7.dp
            ) {
                Row(
                    Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.battle_arena_rune),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp).graphicsLayer { rotationZ = -runeRotation }
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            "ФАЗА $bossPhase",
                            color = phaseAccent,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            phaseTitle,
                            color = Color(0xFFF5E8D0),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        if (telegraphActive && enemyHp > 0 && playerHp > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 62.dp)
                    .graphicsLayer {
                        alpha = (.76f + pulse * .22f).coerceIn(0f, 1f)
                        scaleX = 1f + pulse * .012f
                        scaleY = 1f + pulse * .012f
                    },
                color = Color(0xEE3B1B2B),
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, telegraphAccent.copy(alpha = .94f)),
                shadowElevation = 10.dp
            ) {
                Row(
                    Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.battle_telegraph_warning),
                        contentDescription = "Предупреждение об особой атаке",
                        modifier = Modifier.size(27.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            "ОСОБАЯ АТАКА ГОТОВИТСЯ",
                            color = Color(0xFFFFD98C),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            incomingAbilityName,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            "Активный навык снизит ответный урон",
                            color = Color(0xFFF3D9D2),
                            fontSize = 7.sp,
                            maxLines = 1
                        )
                        LinearProgressIndicator(
                            progress = { .22f + enemyWindup * .72f },
                            modifier = Modifier.fillMaxWidth().height(3.dp),
                            color = telegraphAccent,
                            trackColor = Color(0xFF5A3038)
                        )
                    }
                }
            }
        }

        if ((guardPrepared || guardImpactProgress.value < .98f) && playerHp > 0) {
            Image(
                painter = painterResource(id = R.drawable.battle_guard_barrier),
                contentDescription = if (guardPrepared) "Защитная стойка активна" else "Защитная стойка отразила часть удара",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 0.dp, bottom = 5.dp)
                    .size(160.dp)
                    .graphicsLayer {
                        val impactFlash = (1f - guardImpactProgress.value).coerceIn(0f, 1f)
                        alpha = if (guardPrepared) .46f + pulse * .42f else impactFlash * .88f
                        scaleX = if (guardPrepared) .94f + pulse * .07f else 1f + impactFlash * .26f
                        scaleY = if (guardPrepared) .94f + pulse * .07f else 1f + impactFlash * .26f
                    }
            )
        }

        Image(
            painter = painterResource(id = heroArtworkRes),
            contentDescription = "Боевой образ: $playerName",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 8.dp, bottom = 18.dp)
                .width(132.dp)
                .height(176.dp)
                .graphicsLayer {
                    val hitFlash = (1f - playerHitProgress.value).coerceIn(0f, 1f)
                    translationY = -pulse * 2.5f + heroBreath * 2.1f + hitFlash * 2.5f
                    translationX = heroBreath * 1.4f + strikeLunge.value * 22f +
                        (if (combatAction && impactProgress.value < .22f) {
                            (1f - impactProgress.value / .22f) * 6f
                        } else 0f) +
                        kotlin.math.sin((playerHitProgress.value * 44f).toDouble()).toFloat() * hitFlash * 8f
                    rotationZ = heroBreath * .22f + hitFlash * 4.5f
                    alpha = 1f - hitFlash * .08f
                }
        )
        val playerHitFlash = (1f - playerHitProgress.value).coerceIn(0f, 1f)
        if (playerHitFlash > .01f) {
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 22.dp, bottom = 28.dp)
                    .size(154.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0x99FF5664).copy(alpha = playerHitFlash),
                                Color(0x44D92F49).copy(alpha = playerHitFlash * .6f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
        }

        Box(
            Modifier.align(Alignment.BottomEnd)
                .padding(end = 1.dp, bottom = 15.dp)
                .size(174.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            phaseAccent.copy(alpha = .34f * pulse),
                            phaseAccent.copy(alpha = .12f * pulse),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        if (telegraphActive && enemyHp > 0 && playerHp > 0) {
            Image(
                painter = painterResource(R.drawable.battle_telegraph_sigil),
                contentDescription = "Печать подготовки особой атаки",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 0.dp, bottom = 2.dp)
                    .size(222.dp)
                    .graphicsLayer {
                        alpha = .22f + enemyWindup * .42f
                        scaleX = .88f + enemyWindup * .18f
                        scaleY = .88f + enemyWindup * .18f
                        rotationZ = enemyWindup * 7f
                    }
            )
        }

        Image(
            painter = painterResource(id = enemyPortraitRes),
            contentDescription = bossName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 5.dp, bottom = 7.dp)
                .width(158.dp)
                .height(202.dp)
                .graphicsLayer {
                    val hitShake = if (combatAction && impactProgress.value < .30f) {
                        kotlin.math.sin((impactProgress.value * 72f).toDouble()).toFloat() *
                            (1f - impactProgress.value / .30f) * 8f
                    } else 0f
                    translationX = hitShake - if (telegraphActive) enemyWindup * 13f else 0f
                    translationY = enemyHover * 3.4f - if (telegraphActive) enemyWindup * 5f else 0f
                    rotationZ = enemyHover * .42f - if (telegraphActive) enemyWindup * 1.4f else 0f
                    scaleX = phasePulse + pulse * .008f + if (telegraphActive) enemyWindup * .035f else 0f
                    scaleY = phasePulse + pulse * .008f + if (telegraphActive) enemyWindup * .035f else 0f
                }
        )

        Surface(
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 83.dp, bottom = 11.dp),
            color = Color(0xE91B2940),
            shape = MaterialTheme.shapes.extraLarge,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD9B66D))
        ) {
            Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = petPortraitRes),
                    contentDescription = petName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(47.dp).graphicsLayer {
                        translationY = petHover * 3.dp.toPx()
                        rotationZ = petHover * 1.1f
                    }
                )
                Column(Modifier.padding(end = 6.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text("СПУТНИК", color = Color(0xFFBDEDE1), fontSize = 7.sp, fontWeight = FontWeight.Black)
                    Text(petName, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.Center).padding(top = 28.dp),
            color = Color(0xCC101A2C),
            shape = MaterialTheme.shapes.extraLarge,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD98C).copy(alpha = .75f))
        ) {
            Text("VS", Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = Color(0xFFFFE3A0), fontSize = 12.sp, fontWeight = FontWeight.Black)
        }

        if (combatAction && impactProgress.value < .96f) {
            val progress = impactProgress.value
            Image(
                painter = painterResource(R.drawable.battle_impact_ring),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 42.dp, y = 28.dp)
                    .size(218.dp)
                    .graphicsLayer {
                        alpha = (1f - progress).coerceIn(0f, 1f) * .82f
                        scaleX = .42f + progress * 1.28f
                        scaleY = .42f + progress * 1.28f
                        rotationZ = progress * 18f
                    }
            )
            Canvas(Modifier.fillMaxSize()) {
                val burstCenter = Offset(size.width * .72f, size.height * .69f)
                val radius = 18f + progress * 104f
                val fade = (1f - progress).coerceIn(0f, 1f)
                drawCircle(
                    Color(0xFFFFE5A5).copy(alpha = fade * .72f),
                    radius,
                    burstCenter,
                    style = Stroke(width = 3.8f * fade.coerceAtLeast(.08f))
                )
                drawCircle(
                    phaseAccent.copy(alpha = fade * .62f),
                    radius * .68f,
                    burstCenter,
                    style = Stroke(width = 2.1f * fade.coerceAtLeast(.08f))
                )
                for (index in 0..11) {
                    val angle = index * (Math.PI / 6.0)
                    val inner = radius * .78f
                    val outer = radius * (1.06f + (index % 3) * .12f)
                    val start = Offset(
                        burstCenter.x + kotlin.math.cos(angle).toFloat() * inner,
                        burstCenter.y + kotlin.math.sin(angle).toFloat() * inner * .72f
                    )
                    val end = Offset(
                        burstCenter.x + kotlin.math.cos(angle).toFloat() * outer,
                        burstCenter.y + kotlin.math.sin(angle).toFloat() * outer * .72f
                    )
                    drawLine(
                        Brush.linearGradient(
                            listOf(Color.Transparent, phaseAccent.copy(alpha = fade * .9f), Color.Transparent),
                            start = start,
                            end = end
                        ),
                        start,
                        end,
                        strokeWidth = if (index % 3 == 0) 3.2f * fade else 1.7f * fade
                    )
                }
                when (signatureRes) {
                    R.drawable.battle_signature_blade -> {
                        for (slash in 0..2) {
                            val shift = (slash - 1) * radius * .17f
                            val start = Offset(burstCenter.x - radius * .72f, burstCenter.y + radius * .40f + shift)
                            val end = Offset(burstCenter.x + radius * .74f, burstCenter.y - radius * .46f + shift * .28f)
                            val slashPath = Path().apply {
                                moveTo(start.x, start.y)
                                cubicTo(
                                    burstCenter.x - radius * .18f, burstCenter.y + radius * .16f + shift,
                                    burstCenter.x + radius * .24f, burstCenter.y - radius * .62f + shift,
                                    end.x, end.y
                                )
                            }
                            drawPath(
                                slashPath,
                                Brush.linearGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color(0xFFFFE7A7).copy(alpha = fade),
                                        Color(0xFF8DF2D6).copy(alpha = fade * .88f),
                                        Color.Transparent
                                    ),
                                    start = start,
                                    end = end
                                ),
                                style = Stroke(width = (5.2f - slash * .8f) * fade.coerceAtLeast(.08f))
                            )
                        }
                    }
                    R.drawable.battle_signature_iron -> {
                        drawOval(
                            Color(0xFFFFD58C).copy(alpha = fade * .78f),
                            topLeft = Offset(burstCenter.x - radius * 1.02f, burstCenter.y - radius * .34f),
                            size = Size(radius * 2.04f, radius * .78f),
                            style = Stroke(width = 4.2f * fade.coerceAtLeast(.08f))
                        )
                        drawOval(
                            Color(0xFF8BE2B7).copy(alpha = fade * .72f),
                            topLeft = Offset(burstCenter.x - radius * .74f, burstCenter.y - radius * .20f),
                            size = Size(radius * 1.48f, radius * .48f),
                            style = Stroke(width = 2.6f * fade.coerceAtLeast(.08f))
                        )
                        for (shard in 0..7) {
                            val angle = shard * (Math.PI / 4.0)
                            val inner = radius * .58f
                            val outer = radius * (1.22f + (shard % 2) * .18f)
                            val start = Offset(
                                burstCenter.x + kotlin.math.cos(angle).toFloat() * inner,
                                burstCenter.y + kotlin.math.sin(angle).toFloat() * inner * .62f
                            )
                            val end = Offset(
                                burstCenter.x + kotlin.math.cos(angle).toFloat() * outer,
                                burstCenter.y + kotlin.math.sin(angle).toFloat() * outer * .62f
                            )
                            drawLine(
                                Color(0xFFFFD58C).copy(alpha = fade * .88f),
                                start,
                                end,
                                strokeWidth = if (shard % 2 == 0) 4f * fade else 2.1f * fade
                            )
                        }
                    }
                    R.drawable.battle_signature_elemental -> {
                        val elementColors = listOf(
                            Color(0xFFFFA05F),
                            Color(0xFF83DFFF),
                            Color(0xFFFFE28B)
                        )
                        for (element in 0..2) {
                            val angle = element * (Math.PI * 2.0 / 3.0) + progress.toDouble() * 1.8
                            val start = Offset(
                                burstCenter.x + kotlin.math.cos(angle).toFloat() * radius * .24f,
                                burstCenter.y + kotlin.math.sin(angle).toFloat() * radius * .18f
                            )
                            val end = Offset(
                                burstCenter.x + kotlin.math.cos(angle + 2.1).toFloat() * radius * 1.08f,
                                burstCenter.y + kotlin.math.sin(angle + 2.1).toFloat() * radius * .72f
                            )
                            val elementPath = Path().apply {
                                moveTo(start.x, start.y)
                                cubicTo(
                                    burstCenter.x + kotlin.math.cos(angle + .7).toFloat() * radius * 1.12f,
                                    burstCenter.y + kotlin.math.sin(angle + .7).toFloat() * radius * .78f,
                                    burstCenter.x + kotlin.math.cos(angle + 1.45).toFloat() * radius * .42f,
                                    burstCenter.y + kotlin.math.sin(angle + 1.45).toFloat() * radius * .30f,
                                    end.x, end.y
                                )
                            }
                            drawPath(
                                elementPath,
                                Brush.linearGradient(
                                    listOf(Color.Transparent, elementColors[element].copy(alpha = fade), Color.Transparent),
                                    start = start,
                                    end = end
                                ),
                                style = Stroke(width = 4.4f * fade.coerceAtLeast(.08f))
                            )
                            drawCircle(
                                elementColors[element].copy(alpha = fade * .92f),
                                radius * (.055f + element * .008f),
                                end
                            )
                        }
                    }
                    else -> {
                        for (ring in 0..2) {
                            val ringRadius = radius * (.42f + ring * .24f)
                            drawCircle(
                                Color(0xFFB99AFF).copy(alpha = fade * (.72f - ring * .12f)),
                                ringRadius,
                                burstCenter,
                                style = Stroke(width = (3.2f - ring * .55f) * fade.coerceAtLeast(.08f))
                            )
                        }
                        for (seal in 0..7) {
                            val angle = seal * (Math.PI / 4.0) - progress * 1.4f
                            val point = Offset(
                                burstCenter.x + kotlin.math.cos(angle).toFloat() * radius * .94f,
                                burstCenter.y + kotlin.math.sin(angle).toFloat() * radius * .68f
                            )
                            drawCircle(Color(0xFFD8C3FF).copy(alpha = fade), 3.4f * fade.coerceAtLeast(.08f), point)
                            drawLine(
                                Color(0xFF8FE4D4).copy(alpha = fade * .78f),
                                Offset(
                                    burstCenter.x + kotlin.math.cos(angle).toFloat() * radius * .70f,
                                    burstCenter.y + kotlin.math.sin(angle).toFloat() * radius * .50f
                                ),
                                point,
                                strokeWidth = 2.1f * fade.coerceAtLeast(.08f)
                            )
                        }
                    }
                }
            }
            Image(
                painter = painterResource(id = signatureRes),
                contentDescription = "Фирменная техника выбранного класса",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 42.dp, y = 28.dp)
                    .size(224.dp)
                    .graphicsLayer {
                        alpha = (1f - progress).coerceIn(0f, 1f) * .88f
                        rotationZ = when (signatureRes) {
                            R.drawable.battle_signature_blade -> -34f + progress * 68f
                            R.drawable.battle_signature_iron -> progress * 10f
                            R.drawable.battle_signature_elemental -> -82f + progress * 164f
                            else -> 42f - progress * 84f
                        }
                        scaleX = when (signatureRes) {
                            R.drawable.battle_signature_blade -> .72f + progress * 1.18f
                            R.drawable.battle_signature_iron -> .48f + progress * 1.22f
                            else -> .58f + progress * 1.02f
                        }
                        scaleY = when (signatureRes) {
                            R.drawable.battle_signature_blade -> 1.08f - progress * .20f
                            R.drawable.battle_signature_iron -> .55f + progress * 1.15f
                            else -> .62f + progress * .96f
                        }
                    }
            )
            Image(
                painter = painterResource(id = impactRes),
                contentDescription = "Эффект боевого удара",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 42.dp, y = 28.dp)
                    .size(188.dp)
                    .graphicsLayer {
                        alpha = (1f - progress).coerceIn(0f, 1f)
                        scaleX = .58f + progress * .86f
                        scaleY = .58f + progress * .86f
                        rotationZ = -14f + progress * 28f
                    }
            )
            if (impactDamage.isNotBlank()) {
                Text(
                    "−$impactDamage",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 25.dp)
                        .offset(y = (-30).dp)
                        .graphicsLayer {
                            alpha = (1f - progress).coerceIn(0f, 1f)
                            translationY = -progress * 42f
                            scaleX = .85f + (1f - progress) * .25f
                            scaleY = .85f + (1f - progress) * .25f
                        },
                    color = if (actionText.contains("КРИТИЧЕСКИЙ УДАР", ignoreCase = true)) Color(0xFFFFE08A) else Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
            color = Color(0xD9111C30),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Text(
                if (autoBattle) "АВТОБОЙ · РОТАЦИЯ АКТИВНА" else actionText.ifBlank { "ВЫБЕРИ ДЕЙСТВИЕ" },
                Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                color = if (autoBattle) Jade else Color(0xFFE8D6AD),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BattleHealthPanel(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    current: Int,
    maximum: Int,
    ratio: Float,
    barColor: Color,
    isDefeated: Boolean
) {
    Surface(
        modifier = modifier,
        color = Color(0xE90D1729),
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDefeated) Color(0xFF805C5A) else Color(0xB8D6B36F)),
        tonalElevation = 7.dp
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, color = Color(0xFFFFD98C), fontSize = 7.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(subtitle, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier.fillMaxWidth().height(5.dp),
                color = if (isDefeated) Color(0xFF777B87) else barColor,
                trackColor = Color(0xFF3B4050)
            )
            Text(
                "${current.coerceAtLeast(0)} / ${maximum.coerceAtLeast(1)} HP",
                color = if (isDefeated) Color(0xFFB0A8A8) else Color(0xFFD7E1E9),
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

private fun battleSkillDrawable(classIndex: Int): Int = when (classIndex.coerceIn(0, 3)) {
    0 -> R.drawable.battle_skill_blade
    1 -> R.drawable.battle_skill_iron
    2 -> R.drawable.battle_skill_elemental
    else -> R.drawable.battle_skill_spirit
}

@Composable
private fun BattleSkillCard(
    name: String,
    iconRes: Int,
    cooldown: Int,
    enabled: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.width(104.dp).height(112.dp).clickable(enabled = enabled, onClick = onClick),
        color = when {
            !enabled && cooldown > 0 -> Color(0xE51A2332)
            selected -> Color(0xE33D4B50)
            enabled -> Color(0xE522354B)
            else -> Color(0xE51B293B)
        },
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 2.dp else 1.dp,
            when {
                cooldown > 0 -> Color(0xFF59677B)
                selected -> Color(0xFFFFDF9B)
                enabled -> Color(0xFF82D9C0)
                else -> Color(0xFF45546A)
            }
        ),
        tonalElevation = if (selected) 7.dp else 3.dp,
        shadowElevation = if (enabled) 4.dp else 1.dp
    ) {
        Column(
            Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    alpha = if (cooldown > 0) .48f else 1f
                )
                if (cooldown > 0) {
                    Surface(
                        color = Color(0xE9101724),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF9AA9BD))
                    ) {
                        Text(
                            cooldown.toString(),
                            Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            Text(
                name,
                color = if (enabled) Color(0xFFFFE8B6) else Color(0xFFAAB6C6),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                minLines = 2,
                lineHeight = 9.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                when {
                    cooldown > 0 -> "ПЕРЕЗАРЯДКА"
                    enabled -> "АКТИВИРОВАТЬ"
                    else -> "НЕДОСТУПНО"
                },
                color = when {
                    cooldown > 0 -> Color(0xFF9DAABD)
                    enabled -> Jade
                    else -> Color(0xFF78879A)
                },
                fontSize = 6.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BattleActionButton(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    enabled: Boolean,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.heightIn(min = 43.dp).clickable(enabled = enabled, onClick = onClick),
        color = when {
            !enabled -> Color(0xFF202B3A)
            selected -> Color(0xFF315B50)
            else -> Color(0xFF253D5B)
        },
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                !enabled -> Color(0xFF465064)
                selected -> Color(0xFF9CE4C9)
                else -> Color(0xFFD5B16B)
            }
        ),
        tonalElevation = if (selected) 8.dp else 4.dp,
        shadowElevation = if (selected) 7.dp else 3.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RpgIcon(icon, Modifier.size(27.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                color = if (enabled) Color(0xFFFFE5A5) else Color(0xFF8995A7),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PetCompanionArt(pet: PetDefinition) {
    val transition = rememberInfiniteTransition(label = "spirit-companion-art")
    val breath by transition.animateFloat(.985f, 1.025f, infiniteRepeatable(tween(1900), RepeatMode.Reverse), label = "companion-breath")
    val glow by transition.animateFloat(.25f, .8f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "companion-glow")
    val rarity = petRarityColor(pet.id)
    Box(Modifier.fillMaxWidth().height(174.dp)) {
        Image(
            painter = painterResource(id = R.drawable.pet_companion_banner),
            contentDescription = "Святилище духовных спутников",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = breath; scaleY = breath }
        )
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            for (i in 0..17) {
                val x = ((i * .173f + glow * .12f) % 1f) * w
                val y = h * (.18f + ((i * 29) % 68) / 100f)
                drawCircle(rarity.copy(alpha = glow * .62f), if (i % 3 == 0) 2.8f else 1.5f, Offset(x, y))
            }
            drawRect(brush = Brush.horizontalGradient(0f to Color(0x77101820), .48f to Color.Transparent, 1f to Color(0x66101820)))
            drawRect(brush = Brush.verticalGradient(0f to Color.Transparent, .7f to Color.Transparent, 1f to Color(0xAA101820)))
        }
        Column(Modifier.align(Alignment.CenterEnd).padding(end = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            PetPortraitMedallion(pet.id, 108.dp, true)
            Text(pet.name, color = Color(0xFFFFE5B0), fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
        Text("СВЯТИЛИЩЕ ДУХОВНЫХ СПУТНИКОВ", Modifier.align(Alignment.BottomStart).padding(10.dp), color = Color(0xFFE8D6AD), fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun WorldIllustratedCard(
    index: Int,
    title: String,
    description: String,
    requiredStage: Int,
    currentStage: Int,
    unlocked: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "world-card-$index")
    val pulse by transition.animateFloat(
        .82f, 1.04f,
        infiniteRepeatable(tween(1900 + index * 170), RepeatMode.Reverse),
        label = "world-glow-$index"
    )
    val currentWorldIndex = worldIndexForStage(currentStage)
    val nextWorldStage = worldEntries.getOrNull(index + 1)?.second
    val progress = when {
        !unlocked -> (currentStage.toFloat() / requiredStage).coerceIn(0f, 1f)
        index == currentWorldIndex && nextWorldStage != null ->
            ((currentStage - requiredStage).toFloat() / (nextWorldStage - requiredStage)).coerceIn(0f, 1f)
        else -> 1f
    }
    val stateLabel = when {
        !unlocked -> "ЗАПЕЧАТАН"
        index == currentWorldIndex -> "ТЕКУЩИЙ МИР"
        else -> "ПУТЬ ПРОЙДЕН"
    }
    Surface(
        modifier = Modifier.fillMaxWidth().height(174.dp).clickable(onClick = onClick),
        color = Color(0xFF111C30),
        shape = MaterialTheme.shapes.extraLarge,
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 2.dp else 1.dp,
            when {
                selected && unlocked -> Jade.copy(alpha = pulse)
                selected -> Gold.copy(alpha = pulse)
                unlocked -> Color(0xFF6C9DAB).copy(alpha = .55f)
                else -> Color(0xFF7E718E).copy(alpha = .45f)
            }
        ),
        tonalElevation = if (selected) 10.dp else 4.dp,
        shadowElevation = if (selected) 9.dp else 4.dp
    ) {
        Box(Modifier.fillMaxSize()) {
            WorldLandscapeArt(index, Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(
                0f to Color(0xC9091222),
                .48f to Color(0x3310182B),
                1f to Color(0xB9091222)
            )))
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                0f to Color(0x2210182B),
                .48f to Color.Transparent,
                1f to Color(0xE6091220)
            )))
            if (!unlocked) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Color(0xAA0A1020), Color(0x660A1020), Color.Transparent),
                            center = Offset(size.width * .78f, size.height * .47f),
                            radius = size.height * .46f
                        ),
                        radius = size.height * .46f,
                        center = Offset(size.width * .78f, size.height * .47f)
                    )
                }
                RpgIcon("lock", Modifier.align(Alignment.CenterEnd).padding(end = 22.dp).size(36.dp))
            }
            Column(
                Modifier.fillMaxSize().padding(horizontal = 13.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("МИР ${index + 1}  ·  СТУПЕНЬ $requiredStage", color = Color(0xFFE8D5A7), fontSize = 8.sp, fontWeight = FontWeight.Black)
                        Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
                    }
                    Surface(
                        color = if (unlocked) Color(0xCC173F3A) else Color(0xCC382C3B),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text(
                            stateLabel,
                            Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            color = if (unlocked) Color(0xFFA7F0D1) else Color(0xFFE4C8A0),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(description, color = Color(0xFFE0E6F0), fontSize = 9.sp, lineHeight = 12.sp, maxLines = 2)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f).height(4.dp).background(Color(0x668FA3B8), MaterialTheme.shapes.small)) {
                            Box(
                                Modifier.fillMaxHeight().fillMaxWidth(progress)
                                    .background(if (unlocked) Jade else Gold, MaterialTheme.shapes.small)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            when {
                                !unlocked -> "НУЖНО $requiredStage"
                                index == currentWorldIndex && nextWorldStage != null -> "$currentStage / $nextWorldStage"
                                else -> "ДОСТУПЕН"
                            },
                            color = if (unlocked) Jade else Color(0xFFE8C88F),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorldPortalChip(
    destination: WorldPortalDestination,
    isCurrentWorld: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.width(174.dp).height(62.dp).clickable(onClick = onClick),
        color = Color(0xDD102238),
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCurrentWorld) Jade.copy(alpha = .68f) else Gold.copy(alpha = .42f)
        ),
        tonalElevation = 3.dp
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RpgIcon(destination.icon, Modifier.size(29.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(destination.title, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(destination.subtitle, color = Color(0xFFB9C9D9), fontSize = 8.sp, maxLines = 1)
            }
            RpgIcon("portal", Modifier.size(17.dp))
        }
    }
}

@Composable
private fun WorldLandscapeArt(index: Int, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = worldLandscapeResource(index)),
        contentDescription = worldEntries.getOrElse(index) { worldEntries.first() }.first,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

private fun townBuildingResource(symbol: String): Int = when (symbol) {
    "shop" -> R.drawable.town_building_shop
    "auction" -> R.drawable.town_building_auction
    "forge" -> R.drawable.town_building_forge
    "quests" -> R.drawable.town_building_quests
    "cultivation" -> R.drawable.town_building_cultivation
    "arena" -> R.drawable.town_building_arena
    "skills" -> R.drawable.town_building_skills
    "clan" -> R.drawable.town_building_clan
    "pets" -> R.drawable.town_building_pets
    "inventory" -> R.drawable.town_building_inventory
    "abode" -> R.drawable.town_building_abode
    "tower" -> R.drawable.town_building_tower
    "alchemy" -> R.drawable.town_building_alchemy
    "sanctuary" -> R.drawable.town_building_cultivation
    "garden" -> R.drawable.abode_marker_garden
    "spring" -> R.drawable.abode_marker_spring
    "worlds" -> R.drawable.abode_marker_portal
    else -> R.drawable.town_building_cultivation
}

@Composable
private fun TownBuilding(name: String, description: String, symbol: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "town-building-$symbol")
    val pulse by transition.animateFloat(
        initialValue = .18f,
        targetValue = .42f,
        animationSpec = infiniteRepeatable(tween(1700), RepeatMode.Reverse),
        label = "town-building-glow-$symbol"
    )
    Surface(
        modifier = modifier.heightIn(min = 146.dp).clickable(onClick = onClick),
        color = Color(0xFF172A38),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, Gold.copy(alpha = .52f)),
        shadowElevation = 8.dp,
        tonalElevation = 5.dp
    ) {
        Box {
            Canvas(Modifier.matchParentSize()) {
                drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF263E4A), Color(0xFF101D2B))))
                val center = Offset(size.width * .5f, size.height * .37f)
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0xFF8CDCC6).copy(alpha = pulse), Color(0x00B5DCCB)),
                        center = center,
                        radius = size.minDimension * .68f
                    ),
                    size.minDimension * .68f,
                    center
                )
                drawLine(
                    Gold.copy(alpha = .16f),
                    Offset(size.width * .12f, size.height * .72f),
                    Offset(size.width * .88f, size.height * .72f),
                    1.dp.toPx()
                )
            }
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Image(
                    painter = painterResource(townBuildingResource(symbol)),
                    contentDescription = "$name — иллюстрация здания",
                    modifier = Modifier.fillMaxWidth().height(82.dp),
                    contentScale = ContentScale.Fit
                )
                Text(name, color = Gold, fontWeight = FontWeight.Black, fontSize = 12.sp, maxLines = 1)
                Text(
                    description,
                    color = Color(0xFFC1D3D0),
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                    maxLines = 2
                )
            }
        }
    }
}


@Composable
private fun AuctionHallScreen(
    stage: Int,
    stones: Int,
    onBuyWeapon: (WeaponDefinition, Int) -> Unit,
    onBuyGear: (GearDefinition, Int) -> Unit,
    onClose: () -> Unit
) {
    val weapons = remember(stage) {
        weaponCatalog.filter { it.requiredStage <= stage }
            .sortedByDescending { weaponPowerScore(it) }.take(6)
    }
    val gear = remember(stage) {
        gearCatalog.filter { it.requiredStage <= stage }
            .sortedByDescending { gearPowerScore(it) }.take(6)
    }
    var tab by remember { mutableStateOf(0) }
    var selectedKey by remember(tab) { mutableStateOf("") }
    val selectedWeapon = if (tab == 0) {
        weapons.firstOrNull { it.id == selectedKey.removePrefix("W:") } ?: weapons.firstOrNull()
    } else null
    val selectedGear = if (tab == 1) {
        gear.firstOrNull { it.id == selectedKey.removePrefix("G:") } ?: gear.firstOrNull()
    } else null
    val selectedPrice = selectedWeapon?.let { 250 + weaponPowerScore(it) * 18 }
        ?: selectedGear?.let { 350 + gearPowerScore(it) * 16 } ?: 0
    val selectedName = selectedWeapon?.name ?: selectedGear?.name ?: "Нет доступных лотов"
    val selectedRarity = selectedWeapon?.rarity ?: selectedGear?.rarity
    val selectedScore = selectedWeapon?.let { weaponPowerScore(it) }
        ?: selectedGear?.let { gearPowerScore(it) } ?: 0
    val selectedEffect = selectedWeapon?.effectDescription ?: selectedGear?.effectDescription.orEmpty()
    val selectedStats = selectedWeapon?.let {
        "АТК +${it.attackBonus}   ЗАЩ +${it.defenseBonus}   УРОН +${it.bonusDamage}   ЛЕЧЕНИЕ +${it.healOnHit}"
    } ?: selectedGear?.let {
        "АТК +${it.attackBonus}   ЗАЩ +${it.defenseBonus}   HP +${it.hpBonus}   ТЕХНИКА +${it.skillBonus}"
    }.orEmpty()

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier.fillMaxWidth().height(164.dp).clip(RoundedCornerShape(18.dp))
        ) {
            Image(
                painter = painterResource(R.drawable.auction_hall_scene),
                contentDescription = "Иллюстрация аукционного павильона Лунной Гавани",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color(0x18071420), Color(0xB8071420), Color(0xF20A1725))
                    )
                )
            )
            Column(
                Modifier.align(Alignment.BottomStart).padding(start = 13.dp, end = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    "АУКЦИОН ЛУННОЙ ГАВАНИ",
                    color = Color(0xFFFFD995),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    "Редкости странствующих мастеров",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Каждый лот можно осмотреть до покупки",
                    color = Color(0xFFD1E0E7),
                    fontSize = 9.sp
                )
            }
            Surface(
                Modifier.align(Alignment.TopEnd).padding(9.dp),
                color = Color(0xE9152B35),
                shape = RoundedCornerShape(11.dp),
                border = BorderStroke(1.dp, Gold.copy(alpha = .8f))
            ) {
                Row(
                    Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    RpgIcon("coin", Modifier.size(16.dp))
                    Text(
                        "${stones} камней",
                        color = Color(0xFFFFE4A8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            listOf("ОРУЖИЕ" to 0, "ЭКИПИРОВКА" to 1).forEach { (label, index) ->
                val active = tab == index
                Surface(
                    Modifier.weight(1f).clickable { tab = index },
                    color = if (active) Color(0xFF31564E) else Color(0xFF182A3B),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        if (active) 1.5.dp else 1.dp,
                        if (active) Jade else Color(0x665E7187)
                    )
                ) {
                    Text(
                        label,
                        Modifier.padding(vertical = 10.dp),
                        color = if (active) Color(0xFFE7F8EE) else Color(0xFFB8C6D5),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        letterSpacing = .8.sp
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ВЫСТАВЛЕННЫЕ ЛОТЫ", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text(
                if (tab == 0) "${weapons.size} предметов" else "${gear.size} предметов",
                color = Color(0xFF9FB3C4),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (tab == 0) {
                weapons.forEach { item ->
                    AuctionLotTile(
                        title = item.name,
                        subtitle = "Оружие · ступень ${item.requiredStage}",
                        resourceId = weaponDrawable(item.type),
                        rarity = item.rarity,
                        score = weaponPowerScore(item),
                        price = 250 + weaponPowerScore(item) * 18,
                        selected = selectedWeapon?.id == item.id,
                        onClick = { selectedKey = "W:${item.id}" }
                    )
                }
            } else {
                gear.forEach { item ->
                    AuctionLotTile(
                        title = item.name,
                        subtitle = "${item.slot.title} · ступень ${item.requiredStage}",
                        resourceId = gearDrawable(item.slot),
                        rarity = item.rarity,
                        score = gearPowerScore(item),
                        price = 350 + gearPowerScore(item) * 16,
                        selected = selectedGear?.id == item.id,
                        onClick = { selectedKey = "G:${item.id}" }
                    )
                }
            }
        }

        if (selectedRarity != null) {
            val rarityColor = weaponRarityColor(selectedRarity)
            Surface(
                Modifier.fillMaxWidth(),
                color = Color(0xEE14243A),
                shape = RoundedCornerShape(17.dp),
                border = BorderStroke(1.2.dp, rarityColor.copy(alpha = .85f)),
                tonalElevation = 5.dp
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        RelicArtworkIcon(
                            selectedWeapon?.let { weaponDrawable(it.type) }
                                ?: gearDrawable(selectedGear!!.slot),
                            rarityColor,
                            Modifier.size(76.dp),
                            rarityFrameResource(selectedRarity)
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("ОСМОТР ЛОТА", color = Gold, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Text(selectedName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 2)
                            Text(
                                "${selectedRarity.title.uppercase()}  ·  РЕЙТИНГ ${selectedScore}",
                                color = rarityColor,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Text(selectedStats, color = Color(0xFFE4D2A7), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(selectedEffect, color = Color(0xFFBFCEDB), fontSize = 9.sp, lineHeight = 13.sp)
                    if (selectedGear != null) {
                        Text(
                            "Комплект: ${gearSetNames[selectedGear.setId] ?: selectedGear.setId}",
                            color = Jade,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("СТОИМОСТЬ ЛОТА", color = Color(0xFF9FB3C4), fontSize = 7.sp, fontWeight = FontWeight.Black)
                            Text("${selectedPrice} камней духа", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Button(
                            onClick = {
                                if (selectedWeapon != null) onBuyWeapon(selectedWeapon, selectedPrice)
                                else selectedGear?.let { onBuyGear(it, selectedPrice) }
                            },
                            enabled = selectedPrice > 0 && stones >= selectedPrice,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF376D58),
                                disabledContainerColor = Color(0xFF303B49)
                            ),
                            contentPadding = PaddingValues(horizontal = 13.dp, vertical = 8.dp)
                        ) {
                            Text(
                                if (stones >= selectedPrice && selectedPrice > 0) "ПРИОБРЕСТИ" else "НЕ ХВАТАЕТ",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        Text(
            "Покупка сразу отправляет предмет в сокровищницу. Повторные экземпляры разрешены.",
            color = Color(0xFF9FB3C4),
            fontSize = 8.sp,
            lineHeight = 11.sp
        )
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold)
        ) {
            Text("ВЕРНУТЬСЯ НА ПЛОЩАДЬ", fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun AuctionLotTile(
    title: String,
    subtitle: String,
    resourceId: Int,
    rarity: WeaponRarity,
    score: Int,
    price: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val rarityColor = weaponRarityColor(rarity)
    Surface(
        Modifier.width(144.dp).height(184.dp).clickable(onClick = onClick),
        color = Color(0xFF14243A),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, rarityColor.copy(alpha = if (selected) 1f else .58f)),
        tonalElevation = if (selected) 7.dp else 2.dp
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.auction_lot_frame),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(
                Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(subtitle.substringBefore(" · ").uppercase(), color = Color(0xFFD8E4EF), fontSize = 6.sp, fontWeight = FontWeight.Black, maxLines = 1)
                    if (selected) Text("ОСМОТР", color = Gold, fontSize = 6.sp, fontWeight = FontWeight.Black)
                }
                RelicArtworkIcon(resourceId, rarityColor, Modifier.size(70.dp), rarityFrameResource(rarity))
                Text(
                    title,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    minLines = 2,
                    lineHeight = 11.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        rarity.title.uppercase(),
                        color = rarityColor,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "СИЛА $score",
                        color = Color(0xFFE1D3B5),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (selected) Color(0xFF365D4F) else Color(0xAA0A1728),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(1.dp, rarityColor.copy(alpha = .48f))
                ) {
                    Text(
                        "$price  ·  КАМНЕЙ",
                        modifier = Modifier.padding(vertical = 5.dp),
                        color = Gold,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
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
    Surface(
        color = Color(0xE91A263B),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().height(2.dp).background(
                Brush.horizontalGradient(listOf(Color.Transparent, Gold, Color(0xFF8C6A3D), Gold, Color.Transparent))
            ))
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(title, color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Jade, fontSize = 12.sp)
                content()
            }
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

private data class LootRevealItem(
    val kind: String,
    val key: String,
    val title: String,
    val detail: String,
    val rarity: String
)

private fun encodeLootReveal(items: List<LootRevealItem>): String =
    items.joinToString("~") { item ->
        listOf(item.kind, item.key, item.title, item.detail, item.rarity)
            .joinToString("^") { it.replace("^", " ").replace("~", " ") }
    }

private fun decodeLootReveal(payload: String): List<LootRevealItem> {
    if (payload.isBlank()) return emptyList()
    return payload.split("~").mapNotNull { encoded ->
        val fields = encoded.split('^', limit = 5)
        if (fields.size != 5) null else LootRevealItem(fields[0], fields[1], fields[2], fields[3], fields[4])
    }
}

private enum class WeaponRarity(val title: String, val realmGapShare: Double, val rollSpread: Double) {
    COMMON("Обычное", 0.00, 0.10),
    UNCOMMON("Необычное", 0.16, 0.12),
    RARE("Редкое", 0.34, 0.15),
    EPIC("Эпическое", 0.58, 0.18),
    LEGENDARY("Легендарное", 0.88, 0.23),
    MYTHIC("Мифическое", 1.08, 0.28)
}

private fun rarityFrameResource(rarity: WeaponRarity): Int = when (rarity) {
    WeaponRarity.COMMON, WeaponRarity.UNCOMMON -> R.drawable.ui_rarity_frame_01
    WeaponRarity.RARE, WeaponRarity.EPIC -> R.drawable.ui_rarity_frame_02
    WeaponRarity.LEGENDARY, WeaponRarity.MYTHIC -> R.drawable.ui_rarity_frame_03
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
private fun skillNodeIcon(classIndex: Int, node: Int): String {
    val icons = listOf(
        listOf("weapon", "critical", "shield", "arena", "spear", "skills", "health", "artifact", "portal"),
        listOf("armor", "shield", "health", "gauntlet", "helmet", "critical", "shield", "artifact", "tower"),
        listOf("fire", "ice", "lightning", "staff", "fire", "ice", "lightning", "portal", "artifact"),
        listOf("spiritmystic", "health", "pets", "artifact", "lotus", "shield", "skills", "lotus", "portal")
    )
    return icons[classIndex.coerceIn(0, 3)][node.coerceIn(0, 8)]
}

private fun skillNodeRequiredLevel(node: Int): Int = when (node) {
    0 -> 1
    1, 4 -> 3
    2, 5 -> 6
    3, 6 -> 9
    7 -> 13
    8 -> 18
    else -> 1
}

@Composable
private fun SkillTreeConstellation(
    classIndex: Int,
    levels: List<Int>,
    points: Int,
    activeNode: Int,
    selectedNode: Int,
    characterLevel: Int,
    onSelectNode: (Int) -> Unit
) {
    val accent = listOf(
        Color(0xFFFFD58B),
        Color(0xFFB9D5A7),
        Color(0xFFFFA66F),
        Color(0xFFC6A5FF)
    )[classIndex.coerceIn(0, 3)]
    val density = androidx.compose.ui.platform.LocalDensity.current
    val nodeHeightPx = with(density) { 82.dp.toPx() }
    val rowStepPx = with(density) { 96.dp.toPx() }
    val cardTop = mapOf(
        0 to 0f,
        1 to rowStepPx, 4 to rowStepPx,
        2 to rowStepPx * 2f, 5 to rowStepPx * 2f,
        3 to rowStepPx * 3f, 6 to rowStepPx * 3f,
        7 to rowStepPx * 4f,
        8 to rowStepPx * 5f
    )
    val cardX = mapOf(
        0 to .5f,
        1 to .25f, 4 to .75f,
        2 to .25f, 5 to .75f,
        3 to .25f, 6 to .75f,
        7 to .5f,
        8 to .5f
    )
    BoxWithConstraints(Modifier.fillMaxWidth().height(568.dp)) {
        val nodeCardWidth = maxWidth * .47f
        Canvas(Modifier.fillMaxSize()) {
            val edges = listOf(0 to 1, 0 to 4, 1 to 2, 4 to 5, 2 to 3, 5 to 6, 3 to 7, 6 to 7, 7 to 8)
            edges.forEach { (from, to) ->
                val bothLearned = levels.getOrElse(classIndex * 9 + from) { 0 } == 1 &&
                    levels.getOrElse(classIndex * 9 + to) { 0 } == 1
                val targetOpen = skillNodeUnlocked(classIndex, to, levels, characterLevel)
                val lineColor = when {
                    bothLearned -> Jade
                    targetOpen -> accent
                    else -> Color(0xFF536176)
                }
                val startPoint = Offset(size.width * cardX.getValue(from), cardTop.getValue(from) + nodeHeightPx)
                val endPoint = Offset(size.width * cardX.getValue(to), cardTop.getValue(to))
                drawLine(lineColor.copy(alpha = .16f), startPoint, endPoint, strokeWidth = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                drawLine(lineColor.copy(alpha = .88f), startPoint, endPoint, strokeWidth = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                drawCircle(lineColor.copy(alpha = .9f), 3.dp.toPx(), endPoint)
            }
        }

        @Composable
        fun nodeCard(node: Int, modifier: Modifier) {
            val learned = levels.getOrElse(classIndex * 9 + node) { 0 } == 1
            val unlocked = skillNodeUnlocked(classIndex, node, levels, characterLevel)
            val canLearn = !learned && unlocked && points > 0
            val active = learned && skillDefinitions[classIndex][node].active && activeNode == node
            SkillTreeNodeCard(
                modifier = modifier,
                node = node,
                definition = skillDefinitions[classIndex][node],
                iconKey = skillNodeIcon(classIndex, node),
                learned = learned,
                canLearn = canLearn,
                requiredLevel = skillNodeRequiredLevel(node),
                active = active,
                selected = selectedNode == node,
                accent = accent,
                onClick = { onSelectNode(node) }
            )
        }

        nodeCard(0, Modifier.align(Alignment.TopCenter).fillMaxWidth(.47f).height(82.dp))
        Row(Modifier.fillMaxWidth().offset(y = 96.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            nodeCard(1, Modifier.width(nodeCardWidth).height(82.dp))
            nodeCard(4, Modifier.width(nodeCardWidth).height(82.dp))
        }
        Row(Modifier.fillMaxWidth().offset(y = 192.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            nodeCard(2, Modifier.width(nodeCardWidth).height(82.dp))
            nodeCard(5, Modifier.width(nodeCardWidth).height(82.dp))
        }
        Row(Modifier.fillMaxWidth().offset(y = 288.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            nodeCard(3, Modifier.width(nodeCardWidth).height(82.dp))
            nodeCard(6, Modifier.width(nodeCardWidth).height(82.dp))
        }
        nodeCard(7, Modifier.align(Alignment.TopCenter).offset(y = 384.dp).fillMaxWidth(.47f).height(82.dp))
        nodeCard(8, Modifier.align(Alignment.TopCenter).offset(y = 480.dp).fillMaxWidth(.47f).height(82.dp))
    }
}

@Composable
private fun SkillTreeNodeCard(
    modifier: Modifier,
    node: Int,
    definition: SkillDefinition,
    iconKey: String,
    learned: Boolean,
    canLearn: Boolean,
    requiredLevel: Int,
    active: Boolean,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val edge = when {
        selected || active -> Color(0xFFFFE3A0)
        learned -> Jade
        canLearn -> accent
        else -> Color(0xFF526176)
    }
    val status = when {
        active -> "В БОЕВОМ СЛОТЕ"
        learned && definition.active -> "ИЗУЧЕНА · АКТИВНАЯ"
        learned -> "ПАССИВНОЕ УСИЛЕНИЕ"
        canLearn -> "ДОСТУПНО · 1 ОЧКО"
        else -> "ЗАПЕЧАТАНО · РАНГ $requiredLevel"
    }
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = when {
            active -> Color(0xF02F554D)
            learned -> Color(0xED1E403C)
            canLearn -> Color(0xEC25384F)
            selected -> Color(0xED28384A)
            else -> Color(0xE9101B2D)
        },
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(if (selected || active) 2.dp else 1.dp, edge.copy(alpha = if (learned || canLearn || selected) .96f else .62f)),
        tonalElevation = if (selected || active) 8.dp else 2.dp,
        shadowElevation = if (selected || active) 7.dp else 2.dp
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 7.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(Brush.radialGradient(listOf(edge.copy(alpha = .4f), Color.Transparent)), size.minDimension * .49f, center)
                    drawCircle(edge.copy(alpha = .9f), size.minDimension * .43f, center, style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
                }
                RpgIcon(iconKey, Modifier.size(39.dp))
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd),
                    color = Color(0xFF101A2B),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, edge.copy(alpha = .8f))
                ) {
                    RpgIcon(when { learned -> "check"; canLearn -> "plus"; else -> "lock" }, Modifier.size(13.dp))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    definition.name,
                    color = if (learned) Color(0xFFFFE7B5) else Color(0xFFE1E7F0),
                    fontSize = 8.sp,
                    lineHeight = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Text(
                    status,
                    color = edge,
                    fontSize = 6.sp,
                    lineHeight = 7.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun SkillNodeDetailCard(
    classIndex: Int,
    node: Int,
    levels: List<Int>,
    points: Int,
    weapon: Int,
    activeNode: Int,
    characterLevel: Int,
    onLearn: () -> Unit,
    onSetActive: () -> Unit
) {
    val safeNode = node.coerceIn(0, 8)
    val definition = skillDefinitions[classIndex][safeNode]
    val learned = levels.getOrElse(classIndex * 9 + safeNode) { 0 } == 1
    val unlocked = skillNodeUnlocked(classIndex, safeNode, levels, characterLevel)
    val required = skillNodeRequiredLevel(safeNode)
    val canLearn = !learned && unlocked && points > 0
    val canActivate = learned && definition.active && activeNode != safeNode
    val active = learned && definition.active && activeNode == safeNode
    val status = when {
        active -> "ТЕХНИКА ВЫБРАНА ДЛЯ БОЯ"
        learned && definition.active -> "ИЗУЧЕНО · МОЖНО ПЕРЕКЛЮЧИТЬ"
        learned -> "ПАССИВНЫЙ ЭФФЕКТ УЖЕ ДЕЙСТВУЕТ"
        characterLevel < required -> "ТРЕБУЕТСЯ РАНГ $required"
        !unlocked -> "СНАЧАЛА ИЗУЧИ ПРЕДЫДУЩИЙ УЗЕЛ"
        points <= 0 -> "НЕТ СВОБОДНЫХ ОЧКОВ НАВЫКА"
        else -> "УЗЕЛ ГОТОВ К ИЗУЧЕНИЮ"
    }
    Surface(
        color = Color(0xEC172638),
        shape = RoundedCornerShape(19.dp),
        border = BorderStroke(1.dp, if (active) Gold else if (learned) Jade.copy(alpha = .72f) else Color(0x6689A2BD)),
        tonalElevation = 5.dp,
        shadowElevation = 3.dp
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                RpgIcon(skillNodeIcon(classIndex, safeNode), Modifier.size(49.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("УЗЕЛ ${safeNode + 1} · ${if (definition.active) "АКТИВНАЯ ТЕХНИКА" else "ПАССИВНОЕ ИСКУССТВО"}", color = Gold, fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = .7.sp)
                    Text(definition.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
                Text(if (learned) "ИЗУЧЕНО" else "РАНГ $required", color = if (learned) Jade else Color(0xFFE4C58D), fontSize = 7.sp, fontWeight = FontWeight.Black)
            }
            Text(definition.description, color = Color(0xFFD0DCE9), fontSize = 10.sp, lineHeight = 14.sp)
            if (safeNode == 1 || safeNode == 4) {
                Text(weaponSkillDescription(classIndex, safeNode, weapon), color = Color(0xFF9FC6D0), fontSize = 9.sp, lineHeight = 12.sp)
            }
            Surface(color = Color(0xFF101D2D), shape = RoundedCornerShape(10.dp)) {
                Text(status, Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 7.dp), color = if (active || learned) Jade else Gold, fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
            when {
                canLearn -> Button(
                    onClick = onLearn,
                    modifier = Modifier.fillMaxWidth().height(45.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF806039), contentColor = Color.White)
                ) {
                    Text("ИЗУЧИТЬ · 1 ОЧКО НАВЫКА", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
                canActivate -> Button(
                    onClick = onSetActive,
                    modifier = Modifier.fillMaxWidth().height(45.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28584D), contentColor = Color.White)
                ) {
                    Text("НАЗНАЧИТЬ ОСНОВНОЙ ТЕХНИКОЙ", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
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

@Composable
private fun SkillQuickTile(
    modifier: Modifier,
    classIndex: Int,
    node: Int,
    levels: List<Int>,
    points: Int,
    activeNode: Int,
    characterLevel: Int,
    onClick: () -> Unit
) {
    val definition = skillDefinitions[classIndex][node]
    val learned = levels.getOrElse(classIndex * 9 + node) { 0 } == 1
    val unlocked = skillNodeUnlocked(classIndex, node, levels, characterLevel)
    val active = learned && definition.active && activeNode == node
    val canLearn = !learned && unlocked && points > 0
    val edge = when { active -> Gold; learned -> Jade; canLearn -> Color(0xFFB9D5A7); else -> Color(0xFF526176) }
    Surface(
        modifier = modifier.height(82.dp).clickable(onClick = onClick),
        color = when { active -> Color(0xFF294A43); learned -> Color(0xFF1D3938); canLearn -> Color(0xFF20364B); else -> Color(0xFF111D2E) },
        shape = RoundedCornerShape(13.dp),
        border = BorderStroke(if (active) 2.dp else 1.dp, edge.copy(alpha = .9f)),
        shadowElevation = if (active) 6.dp else 1.dp
    ) {
        Column(Modifier.fillMaxSize().padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            RpgIcon(skillNodeIcon(classIndex, node), Modifier.size(31.dp))
            Text(definition.name, color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold, maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 8.sp)
            Text(when { active -> "В БОЮ"; learned -> "ИЗУЧЕНО"; canLearn -> "ДОСТУПНО"; else -> "РАНГ ${skillNodeRequiredLevel(node)}" }, color = edge, fontSize = 6.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
    }
}

@Composable
private fun PetGalleryTile(
    modifier: Modifier,
    pet: PetDefinition,
    owned: Boolean,
    active: Boolean,
    level: Int,
    onClick: () -> Unit
) {
    val accent = petRarityColor(pet.id)
    Surface(
        modifier = modifier.height(142.dp).clickable(onClick = onClick),
        color = if (active) Color(0xFF244B45) else Color(0xE916263A),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(if (active) 2.dp else 1.dp, if (active) Jade else accent.copy(alpha = .55f)),
        shadowElevation = if (active) 7.dp else 2.dp
    ) {
        Column(Modifier.fillMaxSize().padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(78.dp), contentAlignment = Alignment.Center) {
                Image(painter = painterResource(petPortraitResource(pet.id)), contentDescription = pet.name, modifier = Modifier.fillMaxSize(.76f), contentScale = ContentScale.Fit)
                Image(painter = painterResource(when { active -> R.drawable.ui_rarity_frame_03; owned -> R.drawable.ui_rarity_frame_02; else -> R.drawable.ui_rarity_frame_01 }), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            }
            Text(pet.name, color = if (owned) Color(0xFFFFE5B0) else Color(0xFFADB8C8), fontSize = 8.sp, fontWeight = FontWeight.Black, maxLines = 1, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(when { active -> "АКТИВНЫЙ"; owned -> "УР. $level · ВЫБРАТЬ"; else -> "ЗАКРЫТ · ПОДРОБНЕЕ" }, color = if (active) Jade else accent, fontSize = 6.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
    }
}

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
    val id: String, val name: String, val portraitKey: String, val role: String,
    val description: String, val strength: String, val weakness: String,
    val bestClass: String, val source: String, val dropHint: String, val requiredBoss: Int,
    val attack: Int, val defense: Int, val hp: Int, val skill: Int, val critical: Int, val healing: Int
)
private val petCatalog = listOf(
    PetDefinition("jade_fox", "Нефритовый лис", "pet_fox", "хитрый дуэлянт", "Усиливает техники и помогает наносить стабильный урон.", "техники и гибкость", "мало здоровья", "Мистик духов / Мечник", "начальный спутник", "выдаётся в начале пути", 1, 5, 1, 20, 5, 2, 1),
    PetDefinition("stone_tortoise", "Каменная черепаха", "pet_tortoise", "страж", "Принимает часть натиска врага и укрепляет владельца.", "защита и здоровье", "низкий урон", "Закалённый воин", "стражи горного перевала", "добыча со стражей ранга 2+", 2, 1, 9, 90, 0, 0, 0),
    PetDefinition("thunder_hawk", "Громовой ястреб", "pet_hawk", "скоростной атакующий", "Бьёт стремительно и чаще наносит критические удары.", "атака и крит", "слабая защита", "Заклинатель стихий / Мечник", "грозовые стражи", "добыча со стражей ранга 4+", 4, 10, 0, 12, 3, 7, 0),
    PetDefinition("ember_wolf", "Пепельный волк", "pet_wolf", "охотник", "Разрывает цель сериями атак, но почти не помогает в обороне.", "сильный урон", "низкая выживаемость", "Мечник", "элитные стражи", "добыча со стражей ранга 6+", 6, 13, 1, 5, 2, 4, 0),
    PetDefinition("moon_deer", "Лунный олень", "pet_deer", "целитель", "Возвращает владельцу здоровье и поддерживает долгие сражения.", "лечение и здоровье", "низкий урон", "Мистик духов / Закалённый воин", "редкая находка в саду духовных трав", "шанс 3% при сборе урожая", 99, 1, 4, 65, 2, 0, 8),
    PetDefinition("iron_ape", "Железная обезьяна", "pet_ape", "боец передней линии", "Сочетает мощные удары с крепкой защитой.", "баланс атаки и защиты", "нет выраженного уклона в магию", "Закалённый воин", "стражи древней заставы", "добыча со стражей ранга 8+", 8, 8, 6, 45, 1, 1, 0),
    PetDefinition("river_koi", "Карп тихой реки", "pet_koi", "духовный помощник", "Усиливает духовные техники и помогает восстанавливать силы.", "техника и лечение", "низкая физическая атака", "Мистик духов / Заклинатель стихий", "редкая встреча в роднике", "шанс 3% при сборе духовной воды", 99, 1, 3, 38, 8, 0, 5)
)

private fun petPortraitResource(petId: String): Int = when (petId) {
    "jade_fox" -> R.drawable.pet_jade_fox
    "stone_tortoise" -> R.drawable.pet_stone_tortoise
    "thunder_hawk" -> R.drawable.pet_thunder_hawk
    "ember_wolf" -> R.drawable.pet_ember_wolf
    "moon_deer" -> R.drawable.pet_moon_deer
    "iron_ape" -> R.drawable.pet_iron_ape
    "river_koi" -> R.drawable.pet_river_koi
    else -> R.drawable.pet_jade_fox
}


private data class WorldPortalDestination(
    val title: String,
    val subtitle: String,
    val icon: String,
    val menu: Int
)

private val worldPortalDestinations = listOf(
    listOf(
        WorldPortalDestination("Лунная Гавань", "Город и торговля", "worlds", 0),
        WorldPortalDestination("Путь культивации", "Медитация и прорыв", "cultivation", 1),
        WorldPortalDestination("Арена долины", "Испытания и награды", "arena", 2)
    ),
    listOf(
        WorldPortalDestination("Духовный зверинец", "Спутники и пробуждение", "pets", 7),
        WorldPortalDestination("Зал искусств", "Боевые техники", "skills", 5),
        WorldPortalDestination("Секты", "Пути и союзы", "clan", 6)
    ),
    listOf(
        WorldPortalDestination("Небесная сокровищница", "Реликвии и экипировка", "inventory", 4),
        WorldPortalDestination("Зал бессмертных", "Облик и атрибуты", "character", 3),
        WorldPortalDestination("Небесная арена", "Высшие испытания", "arena", 2)
    ),
    listOf(
        WorldPortalDestination("Разлом Хаоса", "Последние испытания", "arena", 2),
        WorldPortalDestination("Печать Дао", "Пробуждение техник", "skills", 5),
        WorldPortalDestination("Обитель странника", "Развитие резиденции", "abode", 8)
    )
)

private fun worldIndexForStage(stage: Int): Int = when {
    stage >= 19 -> 3
    stage >= 13 -> 2
    stage >= 7 -> 1
    else -> 0
}

private fun worldBossAbilityName(worldIndex: Int): String = when (worldIndex) {
    0 -> "Рёв Каменного Тигра"
    1 -> "Корни Нефритового Пика"
    2 -> "Приговор Небесного Дракона"
    3 -> "Разлом Пустоты"
    else -> ""
}

private fun worldLandscapeResource(index: Int): Int = when (index.coerceIn(0, 3)) {
    0 -> R.drawable.world_mortal_landscape
    1 -> R.drawable.world_spiritual_landscape
    2 -> R.drawable.world_immortal_landscape
    else -> R.drawable.world_chaos_landscape
}

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
