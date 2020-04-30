package com.example.technobit.utilities.notSendedData;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FileGoogle {

    // append data into file
    public void writeToFile(GoogleData data, Context context) throws IOException {
        // get the output stream
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter
                (context.openFileOutput("notSent.txt", Context.MODE_APPEND));
        // write all string to file
        outputStreamWriter.write(data.toString()+"\n");
        outputStreamWriter.flush();
        outputStreamWriter.close();
    }


    // Delete file
    public void delete(Context context) throws IOException {
        context.deleteFile("notSent.txt");
    }

    // read the file if exists and return a list of all data to send
    protected ArrayList<GoogleData> readFile(Context context) throws IOException {
        ArrayList<GoogleData> data = new ArrayList<>();

        InputStreamReader input = new InputStreamReader(context.openFileInput("notSent.txt"));
        BufferedReader in = new BufferedReader(input);
        String line;
        GoogleData toAdd = new GoogleData();
        while ((line = in.readLine()) != null) {
            toAdd.readFromString(line); // Retrieve the data from the line
            data.add(toAdd); // adding the data to list
        }

        in.close();

        return data;
    }

    // Re-write writing the array list
    public void writeAllToFile(ArrayList<GoogleData> datas, Context context) throws IOException {
        // get the output stream
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter
                (context.openFileOutput("notSent.txt", Context.MODE_PRIVATE));
        // write all string to file
        for (GoogleData d:datas) {
            outputStreamWriter.write(d.toString()+"\n");
        }

        outputStreamWriter.flush();
        outputStreamWriter.close();
    }
}
