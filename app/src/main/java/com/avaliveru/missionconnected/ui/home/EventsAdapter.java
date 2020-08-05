package com.avaliveru.missionconnected.ui.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.avaliveru.missionconnected.R;
import com.avaliveru.missionconnected.dataModels.Event;
import com.avaliveru.missionconnected.ui.EventsDetailsActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Event> events;
    public EventsAdapter(Context context, ArrayList<Event> events) {
        this.context = context;
        this.events = events;
        Collections.sort(events);
    }

    @NonNull
    @Override
    public EventsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_eventslistitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final EventsAdapter.ViewHolder holder, int position) {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance()
                .getReference();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference myEventNamesRef = rootRef.child("users").child(currentUser.getUid()).child("events");
        final Event currEvent = events.get(position);

        rootRef.child("clubs").child(currEvent.eventClub).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.setEventClubTitle(snapshot.child("club_name").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.setEventClubTitle("No Club Available :(");
            }
        });

        holder.eventID = currEvent.eventID;
        holder.setEventNameTitle(currEvent.eventName);
        holder.setEventDateTitle(currEvent.eventDate);
        holder.setImage(currEvent.eventImageURL);
        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(context, EventsDetailsActivity.class);
                String eventID = currEvent.eventID;
                intent.putExtra("eventName", eventID);
                myEventNamesRef.child(eventID).child("isGoing").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isGoing = (boolean) snapshot.getValue();
                        intent.putExtra("isGoing", isGoing);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout root;
        public ImageView image;
        public TextView eventName;
        public TextView eventClub;
        public TextView eventDate;
        public String eventID;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.eventImageView);
            eventName = itemView.findViewById(R.id.eventNameTextField);
            eventClub = itemView.findViewById(R.id.eventClubTextField);
            eventDate = itemView.findViewById(R.id.eventDateTextField);
            root = itemView.findViewById(R.id.event_list_root);
        }

        public void setEventNameTitle(String string) {
            eventName.setText(string);
        }
        public void setEventClubTitle(String string) {
            eventClub.setText(string);
        }

        public void setEventDateTitle(Date date) {
            SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
            eventDate.setText(df.format(date));
        }

        public void setImage(String imageURL) {
            Glide.with(context).load(Uri.parse(imageURL)).into(image);
        }
    }
}
