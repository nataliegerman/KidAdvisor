package com.example.woody.kiddymov;

import java.util.Comparator;

/**
 * Created by User on 4/26/2016.
 */
public class MovieViewsAmountComparator implements Comparator<Movie> {

        public int compare(Movie movie1, Movie movie2) {
            //We compare between the distances of both movies from a normalized vector. the smaller the distnce,
            //better the movie
            return (movie2.getViews_amount() -  movie1.getViews_amount());
        }
}
