/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
// group of actors, contains bg, all cities and all armies
package kyle.game.besiege;


import java.util.Scanner;

import kyle.game.besiege.army.Army;
import kyle.game.besiege.army.ArmyPlayer;
import kyle.game.besiege.army.Bandit;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.panels.Panel;
import kyle.game.besiege.voronoi.Center;
import kyle.game.besiege.voronoi.Corner;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;

public class Kingdom extends Group {
	public static BitmapFont arial;
	public static final double DECAY = .1;
	public static final float HOUR_TIME = 2.5f;
	public static final int CITY_START_WEALTH = 100;
	public static final int VILLAGE_START_WEALTH = 10;
	public static final int BANDIT_FREQ = 1000;
	public static final int MAX_BANDITS = 5;

	private final float LIGHT_ADJUST_SPEED = .005f; //adjust this every frame when changing daylight
	private final float NIGHT_FLOAT = .6f;
	private final float MOUSE_DISTANCE = 10; // distance destination must be from mouse to register
	private final int DAWN = 7;
	private final int DUSK = 21;

	public static float clock;
	private int timeOfDay; // 24 hour day is 60 seconds, each hour is 2.5 seconds
	private int day;
	private boolean night; // is nighttime?
	public float currentDarkness; // can be used for LOS
	private float targetDarkness; // for fading

	private Map map;
	private PolygonSpriteBatch psb;
	private MapScreen mapScreen;
	private Array<Army> armies;
	public Array<City> cities;
	public Array<Village> villages;
	private Array<Battle> battles;
	private ArmyPlayer player;

	public int banditCount;

	private Destination currentPanel;

	private boolean mouseOver; // is mouse over Kingdom screen?
	private boolean paused;

	private boolean leftClicked;
	private boolean rightClicked;
	private Point mouse;

	private ShapeRenderer sr = new ShapeRenderer();

	public Kingdom(MapScreen mapScreen) {
		//		double time = System.nanoTime();
		//		System.out.println("starting map");
		map = new Map();

		//		System.out.println("Map took: " + (System.nanoTime() - time)/1000000000.0);

		clock = 0; // set initial clock
		timeOfDay = 0;
		day = 0;
		//		currentDarkness = NIGHT_FLOAT;
		currentDarkness = 0; // fade in

		this.mapScreen = mapScreen;

		//		backgroundMap = new BackgroundMap();
		//		addActor(backgroundMap);
		addActor(map);

		armies = new Array<Army>();
		cities = new Array<City>();
		villages = new Array<Village>();
		battles = new Array<Battle>();

		Faction.initializeFactions(this);

		// initialize all cities
		initializeCities();
		// initialize all villages
		initializeVillages();
		//initializeCastles();
		// initialize all castles
		Faction.updateFactionCityInfo();

		//		for (Faction f: factions) {
		//			System.out.println(f.name + " enemy cities: ");
		//			for (City enemy : f.closeEnemyCities)
		//				System.out.println("   " + enemy.getName() + " (" + enemy.getFaction().name + "), rel with them is " + this.getRelations(f, enemy.getFaction()));
		//			System.out.println("friendly cities: ");
		//			for (City friend : f.closeFriendlyCities)
		//				System.out.println("   " + friend.getName() + " (" + friend.getFaction().name + "), rel with them is " + this.getRelations(f, friend.getFaction()));
		//		}


		for (int i = 0; i < cities.size; i++)
			cities.get(i).updateClosestCities();

		// initialize all armies

		//		Army test2 = new Army(this, "Test2", 2, 40, 40, 4, 100);
		//		addArmy(test2);
		//		test2.setTarget(cities.first());
		//		test2.waitFor(1);

		arial = new BitmapFont();
		mouse = new Point(0,0);
		banditCount = 0;
		currentPanel = new Point(0,0);
	}

	@Override
	public void act(float delta) {
		if (mouseOver) {
			if (leftClicked) leftClick(mouse);
			else if (rightClicked) rightClick(mouse);
			else if (BesiegeMain.appType != 1) mouseOver(mouse);
		}

		if (!paused) {
			time(delta);
			super.act(delta);

			//create bandits
			if (banditCount <= MAX_BANDITS && cities.size != 0) {
				if (Math.random() < 1.0/BANDIT_FREQ) {
					City originCity = cities.random();
					//					if (originCity.getVillages().size == 0) 
					createBandit(originCity);
				}
			}
			Faction.factionAct(delta);
			//			clearContainingPolygons();
		}
		if (leftClicked) leftClicked = false;
		if (rightClicked) rightClicked = false;
	}

