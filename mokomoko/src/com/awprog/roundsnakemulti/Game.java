package com.awprog.roundsnakemulti;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Game {
	final float defaultSpeed = 0.8f, defaultSize = 0.5f, biggestSizeFactor = 1.45f;
	Random random;
	
	float controlSize = 150, controlDead = 20;
	boolean pause=true, unpause=false, roundEnded = false;
	volatile boolean isGameInit = false;
	int nbPlayers, nbPlayerAlive;
	int nbFramePhysic, frame=0;
	Map map; int mapType; float mapScale;
	volatile Player[] players;
	
	Game() {
		random = new Random();
		
		nbPlayers = 2;
		setSpeedLevel(defaultSpeedLevel);
		setScaleLevel(defaultScaleLevel);
		mapType = Map.CIRCLE;
		//initGame(nbJoueurs, nbFramePhysic);
	}
	
	String getWinner() {
		String name = null;
		for(Player p : players) {
			if(p.alive)
				name = p.colorsName[p.number% p.colors.length];
		}
		return name;
	}
	void setMapType(int mapType) {
		this.mapType = mapType;
		isGameInit = false;
	}
	void setNbPlayer(int nbPlayers) {
		this.nbPlayers = nbPlayers;
		isGameInit = false;
	}
	static final int maxSpeedLevel = 7, defaultSpeedLevel = 3;
	void setSpeedLevel(int speed) {
		nbFramePhysic = (new int[] {
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
	void setScaleLevel(int scale) {
		mapScale = (new float[] {
				18,
				21,
				25,
				30,
				36,
				43,
				51,
				60
		})[scale];
		isGameInit = false;
	}

	void initGame() {
		map = new Map(nbPlayers, mapType, mapScale);
		map.height = 50;
		
		players = new Player[nbPlayers];
		for(int i = 0; i < nbPlayers; i++)
			players[i] = new Player(i);
		map.initRound();
		pause = true;
		isGameInit = true;
		frame = 0;
	}
	void initRound() {
		pause=false;
		unpause=false;
		roundEnded = false;
		map.initRound();
	}
	void endRound() {
		pause = true;
		roundEnded = true;
	}
	
	class Map {
		static final int CIRCLE = 0, SQUARE = 1;
		int mapType;
		float scale = 20, height = 30, width = 30;
		int nbApple, nbItem, nbDeath;
		ArrayList<Item> apples, items;
		
		Map(int nbJoueurs, int mapType, float scale) {
			nbApple = Math.max((int) (nbJoueurs*0.7), 1);
			nbItem = (nbJoueurs-nbApple);
			nbDeath = 0;
			apples = new ArrayList<Item>();
			items = new ArrayList<Item>();
			this.mapType = mapType;
			this.height = this.width = scale;
		}
		void setJoystickSize(float d) {
			controlSize = d;
			controlDead = d*0.125f;
		}
		void initRound() {
			nbPlayerAlive = nbPlayers;
			for(Player p : players)
				p.initRound();
			apples.clear();
			items.clear();
			nbDeath = 0;
			addApplesAndItems();
		}
		void destroyApple(int i) {
			apples.remove(i);
		}
		void destroyItem(int i){
			items.remove(i);
		}
		void addDeath(float[] d) {
			items.add(new Item(d));
			nbDeath++;
		}
		void destroyDeath(int i) {
			items.remove(i);
			nbDeath--;
		}
		void addApplesAndItems() {
			while(apples.size() < nbApple)
				apples.add(new Item(true));
			while(items.size() < nbItem+nbDeath)
				items.add(new Item(false));
		}
		// retourne toutes les positions équilvalentes à la position donnée 
		ArrayList<float[]> getAllPositions(float x, float y, float r) {
			ArrayList<float[]> list = new ArrayList<float[]>();
			list.add(new float[] {x,y});
			
			if(map.mapType == Map.SQUARE) {
				if(x-r < 0)
					list.add(new float[] {x+map.width, y});
				else if(x+r > map.width)
					list.add(new float[] {x-map.width, y});
				
				if(y-r < 0) {
					for(int i = list.size(); --i >= 0;) {
						float[] p = list.get(i).clone();
						p[1] += map.height;
						list.add(p);
					}
				}
				else if(y+r > map.height) {
					for(int i = list.size(); --i >= 0;) {
						float[] p = list.get(i).clone();
						p[1] -= map.height;
						list.add(p);
					}
				}
			}
			else if(map.mapType == Map.CIRCLE) {
				float dx = (x-map.width/2), dy = (y-map.height/2);// / (map.height/map.width); // correction en cas d'ovale
				final float d_2 = dx*dx+dy*dy;
				final float radius = Math.min(map.width, map.height)/2;
				if(d_2 > (radius-r)*(radius-r)) {
					float d = (float) Math.sqrt(d_2);
					float k = 2.0f * (radius / d) - 1.0f;
					list.add(new float[] {
						map.width * .5f - dx * k,
						map.height * .5f - dy * k
					});
				}
			}
			
			return list;
		}
		class Item {
			static final int APPLE = 0, BIGGEST = 1, OBSTACLE = 2, DEATH = 3, MONSTER_HEAD = 4;
			float size = 0.75f;
			/*final float radius = random.nextFloat()*(map.width/2-size);
			final float alpha = (float) (random.nextFloat()*2*Math.PI);*/
			float pos[] = new float[2];/*{(float) (Math.cos(alpha)*radius)+map.width/2,
							(float) (Math.sin(alpha)*radius)+map.width/2};*/
			int length = 0; // gain de taille
			Item(boolean apple) {
				setRandomPos(pos, size);
				if(apple)
					length = 2;
				else {
					switch(random.nextInt(3)) {
					case 0:
						type = BIGGEST;
						speed = defaultSpeed*biggestSizeFactor;
						sizeup = defaultSize*biggestSizeFactor;
						duration = (int) (Math.sqrt(map.width*map.height)/defaultSpeed);// pour avoir le temps de traverser la carte un fois au moins
						break;
					case 1:
						type = OBSTACLE;
						obstacle[2] = size * (1 + 2 * random.nextFloat());
						//setRandomPos(obstacle, obstacle[2]);
						break;
					case 2:
						type = MONSTER_HEAD;
						duration = (int) (Math.sqrt(map.width*map.height)/defaultSpeed);// pour avoir le temps de traverser la carte un fois au moins
						break;
					}
				}
			}
			Item (float [] death) {
				type = DEATH;
				size = death[2];
				pos[0] = death[0];
				pos[1] = death[1];
				start = 8;// tour avant mise en service
			}

			private void setRandomPos(float p[], float r) {
				boolean collision;
				do {
					collision = false;
					if(mapType == SQUARE) {
						p[0] = random.nextFloat()*(map.width-r*2)+r;
						p[1] = random.nextFloat()*(map.height-r*2)+r;
					}
					else if(mapType == CIRCLE) {
						final float radius = random.nextFloat()*(map.width/2-r);
						final float alpha = (float) (random.nextFloat()*2*Math.PI);
						p[0] = (float) (Math.cos(alpha)*radius)+map.width/2;
						p[1] = (float) (Math.sin(alpha)*radius)+map.width/2;
					}
					for(Item a : apples) {
						float dx = p[0]- a.pos[0], dy = p[1]-a.pos[1], dr = r+a.size;
						if(dx*dx+dy*dy < dr*dr)
							collision = true;
					}
					for(Item a : items) {
						float dx = p[0]- a.pos[0], dy = p[1]-a.pos[1], dr = r+a.size;
						if(dx*dx+dy*dy < dr*dr)
							collision = true;
					}
				} while(collision);
			}
			
			int type = APPLE;
			float obstacle[] = {0,0,0}; int start = 0;
			float speed=defaultSpeed; int duration = 0; float sizeup = defaultSize;
		}
	}
	
	class Player {
		float dir;
		LinkedList<float[]> snk=new LinkedList<float[]>();
		int length;
		float speed, size;
		boolean monsterHead;
		int duration;
		int pts = 0, number, color;
		boolean alive, hurt;
		float controlPos[] = {0,0};
		
		final int colors[] = {// TODO color auto with HSV
			0xffff0000,//rouge
			0xff0033ff,//bleu
			0xffff9900,//orange
			0xff00bb00,//vert
			0xffaa00aa,//violet
			0xffffff00,//jaune
			0xffff66ff,//rose
			0xff888888,//gris
		};
		final String colorsName[] = {
				"Red",
				"Blue",
				"Orange",
				"Green",
				"Purple",
				"Yellow",
				"Grey",
				"Pink"
		};
		
		Player(int num) {
			color = colors[num % colors.length];
			initRound();
			number = num;
		}
		void updatePos(float[] head) {
			if(map.mapType == Map.SQUARE) {
				if(head[0] < 0) head[0] += map.width;
				if(head[0] > map.width) head[0] -= map.width;
				if(head[1] < 0) head[1] += map.height;
				if(head[1] > map.height) head[1] -= map.height;
			}
			else if(map.mapType == Map.CIRCLE) {
				float dx = (head[0]-map.width/2), dy = (head[1]-map.height/2);// / (map.height/map.width); // correction en cas d'ovale
				final float d_2 = dx*dx+dy*dy;
				final float radius = Math.min(map.width, map.height)/2;
				if(d_2 > radius*radius) {
					/*float d = (float) Math.sqrt(d_2);
					float k = 2.0f * (radius / d) - 1.0f;
					head[0] = map.width * .5f - dx * k;
					head[1] = map.height * .5f - dy * k;*/
					head[0] -= head[4]*Math.cos(head[2]);
					head[1] -= head[4]*Math.sin(head[2]);
					dx = (head[0]-map.width/2); dy = (head[1]-map.height/2);
					float d = (float) Math.sqrt(dx*dx+dy*dy);
					float k = 2.0f * (radius / d) - 1.0f;
					head[0] = map.width * .5f - dx * k + (float)(head[4]*Math.cos(head[2]));
					head[1] = map.height * .5f - dy * k + (float)(head[4]*Math.sin(head[2]));
				}
			}
		}
		void step() {
			float[] head = snk.getFirst().clone();
			head[2] = getDirStep();
			head[3] = head[3]*0.5f + size*0.5f;
			head[4] = head[4]*0.5f + speed*0.5f;
			head[0] += head[4]*Math.cos(head[2]);
			head[1] += head[4]*Math.sin(head[2]);

			updatePos(head);
			
			snk.addFirst(head);
			
			while(snk.size() > length)
				snk.removeLast();

			if(duration == 0) {
				speed = defaultSpeed;
				size = defaultSize;
				monsterHead = false;
			}
			if(duration >= 0)
				duration--;
		}
		
		// retourne true si collision avec un autre serpent ou sa queue
		boolean collision() {
			float[] head = snk.getFirst();
			ArrayList<float[]> headEq = map.getAllPositions(head[0], head[1], head[3]);
			
			for(int i = 0; i < nbPlayers; i++)
				if(players[i].alive){
					int s = 0;
					
					for(float[] pos : players[i].snk) {
						ArrayList<float[]> eq = map.getAllPositions(pos[0], pos[1], pos[3]);
						if((i != number) || s >= 2)
						for(float[] h : headEq)// test de collision pour toutes les positions équivalentes de la tête
						for(float[] p : eq) {// test de collision pour toutes les positions équivalentes de la cible
							float dx=p[0]-h[0], dy=p[1]-h[1];
							float dist = pos[3]+head[3];
							if(dx*dx+dy*dy < dist*dist) {
								if(pos != players[i].snk.getFirst() && 
										monsterHead && (i != number)) {
									while(pos != players[i].snk.getLast()) {
										players[i].snk.removeLast();
										players[i].length--;
									}
									players[i].snk.removeLast();
									players[i].length--;
									break;
								}
								else
									return true;
							}
						}
						s++;
					}
				}
			
			for(int j = 0; j < map.apples.size(); j++) {
				Map.Item i = map.apples.get(j);
				float dist = (i.size+head[3]);
				for(float[] h : headEq) {// test de collision pour toutes les positions équivalentes de la tête
					float dx=i.pos[0]-h[0], dy=i.pos[1]-h[1];
					if(dx*dx+dy*dy < dist*dist) {
						length += i.length;
						if(nbPlayers == 1)
							pts++;
						map.destroyApple(j);
						j--;
					}
				}
			}
			
			for(int j = 0; j < map.items.size(); j++) {
				Map.Item i = map.items.get(j);
				float dist = (i.size+head[3]);
				for(float[] h : headEq) {// test de collision pour toutes les positions équivalentes de la tête
					float dx=i.pos[0]-h[0], dy=i.pos[1]-h[1];
					if(dx*dx+dy*dy < dist*dist) {
						switch(i.type) {
							case Map.Item.BIGGEST:
								speed = i.speed;
								size = i.sizeup;
								duration = i.duration;
								monsterHead = false;
								map.destroyItem(j);
								j--;
								break;
							case Map.Item.OBSTACLE:
								i.setRandomPos(i.obstacle, i.obstacle[2]);
								map.addDeath(i.obstacle);
								map.destroyItem(j);
								j--;
								break;
							case Map.Item.DEATH:
								if(i.start <= 0) {
									hurt = true;
									map.destroyDeath(j);
									j--;
									return true;
								}
								break;
							case Map.Item.MONSTER_HEAD:
								speed = defaultSpeed;
								size = defaultSize;
								duration = i.duration;
								monsterHead = true;
								map.destroyItem(j);
								j--;
								break;
						}
					}
				}
			}

			return false;
		}
		private void initPos() {
			float pos[]= {0,0};
			if(map.mapType == Map.SQUARE) {
				double sqrt = Math.sqrt(nbPlayers);
				int h = (int) Math.round(sqrt);
				int w = (int) Math.ceil(sqrt);
				pos = new float[] {
					(number%w+.5f)/w*map.width,
					(number/w+.5f)/h*map.height,
					dir = (random.nextInt(24) * (float)Math.PI/12),
					size,
					speed
				};
			}
			else if(map.mapType == Map.CIRCLE) {
				final double radiusInterMax = nbPlayers * (defaultSpeed*3 + defaultSize*2) / (2*Math.PI);
				final float distCenter = 0.75f;
				double delta = Math.asin(radiusInterMax/(distCenter*map.width/2));
				if(Double.isNaN(delta)) delta = 0;
				double alpha = 2*Math.PI * number / nbPlayers;
				pos = new float[] {
					((float) Math.cos(alpha) * distCenter + 1) * map.width * .5f,
					((float) Math.sin(alpha) * distCenter + 1) * map.height * .5f,
					dir = (float) (+alpha-Math.PI+delta),//(random.nextInt(24) * (float)Math.PI/12),
					size,
					speed
				};
			}
			snk.clear(); snk.addFirst(pos);
		}
		void initRound() {
			// réinitialisation des valeurs
			length = 4;
			monsterHead = false;
			speed = defaultSpeed;
			size=defaultSize;
			alive=true; hurt = false;
			duration = -1;
			// placement initial en fonction du nombre de joueurs
			initPos();
			if(nbPlayers == 1)
				pts = 0;
		}
		float getDirStep() {
			float last = snk.getFirst()[2];
			float bigdelta = dir-last;
			while(bigdelta > Math.PI) bigdelta-=2*Math.PI;
			while(bigdelta <-Math.PI) bigdelta+=2*Math.PI;

			final float deltamax = (float) Math.min(Math.PI/3, (Math.PI - 2.1*Math.asin(size/speed)));//Math.PI/3;
			if(bigdelta > deltamax)
				bigdelta = deltamax;
			else if(bigdelta < -deltamax)
				bigdelta = -deltamax;

			return last+bigdelta;
		}
		void setDir(float d) {
			dir = d;
		}
	}
}

