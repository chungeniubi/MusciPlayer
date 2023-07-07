package com.example.musicplayer;

import android.graphics.Bitmap;

public class Song {
    private Bitmap front;
    private String name;
    private String artist;
    private long duration;
    private String album;
    private String path;

    public Song(Bitmap front, String name, String artist, long duration, String album, String path) {
        this.front = front;
        this.name = name;
        this.artist = artist;
        this.duration = duration;
        this.album = album;
        this.path = path;
    }

    public Song() {
    }

    public Bitmap getFront() {
        return front;
    }

    public void setFront(Bitmap front) {
        this.front = front;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
