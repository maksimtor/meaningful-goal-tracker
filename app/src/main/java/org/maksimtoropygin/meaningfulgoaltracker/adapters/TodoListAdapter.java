package org.maksimtoropygin.meaningfulgoaltracker.adapters;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.maksimtoropygin.meaningfulgoaltracker.R;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.Todo;
import org.maksimtoropygin.meaningfulgoaltracker.ui.todo_list.ToDoAddEdit;

import java.util.ArrayList;
import java.util.Objects;


public class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.ContactHolder> {

    private ArrayList<Todo> todoList;
    private Context mContext;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public TodoListAdapter(ArrayList<Todo> todoList, Context context) {
        this.todoList = todoList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.recycler_todo, parent, false);
        return new ContactHolder(view);
    }

    @Override
    public int getItemCount() {
        return todoList == null? 0: todoList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactHolder holder, final int position) {
        final Todo todo = todoList.get(position);

        holder.setTodoTitle(todo.getTitle());
        if (todo.getStatus().equals("resolved"))
            holder.todoStatus.setVisibility(View.GONE);
        db.collection("goals").addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                assert queryDocumentSnapshots != null;
                for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                    if (document.getId().equals(todo.getGoalId())){
                        holder.setTodoGoal(Objects.requireNonNull(document.getData().get("title")).toString());
                    }
                }
            }
        });

        holder.todoDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference todoToDelete = FirebaseFirestore.getInstance().collection("todos").document(todoList.get(position).getId());
                todoToDelete
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("FirebaseTest2", "DocumentSnapshot successfully deleted!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("FirebaseTest2", "Error deleted document", e);
                            }
                        });
            }
        });
        holder.todoEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ToDoAddEdit.class);
                intent.putExtra("action", "edit");
                intent.putExtra("title", todoList.get(position).getTitle());
                intent.putExtra("goalId", todoList.get(position).getGoalId());
                intent.putExtra("id", todoList.get(position).getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
            }
        });
        holder.todoStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DocumentReference todoDocument = db.collection("todos").document(todoList.get(position).getId());
                todoDocument.update("status", "resolved");
            }
        });
        holder.expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.forExpand.getVisibility() == View.GONE){
                    holder.forExpand.setVisibility(View.VISIBLE);
                    holder.expandButton.setImageDrawable(mContext.getDrawable(R.drawable.expand_less));
                }
                else {
                    holder.forExpand.setVisibility(View.GONE);
                    holder.expandButton.setImageDrawable(mContext.getDrawable(R.drawable.expand_more));
                }
            }
        });

    }

    public static class ContactHolder extends RecyclerView.ViewHolder {

        private TextView todoTitle;
        private TextView todoGoal;
        private ImageButton todoStatus;
        private ImageButton todoDelete;
        private ImageButton todoEdit;
        private ImageButton expandButton;
        private LinearLayout forExpand;

        public ContactHolder(View itemView) {
            super(itemView);

            todoTitle = itemView.findViewById(R.id.rec_todo_title);
            todoGoal = itemView.findViewById(R.id.rec_todo_goal);
            todoDelete = itemView.findViewById(R.id.rec_todo_delete);
            todoEdit = itemView.findViewById(R.id.rec_todo_edit);
            todoStatus = itemView.findViewById(R.id.rec_todo_status);
            expandButton = itemView.findViewById(R.id.rec_todo_expand);
            forExpand = itemView.findViewById(R.id.todo_for_expand);
        }

        public void setTodoTitle(String title) {
            todoTitle.setText(title);
        }

        public void setTodoGoal(String goal) {
            todoGoal.setText(goal);
        }
    }
}