package com.example.chat

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class MessagesAdapter(var messageData: MutableList<Message>) : RecyclerView.Adapter<MessagesAdapter.ViewHolder>(){
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messageData[position]
        holder.bind(message)
         }


    class ViewHolder( var view: View) : RecyclerView.ViewHolder(view){
        private var name: TextView
        private  var photoUrl: ImageView
        private var text :TextView
        private var profileImage : ImageView


        init {
            name = view.findViewById(R.id.nameTextView)
            text = view.findViewById(R.id.messageTextView)
            photoUrl = view.findViewById(R.id.photoImageView)
            profileImage = view.findViewById(R.id.profile_image)
        }

        fun bind(message: Message) {
            Glide.with(view.context)
                .load(R.drawable.ic_defult)
                //.error(R.drawable.ic_defult)
               // .centerCrop()
                .into(profileImage)
            name?.text = message.name
            text?.text = message.text
            val isPhoto = message!!.photoUrl != null
            val isText = message!!.text==null
            if(isText){
             text?.visibility = View.GONE
            } else {
                text?.visibility =View.VISIBLE
            }
            if (isPhoto) {
                Glide.with(view.context)
                    .load(message.photoUrl)
                        // todo sizing
                    .override(500, 500) // resizes the image to these dimensions (in pixel)
                    .centerCrop()
                    .into(photoUrl)
            }
        }
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesAdapter.ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)

        return ViewHolder(view)
    }



    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = messageData.size
}