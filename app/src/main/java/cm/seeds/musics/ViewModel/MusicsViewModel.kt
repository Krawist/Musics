package cm.seeds.musics.ViewModel

import android.app.Application
import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import cm.seeds.musics.DataModels.*
import cm.seeds.musics.Helper.*
import com.google.gson.Gson
import kotlin.properties.Delegates

class MusicsViewModel(application: Application, val context: Context) : AndroidViewModel(application) {

    val musicOfArtist: MutableLiveData<MutableList<Musique>> = MutableLiveData()
    val musicOfPlaylist: MutableLiveData<MutableList<Musique>> = MutableLiveData()
    val allmusics:MutableLiveData<MutableList<Musique>> = MutableLiveData()
    val allAlbums:MutableLiveData<MutableList<Album>> = MutableLiveData()
    val allPLaylist:MutableLiveData<MutableList<Playlist>> = MutableLiveData()
    val allBestMusics:MutableLiveData<MutableList<Musique>> = MutableLiveData()
    val allArtistes:MutableLiveData<MutableList<Artiste>> = MutableLiveData()
    val musicOfAlbum : MutableLiveData<MutableList<Musique>> = MutableLiveData()
    var actualMusicPlayingState : MutableLiveData<ActualMusicPlayingState> = MutableLiveData()
    var seachData : MutableLiveData<MutableList<*>> = MutableLiveData()

    var listOfDataForSelection : MutableLiveData<List<*>> = MutableLiveData()
    var listOfSelectedData : MutableLiveData<MutableList<*>> = MutableLiveData()

    var pathOfPochetteAlbum = mutableMapOf<Int, String>()
    var pathOfArtistsAlbumsArt = mutableMapOf<Int, MutableList<String>>()

    val currentMusic : MutableLiveData<Musique> = MutableLiveData()

    var favoriteSOngsId by Delegates.notNull<MutableList<Int>>()

    init {
        favoriteSOngsId = getFavoriteSong(context)
        refreshData()
        initiActualPlayingState()
        instance = if(instance==null) this else instance
    }

    fun refreshData() {
        setAllAlbums(loadAlbums(context,pathOfPochetteAlbum,pathOfArtistsAlbumsArt))
        setAllMusics(loadMusics(context,pathOfPochetteAlbum,favoriteSOngsId))
        setBestMusics(loadMusics(context,pathOfPochetteAlbum,favoriteSOngsId))
        setAllPLaylist(loadPlaylists(context))
        setAllArtistes(loadArtist(context,pathOfArtistsAlbumsArt))
    }

    private fun initiActualPlayingState() {
        val initialActualMusicPlayingState = ActualMusicPlayingState(null,null,false,PlaybackStateCompat.REPEAT_MODE_NONE,false,0,null,null)
        val gson = Gson()
        val baseActualPlayerState = gson.toJson(initialActualMusicPlayingState)
        val actual = gson.fromJson(context.getSharedPreferences(ACTUAL_PLAYER_STATE,Context.MODE_PRIVATE).getString(ACTUAL_PLAYER_STATE,baseActualPlayerState),ActualMusicPlayingState::class.java)
        actualMusicPlayingState.value = actual
    }

    companion object{
        private var instance : MusicsViewModel? = null

        @JvmStatic
        fun getInstance(): MusicsViewModel {
            return instance!!
        }

        @JvmStatic
        fun getInstance(application: Application, context: Context): MusicsViewModel {
            if(instance==null){
                instance = MusicsViewModel(application,context)
            }
            return instance!!
        }
    }

    fun loadmusicOfAlbum (context: Context, album: Album) {
        musicOfAlbum.value = loadAlbumMusics(context, pathOfPochetteAlbum, album,favoriteSOngsId)
    }

    fun loadmusicOfPlaylist (context: Context, playlist: Playlist) {
        musicOfPlaylist.value = loadPlaylistMusics(context, pathOfPochetteAlbum, playlist,favoriteSOngsId)
    }

    fun loadmusicOfArtist (context: Context, artiste: Artiste) {
        musicOfArtist.value = loadArtistMusics(context, pathOfPochetteAlbum, artiste,favoriteSOngsId)
    }

    private fun setAllAlbums(list: MutableList<Album>?){
        if(list!=null){
            allAlbums.value = list
        }
    }

    fun setAllArtistes(list: MutableList<Artiste>?){
        if(list!=null){
            allArtistes.value = list
        }
    }

    private fun setAllMusics(list: MutableList<Musique>?){
        if(list!=null){
            allmusics.value = list
        }
    }

    private fun setAllPLaylist(list: MutableList<Playlist>?){
        if(list!=null){
            allPLaylist.value = list
        }
    }

    private fun setBestMusics(list: MutableList<Musique>?){
        if(list!=null){
            allBestMusics.value = list
        }
    }

    fun getAllAlbums() : List<Album>{
        return allAlbums.value ?: mutableListOf()
    }

    fun getAllMusics() : List<Musique>{
        return allmusics.value?: mutableListOf()
    }

    fun getAllPLaylist():List<Playlist>{
        return allPLaylist.value?: mutableListOf()
    }

    fun getBestMusics():List<Musique>{
        return allBestMusics.value?: mutableListOf()
    }

    fun getAllMusicdOfAlbum() : List<Musique>?{
        return musicOfAlbum.value
    }

}