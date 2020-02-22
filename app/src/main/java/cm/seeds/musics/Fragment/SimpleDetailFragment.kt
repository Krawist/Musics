package cm.seeds.musics.Fragment

import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cm.seeds.musics.Activities.MainActivity
import cm.seeds.musics.Adapter.AlbumAdapter
import cm.seeds.musics.Adapter.ArtistAdapter
import cm.seeds.musics.Adapter.MusiqueAdapter
import cm.seeds.musics.Adapter.PlaylistAdapter
import cm.seeds.musics.DataModels.Album
import cm.seeds.musics.DataModels.Artiste
import cm.seeds.musics.DataModels.Musique
import cm.seeds.musics.DataModels.Playlist
import cm.seeds.musics.Helper.ProviderUtils
import cm.seeds.musics.Helper.getFavoriteSong
import cm.seeds.musics.Helper.numberOfItemInLine
import cm.seeds.musics.R
import cm.seeds.musics.ViewModel.MusicsViewModel
import com.google.android.material.appbar.AppBarLayout
import kotlin.properties.Delegates

/**
 * A simple [Fragment] subclass.
 */
class SimpleDetailFragment : Fragment() {

    companion object{
        val WHAT_TO_SEE = "WHAT_TO_SEE"

        val ALL_ALBUM = "ALL_AlBUM"
        val ALL_MUSICS = "ALL_MUSIC"
        val ALL_BEST_MUSIC = "ALL_BEST_MUSIC"
        val ALL_ARTIST = "ALL_ARTIST"
        val ALL_PLAYLIST = "ALL_PLAYLIST"

        val PLAYLIST = "PLAYLIST"
        val ARTIST = "ARTIST"
        val DETAIL_OF_WHAT = "DETAIL_OF"
    }

    private var whatToShow : String? = null

    private var searchview : EditText? = null
    private var recyclerView : RecyclerView? = null
    private var data : List<*>? = null
    private var searchView : EditText? = null
    private var adapter : Any? = null
    private lateinit var model : MusicsViewModel
    private var appbar: AppBarLayout? = null
    private var motionLayout: MotionLayout? = null
    private var fragmentTitle : TextView? = null
    private var menuButton : ImageButton? = null

    private var artiste : Artiste? = null
    private var playlist : Playlist? = null

    private var favoriteSongsId by Delegates.notNull<MutableList<Int>>()

