package com.raidsansavcisi

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType

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
                var filter by remember { mutableStateOf("Tümü") }
                var search by remember { mutableStateOf("") }
                var ownedOnly by remember { mutableStateOf(false) }

                var owned by remember {
                    mutableStateOf(
                        prefs.getStringSet("owned_champions", emptySet())
                            ?.toSet() ?: emptySet()
                    )
                }

                var selectedChampion by remember { mutableStateOf<Champion?>(null) }
                var level by remember { mutableStateOf("1") }
                var stars by remember { mutableStateOf("1") }
                var ascension by remember { mutableStateOf("0") }
                var skills by remember { mutableStateOf("") }
                var equipment by remember { mutableStateOf("") }

                fun save(key: String, value: Int) {
                    prefs.edit().putInt(key, value).apply()
                }

                val champions = listOf(
                    Champion("Bad-el-Kazar", "Efsanevi", "Undead Hordes"),
                    Champion("Arbiter", "Efsanevi", "High Elves"),
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

                fun loadDetails(champion: Champion) {
                    selectedChampion = champion
                    level = prefs.getString("${champion.name}_level", "1") ?: "1"
                    stars = prefs.getString("${champion.name}_stars", "1") ?: "1"
                    ascension = prefs.getString("${champion.name}_ascension", "0") ?: "0"
                    skills = prefs.getString("${champion.name}_skills", "") ?: ""
                    equipment = prefs.getString("${champion.name}_equipment", "") ?: ""
                }

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
                                Text("Mercy sayacı: $mercy")
                                Text("Sahip olduğun şampiyon: ${owned.size}")
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

                                Text("Girilen temel ihtimal: %$probability")
                                Text("Bu değer yalnızca tahmini takip içindir.")
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
                                Text(
                                    "ŞAMPİYON KOLEKSİYONUM",
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Text("Sahip olduğun: ${owned.size}")

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

                                FilterChip(
                                    selected = ownedOnly,
                                    onClick = { ownedOnly = !ownedOnly },
                                    label = { Text("Sadece sahip olduklarım") }
                                )

                                val shown = champions.filter {
                                    (filter == "Tümü" || it.rarity == filter) &&
                                    it.name.contains(search, ignoreCase = true) &&
                                    (!ownedOnly || owned.contains(it.name))
                                }

                                LazyColumn {
                                    items(shown) { champion ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clickable {
                                                    loadDetails(champion)
                                                }
                                        ) {
                                            Column(Modifier.padding(12.dp)) {
                                                Text(
                                                    champion.name,
                                                    style = MaterialTheme.typography.titleMedium
                                                )

                                                Text("${champion.rarity} • ${champion.faction}")
                                                Text("Detayları açmak için dokun")

                                                Spacer(Modifier.height(8.dp))

                                                Button(
                                                    onClick = {
                                                        val updated = owned.toMutableSet()

                                                        if (updated.contains(champion.name)) {
                                                            updated.remove(champion.name)
                                                        } else {
                                                            updated.add(champion.name)
                                                        }

                                                        owned = updated.toSet()

                                                        prefs.edit()
                                                            .putStringSet("owned_champions", owned)
                                                            .apply()
                                                    }
                                                ) {
                                                    Text(
                                                        if (owned.contains(champion.name))
                                                            "✓ Sahibim"
                                                        else
                                                            "+ Sahibim"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                selectedChampion?.let { champion ->
                    AlertDialog(
                        onDismissRequest = { selectedChampion = null },
                        title = { Text(champion.name) },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 480.dp)
                            ) {
                                Text("${champion.rarity} • ${champion.faction}")

                                Spacer(Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = level,
                                    onValueChange = { level = it },
                                    label = { Text("Seviye (1-60)") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    )
                                )

                                OutlinedTextField(
                                    value = stars,
                                    onValueChange = { stars = it },
                                    label = { Text("Yıldız (1-6)") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    )
                                )

                                OutlinedTextField(
                                    value = ascension,
                                    onValueChange = { ascension = it },
                                    label = { Text("Terfi (0-6)") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    )
                                )

                                OutlinedTextField(
                                    value = skills,
                                    onValueChange = { skills = it },
                                    label = { Text("Beceri notları") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = equipment,
                                    onValueChange = { equipment = it },
                                    label = { Text("Ekipman ve aksesuar notları") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                prefs.edit()
                                    .putString("${champion.name}_level", level)
                                    .putString("${champion.name}_stars", stars)
                                    .putString("${champion.name}_ascension", ascension)
                                    .putString("${champion.name}_skills", skills)
                                    .putString("${champion.name}_equipment", equipment)
                                    .apply()

                                selectedChampion = null
                            }) {
                                Text("Kaydet")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                selectedChampion = null
                            }) {
                                Text("Kapat")
                            }
                        }
                    )
                }
            }
        }
    }
}
