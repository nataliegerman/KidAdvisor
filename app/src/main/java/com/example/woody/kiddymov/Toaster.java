package com.example.woody.kiddymov;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

/**
 * Created by user on 09/01/2016.
 */
public class Toaster {
    private Context context;
    public Toaster(Context context) {
        this.context = context;
    }
    public void show_line(String line){
        Toast toast = Toast.makeText(context,
                line,
                Toast.LENGTH_SHORT);
        toast.show();
    }
}
