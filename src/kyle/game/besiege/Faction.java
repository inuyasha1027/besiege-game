/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

import kyle.game.besiege.army.Noble;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.voronoi.Center;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;

public class Faction {
	public static final int INCREASE_INTERVAL = 10; // every these seconds, relations will increase btw these factions
	public static final int CLOSE_CITY_FACTOR = 10; // this times num of close cities of one faction will decrease the relation with that faction
	public static Kingdom kingdom;
	private final int CHECK_FREQ = 5; // how frequently this manages stuff
	private final float ORDER_FACTOR = .9f; // what percent of nobles will be ordered to besiege a city
	public int index; // for keeping track of relations
	public String name; 
	public TextureRegion crest;
	public Color color;
	public Array<City> cities;
	public Array<City> closeEnemyCities;
	public Array<City> closeFriendlyCities;
	public Array<Noble> nobles;
	public Array<Noble> unoccupiedNobles; // nobles that aren't ordered to besiege any cities
	public Array<Location> locationsToAttack; //  and sieges these nobles are currently maintaining
	public Array<Center> centers; // centers under influence of this faction
	public Array<Polygon> territory; // polygon of all centers
	
	private double timeSinceIncrease;  // can make more efficient by moving this to Kingdom
	private boolean hasChecked;
	
	private final int NOBLE_COUNT = 5; //TODO
	
	public static final int MAX_RELATION = 100;
	public static final int MIN_RELATION = -100;
	public static final int INIT_WAR_RELATION = -40; //when war is declared, this is the relation you will have
	private final static int WAR_THRESHOLD = -10; //cross this and you're at war
	public static Array<Faction> factions;
	public static final Faction BANDITS_FACTION = new Faction("Bandits", "crestBandits", Color.BLACK);
	public static final Faction PLAYER_FACTION = new Faction("Rogue", "crestBlank", Color.WHITE);
	private  static int factionCount;
	
	private static Array<Array<Integer>> factionRelations;
//	private static Array<Array<Integer>> factionMilitaryAction; // is this worth it?
//	private static Array<Array<Integer>> factionNearbyCities; // not needed, calced in real time?
//	private static Array<Array<Integer>> factionTrade;
	
	public Faction(String name, String textureRegion, Color color) {
		this.name = name;
		crest = Assets.atlas.findRegion(textureRegion);
		this.color = color;
		
		nobles = new Array<Noble>();
		unoccupiedNobles = new Array<Noble>();
		cities = new Array<City>();
		centers = new Array<Center>();
		territory = new Array<Polygon>();
		closeEnemyCities = new Array<City>();
		closeFriendlyCities = new Array<City>();
		locationsToAttack = new Array<Location>();
		timeSinceIncrease = 0;
	}
	
	public static void initializeFactions(Kingdom kingdom) {
		factions = new Array<Faction>();
		
		factionRelations = new Array<Array<Integer>>();
//		factionMilitaryAction = new Array<Array<Integer>>();

		// add player faction (index 0) 
		createFaction(PLAYER_FACTION);
	
		// add bandits faction (index 1)
		createFaction(BANDITS_FACTION);	

		createFaction("Halmera", "crestBlueRose", Color.BLUE);
		createFaction("Geinever", "crestWhiteLion", Color.RED);
		createFaction("Weyvel", "crestGreenTree", Color.GREEN);
		createFaction("Rolade", "crestOrangeCross", Color.YELLOW);
		createFaction("Selven", "crestGreenStripe", Color.MAGENTA);
		createFaction("Myrnfar", "crestYellowStar", Color.CYAN);
		createFaction("Corson", "crestRedCross", Color.RED);
		
		createFaction("Fernel", "crestBlank", Color.LIGHT_GRAY);
		createFaction("Draekal", "crestBlank", Color.MAGENTA);
		
		for (Faction f : factions) {
			f.kingdom = kingdom;
		}
		
		factions.get(2).declareWar(factions.get(3));
		
//		factionRelations = new int[factionCount][factionCount];
		for (int i = 0; i < factionCount; i++) {
			for (int j = 0; j < factionCount; j++) {
//				factionRelations[i][j] = -30;
				factionRelations.get(i).set(j, -30);
				factionRelations.get(j).set(i, -30);
			}
		}
		for (int i = 0; i < factionCount; i++) {
//			factionRelations[i][i] = 100;
			factionRelations.get(i).set(i, 100);
		}
	}
	
