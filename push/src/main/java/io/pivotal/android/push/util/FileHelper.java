package io.pivotal.android.push.util;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class FileHelper {

    final Context context;

    public FileHelper(Context context) {
        this.context = context;
    }

    public Reader getReader(String filename) throws FileNotFoundException {
        final File file = new File(context.getFilesDir(), filename);
        return new FileReader(file);
    }

    public Writer getWriter(String filename) throws IOException {
        final File file = new File(context.getFilesDir(), filename);
        return new FileWriter(file);
    }
}
