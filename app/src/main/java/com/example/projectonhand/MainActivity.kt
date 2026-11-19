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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable fun Sq(color: Color, w: Int = 10, h: Int = 11) = Box(Modifier.size(w.dp, h.dp).clip(RoundedCornerShape(2.dp)).background(color))

@Composable fun ItemUnit(a: String, b: String, sz: Float = 12f, gap: Int = 8) = Row(verticalAlignment = Alignment.CenterVertically) {
    Ico(AppIcons.Stock, 10, 11, Purple); Spacer(Modifier.width(4.dp)); T(a, sz, Slate); Spacer(Modifier.width(gap.dp)); Ico(AppIcons.Stock, 10, 11, Blue); Spacer(Modifier.width(4.dp)); T(b, sz, Slate)
}

@Composable fun Avatar(res: Int, size: Int = 34) = Image(painterResource(res), null, Modifier.size(size.dp).clip(CircleShape), contentScale = ContentScale.Crop)

val KPI_ICONS get() = listOf(AppIcons.WaitConfirm, AppIcons.Open, AppIcons.InProgress, AppIcons.Hold, AppIcons.Finish)

@Composable fun IconSq(icon: ImageVector, bg: Color, fg: Color = Color.Unspecified) = Box(Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(bg), Alignment.Center) { Ico(icon, 16, 16, fg) }

// ---------- layar utama ----------
@Composable fun Dashboard() {
    val st = rememberScrollState(); val sc = rememberCoroutineScope(); val fr = remember { FocusRequester() }
    LaunchedEffect(Unit) { fr.requestFocus() }
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
    ) { Header(); KpiRow(); Summary(); ProjectTable() }
}

@Composable fun Health(title: String, pct: String, n: String, fg: Color, bg: Color, bc: Color, w: Int, icon: ImageVector) =
    Column(Modifier.width(w.dp).height(66.dp).clip(RoundedCornerShape(12.dp)).background(bg).border(1.dp, bc, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 7.dp), Arrangement.SpaceBetween) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(18.dp).clip(RoundedCornerShape(4.dp)).background(Color.White), Alignment.Center) { Ico(icon, 11, 11) }; Spacer(Modifier.width(6.dp)); F(title, 12f, fg) }
            Chip(pct, fg, Color.White, bc, 10f, 20)
        }
        Row(verticalAlignment = Alignment.Bottom) { T(n, 24f); Spacer(Modifier.width(6.dp)); T("Projects", 12f, Slate, FontWeight.Medium) }
    }

@Composable fun Header() = Row(Modifier.fillMaxWidth().height(116.dp), Arrangement.spacedBy(14.dp)) {
    Card(Modifier.weight(1f).fillMaxHeight()) {
        Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFAF4FF)).border(1.dp, Color(0xFFCEB0FA), RoundedCornerShape(12.dp)), Alignment.Center) { Ico(AppIcons.Factory, 28, 28) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { T("Project On Hand", 30f); T("Executive overview of active project categories, live customer pipeline.", 14f, Slate, FontWeight.Normal) }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Health("Ahead Schedule", "19.7%", "28", Color(0xFF0B5FE8), Color(0xFFE6EFFE), Color(0xFFB1CDF8), 195, AppIcons.Bolt)
                Health("On Schedule", "71.8%", "102", Color(0xFF16C55E), Color(0xFFEEFFF5), Color(0xFFB2ECD0), 174, AppIcons.CheckCircle)
                Health("Running Late", "8.5%", "12", Color(0xFFEF4444), Color(0xFFFFEBEB), Color(0xFFFBC4C4), 174, AppIcons.Warning)
            }
        }
    }
    Card(Modifier.width(332.dp).fillMaxHeight(), 20) { Clock() }
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

