package com.example.technobit.ui.contact;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.technobit.R;
import com.example.technobit.contactdatas.SingleContact;
import com.example.technobit.contactdatas.Singleton;

import java.util.ArrayList;

public class ContactFragment extends Fragment{

    private ContactViewModel contactViewModel;
    private CardArrayAdapter cardArrayAdapter;
    private ListView listView;
    private Singleton sg;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // setto true le opzioni del menu, cosi posso visualizare le icone
        setHasOptionsMenu(true);

        contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
        View root = inflater.inflate(R.layout.fragment_contact, container, false);

        // save the singleton instance
        sg = Singleton.getInstance(getContext());

        listView = (ListView) root.findViewById(R.id.contact_listview);

        cardArrayAdapter = new CardArrayAdapter(getContext(), R.layout.list_item_card);

        addContactToAdapter();
        /*
        for (int i = 0; i < 10; i++) {
            Card card = new Card("company: " + (i+1) + " company1", "email: " + (i+1) + " email2");
            cardArrayAdapter.add(card);
        }
        */

        listView.setAdapter(cardArrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                System.out.println("test2");
            }
        });

        // evento sul click di un elemento della lista
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,final int pos, long id) {
                // salvo la posizone selezionata
                boolean res = cardArrayAdapter.savePositionToDelete(pos);
                if(res) // se ho aggiunto la posizione metto come colore di sfondo il rosso
                    arg1.setBackgroundResource(R.drawable.card_background_selected);
                else // Se l'ho deselezionato rimetto lo sfondo bianco
                    arg1.setBackgroundResource(R.drawable.card_background);

                return true;
            }
        });


        return root;
    }

    private void addContactToAdapter() {
        // retrive all contact from file
        ArrayList<SingleContact> allContact = sg.getContactList();
        if(allContact != null)
            cardArrayAdapter.add(allContact); // add all list to adapter


    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // carico il file per il menu, cosi visualizzo le icone per aggiungere/eliminare un contatto
        inflater.inflate(R.menu.menu_contact, menu); //.xml file name
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Handling Action Bar button click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.icon_remove:

                cardArrayAdapter.removeSelected(); // rimuovo tutti gli oggetti selezionati
                // listView.invalidateViews();
                // forzo aggiornamento del dataSet
                cardArrayAdapter.notifyDataSetChanged();
                return true;
            case R.id.icon_add:
                displayAddContactDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // creo la view sfruttando il mio custom layout
        View v = getLayoutInflater().inflate(R.layout.custom_dialog_add_contact, null);

        builder.setView(v);
        final AlertDialog dialog = builder.create();

        // Accesso agli oggetti della view
        final Button yes = (Button) v.findViewById(R.id.btn_yes);
        final Button no = (Button) v.findViewById(R.id.btn_no);
        final EditText et_name = (EditText) v.findViewById(R.id.et_dialog_name);
        final EditText et_email = (EditText) v.findViewById(R.id.et_dialog_email);

        // voglio che il nome sia obbligatorio, se è vuoto non abilito il bottone "ok"
        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count!=0) // se ha scritto il nome attivo il bottone ok
                    yes.setVisibility(View.VISIBLE);
                else
                    yes.setVisibility(View.INVISIBLE);
            }

        });

        // se clicca ok aggiungo al mio adapter i nuovi valori e chiudo la finestra!
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // prendo i valori
                String name = et_name.getText().toString();
                String email = et_email.getText().toString();
                // aggiungo i valori
                SingleContact contact = new SingleContact(name, email);
                sg.addContact(contact, getContext());
                cardArrayAdapter.add(new Card(contact));
                cardArrayAdapter.notifyDataSetChanged();

                // chiudo la dialog
                dialog.dismiss();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // visualizzo la dialog
        dialog.show();
    }



}