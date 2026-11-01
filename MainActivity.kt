
package com.raidsansavcisi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Black = Color(0xFF101010)
private val Panel = Color(0xFF201D18)
private val Gold = Color(0xFFD6AE55)
private val Cream = Color(0xFFF4E8CC)
private val Muted = Color(0xFFB8AD98)
private val MythicColor = Color(0xFFE16B9A)

private data class Champion(
    val name: String,
    val rarity: String,
    val faction: String
)

private val champions = listOf(
    Champion("Bad-el-Kazar", "Efsanevi", "Karanlık Elfler"),
    Champion("Arbiter", "Efsanevi", "Yüce Elfler"),
    Champion("Ninja", "Efsanevi", "Shadowkin"),
    Champion("Searsha", "Efsanevi", "Bilinmiyor"),
    Champion("Mikage", "Mitik", "Yüce Elfler"),
    Champion("Siegfrund the Nephilim", "Mitik", "Kutsal Düzen"),
    Champion("Mezomel Luperfang", "Mitik", "Kertenkeleler"),
    Champion("Gizmak the Terrible", "Mitik", "Ogryn Kabileleri"),
    Champion("Aphidus the Hivelord", "Mitik", "Sylvan Watchers"),
    Champion("Lazarius the Lifeblood", "Mitik", "Sylvan Watchers"),
    Champion("Krixia the Huntress", "Mitik", "Karanlık Elfler")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RaidApp() }
    }
}

