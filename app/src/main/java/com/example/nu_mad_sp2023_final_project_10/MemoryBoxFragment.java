package com.example.nu_mad_sp2023_final_project_10;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MemoryBoxFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemoryBoxFragment extends Fragment {

    private Button addFriendButton, sendNoteButton, addPhotoButton;
    private ProgressBar progressBar;
    private int loaded = 0;
    private BottomNavigationView navBar;

    private RecyclerView imageGridRecycler;
    private RecyclerView.LayoutManager imageGridRecyclerLayoutManager;
    private ImageGridAdapter imageGridAdapter;
    private RecyclerView notesRecycler;
    private RecyclerView.LayoutManager notesRecyclerLayoutManager;
    private NotesAdapter notesAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private ArrayList<String> notes;
    private ArrayList<Uri> images;
    private ArrayList<String> imageReferences;

    private IcameraFragmentNavigation cListener;
    private IfromFragmentToActivity mListener;

    public MemoryBoxFragment() {
        // Required empty public constructor
    }

    public static MemoryBoxFragment newInstance() {
        MemoryBoxFragment fragment = new MemoryBoxFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {}

        notes = new ArrayList<String>();
        images = new ArrayList<Uri>();
        imageReferences = new ArrayList<String>();

        // initialize firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_memory_box, container, false);
        getActivity().setTitle("Memory Box");

        addFriendButton = rootView.findViewById(R.id.buttonAddFriend);
        sendNoteButton = rootView.findViewById(R.id.buttonSendNote);
        addPhotoButton = rootView.findViewById(R.id.buttonAddPhoto);
        progressBar = rootView.findViewById(R.id.progressBarLoadingContent);
        navBar = rootView.findViewById(R.id.navigationMemoryBox);

        navBar.getMenu().findItem(R.id.memoryBox).setChecked(true);
        navBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.memoryBox:
                        getActivity().setTitle("Memory Box");
                        return true;
                    case R.id.Calendar:
                        getActivity().setTitle("Calendar");
                        mListener.onCalendar();
                        return true;
                    default:
                        return true;
                }
            }
        });

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopUpAddFriend popUpClass = new PopUpAddFriend();
                popUpClass.showPopupWindow(view);
            }
        });

        sendNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopUpSendNote popUpClass = new PopUpSendNote();
                popUpClass.showPopupWindow(view);
            }
        });

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cListener.fromFragToCam();
            }
        });

        notes = new ArrayList<String>();
        images = new ArrayList<Uri>();
        imageReferences = new ArrayList<String>();

        // image recycler view setup
        imageGridRecycler = rootView.findViewById(R.id.recyclerViewImagesGallery);
        imageGridRecyclerLayoutManager = new GridLayoutManager(getContext(), 4);
        imageGridAdapter = new ImageGridAdapter();
        imageGridRecycler.setAdapter(imageGridAdapter);
        imageGridRecycler.setLayoutManager(imageGridRecyclerLayoutManager);
        imageGridAdapter.notifyDataSetChanged();

        // notes recycler view setup
        notesRecycler = rootView.findViewById(R.id.recyclerViewNotes);
        notesRecyclerLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        notesAdapter = new NotesAdapter();
        notesRecycler.setAdapter(notesAdapter);
        notesRecycler.setLayoutManager(notesRecyclerLayoutManager);
        notesAdapter.notifyDataSetChanged();

        progressBar.setVisibility(View.VISIBLE);
        fetchCurrentNotes();
        fetchCurrentImages();

        return rootView;
    }

    private void setVisible() {
        if(loaded == 2) {
            progressBar.setVisibility(View.INVISIBLE);
            loaded = 0;
        }
    }

    private void fetchCurrentNotes() {
        db.collection("users")
                .document(mUser.getEmail())
                .collection("notes")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                String note = (String) documentSnapshot.get("note");
                                notes.add(note);
                            }
                            // update recycler view to show all notes
                            updateNotesRecyclerView();
                        } else {
                            Toast.makeText(getContext(), "Could not fetch notes.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Could not fetch notes.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchCurrentImages() {
        db.collection("users")
                .document(mUser.getEmail())
                .collection("images")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                Uri image = Uri.parse((String) documentSnapshot.get("image"));
                                images.add(image);

                                String ref = (String) documentSnapshot.get("storageRef");
                                imageReferences.add(ref);
                            }
                            // update recycler view to show all images
                            updateImagesRecyclerView();
                        } else {
                            Toast.makeText(getContext(), "Could not fetch images.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Could not fetch images.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof IcameraFragmentNavigation) {
            cListener = (IcameraFragmentNavigation) context;
        } else {
            throw new RuntimeException(context.toString() + "must implement IcameraFragmentNavigation");
        }
        if(context instanceof  IfromFragmentToActivity) {
            mListener = (IfromFragmentToActivity) context;
        } else {
            throw new RuntimeException(context.toString() + "must implement IfromFragmentToActivity");
        }
    }

    public void updateNotesRecyclerView() {
        notesAdapter.setNotes(this.notes);
        notesRecycler.setAdapter(notesAdapter);
        notesRecycler.setLayoutManager(notesRecyclerLayoutManager);
        notesAdapter.notifyDataSetChanged();
        loaded++;
        setVisible();
    }

    public void updateImagesRecyclerView() {
        imageGridAdapter.setImages(this.images);
        imageGridAdapter.setRefs(this.imageReferences);
        imageGridRecycler.setAdapter(imageGridAdapter);
        imageGridRecycler.setLayoutManager(imageGridRecyclerLayoutManager);
        imageGridAdapter.notifyDataSetChanged();
        loaded++;
        setVisible();
    }
}