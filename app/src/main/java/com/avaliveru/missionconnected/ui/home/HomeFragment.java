package com.avaliveru.missionconnected.ui.home;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.avaliveru.missionconnected.R;
import com.avaliveru.missionconnected.dataModels.Club;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HomeFragment extends Fragment {

    private static final String TAG = "MainActivity";

    //vars
    private Set<String> clubNames = new HashSet<>();

    private HomeViewModel homeViewModel;
    private FirebaseRecyclerAdapter adapter;
    private RecyclerView recyclerView;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private void fetchClubs() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance()
                .getReference();
        DatabaseReference myClubNamesRef = rootRef.child("users").child("t8AKiEV08yVulfouZM9xAA1gCCC3").child("clubs");
        myClubNamesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> map = (HashMap<String, Object>) snapshot.getValue();
                clubNames = map.keySet();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Error: " + error);
            }
        });

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("schools").child("missionsanjosehigh").child("clubs");

        FirebaseRecyclerOptions<Club> options =
                new FirebaseRecyclerOptions.Builder<Club>()
                        .setQuery(query, new SnapshotParser<Club>() {
                            @NonNull
                            @Override
                            public Club parseSnapshot(@NonNull DataSnapshot snapshot) {

                                Club club = new Club();
                                club.clubID = snapshot.getKey().toString();
                                club.clubDescription = snapshot.child("club_description").getValue().toString();
                                club.clubPreview = snapshot.child("club_preview").getValue().toString();
                                club.clubImageURL = snapshot.child("club_image_url").getValue().toString();
                                club.clubName = snapshot.child("club_name").getValue().toString();
                                club.numberOfMembers = (long) snapshot.child("member_numbers").getValue();
                                return club;
                            }
                        })
                        .build();

        adapter = new FirebaseRecyclerAdapter<Club, ViewHolder>(options) {
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_myclubslistitem, parent, false);
                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, final int position, @NonNull Club model) {
                if (clubNames.contains(model.clubID)) {
                    holder.setTextTitle(model.clubName);
                    holder.setImage(model.clubImageURL);

                    holder.root.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(getContext(), String.valueOf(position), Toast.LENGTH_SHORT);
                        }
                    });
                }
            }

        };
        recyclerView.setAdapter(adapter);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView = root.findViewById(R.id.myClubsRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        fetchClubs();

        tabLayout = (TabLayout) root.findViewById(R.id.homeTabLayout);
        viewPager = (ViewPager2) root.findViewById(R.id.myClubsViewPager);
        HomeViewPagerAdapter adapter = new HomeViewPagerAdapter(getActivity().getSupportFragmentManager(), getLifecycle());

        adapter.addFragment(new GoingFragment(), "Going");
        adapter.addFragment(new AllFragment(), "All");

        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        if (position == 0) tab.setText(R.string.going);
                        else if (position == 1) {
                            tab.setText(R.string.all);
                        }
                    }
                }).attach();

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout root;
        public ImageView image;
        public TextView clubName;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.clubimage);
            clubName = itemView.findViewById(R.id.clubname);
            root = itemView.findViewById(R.id.my_clubs_list_root);
        }

        public void setTextTitle(String string) {
            clubName.setText(string);
        }

        public void setImage(String imageURL) {
           Glide.with(getContext()).load(Uri.parse(imageURL)).into(image);
        }
    }
}
