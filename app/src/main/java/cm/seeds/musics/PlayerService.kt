package cm.seeds.musics


import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_MEDIA_BUTTON
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import android.widget.Toast
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import androidx.palette.graphics.Palette
import cm.seeds.musics.DataModels.ActualMusicPlayingState
import cm.seeds.musics.DataModels.Musique
import cm.seeds.musics.DataModels.ThemeColor
import cm.seeds.musics.Helper.ACTUAL_PLAYER_STATE
import cm.seeds.musics.Helper.getPochetteMusic
import cm.seeds.musics.Helper.showToast
import cm.seeds.musics.Notification.PlayingNotification
import cm.seeds.musics.Notification.PlayingNotificationImpl
import cm.seeds.musics.Playback.Playback
import cm.seeds.musics.Playback.PlaybackMediaPlayerImpl
import com.google.gson.Gson


class PlayerService() : MediaBrowserServiceCompat(), AudioManager.OnAudioFocusChangeListener{



    companion object{

        private const val MY_MEDIA__ROOT_ID = "media_root_id"
        private const val EMPTY_MEDIA__ROOT_ID = "empty_root_id"

        const val PACKAGE_NAME = "com.example.kmp"
        const val MUSIC_PACKAGE_NAME = "com.android.music"
        const val ACTION_TOGGLE_PAUSE = "$PACKAGE_NAME.togglepause"
        const val ACTION_PLAY = "$PACKAGE_NAME.play"
        const val ACTION_PLAY_PLAYLIST = "$PACKAGE_NAME.play.playlist"
        const val ACTION_PAUSE = "$PACKAGE_NAME.pause"
        const val ACTION_STOP = "$PACKAGE_NAME.stop"
        const val ACTION_SKIP_TO_NEXT = "$PACKAGE_NAME.skiptonext"
        const val ACTION_SKIP_TO_PREVIOUS = "$PACKAGE_NAME.skiptoprevious"
        const val ACTION_SHUFFLE = "$PACKAGE_NAME.shuffle"
        const val ACTION_REPEAT = "$PACKAGE_NAME.repeat"
        const val ACTION_QUIT = "$PACKAGE_NAME.quitservice"
        const val ACTION_LOAD = "$PACKAGE_NAME.load"

        const val SHUFFLE_MODE = "shufflemode"
        const val REPEAT_MODE = "repeatmode"

        const val ACTION_PENDING_QUIT = "$PACKAGE_NAME.pendingquitservice"
        const val INTENT_EXTRA_PLAYLIST = PACKAGE_NAME + "intentextra.playlist"
        const val INTENT_EXTRA_SHUFFLE_MODE = "$PACKAGE_NAME.intentextra.shufflemode"


        const val SERVICE_CREATE = "com.example.kmp.KMP_SERVICE_CREATED"
        const val SERVICE_DESTROYED = "com.example.kmp.KMP_SERVICE_DESTROYED"
        const val PREFERENCES_LAST_PLAYED_SONG_POSITION_KEY =
            PACKAGE_NAME + "position_dernier_song"
        const val PREFERENCES_LAST_LOADED_PLAYLIST_KEY =
            PACKAGE_NAME + "derniere_playlist_chargee"
        const val PREFERENCES_LAST_ACTIVE_PLAYLIST_KEY =
            PACKAGE_NAME + "derniere_playlist_actuelle"
        const val PREFERNCE_SHUFFLE_MODE_KEY = PACKAGE_NAME + "shuffle_mode"
        const val PREFERNCE_REPEAT_MODE_KEY = PACKAGE_NAME + "repeat_mode"
        const val PREFERNCE_POSITION_MILLI_LAST_SONG =
            PACKAGE_NAME + "duree_dernier_song_joué"
        private const val MEDIA_SESSION_LOG_TAG = PACKAGE_NAME + "LOG_FOR_MEDIA_PLAYER"

        private const val SEEK_BAR_POSITION_REFRESH_DELAY = 500.toLong()

        public const val ACTUAL_PLAYING_MUSIC_STATE = "parametre_de_lecture_actuelle"
    }

