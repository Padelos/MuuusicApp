package com.example.muuusicapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.telephony.TelephonyManager;

public class PhoneCallReceiver extends BroadcastReceiver {

    boolean wasPlaying = false;

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if(action == "android.intent.action.PHONE_STATE"){
            PhoneStateChanged(intent);
        }
    }

    private void PhoneStateChanged(Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        MediaPlayer mp = MyMediaPlayer.getMediaPlayer();

        if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
            if(mp.isPlaying()){
                this.wasPlaying = true;
                mp.pause();
            }
            else
                this.wasPlaying = false;

        }
        else if(state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
            if(wasPlaying && !mp.isPlaying()) // because someone could press play while in call
                mp.start();
        }
    }
}