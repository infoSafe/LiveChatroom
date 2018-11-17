package com.pull.stream;

import android.app.Application;
import android.util.Log;

import com.pull.stream.message.ChatroomWelcome;

import io.rong.imlib.AnnotationNotFoundException;
import io.rong.imlib.RongIMClient;

public class StreamingApplication extends Application {
    private static final String TAG = "StreamingApplication";
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化融云
        RongIMClient.init(getApplicationContext(),"appkey");
        RongIMClient.connect("Token", new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                Log.d(TAG, "onTokenIncorrect: ===>");
            }

            @Override
            public void onSuccess(String s) {
                Log.d(TAG, "onSuccess: ===>"+s.toString());
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                Log.d(TAG, "onError: ===>"+errorCode.toString());

            }
        });

        try {
            RongIMClient.registerMessageType(ChatroomWelcome.class);
        } catch (AnnotationNotFoundException e) {
            e.printStackTrace();
        }
    }


}