    private var isLoadingLooping: Boolean = false
    private var playerStateBuilder: Builder = PlaybackStateCompat.Builder()
    private var binder = LocalBinder()
    private var becomingNoisyReceiverRegistered: Boolean = false
    private var mediaSession: MediaSessionCompat? = null
    private var resumeOnFocusGain: Boolean = false
    private var playback : Playback? = null
    private var becomingNoisyReceiver: BroadcastReceiver? = null
    //private var model: MusicsViewModel? = null
    private var audioManager : AudioManager? = null
    private var seekBarPositionRunnable : UpdateSeekBarPosition? = null
    private var focusRequest : AudioFocusRequest? = null
    private var seekbarHandler = Handler()
    private var playingNotification : PlayingNotification? = null
    private var actualMusicPlayingState : ActualMusicPlayingState? = null
    private var uiListener : UpdateUserUi? = null

    interface UpdateUserUi {
        fun setPlayingSongTime(playingSongTime : Long)
        fun setPlayingSong(currentSong : Musique)
        fun setPlayingQueue(playingQueue : MutableList<Musique>)
        fun setIndexOfPlayingSong(indexOfPlayingSong : Int)
        fun setShuffle(isShuffle : Boolean)
        fun setRepeatMode(repeatMode: Int)
        fun setIsPlaying(isPlaying : Boolean)
    }


    fun setUpdateUserUiInterface(listener : UpdateUserUi){
        this.uiListener = listener
    }

    override fun onCreate() {
        super.onCreate()

        focusRequest = if(Build.VERSION.SDK_INT>=26){
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setAudioAttributes(AudioAttributes.Builder().run {
                    setUsage(AudioAttributes.USAGE_GAME)
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                setAcceptsDelayedFocusGain(true)
                setOnAudioFocusChangeListener(this@PlayerService)
                build()
            }
        }else{
            null
        }

        playingNotification = PlayingNotificationImpl(this)

        playback = PlaybackMediaPlayerImpl(this)

        becomingNoisyReceiverRegistered = false

        seekBarPositionRunnable = UpdateSeekBarPosition()

        mediaSession = MediaSessionCompat(applicationContext, MEDIA_SESSION_LOG_TAG)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        //model = MusicsViewModel.getInstance()

        setupMediaSession()

        mediaSession?.isActive = true

        playingNotification?.init()

        becomingNoisyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action != null && action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                    pause()
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (action != null) {
                when (action) {
                    ACTION_PLAY -> play()
                    ACTION_PLAY_PLAYLIST -> {
                        this.actualMusicPlayingState = Gson().fromJson(intent.getStringExtra(ACTUAL_PLAYING_MUSIC_STATE),ActualMusicPlayingState::class.java)
                        loadMusic()
                        play()
                        initialisePlayingQueue()
                        updatePlayListPreference()
                    }
                    ACTION_TOGGLE_PAUSE -> if (isPlaying()) pause() else play()
                    ACTION_PAUSE -> pause()
                    ACTION_SHUFFLE -> {
                        val shuffleMode = intent.getIntExtra(SHUFFLE_MODE, SHUFFLE_MODE_NONE)
                        setShuffle(shuffleMode)
                    }
                    ACTION_REPEAT -> {
                        val repeatMode =
                            intent.getIntExtra(REPEAT_MODE, REPEAT_MODE_NONE)
                        setRepeatMode(repeatMode)
                    }
                    ACTION_SKIP_TO_NEXT -> playNextSong()
                    ACTION_SKIP_TO_PREVIOUS -> playPreviousSong()
                    ACTION_LOAD -> if (!isPlaying()) {
                        preparePlaying()
                    }
                    ACTION_QUIT -> stop()
                    ACTION_MEDIA_BUTTON -> MediaButtonReceiver.handleIntent(
                        mediaSession,
                        intent
                    )
                    else -> {
                    }
                }
            }
        }

        return Service.START_NOT_STICKY
    }

    fun getActualMusicPlayingState() : ActualMusicPlayingState = actualMusicPlayingState!!

    private fun stop() {
        abandonFocusOnAudioOutput()
        actualMusicPlayingState?.isPlaying = false
        uiListener?.setIsPlaying(false)
        registerReceiver(false)
        updateMediaSession()
        playingNotification?.stop()
    }

    private fun abandonFocusOnAudioOutput() {
        if(Build.VERSION.SDK_INT>=26){
            audioManager?.abandonAudioFocusRequest(focusRequest!!)
        }else{
            audioManager?.abandonAudioFocus(this)
        }
    }

    private fun preparePlaying() {
        loadMusic()
        val currentSongTime = if(actualMusicPlayingState!=null) actualMusicPlayingState?.playingSongTimeInMilli!! else 0
        seetTo(currentSongTime)
        uiListener?.setPlayingSongTime(currentSongTime.toLong())
    }

