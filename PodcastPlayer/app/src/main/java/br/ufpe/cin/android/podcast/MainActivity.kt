package br.ufpe.cin.android.podcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.AsyncTask
import android.util.Log
import androidx.annotation.UiThread
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.net.URL
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    companion object {
        val DL_COMPLETED = "br.ufpe.cin.android.podcast.PODCAST_DOWNLOAD_COMPLETED"
        var itemFeedList = ArrayList<ItemFeed>()
    }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == DL_COMPLETED) {
                listRecyclerView.adapter!!.notifyDataSetChanged()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Registra receiver para capturar a action de atualizar o botao de play ao terminar download
        val intentFilter = IntentFilter(DL_COMPLETED)
        registerReceiver(receiver, intentFilter)

        // Set up recyclerView
        listRecyclerView.layoutManager = LinearLayoutManager(this)
        listRecyclerView.adapter = ItemFeedAdapter(itemFeedList, applicationContext)
        listRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        // Download the rss feed in background.
        downloadRssFeed().execute(R.string.action_download)
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    internal inner class downloadRssFeed : AsyncTask<Int, Int, List<ItemFeed> >() {

        // In this method we evaluate the URL and make a get request. If we manage to download the feed,
        // it is parsed and stored in database. The database is then queried for the recently added records
        // and they are returned. Otherwise (in case of not having internet connection for example)
        // the data returned is fetched directly from the database and returned.
        override fun doInBackground(vararg resId: Int?): List<ItemFeed> {
            var parsedFeed: List<ItemFeed> = ArrayList<ItemFeed>()

            if(resId != null) {
                var uri = resId[0]
                if(uri != null){

                    var feed  = try {
                        URL(getString(uri)).readText()
                    } catch(e : Exception) {
                        null
                    }

                    parsedFeed = parseRssFeed(feed)

                }

            }

            val db = ItemFeedDB.getDatabase(applicationContext)

            for(item in parsedFeed){
                //Log.d("RET", item.toString())
                db.itemFeedDAO().insertItems(item)
            }

            // Retorna SELECT * do db ao invés do parsedFeed.
            return db.itemFeedDAO().allItems()
        }

        // After the async task completes, we need to pass the parsed feed to the adapter so it can
        // be properly displayed in our layout.
        override fun onPostExecute(result: List<ItemFeed>?) {
            if(result != null) {
                itemFeedList.addAll(result)
                listRecyclerView.adapter!!.notifyDataSetChanged()
            }
        }

        // This just calls the parser and return the correct values if succeeded,
        // otherwise return an empty ItemFeed list
        private fun parseRssFeed(feed: String?): List<ItemFeed>{
            var ret: List<ItemFeed> = ArrayList<ItemFeed>()

            if(feed != null) {
                ret = Parser.parse(feed)
            }

            return ret
        }
    }
}
