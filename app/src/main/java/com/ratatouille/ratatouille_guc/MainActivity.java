package com.ratatouille.ratatouille_guc;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.ratatouille.ratatouille_guc.Utils.CustomIncomingTextMessageViewHolder;
import com.ratatouille.ratatouille_guc.Utils.CustomIncomingImageMessageViewHolder;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private MessagesList messagesList;
    protected ImageLoader imageLoader;
    private static final String TAG = "HADY";
    static MessagesListAdapter<Message> adapter = null;
    static String uuid = "";
    static Author author = null;
    static int msg_id = 0;
    static Author me = new Author("1", "ratatouille", null);

    public static Response getReq(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(7000);
        conn.setRequestMethod("GET");

//        Log.i(TAG, conn.getPermission().toString());
        conn.setRequestMethod("GET");
        BufferedReader is = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String res = "";
        String cur = "";
        while((cur = is.readLine()) != null){
            res = res + cur;
        }
        Gson g = new Gson();
        System.out.println(res);
        return g.fromJson(res, Response.class);
    }
    public static Response postReq(String message, String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        Gson g  = new Gson();
        Request req = new Request(message);
        String out = g.toJson(req);
        conn.setRequestMethod("POST");
        conn.addRequestProperty("Content-Type", "application/json");
        conn.addRequestProperty("Authorization", uuid);
        conn.getOutputStream().write(out.getBytes("UTF8"));

        BufferedReader is = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String res = "";
        String cur = "";
        while((cur = is.readLine()) != null){
            res = res + cur;
        }

        System.out.println(res);
        return g.fromJson(res, Response.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Picasso.with(MainActivity.this).load(url).into(imageView);
            }
        };

        this.messagesList = (MessagesList) findViewById(R.id.messagesList);
        MessageInput send = (MessageInput) findViewById(R.id.send);
        Response res = null;

        try {
            res = getReq("https://ratatouille-guc.herokuapp.com/welcome");
        } catch (IOException e) {
            e.printStackTrace();
        }
        uuid = res.uuid;
        author = new Author(uuid, "newUser", null);

        //initialize and link adapter
        MessageHolders holders = new MessageHolders();
        holders.setIncomingImageConfig(CustomIncomingImageMessageViewHolder.class, R.layout.item_custom_incoming_image_message);
        holders.setIncomingTextConfig(CustomIncomingTextMessageViewHolder.class, R.layout.item_custom_incoming_text);
        adapter = new MessagesListAdapter<>(uuid, holders,imageLoader);
        adapter.disableSelectionMode();

        this.messagesList.setAdapter(adapter);

        adapter.addToStart(new Message((msg_id++)+"", me, res.message,new Date()), true);

        send.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                //validate and send message
                Message message = new Message((msg_id++)+"", author, input.toString(), new Date());

                adapter.addToStart(message, true);
                Response res = null;
                try {
                    res = postReq(input.toString(), "https://ratatouille-guc.herokuapp.com/chat");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Message back = new Message((msg_id++)+"", me, res.message, new Date(), "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a1/Mallard2.jpg/1200px-Mallard2.jpg");
                Log.i(TAG, back.imageUrl);
                adapter.addToStart(back, true);
                return true;
            }
        });
    }



}
