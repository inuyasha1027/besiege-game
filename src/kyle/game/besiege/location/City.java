/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.location;

import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.Map;
import kyle.game.besiege.Point;
import kyle.game.besiege.army.Merchant;
import kyle.game.besiege.army.Patrol;
import kyle.game.besiege.army.RaidingParty;
import kyle.game.besiege.geom.PointH;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.voronoi.Center;
import kyle.game.besiege.voronoi.Corner;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class City extends Location {
	private final float SCALE = 10;
	private final int CITY_UPPER_VALUE = 40;
	private final float closeCityDistance = 500; // cities within this distance are considered "close" for trading, raiding, etc
	private final float villageRangeMax = 80;
	private final float villageRangeMin = 10;
	private final float villageSeparation = 60;
	private final double MERCHANT_GAIN = .008;
	private final int patrolCost;
	private final int raiderCost;
	private final int merchantCost;
	
	private Array<City> closestFriendlyCities;
	private Array<City> closestEnemyCities;
	
	private Array<Patrol> patrols;
	private Array<Merchant> merchants;
	private boolean[] merchantExists;
	private Array<RaidingParty> raiders;
	private boolean[] raiderExists;
	
	private Corner corner;
	private Center center;
//	private Array<Village> villages;
//	private Array<PointH> villageSpots;

	public City(Kingdom kingdom, String name, int index, Faction faction, float posX,
			float posY, int wealth) {
		super(kingdom, name, index, faction, posX, posY, PartyType.CITY_GARR_1.generate());
				
		getParty().wealth = wealth;
		
		this.getFaction().cities.add(this);
		
		patrols = new Array<Patrol>();
		
		merchants = new Array<Merchant>();
		merchantExists = new boolean[CITY_UPPER_VALUE];
		
		raiders = new Array<RaidingParty>();
		raiderExists = new boolean[CITY_UPPER_VALUE];

		closestFriendlyCities = new Array<City>();
		closestEnemyCities = new Array<City>();
//		villages = new Array<Village>();
		
		this.merchantCost = PartyType.MERCHANT.maxWealth;
		this.patrolCost = PartyType.PATROL.maxWealth;
		this.raiderCost = PartyType.RAIDING_PARTY.maxWealth;
		
		setTextureRegion("Castle");
		setScale(SCALE);
		initializeBox();
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
//		Assets.pixel18.setColor(Kingdom.factionColors.get(getFaction()));
//		String toDraw = getName() + " (" + getParty().wealth + ")";
//		Assets.pixel18.draw(batch, toDraw, getX() - (int) (3*toDraw.length()), getY()-15);
//		Assets.pixel18.setColor(Color.WHITE);
	}
	
	@Override
	public void autoManage() {
		// Organize patrols
		int patrolCount = (int) (getParty().wealth/(patrolCost*2));
		if (getPatrols().size < patrolCount) {
			this.loseWealth(patrolCost);
			createPatrol();
		}
		// Organize merchants
		for (City city : closestFriendlyCities) {
			if (!merchantExists[closestFriendlyCities.indexOf(city, true)] && this.getParty().wealth >= merchantCost) {
				this.loseWealth(merchantCost);
				int merchantWealth = merchantCost + (int) (Kingdom.distBetween(this, city)*MERCHANT_GAIN) + 1;
				createMerchant(merchantWealth, city);
			}
		}
		// Organize raiding parties
		int raiderCount = (int) (getParty().wealth/(raiderCost*2));
		if (getRaiders().size < raiderCount) {
			if (closestEnemyCities.random() != null) {
				this.loseWealth(raiderCost);
				createRaider();
			}
		}
	}
	
	public void createPatrol() {
		Patrol patrol = new Patrol(getKingdom(), this);
		patrol.patrolAround(this);
		getKingdom().addArmy(patrol);
		getPatrols().add(patrol);
		setContainerForArmy(patrol);
	}
	
	public void removePatrol(Patrol patrol) {
		patrols.removeValue(patrol, true);
	}
	public Array<Patrol> getPatrols() {
		return patrols;
	}
	public void createMerchant(int wealth, City goal) {
		if (this != goal) {
			Merchant merchant = new Merchant(getKingdom(), this, goal);
			getKingdom().addArmy(merchant);
			merchants.add(merchant);
			merchantExists[closestFriendlyCities.indexOf(goal, true)] = true;
			setContainerForArmy(merchant);
		}
	}
	
	public void removeMerchant(Merchant merchant) {
		// could make if not -1, but this will stress test
//		if (merchant.getGoal().getFaction() == merchant.getFaction())
		if (getClosestFriendlyCities().indexOf(merchant.getGoal(), true) >= 0)
			merchantExists[getClosestFriendlyCities().indexOf(merchant.getGoal(), true)] = false;
		merchants.removeValue(merchant, true);
	}

	public void createRaider() {
		City targetCity = getCloseEnemyCity();
		RaidingParty raider = new RaidingParty(getKingdom(), "Raider", getFaction(), getCenterX(), getCenterY());
		raider.raidAround(targetCity);
		raider.setDefaultTarget(this);
		getKingdom().addArmy(raider);
		raiders.add(raider);
		raiderExists[closestEnemyCities.indexOf(targetCity, true)] = true;
		setContainerForArmy(raider);
	}
	
	public void removeRaider(RaidingParty raider) {
		raiderExists[closestEnemyCities.indexOf(raider.getRaidAround(), true)] = false;
		raiders.removeValue(raider, true);
	}
	
	@Override
	public void updateToHire() {
//		if (playerIn) System.out.println("updating to hire");
		this.toHire = this.nextHire;
		// some random stuff should happen here, small chance of getting a really good crop of soldiers
		// high chance of sucky ones!
		double random = Math.random();
		if (random > .9) 		this.nextHire = PartyType.CITY_HIRE_1.generate();
		else if (random > .5)	this.nextHire = PartyType.CITY_HIRE_2.generate(); // second best
		else 					this.nextHire = PartyType.CITY_HIRE_3.generate(); // worst
	}
	
	public void updateClosestCities() {
		closestFriendlyCities.clear();
		closestEnemyCities.clear();
		// updates when a city changes hands
		for (City that : getKingdom().getCities()) {
			if (that != this && Kingdom.distBetween(this, that) < closeCityDistance) {
				if (!Faction.isAtWar(getFaction(), that.getFaction())) {
					closestFriendlyCities.add(that);
				}
				else closestEnemyCities.add(that);
			}
		}
	}
	
	public City getCloseEnemyCity() {
		return closestEnemyCities.random();
	}
	public Array<City> getClosestHostileCities() {
		return closestEnemyCities;
	}
	public Array<City> getClosestFriendlyCities() {
		return closestFriendlyCities;
	}
	public void changeFaction(Faction f) {
		BottomPanel.log(f.name + " has taken " + this.getName());
		this.getFaction().cities.removeValue(this,true);
		this.setFaction(f);
		this.getFaction().cities.add(this);
//		for (Village v : villages) v.setFaction(f);
		Faction.updateFactionCityInfo();
	}
		
//	public void createVillages() {
//		int maxVillages = 3; // randomize?
//		for (int i = 0; i < maxVillages; i++) {
//			System.out.println("village spots: " + villageSpots.size);
//
////			while(!createVillage());
//			if (villageSpots.size > 0) createVillage();
//			else break;
//		}
//	}
	
//	public void createVillage() {
//		float x;
//		float y;
//
//		PointH loc = villageSpots.random();
//		x = (float) loc.x;
//		y = (float) loc.y;
//		
//		getKingdom().getMap().availableLocationSites.removeValue(loc, false);
//		
//		addVillage(new Village(getKingdom(), getName() + " Village", -1, getFaction(), x, Map.HEIGHT - y, 0));
//	}
	
	// no longer needed
//	public boolean villageTooClose(float x, float y) {
//		for (Village village : villages) {
//			if (Kingdom.distBetween(village, new Point(x, y)) <= villageSeparation) {
//				return true;
//			}
//		}
//		
//		for (City city : getKingdom().getCities()) {
//			for (Village village : city.getVillages()) {
//				if (Kingdom.distBetween(village, new Point(x, y)) <= villageSeparation) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}

//	public void addVillage(Village village) {
//		villages.add(village);
//		this.getKingdom().addActor(village);
//		village.setParent(this);
//	}
//	public Array<Village> getVillages() {
//		return villages;
//	}
//	
	public Array<RaidingParty> getRaiders() {
		return raiders;
	}

}
