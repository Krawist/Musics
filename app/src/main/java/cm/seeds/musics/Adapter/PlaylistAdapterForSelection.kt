package cm.seeds.musics.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cm.seeds.musics.Activities.MainActivity
import cm.seeds.musics.DataModels.Album
import cm.seeds.musics.DataModels.Musique
import cm.seeds.musics.DataModels.Playlist
import cm.seeds.musics.Fragment.SimpleDetailFragment
import cm.seeds.musics.R
import cm.seeds.musics.ViewModel.MusicsViewModel

class PlaylistAdapterForSelection(
    var playlists: List<Playlist>,
    private val layoutId: Int,
    private val model : MusicsViewModel
) : RecyclerView.Adapter<PlaylistAdapterForSelection.PlaylistViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        return PlaylistViewHolder(LayoutInflater.from(parent.context).inflate(layoutId,parent,false),parent.context)
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    fun setList(playlists: List<Playlist>){
        this.playlists = playlists
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bindData(playlists[position],position)
    }

    inner class PlaylistViewHolder(val view:View, val context: Context) : RecyclerView.ViewHolder(view){

        private var nomPlaylist = view.findViewById<TextView>(R.id.imageview_playlist_item_Nom_palylist)
        private var backgroundPlaylist = view.findViewById<ImageView>(R.id.imageview_playlist_item_background)
        private var checkBox = itemView.findViewById<CheckBox>(R.id.checkbox_playlist_item_full)

        fun bindData(playlist: Playlist, position: Int){
            nomPlaylist.text = playlist.nomPlaylist

            checkBox.visibility = View.VISIBLE
            checkBox.isChecked = (model.listOfSelectedData.value!=null && model.listOfSelectedData.value!!.contains(playlist))

            itemView.setOnClickListener(View.OnClickListener {
                var listOfSelectedData = model.listOfSelectedData.value
                listOfSelectedData = if(listOfSelectedData!=null){
                    listOfSelectedData as MutableList<Playlist>
                }else{
                    mutableListOf<Musique>()
                }

                if(listOfSelectedData!=null){
                    if((listOfSelectedData as MutableList<Playlist>).contains(playlist).not()){
                        (listOfSelectedData as MutableList<Playlist>).add(playlist)
                    }else{
                        (listOfSelectedData as MutableList<Playlist>).remove(playlist)
                    }
                }else{
                    listOfSelectedData = mutableListOf<Playlist>()
                    (listOfSelectedData as MutableList<Playlist>).add(playlist)
                }

                model.listOfSelectedData.value = listOfSelectedData

                notifyItemChanged(position)
            })
        }

    }
}