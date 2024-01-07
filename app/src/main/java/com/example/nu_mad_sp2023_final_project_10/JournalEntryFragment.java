package com.example.nu_mad_sp2023_final_project_10;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JournalEntryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JournalEntryFragment extends Fragment {

    private static final String ARG_JOURNAL_DATE = "journalDate";

    private TextView textDate;
    private EditText editTextJournalBody;
    private EditText editTextLocation;
    private TextView dropdownTags;
    private EditText editTextNewTags;
    private Button buttonDoneJournal;
    private ImageButton imgButtonLocation;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private ChooseMoodAdapter chooseMoodAdapter;

    private ArrayList<Mood> moods = new ArrayList<>();
    private ArrayList<DocumentReference> moodRefs = new ArrayList<>();

    private ArrayList<String> tags = new ArrayList<>();
    private ArrayList<String> selectedTagsList = new ArrayList<>();
    private LocalDate date;
    private DocumentReference currSelectedMood;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String email;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> locationPermissionRequest;

    private IFromJournalEntry sendData;

    public JournalEntryFragment() {
        // Required empty public constructor
    }


    public static JournalEntryFragment newInstance(LocalDate date) {
        JournalEntryFragment fragment = new JournalEntryFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_JOURNAL_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            date = (LocalDate) getArguments().getSerializable(ARG_JOURNAL_DATE);
        }
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        email = mUser.getEmail();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        resetMoodsArray();
        getAllMoodOptions(true);
        getAllTags();

        locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                                locationPressed();
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                                locationPressed();
                            } else {
                                // No location access granted.
                            }
                        }
                );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_journal_entry, container, false);
        getActivity().setTitle("Journal Entry");

        textDate = view.findViewById(R.id.textDate);
        String stringDate = date.getMonth() + " " + date.getDayOfMonth() + ", " + date.getYear();
        textDate.setText(stringDate);

        buttonDoneJournal = view.findViewById(R.id.buttonDoneJournal);
        editTextJournalBody = view.findViewById(R.id.editTextJournalBody);
        editTextLocation = view.findViewById(R.id.editTextLocation);
        dropdownTags = view.findViewById(R.id.dropdownTags);
        editTextNewTags = view.findViewById(R.id.editTextNewTags);
        imgButtonLocation = view.findViewById(R.id.imageButtonLocation);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerViewLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        chooseMoodAdapter = new ChooseMoodAdapter(moods, getContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setAdapter(chooseMoodAdapter);

        imgButtonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationPressed();
            }
        });

        buttonDoneJournal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String entryBody = editTextJournalBody.getText().toString();
                String location = editTextLocation.getText().toString().trim();
                String[] newTagArray = editTextNewTags.getText().toString().trim().split(",");
                int currSelectedMood = chooseMoodAdapter.getCurrSelected();

                if (currSelectedMood > 0) {
                    DocumentReference moodRef = moodRefs.get(currSelectedMood - 1);
                    createJournalEntry(entryBody, moodRef, location, newTagArray);
                } else {
                    Toast.makeText(getContext(), "Please select your mood", Toast.LENGTH_LONG).show();
                }
            }
        });

        db.collection("users")
                .document(email)
                .collection("moods")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            getAllMoodOptions(false);
                        }
                    }
                });

        db.collection("users")
                .document(email)
                .collection("tags")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            getAllTags();
                        }
                    }
                });

        db.collection("moods")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            getAllMoodOptions(false);
                        }
                    }
                });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IFromJournalEntry){
            sendData = (IFromJournalEntry) context;
        } else {
            throw new RuntimeException(context + "must implement IFromJournalEntry");
        }
    }


    private void getAllMoodOptions(boolean firstLoad) {
        ArrayList<Mood> allMoods = new ArrayList<>();
        ArrayList<DocumentReference> allMoodRefs = new ArrayList<>();
        db.collection("users")
                .document(email)
                .collection("moods")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot docSnap : task.getResult()) {
                                DocumentReference moodRef = docSnap.getReference();
                                Mood userMood = docSnap.toObject(Mood.class);
                                allMoodRefs.add(moodRef);
                                allMoods.add(userMood);
                            }
                            db.collection("moods")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot docSnap : task.getResult()) {
                                                    DocumentReference moodRef = docSnap.getReference();
                                                    Mood userMood = docSnap.toObject(Mood.class);
                                                    allMoodRefs.add(moodRef);
                                                    allMoods.add(userMood);
                                                }
                                                resetMoodsArray();
                                                moods.addAll(allMoods);
                                                moodRefs.addAll(allMoodRefs);
                                                chooseMoodAdapter.setMoods(moods);
                                                chooseMoodAdapter.notifyDataSetChanged();
                                                if (firstLoad) {
                                                    loadExistingEntry();
                                                } else {
                                                    selectNewMood();
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void createJournalEntry(String entryBody, DocumentReference moodRef, String location, String[] newTags) {
        String month = date.getMonth().getValue() + "";
        String day = date.getDayOfMonth() + "";
        String year = date.getYear() + "";


        JournalEntry journalEntry = new JournalEntry(entryBody, moodRef, location);

        db.collection("users")
                .document(email)
                .collection("year")
                .document(year)
                .collection("month")
                .document(month)
                .collection("day")
                .document(day)
                .set(journalEntry)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        String journalRefStr = "users/" + email + "/year/" + year + "/month/" + month + "/day/" + day;
                        DocumentReference journalRef = db.document(journalRefStr);
                        createTags(journalRef, newTags);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to update journal entry", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void resetMoodsArray() {
        moods = new ArrayList<>();
        moodRefs = new ArrayList<>();
        moods.add(new Mood(Color.LTGRAY, "Create new mood", 0));
    }

    private void loadExistingEntry() {
        // will load the existing journal entry if one exists
        String month = date.getMonth().getValue() + "";
        String day = date.getDayOfMonth() + "";
        String year = date.getYear() + "";

        db.collection("users")
                .document(email)
                .collection("year")
                .document(year)
                .collection("month")
                .document(month)
                .collection("day")
                .document(day)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentReference docRef = task.getResult().getReference();
                            docRef.collection("tags")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            ArrayList<String> allSelectedTags = new ArrayList<>();
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot docSnap : task.getResult()) {
                                                    allSelectedTags.add(docSnap.getId());
                                                }
                                                selectedTagsList = allSelectedTags;
                                            }
                                        }
                                    });
                        }
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        JournalEntry journalEntry = documentSnapshot.toObject(JournalEntry.class);
                        if (journalEntry != null) {
                            editTextJournalBody.setText(journalEntry.getEntryBody());
                            editTextLocation.setText(journalEntry.getLocation());
                            DocumentReference selectedMoodRef = journalEntry.getMoodRef();
                            for (int i = 0; i < moodRefs.size(); i++) {
                                DocumentReference moodRef = moodRefs.get(i);
                                if (moodRef.equals(selectedMoodRef)) {
                                    chooseMoodAdapter.setCurrSelected(i + 1);
                                    currSelectedMood = selectedMoodRef;
                                }
                            }
                        }
                    }
                });

    }

    public void updateSelectedMood(DocumentReference newMoodRef) {
        currSelectedMood = newMoodRef;
    }

    public void updateSelectedMood(int position) {
        currSelectedMood = moodRefs.get(position - 1);
    }

    private void selectNewMood() {
        for (int i = 0; i < moodRefs.size(); i++) {
            DocumentReference moodRef = moodRefs.get(i);
            if (moodRef.equals(currSelectedMood)) {
                chooseMoodAdapter.setCurrSelected(i + 1);
            }
        }
    }

    private void locationPressed() {
        if (ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                @NonNull
                @Override
                public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                    return null;
                }

                @Override
                public boolean isCancellationRequested() {
                    return false;
                }
            }).addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                        try {
                            List<Address> myList = geocoder.getFromLocation(latitude,longitude, 1);
                            String address = myList.get(0).getAddressLine(0);
                            editTextLocation.setText(address);
                        } catch (IOException e) {
                            String latitudeStr = Location.convert(latitude, Location.FORMAT_DEGREES);
                            String longitudeStr = Location.convert(longitude, Location.FORMAT_DEGREES);
                            editTextLocation.setText("Latitute: " + latitudeStr + ", Longitude: " + longitudeStr);

                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to get location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    private void getAllTags() {
        ArrayList<String> allTags = new ArrayList<>();
        db.collection("users")
                .document(email)
                .collection("tags")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot docSnap : task.getResult()) {
                                DocumentReference tagRef = docSnap.getReference();
                                allTags.add(tagRef.getId());
                            }
                            allTags.add("Add New Tags");
                            tags = allTags;
                            String[] tagArray = tags.toArray(new String[tags.size()]);
                            createDropdownList(tagArray);
                        }
                    }
                });
    }

    private void createTags(DocumentReference journalRef, String[] newTags) {

        ArrayList<String> tagsCopy = tags;
        ArrayList<String> selectedTagsCopy = selectedTagsList;

        for (String tag: tagsCopy) {
            if (!selectedTagsCopy.contains(tag)) {
                journalRef.collection("tags")
                        .document(tag.trim())
                        .delete();
            } else {
                String tmpTagRef = "users/" + email + "/tags/" + tag.trim();
                journalRef.collection("tags")
                        .document(tag)
                        .set(new TagRef(db.document(tmpTagRef)));
            }
        }

        for (String selected : newTags) {
            if (selected.trim().equals("")) {
                break;
            }
            DocumentReference newTagRef = db.collection("users")
                    .document(email)
                    .collection("tags")
                    .document(selected.trim());
            newTagRef.set(new Tag(selected))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                journalRef.collection("tags")
                                        .document(selected.trim())
                                        .set(new TagRef(newTagRef))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                selectedTagsList.add(selected.trim());
                                                String[] tagArray = tags.toArray(new String[tags.size()]);
                                            }
                                        });
                            }
                        }
                    });
            editTextNewTags.setText("");
        }

        sendData.onEntryDone();
    }

    private void createDropdownList(String[] tagArray) {

        boolean[] selectedTags = new boolean[tagArray.length];
        dropdownTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(false);
                if (editTextNewTags.getVisibility() == View.VISIBLE) {
                    selectedTags[selectedTags.length - 1] = true;
                }
                for (String tag : selectedTagsList) {
                }
                for (int i = 0; i < tagArray.length; i++) {
                    if (selectedTagsList.contains(tagArray[i])) {
                        selectedTags[i] = true;
                    }
                 }
                builder.setMultiChoiceItems(tagArray, selectedTags, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        // check condition
                        if (b) {
                            selectedTags[i] = true;
                            if (i == selectedTags.length - 1) {
                                editTextNewTags.setVisibility(View.VISIBLE);
                            } else {
                                selectedTagsList.add(tagArray[i]);
                            }
                        } else {
                            selectedTags[i] = false;
                            if (i == selectedTags.length - 1) {
                                editTextNewTags.setVisibility(View.INVISIBLE);
                            } else {
                                selectedTagsList.remove(tagArray[i]);
                            }
                        }
                    }
                });

                builder.setPositiveButton("OK", null);

                builder.show();
            }
        });
    }

    public interface IFromJournalEntry {
        void onEntryDone();
    }
}