@Composable fun KpiRow() = Card(Modifier.fillMaxWidth(), 12) {
    Row(Modifier.padding(8.dp), Arrangement.spacedBy(7.dp), Alignment.CenterVertically) {
        Card(Modifier.width(219.dp).height(99.dp), 16, Color(0xFFFAF4FF), Color(0xFFCEB0FA)) {
            Column(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.Center) {
                T("TOTAL PROJECT", 14f, Purple)
                Row(verticalAlignment = Alignment.Bottom) { T("142", 34f); Spacer(Modifier.width(8.dp)); T("Project", 12f, Slate, m = Modifier.padding(bottom = 6.dp)) }
                ItemUnit("53 Item", "60 Unit", 14f)
            }
        }
        Row(Modifier.weight(1f), Arrangement.spacedBy(7.dp)) {
            listOf(
                listOf("Wait Confirm", "12.7%", "18", "FEF3C7", "D97706", "FFFBEB", "FDE68A"), listOf("Open", "16.9%", "24", "E0F2FE", "0284C7", "F0F9FF", "BAE6FD"),
                listOf("In Progress", "45.1%", "64", "F3E8FF", "6200EE", "FAF4FF", "CEB0FA"), listOf("Hold", "5.6%", "8", "FEE2E2", "DC2626", "FEF2F2", "FECACA"),
                listOf("Finish", "19.7%", "28", "DCFCE7", "16A34A", "F0FDF4", "BBF7D0")
            ).forEachIndexed { ki, k ->
                fun c(i: Int) = Color(("FF" + k[i]).toLong(16))
                Card(Modifier.weight(1f).height(87.dp)) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) { IconSq(KPI_ICONS[ki], c(3), c(4)); Spacer(Modifier.width(6.dp)); F(k[0], 14f, Color(0xFF1E293B)) }
                            Chip(k[1], c(4), c(5), c(6), 10f, 20)
                        }
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            T(k[2], 24f)
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) { Ico(AppIcons.Stock, 10, 11, Purple); Spacer(Modifier.width(4.dp)); T("53 Item", 12f, Slate) }
                                Row(verticalAlignment = Alignment.CenterVertically) { Ico(AppIcons.Stock, 10, 11, Blue); Spacer(Modifier.width(4.dp)); T("60 Unit", 12f, Slate) }
                            }
                        }
                    }
                }
            }
        }
        Box(Modifier.width(1.dp).height(79.dp).background(Line))
        People("Sales On Hand", Purple, listOf("Sigit", "Sigit")); People("PIC On Hand", Blue, listOf("Faiz", "Giyanto"))
    }
}

@Composable fun People(title: String, badge: Color, names: List<String>) = Card(Modifier.width(295.dp).height(99.dp)) {
    Column(Modifier.padding(start = 16.dp, top = 12.dp)) {
        T(title, 14f, Color(0xFF1E293B)); Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            names.forEach { n ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box { Avatar(R.drawable.avatar_card); Box(Modifier.align(Alignment.TopEnd).offset(7.dp, (-5).dp).size(24.dp, 16.dp).clip(RoundedCornerShape(6.dp)).background(badge), Alignment.Center) { T("38", 11f, Color.White) } }
                    Spacer(Modifier.width(8.dp)); T(n, 14f, Color(0xFF1E293B))
                }
            }
        }
    }
}

// ---------- kartu ringkasan ----------
@Composable fun SRow(name: String, lead: @Composable () -> Unit = {}, sub: @Composable () -> Unit, right: @Composable ColumnScope.() -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
        lead()
        Column(Modifier.weight(1f)) { F(name, 14f, w = FontWeight.Medium); sub() }   // lebih tipis dari judul kartu (SemiBold)   // F: mengecil otomatis, tidak ada "..."
        Column(horizontalAlignment = Alignment.End, content = right)
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFCBD5E1).copy(alpha = .7f)))
}

