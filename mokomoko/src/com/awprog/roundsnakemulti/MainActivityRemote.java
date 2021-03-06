package com.awprog.roundsnakemulti;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.awprog.roundsnakemulti.GameEngine.DeathCertificate;
import com.fbessou.sofa.GameIOClient.ConnectionStateChangedListener;
import com.fbessou.sofa.GameIOHelper;
import com.fbessou.sofa.GameIOHelper.GamePadInGameInformation;
import com.fbessou.sofa.GameIOHelper.GamePadInputEvent;
import com.fbessou.sofa.GameIOHelper.GamePadStateChangedEvent;
import com.fbessou.sofa.GameIOHelper.GamePadStateChangedEvent.Type;
import com.fbessou.sofa.indicator.Indicator;
import com.fbessou.sofa.GameInformation;
import com.fbessou.sofa.InputEvent;
import com.fbessou.sofa.OutputEvent;

public class MainActivityRemote extends Activity implements ConnectionStateChangedListener {
	MySurfaceView mySurfaceView;
	SeekBar sbSpeed;
	GameEngine game = new GameEngine();
	GameRenderer renderer = new GameRenderer(game);
	GameIOHelper easyIO;

	ArrayList<Integer> gamepadInGame_IndexToId = new ArrayList<Integer>();
	SparseIntArray gamepadInGame_IdToIndex = new SparseIntArray();
	
