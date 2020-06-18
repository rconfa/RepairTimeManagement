package com.technobit.repair_timer.ui.model;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.technobit.repair_timer.utils.contact.Contact;
import com.technobit.repair_timer.utils.contact.ContactRepository;

import java.io.IOException;
import java.util.ArrayList;

// Shared view model for chronometer and contact fragment
public class SharedViewModel extends ViewModel {
    // MutableLiveData of all contacts
    private MutableLiveData<ArrayList<Contact>> contacts;
    private ContactRepository repo; // repository


    private void initRepository(Context mContext){
        if(repo == null)
            repo = ContactRepository.getInstance(mContext);
    }

    private void updateData(){
        contacts.setValue(repo.getContactList());
    }

    public MutableLiveData<ArrayList<Contact>> getContacts(Context mContext){
        initRepository(mContext);
        if(contacts == null){
            contacts = new MutableLiveData<>();
            contacts.setValue(repo.getContactList());
        }

        return contacts.getValue() != null ? contacts : new MutableLiveData<ArrayList<Contact>>();
    }

    public void deleteContact(ArrayList<Integer> position, Context mContext) throws IOException {
        initRepository(mContext);
        repo.delete(position, mContext);
        updateData();
    }

    public boolean addContact(Contact c, Context mContext) throws IOException {
        initRepository(mContext);
        boolean result = repo.addContact(c, mContext);
        if(result) {
            updateData();
        }
        return result;
    }

    public void updateContact(Contact c, int position, Context mContext) throws IOException {
        initRepository(mContext);
        repo.updateContact(c, position, mContext);
        updateData();
    }

    public ArrayList<String> getContactsName(Context mContext){
        initRepository(mContext);
        return repo.getContactNameList();
    }

}
