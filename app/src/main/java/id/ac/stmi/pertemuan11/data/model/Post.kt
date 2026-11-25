package id.ac.stmi.pertemuan11.data.model

// Model respons. Nama properti harus sama persis dengan kunci JSON dari MockAPI.
// id di MockAPI bertipe String, bukan angka.
data class Post(
    val id: String,
    val title: String,
    val body: String
)
