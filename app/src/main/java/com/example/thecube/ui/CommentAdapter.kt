package com.example.thecube.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.thecube.R
import com.example.thecube.model.Comment
import com.squareup.picasso.Picasso

class CommentAdapter(private var comments: List<Comment> = emptyList())
    : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    fun updateComments(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun getItemCount(): Int = comments.size

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        Log.d("CommentAdapter", "Binding comment at pos $position: ${comment.comment}")
        holder.bind(comment)
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val commentText: TextView = itemView.findViewById(R.id.textViewComment)
        private val userText: TextView = itemView.findViewById(R.id.textViewCommentUser)
        private val timeText: TextView = itemView.findViewById(R.id.textViewCommentTime)
        private val userImageView: ImageView = itemView.findViewById(R.id.imageViewUserProfile)

        fun bind(comment: Comment) {
            commentText.text = comment.comment
            userText.text = comment.userName
            val dateFormatted = android.text.format.DateFormat.format("MM/dd/yyyy HH:mm", comment.timeStamp)
            timeText.text = dateFormatted

            // If comment has a userImageUrl, load it. Otherwise use a placeholder.
            if (comment.userImageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(comment.userImageUrl)
                    .placeholder(R.drawable.person_icon)
                    .error(R.drawable.person_icon)
                    .into(userImageView)
            } else {
                userImageView.setImageResource(R.drawable.person_icon)
            }
        }
    }
}