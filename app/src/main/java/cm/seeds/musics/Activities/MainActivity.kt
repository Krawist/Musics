package cm.seeds.musics.Activities

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cm.seeds.musics.Adapter.MusiqueAdapter
import cm.seeds.musics.DataModels.*
import cm.seeds.musics.Fragment.SimpleDetailFragment
import cm.seeds.musics.Fragment.DetailAlbumFragment
import cm.seeds.musics.Fragment.MenuBottomSheetFragment
import cm.seeds.musics.Helper.*
import cm.seeds.musics.PlayerService
import cm.seeds.musics.PlayerService.UpdateUserUi
import cm.seeds.musics.R
import cm.seeds.musics.Fragment.SelectionFragment
import cm.seeds.musics.ViewModel.MusicsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import java.util.*
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {

    companion object{
        const val SHOULD_OPEN_DETAILS_OF_PLAYING_SONG = "ouvrir_les_details"
        const val WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 11
        const val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 10
    }

    private var initialisationFinished = false


    private var playerService : PlayerService? = null
    private var favoriteSongsId by Delegates.notNull<MutableList<Int>>()
    private var bounded = false
    private var isMotionAtEndState = false
    private lateinit var model : MusicsViewModel
    private var actualMusicPlayingState : ActualMusicPlayingState? = null
    private var shuffleButton by Delegates.notNull<ImageButton>()
    private var repeatButton  by Delegates.notNull<ImageButton>()
    private var nextButton by Delegates.notNull<ImageButton>()
    private var previousButton by Delegates.notNull<ImageButton>()
    private var titreMusique by Delegates.notNull<TextView>()
    private var artisteMusique by Delegates.notNull<TextView>()
    private var tempsEcoule by Delegates.notNull<TextView>()
    private var dureeMusique by Delegates.notNull<TextView>()
    private var imageMusique by Delegates.notNull<ImageView>()
    private var seekbarSongPosition by Delegates.notNull<SeekBar>()
    private var playlistButton by Delegates.notNull<ImageButton>()
    private var buttonLyrics by Delegates.notNull<TextView>()
    private var favoriteButton by Delegates.notNull<ImageButton>()
    private var motionLayout by Delegates.notNull<MotionLayout>()
    private var bottomSheetBehavior : BottomSheetBehavior<FrameLayout>? = null
    //private var playingList : RecyclerView? = null
    private var menuButtonPlayingMusic by Delegates.notNull<ImageButton>()
    //private var rootView by Delegates.notNull<CoordinatorLayout>()
    private var navController by Delegates.notNull<NavController>()
   // private var blurFrameLayout : BlurLayout? = null

    private var playButton by Delegates.notNull<ImageButton>()
    private var pochetteMusic by Delegates.notNull<ImageView>()
    private var appBackground by Delegates.notNull<ImageView>()
    private var updateUserUi = object : UpdateUserUi {

        override fun setPlayingSongTime(playingSongTime: Long) {
            actualMusicPlayingState?.playingSongTimeInMilli = playingSongTime.toInt()
            updateSeekbar(playingSongTime)
        }

        override fun setPlayingSong(currentSong: Musique) {
            model.currentMusic.value = currentSong
            actualMusicPlayingState?.currentMusic = currentSong
            updatePlayingSongInformations(currentSong)
        }

        override fun setPlayingQueue(playingQueue: MutableList<Musique>) {
            actualMusicPlayingState?.playingQueue = playingQueue
            updatePlayingListInformations(playingQueue)
        }

        override fun setIndexOfPlayingSong(indexOfPlayingSong: Int) {
            actualMusicPlayingState?.indexOfPlayingSong = indexOfPlayingSong
        }

        override fun setShuffle(isShuffle: Boolean) {
            actualMusicPlayingState?.isShuffle = isShuffle
            setShuffleModeInformations(isShuffle)
        }

        override fun setRepeatMode(repeatMode: Int) {
            actualMusicPlayingState?.repeatMode = repeatMode
            setRepeatInformations(repeatMode)
        }

        override fun setIsPlaying(isPlaying: Boolean) {
            actualMusicPlayingState?.isPlaying = isPlaying
            setIspLayingInformations(isPlaying)
        }
    }


    private val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .setPopEnterAnim(R.anim.slide_in_left)
            .build()

    private val contentObserver = object : ContentObserver(Handler()){
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            model.refreshData()
        }
    }

    private val sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if(USER_FAVORITE_SONGS_PREFERENCES_KEY == key){
                favoriteSongsId = getFavoriteSong(this@MainActivity)
            }
        }

    private var playingListMusics : MutableList<Musique>? = null
    private var adapterPlayingList : MusiqueAdapter? = null
    private var bottomSheetDialogPlayingList : MenuBottomSheetFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            /** on a pas la permission, il faut la demander **/
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                /** TODO afficher a quoi va servir cette permission **/
                askPermisson()
            }else{
                askPermisson()
            }
        }else{
            initApp()
        }
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onBackPressed() {
        if(motionLayout.currentState==motionLayout.endState){
            motionLayout.transitionToStart()
        }else{
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterObserverOnCursor()
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, PlayerService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        //blurFrameLayout?.startBlur()
    }

    override fun onStop() {
        unbindService(connection)
        bounded = false
        //blurFrameLayout?.pauseBlur()
        super.onStop()
    }

    private fun initApp() {
        model = MusicsViewModel.getInstance(application,this)

        favoriteSongsId = getFavoriteSong(this@MainActivity)

        setContentView(R.layout.activity_main)

        initialiseViews()

        //actualMusicPlayingState = model.actualMusicPlayingState?.value

        addActionOnViews()

        model.actualMusicPlayingState.observe(this, Observer {
            this.actualMusicPlayingState = it
            updateViews()
        })

        //updateViews()

        addObserverOnCursors()

        handleIntentIfExist()

    }

    private fun handleIntentIfExist() {
        val data = intent?.data
        if(data!=null){
            val musiqueToPlay = getMusicByUri(this,data,model.pathOfPochetteAlbum,favoriteSongsId)
            if(musiqueToPlay!=null){
                loadMusic(musiqueToPlay, mutableListOf(musiqueToPlay),0,false)
            }
        }
    }

    private fun addObserverOnCursors() {

        contentResolver.registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,true, contentObserver)

        contentResolver.registerContentObserver(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,true,contentObserver)

        contentResolver.registerContentObserver(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,true,contentObserver)

        getSharedPreferences(USER_FAVORITE_SONGS_PREFERENCES_KEY,Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    private fun unregisterObserverOnCursor(){
        contentResolver.unregisterContentObserver(contentObserver)
        getSharedPreferences(USER_FAVORITE_SONGS_PREFERENCES_KEY,Context.MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    private fun addActionOnViews() {
        playButton.setOnClickListener(View.OnClickListener {
            togglePlayPause()
        })

        nextButton.setOnClickListener(View.OnClickListener {
            doAction(PlayerService.ACTION_SKIP_TO_NEXT)
        })

        shuffleButton.setOnClickListener(View.OnClickListener {
            doAction(PlayerService.ACTION_SHUFFLE)
        })

        repeatButton.setOnClickListener(View.OnClickListener {
            doAction(PlayerService.ACTION_REPEAT)
        })

        previousButton.setOnClickListener(View.OnClickListener {
            doAction(PlayerService.ACTION_SKIP_TO_PREVIOUS)
        })

        seekbarSongPosition.setOnSeekBarChangeListener( object : OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val newPos = seekBar?.progress!!
                seekbarSongPosition.progress = newPos
                if(bounded){
                    playerService?.seetTo(newPos)
                }
            }
        })

        menuButtonPlayingMusic.setOnClickListener(View.OnClickListener {
            if(actualMusicPlayingState!=null && actualMusicPlayingState?.playingQueue!=null){
                openMenu(
                    actualMusicPlayingState?.indexOfPlayingSong!!,
                    actualMusicPlayingState?.playingQueue!!,
                    listOf(R.id.action_add_music_phone_playlist,R.id.action_share_music,R.id.action_details_music),
                    null,
                    null
                )
            }
        })

        playlistButton.setOnClickListener(View.OnClickListener {
            if(bottomSheetDialogPlayingList!=null && actualMusicPlayingState!=null && actualMusicPlayingState?.originalPlayingList!=null && playerService!=null){
                if(true == bottomSheetDialogPlayingList?.isVisible){
                    bottomSheetDialogPlayingList?.dismiss()
                }else{
                    bottomSheetDialogPlayingList?.setList(actualMusicPlayingState?.playingQueue!!)
                    bottomSheetDialogPlayingList?.show(supportFragmentManager,"playing_list")
                }
            }else{
                bottomSheetDialogPlayingList = MenuBottomSheetFragment(
                    0,
                    listOf<Musique>(),
                    playerService!!,
                    listOf(),
                    actualMusicPlayingState?.playingQueue,
                    model,
                    null,
                    null
                )
                bottomSheetDialogPlayingList?.show(supportFragmentManager,"playing_list")
            }
        })

        buttonLyrics.setOnClickListener(View.OnClickListener {
            //val lyrics = getLyricsOfSongs(actualMusicPlayingState?.currentMusic)
        })

        findViewById<View>(R.id.background_control).setOnClickListener(View.OnClickListener {

        })

        favoriteButton.setOnClickListener(View.OnClickListener {
            val currentMusic = actualMusicPlayingState?.currentMusic
            if(currentMusic!=null){
                if(favoriteSongsId.contains(currentMusic.idMusique)){
                    favoriteButton.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                    removeSongFromFavorite(this@MainActivity,currentMusic)
                }else{
                    favoriteButton.setImageResource(R.drawable.ic_baseline_favorite_24)
                    addSongToFavorite(this@MainActivity,currentMusic)
                }
            }
        })

/*        motionLayout?.setTransitionListener(object : MotionLayout.TransitionListener{
            override fun onTransitionCompleted(motion: MotionLayout?, currentStateId: Int) {
                isMotionAtEndState = currentStateId==R.id.end
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {

            }

            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {

            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {

            }
        })*/
    }

    private fun doAction(action : String){
        val intent = Intent(this,PlayerService::class.java)
        intent.action = action
        when(action){
            PlayerService.ACTION_SHUFFLE -> {
                val shuffleMode = if(true==actualMusicPlayingState?.isShuffle) PlaybackStateCompat.SHUFFLE_MODE_NONE else PlaybackStateCompat.SHUFFLE_MODE_ALL
                intent.putExtra(PlayerService.SHUFFLE_MODE,shuffleMode)
                setShuffleModeInformations(shuffleMode==PlaybackStateCompat.SHUFFLE_MODE_ALL)
            }

            PlayerService.ACTION_REPEAT -> {
                val repeatMode = if(PlaybackStateCompat.REPEAT_MODE_ONE==actualMusicPlayingState?.repeatMode) PlaybackStateCompat.REPEAT_MODE_NONE else
                    if(PlaybackStateCompat.REPEAT_MODE_GROUP==actualMusicPlayingState?.repeatMode || PlaybackStateCompat.REPEAT_MODE_ALL==actualMusicPlayingState?.repeatMode) PlaybackStateCompat.REPEAT_MODE_ONE else PlaybackStateCompat.SHUFFLE_MODE_GROUP

                intent.putExtra(PlayerService.REPEAT_MODE,repeatMode)
                setRepeatInformations(repeatMode)
            }
        }
        startService(intent)
    }

    private fun updateViews() {
        if(initialisationFinished && actualMusicPlayingState!=null){

            setIspLayingInformations(actualMusicPlayingState?.isPlaying)

            setRepeatInformations(actualMusicPlayingState?.repeatMode)

            setShuffleModeInformations(actualMusicPlayingState?.isShuffle)

            //setTintOnIcons()

            if(actualMusicPlayingState!=null && actualMusicPlayingState?.currentMusic!=null){

                updatePlayingSongInformations(actualMusicPlayingState?.currentMusic)

                updateSeekbar(actualMusicPlayingState?.playingSongTimeInMilli!!.toLong())

                updatePlayingListInformations(actualMusicPlayingState?.playingQueue)

            }
        }
    }

    private fun updatePlayingListInformations(playingQueue : MutableList<Musique>?) {
        if (playingQueue != null) {
            if (adapterPlayingList != null) {
                adapterPlayingList?.setList(playingQueue)
            } else {
                adapterPlayingList = MusiqueAdapter(
                    this,
                    playingQueue,
                    true,
                    model,
                    false
                )
            }

            playingListMusics = playingQueue

            if(bottomSheetDialogPlayingList!=null){
                bottomSheetDialogPlayingList?.setList(playingQueue)
            }

        }
    }

    private fun setIspLayingInformations(isPlaying : Boolean?) {
        playButton.setImageResource(if (true == isPlaying) R.drawable.ic_baseline_pause_24 else R.drawable.ic_baseline_play_arrow_24)
    }

    private fun updatePlayingSongInformations(currentMusic: Musique?) {
        if(currentMusic!=null){
            val image1 = findViewById<ImageView>(R.id.image1)
            val image2 = findViewById<ImageView>(R.id.image2)

            loadImageInImageView(
                this,
                currentMusic.pochette,
                appBackground,
                false,
                resources.getDimension(R.dimen.size_activity_main_play_button).toInt(),
                R.drawable.headphones_black_and_white
            )

            loadImageInImageView(
                this,
                currentMusic.pochette,
                pochetteMusic,
                true,
                50,
                R.drawable.headphones_black_and_white
            )

            loadImageInImageView(
                this,
                currentMusic.pochette,
                imageMusique,
                true,
                resources.getDimension(R.dimen.third_image_music_size).toInt(),
                R.drawable.headphones_black_and_white
            )

            loadImageInImageView(
                this,
                currentMusic.pochette,
                image1,
                true,
                resources.getDimension(R.dimen.first_image_music_size).toInt(),
                R.drawable.headphones_black_and_white
            )

            loadImageInImageView(
                this,
                currentMusic.pochette,
                image2,
                true,
                resources.getDimension(R.dimen.second_image_music_size).toInt(),
                R.drawable.headphones_black_and_white
            )

            titreMusique.text = currentMusic.titreMusique

            artisteMusique.text = currentMusic.nomArtiste

            seekbarSongPosition.max = currentMusic.duration.toInt()

            bottomSheetDialogPlayingList?.setPlayingMusic(currentMusic)

            dureeMusique.text = formatDurationToString((currentMusic.duration))

            if(favoriteSongsId.contains(currentMusic.idMusique)){
                favoriteButton.setImageResource(R.drawable.ic_baseline_favorite_24)
            }else{
                favoriteButton.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            }
        }
    }

    private fun updateSeekbar(playingSongTime : Long) {
        seekbarSongPosition.progress = playingSongTime.toInt()
        tempsEcoule.text = formatDurationToString(playingSongTime)
    }

    private fun setTintOnIcons() {
        if(actualMusicPlayingState?.themeColor!=null){
            //shuffleButton?.setColorFilter(actualMusicPlayingState?.themeColor!!.backgroundColor)
            //repeatButton?.setColorFilter(actualMusicPlayingState?.themeColor!!.backgroundColor)
            //playButton?.setColorFilter(actualMusicPlayingState?.themeColor!!.backgroundColor)

           // shuffleButton?.setColorFilter(Color.RED)
        }
    }

    private fun setShuffleModeInformations(shuffle: Boolean?) {
        if(true==shuffle){
            shuffleButton.alpha = 1.toFloat()
        }else{
            shuffleButton.alpha = 0.3.toFloat()
        }
    }

    private fun setRepeatInformations(repeatMode: Int?) {
        if(PlaybackStateCompat.REPEAT_MODE_NONE == repeatMode || PlaybackStateCompat.REPEAT_MODE_INVALID==repeatMode){
            repeatButton.setImageResource(R.drawable.ic_baseline_repeat_24)
            repeatButton.alpha = 0.3.toFloat()
        }else if(PlaybackStateCompat.REPEAT_MODE_ALL == repeatMode || PlaybackStateCompat.REPEAT_MODE_GROUP == repeatMode){
            repeatButton.setImageResource(R.drawable.ic_baseline_repeat_24)
            repeatButton.alpha = 1.toFloat()
        }else if(PlaybackStateCompat.REPEAT_MODE_ONE==repeatMode) {
            repeatButton.setImageResource(R.drawable.ic_baseline_repeat_one_24)
            repeatButton.alpha = 1.toFloat()
        }
    }

    private fun initialiseViews() {
        shuffleButton = findViewById(R.id.button_shuffle)
        repeatButton = findViewById(R.id.button_repeat)
        nextButton = findViewById(R.id.button_next)
        previousButton = findViewById(R.id.button_previous)
        playButton = findViewById(R.id.imagebutton_play_pause_button)
        pochetteMusic = findViewById(R.id.imageview_pochette_music)
        appBackground = findViewById(R.id.imageview_app_background)
        titreMusique = findViewById(R.id.textview_playing_music_titre_music)
        artisteMusique = findViewById(R.id.textview_playing_music_artiste_music)
        imageMusique = findViewById(R.id.imageview_image_music)
        seekbarSongPosition = findViewById(R.id.seekbar_playing_music_progress)
        tempsEcoule = findViewById(R.id.textview_current_song_position)
        dureeMusique = findViewById(R.id.textview_total_song_time)
        motionLayout = findViewById(R.id.motionlayout)
        favoriteButton = findViewById(R.id.button_like)
        //bottomSheetBehavior = BottomSheetBehavior.from(findViewById<FrameLayout>(R.id.framelayout_bottom_sheet_playing_list))
        //bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        playlistButton = findViewById(R.id.button_playlist)
/*        playingList = findViewById(R.id.recycler_view_playing_list)
        playingList?.layoutManager = LinearLayoutManager(this)*/
        menuButtonPlayingMusic = findViewById(R.id.menu_button)
        buttonLyrics = findViewById(R.id.button_lyrics)
        //rootView = findViewById<CoordinatorLayout>(R.id.root_view)
        navController = findNavController(R.id.nav_host_fragment)
        //blurFrameLayout = findViewById(R.id.background_control)

        initialisationFinished = true


    }

    private fun askPermisson() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE ->{
                if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    initApp()
                }else{
                    finish()
                }
            }

            WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE ->{
                if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    //initApp()
                }else{
                    //finish()
                }
            }
        }
    }

    fun navigate(view: View) {
        when (view.id){
            R.id.button_home_fragment_more_albums,R.id.imagebutton_home_fragment_more_albums  -> navigateTo(R.id.albumFragment,SimpleDetailFragment.ALL_ALBUM)
            R.id.button_home_fragment_more_most_played_music, R.id.imagebutton_home_fragment_more_most_played_music -> navigateTo(R.id.albumFragment, SimpleDetailFragment.ALL_ARTIST)
            R.id.button_home_fragment_more_musics, R.id.imagebutton_home_fragment_more_musics -> navigateTo(R.id.albumFragment, SimpleDetailFragment.ALL_MUSICS)
            R.id.button_home_fragment_more_playlists, R.id.imagebutton_home_fragment_more_playlist -> navigateTo(R.id.albumFragment, SimpleDetailFragment.ALL_PLAYLIST)
            else -> Toast.makeText(this,"Not yep Implemented",Toast.LENGTH_SHORT).show()
        }
    }

    fun navigateToDetail(what:Any, key : String){
        val bundle = Bundle()
        bundle.putString(DetailAlbumFragment.WHAT_TO_DETAIL,key)
        when (key){
            DetailAlbumFragment.ALBUM -> {
                bundle.putSerializable(key, what as Album)
                navController.navigate(R.id.detailFragment,bundle, navOptions)
            }

            SimpleDetailFragment.PLAYLIST -> {
                bundle.putSerializable(key,what as Playlist)
                bundle.putString(SimpleDetailFragment.WHAT_TO_SEE,key)
                navController.navigate(R.id.albumFragment,bundle,navOptions)
            }

            SimpleDetailFragment.ARTIST ->{
                bundle.putSerializable(key,what as Artiste)
                bundle.putString(SimpleDetailFragment.WHAT_TO_SEE,key)
                navController.navigate(R.id.albumFragment,bundle,navOptions)
            }
        }

    }

    private fun navigateTo(fragmentId: Int,whatToShow: String) {
        val bundle = Bundle()
        bundle.putString(SimpleDetailFragment.WHAT_TO_SEE,whatToShow)
        navController.navigate(fragmentId, bundle, navOptions)
    }

    fun navigationToSelection(menuId : Int, typeOfData : Int, data: List<*>){
        val params = Bundle()
        model.listOfDataForSelection.value = data
        params.putInt(SelectionFragment.MENU_ID,menuId)
        params.putInt(SelectionFragment.TYPE_OF_DATA,typeOfData)
        navController.navigate(R.id.selectionFragment,params, navOptions)
    }

    fun navigateBack(view: View) {
        onBackPressed()
    }

    fun togglePlayPause() {
        val intent = Intent(this,PlayerService::class.java)
        intent.action = PlayerService.ACTION_TOGGLE_PAUSE
        startService(intent)
    }

    fun openMenu(
        itemPosition: Int,
        listOfData: List<*>,
        menuItemIds: List<Int>,
        typeOfData: Int?,
        moreData: List<*>?
    ){
        if(playerService!=null){
            var listOfSong = listOfData
            when {
                listOfData[itemPosition] is Album -> {
                    listOfSong = loadAlbumMusics(this,model.pathOfPochetteAlbum,listOfData[itemPosition] as Album,favoriteSongsId)
                }
                listOfData[itemPosition] is Playlist-> {
                    if(menuItemIds.contains(R.id.action_delere_album)){
                        deletePlaylists(this, arrayListOf(listOfData[itemPosition] as Playlist))
                        return
                    }else{
                        listOfSong = loadPlaylistMusics(this,model.pathOfPochetteAlbum,listOfData[itemPosition] as Playlist,favoriteSongsId)
                    }
                }
                listOfData[itemPosition]  is Artiste -> {
                    listOfSong = loadArtistMusics(this,model.pathOfPochetteAlbum,listOfData[itemPosition] as Artiste,favoriteSongsId)
                }
            }
            val bottomSheet = MenuBottomSheetFragment(
                itemPosition,
                listOfSong,
                playerService!!,
                menuItemIds,
                null,
                model,
                typeOfData,
                moreData)
            bottomSheet.show(supportFragmentManager,"bottom_sheet")
        }
    }

    fun loadMusic(musique: Musique, musiques: MutableList<Musique>, position: Int, shuffle: Boolean) {
        var isShuffle = if (shuffle) shuffle else false
        if(actualMusicPlayingState!=null){

            isShuffle = actualMusicPlayingState?.isShuffle!!

            actualMusicPlayingState?.isShuffle = if(shuffle) shuffle else isShuffle

            if(shuffle){
                val newPos = Random().nextInt(musiques.size)
                actualMusicPlayingState?.indexOfPlayingSong = newPos
                actualMusicPlayingState?.currentMusic = musiques[newPos]
            }else{
                actualMusicPlayingState?.indexOfPlayingSong = position
                actualMusicPlayingState?.currentMusic = musique
            }

            actualMusicPlayingState?.playingSongTimeInMilli = 0
            actualMusicPlayingState?.originalPlayingList = musiques

            if(bounded){
                playerService?.setActualMusicPlayingState(actualMusicPlayingState)
            }
        }else{
            actualMusicPlayingState = ActualMusicPlayingState(musiques,musique,isShuffle,PlaybackStateCompat.REPEAT_MODE_INVALID,false,0,position,null, ThemeColor())
        }

        val param = Gson().toJson(actualMusicPlayingState)
        val intent = Intent(this, PlayerService::class.java)
        intent.action = PlayerService.ACTION_PLAY_PLAYLIST
        intent.putExtra(PlayerService.ACTUAL_PLAYING_MUSIC_STATE,param)
        startService(intent)

        //seekBarPosition.run()
    }

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as PlayerService.LocalBinder
            playerService = binder.getService()
            bounded = true
            (playerService as PlayerService).setUpdateUserUiInterface(updateUserUi)
            (playerService as PlayerService).setActualMusicPlayingState(actualMusicPlayingState)
//            seekBarPosition.run()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bounded = false
        }
    }
}
