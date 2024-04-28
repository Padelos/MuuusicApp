package com.example.muuusicapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder>{

    ArrayList<AudioData> songList;
    Context context;

    public SongListAdapter(ArrayList<AudioData> songList, Context context) {
        this.songList = songList;
        this.context = context;
    }

    @Override
    public SongListAdapter.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new SongListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( SongListAdapter.ViewHolder holder, int position) {
        int index = position;
        AudioData data = songList.get(index);
        holder.titleTV.setText(data.getTitle());
        holder.durationTV.setText(convertMsToMmSs(data.getDuration()));

        if(MyMediaPlayer.currentIndex == index){
            holder.titleTV.setTextColor(Color.parseColor("#FF0000"));
        }
        else{
            holder.titleTV.setTextColor(Color.parseColor("#FFFFFF"));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyMediaPlayer.currentIndex = index;

                Intent intent = new Intent(context, MusicPlayerActivity.class);

                intent.putExtra("ArrayList",songList);
                intent.putExtra("newSelectedSong",true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView titleTV;
        TextView durationTV;
        ImageView iconIV;
        public ViewHolder(View itemView) {
            super(itemView);

            titleTV = itemView.findViewById(R.id.songTitlePlayingActRI);
            titleTV.setSelected(true); // to use marquee
            durationTV = itemView.findViewById(R.id.songDurationActRI);
            iconIV = itemView.findViewById(R.id.songIconActRI);
        }
    }

    // if you remove the comments will work for hh:mm:ss
    // and also for mm:ss
    // but i prefer to work as mm:ss
    // even if it's going to display 105:04 (seekbar is larger)
    public static String convertMsToMmSs(String str) {
        String tmp;
        try{

            long milliseconds = Long.parseLong(str);
            long totalSeconds = milliseconds / 1000;

//            long hours = totalSeconds / 3600;
//            long minutes = (totalSeconds % 3600) / 60;
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;
//            if (hours > 0)
//                tmp = String.format("%d:%02d:%02d", hours, minutes, seconds);
//            else
                tmp = String.format("%d:%02d", minutes, seconds);
        }
        catch (NumberFormatException ex){
            tmp = new String("0:01");
        }

        return tmp;
    }
}
