
package net.londatiga.android.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Home extends Activity
{
    private Button btnEmpezar;

    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

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
