package com.avaliveru.missionconnected;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {



    private MenuItem mClubsTabAddClubMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_clubs, R.id.navigation_publish)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    /*
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
            Toast.makeText(this, " TODO: Add new logic to add clubs ", Toast.LENGTH_LONG).show();//TODO
        }
        return super.onOptionsItemSelected(item);
    }
    public MenuItem getmClubsTabAddClubMenuItem() {
        return mClubsTabAddClubMenuItem;
    }
    /*
    END Create a App Menu button to add new clubs
    */

}