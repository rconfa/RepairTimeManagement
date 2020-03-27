package com.example.technobit.ui.contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.technobit.R;

public class ContactFragment extends Fragment {

    private ContactViewModel contactViewModel;
    private CardArrayAdapter cardArrayAdapter;
    private ListView listView;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // setto true le opzioni del menu, cosi posso visualizare le icone
        setHasOptionsMenu(true);

        contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
        View root = inflater.inflate(R.layout.fragment_contact, container, false);


        listView = (ListView) root.findViewById(R.id.contact_listview);

        cardArrayAdapter = new CardArrayAdapter(getContext(), R.layout.list_item_card);

        for (int i = 0; i < 10; i++) {
            Card card = new Card("company: " + (i+1) + " company1", "email: " + (i+1) + " email2");
            cardArrayAdapter.add(card);
        }

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
                boolean res = cardArrayAdapter.savePositionToDelete(String.valueOf(pos));
                if(res) // se ho aggiunto la posizione metto come colore di sfondo il rosso
                    arg1.setBackgroundResource(R.drawable.card_background_selected);
                else // Se l'ho deselezionato rimetto lo sfondo bianco
                    arg1.setBackgroundResource(R.drawable.card_background);

                return true;
            }
        });


        return root;
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
            //Back button
            case R.id.icon_remove:
                cardArrayAdapter.removeSelected(); // rimuovo tutti gli oggetti selezionati
                // listView.invalidateViews();
                // forzo aggiornamento del dataSet
                cardArrayAdapter.notifyDataSetChanged();
                return true;
            case R.id.icon_add:
                CustomDialogClass cdd = new CustomDialogClass(getActivity());
                cdd.show();

                Window window = cdd.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                // TODO SAVE new contact se clicca ok
                return true;
            default:
                return true;
        }
    }


}