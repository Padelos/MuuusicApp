package com.example.muuusicapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MusicPlayerActivity extends AppCompatActivity implements MusicInterface {

    TextView titleTv, currentTimeTv, totalTimeTv;
    SeekBar seekBar;
    ImageView menuBtn, prevBtn, nextBtn, stopBtn, playPauseBtn, musicIcon;
    CardView playPauseLayout;

    AudioData currSong;

    MediaPlayer mediaPlayer = MyMediaPlayer.getMediaPlayer();
    boolean newSelectedSong;
    boolean rotated = false;
    Handler handler = new Handler();
    boolean connectedToService = false;
    MusicService musicService;

    private ServiceConnection servCon = new ServiceConnection ()
    {

        @Override
        public void onServiceConnected (ComponentName className, IBinder service)
        {
            Log.d("appRunning","MusicPlayerAct: onServiceConnected");
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            connectedToService = true;
            musicService.addToUpdates (MusicPlayerActivity.this);
            if (newSelectedSong && !rotated) {
                musicService.playMusic();
                initSelectedSong();
            }
        }

        @Override
        public void onServiceDisconnected (ComponentName CompNam)
        {
            connectedToService = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        Log.d("appRunning","MusicPlayerAct: onCreate");

        titleTv = findViewById(R.id.songTitleActMusicPlayer);
        currentTimeTv = findViewById(R.id.timeStartActMusicPlayer);
        totalTimeTv = findViewById(R.id.timeEndActMusicPlayer);
        seekBar = findViewById(R.id.seekBarActMusicPlayer);
        menuBtn = findViewById(R.id.showPlaylistActMusicPlayer);
        stopBtn = findViewById(R.id.stopBtnActMusicPlayer);
        prevBtn = findViewById(R.id.prevBtnActMusicPlayer);
        musicIcon = findViewById(R.id.songIconActMusicPlayer);
        playPauseLayout = findViewById(R.id.playPauseCVActMusicPlayer);
        playPauseBtn = findViewById(R.id.playPauseBtnActMusicPlayer);
        nextBtn = findViewById(R.id.nextBtnActMusicPlayer);


        if( getIntent() != null)
            newSelectedSong = (boolean) getIntent().getBooleanExtra("newSelectedSong",false);

        if(savedInstanceState != null)
            rotated = savedInstanceState.getBoolean("rotated",false);



        playPauseLayout.setOnClickListener(view -> {if(connectedToService) musicService.playPause();} );
        stopBtn.setOnClickListener(view -> {if(connectedToService) musicService.stopMusic();} );
        prevBtn.setOnClickListener(view -> {if(connectedToService) musicService.prevSong();} );
        nextBtn.setOnClickListener(view -> {if(connectedToService) musicService.nextSong();} );
        menuBtn.setOnClickListener(view -> menuList());

        initSelectedSong();


        MusicPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    currentTimeTv.setText(SongListAdapter.convertMsToMmSs(String.valueOf(mediaPlayer.getCurrentPosition())));
                    if (mediaPlayer.isPlaying())
                        playPauseBtn.setImageResource(R.drawable.pause_icon);
                    else
                        playPauseBtn.setImageResource(R.drawable.play_icon);
                }
                handler.postDelayed(this, 100);

            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser){
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mediaPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.start();
                MyMediaPlayer.stopPressed = false;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("appRunning","MusicPlayerAct: onStart");
        if(!connectedToService) {
            Intent musicInt = new Intent(MusicPlayerActivity.this, MusicService.class);
            bindService(musicInt, servCon, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("appRunning","MusicPlayerAct: onSaveInstanceState");
        if(outState != null)
            outState.putBoolean("rotated",true);
    }

    private void menuList() {
        Log.d("appRunning","MusicPlayerAct: menuList");
        super.onBackPressed();
    }

    void initSelectedSong(){
        Log.d("appRunning","MusicPlayerAct: initSelectedSong");
        currSong = MyMediaPlayer.songList.get(MyMediaPlayer.currentIndex);
        titleTv.setText(currSong.getTitle());
        titleTv.setSelected(true);
        totalTimeTv.setText(SongListAdapter.convertMsToMmSs(currSong.getDuration()));
        int tmp = Integer.parseInt(currSong.getDuration());
        seekBar.setMax(tmp);
    }

    public void prevSong(){
        Log.d("appRunning","MusicPlayerAct: prevSong");
        initSelectedSong();
        musicIcon.startAnimation((Animation) AnimationUtils.loadAnimation(getApplicationContext(), R.anim.prev_song_anim));
    }

    public void nextSong(){
        Log.d("appRunning","MusicPlayerAct: nexSong");
        initSelectedSong();
        musicIcon.startAnimation((Animation) AnimationUtils.loadAnimation(getApplicationContext(), R.anim.next_song_anim));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        handler.removeCallbacksAndMessages(null);
        unbindService(servCon);
        Log.d("appRunning","MusicPlayerAct: onDestroy");
    }


}