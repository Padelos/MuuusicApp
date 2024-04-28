package com.example.muuusicapp;

import android.media.MediaPlayer;
import android.util.Log;

import java.util.ArrayList;

public class MyMediaPlayer {

    private static MediaPlayer mediaPlayer = null;
    public static int currentIndex = -1;

    public static boolean stopPressed = false;

    public static ArrayList<AudioData> songList = new ArrayList<>();

    public static  MediaPlayer getMediaPlayer(){

        Log.d("appRunning","MyMediaPlayer: getMediaPlayer");
        if (mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
        }
        return mediaPlayer;
    }

    public static void stopMediaPlayer(){

        Log.d("appRunning","MyMediaPlayer: stopMediaPlayer");

        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;

            songList = new ArrayList<>();
            stopPressed = false;
            currentIndex = -1;
        }
    }
}
