package com.awprog.roundsnakemulti;


public class GameEngine {
	static final float defaultSpeed = 1.6f; /* distance de 1.6*0.5 entre le centre de deux ronds */;
	
	private int stepCount;
	public int nbFramePerStep;
	/// Carte
	private Map map;
	private int scaleLevel;
	private float ratio;
	/// Joueur
	private Player[] players;
	private int playerCount;

	GameEngine() {
		setPlayerCount(2);
		setMapSize(defaultScaleLevel, 1);
		setSpeedLevel(defaultSpeedLevel);
	}
	
	/** Réinitialise tout, à appeler après un changement de mode de jeu par exemple **/
	public void reset() {
		setPlayerCount(playerCount);
		setMapSize(scaleLevel, ratio);
		newGame();
		newRound();
	}
	/** Indique si la partie a commencé **/
	public boolean hasBegun() {
		return stepCount > 0;
	}
	
	/** Crée le nombre de joueurs désiré **/
	public void setPlayerCount(int count) {
		players = new Player[count];
		for(int i = 0; i < count; i++)
			players[i] = new Player(i);
		playerCount = count;
	}
	/** Retourne le nombre de joueur **/
	public int getPlayerCount() {
		return playerCount;
	}
	/** Retourne le joueur numéro i **/
	public Player getPlayer(int i) {
		return players[i];
	}

	
	/** Crée une nouvelle carte avec les dimensions données **/
	public void setMapSize(int scaleLevel, float ratio) {
		this.scaleLevel = scaleLevel;
		this.ratio = ratio;
		map = new Map(players.length, getScaleLevel(scaleLevel)*ratio, getScaleLevel(scaleLevel));
	}
	/** Retourne le ratio height/width de la carte **/
	public float getMapRatio() {
		return ratio;
	}
	/** Retourne le ratio height/width de la carte **/
	public int getMapScaleLevel() {
		return scaleLevel;
	}
	
	/** Traite un évènement de joystick **/
	public void handleJoystickEvent(int player, float x, float y) {
		if(x != 0 || y != 0) {
			float dir = (float) Math.atan2(y, x);
			players[player].setPadDir(dir);
		}
	}
	/** Traite un évènement de bouton **/
	public void handleButtonEvent(int player, boolean keydown) {
		if(keydown)
		if(!players[player].isDead() && players[player].hasEquippedItem()) {
			players[player].sendUseItemRequest();
		}
	}

	/** Exécute le moteur pour un tour **/
	public void step() {
		stepCount++;
		
		/// Joueurs
		for(Player player : players)
			player.step(map, stepCount);
		
		/// Carte
		map.step(players, stepCount);
	}
	
	/** Retourne le nombre de frame écoulé depuis le début de la partie **/
	public int getElapsedFrame() {
		return stepCount;
	}

	/** Initialisation pour une nouvelle partie **/
	public void newGame() {
		stepCount = 0;
		/// Carte
		map.newGame();
		
		/// Joueurs
		for(Player p : players)
			p.newGame();
	}
	
	/** Initialisation pour une nouvelle manche **/
	public void newRound() {
		/// Carte
		map.newRound();
		
		/// Joueurs
		for(Player p : players)
			p.newRound(getPlayerCount(), map);
	}
	
	/** Retourne la carte **/
	public Map getMap() {
		return map;
	}
	
	static final int maxSpeedLevel = 7, defaultSpeedLevel = 3;
	void setSpeedLevel(int speed) {
		nbFramePerStep = (new int[] {
				24,
				18,
				12,
				8,
				6,
				4,
				2,
				1
		})[speed];
	}
	
	static final int maxScaleLevel = 7, defaultScaleLevel = 3;
	float getScaleLevel(int scale) {
		return (new float[] {
				18,
				21,
				25,
				30,
				36,
				43,
				51,
				60
		})[scale];
		// TODO compute width
		// TODO apply the new dimensions to the map
	}
}