    private val contentObserver = object : ContentObserver(Handler()){
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            when(whatToShow) {
                ALL_ALBUM, ALL_PLAYLIST, ALL_ARTIST, ALL_BEST_MUSIC, ALL_MUSICS -> {
                    model.refreshData()
                }

                PLAYLIST -> {
                    model.loadmusicOfPlaylist(context!!, playlist!!)
                }

                ARTIST -> {
                    model.loadmusicOfArtist(context!!, artiste!!)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        whatToShow = arguments?.getString(WHAT_TO_SEE)
        favoriteSongsId = getFavoriteSong(context!!)

        when(whatToShow){
            PLAYLIST -> {
                playlist = arguments?.getSerializable(PLAYLIST) as Playlist
            }

            ARTIST ->{
                artiste = arguments?.getSerializable(ARTIST) as Artiste
            }
        }

        registerObserverOnCursors()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterObserverOnCursor()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_album, container, false)

        initialiseViews(view)

        addDataToViews()

        setLayoutManager()

        configureViewModel()

        addActionOnViews()

        return view
    }

    private fun addDataToViews() {
        when(whatToShow){

            ALL_ALBUM-> {
                fragmentTitle?.setText(R.string.albums)
                searchview?.setHint(R.string.rechercher_un_album_ici)
            }

            ALL_PLAYLIST ->{
                fragmentTitle?.setText(R.string.playlist)
                searchview?.setHint(R.string.rechercher_une_playlist_ici)
            }

            ALL_ARTIST ->{
                fragmentTitle?.setText(R.string.artistes)
                searchview?.setHint(R.string.rechercher_un_artiste_ici)
            }

            ALL_BEST_MUSIC ->{
                fragmentTitle?.setText(R.string.musiques_les_plus_joues)
                searchview?.setHint(R.string.rechercher_une_musique_ici)

            }

            ALL_MUSICS ->{
                fragmentTitle?.setText(R.string.musiques)
                searchview?.setHint(R.string.rechercher_une_musique_ici)
            }

            PLAYLIST ->{
                fragmentTitle?.text = playlist?.nomPlaylist
                searchview?.setHint(R.string.rechercher_une_musique_ici)
            }

            ARTIST ->{
                fragmentTitle?.text = artiste?.nomArtiste
                searchview?.setHint(R.string.rechercher_une_musique_ici)
            }


        }
    }

    private fun setLayoutManager() {
        when(whatToShow){

            ALL_ALBUM-> {
                recyclerView?.layoutManager = GridLayoutManager(context, numberOfItemInLine(activity!!, R.dimen.album_item_full_good_size))
            }

            ALL_PLAYLIST ->{
                recyclerView?.layoutManager = GridLayoutManager(context, numberOfItemInLine(activity!!, R.dimen.playlist_item_full_good_size))
            }

            ALL_ARTIST ->{
                recyclerView?.layoutManager = GridLayoutManager(context, numberOfItemInLine(activity!!, R.dimen.album_item_full_good_size))
            }

            ALL_BEST_MUSIC , ALL_MUSICS, PLAYLIST, ARTIST ->{
                recyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
            }
        }
    }

    private fun addActionOnViews() {
/*        appbar?.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val progress = -verticalOffset / appBarLayout?.totalScrollRange?.toFloat()!!
            motionLayout?.progress = progress
        })*/

        searchView?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                launchSearch(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        menuButton?.setOnClickListener(View.OnClickListener {

            val typeOfData = when (whatToShow){

                ALL_PLAYLIST -> SelectionFragment.TYPE_OF_DATA_PLAYLISTS
                ALL_ALBUM -> SelectionFragment.TYPE_OF_DATA_ALBUMS
                ALL_ARTIST -> SelectionFragment.TYPE_OF_DATA_ARTISTS
                ALL_MUSICS, ALL_BEST_MUSIC, PLAYLIST, ARTIST -> SelectionFragment.TYPE_OF_DATA_MUSICS

                else -> 0
            }

            (context as MainActivity).openMenu(
                0, data!!,
                listOf(R.id.action_select_musics_to_play,
                    R.id.action_select_musics_to_add_to_playlist,
                    R.id.action_select_musics_to_share,
                    R.id.action_select_musics_to_delete),
                typeOfData,
                data
            )
        })
    }

    private fun launchSearch(query: String) {
        when(whatToShow){

            ALL_BEST_MUSIC, ALL_MUSICS, PLAYLIST, ARTIST ->{
                model.seachData.value = ProviderUtils.buildListOfMusicWithCursor(ProviderUtils.searchMusics(context!!,query),model.pathOfPochetteAlbum,favoriteSongsId = favoriteSongsId)
            }

            ALL_ALBUM ->{
                model.seachData.value = ProviderUtils.buildListOfAlbumsWithCursor(ProviderUtils.searchAlbums(context!!,query),model.pathOfPochetteAlbum,model.pathOfArtistsAlbumsArt)
            }

            ALL_PLAYLIST -> {
                model.seachData.value = ProviderUtils.buildListOfPlaylistFromCursor(ProviderUtils.searchPlaylist(context!!,query))
            }

            ALL_ARTIST -> {
                model.seachData.value = ProviderUtils.buildListOfArtistsWithCursor(ProviderUtils.searchArtist(context!!,query),model.pathOfArtistsAlbumsArt)
            }

        }
    }

    private fun configureViewModel() {
        model = MusicsViewModel.getInstance()

        model.currentMusic.observe(this, Observer {
            when(whatToShow){
                ALL_BEST_MUSIC, ALL_MUSICS, PLAYLIST, ARTIST ->{
                    if(adapter!=null){
                        (adapter as MusiqueAdapter).setPlayingMusic(it)
                    }
                }
            }
        })

        model.seachData.observe(this, Observer {
            this.data = it
            configureAdapter()
        })

        when(whatToShow){

            ALL_ALBUM-> {
                model.allAlbums.observe(this, Observer {
                    this.data = it
                    configureAdapter()
                })
            }

            ALL_PLAYLIST ->{
                model.allPLaylist.observe(this, Observer {
                    this.data = it
                    configureAdapter()
                })
            }

            ALL_ARTIST ->{
                model.allArtistes.observe(this, Observer {
                    this.data = it
                    configureAdapter()
                })
            }

            ALL_BEST_MUSIC ->{
                model.allBestMusics.observe(this, Observer {
                    this.data = it
                    configureAdapter()
                })
            }

            ALL_MUSICS ->{
                model.allmusics.observe(this, Observer {
                    this.data = it
                    configureAdapter()
                })
            }

            PLAYLIST ->{
                model.loadmusicOfPlaylist(context!!,playlist!!)
                model.musicOfPlaylist.observe(this, Observer {
                    this.data = it
                    configureAdapter()
                })
            }

            ARTIST ->{
                model.loadmusicOfArtist(context!!,artiste!!)
                model.musicOfArtist.observe(this, Observer {
                    this.data = it
                    configureAdapter()
                })
            }

        }
    }

    private fun registerObserverOnCursors() {

        context?.contentResolver?.registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,true, contentObserver)

        context?.contentResolver?.registerContentObserver(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,true,contentObserver)

        context?.contentResolver?.registerContentObserver(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,true,contentObserver)

    }

    private fun unregisterObserverOnCursor(){
        context?.contentResolver?.unregisterContentObserver(contentObserver)
    }

    private fun configureAdapter() {
        if(data!=null){
            when(whatToShow){
                ALL_ALBUM-> {
                    val listAlbum = data as List<Album>
                    if(adapter!=null){
                        (adapter as AlbumAdapter).setList(listAlbum)
                    }else{
                        adapter = AlbumAdapter(context!!,listAlbum,R.layout.album_item_full)
                    }


                    if(adapter!=recyclerView?.adapter){
                        recyclerView?.adapter = adapter as AlbumAdapter
                    }

                    //list?.adapter = adapter as AlbumAdapter
                }

                ALL_PLAYLIST ->{
                    val playlists = data as List<Playlist>
                    if(adapter!=null){
                        (adapter as PlaylistAdapter).setList(playlists)
                    }else{
                        adapter = PlaylistAdapter(playlists, R.layout.playlist_item_full)
                    }

                    if(adapter!=recyclerView?.adapter){
                        recyclerView?.adapter = adapter as PlaylistAdapter
                    }
                    //this.list?.adapter = adapter as PlaylistAdapter

                }

                ALL_ARTIST ->{
                    val artistes = data as List<Artiste>
                    if(adapter!=null){
                        (adapter as ArtistAdapter).setList(artistes)
                    }else{
                        adapter = ArtistAdapter(
                            context!!,
                            artistes,
                            R.layout.artist_item_full_width
                        )
                    }

                    if(adapter!=recyclerView?.adapter){
                        recyclerView?.adapter = adapter as ArtistAdapter
                    }
                }

                ALL_BEST_MUSIC, ALL_MUSICS, PLAYLIST, ARTIST ->{
                    val musiques = data as MutableList<Musique>
                    if(adapter!=null){
                        (adapter as MusiqueAdapter).setList(musiques)
                    }else{
                        adapter = MusiqueAdapter(context, musiques, true, model, false)
                    }

                    if(adapter!=recyclerView?.adapter){
                        recyclerView?.adapter = adapter as MusiqueAdapter
                    }

                    //this.list?.adapter = adapter as MusiqueAdapter
                }
            }
        }
    }

    private fun initialiseViews(view: View?) {
        searchview = view?.findViewById(R.id.edittext_fragment_album_search_view)
        recyclerView = view?.findViewById(R.id.recyclerview_fragment_album_list)
        appbar = view?.findViewById(R.id.appbar)
        motionLayout = view?.findViewById(R.id.motion_layout)
        fragmentTitle = view?.findViewById(R.id.title)
        searchView = view?.findViewById(R.id.edittext_fragment_album_search_view)
        menuButton = view?.findViewById(R.id.imagebutton_fragment_album_menu_button)
    }
}
