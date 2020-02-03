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
import cm.seeds.musics.Fragment.DetailAlbumFragment
import cm.seeds.musics.Helper.loadImageInImageView
import cm.seeds.musics.R
import cm.seeds.musics.ViewModel.MusicsViewModel

class AlbumAdapterForSelection(val context: Context?, private var albums: List<Album>, private val layoutRes : Int, private val model : MusicsViewModel) : RecyclerView.Adapter<AlbumAdapterForSelection.AlbumsViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumsViewHolder {
        return AlbumsViewHolder(LayoutInflater.from(parent.context).inflate(layoutRes, parent,false),parent.context)
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    fun setList(albums: List<Album>){
        this.albums = albums
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: AlbumsViewHolder, position: Int) {
       holder.bindData(albums[position],position)
    }

    inner class AlbumsViewHolder(val view: View, private val context: Context) : RecyclerView.ViewHolder(view){
        private var nomAlbum = itemView.findViewById<TextView>(R.id.textview_album_item_nom_album)
        private var imageAlbum = itemView.findViewById<ImageView>(R.id.imageview_album_item_pochette_album)
        private var checkBox = itemView.findViewById<CheckBox>(R.id.checkbox_album_item_full)


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

            checkBox.visibility = View.VISIBLE
            checkBox.isChecked = (model.listOfSelectedData.value!=null && model.listOfSelectedData.value!!.contains(album))

            itemView.setOnClickListener(View.OnClickListener {
                var listOfSelectedData = model.listOfSelectedData.value
                listOfSelectedData = if(listOfSelectedData!=null){
                    listOfSelectedData as MutableList<Album>
                }else{
                    mutableListOf<Album>()
                }

                if(listOfSelectedData!=null){
                    if((listOfSelectedData as MutableList<Album>).contains(album).not()){
                        (listOfSelectedData as MutableList<Album>).add(album)
                    }else{
                        (listOfSelectedData as MutableList<Album>).remove(album)
                    }
                }else{
                    listOfSelectedData = mutableListOf<Album>()
                    (listOfSelectedData as MutableList<Album>).add(album)
                }

                model.listOfSelectedData.value = listOfSelectedData

                notifyItemChanged(position)
            })
        }
    }
}