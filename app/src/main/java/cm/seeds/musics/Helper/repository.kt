package cm.seeds.musics.Helper

import android.content.Context
import android.net.Uri
import cm.seeds.musics.DataModels.Album
import cm.seeds.musics.DataModels.Artiste
import cm.seeds.musics.DataModels.Musique
import cm.seeds.musics.DataModels.Playlist

fun loadMusics(context: Context, pathOfPochetteAlbum: MutableMap<Int,String>) : MutableList<Musique>{
    return ProviderUtils.buildListOfMusicWithCursor(ProviderUtils.loadMusics(context),pathOfPochetteAlbum)
}

fun loadAlbums(context: Context, pathOfPochetteAlbum: MutableMap<Int, String>, pathOfArtistsAlbumsArt : MutableMap<Int, MutableList<String>>): MutableList<Album> {
    return ProviderUtils.buildListOfAlbumsWithCursor(ProviderUtils.loadAlbums(context),pathOfPochetteAlbum,pathOfArtistsAlbumsArt)
}

fun loadArtist(context: Context,pathOfArtistsAlbumsArt: MutableMap<Int, MutableList<String>>): MutableList<Artiste> {
    return ProviderUtils.buildListOfArtistsWithCursor(ProviderUtils.loadArtists(context),pathOfArtistsAlbumsArt)
}

fun loadPlaylists(context: Context): MutableList<Playlist> {
    return ProviderUtils.buildListOfPlaylistFromCursor(ProviderUtils.loadPlaylist(context))
}

fun loadAlbumMusics(context: Context, pathOfPochetteAlbum: MutableMap<Int, String>, album: Album) : MutableList<Musique>{
    return ProviderUtils.buildListOfMusicWithCursor(ProviderUtils.loadMusicsOfAlbums(context,album),pathOfPochetteAlbum)
}

fun loadPlaylistMusics(context: Context, pathOfPochetteAlbum: MutableMap<Int, String>, playlist: Playlist) : MutableList<Musique>{
    return ProviderUtils.buildListOfPlaylistMusicWithCursor(ProviderUtils.loadMusicsOfPlaylist(context,playlist),pathOfPochetteAlbum)
}

fun loadArtistMusics(context: Context, pathOfPochetteAlbum: MutableMap<Int, String>, artiste: Artiste) : MutableList<Musique>{
    return ProviderUtils.buildListOfMusicWithCursor(ProviderUtils.loadMusicsOfArtist(context,artiste),pathOfPochetteAlbum)
}

fun getMusicByUri(context: Context, uri : Uri, pathOfPochetteAlbum: MutableMap<Int, String>) : Musique?{
    return ProviderUtils.buildMusiqueWithCursor(ProviderUtils.loadMusicByUri(context,uri),pathOfPochetteAlbum)
}