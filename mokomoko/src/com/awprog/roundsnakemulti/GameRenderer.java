package com.awprog.roundsnakemulti;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;

import com.awprog.roundsnakemulti.Item.Effects;
import com.awprog.roundsnakemulti.Snake.Part;

public class GameRenderer {
	final static int BACKGROUND_COLOR = 0xffeeeeee;
	final static long DELAY_DRAW = 20;
	private final static int itemAppearanceDuration = (int) (280 / DELAY_DRAW);
	private final static int snakeAppearanceDuration = (int) (360 / DELAY_DRAW);
	private final static int snakeDeathDuration = (int) (260 / DELAY_DRAW);
	
	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
	private static final Random random = new Random();
	final GameEngine gameEngineRef;
	public int frameCount = 0;
	
	public GameRenderer(GameEngine gameEngine) {
		paint.setStyle(Style.FILL);
		gameEngineRef = gameEngine;
	}
	
	public void render(Canvas canvas) {
		canvas.drawColor(BACKGROUND_COLOR);
		
		drawMap(canvas, gameEngineRef.getMap(), gameEngineRef);
		
		for(int i = 0; i < gameEngineRef.getPlayerCount(); i++)
			drawSnake(canvas, gameEngineRef.getPlayer(i), gameEngineRef.getMap());
	}
	
	/** Dessine le snake avec les effets actifs et l'item équipé **/
	private void drawSnake(Canvas canvas, Player player, Map map) {
		for(Part part : player.getSnake().getParts()) {
			ArrayList<float[]> positions = map.getEquivalentPositions(part.x, part.y, part.radius);
			boolean isHead = part == player.getSnake().getHead();
			for(float[] pos : positions) {
				paint.setColor(player.getColor()); 
				
				canvas.save();
				canvas.translate(pos[0], pos[1]);
				canvas.scale(part.radius, part.radius);
				
				// Transparence décès
				float alpha = (55 + getSnakeDeathValue(player) * 200) / 255;
				
				paint.setAlpha((int) (255*alpha));
				canvas.drawCircle(0, 0, 1, paint);
				
				/// Dessin de la tête
				if(isHead) {
					// Revive
					canvas.save(); 
					float scale = getSnakeAppearanceValue(player);
					paint.setAlpha((int) (255*alpha* (scale * scale) * 0.75f));
					canvas.scale((1 + 4 * (1 - scale*scale)), (1 + 4 * (1 - scale*scale)));
					canvas.drawCircle(0, 0, 1, paint);
					canvas.restore();
					
					/// Assombrissement de la tête
					paint.setColor(0);
					paint.setAlpha((int) (64*alpha));
					canvas.drawCircle(0, 0, 1, paint);
					
					// Tête de mort
					if(!player.isDead() && player.hasInUseItem() && player.getInUseItem().sharpTeeth) {
						// Disparition en 1.4 sec
						int disappearanceDuration = (int) (1400 / DELAY_DRAW);
						int remainingTime = Math.min(disappearanceDuration, frameCount%gameEngineRef.nbFramePerStep + (player.getInUseItem().maxDuration - player.getInUseItem().duration) * gameEngineRef.nbFramePerStep);
						paint.setColor(0x0);
						paint.setAlpha((int) (255*alpha*remainingTime/disappearanceDuration));
						
						canvas.save();
						canvas.scale(1.05f, 1.05f);
						canvas.rotate((float) Math.toDegrees(part.direction));
						canvas.drawPath(getTeethPath(), paint);
						canvas.restore();
						
						paint.setColor(0xffeeeeee);
						paint.setAlpha((int) (255*alpha*remainingTime/disappearanceDuration));
						canvas.drawCircle(0, 0, 0.9f, paint);
					}
					
					/// Dessin de l'item équipé
					if(player.hasEquippedItem()) {
						Effects effects = player.getEquippedItem();
						if(effects.sharpTeeth) {
							canvas.save();
							canvas.scale(0.85f, 0.85f);
							canvas.rotate((float) Math.toDegrees(part.direction));
							paint.setColor(0xff000000);
							paint.setAlpha((int) (255*alpha));
							paint.setPathEffect(new CornerPathEffect(5f));
							canvas.drawPath(getTrianglePath(), paint);
							canvas.scale(0.75f, 0.6f);
							paint.setColor(0xffeeeeee);
							paint.setAlpha((int) (255*alpha));
							canvas.drawPath(getTrianglePath(), paint);
							paint.setPathEffect(null);
							canvas.restore();
						}
						else if(effects.snakeRadiusMultiplicator > 1.0f) {
							canvas.save();
							canvas.rotate((float) Math.toDegrees(part.direction));
							paint.setColor(0xff000000);
							paint.setAlpha((int) (255*alpha));
							canvas.drawCircle(0, 0, 0.75f, paint);
							paint.setColor(0xffeeeeee);
							paint.setAlpha((int) (255*alpha));
							canvas.drawCircle(-0.15f, 0, 0.45f, paint);
							paint.setColor(0xff000000);
							paint.setAlpha((int) (255*alpha));
							canvas.drawCircle(0.0f, 0, 0.15f, paint);
							canvas.restore();
						}
						else if(effects.fatalTrap) {
							canvas.save();
							canvas.rotate((float) Math.toDegrees(part.direction) + 45);
							paint.setColor(0xff000000);
							paint.setAlpha((int) (255*alpha));
							canvas.drawRoundRect(new RectF(-0.6f, -0.6f, 0.6f, 0.6f), 0.25f, 0.25f, paint);
							paint.setColor(0xffeeeeee);
							paint.setAlpha((int) (255*alpha));
							canvas.drawRoundRect(new RectF(-0.5f, -0.5f, -0.1f, -0.1f), 0.15f, 0.15f, paint);
							canvas.drawRoundRect(new RectF(0.1f, 0.1f, 0.5f, 0.5f), 0.15f, 0.15f, paint);
							canvas.restore();
						}
					}
				}
				
				canvas.restore();
			}
		}
	}
	
