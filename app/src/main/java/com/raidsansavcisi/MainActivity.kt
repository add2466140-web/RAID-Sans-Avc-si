
package com.raidsansavcisi

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val CATALOG_URL =
    "https://raw.githubusercontent.com/PatPat1567/RaidShadowLegendsData/master/champions-base-info.json"

data class Champion(
    val name: String,
    val rarity: String = "Bilinmiyor",
    val affinity: String = "Bilinmiyor",
    val faction: String = "Bilinmiyor",
    val role: String = "Destek",
    val avatarUrl: String = "",
    val detailsUrl: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RaidApp() }
    }
}

private fun fallbackChampions() = listOf(
    "Bad-el-Kazar", "Arbiter", "Ninja", "Searsha", "Rotos",
    "Krisk", "Mithrala", "Trunda", "Gnut", "Duchess Lilitu",
    "Prince Kymar", "Siphi", "Nekhret", "Turvold", "Mikage",
    "Siegfrund", "Frolni", "Drexthar Bloodtwin", "Ma'Shalled",
    "Tomb Lord", "Ithos", "Elenaril", "Nethril", "Skartorsis",
    "Raglin", "Stag Knight", "Geomancer", "Seer",
    "Deacon Armstrong", "Maneater", "Pain Keeper", "Uugo",
    "Alure", "Coldheart", "Apothecary", "Bellower"
).map { Champion(it) }

private fun translateRarity(value: String): String = when (value.lowercase()) {
    "legendary" -> "Efsanevi"
    "mythical" -> "Mitik"
    "epic" -> "Epik"
    "rare" -> "Nadir"
    "uncommon" -> "Sıradışı"
    "common" -> "Yaygın"
    else -> value.ifBlank { "Bilinmiyor" }
}

private fun translateRole(value: String): String = when (value.lowercase()) {
    "attack" -> "Hasar"
    "defense" -> "Savunma"
    "support" -> "Destek"
    "hp" -> "Can"
    else -> value.ifBlank { "Destek" }
}

private fun parseCatalog(json: String): List<Champion> {
    val root = JSONObject(json)
    val result = mutableListOf<Champion>()
    val keys = root.keys()

    while (keys.hasNext()) {
        val name = keys.next()
        val item = root.optJSONObject(name) ?: continue

        result.add(
            Champion(
                name = name,
                rarity = translateRarity(item.optString("rarity")),
                affinity = item.optString("affinity", "Bilinmiyor"),
                faction = item.optString("faction", "Bilinmiyor"),
                role = translateRole(item.optString("role")),
                avatarUrl = item.optString("avatarUrl"),
                detailsUrl = item.optString("detailsUrl")
            )
        )
    }
    return result.sortedBy { it.name }
}

private suspend fun downloadCatalog(): String? = withContext(Dispatchers.IO) {
    try {
        val connection = URL(CATALOG_URL).openConnection() as HttpURLConnection
        connection.connectTimeout = 12000
        connection.readTimeout = 12000
        connection.requestMethod = "GET"
        connection.connect()

        val result = connection.inputStream.bufferedReader().use { it.readText() }
        connection.disconnect()
        result.takeIf { it.isNotBlank() }
    } catch (_: Exception) {
        null
    }
}

