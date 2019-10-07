package br.ufpe.cin.android.podcast

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.lang.Exception
import java.net.URL
import java.util.ArrayList

class DownloadPodcastFeedWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {


        var parsedFeed: List<ItemFeed> = ArrayList<ItemFeed>()
        var uri = inputData.getString("download_url")

        if(uri != null){

            var feed  = try {
                URL(uri).readText()
            } catch(e : Exception) {
                null
            }

            parsedFeed = parseRssFeed(feed)
        }


        val db = ItemFeedDB.getDatabase(applicationContext)

        for(item in parsedFeed){
            db.itemFeedDAO().insertItems(item)
        }

        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(MainActivity.DL_COMPLETED))

        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }

    private fun parseRssFeed(feed: String?): List<ItemFeed>{
        var ret: List<ItemFeed> = ArrayList<ItemFeed>()

        if(feed != null) {
            ret = Parser.parse(feed)
        }

        return ret
    }
}