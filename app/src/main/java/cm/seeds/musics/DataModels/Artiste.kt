package cm.seeds.musics.DataModels

import java.io.Serializable

class Artiste(val idArtiste: Int,
              val nomArtiste: String,
              val nombreMusique: Int,
              val nombreAlbum: Int,
              val albumsArtList: MutableList<String>) : Serializable {

}