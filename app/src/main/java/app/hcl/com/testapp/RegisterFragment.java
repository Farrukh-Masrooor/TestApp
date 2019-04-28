package app.hcl.com.testapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class RegisterFragment extends Fragment implements View.OnClickListener{

    EditText username,password,email,number;
    String name,pass,mail,no;
    SharedPreferences settings;
    TextView textView;
    String regex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    String weburl= "https://192.168.225.38/connection/insertUserDetail.php";
    RequestQueue requestQueue;
    Button button;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.register_fragment,container,false);
        username=view.findViewById(R.id.user_name);
        email=view.findViewById(R.id.user_email);
        password=view.findViewById(R.id.user_password);
        number=view.findViewById(R.id.user_no);
        textView=view.findViewById(R.id.errorText);
        button=view.findViewById(R.id.registerButton);
        button.setOnClickListener(this);
        requestQueue= Volley.newRequestQueue(getContext());
        settings = getContext().getSharedPreferences("MyPrefsFile", 0);
        return view;
    }

    public void getData()
    {

        name=username.getText().toString();
        mail=email.getText().toString();

        no=number.getText().toString();
        pass=password.getText().toString();

        no=PhoneNumberUtils.formatNumber(no).toString();
        Log.d("My_log","phone ==="+no);
        if ((name.equals("") || pass.equals("") || mail.equals("") || no.equals(""))) {
            Toast.makeText(getContext(), "Fields are Empty", Toast.LENGTH_SHORT).show();
        }
        else if (!mail.trim().matches(regex) )
        {
         textView.setText("invalid email format or phone number");
         textView.setTextColor(Color.RED);
         textView.setVisibility(View.VISIBLE);
         email.setText("");

        }

        else if (!(no.length()==10))
        {
            textView.setText("invalid email format or phone number");
            textView.setTextColor(Color.RED);
            textView.setVisibility(View.VISIBLE);
           number.setText("");

        }
        else
            new Connect().execute();


    }
    @Override
    public void onClick(View v) {

        InputMethodManager inputManager = (InputMethodManager)
               getActivity(). getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        ConnectivityManager cm =
                (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isConnected==true)
            getData();
        else
            Toast.makeText(getActivity(),"no internet",Toast.LENGTH_SHORT).show();
    }




    class Connect extends AsyncTask
    {

        @Override
        protected Object doInBackground(Object[]objects) {

            String forecastJsonStr ="2";

            if (isConnectedToServer(weburl,1000)) {


                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                Log.d("My_log", "inside asynctask");
                try {

                    URL url = new URL(weburl);
                    // Create the request to OpenWeatherMap, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.connect();


                        Uri.Builder builder = new Uri.Builder()
                                .appendQueryParameter("user_name", name)
                                .appendQueryParameter("user_mobile", no)
                                .appendQueryParameter("user_email", mail)
                                .appendQueryParameter("user_password", pass);


                        String query = builder.build().getEncodedQuery();
                        byte[] outputBytes = query.getBytes();
                        OutputStream os = urlConnection.getOutputStream();
                        os.write(outputBytes);
                        os.close();
                        // Read the input stream into a String
                        InputStream inputStream = urlConnection.getInputStream();
                        StringBuffer buffer = new StringBuffer();
                        if (inputStream == null) {
                            // Nothing to do.
                            return null;
                        }
                        reader = new BufferedReader(new InputStreamReader(inputStream));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            buffer.append(line + "\n");
                        }

                        if (buffer.length() == 0) {
                            // Stream was empty.  No point in parsing.
                            return null;
                        }
                        forecastJsonStr = buffer.toString();
                        Log.d("My_log", "response===" + forecastJsonStr);
                        return forecastJsonStr;


                } catch (IOException e) {
                    Log.d("My_log", "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attemping
                    // to parse it.
                    return null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.d("My_log", "Error closing stream", e);
                        }
                    }
                }
            }
            return forecastJsonStr;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (o!=null) {
                String result = o.toString();

                Log.d("My_log", "result====" + result);
                if (result != null && result.trim().equals("1")) {
                    Toast.makeText(getContext(), "register successfull", Toast.LENGTH_SHORT).show();
                    launchnewActivity();
                }
                else if (result!=null && result.trim().equals("2"))
                    Toast.makeText(getContext(),"cant connect to server",Toast.LENGTH_SHORT).show();
            }
        }

        public void launchnewActivity()
        {

            settings.edit().putBoolean("my_login", true).commit();
            Intent intent = new Intent(getContext(), WelcomeActivity.class);
            //intent.putExtra("user_id",key);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            getActivity().finish();
        }

        public boolean isConnectedToServer(String url, int timeout) {
            try{
                URL myUrl = new URL(url);
                URLConnection connection = myUrl.openConnection();
                connection.setConnectTimeout(timeout);
                connection.connect();
                return true;
            } catch (Exception e) {
                // Handle your exceptions
                Log.d("My_log","eroor is ===="+e);

                return false;
            }
        }
    }
}
