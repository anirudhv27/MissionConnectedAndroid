package com.avaliveru.missionconnected.ui.clubs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import com.avaliveru.missionconnected.MainActivity;
import com.avaliveru.missionconnected.R;
import com.avaliveru.missionconnected.dataModels.Club;
import com.avaliveru.missionconnected.dataModels.Event;
import com.avaliveru.missionconnected.ui.AllClubEventsActivity;
import com.avaliveru.missionconnected.ui.ClubsDetailsActivity;
import com.avaliveru.missionconnected.ui.EventsDetailsActivity;
import com.avaliveru.missionconnected.ui.home.AllFragment;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_clubs, container, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView = root.findViewById(R.id.myClubsTabRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        fetchMyClubs();

        return root;
    }



    private void fetchMyClubs() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance()
                .getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference myClubNamesRef = rootRef.child("users").child(currentUser.getUid()).child("clubs");

        final DatabaseReference clubDetailsRef= FirebaseDatabase.getInstance()
                .getReference()
                .child("schools").child("missionsanjosehigh").child("clubs");
        FirebaseRecyclerOptions<String> options =
                new FirebaseRecyclerOptions.Builder<String>()
                        .setQuery(myClubNamesRef, new SnapshotParser<String>() {
                            @NonNull
                            @Override
                            public String parseSnapshot(@NonNull DataSnapshot snapshot) {

                                if (snapshot.getValue().toString().equals("Officer") || snapshot.getValue().toString().equals("Member")) {
                                    return snapshot.getValue().toString();
                                } else {
                                    return "";
                                }
                            }
                        })
                        .build();

        adapter = new FirebaseRecyclerAdapter<String, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final String model) {
                String key = this.getRef(position).getKey();
                clubDetailsRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot snapshot) {
                        holder.setClubNameTitle(snapshot.child("club_name").getValue().toString());
                        holder.setClubDescTitle(snapshot.child("club_preview").getValue().toString());
                        holder.setClubMemberStatus(model);
                        holder.setImage(snapshot.child("club_image_url").getValue().toString());

                        holder.root.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(ClubsTabFragment.this.getActivity(), ClubsDetailsActivity.class);
                                intent.putExtra("clubName", snapshot.getKey());
                                intent.putExtra("isMyClub", true);
                                ClubsTabFragment.this.startActivity(intent);
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
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_myclubs_tabitem, parent, false);
                return new ViewHolder(view);
            }


        };
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        (((MainActivity) getActivity()).getmClubsTabAddClubMenuItem()).setVisible(true);
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        (((MainActivity) getActivity()).getmClubsTabAddClubMenuItem()).setVisible(false);
        adapter.stopListening();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout root;
        public ImageView image;
        public TextView clubName;
        public TextView clubDesc;
        public TextView clubMemberStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.clubTabItemImageView);
            clubName = itemView.findViewById(R.id.clubTabItemNameTextField);
            clubDesc = itemView.findViewById(R.id.clubDescTextField);
            clubMemberStatus = itemView.findViewById(R.id.clubMemberStatusField);
            //eventDate = itemView.findViewById(R.id.eventDateTextField);
            root = itemView.findViewById(R.id.club_list_root);
        }

        public void setClubNameTitle(String string) {
            clubName.setText(string);
        }
        public void setClubMemberStatus(String string) {
            clubMemberStatus.setText(string);
        }
        public void setClubDescTitle(String string) {
            clubDesc.setText(string);
        }



        public void setImage(String imageURL) {
            Glide.with(getContext()).load(Uri.parse(imageURL)).into(image);
        }
    }
}

