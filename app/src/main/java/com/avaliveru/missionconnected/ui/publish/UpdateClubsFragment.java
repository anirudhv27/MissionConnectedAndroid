package com.avaliveru.missionconnected.ui.publish;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.app.Activity.RESULT_OK;

public class UpdateClubsFragment extends Fragment {
    public AutoCompleteTextView clubName;
    public TextInputLayout clubPreview;
    public MultiAutoCompleteTextView pickOfficers;
    public TextInputLayout clubDescription;
    public ImageButton clubImage;
    public Button updateButton;
    public String clubID;

    private ScrollView scrollView;
    public Uri mImageUri;

    private static final int PICK_IMAGE_REQUEST = 2;

    public ArrayList<String> userNames;
    public ArrayList<String> userIDs;

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

        final ArrayAdapter<String> userAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, userNames);
        pickOfficers.setAdapter(userAdapter);
        pickOfficers.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        clubImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

//        pickOfficers.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if (b) {
//                    //pickOfficers.setText("");
//                    pickOfficers.setAdapter(userAdapter);
//                }
//            }
//        });
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
        return root;
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

    @Override
    public void onPause() {
        super.onPause();
        clubName.setText("");
        clubPreview.getEditText().setText("");
        pickOfficers.setText("");
        clubDescription.getEditText().setText("");
        clubImage.setImageBitmap(null);
        clubID = "";

        scrollView.setFocusableInTouchMode(true);
        scrollView.fullScroll(View.FOCUS_UP);
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        if (bundle != null) {
            mImageUri = Uri.parse(bundle.getString("clubImageURL"));

            clubID = bundle.getString("clubID");
            clubName.setText(bundle.getString("clubName"));
            Glide.with(getContext()).load(mImageUri).into(clubImage);
            clubDescription.getEditText().setText(bundle.getString("clubDescription"));
            clubPreview.getEditText().setText(bundle.getString("clubPreview"));
        }

        final ArrayAdapter<String> userAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, userNames);
        pickOfficers.setAdapter(userAdapter);
        pickOfficers.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        clubImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
    }
    private void alert(String s) {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(getContext());
        alertDialog2.setTitle("Alert");
        alertDialog2.setMessage(s);
        alertDialog2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                PublishFragment pubFrag = (PublishFragment) getParentFragment();
                pubFrag.viewPager.setCurrentItem(0);
            }
        });
        alertDialog2.show();
    }
}
