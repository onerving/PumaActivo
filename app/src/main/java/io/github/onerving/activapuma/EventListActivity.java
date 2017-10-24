package io.github.onerving.activapuma;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

import io.github.onerving.activapuma.data.JsonUtils;

public class EventListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<JSONArray>,
        SportsEventsAdapter.SportsEventsAdapterOnClickHandler
{


    private String TAG = (String) this.getClass().getSimpleName();

    private static final int RC_SIGN_IN = 740;
    private static final int SPORTS_EVENTS_LOADER_ID = 304;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressBar mLoadingIcon;
    private TextView mErrorMessage;
    private RecyclerView mRecyclerView;
    private SportsEventsAdapter mSportsEventsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        // Inicializamos instancias de la UI
        mLoadingIcon = (ProgressBar) findViewById(R.id.progressBar);
        mErrorMessage = (TextView) findViewById(R.id.errorMessage);
        mRecyclerView = (RecyclerView) findViewById(R.id.eventsRecyclerView);

        mErrorMessage.setVisibility(View.GONE);
        mLoadingIcon.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);

        // Inicializamos el drawer y el navigation view
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Preparamos el RecyclerView con nuestro SportsEventsAdapter
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mSportsEventsAdapter = new SportsEventsAdapter(this);
        mRecyclerView.setAdapter(mSportsEventsAdapter);

        // Manejamos el login de usuarios con Firebase
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged: signed_in");
                } else {
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(
                                            Arrays.asList(
                                                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
                                            )
                                    ).build(),
                            RC_SIGN_IN
                    );
                }
            }
        };


        // Cargar lista de eventos con el Loader
        getSupportLoaderManager().initLoader(SPORTS_EVENTS_LOADER_ID, null, this);
        mLoadingIcon.setVisibility(View.VISIBLE);
        makeSportsEventsQuery();
    }

    private void makeSportsEventsQuery(){
        Bundle queryBundle = new Bundle();
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<JSONArray> sportsEventsLoader = loaderManager.getLoader(SPORTS_EVENTS_LOADER_ID);

        if (sportsEventsLoader == null) {
            loaderManager.initLoader(SPORTS_EVENTS_LOADER_ID, queryBundle, this);
        } else {
            loaderManager.restartLoader(SPORTS_EVENTS_LOADER_ID, queryBundle, this);
        }
    }

    private void showErrorMessage(){
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    private void showJsonData(){
        mErrorMessage.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_refresh:
                getSupportLoaderManager().restartLoader(SPORTS_EVENTS_LOADER_ID, null, this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_notifications) {
        } else if (id == R.id.nav_logout) {
            AuthUI.getInstance().signOut(this);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public Loader<JSONArray> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<JSONArray>(this) {

            JSONArray mJsonResults;
            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                if(null != mJsonResults){
                    deliverResult(mJsonResults);
                } else {
                    forceLoad();
                }
            }

            @Override
            public void deliverResult(JSONArray data) {
                mJsonResults = data;
                showJsonData();
                super.deliverResult(data);
            }

            @Override
            public JSONArray loadInBackground() {
                return JsonUtils.getJsonSportsInfo(getContext());
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<JSONArray> loader, JSONArray sportsInfo) {
        mLoadingIcon.setVisibility(View.GONE);
        if(null != sportsInfo){
            mSportsEventsAdapter.setmSportsEvents(sportsInfo);
            showJsonData();
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<JSONArray> loader) { }

    @Override
    public void onClick(JSONObject eventJsonData) {
        Context context = this;
        Class destinationClass = DetailsActivity.class;
        Intent intent = new Intent(context, destinationClass);
        intent.putExtra("eventJsonData", eventJsonData.toString());
        startActivity(intent);
    }
}
