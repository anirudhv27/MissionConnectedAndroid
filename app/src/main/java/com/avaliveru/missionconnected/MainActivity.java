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
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        toolBar = (Toolbar) findViewById ( R.id.toolbar );
        setSupportActionBar( toolBar );
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled ( true );
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        drawerLayout = findViewById(R.id.navigation_layout);
        navigationView = findViewById ( R.id.navigation_view_drawer );
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_drawer_support, R.id.navigation_drawer_signout)
                .setDrawerLayout( drawerLayout)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController( navigationView, navController);
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        bottomNavigation = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(bottomNavigation, navController);

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