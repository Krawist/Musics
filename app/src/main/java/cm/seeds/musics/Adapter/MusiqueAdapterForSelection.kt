package cm.seeds.musics.Adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
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

class MusiqueAdapterForSelection(
    val context: Context?,
    private var musiques: MutableList<Musique>,
    private val model: MusicsViewModel
) : RecyclerView.Adapter<MusiqueAdapterForSelection.MusiqueViewHolder>(){

    private val NUMBER_OF_DEFAULT_MIN_ITEM = 16
    private var indexOfLastPlayingSong = 0
    private var lastPlayingMusic = model.actualMusicPlayingState.value?.currentMusic

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusiqueViewHolder {
        return MusiqueViewHolder(LayoutInflater.from(parent.context).inflate(viewType,parent,false), context)
    }


    override fun getItemCount(): Int {
        return musiques.size
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.music_item
    }

    fun setList(musiques: MutableList<Musique>){
        this.musiques = musiques
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MusiqueViewHolder, position: Int) {
        holder.bindData(musiques[position],position)
    }

    inner class MusiqueViewHolder(val view: View, val context: Context?) : RecyclerView.ViewHolder(view) {
        private var trackNumber = itemView.findViewById<TextView>(R.id.textview_music_item_music_track_number)
        //private var playPauseButton = itemView.findViewById<ImageButton>(R.id.imagebutton_music_item_play_pause_button)
        private var pochetteMusique = itemView.findViewById<ImageView>(R.id.imageview_music_item_pochette_music)
        private var titreMusique = itemView.findViewById<TextView>(R.id.textview_music_item_titre_music)
        private var artisteMusique = itemView.findViewById<TextView>(R.id.textview_music_item_artiste)
        private var menuButton = itemView.findViewById<ImageButton>(R.id.imageviwbutton_music_item_menu_button)
        private var principalBackground = itemView.findViewById<LinearLayout>(R.id.layout_music_item_principal_background)
        private var decoratorView = itemView.findViewById<ImageView>(R.id.graph)
        private var checkbox = itemView.findViewById<CheckBox>(R.id.checkbox_music_item_checkkbox)

        fun bindData(musique: Musique, position: Int){

            trackNumber.text = (position + 1).toString()
            titreMusique.text = musique.titreMusique
            artisteMusique.text = musique.nomArtiste

            loadImageInImageView(
                context,
                musique.pochette,
                pochetteMusique,
                false,
                50,
                R.drawable.headphones_black_and_white
            )

            menuButton.visibility = GONE
            checkbox.visibility = VISIBLE
            checkbox.isChecked = (model.listOfSelectedData.value!=null && model.listOfSelectedData.value!!.contains(musique))


            itemView.setOnClickListener(View.OnClickListener {

                var listOfSelectedData = model.listOfSelectedData.value
                listOfSelectedData = if(listOfSelectedData!=null){
                    listOfSelectedData as MutableList<Musique>
                }else{
                    mutableListOf<Musique>()
                }

                if(listOfSelectedData!=null){
                    if((listOfSelectedData as MutableList<Musique>).contains(musique).not()){
                        (listOfSelectedData as MutableList<Musique>).add(musique)
                    }else{
                        (listOfSelectedData as MutableList<Musique>).remove(musique)
                    }
                }else{
                    listOfSelectedData = mutableListOf<Musique>()
                    (listOfSelectedData as MutableList<Musique>).add(musique)
                }

                model.listOfSelectedData.value = listOfSelectedData

                notifyItemChanged(position)
            })
        }

    }
}