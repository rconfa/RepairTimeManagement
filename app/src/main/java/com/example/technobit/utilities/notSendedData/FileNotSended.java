package com.example.technobit.utilities.notSendedData;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FileNotSended {

    // read the file if exists and return a list of all data to send
    protected ArrayList<DataToSend> readFile(Context context) throws IOException {
        ArrayList<DataToSend> data = new ArrayList<>();

        InputStreamReader input = new InputStreamReader(context.openFileInput("notSended.txt"));
        BufferedReader in = new BufferedReader(input);
        String line;
        DataToSend toAdd = new DataToSend();
        while ((line = in.readLine()) != null) {
            toAdd.readFromString(line); // Retrieve the data from the line
            data.add(toAdd); // adding the data to list
        }

        in.close();

        return data;
    }

    // append data into file
    protected void writeAllToFile(DataToSend data, Context context) throws IOException {
        // get the output stream
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter
                (context.openFileOutput("notSended.txt", Context.MODE_APPEND));
        // write all string to file
        outputStreamWriter.write(data.toString()+"\n");
        outputStreamWriter.flush();
        outputStreamWriter.close();
    }


    // Delete file
    protected void delete(Context context) throws IOException {
        context.deleteFile("notSended.txt");
    }
}
