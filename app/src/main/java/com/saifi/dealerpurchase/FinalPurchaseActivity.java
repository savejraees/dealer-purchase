package com.saifi.dealerpurchase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.saifi.dealerpurchase.adapter.MobileImageAdapter;
import com.saifi.dealerpurchase.model.ImageModel;
import com.saifi.dealerpurchase.util.ApiFactory;
import com.saifi.dealerpurchase.util.ApiInterface;
import com.saifi.dealerpurchase.util.Helper;
import com.saifi.dealerpurchase.util.SessonManager;
import com.saifi.dealerpurchase.util.Url;
import com.saifi.dealerpurchase.util.Views;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FinalPurchaseActivity extends AppCompatActivity {

    Button doneBtn,selectInvoiceBtn,uploadInvoiceBtn,uploadPdf;
    RecyclerView rv_Invoice;
    int PICK_IMAGE_MULTIPLE = 1;
    private static final int STORAGE_PERMISSION_CODE = 123;
    File photoInvoiceFile = null;
    Uri photoUriInvoice;
    String mCurrentPhotoPathInvoice;
    private String photoPathInVoice;
    ArrayList<String> imagePathListInvoice = new ArrayList<>();
    ArrayList<ImageModel> Invoicelist = new ArrayList<>();
    String imageEncodedInvoice;
    public MobileImageAdapter mIMGAdapter;
    Bitmap bitmapInVoice;
    Views views;
    SessonManager sessonManager;
    boolean upload =false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_purchase);
        getSupportActionBar().setTitle("Final Submit");
        requestStoragePermission();
        requestMultiplePermissions();

        sessonManager = new SessonManager(FinalPurchaseActivity.this);
        views = new Views();
        doneBtn = findViewById(R.id.doneBtn);
        uploadInvoiceBtn = findViewById(R.id.uploadInvoiceBtn);
        selectInvoiceBtn = findViewById(R.id.selectInvoiceBtn);
        uploadPdf = findViewById(R.id.uploadPdf);
        rv_Invoice = findViewById(R.id.rvInvoice);

        Log.d("asdklzxc",DetailActivity.dealerId);

    click();
    }

    private void click() {
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(upload==false){
                    Toast.makeText(FinalPurchaseActivity.this, "Please Upload image or pdf", Toast.LENGTH_SHORT).show();
                }else{
                    DetailActivity.dealerId="";
                    startActivity(new Intent(FinalPurchaseActivity.this, MainActivity.class));
                    finishAffinity();
                }

            }
        });

        selectInvoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDialogInvoice();
            }
        });

        uploadInvoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hitApiUploadInvoice();
            }
        });
    }
    private void hitApiUploadInvoice() {

        views.showProgress(FinalPurchaseActivity.this);
        HashMap<String, RequestBody> partMap = new HashMap<>();
        partMap.put("key", ApiFactory.getRequestBodyFromString(Url.key));
        partMap.put("dealer_id", ApiFactory.getRequestBodyFromString(DetailActivity.dealerId));
        partMap.put("userid", ApiFactory.getRequestBodyFromString(sessonManager.getToken()));

        MultipartBody.Part[] imageArrayInvoice = new MultipartBody.Part[imagePathListInvoice.size()];

        for (int i = 0; i < imageArrayInvoice.length; i++) {
            File file = new File(imagePathListInvoice.get(i));
            try {
                File compressedfile = new Compressor(FinalPurchaseActivity.this).compressToFile(file);
                RequestBody requestBodyArray = RequestBody.create(MediaType.parse("image/*"), compressedfile);
                imageArrayInvoice[i] = MultipartBody.Part.createFormData("image[]", compressedfile.getName(), requestBodyArray);

                Log.d("astysawe", String.valueOf(imageArrayInvoice[i]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ApiInterface iApiServices = ApiFactory.createRetrofitInstance(Url.BASE_URL).create(ApiInterface.class);
        iApiServices.imageAPi(imageArrayInvoice, partMap)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        //Toast.makeText(ClaimAssistanceActivity_form.this, "" + response, Toast.LENGTH_SHORT).show();
                        views.hideProgress();

                        JsonObject jsonObject=null;

                        Log.d("resposed_data", response.toString());
                        try {
                            jsonObject = response.body();

                            if (jsonObject.get("code").getAsString().equals("200")) {
                                upload=true;
                                views.showToast(FinalPurchaseActivity.this, jsonObject.get("msg").getAsString());
                            } else {
                                views.showToast(FinalPurchaseActivity.this, jsonObject.get("msg").getAsString());
                            }
                        } catch (Exception e) {

                            Log.d("error_message", e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                        Toast.makeText(FinalPurchaseActivity.this, "fail" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        views.hideProgress();
                        Log.d("data_error", t.getMessage());

                    }
                });

    }

    private void startDialogInvoice() {

        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle("Upload Pictures Option");
        myAlertDialog.setMessage("How do you want to set your picture?");

        myAlertDialog.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(FinalPurchaseActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_IMAGE_MULTIPLE);

                } else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
                }


            }
        });

        myAlertDialog.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(FinalPurchaseActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_IMAGE_MULTIPLE);

                } else {
                    try {
                        takeInvoiceCameraImg();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        myAlertDialog.show();
    }

    private void takeInvoiceCameraImg() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoInvoiceFile = createImageFileInVOice();
                Log.d("checkexcesdp", String.valueOf(photoInvoiceFile));
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.d("checkexcep", ex.getMessage());
            }

            photoInvoiceFile = createImageFileInVOice();
            //lk changes here
            photoUriInvoice = FileProvider.getUriForFile(FinalPurchaseActivity.this, getPackageName() + ".provider", photoInvoiceFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUriInvoice);
            startActivityForResult(takePictureIntent, 2);
        }

    }

    private File createImageFileInVOice() throws IOException {
        String imageFileName = "GOOGLES" + System.currentTimeMillis();
        String storageDir = Environment.getExternalStorageDirectory() + "/skImages";
        Log.d("storagepath===", storageDir);
        File dir = new File(storageDir);
        if (!dir.exists())
            dir.mkdir();

        File image = new File(storageDir + "/" + imageFileName + ".jpg");
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPathInvoice = image.getAbsolutePath();

        Log.d("lsdplo", mCurrentPhotoPathInvoice);
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == 1 ) {
                selectFromGalleryInvoice(data);

            }
            if (requestCode == 2 ) {
                rotateImageInvoice();
            }

        } catch (Exception e) {
        }
    }

    private void selectFromGalleryInvoice(Intent data) {
        if (data != null) {
            try {

                String[] filePathColumn = {MediaStore.Images.Media.DATA};


                if (data.getClipData() != null) {
                    int imageCount = data.getClipData().getItemCount();
                    for (int i = 0; i < imageCount; i++) {
                        Uri mImageUri = data.getClipData().getItemAt(i).getUri();
                        photoPathInVoice = Helper.pathFromUri(FinalPurchaseActivity.this, mImageUri);
                        imagePathListInvoice.add(photoPathInVoice);

                        // Get the cursor
                        Cursor cursor1 = getApplicationContext().getContentResolver().query(mImageUri,
                                filePathColumn, null, null, null);
                        // Move to first row
                        cursor1.moveToFirst();

                        int columnIndex1 = cursor1.getColumnIndex(filePathColumn[0]);
                        imageEncodedInvoice = cursor1.getString(columnIndex1);
                        bitmapInVoice = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), mImageUri);
                        cursor1.close();
                        ImageModel imageModel = new ImageModel();
                        imageModel.setImageMobile(bitmapInVoice);

                        if (Invoicelist.size() < 5) {
                            Invoicelist.add(imageModel);
                        }
                    }
                } else {
                    Uri mImageUri = data.getData();
                    photoPathInVoice = Helper.pathFromUri(FinalPurchaseActivity.this, mImageUri);
                    imagePathListInvoice.add(photoPathInVoice);

                    // Get the cursor
                    Cursor cursor1 = getApplicationContext().getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor1.moveToFirst();

                    int columnIndex1 = cursor1.getColumnIndex(filePathColumn[0]);
                    imageEncodedInvoice = cursor1.getString(columnIndex1);
                    bitmapInVoice = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), mImageUri);
                    cursor1.close();
                    ImageModel imageModel = new ImageModel();
                    imageModel.setImageMobile(bitmapInVoice);
                    if (Invoicelist.size() < 5) {
                        Invoicelist.add(imageModel);
                    }
                }

                if (Invoicelist.size() > 5) {
                    //Toast.makeText(getApplicationContext(), "Max Limit Only 10", Toast.LENGTH_SHORT).show();
                }

                rv_Invoice.setLayoutManager(new LinearLayoutManager(FinalPurchaseActivity.this, LinearLayoutManager.HORIZONTAL, false));
                mIMGAdapter = new MobileImageAdapter(Invoicelist, FinalPurchaseActivity.this);
                rv_Invoice.setAdapter(mIMGAdapter);

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

    }

    public void rotateImageInvoice() throws IOException {

        try {
            String photoPath = photoInvoiceFile.getAbsolutePath();
            imagePathListInvoice.add(photoPath);
            bitmapInVoice = MediaStore.Images.Media.getBitmap(FinalPurchaseActivity.this.getContentResolver(), photoUriInvoice);
            bitmapInVoice = Bitmap.createScaledBitmap(bitmapInVoice, 800, 800, false);
            ImageModel imageModel = new ImageModel();
            imageModel.setImageMobile(bitmapInVoice);
            if (Invoicelist.size() < 5) {
                Invoicelist.add(imageModel);
            }


            if (Invoicelist.size() > 5) {
                //Toast.makeText(getApplicationContext(), "Max Limit Only 10", Toast.LENGTH_SHORT).show();
            }

            rv_Invoice.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
            mIMGAdapter = new MobileImageAdapter(Invoicelist, FinalPurchaseActivity.this);
            rv_Invoice.setAdapter(mIMGAdapter);

        } catch (Exception e) {
            e.printStackTrace();

        }
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
//                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
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
}
