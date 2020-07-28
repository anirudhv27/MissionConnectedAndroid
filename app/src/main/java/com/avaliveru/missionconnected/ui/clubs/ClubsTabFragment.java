package com.avaliveru.missionconnected.ui.clubs;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.avaliveru.missionconnected.R;
import com.avaliveru.missionconnected.dataModels.Club;
import com.avaliveru.missionconnected.dataModels.Event;
import com.avaliveru.missionconnected.ui.home.AllFragment;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClubsTabFragment extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;

    private Set<String> myClubNames = new HashSet<>();

    //private ClubsTabViewModel clubsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_clubs, container, false);
        /*
        clubsViewModel =
                ViewModelProviders.of(this).get(ClubsTabViewModel.class);

        final TextView textView = root.findViewById(R.id.text_dashboard);
        clubsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        */

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        recyclerView = root.findViewById(R.id.allRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        fetchMyClubs();

        return root;
    }



    private void fetchMyClubs() {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance()
                .getReference();
        //TODO: Get user id key from Firebase
        final DatabaseReference myEventNamesRef = rootRef.child("users").child("t8AKiEV08yVulfouZM9xAA1gCCC3").child("clubs");
        myEventNamesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> map = (HashMap<String, Object>) snapshot.getValue();
                myClubNames = map.keySet();
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
                                club.numberOfMembers = snapshot.child("club_name").getValue().toString();
                                return club;
                            }
                        })
                        .build();

        adapter = new FirebaseRecyclerAdapter<Club, ClubsTabFragment.ViewHolder>(options) {

            @Override
            public ClubsTabFragment.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_myclubs_tabitem, parent, false);
                return new ClubsTabFragment.ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ClubsTabFragment.ViewHolder holder, final int position, @NonNull Club model) {
                 /*
                 rootRef.child("clubs").child(model.eventClub).addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot snapshot) {
                         holder.setEventClubTitle(snapshot.child("club_name").getValue().toString());
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError error) {
                         holder.setEventClubTitle("No Club Available :(");
                     }
                 });
                 */
                 //holder.setEventDateTitle(model.eventDate);
                 holder.setClubNameTitle(model.clubName);
                 holder.setImage(model.clubImageURL);
                holder.setClubDescTitle(model.clubDescription);

                 holder.root.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         Toast.makeText(getContext(), String.valueOf(position), Toast.LENGTH_SHORT);
                     }
                 });

            }

        };
        recyclerView.setAdapter(adapter);
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
        public TextView clubDesc;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.clubImageView);
            clubName = itemView.findViewById(R.id.clubNameTextField);
            clubDesc = itemView.findViewById(R.id.clubDescTextField);
            //eventDate = itemView.findViewById(R.id.eventDateTextField);
            root = itemView.findViewById(R.id.event_list_root);
        }

        public void setClubNameTitle(String string) {
            clubName.setText(string);
        }
        public void setClubDescTitle(String string) {
            clubDesc.setText(string);
        }



        public void setImage(String imageURL) {
            Glide.with(getContext()).load(Uri.parse(imageURL)).into(image);
        }
    }
}

