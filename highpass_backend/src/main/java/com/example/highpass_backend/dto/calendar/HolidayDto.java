package com.example.highpass_backend.dto.calendar;

public class HolidayDto {
    private String date;
    private String localName;

    public HolidayDto() {}
    public HolidayDto(String date, String localName) {
        this.date = date;
        this.localName = localName;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getLocalName() { return localName; }
    public void setLocalName(String localName) { this.localName = localName; }
}

