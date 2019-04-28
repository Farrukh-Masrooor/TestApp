package app.hcl.com.testapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {

    EditText email,password;
    TextView textView;
    final String PREFS_NAME = "MyPrefsFile";
    final String Login_Prefs = "My_Login_Prefs";
    SharedPreferences settings;
    private String pass,mail;
    String regex ="[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    String weburl= "https://192.168.225.38/connection/loginDetails.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email=findViewById(R.id.user_email);
        password=findViewById(R.id.user_password);
        textView=findViewById(R.id.errorText);
        handleSSLHandshake();
        isAlreadyLogin();
    }

    public void isAlreadyLogin() {

        settings = getSharedPreferences(PREFS_NAME, 0);

        if (settings.getBoolean("my_login", false)) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
        }
    }
    public void launchRegister(View view)
    {
        FragmentManager manager=getSupportFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();
        transaction.replace(R.id.relativelayout,new RegisterFragment());
        transaction.addToBackStack("frag");
        transaction.commit();
    }


    public void  login(View view)
    {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        ConnectivityManager cm =
                (ConnectivityManager)getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isConnected==true)
            getData();
        else
            Toast.makeText(this,"no internet",Toast.LENGTH_SHORT).show();
    }
    public void getData()
    {


        mail=email.getText().toString();


        pass=password.getText().toString();


        if ( pass.equals("") || mail.equals("") ) {
            Toast.makeText(this, "Fields are Empty", Toast.LENGTH_SHORT).show();
        }
        else if (!mail.trim().matches(regex))
        {
            textView.setText("invalid email format");
            textView.setTextColor(Color.RED);
            textView.setVisibility(View.VISIBLE);
            email.setText("");

        }
        else
            new Connect().execute();


    }



    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }



    class Connect extends AsyncTask
    {

        @Override
        protected Object doInBackground(Object[]objects) {



            String forecastJsonStr = "2";
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
                if (result != null) {
                    if(result.trim().equals("1")){
                    Log.d("My_log", "Error closing stream");
                    Toast.makeText(getApplicationContext(), "login successful", Toast.LENGTH_SHORT).show();
                    launchnewActivity();}

                    else if (result!=null && result.trim().equals("2"))
                        Toast.makeText(getApplicationContext(),"cant connect to server",Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(),"does not exsists",Toast.LENGTH_SHORT).show();
                }

            }
        }

        public void launchnewActivity()
        {

            settings.edit().putBoolean("my_login", true).commit();
            Intent intent=new Intent(getApplicationContext(),WelcomeActivity.class);
            //intent.putExtra("user_id",key);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
          finish();
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
