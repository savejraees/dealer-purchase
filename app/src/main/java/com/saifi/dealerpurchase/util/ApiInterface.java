package com.saifi.dealerpurchase.util;

import com.saifi.dealerpurchase.retrofitModel.DetailModel;
import com.saifi.dealerpurchase.retrofitModel.LoginModel;
import com.saifi.dealerpurchase.retrofitModel.StatusModel;
import com.saifi.dealerpurchase.retrofitModel.dealer.DealerStatusModel;

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

    @FormUrlEncoded
    @POST("dealer_purchase_new")
    Call<DetailModel> hitSubmitDetailApi(
            @Field("key") String key, @Field("order_no") String order_no,@Field("purchase_cat_name") String purchase_cat_name,
            @Field("product_category") String product_category, @Field("brand_id") String brand_id,@Field("series_name") String series_name,
            @Field("model_id") String model_id, @Field("dealer_id") String dealer_id,@Field("warrenty") String warrenty,
            @Field("warrenty_month") String warrenty_month, @Field("condition") String condition,@Field("business_location_id") String business_location_id,
            @Field("userid") String userid, @Field("imei_no") String imei_no,@Field("purchase_amount") String purchase_amount,
            @Field("barcode_scan") String barcode_scan, @Field("storage") String storage
    );

    @FormUrlEncoded
    @POST("getdealer")
    Call<DealerStatusModel> hitDealer(@Field("key") String key);
}
