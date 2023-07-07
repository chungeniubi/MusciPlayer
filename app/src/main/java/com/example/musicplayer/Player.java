package com.example.musicplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.List;

public class Player extends Activity implements View.OnClickListener{
    Button back_btn, play_btn, next_btn, pre_btn;
    SeekBar volume_sb, duration_sb;
    TextView artist_tv, name_tv, total_tv, current_tv;
    ImageView front_iv;
    static MediaPlayer mediaPlayer;
    ContentObserver volumeObserver;
    private int position;
    private int now_playing;
    Thread updateSb;
    SongTools service;
    private boolean sb_pause = false;
    List<Song> songs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.player_layout);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        service = new SongTools();
        back_btn = findViewById(R.id.play_go_back);
        play_btn = findViewById(R.id.play_btn);
        next_btn = findViewById(R.id.play_next);
        pre_btn = findViewById(R.id.play_pre);
        volume_sb = findViewById(R.id.volume_seekbar);
        duration_sb = findViewById(R.id.play_seekbar);
        artist_tv = findViewById(R.id.play_song_artist);
        name_tv = findViewById(R.id.play_song_name);
        total_tv = findViewById(R.id.play_duration);
        current_tv = findViewById(R.id.play_current_time);
        front_iv = findViewById(R.id.play_song_front);
        songs = service.findSongs(Player.this);
        this.position = bundle.getInt("position");

        artist_tv.setText(songs.get(bundle.getInt("position")).getArtist());
        name_tv.setText(songs.get(bundle.getInt("position")).getName());
        front_iv.setImageBitmap(songs.get(bundle.getInt("position")).getFront());
        String path = songs.get(bundle.getInt("position")).getPath();

        //若后台在播放歌曲，判断是否为正在播放
        if (mediaPlayer != null) {
            if ((bundle.getInt("now_playing") == bundle.getInt("position"))) {
                if (!mediaPlayer.isPlaying()){
                    play_btn.setBackgroundResource(R.drawable.play_blue);
                }
                duration_sb.setProgress(mediaPlayer.getCurrentPosition());
                current_tv.setText(service.getCurTime(mediaPlayer.getCurrentPosition()));
            }else{
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = new MediaPlayer();
                try {
                    //设置歌曲路径为音源
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
            }
        }else{
            //开始播放歌曲
            mediaPlayer = new MediaPlayer();
            try {
                //设置歌曲路径为音源
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
        }
        this.now_playing = bundle.getInt("position");

        service = new SongTools();
        total_tv.setText(service.getCurTime(mediaPlayer.getDuration()));

        //创建一个新线程实现进度条实时更新
        updateSb = new Thread(){
            @Override
            public void run() {
                int total = mediaPlayer.getDuration();
                //先获取position并设置保证重新打开播放器若继续播放时进度条瞬间到位
                int currPos = mediaPlayer.getCurrentPosition();
                duration_sb.setProgress(currPos);

                while(currPos < total){
                    try {
                        //若进度条正在被拖动则进度不会乱动
                        if (sb_pause){
                            sleep(500);
                        }else {
                            sleep(500);
                            currPos = mediaPlayer.getCurrentPosition();
                            duration_sb.setProgress(currPos);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        duration_sb.setMax(mediaPlayer.getDuration());
        updateSb.start();

        //实现拖动进度条调整歌曲进度
        duration_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //保证播放时间显示随进度条拖动而改变
                String curTime = service.getCurTime(seekBar.getProgress());
                current_tv.setText(curTime);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                sb_pause = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sb_pause = false;
                mediaPlayer.seekTo(seekBar.getProgress());
                //若此时为暂停状态则继续播放
                if (!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    play_btn.setBackgroundResource(R.drawable.pause_blue);
                }
                String curTime = service.getCurTime(seekBar.getProgress());
                current_tv.setText(curTime);
            }
        });


        //使textView按1s加1的速度运行
        final Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!sb_pause){
                    String curTime = service.getCurTime(mediaPlayer.getCurrentPosition());
                    current_tv.setText(curTime);
                }
                handler.postDelayed(this, delay);
            }
        }, delay);

        //实现seekbar控制系统音量
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volume_sb.setMax(15);
        volume_sb.setProgress(audioManager.getStreamVolume((AudioManager.STREAM_MUSIC)));

        //注册同步更新广播
        myRegisterReceiver();
        volume_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, i, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        volumeObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                AudioManager audioManager1 = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                volume_sb.setProgress(audioManager1.getStreamVolume(AudioManager.STREAM_MUSIC));
            }
        };

        //播放完成进入下一首
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                next_btn.performClick();
                Intent bc_intent = new Intent();
                bc_intent.putExtra("now_playing_change", now_playing);
                bc_intent.setPackage("com.example.musicplayer");
                bc_intent.setAction(MainActivity.SongChangeReceiver.ACTION);
                Player.this.sendBroadcast(bc_intent);
            }
        });

        //播放按钮，暂停按钮的实现
        play_btn.setOnClickListener(this);
        //实现切换上/下一首
        pre_btn.setOnClickListener(this);
        next_btn.setOnClickListener(this);
        //实现返回按钮
        back_btn.setOnClickListener(this);
    }

    //按钮监听事件
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.play_btn:
                if(mediaPlayer.isPlaying()){
                    play_btn.setBackgroundResource(R.drawable.play_blue);
                    mediaPlayer.pause();
                }else{
                    play_btn.setBackgroundResource(R.drawable.pause_blue);
                    mediaPlayer.start();
                }
                break;
            case R.id.play_pre:
                Song pre_song = new Song();
                position = ((position - 1)<0)?(songs.size() - 1):position - 1;
                pre_song.setName(songs.get(position).getName());
                pre_song.setArtist(songs.get(position).getArtist());
                pre_song.setFront(songs.get(position).getFront());
                pre_song.setDuration(songs.get(position).getDuration());
                pre_song.setPath(songs.get(position).getPath());

                name_tv.setText(pre_song.getName());
                artist_tv.setText(pre_song.getArtist());
                front_iv.setImageBitmap(pre_song.getFront());
                total_tv.setText(service.getCurTime((int) pre_song.getDuration()));
                play_btn.setBackgroundResource(R.drawable.pause_blue);
                now_playing = position;

                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    try {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(pre_song.getPath());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                duration_sb.setMax(mediaPlayer.getDuration());
                duration_sb.setProgress(0);
                current_tv.setText("0:00");
                break;
            case R.id.play_next:
                Song next_song = new Song();
                position = (position + 1) % songs.size();
                next_song.setName(songs.get(position).getName());
                next_song.setArtist(songs.get(position).getArtist());
                next_song.setFront(songs.get(position).getFront());
                next_song.setDuration(songs.get(position).getDuration());
                next_song.setPath(songs.get(position).getPath());

                name_tv.setText(next_song.getName());
                artist_tv.setText(next_song.getArtist());
                front_iv.setImageBitmap(next_song.getFront());
                total_tv.setText(service.getCurTime((int) next_song.getDuration()));
                play_btn.setBackgroundResource(R.drawable.pause_blue);
                now_playing = position;

                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    try {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(next_song.getPath());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                duration_sb.setMax(mediaPlayer.getDuration());
                duration_sb.setProgress(0);
                current_tv.setText("0:00");
                break;
            case R.id.play_go_back:
                Intent intent = new Intent(Player.this, MainActivity.class);
                intent.putExtra("now_playing", now_playing);
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            Intent bc_intent = new Intent();
            bc_intent.putExtra("now_playing_change", now_playing);
            bc_intent.setPackage("com.example.musicplayer");
            bc_intent.setAction(MainActivity.SongChangeReceiver.ACTION);
            Player.this.sendBroadcast(bc_intent);
            finish();
        }
        return false;
    }

    //实现seekbar系统音量控制
    private void myRegisterReceiver(){
        VolumeReceiver volumeReceiver = new VolumeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(volumeReceiver, filter);
    }
    public class VolumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")){
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                volume_sb.setProgress(curVolume);
            }
        }
    }
}
