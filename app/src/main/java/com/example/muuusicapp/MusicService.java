package com.example.muuusicapp;


import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;


public class MusicService extends Service {

    int totalConnections = 0;
    ArrayList<MusicInterface> updateActs = new ArrayList<>();
    MediaPlayer mediaPlayer = MyMediaPlayer.getMediaPlayer();
    AudioData currSong;
    PhoneCallReceiver phoneCallReceiver;

    public MusicService(){

    }

    @Override
    public void onCreate() {
        Log.d("appRunning","MusicService: onCreate");
        super.onCreate();

        phoneCallReceiver = new PhoneCallReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(phoneCallReceiver, intentFilter);

        mediaPlayer.setOnCompletionListener(mp -> nextSong());

    }


    @Override
    public void onDestroy ()
    {
        Log.d("appRunning","MusicService: onDestroy");
        super.onDestroy();
        unregisterReceiver(phoneCallReceiver);
        MyMediaPlayer.stopMediaPlayer();
    }

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {

        MusicService getService(){
            Log.d("appRunning","MusicService: getService");
            return MusicService.this;
        }


    }

    @Override
    public IBinder onBind(Intent intent) {
        this.totalConnections++;
        Log.d("appRunning","MusicService: onBind");
//        if(intent != null)
//            intent.getBooleanExtra("VISIBLE",false);
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        this.totalConnections--;
        Log.d("appRunning","MusicService: onUnBind");
        if(this.totalConnections == 0)
            MyMediaPlayer.stopMediaPlayer();
//            new Handler().postDelayed(()->{},5000);
        return false;
    }

    public void addToUpdates(MusicInterface i){
        Log.d("appRunning","MusicService: addToUpdates");
        updateActs.add(i);
    }


    private void updateActs(String info){ // TODO: change the try-catch inside the loop

        Log.d("appRunning","MusicService: updateActs");
        if(info.equals("NEXT"))
            for (MusicInterface mi : updateActs)
                mi.nextSong();

        else if (info.equals("PREV"))
            for (MusicInterface mi : updateActs)
                mi.prevSong();

    }


    public void playMusic(){
        Log.d("appRunning","MusicService: playMusic ***************");
        if(MyMediaPlayer.currentIndex < 0)
            return;

        Log.d("appRunning","MusicService: playMusic");

        currSong = MyMediaPlayer.songList.get (MyMediaPlayer.currentIndex); // TODO: check if it's gonna be a problem from main to here

        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(currSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.seekTo(0);

            if (!MyMediaPlayer.stopPressed)
                mediaPlayer.start();

        } catch (IOException e) {
            Toast.makeText(MusicService.this,"Can't play the song",Toast.LENGTH_LONG).show();
        }

    }

    public void playPause(){

        if(MyMediaPlayer.currentIndex < 0)
            return;

        Log.d("appRunning","MusicService: playPause");

        MyMediaPlayer.stopPressed = false;

        if(mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.start();
    }

    public void stopMusic(){

        if (MyMediaPlayer.stopPressed)
            return;

        Log.d("appRunning","MusicService: stopMusic");

        mediaPlayer.pause();
        mediaPlayer.seekTo(0);
        MyMediaPlayer.stopPressed = true;
    }

    public void prevSong(){

        if(MyMediaPlayer.currentIndex < 0)
            return;

        Log.d("appRunning","MusicService: prevSong");

        int totalSize = MyMediaPlayer.songList.size();
        MyMediaPlayer.currentIndex = (MyMediaPlayer.currentIndex-1) < 0 ? totalSize-1 : MyMediaPlayer.currentIndex-1;
        mediaPlayer.reset();
        playMusic();
        updateActs("PREV");
    }

    public void nextSong(){

        if(MyMediaPlayer.currentIndex < 0)
            return;

        Log.d("appRunning","MusicService: nextSong");

        int totalSize = MyMediaPlayer.songList.size();
        MyMediaPlayer.currentIndex = (MyMediaPlayer.currentIndex + 1) % totalSize;
        mediaPlayer.reset();
        playMusic();
        updateActs("NEXT");
    }

}
