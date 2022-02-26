package com.example.todo.database;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {TaskEntry.class},version=1,exportSchema=false)//Increment the version when we need to update our database
@TypeConverters(DateConverter.class)//Room wil know how to deal with the date
public abstract class AppDatabase extends RoomDatabase {
    private static final String LOG_TAG=AppDatabase.class.getSimpleName();
    private static final Object LOCK=new Object();
    private static final String DATABASE_NAME="todolist";
    private static AppDatabase sInstance;
    public static AppDatabase getInstance(Context Context)
    {
        if(sInstance==null)
        {
            synchronized(LOCK)
            {
                Log.d(LOG_TAG,"Creating new database instance");
                sInstance= Room.databaseBuilder( Context.getApplicationContext(),AppDatabase.class,AppDatabase.DATABASE_NAME)
//                // COMPLETED (2) call allowMainThreadQueries before building the instance
//                // Queries should be done in a separate thread to avoid locking the UI
//                // We will allow this ONLY TEMPORALLY to see that our DB is working
                 .allowMainThreadQueries()
                        .build();
            }
        }
        Log.d(LOG_TAG,"Getting the database instance");
        return sInstance;
    }
    //To add the task Dao include abstract method that returns it
    //The class that is annotated with Database must have an abstract
    // method that has 0 arguments and returns the class that is
    // annotated with Dao. While generating the code at compile time,
    // Room will generate an implementation of this class.
    public abstract TaskDao taskDao();
}
