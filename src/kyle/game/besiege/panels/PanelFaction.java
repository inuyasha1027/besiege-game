/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Faction;
import kyle.game.besiege.SidePanel;
import kyle.game.besiege.army.Noble;
import kyle.game.besiege.location.Castle;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.ObjectLabel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.esotericsoftware.tablelayout.Cell;

// Displays basic info about the faction including relations with all others.
/* Should contain:
 * 	Total faction wealth (sum of all cities)
 * 	List of Cities? (Should list city's villages on locationPanel then)
 * 		Clicking on cities should redirect to City Panel.
 *  Later: list of warlords (barons or whatever)
 * 	List of relations with other factions (or maybe do this on a separate panel?)
 *  	Mousing over relations should give detailed info (base of 0, military actions (-/+), nearby cities (-), trade (+)) 
 * 		
 * 
 * 
 */
public class PanelFaction extends Panel {
	private Faction faction;
	
	private final float PAD = 10;
	private final float MINI_PAD = 5;
	private final float NEG = -5;
	private final float DESC_HEIGHT = 350;
	private final int r = 3;
	private final String tablePatch = "grey-d9";
	private SidePanel panel;
	
	private Table text;
	private Label title;
	private Label wealth;
	private Table cities;
	private Table castles;
	private Table locations;
	private ScrollPane locationPane;
	private Table nobles;
	private ScrollPane noblesPane;
	private Table relations;
	private ScrollPane relationsPane;
	
//	private Table stats;
//	private Label nameS;
//	private Label levelS;
//	private Label expS;
//	private Label nextS;
//	private Label atkS;
//	private Label defS;
//	private Label spdS;
//	private Label weaponS;
//	private Label equipmentS;
		
	private LabelStyle ls;
	private LabelStyle lsG;	// wounded
	private LabelStyle lsY; // upgrade
	
	public boolean playerTouched;
	
	public PanelFaction(SidePanel panel, Faction faction) {
		this.panel = panel;
		this.faction = faction;
		this.addParentPanel(panel);
		
		LabelStyle lsBig = new LabelStyle();
		lsBig.font = Assets.pixel24;
		
		ls = new LabelStyle();
		ls.font = Assets.pixel16;
		
		lsG = new LabelStyle();
		lsG.font = Assets.pixel16;
		lsG.fontColor = Color.GRAY;
		
		lsY = new LabelStyle();
		lsY.font = Assets.pixel16;
		lsY.fontColor = Color.YELLOW;
		
		Label castlesC = new Label("Castles:", ls);
		Label wealthC = new Label("Wealth:",ls); // maybe give this a mouseover for description.

		title = new Label("", lsBig);
		title.setAlignment(0,0);
		title.setWrap(true);
		title.setWidth(SidePanel.WIDTH-PAD*2-MINI_PAD*2);
		title.setText(faction.name);
		
		wealth = new Label("", ls);
		cities = new Table();
		
		// Create text
		text = new Table();
//		text.debug();
		text.defaults().padTop(NEG).left();
		
//		title.addListener(new InputListener() {
//			public boolean touchDown(InputEvent event, float x,
//					float y, int pointer, int button) {
//				return true;
//			}
//			public void touchUp(InputEvent event, float x, float y,
//					int pointer, int button) {
//				centerCamera();
//			}
//		});
		
		text.add(title).colspan(2).fillX().expandX().padBottom(MINI_PAD);
		text.row();
		text.add().width((SidePanel.WIDTH-PAD*2)/2);
		text.add().width((SidePanel.WIDTH-PAD*2)/2);
		text.row();
		text.add(wealthC).padLeft(MINI_PAD).center();
		text.add(wealth).center();
		text.row();
		text.add().colspan(2).padTop(MINI_PAD);
		text.row();
		
		cities = new Table();
//		cities.debug();
		//cities.defaults().padTop(NEG);
		cities.top();
		cities.defaults().left().padTop(NEG).expandX();
		
		castles = new Table();
//		castles.debug();
		castles.top();
		castles.defaults().left().padTop(NEG).expandX();
		
		locations = new Table();
//		locations.debug();
		locations.top();
		locations.defaults().left().top();
		locations.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(tablePatch), r,r,r,r)));
		locations.add().width((SidePanel.WIDTH-PAD*2)/2);
		locations.add().width((SidePanel.WIDTH-PAD*2)/2);
		locations.row();
		locations.add(cities).width((SidePanel.WIDTH-PAD*2)/2);
		locations.add(castles).width((SidePanel.WIDTH-PAD*2)/2);
		locationPane = new ScrollPane(locations);
		locationPane.setScrollbarsOnTop(true);
		locationPane.setFadeScrollBars(false);
		
		text.add(locationPane).colspan(2).top().padTop(0);
		text.row();
		text.add().colspan(2).padTop(PAD);
		text.row();
		
		nobles = new Table();
