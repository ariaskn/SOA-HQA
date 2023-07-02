package net.londatiga.android.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView username = findViewById(R.id.username);
        TextView password = findViewById(R.id.password);
        Button loginbtn = (Button) findViewById(R.id.loginbtn);

        loginbtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (username.getText().toString().equals("admin")
                         && password.getText().toString().equals("admin") ) {
                            Toast.makeText(MainActivity.this, "LOGIN SUCCESSFUL", Toast.LENGTH_SHORT).show();
                            Intent passToMainActivityOld = new Intent(MainActivity.this, Inicio.class);
                            startActivity(passToMainActivityOld);
                        }else {
                            Toast.makeText(MainActivity.this, "LOGIN FAILED!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );


    }
}