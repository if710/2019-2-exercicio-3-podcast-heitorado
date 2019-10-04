package br.ufpe.cin.android.podcast

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_episode_detail.*
import kotlinx.android.synthetic.main.itemlista.*
import kotlinx.android.synthetic.main.itemlista.item_title

class EpisodeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode_detail)

        item_title.text  = intent.extras?.getString("podcast_title")
        item_desc.text = intent.extras?.getString("podcast_description")
        item_dlLink.text = intent.extras?.getString("podcast_dlLink")
        item_src.text = intent.extras?.getString("podcast_link")
        item_pubDate.text = intent.extras?.getString("podcast_pubdate")

        btn_download.setOnClickListener {
            btn_download.isEnabled = false
            val dlservice = Intent(applicationContext, DownloadPodcastService::class.java)
            dlservice.data = Uri.parse(intent.extras?.getString("podcast_dlLink"))
            startService(dlservice)
        }
    }
}
