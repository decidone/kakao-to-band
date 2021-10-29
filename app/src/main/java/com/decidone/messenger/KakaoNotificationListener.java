package com.decidone.messenger;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class KakaoNotificationListener extends NotificationListenerService {
    public final static String TAG = "MyNotificationListener";
    private final String BASE_URL = "https://openapi.band.us/v2.2/band/";

    private final String ACCESS_TOKEN = "band_access_token";
    private final String BAND_KEY = "band_key";
    private final String TALK_ROOM = "kakao_talk_room_name";
    private final String SENDER = "kakao_sender_name";

    private String pre_text = "base";
    private String pre_text2 = "base";
    private MyAPI myAPI;
    final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
    final String fileName = "sum_text.txt";

    @Override
    public void onCreate() {
        super.onCreate();
        initMyAPI(BASE_URL);
        scheduled_message();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        final String packageName = sbn.getPackageName();
        super.onNotificationPosted(sbn);

        Notification notification = sbn.getNotification();
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);

        if (!TextUtils.isEmpty(packageName) && packageName.equals("com.kakao.talk")) {
            /*
            Log.d(TAG, "onNotificationPosted ~ " +
                    " packageName: " + sbn.getPackageName() +
                    " id: " + sbn.getId() +
                    " postTime: " + sbn.getPostTime() +
                    " title: " + title +
                    " text : " + text +
                    " subText: " + subText);
             */

            if(text != null && pre_text != null && pre_text2 != null){
                if(subText.toString().contains(TALK_ROOM) && !text.toString().equals(pre_text)){
                    //메시지를 바로 밴드로 보낼 때
                    if(title.contains(SENDER)){
                        send(BAND_KEY, text.toString(), true);
                        pre_text = "" + text.toString();
                    }
                }else if(subText.toString().contains(TALK_ROOM) && !text.toString().equals(pre_text2)){
                    //메시지를 텍스트로 저장, 모아서 한번에 보낼 때
                    Calendar cal = Calendar.getInstance();
                    writeFile(fileName, fmt.format(cal.getTime()) + " - " + title.toString() + ": " + text.toString() + "\n\n");
                    pre_text2 = "" + text.toString();
                }
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Nothing to do
    }

    private void initMyAPI(String baseUrl){

        Log.d(TAG,"initMyAPI : " + baseUrl);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        myAPI = retrofit.create(MyAPI.class);
    }

    private void send(String band, String content, boolean do_push){

        Log.d(TAG,"POST");
        Call<PostItem> call;
        call = myAPI.createPost(ACCESS_TOKEN, band, content, do_push);

        call.enqueue(new Callback<PostItem>() {
            @Override
            public void onResponse(Call<PostItem> call, Response<PostItem> response) {
                Calendar cal = Calendar.getInstance();
                Log.d(TAG, fmt.format(cal.getTime()) + " response code: " + response.body().getResult_code());
                // 1003_error == Cool down time restriction
                if(response.body().getResult_code() == 1003){
                    send_later(band, content, do_push);
                }
                if (!response.isSuccessful()) {
                    Log.d(TAG,"Status Code : " + response.code());
                    Log.d(TAG,response.errorBody().toString());
                    Log.d(TAG,call.request().body().toString());
                    return;
                }
            }

            @Override
            public void onFailure(Call<PostItem> call, Throwable t) {
                Log.d(TAG,t.getMessage());
            }
        });
    }

    //라인 서버에 짧은 시간동안 여러 번 메시지를 보낼 경우 cooldown이 걸림
    private void send_later(String band, String content, boolean do_push){
        try
        {
            int time;
            time = 5000 + 5000 * (int)((Math.random()*10000)%10);
            Thread.sleep(time);
            Calendar cal = Calendar.getInstance();
            send(band,fmt.format(cal.getTime()) + "\n" + "서버 cooldown으로 인해 나중에 전송됨.\n\n" + content, do_push);
            Log.d(TAG, "time: " + time);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    //정해진 시간마다 텍스트파일에 모아둔 메시지를 밴드에 전송
    private void scheduled_message(){
        //실행간격 지정(30분)
        //안드로이드에서 앱을 죽여서 다시 살아날 경우 다시 살아난 시점부터 30분 카운트
        int sleepMin = 30;
        final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        exec.scheduleAtFixedRate(new Runnable(){
            public void run(){
                try {
                    Calendar cal = Calendar.getInstance();
                    //콘솔에 현재 시간 출력
                    Log.d(TAG, fmt.format(cal.getTime()));
                    String text = readFile(fileName);
                    Log.d(TAG, "length: " + text.length());
                    if(text != null && text != "" && text.length()>=5){
                        send(BAND_KEY, text, false);
                        resetFile(fileName);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "stop");
                    //에러 발생시 Executor를 중지
                    exec.shutdown();
                }
            }
        }, 0, sleepMin, TimeUnit.MINUTES);
    }

    public String readFile(String fileName){
        String text = "";
        try {
            InputStream inputStream = openFileInput(fileName);
            if(inputStream != null){
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer stringBuffer = new StringBuffer();
                String temp = "";
                while ((temp = bufferedReader.readLine()) != null){
                    stringBuffer.append(temp);
                    stringBuffer.append("\n");
                }
                text = stringBuffer.toString();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

    public void writeFile(String fileName, String text){
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(fileName, Context.MODE_APPEND));
            outputStreamWriter.append(text);
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetFile(String fileName){
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write("");
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