	public static void factionAct(float delta) {
		factions.shrink();
		for (int i = 0; i < factions.size; i++)
			factions.get(i).act(delta);
	}
	public static void createFaction(String name, String textureRegion, Color color) {
		Faction faction = new Faction(name, textureRegion, color);
		factions.add(faction);
		faction.index = factions.indexOf(faction, true);
		for (int i = 0; i < factions.size; i++) {
//			factionRelations[faction.index][i] = 0; // resets faction relations
//			factionRelations[i][faction.index] = 0;
			if (factionRelations.size <= faction.index)
				factionRelations.add(new Array<Integer>());
			
			if (factionRelations.get(i).size <= faction.index)
				factionRelations.get(i).add(0);
			else factionRelations.get(i).set(faction.index, 0);
			
			if (factionRelations.get(faction.index).size <= i)
				factionRelations.get(faction.index).add(0);
			else factionRelations.get(faction.index).set(i, 0);
		}
		if (faction.index >= 1) {
			faction.declareWar(BANDITS_FACTION);
		}
	}
	public static void createFaction(Faction faction) {
		factions.add(faction);
		faction.index = factions.indexOf(faction, true);
		for (int i = 0; i < factions.size; i++) {
//			factionRelations[faction.index][i] = 0; // resets faction relations
//			factionRelations[i][faction.index] = 0;
			if (factionRelations.size <= faction.index)
				factionRelations.add(new Array<Integer>());
			
			if (factionRelations.get(i).size <= faction.index)
				factionRelations.get(i).add(0);
			else factionRelations.get(i).set(faction.index, 0);
			
			if (factionRelations.get(faction.index).size <= i)
				factionRelations.get(faction.index).add(0);
			else factionRelations.get(faction.index).set(i, 0);
		}
		if (faction.index >= 1) {
			faction.declareWar(BANDITS_FACTION);
		}
	}
	
	public void removeFaction(Faction faction) {
		factions.removeValue(faction, true);
		for (int i = 0; i < factions.size; i++) {
//			factionRelations[faction.index][i] = -999; // 'deletes' faction relations
//			factionRelations[i][faction.index] = -999;
			factionRelations.get(i).set(faction.index, null);
			factionRelations.get(faction.index).set(i, null);
		}
	}
	
	public static void updateFactionCityInfo() {
		System.out.println("updating faction city info");
		for (Faction f : factions) { 
			f.updateCloseHostileCities();
			f.centers.clear();
		}
		// update each faction's centers
		for (Center c : kingdom.getMap().connected) calcInfluence(c);
		kingdom.getMap().calcBorderEdges();
		for (Faction f : factions) {
			f.territory.clear();
			Array<Array<Center>> aaCenters = Map.calcConnectedCenters(f.centers);				
			for (Array<Center> centers : aaCenters) {
//				System.out.println("working");
				f.territory.add(Map.centersToPolygon(centers));
			}
		}
		for (Village v : kingdom.villages) {
			v.setFaction(getInfluenceAt(v.center));
		}
	}
	
	/**
	 * figures out which faction has the most influence on this center,
	 * and adds it to that faction's "centers" array
	 * @param center
	 */
	private static void calcInfluence(Center center) {
		Point centerPoint = new Point(center.loc.x, Map.HEIGHT - center.loc.y);
		Faction bestFaction = null;
		double bestScore = 0;
		
		// go through factions and calc influence, saving it if it's the highest
		for (Faction faction : factions) {
			double score = 0; // score is inverse of city distance
			for (City city : faction.cities) {
				double dist = Kingdom.distBetween(city, centerPoint);
				score += 1/dist;
			}
			if (score > bestScore) {
				bestScore = score;
				bestFaction = faction;
			}
		}
				
		if (bestFaction != null) {
			bestFaction.centers.add(center);
			center.faction = bestFaction;
		}
	}
	
	public static Faction getInfluenceAt(Center center) {
		for (Faction f : factions) {
			if (f.centers.contains(center, true)) return f;
		}
		
		System.out.println("no one controls that center");
		return null;

	}
	