@Composable fun SCard(title: String, count: String, ic: Color, icon: ImageVector, body: @Composable ColumnScope.() -> Unit) = Card(Modifier.fillMaxHeight(), 16) {
    Column(Modifier.padding(16.dp).heightIn(min = 184.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) { IconSq(icon, ic.copy(alpha = .15f)); Spacer(Modifier.width(8.dp)); F(title, 14f) }
            // Chip jumlah: disesuaikan dengan desain referensi (min 39 x 32, font 22)
            Box(Modifier.defaultMinSize(39.dp).height(32.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFF9FBFC)).border(1.dp, Color(0xFFE2E4E4), RoundedCornerShape(6.dp)).padding(horizontal = 12.dp), Alignment.Center) { T(count, 22f) }
        }
        Spacer(Modifier.height(3.dp)); body()
        Spacer(Modifier.weight(1f, false)); Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) { T("Other", 12f, Color(0xFF333333), FontWeight.Normal); Spacer(Modifier.width(6.dp)); Chip("+5", Purple, Color(0xFFF5F0FF), Color(0xFFDDD0FA)) }
    }
}

@Composable fun Summary() = Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), Arrangement.spacedBy(5.dp)) {
    val soChip = @Composable { t: String -> Chip(t, Blue, Color(0xFFE6EFFE), Color(0xFFB1CDF8), 14f, 6, AppIcons.Bag) }
    val so = @Composable { T("002/SO/KMI/10/25", 12f, Blue, FontWeight.Medium) }
    val i = Modifier.weight(1f)
    Box(i) { SCard("Project Categories", "4", Color(0xFFD97706), AppIcons.Categories) {
        listOf("Sales Order" to 0xFF16A34A, "Stock Buffer" to 0xFFEC4899, "Internal Project" to 0xFF2563EB).forEach { (n, c) ->
            SRow(n, { Box(Modifier.size(3.dp, 26.dp).clip(CircleShape).background(Color(c))) }, { ItemUnit("53 Item", "60 Unit") }) { soChip("2 Project") } } } }
    Box(i) { SCard("Active Customer", "100", Purple, AppIcons.Customer) {
        listOf("PT Astra Honda Motor", "PT Indofood Sukses Makmur", "PT Barata Indonesia").forEach { n -> SRow(n, sub = { ItemUnit("53 Item", "60 Unit") }) { soChip("2 SO") } } } }
    Box(i) { SCard("Ordered Products", "50", Blue, AppIcons.Box) {
        listOf("Wooden Trophy Wings" to "WVG-01", "Wooden Vandel Gun" to "WVG-02", "Acrylic Plaque Custom" to "AP-08").forEach { (n, s) ->
            SRow(n, { Box(Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF3F3F4))) }, { T(s, 12f, Color(0xFF9D9D9D), FontWeight.Normal) }) { soChip("2 SO"); T("60 Unit", 12f, Slate) } } } }
    Box(i) { SCard("Overdue Project", "3", Red, AppIcons.Overdue) {
        listOf("Aneka Usaha Indonesia" to "1 Day", "PT IVI Steel Indonesia" to "3 Days", "Sinar Mas Group" to "5 Days").forEach { (n, d) ->
            SRow(n, sub = so) { Chip(d, Red, Color(0xFFFEE2E2), Color(0xFFFEE2E2), 14f); T("23 Apr 2026", 12f, Slate, FontWeight.Normal) } } } }
    Box(i) { SCard("Estimated Delay Risks", "5", Color(0xFFD97706), AppIcons.Delay) {
        listOf("PT IVI Steel Indonesia", "Aneka Usaha Indonesia", "Indofood Sukses Makmur").forEach { n -> SRow(n, sub = so) { T("23 Apr 2026", 14f, Red); T("+2 Day", 12f, Red, FontWeight.Normal) } } } }
    Box(i) { SCard("Upcoming Delivery", "5", Green, AppIcons.Truck) {
        listOf("Aneka Usaha Indonesia" to "1 Day", "PT IVI Steel Indonesia" to "2 Days", "PT IVI Steel Indonesia" to "4 Days").forEach { (n, d) ->
            SRow(n, sub = { T("53 Item • 60 Unit", 12f, Slate, FontWeight.Normal) }) { T("23 Apr 2026", 14f); T(d, 12f, Blue, FontWeight.Normal) } } } }
}

