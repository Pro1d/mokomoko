package com.awprog.roundsnakemulti;

import java.lang.reflect.Field;

import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
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
			
			EditText edit = new EditText(getContext());
			try {
				edit.setText("...");
				String type = Rules.Values.class.getDeclaredField(f.getName()).getType().getName();
				if(type.equals("java.lang.String")) {
					edit.setInputType(InputType.TYPE_CLASS_TEXT);
					edit.setText((String)Rules.Values.class.getDeclaredField(f.getName()).get(rules));
				} else if(type.equals("int")) {
					edit.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);
					edit.setText(""+Rules.Values.class.getDeclaredField(f.getName()).getInt(rules));
				} else if(type.equals("float")) {
					edit.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED|InputType.TYPE_NUMBER_FLAG_DECIMAL);
					edit.setText(""+Rules.Values.class.getDeclaredField(f.getName()).getFloat(rules));
				}
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			edit.setImeOptions(EditorInfo.IME_ACTION_DONE);
			edit.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if(actionId == EditorInfo.IME_ACTION_DONE) {
						try {
							switch(v.getInputType()) {
							case InputType.TYPE_CLASS_TEXT:
								Rules.Values.class.getDeclaredField(f.getName()).set(rules, v.getText().toString());
								break;
							case InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED:
								Rules.Values.class.getDeclaredField(f.getName()).setInt(rules, Integer.parseInt(v.getText().toString()));
								break;
							case InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED|InputType.TYPE_NUMBER_FLAG_DECIMAL:
								Rules.Values.class.getDeclaredField(f.getName()).setFloat(rules, Float.parseFloat(v.getText().toString()));
								break;
							}
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (NoSuchFieldException e) {
							e.printStackTrace();
						}
						Log.i("###", ""+f.getName()+ " set to "+v.getText());
						return false;
					}
					return false;
				}
			});
			
			row.addView(edit);
			
			this.addView(row);
		}
	}

}
