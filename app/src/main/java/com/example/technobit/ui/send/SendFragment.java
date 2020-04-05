package com.example.technobit.ui.send;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.technobit.R;
import com.example.technobit.utilities.notSendedData.DataToSend;

import java.io.IOException;
import java.util.ArrayList;

public class SendFragment extends Fragment {

    private SendViewModel sendViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //sendViewModel = ViewModelProviders.of(this).get(SendViewModel.class);
        View root = inflater.inflate(R.layout.fragment_send, container, false);
        final TextView textView = root.findViewById(R.id.text_send);

        ArrayList<DataToSend> allData;
        try {
            allData = new DataToSend().getAll(getContext());
        } catch (IOException e) {
            allData = null;
        }

        if(allData!=null)
            textView.setText("find " + allData.size() + " event not sended yet");
        else
            textView.setText("Error on file reading");


        // View root = inflater.inflate(R.layout.fragment_signature, container, false);
        return root;
    }
}