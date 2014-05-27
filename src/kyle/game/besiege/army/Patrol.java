/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;

import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.Map;
import kyle.game.besiege.Point;
import kyle.game.besiege.location.Castle;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.panels.Panel;
import kyle.game.besiege.party.PartyType;

public class Patrol extends Army {
	private final float PATROL_DIST;
	private final String textureRegion = "KnightHorse";
	private Location patrolAround;

	public Patrol(Kingdom kingdom, Location defaultTarget, int travelFactor) {
		super(kingdom, defaultTarget.getName() + " Patrol", defaultTarget.getFaction(), defaultTarget.getCenterX(), defaultTarget.getCenterY(), PartyType.PATROL);
		this.setDefaultTarget(defaultTarget);
		this.patrolAround = null;
		setTextureRegion(textureRegion);
		this.type = ArmyType.PATROL;
		PATROL_DIST = this.getLineOfSight()*travelFactor;
	}
	
	@Override
	public void uniqueAct() {
		if (!isRunning())
			patrol();
//		else System.out.println(getName() + " is running");
	}

	@Override
	public String getUniqueAction() {
		return "Patrolling around " + patrolAround.getName() + " (Speed: " + Panel.format(getSpeed()*SPEED_DISPLAY_FACTOR+"", 2)+")";
	}
	
	public void patrol() {
		if (path.isEmpty()) { //key
			// create new patrol target
			//TODO inefficient
//			System.out.println(getName() + " getting new patrol target");

			Point newTarget;
			do {
				float dx = (float) ((Math.random()*2-1)*PATROL_DIST); //number btw -1 and 1
				float dy = (float) ((Math.random()*2-1)*PATROL_DIST);
				newTarget = new Point(patrolAround.getCenterX() + dx, patrolAround.getCenterY() + dy);
			} while (getKingdom().getMap().isInWater(newTarget)); 
			if (!setTarget(newTarget)) System.out.println(" patrol set bad water targe");;
		}
//		else 
//			System.out.println(getName() + " has target so not patrolling");
	}
	
	public void patrolAround(Location city) {
		patrolAround = city;
	}
	public void stopPatrolling() {
		System.out.println(getName() + " stopping patrollin");
		patrolAround = null;
		findTarget();
	}
	
	@Override
	public void destroy() {
		getKingdom().removeArmy(this);
		this.remove();
		if (getDefaultTarget() != null) {
			City defaultCity = (City) getDefaultTarget();
			defaultCity.removePatrol(this);
		}
	}
}
