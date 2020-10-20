package com.vva.androidopencbt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Record
{
    //---список когнитивных искажений
    public static Short ALL_OR_NOTHING = 0x1;
    public static Short OVERGENERALIZING = 0x2;
    public static Short FILTERING = 0x4;
    public static Short DISQUAL_POSITIVE = 0x8;
    public static Short JUMP_CONCLUSION = 0x10;
    public static Short MAGN_AND_MIN = 0x20;
    public static Short EMOTIONAL_REASONING = 0x40;
    public static Short MUST_STATEMENTS = 0x80;
    public static Short LABELING = 0x100;
    public static Short PERSONALIZATION = 0x200;
    //--------------------------------

    private long id;
    private String situation;
    private String thought;
    private String rational;
    private String emotion;
    private String feelings;
    private String actions;
    private Date dateTime;
    private short distortions;
    private short intensity;

    long getId() { return this.id; }

    short getIntensity() {return this.intensity;}
    void setIntensity(short value)
    {
        this.intensity = value;
    }

    String getSituation() { return this.situation; }
    void setSituation(String value) { this.situation = value; }

    String getEmotion() { return this.emotion; }
    void setEmotion(String value) { this.emotion = value; }

    String getThought() { return this.thought; }
    void setThought(String value)
    {
        this.thought = value;
    }

    String getRational() { return this.rational; }
    void setRational(String value)
    {
        this.rational = value;
    }


    String getActions() { return this.actions; }
    void setActions(String value) { this.actions = value; }

    String getFeelings() { return this.feelings; }
    void setFeelings(String value) { this.feelings = value; }

    Date getDateTime() {return this.dateTime;}
    void setDateTime(long msValue)
    {
        this.dateTime = new Date(msValue);
    }

    String getDateTimeString()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm dd MMMM yyyy", Locale.getDefault());
        return formatter.format(getDateTime());
    }

    short getDistortionsValue() { return this.distortions; }

    void setDistortionsValue(short value)
    {
        this.distortions = value;
    }

    Record(long id, String situation, String thoughts, String rational, String emotion, String feelings, String actions, short intensity, short distortions, long date)
    {
        this.id = id;
        setSituation(situation);
        setThought(thoughts);
        setRational(rational);
        setEmotion(emotion);
        setFeelings(feelings);
        setActions(actions);
        setIntensity(intensity);
        setDistortionsValue(distortions);
        setDateTime(date);
    }

    boolean isEmpty()
    {
        if(!getSituation().isEmpty()) return false;
        if(!getRational().isEmpty()) return false;
        if(!getEmotion().isEmpty()) return false;
        if(!getThought().isEmpty()) return false;
        if(!getActions().isEmpty()) return false;
        if(!getFeelings().isEmpty()) return false;
        if(distortions!=0) return false;
        if(intensity!=0) return false;
        return true;
    }
}