    fun seetTo(newPosition: Int) {
        playback?.seekTo(newPosition)
    }

    fun playPreviousSong() {
        val indexPreviousPosition = getPreviousPosition()
        if(indexPreviousPosition!=null){
            updatePosition(indexPreviousPosition)
            loadMusic()
            play()
        }else{
            showToast(this,getString(R.string.veuillez_choisir_un_son))
        }
    }

    private fun getPreviousPosition(): Int? {
        val indexOfPlayingSong = getIndexOfPlayingSong()
        return if(indexOfPlayingSong!=null){
            var newPosition: Int = (indexOfPlayingSong - 1) % getPlayingQueueSize()

            if (newPosition < 0) newPosition += getPlayingQueueSize()

            newPosition
        }else
            null
    }

    fun playNextSong() {
        val indexNextPosition = getNextPosition()
        if(indexNextPosition!=null){
            updatePosition(indexNextPosition)
            loadMusic()
            play()
        }else{
            showToast(this,getString(R.string.veuillez_choisir_un_son))
        }
    }

    fun getNextPosition(): Int? {
        val indexOfPlayingSong = getIndexOfPlayingSong()
        return if(indexOfPlayingSong!=null){
            var newPosition: Int = (indexOfPlayingSong + 1) % getPlayingQueueSize()
            if (newPosition >= getPlayingQueueSize()) newPosition = 0

            newPosition
        }else
            null
    }

    fun updatePosition(newPosition: Int) {
        if(newPosition>=0 && newPosition<getPlayingQueueSize()){
            actualMusicPlayingState?.indexOfPlayingSong = newPosition
            actualMusicPlayingState?.currentMusic = actualMusicPlayingState!!.playingQueue!![newPosition]

            uiListener?.setIndexOfPlayingSong(newPosition)
            uiListener?.setPlayingSong(actualMusicPlayingState!!.playingQueue!![newPosition])
            loadMusic()
        }
    }

    fun getPlayingQueueSize(): Int{

        return getPlayingQueue().size

/*        return if(getPlayingQueue()!=null){

        }else{
            0
        }*/
    }

    private fun setRepeatMode(repeatMode: Int) {
        actualMusicPlayingState?.repeatMode = repeatMode
        uiListener?.setRepeatMode(repeatMode)
    }

    private fun updatePlayListPreference() {
        val sharedPreferences = (getSharedPreferences(PREFERENCES_LAST_ACTIVE_PLAYLIST_KEY, Context.MODE_PRIVATE))
        val gson = Gson()
        val playingQueueAsString = gson.toJson(getPlayingQueue())
        val originalPlayListAsString = gson.toJson(getOriginalPlayList())

        sharedPreferences.edit()
            .putString(PREFERENCES_LAST_ACTIVE_PLAYLIST_KEY, playingQueueAsString)
            .putString(PREFERENCES_LAST_LOADED_PLAYLIST_KEY, originalPlayListAsString)
            .apply()
    }

    private fun getPlayingQueue(): MutableList<Musique> {
        if(actualMusicPlayingState!=null && actualMusicPlayingState?.playingQueue!=null){
            return actualMusicPlayingState?.playingQueue!!
        }

        return emptyList<Musique>().toMutableList()
    }

    private fun initialisePlayingQueue() {
        if(getOriginalPlayList().isNotEmpty()){
            val isShuffleMode: Boolean = getShuffleMode()
            if (isShuffleMode) {
                shuffleOriginalList()
            } else {
                actualMusicPlayingState?.playingQueue = getOriginalPlayList().toMutableList()
                uiListener?.setPlayingQueue(getOriginalPlayList().toMutableList())
            }
        }
    }

    private fun getOriginalPlayList(): MutableList<Musique> {
        if(actualMusicPlayingState!=null && actualMusicPlayingState?.originalPlayingList!=null){
            return actualMusicPlayingState?.originalPlayingList!!
        }

        return emptyList<Musique>().toMutableList()
    }

    private fun shuffleOriginalList() {
        if(getOriginalPlayList().isNotEmpty()){
            val list: MutableList<Musique> = getOriginalPlayList().toMutableList()
            val newPlayingQueue: MutableList<Musique> = ArrayList()

            val m = list[getPositionOfSongToPlay()]
            newPlayingQueue.add(0, m)
            list.remove(m)
            list.shuffle()
            newPlayingQueue.addAll(list)
            actualMusicPlayingState?.indexOfPlayingSong = 0
            actualMusicPlayingState?.playingQueue = newPlayingQueue

            uiListener?.setIndexOfPlayingSong(0)
            uiListener?.setPlayingQueue(newPlayingQueue)
        }
    }

