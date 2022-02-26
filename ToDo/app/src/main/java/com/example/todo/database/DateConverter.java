package com.example.todo.database;

import androidx.room.Room;
import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {
    @TypeConverter
    //Room will use this method when written from he database
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
    //Receives the date object and converts it to a timestamp long
    //Room will use this method when writing into the data base
    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
