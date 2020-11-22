package com.emergency.sosalert.firebaseMessaging

import android.app.*
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.emergency.sosalert.R
import com.emergency.sosalert.admin.AdminContainer
import com.emergency.sosalert.chat.ChatDetails
import com.emergency.sosalert.chat.SearchFriend
import com.emergency.sosalert.discussion.Discussion
import com.emergency.sosalert.discussion.DiscussionDetails
import com.emergency.sosalert.discussion.MyPost
import com.emergency.sosalert.locationTracking.TrackingRequest
import com.emergency.sosalert.main.Sos
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random


private const val CHANNEL_ID = "my_channel"

class FirebaseService : FirebaseMessagingService() {

    companion object {
        var sharedPref: SharedPreferences? = null

        var token: String?
            get() {
                return sharedPref?.getString("token", "")
            }
            set(value) {
                sharedPref?.edit()?.putString("token", value)?.apply()
            }
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        token = newToken
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.e(TAG, "MESSAGE RECEIVED")
        super.onMessageReceived(message)
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val notifSosAllow = pref.getBoolean("enable_notification_sos", false)
        val notifChatAllow = pref.getBoolean("enable_notification_chat", false)
        val notifCommentAllow = pref.getBoolean("enable_notification_comment", false)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val la = message.data["latitude"].toString().toDouble()
        val longi = message.data["longitude"].toString().toDouble()
        val picurl = message.data["image"].toString()

        lateinit var notification: Notification

        if (la == 999.0 && longi == 0.0 && notifChatAllow) {
            Log.e(TAG, "MESSAGE NOTIF")
            val titleArr = message.data["title"]!!.split("|")
            val chatIntent =
                Intent(this, ChatDetails::class.java).putExtra("chatgroupid", titleArr[1])
            val pendingChatIntent = TaskStackBuilder.create(this).run {
                addNextIntentWithParentStack(chatIntent)
                getPendingIntent(1, PendingIntent.FLAG_ONE_SHOT)
            }
            val bitmap = getBitmapfromUrl(picurl)
            notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(titleArr[0])
                .setContentText(message.data["message"])
                .setSmallIcon(R.drawable.logo_sos)
                .setLargeIcon(getCircleBitmap(bitmap))
                .setContentIntent(pendingChatIntent)
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(message.data["message"])
                        .setBigContentTitle(titleArr[0])
                )
                .setAutoCancel(true)
                .setOngoing(false)
                .build()
            if (!isAppForeground(this)) {
                notificationManager.notify(notificationID, notification)
            }
        } else if (la == 888.0 && longi == 0.0) {
            Log.e(TAG, "MESSAGE TRACK REQUEST")
            val titleArr = message.data["title"]!!.split("|")
            val permissionIntent =
                Intent(this, TrackingRequest::class.java).putExtra("toallowid", titleArr[1])
            val pendingChatIntent = TaskStackBuilder.create(this).run {
                addNextIntentWithParentStack(permissionIntent)
                getPendingIntent(2, PendingIntent.FLAG_ONE_SHOT)
            }
            val bitmap = getBitmapfromUrl(picurl)
            notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(titleArr[0])
                .setContentText(message.data["message"])
                .setSmallIcon(R.drawable.logo_sos)
                .setLargeIcon(getCircleBitmap(bitmap))
                .setContentIntent(pendingChatIntent)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("${message.data["message"]}\nPress into the notification to accept or decline.")
                        .setBigContentTitle(titleArr[0])
                )
                .setAutoCancel(true)
                .setOngoing(false)
                .build()
            notificationManager.notify(notificationID, notification)
        } else if (la == 222.0 && longi == 222.0) {
            Log.e(TAG, "APPROVAL REQUEST")
            val toAdminPageIntent = Intent(this, AdminContainer::class.java)
            val pendingToAdminIntent = TaskStackBuilder.create(this).run {
                addNextIntentWithParentStack(toAdminPageIntent)
                getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT)
            }

            notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(message.data["title"])
                .setContentText(message.data["message"])
                .setSmallIcon(R.drawable.logo_sos)
                .setContentIntent(pendingToAdminIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .build()
            notificationManager.notify(notificationID, notification)
        } else if (la == 125.0 && longi == 125.0) {
            Log.e(TAG, "DECLINE POST")
            val toMyPostIntent = Intent(this, MyPost::class.java)
            val pendingToMyPostIntent = TaskStackBuilder.create(this).run {
                addNextIntentWithParentStack(toMyPostIntent)
                getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT)
            }

            notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(message.data["title"])
                .setContentText(message.data["message"])
                .setContentIntent(pendingToMyPostIntent)
                .setOngoing(false)
                .setSmallIcon(R.drawable.logo_sos)
                .build()
            notificationManager.notify(notificationID, notification)
        } else if (la == 5000.0 && longi == 0.0) {
            Log.e(TAG, "Reject Notif")
            val rejectIntent =
                Intent(this, Sos::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, rejectIntent,
                PendingIntent.FLAG_ONE_SHOT
            )
            notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(message.data["title"])
                .setContentText(message.data["message"])
                .setSmallIcon(R.drawable.logo_sos)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                .build()
            notificationManager.notify(notificationID, notification)

        } else if (la == 12344.0 && longi == 43211.0 && notifCommentAllow) {
            Log.e(TAG, "COMMENT NOTIF")
            val titleArr = message.data["title"]!!.split("|")

            FirebaseFirestore.getInstance().collection("discussion").document(titleArr[1]).get()
                .addOnSuccessListener {
                    val discussionIntent = Intent(this, DiscussionDetails::class.java).putExtra(
                        "discussiondetails",
                        it.toObject(Discussion::class.java)
                    )
                    val discussionPending = PendingIntent.getActivity(
                        this, 0, discussionIntent,
                        PendingIntent.FLAG_ONE_SHOT
                    )
                    notification = NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle(titleArr[0])
                        .setContentText(message.data["message"])
                        .setSmallIcon(R.drawable.logo_sos)
                        .setAutoCancel(true)
                        .setContentIntent(discussionPending)
                        .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .build()
                    notificationManager.notify(notificationID, notification)
                }
        } else if (notifSosAllow) {
            Log.e(TAG, "SOS NOTIF")

            val intentMaps = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("geo:$la,$longi?q=$la,$longi")
            ).also {
                FirebaseFirestore.getInstance().collection("report").document("count").get()
                    .addOnSuccessListener {
                        var count = it.get("intentpress").toString().toInt()
                        count += 1
                        FirebaseFirestore.getInstance().collection("report").document("count")
                            .update("intentpress", count)
                    }
            }

            val pendingIntent = PendingIntent.getActivity(
                this, 0, intentMaps,
                PendingIntent.FLAG_ONE_SHOT
            )
            val titleArr = message.data["title"]!!.split(",")
            val bitmap = getBitmapfromUrl(picurl)

            val findFriendIntent =
                Intent(this, SearchFriend::class.java).putExtra("predetermine", titleArr[1])
            val findFriendPendingIntent = TaskStackBuilder.create(this).run {
                addNextIntentWithParentStack(findFriendIntent)
                getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT)
            }

            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${titleArr[2]}"))
            val callIntentPendingIntent = TaskStackBuilder.create(this).run {
                addNextIntent(callIntent)
                getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT)
            }

            val rejectIntent = Intent("SOSRejectService")
            rejectIntent.setClass(this, SOSRejectReceiver::class.java)
            val b = Bundle()
            b.putBoolean("reject", true)
            b.putInt("notificationid", notificationID)
            b.putString("senderUid", titleArr[1])
            rejectIntent.putExtras(b)
            val rejectPendingIntent =
                PendingIntent.getBroadcast(this, 0, rejectIntent, PendingIntent.FLAG_ONE_SHOT)

            notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(titleArr[0])
                .setLargeIcon(bitmap)
                .setContentText(message.data["message"])
                .setSmallIcon(R.drawable.logo_sos)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(
                    NotificationCompat
                        .BigPictureStyle().bigPicture(bitmap)
                )
                .addAction(0, "Message", findFriendPendingIntent)
                .addAction(0, "Call", callIntentPendingIntent)
                .addAction(0, "Reject Help", rejectPendingIntent)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                .build()
            notificationManager.notify(notificationID, notification)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "SOSAlert"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {
            description = "SOSAlert Notification Channel"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun getBitmapfromUrl(imageUrl: String): Bitmap {
        try {
            val url = URL(imageUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            return BitmapFactory.decodeStream(input)

        } catch (e: Exception) {
            throw Exception(e)
        }
    }

    private fun getCircleBitmap(bitmap: Bitmap): Bitmap? {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val color = Color.RED
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawOval(rectF, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        bitmap.recycle()
        return output
    }

    private fun isAppForeground(context: Context): Boolean {
        val mActivityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val l = mActivityManager.runningAppProcesses
        for (info in l) {
            if (info.uid == context.applicationInfo.uid && info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true
            }
        }
        return false
    }

}