
package com.raidsansavcisi

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

data class Champion(
    val name: String,
    val rarity: String,
    val faction: String
)

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

                var ancientMercy by remember { mutableIntStateOf(prefs.getInt("mercy_ancient", prefs.getInt("mercy", 0))) }
                var voidMercy by remember { mutableIntStateOf(prefs.getInt("mercy_void", 0)) }
                var sacredMercy by remember { mutableIntStateOf(prefs.getInt("mercy_sacred", 0)) }
                var primalMercy by remember { mutableIntStateOf(prefs.getInt("mercy_primal", 0)) }

                var search by remember { mutableStateOf("") }
                var filter by remember { mutableStateOf("Tümü") }
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

                var dungeon by remember {
                    mutableStateOf(prefs.getString("dungeon_name", "Ejderha") ?: "Ejderha")
                }
                var dungeonStage by remember {
                    mutableStateOf(prefs.getString("dungeon_stage", "1") ?: "1")
                }
                var farmGoal by remember {
                    mutableStateOf(prefs.getString("farm_goal", "") ?: "")
                }
                var farmNotes by remember {
                    mutableStateOf(prefs.getString("farm_notes", "") ?: "")
                }

                var probability by remember { mutableStateOf("1") }

                fun saveInt(key: String, value: Int) {
                    prefs.edit().putInt(key, value).apply()
                }

                fun saveText(key: String, value: String) {
                    prefs.edit().putString(key, value).apply()
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

                fun openChampion(champion: Champion) {
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

                            ScrollableTabRow(selectedTabIndex = tab) {
                                listOf(
                                    "Özet", "Mercy", "Kaynaklar",
                                    "Şampiyonlar", "Zindan / Farm"
                                ).forEachIndexed { index, title ->
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
                                Text("GENEL ÖZET", style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(12.dp))
                                Text("Antik parça: $ancient")
                                Text("Hiçlik parçası: $voidShard")
                                Text("Kutsal parça: $sacred")
                                Text("İlkel parça: $primal")
                                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                Text("Antik Mercy: $ancientMercy")
                                Text("Hiçlik Mercy: $voidMercy")
                                Text("Kutsal Mercy: $sacredMercy")
                                Text("İlkel Mercy: $primalMercy")
                                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                Text("Sahip olduğun şampiyon: ${owned.size}")
                                Text("Kayıtlı şampiyon listesi: ${champions.size}")
                                Text("Son farm hedefi: $farmGoal")
                            }

                            1 -> {
                                Text("MERCY TAKİBİ", style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(8.dp))
                                Text("Her parça türünün sayacını ayrı takip et.")

                                MercyRow("Antik", ancientMercy) {
                                    ancientMercy = it
                                    saveInt("mercy_ancient", it)
                                    saveInt("mercy", it)
                                }
                                MercyRow("Hiçlik", voidMercy) {
                                    voidMercy = it
                                    saveInt("mercy_void", it)
                                }
                                MercyRow("Kutsal", sacredMercy) {
                                    sacredMercy = it
                                    saveInt("mercy_sacred", it)
                                }
                                MercyRow("İlkel", primalMercy) {
                                    primalMercy = it
                                    saveInt("mercy_primal", it)
                                }

                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = probability,
                                    onValueChange = { probability = it },
                                    label = { Text("Temel ihtimal (%)") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text("Girdiğin temel ihtimal: %$probability")
                                Text(
                                    "Bu alan bilgilendirme amaçlıdır. Oyun içi gerçek çağırma ihtimali veya garanti vermez."
                                )
                            }

                            2 -> {
                                Text("KAYNAKLAR", style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(8.dp))

                                ResourceRow("Antik", ancient) {
                                    ancient = it
                                    saveInt("ancient", it)
                                }
                                ResourceRow("Hiçlik", voidShard) {
                                    voidShard = it
                                    saveInt("void", it)
                                }
                                ResourceRow("Kutsal", sacred) {
                                    sacred = it
                                    saveInt("sacred", it)
                                }
                                ResourceRow("İlkel", primal) {
                                    primal = it
                                    saveInt("primal", it)
                                }
                            }

                            3 -> {
                                Text(
                                    "ŞAMPİYON KOLEKSİYONUM",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text("Sahip olduğun: ${owned.size}")

                                OutlinedTextField(
                                    value = search,
                                    onValueChange = { search = it },
                                    label = { Text("Şampiyon ara") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(modifier = Modifier.horizontalScrollCompat()) {
                                    listOf("Tümü", "Efsanevi", "Mitik").forEach {
                                        FilterChip(
                                            selected = filter == it,
                                            onClick = { filter = it },
                                            label = { Text(it) },
                                            modifier = Modifier.padding(end = 6.dp)
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
                                                .clickable { openChampion(champion) }
                                        ) {
                                            Column(Modifier.padding(12.dp)) {
                                                Text(
                                                    champion.name,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Text("${champion.rarity} • ${champion.faction}")
                                                Text("Detayları açmak için karta dokun")

                                                Row {
                                                    Button(onClick = {
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
                                                    }) {
                                                        Text(
                                                            if (owned.contains(champion.name))
                                                                "✓ Sahibim"
                                                            else
                                                                "+ Sahibim"
                                                        )
                                                    }

                                                    Spacer(Modifier.width(8.dp))
                                                    TextButton(onClick = {
                                                        openChampion(champion)
                                                    }) {
                                                        Text("Detay")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            4 -> {
                                Text("ZİNDAN VE FARM TAKİBİ", style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(8.dp))

                                Text("Zindan seç")
                                listOf(
                                    "Ejderha", "Ateş Şövalyesi", "Örümcek",
                                    "Buz Golemi", "Minotor", "Golem Kulesi"
                                ).forEach { name ->
                                    FilterChip(
                                        selected = dungeon == name,
                                        onClick = {
                                            dungeon = name
                                            saveText("dungeon_name", name)
                                        },
                                        label = { Text(name) },
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }

                                OutlinedTextField(
                                    value = dungeonStage,
                                    onValueChange = {
                                        dungeonStage = it
                                        saveText("dungeon_stage", it)
                                    },
                                    label = { Text("Ulaştığın aşama") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = farmGoal,
                                    onValueChange = {
                                        farmGoal = it
                                        saveText("farm_goal", it)
                                    },
                                    label = { Text("Farm hedefin") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = farmNotes,
                                    onValueChange = {
                                        farmNotes = it
                                        saveText("farm_notes", it)
                                    },
                                    label = { Text("Farm notları") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 100.dp)
                                )

                                Spacer(Modifier.height(8.dp))
                                Text("Seçili zindan: $dungeon")
                                Text("Aşama: $dungeonStage")
                                Text("Hedef: $farmGoal")
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
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text("${champion.rarity} • ${champion.faction}")
                                Spacer(Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = level,
                                    onValueChange = { level = it },
                                    label = { Text("Seviye (1-60)") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = stars,
                                    onValueChange = { stars = it },
                                    label = { Text("Yıldız (1-6)") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = ascension,
                                    onValueChange = { ascension = it },
                                    label = { Text("Terfi (0-6)") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    modifier = Modifier.fillMaxWidth()
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

@Composable
fun ResourceRow(
    title: String,
    value: Int,
    change: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
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

@Composable
fun MercyRow(
    title: String,
    value: Int,
    change: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text("Mercy sayacı: $value")
            }
            Row {
                TextButton(onClick = {
                    if (value > 0) change(value - 1)
                }) { Text("-1") }

                TextButton(onClick = {
                    change(value + 1)
                }) { Text("+1") }

                TextButton(onClick = {
                    change(0)
                }) { Text("Sıfırla") }
            }
        }
    }
}

fun Modifier.horizontalScrollCompat(): Modifier = this
