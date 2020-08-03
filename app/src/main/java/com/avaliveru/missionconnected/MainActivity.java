package com.avaliveru.missionconnected;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.avaliveru.missionconnected.ui.AllClubEventsActivity;
import com.avaliveru.missionconnected.ui.AllClubsActivity;
import com.avaliveru.missionconnected.ui.ClubsDetailsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.annotations.Nullable;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private MenuItem mClubsTabAddClubMenuItem;
    private Toolbar toolBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private AppBarConfiguration mAppBarConfiguration;
    private BottomNavigationView bottomNavigation;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolBar = (Toolbar) findViewById (R.id.toolbar);
        setSupportActionBar(toolBar);

        drawerLayout = findViewById(R.id.navigation_layout);
        navigationView = findViewById ( R.id.navigation_view_drawer );
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_drawer_support, R.id.navigation_drawer_signout)
                .setDrawerLayout( drawerLayout)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController( navigationView, navController);

        bottomNavigation = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(bottomNavigation, navController);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
          // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
          // getSupportActionBar().setHomeButtonEnabled(true);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolBar, R.string.home_navigation_drawer_open, R.string.home_navigation_drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

       // View navView = navigationView.inflateHeaderView ( R.layout.navigation_header_layout);
        //NavProfileImage = navView.findViewById ( R.id.nav_profile_image );
        //NavProfileFullName =  navView.findViewById ( R.id.nav_user_full_name );
       // NavProfileEmail =  navView.findViewById ( R.id.nav_user_email );

        //Handle visibility of the application bottom navigation

    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /*********************************************************
    *Create a App Menu button to add new clubs
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.myclubs_tab_menu, menu);
        mClubsTabAddClubMenuItem = menu.findItem(R.id.mybutton);
        mClubsTabAddClubMenuItem.setVisible(false);
        return true;
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mybutton) {
                //String clubID = getIntent().getStringExtra("clubName");
                Intent newIntent = new Intent(MainActivity.this, AllClubsActivity.class);
                //newIntent.putExtra("clubID", clubID);
                startActivity(newIntent);
        }
        return super.onOptionsItemSelected(item);
    }
    public MenuItem getmClubsTabAddClubMenuItem() {
        return mClubsTabAddClubMenuItem;
    }
    /*
    END Create a App Menu button to add new clubs
    ***********************************************************/

}