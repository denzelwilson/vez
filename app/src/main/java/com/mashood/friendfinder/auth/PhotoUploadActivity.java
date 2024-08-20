package com.mashood.friendfinder.auth;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.mashood.friendfinder.R;
import com.mashood.friendfinder.common.NetworkConfig;
import com.mashood.friendfinder.common.ProgressManager;
import com.mashood.friendfinder.common.VolleyMultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class PhotoUploadActivity extends AppCompatActivity {

    MaterialButton btnGallery, btnView;
    ImageView imgPreview, btnUpload;

    String username, displayName, type;
    int REQUEST_GET_SINGLE_FILE = 100;
    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_upload);

        btnGallery = findViewById(R.id.btnGallery);
        btnView = findViewById(R.id.btnView);
        imgPreview = findViewById(R.id.imgPreview);
        btnUpload = findViewById(R.id.btnUpload);

        // Get the username from RegistrationActivity
        username = getIntent().getStringExtra("username");
        type = getIntent().getStringExtra("type");

        setListeners();
    }

    private void setListeners() {
        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GET_SINGLE_FILE);
        });

        btnView.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select an image first!", Toast.LENGTH_SHORT).show();
            } else {
                imgPreview.setVisibility(View.VISIBLE);
                imgPreview.setImageURI(selectedImageUri);
                Toast.makeText(this, displayName, Toast.LENGTH_SHORT).show();
            }
        });

        btnUpload.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select an image first!", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        });
    }

    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GET_SINGLE_FILE) {
                imgPreview.setVisibility(View.GONE);

                // Get the Uri of the selected file
                selectedImageUri = data.getData();
                Bitmap compressedBitmap;
                try {
                    // Compress the image
                    compressedBitmap = new Compressor(this).compressToBitmap(getFile(this, selectedImageUri));

//                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                    // Crop the image to square
                    Bitmap croppedBitmap = cropToSquare(compressedBitmap);
                    selectedImageUri = getImageUri(croppedBitmap);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String uriString = selectedImageUri.toString();
                File myFile = new File(uriString);
                String path = myFile.getAbsolutePath();

                if (uriString.startsWith("content://")) {
                    Cursor cursor = null;
                    try {
                        cursor = this.getContentResolver().query(selectedImageUri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            Log.d("nameeeee>>>>  ", displayName);
                        }
                    } finally {
                        if (cursor != null)
                            cursor.close();
                    }
                } else if (uriString.startsWith("file://")) {
                    displayName = myFile.getName();
                    Log.d("nameeeee>>>>  ", displayName);
                }
            }
        }
    }

    public static File getFile(Context context, Uri uri) throws IOException {
        File destinationFilename = new File(context.getFilesDir().getPath() + File.separatorChar + queryName(context, uri));
        try (InputStream ins = context.getContentResolver().openInputStream(uri)) {
            createFileFromStream(ins, destinationFilename);
        } catch (Exception ex) {
            Log.e("Save File", ex.getMessage());
            ex.printStackTrace();
        }
        return destinationFilename;
    }

    public static void createFileFromStream(InputStream ins, File destination) {
        try (OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = ins.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        } catch (Exception ex) {
            Log.e("Save File", ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static String queryName(Context context, Uri uri) {
        Cursor returnCursor =
                context.getContentResolver().query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    public static Bitmap cropToSquare(Bitmap bitmap){
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = Math.min(height, width);
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = Math.max(cropW, 0);
        int cropH = (height - width) / 2;
        cropH = Math.max(cropH, 0);
        return Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
    }

    public Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void uploadImage() {
        String url = NetworkConfig.BASE_URL + NetworkConfig.PHOTO_UPLOAD_ENDPOINT;
        try {
            InputStream iStream = getContentResolver().openInputStream(selectedImageUri);
            final byte[] inputData = getBytes(iStream);

            ProgressManager.showSimpleProgressDialog(this, "Uploading image...", false);

            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                    response -> {
                        ProgressManager.removeSimpleProgressDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));

                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");

                            if (status.equals("1")) {
                                Toast.makeText(PhotoUploadActivity.this, message, Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(PhotoUploadActivity.this, message, Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        ProgressManager.removeSimpleProgressDialog();
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                    }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("username", username);
                    return params;
                }

                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    params.put("filename", new DataPart(username, inputData));
                    return params;
                }
            };

            volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestQueue rQueue = Volley.newRequestQueue(this);
            rQueue.add(volleyMultipartRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    @Override
    public void onBackPressed() {
        if (type.equals("upload")) {
            new AlertDialog.Builder(this)
                    .setTitle("Alert")
                    .setMessage("Registration process will not be completed without uploading your photo. Please upload your photo before closing this page.")
                    .setNegativeButton("OK", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else
            super.onBackPressed();
    }
}