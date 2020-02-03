package cm.seeds.musics.Helper

import android.R
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class BasicViewHolder(view: View) : RecyclerView.ViewHolder(view){

    private var textView: TextView = itemView.findViewById(R.id.text1)

    fun getTextView(): TextView {
        return textView
    }
}