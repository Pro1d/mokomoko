package com.awprog.roundsnakemulti.gamepad;

import java.util.Random;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.awprog.roundsnakemulti.R;
import com.fbessou.sofa.GamePadIOClient.ConnectionStateChangedListener;
import com.fbessou.sofa.GamePadIOHelper;
import com.fbessou.sofa.GamePadInformation;
import com.fbessou.sofa.indicator.FeedbackIndicator;
import com.fbessou.sofa.indicator.Indicator;
import com.fbessou.sofa.sensor.Analog2DSensor;
import com.fbessou.sofa.sensor.KeySensor;
import com.fbessou.sofa.sensor.Sensor;
import com.fbessou.sofa.view.JoystickView;

public class GamePadActivity extends Activity implements ConnectionStateChangedListener {
	GamePadInformation infos;
	GamePadIOHelper easyIO;
	
	ToggleButton connectionStateView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gamepad_layout);
		
		infos = new GamePadInformation(getNameFromPreferences(), getUUIDFromPreferences());
		easyIO = new GamePadIOHelper(this, infos);
		easyIO.start(this);

		easyIO.attachSensor(new Analog2DSensor(Sensor.ANALOG_CATEGORY_VALUE+1, (JoystickView)findViewById(R.id.joystick)));
		easyIO.attachSensor(new KeySensor(Sensor.KEY_CATEGORY_VALUE+1, findViewById(R.id.action_button)));
		easyIO.attachIndicator(new FeedbackIndicator(this, Indicator.FEEDBACK_CATEGORY_VALUE+1));
		
		connectionStateView = (ToggleButton) findViewById(R.id.connection_state);
		onDisconnectedFromProxy();
	}
	
	private UUID getUUIDFromPreferences() {
		SharedPreferences prefs = getSharedPreferences("UUID", Context.MODE_PRIVATE);
		String s = prefs.getString("UUID", null);
		try {
			return UUID.fromString(s);
		} catch(Exception e) {
			// UUID not found in prefs
			UUID uuid = UUID.randomUUID();
			prefs.edit().putString("UUID", uuid.toString()).commit();
			return uuid;
		}
	}
	private String getNameFromPreferences() {
		SharedPreferences prefs = getSharedPreferences("user", Context.MODE_PRIVATE);
		String s = prefs.getString("name", null);
		if(s != null)
			return s;
		else {
			// user name not found in prefs, select one randomly
			Random rand = new Random();
			String sampleName[] = {"Jojo", "Dyh", "Brian", "Moggs", "Lumbys", "Skrex", "Xazz", "Gloovas", "Toll", "Vahn"};
			s = sampleName[rand.nextInt(sampleName.length)];
			prefs.edit().putString("name", s).commit();
			return s;
		}
	}
	private void saveNameInPreferences(String name) {
		SharedPreferences prefs = getSharedPreferences("user", Context.MODE_PRIVATE);
		prefs.edit().putString("name", name).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Add an option to change the user's nickname in the menu
		menu.add(42, 42, 0, "Change nickname");
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(item.getItemId() == 42)
			changeNamePrompt();
		return super.onMenuItemSelected(featureId, item);
	}
	private void changeNamePrompt() {
		// This edit text is the content of the dialog
		final EditText edit = new EditText(this);
		edit.setText(infos.getNickname());
		
		// Build a dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Change your nickname");
		builder.setView(edit);
		builder.setPositiveButton("Change", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				infos.setNickname(edit.getText().toString());
				saveNameInPreferences(infos.getNickname());
				easyIO.updateInformation(infos);
			}
		});
		builder.setNegativeButton("Cancel", null);
		
		// Display the dialog and let the user enter his new nickname
		builder.show();
	}

	@Override
	public void onConnectedToProxy() {
		connectionStateView.setEnabled(true);
		connectionStateView.setChecked(false);
	}
	@Override
	public void onConnectedToGame() {
		connectionStateView.setEnabled(true);
		connectionStateView.setChecked(true);
	}
	@Override
	public void onDisconnectedFromGame() {
		connectionStateView.setEnabled(true);
		connectionStateView.setChecked(false);
	}
	@Override
	public void onDisconnectedFromProxy() {
		connectionStateView.setEnabled(false);
		connectionStateView.setChecked(false);
		
	}

}
