package com.example.woody.kiddymov;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by user on 08/11/2015.
 */
public class DBobject extends ActionBarActivity{
    private ArrayList<String> list_of_movies_from_db;
    private Boolean get_records;

    private ArrayList<Movie> list_of_movie_objects;
    private ArrayList<Movie> list_of_other_users_movies;
    private String user_name;

    public DBobject (String user) {
        user_name = user;
    }

    public void getMoviesFromDB() {
        if (get_records == false) {
            list_of_movie_objects = new ArrayList<Movie>();
        }
        list_of_movies_from_db = new ArrayList<String>();
        InputStream is = null;
        String result = "";

        try{
            HttpClient httpClient = new DefaultHttpClient();
            // post header
            String php_path = "temp";
            if (get_records == false) {
                php_path = "http://kidadvisor.host22.com/GettingMovies.php";
            }
            else{
                php_path = "http://kidadvisor.host22.com/GettingRecords.php";
            }
            HttpPost httpPost = new HttpPost(php_path);
            // execute HTTP post request
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            is = httpEntity.getContent();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while((line=br.readLine())!=null)
            {
                sb.append(line+"\n");
            }
            is.close();

            result=sb.toString();
            is.close();
            JSONArray Jarray = new JSONArray(result);

            for(int i=0 ;i<Jarray.length();i++)
            {

                list_of_movies_from_db.add(Jarray.getString(i).toString());
            }
        }
        catch (Exception e) {
            Log.v("exception:",e.toString());
            return;
        }

        //separate fields
        for(int i=0;i<list_of_movies_from_db.size();i++)
        {
            try {
                Log.v("l[i]", list_of_movies_from_db.get(i));
                String[] parts = list_of_movies_from_db.get(i).split(",");
                String id = "0";
                String link = "temp";
                String parent_id = "temp";
                String views_amount = "0";
                String audio_rec = "norecord";
                String audio_rec_full = "norecord";
                String answer_str = "00000";
                String movie_name = "no name";
                if (get_records == false){
                    id = parts[0];
                    link = parts[1];
                    parent_id = parts[2]; //parent's email
                    views_amount = parts[3];
                    answer_str = parts[4];
                    movie_name = parts[5];
                }
                else {
                    audio_rec = parts[0];
                    if (false == audio_rec.equals("norecord")) {
                        audio_rec_full = "/data/data/com.example.woody.kiddymov/app_private_folder/" +
                                audio_rec +"audiorecordtest.3gp";
                    }
                }

                if (get_records == false) {
                    Movie temp_movie = new Movie(Integer.parseInt(id), link,movie_name, parent_id, audio_rec, audio_rec_full, Integer.parseInt(views_amount));
                    temp_movie.setAnswerString(answer_str);
                    list_of_movie_objects.add(temp_movie);
                }
                else
                {
                    list_of_movie_objects.get(i).setAudio_record_path(audio_rec);
                    list_of_movie_objects.get(i).setAudio_record_path_full(audio_rec_full);
                }
                Log.v("id", id);
                Log.v("link", link);
                Log.v("parent id", parent_id);
            }//End of try
            catch (Exception e) {
                Log.v("exception:",e.toString());
                //Perhaps a bad entry in DB, shouldnt crash.
                continue;
            }

        } //enf of for(i=..)
    } //end of getMoviesFromDB


    private void performSanityChecks(){
        for (Iterator <Movie> iterator =  list_of_movie_objects.iterator(); iterator.hasNext();) {
            Movie movie = iterator.next();
            try {
                if (movie.getViews_amount() < 0) throw new RuntimeException("movie sanity error");
                if (movie.getLink().startsWith("youtu.be") == false)
                    throw new RuntimeException("movie sanity error");
                if (movie.getAnswer_vector().size() != 5)
                    throw new RuntimeException("movie sanity error");
                for (int j = 0; j < 5; j++) {
                    Integer ans = movie.getAnswer_vector().get(j);
                    if ((ans < 0) || (ans > 2)) throw new RuntimeException("movie sanity error");
                }
            }
            catch (Exception e) {
                //Something isnt right. Remove this
                iterator.remove();
                continue;
            }
        }
    }

    private void filterUserId(){
        list_of_other_users_movies = new ArrayList<Movie>();
        Iterator<Movie> movie_iterator = list_of_movie_objects.iterator();
        int index = 0;
        while (movie_iterator.hasNext()){
            Movie movie = movie_iterator.next();
            if (false == movie.getParent_ID_mail().equals(user_name))
            {
                //Remove irrelevant data from movie
                movie.setAudio_record_path("norecord");
                movie.setAudio_record_path_full("norecord");
                list_of_other_users_movies.add(index, movie);
                index ++;
                movie_iterator.remove();
            }
        }
    }

    private ArrayList<Movie> returnMovieObjectsList() {
        GetMovieObjectsClass get_movies = new GetMovieObjectsClass();
        try {
            ArrayList<Movie> ret;
            ret = get_movies.execute().get();
            return ret;
        } catch (Exception e) {}
        return null;
    }

    public ArrayList<Movie> getMovieList() {
        get_records = false;
        //get movies details
        returnMovieObjectsList();
        get_records = true;
        //get movies records
        returnMovieObjectsList();
        filterUserId();
        performSanityChecks();
        return list_of_movie_objects;
    }

    public ArrayList<Movie> getOtherUsersMovieList() {
        return list_of_other_users_movies;
    }

    private final class GetMovieObjectsClass extends AsyncTask<Void, Void, ArrayList<Movie>> {
        @Override
        protected ArrayList<Movie> doInBackground(Void... params) {
            getMoviesFromDB();
            return list_of_movie_objects;
        }
    } //end of AsyncSender
} //enf of class
