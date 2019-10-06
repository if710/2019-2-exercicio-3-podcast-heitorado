package br.ufpe.cin.android.podcast

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startForegroundService
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemlista.view.*

class ItemFeedAdapter (private val items: List<ItemFeed>, private val c: Context): RecyclerView.Adapter<ItemFeedAdapter.ViewHolder>() {
    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Here we inflate the XML 'itemLista' that corresponds to one single podcast item. After that we pass the inflated XML to the ViewHolder constructor
        // wich will trigger the onBindViewHolder and make all the necessary bindings. The ViewHolder is then returned tho whoever called the adapter, effectively showing what we wanted.
        val view = LayoutInflater.from(c).inflate(R.layout.itemlista, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val i = items[position]
        holder.titulo?.text = i.title
        holder.dataPublicacao?.text = i.pubDate

        holder.itemView.podcast_info.setOnClickListener {
            val intnt = Intent(c, EpisodeDetailActivity::class.java)

            // Not really why this flag is needed, but it makes the app work as intended.
            intnt.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            // Put extra info on the intent so the activity can access it later and display the info
            intnt.putExtra("podcast_title", i.title)
            intnt.putExtra("podcast_description", i.description)
            intnt.putExtra("podcast_dlLink", i.downloadLink)
            intnt.putExtra("podcast_link", i.link)
            intnt.putExtra("podcast_pubdate", i.pubDate)

            // Start the new activity with the provided intent and layout
            c.startActivity(intnt)
        }

        if(i.downloaded_file_path != ""){
            holder.itemView.btn_play.setOnClickListener {
                Toast.makeText(c, "Playing Podcast...", Toast.LENGTH_SHORT).show()
                var intnt = Intent(c, PlayEpisodeService::class.java)
                intnt.data = Uri.parse(i.downloaded_file_path)
                startForegroundService(c, intnt)
            }
        } else {
            holder.itemView.btn_play.isClickable = false
            holder.itemView.btn_play.isFocusable = false
            holder.itemView.btn_play.alpha = (0.1).toFloat()
        }



    }

    class ViewHolder (item : View) : RecyclerView.ViewHolder(item) {
        val titulo = item.item_title
        val dataPublicacao = item.item_dataPub
    }
}