
package net.londatiga.android.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Home extends Activity
{
    private Button btnEmpezar;

    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        //Obtengo el nombre del usuario del activity login
        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        String username = extra.getString("user");

        Toast.makeText(this, "Bienvenido " + username, Toast.LENGTH_SHORT).show();

        btnEmpezar = (Button) findViewById(R.id.btnEmpezar); //le doy el id
        btnEmpezar.setOnClickListener(new View.OnClickListener() // si le dan un click al boton
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(Home.this, BluetoothActivity.class);
                startActivity(intent); // mostrar la siguiente pantalla
            }
        });
    }
}
