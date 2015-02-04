package com.awprog.roundsnakemulti;

import java.util.LinkedList;
import java.util.Random;

import com.awprog.roundsnakemulti.GameEngine.DeathType;
import com.awprog.roundsnakemulti.Item.Effects;
import com.awprog.roundsnakemulti.Snake.Part;

public class Player {
	private float padDir;
	private Snake snk = new Snake();
	private int killScore;
	private int number, color;
	private String name;
	// Bonus équipé et utilisé
	private Item.Effects equippedItem; // should be only items with manualActivation=true
	private Item.Effects inUseItem;
	private boolean useEquippedItemRequest; // demande d'utilisation de l'item équipé
	// Etat vital
	private boolean isDead;
	private int deathDate;
	private int birthDate;
	// Reference vers les données du jeu
	final GameEngine gameEngineRef;
	
	
	static final int colors[] = {// TODO color auto with HSV
		0xffff0000,//rouge
		0xff0033ff,//bleu
		0xffff8800,//orange
		0xff00bb00,//vert
		0xffaa00aa,//violet
		0xfffffa00,//jaune
		0xffff66ff,//rose
		0xff888888,//gris
		0xff22ddee,//cyan
	};
	static final String colorsName[] = {
			"Red",
			"Blue",
			"Orange",
			"Green",
			"Purple",
			"Yellow",
			"Pink",
			"Grey",
			"Cyan",
	};
	static final int nbColor = Math.min(colorsName.length, colors.length);
	static final Random random = new Random();
	
	
	public Player(int num, GameEngine gameEngineRef) {
		color = colors[num % nbColor];
		name = colorsName[num%nbColor] + (num/nbColor >= 1 ? " "+(1+num/nbColor) : "");
		number = num;
		this.gameEngineRef = gameEngineRef;
	}
	
	/** Nom du joueur **/
	public void setName(String s) {
		name = s;
	}
	/** Nom du joueur **/
	public String getName() {
		return name;
	}
	/** Retourne le numéro du joueur **/
	public int getNumber() {
		return number;
	}

	/** Score du joueur en prennant en compte les règles du jeu **/
	public int getScore() {
		return (int) (Rules.current.killScoreFactor * killScore + Rules.current.lengthScoreFactor * snk.getVisibleLength());
	}
	/** Ajoute des kills au compteur de kill de ce joueur **/
	public void addKillScore(int score) {
		killScore += score;
	}
	/** Score du joueur **/
	public int getColor() {
		return color;
	}
	
	/** Retourne l'objet snake du joueur **/
	public Snake getSnake() {
		return snk;
	}
	
	/** Initialise le joueur pour une nouvelle partie **/
	public void newGame() {
		killScore = 0;
	}
	
	/** Initialise le joueur pour une nouvelle manche **/
	public void newRound() {
		Map map = gameEngineRef.getMap();
		int nbPlayers = gameEngineRef.getPlayerCount();
		
		// Calcul de la position et direction initiale
		double sqrt = Math.sqrt(nbPlayers);
		int h = (int) Math.round(sqrt), w = (int) Math.ceil(sqrt);
		float px = (number%w+.5f)/w*map.width;
		float py = (number/w+.5f)/h*map.height;
		
		snk.reset(px, py, padDir);

		equippedItem = null;
		inUseItem = null;
		isDead = false;
		birthDate = GameEngine.getElapsedStep();
		useEquippedItemRequest = false;
	}
	
	/** Tue le joueur **/
	public void kill(int murderer) {
		isDead = true;
		deathDate = GameEngine.getElapsedStep();
		
		equippedItem = null;
		inUseItem = null;
		useEquippedItemRequest = false;
	}
	
	/** Redonne vie au joueur **/
	public void revive() {
		Map map = gameEngineRef.getMap();

		birthDate = GameEngine.getElapsedStep();
		isDead = false;
		
		// Recherche d'une position non occupée par un item
		// TODO position non occupée par un autre snake
		int iterMax = 10;
		float x,y;
		do {
			x = random.nextFloat() * (map.width - Rules.current.snakeDefaultSize*2)  + Rules.current.snakeDefaultSize;
			y = random.nextFloat() * (map.height - Rules.current.snakeDefaultSize*2) + Rules.current.snakeDefaultSize;
		} while(map.collisionCircleItems(x,y,Rules.current.snakeDefaultSize) && --iterMax > 0);
		snk.reset(x, y, padDir);
	}
	
	/** Retourne le temps écoulé depuis la dernière mort **/
	public int getDeathTime() {
		return GameEngine.getElapsedStep() - deathDate;
	}
	/** Retourne le temps écoulé depuis la dernière naissance **/
	public int getLifeTime() {
		return GameEngine.getElapsedStep() - birthDate;
	}
	
	/** Indique si le joueur est mort **/
	public boolean isDead() {
		return isDead;
	}
	/** Indique si le joueur est mort pendant le tour actuel **/
	public boolean isRecentlyDead() {
		return isDead && GameEngine.getElapsedStep() == deathDate;
	}
	
