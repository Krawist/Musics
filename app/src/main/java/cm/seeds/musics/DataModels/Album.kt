package cm.seeds.musics.DataModels

import java.io.Serializable

class Album(val idAlbum: Int,
            val idArtiste: Int,
            val nomArtiste: String? = "",
            val nomAlbum: String? = "",
            val nombreMusique: Int,
            var pochette: String?) : Serializable{


}