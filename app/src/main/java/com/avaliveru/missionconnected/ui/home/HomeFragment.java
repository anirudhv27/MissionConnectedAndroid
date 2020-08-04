package com.avaliveru.missionconnected.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import com.avaliveru.missionconnected.ui.ClubsDetailsActivity;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    private RecyclerView recyclerView;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private ArrayList<String> clubIDs;
    private ArrayList<Club> clubs;
    private RecyclerView.Adapter mAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView = root.findViewById(R.id.myClubsRecyclerView);
        fetchClubIDs();
        fetchClubList();

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        //fetchClubs();
        tabLayout = root.findViewById(R.id.homeTabLayout);
        viewPager = root.findViewById(R.id.myClubsViewPager);
        HomeViewPagerAdapter adapter = new HomeViewPagerAdapter(getActivity().getSupportFragmentManager(), getLifecycle());

        adapter.addFragment(new GoingFragment(), "Going");
        adapter.addFragment(new AllFragment(), "All");

        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        if (position == 0) tab.setText(R.string.going);
                        else if (position == 1) tab.setText(R.string.all);
                    }
                }).attach();

        return root;
    }

    private void fetchClubIDs() {
        clubIDs = new ArrayList<>();

        DatabaseReference rootRef = FirebaseDatabase.getInstance()
                .getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference myClubNamesRef = rootRef.child("users").child(currentUser.getUid()).child("clubs");
        myClubNamesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    if (childSnapshot.getValue().toString().equals("Member") || childSnapshot.getValue().toString().equals("Officer")) {
                        clubIDs.add(childSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void fetchClubList() {
        clubs = new ArrayList<>();

        DatabaseReference clubDetailsRef= FirebaseDatabase.getInstance()
                .getReference()
                .child("schools").child("missionsanjosehigh").child("clubs");
        clubDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    if (clubIDs.contains(childSnapshot.getKey())) {
                        Club club = new Club();
                        club.clubID = childSnapshot.getKey();
                        club.clubName = childSnapshot.child("club_name").getValue().toString();
                        club.clubImageURL = childSnapshot.child("club_image_url").getValue().toString();
                        club.clubPreview = childSnapshot.child("club_preview").getValue().toString();
                        club.clubDescription = childSnapshot.child("club_description").getValue().toString();
                        club.numberOfMembers = Integer.parseInt(childSnapshot.child("member_numbers").getValue().toString());

                        clubs.add(club);
                    }
                }

                mAdapter = new HomeClubAdapter(clubs);
                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private class HomeClubAdapter extends RecyclerView.Adapter<HomeClubAdapter.ViewHolder> {

        ArrayList<Club> clubs;

        public HomeClubAdapter(ArrayList<Club> clubs) {
            this.clubs = clubs;
        }

        @NonNull
        @Override
        public HomeClubAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_myclubslistitem, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HomeClubAdapter.ViewHolder holder, int position) {
            final Club currClub = clubs.get(position);
            holder.setImage(currClub.clubImageURL);
            holder.setTextTitle(currClub.clubName);
            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(HomeFragment.this.getContext(), ClubsDetailsActivity.class);
                    intent.putExtra("clubName", currClub.clubID);
                    intent.putExtra("isMyClub", true);
                    HomeFragment.this.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return clubs.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public RelativeLayout root;
            public ImageView image;
            public TextView clubName;
            public Club club;

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
}
