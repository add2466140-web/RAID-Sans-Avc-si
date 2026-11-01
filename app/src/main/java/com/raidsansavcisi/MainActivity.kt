
package com.raidsansavcisi

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

data class Champion(
    val name: String,
    val rarity: String,
    val faction: String,
    val role: String,
    val uses: String
)

data class SavedTeam(
    val name: String,
    val mode: String,
    val champions: List<String>
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("raid_data", Context.MODE_PRIVATE)

        setContent {
            MaterialTheme {
                val context = LocalContext.current

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
                    mutableStateOf(prefs.getStringSet("owned_champions", emptySet())?.toSet() ?: emptySet())
                }

                var selectedChampion by remember { mutableStateOf<Champion?>(null) }
                var level by remember { mutableStateOf("1") }
                var stars by remember { mutableStateOf("1") }
                var ascension by remember { mutableStateOf("0") }
                var skills by remember { mutableStateOf("") }
                var equipment by remember { mutableStateOf("") }

                var teamName by remember { mutableStateOf("") }
                var teamMode by remember { mutableStateOf("Hydra") }
                var selectedTeamChampions by remember { mutableStateOf(setOf<String>()) }
                var savedTeams by remember {
                    mutableStateOf(loadTeams(prefs.getString("saved_teams", "[]") ?: "[]"))
                }

                var suggestionMode by remember { mutableStateOf("Hydra") }
                var suggestion by remember { mutableStateOf<List<Champion>>(emptyList()) }

                var dungeon by remember { mutableStateOf(prefs.getString("dungeon_name", "Ejderha") ?: "Ejderha") }
                var dungeonStage by remember { mutableStateOf(prefs.getString("dungeon_stage", "1") ?: "1") }
                var farmGoal by remember { mutableStateOf(prefs.getString("farm_goal", "") ?: "") }
                var farmNotes by remember { mutableStateOf(prefs.getString("farm_notes", "") ?: "") }

                var probability by remember { mutableStateOf("1") }
                var message by remember { mutableStateOf("") }

                val champions = listOf(
                    Champion("Bad-el-Kazar", "Efsanevi", "Undead Hordes", "Destek", "İyileştirme, zehir ve arındırma"),
                    Champion("Arbiter", "Efsanevi", "High Elves", "Destek", "Hız, diriltme ve saldırı artırma"),
                    Champion("Ninja", "Efsanevi", "Shadowkin", "Hasar", "Patron ve zindan hasarı"),
                    Champion("Searsha", "Efsanevi", "Bilinmiyor", "Hasar", "Kullanım bilgisi oyuncu notlarına bağlı"),
                    Champion("Rotos", "Efsanevi", "Undead Hordes", "Hasar", "Arena ve tek hedef hasarı"),
                    Champion("Krisk", "Efsanevi", "Lizardmen", "Savunma", "Kışkırtma, savunma ve takım desteği"),
                    Champion("Mithrala", "Efsanevi", "Dark Elves", "Destek", "Kalkan, güçlendirme ve kontrol"),
                    Champion("Trunda", "Efsanevi", "Dwarves", "Hasar", "Arena ve alan hasarı"),
                    Champion("Gnut", "Efsanevi", "Dwarves", "Hasar", "Patron ve zindan hasarı"),
                    Champion("Duchess Lilitu", "Efsanevi", "Demonspawn", "Destek", "Diriltme ve hasar azaltma"),
                    Champion("Prince Kymar", "Efsanevi", "Demonspawn", "Kontrol", "Beceri sıfırlama ve kontrol"),
                    Champion("Siphi", "Efsanevi", "Undead Hordes", "Destek", "İyileştirme, hız ve diriltme"),
                    Champion("Nekhret", "Efsanevi", "Undead Hordes", "Destek", "Kalkan ve koruma"),
                    Champion("Turvold", "Efsanevi", "Barbarians", "Hasar", "Tek hedef hasarı"),
                    Champion("Mikage", "Mitik", "Shadowkin", "Destek", "Takım desteği ve dönüşüm"),
                    Champion("Siegfrund", "Mitik", "Banner Lords", "Hasar", "Yüksek hasar"),
                    Champion("Frolni", "Mitik", "Dwarves", "Hasar", "Hasar ve dayanıklılık"),

                    Champion("Drexthar Bloodtwin", "Efsanevi", "Demonspawn", "Hasar", "Yanma ve Hydra"),
                    Champion("Ma'Shalled", "Efsanevi", "Undead Hordes", "Destek", "Hız ve alan hasarı"),
                    Champion("Tomb Lord", "Efsanevi", "Undead Hordes", "Hasar", "Zehir ve savunma düşürme"),
                    Champion("Ithos", "Efsanevi", "High Elves", "Hasar", "Alan hasarı"),
                    Champion("Elenaril", "Efsanevi", "High Elves", "Hasar", "Zehir ve patron"),
                    Champion("Nethril", "Efsanevi", "Undead Hordes", "Kontrol", "Sersemletme ve zehir"),
                    Champion("Skartorsis", "Efsanevi", "Undead Hordes", "Destek", "Güçlendirme ve kontrol"),
                    Champion("Raglin", "Efsanevi", "Banner Lords", "Destek", "İyileştirme ve diriltme"),
                    Champion("Stag Knight", "Efsanevi", "Banner Lords", "Destek", "Savunma ve saldırı düşürme"),
                    Champion("Geomancer", "Epik", "Dwarves", "Hasar", "Patron ve yansıtma hasarı"),
                    Champion("Seer", "Epik", "Orcs", "Hasar", "Güçlendirme tüketen alan hasarı"),
                    Champion("Deacon Armstrong", "Epik", "Sacred Order", "Destek", "Hız ve savunma düşürme"),
                    Champion("Maneater", "Epik", "Demonspawn", "Destek", "Ölümsüzlük ve patron takımı"),
                    Champion("Pain Keeper", "Epik", "Dark Elves", "Destek", "Bekleme süresi azaltma"),
                    Champion("Uugo", "Epik", "Orcs", "Destek", "İyileştirme ve savunma düşürme"),
                    Champion("Alure", "Epik", "Demonspawn", "Kontrol", "Zindanlarda tur kontrolü"),
                    Champion("Coldheart", "Nadir", "Dark Elves", "Hasar", "Örümcek ve zindan patronları"),
                    Champion("Apothecary", "Nadir", "High Elves", "Destek", "Hız ve iyileştirme"),
                    Champion("Bellower", "Nadir", "Ogryn Tribes", "Hasar", "Dalga temizleme")
                )

                fun saveInt(key: String, value: Int) {
                    prefs.edit().putInt(key, value).apply()
                }

                fun saveText(key: String, value: String) {
                    prefs.edit().putString(key, value).apply()
                }

                fun saveTeams() {
                    prefs.edit().putString("saved_teams", teamsToJson(savedTeams)).apply()
                }

                fun openChampion(champion: Champion) {
                    selectedChampion = champion
                    level = prefs.getString("${champion.name}_level", "1") ?: "1"
                    stars = prefs.getString("${champion.name}_stars", "1") ?: "1"
                    ascension = prefs.getString("${champion.name}_ascension", "0") ?: "0"
                    skills = prefs.getString("${champion.name}_skills", "") ?: ""
                    equipment = prefs.getString("${champion.name}_equipment", "") ?: ""
                }

                fun suggestTeam() {
                    val available = champions.filter { owned.contains(it.name) }
                    val priority = when (suggestionMode) {
                        "Hydra" -> listOf("Destek", "Kontrol", "Hasar", "Savunma")
                        "Arena Saldırı" -> listOf("Destek", "Hasar", "Kontrol", "Savunma")
                        "Arena Savunma" -> listOf("Savunma", "Destek", "Kontrol", "Hasar")
                        "Ejderha", "Örümcek", "Ateş Şövalyesi", "Buz Golemi" ->
                            listOf("Hasar", "Destek", "Kontrol", "Savunma")
                        else -> listOf("Destek", "Hasar", "Savunma", "Kontrol")
                    }

                    val chosen = mutableListOf<Champion>()
                    priority.forEach { role ->
                        available.filter { it.role == role && it !in chosen }
                            .forEach { if (chosen.size < 5) chosen.add(it) }
                    }
                    available.filter { it !in chosen }.forEach {
                        if (chosen.size < 5) chosen.add(it)
                    }
                    suggestion = chosen.take(5)
                }

                val exportLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.CreateDocument("application/json")
                ) { uri: Uri? ->
                    if (uri != null) {
                        try {
                            val backup = JSONObject()
                            prefs.all.forEach { (key, value) ->
                                when (value) {
                                    is Set<*> -> backup.put(key, JSONArray(value.toList()))
                                    is String, is Int, is Long, is Float, is Boolean -> backup.put(key, value)
                                }
                            }
                            context.contentResolver.openOutputStream(uri)?.use {
                                it.write(backup.toString(2).toByteArray())
                            }
                            message = "Yedekleme dosyası oluşturuldu."
                        } catch (e: Exception) {
                            message = "Yedekleme başarısız: ${e.message}"
                        }
                    }
                }

                val importLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.OpenDocument()
                ) { uri: Uri? ->
                    if (uri != null) {
                        try {
                            val content = context.contentResolver.openInputStream(uri)
                                ?.bufferedReader()?.use { it.readText() }
                                ?: throw Exception("Dosya okunamadı")
                            val backup = JSONObject(content)
                            val editor = prefs.edit().clear()
                            val keys = backup.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                when (val value = backup.get(key)) {
                                    is JSONArray -> {
                                        val set = mutableSetOf<String>()
                                        for (i in 0 until value.length()) set.add(value.getString(i))
                                        editor.putStringSet(key, set)
                                    }
                                    is String -> editor.putString(key, value)
                                    is Int -> editor.putInt(key, value)
                                    is Long -> editor.putLong(key, value)
                                    is Boolean -> editor.putBoolean(key, value)
                                    is Number -> editor.putInt(key, value.toInt())
                                }
                            }
                            editor.apply()
                            message = "Yedek geri yüklendi. Uygulama yenileniyor."
                            (context as? Activity)?.recreate()
                        } catch (e: Exception) {
                            message = "Geri yükleme başarısız: ${e.message}"
                        }
                    }
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
                                listOf("Özet", "Mercy", "Kaynaklar", "Şampiyonlar", "Takım", "Zindan", "Yedek")
                                    .forEachIndexed { index, title ->
                                        Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) })
                                    }
                            }
                        }
                    }
                ) { padding ->
                    Column(
                        Modifier.fillMaxSize().padding(padding).padding(16.dp)
                    ) {
                        when (tab) {
                            0 -> Column(Modifier.verticalScroll(rememberScrollState())) {
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
                                Text("Kayıtlı takım: ${savedTeams.size}")
                                Text("Farm hedefi: $farmGoal")
                                Text("Uygulamadaki katalog: ${champions.size} şampiyon")
                            }

                            1 -> Column(Modifier.verticalScroll(rememberScrollState())) {
                                Text("MERCY TAKİBİ", style = MaterialTheme.typography.titleLarge)
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
                                OutlinedTextField(
                                    value = probability,
                                    onValueChange = { probability = it },
                                    label = { Text("Temel ihtimal (%)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text("Girilen temel ihtimal: %$probability")
                                Text("Bilgilendirme amaçlıdır; oyun içi garanti vermez.")
                            }

                            2 -> Column(Modifier.verticalScroll(rememberScrollState())) {
                                Text("KAYNAKLAR", style = MaterialTheme.typography.titleLarge)
                                ResourceRow("Antik", ancient) { ancient = it; saveInt("ancient", it) }
                                ResourceRow("Hiçlik", voidShard) { voidShard = it; saveInt("void", it) }
                                ResourceRow("Kutsal", sacred) { sacred = it; saveInt("sacred", it) }
                                ResourceRow("İlkel", primal) { primal = it; saveInt("primal", it) }
                            }

                            3 -> Column(Modifier.weight(1f)) {
                                Text("ŞAMPİYON KOLEKSİYONUM", style = MaterialTheme.typography.titleLarge)
                                Text("Sahip olduğun: ${owned.size}")
                                OutlinedTextField(
                                    value = search,
                                    onValueChange = { search = it },
                                    label = { Text("Şampiyon ara") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row {
                                    listOf("Tümü", "Efsanevi", "Mitik", "Epik", "Nadir").forEach {
                                        FilterChip(
                                            selected = filter == it,
                                            onClick = { filter = it },
                                            label = { Text(it) },
                                            modifier = Modifier.padding(end = 3.dp)
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
                                            Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                                .clickable { openChampion(champion) }
                                        ) {
                                            Column(Modifier.padding(12.dp)) {
                                                Text(champion.name, style = MaterialTheme.typography.titleMedium)
                                                Text("${champion.rarity} • ${champion.faction}")
                                                Text("Rol: ${champion.role}")
                                                Text("Kullanım: ${champion.uses}", style = MaterialTheme.typography.bodySmall)
                                                Row {
                                                    Button(onClick = {
                                                        val updated = owned.toMutableSet()
                                                        if (champion.name in updated) updated.remove(champion.name)
                                                        else updated.add(champion.name)
                                                        owned = updated.toSet()
                                                        prefs.edit().putStringSet("owned_champions", owned).apply()
                                                    }) {
                                                        Text(if (champion.name in owned) "✓ Sahibim" else "+ Sahibim")
                                                    }
                                                    TextButton(onClick = { openChampion(champion) }) {
                                                        Text("Detay")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            4 -> Column(
                                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                            ) {
                                Text("OTOMATİK TAKIM ÖNERİSİ", style = MaterialTheme.typography.titleLarge)
                                Text("Öneriler yalnızca koleksiyonunda sahip olarak işaretlediğin şampiyonlardan oluşturulur.")
                                listOf("Hydra", "Arena Saldırı", "Arena Savunma", "Ejderha",
                                    "Örümcek", "Ateş Şövalyesi", "Buz Golemi", "Klan Patronu").forEach { mode ->
                                    FilterChip(
                                        selected = suggestionMode == mode,
                                        onClick = { suggestionMode = mode },
                                        label = { Text(mode) },
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                                Button(onClick = { suggestTeam() }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Takım Öner")
                                }
                                if (suggestion.isNotEmpty()) {
                                    Text("ÖNERİLEN TAKIM", style = MaterialTheme.typography.titleMedium)
                                    suggestion.forEachIndexed { index, champ ->
                                        Text("${index + 1}. ${champ.name} — ${champ.role}")
                                    }
                                    Text("Bu öneri rol önceliğine dayalı basit bir taslaktır; kesin en iyi takım garantisi değildir.")
                                    Button(onClick = {
                                        teamName = "$suggestionMode Öneri"
                                        teamMode = suggestionMode
                                        selectedTeamChampions = suggestion.map { it.name }.toSet()
                                    }) {
                                        Text("Takım Kurucuya Aktar")
                                    }
                                } else {
                                    Text("Henüz öneri yok. Önce Şampiyonlar sekmesinden sahip olduklarını işaretle.")
                                }

                                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                Text("MANUEL TAKIM KURUCU", style = MaterialTheme.typography.titleLarge)
                                OutlinedTextField(
                                    value = teamName,
                                    onValueChange = { teamName = it },
                                    label = { Text("Takım adı") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text("Takım alanı: $teamMode")
                                Text("Seçim: ${selectedTeamChampions.size}/5")
                                champions.forEach { champion ->
                                    Row(
                                        Modifier.fillMaxWidth().clickable {
                                            val updated = selectedTeamChampions.toMutableSet()
                                            if (champion.name in updated) updated.remove(champion.name)
                                            else if (updated.size < 5) updated.add(champion.name)
                                            selectedTeamChampions = updated
                                        }.padding(vertical = 3.dp)
                                    ) {
                                        Checkbox(
                                            checked = champion.name in selectedTeamChampions,
                                            onCheckedChange = { checked ->
                                                val updated = selectedTeamChampions.toMutableSet()
                                                if (checked && updated.size < 5) updated.add(champion.name)
                                                else if (!checked) updated.remove(champion.name)
                                                selectedTeamChampions = updated
                                            }
                                        )
                                        Column {
                                            Text(champion.name)
                                            Text("${champion.rarity} • ${champion.role}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                                Button(onClick = {
                                    if (teamName.isBlank()) message = "Önce takıma isim ver."
                                    else if (selectedTeamChampions.size != 5) message = "Tam 5 şampiyon seç."
                                    else {
                                        val team = SavedTeam(teamName.trim(), teamMode, selectedTeamChampions.toList())
                                        savedTeams = savedTeams.filterNot { it.name == team.name } + team
                                        saveTeams()
                                        message = "Takım kaydedildi."
                                        teamName = ""
                                        selectedTeamChampions = emptySet()
                                    }
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Takımı Kaydet")
                                }
                                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                Text("KAYITLI TAKIMLAR", style = MaterialTheme.typography.titleMedium)
                                savedTeams.forEach { team ->
                                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                        Column(Modifier.padding(12.dp)) {
                                            Text(team.name, style = MaterialTheme.typography.titleMedium)
                                            Text("Alan: ${team.mode}")
                                            team.champions.forEachIndexed { index, name ->
                                                Text("${index + 1}. $name")
                                            }
                                            Row {
                                                TextButton(onClick = {
                                                    teamName = team.name
                                                    teamMode = team.mode
                                                    selectedTeamChampions = team.champions.toSet()
                                                }) { Text("Düzenle") }
                                                TextButton(onClick = {
                                                    savedTeams = savedTeams.filterNot { it.name == team.name }
                                                    saveTeams()
                                                    message = "Takım silindi."
                                                }) { Text("Sil") }
                                            }
                                        }
                                    }
                                }
                            }

                            5 -> Column(Modifier.verticalScroll(rememberScrollState())) {
                                Text("ZİNDAN VE FARM TAKİBİ", style = MaterialTheme.typography.titleLarge)
                                listOf("Ejderha", "Ateş Şövalyesi", "Örümcek", "Buz Golemi", "Minotor", "Golem Kulesi")
                                    .forEach { name ->
                                        FilterChip(
                                            selected = dungeon == name,
                                            onClick = { dungeon = name; saveText("dungeon_name", name) },
                                            label = { Text(name) },
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                    }
                                OutlinedTextField(
                                    value = dungeonStage,
                                    onValueChange = { dungeonStage = it; saveText("dungeon_stage", it) },
                                    label = { Text("Ulaştığın aşama") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = farmGoal,
                                    onValueChange = { farmGoal = it; saveText("farm_goal", it) },
                                    label = { Text("Farm hedefin") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = farmNotes,
                                    onValueChange = { farmNotes = it; saveText("farm_notes", it) },
                                    label = { Text("Farm notları") },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp)
                                )
                                Text("Seçili zindan: $dungeon")
                                Text("Aşama: $dungeonStage")
                            }

                            6 -> Column(Modifier.verticalScroll(rememberScrollState())) {
                                Text("YEDEKLEME VE GERİ YÜKLEME", style = MaterialTheme.typography.titleLarge)
                                Text("Şampiyonlarını, kaynaklarını, Mercy sayaçlarını ve takımlarını JSON dosyasına kaydet.")
                                Button(
                                    onClick = { exportLauncher.launch("raid_sans_avcisi_yedek.json") },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Yedek Dosyası Oluştur") }
                                Button(
                                    onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Yedekten Geri Yükle") }
                                Text("Dikkat: Geri yükleme mevcut kayıtların üzerine yazabilir. Önce güncel yedeğini al.")
                                if (message.isNotBlank()) Text(message)
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
                                Modifier.fillMaxWidth().heightIn(max = 480.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text("${champion.rarity} • ${champion.faction}")
                                Text("Rol: ${champion.role}")
                                Text("Kullanım: ${champion.uses}")
                                OutlinedTextField(
                                    value = level,
                                    onValueChange = { level = it },
                                    label = { Text("Seviye (1-60)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(
                                    value = stars,
                                    onValueChange = { stars = it },
                                    label = { Text("Yıldız (1-6)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(
                                    value = ascension,
                                    onValueChange = { ascension = it },
                                    label = { Text("Terfi (0-6)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                            }) { Text("Kaydet") }
                        },
                        dismissButton = {
                            TextButton(onClick = { selectedChampion = null }) { Text("Kapat") }
                        }
                    )
                }
            }
        }
    }
}

fun loadTeams(json: String): List<SavedTeam> {
    return try {
        val array = JSONArray(json)
        val result = mutableListOf<SavedTeam>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val names = item.getJSONArray("champions")
            val champs = mutableListOf<String>()
            for (j in 0 until names.length()) champs.add(names.getString(j))
            result.add(SavedTeam(item.getString("name"), item.getString("mode"), champs))
        }
        result
    } catch (e: Exception) {
        emptyList()
    }
}

fun teamsToJson(teams: List<SavedTeam>): String {
    val array = JSONArray()
    teams.forEach { team ->
        val item = JSONObject()
        item.put("name", team.name)
        item.put("mode", team.mode)
        item.put("champions", JSONArray(team.champions))
        array.put(item)
    }
    return array.toString()
}

@Composable
fun ResourceRow(title: String, value: Int, change: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$title: $value")
        Row {
            TextButton(onClick = { if (value > 0) change(value - 1) }) { Text("-") }
            TextButton(onClick = { change(value + 1) }) { Text("+") }
        }
    }
}

@Composable
fun MercyRow(title: String, value: Int, change: (Int) -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text("Mercy sayacı: $value")
            }
            Row {
                TextButton(onClick = { if (value > 0) change(value - 1) }) { Text("-1") }
                TextButton(onClick = { change(value + 1) }) { Text("+1") }
                TextButton(onClick = { change(0) }) { Text("Sıfırla") }
            }
        }
    }
}
