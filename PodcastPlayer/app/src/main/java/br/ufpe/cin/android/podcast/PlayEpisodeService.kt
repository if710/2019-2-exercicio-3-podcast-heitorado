package br.ufpe.cin.android.podcast

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import org.jetbrains.anko.doAsync
import java.io.File
import java.nio.file.Files.delete





class PlayEpisodeService : Service() {

    private var mPlayer: MediaPlayer? = null
    private var mStartID: Int = 0

    companion object {
        val PLAY_PAUSE_ACTION = "br.ufpe.cin.android.podcast.ACTION_PLAY_PAUSE"
        var podcast_key = ""
    }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == "br.ufpe.cin.android.podcast.ACTION_PLAY_PAUSE") {
                play_pause_toggle()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        // Cria notificação e botões de controle

        //// Intent para ao clicar na notifcação, retornar para a MainActivity
        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        // Pending intent que envia a ação de PLAY_PAUSE
        val playPauseIntent = Intent(PLAY_PAUSE_ACTION)
        val pendingPlayPauseIntent = PendingIntent.getBroadcast(this, 100, playPauseIntent, 0)

        val notification: Notification = NotificationCompat.Builder(this, "PodcastPlayerChannel")
            .setContentTitle("Tocando seu podcast")
            .setContentText("Hooray!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(R.drawable.ic_pause, "play/pause", pendingPlayPauseIntent)
            .setContentIntent(pendingIntent)
            .setTicker("Ticker")
            .build()

        // Registra receiver para capturar a action de play/pause
        val intentFilter = IntentFilter(PLAY_PAUSE_ACTION)

        registerReceiver(receiver, intentFilter)

        // Inicia service em foreground
        startForeground(1, notification)
    }

    override fun onStartCommand(i: Intent?, flags: Int, startId: Int): Int {

        podcast_key = i!!.extras?.getString("podcast_dlLink")?:""

        mPlayer = MediaPlayer.create(this, i!!.data!!)
        mPlayer?.isLooping = false

        mPlayer?.setOnCompletionListener {
            // Apaga o podcast do armazenamento e atualiza o banco

            doAsync {
                var file = File(i!!.data!!.toString())
                val deleted = file.delete()
                if (deleted) {
                    val db = ItemFeedDB.getDatabase(applicationContext)
                    var item = db.itemFeedDAO().searchItemByStoragePath(i!!.data!!.toString())
                    item.downloaded_file_path = ""
                    db.itemFeedDAO().updateItems(item)
                } else {
                    Log.e("PlayEpisodeService", "Não foi possivel excluir podcast do armazenamento")
                }
            }

            // Para o serviço
            stopSelf(mStartID)
        }


        if (mPlayer != null) {
            mStartID = startId
            if (mPlayer!!.isPlaying) {
                mPlayer?.seekTo(0)
            }
            else {
                // Atualizar o valor do SEEKTO com o valor armazenado no banco de onde parou
                mPlayer?.seekTo(i!!.extras?.getInt("podcast_stopped_at")?:0)
                mPlayer?.start()
            }
        }



        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {

        unregisterReceiver(receiver)
        mPlayer?.release()
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun play_pause_toggle(){
        // Se já está tocando:
        // 1 - Guarda onde parou o podcast, pra voltar do mesmo lugar depois.
        // 2 - Pausa o mPlayer
        if(mPlayer!!.isPlaying){
            doAsync {
                val db = ItemFeedDB.getDatabase(applicationContext)
                var item = db.itemFeedDAO().searchItemByDownloadLink(podcast_key)
                item.stopped_at = mPlayer?.currentPosition?:0
                db.itemFeedDAO().updateItems(item)
            }

            mPlayer?.pause()
        } else { // Se não estiver tocando: começa a tocar.
            mPlayer?.start()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "PodcastPlayerChannel",
                "Podcast Player Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }
}