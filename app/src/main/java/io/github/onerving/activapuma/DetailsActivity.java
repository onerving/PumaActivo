package io.github.onerving.activapuma;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class DetailsActivity extends AppCompatActivity {
    private JSONObject mEventJsonData;
    private String mTitle;
    private String mImageUrl;
    private String mPlace;
    private String mLocationName;
    private String mType;
    private String mDescription;
    private String mDate;
    private double mLatitude;
    private double mLongitude;

    private ImageView mBackgroundImage;
    private TextView mEventTitle;
    private TextView mEventDescription;
    private TextView mEventType;
    private TextView mDateTime;
    private TextView mPlaceDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        try {
            mEventJsonData = new JSONObject(getIntent().getStringExtra("eventJsonData"));
            setProperties(mEventJsonData);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        mEventTitle = findViewById(R.id.eventTitle);
        mEventTitle.setText(mTitle);

        mEventType = findViewById(R.id.eventType);
        mEventType.setText(mType);

        mEventDescription = findViewById(R.id.eventDescription);
        mEventDescription.setText(mDescription);

        mPlaceDescription = findViewById(R.id.placeDescription);
        mPlaceDescription.setText(mDescription);

        mDateTime = findViewById(R.id.dateTime);
        mDateTime.setText(mDate);

        mBackgroundImage = findViewById(R.id.background_image);
        Picasso.with(this).load(mImageUrl).fit().centerCrop().into(mBackgroundImage);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.locationFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MapActivity.class);
                intent.putExtra("latitude", mLatitude);
                intent.putExtra("longitude", mLongitude);
                intent.putExtra("locationName", mLocationName);
                startActivity(intent);
            }
        });

    }

    private void setProperties(JSONObject mEventJsonData) throws JSONException {
        mTitle = mEventJsonData.getString("name");
        mImageUrl = mEventJsonData.getString("image");
        mLocationName = mEventJsonData.getString("place");
        mType = mEventJsonData.getString("category");
        mDescription = mEventJsonData.getString("description");
        JSONObject locationCoords = mEventJsonData.getJSONArray("location").getJSONObject(0);
        mLatitude = locationCoords.getDouble("latitude");
        mLongitude = locationCoords.getDouble("longitude");

        Date date = new Date(mEventJsonData.getLong("dateUnix") * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy - HH:mm a");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-6"));
        mDate = sdf.format(date);
    }


}
