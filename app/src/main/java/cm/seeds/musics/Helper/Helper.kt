package cm.seeds.musics.Helper

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cm.seeds.musics.Activities.MainActivity
import cm.seeds.musics.DataModels.Musique
import cm.seeds.musics.DataModels.Playlist
import cm.seeds.musics.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import java.io.File
import java.io.FileNotFoundException
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

import kotlin.math.pow


fun loadImageInImageView(
    context: Context?,
    imagePath: String?,
    imageView: ImageView,
    isCircle: Boolean,
    size: Int,
    defaultResourceId: Int
){
    val defInside = defaultResourceId

    if(isCircle){
        Glide.with(context)
            .load(imagePath)
            .asBitmap()
            .error(defInside)
            .centerCrop()
            .into(object : BitmapImageViewTarget(imageView) {
                override fun setResource(resource: Bitmap) {
                    val drawable =
                        RoundedBitmapDrawableFactory.create(
                            context!!.resources,
                            Bitmap.createScaledBitmap(resource, size,size, false)
                        )
                    drawable.isCircular = true
                    imageView.setImageDrawable(drawable)
                }
            })
    }else{

        Glide.with(context)
            .load(imagePath)
            .centerCrop()
            .crossFade()
            .error(defInside)
            .into(imageView)
    }

    /*if(imagePath!=null){
        if(isCircle){
            if(Build.VERSION.SDK_INT >=29){
                try {
                    var resource = context?.contentResolver?.loadThumbnail(Uri.parse(imagePath), Size(imageView.width,imageView.height),null)
                    if(resource==null){
                        resource = BitmapFactory.decodeResource(context?.resources,defInside)
                    }
                    val drawable =
                        RoundedBitmapDrawableFactory.create(
                            context!!.resources,
                            Bitmap.createScaledBitmap(resource!!, size, size, false)
                        )
                    drawable.isCircular = true
                    imageView.setImageDrawable(drawable)
                }catch (e : FileNotFoundException){
                    Glide.with(context)
                        .load(defaultResourceId)
                        .asBitmap()
                        .error(defInside)
                        .centerCrop()
                        .into(object : BitmapImageViewTarget(imageView) {
                            override fun setResource(resource: Bitmap) {
                                val drawable =
                                    RoundedBitmapDrawableFactory.create(
                                        context!!.resources,
                                        Bitmap.createScaledBitmap(resource, size,size, false)
                                    )
                                drawable.isCircular = true
                                imageView.setImageDrawable(drawable)
                            }
                        })
                }
            }else{
                Glide.with(context)
                    .load(imagePath)
                    .asBitmap()
                    .error(defInside)
                    .centerCrop()
                    .into(object : BitmapImageViewTarget(imageView) {
                        override fun setResource(resource: Bitmap) {
                            val drawable =
                                RoundedBitmapDrawableFactory.create(
                                    context!!.resources,
                                    Bitmap.createScaledBitmap(resource, size,size, false)
                                )
                            drawable.isCircular = true
                            imageView.setImageDrawable(drawable)
                        }
                    })
            }
        }else{
            if(Build.VERSION.SDK_INT>=29){
                try {
                    val bitmap = context?.contentResolver?.loadThumbnail(Uri.parse(imagePath), Size(imageView.width,imageView.height),null)
                    imageView.setImageBitmap(bitmap)
                }catch (e : FileNotFoundException){
                    Glide.with(context)
                        .load(defInside)
                        .centerCrop()
                        .crossFade()
                        .error(defInside)
                        .into(imageView)
                }
            }else{
                Glide.with(context)
                    .load(imagePath)
                    .centerCrop()
                    .crossFade()
                    .error(defInside)
                    .into(imageView)
            }
        }
    }else{
        Glide.with(context)
            .load(defInside)
            .centerCrop()
            .crossFade()
            .error(defInside)
            .into(imageView)
    }*/
}

fun numberOfItemInLine(activity: Activity, dimenRes: Int): Int {
    val display = activity.windowManager.defaultDisplay
    val size = Point();
    display.getSize(size);
    val with = size.x;
    val oneItemWidth = activity.resources.getDimension(dimenRes)
    val numberOfItemInLine = with / oneItemWidth

    return numberOfItemInLine.toInt()
}

