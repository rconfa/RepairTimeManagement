package com.technobit.repair_timer.repositories.contact;

import android.content.Context;

import com.technobit.repair_timer.utils.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FileContact {

    // legge un file txt e riporta tutti i dati come array
    public ArrayList<Contact> readFile(Context context) throws IOException {
        ArrayList<Contact> names = new ArrayList<>();

        File f = new File(context.getFilesDir() + "/" + Constants.CONTACTS_FILENAME);
        if(f.exists()) {
            InputStreamReader input = new InputStreamReader(context.openFileInput(Constants.CONTACTS_FILENAME));
            BufferedReader in = new BufferedReader(input);
            String line;
            Contact toAdd;
            while ((line = in.readLine()) != null) {
                toAdd = new Contact().readFromString(line); // Retrieve the contact from the line
                if (toAdd != null)
                    names.add(toAdd); // adding the contact to list
            }

            in.close();
        }
        return names;
    }


    private void writeAllToFile(ArrayList<Contact> datas, Context context) throws IOException {
        // scrivo sul file
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(Constants.CONTACTS_FILENAME, Context.MODE_PRIVATE));
        // scrivo tutte le stringhe
        for (Contact s:datas) {
            outputStreamWriter.write(s.toString()+"\n");
        }
        outputStreamWriter.close();
    }

    public void writeToFile(Contact data, Context context) throws IOException {

        // scrivo sul file
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(Constants.CONTACTS_FILENAME, Context.MODE_APPEND));
       // Write the data on file using toString method
        outputStreamWriter.write(data.toString()+"\n");

        outputStreamWriter.close();
    }

    // leggo tutte le righe del file tranne la riga "pos", le salvo in un vettore, poi riscrivo tutto
    // cosi ho eliminato la riga pos
    public void delete(ArrayList<Integer> pos,Context context) throws IOException {
        ArrayList<Contact> names = new ArrayList<>();

        InputStreamReader input = new InputStreamReader(context.openFileInput(Constants.CONTACTS_FILENAME));
        BufferedReader in = new BufferedReader(input);
        String line;
        int numLine = 0;
        Contact toAdd;
        while ((line = in.readLine()) != null) {
            if (!pos.contains(numLine)) {
                toAdd = new Contact().readFromString(line); // Retrieve the contact from the line
                if (toAdd != null)
                    names.add(toAdd); // adding the contact to list
            }
            numLine++;
        }

        in.close();

        this.writeAllToFile(names,context);
    }

    public void update(Contact toUpdate, int pos, Context c) throws IOException {
        ArrayList<Contact> names = new ArrayList<>();

        InputStreamReader input = new InputStreamReader(c.openFileInput(Constants.CONTACTS_FILENAME));
        BufferedReader in = new BufferedReader(input);
        String line;
        int numLine = 0;
        Contact toAdd;
        while ((line = in.readLine()) != null) {
            if (numLine!=pos) {
                toAdd = new Contact().readFromString(line); // Retrieve the contact from the line
                if (toAdd != null)
                    names.add(toAdd); // adding the contact to list
            }
            else
                names.add(toUpdate);
            numLine++;
        }

        in.close();

        this.writeAllToFile(names,c);
    }
}