	public void act(float delta) {
		timeSinceIncrease += delta;
		
//		if (this == PLAYER_FACTION)
//			System.out.println(this.name + " " + BANDITS_FACTION.name + getRelations(this, BANDITS_FACTION));

		if (timeSinceIncrease >= INCREASE_INTERVAL) {
//			System.out.println(timeSinceIncrease);
			for (Faction f : factions)
				changeRelation(this, f, 0); // factor to increase relations by
			timeSinceIncrease = 0;
		}
		
		if (this != PLAYER_FACTION) autoManage(delta);
	}
	
	public void autoManage(float delta) {
		// send armies to capture/raid enemy cities/castles/villages
		// negotiate diplomacy, declare war/peace
		// that's it for now :D
		if (Kingdom.getTotalHour() % CHECK_FREQ == 0 && !hasChecked) {
			manageNobles();
			hasChecked = true;
		}
		else if (Kingdom.getTotalHour() % CHECK_FREQ != 0) hasChecked = false;
	}
	public void manageNobles() {
		while (nobles.size < NOBLE_COUNT && cities.size >= 1) {
			createNobleAt(cities.random());
		}
		manageSieges();
		
		// figure out whether or not to organize a siege or something!
	}
	public void manageSieges() {
		if (locationsToAttack.size < 1 && unoccupiedNobles.size > 1 && closeEnemyCities.size > 1) {
			Location randomLocation = closeEnemyCities.random();
			orderSiegeOf(randomLocation);
		}
//		if (nobles.size > 1 && closeEnemyCities.size > 1) {
//			Noble random = nobles.random();
//			if (!random.hasSpecialTarget()) {
//				Location randomLoc = closeEnemyCities.random();
//				random.setSpecialTarget(randomLoc);
//				System.out.println("giving " + random.getName() + " special target " + randomLoc.getName());
//			}
//		}
	}
	public void orderSiegeOf(Location location) {
		locationsToAttack.add(location);
		int noblesToOrder = Math.max((int) (unoccupiedNobles.size * ORDER_FACTOR), 1);
		System.out.println(this.name + " is ordering a siege of " + location.getName() + " involving " + noblesToOrder + " nobles");
		while (noblesToOrder > 0) {
			Noble randomNoble = unoccupiedNobles.random();
			setTask(randomNoble, location);
			noblesToOrder--;
		}
			
	}
	public void setTask(Noble noble, Location location) {
		noble.setSpecialTarget(location);
		this.unoccupiedNobles.removeValue(noble, true);
	}
	public void endTask(Noble noble) {
		noble.setSpecialTarget(null);
		this.unoccupiedNobles.add(noble);
	}
	public void createNobleAt(Location location) {
		Noble noble = new Noble(location.getKingdom(), location);
		// randomize size
		this.addNoble(noble);
		noble.goToNewTarget();
		location.setContainerForArmy(noble);
	}
	public void addNoble(Noble noble) {
		this.nobles.add(noble);
		this.unoccupiedNobles.add(noble);
	}
	public void removeNoble(Noble noble) {
		this.nobles.removeValue(noble, true);
		if (unoccupiedNobles.contains(noble, true)) unoccupiedNobles.removeValue(noble, true);
	}
	
	public void updateCloseHostileCities() {
		Array<City> tempCloseEnemyCities = new Array<City>();
		Array<City> tempCloseFriendlyCities = new Array<City>();
		//System.out.println(this.name + ":");
		for (City c: cities) {
			c.updateClosestCities();
			for (City hostile : c.getClosestHostileCities()) {
//				if (!closeEnemyCities.contains(hostile, true)) {
					tempCloseEnemyCities.add(hostile);
					//this makes factions dislike factions with nearby cities
					if (!closeEnemyCities.contains(hostile, true)) {
						if (hostile.getFaction() == this) System.out.println("hostile: " + hostile.getName());
//						System.out.println("new hostile city: " + hostile.getName());
						changeRelation(this, hostile.getFaction(), -1*CLOSE_CITY_FACTOR);
					}	
//				}
			}
			for (City friendly : c.getClosestFriendlyCities()) {
				if (friendly.getFaction() != this) {
					tempCloseFriendlyCities.add(friendly);
					//this makes factions dislike factions with nearby cities
					if (!closeFriendlyCities.contains(friendly, true) && friendly.getFaction().index != this.index) {
//						System.out.println("new friendly city: " + friendly.getName());
						changeRelation(this, friendly.getFaction(), -1*CLOSE_CITY_FACTOR);
					}	
				}
			}
		}
		closeEnemyCities = new Array<City>(tempCloseEnemyCities);
		closeFriendlyCities = new Array<City>(tempCloseFriendlyCities);
	}

