/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;

import kyle.game.besiege.Character;
import kyle.game.besiege.Destination;
import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.Map;
import kyle.game.besiege.SidePanel;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.party.PartyType;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

public class ArmyPlayer extends Army {
	private final String textureRegion = "Player";
	
	private Character character;
	private SidePanel panel;
	
	// debugging
	private final ShapeRenderer sr;
	
//	private Destination target;

	public ArmyPlayer(Kingdom kingdom, Character character, Faction faction,
			int posX, int posY, int troopCount) {
//		super(kingdom, character.name, Faction.PLAYER_FACTION, posX, posY, PartyType.PATROL);
//		super(kingdom, character.name, Faction.BANDITS_FACTION, posX, posY, PartyType.RAIDING_PARTY);
		super(kingdom, character.name, Faction.factions.get(3), posX, posY, PartyType.NOBLE_DEFAULT_1);

		this.character = character;
		this.panel = getKingdom().getMapScreen().getSidePanel();
		
		setTextureRegion(textureRegion);
//		initializeBox();
//		System.out.println("player origin = " + this.getOriginX() + " y: " + this.getOriginY());
//		System.out.println("player width = " + this.getWidth() + " height: " + this.getHeight());
		
//		target = null;
		setStopped(true);
		setWaiting(false);
		
		// debugging
		sr = new ShapeRenderer();
	}

	@Override
	public void act(float delta) {	
		if (this.lastPathCalc > 0) this.lastPathCalc--;

		if (isStopped() && !isWaiting()) {
//			System.out.println("is stopped and isn't waiting");
			setPaused(true);
		}
		if (isWaiting())
			setStopped(true);
		else if (this.getBattle() != null && isStopped())
			setStopped(false);
		if (!this.isWaiting() && this.isGarrisoned() && this.getTarget() != null) { // eject
			getGarrisonedIn().playerIn = false;
			getGarrisonedIn().eject(this);
			panel.setDefault();
		}
		if (this.isInSiege() && this.getTarget() != null) { //remove from siege?
			if (getSiege().location.playerWaiting) {
				this.setTarget(null);
				getSiege().location.stopWait(); 
			}
			else {
//				System.out.println("armyPlayer.getSiege().remove");
				getSiege().remove(this);
			}
//			System.out.println(getTarget().getName());
		}
		if (!getKingdom().isPaused() && !isInBattle())
			panel.setStay(false);
			
		this.speedFactor = (float) (this.ORIGINAL_SPEED_FACTOR * Character.getAttributeFactor("Marching"));
		setLOS(calcLOS());
//		getKingdom().getMapScreen().getFog().updateFog((int) this.getCenterX(), (int) this.getCenterY(), (int) this.getLineOfSight());

		setSpeed(calcSpeed());   // update speed
		setMorale(calcMorale()); // update morale
		setScale(calcScale());

		if (forceWait) { // forces player to wait
//			System.out.println("forcing wait fix next");
			wait(delta);
			if (!isWaiting()) 
				forceWait = false;
//			System.out.println(forceWait);
		}
		else {
//			if (!isInSiege()) {
				if (hasTarget() && !isStopped() && !isInBattle()) {
					if (targetLost())
					{
						setTarget(null);
						getKingdom().setPaused(true);
						BottomPanel.log("Target lost!", "yellow");
						panel.setDefault();
					}
					else {
						path.travel();
//						getKingdom().getMapScreen().getFog().updateFog((int) this.getCenterX(), (int) this.getCenterY(), (int) this.getLineOfSight());
					}
				}
//			}
//			else if (isInSiege()) {
//			
//			}
		}
		getParty().act(delta);
		momentumDecay();
//		setMorale(100);
//		this.faction.goRogue();
		//getParty().distributeExp(40);
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		batch.draw(getTextureRegion(), getX(), getY(), getOriginX(), getOriginY(),
				getWidth(), getHeight(), 1, 1, getRotation());
		
		if (Map.debug){
			batch.end();
			sr.begin(ShapeType.Line);
			sr.setProjectionMatrix(batch.getProjectionMatrix());
			sr.setColor(1,0,0,1);
			Destination prev;
			prev = this; // do in reverse order (Stack!)
			for (Destination curr : path.dStack) {
				if (prev != null && curr != null && prev != curr) {
					sr.line((float) prev.getX(), (float)(prev.getY()), (float) curr.getX(), (float) (curr.getY()));
				}
				prev = curr;
			}
			sr.end();
			batch.begin();
		}
		//if (mousedOver()) drawInfo(batch, parentAlpha);
	}
	
