package com.avaliveru.missionconnected.ui.publish;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.provider.ContactsContract;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.avaliveru.missionconnected.MainActivity;
import com.avaliveru.missionconnected.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class UpdateClubsFragment extends Fragment {

    public AutoCompleteTextView clubName;
    public TextInputLayout clubPreview;
    public MultiAutoCompleteTextView pickOfficers;
    public TextInputLayout clubDescription;
    public ImageButton clubImage;
    public Button updateButton;

    private ScrollView scrollView;
    private Uri mImageUri;

    private static final int PICK_IMAGE_REQUEST = 2;

    private HashMap<String, String> allUsersDict;

    private ArrayList<String> userNames;
    private ArrayList<String> userIDs;
    private ArrayList<String> clubOfficerIDs;
    private ArrayList<String> clubOfficerNames;
    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_update_club, container, false);
        clubName = root.findViewById(R.id.clubNameInput);
        clubPreview = root.findViewById(R.id.clubPreviewInput);
        pickOfficers = root.findViewById(R.id.pickOfficersInput);
        clubDescription = root.findViewById(R.id.clubDescInput);
        clubImage = root.findViewById(R.id.clubImageChooser);
        updateButton = root.findViewById(R.id.updateButton);

        scrollView = root.findViewById(R.id.updateClubScrollView);

        fetchUsers();
        fetchClubOfficerIDs();
        fetchClubOfficerNames();

        ArrayAdapter<String> clubAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, clubOfficerNames);
        clubName.setAdapter(clubAdapter);

        ArrayAdapter<String> userAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, userNames);
        pickOfficers.setAdapter(userAdapter);
        pickOfficers.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        clubImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
        /*
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(clubName.getEditText().getText().toString().trim())) {
                    clubName.setError("Please select a club from the list.");
                } else if (TextUtils.isEmpty(clubPreview.getEditText().getText().toString().trim())) {
                    clubPreview.setError("Please write a short preview about your club.");
                } else if (TextUtils.isEmpty(pickOfficers.getText().toString().trim())) {
                    pickOfficers.setError("Please pick officers of your club.");
                } else if (TextUtils.isEmpty(clubDescription.getEditText().getText().toString().trim())) {
                    clubDescription.setError("Please write a description of your club, including meeting times, room numbers, and contact info");
                } else if (mImageUri.toString() == "") {

                } else {

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    String imageName = "event" + new Date().getTime();
                    final StorageReference imageRef = storageRef.child("eventimages").child(imageName);

                    if (mImageUri.toString().split("/")[2].equals("firebasestorage.googleapis.com")) {
                        edit(mImageUri.toString(), eventID);

                        eventClub.getEditText().setText("");
                        eventName.getEditText().setText("");
                        eventDate.getEditText().setText("");
                        eventPreview.getEditText().setText("");
                        eventDescription.getEditText().setText("");
                        eventImageButton.setImageBitmap(null);

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
                                        edit(downloadUrl, eventID);
                                        eventClub.getEditText().setText("");
                                        eventName.getEditText().setText("");
                                        eventDate.getEditText().setText("");
                                        eventPreview.getEditText().setText("");
                                        eventDescription.getEditText().setText("");
                                        eventImageButton.setImageBitmap(null);

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
                }
            }
        });
*/
        return root;
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
                    userIDs.add(snapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void fetchClubOfficerIDs() {
        clubOfficerIDs = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid).child("clubs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (child.getValue().toString().equals("Officer")) clubOfficerIDs.add(child.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void fetchClubOfficerNames() {
        clubOfficerNames = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("schools")
                .child("missionsanjosehigh").child("clubs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (clubOfficerIDs.contains(child.getKey())) {
                        clubOfficerNames.add(child.child("club_name").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
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
}
