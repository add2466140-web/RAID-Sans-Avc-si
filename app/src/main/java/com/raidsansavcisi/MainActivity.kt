package com.raidsansavcisi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import android.content.Context

data class Champion(val name: String, val rarity: String, val faction: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("raid_data", Context.MODE_PRIVATE)

        setContent {
            MaterialTheme {
                var tab by remember { mutableIntStateOf(0) }

                var ancient by remember { mutableIntStateOf(prefs.getInt("ancient", 0)) }
                var voidShard by remember { mutableIntStateOf(prefs.getInt("void", 0)) }
                var sacred by remember { mutableIntStateOf(prefs.getInt("sacred", 0)) }
                var primal by remember { mutableIntStateOf(prefs.getInt("primal", 0)) }
                var mercy by remember { mutableIntStateOf(prefs.getInt("mercy", 0)) }
                var probability by remember { mutableStateOf("1") }

                fun save(key: String, value: Int) {
                    prefs.edit().putInt(key, value).apply()
                }

                val champions = listOf(
                    Champion("Bad-el-Kazar", "Efsanevi", "Ölümsüzler"),
                    Champion("Arbiter", "Efsanevi", "Yüce Elfler"),
                    Champion("Ninja", "Efsanevi", "Shadowkin"),
                    Champion("Searsha", "Efsanevi", "Bilinmiyor"),
                    Champion("Rotos", "Efsanevi", "Undead Hordes"),
                    Champion("Krisk", "Efsanevi", "Lizardmen"),
                    Champion("Mithrala", "Efsanevi", "Dark Elves"),
                    Champion("Trunda", "Efsanevi", "Dwarves"),
                    Champion("Gnut", "Efsanevi", "Dwarves"),
                    Champion("Duchess Lilitu", "Efsanevi", "Demonspawn"),
                    Champion("Prince Kymar", "Efsanevi", "Demonspawn"),
                    Champion("Siphi", "Efsanevi", "Undead Hordes"),
                    Champion("Nekhret", "Efsanevi", "Undead Hordes"),
                    Champion("Turvold", "Efsanevi", "Barbarians"),
                    Champion("Mikage", "Mitik", "Shadowkin"),
                    Champion("Siegfrund", "Mitik", "Banner Lords"),
                    Champion("Frolni", "Mitik", "Dwarves")
                )

                var filter by remember { mutableStateOf("Tümü") }
                var search by remember { mutableStateOf("") }

                Scaffold(
                    topBar = {
                        Column {
                            Text(
                                "RAID ŞANS AVCISI",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(16.dp)
                            )
                            TabRow(selectedTabIndex = tab) {
                                listOf("Özet", "Mercy", "Kaynaklar", "Şampiyonlar")
                                    .forEachIndexed { index, title ->
                                        Tab(
                                            selected = tab == index,
                                            onClick = { tab = index },
                                            text = { Text(title) }
                                        )
                                    }
                            }
                        }
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        when (tab) {
                            0 -> {
                                Text("Hoş geldin!")
                                Spacer(Modifier.height(12.dp))
                                Text("Antik: $ancient")
                                Text("Hiçlik: $voidShard")
                                Text("Kutsal: $sacred")
                                Text("İlkel: $primal")
                                Spacer(Modifier.height(12.dp))
                                Text("Mercy sayacı: $mercy")
                                Text("Şampiyon koleksiyonunu ve kaynaklarını takip et.")
                            }

                            1 -> {
                                Text("MERCY TAKİBİ", style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(12.dp))
                                Text("Mercy sayacı: $mercy")
                                Row {
                                    Button(onClick = {
                                        mercy++
                                        save("mercy", mercy)
                                    }) { Text("+1") }
                                    Spacer(Modifier.width(8.dp))
                                    Button(onClick = {
                                        mercy = 0
                                        save("mercy", mercy)
                                    }) { Text("Sıfırla") }
                                }
                                Spacer(Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = probability,
                                    onValueChange = { probability = it },
                                    label = { Text("Temel ihtimal (%)") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal
                                    )
                                )
                                val chance = probability.toDoubleOrNull() ?: 0.0
                                val estimate = 100.0 - (100.0 - chance).coerceIn(0.0, 100.0)
                                Text("Girilen temel ihtimal: %${"%.2f".format(chance)}")
                                Text("Not: Bu sayaç tahmini takip içindir; gerçek oyun ihtimalini değiştirmez.")
                            }

                            2 -> {
                                Text("KAYNAKLAR", style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(12.dp))

                                @Composable
                                fun ResourceRow(
                                    title: String,
                                    value: Int,
                                    change: (Int) -> Unit
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("$title: $value")
                                        Row {
                                            TextButton(onClick = {
                                                if (value > 0) change(value - 1)
                                            }) { Text("-") }
                                            TextButton(onClick = {
                                                change(value + 1)
                                            }) { Text("+") }
                                        }
                                    }
                                }

                                ResourceRow("Antik", ancient) {
                                    ancient = it
                                    save("ancient", it)
                                }
                                ResourceRow("Hiçlik", voidShard) {
                                    voidShard = it
                                    save("void", it)
                                }
                                ResourceRow("Kutsal", sacred) {
                                    sacred = it
                                    save("sacred", it)
                                }
                                ResourceRow("İlkel", primal) {
                                    primal = it
                                    save("primal", it)
                                }
                            }

                            3 -> {
                                Text("ŞAMPİYONLAR", style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = search,
                                    onValueChange = { search = it },
                                    label = { Text("Şampiyon ara") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                                Row {
                                    listOf("Tümü", "Efsanevi", "Mitik").forEach {
                                        FilterChip(
                                            selected = filter == it,
                                            onClick = { filter = it },
                                            label = { Text(it) },
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                    }
                                }
                                val shown = champions.filter {
                                    (filter == "Tümü" || it.rarity == filter) &&
                                    it.name.contains(search, ignoreCase = true)
                                }
                                LazyColumn {
                                    items(shown) { champion ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Column(Modifier.padding(12.dp)) {
                                                Text(
                                                    champion.name,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Text("${champion.rarity} • ${champion.faction}")
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
    }
}
