package com.example.sunchat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity {
    String name;
    ListView listView;
    EditText editMessage;
    ImageButton buttonSendMessage;
    List<DataFromDB> db = new ArrayList<>();
    List<String> messages = new ArrayList<>();
    private Handler handler = new Handler();
    private Runnable repeatUpdateList;
    Retrofit retrofit;
    ChatAPI api;
    private int numMessages;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        listView = findViewById(R.id.listView);
        editMessage = findViewById(R.id.editMessage);
        buttonSendMessage = findViewById(R.id.sendButton);

        retrofit = new Retrofit.Builder()
                .baseUrl("https://sch120.ru")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ChatAPI.class);

        repeatUpdateList = new Runnable() {
            @Override
            public void run() {
                loadFromInternetDB();
                updateList();
                handler.postDelayed(this, 1000);
            }
        };

        handler.post(repeatUpdateList);
    }

    void updateList(){
        if(numMessages<db.size()){
            messages.clear();
            for(DataFromDB a:db) messages.add(a.name+"   "+a.created+"\n"+a.message);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, messages);
            listView.setAdapter(adapter);
            scrollDown();
            numMessages=db.size();
        }
    }

    public void sendMessage(View view) {
        if(editMessage.getText().toString().isEmpty()) return;
        sendToInternetDB(editMessage.getText().toString());
        editMessage.setText("");
    }

    public void sendToInternetDB(String message){
        Call<List<DataFromDB>> call = api.sendQuery(name, message);
        call.enqueue(new Callback<List<DataFromDB>>() {
            @Override
            public void onResponse(Call<List<DataFromDB>> call, Response<List<DataFromDB>> response) {
                db = response.body();
            }

            @Override
            public void onFailure(Call<List<DataFromDB>> call, Throwable t) {

            }
        });
    }

    public void loadFromInternetDB(){
        Call<List<DataFromDB>> call = api.sendQuery("ask");
        call.enqueue(new Callback<List<DataFromDB>>() {
            @Override
            public void onResponse(Call<List<DataFromDB>> call, Response<List<DataFromDB>> response) {
                db = response.body();
            }

            @Override
            public void onFailure(Call<List<DataFromDB>> call, Throwable t) {

            }
        });
    }

    void scrollDown(){
        int itemCount = listView.getAdapter().getCount();
        if (itemCount > 0) {
            listView.setSelection(itemCount - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(repeatUpdateList);
    }
}