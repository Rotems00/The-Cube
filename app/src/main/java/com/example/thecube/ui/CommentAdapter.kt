package com.example.thecube.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.thecube.R
import com.example.thecube.model.Comment

class CommentAdapter(private var comments: List<Comment> = emptyList()) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    fun updateComments(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged() // For smoother updates, you could implement DiffUtil.
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun getItemCount(): Int = comments.size

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val commentText: TextView = itemView.findViewById(R.id.textViewComment)
        private val userText: TextView = itemView.findViewById(R.id.textViewCommentUser)
        private val timeText: TextView = itemView.findViewById(R.id.textViewCommentTime)

        fun bind(comment: Comment) {
            commentText.text = comment.comment
            // Display the commenter's username
            userText.text = comment.userName
            timeText.text = android.text.format.DateFormat.format("MM/dd/yyyy HH:mm", comment.timeStamp)
        }
    }

}
