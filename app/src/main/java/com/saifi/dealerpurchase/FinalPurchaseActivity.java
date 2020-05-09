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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.media.MediaRecorder.VideoSource.CAMERA;

public class FinalPurchaseActivity extends AppCompatActivity {

    Button doneBtn, selectInvoiceBtn, uploadInvoiceBtn, uploadPdf;
    ArrayList<String> imagePathListPdf = new ArrayList<>();
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
    boolean upload = false;
    private static final String IMAGE_DIRECTORY = "/demonuts_upload_gallery";
    private static final int BUFFER_SIZE = 1024 * 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_purchase);
        getSupportActionBar().setTitle("Final Submit");
        requestStoragePermission();
        requestMultiplePermissions();
        askForPermissioncamera(Manifest.permission.CAMERA, CAMERA);

        sessonManager = new SessonManager(FinalPurchaseActivity.this);
        views = new Views();
        doneBtn = findViewById(R.id.doneBtn);
        uploadInvoiceBtn = findViewById(R.id.uploadInvoiceBtn);
        selectInvoiceBtn = findViewById(R.id.selectInvoiceBtn);
        uploadPdf = findViewById(R.id.uploadPdf);
        rv_Invoice = findViewById(R.id.rvInvoice);

        Log.d("asdklzxc", DetailActivity.dealerId);

        click();
    }

    private void click() {
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (upload == false) {
                    Toast.makeText(FinalPurchaseActivity.this, "Please Upload image or pdf", Toast.LENGTH_SHORT).show();
                } else {
                    DetailActivity.dealerId = "";
                    MainActivity.dealerName = "";
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

        uploadPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent, 200);
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

                        JsonObject jsonObject = null;

                        Log.d("resposed_data", response.toString());
                        try {
                            jsonObject = response.body();

                            if (jsonObject.get("code").getAsString().equals("200")) {
                                upload = true;
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

    ///////////////////////////////////// upload pdf //////////////////////////////////////////////////////////
    private void uploadPDF(String path) {

        views.showProgress(FinalPurchaseActivity.this);
        String pdfname = String.valueOf(Calendar.getInstance().getTimeInMillis()) + ".pdf";


        HashMap<String, RequestBody> partMap = new HashMap<>();
        partMap.put("key", ApiFactory.getRequestBodyFromString(Url.key));
        partMap.put("dealer_id", ApiFactory.getRequestBodyFromString(DetailActivity.dealerId));
        partMap.put("userid", ApiFactory.getRequestBodyFromString(sessonManager.getToken()));

        MultipartBody.Part[] imageArray1 = new MultipartBody.Part[imagePathListPdf.size()];

        File file = new File(path);
        // Parsing any Media type file
        RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);

        imageArray1[0] = MultipartBody.Part.createFormData("image[]", pdfname, requestBody);
        RequestBody filename = RequestBody.create(MediaType.parse("text/plain"), pdfname);

        ApiInterface iApiServices = ApiFactory.createRetrofitInstance(Url.BASE_URL).create(ApiInterface.class);
        iApiServices.imageAPi(imageArray1, partMap)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                        views.hideProgress();
                        JsonObject jsonObject = response.body();
                        String code = jsonObject.get("code").getAsString();
                        String msg = jsonObject.get("msg").getAsString();


                        if (code.equals("200")) {
                            upload = true;
                            Toast.makeText(FinalPurchaseActivity.this, "Pdf upload successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(FinalPurchaseActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        views.hideProgress();

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
            if (requestCode == 1) {
                selectFromGalleryInvoice(data);

            }
            if (requestCode == 2) {
                rotateImageInvoice();
            }
            if (resultCode == RESULT_OK && requestCode == 200) {
                Uri uri = data.getData();
                String uriString = uri.toString();
                File myFile = new File(uriString);

                String path = getFilePathFromURI(FinalPurchaseActivity.this, uri);
                imagePathListPdf.add(path);
                uploadPDF(path);
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

    private void askForPermissioncamera(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(FinalPurchaseActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(FinalPurchaseActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(FinalPurchaseActivity.this, new String[]{permission}, requestCode);
            }
        } else {
//            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }


    }

    public static String getFilePathFromURI(Context context, Uri contentUri) {
        //copy file and send new file path
        String fileName = getFileName(contentUri);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }
        if (!TextUtils.isEmpty(fileName)) {
            File copyFile = new File(wallpaperDirectory + File.separator + fileName);
            // create folder if not exists

            copy(context, contentUri, copyFile);
            return copyFile.getAbsolutePath();
        }
        return null;
    }

    public static String getFileName(Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        String path = uri.getPath();
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            fileName = path.substring(cut + 1);
        }
        return fileName;
    }

    public static void copy(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            copystream(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int copystream(InputStream input, OutputStream output) throws Exception, IOException {
        byte[] buffer = new byte[BUFFER_SIZE];

        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                Log.e(e.getMessage(), String.valueOf(e));
            }
            try {
                in.close();
            } catch (IOException e) {
                Log.e(e.getMessage(), String.valueOf(e));
            }
        }
        return count;
    }

    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei = null;
        if (Build.VERSION.SDK_INT > 23) {
            ei = new ExifInterface(input);
        }


        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }


    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(bitmap.getWidth() * bitmap.getHeight());
        bitmap.compress(Bitmap.CompressFormat.PNG, 60, buffer);
        return buffer.toByteArray();
    }
}
