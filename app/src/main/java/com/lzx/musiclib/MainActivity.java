package com.lzx.musiclib;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lzx.starrysky.manager.MediaSessionConnection;
import com.lzx.starrysky.manager.MusicManager;
import com.lzx.starrysky.manager.OnPlayerEventListener;
import com.lzx.starrysky.model.SongInfo;
import com.lzx.starrysky.playback.download.ExoDownload;
import com.lzx.starrysky.utils.TimerTaskManager;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnPlayerEventListener {

    boolean isFavorite = false;
    boolean isChecked = false;

    TimerTaskManager mTimerTask;
    TextView currInfo, currTime;
    SeekBar mSeekBar;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currInfo = findViewById(R.id.currInfo);
        currTime = findViewById(R.id.currTime);
        mSeekBar = findViewById(R.id.seekBar);

        mTimerTask = new TimerTaskManager();


        SongInfo s1 = new SongInfo();
        s1.setSongId("111");
        s1.setSongUrl("http://music.163.com/song/media/outer/url?id=317151.mp3");
        s1.setSongCover("https://www.qqkw.com/d/file/p/2018/04-21/c24fd86006670f964e63cb8f9c129fc6.jpg");
        s1.setSongName("心雨");
        s1.setArtist("贤哥");

        SongInfo s2 = new SongInfo();
        s2.setSongId("222");
        s2.setSongUrl("http://music.163.com/song/media/outer/url?id=281951.mp3");
        s2.setSongCover("https://n.sinaimg.cn/sinacn13/448/w1024h1024/20180504/7b5f-fzyqqiq8228305.jpg");
        s2.setSongName("我曾用心爱着你");
        s2.setArtist("潘美辰");

        SongInfo s3 = new SongInfo();
        s3.setSongId("333");
        s3.setSongUrl("http://music.163.com/song/media/outer/url?id=25906124.mp3");
        s3.setSongCover("http://cn.chinadaily.com.cn/img/attachement/jpg/site1/20180211/509a4c2df41d1bea45f73b.jpg");
        s3.setSongName("不要说话");
        s3.setArtist("陈奕迅");

        List<SongInfo> songInfos = new ArrayList<>();
        songInfos.add(s1);
        songInfos.add(s2);
        songInfos.add(s3);

        findViewById(R.id.play).setOnClickListener(v -> MusicManager.getInstance().playMusic(songInfos, 0));
        findViewById(R.id.pause).setOnClickListener(v -> MusicManager.getInstance().pauseMusic());
        findViewById(R.id.resum).setOnClickListener(v -> MusicManager.getInstance().playMusic());
        findViewById(R.id.stop).setOnClickListener(v -> MusicManager.getInstance().stopMusic());
        findViewById(R.id.pre).setOnClickListener(v -> MusicManager.getInstance().skipToPrevious());
        findViewById(R.id.next).setOnClickListener(v -> MusicManager.getInstance().skipToNext());
        findViewById(R.id.fastForward).setOnClickListener(v -> MusicManager.getInstance().fastForward());
        findViewById(R.id.rewind).setOnClickListener(v -> MusicManager.getInstance().rewind());
        findViewById(R.id.currSong).setOnClickListener(v -> {
            SongInfo songInfo = MusicManager.getInstance().getNowPlayingSongInfo();
            if (songInfo == null) {
                Toast.makeText(MainActivity.this, "songInfo is null", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "curr SongInfo = " + songInfo.getSongId(), Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.currSongId).setOnClickListener(v -> {
            String songId = MusicManager.getInstance().getNowPlayingSongId();
            if (TextUtils.isEmpty(songId)) {
                Toast.makeText(MainActivity.this, "songId is null", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "songId = " + songId, Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.currSongIndex).setOnClickListener(v -> {
            int index = MusicManager.getInstance().getNowPlayingIndex();
            Toast.makeText(MainActivity.this, "index = " + index, Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.sendFavorite).setOnClickListener(v -> {
            if (isFavorite) {
                MusicManager.getInstance().updateFavoriteUI(false);
                isFavorite = false;
            } else {
                MusicManager.getInstance().updateFavoriteUI(true);
                isFavorite = true;
            }
        });
        findViewById(R.id.sendLyrics).setOnClickListener(v -> {
            if (isChecked) {
                MusicManager.getInstance().updateLyricsUI(false);
                isChecked = false;
            } else {
                MusicManager.getInstance().updateLyricsUI(true);
                isChecked = true;
            }
        });
        findViewById(R.id.cacheSize).setOnClickListener(v -> {
            String size = ExoDownload.getInstance().getCachedSize() + "";
            Toast.makeText(MainActivity.this, "大小：" + size, Toast.LENGTH_SHORT).show();
        });

        MusicManager.getInstance().addPlayerEventListener(this);
        mTimerTask.setUpdateProgressTask(() -> {
            int progress = (int) MusicManager.getInstance().getPlayingPosition();
            int buffered = (int) MusicManager.getInstance().getBufferedPosition();
            mSeekBar.setProgress(progress);
            mSeekBar.setSecondaryProgress(buffered);
            currTime.setText(formatMusicTime(progress) + "/" + formatMusicTime(MusicManager.getInstance().getDuration()));
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MusicManager.getInstance().seekTo(seekBar.getProgress());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        MediaSessionConnection.getInstance(this).connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MediaSessionConnection.getInstance(this).disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicManager.getInstance().removePlayerEventListener(this);
    }

    @Override
    public void onMusicSwitch(SongInfo songInfo) {
        currInfo.setText("当前播放：" + songInfo.getSongName());
    }

    @Override
    public void onPlayerStart() {
        mSeekBar.setMax((int) MusicManager.getInstance().getDuration());
        mTimerTask.startToUpdateProgress();
    }

    @Override
    public void onPlayerPause() {
        mTimerTask.stopToUpdateProgress();
    }

    @Override
    public void onPlayerStop() {
        mTimerTask.stopToUpdateProgress();
    }

    @Override
    public void onPlayCompletion() {
        mTimerTask.stopToUpdateProgress();
    }

    @Override
    public void onBuffering() {

    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        mTimerTask.stopToUpdateProgress();
    }

    public static String formatMusicTime(long duration) {
        String time = "";
        long minute = duration / 60000;
        long seconds = duration % 60000;
        long second = Math.round((int) seconds / 1000);
        if (minute < 10) {
            time += "0";
        }
        time += minute + ":";
        if (second < 10) {
            time += "0";
        }
        time += second;
        return time;
    }

}
