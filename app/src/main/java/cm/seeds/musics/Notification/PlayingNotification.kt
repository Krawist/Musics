package cm.seeds.musics.Notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.annotation.RequiresApi
import cm.seeds.musics.PlayerService
import cm.seeds.musics.R


abstract class PlayingNotification(protected val service: PlayerService) : Notification() {

    companion object{
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "playing_notification"
    }

    private var notificationManager: NotificationManager? = null
    private var wasStopped = true
    protected var isPlaying = false

    abstract fun update()

    @Synchronized
    open fun stop() {
        wasStopped = true
        service.stopForeground(true)
        notificationManager!!.cancel(NOTIFICATION_ID)
    }

    @Synchronized
    fun init() {
        notificationManager =
            service.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    fun updateNotifyModeAndPostNotification(notification: Notification?) {
        //isPlaying = service.isPlaying()
        if (isPlaying && wasStopped) {
                service.startForeground(NOTIFICATION_ID, notification)
                wasStopped = false
        } else if(!wasStopped && !isPlaying){
            notificationManager!!.notify(NOTIFICATION_ID, notification)
            service.stopForeground(false)
            wasStopped = true
        }else if(isPlaying && !wasStopped){
            notificationManager!!.notify(NOTIFICATION_ID, notification)
        }
    }

    @RequiresApi(26)
    private fun createNotificationChannel() {
        var notificationChannel = notificationManager!!.getNotificationChannel(
            NOTIFICATION_CHANNEL_ID
        )
        if (notificationChannel == null) {
            notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                service!!.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description =
                service!!.getString(R.string.playing_notification_description)
            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationChannel.setShowBadge(false)
            notificationManager!!.createNotificationChannel(notificationChannel)
        } else {
            notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                service!!.getString(R.string.playing_notification_name),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description =
                service!!.getString(R.string.playing_notification_description)
            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationChannel.setShowBadge(false)
            notificationManager!!.createNotificationChannel(notificationChannel)
        }
    }

}