	// used when traveling
	// randomly gets really slow
	// just check adjacent centers!
	public void updateArmyPolygon(Army army) {
		if (army.containing != null) {

			// THIS IS THE SLOW PART FUCK
			// first check if it's left it's previous polygon
			// if not, return, if so, remove from previous container
			if (army.containing.polygon != null && army.containing.polygon.contains(army.getCenterX(), army.getCenterY())) {
				if (!army.containing.armies.contains(army, true)) army.containing.armies.add(army);
				//				System.out.println("same polygon");
				return;
			}

			army.containing.armies.removeValue(army, true);

			for (Center adjacent : army.containing.neighbors) {
				// checks if in connected (all connected have polygon)
				if (adjacent.polygon != null) {
					Polygon p = adjacent.polygon;
					//					System.out.println("updating polygon for " + army.getName());
					if (p.contains(army.getCenterX(), army.getCenterY())) {
						//						System.out.println("*************************found containing " + army.getName());
						//						System.out.println("adjacent polygon");
						adjacent.armies.add(army);
						army.containing = adjacent;
						return;
					}
				}
			}
			army.containing = null;
			//			System.out.println("nothing adjacent found for " + army.getName());
		}
		// container is null (slow during initialization stages - plan is to initialize armies with accurate center)
		for (Center center : map.connected) {
			Polygon p = center.polygon;
			if (p.contains(army.getCenterX(), army.getCenterY())) {
				center.armies.add(army);
				army.containing = center;
				//						System.out.println("completely differnt polygon");
				return;
			}
		}
		//				System.out.println("not found at all");
	}

	public void time(float delta) {
		clock += delta;
		timeOfDay = (int) ((clock - day*60) / HOUR_TIME);
		if (timeOfDay == 24) {
			day++;
		}
		if (timeOfDay >= DAWN && timeOfDay <= DUSK)
			night = false;
		if (timeOfDay <= DAWN || timeOfDay >= DUSK)
			night = true;
	}

	private void mouseOver(Point mouse) {
		Destination d = getDestAt(mouse);
		//		if (d.getType() != 0)
		this.setPanelTo(d);
		//			d.setMouseOver(true);
	}

	private void setPanelTo(Destination newPanel) {
		//		if (currentPanel == null) System.out.println("currentPanel is null");
		// makes sure not to set the same panel a lot, and makes sure not to return to previous for every single point
		if (newPanel != currentPanel && (newPanel.getType() != 0 || currentPanel.getType() != 0)) {
			mapScreen.getSidePanel().setActiveDestination(newPanel);
			currentPanel = newPanel;
		}
	}

	private void leftClick(Point mouse) {
		Destination d = getDestAt(mouse);
		if (d != player && !player.isInBattle()) {
			paused = false;
			player.setTarget(d);
			mapScreen.shouldCenter = true;
			//if (player.getTarget() != null) System.out.println("target = " + player.getTarget().getName());
		}
	}
	private void rightClick(Point mouse) {
		Destination d = getDestAt(mouse);
		if (d.getType() == 1) {
			Location location = (Location) d;
			mapScreen.getSidePanel().setActiveLocation(location);
		}
		if (d.getType() == 2) { // army
			Army destinationArmy = (Army) d;
			mapScreen.getSidePanel().setActiveArmy(destinationArmy);
		}
		if (d.getType() == 4) { //battle
			Battle battle = (Battle) d;
			mapScreen.getSidePanel().setActiveBattle(battle);
		}
	}

	public void click(int pointer) {
		if (pointer == 0)
			leftClicked = true;
		else if (pointer == 1) 
			rightClicked = true;
		else if (pointer == 4)
			writeCity();
	}
	public void writeCity() {
		float x = mouse.getCenterX();
		float y = mouse.getCenterY();
		//		mapScreen.out.println(x + " " + y);
	}

	private Destination getDestAt(Point mouse) {
		Destination dest = new Point(mouse.getCenterX(), mouse.getCenterY());
		for (City city : cities) {
			if (Kingdom.distBetween(city, mouse) <= MOUSE_DISTANCE)
				dest = city;
		}
		for (Village village : villages) {
			if (Kingdom.distBetween(village, mouse) <= MOUSE_DISTANCE)
				dest = village;
		}
		for (Army army : armies) {
			if (army.isVisible() && Kingdom.distBetween(army, mouse) <= MOUSE_DISTANCE)
				if (mapScreen.losOn) {
					if (Kingdom.distBetween(army, player) <= player.getLineOfSight())
						dest = army;
				}
				else
					dest = army;
		}
		for (Battle battle : battles) {
			if (Kingdom.distBetween(battle, mouse) <= MOUSE_DISTANCE) {
				dest = battle;
			}
		}
		return dest;
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		//batch.setColor(Color.WHITE);
		if (night) targetDarkness = NIGHT_FLOAT;
		else targetDarkness = 1f;
		if (currentDarkness != targetDarkness) adjustDarkness();
		batch.setColor(currentDarkness, currentDarkness, currentDarkness, 1f);

		map.draw(batch, parentAlpha);

		super.draw(batch, parentAlpha);
		arial.setColor(Color.WHITE);
		arial.setScale(2*(mapScreen.getCamera().zoom));
	}