	// for panels displaying info about this I guess
	public int getCloseCityEffect(Faction that) {
		if (this.index == that.index) return 0;
		int totalEffect = 0;
		if (isAtWar(this, that)) {
			for (City c : closeEnemyCities)
				if (c.getFaction() == that)	totalEffect += -1*CLOSE_CITY_FACTOR;
		}
		else {
			for (City c : closeFriendlyCities)
				if (c.getFaction() == that) totalEffect += -1*CLOSE_CITY_FACTOR;
		}
		return totalEffect;
	}
	
	public int getRelationsWith(Faction that) {
		return getRelations(this, that);
	}
	
	public void declareWar(Faction that) {
		if (!Faction.isAtWar(this,  that))
			declareWar(this, that);
	}
	
	public void goRogue() { // just for testing, declares war on all factions other than this one
		for (int i = 0; i < factions.size; i++)
			if (i != index) declareWar(this, factions.get(i));
	}
	
	public int getTotalWealth() {
		int total = 0;
		for (City c : cities)
			total += c.getParty().wealth;
		return total;
	}
	
	
	public static int getRelations(Faction faction1, Faction faction2) {
		return factionRelations.get(faction1.index).get(faction2.index);
	}
	public static boolean isAtWar(Faction faction1, Faction faction2) {
		return (getRelations(faction1, faction2) < WAR_THRESHOLD);
	}
	public static void setAtWar(Faction faction1, Faction faction2) {
//		factionRelations[faction1][faction2] = WAR_THRESHOLD-1;
//		factionRelations[faction2][faction1] = WAR_THRESHOLD-1;
		factionRelations.get(faction1.index).set(faction2.index, INIT_WAR_RELATION);
		factionRelations.get(faction2.index).set(faction1.index, INIT_WAR_RELATION);
	}
	public static void setNeutral(Faction faction1, Faction faction2) {
		factionRelations.get(faction1.index).set(faction2.index, 0);
		factionRelations.get(faction2.index).set(faction1.index, 0);

//		factionRelations[faction1][faction2] = 0;
//		factionRelations[faction2][faction1] = 0;
	}
	
	public static void makePeace(Faction faction1, Faction faction2) {
//		BottomPanel.log(faction1.name + " and " + faction2.name + " have signed a peace agreement!", "magenta");
		setNeutral(faction1, faction2);
	}
	public static void declareWar(Faction faction1, Faction faction2) {
//		BottomPanel.log(faction1.name + " and " + faction2.name + " have declared war!", "magenta");
		setAtWar(faction1, faction2);
	}

	public static void changeRelation(Faction faction1, Faction faction2, int delta) {
		int initialRelation = factionRelations.get(faction1.index).get(faction2.index);
		int newRelation;
		if (initialRelation + delta >= MAX_RELATION) newRelation = MAX_RELATION;
		else if (initialRelation + delta <= MIN_RELATION) newRelation = MIN_RELATION;
		else newRelation = initialRelation + delta;
		if (initialRelation >= WAR_THRESHOLD && newRelation < WAR_THRESHOLD) ;
//			BottomPanel.log(faction1.name + " and " + faction2.name + " have declared war!", "magenta");
		else if (initialRelation < WAR_THRESHOLD && newRelation >= WAR_THRESHOLD) 
			makePeace(faction1, faction2);
		factionRelations.get(faction1.index).set(faction2.index, newRelation);
	}
	
//	public static void calcAllRelations() {
//		for (Faction f : factions) { 
//			for (int i = 0; i < factions.size; i++) {
//				int base = 0;
////				base += factionMilitaryAction.get(f.index).get(i); // Military actions
//				base += f.getCloseCityEffect(factions.get(i));	   // Borders
//				base += factions.get(i).getCloseCityEffect(f);	   // (Borders is 2-way)
//				factionRelations.get(i).set(f.index, base); 	   // can make more efficient
//				factionRelations.get(f.index).set(i, base);
//			}
//		}
//	}
	
	public static Faction get(int index) {
		return factions.get(index);
	}

	public City getRandomCity() {
		if (cities.size > 0) {
			return cities.random();
		}
		else return null;
	}
}
