package com.avaliveru.missionconnected.ui.publish;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.avaliveru.missionconnected.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class UpdateClubsActivity extends AppCompatActivity {
    public AutoCompleteTextView clubName;
    public TextInputLayout clubPreview;
    public MultiAutoCompleteTextView pickOfficers;
    public TextInputLayout clubDescription;
    public ImageButton clubImage;
    public Button updateButton;
    public String clubID;

    private ScrollView scrollView;
    private Uri mImageUri;

    private static final int PICK_IMAGE_REQUEST = 2;

    public ArrayList<String> userNames;
    public ArrayList<String> userIDs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_update_club);
        clubName = findViewById(R.id.clubNameInput);
        clubPreview = findViewById(R.id.clubPreviewInput);
        pickOfficers = findViewById(R.id.pickOfficersInput);
        clubDescription = findViewById(R.id.clubDescInput);
        clubImage = findViewById(R.id.clubImageChooser);
        updateButton = findViewById(R.id.updateButton);
        scrollView = findViewById(R.id.updateClubScrollView);

        fetchUsers();

        Bundle bundle = getIntent().getBundleExtra("bundle");
        if (bundle != null) {
            mImageUri = Uri.parse(bundle.getString("clubImageURL"));

            clubID = bundle.getString("clubID");
            clubName.setText(bundle.getString("clubName"));
            Glide.with(this).load(mImageUri).into(clubImage);
            clubDescription.getEditText().setText(bundle.getString("clubDescription"));
            clubPreview.getEditText().setText(bundle.getString("clubPreview"));
        }

        final ArrayAdapter<String> userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userNames);
        pickOfficers.setAdapter(userAdapter);
        pickOfficers.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        clubImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        pickOfficers.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        clubImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(clubName.getText().toString().trim())) {
                    alert(getString(R.string.empty_club_alert));
                } else if (TextUtils.isEmpty(pickOfficers.getText().toString().trim())) {
                    alert(getString(R.string.empty_officer_alert));
                } else if (TextUtils.isEmpty(clubPreview.getEditText().getText().toString().trim())) {
                    alert(getString(R.string.empty_club_preview_alert));
                } else if (TextUtils.isEmpty(clubDescription.getEditText().getText().toString().trim())) {
                    alert(getString(R.string.empty_club_desc_alert));
                } else if (mImageUri == null) {
                    alert(getString(R.string.empty_club_image_alert));
                } else {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    String imageName = "event" + new Date().getTime();
                    final StorageReference imageRef = storageRef.child("eventimages").child(imageName);
                    if (mImageUri.toString().split("/")[2].equals("firebasestorage.googleapis.com")) {
                        update(mImageUri.toString(), clubID);
                        clubID = "";
                        scrollView.setFocusableInTouchMode(true);
                        scrollView.fullScroll(View.FOCUS_UP);
                    } else {
                        imageRef.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String downloadUrl = uri.toString();
                                        update(downloadUrl, clubID);
                                        clubID = "";
                                        scrollView.setFocusableInTouchMode(true);
                                        scrollView.fullScroll(View.FOCUS_UP);
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println("FAILED");
                            }
                        });
                    }
                    alert(getString(R.string.club_updated_succes_alert));
                }
            }
        });
    }
    private void update(final String downloadURL, final String clubID) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference eventsRef = ref.child("schools").child("missionsanjosehigh").child("events");
        final DatabaseReference clubsRef = ref.child("schools").child("missionsanjosehigh").child("clubs").child(clubID);
        final DatabaseReference userRef = ref.child("users");

        clubsRef.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                HashMap<String, Boolean> eventsDict = (HashMap<String, Boolean>) snapshot.getValue();
                final Set<String> eventNames;
                if (eventsDict != null) {
                    eventNames = eventsDict.keySet();
                } else {
                    eventNames = new HashSet<>();
                }

                clubsRef.child("club_preview").setValue(clubPreview.getEditText().getText().toString().trim());
                clubsRef.child("club_description").setValue(clubDescription.getEditText().getText().toString().trim());
                clubsRef.child("club_image_url").setValue(downloadURL);
                final String[] officers = pickOfficers.getText().toString().split("\\s*,\\s*");
                final ArrayList<String> officerIDs = new ArrayList<>();

                for (String officer : officers) {
                    officerIDs.add(userIDs.get(userNames.indexOf(officer)));
                }

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            if (officerIDs.contains(child.getKey())) {
                                userRef.child(child.getKey()).child("clubs").child(clubID).setValue("Officer");
                            } else {
                                if (child.child("clubs").hasChild(clubID)) {
                                    userRef.child(child.getKey()).child("clubs").child(clubID).setValue("Member");
                                }
                            }

                            for (String eventID : eventNames) {
                                if (child.child("events").hasChild(eventID)) {
                                    if (officerIDs.contains(child.getKey())) {
                                        userRef.child(child.getKey()).child("events").child(eventID).child("member_status").setValue("Officer");
                                    } else {
                                        userRef.child(child.getKey()).child("events").child(eventID).child("member_status").setValue("Member");
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });

                clubName.setText("");
                pickOfficers.setText("");
                clubPreview.getEditText().setText("");
                clubDescription.getEditText().setText("");
                clubImage.setImageBitmap(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void fetchUsers() {
        userIDs = new ArrayList<>();
        userNames = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    userNames.add(child.child("fullname").getValue().toString() +
                            " (" + child.child("email").getValue().toString() + ")");
                    userIDs.add(child.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();

            Picasso.get().load(mImageUri).into(clubImage);
        }
    }
    private void alert(String s) {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(this);
        alertDialog2.setTitle("Alert");
        alertDialog2.setMessage(s);
        alertDialog2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        alertDialog2.show();
    }
}