	public void addCity(City newCity) {
		cities.add(newCity);
		addActor(newCity);
	}

	public void initializeCities() {	
		Array<String> cityArray = Assets.cityArray;
		//		Scanner scanner = Assets.cityList;
		int currentFaction = 2; // no bandits or player 
		int factionRepeats = 0;
		int maxRepeats = (int) (Math.random()*3) + 0; // max 3, min 0
		//		while (scanner.hasNextLine() && (map.cityCorners.size > 0 && map.cityCenters.size > 0)) {
		while (cityArray.size > 0 && (map.cityCorners.size > 0 && map.cityCenters.size > 0)) {
			//			System.out.println("adding city");
			City city;
			int x, y;
			boolean useCorner = true; // later allow to use center
			if (Math.random() > .5)
				useCorner = false; // later allow to use center
			// make with corner
			if (useCorner) {
				Corner corner;
				corner = map.cityCorners.random();
				x = (int) corner.getLoc().x;
				y = (int) corner.getLoc().y;
				map.cityCorners.removeValue(corner, true);
				map.availableCorners.removeValue(corner, true);

				// remove neighboring corners 2 levels deep!
				for (Corner adj : corner.adjacent) {
					if (adj != null) {
						map.cityCorners.removeValue(adj, true);
						map.availableCorners.removeValue(adj, true);
						for (int i = 0; i < adj.adjacent.size(); i++) {
							// two levels deep
							if (adj.adjacent.get(i) != null) 
								map.cityCorners.removeValue(adj.adjacent.get(i), true);
						}
					}
				}
				// remove neighboring centers
				for (Center adj : corner.touches) {
					if (adj != null) {
						map.cityCenters.removeValue(adj, true);
						map.availableCenters.removeValue(adj, true);
					}
				}
				int index = -1;
				//				city = new City(this, scanner.next(), index, Faction.get(currentFaction), x, Map.HEIGHT-y, CITY_START_WEALTH);
				city = new City(this, cityArray.pop(), index, Faction.get(currentFaction), x, Map.HEIGHT-y, CITY_START_WEALTH);
				city.setCorner(corner);
			}
			// make with center
			else {
				Center center;
				center = map.cityCenters.random();
				x = (int) center.loc.x;
				y = (int) center.loc.y;
				map.cityCenters.removeValue(center, true);
				map.availableCenters.removeValue(center, true);

				for (Corner adj : center.corners) {
					if (adj != null) {
						map.cityCorners.removeValue(adj, true);
						//						map.availableLocationSites.removeValue(adj.loc, false);
						for (int i = 0; i < adj.adjacent.size(); i++) {
							// two levels deep
							if (adj.adjacent.get(i) != null)  {
								map.cityCorners.removeValue(adj.adjacent.get(i), true);
								//								map.availableLocationSites.removeValue(adj.adjacent.get(i).loc, false);
							}
						}
					}
				}
				for (Center adjCenter : center.neighbors) {
					if (adjCenter != null) {
						map.cityCenters.removeValue(adjCenter, true);
						//						map.availableLocationSites.removeValue(adjCenter.loc, false);
					}
				}
				int index = -1;
				//				city = new City(this, scanner.next(), index, Faction.get(currentFaction), x, Map.HEIGHT-y, CITY_START_WEALTH);
				city = new City(this, cityArray.pop(), index, Faction.get(currentFaction), x, Map.HEIGHT-y, CITY_START_WEALTH);
				city.setCenter(center);
			}

			addCity(city);
			factionRepeats++;
			if (factionRepeats > maxRepeats) {
				currentFaction++;
				factionRepeats = 0;
				maxRepeats = (int) (Math.random()*5) + 5;
			}
		}
		System.out.println("Number cities: " + cities.size);
	}

	// villages only spawn at centers (for influence purposes)
	public void initializeVillages() {
		//		Scanner scanner = Assets.villageList;
		Array<String> villageArray = Assets.villageArray;

		//		while (scanner.hasNextLine() && map.availableCenters.size > 0) {
		while (villageArray.size > 0 && map.availableCenters.size > 0) {
			Center center = map.availableCenters.random();

			//			Village village = new Village(this, scanner.next(), -1, null, (float) center.loc.x, (float) (Map.HEIGHT-center.loc.y), VILLAGE_START_WEALTH);			
			Village village = new Village(this, villageArray.pop(), -1, null, (float) center.loc.x, (float) (Map.HEIGHT-center.loc.y), VILLAGE_START_WEALTH);			
			villages.add(village);
			village.center = center;
			addActor(village);
		}
		System.out.println("Number villages: " + villages.size);
	}

