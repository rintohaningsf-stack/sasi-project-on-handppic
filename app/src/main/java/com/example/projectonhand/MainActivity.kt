package com.example.projectonhand

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

val Purple = Color(0xFF6200EE); val Blue = Color(0xFF045FF1); val Ink = Color(0xFF0F172A)
val Slate = Color(0xFF64748B); val Line = Color(0xFFE4E4E7); val Red = Color(0xFFFC4B4B); val Green = Color(0xFF08C256)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())
        setContent { TvScale { Dashboard() } }
    }
}

/** Membuat 1 dp = 1 px desain HTML (lebar 1920) di layar apa pun. */
@Composable fun TvScale(content: @Composable () -> Unit) {
    val d = LocalDensity.current
    val wPx = LocalConfiguration.current.screenWidthDp * d.density
    CompositionLocalProvider(LocalDensity provides Density(wPx / 1920f, 1f), content = content)
}

// ---------- helper ----------
@Composable fun T(t: String, sz: Float, c: Color = Ink, w: FontWeight = FontWeight.SemiBold, m: Modifier = Modifier, a: TextAlign? = null) =
    Text(t, m, color = c, fontSize = sz.sp, fontWeight = w, textAlign = a, maxLines = 1, softWrap = false)

/** Teks satu baris yang otomatis mengecil sampai muat (min 9sp) -> tidak ada yang terpotong. */
@Composable fun F(t: String, sz: Float, c: Color = Ink, w: FontWeight = FontWeight.SemiBold, m: Modifier = Modifier) {
    var s by remember(t, sz) { mutableStateOf(sz) }
    Text(t, m, color = c, fontSize = s.sp, fontWeight = w, maxLines = 1, softWrap = false,
        onTextLayout = { if (it.didOverflowWidth && s > 9f) s -= 0.5f })
}

@Composable fun Card(m: Modifier = Modifier, r: Int = 16, bg: Color = Color.White, bc: Color = Line, c: @Composable BoxScope.() -> Unit) =
    Box(m.clip(RoundedCornerShape(r.dp)).background(bg).border(1.dp, bc, RoundedCornerShape(r.dp)), content = c)

@Composable fun Ico(v: ImageVector, w: Int, h: Int, tint: Color = Color.Unspecified) = Icon(v, null, Modifier.size(w.dp, h.dp), tint)

