package com.avaliveru.missionconnected.ui.publish;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.avaliveru.missionconnected.R;
import com.avaliveru.missionconnected.dataModels.Club;
import com.avaliveru.missionconnected.ui.ClubsDetailsActivity;
import com.avaliveru.missionconnected.ui.home.AllFragment;
import com.avaliveru.missionconnected.ui.home.GoingFragment;
import com.avaliveru.missionconnected.ui.home.HomeViewPagerAdapter;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PublishFragment extends Fragment {

    public FirebaseRecyclerAdapter adapter;
    public HomeViewPagerAdapter pagerAdapter;
    public ViewPager2 viewPager;
    public TabLayout tabLayout;
    public RecyclerView recyclerView;

    private void fetchClubs() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance()
                .getReference();
        DatabaseReference myClubNamesRef = rootRef.child("users").child("t8AKiEV08yVulfouZM9xAA1gCCC3").child("clubs");
        final DatabaseReference clubDetailsRef= FirebaseDatabase.getInstance()
                .getReference()
                .child("schools").child("missionsanjosehigh").child("clubs");

        FirebaseRecyclerOptions<Boolean> options =
                new FirebaseRecyclerOptions.Builder<Boolean>()
                        .setQuery(myClubNamesRef, new SnapshotParser<Boolean>() {
                            @NonNull
                            @Override
                            public Boolean parseSnapshot(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue().toString().equals("Officer")) {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        })
                        .build();

        adapter = new FirebaseRecyclerAdapter<Boolean, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final Boolean model) {
                String key = this.getRef(position).getKey();
                clubDetailsRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot snapshot) {
                        holder.setTextTitle(snapshot.child("club_name").getValue().toString());
                        holder.setImage(snapshot.child("club_image_url").getValue().toString());

                        final Club club = new Club();
                        club.clubName = snapshot.child("club_name").getValue().toString();
                        club.clubDescription = snapshot.child("club_description").getValue().toString();
                        club.clubID = snapshot.getKey();
                        club.clubPreview = snapshot.child("club_preview").getValue().toString();
                        club.clubImageURL = snapshot.child("club_image_url").getValue().toString();

                        holder.club = club;

                        holder.root.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //viewPager.setCurrentItem(1);

                                tabLayout.getTabAt(1).select();

                                AddEventFragment addEventFragment = (AddEventFragment) pagerAdapter.createFragment(1);

                                //make a bundle
                                Bundle b = new Bundle();

                                b.putString("clubName", snapshot.child("club_name").getValue().toString());
                                b.putString("eventClubID", snapshot.getKey());
                                b.putBoolean("isFromEdit", false);

                                addEventFragment.setArguments(b);

                                //getActivity().getSupportFragmentManager().beginTransaction().detach(addEventFragment).attach(addEventFragment).commit();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_myclubslistitem, parent, false);
                return new ViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_publish, container, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView = root.findViewById(R.id.myOfficerClubsRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        fetchClubs();

        tabLayout = root.findViewById(R.id.publishTabLayout);
        viewPager = root.findViewById(R.id.publishTabViewPager);
        pagerAdapter = new HomeViewPagerAdapter(getChildFragmentManager(), getLifecycle());

        pagerAdapter.addFragment(new MyClubEventsFragment(), "My Club Events");
        pagerAdapter.addFragment(new AddEventFragment(), "Add Event");

        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        if (position == 0) tab.setText("My Club Events");
                        else if (position == 1) tab.setText("Add Event");
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