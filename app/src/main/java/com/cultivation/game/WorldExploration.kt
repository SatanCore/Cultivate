package com.cultivation.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ExpeditionGold = Color(0xFFE5BD72)
private val ExpeditionJade = Color(0xFF78C6A3)

data class DaoWorldInfo(
    val index: Int,
    val title: String,
    val description: String,
    val requiredStage: Int,
    val backgroundRes: Int,
    val accent: Color
)

data class ExplorationLocation(
    val id: String,
    val worldIndex: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val requiredStage: Int,
    val recommendedPower: Int,
    val iconRes: Int,
    val resourceKind: String,
    val resourceLabel: String,
    val resourceAmount: Int,
    val materialTier: Int = -1,
    val stoneReward: Int,
    val qiReward: Int,
    val isBoss: Boolean = false,
    val bossRank: Int = 0,
    val progressGoal: Int = 3,
    val battlePortraitRes: Int = R.drawable.boss_mountain_lord,
    val enemyName: String = "Дикий дух",
    val enemyPortraitRes: Int = R.drawable.enemy_mortal_wolf,
    val isElite: Boolean = false,
    val eliteAbilityName: String = "",
    val enemyDamageBonus: Int = 0,
    val enemyHpMultiplier: Float = 1f
)

val daoWorldCatalog = listOf(
    DaoWorldInfo(0, "Смертный мир", "Горные долины, древние святилища и первые испытания пути Дао.", 1, R.drawable.world_mortal_landscape, Color(0xFFE8BD78)),
    DaoWorldInfo(1, "Духовный мир", "Нефритовые леса, водопады Ци и забытые владения духовных зверей.", 7, R.drawable.world_spiritual_landscape, Color(0xFF7DE0C1)),
    DaoWorldInfo(2, "Бессмертный мир", "Парящие небесные острова, золотые дворцы и наследие древних бессмертных.", 13, R.drawable.world_immortal_landscape, Color(0xFFFFD98A)),
    DaoWorldInfo(3, "Мир Хаоса", "Разломы пространства, осколки первозданного мира и печати Хаоса.", 19, R.drawable.world_chaos_landscape, Color(0xFFC69AFF))
)

