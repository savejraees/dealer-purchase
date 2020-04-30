package com.saifi.dealerpurchase.util;

import com.saifi.dealerpurchase.retrofitModel.LoginModel;
import com.saifi.dealerpurchase.retrofitModel.StatusModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiInterface {

    @FormUrlEncoded
    @POST("purchase_login_new")
    Call<LoginModel> hitLogin(@Field("key") String key, @Field("mobile") String mobile, @Field("password") String password,
                              @Field("role") String role, @Field("user_role") String user_role);

    @FormUrlEncoded
    @POST("check_active_user")
    Call<StatusModel> hitStatusApi(@Field("key") String key, @Field("user_id") String id);
}
