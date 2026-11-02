package com.raidsansavcisi

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    val symbol: String,
    val skill: String
)

private val heroes = listOf(
    Hero("Ardan", "Karanlık Şövalye", 35, 180, "⚔", "Yıkıcı Darbe"),
    Hero("Lyra", "Büyücü", 48, 110, "✦", "Ateş Patlaması"),
    Hero("Kael", "Gölge Avcısı", 42, 130, "弓", "Zehirli Ok"),
    Hero("Bruna", "Demir Muhafız", 25, 230, "🛡", "Demir Savunma"),
    Hero("Vex", "Gece Suikastçısı", 55, 100, "☠", "Ölümcül Saldırı")
)

private val equipmentNames = listOf("Silah", "Zırh", "Bot", "Yüzük")

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
    var selectedHero by remember { mutableIntStateOf(0) }
    var playerHp by remember { mutableIntStateOf(500) }
    var enemyHp by remember { mutableIntStateOf(150 + stage * 35) }
    var message by remember { mutableStateOf("Savaşmaya hazırsın!") }
    var skillReady by remember { mutableStateOf(true) }
    var equipmentSlot by remember { mutableIntStateOf(0) }

    val levels = remember {
        mutableStateMapOf<String, Int>().apply {
            heroes.forEach {
                put(it.name, prefs.getInt("level_${it.name}", 1))
            }
        }
    }

    val equipmentLevels = remember {
        mutableStateMapOf<String, Int>().apply {
            heroes.forEach { hero ->
                equipmentNames.forEachIndexed { index, slot ->
                    put(
                        "${hero.name}_$slot",
                        prefs.getInt("${hero.name}_$slot", 0)
                    )
                }
            }
        }
    }

    val enemyName = when {
        stage % 10 == 0 -> "İBLİS KRALI"
        stage % 5 == 0 -> "KARANLIK EJDERHA"
        stage % 3 == 0 -> "KEMİK ŞÖVALYE"
        else -> "GÖLGE CANAVARI"
    }

    val enemyMaxHp = 150 + stage * 35 +
            if (stage % 5 == 0) stage * 50 else 0

    val isBoss = stage % 5 == 0

    val hero = heroes[selectedHero]
    val heroLevel = levels[hero.name] ?: 1

    val equipmentBonus = equipmentNames.sumOf {
        (equipmentLevels["${hero.name}_$it"] ?: 0) * 8
    }

    val totalAttack = hero.attack + heroLevel * 5 + equipmentBonus

    val totalHealth = hero.health + heroLevel * 15 +
            equipmentNames.sumOf {
                (equipmentLevels["${hero.name}_$it"] ?: 0) * 15
            }

    fun save() {
        prefs.edit()
            .putInt("gold", gold)
            .putInt("stage", stage)
            .apply()
    }

    fun attack(useSkill: Boolean) {
        if (enemyHp <= 0 || playerHp <= 0) return

        if (useSkill && !skillReady) {
            message = "Özel yetenek henüz hazır değil!"
            return
        }

        val damage = if (useSkill) totalAttack * 3 else totalAttack
        enemyHp = (enemyHp - damage).coerceAtLeast(0)

        if (useSkill) skillReady = false

        if (enemyHp <= 0) {
            val reward = if (isBoss) 500 + stage * 50 else 100 + stage * 20
            gold += reward
            stage += 1
            enemyHp = 150 + stage * 35 +
                    if (stage % 5 == 0) stage * 50 else 0
            playerHp = 500
            skillReady = true
            message = "ZAFER! $reward altın kazandın! Yeni bölüm açıldı."
            save()
        } else {
            val enemyDamage = if (isBoss) 55 + stage * 4 else 25 + stage * 3
            playerHp = (playerHp - enemyDamage).coerceAtLeast(0)

            message = if (useSkill) {
                "${hero.name}, ${hero.skill} kullandı! $damage hasar verdi. Düşman $enemyDamage hasar verdi."
            } else {
                "${hero.name} saldırdı! $damage hasar verdi. Düşman $enemyDamage hasar verdi."
            }

            if (playerHp <= 0) {
                playerHp = 500
                message += " Takım yenildi, canın yenilendi."
            }
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFD5A84C),
            secondary = Color(0xFF9C6BCE),
            background = Color(0xFF101018),
            surface = Color(0xFF252532)
        )
    ) {
        Scaffold(
            containerColor = Color(0xFF101018),
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF191923)) {
                    listOf(
                        "Ana Sayfa",
                        "Kahramanlar",
                        "Ekipman",
                        "Zindan",
                        "Savaş"
                    ).forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = {
                                Text(
                                    when (item) {
                                        "Ana Sayfa" -> "⌂"
                                        "Kahramanlar" -> "⚔"
                                        "Ekipman" -> "🛡"
                                        "Zindan" -> "🏰"
                                        else -> "☠"
                                    }
                                )
                            },
                            label = { Text(item, maxLines = 1) }
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
                    .verticalScroll(rememberScrollState())
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

                Spacer(Modifier.height(18.dp))

                when (tab) {

                    "Ana Sayfa" -> {
                        Text(
                            "MACERA BAŞLIYOR",
                            color = Color(0xFFE4BC68),
                            style = MaterialTheme.typography.titleLarge
                        )

                        GameCard("Karanlık Diyar") {
                            Text(
                                "Canavarları yen, zindanları keşfet, ekipmanlarını geliştir ve efsane ol.",
                                color = Color.LightGray
                            )
                        }

                        GameCard("Kahraman Ekibin") {
                            heroes.forEach {
                                Text(
                                    "${it.symbol} ${it.name} — ${it.title}",
                                    color = Color.White
                                )
                            }
                        }

                        GameCard("Oyun Bilgileri") {
                            Text("Açılan bölüm: ${stage - 1}")
                            Text("Kahraman sayısı: ${heroes.size}")
                            Text("Oyun modu: Çevrimdışı")
                            Text("Patron savaşları: Her 5 bölümde")
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

                        heroes.forEachIndexed { index, h ->
                            val level = levels[h.name] ?: 1

                            GameCard("${h.symbol} ${h.name}") {
                                Text(h.title, color = Color.LightGray)
                                Text("Seviye: $level")
                                Text("Saldırı: ${h.attack + level * 5}")
                                Text("Can: ${h.health + level * 15}")
                                Text("Özel yetenek: ${h.skill}")

                                Button(
                                    onClick = {
                                        val cost = level * 100
                                        if (gold >= cost) {
                                            gold -= cost
                                            levels[h.name] = level + 1
                                            prefs.edit()
                                                .putInt("level_${h.name}", level + 1)
                                                .apply()
                                            save()
                                            message = "${h.name} güçlendirildi!"
                                        } else {
                                            message = "Yeterli altının yok!"
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Seviye yükselt — ${level * 100} altın")
                                }

                                OutlinedButton(
                                    onClick = {
                                        selectedHero = index
                                        message = "${h.name} seçildi!"
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        if (selectedHero == index) "SEÇİLİ KAHRAMAN"
                                        else "BU KAHRAMANI SEÇ"
                                    )
                                }
                            }
                        }
                    }

                    "Ekipman" -> {
                        Text(
                            "EKİPMAN DEMİRCİSİ",
                            color = Color(0xFFE4BC68),
                            style = MaterialTheme.typography.titleLarge
                        )

                        GameCard("Kahraman seçimi") {
                            heroes.forEachIndexed { index, h ->
                                OutlinedButton(
                                    onClick = { selectedHero = index },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        if (selectedHero == index) "✓ ${h.name}"
                                        else h.name
                                    )
                                }
                            }
                        }

                        GameCard("${hero.name} ekipmanları") {
                            equipmentNames.forEach { slot ->
                                val level =
                                    equipmentLevels["${hero.name}_$slot"] ?: 0
                                val cost = (level + 1) * 150

                                Text(
                                    "$slot — Seviye $level",
                                    color = Color(0xFFFFD36A),
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "Bonus: +${level * 8} saldırı, +${level * 15} can",
                                    color = Color.LightGray
                                )

                                Button(
                                    onClick = {
                                        if (gold >= cost) {
                                            gold -= cost
                                            equipmentLevels["${hero.name}_$slot"] =
                                                level + 1
                                            prefs.edit()
                                                .putInt(
                                                    "${hero.name}_$slot",
                                                    level + 1
                                                )
                                                .apply()
                                            save()
                                            message = "$slot geliştirildi!"
                                        } else {
                                            message = "Yeterli altının yok!"
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Geliştir — $cost altın")
                                }

                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        GameCard("Toplam güç") {
                            Text("Saldırı: $totalAttack")
                            Text("Can: $totalHealth")
                        }
                    }

                    "Zindan" -> {
                        Text(
                            "KARANLIK ZİNDANLAR",
                            color = Color(0xFFE4BC68),
                            style = MaterialTheme.typography.titleLarge
                        )

                        GameCard("Gölge Zindanı") {
                            Text("Normal canavarlar ve altın ödülleri.")
                            Text("Mevcut bölüm: $stage")
                            Text("Ödül: ${100 + stage * 20} altın")
                        }

                        GameCard("Ejderha Zindanı") {
                            Text("Her 5 bölümde güçlü ejderha patronu.")
                            Text("Büyük ödül: ${500 + stage * 50} altın")
                        }

                        GameCard("İblis Kralı") {
                            Text("Her 10 bölümde efsanevi patron.")
                            Text("Çok güçlü saldırılar ve büyük ödüller.")
                        }

                        Button(
                            onClick = { tab = "Savaş" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ZİNDANA GİR")
                        }
                    }

                    "Savaş" -> {
                        Text(
                            "KARANLIK ZİNDAN",
                            color = Color(0xFFE4BC68),
                            style = MaterialTheme.typography.titleLarge
                        )

                        GameCard("Bölüm $stage — $enemyName") {
                            if (isBoss) {
                                Text(
                                    "PATRON SAVAŞI!",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Black
                                )
                            }

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
                            Text("Seçili: ${hero.name}")
                            Text("Takım canı: $playerHp / 500")

                            LinearProgressIndicator(
                                progress = {
                                    (playerHp.toFloat() / 500)
                                        .coerceIn(0f, 1f)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.Green
                            )

                            Text("Saldırı gücü: $totalAttack")
                            Text("Yetenek: ${hero.skill}")
                        }

                        GameCard("Savaş günlüğü") {
                            Text(message, color = Color.White)
                        }

                        Button(
                            onClick = { attack(false) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("NORMAL SALDIRI")
                        }

                        Button(
                            onClick = { attack(true) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = skillReady
                        ) {
                            Text(
                                if (skillReady) "ÖZEL YETENEK: ${hero.skill}"
                                else "ÖZEL YETENEK KULLANILDI"
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                skillReady = true
                                message = "Özel yetenek yeniden hazır!"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("YETENEĞİ YENİLE")
                        }

                        Text(
                            "Savaşacak kahramanı seç:",
                            color = Color.White
                        )

                        heroes.forEachIndexed { index, h ->
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedHero == index,
                                    onClick = {
                                        selectedHero = index
                                        message = "${h.name} seçildi!"
                                    }
                                )
                                Text(
                                    "${h.symbol} ${h.name}",
                                    color = Color.White
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                playerHp = 500
                                enemyHp = enemyMaxHp
                                skillReady = true
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