fun getPochetteMusic(context: Context, song : Musique) : Bitmap?{
    var bitmap : Bitmap?
    if(song.pochette!=null){
        if(Build.VERSION.SDK_INT >=29){
            try {
                bitmap = context.contentResolver?.loadThumbnail(Uri.parse(song.pochette), Size(1000,1000),null)
                if(bitmap==null){
                    bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.headphones_black_and_white)
                }
            }catch (e : FileNotFoundException){
                return null
            }
        }else{
            bitmap = BitmapFactory.decodeFile(song.pochette)
            if(bitmap==null){
                bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.headphones_black_and_white)
            }
        }
    }else{
        bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.headphones_black_and_white)
    }

    return bitmap
}

fun showToast(context: Context, message : String){
    Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
}

fun formatDurationToString(duration: Long): String? {
    val hour: Long = TimeUnit.MILLISECONDS.toHours(duration)
    val minute: Long = TimeUnit.MILLISECONDS.toMinutes(duration) - hour * 60
    val second: Long = TimeUnit.MILLISECONDS.toSeconds(duration) - minute * 60
    var time = ""
    time = if (second < 10) "0$second" else second.toString()
    time = if (minute < 10) "0$minute:$time" else "$minute:$time"
    if (hour in 1..9) {
        time = "0$hour:$time"
    } else if (hour >= 10) {
        time = "$hour:$time"
    }
    return time
}

fun shareMusics(context: Context, listOfSong : ArrayList<Musique>){
    val intent = Intent(Intent.ACTION_SEND)
    if(listOfSong.size>1){
        intent.putExtra(Intent.EXTRA_STREAM,ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,(listOfSong[0].idMusique).toLong()))
    }else{
        val listUris = arrayListOf<Uri>()
        for (musique in listOfSong){
            listUris.add(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,(musique.idMusique).toLong()))
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,listUris)
    }
    intent.type = "audio/*"
    context.startActivity(Intent.createChooser(intent,context.getString(R.string.partager_via)))
}

fun deleteMusics(context: Context, listOfSong: ArrayList<Musique>){
    val warningDialog = Dialog(context)
    warningDialog.setContentView(R.layout.warning_dialog_layout)
    warningDialog.show()
    warningDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    val positiveButton = warningDialog.findViewById<Button>(R.id.button_dialog_positive_action)
    val negativeButton = warningDialog.findViewById<Button>(R.id.button_dialog_negative_action)
    positiveButton.setText(R.string.supprimer)

    positiveButton.setOnClickListener(View.OnClickListener {
        positiveButton.isEnabled = false
        negativeButton.isEnabled = false

        val f = {
            val task = object : AsyncTask<Void,Int,Void>() {

                lateinit var progressBar : ProgressBar
                lateinit var progress : TextView
                lateinit var progressLimit : TextView
                lateinit var dialogSongSuppression : Dialog

                override fun onPreExecute() {
                    super.onPreExecute()
                    dialogSongSuppression = Dialog(context)
                    dialogSongSuppression.setContentView(R.layout.dialog_waiting_process_with_progress_layout)
                    dialogSongSuppression.window?.setBackgroundDrawableResource(android.R.color.transparent)
                    progressBar = dialogSongSuppression.findViewById<ProgressBar>(R.id.progress_horizontal)
                    progress = dialogSongSuppression.findViewById<TextView>(R.id.textview_progress)
                    progressLimit = dialogSongSuppression.findViewById<TextView>(R.id.textview_progress_limit)
                    progressLimit.text = listOfSong.size.toString()
                    progressBar.max = listOfSong.size
                    progress.text = 0.toString()
                    dialogSongSuppression.setCanceledOnTouchOutside(false)
                    dialogSongSuppression.show()
                }

                override fun doInBackground(vararg params: Void?): Void? {
                    listOfSong.forEachIndexed { index, musique ->
                        val fileUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,(musique.idMusique).toLong())
                        if(Build.VERSION.SDK_INT>=29){
/*                            val file = File(fileUri.path)
                            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            val id = arrayOf(musique.idMusique.toString())
                            val whereClause = MediaStore.Audio.Media._ID + " ? "*/
                            context.contentResolver.delete(fileUri,null,null)
                            publishProgress(index)
                        }else{
                            val file = File(musique.path!!)
                            file.delete()
                            context.contentResolver.delete(fileUri,null,null)
                            publishProgress(index)
                        }

                    }

                    return null
                }

                override fun onProgressUpdate(vararg values: Int?) {
                    progressBar.progress = values[0]!!
                    progress.text = values[0]!!.toString()
                }

                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)
                    dialogSongSuppression.dismiss()
                    showToast(context,context.getString(R.string.suppression_effectuee))
                }
            }


            task.execute()

        }

        if(havePermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE,context.getString(R.string.permission_write_storage_message),MainActivity.WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)){
            f.invoke()
        }

        warningDialog.dismiss()
    })

    negativeButton.setOnClickListener(View.OnClickListener {
        warningDialog.dismiss()
    })
}

