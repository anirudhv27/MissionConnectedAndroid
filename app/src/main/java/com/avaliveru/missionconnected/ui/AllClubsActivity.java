package com.avaliveru.missionconnected.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.avaliveru.missionconnected.R;
import com.avaliveru.missionconnected.dataModels.Club;
import com.avaliveru.missionconnected.ui.clubs.ClubsTabFragment;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AllClubsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;
    private String clubID;
    private FloatingActionButton addClubsButton;
    private static final int REQUEST_EXIT = 2 ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_clubs);

        //clubID = getIntent().getStringExtra("clubID");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView = findViewById(R.id.myClubsTabRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        addClubsButton = findViewById(R.id.addClubsButton);
        addClubsButton.setVisibility(View.GONE);

        fetchMyClubs();
    }

    private void fetchMyClubs() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance()
                .getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference myClubNamesRef = rootRef.child("users").child(currentUser.getUid()).child("clubs");

        final DatabaseReference clubDetailsRef= FirebaseDatabase.getInstance()
                .getReference()
                .child("schools").child("missionsanjosehigh").child("clubs");
        FirebaseRecyclerOptions<Club> options =
                new FirebaseRecyclerOptions.Builder<Club>()
                        .setQuery(clubDetailsRef, new SnapshotParser<Club>() {
                            @NonNull
                            @Override
                            public Club parseSnapshot(@NonNull DataSnapshot snapshot) {
                                Club club = new Club();
                                club.clubName = snapshot.child("club_name").getValue().toString();
                                club.clubDescription = snapshot.child("club_description").getValue().toString();
                                club.clubID = snapshot.getKey();
                                club.clubImageURL = snapshot.child("club_image_url").getValue().toString();
                                club.clubPreview = snapshot.child("club_preview").getValue().toString();
                                return club;
                            }
                        })
                        .build();

        adapter = new FirebaseRecyclerAdapter<Club, AllClubsActivity.ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final AllClubsActivity.ViewHolder holder, int position, @NonNull final Club model) {
                final String key = this.getRef(position).getKey();
                myClubNamesRef.child(model.clubID).addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull final DataSnapshot snapshot) {
                      if (snapshot.exists()) {
                           holder.clubMemberStatus.setText(snapshot.getValue().toString());
                       } else {
                           holder.clubMemberStatus.setText("Not A Member");
                       }
                       holder.clubName.setText(model.clubName);
                       holder.clubDesc.setText(model.clubPreview);
                       if(!model.clubImageURL.equals(""))
                            holder.setImage(model.clubImageURL);
                       else
                           holder.setImageResource(R.mipmap.ic_login_image);
                       holder.root.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View view) {
                               Intent intent = new Intent(AllClubsActivity.this, ClubsDetailsActivity.class);
                               intent.putExtra("clubName", model.clubID);
                               intent.putExtra("isMyClub", snapshot.exists());
                               AllClubsActivity.this.startActivityForResult(intent,REQUEST_EXIT );
                           }
                       });

                   }
                   @Override
                   public void onCancelled(@NonNull DatabaseError error) { }

               });
            }

            @NonNull
            @Override
            public AllClubsActivity.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_myclubs_tabitem, parent, false);
                return new AllClubsActivity.ViewHolder(view);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EXIT)
            AllClubsActivity.this.finish();
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
            Glide.with(AllClubsActivity.this).load(Uri.parse(imageURL)).into(image);
        }

        public void setImageResource(int ic_login_image) {
            image.setImageResource(ic_login_image);
        }
    }
}
