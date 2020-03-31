package com.example.technobit.contactdatas;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;

// Class that implement Singleton pattern
// store all the contact list that can be used in all class.
public class Singleton {
    // TODO: in catch give back an error to the user.

    private static Singleton instance;

    // Global variable
    private  ArrayList<SingleContact> clienti;
    private RetrieveFromFile dc;

    // Restrict the constructor from being instantiated
    private Singleton(Context c){
        dc = new RetrieveFromFile();

        // Read all contact from file
        try {
            clienti = dc.readFile(c);
        } catch (IOException e) {
            clienti = null;
        }
    }

    public ArrayList<SingleContact> getContactList(){
        return this.clienti;
    }

    // return an array list with all company_name
    public ArrayList<String> getContactNameList(){

        ArrayList<String> name = new ArrayList<String>();
        if(this.clienti != null)
            for(SingleContact s:this.clienti)
                name.add(s.getCompany_name());

        return name;
    }

    public void delete(ArrayList<Integer> pos, Context c){
        if(clienti == null)
            return;
        // Remove the client from the file
        try {
            dc.delete(pos,c);
            // If no errors I delete The client also from the list
            for(int index:pos)
                clienti.remove(index);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addContact(SingleContact c_temp,Context c){
        if(clienti == null)
            clienti = new  ArrayList<SingleContact>();
        try {
            dc.writeToFile(c_temp, c); // Add the new contact to file
            // if no errors I add the contact to list
            clienti.add(c_temp);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static synchronized Singleton getInstance(Context c){
        if(instance==null){
            instance=new Singleton(c);
        }
        return instance;
    }
}
