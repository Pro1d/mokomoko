package com.awprog.roundsnakemulti;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.awprog.roundsnakemulti.Item.Effects;
import com.awprog.roundsnakemulti.Snake.Part;

public class GameRenderer {
	final static int BACKGROUND_COLOR = 0xffeeeeee;
	final static long DELAY_DRAW = 20;
	
	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
	private static final Random random = new Random();
	
	public GameRenderer() {
	}
	
	public void render(Canvas canvas, GameEngine game, int frameCount) {
		canvas.drawColor(BACKGROUND_COLOR);
		
		drawMap(canvas, game.getMap(), game);
		
		for(int i = 0; i < game.getPlayerCount(); i++)
			drawSnake(canvas, game.getPlayer(i), game.getMap(), game, frameCount);
	}
	
	/** Dessine le snake avec les effets actifs et l'item équipé **/
	private void drawSnake(Canvas canvas, Player player, Map map, GameEngine game, int frameCount) {
		for(Part part : player.getSnake().getParts()) {
			ArrayList<float[]> positions = map.getEquivalentPositions(part.x, part.y, part.radius);
			
			for(float[] pos : positions) {
				paint.setColor(player.getColor()); 
				if(player.isDead())
					paint.setAlpha(50);
				
				canvas.drawCircle(pos[0], pos[1], part.radius, paint);
				
				/// Dessin de la tête
				if(part == player.getSnake().getHead()) {
					/// Assombrissement de la tête
					paint.setColor(player.isDead() ? 0x14000000 : 0x64000000);
					canvas.drawCircle(pos[0], pos[1], part.radius, paint);
					
					// Tête de mort
					if(!player.isDead() && player.hasInUseItem() && player.getInUseItem().sharpTeeth) {
						// Disparition en 1.4 sec
						int disappearanceDuration = (int) (1400 / DELAY_DRAW);
						int remainingTime = Math.min(disappearanceDuration, frameCount%game.nbFramePerStep + (player.getInUseItem().maxDuration - player.getInUseItem().duration) * game.nbFramePerStep);
						paint.setColor(0x0);
						paint.setAlpha((int) (255*remainingTime/disappearanceDuration));
						
						canvas.save();
						canvas.translate(pos[0], pos[1]);
						canvas.scale(part.radius*1.05f, part.radius*1.05f);
						canvas.rotate((float) Math.toDegrees(part.direction));
						canvas.drawPath(getTeethPath(), paint);
						canvas.restore();
						
						paint.setColor(0xffeeeeee);
						paint.setAlpha((int) (255*remainingTime/disappearanceDuration));
						canvas.drawCircle(pos[0], pos[1], part.radius * 0.9f, paint);
					}
					
					/// Dessin de l'item équipé
					if(player.hasEquippedItem()) {
						Effects effects = player.getEquippedItem();
						if(effects.sharpTeeth) {
							canvas.save();
							canvas.translate(pos[0], pos[1]);
							canvas.scale(part.radius*0.85f, part.radius*0.85f);
							canvas.rotate((float) Math.toDegrees(part.direction));
							paint.setColor(0xff000000);
							paint.setPathEffect(new CornerPathEffect(5f));
							canvas.drawPath(getTrianglePath(), paint);
							canvas.scale(0.75f, 0.6f);
							paint.setColor(0xffeeeeee);
							canvas.drawPath(getTrianglePath(), paint);
							paint.setPathEffect(null);
							canvas.restore();
						}
						else if(effects.snakeRadiusMultiplicator > 1.0f) {
							canvas.save();
							canvas.translate(pos[0], pos[1]);
							canvas.scale(part.radius, part.radius);
							canvas.rotate((float) Math.toDegrees(part.direction));
							paint.setColor(0xff000000);
							canvas.drawCircle(0, 0, 0.75f, paint);
							paint.setColor(0xffeeeeee);
							canvas.drawCircle(-0.15f, 0, 0.45f, paint);
							paint.setColor(0xff000000);
							canvas.drawCircle(0.0f, 0, 0.15f, paint);
							canvas.restore();
						}
						else if(effects.fatalTrap) {
							canvas.save();
							canvas.translate(pos[0], pos[1]);
							canvas.scale(part.radius, part.radius);
							canvas.rotate((float) Math.toDegrees(part.direction) + 45);
							paint.setColor(0xff000000);
							canvas.drawRoundRect(new RectF(-0.6f, -0.6f, 0.6f, 0.6f), 0.25f, 0.25f, paint);
							paint.setColor(0xffeeeeee);
							canvas.drawRoundRect(new RectF(-0.5f, -0.5f, -0.1f, -0.1f), 0.15f, 0.15f, paint);
							canvas.drawRoundRect(new RectF(0.1f, 0.1f, 0.5f, 0.5f), 0.15f, 0.15f, paint);
							canvas.restore();
						}
					}
				}
			}
		}
	}
	
	/** Dessinne les éléments de la carte **/
	private void drawMap(Canvas canvas, Map map, GameEngine game) {
		/** Dessin des pommes, bonus et obstacle **/
		for(Item i : map.getItems())
		// TODO map.getEquivalentPositions(i.x, i.y, i.radius)
		{
			switch(i.effects.appearance) {
			case APPLE:
				drawApple(canvas, i);
				break;
			case BONUS:
				drawBonus(canvas, i);
				break;
			case OBSTACLE:
				drawObstacle(canvas, i, (i.effects.player != -1) ? game.getPlayer(i.effects.player).getColor() : 0xff202020);
				break;
			}
		}
	}
	
	/** Un rond avec trois petits rond au centre **/
	private void drawApple(Canvas canvas, Item item) {
		// Cercle
		paint.setColor(0xffbb3333);
		canvas.drawCircle(item.x, item.y, item.radius, paint);
		
		// Symbole centrale
		random.setSeed(item.itemId);
		canvas.save();
		canvas.translate(item.x, item.y);
		canvas.scale(item.radius, item.radius);
		canvas.rotate(random.nextFloat()*360.0f);
		paint.setColor(0x88ffffff);
		canvas.drawCircle(0.5f, 0, 0.4f, paint);
		canvas.drawCircle(-0.25f, +0.43f, 0.4f, paint);
		canvas.drawCircle(-0.25f, -0.43f, 0.4f, paint);
		canvas.restore();
	}
	/** Un rond avec des pointes qui dépassent **/
	private void drawObstacle(Canvas canvas, Item item, int pointColor) {
		// Pointes
		random.setSeed(item.itemId);
		canvas.save();
		canvas.translate(item.x, item.y);
		canvas.scale(item.radius*1.15f, item.radius*1.15f);
		canvas.rotate(random.nextFloat()*360.0f);
		paint.setColor(pointColor);
		canvas.drawPath(getPikesPath(), paint);
		canvas.restore();
		
		// Cercle
		paint.setColor(0xff553030);
		canvas.drawCircle(item.x, item.y, item.radius, paint);
		//paint.setColor(pointColor);
		//canvas.drawCircle(item.x, item.y, item.radius*0.1f, paint);
		
	}
	/** Un rond avec une étoile au centre **/
	private void drawBonus(Canvas canvas, Item item) {
		// Cercle
		paint.setColor(0xff3333bb);
		canvas.drawCircle(item.x, item.y, item.radius, paint);
		// Etoile centrale
		random.setSeed(item.itemId);
		canvas.save();
		canvas.translate(item.x, item.y);
		canvas.scale(item.radius*0.9f, item.radius*0.9f);
		canvas.rotate(random.nextFloat()*360.0f);
		paint.setColor(0x88ffffff);
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
