package net.londatiga.android.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import android.app.Activity;
import android.app.ProgressDialog;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/*********************************************************************************************************
 * Activity Principal de la App. Es la primiera activity que se ejecuta cuando el usuario ingresa a la App
 **********************************************************************************************************/

public class BluetoothActivity extends Activity
{
    public static final int MULTIPLE_PERMISSIONS = 10; // codigo que quieres
    //se crea un array de String con los permisos a solicitar en tiempo de ejecucion
    //Esto se debe realizar a partir de Android 6.0, ya que con verdiones anteriores
    //con solo solicitarlos en el Manifest es suficiente
    String[] permissions = new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE,};
    private TextView txtEstado;
    private Button btnActivar;
    private Button btnEmparejar;
    private Button btnBuscar;
    private ProgressDialog mProgressDlg; //que muestra un cuadro de diálogo con un mensaje de progreso
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>(); //una lista que puede contener objetos de tipo BluetoothDevice
    //Handler que captura los Broadcast que emite el SO al ocurrir los eventos del Bluetooth
    //Siempre se necesita un handler para luego pasarselo al registerReceiver(HANDLER, INTENT QUE CONTIENEN LOS BROADCAST MESSAGES QUE ESCUCHAMOS);
    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {

        //Cada vez que recibe un dispositivo nuevo
        public void onReceive(Context context, Intent intent)
        {

            //Atraves del Intent obtengo el evento de Bluetooth que informo el broadcast del SO
            String action = intent.getAction(); // La accion "deviceFounded" cada vez que encuentra uno

            //showToast("Accion:" + action);

            //Si cambio de estado el Bluetooth (Activado/desactivado)
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
            {
                //showToast("Estado:" + action);
                //Obtengo el parametro, aplicando un Bundle, que me indica el estado del Bluetooth
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                //Si esta activado
                if (state == BluetoothAdapter.STATE_ON)
                {
                    showToast("Bluetooth activado.");
                    showEnabled();
                }
                //Si esta desactivado
                if (state == BluetoothAdapter.STATE_OFF)
                {
                    showToast("Bluetooth desactivado.");
                    showDisabled();
                }
            }
            //Si se inicio la busqueda de dispositivos Bluetooth
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
                //Creo la lista donde voy a mostrar los dispositivos encontrados
                mDeviceList = new ArrayList<BluetoothDevice>();

                //muestro el cuadro de dialogo de busqueda
                mProgressDlg.show();
            }
            //Si finalizo la busqueda de dispositivos Bluetooth
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                //se cierra el cuadro de dialogo de busqueda
                mProgressDlg.dismiss();

                //se inicia el activity DeviceListActivity pasandole como parametros, por intent,
                //el listado de dispositovos encontrados
                Intent newIntent = new Intent(BluetoothActivity.this, DeviceListActivity.class);

                newIntent.putParcelableArrayListExtra("device.list", mDeviceList);

                startActivity(newIntent);
            }
            //si se encontro un dispositivo Bluetooth
            else if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                //Se lo agregan sus datos a una lista de dispositivos encontrados
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                mDeviceList.add(device);
                showToast("Dispositivo encontrado:" + device.getName());
            }
        }
    };
    private BluetoothAdapter mBluetoothAdapter; // proporciona métodos para interactuar con el adaptador Bluetooth del dispositivo
    //Metodo que actua como Listener de los eventos que ocurren en los componentes graficos de la activty
    private View.OnClickListener btnDispositivosEmparejadosListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if (pairedDevices == null || pairedDevices.size() == 0)
            {
                showToast("No se encontraron dispositivos emparejados");
            } else
            {
                ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

                list.addAll(pairedDevices);

                Intent intent = new Intent(BluetoothActivity.this, DeviceListActivity.class);

                intent.putParcelableArrayListExtra("device.list", list);

                startActivity(intent);
            }
        }
    };
    private View.OnClickListener btnBuscarListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mBluetoothAdapter.startDiscovery();
        }
    };
    private View.OnClickListener btnActivarListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (mBluetoothAdapter.isEnabled())
            {
                mBluetoothAdapter.disable();

                showDisabled();
            } else
            {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                startActivityForResult(intent, 1000);
            }
        }
    };
    private DialogInterface.OnClickListener btnCancelarDialogListener = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            dialog.dismiss();

            mBluetoothAdapter.cancelDiscovery();
        }
    };

    @Override
    //Metodo On create
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Se definen los componentes del layout // (layout = estructura visual de la interfaz)
        txtEstado = (TextView) findViewById(R.id.txtEstado);
        btnActivar = (Button) findViewById(R.id.btnActivar);
        btnEmparejar = (Button) findViewById(R.id.btnEmparejar);
        btnBuscar = (Button) findViewById(R.id.btnBuscar);

        //Se crea un adaptador para podermanejar el Bluetooth del celular
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Se Crea la ventana de dialogo que indica que se esta buscando dispositivos Bluetooth
        mProgressDlg = new ProgressDialog(this);

        //Se configura el mProcessDlg, no significa que ya lo esté mostrando.
        mProgressDlg.setMessage("Buscando dispositivos..."); // mostrar al usuario que se está realizando una tarea en segundo plano
        mProgressDlg.setCancelable(false); // Esta línea establece si el diálogo de progreso es cancelable o no

        //se asocia un listener al boton cancelar para la ventana de dialogo que busca los dispositivos Bluetooth
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", btnCancelarDialogListener);

        //
        if (checkPermissions())
        {
            enableComponent();
        }
    }

    protected void enableComponent()
    {
        //se determina si existe Bluetooth en el celular
        if (mBluetoothAdapter == null)
        {
            //si el celular no soporta Bluetooth
            showUnsupported();
        } else
        {
            //si el celular soporta Bluetooth, se definen los listener para los botones de la activity
            btnEmparejar.setOnClickListener(btnDispositivosEmparejadosListener);

            btnBuscar.setOnClickListener(btnBuscarListener);

            btnActivar.setOnClickListener(btnActivarListener);

            //se determina si esta activado el Bluetooth
            if (mBluetoothAdapter.isEnabled())
            {
                //se informa si esta habilitado
                showEnabled();
            } else
            {
                //se informa si esta deshabilitado
                showDisabled();
            }
        }


        //Definiendo BroadcastReceiver
        //se definen un broadcastReceiver que captura el broadcast del SO cuando captura los siguientes eventos:
        IntentFilter filter = new IntentFilter();

        //Todos estos eventos los va a capturar el mReceiver luego
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //Cambia el estado del Bluetooth (Acrtivado /Desactivado)
        filter.addAction(BluetoothDevice.ACTION_FOUND); //Se encuentra un dispositivo Bluetooth al realizar una busqueda
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //Cuando se comienza una busqueda de Bluetooth
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //cuando la busqueda de Bluetooth finaliza

        //se define (registra) el handler(mReceiver) que captura los broadcast anteriormente mencionados.
        registerReceiver(mReceiver, filter);
    }

    @Override
    //Cuando se llama al metodo OnPause se cancela la busqueda de dispositivos Bluetooth
    //Este metodo es llamado al pasar de esta activity a la de DeviceListActivity
    public void onPause()
    {
        if (mBluetoothAdapter != null)
        {
            if (mBluetoothAdapter.isDiscovering())
            {
                mBluetoothAdapter.cancelDiscovery();
            }
        }

        super.onPause();
    }

    @Override
    //Cuando se detruye la Acivity se quita el registro de los brodcast. Apartir de este momento no se
    //recibe mas broadcast del SO. del Bluetooth
    public void onDestroy()
    {
        unregisterReceiver(mReceiver); //Desregistrar BroadcastReceiver

        super.onDestroy();
    }

    private void showEnabled()
    {
        txtEstado.setText("Bluetooth Habilitado");
        txtEstado.setTextColor(Color.WHITE);
        txtEstado.setBackgroundColor(Color.GREEN);

        btnActivar.setText("Desactivar");
        btnActivar.setEnabled(true);

        btnEmparejar.setEnabled(true);
        btnBuscar.setEnabled(true);
    }

    private void showDisabled()
    {
        txtEstado.setText("Bluetooth Deshabilitado");
        txtEstado.setTextColor(Color.WHITE);
        txtEstado.setBackgroundColor(Color.RED);

        btnActivar.setText("Activar");
        btnActivar.setEnabled(true);

        btnEmparejar.setEnabled(false);
        btnBuscar.setEnabled(false);
    }

    private void showUnsupported()
    {
        txtEstado.setText("Bluetooth no es soportado por el dispositivo movil");

        btnActivar.setText("Activar");
        btnActivar.setEnabled(false);

        btnEmparejar.setEnabled(false);
        btnBuscar.setEnabled(false);
    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    //Metodo que chequea si estan habilitados los permisos
    private boolean checkPermissions()
    {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        //Se chequea si la version de Android es menor a la 6
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            return true;
        }


        for (String p : permissions)
        {
            result = ContextCompat.checkSelfPermission(this, p);

            if (result != PackageManager.PERMISSION_GRANTED)
            {
                listPermissionsNeeded.add(p); //Agregamos los permisos que faltan a una lista
            }
        }

        //Solicitamos los permisos necesarios al usuario
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }

        return true;
    }

    //Metodo que chequea que tengamos los permisos, si los tenemos todos nos habilita la activity
    //y sino nos muestra el toast que dice "La app no funcionara correctamente debido a la falta de permisos"
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case MULTIPLE_PERMISSIONS:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permissions granted.
                    enableComponent(); // Now you call here what ever you want :)
                } else
                {
                    StringBuilder perStr = new StringBuilder();
                    for (String per : permissions)
                    {
                        perStr.append("\n").append(per);
                    }
                    // permissions list of don't granted permission
                    Toast.makeText(this, "ATENCION: La aplicacion no funcionara correctamente " +
                            "debido a la falta de Permisos ( " + perStr + " )", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
