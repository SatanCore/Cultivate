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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

private val Ink = Color(0xFF101820)
private val Panel = Color(0xFF1B2A32)
private val Gold = Color(0xFFE5BD72)
private val Jade = Color(0xFF78C6A3)

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
    var qi by remember { mutableIntStateOf(prefs.getInt("qi", 0)) }
    var stones by remember { mutableIntStateOf(prefs.getInt("stones", 120)) }
    var stage by remember { mutableIntStateOf(prefs.getInt("stage", 1)) }
    var petLevel by remember { mutableIntStateOf(prefs.getInt("pet", 1)) }
    var enemyHp by remember { mutableIntStateOf(prefs.getInt("enemy", 100)) }
    var wins by remember { mutableIntStateOf(prefs.getInt("wins", 0)) }
    var message by remember { mutableStateOf("Сосредоточься. Путь бессмертного начинается с одного вдоха.") }

    LaunchedEffect(Unit) { while (true) { delay(5000); qi = (qi + 1).coerceAtMost(9999) } }
    LaunchedEffect(qi, stones, stage, petLevel, enemyHp, wins) {
        prefs.edit().putInt("qi", qi).putInt("stones", stones).putInt("stage", stage)
            .putInt("pet", petLevel).putInt("enemy", enemyHp).putInt("wins", wins).apply()
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = Gold, secondary = Jade, background = Ink, surface = Panel, onBackground = Color(0xFFF0E8D8), onSurface = Color(0xFFF0E8D8))) {
        Column(Modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("ПУТЬ БЕССМЕРТНОГО", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Cultivate", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Text("Тихая сила рождается внутри.", color = Jade)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Ци", qi.toString(), Modifier.weight(1f))
                StatCard("Камни духа", stones.toString(), Modifier.weight(1f))
                StatCard("Победы", wins.toString(), Modifier.weight(1f))
            }
            GameCard("Культивация", "Ступень $stage · " + stageName(stage)) {
                Text("Ци накапливается со временем. Для прорыва требуется ${stage * 35} Ци.", color = Color.LightGray)
                LinearProgressIndicator(progress = { (qi.toFloat() / (stage * 35).coerceAtLeast(1)).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth(), color = Jade)
                Button(onClick = {
                    val cost = stage * 35
                    if (qi >= cost) { qi -= cost; stage++; stones += 25; message = "Прорыв свершился! Ты достиг ступени $stage." }
                    else message = "Недостаточно Ци. Продолжай медитировать."
                }, modifier = Modifier.fillMaxWidth()) { Text("Совершить прорыв") }
            }
            GameCard("Арена испытаний", "Страж древнего святилища") {
                Text("Здоровье противника: $enemyHp / 100", color = Color.LightGray)
                LinearProgressIndicator(progress = { enemyHp / 100f }, modifier = Modifier.fillMaxWidth(), color = Color(0xFFD77A68))
                Button(onClick = {
                    if (enemyHp <= 0) message = "Страж уже повержен. Начни следующий бой."
                    else {
                        val damage = 22 + stage * 3
                        enemyHp = (enemyHp - damage).coerceAtLeast(0)
                        if (enemyHp == 0) { stones += 80 + stage * 10; wins++; message = "Победа! Получено ${80 + stage * 10} камней духа." }
                        else message = "Техника Ци нанесла $damage урона."
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Удар потоком Ци") }
                OutlinedButton(onClick = { enemyHp = 100; message = "Новый страж вступает на арену." }, modifier = Modifier.fillMaxWidth()) { Text("Новый противник") }
            }
            GameCard("Духовный спутник", "Нефритовый лис · уровень $petLevel") {
                Text("Следующий уровень стоит ${petLevel * 60} камней духа.", color = Color.LightGray)
                Button(onClick = {
                    val cost = petLevel * 60
                    if (stones >= cost) { stones -= cost; petLevel++; message = "Нефритовый лис достиг $petLevel уровня." }
                    else message = "Не хватает камней духа."
                }, modifier = Modifier.fillMaxWidth()) { Text("Улучшить спутника") }
            }
            Text(message, color = Gold, fontSize = 14.sp)
            Text("Сохранение выполняется автоматически на этом устройстве.", color = Color.Gray, fontSize = 11.sp)
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
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = Gold, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Jade, fontSize = 13.sp)
            content()
        }
    }
}
private fun stageName(stage: Int): String = when {
    stage < 4 -> "Закалка тела"
    stage < 7 -> "Сбор Ци"
    stage < 10 -> "Золотое ядро"
    stage < 13 -> "Зарождение души"
    else -> "Вознесение"
}
