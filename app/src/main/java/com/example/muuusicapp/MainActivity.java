package com.example.muuusicapp;
// 17/8/2023 03:19

import android.Manifest;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements MusicInterface
{

    RecyclerView recyclerView;
    TextView timeStart, timeEnd, songTitlePlaying;
    SeekBar seekBar;
    ImageView selectSongs, songIcon, prevSong, playPause, nextSong;
    CardView playPauseCV;
    MediaPlayer mediaPlayer= MyMediaPlayer.getMediaPlayer();
    AudioData currSong = null;
    int songIndex = -1;
    SongListAdapter songListAdapter = null;

    int currentOrientation; // landscape or portrait
    boolean unBoundFromService = false;
    private static final int REQUEST_CODE_PICK_AUDIO = 123;

    Handler handler = new Handler();

    boolean connectedToService = false;
    MusicService musicService;

    private final ServiceConnection servCon = new ServiceConnection ()
    {

        @Override
        public void onServiceConnected (ComponentName className, IBinder service)
        {
            Log.d("appRunning","MainAct: onServiceConnected");
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            connectedToService = true;
            musicService.addToUpdates (MainActivity.this);
        }

        @Override
        public void onServiceDisconnected (ComponentName CompNam)
        {
            connectedToService = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("appRunning","MainAct: onCreate");

        currentOrientation = getResources().getConfiguration().orientation;

        recyclerView = findViewById(R.id.songListViewActMain);
        selectSongs = findViewById(R.id.selectSongsActMain);
        timeStart = findViewById(R.id.timeStartActMain);
        timeEnd = findViewById(R.id.timeEndActMain);
        songTitlePlaying = findViewById(R.id.songTitlePlayingActMain);
        seekBar = findViewById(R.id.seekBarActMain);
        songIcon = findViewById(R.id.songIconActMain);
        prevSong = findViewById(R.id.prevSongActMain);
        playPauseCV = findViewById(R.id.playPauseCVActMain);
        playPause = findViewById(R.id.playPauseIVActMain);
        nextSong = findViewById(R.id.nextSongActMain);

        selectSongs.setOnClickListener (view -> {
            if(!checkPermissions()) {
                requestPermissions();
                return;
            }
            selectSongs();
        });
        songTitlePlaying.setSelected(true);
        songTitlePlaying.setOnClickListener(view -> openPlayMusicControls());
        songIcon.setOnClickListener(view -> openPlayMusicControls());
        nextSong.setOnClickListener(view -> {if (connectedToService) musicService.nextSong(); });
        prevSong.setOnClickListener(view -> {if (connectedToService) musicService.prevSong(); });
        playPause.setOnClickListener(view ->{if (connectedToService) musicService.playPause();});

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {


                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    timeStart.setText(SongListAdapter.convertMsToMmSs(String.valueOf(mediaPlayer.getCurrentPosition())));

                    if (currSong != null && songIndex != MyMediaPlayer.currentIndex) {
                        songListAdapter.notifyItemChanged(songIndex);
                        songIndex = MyMediaPlayer.currentIndex;
                        songListAdapter.notifyItemChanged(songIndex);
                        initSelectedSong();
                    }


                    if (mediaPlayer.isPlaying())
                        playPause.setImageResource(R.drawable.pause_icon);
                    else
                        playPause.setImageResource(R.drawable.play_icon);

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

        if(!checkPermissions())
        {
            requestPermissions();
        }

        displaySongs();
    }




    @Override
    protected void onStart() {
        super.onStart();

        Log.d("appRunning","MainAct: onStart");

        initSelectedSong();
        if(!connectedToService) {
            Log.d("appRunning","MainAct: onStart... Trying to connect to service");
            Intent MusicInt = new Intent(MainActivity.this, MusicService.class);
            bindService(MusicInt, servCon, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("appRunning","MainAct: onResume");
        if (recyclerView != null){
            songListAdapter = new SongListAdapter(MyMediaPlayer.songList, getApplicationContext()); // TODO: could change
            recyclerView.setAdapter(songListAdapter);

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation != currentOrientation)
            unBoundFromService = false;
        else
            unBoundFromService = true;

    }

    private void openPlayMusicControls(){

        if(MyMediaPlayer.currentIndex < 0 ){
            Toast.makeText(this,"No selected song",Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("appRunning","MainAct: openPlayMusicControls");

        Intent intent = new Intent(this, MusicPlayerActivity.class);

        intent.putExtra("newSelectedSong",false);
        this.startActivity(intent);
    }

    void displaySongs()
    {

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SongListAdapter(MyMediaPlayer.songList,this));
        for(AudioData tmp : MyMediaPlayer.songList){
            Log.d("ArrayList",tmp.getTitle());
        }
        Log.d("ArrayList","==================");
    }


    void selectSongs()
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_CODE_PICK_AUDIO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // If it's our request code and it's okay
        if (requestCode == REQUEST_CODE_PICK_AUDIO && resultCode == RESULT_OK) {
            // if there are selected data
            if (data != null) {
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    // Multiple files selected
                    ArrayList<Uri> arr = new ArrayList<>();
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri uri = clipData.getItemAt(i).getUri();
                        arr.add(uri);
                    }
                    findSongsUsingUriOnMediaStore( arr);
                } else {
                    // Single file selected
                    Uri uri = data.getData();

                    ArrayList<Uri> arr = new ArrayList<>();
                    arr.add(uri);
                    findSongsUsingUriOnMediaStore( arr);
                }
            }
        }
    }

    private void findSongsUsingUriOnMediaStore( ArrayList<Uri> uriList)
    {
        ContentResolver contentResolver = getContentResolver();
        final String[] proj = {DocumentsContract.Document.COLUMN_DOCUMENT_ID};

        ArrayList<String> idList = new ArrayList<>();

        for(Uri uri : uriList)
        {
            Cursor cursor = contentResolver.query(uri, proj, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String id = cursor.getString(0);

                int startIndex = id.lastIndexOf(':');
                if (startIndex != -1 )
                    id = id.substring(startIndex + 1);

                idList.add(id);

                cursor.close();
            }
        }

        if( !idList.isEmpty() )
            findSongData( idList);
    }


    public void findSongData( ArrayList<String> idList)
    {
        String[] proj = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media._ID
        };

        String selection  = MediaStore.Audio.Media.IS_MUSIC + "!= 0";

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, selection, null, null);

        while(cursor.moveToNext()){

            for(String id : idList)
            {
                if (id.equals(cursor.getString(3)))
                {
                    MyMediaPlayer.songList.add(new AudioData(cursor.getString(0), cursor.getString(1), cursor.getString(2)));
                }
            }

        }
        cursor.close();
        displaySongs();
    }


    boolean checkPermissions()
    {
        String tmp = Manifest.permission.READ_EXTERNAL_STORAGE;

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;

        if (currentApiVersion >= Build.VERSION_CODES.S) {
            tmp = android.Manifest.permission.READ_MEDIA_AUDIO;
        }
        int res = ContextCompat.checkSelfPermission(MainActivity.this, tmp);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    void requestPermissions()
    {

        String tmp = Manifest.permission.READ_EXTERNAL_STORAGE;

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;

        if (currentApiVersion >= Build.VERSION_CODES.S) {
            tmp = android.Manifest.permission.READ_MEDIA_AUDIO;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, tmp))
        {
            Toast.makeText(MainActivity.this, "Read perm is required!",Toast.LENGTH_LONG).show();
            return;
        }
        ActivityCompat.requestPermissions(MainActivity.this, new String[] {tmp}, 456);
    }

    @Override
    public void onBackPressed() {
        Log.d("appRunning","MainAct: onBackPressed");
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        Log.d("appRunning","MainAct: onDestroy");
        super.onDestroy();
        if(unBoundFromService)
            unbindService(servCon);
    }

    void initSelectedSong(){
        if(MyMediaPlayer.currentIndex < 0 ){
            return;
        }
        Log.d("appRunning","MainAct: initSelectedSong");
        currSong = MyMediaPlayer.songList.get(MyMediaPlayer.currentIndex);
        songTitlePlaying.setText(currSong.getTitle());
        timeEnd.setText(SongListAdapter.convertMsToMmSs(currSong.getDuration()));
        seekBar.setMax(mediaPlayer.getDuration());
    }


    public void prevSong(){

        Log.d("appRunning","MainAct: prevSong");
        initSelectedSong();

    }

    public void nextSong(){

        Log.d("appRunning","MainAct: nextSong");
        initSelectedSong();
    }


    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN , 0) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

            if (viewHolder.getItemViewType() != target.getItemViewType())
            {
                return false;
            }

            int startPosition = viewHolder.getAdapterPosition();
            int endPosition = target.getAdapterPosition();

            Log.d("recycleView","MyMediaPlayer.currentIndex: " + MyMediaPlayer.currentIndex);
            Log.d("recycleView","songIndex: " + songIndex);

            if(MyMediaPlayer.currentIndex == startPosition) {
                MyMediaPlayer.currentIndex = endPosition;
                songIndex = endPosition;
            }
            else if(MyMediaPlayer.currentIndex == endPosition) {
                MyMediaPlayer.currentIndex = startPosition;
                songIndex = startPosition;
            }

            Log.d("recycleView","Start pos: " + startPosition);
            Log.d("recycleView","End pos:   " + endPosition);
            Log.d("recycleView","MyMediaPlayer.currentIndex: " + MyMediaPlayer.currentIndex);
            Log.d("recycleView","songIndex: " + songIndex);
            Log.d("recycleView","======================");
            Collections.swap(MyMediaPlayer.songList,startPosition,endPosition);
            songListAdapter.notifyItemMoved(startPosition,endPosition);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
//            int pos = viewHolder.getAdapterPosition();
//
//            MyMediaPlayer.songList.remove(pos);
//
//            if(MyMediaPlayer.songList.size() == 0)
//                MyMediaPlayer.currentIndex = -1;
//
//            if (MyMediaPlayer.currentIndex == pos) {
//                int tmp = (MyMediaPlayer.currentIndex-1 >= 0) ? MyMediaPlayer.currentIndex-1 : 0;
//                MyMediaPlayer.currentIndex = tmp;
//                musicService.nextSong();
//            }
//
//
//            songListAdapter.notifyDataSetChanged();


        }
    };
}