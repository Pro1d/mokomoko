package com.awprog.roundsnakemulti;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;


public class GameEngine {
	
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
	// Liste des dernières victimes
	private LinkedList<DeathCertificate> deathHistory = new LinkedList<DeathCertificate>();

	GameEngine() {
		setPlayerCount(2);
		setMapSize(defaultScaleLevel, 1);
		setSpeedLevel(defaultSpeedLevel);
		reset();
	}
	
	/** Réinitialise tout, à appeler après un changement de mode de jeu par exemple **/
	synchronized public void reset() {
		players = new Player[playerCount];
		for(int i = 0; i < playerCount; i++)
			players[i] = new Player(i, this);
		map = new Map(players.length, getScaleLevel(scaleLevel)*ratio, getScaleLevel(scaleLevel), this);
		newGame();
		newRound();
	}
	/** Indique si la partie a commencé **/
	public boolean hasBegun() {
		return stepCount > 0;
	}
	
	/** Crée le nombre de joueurs désiré; nécessite l'appel à reset() après **/
	synchronized public void setPlayerCount(int count) {
		/*players = new Player[count];
		for(int i = 0; i < count; i++)
			players[i] = new Player(i);*/
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
	/** Retourne le tableau des joueurs **/
	public Player[] getPlayers() {
		return players;
	}
	
	/** Crée une nouvelle carte avec les dimensions données; nécessite l'appel à reset() après **/
	synchronized public void setMapSize(int scaleLevel, float ratio) {
		this.scaleLevel = scaleLevel;
		this.ratio = ratio;
		//map = new Map(players.length, getScaleLevel(scaleLevel)*ratio, getScaleLevel(scaleLevel));
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
			player.step();
		
		/// Carte
		map.step();
		map.createItems();
		
		/// Gestion des décès
		for(DeathCertificate dt : deathHistory) {
			/// Nouveau kill, on attribue les points
			if(dt.date == getElapsedStep()) {
				Player.changeKillScore(getPlayers(), dt.murderer, dt.dead);
			}
		}
		
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
		deathHistory.clear();
	}
	
	/** Initialisation pour une nouvelle manche **/
	public void newRound() {
		/// Carte
		map.newRound();
		
		/// Joueurs
		for(Player p : players)
			p.newRound();
		
		isRoundFinished = false;
		deathHistory.clear();
	}
	
	/** Teste si la manche est terminée **/
	private boolean isRoundFinished() {
		// Si il y a un revive, la manche se termine lorsque le score max est atteint
		if(Rules.current.delayRevive != -1) {
			for(Player p : players)
				if(p.getScore() >= Rules.current.scoreAim)
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
			if(p.getScore() >= Rules.current.scoreAim)
				return true;
		return false;
	}
	
	/** Retourne les numéros des gagnants **/
	public ArrayList<Integer> getWinners() {
		ArrayList<Integer> winners = new ArrayList<Integer>();
		int bestScore = players[0].getScore();
		for(Player p : players)
			if(p.getScore() >= Rules.current.scoreAim) {
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
	/** Retourne le classement de chaque joueur en fonction de leur score
	 * sous forme d'une liste contenant à l'index i le numéro du joueur à
	 * la (i+1)ème position **/
	public ArrayList<Integer> getOrder() {
		ArrayList<Integer> order = new ArrayList<Integer>();
		for(int i = 0; i < playerCount; i++)
			order.add(i);
		Collections.sort(order, new Comparator<Integer>() {
			@Override
			public int compare(Integer arg0, Integer arg1) {
				return players[arg1].getScore() - players[arg0].getScore();
			}
		});
		
		return order;
	}
	
	/** La liste des certificats de décès**/
	public LinkedList<DeathCertificate> getDeathCertificates() {
		return deathHistory;
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
	
	public void createDeathCertificate(int dead, int murderer, DeathType dt) {
		deathHistory.add(new DeathCertificate(dead, murderer, dt));
	}
	
	enum DeathType { Trap, Hit };
	public class DeathCertificate {
		static final int validityDuration = 8;
		int date;
		int dead;
		int murderer;
		DeathType death;
	
		public DeathCertificate(int dead, int murderer, DeathType death) {
			date = getElapsedStep();
			this.dead = dead;
			this.murderer = murderer;
			this.death = death; 
		}
	}
}

