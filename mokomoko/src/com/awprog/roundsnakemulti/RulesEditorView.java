package com.awprog.roundsnakemulti;

import java.lang.reflect.Field;

import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class RulesEditorView extends TableLayout {

	public RulesEditorView(Context context) {
		super(context);
	}
	
	public void build(final Rules.Values rules) {
		this.removeAllViews();
		
		for(final Field f : Rules.Values.class.getDeclaredFields()) {
			Log.i("###", "Declared Field : "+f.getName());
			
			TableRow row = new TableRow(getContext());
			
			TextView fieldName = new TextView(getContext());
			fieldName.setText(f.getName());
			row.addView(fieldName);
			
			final EditText edit = new EditText(getContext());
			row.addView(edit);
			
			Button iczBtn = new Button(getContext());
			iczBtn.setText("+");
			Button dczBtn = new Button(getContext());
			dczBtn.setText("-");
			
			try {
				edit.setText("...");
				String type = Rules.Values.class.getDeclaredField(f.getName()).getType().getName();
				
				/// STRING
				if(type.equals("java.lang.String")) {
					edit.setInputType(InputType.TYPE_CLASS_TEXT);
					edit.setText((String)Rules.Values.class.getDeclaredField(f.getName()).get(rules));
				}
				
				/// INTEGER
				else if(type.equals("int")) {
					edit.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);
					edit.setText(""+Rules.Values.class.getDeclaredField(f.getName()).getInt(rules));
					OnClickListener listener = new OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								int val = ((Button)v).getText().toString().equals("-") ? -1 : +1;
								int result = Rules.Values.class.getDeclaredField(f.getName()).getInt(rules) + val;
								Rules.Values.class.getDeclaredField(f.getName()).setInt(rules, result);
								edit.setText(""+Rules.Values.class.getDeclaredField(f.getName()).getInt(rules));
							}
							catch (NoSuchFieldException e) { e.printStackTrace(); }
							catch (IllegalAccessException e) { e.printStackTrace(); }
							catch (IllegalArgumentException e) { e.printStackTrace(); }
						}
					};
					iczBtn.setOnClickListener(listener);
					dczBtn.setOnClickListener(listener);
					row.addView(iczBtn);
					row.addView(dczBtn);
				}
				
				/// FLOAT
				else if(type.equals("float")) {
					edit.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED|InputType.TYPE_NUMBER_FLAG_DECIMAL);
					edit.setText(""+Rules.Values.class.getDeclaredField(f.getName()).getFloat(rules));
					OnClickListener listener = new OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								float val = ((Button)v).getText().toString().equals("-") ? -0.1f : +0.1f;
								float result = Rules.Values.class.getDeclaredField(f.getName()).getFloat(rules) + val;
								Rules.Values.class.getDeclaredField(f.getName()).setFloat(rules, result);
								edit.setText(""+Rules.Values.class.getDeclaredField(f.getName()).getFloat(rules));
							}
							catch (NoSuchFieldException e) { e.printStackTrace(); }
							catch (IllegalAccessException e) { e.printStackTrace(); }
							catch (IllegalArgumentException e) { e.printStackTrace(); }
						}
					};
					iczBtn.setOnClickListener(listener);
					dczBtn.setOnClickListener(listener);
					row.addView(iczBtn);
					row.addView(dczBtn);
				}
			}
			catch (NoSuchFieldException e) { e.printStackTrace(); }
			catch (IllegalAccessException e) { e.printStackTrace(); }
			catch (IllegalArgumentException e) { e.printStackTrace(); }
			
			edit.setImeOptions(EditorInfo.IME_ACTION_DONE);
			edit.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if(actionId == EditorInfo.IME_ACTION_DONE) {
						try {
							switch(v.getInputType()) {
							/// STRING
							case InputType.TYPE_CLASS_TEXT:
								Rules.Values.class.getDeclaredField(f.getName()).set(rules, v.getText().toString());
								break;
								
							/// INTEGER
							case InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED:
								try {
									Rules.Values.class.getDeclaredField(f.getName()).setInt(rules, Integer.parseInt(v.getText().toString()));
								}
								catch(NumberFormatException e) {}
								finally {
									v.setText(""+Rules.Values.class.getDeclaredField(f.getName()).getInt(rules));
								}
								break;
								
							/// FLOAT
							case InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED|InputType.TYPE_NUMBER_FLAG_DECIMAL:
								try {
									Rules.Values.class.getDeclaredField(f.getName()).setFloat(rules, Float.parseFloat(v.getText().toString()));
								}
								catch(NumberFormatException e) {}
								finally {
									v.setText(""+Rules.Values.class.getDeclaredField(f.getName()).getFloat(rules));
								}
								break;
							}
						}
						catch (NoSuchFieldException e) { e.printStackTrace(); }
						catch (IllegalAccessException e) { e.printStackTrace(); }
						catch (IllegalArgumentException e) { e.printStackTrace(); }
						
						Log.i("###", ""+f.getName()+ " set to "+v.getText());
					}
					return false;
				}
			});
			
			
			this.addView(row);
		}
	}

}
