package cm.seeds.musics.Fragment

import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cm.seeds.musics.Activities.MainActivity
import cm.seeds.musics.Adapter.MusiqueAdapter
import cm.seeds.musics.DataModels.Album
import cm.seeds.musics.DataModels.Musique
import cm.seeds.musics.Helper.loadImageInImageView
import cm.seeds.musics.R
import cm.seeds.musics.ViewModel.MusicsViewModel

/**
 * A simple [Fragment] subclass.
 */
class DetailAlbumFragment : Fragment() {

    private lateinit var nomAlbum : TextView
    private lateinit var nomArtiste : TextView
    private lateinit var shuffleButton : ImageButton
    private lateinit var imageAlbum : ImageView
    private lateinit var listsMusique : RecyclerView
    private lateinit var menuButton: ImageButton

    private var album : Album? = null

    private lateinit var model : MusicsViewModel

    companion object{
        val WHAT_TO_DETAIL ="quoi_detailler"
        val ALBUM = "album"
    }

    private lateinit var musiques : MutableList<Musique>

    private var musiqueAdapter : MusiqueAdapter? = null

    private val contentObserver = object : ContentObserver(Handler()){
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            if(model!=null){
                model.loadmusicOfAlbum(context!!,album!!)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (arguments?.getString(WHAT_TO_DETAIL)){
            ALBUM -> album = arguments?.getSerializable(
                ALBUM
            ) as Album
        }

        context?.contentResolver?.registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,true, contentObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.contentResolver?.unregisterContentObserver(contentObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_detail_album, container, false)

        initialiseViews(view)

        configureViewModel()

        addDataToViews()

        addActionsOnViews()

        return view
    }

    private fun addActionsOnViews() {
        shuffleButton.setOnClickListener(View.OnClickListener {
            (context as MainActivity).loadMusic(musiques!!.get(0),musiques!!,0,true)
        })
    }

    private fun configureViewModel() {
        model = MusicsViewModel.getInstance()
        if(model!=null){
            model?.loadmusicOfAlbum(context!!,album!!)
            model?.musicOfAlbum?.observe(this, Observer {
                musiques = it
                configureAdapter()
            })

            model?.actualMusicPlayingState?.observe(this, Observer {
                if(it?.currentMusic!=null && it.indexOfPlayingSong!=null){
                    (musiqueAdapter as MusiqueAdapter).setPlayingMusic(it.currentMusic!!,it.indexOfPlayingSong!!)
                }
            })
        }

        menuButton.setOnClickListener(View.OnClickListener {
            (context as MainActivity).openMenu(
                0, musiques,
                listOf(R.id.action_select_musics_to_play,
                    R.id.action_select_musics_to_add_to_playlist,
                    R.id.action_select_musics_to_share,
                    R.id.action_select_musics_to_delete),
                SelectionFragment.TYPE_OF_DATA_MUSICS,
                musiques
            )
        })
    }

    private fun configureAdapter() {
        if(musiqueAdapter!=null){
            musiqueAdapter?.setList(musiques!!)
        }else{
            musiqueAdapter = MusiqueAdapter(context, musiques!!, true, model!!, false)
        }

        val isThat = listsMusique.adapter?.equals(musiqueAdapter)
        if(isThat==null){
            listsMusique.adapter = musiqueAdapter
        }else if(isThat.not()){
            listsMusique.adapter = musiqueAdapter
        }
    }

    private fun addDataToViews() {
        val s = album?.nomArtiste + " * " + album?.nombreMusique + " " + if (album!!.nombreMusique >= 2) getString(
                R.string.musiques
            ) else getString(R.string.musique)

        nomArtiste.text = s
        nomAlbum.text = album?.nomAlbum
        loadImageInImageView(
            context!!,
            album?.pochette,
            imageAlbum,
            false,
            50,
            R.drawable.headphones_black_and_white
        )
    }

    private fun initialiseViews(view:View) {
        nomAlbum = view.findViewById(R.id.textview_fragment_detail_album_nom_album)
        nomArtiste = view.findViewById(R.id.textview_fragment_detail_album_nom_artiste)
        shuffleButton = view.findViewById(R.id.floating_button_shuffle_all)
        imageAlbum = view.findViewById(R.id.imageview_fragment_detail_album_image_album)
        listsMusique = view.findViewById(R.id.recycler_view_fragment_detail_album_list_music)
        listsMusique.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        menuButton = view.findViewById(R.id.imagebutton_home_fragment_search_button)
    }

}
