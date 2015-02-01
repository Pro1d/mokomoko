package com.awprog.roundsnakemulti;

import java.util.ArrayList;


public class GameEngine {
	static final float defaultSpeed = 1.6f; /* distance de 1.6*0.5 entre le centre de deux ronds */;
	
	private int stepCount;
	public int nbFramePerStep;
	public boolean isRoundFinished, isGameFinished;
	private int roundFinishedDate;
	private static final int interRoundStepDuration = 20;
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
		
		if(isGameFinished)
			return;
		
		if(isRoundFinished) {
			if(stepCount - roundFinishedDate >= interRoundStepDuration)
				newRound();
			else
				return;
		}
		
		
		/// Joueurs
		for(Player player : players)
			player.step(map, stepCount);
		
		/// Carte
		map.step(players, stepCount);
		
		/// Fin de manche / de partie
		if(isRoundFinished()) {
			isRoundFinished = true;
			roundFinishedDate = stepCount;
			
			if(isGameFinished()) {
				isGameFinished = true;
			}
		}
	}
	
	/** Retourne le nombre de frame écoulé depuis le début de la partie **/
	public int getElapsedStep() {
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
		
		isRoundFinished = false;
		isGameFinished = false;
	}
	
	/** Initialisation pour une nouvelle manche **/
	public void newRound() {
		/// Carte
		map.newRound();
		
		/// Joueurs
		for(Player p : players)
			p.newRound(getPlayerCount(), map);
		
		isRoundFinished = false;
	}
	
	/** Teste si la manche est terminée **/
	private boolean isRoundFinished() {
		// Si il y a un revive, la manche se termine lorsque le score max est atteint
		if(Rules.current.delayRevive != -1) {
			for(Player p : players)
				if(p.getScore() >= Rules.current.scoreLimit)
					return true;
		}
		// sinon la manche se termine sur les plages de Normandie
		else {
			/// il y a moins de 2 joueurs en vie
			int alivePlayerCount = playerCount;
			for(Player p : players)
				if(p.isDead())
					alivePlayerCount--;
			
			if(alivePlayerCount < 2)
				return true;
		}
		
		return false;
	}
	/** Teste si la partie est terminée. Si elle retourne vrai, la
	 * fonction getWinners peut être appelée. A appeler seulement si
	 * 'isRoundFinished' retourne vrai **/
	private boolean isGameFinished() {
		for(Player p : players)
			if(p.getScore() >= Rules.current.scoreLimit)
				return true;
		return false;
	}
	
	/** Retourne les numéros des gagnants **/
	public ArrayList<Integer> getWinners() {
		ArrayList<Integer> winners = new ArrayList<Integer>();
		int bestScore = players[0].getScore();
		for(Player p : players)
			if(p.getScore() >= Rules.current.scoreLimit) {
				if(p.getScore() == bestScore)
					winners.add(p.getNumber());
				else if(p.getScore() > bestScore) {
					winners.clear();
					bestScore = p.getScore();
					winners.add(p.getNumber());
				}
			}
		
		return winners;
	}
	/** Retourne les numéros des joueurs encore en vie **/
	public ArrayList<Integer> getAlivePlayers() {
		ArrayList<Integer> alive = new ArrayList<Integer>();
		for(Player p : players)
			if(!p.isDead())
				alive.add(p.getNumber());
		
		return alive;
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