@Composable
fun RaidApp() {
    val prefs = androidx.compose.ui.platform.LocalContext.current
        .getSharedPreferences("raid_tracker", 0)

    var tab by remember { mutableIntStateOf(0) }
    var target by remember {
        mutableStateOf(prefs.getString("target", "") ?: "")
    }
    var shards by remember {
        mutableStateOf(prefs.getInt("shards", 0).toString())
    }
    var baseRate by remember {
        mutableStateOf(prefs.getString("base", "0.5") ?: "0.5")
    }
    var mercyStart by remember {
        mutableStateOf(prefs.getString("start", "200") ?: "200")
    }
    var increase by remember {
        mutableStateOf(prefs.getString("increase", "0.025") ?: "0.025")
    }
    var ancient by remember {
        mutableStateOf(prefs.getInt("ancient", 0).toString())
    }
    var voids by remember {
        mutableStateOf(prefs.getInt("voids", 0).toString())
    }
    var sacred by remember {
        mutableStateOf(prefs.getInt("sacred", 0).toString())
    }
    var primal by remember {
        mutableStateOf(prefs.getInt("primal", 0).toString())
    }

    var search by remember { mutableStateOf("") }
    var rarity by remember { mutableStateOf("Tümü") }

    val count = shards.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val base = baseRate.replace(",", ".")
        .toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.5
    val start = mercyStart.toIntOrNull()?.coerceAtLeast(0) ?: 200
    val step = increase.replace(",", ".")
        .toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.025
    val next = (base + (count + 1 - start).coerceAtLeast(0) * step)
        .coerceIn(0.0, 100.0)

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Gold,
            onPrimary = Black,
            background = Black,
            surface = Panel,
            onSurface = Cream,
            onBackground = Cream,
            secondary = Gold
        )
    ) {
        Column(
            Modifier.fillMaxSize().background(Black)
        ) {
            Column(
                Modifier.fillMaxWidth().background(Panel).padding(18.dp)
            ) {
                Text(
                    "RAID",
                    color = Gold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Text(
                    "ŞANS AVCISI",
                    color = Cream,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Çağırmalarını planla • Şampiyonlarını keşfet",
                    color = Muted,
                    fontSize = 12.sp
                )
            }

            NavigationBar(containerColor = Panel) {
                listOf("Özet", "Mercy", "Kaynaklar", "Şampiyonlar")
                    .forEachIndexed { i, label ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = {
                                Text(listOf("◆", "✦", "▣", "♛")[i])
                            },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Black,
                                indicatorColor = Gold,
                                selectedTextColor = Gold,
                                unselectedTextColor = Muted
                            )
                        )
                    }
            }

            Column(
                Modifier.weight(1f)
                    .verticalScroll(
                        androidx.compose.foundation.rememberScrollState()
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (tab) {
                    0 -> {
                        PanelCard("HEDEF ŞAMPİYON") {
                            OutlinedTextField(
                                value = target,
                                onValueChange = {
                                    target = it
                                    prefs.edit().putString("target", it).apply()
                                },
                                label = { Text("Örn. Bad-el-Kazar") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Text(
                                "Hedef şampiyonunu burada takip et.",
                                color = Muted
                            )
                        }

                        PanelCard("ÇAĞIRMA SAYACI") {
                            NumberInput(shards, {
                                shards = it
                                prefs.edit().putInt(
                                    "shards",
                                    it.toIntOrNull() ?: 0
                                ).apply()
                            }, "Efsanevi çıkmadan açılan parça")

                            Text(
                                "Kayıtlı sayaç: $count",
                                color = Gold,
                                fontWeight = FontWeight.Bold
                            )

                            Button(
                                onClick = {
                                    shards = "0"
                                    prefs.edit().putInt("shards", 0).apply()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Gold,
                                    contentColor = Black
                                )
                            ) {
                                Text("Sayacı sıfırla")
                            }
                        }

                        PanelCard("BİLGİ") {
                            Text(
                                "Uygulama oyun içi şansı değiştirmez. " +
                                    "Sadece girdiğin oranlarla teorik hesap yapar.",
                                color = Muted
                            )
                        }
                    }

                    1 -> {
                        PanelCard("MERCY / OLASILIK HESABI") {
                            Text(
                                "Parça türü ve etkinlik oranına göre değerleri kendin gir.",
                                color = Muted
                            )

                            NumberInput(baseRate, {
                                baseRate = it
                                prefs.edit().putString("base", it).apply()
                            }, "Başlangıç oranı (%)")

                            NumberInput(mercyStart, {
                                mercyStart = it
                                prefs.edit().putString("start", it).apply()
                            }, "Mercy başlangıç sayısı")

                            NumberInput(increase, {
                                increase = it
                                prefs.edit().putString("increase", it).apply()
                            }, "Her sonraki parçada artış (yüzde puan)")

                            NumberInput(shards, {
                                shards = it
                                prefs.edit().putInt(
                                    "shards",
                                    it.toIntOrNull() ?: 0
                                ).apply()
                            }, "Mercy sayacındaki parça")

                            Text(
                                "Sıradaki çağırma için ayarlanmış oran",
                                color = Muted
                            )
                            Text(
                                String.format("%.3f%%", next),
                                color = Gold,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Bu sonuç yalnızca girdiğin parametrelere dayanır. " +
                                    "Güncel Mercy kuralını resmî oyun açıklamasından doğrula.",
                                color = Muted
                            )
                        }
                    }

                    2 -> {
                        PanelCard("PARÇA ENVANTERİ") {
                            NumberInput(ancient, {
                                ancient = it
                                prefs.edit().putInt(
                                    "ancient",
                                    it.toIntOrNull() ?: 0
                                ).apply()
                            }, "Antik parça")

                            NumberInput(voids, {
                                voids = it
                                prefs.edit().putInt(
                                    "voids",
                                    it.toIntOrNull() ?: 0
                                ).apply()
                            }, "Hiçlik parçası")

                            NumberInput(sacred, {
                                sacred = it
                                prefs.edit().putInt(
                                    "sacred",
                                    it.toIntOrNull() ?: 0
                                ).apply()
                            }, "Kutsal parça")

                            NumberInput(primal, {
                                primal = it
                                prefs.edit().putInt(
                                    "primal",
                                    it.toIntOrNull() ?: 0
                                ).apply()
                            }, "Primal parça")

                            Text("Kayıtlar bu telefonda saklanır.", color = Muted)
                        }

                        PanelCard("ETKİNLİK REHBERİ") {
                            Text(
                                "2x: İlgili nadirlikte şampiyon çıkma oranını artırır.",
                                color = Cream
                            )
                            Text(
                                "10x / Progressive Chance: Hedef şampiyonun seçilme ihtimalini etkiler.",
                                color = Cream
                            )
                            Text(
                                "Garanti etkinliklerinde gereken parça sayısını oyun içi duyurudan kontrol et.",
                                color = Muted
                            )
                        }
                    }

                    3 -> {
                        Text(
                            "ŞAMPİYON KOLEKSİYONU",
                            color = Gold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = search,
                            onValueChange = { search = it },
                            label = { Text("Şampiyon ara") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Tümü", "Efsanevi", "Mitik")
                                .forEach { option ->
                                    FilterChip(
                                        selected = rarity == option,
                                        onClick = { rarity = option },
                                        label = { Text(option, fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Gold,
                                            selectedLabelColor = Black
                                        )
                                    )
                                }
                        }

                        val filtered = champions.filter {
                            (rarity == "Tümü" || it.rarity == rarity) &&
                                it.name.contains(search, ignoreCase = true)
                        }

                        Text(
                            "${filtered.size} şampiyon bulundu",
                            color = Muted
                        )

                        filtered.forEach { champion ->
                            ChampionCard(champion)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChampionCard(champion: Champion) {
    val rarityColor = when (champion.rarity) {
        "Efsanevi" -> Gold
        else -> MythicColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Panel),
        border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(58.dp)
                    .background(rarityColor.copy(alpha = 0.18f), CircleShape),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    champion.name.take(1),
                    color = rarityColor,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    champion.name,
                    color = Cream,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(champion.rarity, color = rarityColor)
                Text(champion.faction, color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PanelCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Gold.copy(alpha = .55f))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                title,
                color = Gold,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            content()
        }
    }
}

@Composable
private fun NumberInput(
    value: String,
    onValue: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            onValue(it.filter { c ->
                c.isDigit() || c == '.' || c == ','
            })
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
