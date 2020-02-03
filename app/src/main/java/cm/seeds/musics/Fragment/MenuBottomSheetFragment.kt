package cm.seeds.musics.Fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cm.seeds.musics.Activities.MainActivity
import cm.seeds.musics.Adapter.MusiqueAdapter
import cm.seeds.musics.DataModels.Musique
import cm.seeds.musics.Helper.chooseAndAddSongsToPlaylist
import cm.seeds.musics.Helper.deleteMusics
import cm.seeds.musics.Helper.shareMusics
import cm.seeds.musics.Helper.showDetailsSong
import cm.seeds.musics.PlayerService
import cm.seeds.musics.R
import cm.seeds.musics.ViewModel.MusicsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MenuBottomSheetFragment(
    private val itemPosition: Int,
    private val listOfData: List<*>,
    private val playerService: PlayerService,
    private val params: List<Int>,
    private var playingListMusics: MutableList<Musique>? = null,
    private val model: MusicsViewModel,
    private val typeOfData: Int?,
    private val moreData: List<*>?
)  : BottomSheetDialogFragment(){

    private var layoutContainer : LinearLayout? = null
    private var adapter : MusiqueAdapter? = null
    private var recyclerviewPlayingList : RecyclerView? = null

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(UP or DOWN,0){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val adapter = recyclerView.adapter as MusiqueAdapter
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition

                adapter.moveItem(from,to)
                adapter.notifyItemMoved(from,to)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            }
        }

        ItemTouchHelper(simpleItemTouchCallback)
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        if(playingListMusics==null){
            val rootView = LayoutInflater.from(context).inflate(R.layout.menu_bottom_sheet_layout,null)
            dialog.setContentView(rootView)

            layoutContainer = rootView.findViewById<LinearLayout>(R.id.linearlayout_menu_bottom_sheet_menu_item_container)
            layoutContainer?.removeAllViews()
            for (menuItemId in params){
                getMenuItemViaMenuId(menuItemId)
            }
        }else{
            val rootView = LayoutInflater.from(context).inflate(R.layout.playing_list_bottom_sheet_layout,null)
            dialog.setContentView(rootView)
            recyclerviewPlayingList = dialog.findViewById<RecyclerView>(R.id.recycler_view_playing_list)
            recyclerviewPlayingList?.layoutManager = LinearLayoutManager(context)
            configureAdapter()
        }

    }

    private fun configureAdapter() {
        if(adapter!=null){
            adapter?.setList(playingListMusics!!)
        }else{
            adapter = MusiqueAdapter(context, playingListMusics!!, true, model, true)
        }

        if(recyclerviewPlayingList?.adapter!=adapter){
            recyclerviewPlayingList?.adapter = adapter
            itemTouchHelper.attachToRecyclerView(recyclerviewPlayingList)
        }

        if(model.actualMusicPlayingState.value!=null && model.actualMusicPlayingState.value?.indexOfPlayingSong!=null){
            recyclerviewPlayingList?.scrollToPosition(model.actualMusicPlayingState.value?.indexOfPlayingSong!!)
        }
    }

    private fun getMenuItemViaMenuId(menuItemId : Int) : View{

        lateinit var nomMenu : String

        val rootView = LayoutInflater.from(context).inflate(R.layout.menu_item,layoutContainer,false)

        val view = rootView.findViewById<TextView>(R.id.textview_menu_item_title)

        val currentMusic =  model.actualMusicPlayingState.value?.currentMusic

        val playingQueue = model.actualMusicPlayingState.value?.playingQueue

        when (menuItemId){

            R.id.action_play_music_after ->{

                if((currentMusic!=null && currentMusic.idMusique!=(listOfData[itemPosition] as Musique).idMusique)){
                    view.text = getString(R.string.jouer_apres)

                    view.setOnClickListener(View.OnClickListener {
                        val nextPosition = playerService.getNextPosition()
                        if(nextPosition!=null){
                            playerService.addSong(nextPosition,listOfData[itemPosition] as Musique)
                        }
                        dismiss()
                    })

                    layoutContainer?.addView(rootView)
                }
            }

            R.id.action_add_music_phone_playlist ->{
                view.setText(R.string.ajouter_a_une_playlist)
                view.setOnClickListener(View.OnClickListener {
                    chooseAndAddSongsToPlaylist(context!!,model.getAllPLaylist(), listOf(listOfData[itemPosition] as Musique))
                    dismiss()
                })
                layoutContainer?.addView(rootView)
            }

            R.id.action_add_music_to_current_playlist ->{
                if(false == playingQueue?.contains(listOfData[itemPosition] as Musique)){
                    if((currentMusic!=null && currentMusic.idMusique!=(listOfData[itemPosition] as Musique).idMusique)){
                        view.text = getString(R.string.ajouter_la_liste_de_lecture)
                        view.setOnClickListener(View.OnClickListener {
                            playerService.addSong(listOfData[itemPosition] as Musique)
                            dismiss()
                        })
                        layoutContainer?.addView(rootView)
                    }
                }
            }

            R.id.action_share_music ->{
                view.setText(R.string.partager)

                view.setOnClickListener(View.OnClickListener {
                    val listOfSong = arrayListOf(listOfData[itemPosition] as Musique)
                    shareMusics(context!!,listOfSong)
                    dismiss()
                })
                layoutContainer?.addView(rootView)
            }

            R.id.action_delete_music ->{
                view.setText(R.string.supprimer)
                view.setOnClickListener(View.OnClickListener {
                    val listOfSong = arrayListOf(listOfData[itemPosition] as Musique)
                    deleteMusics(context!!,listOfSong)
                    dismiss()
                })
                layoutContainer?.addView(rootView)
            }

            R.id.action_details_music ->{
                view.setText(R.string.details)
                view.setOnClickListener(View.OnClickListener {
                    showDetailsSong(context!!,listOfData[itemPosition] as Musique)
                    dismiss()
                })
                layoutContainer?.addView(rootView)
            }

            R.id.action_play_album_after ->{
                view.text = getString(R.string.jouer_apres)

                view.setOnClickListener(View.OnClickListener {
                    val nextPosition = playerService.getNextPosition()
                    if(nextPosition!=null){
                        playerService.addSongs(nextPosition,listOfData as MutableList<Musique>)
                    }
                    dismiss()
                })
                layoutContainer?.addView(rootView)
            }

            R.id.action_add_album_to_playlist ->{
                view.text = getString(R.string.ajouter_la_liste_de_lecture)

                view.setOnClickListener(View.OnClickListener {
                    playerService.addSongs(listOfData as MutableList<Musique>)
                    dismiss()
                })
                layoutContainer?.addView(rootView)
            }

            R.id.action_share_album ->{
                view.setText(R.string.partager)

                view.setOnClickListener(View.OnClickListener {
                    val listOfSong = ArrayList(listOfData) as ArrayList<Musique>
                    shareMusics(playerService,listOfSong)
                    dismiss()
                })
                layoutContainer?.addView(rootView)
            }

            R.id.action_delere_album ->{
                view.setText(R.string.supprimer)
                view.setOnClickListener(View.OnClickListener {
                    val listOfSong = ArrayList(listOfData) as ArrayList<Musique>
                    deleteMusics(context!!,listOfSong)
                    dismiss()
                })
                layoutContainer?.addView(rootView)
            }

            R.id.action_select_musics_to_play ->{
                view.setText(R.string.selectionner_pour_jouer)
                view.setOnClickListener(View.OnClickListener {
                    (context as MainActivity).navigationToSelection(R.id.action_select_musics_to_play,typeOfData!!,moreData!!)
                    dismiss()
                })
                layoutContainer?.addView(rootView)
            }

            R.id.action_select_musics_to_delete ->{
                view.setText(R.string.supprimer)
                view.setOnClickListener(View.OnClickListener {
                    (context as MainActivity).navigationToSelection(R.id.action_select_musics_to_delete,typeOfData!!,moreData!!)
                    dismiss()
                })
                layoutContainer?.addView(rootView)
            }

            R.id.action_select_musics_to_share ->{
                view.setText(R.string.partager)
                view.setOnClickListener(View.OnClickListener {
                    (context as MainActivity).navigationToSelection(R.id.action_select_musics_to_share,typeOfData!!,moreData!!)
                    dismiss()
                })
                layoutContainer?.addView(rootView)
            }

            R.id.action_select_musics_to_add_to_playlist ->{
                view.setText(R.string.ajouter_a_une_playlist)
                view.setOnClickListener(View.OnClickListener {
                    (context as MainActivity).navigationToSelection(R.id.action_select_musics_to_add_to_playlist,typeOfData!!,moreData!!)
                    dismiss()
                })
                layoutContainer?.addView(rootView)
            }
        }

        return rootView
    }

    fun setList(currentPlayingList: MutableList<Musique>) {
        playingListMusics = currentPlayingList
        adapter?.setList(currentPlayingList)
    }

    fun setPlayingMusic(currentMusic: Musique, indexOfPlayingSong: Int) {
        if(adapter!=null){
            adapter?.setPlayingMusic(currentMusic, indexOfPlayingSong)
        }
    }

}