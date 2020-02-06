package com.guna.ocrsample;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

public class SuggestionsActivity extends AppCompatActivity {

    private String[] static_nutrients = {"Protein", "Carbohydrates", "Fats"};
    private Api api;
    private String OCR_Data;
    private LinearLayout ResultsLayout;
    private LinearLayout GraphLayout;
    private ArrayList<String> nutrient_headers = new ArrayList<String>();
    private ArrayList<Double> cur_goal = new ArrayList<Double>();
    private ArrayList<Double> cur_history = new ArrayList<Double>();
    private ArrayList<Double> cur_meal = new ArrayList<Double>();
    private ArrayList<TextView> bars = new ArrayList<TextView>();

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            init(result);
        }
    }

    private void init(String result){
        Log.e("pickhacks", result);
        Log.e("pickhacks", OCR_Data);
        api = new Api(OCR_Data, result);
        populate_checklist();
        nutrient_headers = api.getHeaders();
        int margin = 10;
        int width = GraphLayout.getWidth();
        int indiv_w = width/nutrient_headers.size()-margin*2;
        for (int i=0;i<nutrient_headers.size();++i) {
            // Create a new progress bar programmatically
            TextView txV = new TextView(this);
            txV.setText("");
            GraphLayout.addView(txV);
//            ProgressBar pb = new ProgressBar(getApplicationContext(), null, android.R.attr.progressBarStyleHorizontal);
//
//            // Create new layout parameters for progress bar
//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT, // Width in pixels
//                    20 // Height of progress bar
//            );
//
//            pb.setVisibility(View.VISIBLE);
//
//            // Apply the layout parameters for progress bar
//            pb.setLayoutParams(lp);
//
//            // Set the progress bar color
//            pb.getProgressDrawable().setColorFilter(0x008C72, PorterDuff.Mode.SRC_IN);
//
//            // Finally,  add the progress bar to layout
//            GraphLayout.addView(pb);
//            pb.setProgress(80);
            bars.add(txV);

            cur_meal.add(0.0);
            cur_history.add(0.0);
            cur_goal.add(1.0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);
        ResultsLayout = findViewById(R.id.suggestions_resultsLayout);
        GraphLayout = findViewById(R.id.llBarView);
        OCR_Data = getIntent().getExtras().getString("ocr_data");
        double[] tmpDbl = getIntent().getExtras().getDoubleArray("goal_array");
        cur_goal.clear();
        for (int i=0;i<tmpDbl.length;++i) cur_goal.add(tmpDbl[i]);
        new JsonTask().execute("http://pickhacks-scanmenu.appspot.com/api");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_suggestions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionMain:
                this.finish();
                break;
            case R.id.actionProfile:
                showProfile();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showProfile(){
        Intent intentProfile = new Intent(this, ProfileActivity.class);
        startActivity(intentProfile);
    }

    private void populate_checklist(){
        HashSet<String> foods = api.getValidFoods();
        if (foods != null) {
            for (String s : foods) {
                CheckBox cb = new CheckBox(getApplicationContext());
                cb.setText(s);
                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        update_graph(compoundButton.getText().toString(), b);
                    }
                });
                ResultsLayout.addView(cb);
            }
        }
    }

    private void update_graph(String foodItem, boolean b){
        Toast.makeText(this, foodItem + "|" + b, Toast.LENGTH_LONG).show();
        Pair<String, ArrayList<Double>> cur_food = api.getFood(foodItem, "");
        ArrayList<Double> cur_food_nutrients = cur_food.second;
        if (b){
            for (int i=0;i<cur_food_nutrients.size();++i){
                cur_meal.set(i, cur_meal.get(i) + cur_food_nutrients.get(i));
            }
        }
        else {
            for (int i = 0; i < cur_food_nutrients.size(); ++i) {
                cur_meal.set(i, Math.max(0.0, cur_meal.get(i) - cur_food_nutrients.get(i)));
            }
        }
        //Change heights
        for (int i=0;i<cur_food_nutrients.size();++i){
            Log.e("pickhacks", String.valueOf(cur_meal.get(i)));
            bars.get(i).setText(String.format("%.2f%%|%.2f%% ", cur_meal.get(i), cur_goal.get(i)) + static_nutrients[i]);
            if (Math.abs(cur_meal.get(i) - cur_goal.get(i)) < 5.0) bars.get(i).setTextColor(Color.parseColor("#33cc55"));
            else if (cur_meal.get(i) < cur_goal.get(i)) bars.get(i).setTextColor(Color.parseColor("#000000"));
            else bars.get(i).setTextColor(Color.parseColor("#cc5533"));
        }
    }

}
