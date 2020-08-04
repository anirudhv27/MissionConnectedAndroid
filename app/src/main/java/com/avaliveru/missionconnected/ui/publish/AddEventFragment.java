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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ScrollView;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class AddEventFragment extends Fragment {

    public TextInputLayout eventClub;
    public TextInputLayout eventName;
    public TextInputLayout eventDate;
    public TextInputLayout eventPreview;
    public TextInputLayout eventDescription;
    public ImageButton eventImageButton;
    public Button publishButton;

    private DatePickerDialog.OnDateSetListener dateSetListener;
    private ScrollView scrollView;

    private static final int PICK_IMAGE_REQUEST = 1;

    private Uri mImageUri;
    private String currClubName;
    private String currClubID;
    private String eventID;

    private boolean isFromEdit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_event, container, false);
        eventClub = root.findViewById(R.id.eventClubInput);
        eventName = root.findViewById(R.id.eventNameInput);
        eventDate = root.findViewById(R.id.eventDateInput);
        eventPreview = root.findViewById(R.id.eventPreviewInput);
        eventDescription = root.findViewById(R.id.eventDescInput);
        eventImageButton = root.findViewById(R.id.eventImageChooser);
        publishButton = root.findViewById(R.id.publishButton);
        scrollView = root.findViewById(R.id.addEventScrollView);


        eventDate.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getActivity(), R.style.Theme_AppCompat_DayNight_Dialog,
                        dateSetListener, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));
                dialog.show();

            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month += 1;
                String date = month + "/" + day + "/" + year;
                eventDate.getEditText().setText(date);
            }
        };

        eventImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(eventClub.getEditText().getText().toString().trim())) {
                    eventClub.setError("Please select a club from above.");
                } else if (TextUtils.isEmpty(eventName.getEditText().getText().toString().trim())) {
                    eventName.setError("Please name your event.");
                } else if (TextUtils.isEmpty(eventDate.getEditText().getText().toString().trim())) {
                    eventDate.setError("Please set an event date.");
                } else if (TextUtils.isEmpty(eventPreview.getEditText().getText().toString().trim())) {
                    eventPreview.setError("Please write a short Preview of your event");
                } else if (TextUtils.isEmpty(eventDescription.getEditText().getText().toString().trim())) {
                    eventDescription.setError("Please write a description of all important event details.");
                } else if (mImageUri.toString() == "") {

                } else {

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    String imageName = "event" + new Date().getTime();
                    final StorageReference imageRef = storageRef.child("eventimages").child(imageName);

                    if (isFromEdit) {

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
                    } else {
                        // Create a storage reference from our app

                        imageRef.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String downloadUrl = uri.toString();
                                        create(downloadUrl);
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
                }
            }
        });

        return root;
    }

    private void edit(String downloadUrl, String key) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference eventsRef = ref.child("schools").child("missionsanjosehigh").child("events");

        eventsRef.child(key).child("event_image_url").setValue(downloadUrl);

        String[] date = eventDate.getEditText().getText().toString().split("/");
        eventsRef.child(key).child("event_date").setValue(date[0] + "-" + date[1] + "-" + date[2]);

        eventsRef.child(key).child("event_name").setValue(eventName.getEditText().getText().toString());
        eventsRef.child(key).child("event_club").setValue(currClubID);
        eventsRef.child(key).child("event_description").setValue(eventDescription.getEditText().getText().toString());
        eventsRef.child(key).child("event_preview").setValue(eventPreview.getEditText().getText().toString());
        eventsRef.child(key).child("member_numbers").setValue(0);
    }

    private void create(String downloadUrl) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference eventsRef = ref.child("schools").child("missionsanjosehigh").child("events");
        DatabaseReference clubsRef = ref.child("schools").child("missionsanjosehigh").child("clubs");
        final DatabaseReference userRef = ref.child("users");

        final String key = eventsRef.push().getKey();
        eventsRef.child(key).child("event_image_url").setValue(downloadUrl);

        String[] date = eventDate.getEditText().getText().toString().split("/");
        eventsRef.child(key).child("event_date").setValue(date[0] + "-" + date[1] + "-" + date[2]);

        eventsRef.child(key).child("event_name").setValue(eventName.getEditText().getText().toString());
        eventsRef.child(key).child("event_club").setValue(currClubID);
        eventsRef.child(key).child("event_description").setValue(eventDescription.getEditText().getText().toString());
        eventsRef.child(key).child("event_preview").setValue(eventPreview.getEditText().getText().toString());
        eventsRef.child(key).child("member_numbers").setValue(0);

        clubsRef.child(currClubID).child("events").child(key).setValue(true);

        userRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.child("clubs").child(currClubID).exists()) {
                    if (snapshot.child("clubs").child(currClubID).getValue().equals("Officer")) {
                        userRef.child(snapshot.getKey()).child("events").child(key)
                                .child("member_status").setValue("Officer");
                        userRef.child(snapshot.getKey()).child("events").child(key).child("isGoing").setValue(true);
                    } else if (snapshot.child("clubs").child(currClubID).getValue().equals("Member")){
                        userRef.child(snapshot.getKey()).child("events").child(key)
                                .child("member_status").setValue("Member");
                        userRef.child(snapshot.getKey()).child("events").child(key).child("isGoing").setValue(false);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        if (bundle != null) {
            isFromEdit = bundle.getBoolean("isFromEdit");
            currClubID = bundle.getString("eventClubID");
            currClubName = bundle.getString("clubName");
            eventClub.getEditText().setText(currClubName);

            if (isFromEdit) {
                eventID = bundle.getString("eventID");
                mImageUri = Uri.parse(bundle.getString("eventImageURL"));

                eventName.getEditText().setText(bundle.getString("eventName"));
                eventDate.getEditText().setText(bundle.getString("eventDate"));
                eventPreview.getEditText().setText(bundle.getString("eventPreview"));
                eventDescription.getEditText().setText(bundle.getString("eventDescription"));

                Glide.with(getActivity()).load(mImageUri).into(eventImageButton);

            }
        } else {
            isFromEdit = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        eventClub.getEditText().setText("");
        eventName.getEditText().setText("");
        eventDate.getEditText().setText("");
        eventPreview.getEditText().setText("");
        eventDescription.getEditText().setText("");
        eventImageButton.setImageBitmap(null);

        scrollView.setFocusableInTouchMode(true);
        scrollView.fullScroll(View.FOCUS_UP);
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

            Picasso.get().load(mImageUri).into(eventImageButton);
        }
    }
}
