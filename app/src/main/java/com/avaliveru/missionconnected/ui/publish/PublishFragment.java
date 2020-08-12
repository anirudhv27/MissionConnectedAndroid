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

import com.avaliveru.missionconnected.GoogleSignInActivity;
import com.avaliveru.missionconnected.MainActivity;
import com.avaliveru.missionconnected.R;
import com.avaliveru.missionconnected.dataModels.Club;
import com.avaliveru.missionconnected.ui.AllClubsActivity;
import com.avaliveru.missionconnected.ui.ClubsDetailsActivity;
import com.avaliveru.missionconnected.ui.home.AllFragment;
import com.avaliveru.missionconnected.ui.home.GoingFragment;
import com.avaliveru.missionconnected.ui.home.HomeFragment;
import com.avaliveru.missionconnected.ui.home.HomeViewPagerAdapter;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class PublishFragment extends Fragment {

    public HomeViewPagerAdapter pagerAdapter;
    public ViewPager2 viewPager;
    public TabLayout tabLayout;
    public RecyclerView recyclerView;
    private FloatingActionButton addEventsButton;

    private ArrayList<String> clubIDs;
    private ArrayList<Club> clubs;
    private RecyclerView.Adapter mAdapter;

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
                    if (childSnapshot.getValue().toString().equals("Officer")) {
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

                mAdapter = new PublishClubAdapter(clubs);
                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_publish, container, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView = root.findViewById(R.id.myOfficerClubsRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        fetchClubIDs();
        fetchClubList();
        //fetchClubs();
        viewPager = root.findViewById(R.id.publishTabViewPager);
        pagerAdapter = new HomeViewPagerAdapter(getChildFragmentManager(), getLifecycle());


        pagerAdapter.addFragment(new MyClubEventsFragment(), "My Club Events");
        //pagerAdapter.addFragment(new AddEventFragment(), "Add Event");
        //pagerAdapter.addFragment(new UpdateClubsFragment(), "Update Clubs");

        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        viewPager.setAdapter(pagerAdapter);

//        new TabLayoutMediator(tabLayout, viewPager,
//                new TabLayoutMediator.TabConfigurationStrategy() {
//                    @Override
//                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
//                        if (position == 0) tab.setText("My Club Events");
//                        else if (position == 1) tab.setText("Add Event");
//                        //else if (position == 2) tab.setText("Update Clubs");
//                    }
//                }).attach();
        addEventsButton = root.findViewById(R.id.addEventsButton);
        addEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(getContext(), AddEventActivity.class);
                newIntent.putExtra("isFromEdit", false);
                startActivity(newIntent);
            }
        });

        return root;
    }

    private class PublishClubAdapter extends RecyclerView.Adapter<PublishClubAdapter.ViewHolder> {
        ArrayList<Club> clubs;

        public PublishClubAdapter(ArrayList<Club> clubs) {
            this.clubs = clubs;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_myclubslistitem, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final Club currClub = clubs.get(position);
            holder.setImage(currClub.clubImageURL);
            holder.setTextTitle(currClub.clubName);
            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //viewPager.setCurrentItem(1);

                    Intent newIntent = new Intent(getContext(), UpdateClubsActivity.class);
                    Bundle b = new Bundle();

                    b.putString("clubName", currClub.clubName);
                    b.putString("clubID", currClub.clubID);
                    b.putString("clubPreview", currClub.clubPreview);
                    b.putString("clubDescription", currClub.clubDescription);
                    b.putString("clubImageURL", currClub.clubImageURL);

                    newIntent.putExtra("bundle", b);
                    startActivity(newIntent);

                    /*tabLayout.getTabAt(2).select();

                    final UpdateClubsFragment updateClubsFragment = (UpdateClubsFragment) pagerAdapter.createFragment(2);
                    if (updateClubsFragment.clubName != null) {
                        updateClubsFragment.clubName.setText(currClub.clubName);
                    }
                    if (updateClubsFragment.clubImage != null) {
                        Glide.with(getContext()).load(Uri.parse(currClub.clubImageURL)).into(updateClubsFragment.clubImage);
                    }
                    if (updateClubsFragment.clubDescription != null) {
                        updateClubsFragment.clubDescription.getEditText().setText(currClub.clubDescription);
                    }
                    if (updateClubsFragment.clubPreview != null) {
                        updateClubsFragment.clubPreview.getEditText().setText(currClub.clubPreview);
                    }
                        final ArrayList<String> clubOfficerNames = new ArrayList<>();
                        final ArrayList<String> clubOfficerIDs = new ArrayList<>();
                        DatabaseReference usersRef= FirebaseDatabase.getInstance()
                                .getReference().child("users");
                        usersRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                    if (childSnapshot.child("clubs").child(currClub.clubID).exists()) {
                                        if (childSnapshot.child("clubs").child(currClub.clubID)
                                                .getValue().toString().equals("Officer")) {
                                            clubOfficerIDs.add(childSnapshot.getKey());
                                            clubOfficerNames.add(childSnapshot.child("fullname").getValue().toString() +
                                                    " (" + childSnapshot.child("email").getValue().toString() + ")");
                                        }
                                    }
                                }

                                //updateClubsFragment.userIDs = clubOfficerIDs;
                                //updateClubsFragment.userNames = clubOfficerNames;

                                StringBuilder csvBuilder = new StringBuilder();
                                for (String name : clubOfficerNames) {
                                    csvBuilder.append(name);
                                    csvBuilder.append(", ");
                                }

                                String csv = csvBuilder.toString();

                                updateClubsFragment.pickOfficers.setText(csv);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });


                    updateClubsFragment.clubID = currClub.clubID;

                     */
                    //Glide.with(updateClubsFragment.getContext()).load(currClub.clubImageURL).into(updateClubsFragment.clubImage);
                    //make a bundle
//                    Bundle b = new Bundle();
//
//                    b.putString("clubName", currClub.clubName);
//                    b.putString("clubID", currClub.clubID);
//                    b.putString("clubPreview", currClub.clubPreview);
//                    b.putString("clubDescription", currClub.clubDescription);
//                    b.putString("clubImageURL", currClub.clubImageURL);

//                    updateClubsFragment.setArguments(b);
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