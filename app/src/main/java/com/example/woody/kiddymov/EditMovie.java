package com.example.woody.kiddymov;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.io.IOException;
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


public class EditMovie extends ActionBarActivity {
    private Movie movie;
    private boolean deleting = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_movie);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_movie, menu);
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

    //edit existing video in DB
    private void EditMovie(Movie movie) {
        try{
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost;
            if (deleting){
                httpPost = new HttpPost("http://kidadvisor.host22.com/deleteMovie.php");
            }
            else {
                httpPost = new HttpPost("http://kidadvisor.host22.com/EditMovie.php");
            }

            // add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair("movieID", movie.getId().toString()));
            //When editing existing movie:
            if (deleting == false){
                nameValuePairs.add(new BasicNameValuePair("movieLink", movie.getLink()));
                nameValuePairs.add(new BasicNameValuePair("ParentID",movie.getParent_ID_mail()));
                nameValuePairs.add(new BasicNameValuePair("audioRecord",movie.getAudio_record_path()));
                nameValuePairs.add(new BasicNameValuePair("viewsAmount", movie.getViews_amount().toString()));
                nameValuePairs.add(new BasicNameValuePair("answerStr", movie.getAnswer_string()));
                nameValuePairs.add(new BasicNameValuePair("movieName", movie.getMovie_name()));
            }

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


    } //end of edit movie

    private final class AsyncUpdating extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            EditMovie(movie);
            return null;
        }
    }

    public void delete_movie(Movie movie_to_delete) {
        deleting = true;
        update_movie(movie_to_delete);
    }
    public void edit_movie(Movie movie_to_delete) {
        deleting = false;
        update_movie(movie_to_delete);
    }
    private void update_movie(Movie new_movie)
    {
        movie = new_movie;
        AsyncUpdating edit_movie = new AsyncUpdating();
        edit_movie.execute();
    }
}
