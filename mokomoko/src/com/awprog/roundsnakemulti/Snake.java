package com.awprog.roundsnakemulti;

import java.util.LinkedList;


public class Snake {
	private LinkedList<Part> parts = new LinkedList<Part>(); // doit toujours avoir au moins 1 élément
	private int length;
	private float size;
	
	/** Nouveau snake avec juste la tête visible **/
	void reset(float px, float py, float dir) {
		parts.clear();
		parts.add(new Part(px, py, dir));
		length = Rules.current.initialSnakeLength;
		size = Rules.current.snakeDefaultSize;
	}
	
	/** Supprime les derniers éléments afin d'obtenir la longueur voulue; Peut laisser un piège sur la carte **/
	void shrinkToLength(Map map, int player) {
		// Permet une croissance infinie
		if(length <= 0)
			return;
		
		while(parts.size() > length) {
			Part p = parts.getLast();
			if(p.containingTrap)
				map.setTrap(player, p.x, p.y, p.radius);
			parts.removeLast();
		}
	}
	
	/** Ajoute 'len' à la longueur; 'len' peut être négatif; la longueur totale est gardée supérieure à 0 **/
	public void changeLength(int len) {
		length = Math.max(len+length, 1);
	}
	/** Retourne la longueur réelle (pas celle visible) **/
	public int getLength() {
		return length;
	}
	/** Retourne la longueur réelle (pas celle visible) **/
	public int getVisibleLength() {
		return parts.size();
	}
	/** Modifie la longueur réelle **/
	public void setLength(int len) {
		length = len;
	}
	
	/** Fais un pas en avant vers la direction utilisateur souhaitée **/
	public void step(float dir, Map map, int player) {
		Part head = parts.getFirst().clone();
		head.direction = getDirStep(dir);
		
		// New radius
		float lastRadius = head.radius;
		if(parts.getFirst().containingTrap)
			head.radius = size;
		else
			head.radius = (lastRadius*(1-Rules.current.snakeSizeVariationSpeed) + size*Rules.current.snakeSizeVariationSpeed);
		head.radius *= (float) Math.pow(Rules.current.snakeGrowthWithLengthFactor, length);
		
		// Speed
		float speed = (head.radius+lastRadius) / 2 * Rules.current.snakeDefaultSpeed;
		
		// Position
		head.x += speed*Math.cos(head.direction);
		head.y += speed*Math.sin(head.direction);

		map.putInside(head);
		
		parts.addFirst(head);
		shrinkToLength(map, player);
	}
	
	/** Corrige la direction afin d'éviter les demi-tours trop brusques **/
	private float getDirStep(float dir) {
		float last = parts.getFirst().direction;
		float bigdelta = dir - last;
		while(bigdelta > Math.PI) bigdelta-=2*Math.PI;
		while(bigdelta <-Math.PI) bigdelta+=2*Math.PI;

		final float deltamax = (float) Math.toRadians(Rules.current.maxSnakeAngleTurn);//(float) Math.min(Math.PI/3, (Math.PI - 2.1*Math.asin(1.0/GameEngine.defaultSpeed)));//Math.PI/3;
		if(bigdelta > deltamax)
			bigdelta = deltamax;
		else if(bigdelta < -deltamax)
			bigdelta = -deltamax;

		return last+bigdelta;
	}
	
	
	/** Retourne la liste chaînée des parties du snake **/
	public LinkedList<Part> getParts() {
		return parts;
	}
	
	/** Retourne l'élément Part représentant la tête du snake **/
	public Part getHead() {
		return parts.getFirst();
	}
	
	/** Change la taille voulue **/
	public void setSize(float s) {
		size = s;
	}
	
	static class Part {
		float x, y; // Position
		float direction;
		float radius;
		boolean containingTrap = false;
		
		Part(float px, float py, float dir) {
			radius = Rules.current.snakeDefaultSize;
			x = px;
			y = py;
			direction = dir;
		}
		
		public Part clone() {
			Part c = new Part(x, y, direction);
			c.radius = radius;
			return c;
		}
	}
	

	/********\
	|* TODO *|
	\********/
}
