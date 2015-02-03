package com.awprog.roundsnakemulti;

import java.util.HashSet;

import android.os.Handler;
import android.os.Message;
import android.util.SparseIntArray;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class RemoteManager {
	private HashSet<Integer> padId_list = new HashSet<Integer>();
	private TableLayout tableLayout;
	private SparseIntArray padToPlayer = new SparseIntArray();
	private int padRegisteredCount = 0;
	
	private final static int ADD_PLAYER = 0, REMOVE_PLAYER = 1; 
	private Handler GUIModifierHandler;
	
	public RemoteManager(TableLayout tl) {
		tableLayout = tl;
		tl.setMinimumHeight(100);
		GUIModifierHandler = new Handler(tl.getContext().getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case ADD_PLAYER:
					addPlayerRow(msg.arg1, (String)msg.obj);
					break;
				case REMOVE_PLAYER:
					removePlayerRow(msg.arg1);
					break;
				}
			}
		};
	}
	
	/** Construit la table de pad avec le numéro du joueur associé, retourne le
	 * nombre de pad enregistré **/
	public int buildPadTable() {
		padToPlayer.clear();
		padRegisteredCount = 0;
		for(int padId : padId_list) {
			padToPlayer.put(padId, padRegisteredCount);
			padRegisteredCount++;
		}
		return padRegisteredCount;
	}
	/** Retourne le numéro du joueur associé à ce pad, retourne -1 si le pad
	 * n'est pas enregistré. La méthode buildPadTable doit être appelé avant **/
	public int getPlayer(int padId) {
		return padToPlayer.get(padId, -1);
	}
	/** Retourne le nombre de pad enregistré. La méthode buildPadTable doit être appelé avant **/
	public int getPlayerCount() {
		return padRegisteredCount;
	}
	
	public void registerPad(int padId, String name) {
		if(!isPadRegistered(padId)) {
			Message msg = new Message();
			msg.arg1 = padId;
			msg.obj = name;
			msg.what = ADD_PLAYER;
			GUIModifierHandler.sendMessage(msg);
			
			padId_list.add(padId);
		}
	}
	
	public void unregisterPad(int padId) {
		if(isPadRegistered(padId)) {
			Message msg = new Message();
			msg.arg1 = padId;
			msg.what = REMOVE_PLAYER;
			GUIModifierHandler.sendMessage(msg);
			
			padId_list.remove(padId);
		}
	}
	
	private boolean isPadRegistered(int padId) {
		return padId_list.contains(padId);
	}
	
	private void addPlayerRow(int id, String name) {
		TextView playerName = new TextView(tableLayout.getContext());
		playerName.setText(name == null ? "Pad "+id : name);
		
		TableRow row = new TableRow(tableLayout.getContext());
		row.setId(id);
		row.addView(playerName);
		
		tableLayout.addView(row);
	}
	
	private void removePlayerRow(int id) {
		tableLayout.removeView(tableLayout.findViewById(id));
	}
	
	public void clear() {
		padId_list.clear();
		tableLayout.removeAllViews();
	}
}
