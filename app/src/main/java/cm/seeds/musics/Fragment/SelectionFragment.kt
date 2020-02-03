package cm.seeds.musics.Fragment

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cm.seeds.musics.Activities.MainActivity
import cm.seeds.musics.Adapter.*
import cm.seeds.musics.DataModels.Album
import cm.seeds.musics.DataModels.Artiste
import cm.seeds.musics.DataModels.Musique
import cm.seeds.musics.DataModels.Playlist
import cm.seeds.musics.Helper.*
import cm.seeds.musics.R
import cm.seeds.musics.ViewModel.MusicsViewModel
import kotlin.properties.Delegates



/**
 * A simple [Fragment] subclass.
 * Use the [SelectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SelectionFragment : Fragment() {

    private var menuId by Delegates.notNull<Int>()
    private var typeOfData by Delegates.notNull<Int>()
    private var data by Delegates.notNull<List<*>>()
    private var model by Delegates.notNull<MusicsViewModel>()

    private var list by Delegates.notNull<RecyclerView>()
    private var positiveAction by Delegates.notNull<Button>()
    private var negativeAction by Delegates.notNull<Button>()
    private var adapter : Any? = null
    private var checkbox by Delegates.notNull<CheckBox>()
    private var nombreSelectionnes by Delegates.notNull<TextView>()
    private var selectAllButton by Delegates.notNull<Button>()
    private var searchView by Delegates.notNull<EditText>()

    companion object {
        const val TYPE_OF_DATA_MUSICS = 1
        const val TYPE_OF_DATA_PLAYLISTS = 2
        const val TYPE_OF_DATA_ARTISTS = 3
        const val TYPE_OF_DATA_ALBUMS = 4

        const val MENU_ID = "menu_id"
        const val TYPE_OF_DATA = "type_of_data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            menuId = it.getInt(MENU_ID)
            typeOfData = it.getInt(TYPE_OF_DATA)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_selection, container, false)

        initialiseViews(rootView)

        configureViewModel()

        setLayoutManager()

        configureAdapter()

        addActionOnViews()

        return rootView
    }

    private fun addActionOnViews() {

        negativeAction.setOnClickListener(View.OnClickListener {
            model.listOfSelectedData = MutableLiveData()
            (context as MainActivity).navigateBack(it)
        })

        positiveAction.setOnClickListener(View.OnClickListener {
            performsAction()
        })

        when(menuId){

            R.id.action_select_musics_to_add_to_playlist ->{
                positiveAction.setText(R.string.ajouter)
            }

            R.id.action_select_musics_to_share ->{
                positiveAction.setText(R.string.partager)
            }

            R.id.action_select_musics_to_delete ->{
                positiveAction.setText(R.string.supprimer)
            }

            R.id.action_select_musics_to_play ->{
                positiveAction.setText(R.string.jouer)
            }

        }

        checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            manageSelectAll(isChecked)
        }

        selectAllButton.setOnClickListener(View.OnClickListener {
            checkbox.isChecked = checkbox.isChecked.not()
        })

        searchView.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                launchSearch(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
    }

    private fun launchSearch(keyWord: String) {
        when(typeOfData){
            TYPE_OF_DATA_MUSICS ->{
                model.seachData.value = ProviderUtils.buildListOfMusicWithCursor(ProviderUtils.searchMusics(context!!,keyWord),model.pathOfPochetteAlbum)
            }

            TYPE_OF_DATA_ALBUMS ->{
                model.seachData.value = ProviderUtils.buildListOfAlbumsWithCursor(ProviderUtils.searchAlbums(context!!,keyWord),model.pathOfPochetteAlbum,model.pathOfArtistsAlbumsArt)
            }

            TYPE_OF_DATA_PLAYLISTS -> {
                model.seachData.value = ProviderUtils.buildListOfPlaylistFromCursor(ProviderUtils.searchPlaylist(context!!,keyWord))
            }

            TYPE_OF_DATA_ARTISTS -> {
                model.seachData.value = ProviderUtils.buildListOfArtistsWithCursor(ProviderUtils.searchArtist(context!!,keyWord),model.pathOfArtistsAlbumsArt)
            }
        }
    }

    private fun manageSelectAll(isChecked: Boolean) {
        if(isChecked){
            model.listOfSelectedData.value = data as MutableList<*>
        }else{
            model.listOfSelectedData = MutableLiveData()
        }

        when(typeOfData){

            TYPE_OF_DATA_ALBUMS ->{
                (adapter as AlbumAdapterForSelection).notifyDataSetChanged()
            }

            TYPE_OF_DATA_ARTISTS->{
                (adapter as ArtistAdapterForSelection).notifyDataSetChanged()
            }

            TYPE_OF_DATA_MUSICS ->{
                (adapter as MusiqueAdapterForSelection).notifyDataSetChanged()
            }

            TYPE_OF_DATA_PLAYLISTS ->{
                (adapter as PlaylistAdapterForSelection).notifyDataSetChanged()
            }

        }
    }

    private fun performsAction() {

        var listOfMusiques : List<Musique>? = null

        when(typeOfData){
            TYPE_OF_DATA_MUSICS ->{
                listOfMusiques = model.listOfSelectedData.value as List<Musique>?
                if(listOfMusiques==null || listOfMusiques.isEmpty()){
                    showToast(context!!,getString(R.string.veuillez_un_ou_plusieurs_element))
                }
            }

            TYPE_OF_DATA_PLAYLISTS -> {
                val listOfSelectedData = model.listOfSelectedData.value as List<Playlist>?
                if(listOfSelectedData!=null && listOfSelectedData.isNotEmpty()){
                    if(menuId==R.id.action_select_musics_to_delete){
                        deletePlaylists(context!!,listOfSelectedData as ArrayList<Playlist>)
                        listOfMusiques = null
                        closeFragment()
                    }else{
                        listOfMusiques = mutableListOf()
                        for(playlist in listOfSelectedData){
                            listOfMusiques.addAll(ProviderUtils.buildListOfPlaylistMusicWithCursor(ProviderUtils.loadMusicsOfPlaylist(context!!,playlist),model.pathOfPochetteAlbum))
                        }
                    }
                }else{
                    showToast(context!!,getString(R.string.veuillez_un_ou_plusieurs_element))
                }
            }

            TYPE_OF_DATA_ALBUMS -> {
                val listOfSelectedData = model.listOfSelectedData.value as List<Album>?
                if(listOfSelectedData!=null && listOfSelectedData.isNotEmpty()){
                    listOfMusiques = mutableListOf()
                    for(album in listOfSelectedData){
                        listOfMusiques.addAll(ProviderUtils.buildListOfMusicWithCursor(ProviderUtils.loadMusicsOfAlbums(context!!,album),model.pathOfPochetteAlbum))
                    }
                }else{
                    showToast(context!!,getString(R.string.veuillez_un_ou_plusieurs_element))
                }
            }

            TYPE_OF_DATA_ARTISTS -> {
                val listOfSelectedData = model.listOfSelectedData.value as List<Artiste>?
                if(listOfSelectedData!=null && listOfSelectedData.isNotEmpty()){
                    listOfMusiques = mutableListOf()
                    for(artiste in listOfSelectedData){
                        listOfMusiques.addAll(ProviderUtils.buildListOfMusicWithCursor(ProviderUtils.loadMusicsOfArtist(context!!,artiste),model.pathOfPochetteAlbum))
                    }
                }else{
                    showToast(context!!,getString(R.string.veuillez_un_ou_plusieurs_element))
                }
            }

        }

        when(menuId){

            R.id.action_select_musics_to_add_to_playlist ->{
                if(listOfMusiques!=null && listOfMusiques.isNotEmpty()){
                    chooseAndAddSongsToPlaylist(context!!,model.getAllPLaylist(),listOfMusiques)
                    closeFragment()
                }
            }

            R.id.action_select_musics_to_share ->{
                if(listOfMusiques!=null && listOfMusiques.isNotEmpty()){
                    shareMusics(context!!,listOfMusiques as ArrayList<Musique>)
                    closeFragment()
                }
            }

            R.id.action_select_musics_to_delete ->{
                if(listOfMusiques!=null && listOfMusiques.isNotEmpty()){
                    deleteMusics(context!!,listOfMusiques as ArrayList<Musique>)
                    closeFragment()
                }
            }

            R.id.action_select_musics_to_play ->{
                if(listOfMusiques!=null && listOfMusiques.isNotEmpty()){
                    (context as MainActivity).loadMusic(listOfMusiques[0],listOfMusiques as MutableList<Musique>,0,false)
                    closeFragment()
                }
            }

        }
    }

    private fun closeFragment(){
        model.listOfSelectedData = MutableLiveData()
        (context as MainActivity).navigateBack(positiveAction)
        return
    }

    private fun configureAdapter() {
        when(typeOfData){
            TYPE_OF_DATA_ALBUMS -> {
                val listAlbum = data as List<Album>
                if(adapter!=null){
                    (adapter as AlbumAdapterForSelection).setList(listAlbum)
                }else{
                    adapter = AlbumAdapterForSelection(context!!,listAlbum,
                        R.layout.album_item_full, model)
                }

                if(adapter != this.list.adapter){
                    this.list.adapter = adapter as AlbumAdapterForSelection
                }

                //list?.adapter = adapter as AlbumAdapter
            }

            TYPE_OF_DATA_ARTISTS-> {
                val listAlbum = data as List<Artiste>
                if(adapter!=null){
                    (adapter as ArtistAdapterForSelection).setList(listAlbum)
                }else{
                    adapter = ArtistAdapterForSelection(context!!,listAlbum,
                        R.layout.artist_item_full_width, model)
                }

                if(adapter != this.list.adapter){
                    this.list.adapter = adapter as ArtistAdapterForSelection
                }

                //list?.adapter = adapter as AlbumAdapter
            }

            TYPE_OF_DATA_PLAYLISTS ->{
                val list = data as List<Playlist>
                if(adapter!=null){
                    (adapter as PlaylistAdapterForSelection).setList(list)
                }else{
                    adapter = PlaylistAdapterForSelection(list,
                        R.layout.playlist_item_full, model)
                }

                if(adapter!= this.list.adapter){
                    this.list.adapter = adapter as PlaylistAdapterForSelection
                }
            }

            TYPE_OF_DATA_MUSICS ->{
                val list = data as MutableList<Musique>
                if(adapter!=null){
                    (adapter as MusiqueAdapterForSelection).setList(list)
                }else{
                    adapter = MusiqueAdapterForSelection(context, list,  model)
                }

                if(adapter != this.list.adapter){
                    this.list.adapter = adapter as MusiqueAdapterForSelection
                }

            }
        }
    }

    private fun configureViewModel() {
        model = MusicsViewModel.getInstance()

        data = model.listOfDataForSelection.value!!

        model.listOfSelectedData.observe(this, Observer {
            val nb = it?.size ?: 0
            nombreSelectionnes.text = if(nb<=1) nb.toString() +" "+ getString(R.string.selectionne) else (nb.toString()+" "+ getString(R.string.selectionnes))

            enabledPositiveButton(nb>0)

            checkbox.isChecked = nb==data.size

        })

        model.seachData.observe(this, Observer {
            data = it
            configureAdapter()
        })
    }

    private fun enabledPositiveButton(enable: Boolean) {
        positiveAction.isClickable = enable
        positiveAction.isEnabled = enable
        positiveAction.alpha = if(enable) 1.toFloat() else 0.3.toFloat()
    }

    private fun setLayoutManager() {
        when(typeOfData){

            TYPE_OF_DATA_ALBUMS->{
                list.layoutManager =GridLayoutManager(context!!, numberOfItemInLine(activity!!,R.dimen.album_item_full_good_size))
            }

            TYPE_OF_DATA_PLAYLISTS ->{
                list.layoutManager = GridLayoutManager(context!!, numberOfItemInLine(activity!!, R.dimen.playlist_item_full_good_size))
            }

            TYPE_OF_DATA_ARTISTS ->{
                list.layoutManager = GridLayoutManager(context!!, numberOfItemInLine(activity!!, R.dimen.artist_ietm_goog_width))
            }

            TYPE_OF_DATA_MUSICS -> {
                list.layoutManager = LinearLayoutManager(context!!)
            }

        }
    }

    private fun initialiseViews(rootView: View) {
        list = rootView.findViewById(R.id.recyclerview_fragment_selection_list)
        positiveAction = rootView.findViewById(R.id.button_dialog_positive_action)
        negativeAction = rootView.findViewById(R.id.button_dialog_negative_action)

        positiveAction.setTextColor(Color.WHITE)
        negativeAction.setTextColor(Color.WHITE)
        enabledPositiveButton(false)

        nombreSelectionnes = rootView.findViewById(R.id.textview_fragment_selection_nombre_object)
        checkbox = rootView.findViewById(R.id.checkbox_fragment_selection)
        selectAllButton = rootView.findViewById(R.id.button_fragment_selection_selectionner_tout)
        searchView = rootView.findViewById(R.id.edittext_fragment_selection_search_view)

        setSearchViewHint()
    }

    private fun setSearchViewHint() {
        when(typeOfData){

            TYPE_OF_DATA_PLAYLISTS ->{
                searchView.setHint(R.string.rechercher_une_playlist_ici)
            }

            TYPE_OF_DATA_MUSICS ->{
                searchView.setHint(R.string.rechercher_une_musique_ici)
            }

            TYPE_OF_DATA_ARTISTS ->{
                searchView.setHint(R.string.rechercher_un_artiste_ici)
            }

            TYPE_OF_DATA_ALBUMS ->{
                searchView.setHint(R.string.rechercher_un_album_ici)
            }
        }
    }
}