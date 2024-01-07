package com.example.nu_mad_sp2023_final_project_10;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DisplayPhotoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayPhotoFragment extends Fragment {

    private static final String ARG_URI = "imageUri";

    private Uri imageUri;
    private ImageView photoImageView;
    private Button retakeButton, uploadButton;
    private IcameraFragmentNavigation cListener;
    private ProgressBar progressBar;

    public DisplayPhotoFragment() {
        // Required empty public constructor
    }

    public static DisplayPhotoFragment newInstance(Uri imageUri) {
        DisplayPhotoFragment fragment = new DisplayPhotoFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, imageUri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUri = getArguments().getParcelable(ARG_URI);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_display_photo, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        photoImageView = view.findViewById(R.id.imageViewPhoto);
        retakeButton = view.findViewById(R.id.buttonRetake);
        uploadButton = view.findViewById(R.id.buttonUpload);

        Glide.with(view).load(imageUri).centerCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Toast.makeText(getContext(), "Image failed to load",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).into(photoImageView);

        retakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cListener.fromFragToCam();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cListener.fromDisToMem(imageUri, progressBar);
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof IcameraFragmentNavigation) {
            cListener = (IcameraFragmentNavigation) context;
        } else {
            throw new RuntimeException(context.toString() + "must implement IcameraFragmentNavigation");
        }
    }
}
