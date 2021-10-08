package com.project.socially.adapters

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.project.socially.R
import com.project.socially.utilities.Utils
import com.project.socially.activities.EditCommentActivity
import com.project.socially.models.Comment


class CommentAdapter(options: FirestoreRecyclerOptions<Comment>, private val postId: String, private val listener: ICommentAdapter) :
    FirestoreRecyclerAdapter<Comment, CommentAdapter.CommentViewHolder>(options) {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentText: TextView = itemView.findViewById(R.id.commentText)
        val comment_userName: TextView = itemView.findViewById(R.id.comment_userName)
        val comment_createdAt: TextView = itemView.findViewById(R.id.comment_createdAt)
        val comment_likeCount: TextView = itemView.findViewById(R.id.comment_likeCount)
        val comment_dislikeCount: TextView = itemView.findViewById(R.id.comment_dislikeCount)
        val commentOptions: ImageView = itemView.findViewById(R.id.commentOptions)
        val comment_likeButton: ImageView = itemView.findViewById(R.id.comment_likeButton)
        val comment_dislikeButton: ImageView = itemView.findViewById(R.id.comment_dislikeButton)
        val comment_userImage: ImageView = itemView.findViewById(R.id.comment_userImage)
        val context: Context = itemView.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val viewHolder = CommentViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        )
        viewHolder.comment_likeButton.setOnClickListener {
            listener.oncommentLikeClicked(postId, snapshots.getSnapshot(viewHolder.bindingAdapterPosition).id)
//            listener.commentupdateEverything()
        }
        viewHolder.comment_dislikeButton.setOnClickListener {
            listener.oncommentdisLikeClicked(postId, snapshots.getSnapshot(viewHolder.bindingAdapterPosition).id)
//            listener.commentupdateEverything()
        }
        return viewHolder
    }

    private fun deleteComment(position: Int) {
        snapshots.getSnapshot(position).reference.delete()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int, model: Comment) {

//        quick hack to fix messed up item views (but defeats the purpose of recycler view)
//        holder.setIsRecyclable(false)

        holder.comment_userName.text = model.createdBy.displayName
        Glide.with(holder.comment_userImage.context).load(model.createdBy.imageUrl).circleCrop()
            .into(holder.comment_userImage)
        holder.commentText.text = model.text
        holder.comment_createdAt.text = Utils.getTimeAgo(model.createdAt)
        holder.comment_likeCount.text = model.likedBy.size.toString()
        holder.comment_dislikeCount.text = model.dislikedBy.size.toString()

        val auth = Firebase.auth
        val currentUserId = auth.currentUser!!.uid

        val isLiked = model.likedBy.contains(currentUserId)
        val isdisLiked = model.dislikedBy.contains(currentUserId)

        if (isLiked) {
            holder.comment_likeButton.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.comment_likeButton.context,
                    R.drawable.ic_liked
                )
            )
        } else {
            holder.comment_likeButton.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.comment_likeButton.context,
                    R.drawable.ic_unliked
                )
            )
        }

        if (isdisLiked) {
            holder.comment_dislikeButton.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.comment_likeButton.context,
                    R.drawable.ic_disliked
                )
            )
        } else {
            holder.comment_dislikeButton.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.comment_likeButton.context,
                    R.drawable.ic_undisliked
                )
            )
        }

        if (model.createdBy.uid == currentUserId) {
            holder.commentOptions.visibility = View.VISIBLE
            holder.commentOptions.isClickable = true

        } else {
            holder.commentOptions.visibility = View.INVISIBLE
            holder.commentOptions.isClickable = false
        }

        holder.commentOptions.setOnClickListener {
            val popupMenu = PopupMenu(holder.context, holder.commentOptions)
            popupMenu.inflate(R.menu.comment_menu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.edit_comment -> {
                        val intent = Intent(holder.context, EditCommentActivity::class.java)
                        intent.putExtra("postId", postId)
                        intent.putExtra("commentId", snapshots.getSnapshot(position).id)
                        intent.putExtra("oldComment", holder.commentText.text.toString())
                        holder.context.startActivity(intent)
                        true
                    }
                    R.id.delete_comment -> {
                        val builder = AlertDialog.Builder(holder.context)
                        builder.setMessage("Are you sure you want to delete this Comment?")
                            .setCancelable(false)
                            .setPositiveButton("Yes") { _, _ ->
                                try {
                                    deleteComment(position)
                                    Toast.makeText(
                                        holder.context,
                                        "Comment Deleted!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        holder.context,
                                        "Exception: $e",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .setNegativeButton("No") { dialog, _ ->
                                dialog.dismiss()
                            }
                        val alert = builder.create()
                        alert.show()
                        true
                    }
                    else -> true
                }
            }
            popupMenu.setForceShowIcon(true)
            popupMenu.show()
        }

    }

}

interface ICommentAdapter {
    fun commentupdateEverything()
    fun oncommentLikeClicked(postId: String, commentId: String)
    fun oncommentdisLikeClicked(postId: String, commentId: String)
}
