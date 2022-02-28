package com.example.todo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Database;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.todo.database.AppDatabase;
import com.example.todo.database.TaskEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.ItemClickListener {
    // Constant for logging
    private static final String TAG = MainActivity.class.getSimpleName();
    // Member variables for the adapter and RecyclerView
    private RecyclerView mRecyclerView;
    private TaskAdapter mAdapter;
    //Create AppDatabase member variable for the database
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set the RecyclerView to its corresponding view
        mRecyclerView = findViewById(R.id.recyclerViewTasks);
        //Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new TaskAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);
        Log.d("MainActivity", "In onCreate() of MainActivity");
        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(decoration);
                /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            //We may want to use the swipe direction ti implement different actions
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete
                //Get the diskIO Executor from the instance of AppExecutors and call the diskIO execute method with
                //with a new Runnable and implement its run method
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                    //Get the position from the ViewHolder parameter
                        int position=viewHolder.getAdapterPosition();//Returns the Adapter position of the item represented by this ViewHolder.
                        List<TaskEntry> tasks=mAdapter.getTasks();
                        // COMPLETED (4) Call deleteTask in the taskDao with the task at that position
                        mDb.taskDao().deleteTask((tasks.get(position)));
                        //Call retrieveTasks method to refresh th UI
                        //For that we will need to load that task from the db again exactly as done by the onResume method
                        /**Every change in our database will trigger onChanged method of the observer
                        *Therefore we will not need to call the retrieveTask method after deleting a task.*/
                       // retrieveTasks();
                    }
                });
            }

        }).attachToRecyclerView(mRecyclerView);

        /*
         Set the Floating Action Button (FAB) to its corresponding View.
         Attach an OnClickListener to it, so that when it's clicked, a new intent will be created
         to launch the AddTaskActivity.
         */
        FloatingActionButton fabButton = findViewById(R.id.fab);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a new intent to start an AddTaskActivity
                Intent addTaskIntent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivity(addTaskIntent);
            }
        });
        //Initialize member variable for the database
        mDb = AppDatabase.getInstance(getApplicationContext());

        // COMPLETED (7) Call retrieveTasks from here and remove the onResume method
        retrieveTasks();
    }

    /**
     * This method is called after this activity has been paused or restarted.
     * Often, this is after new data has been inserted through an AddTaskActivity,
     * so this re-queries the database data for any changes.
     */
   /* @Override
    protected void onResume() {
        //Call the Adapter's setTasks method using the result of the loadAllTasks method from the taskDao
        super.onResume();
        //The code inside this method was inside the onResume method. Therefore we extracted the code to a new method
        //USing Refractor-->Extract Method option in right click menu
        retrieveTasks();


        //Call the Adapter's setTasks method using the result of the loadAllTasks method from the taskDao--Used before using executors
    //mAdapter.setTasks(mDb.taskDao().loadAllTasks());
}*/

    private void retrieveTasks() {
//        //Execute a new runnable that will query the database to retrieve a list of task entry objects
//        AppExecutors.getInstance().diskIO().execute(new Runnable() {
//            @Override
//            public void run() {
//
//                // COMPLETED (6) Move the logic into the run method and
//                // Extract the list of tasks to a final variable
//                //wrapping the return type with LiveData
//                final LiveData<List<TaskEntry>> tasks = mDb.taskDao().loadAllTasks();
//                //Passing list to the adapter via the settasks method cannot be done from the thread
//                //in our diskIO executor.Therefore we need to wrap it inside a run on UI thread method call.
//
//                // COMPLETED (7) Wrap the setTask call in a call to runOnUiThread
//                // We will be able to simplify this once we learn more
//                // about Android Architecture Components
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mAdapter.setTasks(tasks);
//                    }
//                });
//            }
//        });
        Log.d(TAG,"Actively retrieving the tasks from the Database ");
        //wrapping the return type with LiveData
        // COMPLETED (4) Extract all this logic outside the Executor and remove the Executor
        // COMPLETED (3) Fix compile issue by wrapping the return type with LiveData
        final LiveData<List<TaskEntry>> tasks = mDb.taskDao().loadAllTasks();
        //Calling observe method of tasks which is of type LiveData
        tasks.observe(this, new Observer<List<TaskEntry>>() {
            @Override
            public void onChanged(List<TaskEntry> taskEntries)//taskEntries is same thing we are wrapping in our LiveData object
             {
                 Log.d(TAG,"Receiving database update from LiveData");
                //This method can access the views so we can use it for the logic that we currently have on the run on UI thread method which we can delete now.(commented below)
                 mAdapter.setTasks(taskEntries);//Update the adapter
                 //We are getting the LiveData object and call its observe method.
                 //This happens out of the main thread by default as we are using LiveData, so we do not need executor
                 //We have the logic to update the Ui in the onChanged method of the observer which
                 // runs on the main thread by default
            }
        });//Lifecycle owner is something that has a lifecycle.In our case it is the activity
//        // COMPLETED (7) Wrap the setTask call in a call to runOnUiThread
////                // We will be able to simplify this once we learn more
////                // about Android Architecture Components
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mAdapter.setTasks(tasks);
//                    }
//                });
    }
    //For updating the tasks
    //Put item id in the intent and query the database to get that task once receive AddTaskActivity
    @Override
    public void onItemClickListener(int itemId) {
        // Launch AddTaskActivity adding the itemId as an extra in the intent
        //Launch AddTaskActivity with itemId as extra for the key AddTaskActivity.EXTRA_TASK_ID
        Intent intent=new Intent(MainActivity.this,AddTaskActivity.class);
        intent.putExtra(AddTaskActivity.EXTRA_TASK_ID,itemId);
        startActivity(intent);
    }

    }

