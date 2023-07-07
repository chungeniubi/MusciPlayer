package com.example.musicplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class SongTools {

    //自动从媒体库中取得歌曲
    public List<Song> findSongs(Context context){
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        List<Song> songs = new ArrayList<Song>();
        while(cursor.moveToNext()){

            @SuppressLint("Range") String name = cursor.getString(
                    cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            @SuppressLint("Range") String artist = cursor.getString(
                    cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            @SuppressLint("Range") String album = cursor.getString(
                    cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            @SuppressLint("Range") Long duration = cursor.getLong(
                    cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            @SuppressLint("Range") int album_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            Bitmap front = getAlbumBitmap(context, album_id);
            @SuppressLint("Range") String file_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
            @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

            if (file_name.endsWith(".mp3")){
                Song song = new Song();
                song.setName(name);
                song.setAlbum(album);
                song.setDuration(duration);
                song.setArtist(artist);
                song.setFront(front);
                song.setPath(path);
                songs.add(song);
            }
        }
        cursor.close();
        return songs;
    }

    //获取歌曲封面信息
    public Bitmap getAlbumBitmap(Context context, int album_id){
        String uri = "content://media/external/audio/albums/" + Integer.toString(album_id);
        String album_art = null;
        Bitmap bm = null;
        Cursor album_cursor = context.getContentResolver().query(
                Uri.parse(uri), new String[]{"album_art"},
                null, null, null);
        if(album_cursor.getCount() > 0 && album_cursor.getColumnCount() > 0){
            album_cursor.moveToNext();
            album_art = album_cursor.getString(0);
        }
        album_cursor.close();
        if(album_art != null){
            bm = BitmapFactory.decodeFile(album_art);
        }else {
            //bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_cover);
            return null;
        }
        return bm;
    }

    //将毫秒转换为分：秒的形式
    public String getCurTime(int duration){
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time += min + ":";
        if(sec < 10){
            time += "0";
        }
        time += sec;
        return time;
    }
}
