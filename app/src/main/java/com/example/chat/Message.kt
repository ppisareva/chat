package com.example.chat

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
 class Message(

    var text: String? = null,
    var name: String? = null,
    var photoUrl: String? = null,
    var userProfileUrl: String? = null


)