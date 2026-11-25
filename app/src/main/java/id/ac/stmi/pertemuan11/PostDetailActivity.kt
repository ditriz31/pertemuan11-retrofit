package id.ac.stmi.pertemuan11

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.ac.stmi.pertemuan11.data.api.RetrofitClient
import id.ac.stmi.pertemuan11.data.model.Post
import id.ac.stmi.pertemuan11.data.model.PostRequest
import id.ac.stmi.pertemuan11.databinding.ActivityPostDetailBinding
import id.ac.stmi.pertemuan11.databinding.DialogPatchBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class PostDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_POST_ID = "extra_post_id"
    }

    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var postId: String
    private var currentPost: Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val id = intent.getStringExtra(EXTRA_POST_ID)
        if (id == null) {
            finish()
            return
        }
        postId = id

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnRetry.setOnClickListener { loadPost() }
        binding.btnEdit.setOnClickListener { openEditForm() }
        binding.btnPatch.setOnClickListener { showPatchDialog() }
        binding.btnDelete.setOnClickListener { confirmDelete() }
    }

    // Muat ulang setiap kembali dari form edit (PUT).
    override fun onResume() {
        super.onResume()
        loadPost()
    }

    // GET /posts/{id}
    private fun loadPost() {
        showLoading()
        lifecycleScope.launch {
            try {
                val post = RetrofitClient.apiService.getPostById(postId)
                currentPost = post
                binding.tvId.text = getString(R.string.label_id, post.id)
                binding.tvTitle.text = post.title
                binding.tvBody.text = post.body
                showContent()
            } catch (e: IOException) {
                showError(getString(R.string.error_network))
            } catch (e: HttpException) {
                // 404 = post sudah dihapus atau id salah
                showError(getString(R.string.error_http, e.code()))
            }
        }
    }

    // PUT: buka form dalam mode EDIT, kirim title & body sekaligus.
    private fun openEditForm() {
        val post = currentPost ?: return
        val intent = Intent(this, PostFormActivity::class.java).apply {
            putExtra(PostFormActivity.EXTRA_POST_ID, post.id)
            putExtra(PostFormActivity.EXTRA_TITLE, post.title)
            putExtra(PostFormActivity.EXTRA_BODY, post.body)
        }
        startActivity(intent)
    }

    // PATCH: hanya title yang dikirim, body di server tetap utuh.
    private fun showPatchDialog() {
        val post = currentPost ?: return
        val dialogBinding = DialogPatchBinding.inflate(layoutInflater)
        dialogBinding.etTitle.setText(post.title)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_patch_title)
            .setMessage(R.string.dialog_patch_message)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_send) { _, _ ->
                val newTitle = dialogBinding.etTitle.text.toString().trim()
                if (newTitle.isEmpty()) {
                    Toast.makeText(this, R.string.error_title_required, Toast.LENGTH_SHORT).show()
                } else {
                    patchTitle(newTitle)
                }
            }
            .show()
    }

    private fun patchTitle(newTitle: String) {
        setButtonsEnabled(false)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.patchPost(postId, PostRequest(title = newTitle))
                val (code, json) = response.describe()
                showApiResultDialog("PATCH", code, json) { loadPost() }
            } catch (e: IOException) {
                Toast.makeText(this@PostDetailActivity, R.string.error_network, Toast.LENGTH_LONG).show()
            } finally {
                setButtonsEnabled(true)
            }
        }
    }

    // DELETE: minta konfirmasi dulu, lalu kembali ke daftar.
    private fun confirmDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(R.string.dialog_delete_message)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ -> deletePost() }
            .show()
    }

    private fun deletePost() {
        setButtonsEnabled(false)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deletePost(postId)
                val (code, json) = response.describe()
                showApiResultDialog("DELETE", code, json) {
                    if (response.isSuccessful) finish()
                }
            } catch (e: IOException) {
                Toast.makeText(this@PostDetailActivity, R.string.error_network, Toast.LENGTH_LONG).show()
            } finally {
                setButtonsEnabled(true)
            }
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.btnEdit.isEnabled = enabled
        binding.btnPatch.isEnabled = enabled
        binding.btnDelete.isEnabled = enabled
        binding.progressBar.visibility = if (enabled) View.GONE else View.VISIBLE
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutState.visibility = View.GONE
        binding.layoutContent.visibility = View.GONE
    }

    private fun showContent() {
        binding.progressBar.visibility = View.GONE
        binding.layoutState.visibility = View.GONE
        binding.layoutContent.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.layoutContent.visibility = View.GONE
        binding.layoutState.visibility = View.VISIBLE
        binding.tvState.text = message
    }
}
