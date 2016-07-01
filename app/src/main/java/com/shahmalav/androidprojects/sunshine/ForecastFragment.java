package com.shahmalav.androidprojects.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.util.TimeUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by shahm on 6/26/2016.
 */
public class ForecastFragment extends Fragment {
    public ForecastFragment(){}

    private ArrayAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        List<String> mockList = new ArrayList<String>();
        mockList.add("Today - Sunny - 88/63");
        mockList.add("Tomorrow - Foggy - 70/46");
        mockList.add("Weds - Cloudy - 72/63");
        mockList.add("Thurs - Rainy - 64/51");
        mockList.add("Fri - Foggy - 70/46");
        mockList.add("Sat - Sunny - 76/68");
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
        fetchWeatherTask.execute("94132");

        View rootView =inflater.inflate(R.layout.fragment_main, container, false);

        adapter = new ArrayAdapter(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,mockList);
        ListView listView = (ListView) rootView.findViewById(R.id.list_view_forecast);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = adapter.getItem(position).toString();
        //        Toast.makeText(getActivity(),forecast, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, forecast );
                startActivity(intent);

            }
        });

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private String getDateString(long time){
            return new SimpleDateFormat("EEE MMM dd").format(time);
        }

        private String formatHighLow(double high, double low){
            return Math.round(high) + "/" + Math.round(low);
        }

        private String[] getDataFromJson(String jsonStr, int numDays) throws JSONException{

            final String LIST = "list";
            final String WEATHER = "weather";
            final String TEMPERATURE = "temp";
            final String TEMP_MAX = "max";
            final String TEMP_MIN = "min";
            final String DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(jsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(LIST);

            Time dayTime = new Time();
            dayTime.setToNow();
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
            dayTime = new Time();

            String[] resultArray = new String[numDays];

            for(int i = 0; i < weatherArray.length(); i++) {
                String day;
                String description;
                String highAndLow;

                JSONObject dayForecast = weatherArray.getJSONObject(i);

                JSONObject weatherObject = dayForecast.getJSONArray(WEATHER).getJSONObject(0);
                description = weatherObject.getString(DESCRIPTION);

                JSONObject temperatureObject = dayForecast.getJSONObject(TEMPERATURE);
                double high = temperatureObject.getDouble(TEMP_MAX);
                double low = temperatureObject.getDouble(TEMP_MIN);

                long dateTime;
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getDateString(dateTime);

                highAndLow = formatHighLow(high, low);
                resultArray[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultArray) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultArray;
        }


        @Override
        protected String[] doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJSON = null;

            int days = 7;
            String unit = "metric";
            String format = "json";
            String key = "ea952fe159c6a23d8e55d8e4d4d374d1";

            try {

                final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "appid";

                Uri uriBuilder = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, unit)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(days))
                        .appendQueryParameter(APPID_PARAM,key)
                        .build();

                URL url = new URL(uriBuilder.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null){
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = reader.readLine()) != null){
                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0){
                    return null;
                }

                forecastJSON = buffer.toString();

            }catch (IOException ex){
                //
                Log.e(LOG_TAG, "Error ", ex);
            }catch (Exception ex){
                return null;
            }finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
                if(reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG,"Error closing stream",e);
                    }
                }
            }

            try {
                return getDataFromJson(forecastJSON, days);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        return null;

        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                adapter.clear();
                for(String dayForecastStr : result) {
                    adapter.add(dayForecastStr);
                }
            }
        }
    }
}
