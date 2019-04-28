package app.hcl.com.testapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class WelcomeActivity extends AppCompatActivity {

    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.option1)
        {
            preferences= getSharedPreferences("MyPrefsFile",0);
            Boolean b=preferences.getBoolean("my_login",true);
            Log.d("My_log",""+b.toString());
            preferences.edit().putBoolean("my_login",false).commit();

            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();

        }
        return  true;
    }
}
