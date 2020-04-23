package com.example.technobit.ui.contact;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.technobit.R;
import com.example.technobit.ui.customize.dialog.ConfirmChoiceDialog;
import com.example.technobit.ui.customize.dialog.ManageContactDialog;
import com.example.technobit.utilities.data.Contact;
import com.example.technobit.utilities.data.ContactSingleton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;

// todo: back arrow when item are selected
public class ContactFragment extends Fragment
        implements CardArrayAdapter.ItemLongClickListener, CardArrayAdapter.ItemClickListener {

    private static final String TAG = "ContactFragment";
    private ContactViewModel contactViewModel;
    private CardArrayAdapter mCardArrayAdapter;
    private ContactSingleton mContactSingleton;
    private ArrayList<Integer> mPosToBeRemoved;
    private MenuItem mMenuDeleteItem;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // setto true le opzioni del menu, cosi posso visualizare le icone
        setHasOptionsMenu(true);

        //contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
        View root = inflater.inflate(R.layout.fragment_contact, container, false);

        // save the singleton instance
        mContactSingleton = ContactSingleton.getInstance(getContext());
        // list of position to removed
        mPosToBeRemoved = new ArrayList<>();

        // setting the recycle view for the fragment
        RecyclerView recView = root.findViewById(R.id.contact_listview);
        recView.setHasFixedSize(true); // no change the layout size, better performance
        // setting layout
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recView.setLayoutManager(layoutManager);

        // adapter for the recycle view
        mCardArrayAdapter = new CardArrayAdapter();
        // add all item to the adapter
        addContactToAdapter();

        // set the long click listener and the click listener equal to my local listener
        mCardArrayAdapter.mySetLongClickListener(this);
        mCardArrayAdapter.mySetClickListener(this);

        // set the adapter for the listview
        recView.setAdapter(mCardArrayAdapter);

        return root;
    }

    private void addContactToAdapter() {
        // get all contact from file
        ArrayList<Contact> allContact = mContactSingleton.getContactList();
        if(allContact != null) // if there is some contact
            mCardArrayAdapter.add(allContact); // add all list to adapter
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        // Upload menu file with add/delete icons
        inflater.inflate(R.menu.menu_contact, menu); //.xml file name
        // get the item for delete contact
        mMenuDeleteItem = menu.findItem(R.id.icon_remove);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Handling Action Bar button click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.icon_remove:
                performRemovingContact();
                return true;
            case R.id.icon_add:
                displayAddContactDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // long click on recycle view item
    @Override
    public void onItemLongClick(View view, int position) {
        // save the selected position
        boolean res = mCardArrayAdapter.savePositionToDelete(position);
        if(res) { // if I add the position I set the background
            mPosToBeRemoved.add(position);
            mCardArrayAdapter.notifyItemChanged(position); // update the card view (change background color)
        }
        else { // If I delete the position I set the default background
            mPosToBeRemoved.remove((Object) position); // I want to remove the obj not the index
            mCardArrayAdapter.notifyItemChanged(position); // update the card view (change background color)
        }

        // remove delete icon
        if(this.mPosToBeRemoved.isEmpty())
            mMenuDeleteItem.setVisible(false);
        else
            mMenuDeleteItem.setVisible(true);
    }

    public void performRemovingContact(){
        // Ask the user to confirm the action
        // get the message from the resource
        String message = getString(R.string.dialog_confirm_delete_message);
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new ConfirmChoiceDialog(" ", message,confirmDeleteListener);
        dialog.show(getParentFragmentManager(), TAG);

    }

    // listner for the dialog
    private ConfirmChoiceDialog.NoticeDialogListener confirmDeleteListener = new ConfirmChoiceDialog.NoticeDialogListener() {
        @Override
        public void onDialogPositiveClick() {
            mMenuDeleteItem.setVisible(false); // remove delete icon
            // remove all items selected from file
            try {
                mContactSingleton.delete(mPosToBeRemoved,getContext());
                // clear the list of items to be removed
                mPosToBeRemoved.clear();
                // remove all items from the listview
                mCardArrayAdapter.removeSelected();
            } catch (IOException e) {
                displaySnackbarError();
            }
        }
    };


    // dialog to add new contact
    private void displayAddContactDialog() {
        // get the title for the dialog
        String title = getString(R.string.dialog_add_contact_title);
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new ManageContactDialog(title,"","", -1,
                getContext(), mAddContactListener);
        dialog.show(getParentFragmentManager(), TAG);
    }

    // notice listener on positive button click for add contact
    private ManageContactDialog.NoticeDialogListener mAddContactListener = new ManageContactDialog.NoticeDialogListener() {
        @Override
        public void onDialogPositiveClick(Contact c, int position) {
            try {
                mContactSingleton.addContact(c, getContext());
                mCardArrayAdapter.add(new Card(c));
            } catch (IOException e) {
                displaySnackbarError();
            }

        }
    };


    // click on recycle view item
    @Override
    public void onItemClick(View view, int position, String name, String email) {
        // get the title for the dialog
        String title = getString(R.string.dialog_update_contact_title);
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new ManageContactDialog(title,name,email, position,
                getContext(), mUpdateContactListener);
        dialog.show(getParentFragmentManager(), TAG);
    }

    // notice listener on positive button click for contact update
    private ManageContactDialog.NoticeDialogListener mUpdateContactListener = new ManageContactDialog.NoticeDialogListener() {
        @Override
        public void onDialogPositiveClick(Contact c, int position) {
            Card temp = new Card(c);
            try {
                mContactSingleton.updateContact(c, position, getContext());
                mCardArrayAdapter.modify(temp,position);
            } catch (IOException e) {
                displaySnackbarError();
            }

        }
    };

    private void displaySnackbarError(){
        // create a snackbar with a positive message
        Snackbar snackbar = Snackbar.make(getView(), R.string.snackbar_file_error, Snackbar.LENGTH_LONG);
        snackbar.setTextColor(Color.WHITE);
        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary))
                .setAction(getString(R.string.snackbar_close_btn), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
        snackbar.show();
    }

}