	/** Dessinne les éléments de la carte **/
	private void drawMap(Canvas canvas, Map map, GameEngine game) {
		/** Dessin des pommes, bonus et obstacle **/
		for(Item i : map.getItems())
		for(float[] pos : map.getEquivalentPositions(i.x, i.y, i.radius))
		{
			switch(i.effects.appearance) {
			case APPLE:
				drawApple(canvas, i, pos);
				break;
			case BONUS:
				drawBonus(canvas, i, pos);
				break;
			case OBSTACLE:
				drawObstacle(canvas, i, (i.effects.player != -1) ? game.getPlayer(i.effects.player).getColor() : 0xff202020, pos);
				break;
			}
		}
	}

	/** Retourne l'échelle à appliquer à la tête d'un snake pour l'effet d'apparition **/
	private float getSnakeAppearanceValue(Player p) {
		int frameLife = p.getLifeTime()*gameEngineRef.nbFramePerStep + (frameCount%gameEngineRef.nbFramePerStep);
		return (float)Math.min(snakeAppearanceDuration, frameLife)/snakeAppearanceDuration;
	}
	/** Retourne 1 si vivant, < 1 si mort récemment, 0 si depuis longtemps **/
	private float getSnakeDeathValue(Player p) {
		if(!p.isDead())
			return 1;
		int frameLife = p.getDeathTime()*gameEngineRef.nbFramePerStep + (frameCount%gameEngineRef.nbFramePerStep);
			return Math.max(0, 1 - (float)Math.min(itemAppearanceDuration, frameLife)/snakeDeathDuration);
		
	}
	/** Retourne l'échelle à appliquer à un item pour l'effet d'apparition **/
	private float getItemAppearanceValue(Item item) {
		int frameLife = (GameEngine.getElapsedStep()-item.createDate)*gameEngineRef.nbFramePerStep + (frameCount%gameEngineRef.nbFramePerStep);
		return (float)Math.min(itemAppearanceDuration, frameLife)/itemAppearanceDuration;
	}
	
