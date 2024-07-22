package com.example.todoapp;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> taskList;
    private final Context context;

    public TaskAdapter(List<Task> taskList, Context context) {
        this.taskList = taskList;
        this.context = context;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_layout, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskTextView.setText(task.getTaskText());
        holder.checkBox.setChecked(task.isChecked());

        // Set strikethrough if the task is checked
        if (task.isChecked()) {
            holder.taskTextView.setPaintFlags(holder.taskTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.taskTextView.setPaintFlags(holder.taskTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference()
                    .child("tasks")
                    .child(task.getUserId())
                    .child(task.getId());

            task.setChecked(isChecked);
            taskRef.setValue(task)
                    .addOnSuccessListener(aVoid -> {
                        if (isChecked) {
                            holder.taskTextView.setPaintFlags(holder.taskTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        } else {
                            holder.taskTextView.setPaintFlags(holder.taskTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                        }
                        Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TaskAdapter", "Failed to update task", e);
                        Toast.makeText(context, "Failed to update task", Toast.LENGTH_SHORT).show();
                    });
        });

        holder.editButton.setOnClickListener(v -> {
            // Inflate edit task dialog layout
            if (task.isChecked()){
                Toast.makeText(context, "Task cannot be changed", Toast.LENGTH_SHORT).show();

            }
            else{
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.dialog_edit_task, null);

                EditText editTextTask = dialogView.findViewById(R.id.editTextTask);
                editTextTask.setText(task.getTaskText());

                // Show alert dialog
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                dialogBuilder.setView(dialogView);
                dialogBuilder.setTitle("Edit Task");
                dialogBuilder.setPositiveButton("Update", (dialog, which) -> {
                    String updatedTaskText = editTextTask.getText().toString().trim();
                    if (!updatedTaskText.isEmpty()) {
                        DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference()
                                .child("tasks")
                                .child(task.getUserId())
                                .child(task.getId());

                        task.setTaskText(updatedTaskText);
                        taskRef.setValue(task)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("TaskAdapter", "Failed to update task", e);
                                    Toast.makeText(context, "Failed to update task", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(context, "Task cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                });
                dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();}
        });

        holder.deleteButton.setOnClickListener(v -> {
            DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference()
                    .child("tasks")
                    .child(task.getUserId())
                    .child(task.getId());

            taskRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TaskAdapter", "Failed to delete task", e);
                        Toast.makeText(context, "Failed to delete task", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        public CheckBox checkBox;
        public TextView taskTextView;
        public ImageButton editButton;
        public ImageButton deleteButton;

        public TaskViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.todoCheckBox);
            taskTextView = itemView.findViewById(R.id.taskTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