@Composable fun Chip(t: String, fg: Color, bg: Color, bc: Color, sz: Float = 12f, r: Int = 6, icon: ImageVector? = null) =
    Box(Modifier.clip(RoundedCornerShape(r.dp)).background(bg).border(1.dp, bc, RoundedCornerShape(r.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { if (icon != null) { Ico(icon, 11, 12, fg); Spacer(Modifier.width(3.dp)) }; T(t, sz, fg) }
    }

@Composable fun ItemUnit(a: String, b: String, sz: Float = 12f, gap: Int = 8) = Row(verticalAlignment = Alignment.CenterVertically) {
    Ico(AppIcons.Stock, 10, 11, Purple); Spacer(Modifier.width(4.dp)); T(a, sz, Slate); Spacer(Modifier.width(gap.dp)); Ico(AppIcons.Stock, 10, 11, Blue); Spacer(Modifier.width(4.dp)); T(b, sz, Slate)
}

@Composable fun Avatar(url: String?, res: Int, size: Int = 34) = NetImage(url, Modifier.size(size.dp).clip(CircleShape), res)

@Composable fun IconSq(icon: ImageVector, bg: Color, fg: Color = Color.Unspecified) = Box(Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(bg), Alignment.Center) { Ico(icon, 16, 16, fg) }

@Composable fun SoChip(t: String) = Chip(t, Blue, Color(0xFFE6EFFE), Color(0xFFB1CDF8), 14f, 6, AppIcons.Bag)

// ---------- format ----------
private val ISO = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val OUT = SimpleDateFormat("dd MMM yyyy", Locale.US)
fun fmtDate(raw: String?): String {
    if (raw.isNullOrEmpty()) return "-"
    return runCatching { OUT.format(ISO.parse(raw.take(10))!!) }.getOrDefault(raw)
}
fun dayText(n: Int) = if (n == 1) "$n Day" else "$n Days"

/** Warna nama (mis. "red", "deep-blue", "black-200") dari API -> Color. */
fun namedColor(n: String): Color = when {
    n.contains("red") -> Red
    n.contains("blue") -> Blue
    n.contains("green") -> Green
    n.contains("pink") -> Color(0xFFEC4899)
    n.contains("yellow") || n.contains("amber") || n.contains("orange") -> Color(0xFFD97706)
    else -> Color(0xFF333333)
}

fun statusColor(v: Int) = when (v) {
    0 -> Color(0xFFD97706); 1 -> Color(0xFF814E02); 2 -> Color(0xFF0284C7); 3 -> Blue
    4 -> Color(0xFF16A34A); 5 -> Color(0xFF475569); else -> Red
}

/** Palet kartu status: [bg ikon, warna utama, bg kartu chip, border chip] */
fun statusPal(v: Int): List<String> = when (v) {
    0 -> listOf("FEF3C7", "D97706", "FFFBEB", "FDE68A")
    1 -> listOf("E0F2FE", "0284C7", "F0F9FF", "BAE6FD")
    2 -> listOf("FFEDD5", "EA580C", "FFF7ED", "FED7AA")
    3 -> listOf("F3E8FF", "6200EE", "FAF4FF", "CEB0FA")
    4 -> listOf("DCFCE7", "16A34A", "F0FDF4", "BBF7D0")
    5 -> listOf("E2E8F0", "475569", "F8FAFC", "CBD5E1")
    else -> listOf("FEE2E2", "DC2626", "FEF2F2", "FECACA")
}
fun statusIcon(v: Int): ImageVector = when (v) {
    0 -> AppIcons.WaitConfirm; 1 -> AppIcons.Open; 2 -> AppIcons.Box; 3 -> AppIcons.InProgress
    4, 5 -> AppIcons.Finish; else -> AppIcons.Hold
}

// ---------- layar utama ----------
@Composable fun Dashboard() {
    val st = rememberScrollState(); val sc = rememberCoroutineScope(); val fr = remember { FocusRequester() }
    val state = remember { PohState(sc) }
    LaunchedEffect(Unit) { fr.requestFocus() }
    LaunchedEffect(Unit) { while (true) { state.refresh(); delay(ApiConfig.REFRESH_MS) } }
    val d = state.data
    Column(
        Modifier.fillMaxSize().background(Color(0xFFF8FAFC))
            .onPreviewKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown) false else when (e.key) {
                    Key.DirectionDown -> { sc.launch { st.animateScrollBy(320f) }; true }
                    Key.DirectionUp -> { sc.launch { st.animateScrollBy(-320f) }; true }
                    else -> false
                }
            }.focusRequester(fr).focusable().verticalScroll(st)
            .padding(start = 28.dp, end = 24.dp, top = 28.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        if (d == null) Splash(state) else {
            state.error?.let { T("Gagal menyegarkan data: $it", 12f, Red) }
            val s = d.summary ?: SummaryData.EMPTY
            Header(s); KpiRow(s); Summary(s); ProjectTable(d, state)
        }
    }
}

@Composable fun Splash(state: PohState) = Box(Modifier.fillMaxWidth().height(600.dp).clickable { state.refresh() }, Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.error == null) T("Memuat data…", 28f, Slate) else {
            T("Gagal memuat data", 28f, Red); T(state.error ?: "", 16f, Slate, FontWeight.Normal); T("Ketuk layar untuk mencoba lagi", 16f, Blue, FontWeight.Medium)
        }
    }
}

