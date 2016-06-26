package com.shahmalav.androidprojects.sunshine;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public static class PlaceholderFragment extends Fragment{

        public PlaceholderFragment(){

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView =inflater.inflate(R.layout.fragment_main, container);

            List<String> mockList = new ArrayList<String>();
            mockList.add("Today - Sunny - 88/63");
            mockList.add("Tomorrow - Foggy - 70/46");
            mockList.add("Weds - Cloudy - 72/63");
            mockList.add("Thurs - Rainy - 64/51");
            mockList.add("Fri - Foggy - 70/46");
            mockList.add("Sat - Sunny - 76/68");

            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }
}
