package com.team16_2.temporun;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.O)
public class RunDetail implements Parcelable {
    private String date;
    private String distance;
    private String speed;
    private String steps;
    private String startTime;
    private String endTime;

    public RunDetail() {}

    public RunDetail(String date, String distance, String speed, String steps, String startTime, String endTime) {

        this.date = date;
        this.distance = distance;
        this.speed = speed;
        this.steps = steps;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public RunDetail(Parcel in) {
        date = in.readString();
        distance = in.readString();
        speed = in.readString();
        steps = in.readString();
        startTime = in.readString();
        endTime = in.readString();
    }

    public static final Creator<RunDetail> CREATOR = new Creator<RunDetail>() {
        @Override
        public RunDetail createFromParcel(Parcel in) {
            return new RunDetail(in);
        }

        @Override
        public RunDetail[] newArray(int size) {
            return new RunDetail[size];
        }
    };

    public LocalDateTime getDate() {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH));
    }

    public int getDistance() {
        return Integer.parseInt(distance);
    }

    public int getSpeed() {
        return Integer.parseInt(speed);
    }

    public int getSteps() {
        return Integer.parseInt(steps);
    }

    public LocalDateTime getStartTime() {
        return LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH));
    }

    public LocalDateTime getEndTime() {
        return LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeString(distance);
        dest.writeString(speed);
        dest.writeString(steps);
        dest.writeString(startTime);
        dest.writeString(endTime);
    }
}
