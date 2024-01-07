package com.example.nu_mad_sp2023_final_project_10;

import android.net.Uri;
import android.widget.ProgressBar;

public interface IcameraFragmentNavigation {
    void fromFragToCam();

    void fromCamToDis(Uri imageUri);
    void fromCamToMem(Uri imageUri);

    void fromDisToMem(Uri imageUri, ProgressBar progressBar);
}
