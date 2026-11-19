package com.example.projectonhand

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** State dashboard: data server + filter yang sedang aktif (company, search, satu filter pilihan, jumlah baris) + modal Others. */
class PohState(private val scope: CoroutineScope) {
    var data by mutableStateOf<PohData?>(null); private set
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    var company by mutableStateOf("SAG"); private set      // SAG = semua perusahaan
    var displayed by mutableStateOf(ApiConfig.PAGE_SIZE); private set

    /** Pencarian nomor project (parameter `search`). */
    var query by mutableStateOf(""); private set

    /** Satu filter pilihan aktif: kind = bucket | status | sales | pic. Menekan lagi = lepas. */
    var fKind by mutableStateOf<String?>(null); private set
    var fValue by mutableStateOf<String?>(null); private set

    val bucket: String? get() = if (fKind == "bucket") fValue else null
    fun isSelected(kind: String, value: String) = fKind == kind && fValue == value

    /** Data pemilih (bucket, status, sales, PIC) tanpa filter pilihan, supaya angkanya tetap utuh saat salah satu dipilih. */
    private var baseBuckets by mutableStateOf<List<Bucket>>(emptyList())
    private var baseStatus by mutableStateOf<List<StatusCard>>(emptyList())
    private var baseSales by mutableStateOf<List<PersonRow>>(emptyList())
    private var basePics by mutableStateOf<List<PersonRow>>(emptyList())
    private var baseTotal by mutableStateOf(0)

    private var summary: SummaryData? = null
    private var job: Job? = null
    private var searchJob: Job? = null

    /** Summary untuk ditampilkan: pemilih memakai data dasar (tanpa filter) selama ada filter aktif. */
    fun view(s: SummaryData): SummaryData =
        if (fKind != null && baseTotal > 0)
            s.copy(buckets = baseBuckets, statusCards = baseStatus, sales = baseSales, pics = basePics)
        else s

    /** Total project tanpa filter (untuk persentase tiga kotak jadwal). */
    fun totalForPercent(s: SummaryData) = if (baseTotal > 0) baseTotal else s.total.count

    /** Parameter filter aktif; dipakai tabel, kartu, dan modal Others agar scope-nya sama. */
    private fun filterParams(): List<Pair<String, String>> = buildList {
        if (company != "SAG") add("company[]" to company)
        val k = fKind; val v = fValue
        if (k != null && v != null) add((when (k) { "bucket" -> "bucket[]"; "status" -> "status[]"; "sales" -> "sales[]"; else -> "pic[]" }) to v)
        if (query.isNotBlank()) add("search" to query.trim())
    }

    /** withSummary=false untuk paging tabel saja (lebih ringan; kartu tetap memakai data terakhir). */
    fun refresh(withSummary: Boolean = true) {
        job?.cancel()
        job = scope.launch {
            loading = true
            try {
                val kind = fKind
                val params = buildList {
                    add("total_displayed" to displayed.toString())
                    addAll(filterParams())
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

    private fun resetAndRefresh() { fKind = null; fValue = null; displayed = ApiConfig.PAGE_SIZE; refresh(true) }

    fun selectCompany(key: String) {
        if (company == key) return
        company = key; resetAndRefresh()
    }

    /** Ketik di kotak search: dikirim ke server setelah jeda singkat. */
    fun onQuery(q: String) {
        query = q
        searchJob?.cancel()
        searchJob = scope.launch { delay(600); resetAndRefresh() }
    }

    /** Tombol Search di keyboard: langsung cari. */
    fun searchNow() { searchJob?.cancel(); resetAndRefresh() }

    /** Tekan sekali = filter nilai tersebut; tekan lagi = lepas filter. Memilih yang lain menggantikan filter sebelumnya. */
    fun toggle(kind: String, value: String) {
        if (isSelected(kind, value)) { fKind = null; fValue = null } else { fKind = kind; fValue = value }
        displayed = ApiConfig.PAGE_SIZE; refresh(true)
    }

    fun toggleBucket(key: String) = toggle("bucket", key)

    fun showMore() { displayed = minOf(displayed + ApiConfig.PAGE_SIZE, ApiConfig.MAX_ROWS); refresh(false) }
    fun showLess() { displayed = maxOf(displayed - ApiConfig.PAGE_SIZE, ApiConfig.PAGE_SIZE); refresh(false) }
    fun showAll() { displayed = minOf(data?.total ?: ApiConfig.MAX_ROWS, ApiConfig.MAX_ROWS); refresh(false) }

    // ---------- modal Others ----------
    var othersKey by mutableStateOf<String?>(null); private set
    var othersTitle by mutableStateOf(""); private set
    var othersQuery by mutableStateOf(""); private set
    var othersRows by mutableStateOf<List<SRowData>>(emptyList()); private set
    var othersTotal by mutableStateOf(0); private set
    var othersHasMore by mutableStateOf(false); private set
    var othersLoading by mutableStateOf(false); private set
    var othersError by mutableStateOf<String?>(null); private set
    private var othersJob: Job? = null
    private var othersSearchJob: Job? = null

    fun openOthers(key: String, title: String) {
        othersKey = key; othersTitle = title; othersQuery = ""
        othersRows = emptyList(); othersTotal = 0; othersHasMore = false; othersError = null
        loadOthers(0)
    }

    fun closeOthers() { othersJob?.cancel(); othersSearchJob?.cancel(); othersKey = null; othersLoading = false }

    fun onOthersQuery(q: String) {
        othersQuery = q
        othersSearchJob?.cancel()
        othersSearchJob = scope.launch { delay(500); loadOthers(0) }
    }

    fun othersMore() = loadOthers(othersRows.size)

    private fun loadOthers(offset: Int) {
        val key = othersKey ?: return
        othersJob?.cancel()
        othersJob = scope.launch {
            othersLoading = true
            try {
                val params = buildList {
                    add("section" to key); add("limit" to "10"); add("offset" to offset.toString())
                    if (othersQuery.isNotBlank()) add("q" to othersQuery.trim())
                    addAll(filterParams())
                }
                val r = PohApi.fetchOthers(params)
                othersRows = if (offset == 0) r.rows else othersRows + r.rows
                othersTotal = r.total; othersHasMore = r.hasMore; othersError = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                othersError = e.message ?: "Gagal memuat data"
            } finally {
                othersLoading = false
            }
        }
    }
}