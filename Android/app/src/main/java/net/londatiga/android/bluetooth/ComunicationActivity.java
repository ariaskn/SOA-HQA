package net.londatiga.android.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/*********************************************************************************************************
 * Activity que muestra realiza la comunicacion con Arduino
 **********************************************************************************************************/

//******************************************** Hilo principal del Activity**************************************
public class ComunicationActivity extends Activity implements SensorEventListener
{
    // SPP UUID service  - Funciona en la mayoria de los dispositivos
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Cadena para dirección MAC del Hc05
    private static String address = null;
    final int handlerState = 0; //utilizado para identificar el mensaje del controlador
    SensorManager sm;
    Sensor sensor;
    Button btnNPAA;
    Button btnESE;
    Button btnNEI;
    Button btnNMEEEM;
    Button btnE5MTA;
    Button btnLAC;
    Button btnNM;
    Button btnREINICIAR;
    // Handler en Android es una clase que permite enviar y procesar mensajes y tareas en un hilo o hilo de ejecución específico.
    Handler bluetoothIn; //Este bluetoothIn se va a usar en el hilo ConnectedThread.
    private MediaPlayer mediaPlayer; //Objeto para reproducir el sonido del timbre
    private BluetoothAdapter btAdapter = null; // BluetoothAdapter es una clase fundamental en Android que representa el adaptador Bluetooth del dispositivo.
    private BluetoothSocket btSocket = null; // BluetoothSocket en Android representa un socket Bluetooth, que es una conexión de comunicación entre dos dispositivos Bluetooth
    //Connected Thread es una clase que creamos nosotros, un thread.
    private ConnectedThread mConnectedThread; // ConnectedThread se refiere a una clase o componente que maneja la comunicación en un hilo de ejecución separado
    //Listener del boton enviar que envia  msj para selecionar un mensaje del TecladoMatricial a Arduino atraves del Bluethoot
    private View.OnClickListener btnNPAAListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mConnectedThread.write(new String("1"));

