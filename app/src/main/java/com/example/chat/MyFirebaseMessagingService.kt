package com.example.chat

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

val GROUP_KEY_EMAILS = "group_key_emails"

class MyFirebaseMessagingService : FirebaseMessagingService() {
    val TAG = "Service"
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
      
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.d(TAG, "From: " + remoteMessage!!.from)
        Log.d(TAG, "Notification Message Body: " + remoteMessage.notification!!.body!!)
        sendNotification(remoteMessage)
        // sending notification to watch
        sendNotificationToWearable(remoteMessage)
    }

    private fun sendNotificationToWearable(remoteMessage: RemoteMessage) {


        // Build the notification, setting the group appropriately
        val notif = NotificationCompat.Builder(applicationContext)
            .setContentTitle("New mail from ${remoteMessage!!.from}")
            .setContentText(remoteMessage.notification!!.body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setGroup(GROUP_KEY_EMAILS)
            .build()

// Issue the notification
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(remoteMessage.messageId.hashCode(), notif)
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, getString(R.string.id) )
            .setContentText(remoteMessage.notification!!.body)
            .setAutoCancel(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}