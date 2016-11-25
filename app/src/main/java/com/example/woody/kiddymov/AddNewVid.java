package com.example.woody.kiddymov;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;

import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class AddNewVid extends ActionBarActivity {
    private Movie new_movie;
    private boolean editing_movie;
    private Toaster dbg_obj;
    private EditMovie edit_movie_obj;
    private TextView url_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_vid);
        dbg_obj = new Toaster(getApplicationContext());
        edit_movie_obj = new EditMovie(); //object that adds/edits movies on server
        Intent intent = getIntent();
        String extra_text = intent.getStringExtra(Intent.EXTRA_TEXT); //in case of edit movie
        String new_vid_link;
        String answer_str = "00000";
        String movie_name = "no name";
        Integer id = 0;
        String audio_record = "norecord";
        Integer views_amount = 0;
        if (extra_text.equals("edit")){
            //We are editing an existing movie
            new_vid_link = intent.getStringExtra("link");
            id = Integer.parseInt(intent.getStringExtra("id"));
            answer_str = intent.getStringExtra("answer_str");
            movie_name = intent.getStringExtra("movie_name");
            audio_record = intent.getStringExtra("audio_record");
            views_amount = Integer.parseInt(intent.getStringExtra("views_amount"));

            editing_movie = true;

        }
        //new movie from YouTube
        else {
            movie_name = "insert video name";
            //A new video is to be added
            new_vid_link = extra_text.substring(8);
            if (false == new_vid_link.startsWith("youtu.be")){
                dbg_obj.show_line("Invalid video! Only youtube videos are supported.");
                finish();
                return;
            }
        }
        new_movie = new Movie(id,new_vid_link, movie_name,getUsername(),audio_record, audio_record, views_amount);
        new_movie.setAnswerString(answer_str);
        url_view = (TextView) findViewById(R.id.display_movie_name);

        url_view.setText(movie_name);

        for (int i = 0; i < 5; i++){
            initRadioButtonAnswer(i);
        }

        RadioGroup q1 = (RadioGroup) findViewById(R.id.q1);
        q1.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        handleButtonSelected(group, checkedId, 0);
                    }
                }
        );

        RadioGroup q2 = (RadioGroup) findViewById(R.id.q2);
        q2.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        handleButtonSelected(group, checkedId, 1);
                    }
                }
        );

        RadioGroup q3 = (RadioGroup) findViewById(R.id.q3);
        q3.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        handleButtonSelected(group, checkedId, 2);
                    }
                }
        );

        RadioGroup q4 = (RadioGroup) findViewById(R.id.q4);
        q4.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        handleButtonSelected(group, checkedId, 3);
                    }
                }
        );

        RadioGroup q5 = (RadioGroup) findViewById(R.id.q5);
        q5.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        handleButtonSelected(group, checkedId, 4);
                    }
                }
        );

//save movie to DB
        Button send_button = (Button) findViewById(R.id.send_to_db_button);

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncSender().execute();
            }
        });
//open audio record activity
        Button add_record_button = (Button) findViewById(R.id.add_record_button);
        add_record_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddNewVid.this, AudioRecordActivity.class);
                startActivityForResult(intent,1);
            }
        });
    }
//setting the answer for one specific question #button_number
    private void initRadioButtonAnswer(Integer button_number){
        Integer answer = new_movie.getAnswer_vector().get(button_number) + 1; // a11,a12,a13 - options
        button_number ++; //q1,q2,q3,q4,q5
        String id_text = "a" + button_number.toString() + answer.toString(); //string will look like a11,a12,a13
        Resources res = getResources(); //for finding view identifier by text instead of id
        int identifier = res.getIdentifier(id_text, "id", this.getPackageName());
        RadioButton button = (RadioButton) findViewById(identifier);
        button.setChecked(true);
    }
    private void handleButtonSelected(RadioGroup group, int checkedId, Integer question){
        View radioButton = group.findViewById(checkedId);
        int answer = group.indexOfChild(radioButton);

        String answer_str   = String.format("%d", answer);
        Toast toast = Toast.makeText(getApplicationContext(),
                "question: " + question.toString() + "answer: " + answer_str,
                Toast.LENGTH_SHORT);
        toast.show();
        new_movie.getAnswer_vector().set(question, answer);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                new_movie.setAudio_record_path(data.getStringExtra("FILE_NAME"));
                // Do something with the contact here (bigger example below)
            }
        }
    }
//insert new video to DB
    public void InsertNewRec() {
        try{
            HttpClient httpClient = new DefaultHttpClient();
            // post header
            HttpPost httpPost = new HttpPost("http://kidadvisor.host22.com/SavingMov.php");
            // add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("movieLink", new_movie.getLink()));
            nameValuePairs.add(new BasicNameValuePair("movieName", new_movie.getMovie_name()));
            nameValuePairs.add(new BasicNameValuePair("ParentID",new_movie.getParent_ID_mail()));
            nameValuePairs.add(new BasicNameValuePair("mFileName",new_movie.getAudio_record_path()));
           // nameValuePairs.add(new BasicNameValuePair("viewsAmount",new_movie.getViews_amount().toString()));
            nameValuePairs.add(new BasicNameValuePair("answerStr",new_movie.getAnswer_string()));


            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        // execute HTTP post request
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity resEntity = response.getEntity();

        if (resEntity != null) {

            String responseStr = EntityUtils.toString(resEntity).trim();
            Log.v("", "Response: " + responseStr);
        }
    }
        catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        }


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_new_vid, menu);
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

    private final class AsyncSender extends AsyncTask<Void, Void, Void> {

        ProgressDialog pd;

        protected void onPreExecute() {
            super.onPreExecute();
            dbg_obj.show_line(url_view.getText().toString());
            new_movie.setMovie_name(url_view.getText().toString());
            pd = new ProgressDialog(AddNewVid.this);
            pd.setTitle("Sending Data");
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
           //if the user edits existing movie
            if (editing_movie){
                edit_movie_obj.edit_movie(new_movie);
            }
            //if the user adds new movie
            else {
                InsertNewRec();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pd.dismiss();
            if (editing_movie) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Editing succesfull!",
                        Toast.LENGTH_SHORT);
                toast.show();
                //Return to parent menu (It called us)
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                return;
            }
            //Start a new main menu
            Intent intent = new Intent(AddNewVid.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}



