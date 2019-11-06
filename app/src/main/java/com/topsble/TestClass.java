package com.topsble;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestClass extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);
        stringtopdf("Hiii, how are you!!!");
    }

    public void stringtopdf(String data) {

        String extstoragedir = Environment.getExternalStorageDirectory().toString();
        //File fol = new File(extstoragedir, "pdf");
        //File folder = new File(fol, "pdf");
//        if (!folder.exists()) {
//            boolean bool = folder.mkdir();
//        }
        try {
            final File file = new File(extstoragedir, "test.pdf");
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);


            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new
                    PdfDocument.PageInfo.Builder(100, 100, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            canvas.drawText(data, 5, 5, paint);


            document.finishPage(page);
            document.writeTo(fOut);
            document.close();

        } catch (IOException e) {
            Log.i("error_____", e.getLocalizedMessage());
        }
    }
}