	/**
	 * TODO player/pad manager
	 */
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.menu_remote_layout);
		
		mySurfaceView = new MySurfaceView(this);
		
		((RelativeLayout) findViewById(R.id.rl_main)).addView(mySurfaceView, 0);
		buildMenu();
		GameInformation info = new GameInformation(this);
		info.setName("Mokomoko");
		easyIO = new GameIOHelper(this, info);
		easyIO.start(this);
	}
	/*
	private AlertDialog buildPadRegistrationDialog() {
		TableLayout tableView = new TableLayout(this);
		remoteRegistration = new RemoteManager(tableView);
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		
		ScrollView sv = new ScrollView(this);
		sv.addView(tableView);
		
		adb.setTitle("Press a button to join");
		adb.setView(sv);
		adb.setPositiveButton("Done", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				remoteRegistration.buildPadTable();
				game.setPlayerCount(remoteRegistration.getPlayerCount());
				game.reset();
				registrationPhase = false;
			}
		});
		adb.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				registrationPhase = false;
				remoteRegistration.clear();
			}
		});
		
		return adb.create();
	}*/
	private void buildMenu() {
		final LinearLayout ll = (LinearLayout) findViewById(R.id.ll_main);
		
		final RulesEditorView rulesEdit = new RulesEditorView(this);
		rulesEdit.build(Rules.current);
		ll.addView(rulesEdit);
		((ViewGroup)rulesEdit.getChildAt(0)).getChildAt(1).clearFocus();
		
		ll.setOnTouchListener(new OnTouchListener(){ @SuppressLint("ClickableViewAccessibility")
			@Override public boolean onTouch(View v,MotionEvent event){ return true; }});
		((Button)ll.findViewById(R.id.b_play)).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				hideMenu();
			}
		});
		((Button)ll.findViewById(R.id.b_reset)).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				((Button)findViewById(R.id.b_play)).setText("Play");
				//((Button)findViewById(R.id.b_reset)).setEnabled(false);
				updateGamePadInGame(); // TODO remove that bad hack
				//game.reset();		   // TODO remove that bad hack
			}
		});
		/// Nb players
		((Button)ll.findViewById(R.id.b_add_player)).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				game.setPlayerCount(Math.min(game.getPlayerCount()+1, 42));
				game.reset();
				((Button)findViewById(R.id.b_play)).setText("Play");
				//((Button)findViewById(R.id.b_reset)).setEnabled(false);
			}});
		((Button)ll.findViewById(R.id.b_delete_player)).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				game.setPlayerCount(Math.max(game.getPlayerCount()-1, 2));
				game.reset();
				((Button)findViewById(R.id.b_play)).setText("Play");
				//((Button)findViewById(R.id.b_reset)).setEnabled(false);
			}});
		/*// Pad registration
		final AlertDialog adRegistration = buildPadRegistrationDialog();
		((Button)ll.findViewById(R.id.b_register_player)).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				remoteRegistration.clear();
				registrationPhase = true;
				adRegistration.show();
			}});*/
		/// Game mode
		((RadioGroup) findViewById(R.id.rg_mode)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch(checkedId) {
				case R.id.r_mode_dm:
					Rules.setRulesType(Rules.RULES_DM);
					rulesEdit.build(Rules.current);
					game.reset();
					break;
				case R.id.r_mode_og:
					Rules.setRulesType(Rules.RULES_OG);
					rulesEdit.build(Rules.current);
					game.reset();
					break;
				case R.id.r_mode_lr:
					Rules.setRulesType(Rules.RULES_LR);
					rulesEdit.build(Rules.current);
					game.reset();
					break;
				case R.id.r_mode_cs:
					Rules.setRulesType(Rules.RULES_CS);
					rulesEdit.build(Rules.current);
					game.reset();
					break;
				}
				((Button)findViewById(R.id.b_play)).setText("Play");
				//((Button)findViewById(R.id.b_reset)).setEnabled(false);
			}
		});
		sbSpeed = ((SeekBar) findViewById(R.id.sb_speed));
		sbSpeed.setMax(Game.maxSpeedLevel);
		sbSpeed.setProgress(Game.defaultSpeedLevel);
		sbSpeed.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override public void onStopTrackingTouch(SeekBar seekBar) {
				game.setSpeedLevel(seekBar.getProgress());
			}
			@Override public void onStartTrackingTouch(SeekBar seekBar) { }
			@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { 
				if(!fromUser) game.setSpeedLevel(seekBar.getProgress());
			}
		});
		SeekBar sbScale = ((SeekBar) findViewById(R.id.sb_map_scale));
		sbScale.setMax(Game.maxScaleLevel);
		sbScale.setProgress(Game.defaultScaleLevel);
		sbScale.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override public void onStopTrackingTouch(SeekBar seekBar) {
				game.setMapSize(seekBar.getProgress(), game.getMapRatio());
				game.reset();
				((Button)findViewById(R.id.b_play)).setText("Play");
				//((Button)findViewById(R.id.b_reset)).setEnabled(false);
			}
			@Override public void onStartTrackingTouch(SeekBar seekBar) { }
			@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
		});
	}
	private void showMenu() {
		((View) findViewById(R.id.sv_main)).setVisibility(View.VISIBLE);

		// jeu en cours
		if(game.hasBegun()) {
			((Button)findViewById(R.id.b_play)).setText("Resume");
			//((Button)findViewById(R.id.b_reset)).setEnabled(true);
		}
		// jeu non commenc�
		else {
			((Button)findViewById(R.id.b_play)).setText("Play");
			//((Button)findViewById(R.id.b_reset)).setEnabled(false);
		}
	}
	@SuppressLint("NewApi")
	private void hideMenu() {
		((View) findViewById(R.id.sv_main)).setVisibility(View.GONE);

		if (android.os.Build.VERSION.SDK_INT >= 19)
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}
	private boolean isMenuOpened() {
		return ((View) findViewById(R.id.sv_main)).getVisibility() == View.VISIBLE;
	}
	
	public void updateGamePadInGame() {
		HashMap<String, Integer> names = new HashMap<String, Integer>();
		game.setPlayerCount(easyIO.getGamePadCount());
		game.reset();
		gamepadInGame_IndexToId.clear();
		gamepadInGame_IdToIndex.clear();

		for(int id : easyIO.getGamePadIds()) {
			GamePadInGameInformation info = easyIO.getGamePadInformation(id);
			gamepadInGame_IndexToId.add(id);
			gamepadInGame_IdToIndex.put(id, gamepadInGame_IndexToId.size()-1);

			String name = info.staticInformations.getNickname();
			if(!names.containsKey(name)) {
				names.put(name, 1);
				game.getPlayer(gamepadInGame_IndexToId.size()-1).setName(name);
			} else {
				names.put(name, names.get(name)+1);
				game.getPlayer(gamepadInGame_IndexToId.size()-1).setName(name+" ("+names.get(name)+")");
			}
		}
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus && android.os.Build.VERSION.SDK_INT >= 19)
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}
	
	@Override
	public void onBackPressed() {
		if(isMenuOpened()) {
			super.onBackPressed();
		} else {
			showMenu();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mySurfaceView.onResumeMySurfaceView();
	}
	@Override
	protected void onPause() {
		super.onPause();
		mySurfaceView.onPauseMySurfaceView();
	}

	class MySurfaceView extends SurfaceView implements Runnable{

		Thread thread = null;
		SurfaceHolder surfaceHolder;
		volatile boolean running = false;
		
		private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);

		final static int SCORE_BACKGROUND_COLOR = 0xff212121, SCORE_TEXT_COLOR = 0xffffffff;

		public MySurfaceView(Context context) {
			super(context);
			surfaceHolder = getHolder();
			
			paint.setStrokeCap(Paint.Cap.ROUND);
			paint.setStrokeJoin(Paint.Join.ROUND);
			
			FunnyFilter.clearFilter();
		}

		private float downX, downY;
		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouchEvent(MotionEvent event)
		{
			/*boolean actiondown = event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN
									|| event.getAction() == MotionEvent.ACTION_DOWN;
			int id = event.getActionIndex();
			float x = event.getX(id), y = event.getY(id);
			float w = getWidth(), h = getHeight();
			Map map=game.map;
			if(actiondown) {
				if(Math.abs(y-h/2)<map.height/2*map.scale) {
					/// clic sur la bande gauche
					if(w/2-x > map.width/2*map.scale) {
						if(y-h/2 < 0) {
							if(!game.roundEnded) {
								if(game.pause)
									game.unpause = true;
								else
									game.pause = true;
							}
						} else if(y-h/2 > 0) {
							onBackPressed();// fuck code
						}
					}
					/// clic sur la bande droite
					else if(x-w/2 > map.width/2*map.scale) {
					}
					/// clic sur la zone centrale
					else {
						if(game.pause)
							game.unpause = true;
						else
							game.pause = true;
					}
				}
			}*/
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
				downX = event.getX();
				downY = event.getY();
			} else if(event.getAction()== MotionEvent.ACTION_UP) {
				float dx = event.getX() - downX, dy = event.getY() - downY;
				if(Math.abs(dx+dy) <= 50)
					showMenu();
				else if(dx < 50)
					FunnyFilter.previousFilter();
				else if(dx > 50)
					FunnyFilter.nextFilter();
			}
			
			return true;
		}

		public void onResumeMySurfaceView(){
			running = true;
			thread = new Thread(this);
			thread.start();
		}
		public void onPauseMySurfaceView(){
			boolean retry = true;
			running = false;
			while(retry){
				try {
					thread.join();
					retry = false;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void run() {
			Bitmap bitmap = null;
			
			while(running) {
				long t = SystemClock.elapsedRealtime();
				renderer.frameCount++;
				
				synchronized (game) {
					easyIO.flushCustomMessage();
					
					GamePadStateChangedEvent scEvent;
					while((scEvent = easyIO.pollStateChangedEvent()) != null) {
						
						if(!game.hasBegun() || game.isGameFinished) {
							updateGamePadInGame();
						} else {
							if(scEvent.eventType == Type.UNEXPECTEDLY_DISCONNECTED) {
								// TODO handle this case
							}
							// Mise à jour du Nickname
							else if(scEvent.eventType == Type.INFORMATION) {
								int playerNumber = gamepadInGame_IdToIndex.get(scEvent.gamePadId, -1);
								if(0 <= playerNumber && playerNumber < game.getPlayerCount()) {
									game.getPlayer(playerNumber).setName(scEvent.newInformation.getNickname());
								}
							}
						}
					}
					
					GamePadInputEvent iEvent;
					while((iEvent = easyIO.pollInputEvent()) != null) {
						int playerNumber = gamepadInGame_IdToIndex.get(iEvent.gamePadId, -1);
						if(0 <= playerNumber && playerNumber < game.getPlayerCount()) {
							InputEvent e = iEvent.event;
							switch(e.eventType) {
							case FLOATMOVE:
								if(e.values.length == 2)
									game.handleJoystickEvent(playerNumber, e.getX(), -e.getY());
								break;
							case KEYDOWN:
							case KEYUP:
								game.handleButtonEvent(playerNumber, e.eventType == InputEvent.Type.KEYDOWN);
								break;
							default:
								break;
							}
						}
					}
					
					// Game engine
					if(!isMenuOpened() && renderer.frameCount % game.nbFramePerStep == 0) {
						game.step();
						for(DeathCertificate dt : game.getDeathCertificates()) {
							if(dt.date == GameEngine.getElapsedStep()) {
								int padid = gamepadInGame_IndexToId.get(dt.dead);
								if(padid == -1)
									continue;
								Log.i("###", "Send death feedback to "+padid);
								easyIO.sendOutputEvent(OutputEvent.createFeedback(OutputEvent.VIBRATE_LONG, Indicator.FEEDBACK_CATEGORY_VALUE+1), padid);
							}
						}
					}
					
					// graphic engine
					if(surfaceHolder.getSurface().isValid()){
						Canvas cvs = surfaceHolder.lockCanvas();
						if(bitmap == null || bitmap.getWidth() != cvs.getWidth() || bitmap.getHeight() != cvs.getHeight())
							bitmap = Bitmap.createBitmap(cvs.getWidth(), cvs.getHeight(), Config.ARGB_8888);
						Canvas canvas = new Canvas(bitmap);
						
						final int w = canvas.getWidth();
						final int h = canvas.getHeight();
						final int scoreWidth = h / 3;
						
						/// Compute the scale of the game's surface
						float scale = (float) h / game.getMap().height;
						float ratio = (float) (w-scoreWidth) / h;
						//Log.i("###", "m.w=" + game.getMap().width+ " m.h="+game.getMap().height+" r1="+((float) (w-scoreWidth) / h)+" r2="+(game.getMap().width/game.getMap().height));
						// Si le ratio n'a jamais été modifié, on l'adapte à la taille de l'écran
						if(game.getMapRatio() != ratio) {
							game.setMapSize(game.getMapScaleLevel(), ratio);
							game.reset();
						}
						
						// Affichage du jeu
						canvas.save();
						canvas.clipRect(0, 0, (w-scoreWidth), h);
						canvas.scale(scale, scale);
						
						renderer.render(canvas);
						
						canvas.restore();
						
						// Affichage des vainqueurs si manche ou partie terminée
						if(game.isRoundFinished || game.isGameFinished) {
							paint.setTextSize((w-scoreWidth)/20);
							paint.setColor(0xff111111);
							paint.setTextAlign(Align.CENTER);
							String text;
							if(game.isGameFinished) {
								ArrayList<Integer> winners = game.getWinners();
								if(winners.size() == 0)
									text = "Nobody wins";
								else if(winners.size() == 1)
									text = game.getPlayer(winners.get(0)).getName() + " wins";
								else {
									text = "";
									for(Integer i : winners) {
										text += game.getPlayer(i).getName()+ " & ";
									}
									text = text.substring(0, text.length()-2) + "win";
								}
							} else {
								ArrayList<Integer> alive = game.getAlivePlayers();
								if(alive.size() == 0)
									text = "Nobody survives";
								else if(alive.size() == 1)
									text = "Survivor : " + game.getPlayer(alive.get(0)).getName();
								else {
									text = "Survivor : ";
									for(Integer i : alive) {
										text += game.getPlayer(i).getName()+ " & ";
									}
									text = text.substring(0, text.length()-2);
								}
							}
							canvas.drawText(text, (w-scoreWidth)/2, h/2, paint);
						}
						
						/// Affichage des scores
						canvas.save();
						canvas.clipRect((w-scoreWidth), 0, w, h);
						canvas.translate((w-scoreWidth), 0);
						
						canvas.drawColor(SCORE_BACKGROUND_COLOR);
						
						float textSize = (float) scoreWidth / 7.5f;
						paint.setTextSize(textSize);
						
						/// Mode de jeu
						paint.setColor(SCORE_TEXT_COLOR);
						paint.setTextAlign(Align.CENTER);
						canvas.drawText(Rules.current.name, (float)scoreWidth/2, textSize*1.2f, paint);
						canvas.drawText("Goal : "+Rules.current.scoreAim, (float)scoreWidth/2, (textSize*1.2f)*2, paint);
						
						/// Joueurs et scores
						ArrayList<Integer> order = game.getOrder();
						for(int i = 0; i < game.getPlayerCount(); i++) {
							Player p = game.getPlayer(order.get(i));
							paint.setColor(p.getColor());
							paint.setTextAlign(Align.LEFT);
							canvas.drawText(p.getName(), textSize*0.5f, (textSize*1.2f)*(i+1+3), paint);
							paint.setTextAlign(Align.RIGHT);
							canvas.drawText(""+p.getScore(), scoreWidth-textSize*0.5f, (textSize*1.2f)*(i+1+3), paint);
							
							if(p.isDead()) {
								paint.setColor(p.getColor());
								paint.setStrokeWidth(textSize / 15);
								
								canvas.drawLine(textSize*0.5f, (textSize*1.2f)*(i+1+3-0.25f), textSize*0.5f+paint.measureText(p.getName()), (textSize*1.2f)*(i+1+3-0.25f), paint);
							}
						}
						
						canvas.restore();

						//Log.i("###", "draw="+(SystemClock.elapsedRealtime() - t));
						FunnyFilter.applyCurrentFilter(bitmap);
						cvs.drawBitmap(bitmap, 0, 0, null);
						surfaceHolder.unlockCanvasAndPost(cvs);
					}
					
				} // Fin synchronized(game)
			
				try {
					Thread.sleep(Math.max(1, GameRenderer.DELAY_DRAW - (SystemClock.elapsedRealtime() - t)));
				} catch (InterruptedException e) {}
			}// Fin while(running)
		}
	}

	@Override
	public void onConnected() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
}
