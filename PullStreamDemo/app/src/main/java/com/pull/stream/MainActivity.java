package com.pull.stream;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.pili.pldroid.player.AVOptions;
import com.pull.stream.utils.GetPathFromUri;
import com.pull.stream.utils.PermissionChecker;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String DEFAULT_TEST_URL = "rtmp://live.hkstv.hk.lxdns.com/live/hks";

    private Spinner mActivitySpinner;
    private EditText mEditText;
    private RadioGroup mStreamingTypeRadioGroup;
    private RadioGroup mDecodeTypeRadioGroup;
    private CheckBox mVideoCacheCheckBox;
    private CheckBox mLoopCheckBox;
    private CheckBox mVideoDataCallback;
    private CheckBox mAudioDataCallback;
    private CheckBox mDisableCheckBox;
    private RadioButton mLivingCheckBox;
    private RadioButton mPlayCheckBox;
    private LinearLayout mStartSetting;
    private EditText mStartPosEditText;

    public static final String[] TEST_ACTIVITY_ARRAY = {
            "PLMediaPlayerActivity",
            "PLAudioPlayerActivity",
            "PLVideoViewActivity",
            "PLVideoTextureActivity",
            "MultiInstanceActivity"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.pull.stream.R.layout.activity_main);

        TextView mVersionInfoTextView = findViewById(com.pull.stream.R.id.version_info);
        mVersionInfoTextView.setText("版本号: " + BuildConfig.VERSION_NAME + "\n");
        mVersionInfoTextView.append("编译时间： " + getBuildTimeDescription());

        mEditText = findViewById(com.pull.stream.R.id.VideoPathEdit);
        mEditText.setText(DEFAULT_TEST_URL);

        mStreamingTypeRadioGroup = findViewById(com.pull.stream.R.id.StreamingTypeRadioGroup);
        mDecodeTypeRadioGroup = findViewById(com.pull.stream.R.id.DecodeTypeRadioGroup);

        mActivitySpinner = findViewById(com.pull.stream.R.id.TestSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, TEST_ACTIVITY_ARRAY);
        mActivitySpinner.setAdapter(adapter);
        mActivitySpinner.setSelection(2);

        mVideoCacheCheckBox = findViewById(com.pull.stream.R.id.CacheCheckBox);
        mLoopCheckBox = findViewById(com.pull.stream.R.id.LoopCheckBox);
        mVideoDataCallback = findViewById(com.pull.stream.R.id.VideoCallback);
        mAudioDataCallback = findViewById(com.pull.stream.R.id.AudioCallback);
        mDisableCheckBox = findViewById(com.pull.stream.R.id.DisableLog);

        mVideoCacheCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!isPermissionOK()) {
                    mVideoCacheCheckBox.setChecked(false);
                }
            }
        });

        mLivingCheckBox = findViewById(com.pull.stream.R.id.RadioLiveStreaming);
        mPlayCheckBox = findViewById(com.pull.stream.R.id.RadioPlayback);
        mStartSetting = findViewById(com.pull.stream.R.id.StartSetting);

        mLivingCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartSetting.setVisibility(View.INVISIBLE);
            }
        });
        mPlayCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartSetting.setVisibility(View.VISIBLE);
            }
        });

        mStartPosEditText = findViewById(com.pull.stream.R.id.TextStartPos);

    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public boolean isPermissionOK() {
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            Toast.makeText(this, "Some permissions is not approved !!!", Toast.LENGTH_SHORT).show();
        }
        return isPermissionOK;
    }

    public void onClickLocalFile(View v) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*");
        }
        startActivityForResult(Intent.createChooser(intent, "选择要导入的视频"), 0);
    }

    public void onClickScanQrcode(View v) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setOrientationLocked(true);
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    public void onClickPlay(View v) {
        String videopath = mEditText.getText().toString();
        if (!"".equals(videopath)) {
            jumpToPlayerActivity(videopath, false);
        }
    }

    public void onClickList(View v) {
        String videopath = mEditText.getText().toString();
        if (!"".equals(videopath)) {
            jumpToPlayerActivity(videopath, true);
        }
    }

    public void jumpToPlayerActivity(String videoPath, boolean isList) {
        if (isList) {
            Intent intent = new Intent(this, PLVideoListActivity.class);
            intent.putExtra("videoPath", videoPath);
            startActivity(intent);
            return;
        }
        Class<?> cls;
        switch (mActivitySpinner.getSelectedItemPosition()) {
            case 2:
                cls = PLVideoViewActivity.class;
                break;
            default:
                return;
        }
        Intent intent = new Intent(this, cls);
        intent.putExtra("videoPath", videoPath);
        if (mDecodeTypeRadioGroup.getCheckedRadioButtonId() == com.pull.stream.R.id.RadioHWDecode) {
            intent.putExtra("mediaCodec", AVOptions.MEDIA_CODEC_HW_DECODE);
        } else if (mDecodeTypeRadioGroup.getCheckedRadioButtonId() == com.pull.stream.R.id.RadioSWDecode) {
            intent.putExtra("mediaCodec", AVOptions.MEDIA_CODEC_SW_DECODE);
        } else {
            intent.putExtra("mediaCodec", AVOptions.MEDIA_CODEC_AUTO);
        }
        if (mStreamingTypeRadioGroup.getCheckedRadioButtonId() == com.pull.stream.R.id.RadioLiveStreaming) {
            intent.putExtra("liveStreaming", 1);
        } else {
            intent.putExtra("liveStreaming", 0);
        }
        intent.putExtra("cache", mVideoCacheCheckBox.isChecked());
        intent.putExtra("loop", mLoopCheckBox.isChecked());
        intent.putExtra("video-data-callback", mVideoDataCallback.isChecked());
        intent.putExtra("audio-data-callback", mAudioDataCallback.isChecked());
        intent.putExtra("disable-log", mDisableCheckBox.isChecked());
        if (!"".equals(mStartPosEditText.getText().toString())) {
            intent.putExtra("start-pos", Integer.valueOf(mStartPosEditText.getText().toString()));
        }
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == 0) {
            String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (selectedFilepath != null && !"".equals(selectedFilepath)) {
                mEditText.setText(selectedFilepath, TextView.BufferType.EDITABLE);
            }
        } else if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(this, "扫码取消！", Toast.LENGTH_SHORT).show();
                } else {
                    mEditText.setText(result.getContents());
                }
            }
        }
    }

    protected String getBuildTimeDescription() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(BuildConfig.BUILD_TIMESTAMP);
    }
}
