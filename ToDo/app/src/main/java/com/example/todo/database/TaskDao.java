package com.example.todo.database;

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
    List<TaskEntry> loadAllTasks();
    @Insert
    void insertTask(TaskEntry taskEntry);
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateTask(TaskEntry taskEntry);
    @Delete
    void deleteTask(TaskEntry taskEntry);
    //Create a Query method named loadTaskById that receives an int id and returns a TaskEntry Object
    //    // The query for this method should get all the data for that id in the task table
    @Query("SELECT * FROM task WHERE id= :id")
    TaskEntry loadTaskById(int id);
}