    private fun getPositionOfSongToPlay(): Int {
        if(actualMusicPlayingState!=null){
            return actualMusicPlayingState?.indexOfPlayingSong!!
        }

        return 0
    }

    private fun getShuffleMode(): Boolean {
        if(actualMusicPlayingState!=null){
            return actualMusicPlayingState?.isShuffle!!
        }

        return false
    }

    fun loadMusic() {
        val musique = getCurrentSong()
        if (musique != null) {
            uiListener?.setPlayingSong(musique)
            playback?.setDataSource(getCurrentSong()!!)
            try {
                if (getCurrentSong()?.pochette != null) {
                    val bitmap = BitmapFactory.decodeFile(getCurrentSong()!!.pochette)
                    if (bitmap != null) {
                        createPalette(bitmap)
                    }
                }
            } catch (e: OutOfMemoryError) {

            }
        }
    }

    private fun createPalette(bitmap: Bitmap) {
        Palette.from(bitmap)
            .generate { palette -> changeThemeColor(palette) }
    }

    private fun changeThemeColor(palette: Palette?) {
        var theme = palette?.vibrantSwatch
        if(theme!=null){
           val  themeColor = ThemeColor(theme.rgb,theme.bodyTextColor,theme.titleTextColor)
            actualMusicPlayingState?.themeColor = themeColor
        }else{
            theme = palette?.dominantSwatch
            if(theme!=null){
                val  themeColor = ThemeColor(theme.rgb,theme.bodyTextColor,theme.titleTextColor)
                actualMusicPlayingState?.themeColor = themeColor
            }
        }
    }

    fun getIndexOfPlayingSong(): Int? {
        return actualMusicPlayingState!!.indexOfPlayingSong ?: 0
    }

    private fun updateNotification() {
        if (playingNotification != null && getCurrentSong() != null && getCurrentSong()?.idMusique != -1) {
            playingNotification?.update()
        }
    }

    fun getCurrentSong(): Musique? {
        return actualMusicPlayingState?.currentMusic
    }


    public fun isLastTrack() : Boolean {
        return if(getPlayingQueueSize()>0){
            getIndexOfPlayingSong() == getOriginalPlayList().size - 1
        }else {
            false
        }
    }

    fun getPlayingSongDuration() : Int{
        return playback?.getDuration()!!
    }

    fun getRepeatMode() : Int {
        return actualMusicPlayingState?.repeatMode!!
    }

    override fun onDestroy() {
        super.onDestroy()
        if(becomingNoisyReceiverRegistered){
            unregisterReceiver(becomingNoisyReceiver)
            becomingNoisyReceiverRegistered = false
        }
        mediaSession?.isActive = false
        quit()
        releaseResources()
    }

    private fun releaseResources() {
        abandonFocusOnAudioOutput()
        playback?.release()
    }

    private fun setShuffle(shuffle: Int) {
        when(shuffle){
            SHUFFLE_MODE_ALL,
            SHUFFLE_MODE_GROUP -> {
                actualMusicPlayingState?.isShuffle = true
                uiListener?.setShuffle(true)
                shuffleOriginalList()
            }

            SHUFFLE_MODE_INVALID, SHUFFLE_MODE_NONE -> {
                actualMusicPlayingState?.isShuffle = false
                uiListener?.setShuffle(false)
                actualMusicPlayingState?.playingQueue = getOriginalPlayList()
                uiListener?.setPlayingQueue(getOriginalPlayList())
            }
        }
    }

    private fun quit() {
        pause()
        playingNotification?.stop()
        releaseResources()
    }

    private fun setupMediaSession() {
        mediaSession?.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )

