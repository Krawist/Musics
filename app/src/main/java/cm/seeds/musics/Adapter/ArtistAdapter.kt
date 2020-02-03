package cm.seeds.musics.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cm.seeds.musics.Activities.MainActivity
import cm.seeds.musics.DataModels.Artiste
import cm.seeds.musics.Fragment.SimpleDetailFragment
import cm.seeds.musics.Helper.loadImageInImageView
import cm.seeds.musics.R

class ArtistAdapter(
    context: Context,
    private var artistes: List<Artiste>,
    private val layoutRes: Int
): RecyclerView.Adapter<ArtistAdapter.BestMusicViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestMusicViewHolder {
        return BestMusicViewHolder(LayoutInflater.from(parent.context).inflate(viewType,parent,false),parent.context)
    }

    override fun getItemCount(): Int {
        return artistes.size
    }

    override fun onBindViewHolder(holder: BestMusicViewHolder, position: Int) {
        holder.bindData(artistes[position],position)
    }

    fun setList(artistes: List<Artiste>){
        this.artistes = artistes
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return layoutRes
    }


    inner class BestMusicViewHolder(val view : View, val context: Context) : RecyclerView.ViewHolder(view){

        private val nomArtiste: TextView = view.findViewById(R.id.textview_most_played_music_titre_musique)
        private val detailArtiste: TextView = view.findViewById(R.id.textview_most_palyed_song_item_artist_and_album_names)
        private val image: ImageView = view.findViewById(R.id.imageview_most_played_song_pochette)


        fun bindData(artiste: Artiste, position:Int){
            nomArtiste.text = artiste.nomArtiste
            val detail = artiste.nombreAlbum.toString() +" " + if(artiste.nombreAlbum>1) context.getString(R.string.albums) else context.getString(R.string.album) + " * " + artiste.nombreMusique + " " + if(artiste.nombreMusique>1) context.getString(R.string.musiques) else context.getString(R.string.musique)
            detailArtiste.text = detail

            loadImageInImageView(context, artiste.albumsArtList[0], image, false, 50, R.drawable.headphones_black_and_white)

            itemView.setOnClickListener {
                (context as MainActivity).navigateToDetail(artiste, SimpleDetailFragment.ARTIST)
            }

            itemView.setOnLongClickListener(View.OnLongClickListener {

                (context as MainActivity).openMenu(
                    position,
                    artistes!!,
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