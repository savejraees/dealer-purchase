package com.saifi.dealerpurchase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.saifi.dealerpurchase.adapter.DealerAdapter;
import com.saifi.dealerpurchase.adapter.DetailsAdapter;
import com.saifi.dealerpurchase.adapter.ModelAdapter;
import com.saifi.dealerpurchase.model.BrandSpinner;
import com.saifi.dealerpurchase.model.DetailsModel;
import com.saifi.dealerpurchase.model.Model_Model;
import com.saifi.dealerpurchase.model.SeriesModel;
import com.saifi.dealerpurchase.retrofitModel.FinalModel;
import com.saifi.dealerpurchase.retrofitModel.ResponseError;
import com.saifi.dealerpurchase.retrofitModel.dealer.DealerDatum;
import com.saifi.dealerpurchase.retrofitModel.dealer.DealerStatusModel;
import com.saifi.dealerpurchase.util.ApiInterface;
import com.saifi.dealerpurchase.util.SessonManager;
import com.saifi.dealerpurchase.util.Url;
import com.saifi.dealerpurchase.util.Views;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.media.MediaRecorder.VideoSource.CAMERA;

public class MainActivity extends AppCompatActivity {

    Button btnAddDetails, btnFinalSubmit;
    TextView txtLogout;
    private static final int STORAGE_PERMISSION_CODE = 123;
    RadioGroup radioShop, radioWarranty;
    RadioButton radioAccesories;
    LinearLayout layoutAccesother, layoutMobTab;
    Spinner Warranty_spinner, spinnerSeries, spinnerBrandMobile, condition_spinner;
    AutoCompleteTextView model_autocompleteTv, dealer_autocompleteTv;
    EditText edit_Brand, edt_model;

    String warrenty = "", warrenty_month = "", productCategory = "", conditon_Mobile = "";
    String brand_id = "", brandName = "", series_id = "", seriesName = "", idmodel = "", modelName = "";
    ModelAdapter modelAdapter;
    DealerAdapter dealerAdapter;
    Views views = new Views();
    ArrayList<BrandSpinner> brand_list = new ArrayList<>();
    final ArrayList<String> brand_list_datamobile = new ArrayList();
    ArrayList<SeriesModel> series_list = new ArrayList<>();
    final ArrayList<String> series_list_dataSeries = new ArrayList();
    ArrayList<Model_Model> model_list = new ArrayList<>();
    ArrayList<DealerDatum> listDealer = new ArrayList<>();

    String Warranty_data[] = {
            "0 - 1 month old",
            "1 - 3 month old",
            "3 - 6 month old",
            "6 - 9 month old",
            "9 - 12 month old",
    };

    String condition[] = {"Excellent", "Very Good", "Good", "Average"};
    SessonManager sessonManager;
    String userId;
    LinearLayout mainLayout;
    long back_pressed = 0;
    TextView txtTotal,txtDealer;
    static String dealerName="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        askForPermissioncamera(Manifest.permission.CAMERA, CAMERA);
        requestStoragePermission();
        requestMultiplePermissions();

        init();
        click();

