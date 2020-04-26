package com.example.technobit.utilities.data;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;

// Class that implement Singleton pattern
// store all the contact list that can be used in all class.
public class ContactSingleton {
    private static ContactSingleton instance;

    // Global variable
    private  ArrayList<Contact> clienti;
    private FileContact dc;

    // Restrict the constructor from being instantiated
    private ContactSingleton(Context c){
        dc = new FileContact();

        // Read all contact from file
        try {
            clienti = dc.readFile(c);
        } catch (IOException e) {
            clienti = null;
        }
    }

    public ArrayList<Contact> getContactList(){
        return this.clienti;
    }

    // return an array list with all company_name
    public ArrayList<String> getContactNameList(){

        ArrayList<String> name = new ArrayList<>();
        if(this.clienti != null)
            for(Contact s:this.clienti)
                name.add(s.getCompany_name());

        return name;
    }

    public void delete(ArrayList<Integer> pos, Context c) throws IOException {
        if(clienti == null)
            return;
        // Remove the client from the file
        dc.delete(pos,c);
        // If no errors I delete The client also from the list
        for(int index:pos)
            clienti.remove(index);
    }

    public boolean addContact(Contact c_temp, Context c) throws IOException {
        if(clienti == null)
            clienti = new  ArrayList<>();

        if(!clienti.contains(c_temp)) {
            dc.writeToFile(c_temp, c); // Add the new contact to file
            // if no errors I add the contact to list
            clienti.add(c_temp);
            return true;
        }

        return false;
    }

    public void updateContact(Contact toUpdate, int pos, Context c) throws IOException {
        if(clienti == null)
            return;
        // update the contact on file
        dc.update(toUpdate, pos,c);
        // If no errors I update it also in the list
        clienti.set(pos, toUpdate); // update into client list
    }

    public static synchronized ContactSingleton getInstance(Context c){
        if(instance==null){
            instance=new ContactSingleton(c);
        }
        return instance;
    }
}
