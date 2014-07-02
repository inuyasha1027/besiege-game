/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;

import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.army.Army.ArmyType;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.party.PartyType;


public class Merchant extends Army {
	public static int MERCHANT_WAIT = 30;
//	private final double waitTime = 10;
	private final String textureRegion = "Merchant";
	private City goal;

	public Merchant(Kingdom kingdom,
			City defaultTarget, City goal) {
		super(kingdom, "Merchant of " + defaultTarget.getName(), defaultTarget.getFaction(), defaultTarget.getCenterX(), defaultTarget.getCenterY(), PartyType.MERCHANT);
		this.setGoal(goal);
		this.setDefaultTarget(defaultTarget);
		this.setTextureRegion(textureRegion);
		this.type = ArmyType.MERCHANT;
		this.passive = true;
	}

//	@Override
//	public void detectNearby() {
//		//naive approach (N^2)
//		Army army = closestHostileArmy();
//		if (army != null) {
//			if (shouldRunFrom(army)) runFrom(army);
//			//else if (shouldAttack(army)) setTarget(army);
//			// Doesn't attack
//		}
//	}
//
	
//	@Override
//	public void garrisonIn(Location city) {
//		if (city == goal) {
//			waitFor(waitTime);
//			runFrom(null);
//			city.garrison(this); 
//			setGarrisonedIn(city);
//		}
//		else {
//			// test to see if should garrison goes here
//			waitFor(3); //arbitrary
//			runFrom(null);
//			city.garrison(this); 
//			setGarrisonedIn(city);
//		}
//	}
	
	@Override
	public void wait(float delta) {
		if (getKingdom().clock() >= getWaitUntil()) {
			if (isGarrisonedIn(goal)) {
				deposit();
			}
			setWaiting(false);
			setWaitUntil(0);
			setForceWait(false);
		}
	}
	
	public void deposit() {
		goal.getParty().wealth += getParty().wealth;
		this.destroy();
	}
	
	@Override
	public void destroy() {
		if (isGarrisoned())
			goal.eject(this);
		getKingdom().removeArmy(this);
		this.remove();
		if (getDefaultTarget() != null) {
			((City) getDefaultTarget()).removeMerchant(this);
		}
	}
	@Override
	public void uniqueAct() {
//		System.out.println(getName() + " setting next target");
		if (this.path.isEmpty())
			setTarget(goal);
	}
	public void setGoal(City goal) {
		this.goal = goal;
		newTarget(goal);
	}
	public City getGoal() {
		return goal;
	}
}
