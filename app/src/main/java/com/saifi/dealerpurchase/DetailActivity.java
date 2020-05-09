package com.saifi.dealerpurchase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.saifi.dealerpurchase.adapter.DetailsAdapter;
import com.saifi.dealerpurchase.model.DetailsModel;
import com.saifi.dealerpurchase.retrofitModel.DetailModel;
import com.saifi.dealerpurchase.retrofitModel.DetailModel;
import com.saifi.dealerpurchase.retrofitModel.ResponseError;
import com.saifi.dealerpurchase.util.ApiInterface;
import com.saifi.dealerpurchase.util.NoScanResultException;
import com.saifi.dealerpurchase.util.ScanFragment;
import com.saifi.dealerpurchase.util.ScanResultReceiver;
import com.saifi.dealerpurchase.util.SessonManager;
import com.saifi.dealerpurchase.util.Url;
import com.saifi.dealerpurchase.util.Views;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.media.MediaRecorder.VideoSource.CAMERA;

public class DetailActivity extends AppCompatActivity implements ScanResultReceiver {

    EditText edt_Imei, edt_Barcode, edt_Price, edt_GB;
    Button btnImeiScan, btnBarcodeScan, btnAddMore, btnSubmitDetail;
    RecyclerView rvDetails;
    RelativeLayout relativeLayout;
    DetailsAdapter detailsAdapter;
    ArrayList<DetailsModel> detailsList = new ArrayList<>();
    int countScan;

    static long totalAmount = 0;
    static String dealerId="";
    String productCategory, brand_id, seriesName, model_id, warrenty, warrenty_month, conditon_Mobile;
    String imei_no = "", purchase_amount = "", barcode_scan = "", storage = "";

    Views views;
    SessonManager sessonManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setTitle("Add Details");

        views = new Views();
        sessonManager = new SessonManager(DetailActivity.this);

        productCategory = getIntent().getStringExtra("product_category");
        brand_id = getIntent().getStringExtra("brand_id");
        seriesName = getIntent().getStringExtra("series_name");
        model_id = getIntent().getStringExtra("model_id");
        warrenty = getIntent().getStringExtra("warrenty");
        warrenty_month = getIntent().getStringExtra("warrenty_month");
        conditon_Mobile = getIntent().getStringExtra("condition");


