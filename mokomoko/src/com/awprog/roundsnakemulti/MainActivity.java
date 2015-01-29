package com.awprog.roundsnakemulti;

import java.net.Socket;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
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
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.awprog.roundsnakemulti.Game.Map;
import com.awprog.roundsnakemulti.Game.Player;
import com.fbessou.sofa.GameMessageReceiver;
import com.fbessou.sofa.InputEvent;
import com.fbessou.sofa.InputEvent.EventType;
import com.fbessou.sofa.ProxyConnector;
import com.fbessou.sofa.ProxyConnector.OnConnectedListener;
import com.fbessou.sofa.StringReceiver;

public class MainActivity extends Activity {
		MySurfaceView mySurfaceView;
		SeekBar sbSpeed;
		Game game = new Game();
		GameMessageReceiver gameMsgReceiver = new GameMessageReceiver();
		
		/**
		 * TODO :
		 * �chelle adapt�e au nombre de joueurs
		 * couleurs plus harmonieuses
		 * item : direction invers� !!!
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
					game.isGameInit = false;
				}
			});
			/// Nb players
			((Button)ll.findViewById(R.id.b_add_player)).setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					game.setNbPlayer(Math.min(game.nbPlayers+1, 42));
					((Button)findViewById(R.id.b_play)).setText("Play");
					((Button)findViewById(R.id.b_reset)).setEnabled(false);
				}});
			((Button)ll.findViewById(R.id.b_delete_player)).setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					game.setNbPlayer(Math.max(game.nbPlayers-1, 1));
					((Button)findViewById(R.id.b_play)).setText("Play");
					((Button)findViewById(R.id.b_reset)).setEnabled(false);
				}});
			/// Map Type
			((RadioGroup) findViewById(R.id.rg_map)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override public void onCheckedChanged(RadioGroup group, int checkedId) {
					switch(checkedId) {
					case R.id.r_map_circle:
						game.setMapType(Game.Map.CIRCLE);
						break;
					case R.id.r_map_square:
						game.setMapType(Game.Map.SQUARE);
						break;
					}
					((Button)findViewById(R.id.b_play)).setText("Play");
					((Button)findViewById(R.id.b_reset)).setEnabled(false);
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
					game.setScaleLevel(seekBar.getProgress());
					((Button)findViewById(R.id.b_play)).setText("Play");
					((Button)findViewById(R.id.b_reset)).setEnabled(false);
				}
				@Override public void onStartTrackingTouch(SeekBar seekBar) { }
				@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
			});
		}
		public void showMenu() {
			((LinearLayout) findViewById(R.id.ll_main)).setVisibility(View.VISIBLE);
			// jeu en cours
			if(game.frame != 0) {
				((Button)findViewById(R.id.b_play)).setText("Resume");
				((Button)findViewById(R.id.b_reset)).setEnabled(true);
			}
			// jeu non commenc�
			else {
				((Button)findViewById(R.id.b_play)).setText("Play");
				((Button)findViewById(R.id.b_reset)).setEnabled(false);
			}
		}
		public void hideMenu() {
			((LinearLayout) findViewById(R.id.ll_main)).setVisibility(View.GONE);
		}
		@Override
		public void onBackPressed() {
			if(((LinearLayout) findViewById(R.id.ll_main)).getVisibility() == View.VISIBLE) {
				super.onBackPressed();
			} else {
				game.pause = true;
				showMenu();
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
					new Thread(sr).start();
				}
			});
			pc.connect();
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

			final static long DELAY_DRAW = 20;
			final static int BACKGROUND_COLOR = 0xffaaaaaa, FRAME_COLOR = 0xff111111;

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
				boolean actiondown = event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN
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
				}
				
				if(!game.pause)
				for(Player player : game.players)
				{
					for(int i = event.getPointerCount(); --i >= 0;)
					{
						float dx = event.getX(i) - player.controlPos[0];
						float dy = event.getY(i) - player.controlPos[1];
						float d = dx*dx+dy*dy;
						if(d > game.controlDead*game.controlDead && d < game.controlSize*game.controlSize)
							player.setDir((float) Math.atan2(dy, dx));
					}
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
				while(running){
					long t = SystemClock.elapsedRealtime();
					
					if(!game.isGameInit)
						game.initGame();
					
					InputEvent event;
					while((event = gameMsgReceiver.pollInputEvent()) != null) {
						if(game != null && game.players != null && event.padId < game.nbPlayers) {
							switch(event.eventType) {
							case MOTION_2D:
								float dx = event.x;
								float dy = -event.y;
								game.players[event.padId].setDir((float) Math.atan2(dy, dx));
								break;
							default:
								break;
							}
						}
					}
					
					// Game engine
					if(!game.pause) {
						if(++game.frame % game.nbFramePhysic == 0) {
							
							// maj item
							for(Map.Item i : game.map.items)
							if(i.type == Map.Item.DEATH)
								i.start --;
							
							// deplacement joueur
							for(Player player : game.players)
								if(player.alive)
									player.step();

							int nbDead = 0;
							// collision entre serpents => blessure
							for(Player player : game.players)
								if(player.alive && player.collision()) {
									player.hurt = true;
									nbDead++;
								}
							// �limination des joueurs bless�s
							for(Player player : game.players)
								if(player.hurt && player.alive) {
									player.alive = false;
									game.nbPlayerAlive--;
								} else if(player.alive) {
									// +1pt/joueurs morts
									player.pts+=nbDead;
								}

							if(game.nbPlayers > 1 ? game.nbPlayerAlive <= 1 : game.nbPlayerAlive == 0) {
								game.endRound();
							}

							game.map.addApplesAndItems();
							
							//Log.i("###", "t="+(SystemClock.elapsedRealtime()-t));
						}
						
					} else if(game.unpause) {
						if(game.roundEnded)
							game.initRound();
						else {
							game.pause = false;
							game.unpause = false;
						}
					}
					
					// graphic engine
					if(surfaceHolder.getSurface().isValid()){
						Canvas canvas = surfaceHolder.lockCanvas();

						final int w = canvas.getWidth();
						final int h = canvas.getHeight();
						//game.defaultSize * 2 * game.biggestSizeFactor * 1.01f;// �paisseur cadre
						Map map = game.map;
						/// Compute the scale of the game's surface
						map.scale = Math.min((float)w / map.width, (float)h / map.height) * 0.75f;
						
						/// clear screen
						canvas.drawColor(BACKGROUND_COLOR);

						
						float sx = (w-map.scale*map.width)/2;
						float sy = (h-map.scale*map.height)/2;
						canvas.save();
						canvas.translate(sx, sy);
						canvas.scale(map.scale, map.scale);
						
						// dessin des pommes
						paint.setStyle(Paint.Style.FILL);
						paint.setColor(0xffbb3333);
						for(Map.Item i : map.apples)
							canvas.drawCircle(i.pos[0], i.pos[1], i.size, paint);
						
						// dessin des objets
						paint.setColor(0xff3333bb);
						for(Map.Item i : map.items)
							if(i.type != Map.Item.DEATH)
								canvas.drawCircle(i.pos[0], i.pos[1], i.size, paint);
						
						// dessin des obstacles
						for(Map.Item i : map.items) {
							if(i.type == Map.Item.DEATH) {
								if(i.start >= 0) {
									paint.setColor(0x44553030);
									canvas.drawCircle(i.pos[0], i.pos[1], i.size*(game.nbFramePhysic-i.start*.5f)/game.nbFramePhysic, paint);
								} else {
									paint.setColor(0xff553030);
									canvas.drawCircle(i.pos[0], i.pos[1], i.size, paint);
								}
							}
						}
						
						// dessin des serpents
						for(Player player : game.players)
						{
							for(float[] pos : player.snk) {
								ArrayList<float[]> eq = map.getAllPositions(pos[0], pos[1], pos[3]);
								for(float[] p : eq) {
									paint.setColor(player.color); 
									if(!player.alive) paint.setAlpha(50);
									canvas.drawCircle(p[0], p[1], pos[3], paint);
									
									if(pos == player.snk.getFirst()) {
										paint.setColor(0x64000000);
										if(!player.alive) paint.setAlpha(20);
										canvas.drawCircle(p[0], p[1], pos[3], paint);
										
										if(player.monsterHead) {
											int frameEnd = (int) (1600/DELAY_DRAW);
											float end = Math.min(frameEnd, player.duration * game.nbFramePhysic);
											paint.setColor(0xff000000);
											if(!player.alive) paint.setAlpha(50);
											paint.setAlpha((int) (paint.getAlpha()*end/frameEnd));
											canvas.drawCircle(p[0], p[1], pos[3]*1.05f, paint);
											
											paint.setColor(0xffeeeeee);
											if(!player.alive) paint.setAlpha(50);
											paint.setAlpha((int) (paint.getAlpha()*end/frameEnd));
											canvas.drawCircle(p[0], p[1], pos[3]*.7f, paint);
										}
									}
								}
							}
						}

						/// cadre
						final float ft = (float) w * 0.01f / map.scale; // frameThickness : �paisseur de la bordure de la carte
						final float e = 2 * (game.defaultSize*game.biggestSizeFactor); // �paisseur de la bordure pour effacer les d�passements
						paint.setStyle(Paint.Style.STROKE);
						if(map.mapType == Map.SQUARE) {
							paint.setColor(BACKGROUND_COLOR);
							paint.setStrokeWidth(e);
							// effa�age du contour
							canvas.drawRect(-e/2, -e/2, map.width+e/2,  map.height+e/2, paint);
							
							paint.setColor(FRAME_COLOR);
							paint.setStrokeWidth(ft);
							canvas.drawRect(-ft/2, -ft/2,  map.width+ft/2,  map.height+ft/2, paint);
						}
						else {
							final float radius = Math.min(map.width, map.height) / 2 + ft/2;
							final float radiusEraser = Math.min(map.width, map.height) / 2 + e/2;
							paint.setColor(BACKGROUND_COLOR);
							paint.setStrokeWidth(e);
							// effa�age du contour
							canvas.drawCircle(map.width/2, map.height/2, radiusEraser, paint);
							
							paint.setColor(FRAME_COLOR);
							paint.setStrokeWidth(ft);
							canvas.drawCircle(map.width/2, map.height/2, radius, paint);
						}

						canvas.restore(); canvas.save();
						/// draw joysticks & score
						paint.setStyle(Paint.Style.FILL);
						
						// dessin du joystick
						map.setJoystickSize(0.95f * 0.5f * Math.min((float)w / (int)((game.nbPlayers+1)/2), (h*.5f - map.scale*(map.height+ft*2)*.5f)));
						paint.setStrokeWidth(game.controlDead);
						
						for(Player player : game.players) {
							// calcul de la position des joysticks
							player.controlPos[0] = (float)w*(player.number/2+.5f)/((game.nbPlayers+(player.number%2==0?1:0))/2);
							player.controlPos[1] = h/2 + (h + map.scale*(map.height+ft*2))/4 *(player.number%2==0?1:-1);
							paint.setColor(player.color);
							paint.setAlpha(100);
							canvas.drawCircle(player.controlPos[0], player.controlPos[1], game.controlSize, paint);
							paint.setAlpha(255);
							canvas.drawCircle(player.controlPos[0], player.controlPos[1], game.controlDead, paint);
							canvas.drawLine(player.controlPos[0], player.controlPos[1],
									player.controlPos[0]+(game.controlSize-game.controlDead/4)*(float)Math.cos(player.dir),
									player.controlPos[1]+(game.controlSize-game.controlDead/4)*(float)Math.sin(player.dir), paint);
						}
						// score
						paint.setColor(0xff333333);
						paint.setTextSize(game.controlSize*0.3f);
						for(Player player : game.players) {
							canvas.drawText(""+player.pts, player.controlPos[0], player.controlPos[1]-game.controlSize*0.42f, paint);
							canvas.save();
							canvas.rotate(180, player.controlPos[0], player.controlPos[1]);
							canvas.drawText(""+player.pts, player.controlPos[0], player.controlPos[1]-game.controlSize*0.42f, paint);
							canvas.restore();
						}
						
						//// Buttons (mise � l'�chelle -> case de dimensions 1.0*1.0)

						float bandWidth = ((float)w - (map.width+ft*2)*map.scale) * .5f;
						float bandHeight = map.height*map.scale;
						float caseDim = Math.min(bandHeight/3, bandWidth);
						float strokeWidth = (float) w * store.sw / caseDim;
						store.buttonPaint.setStrokeWidth(strokeWidth);
						/// left-side buttons

						// menu 
						canvas.restore(); canvas.save();
						canvas.translate(bandWidth/2, h/2+bandHeight/6);
						canvas.scale(caseDim, caseDim);
						canvas.drawCircle(0, 0, .4f, store.buttonPaint);
						canvas.drawLines(store.buttonMenuPts, store.buttonPaint);
						// Play / Pause
						canvas.restore(); canvas.save();
						canvas.translate(bandWidth/2, h/2-bandHeight/6);
						canvas.scale(caseDim, caseDim);
						canvas.drawCircle(0, 0, .4f, store.buttonPaint);
						//canvas.drawRect(store.buttonSquareRectF, store.buttonPaint);
						if(game.pause)
							canvas.drawLines(store.buttonPlayPts, store.buttonPaint);
						else
							canvas.drawLines(store.buttonPausePts, store.buttonPaint);

						/*if(game.pause) {
							/// right-side buttons
							
							// restart game
							canvas.restore(); canvas.save();
							canvas.translate(w-bandWidth/2, h/2);
							canvas.scale(caseDim, caseDim);
							canvas.drawRect(store.buttonSquareRectF, store.buttonPaint);
							canvas.drawLines(store.buttonResetPts, store.buttonPaint);
							// Player--
							canvas.restore(); canvas.save();
							canvas.translate(w-bandWidth/2, h/2+bandHeight/3);
							canvas.scale(caseDim, caseDim);
							canvas.drawRect(store.buttonSquareRectF, store.buttonPaint);
							canvas.drawLines(store.buttonPlusPts, 0,4, store.buttonPaint);
							// Player++
							canvas.restore(); canvas.save();
							canvas.translate(w-bandWidth/2, h/2-bandHeight/3);
							canvas.scale(caseDim, caseDim);
							canvas.drawRect(store.buttonSquareRectF, store.buttonPaint);
							canvas.drawLines(store.buttonPlusPts, store.buttonPaint);
						}*/
						
