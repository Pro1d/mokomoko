package com.awprog.roundsnakemulti;

import java.net.Socket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.fbessou.sofa.GameMessageReceiver;
import com.fbessou.sofa.InputEvent;
import com.fbessou.sofa.InputEvent.EventType;
import com.fbessou.sofa.ProxyConnector;
import com.fbessou.sofa.ProxyConnector.OnConnectedListener;
import com.fbessou.sofa.StringReceiver;

public class MainActivityRemote extends Activity {
		MySurfaceView mySurfaceView;
		SeekBar sbSpeed;
		GameEngine game = new GameEngine();
		GameRenderer renderer = new GameRenderer();
		GameMessageReceiver gameMsgReceiver = new GameMessageReceiver();
		Thread strRcvThread;
		
		/**
		 * TODO player/pad manager
		 */
		
		/** Called when the activity is first created. */
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.menu_layout);
			
			mySurfaceView = new MySurfaceView(this);
			
			((RelativeLayout) findViewById(R.id.rl_main)).addView(mySurfaceView, 0);
			buildMenu();
		}
		private void buildMenu() {
			final LinearLayout ll = (LinearLayout) findViewById(R.id.ll_main);
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
					((Button)findViewById(R.id.b_reset)).setEnabled(false);
					game.reset();
				}
			});
			/// Nb players
			((Button)ll.findViewById(R.id.b_add_player)).setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					game.setPlayerCount(Math.min(game.getPlayerCount()+1, 42));
					((Button)findViewById(R.id.b_play)).setText("Play");
					((Button)findViewById(R.id.b_reset)).setEnabled(false);
				}});
			((Button)ll.findViewById(R.id.b_delete_player)).setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					game.setPlayerCount(Math.max(game.getPlayerCount()-1, 2));
					((Button)findViewById(R.id.b_play)).setText("Play");
					((Button)findViewById(R.id.b_reset)).setEnabled(false);
				}});
			
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
					((Button)findViewById(R.id.b_play)).setText("Play");
					((Button)findViewById(R.id.b_reset)).setEnabled(false);
				}
				@Override public void onStartTrackingTouch(SeekBar seekBar) { }
				@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
			});
		}
		private void showMenu() {
			((LinearLayout) findViewById(R.id.ll_main)).setVisibility(View.VISIBLE);
			// jeu en cours
			if(game.hasBegun()) {
				((Button)findViewById(R.id.b_play)).setText("Resume");
				((Button)findViewById(R.id.b_reset)).setEnabled(true);
			}
			// jeu non commenc�
			else {
				((Button)findViewById(R.id.b_play)).setText("Play");
				((Button)findViewById(R.id.b_reset)).setEnabled(false);
			}
		}
		private void hideMenu() {
			((LinearLayout) findViewById(R.id.ll_main)).setVisibility(View.GONE);
		}
		@Override
		public void onBackPressed() {
			if(isMenuOpened()) {
				super.onBackPressed();
			} else {
				showMenu();
			}
		}
		private boolean isMenuOpened() {
			return ((LinearLayout) findViewById(R.id.ll_main)).getVisibility() == View.VISIBLE;
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
		protected void onStart() {
			super.onStart();
			ProxyConnector pc = new ProxyConnector(this, 6969, new OnConnectedListener() {
				@Override
				public void onConnected(Socket socket) {
					StringReceiver sr = new StringReceiver(socket);
					
					/*gameMsgReceiver.setInputEventListener(new GameMessageReceiver.InputEventListener() {
						@Override
						public void onInputEvent(InputEvent event) {
							if(game != null && game.players != null && event.padId < game.nbPlayers)
							if(!game.pause) {
								float dx = event.x;
								float dy = -event.y;
								game.players[event.padId].setDir((float) Math.atan2(dy, dx));
							}
						}
					});*/
					sr.setListener(gameMsgReceiver);
					strRcvThread = new Thread(sr);
					strRcvThread.start();
				}
			});
			pc.connect();
		}
		@Override
		protected void onStop() {
			/*if(!strRcvThread.isInterrupted())
				strRcvThread.interrupt();
			try {
				strRcvThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
			super.onStop();
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

			final static int SCORE_BACKGROUND_COLOR = 0xffffffff;

			public MySurfaceView(Context context) {
				super(context);
				// TODO Auto-generated constructor stub
				surfaceHolder = getHolder();
				
				//initGame(3, 8);
				
				paint.setStrokeCap(Paint.Cap.ROUND);
				paint.setStrokeJoin(Paint.Join.ROUND);
				paint.setTextAlign(Paint.Align.CENTER);
			}

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
				int frameCount = 0;
				while(running){
					long t = SystemClock.elapsedRealtime();
					frameCount++;
					
					InputEvent event;
					while((event = gameMsgReceiver.pollInputEvent()) != null) {
						if(event.padId < game.getPlayerCount()) {
							switch(event.eventType) {
							case MOTION_2D:
								game.handleJoystickEvent(event.padId, event.x, -event.y);
								break;
							case KEY_DOWN:
							case KEY_UP:
								game.handleButtonEvent(event.padId, event.eventType == EventType.KEY_DOWN);
								break;
							default:
								break;
							}
						}
					}
					
					// Game engine
					if(!isMenuOpened() && frameCount % game.nbFramePerStep == 0) {
						game.step();
					}
					
					// graphic engine
					if(surfaceHolder.getSurface().isValid()){
						Canvas canvas = surfaceHolder.lockCanvas();

						final int w = canvas.getWidth();
						final int h = canvas.getHeight();
						final int scoreWidth = h / 3;
						
						/// Compute the scale of the game's surface
						float scale = (float) h / game.getMap().height;
						// Si le ratio n'a jamais été modifié, on l'adapte à la taille de l'écran
						if(game.getMapRatio() == 1) {
							game.setMapSize(game.getMapScaleLevel(), (float)(w-scoreWidth) / h);
						}
						
						// Affichage du jeu
						canvas.save();
						canvas.clipRect(0, 0, (w-scoreWidth), h);
						canvas.scale(scale, scale);
						
						renderer.render(canvas, game);
						
						canvas.restore();
						
						/// Affichage des scores
						canvas.save();
						canvas.clipRect((w-scoreWidth), 0, scoreWidth, h);
						canvas.translate((w-scoreWidth), 0);
						
						canvas.drawColor(SCORE_BACKGROUND_COLOR);
						
						float textSize = (float)(w-scoreWidth)/20;
						paint.setTextSize(textSize);
						
						/// Mode de jeu
						paint.setColor(0xff111111);
						paint.setTextAlign(Align.CENTER);
						canvas.drawText(Rules.getRules().name, (float)(w-scoreWidth)/2, textSize*1.2f, paint);
						canvas.drawText("Goal : "+Rules.getRules().scoreLimit, (float)(w-scoreWidth)/2, (textSize*1.2f)*2, paint);
						
						/// Joueurs et scores
						for(int i = 0; i < game.getPlayerCount(); i++) {
							Player p = game.getPlayer(i);
							paint.setColor(p.getColor());
							paint.setTextAlign(Align.LEFT);
							canvas.drawText(p.getName(), textSize*1.2f, (textSize*1.2f)*i, paint);
							paint.setTextAlign(Align.RIGHT);
							canvas.drawText(""+p.getScore(), scoreWidth-textSize*1.2f, (textSize*1.2f)*i, paint);
							
							paint.setColor(p.getColor());
							canvas.drawLine(textSize*1.2f, (textSize*1.2f)*(i-0.5f), textSize*1.2f+paint.measureText(p.getName()), (textSize*1.2f)*(i-0.5f), paint);
						}
						
						canvas.restore();
						
						
						//Log.i("###", "draw="+(SystemClock.elapsedRealtime() - t));
						surfaceHolder.unlockCanvasAndPost(canvas);
					}
					try {
						
						Thread.sleep(Math.max(0, GameRenderer.DELAY_DRAW - (SystemClock.elapsedRealtime() - t)));
					} catch (InterruptedException e) {}
				}
			}
		}
}