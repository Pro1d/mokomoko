package com.awprog.roundsnakemulti;

public class Rules {
	/**
	 * Mode de jeu:
	 * 	match à mort : -> faire des frags
	 * 	law of the strongest croissance : -> atteindre un taille 
	 *  overgrowth "42" : longueur fixe pas de pomme ni de bonus
	 */
	
	String name; // Nom du mode de jeu
	
	/** Carte **/
	 // nombre de pommes et d'items max présents en même temps sur la carte
	float nbApplesPerPlayer, nbApples;// total = nbApples + nbPlayer*nbApplesPerPlayer
	float nbItemsPerPlayer, nbItems;// total = nbItems + nbPlayer*nbItemsPerPlayer
	float mapScale; // Echelle de la carte

	/** Bonus **/
	float bonusDurationMultiplicator; // multiplicateur de la durée des bonus 
	
	/** Vie/mort **/
	int delayRevive; // temps avant revive; -1 pas de revive et fin du round si moins de 2 joueurs vivants
	
	/** Snake **/
	int initialLength; // longueur initiale des serpents
	float growthWithLengthFactor; // Taille des snakes en fonction de sa longueur
	
	/** Score **/
	int scoreKiller, scoreTarget, scoreOther; // score obtenu par chacun à chaque kill
	int scoreLimit; // Score à atteindre pour gagner
	float killScoreFactor, lengthScoreFactor; // score joueur = scoreKill*killScoreFactor + snake.length*lengthScoreFactor
	
	
	
	public static final int RULES_DM = 0, RULES_OG = 1, RULES_LR = 2;
	private static final Rules[] gameMode = {
		createRules(RULES_DM),
		createRules(RULES_OG),
		createRules(RULES_LR)
	};
	
	public static int currentRules = RULES_DM;
	
	public static void setRulesType(int type) {
		currentRules = type;
	}
	public static Rules getRules() {
		return gameMode[currentRules];
	}
	
	/** Création des modes de jeu **/
	private static Rules createRules(int mode) {
		Rules r = new Rules();
		
		r.nbApples = 1;
		r.nbApplesPerPlayer = 0.7f;
		r.nbItems = 1;
		r.nbItemsPerPlayer = 0.3f;
		r.mapScale = 1.0f;
		r.bonusDurationMultiplicator = 1.0f;
		r.delayRevive = -1;
		r.initialLength = 4;
		r.growthWithLengthFactor = 1.0f;
		r.scoreKiller = 0;
		r.scoreOther = 1;
		r.scoreTarget = 0;
		r.scoreLimit = 10;
		r.killScoreFactor = 1;
		r.lengthScoreFactor = 0;
		
		switch(mode) {
		case RULES_DM:
			r.name = "Death Match";
			r.scoreKiller = 1;
			r.scoreOther = 0;
			r.delayRevive = 10;
			r.bonusDurationMultiplicator = 1.5f;
			break;
		case RULES_OG:
			r.name = "Overgrowth";
			r.scoreKiller = 0;
			r.scoreOther = 1;
			r.initialLength = 42;
			r.nbApples = r.nbApplesPerPlayer = r.nbItems = r.nbItemsPerPlayer = 0;
			break;
		case RULES_LR:
			r.name = "Race";
			r.initialLength = 2;
			r.scoreKiller = 1;
			r.scoreOther = 0;
			r.delayRevive = 10;
			r.bonusDurationMultiplicator = 1.5f;
			r.scoreLimit = 12; // <=> longueur à atteindre
			r.growthWithLengthFactor = 1.03f;//~ pow(1.4, 1/12)
			r.killScoreFactor = 0;
			r.lengthScoreFactor = 1;
			break;
		}
		
		return r;
	}
}
