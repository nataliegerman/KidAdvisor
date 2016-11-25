package com.example.woody.kiddymov;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ParentMenu extends ActionBarActivity {
    private EditText search_key_text;
    static int EDIT_MOVIE_FINISHED = 5;
    ListView listView ;
    ArrayAdapter<String> adapter;
    private ArrayList<Movie> list_of_movies;
    private ArrayList<Movie> list_of_recommended_movies;
    DBobject db_object;
    private Movie selected_movie;
    private EditMovie delete_movie_obj;
    private Toaster toaster_obj;
    private Algorithm algorithm;
    private boolean showing_user_movies = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toaster_obj = new Toaster(getApplicationContext());
        setContentView(R.layout.activity_parents_menu); //set xml view
        search_key_text = (EditText) findViewById(R.id.key_to_search);

        delete_movie_obj = new EditMovie();

        listView = (ListView) findViewById(R.id.list);
        String user = getUsername();
        db_object = new DBobject(user);
        //downloads and shows the movies list from the server
        updateMovieList();

        // ListView Item Click Listener
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                //showing user's existing movies
                if (showing_user_movies) {
                    selected_movie = list_of_movies.get(itemPosition);
                }
                //showing recommended movies
                else {
                    selected_movie = list_of_recommended_movies.get(itemPosition);
                    String url = "https://" + selected_movie.getLink();
                    Intent temp_intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(temp_intent);
                    finish();
                }
            }

        });
    }

    private void filter_existing_movies(){
        for (int i = 0; i < list_of_movies.size(); i++){
            String user_link = list_of_movies.get(i).getLink();
            Iterator<Movie> movie_iterator = list_of_recommended_movies.iterator();
            while (movie_iterator.hasNext()){
                Movie movie = movie_iterator.next();
                if (true == movie.getLink().equals(user_link))
                {
                    //Remove existing movie from list of recommended movies
                    movie_iterator.remove();
                }
            }
        }

    }
    private void showRecommendedMovies(){

        list_of_recommended_movies  = db_object.getOtherUsersMovieList();
        if (list_of_recommended_movies.size() == 0){
            toaster_obj.show_line("DB connection error!");
        }
        //Remove movies from the recommendation list which we already have
        filter_existing_movies();

        algorithm = new Algorithm(this.list_of_movies);

        //toaster_obj.show_line(algorithm.get_weighted_average_ans_str());
        algorithm.sort_movies(list_of_recommended_movies);

        int max_recomendations_amount = 5;
        if (max_recomendations_amount >= list_of_recommended_movies.size()){
            max_recomendations_amount = list_of_recommended_movies.size();
        }
        ArrayList<String> movies_to_suggest = new ArrayList<String>();
        for (int i = 0; i < max_recomendations_amount; i ++){
            movies_to_suggest.add(i, list_of_recommended_movies.get(i).getMovie_name());
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, movies_to_suggest);

        // Assign adapter to ListView
        listView.setAdapter(adapter);
        showing_user_movies = false;
    }

    private void updateMovieList(){
      //  toaster_obj.show_line("Getting movies from DB...");
        list_of_movies = db_object.getMovieList();
        if (list_of_movies.size() == 0){
            toaster_obj.show_line("DB connection error!");
        }

        //Sort movies by views amount to display to parent sorted
        MovieViewsAmountComparator views_comparator = new MovieViewsAmountComparator();
        Collections.sort(list_of_movies,views_comparator);

        ArrayList<String> movies = new ArrayList<String>();
        for (int i = 0; i < list_of_movies.size(); i ++){
            movies.add(i, list_of_movies.get(i).getMovie_name() +
                    " ,views = " + list_of_movies.get(i).getViews_amount().toString());
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, movies);

        // Assign adapter to ListView
        listView.setAdapter(adapter);
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

    public void searchVidOnLine(View view){
        Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
        //Only support viewing of youtube videos
        String query = search_key_text.getText().toString() + " site:www.youtube.com";
        search.putExtra(SearchManager.QUERY, query);
        startActivity(search);
        finish();
    }

    public void recommendMovies(View view){
        toaster_obj.show_line("Recommending movies");
        ImageButton edit_btn = (ImageButton) findViewById(R.id.editBtn);
        ImageButton delete_btn = (ImageButton) findViewById(R.id.deleteBtn);
        edit_btn.setVisibility(View.INVISIBLE);
        delete_btn.setVisibility(View.INVISIBLE);
        showRecommendedMovies();

    }

    public void editVid(View view){
        if (selected_movie == null){
            toaster_obj.show_line("select movie to edit!");
            return;
        }
        Intent intent = new Intent(this,AddNewVid.class);
        intent.putExtra(Intent.EXTRA_TEXT,"edit");
        intent.putExtra("link",selected_movie.getLink());
        intent.putExtra("id",selected_movie.getId().toString());
        intent.putExtra("answer_str", selected_movie.getAnswer_string());
        intent.putExtra("movie_name", selected_movie.getMovie_name());
        intent.putExtra("audio_record", selected_movie.getAudio_record_path());
        intent.putExtra("views_amount", selected_movie.getViews_amount().toString());
        startActivityForResult(intent, EDIT_MOVIE_FINISHED);
    }

    public void deleteVid(View view){
        if (selected_movie == null){
            toaster_obj.show_line("select movie to delete!");
            return;
        }
        delete_movie_obj.delete_movie(selected_movie);
        updateMovieList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_MOVIE_FINISHED) {
            updateMovieList();
        }
    }

        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_parent_menu, menu);
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

}
