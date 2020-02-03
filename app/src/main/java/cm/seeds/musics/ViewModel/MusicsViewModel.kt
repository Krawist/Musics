package cm.seeds.musics.ViewModel

import android.app.Application
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cm.seeds.musics.DataModels.*
import cm.seeds.musics.Helper.*
import com.google.gson.Gson

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

    /* */
/*    val playingMusic : MutableLiveData<Musique> = MutableLiveData()
    val shuffleMode : MutableLiveData<Boolean> = MutableLiveData()
    val repeatMode : MutableLiveData<Int> = MutableLiveData()
    val playingQueue : MutableLiveData<MutableList<Musique>> = MutableLiveData()
    val originalPlayingQueue : MutableLiveData<MutableLiveData<Musique>> = MutableLiveData()*/


    var listOfDataForSelection : MutableLiveData<List<*>> = MutableLiveData()
    var listOfSelectedData : MutableLiveData<MutableList<*>> = MutableLiveData()

    var pathOfPochetteAlbum = mutableMapOf<Int, String>()
    var pathOfArtistsAlbumsArt = mutableMapOf<Int, MutableList<String>>()

    init {
        refreshData()
        initiActualPlayingState()
        instance = if(instance==null) this else instance
    }

    fun refreshData() {
        setAllAlbums(loadAlbums(context,pathOfPochetteAlbum,pathOfArtistsAlbumsArt))
        setAllMusics(loadMusics(context,pathOfPochetteAlbum))
        setBestMusics(loadMusics(context,pathOfPochetteAlbum))
        setAllPLaylist(loadPlaylists(context))
        setAllArtistes(loadArtist(context,pathOfArtistsAlbumsArt))
    }

    private fun initiActualPlayingState() {
        val initialActualMusicPlayingState = ActualMusicPlayingState(null,null,false,PlaybackStateCompat.REPEAT_MODE_NONE,false,0,null,null)
        val gson = Gson()
        val baseActualPlayerState = gson.toJson(initialActualMusicPlayingState)
        actualMusicPlayingState.value = gson.fromJson(context.getSharedPreferences(ACTUAL_PLAYER_STATE,Context.MODE_PRIVATE).getString(ACTUAL_PLAYER_STATE,baseActualPlayerState),ActualMusicPlayingState::class.java)
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
        musicOfAlbum.value = loadAlbumMusics(context, pathOfPochetteAlbum, album)
    }

    fun loadmusicOfPlaylist (context: Context, playlist: Playlist) {
        musicOfPlaylist.value = loadPlaylistMusics(context, pathOfPochetteAlbum, playlist)
    }

    fun loadmusicOfArtist (context: Context, artiste: Artiste) {
        musicOfArtist.value = loadArtistMusics(context, pathOfPochetteAlbum, artiste)
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