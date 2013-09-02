package com.examples.app_2.activities;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.app_2.R;

public class SimpleClientActivity extends Activity{
	private Socket client;
	private PrintWriter printwriter;
	private EditText textField;
	private Button button;
	private String message;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_client);
		
		textField = (EditText) findViewById(R.id.message);
		button =  (Button) findViewById(R.id.send_button);
		
	}
	
	public void onClick(View view){
		switch(view.getId()){
			case R.id.send_button:
				message = textField.getText().toString();
				textField.setText("");
				
				try{
					client  = new Socket("192.168.0.103", 4444);
					printwriter = new PrintWriter(client.getOutputStream(),true);
					printwriter.write(message);
					
					printwriter.flush();
					printwriter.close();
					client.close(); //closing the connection
					
					
				}catch(UnknownHostException e){
					e.printStackTrace();
				}catch(IOException e){
					e.printStackTrace();
				}
			break;
			default:
				break;
		}
	}

}