        playerStateBuilder
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        ACTION_PLAY_PAUSE or
                        ACTION_SET_SHUFFLE_MODE or
                        ACTION_SET_REPEAT_MODE or
                        PlaybackStateCompat.ACTION_STOP or
                        ACTION_SEEK_TO
            )

        mediaSession?.setPlaybackState(playerStateBuilder.build())

        mediaSession?.setCallback(object : MediaSessionCompat.Callback() {

            override fun onPlay() {
                play()
            }

            override fun onPause() {
                pause()
            }

            override fun onSkipToNext() {
                playNextSong()
            }

            override fun onSkipToPrevious() {
                playPreviousSong()
            }

            override fun onStop() {
                stop()
            }

            override fun onSetRepeatMode(repeatMode: Int) {
                setRepeatMode(repeatMode)
            }

            override fun onSetShuffleMode(shuffleMode: Int) {
                setShuffle(shuffleMode)
            }

            override fun onSeekTo(pos: Long) {
                seetTo(pos.toInt())
            }
        })

        mediaSession?.setFlags(
            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                    or MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
        )

        sessionToken = mediaSession?.sessionToken
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {

    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return null
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when(focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pause()
            }

            AudioManager.AUDIOFOCUS_GAIN -> {
                playback?.modifyVolume(1.toFloat())
                if (resumeOnFocusGain) {
                    play()
                    resumeOnFocusGain = false
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT-> {
                if (isPlaying()){
                    resumeOnFocusGain = true
                }
                pause()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> playback?.modifyVolume(0.2f)

        }
    }

    fun isPlaying(): Boolean {
        return playback?.isPlaying()!!
    }

    fun play() {
        synchronized(this) {
            if (requestFocus()) {
                val isPlaying = playback?.play()
                if (isPlaying?.not()!!) {
                    if (!isLoadingLooping) {
                        loadMusic()
                        isLoadingLooping = true
                        play()
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.veuillez_choisir_un_son),
                            Toast.LENGTH_SHORT
                        ).show()
                        isLoadingLooping = false
                    }
                }else{
                    isLoadingLooping = false
                    updateMediaSession()
                    actualMusicPlayingState?.isPlaying = true
                    uiListener?.setIsPlaying(true)
                    mediaSession?.isActive = true
                    updateNotification()
                    registerReceiver(true)
                    seekBarPositionRunnable?.run()
                }
            }
        }
    }

    private fun registerReceiver(shouldRegister: Boolean) {
        if (shouldRegister) {
            if (!becomingNoisyReceiverRegistered) {
                val filter = IntentFilter()
                filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                registerReceiver(becomingNoisyReceiver, filter)
            }
        } else {
            try {
                if (becomingNoisyReceiverRegistered) {
                    unregisterReceiver(becomingNoisyReceiver)
                }
            } catch (e: IllegalArgumentException) {
            }
        }
        becomingNoisyReceiverRegistered = shouldRegister
    }

    private fun updateMediaSession() {
        mediaSession?.isActive = isPlaying()

        updateMediaSessionMetaData()

        updateMediaSessionPlaybackState()
    }

    private fun updateMediaSessionPlaybackState() {
        mediaSession?.setPlaybackState(
            playerStateBuilder
                .setState(
                    if (isPlaying()) STATE_PLAYING else STATE_PAUSED,
                    getSongProgressMillis().toLong(),
                    if (isPlaying()) 1.toFloat() else 0.toFloat()
                )
                .build()
        )
    }

    private fun getSongProgressMillis(): Int {
        return playback?.getCurrentPosition()!!
    }

    private fun updateMediaSessionMetaData() {
        if (isPlaying()) {
            val song = getCurrentSong()
            if ((song == null) || (song.idMusique == -1)) {
                mediaSession?.setMetadata(null)
                return
            }
            val bitmap: Bitmap? = getPochetteMusic(applicationContext,song)
            val metaData = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.nomArtiste)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, song.nomArtiste)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.titreAlbum)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.titreMusique)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
                .putLong(
                    MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER,
                    getPositionOfSongToPlay().toLong()
                )
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                metaData.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, getPlayingQueueSize().toLong())
            }
            mediaSession?.setMetadata(metaData.build())
        } else {
            mediaSession?.setMetadata(null)
        }
    }

    private fun requestFocus(): Boolean {
        return if(Build.VERSION.SDK_INT>=26){
            audioManager?.requestAudioFocus(focusRequest!!)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }else{
            (audioManager?.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        }
    }

    fun pause() {
        actualMusicPlayingState?.isPlaying = false
        uiListener?.setIsPlaying(false)
        playback?.pause()
        updateMediaSession()
        updateNotification()
        //abandonFocusOnAudioOutput()
    }

    fun addSongs(position : Int, listOfSongs : MutableList<Musique>){
        if(getPlayingQueue().isNotEmpty()){
            if(1==getPlayingQueueSize()){
                getPlayingQueue().addAll(listOfSongs)
                notifyPlaylistChange()
                showToast(this,getString(R.string.la_liste_sera_joue_apres_le_song_en_cours))
            }else{
                getPlayingQueue().addAll(position, listOfSongs)
                notifyPlaylistChange()
                showToast(this,getString(R.string.la_liste_sera_joue_apres_le_song_en_cours))
            }
        }else{
            getPlayingQueue().addAll(listOfSongs)
        }

        getOriginalPlayList().addAll(listOfSongs)

        notifyPlaylistChange()

        uiListener?.setPlayingQueue(getPlayingQueue())
    }

    fun addSongs(listOfSongs: MutableList<Musique>){
        getPlayingQueue().addAll(listOfSongs)
        showToast(this,getString(R.string.la_liste_sera_joue_apres_celle_en_cours))

        getOriginalPlayList().addAll(listOfSongs)

        notifyPlaylistChange()

        uiListener?.setPlayingQueue(getPlayingQueue())
    }

    fun addSong(position : Int, musique : Musique){
        if(getPlayingQueueSize()<=1){
            if(getPlayingQueueSize()==0){
                getPlayingQueue().add(musique)
                showToast(this,"Ajouté")
            }else{
                if(getPlayingQueue()[0].idMusique!=musique.idMusique){
                    getPlayingQueue().add(musique)
                    showToast(this,getString(R.string.le_song_sera_jouer_juste_apres))
                }
            }
        }else{
            var itemPositionIfInside = -1
            getPlayingQueue().forEachIndexed { index, music ->
                if(music.idMusique==musique.idMusique){
                    itemPositionIfInside = index
                }
            }
            if(itemPositionIfInside>-1){
                getPlayingQueue().removeAt(itemPositionIfInside)

            }
            getPlayingQueue().add(position, musique)
            showToast(this,getString(R.string.le_song_sera_jouer_juste_apres))
        }

        getOriginalPlayList().add(musique)

        notifyPlaylistChange()

        uiListener?.setPlayingQueue(getPlayingQueue())

        /*getOriginalPlayList()?.add(musique)
notifyPlaylistChange()*/
    }

    fun addSong(musique: Musique){
        var itemPositionIfInside = -1
        if(getPlayingQueueSize()>0){
            getPlayingQueue().forEachIndexed { index, music ->
                if(music.idMusique==musique.idMusique){
                    itemPositionIfInside = index
                }
            }
        }
        if(itemPositionIfInside>-1){
            getPlayingQueue().removeAt(itemPositionIfInside)
        }

        getPlayingQueue().add( musique)
        showToast(this,getString(R.string.le_song_sera_joue_juste_apres_la_liste_en_cours))

        getOriginalPlayList().add(musique)

        notifyPlaylistChange()

        uiListener?.setPlayingQueue(getPlayingQueue())
    }

    private fun notifyPlaylistChange() {
        updateMediaSession()
        updatePlayListPreference()
    }

    fun getMediaSession(): MediaSessionCompat {
        return mediaSession!!
    }

    fun setActualMusicPlayingState(actualMusicPlayingState: ActualMusicPlayingState?) {
        if(this.actualMusicPlayingState==null && actualMusicPlayingState!=null){
            this.actualMusicPlayingState = actualMusicPlayingState
            loadMusic()
            seetTo(actualMusicPlayingState.playingSongTimeInMilli)
        }
    }


    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): PlayerService = this@PlayerService
    }

    inner class UpdateSeekBarPosition() : Runnable{
        override fun run() {
            actualMusicPlayingState?.playingSongTimeInMilli = getSongProgressMillis()
            uiListener?.setPlayingSongTime(getSongProgressMillis().toLong())

            savePlayingMusicState()

            if(isPlaying())
                seekbarHandler.postDelayed(this, SEEK_BAR_POSITION_REFRESH_DELAY)
            else
                seekbarHandler.removeCallbacks(this)
        }
    }

    private fun savePlayingMusicState() {
        val currentState = actualMusicPlayingState?.isPlaying

        actualMusicPlayingState?.isPlaying = false
        getSharedPreferences(ACTUAL_PLAYER_STATE, Context.MODE_PRIVATE).edit()
            .putString(ACTUAL_PLAYER_STATE, Gson().toJson(actualMusicPlayingState))
            .apply()

        actualMusicPlayingState?.isPlaying = currentState ?: false
    }
}