	/** Avance d'un pas, actionne les items si nécessaire **/
	public void step() {
		Map map = gameEngineRef.getMap();
		if(isDead()) {
			if(getDeathTime() >= Rules.current.delayRevive && Rules.current.delayRevive != -1)
				revive();
		}
		else {
			snk.step(padDir, map, number);
			
			if(useEquippedItemRequest && hasEquippedItem()) {
				// Effet sur une période, interruption de l'effet en cours
				if(equippedItem.maxDuration > 0) {
					finishCurrenEffects();
					inUseItem = equippedItem;
				}
				// Si Utilisation instantanée, l'item en cours d'utilisation n'est pas interrompu
				
	
				// Utilisattion de l'item équipé
				applyEffects(equippedItem);
				equippedItem = null;
			}
			else {
				// mise à jour de l'item activé
				if(hasInUseItem()) {
					inUseItem.duration++;
					if(inUseItem.duration >= inUseItem.maxDuration) {
						finishCurrenEffects();
						inUseItem = null;
					}
				}
			}
			
			useEquippedItemRequest = false;
		}
	}

	/** Indique si un item est déjà équipé **/
	public boolean hasEquippedItem() {
		return equippedItem != null;
	}
	/** Retourne les effets de l'item équipé **/
	public Effects getEquippedItem() {
		return equippedItem;
	}
	
	/** Indique si un item est en cours d'utilisation **/
	public boolean hasInUseItem() {
		return inUseItem != null;
	}
	/** Retourne les effets de l'item utilisé **/
	public Effects getInUseItem() {
		return inUseItem;
	}
	
	/** Demande l'utilisation de l'item équipé **/
	public void sendUseItemRequest() {
		if(hasEquippedItem() && !isDead)
			useEquippedItemRequest = true;
	}

	/** Applique l'effet **/
	public void applyEffects(Effects effect) {
		/// Accélération / décélération
		if(effect.snakeRadiusMultiplicator != 1.0f)
			snk.setSize(Rules.current.snakeDefaultSize * inUseItem.snakeRadiusMultiplicator);
		
		if(effect.fatalTrap) {
			/// Piège posé par le joueur
			if(effect.manualActivation) {
				Part head = snk.getHead();
				float lastRadius = head.radius;
				head.radius = Rules.current.trapMinRadius +  Rules.current.trapMaxRadiusAugmentation*random.nextFloat();
				head.x += (head.radius-lastRadius)*Math.cos(head.direction);
				head.y += (head.radius-lastRadius)*Math.sin(head.direction);
				head.containingTrap = true;
			}
			/// Joueur tombé dans le piège 
			else {
				kill(effect.player);
				gameEngineRef.createDeathCertificate(number, effect.player, DeathType.Trap);
			}
		}
		
		/// Tête cisaillante
		if(effect.sharpTeeth)
			/* Just keep this effect in 'inUseItem' */;
		
		/// Croissance
		if(effect.snakeGrowth != 0)
			snk.changeLength(effect.snakeGrowth);
		
	}
	
	/** Stoppe l'effet actuel **/
	public void finishCurrenEffects() {
		if(hasInUseItem()) {
			if(inUseItem.snakeRadiusMultiplicator != 1.0f)
				snk.setSize(Rules.current.snakeDefaultSize);
		}
	}
	
	/** Modifie la direction souhaité par le joueur; en radians **/
	void setPadDir(float dir) {
		padDir = dir;
	}

	/** Retourne la position en X de la tête du snake **/
	public float getSnakeHeadX() {
		return snk.getHead().x;
	}
	/** Retourne la position en Y de la tête du snake **/
	public float getSnakeHeadY() {
		return snk.getHead().y;
	}
	/** Retourne la taille (rayon) de la tête du snake **/
	public float getSnakeHeadRadius() {
		return snk.getHead().radius;
	}
	/** Coupe le snake à partir de la partie donnée **/
	public void cutSnake(Part p) {
		LinkedList<Part> body = snk.getParts();
		Part queue;
		do {
			queue = body.getLast();
			body.removeLast();
		} while(queue != p && body.size() > 1);
		
		snk.setLength(body.size());
	}
	
	/** Indique si le joueur peut ramasser l'item donné **/
	public boolean canCollect(Item item) {
		return !(item.effects.manualActivation && hasEquippedItem()) && !isDead;
	}
	
	/** Ramasse et applique les effets de l'item **/
	public void collect(Item item) {
		if(item.effects.manualActivation) {
			if(!hasEquippedItem())
				equippedItem = item.effects;
		}
		else {
			// Effet sur une période, interruption de l'effet en cours
			if(item.effects.maxDuration > 0) {
				finishCurrenEffects();
				inUseItem = item.effects;
			}
			// Si Utilisation instantanée, l'item en cours d'utilisation n'est pas interrompu

			// Utilisattion de l'item équipé
			applyEffects(item.effects);
		}
	}
	

	/** Donne des points aux joueurs en fonction de l'identité de la victime et du tueur **/
	static protected void changeKillScore(Player[] players, int murderer, int victim) {
		if(murderer == victim)
			players[victim].addKillScore(Rules.current.scoreSuicide);
		
		for(int i = 0; i < players.length; i++) {
			if(i == victim) { 
				if(victim != murderer)
					players[i].addKillScore(Rules.current.scoreTarget);
			}
			else if(i == murderer) {
				if(victim != murderer && !players[i].isDead())
					players[i].addKillScore(Rules.current.scoreKiller);
			}
			else {
				if(!players[i].isDead())
					players[i].addKillScore(Rules.current.scoreOther);
			}
		}
	}
}
