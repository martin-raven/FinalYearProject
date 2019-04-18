package in.co.iodev.secondattendance;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.PendingIntent.getActivity;
import static android.os.Environment.getExternalStoragePublicDirectory;

public class MainActivity extends AppCompatActivity {

    Button takePicture;
    Button uploadPicture;
    TextView resulttv;
    ImageView displayImage;
    String pathToFile,imageurl;
    Bitmap bitimage;
    Bitmap thumbnail;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePicture = (Button) findViewById(R.id.button);

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }

        displayImage = (ImageView) findViewById(R.id.imageView);
        uploadPicture = (Button) findViewById(R.id.upload_button);
        resulttv = (TextView) findViewById(R.id.textView);
        bitimage = null;

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                getPictureFromCamera();
//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, 1);
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                imageUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 1);
            }
        });

        uploadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadPictureToWeb();
            }
        });

    }

    private void uploadPictureToWeb() {
        if(bitimage!=null) {
            try {
                String image64 = convertBase64(bitimage);
//                String URL = "https://m93y8bihcj.execute-api.ap-south-1.amazonaws.com/Dev/storeimage";
                String URL="https://m93y8bihcj.execute-api.ap-south-1.amazonaws.com/Dev/reconapi";
                RequestQueue requestQueue = Volley.newRequestQueue(this);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("Image", image64);
                final String requestBody = jsonBody.toString();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.i("Response", response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", error.toString());
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return requestBody == null ? null : requestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                            return null;
                        }
                    }


                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        String responseString = "";
                        String responseData = "";
                        if (response != null) {
                            responseString = new String(response.data);
                            Log.d("Response String",responseString);
                            JSONObject parsed=null;
                            try {
                                parsed = new JSONObject(responseString);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.i("resultDataString",responseString);
                            if (parsed != null) {
                                Log.i("resultData",parsed.toString());
                            }

                            if(parsed.has("Names")) {

                                try {
                                    if(parsed.getString("Names").equals("[]"))
                                        resulttv.setText("No face detected");
                                    else{
                                        final String Text=parsed.getString("Names");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                resulttv.setText(Text);
                                            }
                                        });
                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                resulttv.setText("No face detected");
                            }
                            // can get more details such as response.headers
                        }
                        return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                    }
                };
                int socketTimeout = 360000;//30 seconds - change to what you want
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                stringRequest.setRetryPolicy(policy);
                requestQueue.add(stringRequest);
//                requestQueue.add(stringRequest);
//
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.i("Error","No image");
        }
    }

    private String convertBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    thumbnail = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), imageUri);
                    displayImage.setImageBitmap(thumbnail);
                    imageurl = getRealPathFromURI(imageUri);
                    bitimage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }


// super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode == RESULT_OK) {
//            if(requestCode == 1) {
//                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
//                bitimage = bitmap;
//                displayImage.setImageBitmap(bitmap);
//            }
//        }
//    }

    private void getPictureFromCamera() {
        Intent takePic =  new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePic.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createPhotoFile();
            }
            catch (Exception e) {

            }

            if(photoFile!=null) {
                pathToFile  = photoFile.getAbsolutePath();
                Uri photoUri = FileProvider.getUriForFile(MainActivity.this,"sadas", photoFile);
                takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePic,1);
            }
        }
    }

    private File createPhotoFile() throws IOException {
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(name, ".jpg", storageDir);
        }
        catch (Exception e) {
            Log.d("Picture", "Excep: "+e.toString());
        }
        return image;
    }
}