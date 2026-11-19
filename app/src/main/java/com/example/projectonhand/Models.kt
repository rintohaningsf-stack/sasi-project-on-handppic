package com.example.projectonhand

import org.json.JSONArray
import org.json.JSONObject

data class Totals(val count: Int = 0, val item: Int = 0, val unit: Int = 0)
data class StatusCard(val value: Int, val label: String, val count: Int, val item: Int, val unit: Int)
data class Bucket(val key: String, val label: String, val count: Int)
data class PersonRow(val label: String, val image: String?, val count: Int)
data class Pill(val key: String, val label: String, val count: Int)

/** Baris generik untuk 6 kartu summary (field yang tidak dipakai section tertentu = default). */
data class SRowData(
    val name: String, val code: String, val image: String?, val doc: String, val dueDate: String,
    val days: Int, val count: Int, val item: Int, val unit: Int, val color: String
)

data class SummaryCard(val key: String, val title: String, val total: Int, val otherCount: Int, val rows: List<SRowData>)

data class SummaryData(
    val total: Totals,
    val buckets: List<Bucket>,
    val statusCards: List<StatusCard>,
    val sales: List<PersonRow>,
    val pics: List<PersonRow>,
    val cards: List<SummaryCard>
) {
    companion object { val EMPTY = SummaryData(Totals(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList()) }
}

data class TRow(
    val id: Int, val doc: String, val customer: String, val companyImage: String?, val typeColor: String,
    val itemName: String, val itemCode: String, val itemPicture: String?, val itemExtra: Int,
    val orderDate: String, val dueLabel: String, val isOverdue: Boolean, val leadTime: Int, val daysFromToday: Int,
    val sales: String, val salesImage: String?,
    val rawPct: Int, val rawDone: Int, val rawTotal: Int, val wipGood: Int, val wipTotal: Int,
    val startActual: String?, val startPlan: String?, val startActualColor: String,
    val finishActual: String?, val finishPlan: String?, val finishActualColor: String,
    val progress: Int, val pic: String, val picImage: String?,
    val statusName: String, val statusValue: Int
)

data class PohData(
    val periodeStart: String?, val periodeFinish: String?,
    val pills: List<Pill>, val summary: SummaryData?,
    val rows: List<TRow>, val total: Int
)

// ---------- parser ----------
private fun JSONObject.s(k: String): String? = if (isNull(k)) null else optString(k).ifEmpty { null }
private fun JSONObject.i(k: String): Int = if (isNull(k)) 0 else optInt(k)
private fun <T> JSONArray?.list(f: (JSONObject) -> T): List<T> {
    if (this == null) return emptyList()
    val out = ArrayList<T>(length())
    for (n in 0 until length()) optJSONObject(n)?.let { out.add(f(it)) }
    return out
}

object PohParser {
    fun parse(root: JSONObject): PohData {
        val per = root.optJSONObject("meta")?.optJSONObject("periode")
        val table = root.optJSONObject("table")
        val rows = table?.optJSONArray("rows").list(::row)
        return PohData(
            periodeStart = per?.s("start"), periodeFinish = per?.s("finish"),
            pills = root.optJSONArray("company_pills").list { Pill(it.s("key") ?: "", it.s("label") ?: "", it.i("count")) },
            summary = root.optJSONObject("summary")?.let(::summary),
            rows = rows,
            total = table?.i("total") ?: rows.size
        )
    }

    private fun totals(o: JSONObject?) = if (o == null) Totals() else Totals(o.i("count"), o.i("item"), o.i("unit"))

    private fun people(o: JSONObject?): List<PersonRow> =
        o?.optJSONArray("rows").list { PersonRow(it.s("label") ?: "-", it.s("image"), it.i("count")) }

    private fun summary(o: JSONObject) = SummaryData(
        total = totals(o.optJSONObject("total_projects")),
        buckets = o.optJSONArray("buckets").list { Bucket(it.s("key") ?: "", it.s("label") ?: "", it.i("count")) },
        statusCards = o.optJSONArray("status_cards").list { StatusCard(it.i("value"), it.s("label") ?: "", it.i("count"), it.i("item"), it.i("unit")) },
        sales = people(o.optJSONObject("sales_on_hand")),
        pics = people(o.optJSONObject("pic_on_hand")),
        cards = o.optJSONArray("cards").list { c ->
            SummaryCard(c.s("key") ?: "", c.s("title") ?: "", c.i("total"), c.i("other_count"), c.optJSONArray("rows").list(::srow))
        }
    )

    private fun srow(o: JSONObject) = SRowData(
        name = o.s("name") ?: o.s("label") ?: o.s("customer") ?: "-",
        code = o.s("code") ?: "",
        image = o.s("image"),
        doc = o.s("doc") ?: "",
        dueDate = o.s("due_date") ?: o.s("due_date_raw") ?: "",
        days = maxOf(o.i("days_late"), o.i("days_delay"), o.i("days_remaining")),
        count = o.i("count"), item = o.i("item"), unit = o.i("unit"),
        color = o.s("color") ?: ""
    )

    private fun row(o: JSONObject): TRow {
        val first = o.optJSONArray("items")?.optJSONObject(0) ?: o.optJSONArray("extra_items")?.optJSONObject(0)
        return TRow(
            id = o.i("id"), doc = o.s("doc") ?: o.s("number") ?: "", customer = o.s("customer") ?: "-",
            companyImage = o.s("company_image"), typeColor = o.s("type_color") ?: "green",
            itemName = first?.s("name") ?: "-", itemCode = first?.s("code") ?: "",
            itemPicture = first?.s("picture") ?: first?.s("image"), itemExtra = o.i("item_extra"),
            orderDate = o.s("order_date") ?: "-", dueLabel = o.s("due_date_label") ?: o.s("due_date") ?: "-",
            isOverdue = o.optBoolean("is_overdue", false), leadTime = o.i("lead_time_days"), daysFromToday = o.i("days_from_today"),
            sales = o.s("sales") ?: "-", salesImage = o.s("sales_image"),
            rawPct = o.i("raw_pct"), rawDone = o.i("raw_done_items"), rawTotal = o.i("raw_total_items"),
            wipGood = o.i("wip_good"), wipTotal = o.i("wip_total"),
            startActual = o.s("start_actual_label"), startPlan = o.s("start_plan_label"), startActualColor = o.s("start_actual_color") ?: "",
            finishActual = o.s("finish_actual_label"), finishPlan = o.s("finish_plan_label"), finishActualColor = o.s("finish_actual_color") ?: "",
            progress = o.i("process_pct"), pic = o.s("pic")?.trim()?.takeIf { it != "-" && it != "–" } ?: "", picImage = o.s("pic_image"),
            statusName = o.s("status_name") ?: "-", statusValue = o.i("status_value")
        )
    }
}