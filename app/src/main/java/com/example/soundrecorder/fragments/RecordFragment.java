package com.example.soundrecorder.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.soundrecorder.R;
import com.example.soundrecorder.RecordingService;
import com.example.soundrecorder.activities.MainActivity;

import java.io.File;

public class RecordFragment extends Fragment {

    Button recordButton = null;
    Button pauseButton = null;
    ImageView  firstRecordButton = null;
    TextView  pauseViewText= null;
    int RecordingPrompetCount = 0;
    boolean startRecording = true;
    boolean pauseRecording = true;
    Chronometer chronometer = null;
    ProgressBar progressBar;
    ProgressBar progressBarLoad;

    private static final int REQUEST_PERMISSION_AUDIO_RECORD = 100;
    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 101;
    private SharedPreferences permissionStatus;
    private  boolean sendToSettings = false;

    long timeWhenPaused = 0; //start time when user clicks the pause button

    public static RecordFragment newIntence (int position){

        RecordFragment recordFragment = new RecordFragment();
        Bundle b = new Bundle();
        b.putInt("position" , position);
        recordFragment.setArguments(b);
        return recordFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.record_fragment,container,false);

        chronometer = view.findViewById(R.id.chronometer);
        pauseViewText = view.findViewById(R.id.pause_text);
        pauseViewText.setVisibility(View.INVISIBLE);

        firstRecordButton = view.findViewById(R.id.first_Record_Button);

        recordButton = view.findViewById(R.id.record_Button);
        recordButton.setVisibility(View.INVISIBLE);

        progressBar = view.findViewById(R.id.recordProgressBar);
        progressBarLoad = view.findViewById(R.id.progressbarLoad);
        progressBar.setVisibility(View.VISIBLE);
        progressBarLoad.setVisibility(View.INVISIBLE);


        firstRecordButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                // Request Permission
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)  != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.RECORD_AUDIO)
                        || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                        // show info about  why you need to this permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Need Record and Storage Permission");
                        builder.setMessage("This app needs Record and Storage permission");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION_AUDIO_RECORD);
                            }
                        });
                        builder.show();
                    }else if (permissionStatus.getBoolean(Manifest.permission.RECORD_AUDIO,false)){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Need Record and Storage Permission");
                        builder.setMessage("This app needs Record and Storage permission");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                sendToSettings = true;
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getActivity().getPackageName(),null);
                                intent.setData(uri);
                                Toast.makeText(getContext(),"Go to permissions to Grant Microphone and Storage ",Toast.LENGTH_LONG).show();
                            }
                        });
                        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }else {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION_AUDIO_RECORD);
                    }
                    permissionStatus =PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences.Editor editor = permissionStatus.edit();
                    editor.putBoolean(Manifest.permission.RECORD_AUDIO,true);
                    editor.apply();
                }else {
                    Toast.makeText(getContext(),"we got the permission",Toast.LENGTH_LONG).show();
                    onRecord(startRecording);
                    startRecording = !startRecording;
                }
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(startRecording);

                startRecording = !startRecording;
            }
        });

        pauseButton= view.findViewById(R.id.pauseBtn);
        pauseButton.setVisibility(View.INVISIBLE);

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onPauseRecord(pauseRecording);
                pauseRecording = !pauseRecording;
            }
        });

        return view;
    }

    private void onPauseRecord(boolean pauseRecording) {

        if (pauseRecording){ //pause recording
            timeWhenPaused = chronometer.getBase() - SystemClock.elapsedRealtime();
            chronometer.stop();
            progressBarLoad.setVisibility(View.INVISIBLE);
            pauseViewText.setVisibility(View.VISIBLE);
            pauseButton.setBackgroundResource(R.drawable.ic_play_white);
            Toast.makeText(getActivity(),"Pause Recording.",Toast.LENGTH_SHORT).show();


        }else { //resume recording

            pauseButton.setBackgroundResource(R.drawable.ic_pause_white);
            chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
            chronometer.start();
            progressBarLoad.setVisibility(View.VISIBLE);
            pauseViewText.setVisibility(View.INVISIBLE);
            Toast.makeText(getActivity(),"Resume Recording.",Toast.LENGTH_SHORT).show();

        }
    }

    private Notification onRecord(boolean startRecording) {


        Intent i = new Intent(getActivity(), RecordingService.class);

        if (startRecording){

            pauseButton.setVisibility(View.VISIBLE);
            recordButton.setVisibility(View.VISIBLE);
            firstRecordButton.setVisibility(View.INVISIBLE);
            progressBarLoad.setVisibility(View.VISIBLE);
            pauseViewText.setVisibility(View.INVISIBLE);

            Toast.makeText(getActivity(),"Recording Started",Toast.LENGTH_SHORT).show();
            File folder = new File(Environment.getExternalStorageDirectory()+ "/SoundRecorder");

            if (!folder.exists()) {
                folder.mkdir();
            }

            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            getActivity().startService(i);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            RecordingPrompetCount++;

        }
        else {
            //stop recording
            pauseButton.setVisibility(View.INVISIBLE);
            recordButton.setVisibility(View.INVISIBLE);
            firstRecordButton.setVisibility(View.VISIBLE);
            progressBarLoad.setVisibility(View.INVISIBLE);
            pauseButton.setBackgroundResource(R.drawable.ic_pause_white);

            chronometer.stop();
            chronometer.setBase(SystemClock.elapsedRealtime());
            timeWhenPaused = 0;
            getActivity().stopService(i);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            pauseRecording = true;


        }

        return null;
    }

}
