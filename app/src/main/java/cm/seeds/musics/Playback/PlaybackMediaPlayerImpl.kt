package cm.seeds.musics.Playback

import android.content.ContentUris
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.provider.MediaStore
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import cm.seeds.musics.DataModels.Musique
import cm.seeds.musics.Helper.showToast
import cm.seeds.musics.PlayerService
import cm.seeds.musics.R
import java.io.IOException

class PlaybackMediaPlayerImpl(private val playerService: PlayerService) : Playback, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private var isPrepared = false
    private val mediaPlayer : MediaPlayer = MediaPlayer()
    //private val context : Context

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        mediaPlayer.setAudioAttributes(audioAttributes)
        //context = playerService.baseContext
    }


    override fun setDataSource(musique: Musique) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setOnPreparedListener(null)
            if(Build.VERSION.SDK_INT>=29){
                //val uri = MediaStore.Audio.Media.getContentUri(musique.musicRelativePath)
                val fileUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,musique.idMusique.toLong())
                mediaPlayer.setDataSource(playerService,fileUri)
            }else{
                mediaPlayer.setDataSource(musique.path!!)
            }
            mediaPlayer.prepare()
            isPrepared = true // pour mettre le boolean a vrai
            mediaPlayer.setOnErrorListener(this)
            mediaPlayer.setOnCompletionListener(this);
        }catch (e : IOException){
            showToast(playerService,playerService.getString(R.string.error_loading_song))
        }
    }

    override fun play(): Boolean {
        if(isPrepared){
            mediaPlayer.start()
            return true
        }
        return false
    }

    override fun pause(): Boolean {
        if(isPrepared){
            mediaPlayer.pause()
            return true
        }

        return false
    }

    override fun stop(): Boolean {
        if(isPrepared){
            mediaPlayer.stop()
        }
        return true
    }

    override fun release(): Boolean {
        mediaPlayer.release()
        isPrepared = false
        return true
    }

    override fun modifyVolume(volume: Float) {
        if(isPrepared){
            mediaPlayer.setVolume(volume, volume)
        }
    }

    override fun seekTo(milli: Int) {
        if(isPrepared){
            mediaPlayer.seekTo(milli)
        }
    }

    override fun isPlaying(): Boolean {
        return if(isPrepared)
            mediaPlayer.isPlaying
        else
            false
    }

    override fun getCurrentPosition(): Int {
        return if(isPrepared)
            mediaPlayer.currentPosition
        else
            0
    }

    override fun setLooping(bool: Boolean) {
        if(isPrepared){
            mediaPlayer.isLooping = bool
        }
    }

    override fun getDuration(): Int {
        if(isPrepared){
            return mediaPlayer.duration
        }
        return 0
    }

    override fun loadMediaEffects() {

    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Toast.makeText(playerService,playerService.getString(R.string.une_erreur_est_survenue),Toast.LENGTH_SHORT).show();
        if(playerService.getIndexOfPlayingSong()==playerService.getPlayingQueueSize()-1){
        }else{
            playerService.playNextSong()
        }

        return true
    }

    override fun onCompletion(mp: MediaPlayer?) {
        when{
            playerService.isLastTrack() && PlaybackStateCompat.REPEAT_MODE_ALL==playerService.getRepeatMode() -> playerService.playNextSong()
            playerService.isLastTrack().not() && PlaybackStateCompat.REPEAT_MODE_ONE!=playerService.getRepeatMode() -> playerService.playNextSong()
            PlaybackStateCompat.REPEAT_MODE_ONE==playerService.getRepeatMode() -> {
                playerService.loadMusic()
                play()
            }
            playerService.isLastTrack() && PlaybackStateCompat.REPEAT_MODE_ALL!=playerService.getRepeatMode() && PlaybackStateCompat.REPEAT_MODE_ONE!=playerService.getRepeatMode() && PlaybackStateCompat.REPEAT_MODE_GROUP!=playerService.getRepeatMode()-> {
                playerService.pause()
                if(playerService.getNextPosition()!=null){
                    playerService.updatePosition(playerService.getNextPosition()!!)
                }
            }
        }
    }
}