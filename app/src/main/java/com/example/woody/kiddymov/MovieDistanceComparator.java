package com.example.woody.kiddymov;

import java.util.Comparator;

public class MovieDistanceComparator implements Comparator<Movie> {

    public int compare(Movie movie1, Movie movie2) {
        //We compare between the distances of both movies from a normalized vector. the smaller the distnce,
        //better the movie
        return (int) ((movie1.vector_distance - movie2.vector_distance) * 100.0);
    }
}
