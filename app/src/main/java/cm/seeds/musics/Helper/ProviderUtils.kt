package cm.seeds.musics.Helper

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import cm.seeds.musics.DataModels.Album
import cm.seeds.musics.DataModels.Artiste
import cm.seeds.musics.DataModels.Musique
import cm.seeds.musics.DataModels.Playlist

class ProviderUtils {

    companion object{

        private val baseAlbumArtUri = "content://media/external/audio/albumart"

        fun loadMusics(context: Context): Cursor?{

             val fieldToget =  getProjectionFieldsOfMusic()
            val selection = MediaStore.Audio.Media.IS_MUSIC + " = ? "
            val selectionArgs = arrayOf("1")
            return context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,fieldToget,selection,selectionArgs,MediaStore.Audio.Media.TITLE)
        }

        private fun getProjectionFieldOfPlaylistMusic() : Array<String>{
            return if(Build.VERSION.SDK_INT>=29)
                arrayOf(MediaStore.Audio.Playlists.Members.AUDIO_ID,
                    MediaStore.Audio.Playlists.Members.ALBUM,
                    MediaStore.Audio.Playlists.Members.TITLE,
                    MediaStore.Audio.Playlists.Members.ALBUM_ID,
                    MediaStore.Audio.Playlists.Members.ARTIST,
                    MediaStore.Audio.Playlists.Members.ARTIST_ID,
                    MediaStore.Audio.Playlists.Members.DURATION,
                    MediaStore.Audio.Playlists.Members.TRACK,
                    MediaStore.Audio.Playlists.Members.IS_MUSIC,
                    MediaStore.Audio.Playlists.Members.SIZE,
                    MediaStore.Audio.Playlists.Members.BOOKMARK,
                    MediaStore.Audio.Playlists.Members.VOLUME_NAME)
            else
                arrayOf(MediaStore.Audio.Playlists.Members.AUDIO_ID,
                    MediaStore.Audio.Playlists.Members.ALBUM,
                    MediaStore.Audio.Playlists.Members.TITLE,
                    MediaStore.Audio.Playlists.Members.ALBUM_ID,
                    MediaStore.Audio.Playlists.Members.ARTIST,
                    MediaStore.Audio.Playlists.Members.ARTIST_ID,
                    MediaStore.Audio.Playlists.Members.DURATION,
                    MediaStore.Audio.Playlists.Members.TRACK,
                    MediaStore.Audio.Playlists.Members.DATA,
                    MediaStore.Audio.Playlists.Members.BOOKMARK,
                    MediaStore.Audio.Playlists.Members.IS_MUSIC,
                    MediaStore.Audio.Playlists.Members.SIZE)
        }

        private fun getProjectionFieldsOfMusic() : Array<String>{

            return if(Build.VERSION.SDK_INT>=29)
                arrayOf(MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ARTIST_ID,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.TRACK,
                    MediaStore.Audio.Media.IS_MUSIC,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.BOOKMARK,
                    MediaStore.Audio.Media.RELATIVE_PATH
                ) else
                arrayOf(MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ARTIST_ID,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.TRACK,
                    MediaStore.Audio.Media.IS_MUSIC,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.BOOKMARK)
        }

        private fun getProjectionFieldOfAlbums() : Array<String>{
            return arrayOf(MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.ARTIST_ID,
                MediaStore.Audio.Albums.ALBUM_ART,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS)
        }

        private fun getProjectionFieldOfArtist() : Array<String>{
            return arrayOf(MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
        }

        private fun getProjectionFieldsOfPlaylist() : Array<String>{
            return arrayOf(MediaStore.Audio.Playlists._ID,
                MediaStore.Audio.Playlists.NAME)
        }

        fun searchMusics(context: Context,keyWord : String) : Cursor?{
            val fieldToget =  getProjectionFieldsOfMusic()
            val selection = MediaStore.Audio.Media.IS_MUSIC + " = ? AND " + MediaStore.Audio.Media.TITLE + " LIKE ? "
            val selectionArgs = arrayOf("1", "%$keyWord%")
            return context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,fieldToget,selection,selectionArgs,MediaStore.Audio.Media.TITLE)
        }

