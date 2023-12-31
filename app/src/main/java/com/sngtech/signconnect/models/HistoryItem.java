package com.sngtech.signconnect.models;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistoryItem {

    public enum SignType {
        LETTER("L"),
        WORD("W");

        final String label;

        SignType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    String result;
    String datetimeLearnt;
    SignType signType;
    String capturedPath;
    int facing;

    public HistoryItem(String result, String datetimeLearnt, SignType signType, String capturedPath) {
        this.result = result;
        this.datetimeLearnt = datetimeLearnt;
        this.signType = signType;
        this.capturedPath = capturedPath;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public HistoryItem(String result, SignType signType) {
        this.result = result;
        this.datetimeLearnt = getCurrentDateTime();
        this.signType = signType;
        this.capturedPath = "";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getCurrentDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, uuuu HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    public String getResult() {
        return result;
    }

    public String getDateTimeLearnt() {
        return datetimeLearnt;
    }

    public void setFacing(int facing) {
        this.facing = facing;
    }

    public int getFacing() {
        return facing;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public SignType getSignType() {
        return signType;
    }

    public String getCapturedPath() {
        return capturedPath;
    }

    public void setCapturedPath(String capturedPath) {
        this.capturedPath = capturedPath;
    }
}
