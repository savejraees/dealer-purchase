package com.saifi.dealerpurchase;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.saifi.dealerpurchase.retrofitModel.StatusModel;
import com.saifi.dealerpurchase.util.ApiInterface;
import com.saifi.dealerpurchase.util.SessonManager;
import com.saifi.dealerpurchase.util.Url;
import com.saifi.dealerpurchase.util.Views;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SplashActivity extends Activity {

    SessonManager sessonManager;
    String token;
    Views views;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessonManager = new SessonManager(SplashActivity.this);
        views = new Views();
        token = sessonManager.getToken();

        if(token.isEmpty() || token==null || token.equals("")){

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    Intent i = new Intent(getBaseContext(), LoginActivity.class);
                    startActivity(i);
                }
            }, 3000);

        }else {
            hitStatusApi();
        }


    }

    private void hitStatusApi() {
        views.showProgress(SplashActivity.this);

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Url.BASE_URL)
                .build();

        ApiInterface api = retrofit.create(ApiInterface.class);
        Call<StatusModel> call = api.hitStatusApi(Url.key,sessonManager.getToken());
        call.enqueue(new Callback<StatusModel>() {
            @Override
            public void onResponse(Call<StatusModel> call, Response<StatusModel> response) {
                views.hideProgress();
                if(response.isSuccessful()){
                    StatusModel statusModel = response.body();
                    if(statusModel.getCode().equalsIgnoreCase("200")){
                        views.showToast(getBaseContext(),statusModel.getMsg());
                        finish();
                        Intent i = new Intent(getBaseContext(), MainActivity.class);
                        startActivity(i);
                    }
                    else {
                        views.showToast(getBaseContext(),statusModel.getMsg());
                        finish();
                        Intent i = new Intent(getBaseContext(), LoginActivity.class);
                        startActivity(i);
                    }
                }
            }

            @Override
            public void onFailure(Call<StatusModel> call, Throwable t) {
                views.hideProgress();
            }
        });
    }


}