@Composable fun Health(title: String, pct: String, n: String, fg: Color, bg: Color, bc: Color, w: Int, icon: ImageVector) =
    Column(Modifier.width(w.dp).height(66.dp).clip(RoundedCornerShape(12.dp)).background(bg).border(1.dp, bc, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 7.dp), Arrangement.SpaceBetween) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(18.dp).clip(RoundedCornerShape(4.dp)).background(Color.White), Alignment.Center) { Ico(icon, 11, 11) }; Spacer(Modifier.width(6.dp)); F(title, 12f, fg) }
            Chip(pct, fg, Color.White, bc, 10f, 20)
        }
        Row(verticalAlignment = Alignment.Bottom) { T(n, 24f); Spacer(Modifier.width(6.dp)); T("Projects", 12f, Slate, FontWeight.Medium) }
    }

@Composable fun Header(s: SummaryData) {
    val tot = s.total.count
    fun b(k: String) = s.buckets.firstOrNull { it.key == k }?.count ?: 0
    fun pct(n: Int) = if (tot > 0) "%.1f%%".format(Locale.US, n * 100f / tot) else "0%"
    Row(Modifier.fillMaxWidth().height(116.dp), Arrangement.spacedBy(14.dp)) {
        Card(Modifier.weight(1f).fillMaxHeight()) {
            Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFAF4FF)).border(1.dp, Color(0xFFCEB0FA), RoundedCornerShape(12.dp)), Alignment.Center) { Ico(AppIcons.Factory, 28, 28) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { T("Project On Hand", 30f); T("Executive overview of active project categories, live customer pipeline.", 14f, Slate, FontWeight.Normal) }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Health("Ahead Schedule", pct(b("ahead")), "${b("ahead")}", Color(0xFF0B5FE8), Color(0xFFE6EFFE), Color(0xFFB1CDF8), 195, AppIcons.Bolt)
                    Health("On Schedule", pct(b("on_track")), "${b("on_track")}", Color(0xFF16C55E), Color(0xFFEEFFF5), Color(0xFFB2ECD0), 174, AppIcons.CheckCircle)
                    Health("Running Late", pct(b("overdue")), "${b("overdue")}", Color(0xFFEF4444), Color(0xFFFFEBEB), Color(0xFFFBC4C4), 174, AppIcons.Warning)
                }
            }
        }
        Card(Modifier.width(332.dp).fillMaxHeight(), 20) { Clock() }
    }
}

@Composable fun Clock() {
    var now by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) { while (true) { now = Calendar.getInstance(); delay(1000) } }
    val id = Locale.forLanguageTag("id-ID")
    Row(Modifier.fillMaxSize().padding(5.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(Color(0xFFB840E6), Color(0xFF9219EB)))).padding(start = 11.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.size(92.dp, 88.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF1F8)), Arrangement.Top, Alignment.CenterHorizontally) {
            T(SimpleDateFormat("EEEE", id).format(now.time), 16f, Color(0xFF2D2D2D), FontWeight.Medium)
            Column(Modifier.size(82.dp, 56.dp).clip(RoundedCornerShape(10.dp)).background(Color.White), Arrangement.Center, Alignment.CenterHorizontally) {
                T("${now.get(Calendar.DAY_OF_MONTH)}", 36f, Color(0xFF9D28DD)); T(SimpleDateFormat("MMM yyyy", id).format(now.time), 14f, Color(0xFF7A7A7A))
            }
        }
        Spacer(Modifier.width(26.dp)); T("%02d:%02d".format(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)), 52f, Color.White, FontWeight.Bold)
    }
}

