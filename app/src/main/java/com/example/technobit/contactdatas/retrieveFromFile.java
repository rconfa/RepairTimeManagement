package com.example.technobit.contactdatas;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class retrieveFromFile {

    // legge un file txt e riporta tutti i dati come array
    public ArrayList<singleContact> readFile(Context context) throws IOException {
        ArrayList<singleContact> names = new ArrayList<singleContact>();

        int i = 0;
        InputStreamReader input = new InputStreamReader(context.openFileInput("clienti.txt"));
        BufferedReader in = new BufferedReader(input);
        String line;
        singleContact toAdd;
        while ((line = in.readLine()) != null) {
            toAdd = new singleContact().readFromString(line); // Retrieve the contact from the line
            names.add(toAdd); // adding the contact to list
        }

        in.close();

        return names;
    }


    public void writeAllToFile(ArrayList<singleContact> datas, Context context) throws IOException {
        // scrivo sul file
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("clienti.txt", Context.MODE_PRIVATE));
        // scrivo tutte le stringhe
        for (singleContact s:datas) {
            outputStreamWriter.write(s.toString()+"\n");
        }
        outputStreamWriter.close();
    }

    public void writeToFile(singleContact data, Context context) throws IOException {

        // scrivo sul file
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("clienti.txt", Context.MODE_APPEND));
       // Write the data on file using toString method
        outputStreamWriter.write(data.toString()+"\n");

        outputStreamWriter.close();
    }

    // leggo tutte le righe del file tranne la riga "pos", le salvo in un vettore, poi riscrivo tutto
    // cosi ho eliminato la riga pos
    public void delete(int pos,Context context) throws IOException {
        ArrayList<singleContact> names = new ArrayList<singleContact>();

        InputStreamReader input = new InputStreamReader(context.openFileInput("clienti.txt"));
        BufferedReader in = new BufferedReader(input);
        String line;
        int numLine = 0;
        singleContact toAdd;
        while ((line = in.readLine()) != null) {
            if (pos!=numLine) {
                toAdd = new singleContact().readFromString(line); // Retrieve the contact from the line
                names.add(toAdd); // adding the contact to list
            }
            numLine++;
        }

        in.close();

        this.writeAllToFile(names,context);
    }
}
