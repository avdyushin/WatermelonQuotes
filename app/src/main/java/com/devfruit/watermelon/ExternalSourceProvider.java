package com.devfruit.watermelon;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class QuoteSource {
    String title;
    String description;
    QuoteSource(String title, String description) {
        this.title = title;
        this.description = description;
    }
}

class ExternalSourceProvider {

    private static final String TAG = ExternalSourceProvider.class.getName();

    private static final String EXTERNAL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    static final String QUOTES_PATH = EXTERNAL_PATH + File.separator + "watermelon";

    static String filePathForSource(String source) {
        return QUOTES_PATH + File.separator + source;
    }

    static ArrayList<QuoteSource> userInstalledQuotes() {
        File dir = new File(QUOTES_PATH);
        ArrayList<QuoteSource> available = new ArrayList<>();
        if (dir.exists() && dir.isDirectory()) {
            File [] files = dir.listFiles();
            if (files.length > 0) {
                for (File f : files) {
                    if (f.canRead()) {
                        try {
                            BufferedReader bf = new BufferedReader(new FileReader(f));
                            String title = bf.readLine();
                            String description = bf.readLine();
                            if (!title.trim().isEmpty() && !description.trim().isEmpty()) {
                                available.add(new QuoteSource(title, description));
                            }
                        } catch (FileNotFoundException e) {
                            Log.w(TAG, "Can't find file: " + e);
                        } catch (IOException e) {
                            Log.w(TAG, "Can't read file: " + e);
                        }
                    }
                }
            }
        }
        return available;
    }
}