// ---------- tabel ----------
class P(val no: Int, val cust: String, val so: String, val item: String, val sku: String, val qty: Int, val order: String, val due: String,
        val dueNote: String, val late: Boolean, val rm: Int, val wip: Int, val prog: String, val sales: String, val pic: String, val status: String)

// Kolom QTY dihapus -> semua indeks setelah ITEM LIST bergeser 1
val W = listOf(34, 242, 311, 121, 109, 110, 123, 99, 132, 130, 126, 109, 110)
val HEAD = listOf("NO.", "CUSTOMER", "ITEM LIST", "ORDER DATE", "DUE DATE", "SALES", "RAW MATERIAL", "WIP", "START PROJECT", "PROGRESS (%)", "EST FINISH", "PIC", "STATUS")
val FILTER_COLS = setOf(1, 2, 3, 4, 5, 8, 10, 11, 12)
val DATA = listOf(
    P(1, "PT N3 Steel Indonesia", "001/SO/KMI/10/25", "Metrisis Digital Weight Scale 02", "TKI_01", 100, "23 Apr 2020", "23 Apr 2020", "2 Day", true, 78, 5, "78/83", "Sigit", "Faiz", "Open"),
    P(2, "Aneka Usaha Indonesia", "001/SO/KMI/10/25", "Wooden Trophy Wings A", "TKI_01", 50, "12 Aug 2019", "12 Aug 2019", "2 Day", true, 78, 5, "78/78", "Sigit", "Giyanto", "Open"),
    P(3, "Bumi Resources", "002/SO/KMI/10/25", "Stainless Steel Water Bottle 500ml", "TKI_02", 70, "05 Jan 2021", "05 Jan 2021", "1 Week", true, 78, 5, "78/83", "Sigit", "Faiz", "In Progress"),
    P(4, "Sinar Mas Group", "002/SO/KMI/10/25", "Eco-Friendly Yoga Mat", "TKI_02", 23, "17 Mar 2022", "15 Mar 2022", "3 Days", false, 78, 5, "78/78", "Sigit", "Faiz", "In Progress"),
    P(5, "Indofood Sukses Makmur", "003/SO/KMI/10/25", "Bluetooth Wireless Headphones", "TKI_03", 85, "29 Oct 2018", "30 Nov 2020", "5 Days", false, 78, 5, "78/78", "Sigit", "Faiz", "In Progress"),
    P(10, "United Tractors Indonesia", "009/SO/KMI/12/25", "Hydraulic Sensor Unit Pro-2", "TKI_08", 110, "25 Oct 2021", "12 Nov 2021", "3 Week", false, 82, 9, "78/78", "Sigit", "Faiz", "In Progress"),
    P(9, "Mayora Indah Tbk", "008/SO/KMI/10/25", "Automatic Sealing Machine Pro", "TKI_07", 60, "10 Sep 2021", "24 Sep 2021", "2 Week", false, 75, 6, "78/83", "Sigit", "Giyanto", "Open"),
    P(8, "Barito Pacific Timber", "007/SO/KMI/10/25", "Heavy Duty Industrial Cutter", "TKI_06", 15, "19 Aug 2021", "05 Sep 2021", "2 Week", false, 95, 3, "78/78", "Sigit", "Faiz", "In Progress"),
    P(7, "Kalbe Farma Tbk", "006/SO/KMI/11/25", "Medical Centrifuge Model C-10", "TKI_05", 30, "02 Jul 2021", "15 Jul 2021", "1 Week", false, 88, 4, "78/83", "Sigit", "Giyanto", "Open"),
    P(6, "PT Astra Agro Lestari", "005/SO/KMI/10/25", "Precision Oil Viscometer V3_04", "TKI_04", 45, "14 Jun 2021", "28 Jun 2021", "2 Week", false, 92, 7, "78/78", "Sigit", "Faiz", "In Progress")
)