@Composable fun KpiRow(s: SummaryData) = Card(Modifier.fillMaxWidth(), 12) {
    Row(Modifier.padding(8.dp), Arrangement.spacedBy(7.dp), Alignment.CenterVertically) {
        Card(Modifier.width(219.dp).height(99.dp), 16, Color(0xFFFAF4FF), Color(0xFFCEB0FA)) {
            Column(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.Center) {
                T("TOTAL PROJECT", 14f, Purple)
                Row(verticalAlignment = Alignment.Bottom) { T("${s.total.count}", 34f); Spacer(Modifier.width(8.dp)); T("Project", 12f, Slate, m = Modifier.padding(bottom = 6.dp)) }
                ItemUnit("${s.total.item} Item", "${s.total.unit} Unit", 14f)
            }
        }
        Row(Modifier.weight(1f), Arrangement.spacedBy(7.dp)) {
            s.statusCards.filter { it.count > 0 }.forEach { k ->
                val pal = statusPal(k.value)
                fun c(i: Int) = Color(("FF" + pal[i]).toLong(16))
                val pct = if (s.total.count > 0) "%.1f%%".format(Locale.US, k.count * 100f / s.total.count) else "0%"
                Card(Modifier.weight(1f).height(87.dp)) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) { IconSq(statusIcon(k.value), c(0), c(1)); Spacer(Modifier.width(6.dp)); F(k.label, 14f, Color(0xFF1E293B)) }
                            Chip(pct, c(1), c(2), c(3), 10f, 20)
                        }
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            T("${k.count}", 24f)
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) { Ico(AppIcons.Stock, 10, 11, Purple); Spacer(Modifier.width(4.dp)); T("${k.item} Item", 12f, Slate) }
                                Row(verticalAlignment = Alignment.CenterVertically) { Ico(AppIcons.Stock, 10, 11, Blue); Spacer(Modifier.width(4.dp)); T("${k.unit} Unit", 12f, Slate) }
                            }
                        }
                    }
                }
            }
        }
        Box(Modifier.width(1.dp).height(79.dp).background(Line))
        People("Sales On Hand", Purple, s.sales); People("PIC On Hand", Blue, s.pics)
    }
}

@Composable fun People(title: String, badge: Color, rows: List<PersonRow>) = Card(Modifier.width(295.dp).height(99.dp)) {
    Column(Modifier.padding(start = 16.dp, top = 12.dp)) {
        T(title, 14f, Color(0xFF1E293B)); Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            rows.take(2).forEach { p ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box { Avatar(p.image, R.drawable.avatar_card); Box(Modifier.align(Alignment.TopEnd).offset(7.dp, (-5).dp).defaultMinSize(24.dp).height(16.dp).clip(RoundedCornerShape(6.dp)).background(badge).padding(horizontal = 3.dp), Alignment.Center) { T("${p.count}", 11f, Color.White) } }
                    Spacer(Modifier.width(8.dp)); F(p.label, 14f, Color(0xFF1E293B), m = Modifier.widthIn(max = 70.dp))
                }
            }
        }
    }
}

// ---------- kartu ringkasan ----------
@Composable fun SRow(name: String, lead: @Composable () -> Unit = {}, sub: @Composable () -> Unit, right: @Composable ColumnScope.() -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
        lead()
        Column(Modifier.weight(1f)) { F(name, 14f, w = FontWeight.Medium); sub() }   // lebih tipis dari judul kartu
        Column(horizontalAlignment = Alignment.End, content = right)
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFCBD5E1).copy(alpha = .7f)))
}

@Composable fun SCard(title: String, count: String, ic: Color, icon: ImageVector, other: Int, body: @Composable ColumnScope.() -> Unit) = Card(Modifier.fillMaxHeight(), 16) {
    Column(Modifier.padding(16.dp).heightIn(min = 184.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) { IconSq(icon, ic.copy(alpha = .15f)); Spacer(Modifier.width(8.dp)); F(title, 14f) }
            // Chip jumlah: disesuaikan dengan desain referensi (min 39 x 32, font 22)
            Box(Modifier.defaultMinSize(39.dp).height(32.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFF9FBFC)).border(1.dp, Color(0xFFE2E4E4), RoundedCornerShape(6.dp)).padding(horizontal = 12.dp), Alignment.Center) { T(count, 22f) }
        }
        Spacer(Modifier.height(3.dp)); body()
        if (other > 0) {
            Spacer(Modifier.weight(1f, false)); Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) { T("Other", 12f, Color(0xFF333333), FontWeight.Normal); Spacer(Modifier.width(6.dp)); Chip("+$other", Purple, Color(0xFFF5F0FF), Color(0xFFDDD0FA)) }
        }
    }
}

