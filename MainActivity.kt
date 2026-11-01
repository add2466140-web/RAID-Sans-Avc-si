package com.raidsansavcisi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow

private val Black = Color(0xFF101010)
private val Panel = Color(0xFF201D18)
private val Gold = Color(0xFFD6AE55)
private val Cream = Color(0xFFF4E8CC)
private val Muted = Color(0xFFB8AD98)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RaidApp() }
    }
}

@Composable
fun RaidApp() {
    val prefs = androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("raid_tracker", 0)
    var tab by remember { mutableIntStateOf(0) }
    var target by remember { mutableStateOf(prefs.getString("target", "") ?: "") }
    var shards by remember { mutableStateOf(prefs.getInt("shards", 0).toString()) }
    var baseRate by remember { mutableStateOf(prefs.getString("base", "0.5") ?: "0.5") }
    var mercyStart by remember { mutableStateOf(prefs.getString("start", "200") ?: "200") }
    var increase by remember { mutableStateOf(prefs.getString("increase", "0.025") ?: "0.025") }
    var ancient by remember { mutableStateOf(prefs.getInt("ancient", 0).toString()) }
    var voids by remember { mutableStateOf(prefs.getInt("voids", 0).toString()) }
    var sacred by remember { mutableStateOf(prefs.getInt("sacred", 0).toString()) }
    var primal by remember { mutableStateOf(prefs.getInt("primal", 0).toString()) }
    val count = shards.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val base = baseRate.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.5
    val start = mercyStart.toIntOrNull()?.coerceAtLeast(0) ?: 200
    val step = increase.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.025
    val next = (base + (count + 1 - start).coerceAtLeast(0) * step).coerceIn(0.0, 100.0)

    MaterialTheme(colorScheme = darkColorScheme(primary=Gold, onPrimary=Black, background=Black, surface=Panel, onSurface=Cream, onBackground=Cream, secondary=Gold)) {
        Column(Modifier.fillMaxSize().background(Black)) {
            Column(Modifier.fillMaxWidth().background(Panel).padding(18.dp)) {
                Text("RAID", color=Gold, fontSize=13.sp, fontWeight=FontWeight.Bold, letterSpacing=4.sp)
                Text("ŞANS AVCISI", color=Cream, fontSize=26.sp, fontWeight=FontWeight.Black)
                Text("Çağırmalarını planla • Kaynaklarını takip et", color=Muted, fontSize=12.sp)
            }
            NavigationBar(containerColor=Panel) {
                listOf("Özet","Mercy","Kaynaklar").forEachIndexed { i, label ->
                    NavigationBarItem(selected=tab==i, onClick={tab=i}, icon={Text(listOf("◆","✦","▣")[i])}, label={Text(label)},
                        colors=NavigationBarItemDefaults.colors(selectedIconColor=Black, indicatorColor=Gold, selectedTextColor=Gold, unselectedTextColor=Muted))
                }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
                when(tab) {
                    0 -> {
                        PanelCard("HEDEF ŞAMPİYON") {
                            OutlinedTextField(target, { target=it; prefs.edit().putString("target",it).apply() }, label={Text("Örn. Bad-el-Kazar")}, modifier=Modifier.fillMaxWidth(), singleLine=true)
                            Text("Hedef şampiyonunu burada takip et.", color=Muted)
                        }
                        PanelCard("ÇAĞIRMA SAYACI") {
                            NumberInput(shards, { shards=it; prefs.edit().putInt("shards",it.toIntOrNull()?:0).apply() }, "Efsanevi çıkmadan açılan parça")
                            Text("Kayıtlı sayaç: $count", color=Gold, fontWeight=FontWeight.Bold)
                            Button(onClick={shards="0";prefs.edit().putInt("shards",0).apply()}, colors=ButtonDefaults.buttonColors(containerColor=Gold,contentColor=Black)) { Text("Sayacı sıfırla") }
                        }
                        PanelCard("BİLGİ") {
                            Text("Uygulama oyun içi şansı değiştirmez. Sadece girdiğin oranlarla teorik hesap yapar.", color=Muted)
                        }
                    }
                    1 -> PanelCard("MERCY / OLASILIK HESABI") {
                        Text("Parça türü ve etkinlik oranına göre değerleri kendin gir.", color=Muted)
                        NumberInput(baseRate,{baseRate=it;prefs.edit().putString("base",it).apply()},"Başlangıç oranı (%)")
                        NumberInput(mercyStart,{mercyStart=it.filter(Char::isDigit);prefs.edit().putString("start",it).apply()},"Mercy başlangıç sayısı")
                        NumberInput(increase,{increase=it;prefs.edit().putString("increase",it).apply()},"Her sonraki parçada artış (yüzde puan)")
                        NumberInput(shards,{shards=it;prefs.edit().putInt("shards",it.toIntOrNull()?:0).apply()},"Mercy sayacındaki parça")
                        Text("Sıradaki çağırma için ayarlanmış oran", color=Muted)
                        Text(String.format("%.3f%%",next), color=Gold, fontSize=30.sp, fontWeight=FontWeight.Bold)
                        Text("Bu sonuç yalnızca girdiğin parametrelere dayanır. Güncel Mercy kuralını resmî oyun açıklamasından doğrula.", color=Muted)
                    }
                    else -> {
                        PanelCard("PARÇA ENVANTERİ") {
                            NumberInput(ancient,{ancient=it;prefs.edit().putInt("ancient",it.toIntOrNull()?:0).apply()},"Antik parça")
                            NumberInput(voids,{voids=it;prefs.edit().putInt("voids",it.toIntOrNull()?:0).apply()},"Hiçlik parçası")
                            NumberInput(sacred,{sacred=it;prefs.edit().putInt("sacred",it.toIntOrNull()?:0).apply()},"Kutsal parça")
                            NumberInput(primal,{primal=it;prefs.edit().putInt("primal",it.toIntOrNull()?:0).apply()},"Primal parça")
                            Text("Kayıtlar bu telefonda saklanır.", color=Muted)
                        }
                        PanelCard("ETKİNLİK REHBERİ") {
                            Text("2x: İlgili nadirlikte şampiyon çıkma oranını artırır.", color=Cream)
                            Text("10x / Progressive Chance: Hedef şampiyonun seçilme ihtimalini etkiler; temel nadirlik oranını tek başına artırmaz.", color=Cream)
                            Text("Garanti etkinliklerinde gereken parça sayısını oyun içi duyurudan kontrol et.", color=Muted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelCard(title:String, content:@Composable ColumnScope.()->Unit) {
    Card(colors=CardDefaults.cardColors(containerColor=Panel), shape=RoundedCornerShape(16.dp), border=BorderStroke(1.dp,Gold.copy(alpha=.55f))) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text(title,color=Gold,fontWeight=FontWeight.Bold,letterSpacing=1.sp)
            content()
        }
    }
}

@Composable
private fun NumberInput(value:String,onValue:(String)->Unit,label:String) {
    OutlinedTextField(value=value,onValueChange={onValue(it.filter { c -> c.isDigit() || c=='.' || c==',' })},
        label={Text(label)},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),modifier=Modifier.fillMaxWidth(),singleLine=true)
}
