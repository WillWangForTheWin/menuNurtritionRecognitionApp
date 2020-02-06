package com.guna.ocrsample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.guna.ocrlibrary.OCRCapture;

import java.util.ArrayList;

import static com.guna.ocrlibrary.OcrCaptureActivity.TextBlockObject;

public class MainActivity extends AppCompatActivity {

    private double[][] static_goals = {{120.0, 50.0, 100.0}, {120.0, 100.0, 90.0}, {150.0, 100.0, 80.0}};
    private String[] static_nutrients = {"Protein", "Carbohydrates", "Fats"};
    private TextView textView, goalView;
    private final int CAMERA_SCAN_TEXT = 0;
    private final int LOAD_IMAGE_RESULTS = 1;
    private Button Goal_Diet, Goal_Maintain, Goal_Bulk;
    private double[] goal = {100.0, 100.0, 100.0};

    private String OCR_Data = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        goalView = findViewById(R.id.goal_stats);
        Goal_Diet = findViewById(R.id.btn_diet);
        Goal_Diet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goal = static_goals[0];
                update_goalText();
            }
        });

        Goal_Maintain = findViewById(R.id.btn_maintain);
        Goal_Maintain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goal = static_goals[1];
                update_goalText();
            }
        });

        Goal_Bulk = findViewById(R.id.btn_bulk);
        Goal_Bulk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goal = static_goals[2];
                update_goalText();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionCamera:
                OCRCapture.Builder(this)
                        .setUseFlash(false)
                        .setAutoFocus(true)
                        .buildWithRequestCode(CAMERA_SCAN_TEXT);
                break;
            case R.id.actionPhoto:
                if (hasPermission()) {
                    pickImage();
                } else {
                    getPermission();
                }
                break;
            case R.id.actionProfile:
                showProfile();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getPermission() {
// Permission is not granted
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //TODO:
        } else {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    pickImage();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void pickImage() {
        Intent intentGallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intentGallery, LOAD_IMAGE_RESULTS);
    }

    private void showProfile(){
        Intent intentProfile = new Intent(this, ProfileActivity.class);
        startActivity(intentProfile);
    }

    private void showResults(){
        Intent intentSuggestions = new Intent(this, SuggestionsActivity.class);
        intentSuggestions.putExtra("ocr_data", OCR_Data);
        intentSuggestions.putExtra("goal_array", goal);
        startActivity(intentSuggestions);
    }

    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void update_goalText(){
        String s = "Goal % standard intake:\n";
        for (int i=0;i<goal.length;++i) s += String.valueOf(goal[i]) + "% " + static_nutrients[i] + "\n";
        goalView.setText(s);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == CAMERA_SCAN_TEXT) {
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    textView.setText(data.getStringExtra(TextBlockObject));
                    OCR_Data = "";
                    OCR_Data += textView.getText();
                }
            } else if (requestCode == LOAD_IMAGE_RESULTS) {
                Uri pickedImage = data.getData();
                String text = OCRCapture.Builder(this).getTextFromUri(pickedImage);
                textView.setText(text);
                OCR_Data = text;
            }
            showResults();
        }
    }
}
