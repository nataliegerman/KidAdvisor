package com.example.woody.kiddymov;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.content.Context;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;


public class AudioRecordActivity extends Activity
{
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    private static String time_str = null;

    private MediaRecorder mRecorder = null;

    private MediaPlayer   mPlayer = null;
    private Boolean recording = false;
    private Boolean playing = false;


    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }


    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }


    public void nameTheFile() {
        //Creating an internal directory
        //String dir_name = R.string.app_name + "_private_folder";
        String dir_name = "private_folder";
        File mydir = this.getDir(dir_name, Context.MODE_PRIVATE);
        //Getting a file within the dir.
        File fileWithinMyDir = new File(mydir, dir_name);
        try {
            FileOutputStream out = new FileOutputStream(fileWithinMyDir);
        }
        catch (Exception e){}
        mFileName = mydir.getAbsolutePath();
        //Adding a date + time to the file name to make it unique
        Calendar c = Calendar.getInstance();
        time_str = c.getTime().toString();
        time_str = time_str.replace(" ", "");
        time_str = time_str.replace(".","_");
        time_str = time_str.replace(":","_");
        time_str = time_str.replace("+","p");
        time_str = time_str.replace("-","m");

        mFileName += "/" + time_str + "audiorecordtest.3gp";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);
        nameTheFile();
        Button button = (Button)findViewById(R.id.done_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AudioRecordActivity.this, AddNewVid.class);
                intent.putExtra("FILE_NAME", time_str);
                setResult(RESULT_OK, intent);
                Toast toast = Toast.makeText(getApplicationContext(),
                        "click on save button to save the changes",
                        Toast.LENGTH_SHORT);
                toast.show();
                finish();
            }
        });

        button = (Button)findViewById(R.id.add_record_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recording == false) {
                    startRecording();
                    recording = true;
                    Button button = (Button)findViewById(R.id.add_record_button);
                    button.setText("Stop recording");
                }
                else{
                    stopRecording();
                    recording = false;
                    Button button = (Button)findViewById(R.id.add_record_button);
                    button.setText("Record");
                    //Enable playing after we have a recording
                    button = (Button)findViewById(R.id.play_pause);
                    button.setActivated(true);
                }
            }
        });

        button = (Button)findViewById(R.id.play_pause);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playing == false) {
                    startPlaying();
                    playing = true;
                    Button button = (Button)findViewById(R.id.play_pause);
                    button.setText("Stop playing");
                }
                else{
                    stopPlaying();
                    playing = false;
                    Button button = (Button)findViewById(R.id.play_pause);
                    button.setText("Play");
                }
            }
        });
        //At first, playing is not allowed until we get a recording
        button.setActivated(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

}
