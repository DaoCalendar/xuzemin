package com.android.androidlauncher;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.VideoView;


import com.android.androidlauncher.Service.BackWackService;
import com.android.androidlauncher.utils.MyConstant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private GridView gameGird;
    private VideoView videoView;
    private SimpleAdapter adapter;
    private static Thread thread = null;
    private LinearLayout ll_video,ll_game;
    private ArrayList<Map<String, Object>> dataList;
    public Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MyConstant.EVENT_START_VIDEO:
                    stopThread();
                    startPlay();
                    break;
            }
            return false;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_main);
        gameGird = findViewById(R.id.gameGird);
        videoView = findViewById(R.id.videoView);
        ll_video = findViewById(R.id.ll_video);
        ll_game = findViewById(R.id.ll_game);
        initData();
        String[] from={"img","text"};
        int[] to={R.id.img,R.id.text};
        adapter=new SimpleAdapter(this, dataList, R.layout.gridview_item, from, to);
        gameGird.setAdapter(adapter);
        gameGird.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                MyConstant.debugLog("onItemClick");
                MyConstant.isResetPlay = true;
//                AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);
//                builder.setTitle("提示").setMessage(dataList.get(arg2).get("text").toString()).create().show();
            }
        });
        ll_video.setOnClickListener(this);
        ll_game.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.sendEmptyMessage(MyConstant.EVENT_START_VIDEO);
    }

    public void startPlay() {
        ll_video.setVisibility(View.VISIBLE);
        ll_game.setVisibility(View.GONE);
        MyConstant.CurrentNumber = 0;
        MyConstant.isVideoPlay = true;
        videoView.setVideoURI(Uri.parse(MyConstant.VideoPath));
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);

            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        videoView.setVideoURI(Uri.parse(MyConstant.VideoPath));
                        videoView.start();
                    }
                });
}

    void initData() {
        //图标
        int icno[] = { R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher,
                R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher,
                 };
        //图标下的文字
        String name[]={"时钟","信号","宝箱","秒钟","大象","FF"};
        dataList = new ArrayList<>();
        for (int i = 0; i <icno.length; i++) {
            Map<String, Object> map=new HashMap<>();
            map.put("img", icno[i]);
            map.put("text",name[i]);
            dataList.add(map);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_video:
                videoView.pause();
                ll_video.setVisibility(View.GONE);
                ll_game.setVisibility(View.VISIBLE);
                MyConstant.isVideoPlay = false;
                startThread();
                break;
            case R.id.ll_game:
                MyConstant.debugLog("ll_game");
                MyConstant.isResetPlay = true;
//                startThread();
                break;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopThread();
    }

    public void stopThread(){
        videoView.pause();
        ll_video.setVisibility(View.GONE);
        ll_game.setVisibility(View.VISIBLE);
        if(thread!=null){
            thread.interrupt();
            thread = null;
        }
    }

    public void startThread(){
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!MyConstant.isVideoPlay){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    MyConstant.CurrentNumber ++;
                    if(MyConstant.isResetPlay){
                        MyConstant.CurrentNumber = 0;
                        MyConstant.isResetPlay = false;
                    }
                    if(MyConstant.CurrentNumber == 5){
                        handler.sendEmptyMessage(MyConstant.EVENT_START_VIDEO);
                    }
                    MyConstant.debugLog("MyConstant.CurrentNumber"+MyConstant.CurrentNumber);
                }
            }
        });
        thread.start();
    }

}