package net.londatiga.android.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView username = findViewById(R.id.username);
        TextView password = findViewById(R.id.password);
        Button loginbtn = (Button) findViewById(R.id.loginbtn);

        loginbtn.setOnClickListener(view ->
        {
            if (username.getText().toString().equals("hqa_soa") && password.getText().toString().equals("hqa_soa"))
            {
                Intent passToInicio = new Intent(Login.this, Home.class);
                passToInicio.putExtra("user", "hqa_soa");
                startActivity(passToInicio);
            } else
            {
                Toast.makeText(Login.this, "LOGIN FAILED!!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
