package com.avaliveru.missionconnected.ui.navdrawer;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
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

public class SupportFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_support, container, false);
        final TextView textView = root.findViewById(R.id.text_support);
        textView.setText(getString(R.string.support_message) + getString(R.string.linkwebite) + getString(R.string.support_outro));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        return root;
    }
}