	/** Un rond avec trois petits rond au centre **/
	private void drawApple(Canvas canvas, Item item, float[] pos) {
		canvas.save();
		canvas.translate(pos[0], pos[1]);
		float scale = getItemAppearanceValue(item);
		canvas.scale(item.radius*(scale+1)/2, item.radius*(scale+1)/2);
		
		// Cercle
		paint.setColor(0xffbb3333);
		paint.setAlpha((int) (255*scale));
		canvas.drawCircle(0, 0, 1, paint);
		
		// Symbole centrale
		random.setSeed(item.itemId);
		canvas.rotate(random.nextFloat()*360.0f);
		paint.setColor(0x88ffffff);
		paint.setAlpha((int) (128*scale));
		canvas.drawCircle(0.5f, 0, 0.4f, paint);
		canvas.drawCircle(-0.25f, +0.43f, 0.4f, paint);
		canvas.drawCircle(-0.25f, -0.43f, 0.4f, paint);
		canvas.restore();
	}
	/** Un rond avec des pointes qui dépassent **/
	private void drawObstacle(Canvas canvas, Item item, int pointColor, float[] pos) {
		// Pointes
		random.setSeed(item.itemId);
		canvas.save();
		canvas.translate(pos[0], pos[1]);
		// Fais pousser les pointes
		float scale = getItemAppearanceValue(item);
		canvas.scale(item.radius*1.15f*(scale+1)/2, item.radius*1.15f*(scale+1)/2);
		canvas.rotate(random.nextFloat()*360.0f);
		paint.setColor(pointColor);
		canvas.drawPath(getPikesPath(), paint);
		canvas.restore();
		
		// Cercle
		paint.setColor(0xff553030);
		canvas.drawCircle(pos[0], pos[1], item.radius, paint);
		//paint.setColor(pointColor);
		//canvas.drawCircle(item.x, item.y, item.radius*0.1f, paint);
		
	}
	/** Un rond avec une étoile au centre **/
	private void drawBonus(Canvas canvas, Item item, float[] pos) {
		canvas.save();
		canvas.translate(pos[0], pos[1]);
		float scale = getItemAppearanceValue(item);
		canvas.scale(item.radius*(scale+1)/2, item.radius*(scale+1)/2);
		
		// Cercle
		paint.setColor(0xff3333bb);
		paint.setAlpha((int) (255*scale));
		canvas.drawCircle(0, 0, 1, paint);
		
		// Etoile centrale
		random.setSeed(item.itemId);
		canvas.scale(0.9f, 0.9f);
		canvas.rotate(random.nextFloat()*360.0f);
		paint.setColor(0x88ffffff);
		paint.setAlpha((int) (128*scale));
		paint.setPathEffect(new CornerPathEffect(5f));
		canvas.drawPath(getStarPath(), paint);
		paint.setPathEffect(null);
		canvas.restore();
	}
	
	private static final Path pikesPath = new Path();
	/** Retourne le path d'une étoile aléatoire de rayon 1 **/
	private static Path getPikesPath() {
		pikesPath.reset();
		
		pikesPath.moveTo(1, 0);
		int pikesCount = random.nextInt(5) + 10;
		for(int i = 1; i < pikesCount*2; i++) {
			double a = (i%2 == 0) ? (random.nextDouble()*2-1) * Math.PI / pikesCount : 0;
			double r = (i%2 == 0 ? 1 : 0.7);
			double x = Math.cos(Math.PI / pikesCount * i + a) * r;
			double y = Math.sin(Math.PI / pikesCount * i + a) * r;
			pikesPath.lineTo((float)x, (float)y);
		}
		pikesPath.close();
		
		return pikesPath;
		
	}
	
	private static final Path starPath = new Path();
	/** Retourne le path d'une étoile de rayon 1 **/
	private static Path getStarPath() {
		if(starPath.isEmpty()) {
			starPath.moveTo(1, 0);
			for(int i = 1; i < 10; i++) {
				double x = Math.cos(Math.PI*2/10 * i) * (i%2 == 0? 1 : 0.6f);
				double y = Math.sin(Math.PI*2/10 * i) * (i%2 == 0? 1 : 0.6f);
				starPath.lineTo((float)x, (float)y);
			}
			starPath.close();
		}
		return starPath;
		
	}
	
	private static final Path teethPath = new Path();
	/** Retourne le path d'une dentelle en cercle **/
	private static Path getTeethPath() {
		if(teethPath.isEmpty()) {
			teethPath.moveTo(1, 0);
			final int teethCount = 20;
			for(int i = 1; i < teethCount*2; i++) {
				double x = Math.cos(Math.PI/teethCount * i) * (i%2 == 0? 1 : 1.12);
				double y = Math.sin(Math.PI/teethCount * i) * (i%2 == 0? 1 : 1.12);
				teethPath.lineTo((float)x, (float)y);
			}
			teethPath.close();
		}
		return teethPath;
	}
	
	private static final Path trianglePath = new Path();
	/** Retourne le path d'un triangle de cercle circonscrit centré en (0,0) et de rayon 1 **/
	private static Path getTrianglePath() {
		if(trianglePath.isEmpty()) {
			trianglePath.moveTo(1, 0);
			trianglePath.lineTo(-0.5f, 0.87f);
			trianglePath.lineTo(-0.5f, -0.87f);
			trianglePath.close();
			//trianglePath.lineTo(1, 0);
		}
		return trianglePath;
	}

}
