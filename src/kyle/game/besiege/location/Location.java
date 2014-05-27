/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.location;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Battle;
import kyle.game.besiege.Destination;
import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.Point;
import kyle.game.besiege.Siege;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.army.Farmer;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.Weapon;
import kyle.game.besiege.voronoi.Center;
import kyle.game.besiege.voronoi.Corner;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class Location extends Actor implements Destination {
	private final float SCALE = .06f;
	private final int offset = 30;
	private final int HIRE_REFRESH = 120; // seconds it takes for soldiers to refresh in city
	// TODO ^ change this to a variable. later make city wealth affect quality of soldiers.
	private final int CLOSE_LOC_DISTANCE = 1000; // distance away locations are considered "close"
	private TextureRegion region;
	public enum LocationType {CITY, CASTLE, VILLAGE};
	public LocationType type;

//	protected Array<Location> closestFriendlyLocations;
//	protected Array<Location> closestEnemyLocations;
	
	protected Array<City> closestFriendlyCities;
	protected Array<Castle> closestFriendlyCastles;
	protected Array<Village> closestFriendlyVillages;
	
	public Array<City> closestEnemyCities;
	public Array<Castle> closestEnemyCastles;
	public Array<Village> closestEnemyVillages;
	
	private boolean mouseOver;
	
	private Kingdom kingdom;
	private String name;
	private int index;
	private Faction faction;
	
	private int population;
	
	private Array<Army> garrisonedArmies;
	protected Party toHire;
	protected Party nextHire; // prevents player from loading and quitting to get ideal choice of hire
	public Army garrison;
	
	private float timeSinceFreshHire;
	
	private Siege siege;
	
	private float spawnX; // where should units spawn? must be inside
	private float spawnY;
	
	private boolean autoManage;
	public boolean playerIn; //is player garrisoned inside (special menu)
	public boolean hostilePlayerTouched;
	public boolean playerWaiting; // is player waiting inside?
	public boolean playerBesieging;
	
	public Point spawnPoint; // point where armies and farmers should spawn if on water
	public Center center; // one of these will be null
	public Corner corner;
	
	
	public Location(Kingdom kingdom, String name, int index, Faction faction, float posX, float posY, Party garrison) {
		this.kingdom = kingdom;
		this.name = name;
		this.index = index;
		this.faction = faction;
				
		setPosition(posX, posY);
		
		setTextureRegion("Castle"); // default location textureRegion
	
		garrisonedArmies = new Array<Army>();
		this.garrison = new Army(getKingdom(), this.getName() + " Garrison", getFaction(), getCenterX(), getCenterY(), null);
		this.garrison.setParty(garrison);
//		this.garrison(this.garrison);

		autoManage = true;
		playerIn = false;
		hostilePlayerTouched = false;
		
//		closestFriendlyLocations = new Array<Location>();
//		closestEnemyLocations = new Array<Location>();
		
		closestEnemyCities = new Array<City>(); 
		closestEnemyCastles = new Array<Castle>();
		closestEnemyVillages = new Array<Village>();
		
		closestFriendlyCities = new Array<City>(); 
		closestFriendlyCastles = new Array<Castle>();
		closestFriendlyVillages = new Array<Village>();
		
		timeSinceFreshHire = 0;
		nextHire = new Party(); //empty
		updateToHire();
		updateToHire();
		
		this.setRotation(0);
		this.setScale(1);
		initializeBox();
	}

	public void initializeBox() {
		this.setWidth(region.getRegionWidth()*SCALE);
		this.setHeight(region.getRegionHeight()*SCALE);
		this.setOrigin(region.getRegionWidth()*SCALE/2, region.getRegionHeight()*SCALE/2);
	}
	public void setCorner(Corner corner) {
		this.corner = corner;
	}
	public void setCenter(Center center) {
		this.center = center;
	}
	
	/** Initialize containers for armies created at this site 
	 * @param army Army to be created */
	public void setContainerForArmy(Army army) {
		if (this.center != null) {
			army.containing = center;
			center.armies.add(army);
		}
		else {
			army.containing = corner.touches.get(0);
			corner.touches.get(0).armies.add(army);
		}
	}
	
	@Override
	public void act(float delta) {
		if (autoManage) {
			autoManage();
		}
		if (timeSinceFreshHire >= HIRE_REFRESH) {
			timeSinceFreshHire = 0;
			updateToHire();
		}
		else timeSinceFreshHire += delta;
		
		if (!kingdom.isPaused()) {
			hostilePlayerTouched = false; // only can be selected when game is paused;
		}
//		if (underSiege()) {
//			if (playerBesieging) {
//				if (!siege.armies.contains(kingdom.getPlayer(), true)) {
//					playerBesieging = false;
//					kingdom.getMapScreen().getSidePanel().setDefault();
//				}
//			}
//			if (!playerBesieging) {
//				if (siege.armies.contains(kingdom.getPlayer(), true))
//					playerBesieging = true;
//			}
//		}
		
		super.act(delta);
	}
	
	public void autoManage() {
		//contains actions in extensions
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		setRotation(kingdom.getMapScreen().rotation);
		batch.draw(region, getX(), getY(), getOriginX(), getOriginY(),
				getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
	}

//	public void drawInfo(SpriteBatch batch, float parentAlpha) {
//		Kingdom.arial.setColor(Kingdom.factionColors.get(getFaction()));
//		Kingdom.arial.draw(batch, getName() + " (" + garrison.wealth + ")", getX(), getY());	
//		
//		float offset = 0;
//		for (Army army : getGarrisoned()) {
//			offset -= getOffset()*getKingdom().getZoom();
//			Kingdom.arial.draw(batch, army.getName() + ": " + army.getTroopCount(), getX(), getY() + offset);
//		}	
//	}

	/** initializes closeLocation arrays with close locations
	 */
	public void findCloseLocations() {
		closestEnemyCities.clear();
		closestEnemyCastles.clear();
		closestEnemyVillages.clear();
		
		for (City that : getKingdom().getCities()) {
			if (that != this && Kingdom.distBetween(this, that) < CLOSE_LOC_DISTANCE) {
				if (!Faction.isAtWar(getFaction(), that.getFaction())) 
					closestFriendlyCities.add(that);
				else closestEnemyCities.add(that);
			}
		}
		for (Castle castle : getKingdom().castles) {
			if (castle != this && Kingdom.distBetween(this, castle) < CLOSE_LOC_DISTANCE) {
				if (!Faction.isAtWar(getFaction(), castle.getFaction())) 
					closestFriendlyCastles.add(castle);
				else closestEnemyCastles.add(castle);
			}
		}
		// when is village faction info initialized?
//		for (Village village : getKingdom().villages) {
//			if (village != this && Kingdom.distBetween(this, village) < CLOSE_LOC_DISTANCE) {
//				if (!Faction.isAtWar(getFaction(), village.getFaction()))
//					closestFriendlyVillages.add(village);
//				else closestEnemyVillages.add(village);
//			}
//		}
	}
	
	/** Update closeLocation arrays (doesn't look for new cities)
	 * 
	 */
	public void updateCloseLocations() {
		Array<City> newCloseEnemyCities = new Array<City>();
		Array<City> newCloseFriendlyCities = new Array<City>();
		
		// cities
		for (City c : closestEnemyCities) {
			if (Faction.isAtWar(this.getFaction(), c.getFaction())) {
				if (!newCloseEnemyCities.contains(c, true))
					newCloseEnemyCities.add(c);
			}
			else if (!newCloseFriendlyCities.contains(c, true))
				newCloseFriendlyCities.add(c);
		}
		for (City c : closestFriendlyCities) {
			if (Faction.isAtWar(this.getFaction(), c.getFaction())) {
				if (!newCloseEnemyCities.contains(c, true))
					newCloseEnemyCities.add(c);
			}
			else if (!newCloseFriendlyCities.contains(c, true))
				newCloseFriendlyCities.add(c);
		}
		closestEnemyCities = new Array<City>(newCloseEnemyCities);
		closestFriendlyCities = new Array<City>(newCloseFriendlyCities);
		
		Array<Castle> newCloseEnemyCastles = new Array<Castle>();
		Array<Castle> newCloseFriendlyCastles = new Array<Castle>();
		
		// castles
		for (Castle c : closestEnemyCastles) {
			if (Faction.isAtWar(this.getFaction(), c.getFaction())) {
				if (!newCloseEnemyCastles.contains(c, true))
					newCloseEnemyCastles.add(c);
			}
			else if (!newCloseFriendlyCastles.contains(c, true))
				newCloseFriendlyCastles.add(c);
		}
		for (Castle c : closestFriendlyCastles) {
			if (Faction.isAtWar(this.getFaction(), c.getFaction())) {
				if (!newCloseEnemyCastles.contains(c, true))
					newCloseEnemyCastles.add(c);
			}
			else if (!newCloseFriendlyCastles.contains(c, true))
				newCloseFriendlyCastles.add(c);
		}
		closestEnemyCastles = new Array<Castle>(newCloseEnemyCastles);
		closestFriendlyCastles = new Array<Castle>(newCloseFriendlyCastles);

		Array<Village> newCloseEnemyVillages = new Array<Village>();
		Array<Village> newCloseFriendlyVillages = new Array<Village>();
		
		// villages
		for (Village v : closestEnemyVillages) {
			if (Faction.isAtWar(this.getFaction(), v.getFaction())) {
				if (!newCloseEnemyVillages.contains(v, true))
					newCloseEnemyVillages.add(v);
			}
			else if (!newCloseFriendlyVillages.contains(v, true))
				newCloseFriendlyVillages.add(v);
		}
		for (Village v : closestFriendlyVillages) {
			if (Faction.isAtWar(this.getFaction(), v.getFaction())) {
				if (!newCloseEnemyVillages.contains(v, true))
					newCloseEnemyVillages.add(v);
			}
			else if (!newCloseFriendlyVillages.contains(v, true))
				newCloseFriendlyVillages.add(v);
		}
		closestEnemyVillages = new Array<Village>(newCloseEnemyVillages);
		closestFriendlyVillages = new Array<Village>(newCloseFriendlyVillages);

	}
	public Location getCloseEnemyCity() {
		return closestEnemyCities.random();
	}
	public Location getCloseEnemyCastles() {
		return closestEnemyCastles.random();
	}
	public Location getCloseEnemyVillage() {
		return closestEnemyVillages.random();
	}
	
	public void siegeAttack(Array<Army> attackers) {
//		Army garrisonArmy = new Army(getKingdom(), this.getName() + " Garrison", getFaction(), getCenterX(), getCenterY(), null);
//		garrisonArmy.setParty(garrison);
		attackers.first().createBattleWith(garrison);
		Battle b = garrison.getBattle();
		b.setPosition(this.getX()-b.getWidth()/2, this.getY()-b.getHeight()/2);
		b.dAdvantage = this.getDefenseFactor();
		for (Army a : attackers) {
			if (a.getParty().player) ;
				// bring up option to attack, pause/stay etc
			if (a != attackers.first())
				a.joinBattle(b);
		}
  		for (Army a : garrisonedArmies) {
//			System.out.println("adding " + a.getName() + " to siege battle");
			a.joinBattle(b);
		}
	}
	public void beginSiege(Army army) {
		siege = new Siege(this, army.getFaction());
		siege.add(army);
		kingdom.addActor(siege);
	}
	public void endSiege() {
		kingdom.removeActor(siege);
		siege = null;
	}
	public boolean underSiege() {
		return siege != null;
	}
	public Siege getSiege() {
		return siege;
	}
	public void updateToHire() {
//		if (this.toHire.size == 0) toHire.add(new Soldier(Weapon.PITCHFORK, null));
		// contained in extensions
	}
	public void garrison(Soldier soldier) {
		garrison.getParty().addSoldier(soldier);
	}
	public void garrison(Army army) {
		if (army.shouldRepair()) {
			repair(army);
		}
		garrisonedArmies.add(army);
		army.setVisible(false);
		army.setPosition(this.getCenterX()-army.getOriginX(), getCenterY()-army.getOriginY());
		kingdom.removeArmy(army);
	}
	public void eject(Army army) {
		garrisonedArmies.removeValue(army, true);
		army.setGarrisonedIn(null);
		army.setVisible(true);
		kingdom.addArmy(army);
		if (army == getKingdom().getPlayer()) {
			if (playerWaiting)
				stopWait();
		}
	}
	public void repair(Army army) {
		PartyType pt = army.getPartyType();
		army.getParty().repair(pt);
	}
	public void startWait() {
//		System.out.println("location.startWait()");
		playerWaiting = true;
		getKingdom().getPlayer().setWaiting(true);
		getKingdom().getMapScreen().shouldFastForward = true;
		getKingdom().getMapScreen().shouldLetRun = true;
		getKingdom().setPaused(false);
	}
	
	public void stopWait() {
//		System.out.println("location.stopWait()");
		playerWaiting = false;
		getKingdom().getPlayer().setWaiting(false);
		getKingdom().getMapScreen().shouldFastForward = false;
		getKingdom().getMapScreen().shouldLetRun = false;
		getKingdom().setPaused(true);
	}
	public boolean hire(Party party, Soldier s) { // returns true if hired successfully, false if not (not enough money?)
		if (toHire.getHealthy().contains(s, true)) {
			if (party.wealth - Weapon.TIER_COST[s.tier] >= party.minWealth) {
				party.wealth -= Weapon.TIER_COST[s.tier];
				toHire.removeSoldier(s);
				party.addSoldier(s);
				s.party = party;
				return true;
			}
		}
		return false;
	}
	public Party getToHire() {
		return toHire;
	}
	public void setToHire(Party toHire) {
		this.toHire = toHire;
	}
	
	@Override
	public String getName() {
		return name;
	}
	@Override
	public Faction getFaction() {
		return faction;
	}
	private void setFaction(Faction faction) {
		this.faction = faction;
	}
	public void changeFaction(Faction newFaction) {
		// TODO updateMerchants();
		// TODO update villagers();
		if (this.type == LocationType.CITY) {
			this.faction.cities.removeValue((City) this, true);
			newFaction.cities.add((City) this);
			BottomPanel.log(newFaction.name + " has taken " + this.getName() + " from " + this.getFactionName());
		}
		else if (this.type == LocationType.CASTLE) {
			this.faction.castles.removeValue((Castle) this, true);
			newFaction.castles.add((Castle) this);
			BottomPanel.log(newFaction.name + " has taken " + this.getName() + " from " + this.getFactionName());
		}
		else if (this.type == LocationType.VILLAGE) {
			for (Farmer f : ((Village) this).farmers) {
				
			}
		}
		this.faction = newFaction;
		// update friendly arrays, and everything (if this isn't a village)
		if (this.type != LocationType.VILLAGE) 
			Faction.updateFactionCityInfo();
	}
	@Override 
	public int getType() {
		return 1;
	}
	public int getIndex() {
		return index;
	}
	public Party getParty() {
		return garrison.getParty();
	}
	public void setParty(Party party) {
		this.garrison.setParty(party);
	}
	public int getPop() {
		return population;
	}
	public void addWealth(int wealth) {
		this.getParty().wealth += wealth;
	}
	public void loseWealth(int wealth) {
		this.getParty().wealth -= wealth;
	}
//	public int getWealth() {
//		return wealth;
//	}
//	public void setWealth(int wealth) {
//		this.wealth = wealth;
//	}
//	@Override
//	public double distToCenter(Destination d) {
//		return Math.sqrt((d.getX()-getCenterX())*(d.getX()-getCenterX())+(d.getY()-getCenterY())*(d.getY()-getCenterY()));
//	}
//	@Override
//	public double distTo(Destination d) {
//		return Math.sqrt((d.getX()-getX())*(d.getX()-getX())+(d.getY()-getY())*(d.getY()-getY()));
//	}

	@Override
	public void setMouseOver(boolean mouseOver) {
		if (this.mouseOver) {
			if (!mouseOver) {
				kingdom.getMapScreen().getSidePanel().returnToPrevious();
				this.mouseOver = false;
			}
		}
		else if (mouseOver) {
			if (!this.mouseOver) {
				System.out.println("MASODFHASIDj");
				kingdom.getMapScreen().getSidePanel().setActiveLocation(this);
				this.mouseOver = true;
			}
		}
	}
	
	public float getCenterX() {
		return this.getX() + this.getOriginX();
	}
	public float getCenterY() {
		return this.getY() + this.getOriginY();
	}

	public Kingdom getKingdom() {
		return kingdom;
	}
	
	public void setTextureRegion(String textureRegion) {
		region = Assets.atlas.findRegion(textureRegion);
	}
	
	public Array<Army> getGarrisoned() {
		return garrisonedArmies;
	}
	
	public Array<Army> getGarrisonedAndGarrison() {
		Array<Army> garrisoned = new Array<Army>(getGarrisoned());
//		Army garrisonArmy = new Army(getKingdom(), "Garrison", getFaction(), getCenterX(), getCenterY(), null);
//		garrisonArmy.setParty(garrison);
		garrisoned.add(garrison);
		return garrisoned;
	}
	
	public int getOffset() {
		return offset;
	}
	public boolean isVillage() {
		return false;
	}
	public String getFactionName() {
		return faction.name;
	}
	public String getTypeStr() {
		if (this.isVillage())
			return "Village";
		else {
			return "City";
		}
	}
	public float getDefenseFactor() {
		return 1.5f; //TODO
	}
}
