package com.example.todo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.example.todo.database.AppDatabase;
import com.example.todo.database.TaskEntry;

import java.util.Date;


public class AddTaskActivity extends AppCompatActivity {

        // Extra for the task ID to be received in the intent
        public static final String EXTRA_TASK_ID = "extraTaskId";
        // Extra for the task ID to be received after rotation
        public static final String INSTANCE_TASK_ID = "instanceTaskId";
        // Constants for priority
        public static final int PRIORITY_HIGH = 1;
        public static final int PRIORITY_MEDIUM = 2;
        public static final int PRIORITY_LOW = 3;
        // Constant for default task id to be used when not in update mode
        private static final int DEFAULT_TASK_ID = -1;
        // Constant for logging
        private static final String TAG = AddTaskActivity.class.getSimpleName();
        // Fields for views
        EditText mEditText;
        RadioGroup mRadioGroup;
        Button mButton;

        private int mTaskId = DEFAULT_TASK_ID;
        //  Create AppDatabase member variable for the Database
        // Member variable for the Database
        private AppDatabase mDb;

        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_task);

            initViews();
            //  Initialize member variable for the data base
            mDb = AppDatabase.getInstance(getApplicationContext());

            if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_TASK_ID)) {
                mTaskId = savedInstanceState.getInt(INSTANCE_TASK_ID, DEFAULT_TASK_ID);
            }

            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(EXTRA_TASK_ID)) {
                mButton.setText(R.string.update_button);
                Log.e("AddTaskActivity", "Task has been clicked to update");
                if (mTaskId == DEFAULT_TASK_ID) {
                    // populate the UI
                    //Assign the value of EXTRA_TASK_ID in the intent to mTaskId
                    // Use DEFAULT_TASK_ID as the default
                    mTaskId = intent.getIntExtra(EXTRA_TASK_ID, DEFAULT_TASK_ID);
                    // COMPLETED (4) Get the diskIO Executor from the instance of AppExecutors and
                    // call the diskIO execute method with a new Runnable and implement its run method
                    /*AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            // COMPLETED (5) Use the loadTaskById method to retrieve the task with id mTaskId and
                            // assign its value to a final TaskEntry variable
                            //wrapping the return type with LiveData
                            final LiveData<TaskEntry> task = mDb.taskDao().loadTaskById(mTaskId);
                            // COMPLETED (6) Call the populateUI method with the retrieve tasks
                            // Remember to wrap it in a call to runOnUiThread as we cannot populate UI on this thread
                            // We will be able to simplify this once we learn more
                            // about Android Architecture Components
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    populateUI(task);
                                }
                            });
                        }
                    });*/
                    //As LiveData run of the main thread we can delete executors
                    //wrapping the return type with LiveData
                    final LiveData<TaskEntry> task = mDb.taskDao().loadTaskById(mTaskId);
                    task.observe(this, new Observer<TaskEntry>() {
                        @Override
                        public void onChanged(TaskEntry taskEntry) {
                            //In this case we do not want to receive updates so we need to remove the observer
                            //from the LiveData object
                            task.removeObserver(this);
                            Log.e(TAG,"Receiving database update from LiveData");
                            populateUI(taskEntry);

                        }
                    });
                }
            }
        }

        @Override
        protected void onSaveInstanceState(Bundle outState) {
            outState.putInt(INSTANCE_TASK_ID, mTaskId);
            super.onSaveInstanceState(outState);
        }

        /**
         * initViews is called from onCreate to init the member variable views
         */
        private void initViews() {
            mEditText = findViewById(R.id.editTextTaskDescription);
            mRadioGroup = findViewById(R.id.radioGroup);

            mButton = findViewById(R.id.saveButton);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSaveButtonClicked();
                }
            });
        }

        /**
         * populateUI would be called to populate the UI when in update mode
         *
         * @param task the taskEntry to populate the UI
         */
        private void populateUI(TaskEntry task) {
            // COMPLETED (7) return if the task is null
            if (task == null) {
                return;
            }

            // COMPLETED (8) use the variable task to populate the UI
            mEditText.setText(task.getDescription());
            setPriorityInViews(task.getPriority());
        }



        /**
         * onSaveButtonClicked is called when the "save" button is clicked.
         * It retrieves user input and inserts that new task data into the underlying database.
         */
        public void onSaveButtonClicked() {
            //  Create a description variable and assign to it the value in the edit text
            String description = mEditText.getText().toString();
            //  Create a priority variable and assign the value returned by getPriorityFromViews()
            int priority = getPriorityFromViews();
            //  Create a date variable and assign to it the current Date
            Date date = new Date();

            //  Create taskEntry variable using the variables defined above
            //Make it final so it is visible inside the run method
           final TaskEntry task = new TaskEntry(description, priority, date);

            //Get the diskIo Executor from the instance of AppExecutors and call the
            //diskIO execute method with a new Runnable and implement its run method.
            //Use diskIO executor to execute a new runnable and that will contain our database logic
            //  Use the taskDao in the AppDatabase variable to insert the taskEntry
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    //Move the remaining logic inside the run method
                    // COMPLETED (9) insert the task only if mTaskId matches DEFAULT_TASK_ID
                    // Otherwise update it
                    // call finish in any case
//                    mDb.taskDao().insertTask(task);
//                    finish();
                    if (mTaskId == DEFAULT_TASK_ID) {
                        // insert new task
                        mDb.taskDao().insertTask(task);
                    } else {
                        //update task
                        task.setId(mTaskId);
                        mDb.taskDao().updateTask(task);
                    }
                    finish();
                }
            });
//            mDb.taskDao().insertTask(taskEntry);//This will add a record with the given data to our database
//            //  call finish() to come back to MainActivity
//            finish();//Return to the list of to do list i.e. MainActivity.
        }

        /**
         * getPriority is called whenever the selected priority needs to be retrieved
         */
        public int getPriorityFromViews() {
            int priority = 1;
            int checkedId = ((RadioGroup) findViewById(R.id.radioGroup)).getCheckedRadioButtonId();
            switch (checkedId) {
                case R.id.radButton1:
                    priority = PRIORITY_HIGH;
                    break;
                case R.id.radButton2:
                    priority = PRIORITY_MEDIUM;
                    break;
                case R.id.radButton3:
                    priority = PRIORITY_LOW;
            }
            return priority;
        }

        /**
         * setPriority is called when we receive a task from MainActivity
         *
         * @param priority the priority value
         */
        public void setPriorityInViews(int priority) {
            switch (priority) {
                case PRIORITY_HIGH:
                    ((RadioGroup) findViewById(R.id.radioGroup)).check(R.id.radButton1);
                    break;
                case PRIORITY_MEDIUM:
                    ((RadioGroup) findViewById(R.id.radioGroup)).check(R.id.radButton2);
                    break;
                case PRIORITY_LOW:
                    ((RadioGroup) findViewById(R.id.radioGroup)).check(R.id.radButton3);
            }
        }
    }
