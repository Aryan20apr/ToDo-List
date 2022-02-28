package com.example.todo;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.todo.database.AppDatabase;
import com.example.todo.database.TaskEntry;

import java.util.List;


public class MainViewModel extends AndroidViewModel {
    // Constant for logging
    private static final String TAG = MainViewModel.class.getSimpleName();
    //We will use this ViewModel to cache our list of task entry objects wrap
//in a live data object.
//This variable will be private and we will have a public getter.
    private LiveData<List<TaskEntry>> tasks;
    public MainViewModel(@NonNull Application application) {
        super(application);
        //Get instance of the database and call loadAllTasks method of TaskDao
        AppDatabase database=AppDatabase.getInstance(this.getApplication());
        Log.d(TAG,"Actively retrieving the tasks from the database");
        tasks=database.taskDao().loadAllTasks();

    }
    public LiveData<List<TaskEntry>> getTasks()
    {
        return tasks;
    }
}
