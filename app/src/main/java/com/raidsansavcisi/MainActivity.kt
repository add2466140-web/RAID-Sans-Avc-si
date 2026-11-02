package com.raidsansavcisi

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Hero(
    val name: String,
    val title: String,
    val attack: Int,
    val health: Int,
    val symbol: String
)

private val heroes = listOf(
    Hero("Ardan", "Karanlık Şövalye", 35, 180, "⚔"),
    Hero("Lyra", "Büyücü", 48, 110, "✦"),
    Hero("Kael", "Gölge Avcısı", 42, 130, "弓"),
    Hero("Bruna", "Demir Muhafız", 25, 230, "🛡"),
    Hero("Vex", "Gece Suikastçısı", 55, 100, "☠")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GGGame() }
    }
}

@Composable
fun GGGame() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("gg_game", Context.MODE_PRIVATE)
    }

    var tab by remember { mutableStateOf("Ana Sayfa") }
    var gold by remember { mutableIntStateOf(prefs.getInt("gold", 500)) }
    var stage by remember { mutableIntStateOf(prefs.getInt("stage", 1)) }
    var playerHp by remember { mutableIntStateOf(500) }
    var enemyHp by remember { mutableIntStateOf(150 + stage * 35) }
    var message by remember { mutableStateOf("Savaşmaya hazırsın!") }
    var selectedHero by remember { mutableIntStateOf(0) }

    val levels = remember {
        mutableStateMapOf<String, Int>().apply {
            heroes.forEach {
                put(it.name, prefs.getInt("level_${it.name}", 1))
            }
        }
    }

    val enemyName = when {
        stage % 5 == 0 -> "Karanlık Ejderha"
        stage % 3 == 0 -> "Kemik Şövalye"
        else -> "Gölge Canavarı"
    }

    val enemyMaxHp = 150 + stage * 35

    fun save() {
        prefs.edit()
            .putInt("gold", gold)
            .putInt("stage", stage)
            .apply()
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFD5A84C),
            secondary = Color(0xFF9C6BCE),
            background = Color(0xFF101018),
            surface = Color(0xFF20202B)
        )
    ) {
        Scaffold(
            containerColor = Color(0xFF101018),
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF191923)
                ) {
                    listOf("Ana Sayfa", "Kahramanlar", "Savaş").forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = {
                                Text(
                                    when (item) {
                                        "Ana Sayfa" -> "⌂"
                                        "Kahramanlar" -> "⚔"
                                        else -> "☠"
                                    }
                                )
                            },
                            label = { Text(item) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF252033),
                                Color(0xFF101018)
                            )
                        )
                    )
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    "GG",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFFE4BC68),
                    fontWeight = FontWeight.Black
                )

                Text(
                    "Karanlık diyarların efsanesi",
                    color = Color.LightGray
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Altın: $gold", color = Color(0xFFFFD36A))
                    Text("Bölüm: $stage", color = Color.White)
                }

                Spacer(Modifier.height(20.dp))

                when (tab) {
                    "Ana Sayfa" -> {
                        Text(
                            "MACERA BAŞLIYOR",
                            color = Color(0xFFE4BC68),
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(Modifier.height(16.dp))

                        GameCard("Karanlık Diyar") {
                            Text(
                                "Canavarlarla savaş, altın kazan ve kahramanlarını güçlendir.",
                                color = Color.LightGray
                            )
                        }

                        GameCard("Ekibin") {
                            heroes.forEach {
                                Text(
                                    "${it.symbol} ${it.name} — ${it.title}",
                                    color = Color.White,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                )
                            }
                        }

                        GameCard("İlerleme") {
                            Text("Tamamlanan bölüm: ${stage - 1}")
                            Text("Savaşçı sayısı: ${heroes.size}")
                            Text("Oyun modu: Çevrimdışı")
                        }

                        Button(
                            onClick = { tab = "Savaş" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SAVAŞA GİR")
                        }
                    }

                    "Kahramanlar" -> {
                        Text(
                            "KAHRAMANLAR",
                            color = Color(0xFFE4BC68),
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(Modifier.height(10.dp))

                        LazyColumn {
                            items(heroes) { hero ->
                                val level = levels[hero.name] ?: 1

                                GameCard("${hero.symbol} ${hero.name}") {
                                    Text(hero.title, color = Color.LightGray)
                                    Text("Seviye: $level", color = Color.White)
                                    Text(
                                        "Saldırı: ${hero.attack + level * 5}",
                                        color = Color(0xFFFFD36A)
                                    )
                                    Text("Can: ${hero.health + level * 15}")

                                    Button(
                                        onClick = {
                                            val cost = level * 100
                                            if (gold >= cost) {
                                                gold -= cost
                                                levels[hero.name] = level + 1
                                                prefs.edit()
                                                    .putInt(
                                                        "level_${hero.name}",
                                                        level + 1
                                                    )
                                                    .apply()
                                                save()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Geliştir — ${level * 100} altın")
                                    }
                                }
                            }
                        }
                    }

                    "Savaş" -> {
                        Text(
                            "KARANLIK ZİNDAN",
                            color = Color(0xFFE4BC68),
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(Modifier.height(12.dp))

                        GameCard("Bölüm $stage — $enemyName") {
                            Text(
                                "Düşman canı: $enemyHp / $enemyMaxHp",
                                color = Color(0xFFFF7777)
                            )
                            LinearProgressIndicator(
                                progress = {
                                    (enemyHp.toFloat() / enemyMaxHp)
                                        .coerceIn(0f, 1f)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.Red
                            )
                        }

                        GameCard("Senin takımın") {
                            Text(
                                "Takım canı: $playerHp / 500",
                                color = Color(0xFF7DE59A)
                            )
                            LinearProgressIndicator(
                                progress = {
                                    (playerHp.toFloat() / 500)
                                        .coerceIn(0f, 1f)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.Green
                            )
                        }

                        GameCard("Savaş günlüğü") {
                            Text(message, color = Color.White)
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (enemyHp > 0 && playerHp > 0) {
                                    val hero = heroes[selectedHero]
                                    val level = levels[hero.name] ?: 1
                                    val damage = hero.attack + level * 5
                                    val remaining = enemyHp - damage

                                    if (remaining <= 0) {
                                        enemyHp = 0
                                        val reward = 100 + stage * 20
                                        gold += reward
                                        stage += 1
                                        enemyHp = 150 + stage * 35
                                        playerHp = 500
                                        message =
                                            "Zafer! $reward altın kazandın. Yeni bölüm açıldı!"
                                        save()
                                    } else {
                                        enemyHp = remaining
                                        val enemyDamage = 25 + stage * 3
                                        playerHp =
                                            (playerHp - enemyDamage).coerceAtLeast(0)

                                        message =
                                            "${hero.name} $damage hasar verdi. " +
                                            "Düşman $enemyDamage hasar verdi."

                                        if (playerHp <= 0) {
                                            playerHp = 500
                                            message =
                                                "Takım yenildi! Canın yenilendi. Tekrar dene."
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SALDIR")
                        }

                        Spacer(Modifier.height(8.dp))

                        Text("Savaşacak kahramanı seç:", color = Color.White)

                        heroes.forEachIndexed { index, hero ->
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedHero == index,
                                    onClick = { selectedHero = index }
                                )
                                Text(
                                    "${hero.symbol} ${hero.name}",
                                    color = Color.White
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                playerHp = 500
                                enemyHp = enemyMaxHp
                                message = "Yeni savaş başladı!"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SAVAŞI YENİLE")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF252532)
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                title,
                color = Color(0xFFE4BC68),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}