val worldLocationCatalog = listOf(
    ExplorationLocation("mortal_valley", 0, "Долина Туманов", "Разведка · каменные тропы", "Безопасные тропы вокруг Лунной Гавани. Здесь можно найти железную руду и следы старых караванов.", 1, 650, R.drawable.world_location_valley, "ore", "железная руда", 1, stoneReward = 70, qiReward = 8, enemyName = "Волк туманных троп", enemyPortraitRes = R.drawable.enemy_mortal_wolf),
    ExplorationLocation("mortal_bamboo", 0, "Бамбуковый лес", "Сбор · духовные травы", "Тихий лес скрывает лекарственные растения и небольшие потоки природной Ци.", 2, 800, R.drawable.world_location_forest, "herbs", "духовные травы", 2, stoneReward = 80, qiReward = 10, enemyName = "Клыкастый вепрь", enemyPortraitRes = R.drawable.enemy_mortal_boar),
    ExplorationLocation("mortal_ruins", 0, "Забытое святилище", "Руины · осколки духа", "Разрушенный храм хранит остатки печатей. В глубине можно найти материалы для развития боевых техник.", 4, 1050, R.drawable.world_location_ruins, "material", "осколок духа", 1, materialTier = 0, stoneReward = 95, qiReward = 12, enemyName = "Смотритель святилища", enemyPortraitRes = R.drawable.enemy_mortal_boar, isElite = true, eliteAbilityName = "Каменный натиск", enemyDamageBonus = 12),
    ExplorationLocation("mortal_guardian", 0, "Страж долины", "Хранитель · босс мира", "Древний страж проверяет тех, кто исследовал все окрестные земли. Победа открывает путь к редким фрагментам мира.", 6, 1350, R.drawable.world_location_guardian, "material", "осколок духа", 2, materialTier = 0, stoneReward = 250, qiReward = 30, isBoss = true, bossRank = 7, battlePortraitRes = R.drawable.boss_mortal_guardian),

    ExplorationLocation("spirit_jade_forest", 1, "Нефритовый лес", "Сбор · духовные травы", "Древние деревья впитали чистую Ци. Среди корней растут редкие травы духовного мира.", 7, 1650, R.drawable.world_location_forest, "herbs", "духовные травы", 3, stoneReward = 120, qiReward = 15, enemyName = "Нефритовый лис", enemyPortraitRes = R.drawable.enemy_spirit_fox),
    ExplorationLocation("spirit_waterfall", 1, "Водопад Небесной Росы", "Родник · духовная вода", "Вода падает с парящих скал и собирает энергию нефритовых жил.", 8, 1900, R.drawable.world_location_valley, "water", "духовная вода", 2, stoneReward = 140, qiReward = 18, enemyName = "Страж водопада", enemyPortraitRes = R.drawable.enemy_spirit_fox),
    ExplorationLocation("spirit_ruins", 1, "Руины Древней Секты", "Руины · нефритовая эссенция", "Заброшенные залы всё ещё хранят следы древних техник и редкие материалы для навыков.", 10, 2250, R.drawable.world_location_ruins, "material", "нефритовая эссенция", 1, materialTier = 1, stoneReward = 165, qiReward = 20, enemyName = "Древесный хранитель", enemyPortraitRes = R.drawable.enemy_spirit_tree, isElite = true, eliteAbilityName = "Корни древней секты", enemyDamageBonus = 18),
    ExplorationLocation("spirit_guardian", 1, "Страж Нефритового Пика", "Хранитель · босс мира", "Духовный зверь охраняет путь к небесным островам. Чтобы вызвать его, исследуй все три территории мира.", 12, 2700, R.drawable.world_location_guardian, "material", "нефритовая эссенция", 2, materialTier = 1, stoneReward = 320, qiReward = 40, isBoss = true, bossRank = 13, battlePortraitRes = R.drawable.boss_spirit_stag),

    ExplorationLocation("immortal_isles", 2, "Парящие острова", "Разведка · небесные жилы", "На островах среди облаков встречаются кристаллы, из которых рождаются бессмертные артефакты.", 13, 3150, R.drawable.world_location_celestial, "material", "сердце зверя", 1, materialTier = 2, stoneReward = 195, qiReward = 24, enemyName = "Небесный журавль", enemyPortraitRes = R.drawable.enemy_immortal_crane),
    ExplorationLocation("immortal_lake", 2, "Золотое озеро", "Родник · чистая вода", "Вода озера отражает небесные дворцы и насыщена энергией золотого лотоса.", 14, 3500, R.drawable.world_location_valley, "water", "духовная вода", 3, stoneReward = 220, qiReward = 27, enemyName = "Золотой карп-хранитель", enemyPortraitRes = R.drawable.enemy_immortal_crane),
    ExplorationLocation("immortal_archive", 2, "Архив Бессмертных", "Руины · кристаллы души", "В древнем архиве сохранились фрагменты техник, которые усиливают высокие ступени культивации.", 16, 3950, R.drawable.world_location_ruins, "material", "кристалл души", 1, materialTier = 3, stoneReward = 255, qiReward = 31, enemyName = "Страж золотого архива", enemyPortraitRes = R.drawable.enemy_immortal_sentinel, isElite = true, eliteAbilityName = "Золотая печать", enemyDamageBonus = 24),
    ExplorationLocation("immortal_guardian", 2, "Небесный Страж", "Хранитель · босс мира", "Страж небесного дворца не пропустит путника без доказательства силы. Исследуй все три острова, прежде чем бросить вызов.", 18, 4550, R.drawable.world_location_guardian, "material", "кристалл души", 2, materialTier = 3, stoneReward = 420, qiReward = 50, isBoss = true, bossRank = 19, battlePortraitRes = R.drawable.boss_immortal_dragon),

    ExplorationLocation("chaos_shore", 3, "Берег Пустоты", "Разведка · осколки Хаоса", "На краю разлома оседают обломки первозданной материи. Их энергия опасна, но ценна.", 19, 5200, R.drawable.world_location_chaos, "material", "печать бессмертия", 2, materialTier = 4, stoneReward = 275, qiReward = 34, enemyName = "Осколочный призрак", enemyPortraitRes = R.drawable.enemy_chaos_wraith),
    ExplorationLocation("chaos_ruins", 3, "Расколотая цитадель", "Руины · железная руда", "Разрушенная крепость парит над бездной. В её основании сохранились жилы прочного чёрного металла.", 20, 5750, R.drawable.world_location_ruins, "ore", "железная руда", 4, stoneReward = 305, qiReward = 37, enemyName = "Колосс цитадели", enemyPortraitRes = R.drawable.enemy_chaos_colossus, isElite = true, eliteAbilityName = "Разлом бездны", enemyDamageBonus = 30),
    ExplorationLocation("chaos_garden", 3, "Сад Первозданной Тьмы", "Сбор · редкие травы", "В саду растут растения, питающиеся энергией разломов. Их нельзя найти ни в одном другом мире.", 22, 6400, R.drawable.world_location_forest, "herbs", "духовные травы", 4, stoneReward = 345, qiReward = 42, enemyName = "Пожиратель Ци", enemyPortraitRes = R.drawable.enemy_chaos_wraith),
    ExplorationLocation("chaos_guardian", 3, "Печать Хаоса", "Хранитель · босс мира", "За печатью скрывается древний хранитель разлома. Победа приносит фрагменты первозданного мира.", 24, 7100, R.drawable.world_location_guardian, "material", "ядро Хаоса", 3, materialTier = 5, stoneReward = 600, qiReward = 65, isBoss = true, bossRank = 25, battlePortraitRes = R.drawable.boss_chaos_overlord)
)