	public void addArmy(Army add) {
		armies.add(add);
		addActor(add);
	}
	public void addPlayer() {
		player = new ArmyPlayer(this, mapScreen.getCharacter(), Faction.PLAYER_FACTION, (int) map.reference.loc.x, (int) (Map.HEIGHT-map.reference.loc.y), 6);
		player.getParty().player = true;
		player.containing = map.reference;
		addArmy(player);
		//		player.initializeBox(); // otherwise line of sight will be 0!
		//mapScreen.center(); doesn't do anything bc of auto resize
		//player.getParty().wound(player.getParty().getHealthy().random());
		//		mapScreen.getFog().updateFog();
	}
	public void createBandit(City originCity) {
		//get a good bandit location, out of player's LOS, away from other armies, etc.
		Bandit bandit = new Bandit(this, "Bandit", 0, 0);
		float posX, posY;
		//		float posX = 2048;
		//		float posY = 2048;
		Point p;
		int count = 0;
		do {
			count++;
			posX = (float) (originCity.getCenterX() + (Math.random()+0.5)*bandit.getLineOfSight());
			posY = (float) (originCity.getCenterY() + (Math.random()+0.5)*bandit.getLineOfSight());
			p = new Point(posX, posY); 
			//			System.out.println("creating bandit spot");
		} while ((map.isInWater(p) || Kingdom.distBetween(p, player) <= player.getLineOfSight()) && count < 10); // makes sure bandit is out of sight of player!
		if (count == 10) return;
		bandit.setDefaultTarget(cities.random());
		bandit.setPosition(posX, posY);
		//		System.out.println("new bandit created at " + origin.getName() + posX + "  " + posY);
		this.addArmy(bandit);
		banditCount++;
	}
	public void removeArmy(Army remove) {
		if (remove.getParty().player) System.out.println("kingdom removing player");
		armies.removeValue(remove, true);
		if (remove.containing != null)
			remove.containing.armies.removeValue(remove, true);
	}

	public Array<Army> getArmies() {
		return armies;
	}
	public void addBattle(Battle battle) {
		battles.add(battle);
	}
	public void removeBattle(Battle battle) {
		battles.removeValue(battle, true);
	}
	public Array<City> getCities() {
		return cities;
	}
	public ArmyPlayer getPlayer() {
		return player;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public float getMouseX() {
		return mouse.getX();
	}
	public float getMouseY() {
		return mouse.getY();
	}
	public void setMouse(Vector2 mousePos) {
		mouse.setPos(mousePos.x, mousePos.y);
	}
	public MapScreen getMapScreen() {
		return mapScreen;
	}

	public float clock() {
		return clock;
	}
	public void toggleNight() {
		night = !night;
	}
	//	public void setToNight(SpriteBatch b) {
	//		b.setColor(NIGHT_FLOAT, NIGHT_FLOAT, NIGHT_FLOAT, 1f);
	//	}
	//	public void setToDay(SpriteBatch b) {
	//		b.setColor(1f, 1f, 1f, 1f);
	//	}
	private void adjustDarkness() {
		if (targetDarkness - currentDarkness > LIGHT_ADJUST_SPEED) currentDarkness += LIGHT_ADJUST_SPEED;
		else if (currentDarkness - targetDarkness > LIGHT_ADJUST_SPEED) currentDarkness -= LIGHT_ADJUST_SPEED;
	}
	public int getTime() {
		return timeOfDay;
	}
	public static int getTotalHour() {
		return (int) (clock/HOUR_TIME);
	}
	public int getDay() {
		return day;
	}
	public float getZoom() {
		return getMapScreen().getCamera().zoom;
	}
	public void setMouseOver(boolean b) {
		mouseOver = b;
	}
	public boolean isPaused() {
		return paused;
	}
	public Map getMap() {
		return map;
	}
	public static double distBetween(Destination d1, Destination d2) {
		// TODO optimize by computing getCenter only once per
		return Math.sqrt((d1.getCenterX()-d2.getCenterX())*(d1.getCenterX()-d2.getCenterX())+(d1.getCenterY()-d2.getCenterY())*(d1.getCenterY()-d2.getCenterY()));
	}
	// should be slightly faster than above
	public static double sqDistBetween(Destination d1, Destination d2) {
		return (d1.getCenterX()-d2.getCenterX())*(d1.getCenterX()-d2.getCenterX())+(d1.getCenterY()-d2.getCenterY())*(d1.getCenterY()-d2.getCenterY());
	}
}
