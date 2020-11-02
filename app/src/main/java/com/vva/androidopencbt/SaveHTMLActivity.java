package com.vva.androidopencbt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

public class SaveHTMLActivity extends AppCompatActivity {

    private final String FILE_NAME = "CBT_diary.html";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_html);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void saveHTML(View v)
    {
        saveToHTML();
        sendFile();
    }

    public void sendFile()
    {
        File file = new File(this.getFilesDir(),FILE_NAME);
        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, file);
        Intent forSendIntent = new Intent(Intent.ACTION_SEND);
        forSendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        forSendIntent.putExtra(Intent.EXTRA_STREAM,uri);
        forSendIntent.setDataAndType(uri,"application/html");
       // Toast.makeText(this, uri.getPath(), Toast.LENGTH_SHORT).show();
        PackageManager pm = getPackageManager();
        if(forSendIntent.resolveActivity(pm)!=null)
        {
            startActivity(Intent.createChooser(forSendIntent,getString(R.string.savehtml_text_share)));
            finish();
        }
        else
        {
            Toast.makeText(this, getString(R.string.savehtml_error), Toast.LENGTH_SHORT).show();
        }
    }

    public void saveToHTML()
    {
        DateBaseAdapter adapter = new DateBaseAdapter(this);
        adapter.open();
        List<Record> records = adapter.getRecords(0);
        adapter.close();

        StringBuilder forHtml = new StringBuilder();
        forHtml.append("<!DOCTYPE html>");
        forHtml.append("<html><head><meta content='text/html; charset=utf-8'>");
        forHtml.append("<title>Дневник</title></head>");
        forHtml.append("<body>");
        forHtml.append("<table border='1' width='100%'>");

        //--- Заголовок
        forHtml.append("<tr>");
        forHtml.append("<th>Дата и время</th>");
        forHtml.append("<th>Ситуация</th>");
        forHtml.append("<th>Автоматические мысли</th>");
        forHtml.append("<th>Эмоции</th>");
        forHtml.append("<th>Диск. (в %)</th>");
        forHtml.append("<th>Телесные ощущения</th>");
        forHtml.append("<th>Предпринятые действия</th>");
        forHtml.append("<th>Когнитивные искажения</th>");
        forHtml.append("<th>Рациональный ответ</th>");
        forHtml.append("</tr>");
        //---
        //---заполнение таблицы строками
        for(int i=0;i<records.size();i++)
        {
            Record record = records.get(i);
            forHtml.append("<tr>");
            forHtml.append("<td>").append(record.getShortDateTimeString()).append("</td>");
            forHtml.append("<td>").append(record.getSituation()).append("</td>");
            forHtml.append("<td>").append(record.getThought()).append("</td>");
            forHtml.append("<td>").append(record.getEmotion()).append("</td>");
            forHtml.append("<td>").append(record.getIntensity()).append("</td>");
            forHtml.append("<td>").append(record.getFeelings()).append("</td>");
            forHtml.append("<td>").append(record.getActions()).append("</td>");
            forHtml.append("<td>").append(record.getDistortionsString(this)).append("</td>");
            forHtml.append("<td>").append(record.getRational()).append("</td>");
            forHtml.append("</tr>");
        }
        //--------------
        forHtml.append("</table>");
        forHtml.append("</body>");
        forHtml.append("</html>");
        String document = forHtml.toString();
        FileOutputStream fos;
        try {
           fos = openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
           fos.write(document.getBytes());
           fos.close();
        }
        catch (IOException e)
        {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }


}
