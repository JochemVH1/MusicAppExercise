package com.dev.jvh.musicappexercise;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private String mediaPath;
    private List<String> songs = new ArrayList<>();
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private LoadSongsTask loadSongsTask;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;


    private static final String TAG = "MAIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionReadingExternalStorage();
        listView = (ListView) findViewById(R.id.listView);
        mediaPath = Environment.getExternalStorageDirectory().getPath() + "/Music/";

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try{
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(songs.get(position));
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                }catch(Exception e)
                {
                    Log.e(TAG, e.getMessage());
                }
            }
        });

        loadSongsTask = new LoadSongsTask();
        loadSongsTask.execute();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if(mediaPlayer.isPlaying()) mediaPlayer.reset();
    }

    private void requestPermissionReadingExternalStorage() {
        //This method checks it the manifest contains the READ_EXTERNAL_STORAGE permission
        //Although Manifest defines a uses permission to read the external storage, my one plus 2
        //didn't accept this and needed explicit approval of the user to read the storage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            //I think this method shows the popup screen asking for permission to access
            //External Storage
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
    }

    private class LoadSongsTask extends AsyncTask<Void,String,Void>
    {
        private List<String> loadedSongs = new ArrayList<>();
        @Override
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(),"LOADING...",Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            updateSongListRecursive(new File(mediaPath));
            return null;
        }

        public void updateSongListRecursive(File file) {
            if(file.isDirectory()) {
                for (int i = 0; i < file.listFiles().length; i++) {
                    File _file = file.listFiles()[i];
                    updateSongListRecursive(_file);
                }
            } else {
                String name = file.getAbsolutePath();
                publishProgress(name);
                if(name.endsWith(".mp3"))
                    loadedSongs.add(name);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ArrayAdapter<String> songList = new
                    ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.simple_list_item_1, loadedSongs);
            listView.setAdapter(songList);
            songs = loadedSongs;

            Toast.makeText(getApplicationContext(),
                    "Songs="+songs.size(),
                    Toast.LENGTH_LONG).show();

        }
    }
}
