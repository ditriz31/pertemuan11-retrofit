package id.ac.stmi.pertemuan11

import android.content.Context
import android.graphics.Typeface
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import retrofit2.Response

private val prettyGson = GsonBuilder().setPrettyPrinting().create()

// Objek Kotlin -> JSON yang rapi untuk ditampilkan di dialog.
fun toPrettyJson(value: Any?): String = prettyGson.toJson(value)

// Teks JSON mentah (misalnya balasan DELETE) -> JSON yang rapi.
// Kalau ternyata bukan JSON, tampilkan apa adanya.
fun prettyJsonString(raw: String?): String {
    if (raw.isNullOrBlank()) return "(body kosong)"
    return try {
        prettyGson.toJson(JsonParser.parseString(raw))
    } catch (e: Exception) {
        raw
    }
}

// Ringkasan isi Response<T>: status code + body (sukses) atau error body (gagal).
fun <T> Response<T>.describe(): Pair<Int, String> {
    val json = if (isSuccessful) {
        val data = body()
        if (data is okhttp3.ResponseBody) prettyJsonString(data.string()) else toPrettyJson(data)
    } else {
        prettyJsonString(errorBody()?.string())
    }
    return code() to json
}

// Dialog konfirmasi tiap aksi: status code + JSON respons.
fun Context.showApiResultDialog(
    title: String,
    statusCode: Int,
    json: String,
    onDismiss: () -> Unit = {}
) {
    val padding = (20 * resources.displayMetrics.density).toInt()
    val textView = TextView(this).apply {
        text = json
        typeface = Typeface.MONOSPACE
        textSize = 13f
        setTextIsSelectable(true)
        setPadding(padding, padding / 2, padding, 0)
    }
    val scrollView = ScrollView(this).apply { addView(textView) }

    MaterialAlertDialogBuilder(this)
        .setTitle(getString(R.string.dialog_result_title, title, statusCode))
        .setView(scrollView)
        .setPositiveButton(android.R.string.ok, null)
        .setOnDismissListener { onDismiss() }
        .show()
}
