package com.project.socially.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
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
import com.project.socially.activities.CommentActivity
import com.project.socially.activities.EditPostActivity
import com.project.socially.R
import com.project.socially.activities.ImageFullscreenActivity
import com.project.socially.activities.ProfileActivity
import com.project.socially.utilities.Utils
import com.project.socially.models.Post


class PostAdapter(options: FirestoreRecyclerOptions<Post>, private val listener: IPostAdapter) :
    FirestoreRecyclerAdapter<Post, PostAdapter.PostViewHolder>(options) {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postText: TextView = itemView.findViewById(R.id.postText)
        val userName: TextView = itemView.findViewById(R.id.userName)
        val createdAt: TextView = itemView.findViewById(R.id.createdAt)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        val dislikeCount: TextView = itemView.findViewById(R.id.dislikeCount)

        //val commentCount: TextView = itemView.findViewById(R.id.commentCount)

        val postOptions: ImageView = itemView.findViewById(R.id.postOptns)
        val likeButton: ImageView = itemView.findViewById(R.id.likeButton)
        val dislikeButton: ImageView = itemView.findViewById(R.id.dislikeButton)
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val commentButton: ImageView = itemView.findViewById(R.id.commentButton)
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val context: Context = itemView.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val viewHolder = PostViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        )
        viewHolder.likeButton.setOnClickListener {
            listener.onLikeClicked(snapshots.getSnapshot(viewHolder.bindingAdapterPosition).id)
//            listener.updateEverything() [ Look at timer in MainActivity ]
        }
        viewHolder.dislikeButton.setOnClickListener {
            listener.ondisLikeClicked(snapshots.getSnapshot(viewHolder.bindingAdapterPosition).id)
//            listener.updateEverything() [ Look at timer in MainActivity ]
        }
        return viewHolder
    }

    private fun deletePost(position: Int) {
        snapshots.getSnapshot(position).reference.delete()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: Post) {

//        quick hack to fix messed up item views (but defeats the purpose of recycler view)
//        holder.setIsRecyclable(false)

        holder.userName.text = model.createdBy.displayName
        Glide.with(holder.userImage.context)
            .load(model.createdBy.imageUrl)
            .circleCrop()
            .into(holder.userImage)
        holder.postText.text = model.text
        holder.likeCount.text = model.likedBy.size.toString()
        holder.dislikeCount.text = model.dislikedBy.size.toString()
        holder.createdAt.text = Utils.getTimeAgo(model.createdAt)

        if (model.photoUrl.isNotEmpty()) {
            holder.postImage.visibility = View.VISIBLE
            holder.postImage.isClickable = true
            Glide.with(holder.postImage.context)
                .load(model.photoUrl)
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.postImage)
        } else {
            holder.postImage.visibility = View.GONE
            holder.postImage.isClickable = false
        }

        //holder.commentCount.text = model.commentCount.toString()

        val auth = Firebase.auth
        val currentUserId = auth.currentUser!!.uid

        val isLiked = model.likedBy.contains(currentUserId)
        val isdisLiked = model.dislikedBy.contains(currentUserId)

        if (isLiked) {
            holder.likeButton.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.likeButton.context,
                    R.drawable.ic_liked
                )
            )
        } else {
            holder.likeButton.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.likeButton.context,
                    R.drawable.ic_unliked
                )
            )
        }

        if (isdisLiked) {
            holder.dislikeButton.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.likeButton.context,
                    R.drawable.ic_disliked
                )
            )
        } else {
            holder.dislikeButton.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.likeButton.context,
                    R.drawable.ic_undisliked
                )
            )
        }

        holder.postImage.setOnClickListener {
//            Toast.makeText(holder.context, "Image $position", Toast.LENGTH_SHORT).show()
            val i = Intent(holder.context, ImageFullscreenActivity::class.java)
            i.putExtra("imgUrl", model.photoUrl)
            holder.context.startActivity(i)
        }

        holder.commentButton.setOnClickListener {
            val i = Intent(holder.context, CommentActivity::class.java)
            i.putExtra("postId", snapshots.getSnapshot(position).id)
            holder.context.startActivity(i)
        }

        holder.userImage.setOnClickListener {
            val i = Intent(holder.context, ProfileActivity::class.java)
            i.putExtra("userId", model.createdBy.uid)
            holder.context.startActivity(i)
        }

        if (model.createdBy.uid == currentUserId) {
            holder.postOptions.visibility = View.VISIBLE
            holder.postOptions.isClickable = true

        } else {
            holder.postOptions.visibility = View.INVISIBLE
            holder.postOptions.isClickable = false
        }

        holder.postOptions.setOnClickListener {
            val popupMenu = PopupMenu(holder.context, holder.postOptions)
            popupMenu.inflate(R.menu.post_menu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.edit_post -> {
                        Log.d(
                            "Number",
                            snapshots.getSnapshot(position).get("text").toString()
                        )
                        val intent = Intent(holder.context, EditPostActivity::class.java)
                        intent.putExtra("postId", snapshots.getSnapshot(position).id)
                        intent.putExtra(
                            "postText",
                            snapshots.getSnapshot(position).get("text").toString()
                        )
                        holder.context.startActivity(intent)
                        true
                    }
                    R.id.delete_post -> {
                        val builder = AlertDialog.Builder(holder.context)
                        builder.setMessage("Are you sure you want to delete this Post?")
                            .setCancelable(false)
                            .setPositiveButton("Yes") { _, _ ->
                                try {
                                    deletePost(position)
                                    Toast.makeText(
                                        holder.context,
                                        "Post Deleted!",
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

interface IPostAdapter {
    fun updateEverything()
    fun onLikeClicked(postId: String)
    fun ondisLikeClicked(postId: String)
}