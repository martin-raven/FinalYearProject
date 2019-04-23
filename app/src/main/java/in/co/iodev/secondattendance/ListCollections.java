package in.co.iodev.secondattendance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ListCollections extends AppCompatActivity {

    Spinner spinner;
    Button button;
    ArrayList<String> classes=new ArrayList<>();
    ArrayAdapter<String> spinnerAdapter;
    Context context;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_collections);
        context=this;
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        classes.clear();
        classes.add("Empty List");
        spinner = findViewById(R.id.spinner);
        button = findViewById(R.id.button);

        getClasses();

        spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, classes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("SelectClass",classes.get(i));

                editor.putString("CollectionId",classes.get(i));
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent CameraActivity = new Intent(getBaseContext(), in.co.iodev.secondattendance.CameraActivity.class);
                    startActivity(CameraActivity);
            }
        });
    }

    public void getClasses() {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://m93y8bihcj.execute-api.ap-south-1.amazonaws.com/Dev/get-collections";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("SelectClass", response.toString());
                        try {
                            JSONArray jsonArr = new JSONArray(response.getString("Collections"));
                            ArrayList<String> list = new ArrayList<String>();
                            for(int i = 0; i < jsonArr.length(); i++){
                                list.add(jsonArr.get(i).toString());
                            }
                            classes = list;
                            spinnerAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, classes);
                            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(spinnerAdapter);
                            Log.d("SelectClass",classes.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("SelectClassError", error.toString());
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }
}

