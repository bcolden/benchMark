Almost working main 2
package com.mylab_final.cisc181.finalproject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class MapsMain extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener {


    //Initializing variables
    private GoogleMap myMap;
    boolean locationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;
    private static final String TAG = MapsMain.class.getSimpleName();
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private AlertDialog.Builder userPrompt;
    private AlertDialog.Builder markerInfo;
    private String titleInput;
    private String descInput;
    private ArrayList<MapItem> markerArray = new ArrayList<>();
    private MapItem tryMarker;
    private MapItem putMarker;

    private Button button;
    private TextView myTextView;
    private ImageView mainImage;
    private TextView instructionTextView;
    private TextView functionTextView;

    //Initialization for create_popup
    private Button cameraButton;
    private EditText titleEdit;
    private EditText descriptionEdit;
    private Button scenicButton;
    private Button shadyButton;
    private Button coveredButton;
    private Button workButton;
    private Button createButton;
    private Button cancelButton;

    //Initialization for View Marker
    private ImageView benchPicView;
    private TextView benchNameView;
    private TextView benchDescriptionView;
    private Button upvoteButton;
    private Button downvoteButton;
    private TextView scoreBox;
    private Button closeView;


    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView mImageView;
    private int markerCounter = 0;
    String tempImg = "";



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_main);

        editor = getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

        mainImage = findViewById(R.id.imageView1); //accessing image
        button = findViewById(R.id.button);    //accessing button
        myTextView = findViewById(R.id.TitleText); //accessing caption
        instructionTextView = findViewById(R.id.instructionText);
        functionTextView = findViewById(R.id.functionsText);

        button.setOnClickListener((v) -> {
            Intent intent = new Intent(this, DisplayMessageActivity.class);
            setContentView(R.layout.map_fragment);

            // Grab SupportMapFragment and async data call when it's ready
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        });

    }



    //mainly boilerplate code using tutorials online; this allows map to be shown.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;

        //pull shared preferences HERE

        //Turn on My Location layer then get user location
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
        myMap.setOnMapClickListener(this);
        myMap.setOnMapLongClickListener(this);
        myMap.setOnMarkerClickListener(this);
        myMap.addMarker(new MarkerOptions().position(new LatLng(50, 50)).title("Marker in Sydney"));
    }

    //Creates AlertDialog box to prompt user
    private void promptUser() {
        //LinearLayout necessary for all EditText boxes to be in-line.
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        //creates user input box #1, for title
        final EditText promptTitle = new EditText(this);
        promptTitle.setHint("Title");

        layout.addView(promptTitle);

        //creates user input box #2, for description
        final EditText promptDescription = new EditText(this);
        promptDescription.setHint("Description");
        layout.addView(promptDescription);
        /* Will create user input  box #3 for hashtags, once this is figured out.
        final EditText promptHashtags = new EditText(this);
        promptHashtags.setHint("Tags");
        layout.addView(promptHashtags);
        */

        //initializes AlertDialog box "userPrompt" with information given above
        userPrompt = new AlertDialog.Builder(this);
        userPrompt.setView(layout);
        userPrompt.setTitle("Enter map information").setView(layout);
        //defines event listener for "positive" button (OK, create, etc.)
        userPrompt.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.i("AlertDialog", "Create button was hit");
                Log.i("AlertDialog", "This is the text that was entered:" + promptTitle.getText().toString());
                titleInput = promptTitle.getText().toString();
                descInput = promptDescription.getText().toString();
                LatLng currentCoords = tryMarker.getCoords();
                //putMarker = new MapItem(currentCoords, titleInput, descInput);
                markerArray.add(putMarker);
                populateMap();
            }
        });

        //defines event listener for "negative" button (cancel)
        userPrompt.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.i("AlertDialog", "Cancel button was hit");
            }
        });

        userPrompt.show();
    }

    public void markerZoom(Marker marker, String title, String description) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView titleInfo = new TextView(this);
        titleInfo.setText(title);
        titleInfo.setPadding(15, 15, 15, 15);
        titleInfo.setTextSize(30);
        layout.addView(titleInfo);

        final TextView descriptionInfo = new TextView(this);
        descriptionInfo.setText(description);
        descriptionInfo.setPadding(15, 15, 15, 15);
        descriptionInfo.setTextSize(30);
        layout.addView(descriptionInfo);

        markerInfo = new AlertDialog.Builder(this);
        markerInfo.setView(layout);
        markerInfo.setTitle("Displaying information for current marker");
        markerInfo.setNeutralButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.i("NeutralButton", "Neutral Button Works! Grab your attention!Neutral Button Works! Grab your attention!Neutral Button Works! Grab your attention!");
            }
        });

        Log.i("ATTENTION", "Neutral Button Works! Grab your attention!");
        markerInfo.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView = (ImageView) findViewById(R.id.sampleImg);
            mImageView.setImageBitmap(imageBitmap);
            tempImg = encodeToBase64(imageBitmap);
        }
    }


    //Defining MapItem Object
    public class MapItem {
        String title = "";
        String description = "";
        LatLng coords;
        String base64Img;
        String additionalInfo;

        //constructor 1, temporary, ideally only use second constructor in the future
        public MapItem(LatLng coords) {
            this.coords = coords;
        }

        //second constructor, after button issue can get worked out.

        public LatLng getCoords() {
            return this.coords;
        }

        public void setTitle(String t) {
            this.title = t;
        }

        public String getTitle() {
            return this.title;
        }

        public void setDescription(String d) {
            this.description = d;
        }

        public String getDescription() {
            return this.description;
        }

        public void setIsShady() {
            this.additionalInfo = this.additionalInfo + "\n This bench is shady!";
        }

        public void setIsScenic() {
            this.additionalInfo = this.additionalInfo + "\n This bench is scenic!";
        }

        public void setIsCovered() {
            this.additionalInfo = this.additionalInfo + " \n Safe from the elements";
        }

        public void setWorkBench() {
            this.additionalInfo = this.additionalInfo + "\n Good place to do work";
        }

        public void setAdditionalInfo() {
            this.description = this.description + this.additionalInfo;
        }

        public void setBase64Img(String s) {
            this.base64Img = s;
        }

        public String getBase64Img() {
            return this.base64Img;
        }

    }

    public void populateMap() {
        if (markerArray.size() == 0) {
            int tempCounter = 0;
            while (true) {

            }
        }
        for (MapItem g: markerArray) {
            myMap.addMarker(new MarkerOptions().position(g.coords));
        }
    }


    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;

            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (myMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                myMap.setMyLocationEnabled(true);
                myMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                myMap.setMyLocationEnabled(false);
                myMap.getUiSettings().setMyLocationButtonEnabled(false);
                //Google Maps API documentation says this line is necessary
                //but variable type not specified
                //myLastKnownLocation = null;
            }
        }
        catch (SecurityException e) {
                Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        //this function gets most recent device location, which is rare when location != available
        try {
            if (locationPermissionGranted) {
                //android error suggestion is handled already, suppress.
                @SuppressLint("MissingPermission") Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            //set map camera to current position
                            mLastKnownLocation = (Location) task.getResult();
                            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM
                            ));
                        }
                        else {
                            Log.d(TAG, "No current location. Set default");
                            Log.d(TAG, "Exception: %s", task.getException());
                            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            myMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        }
        catch(SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    @Override
    public void onMapClick(LatLng point) {
        //on click this creates new marker using MapItem's base constructor, to get coordinate data into the AlertDialog UI
        setContentView(R.layout.relative_create_popup);
        createMarker(point);
        //calls user prompt method
        //promptUser();

    }
        /*
    @Override
    public void onDestroy() {

        Fragment f = (Fragment) getFragmentManager().findFragmentById(R.id.map);
        if (f != null) {
            getFragmentManager().beginTransaction().remove(f).commit();
        }

        super.onDestroy();
    }
    */
    public static String encodeToBase64(Bitmap image) {
        Bitmap myImage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        myImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        return imageEncoded;
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public void createMarker(LatLng coords) {
        String tempTitle;
        String tempDescription;

        MapItem tempMapItem = new MapItem(coords);


        cameraButton = (Button) findViewById(R.id.cameraButton);
        titleEdit = (EditText) findViewById(R.id.titleEdit);
        descriptionEdit = (EditText) findViewById(R.id.descriptionEdit);
        scenicButton = (Button) findViewById(R.id.scenicButton);
        shadyButton = (Button) findViewById(R.id.shadyButton);
        coveredButton = (Button) findViewById(R.id.coveredButton);
        workButton = (Button) findViewById(R.id.workButton);
        createButton = (Button) findViewById(R.id.createButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);


        cameraButton.setOnClickListener((camera) -> {
            dispatchTakePictureIntent();
            tempMapItem.setBase64Img(tempImg);
        });


        tempMapItem.setTitle(titleEdit.getText().toString());
        tempMapItem.setDescription(descriptionEdit.getText().toString());


        scenicButton.setOnClickListener((scenic) -> {
            tempMapItem.setIsScenic();
            scenicButton.setBackgroundColor(Color.BLUE);
        });

        shadyButton.setOnClickListener((shady) -> {
            tempMapItem.setIsShady();
            shadyButton.setBackgroundColor(Color.BLUE);
        });

        coveredButton.setOnClickListener((covered) -> {
            tempMapItem.setIsCovered();
            coveredButton.setBackgroundColor(Color.BLUE);
        });

        workButton.setOnClickListener((work) -> {
            tempMapItem.setWorkBench();
            workButton.setBackgroundColor(Color.BLUE);
        });

        createButton.setOnClickListener((create) -> {
            //tempMapItem.setAdditionalInfo();
            //markerArray.add(tempMapItem);
            //editor.putString("title"+markerCounter, tempMapItem.getTitle());
            //editor.putString("description"+markerCounter, tempMapItem.getDescription());
            //editor.putFloat("latitude"+markerCounter, (float) coords.latitude);
            //editor.putFloat("longitude"+markerCounter, (float) coords.longitude);
            //editor.putString("image"+markerCounter, tempMapItem.getBase64Img());
            //populateMap();

            setContentView(R.layout.map_fragment);

            // Grab SupportMapFragment and async data call when it's ready
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        });

        cancelButton.setOnClickListener((cancel) -> {
            //setContentView, based on how it should work
        });


    }

    public void viewMarker(MapItem g) {
        benchPicView = (ImageView) findViewById(R.id.benchPicView);
        benchNameView = (TextView) findViewById(R.id.benchNameView);
        benchDescriptionView = (TextView) findViewById(R.id.benchDescriptionView);
        //upvoteButton = (Button) findViewById(R.id.upvoteButton);
        //downvoteButton = (Button) findViewById(R.id.downvoteButton);
        //scoreBox = (TextView) findViewById(R.id.scoreBox);
        closeView = (Button) findViewById(R.id.closeView);

        benchPicView.setImageBitmap((decodeBase64(g.getBase64Img())));
        benchNameView.setText(g.getTitle());
        benchDescriptionView.setText(g.getDescription());

        closeView.setOnClickListener((v) -> {
            //return to Map
        });

    }




    @Override
    public void onMapLongClick(LatLng point) {
        //For testing; long click clears map, next marker input will re-populate fully!
        createMarker(point);
        myMap.clear();
        dispatchTakePictureIntent();
    }



    @Override
    public boolean onMarkerClick(Marker marker) {
        MapItem clickedItem = markerArray.get(0);
        setContentView(R.layout.view_popup);
        String tempTitle ="";
        String tempDescription = "";
        //current error: not able to distinguish coordinates between the two!!! Ask prof. Rasmussen?
        for (MapItem g: markerArray) {
            double lat1 = g.getCoords().latitude;
            double long1= g.getCoords().longitude;
            double lat2 = marker.getPosition().latitude;
            double long2 = marker.getPosition().longitude;
            double threshold = .0005;
            if ((Math.abs(lat1-lat2) < threshold) && (Math.abs(long1-long2) < threshold)) {
                clickedItem = g;
                break;
            }
        }

        viewMarker(clickedItem);

        //markerZoom(marker, tempTitle, tempDescription);
        return false;
    }
}
