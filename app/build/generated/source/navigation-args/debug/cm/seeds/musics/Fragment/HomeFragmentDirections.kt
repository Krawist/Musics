package cm.seeds.musics.Fragment

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import cm.seeds.musics.R

class HomeFragmentDirections private constructor() {
    companion object {
        fun actionHomeFragmentToAlbumFragment(): NavDirections =
                ActionOnlyNavDirections(R.id.action_homeFragment_to_albumFragment)
    }
}