fun deletePlaylists(context: Context, listOfPlaylist: ArrayList<Playlist>){
    val warningDialog = Dialog(context)
    warningDialog.setContentView(R.layout.warning_dialog_layout)
    warningDialog.show()
    warningDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    val positiveButton = warningDialog.findViewById<Button>(R.id.button_dialog_positive_action)
    val negativeButton = warningDialog.findViewById<Button>(R.id.button_dialog_negative_action)
    positiveButton.setText(R.string.supprimer)

    positiveButton.setOnClickListener(View.OnClickListener {
        positiveButton.isEnabled = false
        negativeButton.isEnabled = false

        val f = {
            val task = object : AsyncTask<Void,Int,Void>() {

                lateinit var progressBar : ProgressBar
                lateinit var progress : TextView
                lateinit var progressLimit : TextView
                lateinit var dialogSongSuppression : Dialog

                override fun onPreExecute() {
                    super.onPreExecute()
                    dialogSongSuppression = Dialog(context)
                    dialogSongSuppression.setContentView(R.layout.dialog_waiting_process_with_progress_layout)
                    dialogSongSuppression.window?.setBackgroundDrawableResource(android.R.color.transparent)
                    progressBar = dialogSongSuppression.findViewById<ProgressBar>(R.id.progress_horizontal)
                    progress = dialogSongSuppression.findViewById<TextView>(R.id.textview_progress)
                    progressLimit = dialogSongSuppression.findViewById<TextView>(R.id.textview_progress_limit)
                    progressLimit.text = listOfPlaylist.size.toString()
                    progressBar.max = listOfPlaylist.size
                    progress.text = 0.toString()
                    dialogSongSuppression.setCanceledOnTouchOutside(false)
                    dialogSongSuppression.show()
                }

                override fun doInBackground(vararg params: Void?): Void? {
                    listOfPlaylist.forEachIndexed { index, playlist ->
                        val fileUri = ContentUris.withAppendedId(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,(playlist.idPlaylist).toLong())
                        context.contentResolver.delete(fileUri,null,null)
                        publishProgress(index)
                    }

                    return null
                }

                override fun onProgressUpdate(vararg values: Int?) {
                    progressBar.progress = values[0]!!
                    progress.text = values[0]!!.toString()
                }

                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)
                    dialogSongSuppression.dismiss()
                    showToast(context,context.getString(R.string.suppression_effectuee))
                }
            }


            task.execute()

        }

        if(havePermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE,context.getString(R.string.permission_write_storage_message),MainActivity.WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)){
            f.invoke()
        }

        warningDialog.dismiss()
    })

    negativeButton.setOnClickListener(View.OnClickListener {
        warningDialog.dismiss()
    })
}

fun havePermission(context: Context,permission: String, message: String, requestCode : Int): Boolean {
    when {
        ContextCompat.checkSelfPermission(context,permission)==PackageManager.PERMISSION_GRANTED -> {
            return true
        }
        ActivityCompat.shouldShowRequestPermissionRationale(context as Activity,permission) -> {
            // TODO show why permission
            askPermission(context,permission,requestCode)
        }
        else -> {
            askPermission(context,permission,requestCode)
        }
    }
    return false
}

fun askPermission(context: Context, permission: String,requestCode : Int) {
    ActivityCompat.requestPermissions(context as Activity, arrayOf(permission),requestCode)
}

