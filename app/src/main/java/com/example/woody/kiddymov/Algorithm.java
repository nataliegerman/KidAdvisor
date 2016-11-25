package com.example.woody.kiddymov;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by User on 2/19/2016.
 */
public class Algorithm {
    private ArrayList <Float> weighted_average_ans_vect;
//getting array of movies and calculates kid's preferences vector
    public Algorithm(ArrayList<Movie> movie_list){
        calculate_weighted_average_ans_vect(movie_list);
    }

    public String get_weighted_average_ans_str() {
        String ret = "weighted average ans vector: ";
        for (int i = 0; i < 5; i ++){
            ret = ret + weighted_average_ans_vect.get(i).toString() + ", ";
        }

        return ret;
    }
//sorts movie by distance from the average_weighted_vector - the smallest distance is the first
    public void sort_movies(ArrayList <Movie> movie_list){
        //First, we need to set each movie's distance from weighted_average_ans_vect.
        for (int i = 0; i< movie_list.size(); i++){
            movie_list.get(i).calc_vector_distance(this.weighted_average_ans_vect);
        }

        MovieDistanceComparator comparator = new MovieDistanceComparator();
        //Now, we use the sort method. It sorts according to vector distance of each movie.
        Collections.sort(movie_list, comparator);
    }
    private void calculate_weighted_average_ans_vect(ArrayList <Movie> kid_movie_list) {
         weighted_average_ans_vect = new ArrayList<Float>();
        //Init vector , 5 - questions amount
        for (int i = 0; i < 5; i++){
            weighted_average_ans_vect.add(i, (float) 0);
        }

        Integer total_views_amount = 0;
        Integer views_amount = 0;
        for (int i=0; i< kid_movie_list.size(); i++){
            ArrayList<Integer> ans_vect = kid_movie_list.get(i).getAnswer_vector();
            //Add one here so that if for example no movie was ever watched, we will get a simple average of all movie vectors.
            //Also, this way there is no danger we will divide by zero When we normalize.
            views_amount = kid_movie_list.get(i).getViews_amount()+1;
            total_views_amount += views_amount;
            for (int j = 0; j < 5; j ++){
                float answer = weighted_average_ans_vect.get(j);
                answer += (float)(ans_vect.get(j)*views_amount);
                weighted_average_ans_vect.set(j, answer);
            }
        }

        //Normalize vector
        for (int i = 0; i < 5; i++){
            float answer = weighted_average_ans_vect.get(i);
            answer = answer/total_views_amount;
            weighted_average_ans_vect.set(i, answer);
        }
    }
}


