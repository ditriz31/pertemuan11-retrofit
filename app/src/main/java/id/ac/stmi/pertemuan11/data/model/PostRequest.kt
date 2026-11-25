package id.ac.stmi.pertemuan11.data.model

// Model request. Field yang null tidak ikut dikirim oleh Gson,
// sehingga PATCH bisa mengubah sebagian data saja.
data class PostRequest(
    val title: String? = null,
    val body: String? = null
)
