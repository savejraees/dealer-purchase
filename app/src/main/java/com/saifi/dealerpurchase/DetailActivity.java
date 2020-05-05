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

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.saifi.dealerpurchase.adapter.DetailsAdapter;
import com.saifi.dealerpurchase.model.DetailsModel;
import com.saifi.dealerpurchase.util.NoScanResultException;
import com.saifi.dealerpurchase.util.ScanFragment;
import com.saifi.dealerpurchase.util.ScanResultReceiver;

import java.util.ArrayList;
import java.util.List;

import static android.media.MediaRecorder.VideoSource.CAMERA;

public class DetailActivity extends AppCompatActivity implements ScanResultReceiver {

    EditText edt_Imei, edt_Barcode, edt_Price, edt_GB;
    Button btnImeiScan, btnBarcodeScan, btnAddMore, btnSubmitDetail;
    RecyclerView rvDetails;
    RelativeLayout relativeLayout;
    DetailsAdapter detailsAdapter;
    ArrayList<DetailsModel> detailsList = new ArrayList<>();
    int count;

     static int totalAmount=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setTitle("Add Details");


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
                count = 0;
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
                count = 1;
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
                if(edt_Imei.getText().toString().isEmpty()){
                    edt_Imei.setError("Can't be Blank");
                    edt_Imei.requestFocus();
                }
                else if(edt_Barcode.getText().toString().isEmpty()){
                    edt_Barcode.setError("Can't be Blank");
                    edt_Barcode.requestFocus();
                }
               else if(edt_Price.getText().toString().isEmpty()){
                    edt_Price.setError("Can't be Blank");
                    edt_Price.requestFocus();
                }
               else if(edt_GB.getText().toString().isEmpty()){
                    edt_GB.setError("Can't be Blank");
                    edt_GB.requestFocus();
                }
               else {
                    edt_Imei.getText().clear();
                    edt_Barcode.getText().clear();
                    edt_Price.getText().clear();
                    edt_Price.getText().clear();
                    totalAmount = totalAmount+Integer.parseInt(edt_Price.getText().toString());
                    setRv();
                }

            }
        });
        btnSubmitDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edt_Imei.getText().toString().isEmpty()){
                    edt_Imei.setError("Can't be Blank");
                    edt_Imei.requestFocus();
                }
                else if(edt_Barcode.getText().toString().isEmpty()){
                    edt_Barcode.setError("Can't be Blank");
                    edt_Barcode.requestFocus();
                }
               else if(edt_Price.getText().toString().isEmpty()){
                    edt_Price.setError("Can't be Blank");
                    edt_Price.requestFocus();
                }
               else if(edt_GB.getText().toString().isEmpty()){
                    edt_GB.setError("Can't be Blank");
                    edt_GB.requestFocus();
                }
               else {
                    totalAmount = totalAmount+Integer.parseInt(edt_Price.getText().toString());
                    Log.d("acsaasf",""+totalAmount);
                    setRv();
                    startActivity(new Intent(DetailActivity.this,MainActivity.class));
                    //onBackPressed();
                }

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

        LinearLayoutManager layoutManager = new GridLayoutManager(DetailActivity.this,1);
        rvDetails.setLayoutManager(layoutManager);
        detailsAdapter = new DetailsAdapter(detailsList,DetailActivity.this);
        rvDetails.setAdapter(detailsAdapter);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void scanResultData(String codeFormat, String codeContent) {
        if (count == 0) {
            edt_Imei.setText(codeContent);
        }
        if (count == 1) {
            edt_Barcode.setText(codeContent);
        }

    }

    @Override
    public void scanResultData(NoScanResultException noScanData) {

    }
}
