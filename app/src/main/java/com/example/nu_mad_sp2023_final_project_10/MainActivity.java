package com.example.nu_mad_sp2023_final_project_10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import android.Manifest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements IfromFragmentToActivity,
        CreateMoodFragment.IFromCreateMood,
        ChooseMoodAdapter.IFromChooseMoodAdapter,
        JournalEntryFragment.IFromJournalEntry,
        IcameraFragmentNavigation {
    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_CODE = 0x100;

    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private User currentLocalUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        Boolean cameraAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        Boolean readAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        Boolean writeAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if(cameraAllowed && readAllowed && writeAllowed) {
            Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
            loadJournal();
        } else {
            requestPermissions(new String[] {
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSIONS_CODE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        loadJournal();
    }

    @Override
    public void fromFragment(boolean toLoginPage) {
        if(toLoginPage) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, LoginFragment.newInstance(), "LoginFragment")
                    .addToBackStack(null).commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, RegisterFragment.newInstance(), "RegisterFragment")
                    .addToBackStack(null).commit();
        }
    }

    private void loadJournal() {
        if (currentUser != null) {
            getSupportFragmentManager().popBackStack();
            populateMainFragment(currentUser);
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, HomePageFragment
                            .newInstance(),"HomePageFragment")
                    .commit();
        }
    }

    @Override
    public void onLogout() {
        mAuth.signOut();
        currentUser = null;
        loadJournal();
    }

    @Override
    public void onMemoryBox() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, MemoryBoxFragment.newInstance(), "MemoryBoxFragment")
                .addToBackStack("JournalHomePageFragment")
                .commit();
    }

    @Override
    public void onCalendar() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, JournalHomePageFragment.newInstance(currentLocalUser), "JournalHomePageFragmentMemoryBoxFragment")
                .addToBackStack("MemoryBoxFragment")
                .commit();
    }


    @Override
    public void populateMainFragment(FirebaseUser currentUser) {
        this.currentUser = currentUser;
        getSupportFragmentManager().popBackStack();
        db.collection("users")
                .document(currentUser.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            currentLocalUser = task.getResult()
                                    .toObject(User.class);
                            //Populating The Main Fragment....
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, JournalHomePageFragment.newInstance(currentLocalUser),"JournalHomePageFragment")
                                    .commit();
                            // This is the call that needs to be made when something is pressed in the recylcer "calendar"
                            //  ** pass in the actual date clicked, not today's date (but has to be a Date object) **

                        }
                    }
                });
    }

    @Override
    public void registerDone(FirebaseUser firebaseUser, User user) {
        this.currentLocalUser = user;
        this.currentUser = firebaseUser;
//        Updating the Firestore structure....
        updateFirestoreWithUserDetails(user);
    }

    @Override
    public void onDaySelected(LocalDate date) {

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, JournalEntryFragment.newInstance(date), "JournalEntryFragment")
                .addToBackStack(null).commit();
    }

    private void updateFirestoreWithUserDetails(User user) {
        db.collection("users")
                .document(user.getEmail())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
//                      On success populate home screen...
                        populateMainFragment(currentUser);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    @Override
    public void onMoodCreation(DocumentReference newMoodRef) {
        getSupportFragmentManager().popBackStack();
        JournalEntryFragment journalEntryFragment = (JournalEntryFragment) getSupportFragmentManager()
                .findFragmentByTag("JournalEntryFragment");
        journalEntryFragment.updateSelectedMood(newMoodRef);
    }

    @Override
    public void onChooseMood(int position) {
        JournalEntryFragment journalEntryFragment = (JournalEntryFragment) getSupportFragmentManager()
                .findFragmentByTag("JournalEntryFragment");
        journalEntryFragment.updateSelectedMood(position);
    }

    @Override
    public void onCreateMood() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, CreateMoodFragment.newInstance(), "CreateMoodFragment")
                .addToBackStack(null).commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0) {
            loadJournal();
        } else {
            Toast.makeText(this, "You must allow Camera and Storage permissions!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void fromFragToCam() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, CameraControlFragment.newInstance(), "CameraControlFragment")
                .addToBackStack("CameraControl").commit();
    }

    @Override
    public void fromCamToDis(Uri imageUri) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, DisplayPhotoFragment.newInstance(imageUri), "DisplayPhotoFragment")
                .addToBackStack("DisplayPhoto").commit();
    }

    public void fromCamToMem(Uri imageUri) {
        StorageReference storageReference = storage.getReference();
        storageReference = storageReference.child("images/" + imageUri.getLastPathSegment());

        UploadTask uploadImage = storageReference.putFile(imageUri);
        uploadImage.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Upload Failed! Try again!", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();

                Map<String, Object> im = new HashMap<>();
                im.put("image", imageUri);
                im.put("storageRef", "images/" + imageUri.getLastPathSegment());

                db.collection("users")
                        .document(currentUser.getEmail())
                        .collection("images")
                        .document(imageUri.getLastPathSegment()).set(im);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, MemoryBoxFragment.newInstance(), "MemoryBoxFragment")
                        .addToBackStack("MemoryBox").commit();
            }
        });
    }

    @Override
    public void fromDisToMem(Uri imageUri, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);

        StorageReference storageReference = storage.getReference();
        storageReference = storageReference.child("images/" + imageUri.getLastPathSegment());

        UploadTask uploadImage = storageReference.putFile(imageUri);
        uploadImage.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Upload Failed! Try again!", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);

                Map<String, Object> im = new HashMap<>();
                im.put("image", imageUri);
                im.put("storageRef", "images/" + imageUri.getLastPathSegment());

                db.collection("users")
                        .document(currentUser.getEmail())
                        .collection("images")
                        .document(imageUri.getLastPathSegment()).set(im);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, MemoryBoxFragment.newInstance(), "MemoryBoxFragment")
                        .addToBackStack("MemoryBox").commit();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                progressBar.setProgress((int) progress);
            }
        });
    }

    @Override
    public void onEntryDone() {
        getSupportFragmentManager().popBackStack();
    }
}