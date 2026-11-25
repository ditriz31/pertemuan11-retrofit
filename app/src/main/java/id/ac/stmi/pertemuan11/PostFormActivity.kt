package id.ac.stmi.pertemuan11

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import id.ac.stmi.pertemuan11.data.api.RetrofitClient
import id.ac.stmi.pertemuan11.data.model.PostRequest
import id.ac.stmi.pertemuan11.databinding.ActivityPostFormBinding
import kotlinx.coroutines.launch
import java.io.IOException

// Satu activity, dua mode:
// - CREATE (tanpa EXTRA_POST_ID) -> POST /posts
// - EDIT   (dengan EXTRA_POST_ID) -> PUT /posts/{id}
class PostFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_POST_ID = "extra_post_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_BODY = "extra_body"
    }

    private lateinit var binding: ActivityPostFormBinding
    private var postId: String? = null

    private val isEditMode: Boolean
        get() = postId != null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPostFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        postId = intent.getStringExtra(EXTRA_POST_ID)
        binding.toolbar.setNavigationOnClickListener { finish() }

        if (isEditMode) {
            binding.toolbar.setTitle(R.string.title_edit_post)
            binding.btnSave.setText(R.string.action_save_put)
            if (savedInstanceState == null) {
                binding.etTitle.setText(intent.getStringExtra(EXTRA_TITLE))
                binding.etBody.setText(intent.getStringExtra(EXTRA_BODY))
            }
        } else {
            binding.toolbar.setTitle(R.string.title_create_post)
            binding.btnSave.setText(R.string.action_save_post)
        }

        binding.btnSave.setOnClickListener { submit() }
    }

    private fun submit() {
        val title = binding.etTitle.text.toString().trim()
        val body = binding.etBody.text.toString().trim()

        binding.tilTitle.error = if (title.isEmpty()) getString(R.string.error_title_required) else null
        binding.tilBody.error = if (body.isEmpty()) getString(R.string.error_body_required) else null
        if (title.isEmpty() || body.isEmpty()) return

        val request = PostRequest(title = title, body = body)
        setLoading(true)
        lifecycleScope.launch {
            try {
                val id = postId
                val response = if (id == null) {
                    RetrofitClient.apiService.createPost(request)      // 201 Created
                } else {
                    RetrofitClient.apiService.updatePost(id, request)  // 200 OK
                }
                val (code, json) = response.describe()
                val method = if (isEditMode) "PUT" else "POST"
                showApiResultDialog(method, code, json) {
                    if (response.isSuccessful) finish()
                }
            } catch (e: IOException) {
                Toast.makeText(this@PostFormActivity, R.string.error_network, Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !loading
        binding.etTitle.isEnabled = !loading
        binding.etBody.isEnabled = !loading
    }
}