@Composable
fun RaidApp() {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("raid_data", Context.MODE_PRIVATE)
    }

    var tab by remember { mutableStateOf("Özet") }
    var champions by remember { mutableStateOf(fallbackChampions()) }
    var loading by remember { mutableStateOf(true) }
    var catalogMessage by remember { mutableStateOf("Şampiyon kataloğu hazırlanıyor") }

    var ancient by remember { mutableIntStateOf(prefs.getInt("ancient", 0)) }
    var voidShard by remember { mutableIntStateOf(prefs.getInt("void", 0)) }
    var sacred by remember { mutableIntStateOf(prefs.getInt("sacred", 0)) }
    var primal by remember { mutableIntStateOf(prefs.getInt("primal", 0)) }

    var mercyAncient by remember {
        mutableIntStateOf(prefs.getInt("mercy_ancient", prefs.getInt("mercy", 0)))
    }
    var mercyVoid by remember { mutableIntStateOf(prefs.getInt("mercy_void", 0)) }
    var mercySacred by remember { mutableIntStateOf(prefs.getInt("mercy_sacred", 0)) }
    var mercyPrimal by remember { mutableIntStateOf(prefs.getInt("mercy_primal", 0)) }

    var owned by remember {
        mutableStateOf(prefs.getStringSet("owned_champions", emptySet())?.toSet() ?: emptySet())
    }

    var search by remember { mutableStateOf("") }
    var rarityFilter by remember { mutableStateOf("Tümü") }
    var affinityFilter by remember { mutableStateOf("Tümü") }
    var selectedChampion by remember { mutableStateOf<Champion?>(null) }

    var dungeonName by remember { mutableStateOf(prefs.getString("dungeon_name", "Ejderha") ?: "Ejderha") }
    var dungeonStage by remember { mutableStateOf(prefs.getInt("dungeon_stage", 13).toString()) }
    var farmGoal by remember { mutableStateOf(prefs.getString("farm_goal", "") ?: "") }
    var farmNotes by remember { mutableStateOf(prefs.getString("farm_notes", "") ?: "") }

    var teamMode by remember { mutableStateOf("Hydra") }
    var savedTeams by remember {
        mutableStateOf(
            try {
                val array = JSONArray(prefs.getString("saved_teams", "[]"))
                (0 until array.length()).map { array.getString(it) }
            } catch (_: Exception) {
                emptyList()
            }
        )
    }
    var teamSelection by remember { mutableStateOf(emptyList<String>()) }

    fun saveNumber(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun saveOwned() {
        prefs.edit().putStringSet("owned_champions", owned.toSet()).apply()
    }

    fun saveTeams() {
        val array = JSONArray()
        savedTeams.forEach { array.put(it) }
        prefs.edit().putString("saved_teams", array.toString()).apply()
    }

    LaunchedEffect(Unit) {
        val cached = prefs.getString("champion_catalog_json", null)
        if (cached != null) {
            try {
                val parsed = parseCatalog(cached)
                if (parsed.isNotEmpty()) champions = parsed
            } catch (_: Exception) { }
        }

        val downloaded = downloadCatalog()
        if (downloaded != null) {
            try {
                val parsed = parseCatalog(downloaded)
                if (parsed.isNotEmpty()) {
                    champions = parsed
                    prefs.edit().putString("champion_catalog_json", downloaded).apply()
                    catalogMessage = "Şampiyon kataloğu güncellendi"
                } else {
                    catalogMessage = "İndirilen katalog boş. Kayıtlı liste kullanılıyor."
                }
            } catch (_: Exception) {
                catalogMessage = "Katalog okunamadı. Kayıtlı liste kullanılıyor."
            }
        } else {
            catalogMessage = "İnternet yok. Kayıtlı katalog kullanılıyor."
        }
        loading = false
    }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val data = JSONObject()
                data.put("ancient", ancient)
                data.put("void", voidShard)
                data.put("sacred", sacred)
                data.put("primal", primal)
                data.put("mercy_ancient", mercyAncient)
                data.put("mercy_void", mercyVoid)
                data.put("mercy_sacred", mercySacred)
                data.put("mercy_primal", mercyPrimal)
                data.put("owned_champions", JSONArray(owned.toList()))
                data.put("saved_teams", JSONArray(savedTeams))
                data.put("dungeon_name", dungeonName)
                data.put("dungeon_stage", dungeonStage)
                data.put("farm_goal", farmGoal)
                data.put("farm_notes", farmNotes)

                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(data.toString(2).toByteArray())
                }
            } catch (_: Exception) { }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val text = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()?.use { it.readText() } ?: ""
                val data = JSONObject(text)

                ancient = data.optInt("ancient", ancient)
                voidShard = data.optInt("void", voidShard)
                sacred = data.optInt("sacred", sacred)
                primal = data.optInt("primal", primal)
                mercyAncient = data.optInt("mercy_ancient", mercyAncient)
                mercyVoid = data.optInt("mercy_void", mercyVoid)
                mercySacred = data.optInt("mercy_sacred", mercySacred)
                mercyPrimal = data.optInt("mercy_primal", mercyPrimal)

                val ownedArray = data.optJSONArray("owned_champions") ?: JSONArray()
                owned = (0 until ownedArray.length()).map { ownedArray.getString(it) }.toSet()

                val teams = data.optJSONArray("saved_teams") ?: JSONArray()
                savedTeams = (0 until teams.length()).map { teams.getString(it) }

                dungeonName = data.optString("dungeon_name", dungeonName)
                dungeonStage = data.optString("dungeon_stage", dungeonStage)
                farmGoal = data.optString("farm_goal", farmGoal)
                farmNotes = data.optString("farm_notes", farmNotes)

                prefs.edit()
                    .putInt("ancient", ancient)
                    .putInt("void", voidShard)
                    .putInt("sacred", sacred)
                    .putInt("primal", primal)
                    .putInt("mercy_ancient", mercyAncient)
                    .putInt("mercy_void", mercyVoid)
                    .putInt("mercy_sacred", mercySacred)
                    .putInt("mercy_primal", mercyPrimal)
                    .putStringSet("owned_champions", owned)
                    .putString("dungeon_name", dungeonName)
                    .putInt("dungeon_stage", dungeonStage.toIntOrNull() ?: 13)
                    .putString("farm_goal", farmGoal)
                    .putString("farm_notes", farmNotes)
                    .apply()
                saveTeams()
            } catch (_: Exception) { }
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("RAID Şans Avcısı", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(catalogMessage, style = MaterialTheme.typography.bodySmall)
                }
            },
            bottomBar = {
                NavigationBar {
                    listOf("Özet", "Mercy", "Kaynaklar", "Şampiyonlar", "Takım", "Zindan", "Yedek").forEach {
                        NavigationBarItem(
                            selected = tab == it,
                            onClick = { tab = it },
                            icon = { Text(it.take(1)) },
                            label = { Text(it) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 14.dp)
            ) {
                when (tab) {
                    "Özet" -> {
                        Text("Hesap özeti", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(12.dp))
                        SummaryCard("Kadim Parça", ancient)
                        SummaryCard("Hiçlik Parçası", voidShard)
                        SummaryCard("Kutsal Parça", sacred)
                        SummaryCard("İlkel Parça", primal)
                        Spacer(Modifier.height(12.dp))
                        Text("Koleksiyon: ${owned.size} şampiyon")
                        Text("Katalog: ${champions.size} şampiyon")
                        if (loading) CircularProgressIndicator()
                    }

                    "Kaynaklar" -> {
                        Text("Parça sayıları", style = MaterialTheme.typography.titleLarge)
                        NumberEditor("Kadim", ancient) {
                            ancient = it.coerceAtLeast(0); saveNumber("ancient", ancient)
                        }
                        NumberEditor("Hiçlik", voidShard) {
                            voidShard = it.coerceAtLeast(0); saveNumber("void", voidShard)
                        }
                        NumberEditor("Kutsal", sacred) {
                            sacred = it.coerceAtLeast(0); saveNumber("sacred", sacred)
                        }
                        NumberEditor("İlkel", primal) {
                            primal = it.coerceAtLeast(0); saveNumber("primal", primal)
                        }
                    }

                    "Mercy" -> {
                        Text("Mercy takibi", style = MaterialTheme.typography.titleLarge)
                        Text("Sayaçlarını kendi oyun hesabındaki değerlere göre güncelle.")
                        NumberEditor("Kadim Mercy", mercyAncient) {
                            mercyAncient = it.coerceAtLeast(0)
                            saveNumber("mercy_ancient", mercyAncient)
                        }
                        NumberEditor("Hiçlik Mercy", mercyVoid) {
                            mercyVoid = it.coerceAtLeast(0)
                            saveNumber("mercy_void", mercyVoid)
                        }
                        NumberEditor("Kutsal Mercy", mercySacred) {
                            mercySacred = it.coerceAtLeast(0)
                            saveNumber("mercy_sacred", mercySacred)
                        }
                        NumberEditor("İlkel Mercy", mercyPrimal) {
                            mercyPrimal = it.coerceAtLeast(0)
                            saveNumber("mercy_primal", mercyPrimal)
                        }
                    }

                    "Şampiyonlar" -> {
                        Text("Şampiyon kataloğu", style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(
                            value = search,
                            onValueChange = { search = it },
                            label = { Text("Şampiyon ara") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilterMenu("Nadirlik", rarityFilter,
                                listOf("Tümü", "Efsanevi", "Mitik", "Epik", "Nadir", "Sıradışı", "Yaygın")
                            ) { rarityFilter = it }
                            Spacer(Modifier.width(8.dp))
                            FilterMenu("Element", affinityFilter,
                                listOf("Tümü", "Magic", "Force", "Spirit", "Void")
                            ) { affinityFilter = it }
                        }

                        val filtered = champions.filter {
                            it.name.contains(search, true) &&
                            (rarityFilter == "Tümü" || it.rarity == rarityFilter) &&
                            (affinityFilter == "Tümü" || it.affinity.equals(affinityFilter, true))
                        }

                        Text("${filtered.size} şampiyon bulundu")
                        LazyColumn {
                            items(filtered, key = { it.name }) { champion ->
                                ChampionRow(
                                    champion = champion,
                                    isOwned = owned.contains(champion.name),
                                    onClick = { selectedChampion = champion },
                                    onToggle = {
                                        owned = if (owned.contains(champion.name))
                                            owned - champion.name else owned + champion.name
                                        saveOwned()
                                    }
                                )
                            }
                        }
                    }

                    "Takım" -> {
                        Text("Takım oluşturucu", style = MaterialTheme.typography.titleLarge)
                        FilterMenu(
                            "İçerik",
                            teamMode,
                            listOf("Hydra", "Arena Saldırı", "Arena Savunma", "Zindan", "Boss")
                        ) { teamMode = it }

                        val recommended = champions.filter { owned.contains(it.name) }
                            .sortedBy {
                                when (teamMode) {
                                    "Hydra" -> when (it.role) {
                                        "Destek" -> 0
                                        "Savunma" -> 1
                                        "Hasar" -> 2
                                        else -> 3
                                    }
                                    "Arena Saldırı" -> when (it.role) {
                                        "Hasar" -> 0
                                        "Destek" -> 1
                                        else -> 2
                                    }
                                    else -> when (it.role) {
                                        "Destek" -> 0
                                        "Hasar" -> 1
                                        else -> 2
                                    }
                                }
                            }.take(5)

                        Text("Önerilen takım")
                        recommended.forEach { Text("• ${it.name} — ${it.role}") }

                        Spacer(Modifier.height(8.dp))
                        Text("Takıma şampiyon ekle")
                        LazyColumn(Modifier.weight(1f)) {
                            items(champions.filter { owned.contains(it.name) }) { champ ->
                                Row(
                                    Modifier.fillMaxWidth().clickable {
                                        teamSelection = if (teamSelection.contains(champ.name))
                                            teamSelection - champ.name else teamSelection + champ.name
                                    }.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = teamSelection.contains(champ.name),
                                        onCheckedChange = {
                                            teamSelection = if (it) teamSelection + champ.name
                                            else teamSelection - champ.name
                                        }
                                    )
                                    Text(champ.name)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (teamSelection.isNotEmpty()) {
                                    savedTeams = savedTeams + teamSelection.joinToString(", ")
                                    saveTeams()
                                    teamSelection = emptyList()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Seçili takımı kaydet") }

                        Text("Kayıtlı takımlar")
                        savedTeams.forEachIndexed { index, team ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${index + 1}. $team", Modifier.weight(1f))
                                TextButton(onClick = {
                                    savedTeams = savedTeams.filterIndexed { i, _ -> i != index }
                                    saveTeams()
                                }) { Text("Sil") }
                            }
                        }
                    }

                    "Zindan" -> {
                        Text("Zindan ve farm takibi", style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(
                            value = dungeonName,
                            onValueChange = {
                                dungeonName = it
                                prefs.edit().putString("dungeon_name", it).apply()
                            },
                            label = { Text("Zindan adı") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = dungeonStage,
                            onValueChange = {
                                dungeonStage = it
                                prefs.edit().putInt("dungeon_stage", it.toIntOrNull() ?: 0).apply()
                            },
                            label = { Text("Ulaştığın aşama") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = farmGoal,
                            onValueChange = {
                                farmGoal = it
                                prefs.edit().putString("farm_goal", it).apply()
                            },
                            label = { Text("Farm hedefin") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = farmNotes,
                            onValueChange = {
                                farmNotes = it
                                prefs.edit().putString("farm_notes", it).apply()
                            },
                            label = { Text("Farm notları") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4
                        )
                    }

                    "Yedek" -> {
                        Text("Veri yedekleme", style = MaterialTheme.typography.titleLarge)
                        Text("Kaynaklarını, Mercy sayaçlarını, koleksiyonunu, takımlarını ve farm notlarını JSON dosyasına kaydedebilirsin.")
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { backupLauncher.launch("raid-sans-avcisi-yedek.json") },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Yedek oluştur") }
                        Button(
                            onClick = { restoreLauncher.launch(arrayOf("application/json", "text/*")) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Yedekten geri yükle") }
                    }
                }
            }
        }

        selectedChampion?.let { champ ->
            ChampionDetailDialog(
                champion = champ,
                prefs = prefs,
                onClose = { selectedChampion = null }
            )
        }
    }
}

@Composable
fun SummaryCard(title: String, value: Int) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title)
            Text(value.toString(), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NumberEditor(title: String, value: Int, onChange: (Int) -> Unit) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, Modifier.weight(1f))
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it.filter { char -> char.isDigit() }
                onChange(text.toIntOrNull() ?: 0)
            },
            modifier = Modifier.width(110.dp),
            singleLine = true
        )
    }
}

@Composable
fun FilterMenu(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("$label: $selected")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ChampionRow(
    champion: Champion,
    isOwned: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() }
    ) {
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChampionAvatar(champion.avatarUrl)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(champion.name, fontWeight = FontWeight.Bold)
                Text("${champion.rarity} • ${champion.affinity} • ${champion.role}")
                Text(champion.faction, style = MaterialTheme.typography.bodySmall)
            }
            Checkbox(checked = isOwned, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
fun ChampionAvatar(url: String) {
    var bitmap by remember(url) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(url) {
        if (url.isNotBlank()) {
            bitmap = withContext(Dispatchers.IO) {
                try {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.connectTimeout = 8000
                    connection.readTimeout = 8000
                    val image = connection.inputStream.use { BitmapFactory.decodeStream(it) }
                    connection.disconnect()
                    image
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Şampiyon portresi",
            modifier = Modifier.size(58.dp)
        )
    } else {
        Surface(
            modifier = Modifier.size(58.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("RAID")
            }
        }
    }
}

@Composable
fun ChampionDetailDialog(
    champion: Champion,
    prefs: android.content.SharedPreferences,
    onClose: () -> Unit
) {
    var level by remember(champion.name) {
        mutableStateOf(prefs.getInt("${champion.name}_level", 60).toString())
    }
    var stars by remember(champion.name) {
        mutableStateOf(prefs.getInt("${champion.name}_stars", 6).toString())
    }
    var ascension by remember(champion.name) {
        mutableStateOf(prefs.getInt("${champion.name}_ascension", 6).toString())
    }
    var skills by remember(champion.name) {
        mutableStateOf(prefs.getString("${champion.name}_skills", "") ?: "")
    }
    var equipment by remember(champion.name) {
        mutableStateOf(prefs.getString("${champion.name}_equipment", "") ?: "")
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(champion.name) },
        text = {
            Column {
                ChampionAvatar(champion.avatarUrl)
                Text("${champion.rarity} • ${champion.affinity}")
                Text("Hizip: ${champion.faction}")
                Text("Rol: ${champion.role}")
                OutlinedTextField(level, { level = it }, label = { Text("Seviye") })
                OutlinedTextField(stars, { stars = it }, label = { Text("Yıldız") })
                OutlinedTextField(ascension, { ascension = it }, label = { Text("Yükseltme") })
                OutlinedTextField(
                    skills, { skills = it },
                    label = { Text("Yetenek notları") },
                    minLines = 2
                )
                OutlinedTextField(
                    equipment, { equipment = it },
                    label = { Text("Ekipman notları") },
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                prefs.edit()
                    .putInt("${champion.name}_level", level.toIntOrNull() ?: 0)
                    .putInt("${champion.name}_stars", stars.toIntOrNull() ?: 0)
                    .putInt("${champion.name}_ascension", ascension.toIntOrNull() ?: 0)
                    .putString("${champion.name}_skills", skills)
                    .putString("${champion.name}_equipment", equipment)
                    .apply()
                onClose()
            }) { Text("Kaydet") }
        },
        dismissButton = {
            TextButton(onClick = onClose) { Text("Kapat") }
        }
    )
}
