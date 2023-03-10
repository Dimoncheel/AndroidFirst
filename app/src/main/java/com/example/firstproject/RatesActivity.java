package com.example.firstproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RatesActivity extends AppCompatActivity {

    private final String nbuApiUrl="https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    private TextView tvContent;
    private String content;

    public List<Rate> rates;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rates);

        tvContent=findViewById(R.id.tv_rates_content);

        //loadUrl();
        new Thread(this::loadUrl).start();
    }

    private void loadUrl(){
        try(InputStream urlSream=new URL(nbuApiUrl).openStream()){
            StringBuilder sb=new StringBuilder();
            int sym;
            while ((sym=urlSream.read())!=-1){
                sb.append((char)sym);
            }
            content=new String(
                sb.toString().getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);
            new Thread(this::parseContect).start();
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
            JSONArray array=new JSONArray(content);
            rates=new ArrayList<>();
            int len=array.length();
            StringBuilder sb=new StringBuilder();
            for(int i=0;i<len;++i){
                rates.add(new Rate(array.getJSONObject(i)));

            }
        }
        catch (JSONException ex){
            Log.d("parseContent","JSONException"+ex.getMessage());
        }
        runOnUiThread(this::showContent);
    }

    private void showContentTxt(){
        StringBuilder sb=new StringBuilder();
        for (Rate obj:rates
             ) {
            sb.append(obj.txt);
            sb.append("-");
            sb.append(obj.rate);
            sb.append("\n");
        }
        content=sb.toString();
        tvContent.setText(content);
    }
    private void showContent(){
        LinearLayout ratesContainer=findViewById(R.id.rates_container);
        Drawable rateBg= AppCompatResources.getDrawable(getApplicationContext(),R.drawable.rates_shape);
        Drawable rateBg_r= AppCompatResources.getDrawable(getApplicationContext(),R.drawable.rates_shape_r);
        LinearLayout.LayoutParams rareParams=new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rareParams.setMargins(10,7,10,7);

        LinearLayout.LayoutParams rateParams=new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rateParams.gravity=Gravity.RIGHT;
        rateParams.setMargins(10,7,12,7);
        int i=0;
        for(Rate rate:this.rates){
            TextView tvRate=new TextView(this);
            tvRate.setText(rate.getTxt()+" "+Double.toString(rate.getRate()));
            if(i==0){
                tvRate.setBackground(rateBg);
                tvRate.setPadding(15,5,15,5);
                tvRate.setLayoutParams(rareParams);
                ratesContainer.addView(tvRate);
                i++;
            }
            else if(i==1){
                tvRate.setBackground(rateBg_r);
                tvRate.setPadding(15,5,15,5);
                tvRate.setLayoutParams(rateParams);
                ratesContainer.addView(tvRate);
                i--;
            }
        }
    }
    static class Rate {
        private int r030;
        private String txt;
        private double rate;

        public String getCc() {
            return cc;
        }

        public void setCc(String cc) {
            this.cc = cc;
        }

        private String cc;
        private String exchangedate;

        public Rate(JSONObject obj)throws JSONException{
            setR030(obj.getInt("r030"));
            setTxt(obj.getString("txt"));
            setRate(obj.getDouble("rate"));
            setCc(obj.getString("cc"));
            setExchangedate(obj.getString("exchangedate"));

        }

        public int getR030() {
            return r030;
        }

        public void setR030(int r030) {
            this.r030 = r030;
        }

        public String getTxt() {
            return txt;
        }

        public void setTxt(String txt) {
            this.txt = txt;
        }

        public double getRate() {
            return rate;
        }

        public void setRate(double rate) {
            this.rate = rate;
        }

        public String getExchangedate() {
            return exchangedate;
        }

        public void setExchangedate(String exchangedate) {
            this.exchangedate = exchangedate;
        }
    }

}
//https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json