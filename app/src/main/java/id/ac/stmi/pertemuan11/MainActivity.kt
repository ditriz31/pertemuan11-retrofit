package id.ac.stmi.pertemuan11

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import id.ac.stmi.pertemuan11.data.api.RetrofitClient
import id.ac.stmi.pertemuan11.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val postAdapter = PostAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.listPosts.adapter = postAdapter
        binding.listPosts.setOnItemClickListener { _, _, position, _ ->
            val post = postAdapter.getItem(position)
            val intent = Intent(this, PostDetailActivity::class.java)
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.id)
            startActivity(intent)
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, PostFormActivity::class.java))
        }
        binding.btnRetry.setOnClickListener { loadPosts() }
    }

    // Muat ulang setiap kembali ke layar ini (setelah POST, PUT, PATCH, atau DELETE).
    override fun onResume() {
        super.onResume()
        loadPosts()
    }

    private fun loadPosts() {
        showLoading()
        lifecycleScope.launch {
            try {
                val posts = RetrofitClient.apiService.getPosts()
                postAdapter.updateData(posts)
                if (posts.isEmpty()) {
                    showMessage(getString(R.string.state_empty), showRetry = false)
                } else {
                    showContent()
                }
            } catch (e: IOException) {
                // tidak ada internet / server tidak bisa dihubungi
                showMessage(getString(R.string.error_network), showRetry = true)
            } catch (e: HttpException) {
                // 4xx / 5xx
                showMessage(getString(R.string.error_http, e.code()), showRetry = true)
            }
        }
    }

    // Tiga status layar: Loading, Konten, dan Pesan (kosong / error).
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutState.visibility = View.GONE
    }

    private fun showContent() {
        binding.progressBar.visibility = View.GONE
        binding.layoutState.visibility = View.GONE
        binding.listPosts.visibility = View.VISIBLE
    }

    private fun showMessage(message: String, showRetry: Boolean) {
        binding.progressBar.visibility = View.GONE
        binding.listPosts.visibility = View.GONE
        binding.layoutState.visibility = View.VISIBLE
        binding.tvState.text = message
        binding.btnRetry.visibility = if (showRetry) View.VISIBLE else View.GONE
    }
}
