package cm.seeds.musics.Fragment

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import cm.seeds.musics.Adapter.AlbumAdapter
import cm.seeds.musics.Adapter.ArtistAdapter
import cm.seeds.musics.Adapter.MusiqueAdapter
import cm.seeds.musics.Adapter.PlaylistAdapter
import cm.seeds.musics.DataModels.Album
import cm.seeds.musics.DataModels.Artiste
import cm.seeds.musics.DataModels.Musique
import cm.seeds.musics.DataModels.Playlist
import cm.seeds.musics.R
import cm.seeds.musics.ViewModel.MusicsViewModel
import kotlin.math.abs

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    private lateinit var bestMusique : ViewPager2
    private lateinit var listAlbums : RecyclerView
    private lateinit var listsMusique: RecyclerView
    private lateinit var listPlaylist: RecyclerView

    private var artistAdapter: ArtistAdapter? = null
    private var albumAdapter : AlbumAdapter? = null
    private var  musiqueAdapter : MusiqueAdapter? = null
    private var playlistAdapter : PlaylistAdapter? = null

    private var albums:List<Album>? = null
    private var musiques:MutableList<Musique>?=null
    private var playlists:MutableList<Playlist>? = null
    private var allArtists:MutableList<Artiste>? = null

    private lateinit var model: MusicsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        initialiseViews(view)

        configureViewModel();

        addActionsOnViews()

        return view
    }

    private fun addActionsOnViews() {

    }

    private fun configureViewModel() {
        model = MusicsViewModel.getInstance()

        model.allAlbums.observe(this, Observer {
            albums = it
            configureAdapterAlbums()
        })

        model.allArtistes.observe(this, Observer {
            allArtists = it
            configureAdapterBestMusique()
        })

        model.allPLaylist.observe(this, Observer {
            playlists = it
            configureAdapterPlaylist()
        })

        model.allmusics.observe(this, Observer {
            musiques = it
            configureAdapterMusique()
        })

        model.currentMusic.observe(this, Observer {
            (musiqueAdapter as MusiqueAdapter).setPlayingMusic(it)
        })

    }

    private fun configureAdapterAlbums() {
        if(albumAdapter!=null){
            albumAdapter?.setList(albums!!)
        }else{
            albumAdapter = AlbumAdapter(context,albums!!,R.layout.album_item)
        }

        val isThat = listAlbums.adapter?.equals(albumAdapter)
        if(isThat==null){
            listAlbums.adapter = albumAdapter
        }else{
            if(isThat.not()){
                listAlbums.adapter = albumAdapter
            }
        }
    }

    private fun configureAdapterMusique() {
        if(musiqueAdapter!=null){
            musiqueAdapter?.setList(musiques!!)
        }else{
            musiqueAdapter = MusiqueAdapter(context, musiques!!, false, model, false)
        }

        val isThat = listsMusique.adapter?.equals(musiqueAdapter)
        if(isThat==null){
            listsMusique.adapter = musiqueAdapter
        }else if(isThat.not()){
            listsMusique.adapter = musiqueAdapter
        }
    }

    private fun configureAdapterBestMusique() {
        if(artistAdapter!=null){
            artistAdapter?.setList(allArtists!!)
        }else{
            artistAdapter = ArtistAdapter(
                context!!,
                allArtists!!,
                R.layout.artist_item_full_height_and_width
            )
        }

        val isThat = bestMusique.adapter?.equals(artistAdapter)
        if(isThat==null){
            bestMusique.adapter = artistAdapter
        }else if(isThat.not()){
            bestMusique.adapter = artistAdapter
        }
    }

    private fun configureAdapterPlaylist() {
        if(playlistAdapter!=null){
            playlistAdapter?.setList(playlists!!)
        }else{
            playlistAdapter = PlaylistAdapter(playlists!!, R.layout.playlist_item)
        }

        val isThat = listPlaylist.adapter?.equals(playlistAdapter)
        if(isThat==null){
            listPlaylist.adapter = playlistAdapter
        }else if(isThat.not()){
            listPlaylist.adapter = playlistAdapter
        }
    }

    private fun initialiseViews(view: View?) {
        if(view!=null){
            bestMusique = view.findViewById(R.id.viewpager_home_fragment_favorite_song)
            listAlbums = view.findViewById(R.id.recyclerview_home_fragment_list_album)
            listPlaylist = view.findViewById(R.id.recyclerview_home_fragment_list_playlist)
            listsMusique = view.findViewById(R.id.recyclerview_home_fragment_list_music)

            configureViewPagerAspect()

            listAlbums.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
            listPlaylist.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
            listsMusique.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        }
    }

    private fun configureViewPagerAspect() {
        bestMusique.offscreenPageLimit = 1
        val nextItemVisiblePx = resources.getDimension(R.dimen.viewpager_next_item_visible)
        val currentItemHorizontalMarginPx = resources.getDimension(R.dimen.viewpager_current_item_horizontal_margin)
        val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
        val pageTransformer = ViewPager2.PageTransformer { page, position ->
            page.translationX = -pageTranslationX * position
            page.scaleY = 1-(0.25f * abs(position))
        }
        bestMusique.setPageTransformer(pageTransformer)
        val itemDecoration = HorizontalMarginItemDecoration(context!!,R.dimen.viewpager_current_item_horizontal_margin)
        bestMusique.addItemDecoration(itemDecoration)
    }

    class HorizontalMarginItemDecoration(context: Context, @DimenRes horizontalMarginInDp: Int): RecyclerView.ItemDecoration(){
        private val horizontalMarginInPx: Int = context.resources.getDimension(horizontalMarginInDp).toInt()

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.right = horizontalMarginInPx
            outRect.left = horizontalMarginInPx
        }
    }

}
