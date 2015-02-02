package com.awprog.roundsnakemulti;

import java.util.Random;


public class Item {
	private static int nextItemId = 0;
	private static final Random random = new Random();

	final int itemId;
	float x, y;
	float radius = 0.75f;
	Effects effects;
	
	/** Crée un item en le positionnant correctement sur la carte **/
	private Item(Map map) {
		if(map != null)
			setRandomPosition(map);
		itemId = (nextItemId += random.nextInt(1337));
	}
	public static Item createApple(Map map) {
		Item i = new Item(map);
		i.effects = Effects.getAppleEffects();
		return i;
	}
	public static Item createBonus(Map map) {
		Item i = new Item(map);
		switch(random.nextInt(3)) {
		case 0:
			i.effects = Effects.getTrapperEffects();
			break;
		case 1:
			i.effects = Effects.getGrowthEffects(map);
			break;
		case 2:
			i.effects = Effects.getSharpTeethEffects(map);
			break;
		}
		return i;
	}
	public static Item createTrap(float px, float py, float r) {
		Item i = new Item(null);
		i.x = px;
		i.y = py;
		i.radius = r;
		i.effects = Effects.getTrapEffects();
		return i;
	}
	
	private void setRandomPosition(Map map) {
		int iterMax = 10;
		do {
			x = random.nextFloat() * (map.width - radius*2)  + radius;
			y = random.nextFloat() * (map.height - radius*2) + radius;
		} while(map.collisionCircleItems(x,y,radius) && --iterMax > 0);
	}
	
	/// Effets de l'item 
	public static class Effects {
		/** Graphical appearance **/
		enum Appearance { APPLE, BONUS, OBSTACLE };
		
		final Appearance appearance;
		boolean fatalTrap = false; // piège mortelle
		int snakeGrowth = 0; // gain en taille
		float snakeRadiusMultiplicator = 1.0f; // changement de taille
		int duration = 0, maxDuration = 0; // durée effective/totale, en step; maxDuration  = 0 => utilisation instantanée, sinon utilisation sur une période
		boolean sharpTeeth = false; // dents tranchantes
		boolean manualActivation = false; // activation par le joueur, sinon automatique au contact
		int player = -1; // Joueur ayant créé cet item, -1 si créé par le jeu
		
		private Effects(Appearance a) {
			appearance = a;
		}
		
		static Effects getAppleEffects() {
			Effects e = new Effects(Appearance.APPLE);
			e.snakeGrowth = Rules.current.appleGrowth;
			return e;
		}
		static Effects getGrowthEffects(Map map) {
			Effects e = new Effects(Appearance.BONUS);
			e.snakeRadiusMultiplicator = 1.45f;
			e.maxDuration = (int) (Math.sqrt(map.width*map.height) / (GameEngine.defaultSpeed*e.snakeRadiusMultiplicator) * Rules.current.bonusDurationMultiplicator);
			e.manualActivation = true;
			return e;
		}
		static Effects getSharpTeethEffects(Map map) {
			Effects e = new Effects(Appearance.BONUS);
			e.sharpTeeth = true;
			e.maxDuration = (int) (Math.sqrt(map.width*map.height) / GameEngine.defaultSpeed * Rules.current.bonusDurationMultiplicator);
			e.manualActivation = true;
			return e;
		}
		static Effects getTrapperEffects() {
			Effects e = new Effects(Appearance.BONUS);
			e.fatalTrap = true;
			e.manualActivation = true;
			return e;
		}
		static Effects getTrapEffects() {
			Effects e = new Effects(Appearance.OBSTACLE);
			e.fatalTrap = true;
			return e;
		}
	}
}