						/// Text pause/end
						canvas.restore(); canvas.save();
						canvas.translate(w/2, h/2);
						
						if(game.pause) {
							paint.setStyle(Paint.Style.FILL);
							paint.setColor(0xff333333);
							
							paint.setTextSize(map.height*map.scale*.5f * 0.16f);
							canvas.drawText(game.roundEnded?(game.getWinner()==null?"End":game.getWinner()+" wins"):"Pause", 0,map.height/4*map.scale, paint);
							paint.setTextSize(map.height*map.scale*.5f * 0.09f);
							canvas.drawText("Tap to continue", 0, (map.height*2.6f/8)*map.scale, paint);
							
							canvas.rotate(180);

							paint.setTextSize(map.height*map.scale*.5f * 0.16f);
							canvas.drawText(game.roundEnded?(game.getWinner()==null?"End":game.getWinner()+" wins"):"Pause", 0,map.height/4*map.scale, paint);
							paint.setTextSize(map.height*map.scale*.5f * 0.09f);
							canvas.drawText("Tap to continue", 0, (map.height*2.6f/8)*map.scale, paint);
							
						}
						//Log.i("###", "draw="+(SystemClock.elapsedRealtime() - t));
						surfaceHolder.unlockCanvasAndPost(canvas);
					}
					try {
						
						Thread.sleep(Math.max(0, DELAY_DRAW - (SystemClock.elapsedRealtime() - t)));
					} catch (InterruptedException e) {}
				}
			}
			
			final Store store = new Store();
		}
		static class Store {
			Paint buttonPaint;
			//Path buttonSpeedPath;
			RectF buttonSquareRectF;
			float d = 0.15f, e = 0.10f;
			/*float[] buttonPlusPts = {-d, 0, +d, 0, 0, -d,  0, +d};
			float[] buttonResetPts = { -d, 0, -d, -d, 0, -d,  d, -d,
										d, 0,  d,  d, 0,  d, -d,  d};*/
			float sw = 0.0075f;
			/*float[] buttonPausePts={-.4f/3,-.35f+sw,-.4f/3, .35f-sw,
					 				.4f/3, -.35f+sw, .4f/3, .35f-sw};
			float[] buttonPlayPts= {-.4f/2, -.4f+sw, +.4f/2, 0,
									+.4f/2,  0, 	 -.4f/2, .4f-sw,
					 				-.4f/2, +.4f-sw, -.4f/2,-.4f+sw};*/
			float[] buttonPausePts={-e,-d,-e,+d,
	 								+e,-d,+e,+d};
			float[] buttonPlayPts= {-e,-d,+d, 0,
									+d, 0,-e,+d,
									-e,+d,-e,-d};
			float[] buttonMenuPts= {-d, 0,+d, 0,
									-d,+d,+d,+d,
									-d,-d,+d,-d};
			/*float[] buttonMenuPts= {-.4f+sw, 0,        .4f-sw, 0,
									-.4f+sw, .4f*2/3,  .4f-sw, .4f*2/3,
									-.4f+sw, -.4f*2/3, .4f-sw, -.4f*2/3};*/

			Store() {
				/// Buttons
				buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
				//buttonPaint.setStrokeWidth(sw); // -> calculer plus tard en fonction de la largeur de l'�cran
				buttonPaint.setColor(0xff333333);
				buttonPaint.setStrokeCap(Paint.Cap.ROUND);
				buttonPaint.setStrokeJoin(Paint.Join.ROUND);
				buttonPaint.setStyle(Paint.Style.STROKE);
				/*buttonSpeedPath = new Path();
				buttonSpeedPath.moveTo(-.4f+sw, -.4f+sw);
				buttonSpeedPath.lineTo(0, 0); buttonSpeedPath.lineTo(0, -.4f+sw);
				buttonSpeedPath.lineTo(.4f-sw, 0); buttonSpeedPath.lineTo(0, +.4f-sw);
				buttonSpeedPath.lineTo(0, 0); buttonSpeedPath.lineTo(-.4f+sw, .4f-sw);
				buttonSpeedPath.lineTo(-.4f+sw, -.4f+sw);*/
				buttonSquareRectF = new RectF(-.4f+sw, -.4f+sw, .4f-sw, .4f-sw);
			}
		}

}