fun getMusicTrack(context: Context, musique: Musique) : String {
    val track = musique.track
    val MILLE = 1000
    val DEUX_MILLE = 2000
    val TROIS_MILLE = 3000
    val QUATRE_MILLE = 4000
    val CINQ_MILLE = 5000

    return when (track) {
        in 1000..2000 -> track.rem(MILLE).toString()+""
        in 2000..3000 -> track.rem(DEUX_MILLE).toString() + context.getString(R.string.disque) + " 2"
        in 3000..4000 -> track.rem(TROIS_MILLE).toString() + context.getString(R.string.disque) + " 3"
        in 4000..5000 -> track.rem(QUATRE_MILLE).toString() + context.getString(R.string.disque) + " 4"
        in 5000..5999 -> track.rem(CINQ_MILLE).toString() + context.getString(R.string.disque) + " 5"
        else -> track.toString()+""
    }
}

fun getMusicSize(musique: Musique) : String{
    val format = DecimalFormat("#,##")
    val sizeInOctet = musique.size
    val BASE = 1024
    return if(sizeInOctet>BASE){
        if(sizeInOctet> BASE.toDouble().pow(2.toDouble())){
            if(sizeInOctet> BASE.toDouble().pow(3.toDouble())){
                if(sizeInOctet> BASE.toDouble().pow(4.toDouble())){
                    format.format(sizeInOctet/ BASE.toDouble().pow(4.toDouble()))+" TB";
                }else
                    format.format(sizeInOctet/ BASE.toDouble().pow(3.toDouble()))+" GB";
            }else
                format.format(sizeInOctet/(BASE.toDouble().pow(2.toDouble())))+" MB";
        }else{
            format.format(sizeInOctet/BASE)+" KB";
        }
    }else
        "$sizeInOctet O";
}

fun showDetailsSong(context: Context, musique: Musique){
        val dialog = Dialog(context);
        dialog.setContentView(R.layout.dialog_musique_details_layout)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val espace = ": "
        val titreAlbum = dialog.findViewById<TextView>(R.id.textview_dialog_music_details_titre_musique);
        val nomAlbum = dialog.findViewById<TextView>(R.id.textview_dialog_music_details_nom_album);
        val nomArtiste = dialog.findViewById<TextView>(R.id.textview_dialog_music_details_nom_artiste);
        //TextView genre = dialog.findViewById(R.id.textview_dialog_music_details_genre_musique);
        val  track = dialog.findViewById<TextView>(R.id.textview_dialog_music_details_track_musique);
        val taille = dialog.findViewById<TextView>(R.id.textview_dialog_music_details_taille_musique);
        val  duree = dialog.findViewById<TextView>(R.id.textview_dialog_music_details_duree_musique);
        val localisation = dialog.findViewById<TextView>(R.id.textview_dialog_music_details_localisation_musique);
        val buttonFermer = dialog.findViewById<TextView>(R.id.button_dialog_positive_action)

    dialog.findViewById<Button>(R.id.button_dialog_negative_action).visibility = GONE

        buttonFermer.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })

        titreAlbum.text = espace + musique.titreMusique
        nomAlbum.text = espace + musique.titreAlbum
        nomArtiste.text = espace + musique.nomArtiste
        track.text = espace + getMusicTrack(context, musique)
        taille.text = espace + getMusicSize(musique)
        duree.text = espace + formatDurationToString(musique.duration)

    if(Build.VERSION.SDK_INT>=29){
        localisation.text = espace + musique.musicRelativePath
    }else{
        localisation.text = espace + musique.path
    }

        dialog.show()
}

private fun addSongsToPlaylist(context: Context, playlist: Playlist, musiques : List<Musique>){

    val whatToDo = {
        if(musiques.isNotEmpty()){
            val cols = arrayOf("count ( * )")
            val uri = MediaStore.Audio.Playlists.Members.getContentUri("external",playlist.idPlaylist.toLong())
            val cur = context.contentResolver.query(uri,cols,null,null,null)
            if(cur!=null){
                cur.moveToFirst()
                val base = cur.getInt(0)
                cur.close()

                val contentValues = arrayOfNulls<ContentValues>(musiques.size)

                musiques.forEachIndexed{index, musique ->
                    val content = ContentValues()
                    content.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Integer.valueOf(base + musique.idMusique))
                    content.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, musique.idMusique)

                    contentValues[index] = content
                }

                val size = context.contentResolver.bulkInsert(uri,contentValues)

                if(size>0){
                    showToast(context,context.getString(R.string.ajoute_a) + " "+playlist.nomPlaylist)
                }else{
                    showToast(context,context.getString(R.string.erreur_survenue_veuillez_reesayez))
                }
            }
        }
    }

    if(havePermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE,context.getString(R.string.permission_write_storage_message),MainActivity.WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)){
        whatToDo.invoke()
    }
}

