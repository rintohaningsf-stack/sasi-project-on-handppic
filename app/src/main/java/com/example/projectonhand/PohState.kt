package com.example.projectonhand

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** State dashboard: data server + filter yang sedang aktif (company, satu filter pilihan, jumlah baris). */
class PohState(private val scope: CoroutineScope) {
    var data by mutableStateOf<PohData?>(null); private set
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    var company by mutableStateOf("SAG"); private set      // SAG = semua perusahaan
    var displayed by mutableStateOf(ApiConfig.PAGE_SIZE); private set

    /** Satu filter pilihan aktif: kind = bucket | status | sales | pic. Menekan lagi = lepas. */
    var fKind by mutableStateOf<String?>(null); private set
    var fValue by mutableStateOf<String?>(null); private set

    val bucket: String? get() = if (fKind == "bucket") fValue else null
    fun isSelected(kind: String, value: String) = fKind == kind && fValue == value

    /** Data pemilih (bucket, status, sales, PIC) tanpa filter, supaya angkanya tetap utuh saat salah satu dipilih. */
    private var baseBuckets by mutableStateOf<List<Bucket>>(emptyList())
    private var baseStatus by mutableStateOf<List<StatusCard>>(emptyList())
    private var baseSales by mutableStateOf<List<PersonRow>>(emptyList())
    private var basePics by mutableStateOf<List<PersonRow>>(emptyList())
    private var baseTotal by mutableStateOf(0)

    private var summary: SummaryData? = null
    private var job: Job? = null

    /** Summary untuk ditampilkan: pemilih memakai data dasar (tanpa filter) selama ada filter aktif. */
    fun view(s: SummaryData): SummaryData =
        if (fKind != null && baseTotal > 0)
            s.copy(buckets = baseBuckets, statusCards = baseStatus, sales = baseSales, pics = basePics)
        else s

    /** Total project tanpa filter (untuk persentase tiga kotak jadwal). */
    fun totalForPercent(s: SummaryData) = if (baseTotal > 0) baseTotal else s.total.count

    /** withSummary=false untuk paging tabel saja (lebih ringan; kartu tetap memakai data terakhir). */
    fun refresh(withSummary: Boolean = true) {
        job?.cancel()
        job = scope.launch {
            loading = true
            try {
                val kind = fKind; val v = fValue
                val params = buildList {
                    add("total_displayed" to displayed.toString())
                    if (company != "SAG") add("company[]" to company)
                    if (kind != null && v != null) {
                        add((when (kind) { "bucket" -> "bucket[]"; "status" -> "status[]"; "sales" -> "sales[]"; else -> "pic[]" }) to v)
                    }
                    if (!withSummary) add("summary" to "0")
                }
                val r = PohApi.fetchList(params)
                if (kind == null) r.summary?.let {
                    baseBuckets = it.buckets; baseStatus = it.statusCards; baseSales = it.sales; basePics = it.pics; baseTotal = it.total.count
                }
                summary = r.summary ?: summary
                data = r.copy(summary = summary)
                error = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = e.message ?: "Tidak dapat terhubung ke server"
            } finally {
                loading = false
            }
        }
    }

    fun selectCompany(key: String) {
        if (company == key) return
        company = key; fKind = null; fValue = null; displayed = ApiConfig.PAGE_SIZE; refresh(true)
    }

    /** Tekan sekali = filter nilai tersebut; tekan lagi = lepas filter. Memilih yang lain menggantikan filter sebelumnya. */
    fun toggle(kind: String, value: String) {
        if (isSelected(kind, value)) { fKind = null; fValue = null } else { fKind = kind; fValue = value }
        displayed = ApiConfig.PAGE_SIZE; refresh(true)
    }

    fun toggleBucket(key: String) = toggle("bucket", key)

    fun showMore() { displayed = minOf(displayed + ApiConfig.PAGE_SIZE, ApiConfig.MAX_ROWS); refresh(false) }
    fun showLess() { displayed = maxOf(displayed - ApiConfig.PAGE_SIZE, ApiConfig.PAGE_SIZE); refresh(false) }
    fun showAll() { displayed = minOf(data?.total ?: ApiConfig.MAX_ROWS, ApiConfig.MAX_ROWS); refresh(false) }
}