        init();
        allAclick();

    }

    private void init() {
        edt_Imei = findViewById(R.id.edt_Imei);
        edt_Barcode = findViewById(R.id.edt_Barcode);
        edt_Price = findViewById(R.id.edt_Price);
        edt_GB = findViewById(R.id.edt_GB);
        btnImeiScan = findViewById(R.id.btnImeiScan);
        btnBarcodeScan = findViewById(R.id.btnBarcodeScan);
        btnAddMore = findViewById(R.id.btnAddMore);
        btnSubmitDetail = findViewById(R.id.btnSubmitDetail);
        rvDetails = findViewById(R.id.rvDetails);
        relativeLayout = findViewById(R.id.relativeLayout);
    }

    private void allAclick() {
        btnImeiScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countScan = 0;
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                ScanFragment scanFragment = new ScanFragment();
                fragmentTransaction.add(R.id.relativeLayout, scanFragment);
                fragmentTransaction.commit();
            }
        });
        btnBarcodeScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countScan = 1;
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                ScanFragment scanFragment = new ScanFragment();
                fragmentTransaction.add(R.id.relativeLayout, scanFragment);
                fragmentTransaction.commit();
            }
        });

        btnAddMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edt_Imei.getText().toString().isEmpty()) {
                    edt_Imei.setError("Can't be Blank");
                    edt_Imei.requestFocus();
                } else if (edt_Barcode.getText().toString().isEmpty()) {
                    edt_Barcode.setError("Can't be Blank");
                    edt_Barcode.requestFocus();
                } else if (edt_Price.getText().toString().isEmpty()) {
                    edt_Price.setError("Can't be Blank");
                    edt_Price.requestFocus();
                } else if (edt_GB.getText().toString().isEmpty()) {
                    edt_GB.setError("Can't be Blank");
                    edt_GB.requestFocus();
                } else {
                    if (purchase_amount.equals("")) {
                        purchase_amount = edt_Price.getText().toString();
                        totalAmount = totalAmount + Long.parseLong(edt_Price.getText().toString());

                    } else {
                        purchase_amount = purchase_amount + "," + edt_Price.getText().toString();
                        totalAmount = totalAmount + Long.parseLong(edt_Price.getText().toString());

                    }
                    if (storage.equals("")) {
                        storage = edt_GB.getText().toString();

                    } else {
                        storage = storage + "," + edt_GB.getText().toString();

                    }

                    setRv();

                    Log.d("daslksa", purchase_amount + " " + storage);

                    edt_Imei.getText().clear();
                    edt_Barcode.getText().clear();
                    edt_Price.getText().clear();
                    edt_GB.getText().clear();


                }

            }
        });
        btnSubmitDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edt_Imei.getText().toString().isEmpty()&&edt_Barcode.getText().toString().isEmpty()
                && edt_Price.getText().toString().isEmpty()&& edt_GB.getText().toString().isEmpty()) {
//                    edt_Imei.setError("Can't be Blank");
//                    edt_Imei.requestFocus();
                    if (storage.equals("")){
                        Toast.makeText(DetailActivity.this, "Please add Some Detail", Toast.LENGTH_SHORT).show();
                    }else {
                        hitSubmitApi();
                    }

                }

                if (!(edt_Imei.getText().toString().isEmpty()&&edt_Barcode.getText().toString().isEmpty()
                        && edt_Price.getText().toString().isEmpty()&& edt_GB.getText().toString().isEmpty())) {
                    if (purchase_amount.equals("")) {
                        purchase_amount = edt_Price.getText().toString();
                        totalAmount = totalAmount + Long.parseLong(edt_Price.getText().toString());

                    } else {
                        purchase_amount = purchase_amount + "," + edt_Price.getText().toString();
                        totalAmount = totalAmount + Long.parseLong(edt_Price.getText().toString());

                    }
                    if (storage.equals("")) {
                        storage = edt_GB.getText().toString();

                    } else {
                        storage = storage + "," + edt_GB.getText().toString();

                    }
                    if (imei_no.equals("")) {
                        imei_no = edt_Imei.getText().toString();
                    } else {
                        imei_no = imei_no + "," + edt_Imei.getText().toString();;
                    }
                    if (barcode_scan.equals("")) {
                        barcode_scan = edt_Barcode.getText().toString();;
                    } else {
                        barcode_scan = barcode_scan + "," + edt_Barcode.getText().toString();;;
                    }


                    hitSubmitApi();
                   // startActivity(new Intent(DetailActivity.this, MainActivity.class));

                }

            }
        });
    }

    private void hitSubmitApi() {
        views.showProgress(DetailActivity.this);
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Url.BASE_URL)
                .build();

        ApiInterface api = retrofit.create(ApiInterface.class);
        Call<DetailModel> call = api.hitSubmitDetailApi(Url.key,"","Dealer Purchase",productCategory,
                brand_id,seriesName,model_id,dealerId,warrenty,warrenty_month,conditon_Mobile,sessonManager.getBuisnessLocationId(),
                sessonManager.getToken(),imei_no,purchase_amount,barcode_scan,storage);

        call.enqueue(new Callback<DetailModel>() {
            @Override
            public void onResponse(Call<DetailModel> call, Response<DetailModel> response) {
                views.hideProgress();
                if (response.isSuccessful()) {
                    DetailModel DetailModel = response.body();

                    if (DetailModel.getCode().equals("200")) {
                        views.showToast(getApplicationContext(), DetailModel.getMsg());

                        startActivity(new Intent(getApplicationContext(), MainActivity.class));

                    }
                    else {

                        views.showToast(getApplicationContext(), ""+DetailModel.getMsg());
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
            public void onFailure(Call<DetailModel> call, Throwable t) {
                views.showToast(getApplicationContext(), t.getMessage());
                views.hideProgress();
            }
        });
    }

    private void setRv() {

        DetailsModel detailsModel = new DetailsModel();
        detailsModel.setImei(edt_Imei.getText().toString());
        detailsModel.setBarcode(edt_Barcode.getText().toString());
        detailsModel.setPrice(edt_Price.getText().toString());
        detailsModel.setGb(edt_GB.getText().toString());

        detailsList.add(detailsModel);

        LinearLayoutManager layoutManager = new GridLayoutManager(DetailActivity.this, 1);
        rvDetails.setLayoutManager(layoutManager);
        detailsAdapter = new DetailsAdapter(detailsList, DetailActivity.this);
        rvDetails.setAdapter(detailsAdapter);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void scanResultData(String codeFormat, String codeContent) {
        if (codeContent != null) {
            if (countScan == 0) {
                edt_Imei.setText(codeContent);

                if ((!codeContent.equals(""))) {
                    if (imei_no.equals("")) {
                        imei_no = codeContent;
                    } else {
                        imei_no = imei_no + "," + codeContent;
                    }
                }

            }
            if (countScan == 1) {
                edt_Barcode.setText(codeContent);
                if ((!codeContent.equals(""))) {
                    if (barcode_scan.equals("")) {
                        barcode_scan = codeContent;
                    } else {
                        barcode_scan = barcode_scan + "," + codeContent;
                    }
                }
            }
        } else {
        }
    }

    @Override
    public void scanResultData(NoScanResultException noScanData) {

    }
}