@Composable fun RowScope.Cell(i: Int, c: @Composable ColumnScope.() -> Unit) = Column(Modifier.weight(W[i].toFloat()).padding(horizontal = 12.dp), Arrangement.Center, content = c)

@Composable fun Bar(frac: Float, c: Color) = Box(Modifier.size(62.dp, 5.dp).clip(CircleShape).background(Color(0xFFF1F1F1))) { Box(Modifier.fillMaxWidth(frac).fillMaxHeight().background(c)) }

@Composable fun Pa(p: String, a: String) = Column(Modifier.width(105.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE2E4E4), RoundedCornerShape(8.dp))) {
    listOf("P" to p, "A" to a).forEach { (l, d) ->
        val pl = l == "P"
        Row(Modifier.height(24.dp).background(if (pl) Color.White else Color(0xFFE6EFFE)), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(19.dp), Alignment.Center) { T(l, 14f, if (pl) Purple else Blue) }
            Box(Modifier.weight(1f), Alignment.Center) { T(d, 14f, if (pl) Purple else Blue) }
        }
    }
}

@Composable fun StockItem(a: String, b: String) = Row(verticalAlignment = Alignment.CenterVertically) {
    Ico(AppIcons.Stock, 10, 11, Purple); Spacer(Modifier.width(4.dp)); T(a, 14f, Color(0xFF333333)); T(b, 12f, Color(0xFF888A8A)); Spacer(Modifier.width(4.dp)); T("Item", 12f, Color(0xFFA1A1A1), FontWeight.Normal)
}

/** Atas = tanggal aktual/estimasi (merah jika terlambat), bawah = tanggal plan (kecil, abu-abu). */
@Composable fun DateStack(actual: String, plan: String, late: Boolean) {
    T(actual, 14f, if (late) Red else Color(0xFF333333)); T(plan, 12f, Color(0xFF9D9D9D), FontWeight.Normal)
}

class Sch(val sA: String, val sP: String, val sLate: Boolean, val eA: String, val eP: String, val eLate: Boolean)
val SCHED = listOf(
    Sch("9 Apr 2026", "9 Apr 2026", true, "12 Apr 2026", "13 Apr 2026", false),
    Sch("9 Apr 2026", "8 Apr 2026", true, "12 Apr 2026", "10 Apr 2026", true),
    Sch("8 Apr 2026", "8 Apr 2026", false, "9 Apr 2026", "14 Apr 2026", false),
    Sch("8 Apr 2026", "8 Apr 2026", false, "9 Apr 2026", "10 Apr 2026", false),
    Sch("9 Apr 2026", "8 Apr 2026", false, "9 Apr 2026", "11 Apr 2026", false),
    Sch("8 Apr 2026", "8 Apr 2026", false, "10 Apr 2026", "12 Apr 2026", false),
    Sch("10 Apr 2026", "8 Apr 2026", true, "13 Apr 2026", "11 Apr 2026", true),
    Sch("8 Apr 2026", "8 Apr 2026", false, "9 Apr 2026", "13 Apr 2026", false),
    Sch("9 Apr 2026", "8 Apr 2026", true, "12 Apr 2026", "10 Apr 2026", true),
    Sch("8 Apr 2026", "8 Apr 2026", false, "9 Apr 2026", "12 Apr 2026", false)
)

