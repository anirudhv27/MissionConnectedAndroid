package com.avaliveru.missionconnected;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.avaliveru.missionconnected.ui.AllClubsActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private MenuItem mClubsTabAddClubMenuItem;
    private Toolbar toolBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private AppBarConfiguration mAppBarConfiguration;
    private BottomNavigationView bottomNavigation;
    private ImageView profileImage;
    private TextView profileName;
    private TextView profileSchool;

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
                R.id.navigation_drawer_support,  R.id.navigation_drawer_policy,R.id.navigation_drawer_signout)
                .setDrawerLayout( drawerLayout)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController( navigationView, navController);

        bottomNavigation = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(bottomNavigation, navController);

        navigationView.removeHeaderView(navigationView.getHeaderView(0));
        View navHeaderView = navigationView.inflateHeaderView ( R.layout.nav_drawer_header_main);

        profileImage = navHeaderView.findViewById ( R.id.nav_head_avatar );
        profileName =  navHeaderView.findViewById ( R.id.nav_head_username );
        profileSchool=  navHeaderView.findViewById ( R.id.nav_head_school );

        FirebaseUser loginUser = FirebaseAuth.getInstance().getCurrentUser();
        Glide.with(MainActivity.this)
                .load(loginUser.getPhotoUrl())
                .into(profileImage);
        profileName.setText(loginUser.getDisplayName());
        profileSchool.setText("Mission San Jose High");

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
                Intent newIntent = new Intent(MainActivity.this, AllClubsActivity.class);
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
