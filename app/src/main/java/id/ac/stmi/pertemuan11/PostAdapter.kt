package id.ac.stmi.pertemuan11

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import id.ac.stmi.pertemuan11.data.model.Post
import id.ac.stmi.pertemuan11.databinding.ItemPostBinding

class PostAdapter : BaseAdapter() {

    private val posts = mutableListOf<Post>()

    fun updateData(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = posts.size

    override fun getItem(position: Int): Post = posts[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // convertView + tag: pakai ulang baris yang sudah ada supaya hemat memori.
        val binding = if (convertView == null) {
            ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false).also {
                it.root.tag = it
            }
        } else {
            convertView.tag as ItemPostBinding
        }

        val post = getItem(position)
        binding.tvId.text = parent.context.getString(R.string.label_id, post.id)
        binding.tvTitle.text = post.title
        binding.tvBody.text = post.body
        return binding.root
    }
}
