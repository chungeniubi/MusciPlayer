package com.example.musicplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends BaseAdapter {

    private List<Song> songs;
    private Context mContext;

    public MyAdapter(List<Song> songs, Context mContext){
        this.mContext = mContext;
        this.songs = songs;
    }
    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if(view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.song_front = (ImageView)view.findViewById(R.id.front);
            holder.song_name = (TextView)view.findViewById(R.id.song_name);
            holder.song_artist = (TextView)view.findViewById(R.id.song_artist);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }
        Bitmap bm = songs.get(i).getFront();
        //找不到封面时自动使用默认封面填充
        if (bm == null){
            bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_cover);
        }
        String name_str = songs.get(i).getName();
        String artist_album_str = songs.get(i).getArtist()+ " - " + songs.get(i).getAlbum();
        holder.song_front.setImageBitmap(bm);
        holder.song_name.setText(name_str);
        holder.song_artist.setText(artist_album_str);
        return view;
    }

    static class ViewHolder{
        TextView song_name;
        TextView song_artist;
        ImageView song_front;
    }
}
