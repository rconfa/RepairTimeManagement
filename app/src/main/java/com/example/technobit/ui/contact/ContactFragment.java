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
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.technobit.R;
import com.example.technobit.utilities.data.Contact;
import com.example.technobit.utilities.data.ContactSingleton;

import java.util.ArrayList;

/* TODO: edit existing contact??
*        Dialog message for confirm deletion?
* */
public class ContactFragment extends Fragment implements CardArrayAdapter.ItemLongClickListener{

    private ContactViewModel contactViewModel;
    private CardArrayAdapter cardArrayAdapter;
    private RecyclerView recView;
    private ContactSingleton sg;
    private ArrayList<Integer> posToBeRemoved;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // setto true le opzioni del menu, cosi posso visualizare le icone
        setHasOptionsMenu(true);

        contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
        View root = inflater.inflate(R.layout.fragment_contact, container, false);

        // save the singleton instance
        sg = ContactSingleton.getInstance(getContext());
        // list of position to removed
        posToBeRemoved = new ArrayList<>();

        recView = root.findViewById(R.id.contact_listview);
        recView.setHasFixedSize(true); // no change the layout size
        // setting layout
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recView.setLayoutManager(layoutManager);

        // adapter for the listview
        cardArrayAdapter = new CardArrayAdapter();
        // add all item to the adapter
        addContactToAdapter();

        // set the long click listener equal to my local listener
        cardArrayAdapter.mySetLongClickListener(this);

        // set the adapter for the listview
        recView.setAdapter(cardArrayAdapter);

        return root;
    }

    private void addContactToAdapter() {
        // retrive all contact from file
        ArrayList<Contact> allContact = sg.getContactList();
        if(allContact != null)
            cardArrayAdapter.add(allContact); // add all list to adapter
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
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
                // remove all items selected from file
                sg.delete(posToBeRemoved,getContext());

                // clear the list of items to be removed
                posToBeRemoved.clear();
                // remove all items from the listview
                cardArrayAdapter.removeSelected();

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

        // Create the view using the layout
        View v = getLayoutInflater().inflate(R.layout.custom_dialog_add_contact, null);

        builder.setView(v);
        final AlertDialog dialog = builder.create();

        // get layout items
        final Button yes = v.findViewById(R.id.btn_yes);
        final Button no = v.findViewById(R.id.btn_no);
        final EditText et_name = v.findViewById(R.id.et_dialog_name);
        final EditText et_email = v.findViewById(R.id.et_dialog_email);

        // Set button yes enable only if the name is not empty
        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count!=0) // if the name is not empty set the yes but to visible
                    yes.setVisibility(View.VISIBLE);
                else
                    yes.setVisibility(View.INVISIBLE);
            }

        });

        // If click on yes I add the values into my arrayAdapter
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get values
                String name = et_name.getText().toString();
                String email = et_email.getText().toString();
                // add values
                Contact contact = new Contact(name, email);
                sg.addContact(contact, getContext());
                cardArrayAdapter.add(new Card(contact));

                // close dialog
                dialog.dismiss();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    @Override
    public void onItemLongClick(View view, int position) {
        // save the selected position
        boolean res = cardArrayAdapter.savePositionToDelete(position);
        if(res) { // if I add the position I set the background
            view.setBackgroundResource(R.drawable.card_background_selected);
            posToBeRemoved.add(position);
        }
        else { // If I delete the position I set the default background
            view.setBackgroundResource(R.drawable.card_background);
            posToBeRemoved.remove((Object) position); // I want to remove the obj not the index
        }
    }
}