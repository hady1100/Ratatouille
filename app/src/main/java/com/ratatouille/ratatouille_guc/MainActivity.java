package com.ratatouille.ratatouille_guc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    MessagesList messagesList = null;
    MessagesListAdapter<Message> adapter = null;
    static String uuid = "";
    static Author author = null;
    static int msg_id = 0;

    public static Response getReq(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader is = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        Gson g = new Gson();
        String res = "";
        String cur = "";
        while((cur = is.readLine()) != null){
            res = res + cur;
        }
        return g.fromJson(res, Response.class);
    }
    public static void postReq(String message, String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        Gson g  = new Gson();
        Request req = new Request(message);
        String out = g.toJson(req);
        conn.getOutputStream().write(out.getBytes("UTF8"));
        conn.addRequestProperty("Content-Type", "application/json");
        conn.addRequestProperty("Authorization", uuid);

        conn.setRequestMethod("POST");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MessageInput send = (MessageInput) findViewById(R.id.send);
        messagesList = (MessagesList) findViewById(R.id.messagesList);

        Response res = null;

        try {
            res = getReq("https://ratatouille-guc.herokuapp.com/welcome");
        } catch (IOException e) {
            e.printStackTrace();
        }
        uuid = res.uuid;
        author = new Author(uuid, "newUser", null);

        adapter = new MessagesListAdapter<>(res.uuid, null);
        messagesList.setAdapter(adapter);

        send.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                //validate and send message
                Message message = new Message((msg_id++)+"", author, input.toString(), new Date());
                adapter.addToStart(message, true);
                try {
                    postReq(input.toString(), "https://ratatouille-guc.herokuapp.com/chat");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
    }



}
