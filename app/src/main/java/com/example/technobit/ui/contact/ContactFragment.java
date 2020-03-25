package com.example.technobit.ui.contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.technobit.R;

public class ContactFragment extends Fragment {

    private ContactViewModel contactViewModel;
    private MenuItem fav;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // setto true le opzioni del menu, cosi posso visualizare le icone
        setHasOptionsMenu(true);

        contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
        View root = inflater.inflate(R.layout.fragment_contac, container, false);


        return root;
    }


    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // aggiungo l'icona "add" alla barra in alto
        fav = menu.add("add");
        fav.setIcon(R.drawable.add_contact);
        fav.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

    }


}