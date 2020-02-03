package cm.seeds.musics.DataModels

import android.net.Uri
import java.io.Serializable

data class Musique(val idMusique: Int,
              val idAlbum:Int,
              val idArtiste: Int,
              val titreMusique: String,
              val titreAlbum: String? = "",
              val nomArtiste: String? = "",
              val duration: Long,
              val pochette: String?,
              val path: String?,
              val track: Int,
              val size: Long,
               val musicRelativePath: String?): Serializable{
}