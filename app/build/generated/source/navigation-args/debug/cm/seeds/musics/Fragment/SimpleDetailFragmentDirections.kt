package cm.seeds.musics.Fragment

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import cm.seeds.musics.R

class SimpleDetailFragmentDirections private constructor() {
    companion object {
        fun actionAlbumFragmentToDetailFragment(): NavDirections =
                ActionOnlyNavDirections(R.id.action_albumFragment_to_detailFragment)
    }
}
