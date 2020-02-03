package cm.seeds.musics.Notification

import android.app.PendingIntent
import android.content.Intent
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import cm.seeds.musics.Activities.MainActivity
import cm.seeds.musics.Helper.getPochetteMusic
import cm.seeds.musics.PlayerService
import cm.seeds.musics.R


class PlayingNotificationImpl(service: PlayerService ) : PlayingNotification(service) {

    override fun update() {

        val musique = service.getCurrentSong()

        isPlaying = service.isPlaying()

        val intent = Intent(service, MainActivity::class.java)
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(MainActivity.SHOULD_OPEN_DETAILS_OF_PLAYING_SONG,true)
        val clicIntent: PendingIntent = PendingIntent.getActivity(service.applicationContext,0, intent,PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(musique!!.titreMusique)
                .setContentText(musique.nomArtiste + " * " + musique.titreAlbum)
                .setLargeIcon(getPochetteMusic(service,musique))
                .setContentIntent(clicIntent)
                .setDeleteIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        service,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(if(isPlaying) R.drawable.ic_baseline_play_arrow_24 else R.drawable.ic_baseline_pause_24)
                .setStyle(
                    androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle()
                        .setMediaSession(service.getMediaSession().sessionToken)
                        .setShowActionsInCompactView(0, 1, 2)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
                                service,
                                PlaybackStateCompat.ACTION_STOP
                            )
                        )
                )
                .setShowWhen(false)

        addActionsOnNotifications(builder)

        updateNotifyModeAndPostNotification(builder.build())
    }

    private fun addActionsOnNotifications(builder: NotificationCompat.Builder) {
        val playPauseAction = NotificationCompat.Action(
                if (isPlaying) R.drawable.ic_baseline_pause_24 else R.drawable.ic_baseline_play_arrow_24,
                if (isPlaying) service.getString(R.string.play) else service.getString(R.string.pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    service,
                    PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            )

        val prevAction =
            NotificationCompat.Action(
                R.drawable.ic_baseline_skip_previous_24,
                service.getString(R.string.precedent),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    service,
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
            )

        val nextAction =
            NotificationCompat.Action(
                R.drawable.ic_baseline_skip_next_24,
                service.getString(R.string.suivant),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    service,
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                )
            )

        builder.addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
    }
}