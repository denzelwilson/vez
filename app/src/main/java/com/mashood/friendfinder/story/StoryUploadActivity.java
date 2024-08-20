package com.mashood.friendfinder.story;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.mashood.friendfinder.BuildConfig;
import com.mashood.friendfinder.common.NetworkConfig;
import com.mashood.friendfinder.common.ProgressManager;
import com.mashood.friendfinder.common.SessionManager;
import com.mashood.friendfinder.common.VolleyMultipartRequest;
import com.mashood.friendfinder.databinding.ActivityStoryUploadBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class StoryUploadActivity extends AppCompatActivity {

    String username, name, profileImage, currentPhotoPath;
    private Uri photoURI;
    private ActivityStoryUploadBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStoryUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setListeners();
    }

    private void init() {
        username = new SessionManager(this).getUserDetails().get("username");
        name = new SessionManager(this).getUserDetails().get("name");
        profileImage = new SessionManager(this).getUserDetails().get("image");

        cameraResult.launch(Manifest.permission.CAMERA);
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(view ->
                finish()
        );

        binding.btnUpload.setOnClickListener(view ->
                uploadStory()
        );
    }

    private final ActivityResultLauncher<String> cameraResult = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Please enable camera permission to continue", Toast.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<Intent> startForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (photoURI != null) {
                        // TODO: Handle the compression here
                        Bitmap compressedBitmap;
                        try {
                            compressedBitmap = new Compressor(this).compressToBitmap(getFile(this, photoURI));
//                            compressedBitmap = new Compressor(this).compressToBitmap(new File(photoURI.getPath()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (compressedBitmap != null)
                            photoURI = getImageUri(compressedBitmap);

                        binding.imgStory.setImageURI(photoURI);
                    } else {
                        Toast.makeText(this, "No image!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

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

    public Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void openCamera() {
        //  startForResult.launch(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE));
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Error capturing image", Toast.LENGTH_SHORT).show();
                finish();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startForResult.launch(intent);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void uploadStory() {
        String place = binding.etPlace.getText().toString();
        binding.lytPlace.setError(null);

        if (photoURI == null) {
            Toast.makeText(this, "No Image is captured!", Toast.LENGTH_LONG).show();
            return;
        }
        if (place.isEmpty()) {
            binding.lytPlace.setError("Please enter username");
            binding.etPlace.requestFocus();
            return;
        }

        String url = NetworkConfig.BASE_URL + NetworkConfig.STORY_UPLOAD_ENDPOINT;
        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

        try {
            InputStream iStream = getContentResolver().openInputStream(photoURI);
            final byte[] inputData = getBytes(iStream);

            ProgressManager.showSimpleProgressDialog(this, "Uploading the story...", false);

            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                    response -> {
                        ProgressManager.removeSimpleProgressDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));

                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");

                            if (status.equals("1")) {
                                Toast.makeText(StoryUploadActivity.this, message, Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(StoryUploadActivity.this, message, Toast.LENGTH_SHORT).show();
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
                    params.put("name", name);
                    params.put("place", place);
                    params.put("time", time);
                    params.put("profile_image", profileImage);
                    return params;
                }

                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    params.put("filename", new DataPart(timeStamp, inputData));
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

}