        hitApiDealer();
    }

    private void init() {
        btnAddDetails = findViewById(R.id.btnAddDetails);
        btnFinalSubmit = findViewById(R.id.btnFinalSubmit);
        txtTotal = findViewById(R.id.txtTotal);
        txtDealer = findViewById(R.id.txtDealer);
        txtTotal.setText("Total Amount = ₹"+DetailActivity.totalAmount+"/-");
        txtDealer.setText("Selected Dealer: "+dealerName);

        radioShop = findViewById(R.id.radioShop);
        radioWarranty = findViewById(R.id.radioWarranty);
        layoutAccesother = findViewById(R.id.layoutAccesother);
        layoutMobTab = findViewById(R.id.layoutMobTab);
        Warranty_spinner = findViewById(R.id.Warranty_spinner);
        txtLogout = findViewById(R.id.txtLogout);
        mainLayout = findViewById(R.id.mainLayout);

        spinnerBrandMobile = findViewById(R.id.spinnerBrandMobile);
        spinnerSeries = findViewById(R.id.spinnerSeries);
        radioAccesories = findViewById(R.id.radioAccesories);
        edit_Brand = findViewById(R.id.edit_Brand);
        edt_model = findViewById(R.id.edt_model);
        model_autocompleteTv = findViewById(R.id.model_autocompleteTv);
        dealer_autocompleteTv = findViewById(R.id.dealer_autocompleteTv);
        model_autocompleteTv.setThreshold(1);
        dealer_autocompleteTv.setThreshold(1);

        sessonManager = new SessonManager(MainActivity.this);
        userId = sessonManager.getToken();

        Log.d("delerCheck",DetailActivity.dealerId);

        //////////////////// condtion spinner //////////////////
        condition_spinner = findViewById(R.id.condition_spinner);
        ArrayAdapter cndnAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, condition);
        condition_spinner.setAdapter(cndnAdapter);
    }

    private void click() {
        btnAddDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(radioAccesories.isChecked()){
                    brand_id = edit_Brand.getText().toString();
                    idmodel = edt_model.getText().toString();
                }
                if (productCategory.equals("")) {
                    Toast.makeText(MainActivity.this, "Select Mobile, Tablet Or Accessories", Toast.LENGTH_SHORT).show();
                }
                else if (warrenty == "") {
                    Toast.makeText(MainActivity.this, "Select Warranty", Toast.LENGTH_SHORT).show();
                }
                else if (brand_id == "") {
                    Toast.makeText(MainActivity.this, "Select Brand", Toast.LENGTH_SHORT).show();
                }
                else if (idmodel == "") {
                    Toast.makeText(MainActivity.this, "Select Model", Toast.LENGTH_SHORT).show();
                }
                else if (DetailActivity.dealerId == "") {
                    Toast.makeText(MainActivity.this, "Select Dealer", Toast.LENGTH_SHORT).show();
                }
                else {
                    startActivity(new Intent(MainActivity.this, DetailActivity.class)
                            .putExtra("product_category", productCategory)
                            .putExtra("brand_id", brand_id)
                            .putExtra("series_name", seriesName)
                            .putExtra("model_id", idmodel)
                            .putExtra("warrenty", warrenty)
                            .putExtra("warrenty_month", warrenty_month)
                            .putExtra("condition", conditon_Mobile)
                    );
                }

            }
        });

        btnFinalSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(DetailActivity.totalAmount==0){
                   Toast.makeText(MainActivity.this, "Please Submit Some Phone or Tablet", Toast.LENGTH_SHORT).show();
               }else if(DetailActivity.dealerId=="") {
                   Toast.makeText(MainActivity.this, "Please Select Dealer", Toast.LENGTH_SHORT).show();
               }else {
                   hitApiFinal();
               }

               // startActivity(new Intent(MainActivity.this, FinalPurchaseActivity.class));
            }
        });

        radioShop.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton checkedRadioButton = radioGroup.findViewById(i);
                boolean checked = checkedRadioButton.isChecked();
                if (checked) {
                    if (checkedRadioButton.getText().toString().equals("Mobile")) {
                        layoutMobTab.setVisibility(View.VISIBLE);
                        layoutAccesother.setVisibility(View.GONE);
                        productCategory = "Mobile";
                        hitSpinnerBrandMobile();
                    }
                    if (checkedRadioButton.getText().toString().equals("Tablet")) {
                        layoutMobTab.setVisibility(View.VISIBLE);
                        layoutAccesother.setVisibility(View.GONE);
                        productCategory = "Tablet";
                        hitSpinnerBrandMobile();
                    }
                    if (checkedRadioButton.getText().toString().equals("Accessories")) {
                        layoutMobTab.setVisibility(View.GONE);
                        layoutAccesother.setVisibility(View.VISIBLE);
                        productCategory = "Accessories";
                        brand_id = "";
                        brandName = "";
                        series_id = "";
                        seriesName = "";
                        modelName = "";
                        idmodel = "";
                    }
                }
            }
        });

        radioWarranty.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton checkedRadioButton = radioGroup.findViewById(i);
                boolean checked = checkedRadioButton.isChecked();
                if (checked) {
                    if (checkedRadioButton.getText().toString().equals("In")) {
                        Warranty_spinner.setVisibility(View.VISIBLE);

                        ArrayAdapter ab = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, Warranty_data);
                        Warranty_spinner.setAdapter(ab);
                        Warranty_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view,
                                                       int position, long id) {

                                warrenty = "In";
                                warrenty_month = Warranty_data[position];
                                Log.d("fasfafas", warrenty_month);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });
                    }
                    if (checkedRadioButton.getText().toString().equals("Out")) {
                        Warranty_spinner.setVisibility(View.GONE);
                        warrenty_month = "";
                        warrenty = "Out";
                    }

                }
            }
        });

        spinnerBrandMobile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                //int position = adapterView.getSelectedItemPosition();
                brand_id = String.valueOf(brand_list.get(i).getBrandId());
                brandName = brand_list.get(i).getBrand_name();

                Log.d("fsdklj", brand_id + brandName);

                hitSpinnerSeries(brand_id);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinnerSeries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int position = adapterView.getSelectedItemPosition();
                series_id = String.valueOf(series_list.get(position).getSeries_id());
                seriesName = series_list.get(i).getSeries_name();
                hitModelApi(series_id);
                Log.d("sepo", seriesName + " " + series_id);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        model_autocompleteTv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Model_Model modelObject = (Model_Model) parent.getItemAtPosition(position);
                modelName = modelObject.getModelName();
                idmodel = modelObject.getModel_id();
                Log.d("asdsda", idmodel);
            }
        });

        dealer_autocompleteTv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                DealerDatum modelObject = (DealerDatum) parent.getItemAtPosition(position);
                dealerName = modelObject.getDealerName();
                DetailActivity.dealerId = String.valueOf(modelObject.getId());
                txtDealer.setText("Select Dealer: "+dealerName);

                Log.d("asdsasdda", DetailActivity.dealerId + " " + dealerName);
            }
        });

        condition_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                conditon_Mobile = condition[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        txtLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sessonManager.setToken("");
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finishAffinity();
            }
        });

    }


    //////////////////////////////////////////Brand Spinner /////////////////////////////////////

    private void hitSpinnerBrandMobile() {
        views.showProgress(MainActivity.this);

        RequestQueue requestQueueMobile = Volley.newRequestQueue(MainActivity.this);
        StringRequest request = new StringRequest(Request.Method.POST, com.saifi.dealerpurchase.util.Url.getBrand, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Loginresponse", response);
                views.hideProgress();

                brand_list.clear();
                brand_list_datamobile.clear();
                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getString("code").equals("200")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("data");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            BrandSpinner brandSpinner = new BrandSpinner();
                            brandSpinner.setBrandId(jsonObject1.getInt("id"));
                            brandSpinner.setBrand_name(jsonObject1.getString("brand_name"));
                            brand_list.add(brandSpinner);
                            brand_list_datamobile.add(brand_list.get(i).getBrand_name());
                        }

                        Log.d("jhkljk", brand_list_datamobile.toString());

                        ArrayAdapter brandAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, brand_list_datamobile);
                        spinnerBrandMobile.setAdapter(brandAdapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                views.hideProgress();

                Toast.makeText(MainActivity.this, "Something Wrong", Toast.LENGTH_SHORT).show();
                Log.d("errodfr", error.getMessage() + "errorr");

            }
        }) {
            protected Map<String, String> getParams() {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("key", Url.key);
                hashMap.put("type", "Mobile");
                return hashMap;
            }
        };
        requestQueueMobile.getCache().clear();
        requestQueueMobile.add(request);
    }

    ////////////////////////////////////////// Series Spinner //////////////////////////////////

    private void hitSpinnerSeries(final String brand) {

        views.showProgress(MainActivity.this);
        series_list_dataSeries.clear();
        series_list.clear();

        RequestQueue requestQueueMobile = Volley.newRequestQueue(MainActivity.this);
        StringRequest request = new StringRequest(Request.Method.POST, Url.getseries, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Logdfgonse", response);
                views.hideProgress();
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getString("code").equals("200")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("data");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            SeriesModel seriesModel = new SeriesModel();
                            seriesModel.setSeries_id(jsonObject1.getString("id"));
                            seriesModel.setSeries_name(jsonObject1.getString("series_name"));
                            series_list.add(seriesModel);
                            series_list_dataSeries.add(series_list.get(i).getSeries_name());
                        }

                        Log.d("jhkljasdk", series_list_dataSeries.toString());

                        ArrayAdapter brandAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, series_list_dataSeries);
                        spinnerSeries.setAdapter(brandAdapter);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                views.hideProgress();
                Toast.makeText(MainActivity.this, "Something Wrong", Toast.LENGTH_SHORT).show();
                Log.d("errodfr", error.getMessage() + "errorr");

            }
        }) {
            protected Map<String, String> getParams() {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("key", Url.key);
                hashMap.put("brand_id", brand);
                Log.d("brandidd", brand_id);

                return hashMap;

            }

        };
        requestQueueMobile.getCache().clear();
        requestQueueMobile.add(request);
    }

    /////////////////////////////////////////// Model Spinner //////////////////////////////////

    void hitModelApi(final String series) {
        views.showProgress(MainActivity.this);

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        StringRequest request = new StringRequest(Request.Method.POST, Url.getModel, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response_model", response);
                views.hideProgress();

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getString("code").equals("200")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        // model_list_dataModel.clear();
                        model_list.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            Model_Model model = new Model_Model();
                            model.setModel_id(jsonObject1.getString("id"));
                            model.setModelName(jsonObject1.getString("model_name"));
                            model_list.add(model);


                        }

                        modelAdapter = new ModelAdapter(getApplicationContext(), R.layout.activity_main,
                                R.id.lbl_name, model_list);
                        model_autocompleteTv.setAdapter(modelAdapter);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                views.hideProgress();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("key", Url.key);
                hashMap.put("series_id", series);
                return hashMap;
            }
        };
        requestQueue.getCache().clear();
        requestQueue.add(request);


    }

    ////////////////////////// hit Dealer Api ////////////////////////////////////////
    private void hitApiDealer() {
        views.showProgress(MainActivity.this);
        listDealer.clear();


        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Url.BASE_URL)
                .build();

        ApiInterface api = retrofit.create(ApiInterface.class);

        Call<DealerStatusModel> call = api.hitDealer(Url.key);


        call.enqueue(new Callback<DealerStatusModel>() {
            @Override
            public void onResponse(Call<DealerStatusModel> call, Response<DealerStatusModel> response) {
                views.hideProgress();

                if (response.isSuccessful()) {
                    DealerStatusModel model = response.body();

                    listDealer = model.getData();

                    dealerAdapter = new DealerAdapter(getApplicationContext(), R.layout.activity_main,
                            R.id.lbl_name, listDealer);
                    dealer_autocompleteTv.setAdapter(dealerAdapter);


                } else {
                    views.showToast(MainActivity.this, String.valueOf(response));
                }
            }

            @Override
            public void onFailure(Call<DealerStatusModel> call, Throwable t) {
                views.hideProgress();
                views.showToast(MainActivity.this, t.getMessage());
            }
        });
    }
 ///////////////////////////// hit Final Api ////////////////////////////
 private void hitApiFinal() {
     views.showProgress(MainActivity.this);
     Retrofit retrofit = new Retrofit.Builder()
             .addConverterFactory(GsonConverterFactory.create())
             .baseUrl(Url.BASE_URL)
             .build();

     ApiInterface api = retrofit.create(ApiInterface.class);
     Call<FinalModel> call = api.hitFinalApi(Url.key,DetailActivity.dealerId,sessonManager.getToken());

     call.enqueue(new Callback<FinalModel>() {
         @Override
         public void onResponse(Call<FinalModel> call, Response<FinalModel> response) {
             views.hideProgress();
             if (response.isSuccessful()) {
                 FinalModel FinalModel = response.body();

                 if (FinalModel.getCode().equals("200")) {
                     views.showToast(getApplicationContext(), FinalModel.getMsg());
                     DetailActivity.totalAmount=0;
                     startActivity(new Intent(getApplicationContext(), FinalPurchaseActivity.class));

                 }
                 else {

                     views.showToast(getApplicationContext(), ""+FinalModel.getMsg());
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
         public void onFailure(Call<FinalModel> call, Throwable t) {
             views.showToast(getApplicationContext(), t.getMessage());
             views.hideProgress();
         }
     });
 }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void askForPermissioncamera(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
//            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }


    }

    private void requestMultiplePermissions() {
        Dexter.withActivity(this)
                .withPermissions(

                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
//                            Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings

                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    @Override
    public void onBackPressed() {


        if (back_pressed + 2000 > System.currentTimeMillis()){
            super.onBackPressed();
        }

            else {
                Snackbar snackbar = Snackbar.make(mainLayout, "Double Tap to Exit!", Snackbar.LENGTH_SHORT);
                View view = snackbar.getView();
                view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                snackbar.show();
                back_pressed = System.currentTimeMillis();
            }
//        }


    }
}