            showToast("Mensaje enviado");
        }
    };
    //Listener del boton enviar que envia  msj para selecionar un mensaje del TecladoMatricial a Arduino atraves del Bluethoot
    private View.OnClickListener btnESEListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mConnectedThread.write(new String("2"));

            showToast("Mensaje enviado");
        }
    };
    private View.OnClickListener btnNEIListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mConnectedThread.write(new String("3"));

            showToast("Mensaje enviado");
        }
    };
    private View.OnClickListener btnNMEEEMListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mConnectedThread.write(new String("4"));

            showToast("Mensaje enviado");
        }
    };
    private View.OnClickListener btnE5MTAListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mConnectedThread.write(new String("5"));

            showToast("Mensaje enviado");
        }
    };
    private View.OnClickListener btnLACListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mConnectedThread.write(new String("6"));

            showToast("Mensaje enviado");
        }
    };
    private View.OnClickListener btnNMListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mConnectedThread.write(new String("*"));

            showToast("Mensaje enviado");
        }
    };
    private View.OnClickListener btnREINICIARListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mConnectedThread.write(new String("#"));

            showToast("Mensaje enviado");
        }
    };

    protected void Ini_Sensores()
    {
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Metodo para parar la escucha de los sensores
    private void Parar_Sensores()
    {
        sm.unregisterListener(this, sm.getDefaultSensor(Sensor.TYPE_PROXIMITY));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunicacion);
        mediaPlayer = MediaPlayer.create(this, R.raw.timbre);
        //Se definen los componentes del layout
        btnNPAA = (Button) findViewById(R.id.NPAA);
        btnESE = (Button) findViewById(R.id.ESE);
        btnNEI = (Button) findViewById(R.id.NEI);
        btnNMEEEM = (Button) findViewById(R.id.NMEEEM);
        btnE5MTA = (Button) findViewById(R.id.E5MTA);
        btnLAC = (Button) findViewById(R.id.LAC);
        btnNM = (Button) findViewById(R.id.NM);
        btnREINICIAR = (Button) findViewById(R.id.REINICIAR);

        //obtengo el adaptador del bluethoot
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //defino el Handler de comunicacion entre el hilo Principal  el secundario.
        //El hilo secundario va a mostrar informacion al layout atraves utilizando indeirectamente a este handler
        bluetoothIn = Handler_Msg_Hilo_Principal();

        //defino los handlers para los botones Apagar y encender
        btnNPAA.setOnClickListener(btnNPAAListener);
        btnESE.setOnClickListener(btnESEListener);
        btnNEI.setOnClickListener(btnNEIListener);
        btnNMEEEM.setOnClickListener(btnNMEEEMListener);
        btnE5MTA.setOnClickListener(btnE5MTAListener);
        btnLAC.setOnClickListener(btnLACListener);
        btnNM.setOnClickListener(btnNMListener);
        btnREINICIAR.setOnClickListener(btnREINICIARListener);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    @Override
    //Cada vez que se detecta el evento OnResume se establece la comunicacion con el HC05, creando un
    //socketBluethoot
    public void onResume()
    {
        Ini_Sensores();
        super.onResume();

        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        Intent intent = getIntent(); //Obtengo el intent que levantó esta activity
        Bundle extras = intent.getExtras();

        //Obtiene la direccion bluetooth que se definio en DeviceListActivity
        address = extras.getString("Direccion_Bluetooth");

        //Obtengo el arduino
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        //Creamos el socket para conectarnos con el arduino
        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e)
        {
            e.printStackTrace();
            showToast("La creacción del Socket fallo");
        }

        //Establecemos la conexión con el arduino
        try
        {
            btSocket.connect();
        } catch (IOException e)
        {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                e2.printStackTrace();
            }
        }

        //Una establecida la conexion con el Hc05 se crea el hilo secundario, el cual va a recibir
        // los datos de Arduino atraves del bluethoot

        //Creamos un hilo secundario para la conexión
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //Envío un carácter al reanudar para verificar que el dispositivo esté conectado
        //Si el dispositivo no esta conectado, el método "write" lanza una excepción y se llamará a finish() que finaliza la aplicación
        mConnectedThread.write("x");
    }

    @Override
    //Cuando se ejecuta el evento onPause se cierra el socket Bluethoot, para no recibiendo datos
    public void onPause()
    {
        Parar_Sensores();
        super.onPause();
        try
        {
            //Al salir de la activity, cerramos la conexión.
            btSocket.close();
        } catch (IOException e2)
        {
            e2.printStackTrace();
        }
    }

    @Override
    protected void onStop()
    {

        Parar_Sensores();

        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Parar_Sensores();

        super.onDestroy();
    }

    @Override
    protected void onRestart()
    {
        Ini_Sensores();

        super.onRestart();
    }

    //Metodo que crea el socket bluethoot
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    //Handler que sirve que permite mostrar datos en el Layout al hilo secundario
    @SuppressLint("HandlerLeak")
    private Handler Handler_Msg_Hilo_Principal()
    {
        return new Handler()
        {
            //Sobrescribe el método handleMessage para manejar los mensajes enviados al hilo principal
            @SuppressLint("HandlerLeak")
            public void handleMessage(android.os.Message msg)
            {
                //Si se recibio un msj del hilo secundario
                if (msg.what == handlerState) // si msg.what = 0 -> el what es para identificar tipos de mensaje. User-defined message code so that the recipient can identify what this message is about.
                {
                    String readMessage = (String) msg.obj; // obj es un campo de tipo genérico que puede contener cualquier objeto.
                    String timbre = new String(readMessage);

                    if (timbre.equals("1"))
                    {
                        showToast("Tocan timbre!!!");
                        mediaPlayer.start();
                    }
                }
            }
        };

    }

    private void showToast(String message) // es un método personalizado que se utiliza para mostrar un mensaje emergente (toast)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show(); // el mensaje que se desea mostrar y la duración del mensaje
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {

        synchronized (this)
        {
            mConnectedThread.write(new String("5"));
            showToast("Mensaje enviado");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {
    }

    //******************************************** Hilo secundario del Activity**************************************
    //*************************************** recibe los datos enviados por el HC05**********************************

    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream; // InputStream es una clase abstracta en Java que proporciona una
        // interfaz para leer datos de una fuente de entrada, como un archivo o un flujo de red
        private final OutputStream mmOutStream;

        //Constructor de la clase del hilo secundario
        // El socket llega ya conectado al bluetooth
        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {
                //Crear flujos de E/S para la conexión
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //metodo run del hilo, que va a entrar en una espera activa para recibir los msjs del HC05
        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            //el hilo secundario se queda esperando mensajes del HC05
            while (true)
            {
                try
                {
                    //se leen los datos del Bluethoot
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);

                    //se muestran en el layout de la activity, utilizando el handler del hilo
                    // principal antes mencionado
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget(); // crea un mensaje para enviar
                    // información desde el hilo de conexión Bluetooth (bluetoothIn) al hilo principal de la interfaz de usuario.
                } catch (IOException e)
                {
                    e.printStackTrace();
                    break;
                }
            }
        }


        //método de escritura
        public void write(String input)
        {
            byte[] msgBuffer = input.getBytes(); //convierte la cadena ingresada en bytes
            try
            {
                mmOutStream.write(msgBuffer);    //escribir bytes sobre la conexión BT a través de outstream
            } catch (IOException e)
            {
                e.printStackTrace();
                //si no puede escribir, cierre la aplicación
                showToast("La conexion fallo");
                finish();
            }
        }
    }

}
