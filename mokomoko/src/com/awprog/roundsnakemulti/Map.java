package com.awprog.roundsnakemulti;

import java.util.ArrayList;

import com.awprog.roundsnakemulti.GameEngine.DeathType;
import com.awprog.roundsnakemulti.Item.Effects.Appearance;
import com.awprog.roundsnakemulti.Snake.Part;

public class Map {
	float width, height;// en nombre de case (<=> largeur d'un serpent)
	private int maxApple, maxBonus, nbApples = 0, nbBonus = 0;
	private ArrayList<Item> items = new ArrayList<Item>();
	// Reference vers les données du jeu
	final GameEngine gameEngineRef;
	
	Map(int nbPlayer, float width, float height, GameEngine gameEngineRef) {
		this.width = width;
		this.height = height;
		maxApple = Math.round(Rules.current.nbApplesPerPlayer * nbPlayer + Rules.current.nbApplesConstant);
		maxBonus = Math.round(Rules.current.nbBonusPerPlayer * nbPlayer + Rules.current.nbBonusConstant);
		
		this.gameEngineRef = gameEngineRef;
	}

	/** Initialise la carte pour une nouvelle partie **/
	public void newGame() {
		items.clear();
		nbApples = 0;
		nbBonus = 0;
	}
	/** Initialise la carte pour une nouvelle manche **/
	public void newRound() {
		items.clear();
		nbApples = 0;
		nbBonus = 0;
		createItems();
	}
	
	/** Rajoute de nouveaux items pour remplacer ceux qui ont été supprimé **/
	void createItems() {
		while(maxApple > nbApples) {
			items.add(Item.createApple(this));
			nbApples++;
		}
		while(maxBonus > nbBonus) {
			items.add(Item.createBonus(this));
			nbBonus++;
		}
	}
	/** Pose un piège **/
	public void setTrap(int player, float px, float py, float radius) {
		Item trap = Item.createTrap(px, py, radius);
		trap.effects.player = player;
		items.add(trap);
	}
	/** Retourne la listes des items présents sur la carte **/
	ArrayList<Item> getItems() {
		return items;
	}
	
	/** Indique si le cercle de position (x,y) et de rayon 'radius' est en collision avec un des items de la map **/
	public boolean collisionCircleItems(float x, float y, float radius) {
		for(Item i : items)
			if((x-i.x)*(x-i.x)+(y-i.y)*(y-i.y) <= (radius+i.radius)*(radius+i.radius))
				return true;
		return false;
	}
	
	/** Retourne toutes les positions équivalentes à la position donnée **/
	ArrayList<float[]> getEquivalentPositions(float x, float y, float r) {
		ArrayList<float[]> list = new ArrayList<float[]>();
		list.add(new float[] {x,y});
		
		if(x-r < 0)
			list.add(new float[] {x+width, y});
		else if(x+r > width)
			list.add(new float[] {x-width, y});
		
		if(y-r < 0) {
			for(int i = list.size(); --i >= 0;) {
				float[] p = list.get(i).clone();
				p[1] += height;
				list.add(p);
			}
		}
		else if(y+r > height) {
			for(int i = list.size(); --i >= 0;) {
				float[] p = list.get(i).clone();
				p[1] -= height;
				list.add(p);
			}
		}
		
		return list;
	}
	
	/** Replace l'élément dans le cadre de la carte **/
	void putInside(Part head) {
		if(head.x < 0) head.x += width;
		if(head.x > width) head.x -= width;
		if(head.y < 0) head.y += height;
		if(head.y > height) head.y -= height;
	}
	
	/** Mise à jour de la carte, donne les items aux joueurs qui les ramassent **/
	public void step() {
		/// Contact avec un items, le joueur le plus proche ramasse
		for(int i = 0; i < items.size(); i++) {
			Item item = items.get(i);
			Player p = null;
			float depth = 0;// profondeur de collision maximum
			
			for(float[] pos : getEquivalentPositions(item.x, item.y, item.radius)) {
				for(Player player : gameEngineRef.getPlayers()) { 
					for(float[] ppos : getEquivalentPositions(player.getSnakeHeadX(), player.getSnakeHeadY(), item.radius)) {
						if(!player.isDead()) {
							float x = ppos[0] - pos[0], y = ppos[1] - pos[1];
							float d = (float) Math.hypot(x, y);
							float d_col = item.radius + player.getSnakeHeadRadius();
							if(d <= d_col && player.canCollect(item))
							if(depth < d_col-d) {
								p = player;
								depth = d_col-d;
							}
						}
					}
				}
			}
			
			if(p != null) {
				p.collect(item);
				if(item.effects.appearance == Appearance.APPLE)
					nbApples--;
				else if(item.effects.appearance == Appearance.BONUS)
					nbBonus--;
				
				items.remove(i);
				i--;
			}
		}
		
		/// Collision entre joueur
		for(int i = 0; i < gameEngineRef.getPlayerCount(); i++) {
			/// Pour varier les priorités, on change à chaque frame
			Player player = gameEngineRef.getPlayer((i+GameEngine.getElapsedStep()) % gameEngineRef.getPlayerCount());
			if(player.isDead())
				continue;
			Part head = player.getSnake().getHead();
			
			for(Player p : gameEngineRef.getPlayers())
			if(!p.isDead() || p.isRecentlyDead()) {
				int partIndex = 0;
				for(Part part : p.getSnake().getParts()) {
					if(p != player || partIndex > 2){
						float dx = part.x-head.x, dy = part.y-head.y;
						float d2 = dx*dx+dy*dy, d_col = part.radius+head.radius;
						
						// Collision
						if(d2 < d_col*d_col) {
							// Dents tranchantes -> l'adversaire se fait couper la queue
							if(player.hasInUseItem() && player.getInUseItem().sharpTeeth && part != p.getSnake().getHead()) {
								p.cutSnake(part);
								break;
							}
							// Le joueur mange la queue de l'autre et meurt
							else {
								player.kill(p.getNumber()); // peut être un suicide
								gameEngineRef.createDeathCertificate(player.getNumber(), p.getNumber(), DeathType.Hit);
								break;
							}
						}
					}
					partIndex++;
				}
			}
		}
	}

}
