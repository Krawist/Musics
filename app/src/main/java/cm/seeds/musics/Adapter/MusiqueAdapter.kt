package cm.seeds.musics.Adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import cm.seeds.musics.Activities.MainActivity
import cm.seeds.musics.DataModels.Musique
import cm.seeds.musics.Helper.loadImageInImageView
import cm.seeds.musics.R
import cm.seeds.musics.ViewModel.MusicsViewModel
import java.util.*
import kotlin.math.max

class MusiqueAdapter(
    val context: Context?,
    private var musiques: MutableList<Musique>,
    private val showAll: Boolean,
    private val model: MusicsViewModel,
    private val canDrag: Boolean
) : RecyclerView.Adapter<MusiqueAdapter.MusiqueViewHolder>(){

    private val NUMBER_OF_DEFAULT_MIN_ITEM = 16
    private var indexOfLastPlayingSong = 0
    private var lastPlayingMusic = model.actualMusicPlayingState.value?.currentMusic

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusiqueViewHolder {
        return MusiqueViewHolder(LayoutInflater.from(parent.context).inflate(viewType,parent,false), context)
    }


    override fun getItemCount(): Int {
        if(musiques!=null){
            return if(showAll){
                musiques.size
            }else{
                if(musiques.size>NUMBER_OF_DEFAULT_MIN_ITEM){

                    NUMBER_OF_DEFAULT_MIN_ITEM
                }else{
                    musiques.size
                }
            }
        }

        return 0
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position==NUMBER_OF_DEFAULT_MIN_ITEM-1 && showAll.not()-> {
                R.layout.see_all_vertical_list_button_layout
            }
            else -> {
                R.layout.music_item
            }
        }
    }

    fun setList(musiques: MutableList<Musique>){
        this.musiques = musiques
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MusiqueViewHolder, position: Int) {
        holder.bindData(musiques[position],position)
    }

    fun setPlayingMusic(currentMusic: Musique, indexOfPlayingSong: Int) {
        if(lastPlayingMusic?.idMusique!=currentMusic.idMusique){
            lastPlayingMusic = currentMusic
            notifyItemChanged(indexOfLastPlayingSong)
            indexOfLastPlayingSong = indexOfPlayingSong
            notifyItemChanged(indexOfLastPlayingSong)
        }
    }

    fun moveItem(from: Int, to: Int) {
        Collections.swap(musiques,from,to)
        if(model.actualMusicPlayingState.value!=null &&
            model.actualMusicPlayingState.value?.playingQueue!=null &&
            !model.actualMusicPlayingState.value?.playingQueue.isNullOrEmpty() &&
            model.actualMusicPlayingState.value?.playingQueue!!.size> max(from,to)
        ){
            val musicToMove = model.actualMusicPlayingState.value?.playingQueue!![from]
            val musicThatIsPLaying = model.actualMusicPlayingState.value?.currentMusic

            if(musicThatIsPLaying!=null){
                if(musicToMove.idMusique==musicThatIsPLaying.idMusique){
                    model.actualMusicPlayingState.value?.indexOfPlayingSong = to
                }
            }
        }
        notifyItemChanged(from)
        notifyItemChanged(to)
       //Collections.swap(model.actualMusicPlayingState.value?.playingQueue,from)
    }

    inner class MusiqueViewHolder(val view: View, val context: Context?) : RecyclerView.ViewHolder(view) {
        private val trackNumber = itemView.findViewById<TextView>(R.id.textview_music_item_music_track_number)
        //private var playPauseButton = itemView.findViewById<ImageButton>(R.id.imagebutton_music_item_play_pause_button)
        private val pochetteMusique = itemView.findViewById<ImageView>(R.id.imageview_music_item_pochette_music)
        private val titreMusique = itemView.findViewById<TextView>(R.id.textview_music_item_titre_music)
        private val artisteMusique = itemView.findViewById<TextView>(R.id.textview_music_item_artiste)
        private val menuButton = itemView.findViewById<ImageButton>(R.id.imageviwbutton_music_item_menu_button)
        private val principalBackground = itemView.findViewById<LinearLayout>(R.id.layout_music_item_principal_background)
        private val decoratorView = itemView.findViewById<ImageView>(R.id.graph)
        private val dragview = itemView.findViewById<ImageButton>(R.id.imagebutton_drag)

        fun bindData(musique: Musique, position: Int){
           when{

               position==NUMBER_OF_DEFAULT_MIN_ITEM -1 && showAll.not() ->{
                   val buttonSeeAll = itemView.findViewById<Button>(R.id.button_see_all_vertical_layout_see_all)
                   buttonSeeAll.setOnClickListener(View.OnClickListener {
                       val view = View(itemView.context)
                       view.id = R.id.button_home_fragment_more_musics
                       (context as MainActivity).navigate(view)
                   })
               }

               else -> addActionsOnMusiqueItem(position, musique)
           }
        }

        private fun addActionsOnMusiqueItem(position: Int, musique: Musique) {
            trackNumber.text = (position + 1).toString()
            titreMusique.text = musique.titreMusique
            artisteMusique.text = musique.nomArtiste
            //playPauseButton.visibility = View.GONE
            loadImageInImageView(
                context,
                musique.pochette,
                pochetteMusique,
                false,
                50,
                R.drawable.headphones_black_and_white
            )

            /*            playPauseButton.setOnClickListener(View.OnClickListener {
                            (context as MainActivity).togglePlayPause()
                        })*/

            principalBackground.setOnClickListener(View.OnClickListener {
                (context as MainActivity).loadMusic(musique, musiques, position, false)
            })

            if(canDrag){
                trackNumber.visibility = GONE
                dragview.visibility = VISIBLE
            }else{
                trackNumber.visibility = VISIBLE
                dragview.visibility = GONE
            }

            menuButton.setOnClickListener(View.OnClickListener {

                val menuList = emptyList<Int>()

                (context as MainActivity).openMenu(
                    position,
                    musiques,
                    listOf(
                        R.id.action_play_music_after,
                        R.id.action_add_music_to_current_playlist,
                        R.id.action_share_music,
                        R.id.action_add_music_phone_playlist,
                        R.id.action_delete_music,
                        R.id.action_details_music
                    ),
                    null,
                    null
                )
            })

            if (musique.idMusique == lastPlayingMusic?.idMusique) {
                principalBackground.setBackgroundResource(R.drawable.music_item_baclground)
                //principalBackground.isClickable = false
                val slide = Slide(Gravity.START)
                TransitionManager.beginDelayedTransition(principalBackground, slide)
                decoratorView.visibility = View.VISIBLE
                //playPauseButton.visibility = View.VISIBLE
                indexOfLastPlayingSong = position
            } else {
                principalBackground.setBackgroundResource(android.R.color.transparent)
                val slide = Slide(Gravity.START)
                TransitionManager.beginDelayedTransition(principalBackground, slide)
                decoratorView.visibility = GONE
                //playPauseButton.visibility = GONE
                //principalBackground.isClickable = true
            }
        }

    }
}