//		nobles.debug();
		nobles.top();
		nobles.defaults().padTop(NEG).left();
		noblesPane = new ScrollPane(nobles);
		noblesPane.setScrollbarsOnTop(true);
		noblesPane.setFadeScrollBars(false);
		nobles.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(tablePatch), r,r,r,r)));		
		nobles.add().width((SidePanel.WIDTH-PAD*2)/2);
		nobles.add().width((SidePanel.WIDTH-PAD*2)/2);
		nobles.row();
		
		text.add(noblesPane).colspan(2);
		text.row();
		text.add().colspan(2).padTop(PAD);
		text.row();
		
		relations = new Table();
		relations.top();
		relations.defaults().padTop(NEG).left();
		relationsPane = new ScrollPane(relations);
		relationsPane.setScrollbarsOnTop(true);
		relationsPane.setFadeScrollBars(false);
		relations.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(tablePatch), r,r,r,r)));		
		
		text.add(relationsPane).colspan(2);
		text.row();
		
//		//Soldier's stats
//		stats = new Table();
//		stats.setVisible(false);
//		
//		Label levelSC = new Label("Level:", ls);
//		Label expSC = new Label("Exp:",ls);
//		Label nextSC = new Label("Next:",ls);
//		Label atkSC = new Label("Atk:", ls);
//		Label defSC = new Label("Def:", ls);
//		Label spdSC = new Label("Spd:", ls); 
//		Label weaponSC = new Label("Weapon: ", ls);
//		Label equipmentSC = new Label("Armor: ", ls);
//
//		nameS = new Label("", ls);
//		nameS.setAlignment(0,0);
//		levelS = new Label("", ls);
//		expS = new Label("", ls);
//		nextS = new Label("", ls);
//		atkS = new Label("" ,ls);
//		defS = new Label("", ls);
//		spdS = new Label("", ls);
//		weaponS = new Label("", ls);
//		equipmentS = new Label("", ls);
//		
//		stats.defaults().left().padTop(NEG);
//		stats.add(nameS).colspan(4).width(SidePanel.WIDTH-PAD*2).fillX().expandX().padBottom(MINI_PAD);
//		stats.row();
//		stats.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
//		stats.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
//		stats.row();
//		stats.add(levelSC).padLeft(MINI_PAD);
//		stats.add(levelS);
//		stats.add(atkSC).padLeft(PAD);
//		stats.add(atkS);
//		stats.row();
//		stats.add(expSC).padLeft(MINI_PAD);
//		stats.add(expS);
//		stats.add(defSC).padLeft(PAD);
//		stats.add(defS);
//		stats.row();
//		stats.add(nextSC).padLeft(MINI_PAD);
//		stats.add(nextS);
//		stats.add(spdSC).padLeft(PAD);
//		stats.add(spdS);
//		stats.row();
//		stats.add(weaponSC).colspan(2).padLeft(MINI_PAD).padTop(0);
//		stats.add(weaponS).colspan(2).padTop(0);
//		stats.row();
//		stats.add(equipmentSC).colspan(2).padLeft(MINI_PAD).padTop(0).top();
//		stats.add(equipmentS).colspan(2).padTop(0);
//		
//		//stats.debug();
//		
////		text.add(stats).colspan(4).padTop(PAD);

		this.addTopTable(text);
		
		this.setButton(4, "Back");
		updateRelations();
	}
	
	@Override
	public void act(float delta) {
		wealth.setText(faction.getTotalWealth() + "");
		updateCities();
		updateCastles();
		updateNobles();
		updateRelations();
		super.act(delta);
	}
	
	public void updateCities() {
		cities.clear(); // clearing the table is a problem right now. it hides the scroll bar and prevents click-drag scrolling
		cities.padLeft(MINI_PAD).padRight(MINI_PAD);
		cities.defaults().left();
		Label citiesC = new Label("Cities:", ls);
		cities.add(citiesC).center();
		cities.row();
		if (faction.cities.size != 0) {	
			for (City c : faction.cities) {
				ObjectLabel cityName = new ObjectLabel(c.getName(), ls, c);
				cityName.addListener(new InputListener() {
					public boolean touchDown(InputEvent event, float x,
							float y, int pointer, int button) {
						return true;
					}
					public void touchUp(InputEvent event, float x, float y,
							int pointer, int button) {
						setActiveLocation((Location) ((ObjectLabel) event.getTarget()).object);
					}
				});
				cities.add(cityName);
				cities.row();
			}
		}
		else {
			Label empty = new Label("None",ls);
			cities.add(empty).top().center();	
		}	
	}
	public void updateCastles() {
		castles.clear(); // clearing the table is a problem right now. it hides the scroll bar and prevents click-drag scrolling
		castles.padLeft(MINI_PAD).padRight(MINI_PAD);
		Label castlesC = new Label("Castles:", ls);
		castles.add(castlesC).center();
		castles.row();
		if (faction.castles.size > 0) {
			for (Castle c : faction.castles) {
				ObjectLabel castleName = new ObjectLabel(c.getName(), ls, c);
				castleName.addListener(new InputListener() {
					public boolean touchDown(InputEvent event, float x,
							float y, int pointer, int button) {
						return true;
					}
					public void touchUp(InputEvent event, float x, float y,
							int pointer, int button) {
						setActiveLocation((Location) ((ObjectLabel) event.getTarget()).object);
					}
				});
				castles.add(castleName);
				castles.row();
			}
		}
		else {
			Label empty = new Label("None",ls);
			castles.add(empty).expandY().fillY().center();	
		}	
	}
	
	public void updateNobles() {
		nobles.clear(); 
		nobles.padLeft(MINI_PAD).padRight(MINI_PAD);
		nobles.add().colspan(2).width((SidePanel.WIDTH-PAD*2));
		nobles.row();
		nobles.add(new Label("Nobles:", ls)).colspan(2).center();
		nobles.row();
		for (Noble noble : faction.nobles) {
			ObjectLabel name = new ObjectLabel(noble.getName(), ls, noble);
			name.addListener(new InputListener() {
				public boolean touchDown(InputEvent event, float x,
						float y, int pointer, int button) {
					return true;
				}
				public void touchUp(InputEvent event, float x, float y,
						int pointer, int button) {
					setActiveNoble((Noble) ((ObjectLabel) event.getTarget()).object);
				}
			});
			nobles.add(name);
			ObjectLabel troopCount = new ObjectLabel(noble.getTroopCount() +"", ls, noble);
			nobles.add(troopCount).right();
			nobles.row();
		}
	}
	
	public void updateRelations() {
		relations.clear(); 
		relations.padLeft(MINI_PAD).padRight(MINI_PAD);
		relations.add().colspan(2).width((SidePanel.WIDTH-PAD*2));
		relations.row();
		relations.add(new Label("Faction Relations:", ls)).colspan(2).center();
		relations.row();
		for (Faction f : Faction.factions) {
			if (!(f == faction) && f.index != 0 && f.index != 1) {
				ObjectLabel name = new ObjectLabel(f.name, ls, f);
				name.addListener(new InputListener() {
					public boolean touchDown(InputEvent event, float x,
							float y, int pointer, int button) {
						return true;
					}
					public void touchUp(InputEvent event, float x, float y,
							int pointer, int button) {
						setActiveFaction((Faction) ((ObjectLabel) event.getTarget()).object);
					}
				});
				relations.add(name);
				ObjectLabel relation = new ObjectLabel(f.getRelationsWith(faction) +"", ls, f);
				relations.add(relation).right();
				relations.row();
			}
		}
	}

