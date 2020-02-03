package cm.seeds.musics.DataModels

import android.support.v4.media.session.PlaybackStateCompat

data class ActualMusicPlayingState(
    var originalPlayingList : MutableList<Musique>?,
    var currentMusic : Musique?,
    var isShuffle : Boolean = false,
    var repeatMode : Int = PlaybackStateCompat.REPEAT_MODE_NONE,
    var isPlaying : Boolean = false,
    var playingSongTimeInMilli : Int = 0,
    var indexOfPlayingSong : Int?,
    var playingQueue : MutableList<Musique>?,
    var themeColor: ThemeColor = ThemeColor()
) {

}