        fun searchAlbums(context: Context,keyWord : String) : Cursor?{
            val fieldToget =  getProjectionFieldOfAlbums()
            val selection = MediaStore.Audio.Albums.ALBUM + " LIKE ? "
            val selectionArgs = arrayOf("%$keyWord%")
            return context.contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,fieldToget,selection,selectionArgs,MediaStore.Audio.Albums.ALBUM)
        }

        fun searchPlaylist(context: Context,keyWord : String) : Cursor?{
            val fieldToget = getProjectionFieldsOfPlaylist()
            val selection = MediaStore.Audio.Playlists.NAME + " LIKE ? "
            val selectionArgs = arrayOf("%$keyWord%")
            return context.contentResolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,fieldToget,selection,selectionArgs,MediaStore.Audio.Playlists.NAME)
        }

        fun searchArtist(context: Context,keyWord : String) : Cursor?{
            val fieldToget = getProjectionFieldOfArtist()
            val selection = MediaStore.Audio.Artists.ARTIST + " LIKE ? "
            val selectionArgs = arrayOf("%$keyWord%")
            return context.contentResolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,fieldToget,selection,selectionArgs,MediaStore.Audio.Artists.ARTIST)
        }

        fun loadMusicByUri(context: Context, uri : Uri) : Cursor?{
            val fieldToget = getProjectionFieldsOfMusic()
            /*
            val selection = MediaStore.Audio.Media.IS_MUSIC + " = ?" + " AND " + MediaStore.Audio.Media._ID + " = ?"
            val selectionArgs = arrayOf("1", musicId.toString())*/
            return context.contentResolver.query(uri,fieldToget,null,null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER)
        }

        fun loadMusicsOfAlbums(context: Context, album: Album) : Cursor?{

            val fieldToget = getProjectionFieldsOfMusic()

            val selection = MediaStore.Audio.Media.IS_MUSIC + " = ?" + " AND " + MediaStore.Audio.Media.ALBUM_ID + " = ?"
            val selectionArgs = arrayOf("1", album.idAlbum.toString())

            return context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,fieldToget,selection,selectionArgs,MediaStore.Audio.Media.TRACK)
        }

        fun loadMusicsOfArtist(context: Context, artiste: Artiste) : Cursor?{

            val fieldToget = getProjectionFieldsOfMusic()

            val selection = MediaStore.Audio.Media.IS_MUSIC + " = ?" + " AND " + MediaStore.Audio.Media.ARTIST_ID + " = ?"
            val selectionArgs = arrayOf("1", artiste.idArtiste.toString())

            return context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,fieldToget,selection,selectionArgs,MediaStore.Audio.Media.TRACK)
        }

        fun loadMusicsOfPlaylist(context: Context, playlist: Playlist) : Cursor?{

            val fieldToget = getProjectionFieldOfPlaylistMusic()

            val selection = MediaStore.Audio.Playlists.Members.IS_MUSIC + " = ?" + " AND " + MediaStore.Audio.Playlists.Members.PLAYLIST_ID + " = ?"
            val selectionArgs = arrayOf("1", playlist.idPlaylist.toString())

            return context.contentResolver.query(MediaStore.Audio.Playlists.Members.getContentUri("external", playlist.idPlaylist.toLong()), fieldToget,selection,selectionArgs, MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
        }

        fun buildListOfMusicWithCursor(cursor: Cursor?, pathOfSongAlbumArt:MutableMap<Int, String>): MutableList<Musique> {
            val listOfSong = mutableListOf<Musique>()
            if(cursor!=null){
                while (!cursor.isLast){
                    val musique =buildMusiqueWithCursor(cursor, pathOfSongAlbumArt)
                    if(musique!=null){
                        listOfSong.add(musique)
                    }
                }
            }
            return listOfSong
        }

         fun buildMusiqueWithCursor(cursor: Cursor?, pathOfSongAlbumArt: MutableMap<Int, String>): Musique? {
             if(cursor!=null) {
                 cursor.moveToNext()
                 val albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                 return Musique(
                     cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                     cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
                     cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)),
                     cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                     cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                     cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                     cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)),
                     pathOfSongAlbumArt[albumId],
                     if (Build.VERSION.SDK_INT >= 29) null else cursor.getString(
                         cursor.getColumnIndex(
                             MediaStore.Audio.Media.DATA
                         )
                     ),
                     cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)),
                     cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)),
                     if (Build.VERSION.SDK_INT >= 29) cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)) else null
                 )
             }else return null
        }

        fun buildListOfPlaylistMusicWithCursor(cursor: Cursor?, pathOfSongAlbumArt:MutableMap<Int, String>): MutableList<Musique> {
            val listOfSong = mutableListOf<Musique>()
            if(cursor!=null){
                while (cursor.moveToNext()){
                    listOfSong.add(buildPlaylistMusicWithCursor(cursor, pathOfSongAlbumArt))
                }
            }
            return listOfSong
        }

        private fun buildPlaylistMusicWithCursor(
            cursor: Cursor,
            pathOfSongAlbumArt: MutableMap<Int, String>
        ): Musique {
            val albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
            val musique = Musique(
                cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID)),
                cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM_ID)),
                cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST_ID)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST)),
                cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.DURATION)),
                pathOfSongAlbumArt[albumId],
                if (Build.VERSION.SDK_INT >= 29) null else cursor.getString(
                    cursor.getColumnIndex(
                        MediaStore.Audio.Media.DATA
                    )
                ),
                cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TRACK)),
                cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.SIZE)),
                if (Build.VERSION.SDK_INT >= 29) cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.VOLUME_NAME)) else null
            )
            return musique
        }

        fun loadAlbums(context: Context): Cursor?{

            val projection = getProjectionFieldOfAlbums()

            return context.contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,projection,null,null,MediaStore.Audio.Albums.DEFAULT_SORT_ORDER)

        }

        fun buildListOfAlbumsWithCursor(cursor: Cursor?, pathOfSongAlbumArt: MutableMap<Int, String>, pathOfArtistAlbumsArt: MutableMap<Int, MutableList<String>>): MutableList<Album> {
            val listOfAlbums = mutableListOf<Album>()
            if(cursor!=null){
                while (cursor?.moveToNext()){
                    val album = Album(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums._ID)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST_ID)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)))

                    if(Build.VERSION.SDK_INT>=29){
                        val uriOfAlbumArt = ContentUris.withAppendedId(Uri.parse(baseAlbumArtUri),album.idAlbum.toLong())
                        album.pochette = uriOfAlbumArt.toString()
                    }

                    val pochetteAlbum = if(album.pochette!=null) album.pochette!! else ""

                    pathOfSongAlbumArt[album.idAlbum] = pochetteAlbum

                    var mutableList = if(pathOfArtistAlbumsArt.containsKey(album.idArtiste)) pathOfArtistAlbumsArt[album.idArtiste] else mutableListOf(pochetteAlbum)

                    if(mutableList!=null){
                        if(!mutableList.contains(pochetteAlbum)){
                            mutableList.add(pochetteAlbum)
                        }
                    }else{
                        mutableList = mutableListOf(pochetteAlbum)
                    }

                    pathOfArtistAlbumsArt[album.idArtiste] = mutableList

                    listOfAlbums.add(album)
                }
            }

            return listOfAlbums;
        }

        fun loadArtists(context: Context): Cursor?{

            val projection = getProjectionFieldOfArtist()

            return context.contentResolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,projection,null,null,MediaStore.Audio.Artists.DEFAULT_SORT_ORDER)

        }

        fun buildListOfArtistsWithCursor(cursor: Cursor?, pathOfArtistAlbumsArt: MutableMap<Int, MutableList<String>>): MutableList<Artiste> {
            val listOfArtists = mutableListOf<Artiste>()
            if(cursor!=null){
                while (cursor.moveToNext()){
                    val id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists._ID))
                    val artistArtList : MutableList<String> = if(pathOfArtistAlbumsArt.containsKey(id) && pathOfArtistAlbumsArt[id]!=null) pathOfArtistAlbumsArt[id]!! else mutableListOf("")
                    val artiste = Artiste(id,
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)),
                        artistArtList)

                    listOfArtists.add(artiste)
                }
            }

            return listOfArtists;
        }

        fun loadPlaylist(context: Context): Cursor?{

            val projection = getProjectionFieldsOfPlaylist()

            return context.contentResolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,projection, null, null, MediaStore.Audio.Playlists.DATE_MODIFIED)
        }

        fun buildListOfPlaylistFromCursor(cursor: Cursor?): MutableList<Playlist>{
            val listOfPlaylist = mutableListOf<Playlist>()
            if(cursor!=null){
                while(cursor.moveToNext()){
                    val playlist = Playlist(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME)))

                        listOfPlaylist.add(playlist)
                }
            }

            return listOfPlaylist
        }

    }

}