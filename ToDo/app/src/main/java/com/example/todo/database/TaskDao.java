package com.example.todo.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM task ORDER BY priority")
    LiveData <List<TaskEntry>>loadAllTasks();
    //Currently above method is returning a list of taskEntry objects
    //Everytime that we know there has been a change in the database, we need to call this method to query our DB again.
    //It is much  more efficient to notify any change in the databse.
    //Therefore we return our object as LiveData
    @Insert
    void insertTask(TaskEntry taskEntry);
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateTask(TaskEntry taskEntry);
    @Delete
    void deleteTask(TaskEntry taskEntry);
    //Create a Query method named loadTaskById that receives an int id and returns a TaskEntry Object
    //    // The query for this method should get all the data for that id in the task table
    @Query("SELECT * FROM task WHERE id= :id")
    LiveData<TaskEntry> loadTaskById(int id);
}

