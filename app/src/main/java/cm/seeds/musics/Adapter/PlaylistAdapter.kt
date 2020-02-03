package cm.seeds.musics.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cm.seeds.musics.Activities.MainActivity
import cm.seeds.musics.DataModels.Playlist
import cm.seeds.musics.Fragment.SimpleDetailFragment
import cm.seeds.musics.R

class PlaylistAdapter(
    var playlists: List<Playlist>,
    private val layoutId: Int
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>(){

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

        fun bindData(playlist: Playlist, position: Int){
            nomPlaylist.text = playlist.nomPlaylist

            itemView.setOnClickListener {
                (context as MainActivity).navigateToDetail(playlist,
                    SimpleDetailFragment.PLAYLIST)
            }

            itemView.setOnLongClickListener(View.OnLongClickListener {

                (context as MainActivity).openMenu(
                    position,
                    playlists,
                    listOf(R.id.action_play_album_after,
                        R.id.action_add_album_to_playlist,
                        R.id.action_share_album,
                        R.id.action_delere_album),
                    null,
                    null
                )

                true

            })
        }

    }
}