fun createAndAddSongToPlaylist(context: Context, playlistName : String, musiques: List<Musique>) {
    val whatToDo = {
        var uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Audio.Playlists.NAME,playlistName)

        uri = context.contentResolver.insert(uri,contentValues);
        if(uri!=null){
            val playlistId = ContentUris.parseId(uri)
            val newPlaylist = Playlist(playlistId.toInt(),playlistName)

            addSongsToPlaylist(context,newPlaylist,musiques);
        }
    }

    if(havePermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE,context.getString(R.string.permission_write_storage_message),MainActivity.WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)){
        whatToDo.invoke()
    }
}

fun chooseAndAddSongsToPlaylist(context: Context, playlists : List<Playlist>, musiques: List<Musique>) {
    if(musiques.isNotEmpty()){
        val dialogListOfPlaylist = Dialog(context)
        dialogListOfPlaylist.setContentView(R.layout.dialog_list_layout)
        dialogListOfPlaylist.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val list = dialogListOfPlaylist.findViewById<RecyclerView>(R.id.recyclerview_dialog_list_)
        dialogListOfPlaylist.setCanceledOnTouchOutside(false)

        val adapter = object : RecyclerView.Adapter<BasicViewHolder>() {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasicViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
                return BasicViewHolder(view)
            }

            override fun onBindViewHolder(holder: BasicViewHolder, position: Int) {
                val textView = holder.getTextView()
                if(position==playlists.size){
                    textView.text = context.getString(R.string.nouvelle_playlist)

                    textView.setOnClickListener(View.OnClickListener {
                        dialogListOfPlaylist.dismiss()
                        val dialogNewPlaylist = Dialog(context);
                        dialogNewPlaylist.setContentView(R.layout.dialog_with_one_textfied_layout)
                        dialogNewPlaylist.window?.setBackgroundDrawableResource(android.R.color.transparent)
                        val editText = dialogNewPlaylist.findViewById<EditText>(R.id.editext_dialog_with_one_field_edittext)
                        val positiveButton = dialogNewPlaylist.findViewById<Button>(R.id.button_dialog_positive_action)
                        val negativeButton = dialogNewPlaylist.findViewById<Button>(R.id.button_dialog_negative_action)
                        positiveButton.text = context.getString(R.string.valider)
                        negativeButton.text = context.getString(R.string.annuler)
                        dialogNewPlaylist.setCanceledOnTouchOutside(false)
                        dialogNewPlaylist.show()

                        positiveButton.setOnClickListener(View.OnClickListener {
                            val plalistName = editText.text.toString()
                            if(TextUtils.isEmpty(plalistName)){
                                showToast(context,context.getString(R.string.donner_un_nom_a_la_playlist))
                            }else{
                                dialogNewPlaylist.dismiss()
                                createAndAddSongToPlaylist(context, plalistName, musiques)
                                Toast.makeText(context, context.getString(R.string.ajoute_a)+" "+plalistName, Toast.LENGTH_SHORT).show();
                            }
                        })

                        negativeButton.setOnClickListener(View.OnClickListener {
                            dialogNewPlaylist.dismiss()
                        })
                    })

                }else{
                    textView.text = playlists[position].nomPlaylist
                    textView.setOnClickListener(View.OnClickListener {
                        addSongsToPlaylist(context, playlists.get(position), musiques)
                        dialogListOfPlaylist.dismiss();
                    })
                }
            }

            override fun getItemCount(): Int {
                return if(playlists!=null)
                    playlists.size + 1
                else
                    1
            }
        };

        val positiveAction = dialogListOfPlaylist.findViewById<Button>(R.id.button_dialog_positive_action)
        val negativeAction = dialogListOfPlaylist.findViewById<Button>(R.id.button_dialog_negative_action)

        positiveAction.visibility = View.GONE;
        negativeAction.text = context.getString(R.string.annuler);

        negativeAction.setOnClickListener(View.OnClickListener {
            dialogListOfPlaylist.dismiss()
        })

        list.layoutManager = LinearLayoutManager(context)
        list.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        list.adapter = adapter

        dialogListOfPlaylist.show()
    }
}
