package cm.seeds.musics.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cm.seeds.musics.Activities.MainActivity
import cm.seeds.musics.DataModels.Album
import cm.seeds.musics.Fragment.DetailAlbumFragment
import cm.seeds.musics.Helper.loadImageInImageView
import cm.seeds.musics.R

class AlbumAdapter(val context: Context?, private var albums: List<Album>?, private val layoutRes : Int) : RecyclerView.Adapter<AlbumAdapter.AlbumsViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumsViewHolder {
        return AlbumsViewHolder(LayoutInflater.from(parent.context).inflate(layoutRes, parent,false),parent.context)
    }

    override fun getItemCount(): Int {
       if(albums!=null){
           return albums!!.size
       }

        return 0
    }

    fun setList(albums: List<Album>){
        this.albums = albums
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: AlbumsViewHolder, position: Int) {
       holder.bindData(albums!![position],position)
    }

    inner class AlbumsViewHolder(val view: View, private val context: Context) : RecyclerView.ViewHolder(view){
        private var nomAlbum = itemView.findViewById<TextView>(R.id.textview_album_item_nom_album)
        private var imageAlbum = itemView.findViewById<ImageView>(R.id.imageview_album_item_pochette_album)


        fun bindData(album:Album, position:Int){
            nomAlbum.text = album.nomAlbum
            loadImageInImageView(
                context,
                album.pochette,
                imageAlbum,
                false,
                50,
                R.drawable.headphones_black_and_white
            )

            itemView.setOnLongClickListener(View.OnLongClickListener {

                (context as MainActivity).openMenu(
                    position,
                    albums!!,
                    listOf(R.id.action_play_album_after,
                        R.id.action_add_album_to_playlist,
                        R.id.action_share_album,
                        R.id.action_delere_album),
                    null,
                    null
                )
                true
            })

            itemView.setOnClickListener {
                (context as MainActivity).navigateToDetail(album,
                    DetailAlbumFragment.ALBUM)
            }
        }
    }
}