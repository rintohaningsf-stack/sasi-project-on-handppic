package com.example.projectonhand

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** State dashboard: data server + filter yang sedang aktif (company, jumlah baris). */
class PohState(private val scope: CoroutineScope) {
    var data by mutableStateOf<PohData?>(null); private set
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    var company by mutableStateOf("SAG"); private set      // SAG = semua perusahaan
    var displayed by mutableStateOf(ApiConfig.PAGE_SIZE); private set

    private var summary: SummaryData? = null
    private var job: Job? = null

    /** withSummary=false untuk paging tabel saja (lebih ringan; kartu tetap memakai data terakhir). */
    fun refresh(withSummary: Boolean = true) {
        job?.cancel()
        job = scope.launch {
            loading = true
            try {
                val params = buildList {
                    add("total_displayed" to displayed.toString())
                    if (company != "SAG") add("company[]" to company)
                    if (!withSummary) add("summary" to "0")
                }
                val r = PohApi.fetchList(params)
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
        company = key; displayed = ApiConfig.PAGE_SIZE; refresh(true)
    }

    fun showMore() { displayed = minOf(displayed + ApiConfig.PAGE_SIZE, ApiConfig.MAX_ROWS); refresh(false) }
    fun showLess() { displayed = maxOf(displayed - ApiConfig.PAGE_SIZE, ApiConfig.PAGE_SIZE); refresh(false) }
    fun showAll() { displayed = minOf(data?.total ?: ApiConfig.MAX_ROWS, ApiConfig.MAX_ROWS); refresh(false) }
}