@Composable fun Summary(s: SummaryData) = Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), Arrangement.spacedBy(5.dp)) {
    s.cards.forEach { c -> Box(Modifier.weight(1f)) { SummaryCardView(c) } }
}

@Composable fun SummaryCardView(c: SummaryCard) {
    val (ic, icon) = when (c.key) {
        "categories" -> Color(0xFFD97706) to AppIcons.Categories
        "customers" -> Purple to AppIcons.Customer
        "products" -> Blue to AppIcons.Box
        "overdue" -> Red to AppIcons.Overdue
        "risks" -> Color(0xFFD97706) to AppIcons.Delay
        else -> Green to AppIcons.Truck
    }
    val palette = listOf(Color(0xFF16A34A), Color(0xFFEC4899), Color(0xFF2563EB))
    SCard(c.title, "${c.total}", ic, icon, c.otherCount) {
        c.rows.take(3).forEachIndexed { idx, r ->
            val iu: @Composable () -> Unit = { ItemUnit("${r.item} Item", "${r.unit} Unit") }
            val so: @Composable () -> Unit = { T(r.doc, 12f, Blue, FontWeight.Medium) }
            when (c.key) {
                "categories" -> {
                    val col = if (r.color.isNotEmpty()) namedColor(r.color) else palette[idx % palette.size]
                    SRow(r.name, { Box(Modifier.size(3.dp, 26.dp).clip(CircleShape).background(col)) }, iu) { SoChip("${r.count} Project") }
                }
                "customers" -> SRow(r.name, sub = iu) { SoChip("${r.count} SO") }
                "products" -> SRow(r.name, { NetImage(r.image, Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF3F3F4))) },
                    { T(r.code, 12f, Color(0xFF9D9D9D), FontWeight.Normal) }) { SoChip("${r.count} SO"); T("${r.unit} Unit", 12f, Slate) }
                "overdue" -> SRow(r.name, sub = so) { Chip(dayText(r.days), Red, Color(0xFFFEE2E2), Color(0xFFFEE2E2), 14f); T(fmtDate(r.dueDate), 12f, Slate, FontWeight.Normal) }
                "risks" -> SRow(r.name, sub = so) { T(fmtDate(r.dueDate), 14f, Red); T("+${r.days} Day", 12f, Red, FontWeight.Normal) }
                else -> SRow(r.name, sub = { T("${r.item} Item • ${r.unit} Unit", 12f, Slate, FontWeight.Normal) }) { T(fmtDate(r.dueDate), 14f); T(dayText(r.days), 12f, Blue, FontWeight.Normal) }
            }
        }
    }
}

// ---------- tabel ----------
// Kolom QTY dihapus -> semua indeks setelah ITEM LIST bergeser 1
val W = listOf(46, 242, 311, 121, 109, 110, 123, 99, 132, 130, 126, 109, 110)
val HEAD = listOf("NO.", "CUSTOMER", "ITEM LIST", "ORDER DATE", "DUE DATE", "SALES", "RAW MATERIAL", "WIP", "START PROJECT", "PROGRESS (%)", "EST FINISH", "PIC", "STATUS")
val FILTER_COLS = setOf(1, 2, 3, 4, 5, 8, 10, 11, 12)

@Composable fun RowScope.Cell(i: Int, c: @Composable ColumnScope.() -> Unit) = Column(Modifier.weight(W[i].toFloat()).padding(horizontal = if (i == 0) 6.dp else 12.dp), Arrangement.Center, content = c)

