package com.example.woody.kiddymov;

import java.util.ArrayList;

/**
 * Created by user on 05/12/2015.
 */
public class Movie {
    //id, link, parent_id, audio_record, views_amount
    private int id;
    public Double vector_distance = 0.0;
    private String link;
    private String movie_name;
    private String parent_ID_mail;
    private String audio_record_path;
    private ArrayList<Integer> answer_vector;
    private String audio_record_path_full;
    private int views_amount;


    public ArrayList<Integer> getAnswer_vector() {
        return answer_vector;
    }

    public void setAnswer_vector(ArrayList<Integer> answer_vector) {
        this.answer_vector = answer_vector;
    }
//calculate the distance from kid's normalized vector
    public void calc_vector_distance(ArrayList<Float> normalized_ans_vector){
        double sum = 0;
        //Euklidian distance: root of sum of squares
        for (int i = 0; i < this.answer_vector.size(); i++){
            sum += Math.pow(((double)this.answer_vector.get(i) - (double)normalized_ans_vector.get(i)), (double)2);
        }
        vector_distance = Math.sqrt(sum);
    }

    public String getAnswer_string() {

        return answerVectorToString(this.answer_vector);
    }

    public void setAnswerString(String answer_str) {
        for (int i = 0; i < answer_str.length(); i++){
            String num = "" + answer_str.charAt(i);
            Integer answer = Integer.parseInt(num);
            this.answer_vector.set(i, answer);
        }
    }


    public String getAudio_record_path_full() {
        return audio_record_path_full;
    }

    public void setAudio_record_path_full(String audio_record_path_full) {
        this.audio_record_path_full = audio_record_path_full;
    }
    public String answerVectorToString(ArrayList <Integer> answer_vector){
        String res = "";
        for (int i = 0; i < answer_vector.size(); i++){
            res = res + answer_vector.get(i).toString();
        }
        return res;
    }

    public Movie(Integer id, String link, String name, String parent_ID_mail, String audio_record_path,String audio_record_path_full,
                 Integer views_amount) {
        this.id = id;
        this.link = link;
        this.movie_name = name;
        this.parent_ID_mail = parent_ID_mail;
        this.audio_record_path_full = audio_record_path_full;
        this.audio_record_path = audio_record_path;
        this.views_amount = views_amount;
        this.answer_vector = new ArrayList<Integer>();
        answer_vector.add(0,0);
        answer_vector.add(1,0);
        answer_vector.add(2,0);
        answer_vector.add(3,0);
        answer_vector.add(4,0);
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }
    public String getMovie_name() {
        return movie_name;
    }

    public void setLink(String link) {
        this.link = link;
    }
    public void setMovie_name (String name) {this.movie_name = name;}

    public String getParent_ID_mail() {
        return parent_ID_mail;
    }

    public void setParent_ID_mail(String parent_ID_mail) {
        this.parent_ID_mail = parent_ID_mail;
    }

    public String getAudio_record_path() {
        return audio_record_path;
    }

    public void setAudio_record_path(String audio_record_path) {
        this.audio_record_path = audio_record_path;
    }

    public Integer getViews_amount() {
        return views_amount;
    }


    public void setViews_amount(int views_amount) {
        this.views_amount = views_amount;
    }
}
