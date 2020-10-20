package com.vva.androidopencbt;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

public class RecordAdapter extends ArrayAdapter<Record>
{
    private List<Record> records;
    private LayoutInflater inflater;
    private int layout;
    private int format;


    public RecordAdapter(Context context, int resource, List<Record> records)
    {
        super(context, resource, records);
       // SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS_FILENAME, context.MODE_PRIVATE);
       // format = sharedPreferences.getInt("format",0);
        this.records = records;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView==null)
        {
            convertView = inflater.inflate(layout,parent, false);
        }
        Resources res = convertView.getResources();

        Record record = records.get(position);

        TextView dateTextView = convertView.findViewById(R.id.dateTextView);
        TextView emptyTextView = convertView.findViewById(R.id.emptyTextView);

        dateTextView.setText(record.getDateTimeString());

        TextView situationTextView = convertView.findViewById(R.id.situationTextView);
        TextView thoughtTextView = convertView.findViewById(R.id.thoughtTextView);
        TextView disputTextView = convertView.findViewById(R.id.rationalTextView);

        TextView emotionTextView = convertView.findViewById(R.id.emotionTextView);
        TextView feelingsTextView = convertView.findViewById(R.id.feelingsTextView);
        TextView actionsTextView = convertView.findViewById(R.id.actionsTextView);
        TextView distortionTextView = convertView.findViewById(R.id.distortionTextView);
        TextView intensityTextView = convertView.findViewById(R.id.intensityTextView);

        //---выведем список когнитивных искажений
        if(record.getDistortionsValue()==0x0)//не заполнена графа с искажениями
        {
            distortionTextView.setVisibility(View.GONE);
        }
        else
        {
            StringBuilder builder = new StringBuilder();
            if((record.getDistortionsValue()&Record.ALL_OR_NOTHING)!=0)
                builder.append(res.getString(R.string.dist_all_or_nothing)).append(", ");
            if((record.getDistortionsValue()&Record.OVERGENERALIZING)!=0)
                builder.append(res.getString(R.string.dist_overgeneralizing)).append(", ");
            if((record.getDistortionsValue()&Record.FILTERING)!=0)
                builder.append(res.getString(R.string.dist_filtering)).append(", ");
            if((record.getDistortionsValue()&Record.DISQUAL_POSITIVE)!=0)
                builder.append(res.getString(R.string.dist_disqual_positive) ).append(", ");
            if((record.getDistortionsValue()&Record.JUMP_CONCLUSION)!=0)
                builder.append(res.getString(R.string.dist_jump_conclusion)).append(", ");
            if((record.getDistortionsValue()&Record.MAGN_AND_MIN)!=0)
                builder.append(res.getString(R.string.dist_magn_and_min)).append(", ");
            if((record.getDistortionsValue()&Record.EMOTIONAL_REASONING)!=0)
                builder.append(res.getString(R.string.dist_emotional_reasoning)).append(", ");
            if((record.getDistortionsValue()&Record.MUST_STATEMENTS)!=0)
                builder.append(res.getString(R.string.dist_must_statement)).append(", ");
            if((record.getDistortionsValue()&Record.LABELING)!=0)
                builder.append(res.getString(R.string.dist_labeling)).append(", ");
            if((record.getDistortionsValue()&Record.PERSONALIZATION)!=0)
                builder.append(res.getString(R.string.dist_personalistion)).append(", ");;

            for(int i = 0;i < builder.length(); i++) //убираем заглавные буквы
            {
                if(Character.isAlphabetic(builder.codePointAt(i)))
                {
                    if(Character.isUpperCase(builder.charAt(i)))
                    {
                        builder.setCharAt(i,Character.toLowerCase(builder.charAt(i)));
                    }
                }
            }

            String result = builder.substring(0,builder.length()-2);
            distortionTextView.setVisibility(View.VISIBLE);
            distortionTextView.setText(res.getString(R.string.adapter_distortions, result));
        }

        if(record.getSituation().isEmpty()) {
            situationTextView.setVisibility(View.GONE);
        }
        else
        {
            situationTextView.setVisibility(View.VISIBLE);
            situationTextView.setText(res.getString(R.string.adapter_situation, record.getSituation()));
        }

        if(record.getIntensity()==0)
        {
            intensityTextView.setVisibility(View.GONE);
        }
        else
        {
            intensityTextView.setVisibility(View.VISIBLE);
            intensityTextView.setText(res.getString(R.string.adapter_intensity, record.getIntensity()));
        }

        if(record.getThought().isEmpty())
        {
            thoughtTextView.setVisibility(View.GONE);
        }
        else
        {
            thoughtTextView.setText(res.getString(R.string.adapter_thought, record.getThought()));
        }

        if(record.getEmotion().isEmpty())
        {
            emotionTextView.setVisibility(View.GONE);
        }
        else
        {
            emotionTextView.setVisibility(View.VISIBLE);
            emotionTextView.setText(res.getString(R.string.adapter_emotions,record.getEmotion()));
        }

        if(record.getFeelings().isEmpty())
        {
            feelingsTextView.setVisibility(View.GONE);
        }
        else
        {
            feelingsTextView.setVisibility(View.VISIBLE);
            feelingsTextView.setText(res.getString(R.string.adapter_feelsing,record.getFeelings()));
        }

        if(record.getActions().isEmpty())
        {
            actionsTextView.setVisibility(View.GONE);
        }
        else
        {
            actionsTextView.setVisibility(View.VISIBLE);
            actionsTextView.setText(res.getString(R.string.adapter_actions,record.getActions()));
        }

        if(record.getRational().isEmpty())
        {
            disputTextView.setVisibility(View.GONE);
        }
        else
        {
            disputTextView.setVisibility(View.VISIBLE);
            disputTextView.setText(res.getString(R.string.adapter_disput,record.getRational()));
        }

        if(record.isEmpty())
        {
            emptyTextView.setVisibility(View.VISIBLE);
        //    return convertView;
        }
        else
        {
            emptyTextView.setVisibility(View.GONE);
        }



        return convertView;
    }

}
