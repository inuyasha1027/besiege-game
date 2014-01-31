/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;

import kyle.game.besiege.Kingdom;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.party.PartyType;

import com.badlogic.gdx.math.MathUtils;

public class Noble extends Army {
	private static final String[] RANKS = {"Baron", "Earl", "Count", "Duke", "Prince", "Archduke", "King"};
	private static final int[] REKNOWN_RANK = {0,    50,      100,     150,    200, 	250,		 300, 301}; 
	private static final int BASE_PC = 35;
	private static final float REKNOWN_PC_FACTOR = .5f;
	private final float WAIT = 30;
	//	private static final int MAX_LEVEL = 25;
	public Location home;
	public String rankName;
	public int rank;
	private int reknown;
	private int nextRank;
	private Location specialTarget;
	private boolean toggleWait;

	public int partyCap;
	//	private int level;

	public Noble(Kingdom kingdom, Location home) {
		super(kingdom, "", home.getFaction(), home.getCenterX(), home.getCenterY(), PartyType.NOBLE_TEST);
		this.home = home;
		this.setDefaultTarget((City) home);
		// set up initial party, rank, etc
		rank = 0; // baron for now
		reknown = 0;
		rankName = RANKS[rank];
		nextRank = REKNOWN_RANK[rank + 1];
		updateName();

		String region = "knightFlail";
		double random = Math.random();
		if (random >= .33) region = "knightLance";
		if (random > .67) region = "knightSword";
		this.setTextureRegion(region);

		this.giveReknown(MathUtils.random(150));//
		//		System.out.println("creating noble");
		kingdom.addArmy(this);
		this.type = ArmyType.NOBLE;
	}

	@Override
	public void uniqueAct() {
		//		System.out.println(getName() + " unique act");
		// nobles do: 
		// travel between their own cities (by default)
		// or are sent to besiege other cities (by faction)
		if (this.hasSpecialTarget()) {
			//			System.out.println(getName() + " managing special target " + this.specialTarget.getName());
			// go to city to besiege/raid
			manageSpecialTarget();
		}
		else {
			wanderBetweenCities();
		}
	}

	@Override
	public void garrisonIn(Location location) {
		super.garrisonIn(location);
		this.waitFor(randomWait());
	}

	private float randomWait() {
		return MathUtils.random(0, WAIT);
	}

	public void manageSpecialTarget() {
		if (this.path.isEmpty()) {
			if (specialTarget != null) {
				setTarget(specialTarget);
//				System.out.println(getName() + " setting special target " + specialTarget.getName());
				path.travel();
			}
			else {
//				System.out.println(getName() + " special target is null");
				path.travel();
			}
		}
		else path.travel();
	}

	@Override 
	public void besiege(Location location) {
		super.besiege(location);
//		System.out.println(this.getName() + " is besieging " + location.getName());
	}

	public void wanderBetweenCities() {
		if (this.path.isEmpty()) {
			goToNewTarget();
			toggleWait = true;
			//			System.out.println("doesn't have target and is waiting? " + this.isWaiting() + " and is garrisoned? " + isGarrisoned());
			//				System.out.println("starting to wait");

			//				System.out.println("getting new target");
		}
	}

	public void goToNewTarget() {
		Location newTarget = this.getFaction().getRandomCity();
		if (this.getGarrisonedIn() != newTarget) {
			if (newTarget != null)
				setTarget(newTarget);
			else {
//				System.out.println("noble.wanderBetweenCities target");
			}
		}
//		else System.out.println("new target is garrisoned in");
	}

	@Override
	public String getUniqueAction() {
		return "Noble unique action";
	}

	public void giveReknown(int reknown) {
		this.reknown += reknown;
		if (rank <= RANKS.length - 1) {
			if (this.reknown >= nextRank) {
				increaseRank();
			}
		}
		partyCap = (int) (reknown * REKNOWN_PC_FACTOR + BASE_PC);
	}
	private void increaseRank() {
		this.rank++;
		rankName = RANKS[rank];
		nextRank = REKNOWN_RANK[rank+1];
		updateName();
	}
	private void updateName() {
		this.setName(rankName + " of " + home.getName());
	}
	// for when their old estate no longer belongs to their kingdom.
	public void giveNewHome() {

	}
	@Override
	public void nextTarget() {
		super.nextTarget();
		//		System.out.println(this.getTarget().getName() + " is new target of " + this.getName());
	}
	public void setSpecialTarget(Location specialTarget) {
		this.specialTarget = specialTarget;
	}
	public boolean hasSpecialTarget() {
		return specialTarget != null;
	}
	@Override 
	public void endSiege() {
		super.endSiege();
		this.specialTarget = null;
	}
}
