package com.vva.androidopencbt;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.vva.androidopencbt.recordslist.RvFragment;
import com.vva.androidopencbt.recordslist.RvFragmentDirections;
import com.vva.androidopencbt.statistic.StatisticFragment;
import com.vva.androidopencbt.statistic.StatisticViewModel;


public class MainActivity extends AppCompatActivity {
    RecordsViewModel vm;
    StatisticViewModel sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vm = new ViewModelProvider(this).get(RecordsViewModel.class);

        vm.getNewRecordNavigated().observe(this, aLong -> {
            Intent newRecordIntent = new Intent(MainActivity.this, NewRecordActivity.class);
            newRecordIntent.putExtra("ID", aLong);
            startActivity(newRecordIntent);
        });

        sm = new ViewModelProvider(this).get(StatisticViewModel.class);
    }

    public void addNewRecord(View v) {
        Intent newRecordIntent = new Intent(MainActivity.this,NewRecordActivity.class);
        startActivity(newRecordIntent);
    }
}
