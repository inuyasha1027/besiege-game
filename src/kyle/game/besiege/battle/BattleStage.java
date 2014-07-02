package kyle.game.besiege.battle;

/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
// stage for battles, contains all information regarding battle.

import kyle.game.besiege.BesiegeMain;
import kyle.game.besiege.Destination;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.Point;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.Soldier;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;

public class BattleStage extends Group {
	public float scale = 1.5f;

	public static boolean drawCrests = true;

	private final float MOUSE_DISTANCE = 10; // distance destination must be from mouse to register

	public Array<Unit> allies;
	public Array<Unit> enemies;

	Party player;
	Party enemy;

	public boolean closed[][]; // open or closed? 
	public Unit[][] map; // units on map

	public int size_x = 44;
	public int size_y = 44;

	private MapScreen mapScreen;
	private Kingdom kingdom;
	//		private Party player;
	private BattleMap battlemap;

	private boolean mouseOver; // is mouse over Battle screen?
	private boolean paused;

	public int unit_width = 16;
	public int unit_height = 16;

	private Unit currentPanel;

	private boolean leftClicked;
	private boolean rightClicked;
	private Point mouse;

	// take in battle object containing arrays of armies and stuff
	public BattleStage(MapScreen mapScreen, Party player, Party enemy) {
		this.mapScreen = mapScreen;

		mouse = new Point(0,0);

		this.player = player;
		this.enemy = enemy;
		
		closed = new boolean[size_y][size_x];
		map  = new Unit[size_y][size_x];

		if (mapScreen != null)
			this.kingdom = mapScreen.getKingdom();
		else {
			createFakeBattle();
		}

		allies = new Array<Unit>();
		enemies = new Array<Unit>();

		
		this.battlemap = new BattleMap(this);
		
		addUnits();
	}

	public void createFakeBattle() {
		player = PartyType.MILITIA.generate();
		enemy = PartyType.MERCHANT.generate();
	}

	// for now, put them randomly on the field
	public void addUnits() {
		for (Soldier s : player.getHealthy()) {
			int team = 0;

			int pos_x = (int) (Math.random()*size_x);
			int pos_y = 0;

			while (map[pos_y][pos_x] != null) {
				pos_x = (int) (Math.random()*size_x);
				pos_y = (int) (Math.random()*size_y);
			}

			Unit unit = new Unit(this, pos_x, pos_y, team, s);
			addUnit(unit);
		}
		for (Soldier s : enemy.getHealthy()) {
			int team = 1;

			int pos_x = (int) (Math.random()*size_x);
			int pos_y = size_y-1;

			while (map[pos_y][pos_x] != null) {
				pos_x = (int) (Math.random()*size_x);
				pos_y = (int) (Math.random()*size_y);
			}

			Unit unit = new Unit(this, pos_x, pos_y, team, s);
			addUnit(unit);
		}
	}

	@Override
	public void act(float delta) {
		if (mouseOver) {
			if (leftClicked) leftClick(mouse);
			else if (rightClicked) rightClick(mouse);
			else if (BesiegeMain.appType != 1) mouseOver(mouse);
		}

//		if (!paused) {
			super.act(delta);
//		}
		if (leftClicked) leftClicked = false;
		if (rightClicked) rightClicked = false;
	}

	private void mouseOver(Point mouse) {
		Unit u = getUnitAt(mouse);
		//		if (d.getType() != 0)
		this.setPanelTo(u);
		//			d.setMouseOver(true);
	}

	private void setPanelTo(Unit newPanel) {
		//		if (currentPanel == null) System.out.println("currentPanel is null");
		// makes sure not to set the same panel a lot, and makes sure not to return to previous for every single point
		if (newPanel != currentPanel) {
			mapScreen.getSidePanel().setActiveUnit(newPanel);
			currentPanel = newPanel;
		}
	}

	private void leftClick(Point mouse) {
		Unit u = getUnitAt(mouse);
	}

	private void rightClick(Point mouse) {
		Unit u = getUnitAt(mouse);			
	}

	public void click(int pointer) {
		if (pointer == 0)
			leftClicked = true;
		else if (pointer == 1) 
			rightClicked = true;
		else if (pointer == 4)
			writeUnit();
	}

	public void writeUnit() {
		float x = mouse.getCenterX();
		float y = mouse.getCenterY();
		//		mapScreen.out.println(x + " " + y);
	}

	private Unit getUnitAt(Point mouse) {
		Unit close = null;
		for (Unit unit : enemies) {
			if (mouseDistTo(unit) < MOUSE_DISTANCE) {
				close = unit;
			}
		}
		for (Unit unit : allies) {
			if (mouseDistTo(unit) < MOUSE_DISTANCE) {
				close = unit;
			}
		}
		return close;
	}

	private double mouseDistTo(Unit unit) {
		float dx = mouse.getX() - unit.getCenterX();
		float dy = mouse.getY() - unit.getCenterY();
		return Math.sqrt(dx*dx + dy*dy);
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		//batch.setColor(Color.WHITE);
		//			batch.setColor(kingdom.currentDarkness, kingdom.currentDarkness, kingdom.currentDarkness, 1f);

		// draw BG
		battlemap.draw(batch, parentAlpha);

		super.draw(batch, parentAlpha);

		if (drawCrests) {

		}
	}

	public void addUnit(Unit unit) {
		if (unit.team == 0)
			this.allies.add(unit);
		else if (unit.team == 1)
			this.enemies.add(unit);
		this.addActor(unit);
	}

	public void removeUnit(Unit remove) {
		if (remove.team == 0)
			allies.removeValue(remove, true);
		else enemies.removeValue(remove, true);
		this.removeActor(remove);
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

	public float getZoom() {
		return getMapScreen().getCamera().zoom;
	}
	public void setMouseOver(boolean b) {
		mouseOver = b;
	}
	public boolean isPaused() {
		return paused;
	}
	public static double distBetween(Unit d1, Unit d2) {
		// TODO optimize by computing getCenter only once per
		return Math.sqrt((d1.getCenterX()-d2.getCenterX())*(d1.getCenterX()-d2.getCenterX())+(d1.getCenterY()-d2.getCenterY())*(d1.getCenterY()-d2.getCenterY()));
	}
	// should be slightly faster than above
	public static double sqDistBetween(Destination d1, Destination d2) {
		return (d1.getCenterX()-d2.getCenterX())*(d1.getCenterX()-d2.getCenterX())+(d1.getCenterY()-d2.getCenterY())*(d1.getCenterY()-d2.getCenterY());
	}


}
