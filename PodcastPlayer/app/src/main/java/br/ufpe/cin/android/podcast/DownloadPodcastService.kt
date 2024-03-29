package br.ufpe.cin.android.podcast

import android.app.IntentService
import android.content.Intent
import android.os.Environment.getExternalStoragePublicDirectory
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class DownloadPodcastService : IntentService("DownloadPodcastService") {
    public override fun onHandleIntent(i: Intent?) {
        try {

            val root = getExternalFilesDir(DIRECTORY_DOWNLOADS)
            root?.mkdirs()
            val output = File(root, i!!.data!!.lastPathSegment)
            if (output.exists()) {
                output.delete()
            }
            val url = URL(i.data!!.toString())
            val c = url.openConnection() as HttpURLConnection
            val fos = FileOutputStream(output.path)
            val out = BufferedOutputStream(fos)
            try {
                val `in` = c.inputStream
                val buffer = ByteArray(8192)
                var len = `in`.read(buffer)
                while (len >= 0) {
                    out.write(buffer, 0, len)
                    len = `in`.read(buffer)

                    Log.e("DL_STATUS", "downloading.....")
                }
                out.flush()
            } finally {

                val db = ItemFeedDB.getDatabase(applicationContext)

                var podcast_item = db.itemFeedDAO().searchItemByDownloadLink(i.data!!.toString())

                podcast_item.downloaded_file_path = output.path

                db.itemFeedDAO().updateItems(podcast_item)
                MainActivity.itemFeedList.clear()
                MainActivity.itemFeedList.addAll(db.itemFeedDAO().allItems())

                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(MainActivity.DL_COMPLETED))
                Log.e("DL_STATUS", "DOWNLOADED!!!")


                fos.fd.sync()
                out.close()
                c.disconnect()
            }

        } catch (e2: IOException) {
            Log.e(javaClass.getName(), "Exception durante download", e2)
        }

    }
}