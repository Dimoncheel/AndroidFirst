package com.example.firstproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    private final String CHAT_URL="https://diorama-chat.ew.r.appspot.com/story";
    private String content;

    private ChatMessage userMessage;
    private LinearLayout chatContainer;
    private List<ChatMessage> msgs;

    private EditText etUserName;
    private EditText etUserMassege;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatContainer=findViewById(R.id.chat_container);
        etUserName=findViewById(R.id.et_chat_user_name);
        etUserMassege=findViewById(R.id.et_chat_message);
        findViewById(R.id.btn_chat_send).setOnClickListener(this::sendBtnClick);
        new Thread(this::loadUrl).start();
    }

    private void sendBtnClick(View v){
        String author=etUserName.getText().toString();
        if(author.isEmpty()){
            Toast.makeText(this,"Enter author name",Toast.LENGTH_SHORT).show();
            etUserName.requestFocus();
            return;
        }
        String messageText=etUserMassege.getText().toString();
        this.userMessage=new ChatMessage();
        this.userMessage.setAuthor(author);
        this.userMessage.setText(messageText);
        new Thread(this::postUserMessage).start();
    }

    private void postUserMessage(){
        try{
            URL chatUrl=new URL(CHAT_URL);
            HttpURLConnection connection=(HttpURLConnection)chatUrl.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/json");
            connection.setRequestProperty("Accept","*/*");
            connection.setChunkedStreamingMode(0);

            OutputStream body=connection.getOutputStream();
            body.write(userMessage.toJsonString().getBytes());
            body.flush();
            body.close();

            int responseCode=connection.getResponseCode();
            if(responseCode>=400){
                Log.d("postUserMessage","Request fails with code"+responseCode);
                return;
            }
            InputStream response=connection.getInputStream();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int len;
            while( ( len = response.read( chunk ) ) != -1 ) {
                bytes.write( chunk, 0, len ) ;
            }
            String responseBody = new String( bytes.toByteArray(), StandardCharsets.UTF_8 ) ;
            bytes.close() ;

            response.close();
            connection.disconnect();

            new Thread(this::loadUrl).start();
        }catch (Exception ex){
            Log.d("postUserMessage",ex.getMessage());
        }
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
            runOnUiThread(this::parseContent);
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
    private void parseContent(){
        try{
            JSONObject array=new JSONObject(content);
            msgs=new ArrayList<>();

            if(array.getString("status").contains("success")){
                JSONArray tmp=array.getJSONArray("data");
                int len=tmp.length();
                for(int i=0;i<len;++i){
                    msgs.add(new ChatMessage(tmp.getJSONObject(i)));

                }
            }
            runOnUiThread(this::showChatMessage);
        }
        catch (JSONException ex){
            Log.d("parseContent","JSONException"+ex.getMessage());
        }

    }
    private void showChatMessage(){
        LinearLayout chatContainer=findViewById(R.id.chat_container);
        Drawable otherBg= AppCompatResources.getDrawable(getApplicationContext(),R.drawable.chat_other_bg);
        Drawable meBg= AppCompatResources.getDrawable(getApplicationContext(),R.drawable.chat_me_bg);
        LinearLayout.LayoutParams otherParams=new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        otherParams.setMargins(10,7,10,7);

        LinearLayout.LayoutParams meParams=new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        meParams.gravity= Gravity.RIGHT;

        meParams.setMargins(10,7,12,7);
        int i=0;
        for(ChatMessage msg:this.msgs){
            TextView tvChat=new TextView(this);
            tvChat.setText(msg.getDate()+":"+msg.getAuthor()+"-"+msg.getText());
            if(msg.getAuthor().equals(etUserName.getText().toString())){
                tvChat.setBackground(meBg);
                tvChat.setPadding(15,5,15,5);
                tvChat.setLayoutParams(meParams);
                chatContainer.addView(tvChat);

            }
            else{
                tvChat.setBackground(otherBg);
                tvChat.setPadding(15,5,15,5);
                tvChat.setLayoutParams(otherParams);
                chatContainer.addView(tvChat);

            }
        }
    }

    static class ChatMessage {
        private UUID id;
        private String author;
        private String text;
        private Date date;

        private static final SimpleDateFormat scanFormat=
                new SimpleDateFormat("MMM d,yyyy KK:mm:ss a", Locale.US);
        private UUID idReply;
        private String replyPreview;

        public UUID getIdReply() {
            return idReply;
        }

        public void setIdReply(UUID idReply) {
            this.idReply = idReply;
        }

        public String getReplyPreview() {
            return replyPreview;
        }

        public void setReplyPreview(String replyPreview) {
            this.replyPreview = replyPreview;
        }
        public ChatMessage(){

        }

        public ChatMessage(JSONObject obj)throws JSONException {
            setId(UUID.fromString(obj.getString("id")));
            setAuthor(obj.getString("author"));
            setText(obj.getString("txt"));
            try {
                setDate(scanFormat.parse(obj.getString("moment")));
            }
            catch (ParseException ex){
                throw new JSONException("Date (moment) parse error"+obj.getString("moment"));
            }
            if(obj.has("idReply")) {
                setIdReply(UUID.fromString(obj.getString("idReply")));
            }
            if(obj.has("replyPreview")){
                setReplyPreview(obj.getString("replyPreview"));
            }
        }

        public String toJsonString(){
            StringBuilder sb=new StringBuilder();
            sb.append(String.format("{\"author\":\"%s\",\"txt\":\"%s\"",getAuthor(),getText()));
            if(idReply!=null)
                sb.append(String.format(",\"idReply\":\"%s\"",getIdReply().toString()));
            sb.append("}");
            return sb.toString();
        }
        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
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

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}