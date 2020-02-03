package cm.seeds.musics.Playback

import cm.seeds.musics.DataModels.Musique

interface Playback {
    fun setDataSource(musique: Musique)

    fun play(): Boolean

    fun pause(): Boolean

    fun stop(): Boolean

    fun release(): Boolean

    fun modifyVolume(volume : Float)

    fun seekTo(milli: Int)

    fun isPlaying(): Boolean

    fun getCurrentPosition(): Int

    fun setLooping(bool: Boolean)

    fun getDuration(): Int

    fun loadMediaEffects()
}