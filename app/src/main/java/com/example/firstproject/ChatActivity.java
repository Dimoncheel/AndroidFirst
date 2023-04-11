package com.example.firstproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    private final String CHAT_URL="https://diorama-chat.ew.r.appspot.com/story";
    private String content;
    private final String CHANNEL_ID="CHAT_NOTIFY";
    private Handler handler;

    private MediaPlayer incomingMessagePlayer;
    private ChatMessage userMessage;
    private LinearLayout chatContainer;
    private List<ChatMessage> msgs;

    private EditText etUserName;
    private EditText etUserMassege;
    private ScrollView svContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatContainer=findViewById(R.id.chat_container);
        etUserName=findViewById(R.id.et_chat_user_name);
        etUserMassege=findViewById(R.id.et_chat_message);
        this.handler=new Handler();
        this.handler.post(this::updateChat);
        //handler.postDelayed(this::showNotification,3000);
        findViewById(R.id.btn_chat_send).setOnClickListener(this::sendBtnClick);
        svContainer=findViewById(R.id.sv_container);
        msgs=new ArrayList<>();

        this.incomingMessagePlayer=MediaPlayer.create(this,R.raw.sound_1);
        //new Thread(this::loadUrl).start();
    }

    private void updateChat(){
        new Thread(this::loadUrl).start();
        this.handler.postDelayed(this::updateChat,3000);
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

            if(array.getString("status").contains("success")){
                JSONArray tmp=array.getJSONArray("data");
                int len=tmp.length();
                for(int i=0;i<len;++i){
                    ChatMessage tmp1=new ChatMessage(tmp.getJSONObject(i));
                    if(this.msgs.stream().noneMatch(cm->cm.getId().equals(tmp1.getId()))){
                        this.msgs.add(tmp1);
                    }
                }
            }
            this.msgs.sort(Comparator.comparing(ChatMessage::getDate));
            runOnUiThread(this::showChatMessage);
        }
        catch (JSONException ex){
            Log.d("parseContent","JSONException"+ex.getMessage());
        }

    }

    private String CheckDate(Date date){
        String[] msgDate=date.toString().split(" ");
        String[] nowDate=new Date().toString().split(" ");
        System.out.println(msgDate[1]);
        System.out.println(nowDate[1]);
        if(msgDate[1].equals(nowDate[1])
                &&msgDate[2].equals(nowDate[2])
                &&msgDate[5].equals(nowDate[5])){

            return msgDate[3];
        }
        else{
            return date.toString();
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
        boolean needScroll=false;
        for(ChatMessage msg:this.msgs){
            if(msg.getView()!=null)continue;
            TextView tvChat=new TextView(this);
            tvChat.setText(CheckDate(msg.getDate())+":"+msg.getAuthor()+"-"+msg.getText());
            if(msg.getAuthor().equals(etUserName.getText().toString())){
                tvChat.setBackground(meBg);
                tvChat.setPadding(15,5,15,5);
                tvChat.setLayoutParams(meParams);
                chatContainer.addView(tvChat);
                msg.setView(tvChat);
                tvChat.setTag(msg);
                needScroll=true;
            }
            else{
                tvChat.setBackground(otherBg);
                tvChat.setPadding(15,5,15,5);
                tvChat.setLayoutParams(otherParams);
                chatContainer.addView(tvChat);
                msg.setView(tvChat);
                tvChat.setTag(msg);
                needScroll=true;
            }
        }
        if(needScroll){
            svContainer.post(()->svContainer.fullScroll(View.FOCUS_DOWN));
            incomingMessagePlayer.start();
        }
    }


    private void showNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder=
                new NotificationCompat.Builder(ChatActivity.this,CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.sym_def_app_icon)
                        .setContentTitle("Chat")
                        .setContentText("New message in chat")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Notification notification=builder.build();

        NotificationManagerCompat notificationManager= NotificationManagerCompat.from(ChatActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        ChatActivity.this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        10002
                );
                return;
            }
        }
        notificationManager.notify(1002,notification);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==10002){

        }
    }

    static class ChatMessage {
        private View view; //ref to View on UI
        private UUID id;

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }

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