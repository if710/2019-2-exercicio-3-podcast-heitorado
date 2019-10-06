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



class PlayEpisodeService : Service() {

    private var mPlayer: MediaPlayer? = null
    private var mStartID: Int = 0

    companion object {
        val PLAY_PAUSE_ACTION = "br.ufpe.cin.android.podcast.ACTION_PLAY_PAUSE"
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        // Cria notificação e botões de controle
        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

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

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == "br.ufpe.cin.android.podcast.ACTION_PLAY_PAUSE") {
                   play_pause_toggle()
                }
            }
        }
        registerReceiver(receiver, intentFilter)

        // Inicia service em foreground
        startForeground(1, notification)
    }

    override fun onStartCommand(i: Intent?, flags: Int, startId: Int): Int {

        mPlayer = MediaPlayer.create(this, i!!.data!!)
        mPlayer?.isLooping = false

        mPlayer?.setOnCompletionListener {
            stopSelf(mStartID)
            // colocar aqui pra apagar o podcast do armazenamento tb (e atualizar o banco)
        }


        if (mPlayer != null) {
            mStartID = startId
            if (mPlayer!!.isPlaying) {
                mPlayer?.seekTo(0) //Atualizar o valor do SEEKTO com o valor do banco de onde parou
            }
            else {
                mPlayer?.start()
            }
        }



        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        // guardar aqui onde parou a musica, pra voltar do mesmo lugar dps.
        mPlayer?.release()
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun play_pause_toggle(){
        if(mPlayer!!.isPlaying){
            mPlayer?.pause()
        } else {
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