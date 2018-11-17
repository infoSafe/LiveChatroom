package com.live.demo.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.live.demo.R;
import com.live.demo.Util.Util;

import java.util.UUID;

import io.rong.imlib.RongIMClient;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static final String GENERATE_STREAM_TEXT = "http://api-demo.qnsdk.com/v1/live/stream/";

    private TextView tv_start;

    private EditText et_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

    public void initView() {
        tv_start = findViewById(R.id.tv_start);
        tv_start.setOnClickListener(this);
        et_name = findViewById(R.id.et_name);

        //连接融云服务器
        RongIMClient.getInstance().setConnectionStatusListener(new RongIMClient.ConnectionStatusListener() {
            @Override
            public void onChanged(ConnectionStatus status) {
                switch (status) {

                    case CONNECTED://连接成功。
                        Log.i(TAG, "连接成功");
                        break;
                    case DISCONNECTED://断开连接。
                        Log.i(TAG, "断开连接");
                        break;
                    case CONNECTING://连接中。
                        Log.i(TAG, "连接中");
                        break;
                    case NETWORK_UNAVAILABLE://网络不可用。
                        Log.i(TAG, "网络不可用");
                        break;
                    case KICKED_OFFLINE_BY_OTHER_CLIENT://用户账户在其他设备登录，本机会被踢掉线
                        Log.i(TAG, "用户账户在其他设备登录");
                        break;
                }
            }
        });

    }


    @Override
    public void onClick(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String tlpath = genPublishURL();
//                video_Audio
                String name = et_name.getText().toString().trim();
                Intent intent = new Intent(MainActivity.this, SWCameraStreamingActivity.class);
//                传推流地址
                intent.putExtra("stream_publish_url", tlpath);
                intent.putExtra("name", name);
                startActivity(intent);

            }
        }).start();
    }


    /**
     * @author:
     * @create at: 2018/11/13  13:01
     * @Description: 获取推流地址
     */
    private String genPublishURL() {
        String publishUrl = Util.syncRequest(GENERATE_STREAM_TEXT + UUID.randomUUID());
        return publishUrl;
    }


}
