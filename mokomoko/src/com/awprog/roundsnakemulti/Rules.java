package com.awprog.roundsnakemulti;

public class Rules {
	/**
	 * Mode de jeu:
	 * 	match à mort : -> faire des frags
	 * 	law of the strongest croissance : -> atteindre un taille 
	 *  overgrowth "42" : longueur fixe pas de pomme ni de bonus
	 */
	static class Values {
		String name; // Nom du mode de jeu
		
		/** Carte **/
		 // nombre de pommes et d'items max présents en même temps sur la carte
		float nbApplesPerPlayer, nbApplesConstant;// total = nbApplesConstant + nbPlayer*nbApplesPerPlayer
		float nbBonusPerPlayer, nbBonusConstant;// total = nbItemsConstant + nbPlayer*nbItemsPerPlayer
		//float mapScale; // Echelle de la carte
	
		/** Bonus **/
		float bonusEffectDurationFactor; // multiplicateur de la durée des effets des bonus
		int appleGrowth; // longueur gagnée avec un pomme
		float trapMinRadius; // Rayon minimum d'un piège
		float trapMaxRadiusAugmentation; // Rayon supplémentaire max, rayon total = trapMinRadius + random * trapMaxRadiusAugmentation
		float itemRadius; // Taille des pommes et bonus 
		
		/** Vie/mort **/
		int delayRevive; // temps avant revive; -1 pas de revive et fin du round si moins de 2 joueurs vivants
		
		/** Snake **/
		int initialSnakeLength; // longueur initiale des serpents
		float snakeGrowthWithLengthFactor; // Taille du snake en fonction de sa longueur
		float maxSnakeAngleTurn; // Angle de variation maximum du snake pour les déplacements (minimise la courbure des virages)
		float snakeSpeedBonusFactor; // taux de croissance avec le bonus de vitesse
		float snakeDefaultSpeed;// vitesse par défaut; écart entre rond égale à size*speed
		float snakeDefaultSize;// vitesse par défaut; écart entre rond égale à size*speed
		float snakeSizeVariationSpeed; // k : vitesse de variation de taille; newsize = realsize * k + lastsize * (1-k)
		
		/** Score **/
		int scoreKiller, scoreTarget, scoreOther, scoreSuicide; // score obtenu par chacun à chaque kill
		int scoreAim; // Score à atteindre pour gagner
		float killScoreFactor, lengthScoreFactor; // score joueur = scoreKill*killScoreFactor + snake.length*lengthScoreFactor
	}
	
	
	public static final int RULES_DM = 0, RULES_OG = 1, RULES_LR = 2, RULES_CS = 3;
	private static final Values[] gameMode = {
		createRules(RULES_DM),
		createRules(RULES_OG),
		createRules(RULES_LR),
		createRules(RULES_CS)
	};
	
	private static int currentRules = RULES_DM;
	public static Values current = gameMode[currentRules];
	
	public static void setRulesType(int type) {
		currentRules = type;
		current = gameMode[currentRules];
	}
	/*public static Rules getRules() {
		return gameMode[currentRules];
	}*/
	
	/** Création des modes de jeu **/
	private static Values createRules(int mode) {
		Values r = new Values();
		
		r.nbApplesConstant = 1;
		r.nbApplesPerPlayer = 0.7f;
		r.nbBonusConstant = 1;
		r.nbBonusPerPlayer = 0.3f;

		r.bonusEffectDurationFactor = 1.0f;
		r.appleGrowth = 2;
		r.trapMinRadius = 1.0f;
		r.trapMaxRadiusAugmentation = 0.5f;
		r.itemRadius = 0.75f;
		
		r.delayRevive = -1;
		
		r.initialSnakeLength = 4;
		r.snakeGrowthWithLengthFactor = 1.0f;
		r.maxSnakeAngleTurn = 60;
		r.snakeSpeedBonusFactor = 1.45f;
		r.snakeDefaultSize = 0.5f;
		r.snakeDefaultSpeed = 1.6f;
		r.snakeSizeVariationSpeed = 0.5f;
		
		r.scoreKiller = 1;
		r.scoreOther = 1;
		r.scoreTarget = 0;
		r.scoreSuicide = 0;
		r.scoreAim = 10;
		r.killScoreFactor = 1;
		r.lengthScoreFactor = 0;
		
		switch(mode) {
		case RULES_DM:
			r.name = "Death Match";
			r.scoreOther = 0;
			r.delayRevive = 10;
			r.bonusEffectDurationFactor = 1.5f;
			break;
		case RULES_OG:
			r.name = "Overgrowth";
			r.initialSnakeLength = -1;
			r.nbApplesConstant = r.nbApplesPerPlayer = r.nbBonusConstant = r.nbBonusPerPlayer = 0;
			break;
		case RULES_LR:
			r.name = "Race";
			r.initialSnakeLength = 2;
			r.scoreKiller = 0;
			r.scoreOther = 0;
			r.delayRevive = 10;
			r.bonusEffectDurationFactor = 1.5f;
			r.scoreAim = 12; // <=> longueur à atteindre
			r.snakeGrowthWithLengthFactor = 1.015f;//~ pow(1.4, 1/12)
			r.killScoreFactor = 0;
			r.lengthScoreFactor = 1;
			break;
		case RULES_CS:
			r.name = "Suicide";
			r.initialSnakeLength = 2;
			r.scoreKiller = 0;
			r.scoreOther = 0;
			r.scoreSuicide = 1;
			r.delayRevive = 3;
			r.scoreAim = 7; // <=> nombre de suicides à faire
			r.maxSnakeAngleTurn = 45;
			r.appleGrowth = 1;
			break;
		}
		
		return r;
	}
}
