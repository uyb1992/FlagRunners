package com.comsci436.flagrunners;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class JoinGroup extends AppCompatActivity {
    ListView listView;
    ArrayList<HashMap<String, String>> feedList = new ArrayList<HashMap<String, String>>();
    Firebase mFirebase = new Firebase("https://radiant-fire-7313.firebaseio.com/");
    String username = "";
    Group passed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Join Group");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        listView = (ListView) findViewById(R.id.listViewTwo);

        passed = (Group)getIntent().getSerializableExtra("passed");

        for(String temp : passed.getUserList()){
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("name", temp);
            feedList.add(map);
        }


        SimpleAdapter simpleAdapter = new SimpleAdapter(this, feedList, R.layout.view_group,
                new String[]{"name"},
                new int[]{R.id.textViewName}) {};
        listView.setAdapter(simpleAdapter);
    }

    public void cancel(View view){
        finish();
    }
    public void joinGroup(View view){
        if (passed.getPassword().equals("")) {
            join();
        }else{
            //If the room has password
            showDialog();
        }
    }

    public void join(){
        mFirebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                DataSnapshot n = snapshot.child("users").child(mFirebase.getAuth().getUid()).child("username");
                username = n.getValue().toString();

                for (DataSnapshot postSnapshot : snapshot.child("Groups").getChildren()) {
                    for (DataSnapshot post : postSnapshot.getChildren()) {
                        Group group = post.getValue(Group.class);
                        String curr = group.getCurrent_username();

                        if (curr.equals(passed.getCurrent_username())) {
                            HashSet<String> userList = passed.getUserList();
                            userList.add(username);

                            int open = passed.getOpen_spot() - 1;

                            post.getRef().child("userList").setValue(userList);
                            post.getRef().child("open_spot").setValue(open);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        Toast.makeText(JoinGroup.this, "Joined group", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("tcf_enabled", "yes");
        startActivity(intent);
    }

    public void showDialog(){
        LayoutInflater inflater = LayoutInflater.from(JoinGroup.this);
        View promptsView = inflater.inflate(R.layout.dialog_signin, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(JoinGroup.this);
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.groupPassword);

        alertDialogBuilder
                .setTitle("Enter password")
                .setMessage("Password required for this Room")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String input = userInput.getText().toString();

                        if (input.equals(passed.getPassword()))
                        {
                            join();
                        }else{
                            String message = "The password you have entered is incorrect." + " \n \n" + "Please try again!";
                            AlertDialog.Builder builder = new AlertDialog.Builder(JoinGroup.this);
                            builder.setTitle("Error");
                            builder.setMessage(message);
                            builder.setNegativeButton("Cancel", null);
                            builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    showDialog();
                                }
                            });
                            builder.create().show();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }
}


