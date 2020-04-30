package com.saifi.dealerpurchase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.saifi.dealerpurchase.retrofitModel.LoginModel;
import com.saifi.dealerpurchase.retrofitModel.ResponseError;
import com.saifi.dealerpurchase.util.ApiInterface;
import com.saifi.dealerpurchase.util.SessonManager;
import com.saifi.dealerpurchase.util.Url;
import com.saifi.dealerpurchase.util.Views;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {


    Button loginButton;
    EditText editTextMobile, editTextPassword;
    SessonManager sessonManager;
    Views views;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        sessonManager = new SessonManager(getApplicationContext());
        views = new Views();

        loginButton = findViewById(R.id.btnLogin);
        editTextMobile = findViewById(R.id.edtMob);
        editTextPassword = findViewById(R.id.edtPwd);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editTextMobile.getText().toString().isEmpty()) {
                    editTextMobile.setError("Can't be Blank");
                    editTextMobile.requestFocus();
                }

                else if (editTextPassword.getText().toString().isEmpty()) {
                    editTextPassword.setError("Can't be Blank");
                    editTextPassword.requestFocus();
                }

                else {
                    hitApi();
                }

            }
        });

    }

    private void hitApi() {
        views.showProgress(LoginActivity.this);
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Url.BASE_URL)
                .build();

        ApiInterface api = retrofit.create(ApiInterface.class);

        Call<LoginModel> call = api.hitLogin(Url.key, editTextMobile.getText().toString(),
                editTextPassword.getText().toString(), "dealer","user");

        call.enqueue(new Callback<LoginModel>() {
            @Override
            public void onResponse(Call<LoginModel> call, Response<LoginModel> response) {
                views.hideProgress();
                if (response.isSuccessful()) {
                    LoginModel loginModel = response.body();

                    if (loginModel.getCode().equals("200")) {
                        views.showToast(getApplicationContext(), loginModel.getMsg());

                        String name = loginModel.getName();
                        String mobile = loginModel.getMobile();
                        String location = loginModel.getLocation();
                        int Buisnesslocation = loginModel.getBusinessLocationId();
                        int userId = loginModel.getUserid();
                        sessonManager.setToken(String.valueOf(userId));
                        sessonManager.setMobile(mobile);
                        sessonManager.setUserName(name);
                        sessonManager.setLocation(location);
                        sessonManager.setBuisnessLocationId(String.valueOf(Buisnesslocation));

                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finishAffinity();
                    }
                    else {

                        views.showToast(getApplicationContext(), ""+loginModel.getMsg());
                    }
                }
                else
                {
                    Gson gson = new GsonBuilder().create();
                    ResponseError responseError = gson.fromJson(response.errorBody().charStream(),ResponseError.class);
                    views.showToast(getApplicationContext(), responseError.getMsg());
                }
            }

            @Override
            public void onFailure(Call<LoginModel> call, Throwable t) {
                views.showToast(getApplicationContext(), t.getMessage());

            }
        });
    }

}