fun worldEncounterMaxHp(location: ExplorationLocation): Int {
    val eliteScale = if (location.isElite) 1.45f else 1f
    return ((180f + location.requiredStage * 45f) * location.enemyHpMultiplier * eliteScale)
        .toInt().coerceIn(250, 18_000)
}

@Composable
fun WorldExplorationScreen(
    stage: Int,
    combatPower: Int,
    selectedWorldIndex: Int,
    onSelectWorld: (Int) -> Unit,
    explorationEnergy: Int,
    locationProgress: List<Int>,
    worldFragments: List<Int>,
    defeatedBossIds: Set<String>,
    pendingBossId: String,
    pendingEncounterId: String,
    statusMessage: String,
    selectedLocationId: String,
    onSelectLocation: (String) -> Unit,
    onExplore: (String) -> Unit,
    onExchangeFragments: (Int) -> Unit
) {
    val safeWorldIndex = selectedWorldIndex.coerceIn(0, daoWorldCatalog.lastIndex)
    val world = daoWorldCatalog[safeWorldIndex]
    val locations = worldLocationCatalog.filter { it.worldIndex == safeWorldIndex }
    val selectedLocation = locations.firstOrNull { it.id == selectedLocationId } ?: locations.first()
    val selectedIndex = worldLocationCatalog.indexOfFirst { it.id == selectedLocation.id }.coerceAtLeast(0)
    val selectedProgress = locationProgress.getOrElse(selectedIndex) { 0 }.coerceIn(0, selectedLocation.progressGoal)
    val normalLocations = locations.filterNot { it.isBoss }
    val bossUnlocked = normalLocations.all { location ->
        val index = worldLocationCatalog.indexOfFirst { it.id == location.id }
        locationProgress.getOrElse(index) { 0 } >= location.progressGoal
    }
    val selectedDefeated = selectedLocation.isBoss && selectedLocation.id in defeatedBossIds
    val selectedPending = pendingBossId == selectedLocation.id || pendingEncounterId == selectedLocation.id
    val selectedLocked = stage < selectedLocation.requiredStage || (selectedLocation.isBoss && !bossUnlocked)
    val selectedComplete = !selectedLocation.isBoss && selectedProgress >= selectedLocation.progressGoal
    val canExplore = when {
        selectedPending -> true
        selectedLocked || selectedDefeated || selectedComplete -> false
        selectedLocation.isBoss -> explorationEnergy >= 3
        else -> explorationEnergy > 0
    }
    val actionLabel = when {
        selectedPending -> "ПРОДОЛЖИТЬ БИТВУ"
        selectedDefeated -> "ХРАНИТЕЛЬ ПОВЕРЖЕН"
        selectedComplete -> "ТЕРРИТОРИЯ ИССЛЕДОВАНА"
        selectedLocked && selectedLocation.isBoss && stage >= selectedLocation.requiredStage -> "ИССЛЕДУЙ ВСЕ 3 ТЕРРИТОРИИ"
        selectedLocked -> "ТРЕБУЕТСЯ СТУПЕНЬ ${selectedLocation.requiredStage}"
        selectedLocation.isBoss -> "ВЫЗВАТЬ ХРАНИТЕЛЯ · 3 ЭНЕРГИИ"
        explorationEnergy <= 0 -> "ЭНЕРГИЯ ИСЧЕРПАНА"
        else -> if (selectedLocation.isElite) "ВСТУПИТЬ В ЭЛИТНЫЙ БОЙ · 1 ЭНЕРГИЯ" else "ВСТУПИТЬ В БОЙ · 1 ЭНЕРГИЯ"
    }
    val nextWorld = daoWorldCatalog.getOrNull(safeWorldIndex + 1)
    val worldAccent = world.accent
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        if (statusMessage.isNotBlank()) {
            Surface(
                color = Color(0xCC17263A),
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, worldAccent.copy(alpha = .35f))
            ) {
                Text(statusMessage, Modifier.fillMaxWidth().padding(9.dp), color = Color(0xFFE8D8B7), fontSize = 9.sp, maxLines = 3)
            }
        }
        Surface(
            color = Color(0xEE14253B),
            shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(1.dp, worldAccent.copy(alpha = .72f)),
            shadowElevation = 8.dp
        ) {
            Box(Modifier.fillMaxWidth().height(172.dp)) {
                Image(
                    painter = painterResource(world.backgroundRes),
                    contentDescription = world.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                    0f to Color(0x44101A2D),
                    .48f to Color(0x55101A2D),
                    1f to Color(0xF20B1424)
                )))
                Column(
                    Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(13.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("КАРТА ЭКСПЕДИЦИЙ · МИР ${safeWorldIndex + 1}", color = worldAccent, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    Text(world.title, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                    Text(world.description, color = Color(0xFFE0E8F2), fontSize = 10.sp, maxLines = 2)
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            daoWorldCatalog.forEach { realm ->
                val unlocked = stage >= realm.requiredStage
                val selected = realm.index == safeWorldIndex
                Surface(
                    modifier = Modifier.width(145.dp).height(83.dp).clickable { onSelectWorld(realm.index) },
                    color = Color(0xFF14243A),
                    shape = MaterialTheme.shapes.large,
                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) realm.accent else Color(0x557F91A8))
                ) {
                    Box {
                        Image(
                            painter = painterResource(realm.backgroundRes),
                            contentDescription = realm.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                            0f to Color(0x22101A2D),
                            1f to Color(0xE9101B2D)
                        )))
                        Column(Modifier.align(Alignment.BottomStart).padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(realm.title, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            Text(
                                if (!unlocked) "ПЕЧАТЬ · СТУПЕНЬ ${realm.requiredStage}" else if (selected) "ВЫБРАНО" else "ОТКРЫТО",
                                color = if (unlocked) realm.accent else Color(0xFFE8C88F),
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            ExplorationMetric("ЭНЕРГИЯ", "$explorationEnergy / 6", "экспедиции", Modifier.weight(1f), worldAccent)
            ExplorationMetric("ФРАГМЕНТЫ", worldFragments.getOrElse(safeWorldIndex) { 0 }.toString(), "этого мира", Modifier.weight(1f), worldAccent)
            ExplorationMetric("МОЩЬ", combatPower.toString(), "текущая", Modifier.weight(1f), worldAccent)
        }

        Text("ТЕРРИТОРИИ И ИСПЫТАНИЯ", color = ExpeditionGold, fontSize = 10.sp, fontWeight = FontWeight.Black)
        locations.forEach { location ->
            val index = worldLocationCatalog.indexOfFirst { it.id == location.id }.coerceAtLeast(0)
            val progress = locationProgress.getOrElse(index) { 0 }.coerceIn(0, location.progressGoal)
            val isBossUnlocked = !location.isBoss || normalLocations.all { normal ->
                val normalIndex = worldLocationCatalog.indexOfFirst { it.id == normal.id }
                locationProgress.getOrElse(normalIndex) { 0 } >= normal.progressGoal
            }
            val locked = stage < location.requiredStage || !isBossUnlocked
            val completed = if (location.isBoss) location.id in defeatedBossIds else progress >= location.progressGoal
            WorldLocationCard(
                location = location,
                progress = progress,
                locked = locked,
                completed = completed,
                selected = selectedLocation.id == location.id,
                pending = location.id == pendingBossId || location.id == pendingEncounterId,
                onClick = { onSelectLocation(location.id) }
            )
        }

        Surface(
            color = Color(0xE8172942),
            shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(1.dp, worldAccent.copy(alpha = .65f)),
            shadowElevation = 6.dp
        ) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(if (selectedLocation.isBoss) selectedLocation.battlePortraitRes else selectedLocation.enemyPortraitRes),
                        contentDescription = selectedLocation.title,
                        modifier = Modifier.size(52.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            if (selectedLocation.isBoss) "ХРАНИТЕЛЬ МИРА" else if (selectedLocation.isElite) "ЭЛИТНЫЙ ПРОТИВНИК" else "ПРОТИВНИК ТЕРРИТОРИИ",
                            color = worldAccent, fontSize = 8.sp, fontWeight = FontWeight.Black
                        )
                        Text(if (selectedLocation.isBoss) selectedLocation.title else selectedLocation.enemyName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text(if (selectedLocation.isBoss) selectedLocation.subtitle else "${selectedLocation.title} · ${selectedLocation.subtitle}", color = Color(0xFFC9D5E4), fontSize = 9.sp)
                    }
                }
                Text(selectedLocation.description, color = Color(0xFFE0E8F2), fontSize = 10.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    ExplorationRewardChip("Сила", selectedLocation.recommendedPower.toString(), Modifier.weight(1f))
                    ExplorationRewardChip("Камни", "+${selectedLocation.stoneReward}", Modifier.weight(1f))
                    ExplorationRewardChip("Ци", "+${selectedLocation.qiReward}", Modifier.weight(1f))
                }
                Text(
                    if (selectedLocation.isBoss) "Победа над хранителем: фрагменты мира ×5 · ранг стража ${selectedLocation.bossRank}"
                    else "Победа над ${selectedLocation.enemyName}: ${selectedLocation.resourceLabel} ×${selectedLocation.resourceAmount} · прогресс ${selectedProgress}/${selectedLocation.progressGoal}",
                    color = ExpeditionJade,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { onExplore(selectedLocation.id) },
                    enabled = canExplore,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            selectedLocation.isBoss -> Color(0xFF7D4A83)
                            selectedLocation.isElite -> Color(0xFF75552B)
                            else -> Color(0xFF236A5B)
                        },
                        disabledContainerColor = Color(0xFF354153)
                    )
                ) {
                    Text(actionLabel, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
                if (nextWorld != null && safeWorldIndex == daoWorldCatalog.lastIndex - 1) {
                    Text("Следующие врата откроются на ступени ${nextWorld.requiredStage}.", color = Color(0xFFC6D2E2), fontSize = 8.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("СОКРОВИЩНИЦА МИРА", color = ExpeditionGold, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        Text("Фрагменты можно обменять на редкие материалы развития.", color = Color(0xFFC6D2E2), fontSize = 8.sp)
                    }
                    Button(
                        onClick = { onExchangeFragments(safeWorldIndex) },
                        enabled = worldFragments.getOrElse(safeWorldIndex) { 0 } >= 5,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6A552E),
                            disabledContainerColor = Color(0xFF354153)
                        )
                    ) {
                        Text("ОБМЕН · 5", fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun WorldLocationCard(
    location: ExplorationLocation,
    progress: Int,
    locked: Boolean,
    completed: Boolean,
    selected: Boolean,
    pending: Boolean,
    onClick: () -> Unit
) {
    val accent = daoWorldCatalog[location.worldIndex].accent
    val value = if (location.isBoss) if (completed) 1f else 0f else (progress.toFloat() / location.progressGoal).coerceIn(0f, 1f)
    Surface(
        modifier = Modifier.fillMaxWidth().height(91.dp).clickable(onClick = onClick),
        color = Color(0xFF17263A),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(if (selected) 2.dp else 1.dp, when {
            completed -> ExpeditionJade.copy(alpha = .8f)
            selected -> accent
            locked -> Color(0x557B879B)
            else -> accent.copy(alpha = .35f)
        }),
        shadowElevation = if (selected) 6.dp else 2.dp
    ) {
        Row(Modifier.fillMaxSize().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF0D1929)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(if (location.isBoss) location.battlePortraitRes else location.enemyPortraitRes),
                    contentDescription = location.title,
                    modifier = Modifier.fillMaxSize().padding(5.dp),
                    contentScale = ContentScale.Fit
                )
                if (locked) {
                    Box(Modifier.fillMaxSize().background(Color(0x99070E1B)), contentAlignment = Alignment.Center) {
                        Text("✦", color = Color(0xFFE5C78C), fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(location.title, color = if (completed) ExpeditionJade else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1)
                Text(if (location.isBoss) location.subtitle else "${location.enemyName} · ${location.subtitle}", color = Color(0xFFC7D4E3), fontSize = 8.sp, maxLines = 1)
                LinearProgressIndicator(
                    progress = { value },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = if (completed) ExpeditionJade else accent,
                    trackColor = Color(0xFF344459)
                )
                Text(
                    when {
                        completed -> "ЗАВЕРШЕНО"
                        pending -> "БОЙ ОЖИДАЕТ ПРОДОЛЖЕНИЯ"
                        locked && location.isBoss -> "НУЖНО ЗАВЕРШИТЬ 3 ТЕРРИТОРИИ"
                        locked -> "ОТКРЫВАЕТСЯ НА СТУПЕНИ ${location.requiredStage}"
                        location.isBoss -> "ХРАНИТЕЛЬ · РАНГ ${location.bossRank}"
                        location.isElite -> "ЭЛИТА · ИССЛЕДОВАНО $progress / ${location.progressGoal}"
                        else -> "ИССЛЕДОВАНО · $progress / ${location.progressGoal}"
                    },
                    color = if (completed) ExpeditionJade else if (locked) Color(0xFFE4C58B) else accent,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Text(if (completed) "✓" else if (locked) "✦" else "›", color = if (completed) ExpeditionJade else accent, fontSize = 23.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun ExplorationMetric(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier,
    accent: Color
) {
    Surface(
        modifier = modifier,
        color = Color(0xCC17263A),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, accent.copy(alpha = .38f))
    ) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, color = accent, fontSize = 7.sp, fontWeight = FontWeight.Black)
            Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(subtitle, color = Color(0xFFB7C5D5), fontSize = 7.sp)
        }
    }
}

@Composable
private fun ExplorationRewardChip(label: String, value: String, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xCC0F1C2D),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, Color(0x446E8AA6))
    ) {
        Column(Modifier.padding(horizontal = 7.dp, vertical = 6.dp)) {
            Text(label, color = Color(0xFFB7C5D5), fontSize = 7.sp)
            Text(value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