@Composable fun ProjectTable() = Card(Modifier.fillMaxWidth(), 16) {
    Column(Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.height(39.dp).clip(RoundedCornerShape(10.dp)).border(1.dp, Color(0xFFE9ECEF), RoundedCornerShape(10.dp)).padding(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    T("Periode :", 12f, Color(0xFF475569), FontWeight.Medium, Modifier.padding(start = 9.dp, end = 12.dp))
                    Row(Modifier.height(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF9FAFB)).border(1.dp, Color(0xFFD9DCE3), RoundedCornerShape(8.dp)), verticalAlignment = Alignment.CenterVertically) {
                        DateBox("1 Jan 2024")
                        Box(Modifier.fillMaxHeight().background(Color.White).padding(horizontal = 12.dp), Alignment.Center) { T("To", 12f, Color(0xFF333333), FontWeight.Medium) }
                        DateBox("31 Jan 2024")
                    }
                }
                Box(Modifier.width(1.dp).height(24.dp).background(Line))
                Row(Modifier.size(211.dp, 32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFAFAFA)).border(1.dp, Color(0xFFEEEEF0), RoundedCornerShape(8.dp)).padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Ico(AppIcons.Search, 18, 18); Spacer(Modifier.width(10.dp)); T("Search Here", 12f, Color(0xFFCBD5E1), FontWeight.Normal)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CompanyPill("SAG", 80, true) { Image(painterResource(R.drawable.logo_sag), null, Modifier.size(24.dp, 30.dp)) }
                CompanyPill("SAI", 60, false) { Image(painterResource(R.drawable.logo_sai), null, Modifier.size(25.dp, 26.dp)) }
                CompanyPill("KMI", 20, false) { Ico(AppIcons.Kmi, 22, 21) }
            }
        }
        Column(Modifier.clip(RoundedCornerShape(10.dp)).border(1.dp, Color(0xFFE3E4E5), RoundedCornerShape(10.dp))) {
            Row(Modifier.fillMaxWidth().height(36.dp).background(Color(0xFFF9FAFB)), verticalAlignment = Alignment.CenterVertically) { HEAD.forEachIndexed { i, h -> Cell(i) {
                Row(Modifier.fillMaxWidth(), if (i == 12) Arrangement.Center else Arrangement.Start, Alignment.CenterVertically) {
                    Box(Modifier.weight(1f, false)) { F(h, 11f, Color(0xFF475569)) }
                    if (i in FILTER_COLS) { Spacer(Modifier.width(4.dp)); Ico(AppIcons.Filter, 15, 15) }
                } } } }
            DATA.forEachIndexed { idx, p ->
                val sc = if (p.late) Red else Blue; val sh = SCHED[idx]
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE3E4E5)))
                Row(Modifier.fillMaxWidth().height(68.dp).background(if (idx % 2 == 0) Color.White else Color(0xFFF9FBFC)), verticalAlignment = Alignment.CenterVertically) {
                    Cell(0) { T("${p.no}", 14f, Color(0xFF333333)) }
                    Cell(1) { Row(verticalAlignment = Alignment.CenterVertically) {
                        Ico(AppIcons.Kmi, 18, 17); Spacer(Modifier.width(3.dp)); Box(Modifier.size(2.dp, 17.dp).clip(CircleShape).background(Green)); Spacer(Modifier.width(3.dp))
                        Column(Modifier.weight(1f)) { F(p.cust, 14f, Color(0xFF333333)); T(p.so, 12f, Color(0xFF0066FE), FontWeight.Medium) }
                    } }
                    Cell(2) { Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFBFBFB)).border(1.dp, Color(0xFFE2E4E4), RoundedCornerShape(12.dp)))
                        Spacer(Modifier.width(7.dp))
                        Column(Modifier.weight(1f)) { F(p.item, 14f, Color(0xFF333333)); T(p.sku, 12f, Color(0xFF9D9D9D), FontWeight.Normal); Chip("Product", Green, Color(0xFFEEFFF5), Color(0xFFB2ECCB), 11f) }
                        Spacer(Modifier.width(12.dp)); Chip("+1", Purple, Color(0xFFF3E7FF), Color(0xFFCEB0FA), 14f, 4)
                    } }
                    Cell(3) { T(p.order, 14f, Color(0xFF333333)) }
                    Cell(4) {
                        T(p.due, 14f, sc)
                        // Icon calendar + Lead Time ("2"); jika overdue: pemisah "|" + teks overdue (merah)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Ico(AppIcons.CalendarFill, 13, 13, Blue); Spacer(Modifier.width(4.dp)); T("2", 12f, Color(0xFF333333), FontWeight.Medium)
                            if (p.late) {
                                Spacer(Modifier.width(6.dp)); Box(Modifier.size(1.dp, 10.dp).background(Color(0xFFD9DCE3))); Spacer(Modifier.width(6.dp))
                                T(p.dueNote, 12f, Red, FontWeight.Normal)
                            }
                        }
                    }
                    Cell(5) { Row(verticalAlignment = Alignment.CenterVertically) { Avatar(R.drawable.avatar_sales, 28); Spacer(Modifier.width(6.dp)); F(p.sales, 14f, Color(0xFF333333)) } }
                    Cell(6) { Row(verticalAlignment = Alignment.CenterVertically) { Bar(p.rm / 100f, Green); Spacer(Modifier.width(10.dp)); T("${p.rm}%", 14f, Green) }; Spacer(Modifier.height(2.dp)); StockItem("5", "/10") }
                    Cell(7) { StockItem("${p.wip}", "/10") }
                    Cell(8) { DateStack(sh.sA, sh.sP, sh.sLate) }
                    Cell(9) { val ok = p.prog.substringBefore('/') == p.prog.substringAfter('/'); val c = if (ok) Green else Red
                        Row(verticalAlignment = Alignment.CenterVertically) { Bar(.78f, c); Spacer(Modifier.width(10.dp)); T(p.prog.substringBefore('/') + "%", 12f, c) } }
                    Cell(10) { DateStack(sh.eA, sh.eP, sh.eLate) }
                    Cell(11) { Row(verticalAlignment = Alignment.CenterVertically) { Avatar(R.drawable.avatar_pic, 28); Spacer(Modifier.width(6.dp)); F(p.pic, 14f, Color(0xFF333333)) } }
                    Cell(12) { Box(Modifier.fillMaxWidth(), Alignment.Center) {
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(if (p.status == "Open") Color(0xFF814E02) else Blue).padding(horizontal = 8.dp, vertical = 5.dp)) { T(p.status, 14f, Color.White) }
                    } }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.align(Alignment.CenterHorizontally), Arrangement.spacedBy(10.dp)) {
            ShowBtn("Show Less", AppIcons.ShowLess); ShowBtn("Show more", AppIcons.ShowMore); ShowBtn("Show All", AppIcons.ShowAll)
        }
        Spacer(Modifier.height(4.dp))
        T("(showed 10 dari 100)", 10f, Color(0xFF9D9D9D), FontWeight.Normal, Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable fun DateBox(d: String) = Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
    Ico(AppIcons.Calendar, 16, 16); Spacer(Modifier.width(8.dp)); T(d, 12f, Color(0xFF333333), FontWeight.Medium)
}

