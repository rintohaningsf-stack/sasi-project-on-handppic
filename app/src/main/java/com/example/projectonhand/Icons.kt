@file:Suppress("ObjectPropertyName")
package com.example.projectonhand

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Dihasilkan dari path SVG di HTML (bentuk identik). Warna "currentColor" = hitam -> beri tint saat dipakai. */
private fun vec(n: String, vw: Float, vh: Float, b: ImageVector.Builder.() -> Unit) =
    ImageVector.Builder(n, vw.dp, vh.dp, vw, vh).apply(b).build()

private fun ImageVector.Builder.p(d: String, fill: Color? = null, eo: Boolean = false, stroke: Color? = null, sw: Float = 0f, round: Boolean = false) =
    addPath(addPathNodes(d), pathFillType = if (eo) PathFillType.EvenOdd else PathFillType.NonZero,
        fill = fill?.let { SolidColor(it) }, stroke = stroke?.let { SolidColor(it) }, strokeLineWidth = sw,
        strokeLineCap = if (round) StrokeCap.Round else StrokeCap.Butt, strokeLineJoin = if (round) StrokeJoin.Round else StrokeJoin.Miter)

object AppIcons {
    val Factory: ImageVector by lazy { vec("Factory", 24f, 24f) {
        p("M3 21V9.5l5 3V8l5 3V5h8v16H3z", fill = Color(0xFF6200EE))
        p("M7.0 15.0h2.0v3.4h-2.0z", fill = Color(0xFFFAF4FF))
        p("M11.5 15.0h2.0v3.4h-2.0z", fill = Color(0xFFFAF4FF))
        p("M16.0 15.0h2.0v3.4h-2.0z", fill = Color(0xFFFAF4FF))
    } }
    val Bolt: ImageVector by lazy { vec("Bolt", 24f, 24f) {
        p("M13 2L4 14h6.5L10 22l9-12h-6.5L13 2z", fill = Color(0xFF0B5FE8))
    } }
    /** Icon jam (On Schedule): lingkaran hijau dengan jarum jam berlubang (even-odd). */
    val CheckCircle: ImageVector by lazy { vec("CheckCircle", 24f, 24f) {
        p("M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18z", stroke = Color(0xFF16C55E), sw = 2.2f, round = true)
        p("M12 7v5l3.2 2", stroke = Color(0xFF16C55E), sw = 2.2f, round = true)
    } }
    val Warning: ImageVector by lazy { vec("Warning", 24f, 24f) {
        p("M12 3L1.5 21h21L12 3zm1 14h-2v-2h2v2zm0-4h-2V9h2v4z", fill = Color(0xFFF85555))
    } }
    val Stock: ImageVector by lazy { vec("Stock", 10f, 11f) {
        p("M8.081 2.45L7.33 1.4L2.472 1.4L1.722 2.45L8.081 2.45ZM0 2.898C0 2.608 0.092 2.323 0.26 2.085L1.332 0.586C1.595 0.219 2.019 0 2.47 0L7.328 0C7.781 0 8.205 0.219 8.468 0.586L9.538 2.085C9.708 2.323 9.798 2.608 9.798 2.898L9.8 8.75C9.8 9.522 9.172 10.15 8.4 10.15L1.4 10.15C0.628 10.15 0 9.522 0 8.75L0 2.898Z", fill = Color(0xFF000000))
    } }
    val WaitConfirm: ImageVector by lazy { vec("WaitConfirm", 14f, 14f) {
        p("M11.55 12.25L12.066 11.783L10.684 10.533L10.684 8.667L9.947 8.667L9.947 10.8L11.55 12.25ZM1.474 13.333C1.068 13.333 0.722 13.203 0.433 12.942C0.145 12.681 0 12.367 0 12L0 2.667C0 2.3 0.144 1.986 0.433 1.725C0.722 1.464 1.069 1.334 1.474 1.333L4.55 1.333C4.685 0.944 4.949 0.625 5.342 0.375C5.735 0.126 6.165 0 6.632 0C7.123 0 7.562 0.125 7.949 0.375C8.336 0.626 8.597 0.945 8.732 1.333L11.789 1.333C12.195 1.333 12.542 1.464 12.831 1.725C13.119 1.987 13.264 2.3 13.263 2.667L13.263 6.833C13.042 6.689 12.809 6.567 12.563 6.467C12.318 6.367 12.06 6.278 11.789 6.2L11.789 2.667L10.316 2.667L10.316 4.667L2.947 4.667L2.947 2.667L1.474 2.667L1.474 12L5.379 12C5.465 12.244 5.563 12.478 5.674 12.7C5.784 12.922 5.919 13.133 6.079 13.333L1.474 13.333ZM7.157 2.475C7.298 2.347 7.368 2.189 7.368 2C7.368 1.811 7.298 1.653 7.156 1.525C7.015 1.398 6.84 1.334 6.632 1.333C6.423 1.333 6.248 1.397 6.107 1.525C5.965 1.654 5.895 1.812 5.895 2C5.895 2.188 5.965 2.346 6.107 2.475C6.248 2.604 6.423 2.668 6.632 2.667C6.84 2.665 7.015 2.601 7.157 2.475Z", fill = Color(0xFF000000))
    } }
    val Open: ImageVector by lazy { vec("Open", 14f, 14f) {
        p("M1.302 14C0.944 14 0.638 13.829 0.383 13.486C0.128 13.144 0 12.732 0 12.25L0 1.75C0 1.269 0.128 0.857 0.383 0.514C0.638 0.172 0.945 0.001 1.302 0L5.209 0L6.512 1.75L11.721 1.75C12.079 1.75 12.386 1.921 12.641 2.264C12.896 2.607 13.024 3.019 13.023 3.5L1.302 3.5L1.302 12.25L2.865 5.25L14 5.25L12.323 12.753C12.236 13.132 12.076 13.435 11.843 13.661C11.61 13.888 11.352 14.001 11.07 14L1.302 14Z", fill = Color(0xFF000000))
    } }
    val InProgress: ImageVector by lazy { vec("InProgress", 14f, 14f) {
        p("M5.25 10.15L10.15 7L5.25 3.85L5.25 10.15ZM7 14C6.032 14 5.122 13.816 4.27 13.448C3.418 13.081 2.677 12.582 2.047 11.952C1.417 11.323 0.919 10.582 0.552 9.73C0.184 8.878 0 7.968 0 7C0 6.032 0.183 5.122 0.552 4.27C0.92 3.418 1.418 2.677 2.047 2.047C2.677 1.418 3.417 0.919 4.27 0.552C5.123 0.184 6.033 0 7 0C7.967 0 8.877 0.184 9.73 0.552C10.583 0.919 11.323 1.418 11.952 2.047C12.582 2.677 13.08 3.418 13.449 4.27C13.818 5.122 14.001 6.032 14 7C13.999 7.968 13.815 8.878 13.448 9.73C13.082 10.582 12.583 11.322 11.952 11.952C11.322 12.582 10.581 13.081 9.73 13.449C8.879 13.817 7.969 14.001 7 14Z", fill = Color(0xFF000000))
    } }
    val Hold: ImageVector by lazy { vec("Hold", 14f, 14f) {
        p("M4.9 9.8L6.3 9.8L6.3 4.2L4.9 4.2L4.9 9.8ZM7.7 9.8L9.1 9.8L9.1 4.2L7.7 4.2L7.7 9.8ZM7 14C6.032 14 5.122 13.816 4.27 13.448C3.418 13.081 2.677 12.582 2.047 11.952C1.417 11.323 0.919 10.582 0.552 9.73C0.184 8.878 0 7.968 0 7C0 6.032 0.183 5.122 0.552 4.27C0.92 3.418 1.418 2.677 2.047 2.047C2.677 1.418 3.417 0.919 4.27 0.552C5.123 0.184 6.033 0 7 0C7.967 0 8.877 0.184 9.73 0.552C10.583 0.919 11.323 1.418 11.952 2.047C12.582 2.677 13.08 3.418 13.449 4.27C13.818 5.122 14.001 6.032 14 7C13.999 7.968 13.815 8.878 13.448 9.73C13.082 10.582 12.583 11.322 11.952 11.952C11.322 12.582 10.581 13.081 9.73 13.449C8.879 13.817 7.969 14.001 7 14Z", fill = Color(0xFF000000))
    } }
    val Finish: ImageVector by lazy { vec("Finish", 14f, 14f) {
        p("M7 14C6.032 14 5.122 13.816 4.27 13.448C3.418 13.081 2.678 12.582 2.048 11.952C1.418 11.323 0.919 10.582 0.552 9.73C0.184 8.878 0 7.968 0 7C0 6.032 0.183 5.122 0.552 4.27C0.92 3.418 1.418 2.677 2.048 2.047C2.677 1.418 3.417 0.919 4.27 0.552C5.123 0.184 6.033 0 7 0C7.758 0 8.476 0.111 9.153 0.332C9.829 0.554 10.453 0.863 11.025 1.26L10.01 2.292C9.567 2.012 9.094 1.794 8.593 1.637C8.091 1.479 7.56 1.4 7 1.4C5.448 1.4 4.127 1.946 3.036 3.037C1.945 4.128 1.4 5.449 1.4 7C1.4 8.551 1.946 9.873 3.037 10.964C4.127 12.056 5.448 12.601 7 12.6C8.552 12.599 9.873 12.054 10.964 10.964C12.055 9.874 12.6 8.553 12.6 7C12.6 6.79 12.588 6.58 12.565 6.37C12.542 6.16 12.507 5.956 12.46 5.757L13.598 4.62C13.726 4.993 13.825 5.378 13.895 5.775C13.965 6.172 14 6.58 14 7C14 7.968 13.816 8.878 13.448 9.73C13.081 10.582 12.582 11.322 11.953 11.952C11.323 12.582 10.582 13.081 9.73 13.449C8.878 13.817 7.968 14 7 14ZM6.02 10.22L3.045 7.245L4.025 6.265L6.02 8.26L13.02 1.242L14 2.222L6.02 10.22Z", fill = Color(0xFF000000))
    } }
    val Categories: ImageVector by lazy { vec("Categories", 24f, 24f) {
        p("M3 21V9.5l5 3V8l5 3V5h8v16H3z", fill = Color(0xFFEB8D04))
        p("M7.0 15.0h2.0v3.4h-2.0z", fill = Color(0xFFFDF4E6))
        p("M11.5 15.0h2.0v3.4h-2.0z", fill = Color(0xFFFDF4E6))
        p("M16.0 15.0h2.0v3.4h-2.0z", fill = Color(0xFFFDF4E6))
    } }
    val Bag: ImageVector by lazy { vec("Bag", 12f, 13f) {
        p("M9 3.5V3a3 3 0 00-6 0v.5H1.2A.7.7 0 00.5 4.2l.6 7.4a1.3 1.3 0 001.3 1.2h7.2a1.3 1.3 0 001.3-1.2l.6-7.4a.7.7 0 00-.7-.7H9zM4.2 3a1.8 1.8 0 013.6 0v.5H4.2V3zm.05 3.6a.65.65 0 111.3 0 .65.65 0 01-1.3 0zm2.2 0a.65.65 0 111.3 0 .65.65 0 01-1.3 0z", fill = Color(0xFF000000))
    } }
    val Customer: ImageVector by lazy { vec("Customer", 24f, 24f) {
        p("M12 3L2 8v13h20V8L12 3z", fill = Color(0xFF6200EE))
        p("M9.0 12.0h6.0v6.0h-6.0z", fill = Color(0xFFFFFFFF))
        p("M11.0 14.0h2.0v2.0h-2.0z", fill = Color(0xFF6200EE))
    } }
    val Box: ImageVector by lazy { vec("Box", 24f, 24f) {
        p("M12 2L3 6.8v10.4L12 22l9-4.8V6.8L12 2zm0 2.3l5.8 3.1L12 10.5 6.2 7.4 12 4.3zM5 8.9l6 3.2v7.1l-6-3.2V8.9zm8 10.3v-7.1l6-3.2v7.1l-6 3.2z", fill = Color(0xFF045FF1))
    } }
    /** Icon jam (Overdue Project): lingkaran merah dengan jarum jam berlubang (even-odd). */
    val Overdue: ImageVector by lazy { vec("Overdue", 24f, 24f) {
        p("M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18z", stroke = Color(0xFFF04438), sw = 2.2f, round = true)
        p("M12 7v5l3.2 2", stroke = Color(0xFFF04438), sw = 2.2f, round = true)
    } }
    val Delay: ImageVector by lazy { vec("Delay", 24f, 24f) {
        p("M12 3L1.5 21h21L12 3zm1.1 14.2h-2.2v-2.2h2.2v2.2zm0-4h-2.2V9h2.2v4.2z", fill = Color(0xFFEB8D04))
    } }
    val Truck: ImageVector by lazy { vec("Truck", 24f, 24f) {
        p("M3 5h11v3h3.6L21 12v5h-1.8a2.7 2.7 0 01-5.4 0H9.6a2.7 2.7 0 01-5.4 0H3V5zm13 4.5V12h3.4l-1.7-2.5H16zM7 16a1.2 1.2 0 100 2.4A1.2 1.2 0 007 16zm10.5 0a1.2 1.2 0 100 2.4 1.2 1.2 0 000-2.4z", fill = Color(0xFF16A34A))
    } }
    val Calendar: ImageVector by lazy { vec("Calendar", 24f, 24f) {
        p("M6.0 5.0h12.0a2.5 2.5 0 0 1 2.5 2.5v10.5a2.5 2.5 0 0 1 -2.5 2.5h-12.0a2.5 2.5 0 0 1 -2.5 -2.5v-10.5a2.5 2.5 0 0 1 2.5 -2.5z", stroke = Color(0xFF9AA3B5), sw = 1.6f, round = true)
        p("M3.5 10h17M8 3v4M16 3v4", stroke = Color(0xFF9AA3B5), sw = 1.6f, round = true)
    } }
    /** Calendar solid (dipakai di Due Date -> Lead Time): badan terisi, 2 pengait atas, garis header & kotak tanggal berlubang. */
    val CalendarFill: ImageVector by lazy { vec("CalendarFill", 24f, 24f) {
        p("M7 2h2v3H7zM15 2h2v3h-2z", fill = Color(0xFF000000))
        p("M5 4h14a2 2 0 012 2v13a2 2 0 01-2 2H5a2 2 0 01-2-2V6a2 2 0 012-2z" +
                "M3 9h18v1.5H3z" +
                "M7 13h2v2H7zM11 13h2v2h-2zM15 13h2v2h-2z" +
                "M7 17h2v2H7zM11 17h2v2h-2z", fill = Color(0xFF000000), eo = true)
    } }
    val Search: ImageVector by lazy { vec("Search", 24f, 24f) {
        p("M4.5 11.0a6.5 6.5 0 1 0 13.0 0a6.5 6.5 0 1 0 -13.0 0z", stroke = Color(0xFFCBD5E1), sw = 1.8f, round = true)
        p("M16 16l4.5 4.5", stroke = Color(0xFFCBD5E1), sw = 1.8f, round = true)
    } }
    val Kmi: ImageVector by lazy { vec("Kmi", 18.17f, 17.12f) {
        p("M12.723 8.829l1.406.005c.265 0 .674.01.923-.022-.311.642-.7 1.325-1.043 1.958l-1.98 3.644-1.015 1.87c-.088.161-.37.724-.472.825-.251.006-2.22.021-2.299-.029.031-.097.199-.39.254-.493l.487-.899 1.793-3.293 1.272-2.35c.141-.261.519-1.003.674-1.216z", fill = Color(0xFF007BCA))
        p("M9.475 8.83l1.298.001c.3 0 .674-.01.967.008l.016.032c-.01.07-.034.11-.067.171-1.065 1.982-2.143 3.959-3.214 5.936l-.704 1.304c-.088.165-.37.719-.471.825-.755.02-1.564 0-2.327.006.097-.162.218-.415.313-.59l.737-1.361 2.325-4.29.769-1.413c.09-.163.255-.491.358-.629z", fill = Color(0xFF007BCA))
        p("M15.846 8.83c.769.007 1.539.005 2.309-.005-.163.399-.578 1.114-.796 1.518l-1.575 2.898-1.471 2.728c-.086.158-.554 1.055-.64 1.142h-2.329c.306-.538.624-1.162.922-1.712l2.446-4.508c.339-.615.765-1.487 1.134-2.062z", fill = Color(0xFF007BCA))
        p("M10.871.017c.683-.031 1.606-.011 2.3.007-1.375 2.629-2.858 5.216-4.244 7.841-.048.091-.159.321-.227.382-.769.017-1.557-.006-2.331.005.233-.488.587-1.1 1.849-1.577l1.195-2.201L9.95 1.64C10.099 1.364 10.691.186 10.871.017z", fill = Color(0xFF007BCA))
        p("M7.624.014c.736-.021 1.563-.002 2.306-.001-.182.289-.423.784-.596 1.102L6.655 6.054c-.15.275-1.115 2.117-1.222 2.19-.7.025-1.638-.038-2.306.018.198-.426.499-.952.727-1.372l1.144-2.121L6.662 1.716C6.802 1.459 7.47.15 7.624.014z", fill = Color(0xFF007BCA))
        p("M4.485.013c.212-.014.554-.003.775-.003.516.002 1.033.002 1.549-.001-.562.918-4.32 8.122-4.497 8.226-.373.019-.858.001-1.241.005-.271.003-.802.013-1.047-.012l-.008-.026 3.16-5.83 1.765-1.417c.138-.258.37-.729.544-.942z", fill = Color(0xFF007BCA))
        p("M13.919.496c.156.092.521.841.621 1.029l.815 1.511c.716 1.36 1.483 2.703 2.201 4.063.198.377.398.764.614 1.131-.318.038-.884.019-1.218.018-.217-.001-.925-.017-1.097.026-.132-.2-.263-.43-.374-.642-.891-1.706-1.85-3.379-2.749-5.08.117-.274.296-.536.443-.795.242-.43.483-.842.744-1.261z", fill = Color(0xFF00A5EF))
        p("M0 8.827c.491.03 1.059-.004 1.554.007.202.004.57-.021.753.027.201.197.739 1.27 1.907 1.58l1.299 2.406c.176.328.363.649.533.984.116.229.283.462.375.704-.048.148-.64.135-.756.335-.113.204-.312.575-.451.746-.365-.601-.752-1.361-1.091-1.984L2.339 11.326 1.48 9.749C1.338 9.489 1.108 9.09 0 8.827z", fill = Color(0xFF00A5EF))
    } }
    val Filter: ImageVector by lazy { vec("Filter", 18f, 18f) {
        p("M7.476 0L0.976 0C0.377 0 -0.122 0.493 0.026 1.073C0.184 1.687 0.474 2.259 0.876 2.749C1.278 3.239 1.783 3.635 2.354 3.91C2.8 4.123 3.142 4.542 3.142 5.036L3.142 8.332C3.142 8.424 3.166 8.515 3.211 8.596C3.256 8.676 3.321 8.744 3.399 8.792C3.478 8.841 3.568 8.869 3.66 8.873C3.752 8.877 3.844 8.857 3.926 8.816L5.01 8.275C5.1 8.23 5.175 8.16 5.228 8.075C5.281 7.989 5.309 7.891 5.309 7.79L5.309 5.036C5.309 4.542 5.651 4.123 6.097 3.91C6.668 3.635 7.173 3.239 7.576 2.749C7.978 2.259 8.268 1.687 8.426 1.073C8.573 0.493 8.073 0 7.476 0Z", stroke = Color(0xFF045FF1), sw = 1.2f, round = true)
    } }
    val ShowLess: ImageVector by lazy { vec("ShowLess", 20f, 20f) {
        p("M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zM7 11h10v2H7z", fill = Color(0xFFF59E0B), eo = true)
    } }
    val ShowMore: ImageVector by lazy { vec("ShowMore", 24f, 24f) {
        p("M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm5 11h-4v4h-2v-4H7v-2h4V7h2v4h4v2z", fill = Color(0xFF0799EC), eo = true)
    } }
    val ShowAll: ImageVector by lazy { vec("ShowAll", 24f, 24f) {
        p("M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2H5a2 2 0 0 1 -2 -2V5a2 2 0 0 1 2 -2z", stroke = Color(0xFF16C784), sw = 1.8f, round = true)
        p("M8 16l8-8M10 8h6v6", stroke = Color(0xFF16C784), sw = 1.8f, round = true)
    } }
}