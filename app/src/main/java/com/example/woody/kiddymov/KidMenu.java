package com.example.woody.kiddymov;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class KidMenu extends ActionBarActivity {

    private static final String LOG_TAG = "AudioplayerKidsMenuTest";
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int REQ_CODE_PLAY_IS_OVER = 1;

    private ArrayList<Movie> list_of_movie_objects;
    private ArrayList<Movie> list_of_parent_recommendations;

    private int suggest_index;
    private int suggested_movies_amount;
    private Algorithm alg_obj;
    private Timer timer;
    private String user;
    private DBobject db_object;
    private EditMovie edit_movie_obj;
    private Toaster dbg_obj;
    private MediaPlayer mPlayer = null;
    private String speech_to_string = "none";
    private Movie movie_to_play;
    private Movie prev_movie;
    ImageButton startS2Tbtn;
    TextView text_view_to_display_S2T;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kids_menu);
        dbg_obj = new Toaster(getApplicationContext());
        user = getUsername();
        db_object = new DBobject(user);
        edit_movie_obj = new EditMovie();
        list_of_movie_objects = db_object.getMovieList();
        if (list_of_movie_objects.isEmpty()){
            dbg_obj.show_line("DB connection error!");
            return;
        }

        ImageButton play_button = (ImageButton) findViewById(R.id.playBtn);
        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              playCurrentVideo();
            }
        });
        ImageButton next_button = (ImageButton) findViewById(R.id.nextBtn);
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //suggest next movie
                suggestNextMovie();
            }
        });

        ImageButton back_button = (ImageButton) findViewById(R.id.backButton);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //suggest previous movie
                suggest_index--;
                suggested_movies_amount--;
                suggestNextMovie();

            }
        });
        getParentRecomendaions();
        alg_obj = new Algorithm(list_of_movie_objects);

      //  dbg_obj.show_line(alg_obj.get_weighted_average_ans_str());

        alg_obj.sort_movies(list_of_movie_objects);

        text_view_to_display_S2T = (TextView) findViewById(R.id.kids_menu_s2t_text_view);
        startS2Tbtn = (ImageButton) findViewById(R.id.kids_menu_start_voice_to_text);
        //suggestNextMovie increments the index. If we want to see the first movie
        //We have to init the index with -1
        suggest_index = -1;
        suggested_movies_amount = 0;
        suggestNextMovie();
        startS2Tbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suggestNextMovie();
            }
        });

    }

    private void getParentRecomendaions() {
        list_of_parent_recommendations = new ArrayList<Movie>();
        for (int i = 0; i < list_of_movie_objects.size(); i++){
            Movie movie = list_of_movie_objects.get(i);
            int PARENT_RECOMENDATION_INDEX = 4;
            int PARENT_RECOMENDATION_VALUE = 2;
            //Check if this is a parent recommendation
            if (movie.getAnswer_vector().get(PARENT_RECOMENDATION_INDEX) == PARENT_RECOMENDATION_VALUE){
                list_of_parent_recommendations.add(movie);
             //   dbg_obj.show_line("Added " + movie.getMovie_name() + "to parent list");
            }
        }
    }
//play movie in youtube
    private void playVid() {
        String youtube_id = movie_to_play.getLink();
        youtube_id = youtube_id.split("/")[1];
        Intent play_vid_intent = new Intent(this, KidPlayer.class);
        play_vid_intent.putExtra("id", youtube_id);
        startActivityForResult(play_vid_intent, REQ_CODE_PLAY_IS_OVER); //when the movie is over, call func onActivityResult

    }

    //Play parent's audio record
    private void startPlaying(String mFileName) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp){
                    mp.release();
                    //parent's audio finished, now show speech input to get user's (kid's) response
                    promptSpeechInput();
                }
            });
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }
    private Bitmap getYoutubThumbnail(String youtube_url)
    {
        String vid_ID = null;
        vid_ID = getVidID(youtube_url);
        if ( vid_ID == null )
        {
            return getDefaultThumbnail();
        }
        else
        {
            return getThumbnailFromVidId(vid_ID);
        }
    }

    private Bitmap getDefaultThumbnail()
    {
        Bitmap bm = BitmapFactory.decodeResource(null,R.mipmap.nothumbnailpic);
        return  bm;
    }
