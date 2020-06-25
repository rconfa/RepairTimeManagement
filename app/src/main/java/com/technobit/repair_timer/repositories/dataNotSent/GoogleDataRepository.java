package com.technobit.repair_timer.repositories.dataNotSent;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;

// class that manages the interaction with the google data file
public class GoogleDataRepository {
    // write all data to the repository (file)
    public void writeAll(ArrayList<GoogleData> datas, Context context) throws IOException {
        new FileGoogle().writeAllToFile(datas, context);
    }

    // read all data from the repository (file)
    public ArrayList<GoogleData> getAll(Context mContext) throws IOException {
        return new FileGoogle().readFile(mContext);
    }
}