	@Override
	public boolean detectCollision() {
		switch (getTarget().getType()) {
		case 0: // point reached
			return detectPointCollision();
		case 1: // city reached
			return detectLocationCollision();
		case 2: // army reached
			return detectArmyCollision();
		default:
			return false;
		}
	}
	
	@Override
	public void enemyLocationCollision(Location targetLocation) {
		setPaused(true);
		this.setTarget(null);
		targetLocation.hostilePlayerTouched = true;
		panel.setActiveLocation(targetLocation);
		panel.setStay(true);
	}
	
	@Override
	public void friendlyLocationCollision(Location targetLocation) {
		this.garrisonIn(targetLocation);
		targetLocation.playerIn = true;
		this.setTarget(null);
		panel.setActiveLocation(targetLocation);
	}
	
	@Override
	public boolean detectPointCollision() {
		if (distToCenter(getTarget()) < 1) {
			setStopped(true);
			setPaused(true);
			return true;
		}
		return false;
	}
	
	@Override
	public void enemyArmyCollision(Army targetArmy) {
		setPaused(true);
		if (!targetArmy.isInBattle() && !this.isInBattle() && targetArmy.getTarget() != this) {
			targetArmy.playerTouched = true;
			panel.setActiveArmy(targetArmy);
			panel.setStay(true); // when an army is reached, force a user decision
		}
		else if (targetArmy.isInBattle() && !this.isInBattle()){
			// join battle?
			targetArmy.getBattle().add(this);
			this.setBattle(targetArmy.getBattle());
		}
	}
	
	@Override
	public void friendlyArmyCollision(Army targetArmy) {
		//follow
		if (this.getSpeed() >= targetArmy.getSpeed()) this.setSpeed(targetArmy.getSpeed() - 5);
	}

	@Override
	public boolean targetLost() {
		if (hasTarget() && getTarget().getType() == 2) {
			Army targetArmy = (Army) getTarget();
			if (!targetArmy.hasParent() || targetArmy.isGarrisoned()) {
				return true;
			}
			if (targetArmy.isInBattle())
				//if (targetArmy.getBattle().shouldJoin(this) == 0) // Player should get to choose what battles to join!
					return true;
			if (getKingdom().getMapScreen().losOn) {
				if (distToCenter(getTarget()) > this.getLineOfSight() - 5)
					return true;
			}
		}
		return false;
	}
	
	public void attack(Army army) {
		createBattleWith(army);
		BottomPanel.log("Attacking " + getTarget().getName() + "!", "blue5");
	}
	
	@Override
	public float calcLOS() {
		return ((float) (super.calcLOS()*Character.getAttributeFactor("Spotting")));
	}

	@Override 
	public boolean setTarget(Destination target) {
//		if (target != null)
//			System.out.println("setting target " + target.getName());
		boolean toReturn = super.setTarget(target);
		setStopped(false);
		return toReturn;
	}
	
	@Override
	public void newTarget(Destination target) {
//		System.out.println("player can't get new target");
	}
//	@Override
//	public Destination getTarget() {
//		return target;
//	}
	@Override
	public void destroy() {
		BottomPanel.log("Player lost battle!", "red");
		super.destroy();
		getKingdom().removeArmy(this);
		//TODO player death menu
	}
	@Override
	public void nextTarget() {
//		System.out.println("next target");
		setTarget(null);
		setPaused(true);
	}

	public void setPaused(boolean paused) {
		this.getKingdom().setPaused(paused);
	}
	
	public int getLevel() {
		return character.level; // todo
	}
	public String getPartyInfo() {
		return getParty().getHealthySize() + "/" + getParty().getTotalSize();
	}
	public Character getCharacter() {
		return character;
	}
}
