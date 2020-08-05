package com.avaliveru.missionconnected.ui.navdrawer;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.avaliveru.missionconnected.R;

public class PolicyFragment extends Fragment {

    private PolicyViewModel mViewModel;

    public static PolicyFragment newInstance() {
        return new PolicyFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_support, container, false);

        mViewModel =
                ViewModelProviders.of(this).get(PolicyViewModel.class);
        final TextView textView = root.findViewById(R.id.text_support);
        mViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(PolicyViewModel.class);
        // TODO: Use the ViewModel
    }

}