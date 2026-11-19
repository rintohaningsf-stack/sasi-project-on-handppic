package com.example.projectonhand

/**
 * Konfigurasi API. Isi USERNAME/PASSWORD dengan akun yang punya akses PPIC.
 * Disarankan dipindah ke BuildConfig / local.properties agar tidak ikut ter-commit.
 */
object ApiConfig {
    const val BASE_URL = "https://app.sasisolo-server.com"
    const val USERNAME = "Giyanto"
    const val PASSWORD = "Ppic-0116"

    /** Jumlah baris awal tabel (desain: 10). Server membatasi maksimum 500. */
    const val PAGE_SIZE = 10
    const val MAX_ROWS = 500

    /** Auto refresh dashboard (ms). Rate limit server: 60 request/menit. */
    const val REFRESH_MS = 120_000L
}