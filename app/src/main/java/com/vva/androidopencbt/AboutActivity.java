package com.vva.androidopencbt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView versionTextView = findViewById(R.id.versionTextView);
        versionTextView.setText(getString(R.string.app_version,BuildConfig.VERSION_NAME, BuildConfig.BUILD_TYPE));
    }

    public void sendFeedBack(View v)
    {
        Intent fbIntent = new Intent(Intent.ACTION_SENDTO);
        fbIntent.setData(Uri.parse("mailto:"));
        fbIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"androidopencbt@yandex.ru"});
        fbIntent.putExtra(Intent.EXTRA_SUBJECT, "Android OpenCBT");
        if(fbIntent.resolveActivity(getPackageManager())!=null)
        {
            startActivity(fbIntent);
        }
    }

    public void openGitHub(View v)
    {
        Toast.makeText(this, "Скоро эта кнопка будет открывать страницу программы на GitHub", Toast.LENGTH_SHORT).show();
    }

    public void getCBTInfo(View v)
    {
        Toast.makeText(this, "Скоро эта кнопка будет открывать страницу со списком литературы по КПТ", Toast.LENGTH_SHORT).show();
    }
}