//	public void setStats(Soldier s) {
//		stats.setVisible(true);
//		nameS.setText(s.name + "");
//		levelS.setText(s.level + "");
//		expS.setText(s.exp + "");
//		nextS.setText(s.next + "");
//		if (s.bonusAtk >= 0)
//			atkS.setText(s.atk + " (" + s.baseAtk + "+" + s.bonusAtk + ")");
//		else 
//			atkS.setText(s.atk + " (" + s.baseAtk + s.bonusAtk + ")");
//		if (s.bonusDef >= 0)
//			defS.setText(s.def + " (" + s.baseDef + "+" + s.bonusDef + ")");
//		else 
//			defS.setText(s.def + " (" + s.baseDef + s.bonusDef + ")");
//		if (s.bonusSpd >= 0)
//			spdS.setText(s.spd + " (" + s.baseSpd + "+" + s.bonusSpd + ")");
//		else 
//			spdS.setText(s.spd + " (" + s.baseSpd + s.bonusSpd + ")");
//		weaponS.setText(s.weapon.name);
//		equipmentS.setText(s.equipmentList());
//	}
	
//	public void clearStats() {
//		stats.setVisible(false);
//	}
	public void setActiveLocation(Location location) {
		panel.setActiveLocation(location);
	}
	public void setActiveNoble(Noble noble) {
		panel.setActiveArmy(noble);
	}
	public void setActiveFaction(Faction faction) {
		panel.setActiveFaction(faction);
	}
	
	@Override
	public void resize() { // problem with getting scroll bar to appear...
		Cell cell = text.getCell(locationPane);
		cell.height((panel.getHeight() - DESC_HEIGHT)/3).setWidget(null);
//		locationPane = new ScrollPane(locations);
		locationPane.setHeight((panel.getHeight() - DESC_HEIGHT)/3);
//		locationPane.setScrollingDisabled(true, false);
//		locationPane.setFadeScrollBars(false); 
//		locationPane.setScrollbarsOnTop(true);
		cell.setWidget(locationPane);
		
		cell = text.getCell(noblesPane);
		cell.height((panel.getHeight() - DESC_HEIGHT)/3).setWidget(null);
//		relationsPane = new ScrollPane(relations);
		noblesPane.setHeight((panel.getHeight() - DESC_HEIGHT)/3);
//		relationsPane.setScrollingDisabled(true, false);
//		relationsPane.setFadeScrollBars(false);
//		relationsPane.setScrollbarsOnTop(true);
		cell.setWidget(noblesPane);
		
		cell = text.getCell(relationsPane);
		cell.height((panel.getHeight() - DESC_HEIGHT)/3).setWidget(null);
//		relationsPane = new ScrollPane(relations);
		relationsPane.setHeight((panel.getHeight() - DESC_HEIGHT)/3);
//		relationsPane.setScrollingDisabled(true, false);
//		relationsPane.setFadeScrollBars(false);
//		relationsPane.setScrollbarsOnTop(true);
		cell.setWidget(relationsPane);
		super.resize();
	}
	
	@Override
	public void button1() {
		
	}
	@Override
	public void button2() {
		
	}
	@Override
	public void button3() {
		
	}
	@Override
	public void button4() {
		panel.returnToPrevious();
	}
	
	@Override
	public TextureRegion getCrest() {
		return faction.crest;
	}
}

