package com.vva.androidopencbt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.vva.androidopencbt.db.DbRecord;
import com.vva.androidopencbt.recordslist.RvFragment;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    ListView listView;
    TextView welcomeTextView;
    RecordAdapter recordAdapter;
    boolean activity_flag = false;

    RecordsViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView = findViewById(R.id.listView);
        welcomeTextView = findViewById(R.id.welcomeTextView);

//        listView.setOnItemLongClickListener(listener);
        vm = new ViewModelProvider(this).get(RecordsViewModel.class);
        vm.getAllRecords().observe(this, dbRecords -> {
            if (!dbRecords.isEmpty()) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new RvFragment())
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new WelcomeFragment())
                        .commit();
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
//        showRecords();
    }

    ListView.OnItemLongClickListener listener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            Record record = recordAdapter.getItem(position);
            if(record!=null)
            {
                activity_flag = true;
                Intent newRecordIntent = new Intent(MainActivity.this,NewRecordActivity.class);
                newRecordIntent.putExtra("ID",record.getId());
                startActivity(newRecordIntent);
            }
            return true;
        }

    };

    public void showRecords()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int ordering;
        if (prefs.getBoolean("desc_ordering",false))
            ordering=DateBaseHelper.ORDER_DESC;
        else
            ordering=DateBaseHelper.ORDER_ASC;

        DateBaseAdapter adapter = new DateBaseAdapter(this);
        adapter.open();
        List<Record> records = adapter.getRecords(ordering);
        adapter.close();

        if(records.isEmpty())
        {
            welcomeTextView.setVisibility(View.VISIBLE);
        }
        else
        {
            welcomeTextView.setVisibility(View.INVISIBLE);
        }

        recordAdapter = new RecordAdapter(this,R.layout.list_item,records);
        listView.setAdapter(recordAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about)
        {
            activity_flag = true;
            Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        }

        if (id == R.id.action_html)
        {
            activity_flag = true;
            Intent pdfIntent = new Intent(MainActivity.this, SaveHTMLActivity.class);
            startActivity(pdfIntent);
            return true;
        }

        if(id == R.id.action_settings)
        {
            activity_flag = true;
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        if(id == R.id.action_newrecord)
        {
            activity_flag = true;
            Intent newRecordIntent = new Intent(MainActivity.this,NewRecordActivity.class);
            startActivity(newRecordIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    //-----

    public void addNewRecord(View v)
    {
        activity_flag = true;
        Intent newRecordIntent = new Intent(MainActivity.this,NewRecordActivity.class);
        startActivity(newRecordIntent);
    }

    @Override
    protected void onUserLeaveHint()
    {
        if(activity_flag)
        {
            activity_flag = false;
        }
        else
        {
            finish();
        }
        super.onUserLeaveHint();
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }
}