@Composable fun CompanyPill(name: String, count: Int, active: Boolean, logo: @Composable () -> Unit) =
    Row(Modifier.height(38.dp).clip(RoundedCornerShape(12.dp)).background(if (active) Color(0xFFFAF5FF) else Color(0xFFFBFBFB))
        .border(1.dp, if (active) Color(0xFFD2B3FB) else Line, RoundedCornerShape(12.dp)).padding(start = 11.dp, end = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        logo(); Spacer(Modifier.width(8.dp)); T(name, 13f, Color(0xFF1E293B)); Spacer(Modifier.width(8.dp))
        Box(Modifier.defaultMinSize(30.dp).height(22.dp).clip(CircleShape).background(if (active) Color(0xFF8A00FF) else Color(0xFFB0B4C2)).padding(horizontal = 8.dp), Alignment.Center) { T("$count", 13f, Color.White, FontWeight.Medium) }
    }

@Composable fun ShowBtn(label: String, icon: ImageVector) =
    Row(Modifier.clip(RoundedCornerShape(8.dp)).border(1.dp, Line, RoundedCornerShape(8.dp)).padding(horizontal = 14.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Ico(icon, 16, 16); Spacer(Modifier.width(6.dp)); T(label, 12f, Color(0xFF1E293B), FontWeight.Medium)
    }