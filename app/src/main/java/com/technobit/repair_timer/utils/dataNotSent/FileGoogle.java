package com.technobit.repair_timer.utils.dataNotSent;

import android.content.Context;

import com.technobit.repair_timer.utils.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FileGoogle {
    private static final String filename = Constants.EVENT_NOT_SENT_FILENAME;

    // append data into file
    protected void writeToFile(GoogleData data, Context context) throws IOException {
        // get the output stream
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter
                (context.openFileOutput(filename, Context.MODE_APPEND));
        // write all string to file
        outputStreamWriter.write(data.toString()+"\n");
        outputStreamWriter.flush();
        outputStreamWriter.close();
    }


    // Delete file
    protected void delete(Context context) {
        context.deleteFile(filename);
    }

    // read the file if exists and return a list of all data to send
    protected ArrayList<GoogleData> readFile(Context context) throws IOException {
        ArrayList<GoogleData> data = new ArrayList<>();

        File f = new File(context.getFilesDir() + "/" + filename);
        if(f.exists()) {
            InputStreamReader input = new InputStreamReader(context.openFileInput(filename));
            BufferedReader in = new BufferedReader(input);
            String line;
            GoogleData toAdd = new GoogleData();
            while ((line = in.readLine()) != null) {
                toAdd.readFromString(line); // Retrieve the data from the line
                data.add(toAdd); // adding the data to list
            }

            in.close();
        }
        return data;
    }

    // Re-write writing the array list
    protected void writeAllToFile(ArrayList<GoogleData> datas, Context context) throws IOException {
        // get the output stream
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter
                (context.openFileOutput(filename, Context.MODE_PRIVATE));
        // write all string to file
        for (GoogleData d:datas) {
            outputStreamWriter.write(d.toString()+"\n");
        }

        outputStreamWriter.flush();
        outputStreamWriter.close();
    }
}
