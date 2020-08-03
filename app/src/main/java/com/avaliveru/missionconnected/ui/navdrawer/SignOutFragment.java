package com.avaliveru.missionconnected.ui.navdrawer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.avaliveru.missionconnected.MainActivity;
import com.avaliveru.missionconnected.R;
import com.google.firebase.auth.FirebaseAuth;

public class SignOutFragment extends Fragment {

    private SignOutViewModel signOutViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_signout, container, false);

        // FirebaseAuth mAuth = FirebaseAuth.getInstance();
        //mAuth.signOut();

        return root;
    }
}
