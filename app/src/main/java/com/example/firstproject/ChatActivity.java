package com.example.firstproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private final String CHAT_URL="https://diorama-chat.ew.r.appspot.com/story";
    private String content;
    private LinearLayout chatContainer;

    private List<User> users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatContainer=findViewById(R.id.chat_container);
        new Thread(this::loadUrl).start();
    }

    private void loadUrl(){
        try(InputStream urlStream=new URL(CHAT_URL).openStream()){
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int len;
            while( ( len = urlStream.read( chunk ) ) != -1 ) {
                bytes.write( chunk, 0, len ) ;
            }
            content = new String( bytes.toByteArray(), StandardCharsets.UTF_8 ) ;
            bytes.close() ;
            runOnUiThread(this::parseContect);
        }
        catch (android.os.NetworkOnMainThreadException ignored){
            Log.d("loadUrl","NetworkOnMainThreadException");
        }
        catch (MalformedURLException ex){
            Log.d("loadUrl","MalformedURLException"+ex.getMessage());
        }
        catch (IOException ex){
            Log.d("loadUrl","IOException"+ex.getMessage());
        }
    }
    private void parseContect(){
        try{
            JSONObject array=new JSONObject(content);
            users=new ArrayList<>();

            if(array.getString("status").contains("success")){
                JSONArray tmp=array.getJSONArray("data");
                int len=tmp.length();
                for(int i=0;i<len;++i){
                    users.add(new User(tmp.getJSONObject(i)));

                }
            }
            runOnUiThread(this::showChatMessage);
        }
        catch (JSONException ex){
            Log.d("parseContent","JSONException"+ex.getMessage());
        }

    }
    private void showChatMessage(){
        TextView tvMassege=new TextView(this);
        StringBuilder sb=new StringBuilder();
        for (User user:users
             ) {
            sb.append(user.getDate());
            sb.append(":");
            sb.append(user.getAuthor());
            sb.append("-");
            sb.append(user.getText());
            sb.append("\n");
        }
        content=sb.toString();
        tvMassege.setText(content);
        chatContainer.addView(tvMassege);
    }

    static class User{
        private String id;
        private String author;
        private String text;
        private String date;

        public User(JSONObject obj)throws JSONException{
            setId(obj.getString("id"));
            setAuthor(obj.getString("author"));
            setText(obj.getString("txt"));
            setDate(obj.getString("moment"));
        }


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }
}