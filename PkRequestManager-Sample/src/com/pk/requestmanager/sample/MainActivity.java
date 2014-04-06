package com.pk.requestmanager.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity
{
	private Button btnBasic;
	private Button btnIntermediate;
	private Button btnAdvanced;
	private Button btnAutomatic;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initViews();
		setListeners();
	}
	
	private void initViews()
	{
		btnBasic = (Button) findViewById(R.id.btnBasic);
		btnIntermediate = (Button) findViewById(R.id.btnIntermediate);
		btnAdvanced = (Button) findViewById(R.id.btnAdvanced);
		btnAutomatic = (Button) findViewById(R.id.btnAutomatic);
	}
	
	private void setListeners()
	{
		btnBasic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, BasicActivity.class));
			}
		});
		btnIntermediate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, IntermediateActivity.class));
			}
		});
		btnAdvanced.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, AdvancedActivity.class));
			}
		});
		btnAutomatic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, AutomaticActivity.class));
			}
		});
	}
}
