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
import java.util.Arrays;
import java.util.Date;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    static final String help_message = "It seems like you are having trouble finding your ingredients.\nHere is how its done:\n" +
            "step 1: search for a recipe\n" +
            "step 2: click on the image or the title of the recipe that you desire.\n" +
            "ps: if you are stuck at any stage or want to start over fom any point, click the red \"X\" button or type \"cancel\"\n" +
            "If you are looking for suggestions, send an empty message";
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

        conn.setRequestMethod("GET");
        BufferedReader is = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String res = "";
        String cur = "";
        while((cur = is.readLine()) != null){
            String tmp = cur.replace(" ","");
            if(tmp.isEmpty())
                continue;
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
            String tmp = cur.replace(" ","");
            if(tmp.isEmpty())
                continue;
            res = res + cur;
        }
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
        final MessageInput send = (MessageInput) findViewById(R.id.send);
        Response res = null;

        try {
            res = getReq("https://ratatouille-guc.herokuapp.com/welcome");
            if(res.message.contains("Ratatouille")){
                res.message =  "type \"/help\" if you need help using the app\n\n" + res.message;
            }
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
        adapter.setOnMessageClickListener(new MessagesListAdapter.OnMessageClickListener<Message>() {
            @Override
            public void onMessageClick(Message message) {
                try {
                   Response res = postReq(message.recipe_id, "https://ratatouille-guc.herokuapp.com/chat");
                    if(!res.message.contains("Make sure you sent a correct recipe ID") && (res.message.length() >= 1)){
                        Message back = new Message((msg_id++) + "", me, res.message, new Date());
                        adapter.addToStart(back, true);
                        res = postReq(message.recipe_id, "https://ratatouille-guc.herokuapp.com/chat");
                        back = new Message((msg_id++) + "", me, res.message, new Date());
                        adapter.addToStart(back, true);
                    } else {
                        postReq("cancel", "https://ratatouille-guc.herokuapp.com/chat");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        this.messagesList.setAdapter(adapter);

        adapter.addToStart(new Message((msg_id++)+"", me, res.message,new Date()), true);

        send.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                String in = input.toString();
                try {
                    StringTokenizer st = new StringTokenizer(in);//new lines causes app to crash
                    in = st.nextToken();
                } catch (Exception e){
                    in = "";
                }
                Log.i(TAG, in);
                if(in.equals("/help")){
                    Message help = new Message((msg_id++) + "", me, help_message, new Date());
                    Message message = new Message((msg_id++) + "", author, in, new Date());
                    adapter.addToStart(message, true);
                    adapter.addToStart(help, true);
                } else {
                    //validate and send message
                    Message message = new Message((msg_id++) + "", author, in, new Date());

                    adapter.addToStart(message, true);
                    Response res = null;
                    try {
                        res = postReq(in.toString(), "https://ratatouille-guc.herokuapp.com/chat");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String[] parsed = res.message.split("<New>");
                    Message back = null;
                    if (!res.message.contains("<New>")) {
                        if ((res.message.length() >= 2)) {
                            if (res.message.contains("What do you feel like cooking today?")) {
                                send.setAttachmentsListener(new MessageInput.AttachmentsListener() {
                                    @Override
                                    public void onAddAttachments() {
                                        try {
                                            Response res = postReq("cancel", "https://ratatouille-guc.herokuapp.com/chat");
                                            Message message = new Message((msg_id++) + "", me, res.message, new Date());
                                            adapter.addToStart(message, true);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                            }
                            back = new Message((msg_id++) + "", me, res.message, new Date());
                            adapter.addToStart(back, true);
                        } else {
                            try {
                                postReq("cancel", "https://ratatouille-guc.herokuapp.com/chat");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            back = new Message((msg_id++) + "", me, "No results found.\nPlease try again and make sure the ingredients are valid!", new Date());
                            adapter.addToStart(back, true);
                        }
                    } else {
                        for (int i = 0; i < parsed.length; i++) {
                            if (i == 5)
                                break;
                            String[] param = parsed[i].split("#");
                            //Create messages with the parameters
                            Log.i(TAG, Arrays.toString(param));
                            back = new Message((msg_id++) + "", me, param[0], new Date(), param[1], param[2]);
                            adapter.addToStart(back, true);
                            back = new Message((msg_id++) + "", me, param[0], new Date(), param[2]);
                            adapter.addToStart(back, true);
                        }
                    }
                    return true;
                }
                return true;
            }
        });
    }



}
