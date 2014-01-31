/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.party;

import java.util.Iterator;
import kyle.game.besiege.Character;

import kyle.game.besiege.panels.BottomPanel;

import com.badlogic.gdx.utils.Array;

public class Party {
	private final double BASE_CHANCE = .5;
	private final double MIN_WEALTH_FACTOR = 1.4; // times troopcount
	
	public int wealth;
	public int minWealth; // keeps the party out of debt, of course!
	
	public boolean player;
	private Array<Soldier> healthy;
	private Array<Soldier> wounded;
	private Array<Soldier> prisoners;
	
	private int atkTotal;
	private int defTotal;
	private int spdTotal;
	
	public double woundChance;
	
	public Party() {
		player = false;
		healthy = new Array<Soldier>();
		wounded = new Array<Soldier>();
		prisoners = new Array<Soldier>();
		atkTotal = 0;
		defTotal = 0;
		spdTotal = 0;
		calcStats();
		
		wealth = 0;
		
		woundChance = BASE_CHANCE;
	}
	
	public void act(float delta) {
		if (player) woundChance = BASE_CHANCE * Character.getAttributeFactor("Reviving");
		checkHeal();
		calcStats();
	}
	
	public void checkUpgrades() {
		for (Soldier s : getUpgradable()) {
			s.upgrade(Weapon.upgrade(s.weapon).random());
		}
	}
	
	public void checkHeal() { // to be called every frame 
		Iterator<Soldier> iter = wounded.iterator();
		while (iter.hasNext()) {
			Soldier soldier = iter.next();
			if (soldier.isHealed())
				heal(soldier);
		}
	}
	
	public void addSoldier(Soldier soldier) {
		if (soldier.isWounded()) {
			wounded.add(soldier);
			wounded.sort();
		}
		else {
			healthy.add(soldier);
			healthy.sort();
		}
		calcStats();
	}
	public void removeSoldier(Soldier soldier) {
		if (healthy.contains(soldier, true)) {
			healthy.removeValue(soldier, true);
		}
		else if (wounded.contains(soldier, true))
			wounded.removeValue(soldier, true);
		calcStats();
	}

	public void addPrisoner(Soldier soldier) {
		prisoners.add(soldier);
		prisoners.sort();
	}
	public boolean casualty(Soldier soldier) { // returns true if killed, false if wounded
		if (Math.random() < woundChance) {
			wound(soldier);
			return false;
		}
		else kill(soldier);
		return true;
	}
	public void kill(Soldier soldier) {
		removeSoldier(soldier); //can be used to kill both healthy and wounded soldiers.
		wounded.sort();
		healthy.sort();
	}
	public void wound(Soldier soldier) {
		soldier.wound();
		healthy.removeValue(soldier, true);
		this.addSoldier(soldier);
	//	if (player) BottomPanel.log(soldier.name + " wounded", "orange");
		calcStats();
	}
	public void heal(Soldier soldier) {
		soldier.heal();
		wounded.removeValue(soldier, true);
		this.addSoldier(soldier);
		if (player) BottomPanel.log(soldier.name + " healed", "blue");
		healthy.sort();
	}
	public Array<Soldier> getUpgradable() {
		Array<Soldier> upgradable = new Array<Soldier>();
		for (Soldier s : healthy) {
			if (s.canUpgrade)
				upgradable.add(s);
		}
		for (Soldier s : wounded) {
			if (s.canUpgrade)
				upgradable.add(s);
		}
		return upgradable;
	}
	public void calcStats() {
		atkTotal = 0;
		defTotal = 0;
		spdTotal = 0;
		for (Soldier s : healthy) {
			atkTotal += s.baseAtk + s.bonusAtk;
			defTotal += s.baseDef + s.bonusDef;
			spdTotal += s.baseSpd + s.bonusSpd;
		}
		if (!player) minWealth = (int) (MIN_WEALTH_FACTOR*getTotalSize());
		else minWealth = 0;
	}
	public void givePrisoner(Soldier prisoner, Party recipient) {
		if (this.wounded.contains(prisoner, true))
			this.wounded.removeValue(prisoner, true);
		else BottomPanel.log("trying to add invalid prisoner", "red");
		recipient.addPrisoner(prisoner);
	}

	public void returnPrisoner(Soldier prisoner, Party recipient) {
		if (this.prisoners.contains(prisoner, true))
			this.prisoners.removeValue(prisoner, true);
		else BottomPanel.log("trying to remove invalid prisoner", "red");
		recipient.addSoldier(prisoner);
	}
	
	public int getHealthySize() {
		return healthy.size;
	}
	public int getWoundedSize() {
		return wounded.size;
	}
	public int getTotalSize() {
		return getHealthySize() + getWoundedSize();
	}
	public Array<Soldier> getHealthy() {
		return healthy;
	}
	public Array<Soldier> getWounded() {
		return wounded;
	}
	public Array<Soldier> getHealthyCopy() {
		return new Array<Soldier>(healthy);
	}
	public Array<Soldier> getPrisoners() {
		return prisoners;
	}
	public Array<Array<Soldier>> getConsolHealthy() {
		return getConsol(healthy);
	}
	public Array<Array<Soldier>> getConsolWounded() {
		return getConsol(wounded);
	}
	public Array<Array<Soldier>> getConsolPrisoners() {
		return getConsol(prisoners);
	}
	// TODO maybe inefficient? can make more by sorting array by name
	private Array<Array<Soldier>> getConsol(Array<Soldier> arrSoldier) {
		Array<String> names = new Array<String>();
		Array<Array<Soldier>> consol = new Array<Array<Soldier>>();
		for (Soldier s : arrSoldier) {
			if (!names.contains(s.name, false)) {
				names.add(s.name);
				Array<Soldier> type = new Array<Soldier>();
				type.add(s);
				consol.add(type);
			}
			else {
				consol.get(names.indexOf(s.name, false)).add(s);
			}
		}
		return consol;	
	}
	
	@Override
	public String toString() {
		return null;
	}
	public int getAtk() {
		return atkTotal;
	}
	public int getDef() {
		return defTotal;
	}
	public int getSpd() {
		return spdTotal;
	}
	public float getAvgDef() {
		return defTotal/1f/getHealthySize();
	}
	public float getAvgSpd() {
		return spdTotal/1f/getHealthySize();
	}
	public void distributeExp(int total) {
		int exp = (int) (total/1.0/getHealthySize());
		getHealthy().shrink();
		for (int i = 0; i < getHealthy().size; i++)
			getHealthy().get(i).addExp(exp);
		wounded.sort();
		healthy.sort();
	}

	public void repair(PartyType pt) { // returns a repair cost
		int newSize = pt.getRandomSize();
		int missing = Math.max(newSize - this.getTotalSize(), 0); // no negative ints
		while (missing > 0) {
			this.addSoldier(new Soldier(pt.randomSoldierType(), this));
			missing--;
		}
		System.out.println("party repaired");
	}

}