//in use for getting youtube thumbnail
    private Bitmap getThumbnailFromVidId(String vid_ID)
    {
        String pic_url = "http://img.youtube.com/vi/"+vid_ID+"/0.jpg";
        Bitmap temp_pic;
        try {
            temp_pic = getBitmapFromURL(pic_url);
        } catch ( Exception e) {
            temp_pic = null;
        }
        return temp_pic ;
    }
    private String getVidID(String youtube_url)
    {
        String vid_ID = null;
        if (youtube_url != null) {
            // Method 1 : for .../v?=XXX/...
            String temp_first_split = youtube_url.split("&")[0];
            if (temp_first_split != null) {
                String[] temp_second_split_array = temp_first_split.split("=");
                if (temp_second_split_array  != null)
                {
                    if (temp_second_split_array.length > 1)
                    {
                        vid_ID = temp_second_split_array[1];
                    }
                }
            }
            // Method 2 : for ....youtu.be/XXX/...
            if (vid_ID == null)
            {
                String temp_first_split2 = youtube_url.split("&")[0];
                if (temp_first_split2 != null) {
                    String[] temp_second_split_array2 = temp_first_split2.split(".be/");
                    if (temp_second_split_array2 != null)
                    {
                        if (temp_second_split_array2.length > 1)
                        {
                            vid_ID = temp_second_split_array2[1];
                        }
                    }
                }
            }
        }
        return vid_ID;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            DownloadImageTask temp_download_task = new DownloadImageTask();

            Bitmap myBitmap = temp_download_task.execute(src).get();
            return myBitmap;
        }
        catch (Exception e) {
            return null;
        }
    }

    ///SPEECH TO TEXT
    public void doSpeechCommand(String command)
    {
        if (command.equals("next") || command.equals("nex") || command.equals("no")) {
            suggestNextMovie();
        }
        else if (command.equals("play")|| command.equals("ken") ||command.equals("yes") ) {
            playCurrentVideo();
        }
        else {
            //Retry to get user command
            promptSpeechInput();
        }
    }

    private void playCurrentVideo(){
        Integer views_amount = movie_to_play.getViews_amount() + 1;
        movie_to_play.setViews_amount(views_amount);
        edit_movie_obj.edit_movie(movie_to_play);
        playVid();
    }
    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    speech_to_string = result.get(0);
                    text_view_to_display_S2T.setText(speech_to_string);
                    doSpeechCommand(speech_to_string);
                }
                break;
            }
            case REQ_CODE_PLAY_IS_OVER: {
                suggestNextMovie();
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_kids_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //the main function of movie suggestions to kid
    private void suggestNextMovie(){
        //Run till a different movie is selected
        while (true) {
            int PARENT_RECOMEND_PERIOD = 3;
            suggested_movies_amount++;
            if (suggested_movies_amount == PARENT_RECOMEND_PERIOD) {
                dbg_obj.show_line("recommended by parent");
                suggested_movies_amount = 0;
                Random ran = new Random();
                int parent_index = ran.nextInt(list_of_parent_recommendations.size());
                //random movie from parent recommendations
                //movie_to_play = list_of_parent_recommendations.get(parent_index);
                movie_to_play = list_of_parent_recommendations.get(parent_index);
            } else {
                //modulu - if the array size is smaller then suggest_index
                suggest_index = (suggest_index + 1) % list_of_movie_objects.size();
                movie_to_play = list_of_movie_objects.get(suggest_index);
            }

            //The very first movie
            if (prev_movie == null){
                prev_movie = movie_to_play;
                break;
            }
            //The current movie is the same as the previous.
            //Run the suggestion alg again
            if (prev_movie.getLink() == movie_to_play.getLink()){
                continue; //back to the beggining of while loop if the suggested movie equals the previous
            }
            //Movies are not the same
            prev_movie = movie_to_play;
            break;
        }

        String url = "https://" + movie_to_play.getLink();
        ShowMovie(url);  //show thumbnail
        String record = movie_to_play.getAudio_record_path_full();
        if (record.equals("norecord")) {
            dbg_obj.show_line("No Recording");
            scheduleSpeechInput(); //delay speech input to allow the user to see thumbnail
            return;
        }
        //Now check if record file exists on phone
        File file = new File(record);
        if(file.exists() == false) {
            dbg_obj.show_line("Recording file is missing!");
            scheduleSpeechInput();
            return;

        }
        startPlaying(record);
    }

    private void scheduleSpeechInput(){
        timer = new Timer();
        timer.schedule(new DelayedPromptSpeech(), 3500);
    }


    protected class DelayedPromptSpeech extends TimerTask {
        public void run(){
            timer.cancel();
            promptSpeechInput();
        }
    }
//showing YouTube thumbnail
    private void ShowMovie(String url){

        text_view_to_display_S2T.setText(url);
        Bitmap temp_bit = getYoutubThumbnail(url);

        if (temp_bit != null) {
            startS2Tbtn.setImageBitmap(Bitmap.createScaledBitmap(temp_bit, 800, 600, false));
        }

    }

    public String getUsername() {
        AccountManager manager = AccountManager.get(this);
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<String>();

        for (Account account : accounts) {
            // TODO: Check possibleEmail against an email regex or treat
            // account.name as an email address only for certain account.type values.
            possibleEmails.add(account.name);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            String email = possibleEmails.get(0);
            String[] parts = email.split("@");

            if (parts.length > 1)
                return parts[0];
        }
        return null;
    }
}