@Composable fun Bar(frac: Float, c: Color) = Box(Modifier.size(62.dp, 5.dp).clip(CircleShape).background(Color(0xFFF1F1F1))) { Box(Modifier.fillMaxWidth(frac.coerceIn(0f, 1f)).fillMaxHeight().background(c)) }

@Composable fun StockItem(a: String, b: String) = Row(verticalAlignment = Alignment.CenterVertically) {
    Ico(AppIcons.Stock, 10, 11, Purple); Spacer(Modifier.width(4.dp)); T(a, 14f, Color(0xFF333333)); T(b, 12f, Color(0xFF888A8A)); Spacer(Modifier.width(4.dp)); T("Item", 12f, Color(0xFFA1A1A1), FontWeight.Normal)
}

/** Atas = tanggal aktual/estimasi, bawah = tanggal plan (kecil, abu-abu). */
@Composable fun DateStack(actual: String?, plan: String?, color: Color) {
    T(actual ?: "-", 14f, color); T(plan ?: "-", 12f, Color(0xFF9D9D9D), FontWeight.Normal)
}

@Composable fun ProjectTable(d: PohData, state: PohState) = Card(Modifier.fillMaxWidth(), 16) {
    Column(Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.height(39.dp).clip(RoundedCornerShape(10.dp)).border(1.dp, Color(0xFFE9ECEF), RoundedCornerShape(10.dp)).padding(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    T("Periode :", 12f, Color(0xFF475569), FontWeight.Medium, Modifier.padding(start = 9.dp, end = 12.dp))
                    Row(Modifier.height(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF9FAFB)).border(1.dp, Color(0xFFD9DCE3), RoundedCornerShape(8.dp)), verticalAlignment = Alignment.CenterVertically) {
                        DateBox(fmtDate(d.periodeStart))
                        Box(Modifier.fillMaxHeight().background(Color.White).padding(horizontal = 12.dp), Alignment.Center) { T("To", 12f, Color(0xFF333333), FontWeight.Medium) }
                        DateBox(fmtDate(d.periodeFinish))
                    }
                }
                Box(Modifier.width(1.dp).height(24.dp).background(Line))
                Row(Modifier.size(211.dp, 32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFAFAFA)).border(1.dp, Color(0xFFEEEEF0), RoundedCornerShape(8.dp)).padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Ico(AppIcons.Search, 18, 18); Spacer(Modifier.width(10.dp)); T("Search Here", 12f, Color(0xFFCBD5E1), FontWeight.Normal)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("SAG", "SAI", "KMI").forEach { k ->
                    val cnt = d.pills.firstOrNull { it.key == k }?.count ?: 0
                    CompanyPill(k, cnt, state.company == k, { state.selectCompany(k) }) {
                        when (k) {
                            "SAG" -> Image(painterResource(R.drawable.logo_sag), null, Modifier.size(24.dp, 30.dp))
                            "SAI" -> Image(painterResource(R.drawable.logo_sai), null, Modifier.size(25.dp, 26.dp))
                            else -> Ico(AppIcons.Kmi, 22, 21)
                        }
                    }
                }
            }
        }
        Column(Modifier.clip(RoundedCornerShape(10.dp)).border(1.dp, Color(0xFFE3E4E5), RoundedCornerShape(10.dp))) {
            Row(Modifier.fillMaxWidth().height(36.dp).background(Color(0xFFF9FAFB)), verticalAlignment = Alignment.CenterVertically) { HEAD.forEachIndexed { i, h -> Cell(i) {
                Row(Modifier.fillMaxWidth(), if (i == 12) Arrangement.Center else Arrangement.Start, Alignment.CenterVertically) {
                    Box(Modifier.weight(1f, false)) { F(h, 11f, Color(0xFF475569)) }
                    if (i in FILTER_COLS) { Spacer(Modifier.width(4.dp)); Ico(AppIcons.Filter, 15, 15) }
                } } } }
            d.rows.forEachIndexed { idx, r ->
                val sc = if (r.isOverdue) Red else Blue
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE3E4E5)))
                Row(Modifier.fillMaxWidth().height(68.dp).background(if (idx % 2 == 0) Color.White else Color(0xFFF9FBFC)), verticalAlignment = Alignment.CenterVertically) {
                    Cell(0) { T("${idx + 1}", 14f, Color(0xFF333333)) }
                    Cell(1) { Row(verticalAlignment = Alignment.CenterVertically) {
                        if (r.companyImage != null) NetImage(r.companyImage, Modifier.size(18.dp, 17.dp), contentScale = androidx.compose.ui.layout.ContentScale.Fit) else Ico(AppIcons.Kmi, 18, 17)
                        Spacer(Modifier.width(3.dp)); Box(Modifier.size(2.dp, 17.dp).clip(CircleShape).background(if (r.typeColor == "pink") Color(0xFFEC4899) else Green)); Spacer(Modifier.width(3.dp))
                        Column(Modifier.weight(1f)) { Text(r.customer, color = Color(0xFF333333), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis); T(r.doc, 12f, Color(0xFF0066FE), FontWeight.Medium) }
                    } }
                    Cell(2) { Row(verticalAlignment = Alignment.CenterVertically) {
                        NetImage(r.itemPicture, Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFBFBFB)).border(1.dp, Color(0xFFE2E4E4), RoundedCornerShape(12.dp)))
                        Spacer(Modifier.width(7.dp))
                        Column(Modifier.weight(1f)) { F(r.itemName, 14f, Color(0xFF333333)); T(r.itemCode, 12f, Color(0xFF9D9D9D), FontWeight.Normal); Chip("Product", Green, Color(0xFFEEFFF5), Color(0xFFB2ECCB), 11f) }
                        if (r.itemExtra > 0) { Spacer(Modifier.width(12.dp)); Chip("+${r.itemExtra}", Purple, Color(0xFFF3E7FF), Color(0xFFCEB0FA), 14f, 4) }
                    } }
                    Cell(3) { T(r.orderDate, 14f, Color(0xFF333333)) }
                    Cell(4) {
                        T(r.dueLabel, 14f, sc)
                        // Icon calendar + Lead Time; jika overdue: pemisah "|" + teks overdue (merah)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Ico(AppIcons.CalendarFill, 13, 13, Blue); Spacer(Modifier.width(4.dp)); T("${r.leadTime}", 12f, Color(0xFF333333), FontWeight.Medium)
                            if (r.isOverdue) {
                                Spacer(Modifier.width(6.dp)); Box(Modifier.size(1.dp, 10.dp).background(Color(0xFFD9DCE3))); Spacer(Modifier.width(6.dp))
                                T("${abs(r.daysFromToday)} Day", 12f, Red, FontWeight.Normal)
                            }
                        }
                    }
                    Cell(5) { Row(verticalAlignment = Alignment.CenterVertically) { Avatar(r.salesImage, R.drawable.avatar_sales, 28); Spacer(Modifier.width(6.dp)); F(r.sales, 14f, Color(0xFF333333)) } }
                    Cell(6) { Row(verticalAlignment = Alignment.CenterVertically) { Bar(r.rawPct / 100f, Green); Spacer(Modifier.width(10.dp)); T("${r.rawPct}%", 14f, Green) }; Spacer(Modifier.height(2.dp)); StockItem("${r.rawDone}", "/${r.rawTotal}") }
                    Cell(7) { StockItem("${r.wipGood}", "/${r.wipTotal}") }
                    Cell(8) { DateStack(r.startActual, r.startPlan, namedColor(r.startActualColor)) }
                    Cell(9) { val c = if (r.isOverdue && r.progress < 100) Red else Green
                        Row(verticalAlignment = Alignment.CenterVertically) { Bar(r.progress / 100f, c); Spacer(Modifier.width(10.dp)); T("${r.progress}%", 12f, c) } }
                    Cell(10) { DateStack(r.finishActual, r.finishPlan, namedColor(r.finishActualColor)) }
                    Cell(11) {
                        // Nilai kosong / "-" = Tidak ada PIC: gambar kosong + keterangan "Tidak ada PIC"
                        if (r.pic.isBlank()) Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFF3F3F4))); Spacer(Modifier.width(6.dp))
                            F("Tidak ada PIC", 14f, Color(0xFF9D9D9D), FontWeight.Medium, Modifier.weight(1f, false))
                        }
                        else Row(verticalAlignment = Alignment.CenterVertically) { Avatar(r.picImage, R.drawable.avatar_pic, 28); Spacer(Modifier.width(6.dp)); F(r.pic, 14f, Color(0xFF333333)) }
                    }
                    Cell(12) { Box(Modifier.fillMaxWidth(), Alignment.Center) {
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(statusColor(r.statusValue)).padding(horizontal = 8.dp, vertical = 5.dp)) { F(r.statusName, 14f, Color.White) }
                    } }
                }
            }
        }
        Spacer(Modifier.height(7.dp))
        val hasMore = d.rows.size < d.total
        Row(Modifier.align(Alignment.CenterHorizontally), Arrangement.spacedBy(20.dp)) {
            ShowBtn("Show Less", AppIcons.ShowLess, Color(0xFFF59E0B), enabled = state.displayed > ApiConfig.PAGE_SIZE) { state.showLess() }
            if (hasMore) {
                ShowBtn("Show More", AppIcons.ShowMore, Color(0xFF0799EC)) { state.showMore() }
                ShowBtn("Show All", AppIcons.ShowAll, Color(0xFF16C784)) { state.showAll() }
            }
        }
        Spacer(Modifier.height(11.dp))
        T("Showing ${d.rows.size} of ${d.total}", 13f, Color(0xFF555555), FontWeight.Normal, Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable fun DateBox(d: String) = Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
    Ico(AppIcons.Calendar, 16, 16); Spacer(Modifier.width(8.dp)); T(d, 12f, Color(0xFF333333), FontWeight.Medium)
}

