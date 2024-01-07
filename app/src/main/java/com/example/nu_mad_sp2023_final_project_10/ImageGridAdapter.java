package com.example.nu_mad_sp2023_final_project_10;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ImageGridAdapter extends RecyclerView.Adapter<ImageGridAdapter.ViewHolder>{
    private ArrayList<Uri> images = new ArrayList<>();
    private ArrayList<String> refs = new ArrayList<>();

    ImageGridAdapter() {
    }

    ImageGridAdapter(ArrayList<Uri> uris, ArrayList<String> refs) {
        this.images = uris;
        this.refs = refs;
    }

    public ArrayList<Uri> getImages() {
        return images;
    }

    public void setImages(ArrayList<Uri> images) {
        this.images = images;
    }

    public ArrayList<String> getRefs() {
        return refs;
    }

    public void setRefs(ArrayList<String> refs) {
        this.refs = refs;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageDisplay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageDisplay = itemView.findViewById(R.id.imageView);
        }

        public ImageView getImageDisplay() {return imageDisplay;}
    }

    @NonNull
    @Override
    public ImageGridAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemRecyclerView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.imagegrid_row, parent, false);

        return new ViewHolder(itemRecyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageGridAdapter.ViewHolder holder, int position) {
        String ref = this.getRefs().get(position);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference = storageReference.child(ref);

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                Glide.with(holder.itemView)
                .load(uri)
                .into(holder.getImageDisplay());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(holder.itemView.getContext(), "Image failed to load", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.getRefs().size();
    }
}

