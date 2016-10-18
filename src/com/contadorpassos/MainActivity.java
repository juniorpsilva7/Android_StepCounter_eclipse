package com.contadorpassos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity implements SensorEventListener{

	private int mStepValue;
	private int StepAux;
	private final static int SENSITIVITY = 12;
	
	private boolean mInitialized;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	
	//variavel booleana para deixar o serviço do acelerometro só iniciar quando clicar em START
	public boolean proginiciado = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mStepValue = 0;
		setContentView(R.layout.activity_main);
		mInitialized = false;
		
		AlertDialog.Builder msg1 = new AlertDialog.Builder(MainActivity.this);
		msg1.setTitle("Bluetooth");
		msg1.setMessage("Neste momento seria conectado o Bluetooth com o Micro e ele iria converter para serial do Arduino");
		msg1.setNeutralButton("Ok", null);
		msg1.show();
		
		
		
		//Pega a instância do sensor de aceleração padrão
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
				
		final Button startbutton = (Button) findViewById(R.id.btnStart);
        startbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // ação ao clicar
            	proginiciado = true; // seta o booleano informando que iniciou a aplicação
            	TextView numberSteps = (TextView)findViewById( R.id.txtCount);
            	numberSteps.setText(Integer.toString(0));
            	//na função OnResume() ele inicia a instância do sensor acelerômetro
            	onResume();
            }
        });
        
        final Button stopbutton = (Button) findViewById(R.id.btnStop);
        stopbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // ação ao clicar
            	TextView numberSteps = (TextView)findViewById( R.id.txtCount);
            	numberSteps.setText(Integer.toString(0));
            	//na função OnPause() ele pausa a instância do sensor acelerômetro
            	onPause();
            }
        });
        
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
	  TextView numberSteps = (TextView)findViewById( R.id.txtCount);
  	  numberSteps.setText(numberSteps.getText()); 
  	  System.out.println("CHAANGED");
	}

	protected void onResume() {
		super.onResume();
		// se o botão START foi clicado
		if(proginiciado){
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		}else{
			onPause();
		}
	}

	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onSensorChanged(SensorEvent event){
		// alpha é calculado como t / (t + dT)
		// com t sendo a constante tempo do filtro low-pass
		// e dT sendo a taxa de entrega do evento
		
		TextView numberSteps = (TextView)findViewById( R.id.txtCount);


		if (!mInitialized) {
			mInitialized = true;
		} else {
			float[] gravity = new float[3];
			float[] linear_acceleration = new float[3];

			final float alpha = (float) 0.8;

			// isola a força da gravidade  com o filtro low-pass
			// em cada eixo
			gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
			gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
			gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
			
			// remove a contribuição da gravidade com o filtro high-pass
			// em cada eixo
			linear_acceleration[0] = event.values[0] - gravity[0];
			linear_acceleration[1] = event.values[1] - gravity[1];
			linear_acceleration[2] = event.values[2] - gravity[2];
			

			mStepValue = Integer.parseInt((String) numberSteps.getText());
			// incrementa a variavel que conta a quantidade de passos se
			// as acelerações lineares em todos os eixos forem maior que
			// a variavel SENSIVITY e todas juntas forem maior que zero
			if(linear_acceleration[0] > SENSITIVITY || linear_acceleration[1] > SENSITIVITY || linear_acceleration[2] > SENSITIVITY && linear_acceleration[2]>0){
				numberSteps.setText(Integer.toString(++mStepValue));
				StepAux++;
				if (StepAux == 20){
					AlertDialog.Builder msg2 = new AlertDialog.Builder(MainActivity.this);
					msg2.setTitle("Relatorio");
					msg2.setMessage("Neste momento seria enviado um relatório para alguma unidade de armazenamento para fins de relatórios médicos.");
					msg2.setNeutralButton("Ok", null);
					msg2.show();
					StepAux = 0;
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy){

	}


}