@Composable fun CompanyPill(name: String, count: Int, active: Boolean, onClick: () -> Unit, logo: @Composable () -> Unit) =
    Row(Modifier.height(38.dp).clip(RoundedCornerShape(12.dp)).background(if (active) Color(0xFFFAF5FF) else Color(0xFFFBFBFB))
        .border(1.dp, if (active) Color(0xFFD2B3FB) else Line, RoundedCornerShape(12.dp)).clickable(onClick = onClick)
        .padding(start = 11.dp, end = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        logo(); Spacer(Modifier.width(8.dp)); T(name, 13f, Color(0xFF1E293B)); Spacer(Modifier.width(8.dp))
        Box(Modifier.defaultMinSize(30.dp).height(22.dp).clip(CircleShape).background(if (active) Color(0xFF8A00FF) else Color(0xFFB0B4C2)).padding(horizontal = 8.dp), Alignment.Center) { T("$count", 13f, Color.White, FontWeight.Medium) }
    }

/** Tombol pagination mengikuti CSS: 197x30, bg #f8f9fa, border #dfe3e6, radius 7, gap 8, font 13 / 500. */
@Composable fun ShowBtn(label: String, icon: ImageVector, color: Color, enabled: Boolean = true, onClick: () -> Unit) =
    Row(
        Modifier.size(197.dp, 30.dp).alpha(if (enabled) 1f else 0.45f).clip(RoundedCornerShape(7.dp)).background(Color(0xFFF8F9FA))
            .border(1.dp, Color(0xFFDFE3E6), RoundedCornerShape(7.dp)).clickable(enabled = enabled, onClick = onClick),
        Arrangement.Center, Alignment.CenterVertically
    ) {
        Ico(icon, 14, 14); Spacer(Modifier.width(8.dp)); T(label, 13f, color, FontWeight.Medium)
    }