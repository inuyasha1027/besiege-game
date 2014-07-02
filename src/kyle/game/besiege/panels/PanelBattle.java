/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Character;
import kyle.game.besiege.MiniMap;
import kyle.game.besiege.SidePanel;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.army.ArmyPlayer;
import kyle.game.besiege.battle.Battle;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.SoldierLabel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.Cell;

public class PanelBattle extends Panel { // TODO organize soldier display to consolidate same-type soldiers
	private final float PAD = 10;
	private final float MINI_PAD = 5;
	private final float NEG = -5;
	private final float DESC_HEIGHT = 540;
	private final float VS_WIDTH = 30;
	private final int r = 3;
	private final String tablePatch = "grey-d9";
	private final String redPatch = "red9";
	private final String greenPatch = "green9";
	private SidePanel panel;
	private Battle battle;
	
	private Label title;
	
	private Table text;
	private Label attackers;
	private Label defenders;
	private Label initA;
	private Label initD;
	private Label trpsA;
	private Label trpsD;
	private Label atkA;
	private Label atkD;
	private Label defA;
	private Label defD;
//	private Label advA;
//	private Label advD;
	private Table balance;
	private Table red;
	private Table green;
//	private Label def;
//	private Label spd;
	
	private Table stats;
	private Label nameS;
	private Label levelS;
	private Label expS;
	private Label nextS;
	private Label atkS;
	private Label defS;
	private Label spdS;
	private Label weaponS;
	private Label equipmentS;
	
	private Table soldierTable;
	private Table aTable;
	private Table dTable;
	private Table aRetTable;
	private Table dRetTable;
	private ScrollPane soldierPane;
	
	private LabelStyle ls;
	private LabelStyle lsRetreat;
	private LabelStyle lsSmall;
	private LabelStyle lsSmallG;
	
	public PanelBattle(SidePanel panel, Battle battle) {
		this.panel = panel;
		this.battle = battle;
		this.addParentPanel(panel);
		
		LabelStyle lsBig = new LabelStyle();
		lsBig.font = Assets.pixel24;
		
		LabelStyle lsFaction = new LabelStyle();
		lsFaction.font = Assets.pixel16neg;
		
		ls = new LabelStyle();
		ls.font = Assets.pixel16neg;
		
		lsRetreat = new LabelStyle();
		lsRetreat.font = Assets.pixel13neg;
		lsRetreat.fontColor = Color.YELLOW;
		
		lsSmall = new LabelStyle();
		lsSmall.font = Assets.pixel13neg;
		
		lsSmallG = new LabelStyle();
		lsSmallG.font = Assets.pixel13neg;
		lsSmallG.fontColor = Color.GRAY;
		
		title = new Label("Battle!",lsBig);
//		Label vsC = new Label("vs", ls);
		Label trpsAC = new Label("Trps:", ls);
		Label trpsDC = new Label("Trps:", ls);
		Label atkAC = new Label("Atk:", ls);
		Label atkDC = new Label("Atk:", ls);
		Label defAC = new Label("Def:", ls);
		Label defDC = new Label("Def:", ls);
		Label advAC = new Label("Adv:", ls); 
		Label advDC = new Label("Adv:", ls);

		title.setAlignment(0,0);
		title.setWrap(true);
		title.setWidth(SidePanel.WIDTH-PAD*2-MINI_PAD*2);
		initA = new Label("", lsFaction);
		initD = new Label("", lsFaction);
		initA.setAlignment(Align.center);
		initD.setAlignment(Align.center);
		initA.setWrap(true);
		initD.setWrap(true);

		if (battle.aArmies.size > 0 && battle.aArmies.first() != null)
			initA.setText(battle.aArmies.first().getName());
		if (battle.dArmies.size > 0 && battle.dArmies.first() != null)
			initD.setText(battle.dArmies.first().getName());

		attackers = new Label("",ls);
		defenders = new Label("",ls);
		trpsA = new Label("", ls);
		trpsD = new Label("" ,ls);
		trpsA.setWrap(true);
		trpsD.setWrap(true);
		atkA = new Label("", ls);
		atkD = new Label("", ls);
		defA = new Label("", ls);
		defD = new Label("", ls);
//		advA = new Label("", ls);
//		advD = new Label("" ,ls);
		
		balance = new Table();
		red = new Table();
		red.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(redPatch), r,r,r,r)));
		green = new Table();
		green.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(greenPatch), r,r,r,r)));
//		def = new Label("", ls);
//		spd = new Label("", ls);
		
		// Create text
		text = new Table();
		//text.debug();
		text.defaults().padTop(NEG).left();
		
		text.add(title).colspan(4).fillX().expandX().padBottom(MINI_PAD);
		text.row();
		text.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
		text.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
//		text.row();
//		text.add(vsC).colspan(4).center();
		text.row();
		text.add(initA).center().colspan(2).expandX().width((SidePanel.WIDTH - PAD*2-MINI_PAD)/2);
		text.add(initD).center().colspan(2).expandX().width((SidePanel.WIDTH - PAD*2-MINI_PAD)/2);
		text.row();
		text.add(attackers).center().colspan(2);
		text.add(defenders).center().colspan(2);
		text.row();
		text.add(trpsAC).padLeft(MINI_PAD);
		text.add(trpsA).left();
		text.add(trpsDC);
		text.add(trpsD).left();
		text.row();
		text.add(atkAC).padLeft(MINI_PAD);
		text.add(atkA).left();
		text.add(atkDC);
		text.add(atkD).left();
		text.row();
		text.add(defAC).padLeft(MINI_PAD);
		text.add(defA).left();
		text.add(defDC);
		text.add(defD).left();
		text.row();
//		text.add(advAC).padLeft(MINI_PAD);
//		text.add(advA).left();
//		text.add(advDC);
//		text.add(advD).left();
		text.add(balance).colspan(4).padTop(MINI_PAD);
		text.row();
		
		Table leftTable = new Table();
		Table rightTable = new Table();
		aTable = new Table();
		dTable = new Table();
		aTable.defaults().left().padTop(NEG).top().left();
		dTable.defaults().right().padTop(NEG).top().right();
		aTable.top();
		dTable.top();
		aRetTable = new Table();
		dRetTable = new Table();
		aRetTable.defaults().left().padTop(NEG).top().left();
		dRetTable.defaults().right().padTop(NEG).top().right();
		leftTable.add(aTable);
		leftTable.row();
		leftTable.add(aRetTable);
		leftTable.top();
		rightTable.add(dTable);
		rightTable.row();
		rightTable.add(dRetTable);
		rightTable.top();
		
		//aTable.debug();
		//dTable.debug();
		
		soldierTable = new Table();

		//soldierTable.debug();
		
		soldierTable.top();
		soldierTable.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(tablePatch), r,r,r,r)));
		soldierTable.add().width(SidePanel.WIDTH-2*PAD).colspan(2);
		soldierTable.row();
		soldierTable.add(leftTable).top();
		soldierTable.add(rightTable).top();
		text.add().colspan(4).padBottom(PAD);
		text.row();
		
		soldierPane = new ScrollPane(soldierTable);
		soldierPane.setScrollbarsOnTop(true);
		soldierPane.setFadeScrollBars(false);
		text.add(soldierPane).colspan(4).top().padTop(0).expand().fill();
		
		text.row();
		
		
		
		// Soldier's stats
		stats = new Table();
		stats.setVisible(false);
		
		Label levelSC = new Label("Level:", ls);
		Label expSC = new Label("Exp:",ls);
		Label nextSC = new Label("Next:",ls);
		Label atkSC = new Label("Atk:", ls);
		Label defSC = new Label("Def:", ls);
		Label spdSC = new Label("Spd:", ls); 
		Label weaponSC = new Label("Weapon: ", ls);
		Label equipmentSC = new Label("Armor: ", ls);

		nameS = new Label("", ls);
		nameS.setAlignment(0,0);
		levelS = new Label("", ls);
		expS = new Label("", ls);
		nextS = new Label("", ls);
		atkS = new Label("" ,ls);
		defS = new Label("", ls);
		spdS = new Label("", ls);
		weaponS = new Label("", ls);
		equipmentS = new Label("", ls);
		
		stats.defaults().left().padTop(NEG);
		stats.add(nameS).colspan(4).width(SidePanel.WIDTH-PAD*2).fillX().expandX().padBottom(MINI_PAD);
		stats.row();
		stats.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
		stats.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
		stats.row();
		stats.add(levelSC);
		stats.add(levelS);
		stats.add(atkSC).padLeft(PAD);
		stats.add(atkS);
		stats.row();
		stats.add(expSC);
		stats.add(expS);
		stats.add(defSC).padLeft(PAD);
		stats.add(defS);
		stats.row();
		stats.add(nextSC);
		stats.add(nextS);
		stats.add(spdSC).padLeft(PAD);
		stats.add(spdS);
		stats.row();
		stats.add(weaponSC).colspan(2).padLeft(MINI_PAD).padTop(0);
		stats.add(weaponS).colspan(2).padTop(0);
		stats.row();
		stats.add(equipmentSC).colspan(2).padLeft(MINI_PAD).padTop(0).top();
		stats.add(equipmentS).colspan(2).padTop(0);
		
		//stats.debug();
		
		text.add(stats).colspan(4).padTop(PAD);

		this.addTopTable(text);
		
		this.setButton(1, "Retreat!");
	}
	
	// TODO this is extremely memory inefficient. like, it's really bad.
	@Override
	public void act(float delta) {
//		System.out.println("acting " + delta + " is over: " + battle.isOver);
		title.setText("Battle!");
		if (battle.isOver) {
			if (!battle.didAtkWin) {
//				System.out.println("def won");
				soldierTable.clear();
				trpsA.setText("0");
				atkA.setText("0");
				defA.setText("0");
				battle.balanceD = 1;
				battle.balanceA = 0;
			}
			else {
//				System.out.println("atk won");
				soldierTable.clear();
				trpsD.setText("0");
				atkD.setText("0");
				defD.setText("0");
				battle.balanceA = 1;
				battle.balanceD = 0;
			}
			balance.clear();
			float totalWidth = SidePanel.WIDTH - PAD;
			balance.add(red).width((float) (totalWidth*battle.balanceA));
			balance.add(green).width((float) (totalWidth*battle.balanceD));
		}
		else {
			boolean shouldUpdate = false;
			for (Army army : battle.aArmies)
				if (army.getParty().updated) {
					shouldUpdate = true;
					army.getParty().updated = false;
				}
//			if (!shouldUpdate)
				for (Army army : battle.dArmies)
					if (army.getParty().updated) {
						shouldUpdate = true;
						army.getParty().updated = false;
					}
//			if (!shouldUpdate)
				for (Army army : battle.aArmiesRet)
					if (army.getParty().updated) {
						shouldUpdate = true;
						army.getParty().updated = false;
					}
//			if (!shouldUpdate)
				for (Army army : battle.dArmiesRet)
					if (army.getParty().updated) {
						shouldUpdate = true;
						army.getParty().updated = false;
					}
			if (shouldUpdate) {
//				System.out.println("updated soldier table");
				updateTopTable();
				updateSoldierTable();
			}
		}
		super.act(delta);
	}
	
	// modularize this please
	public void updateTopTable() {
		boolean aAllies = false;
		boolean dAllies = false;
		for (Army a : battle.aArmies)
			if (a.getFaction() != battle.aArmies.first().getFaction()) aAllies = true;
		for (Army d : battle.dArmies)
			if (d.getFaction() != battle.dArmies.first().getFaction()) dAllies = true;
		
		if (battle.aArmies.size >= 1) {
			if (aAllies)
				attackers.setText(battle.aArmies.first().getFactionName() + " and allies");
			else attackers.setText(battle.aArmies.first().getFactionName());

			String trpsStrA = battle.aArmies.first().getParty().getHealthySize() + "";
			for (Army a: battle.aArmies) {
				if (a != battle.aArmies.first())
					trpsStrA += "+" + a.getParty().getHealthySize();
			}
			trpsA.setText(trpsStrA);
			
			String defStrA = Panel.format(""+ battle.aArmies.first().getParty().getAvgDef(), 2);
			for (Army a: battle.aArmies) {
				if (a != battle.aArmies.first())
					 defStrA = Panel.format(""+ a.getParty().getAvgDef(), 2);
			}
			defA.setText(defStrA);
		}
		else if (battle.aArmiesRet.size >= 1) {
			trpsA.setText(""+battle.aArmiesRet.first().getParty().getHealthySize());
		}
		if (battle.dArmies.size >= 1) {
			if (dAllies)
				defenders.setText(battle.dArmies.first().getFactionName() + " and allies");
			else defenders.setText(battle.dArmies.first().getFactionName());

			String trpsStrD = battle.dArmies.first().getParty().getHealthySize() + "";
			for (Army a: battle.dArmies) {
				if (a != battle.dArmies.first())
					trpsStrD += "+" + a.getParty().getHealthySize();
			}
			trpsD.setText(trpsStrD);
			
			String defStrD = Panel.format(""+battle.dArmies.first().getParty().getAvgDef(), 2);
			for (Army a: battle.dArmies) {
				if (a != battle.dArmies.first())
					defStrD += "\n" + Panel.format(""+a.getParty().getAvgDef(), 2);			
			}
			defD.setText(defStrD);
		}
		else if (battle.dArmiesRet.size >= 1) {
			trpsD.setText(""+battle.dArmiesRet.first().getParty().getHealthySize());
		}
		
		atkA.setText(battle.aAtk + "");
		atkD.setText(battle.dAtk + "");
		
		balance.clear();
		float totalWidth = SidePanel.WIDTH - PAD;
		balance.add(red).width((float) (totalWidth*battle.balanceA));
		balance.add(green).width((float) (totalWidth*battle.balanceD));
	}
	
	public void updateSoldierTable() {
		updateTable(aTable, battle.aArmies);
		updateTable(dTable, battle.dArmies);
//		if (battle.aArmiesRet.size >= 1)
			updateTable(aRetTable, battle.aArmiesRet);
//		if (battle.dArmiesRet.size >= 1)
			updateTable(dRetTable, battle.dArmiesRet);
	}
	
	public void updateTable(Table table, Array<Army> armies) {
		table.clear();
		boolean retreat = false;
		if (table == aRetTable || table == dRetTable)
			retreat = true;
		
		for (Army a : armies) {
			Party party = a.getParty();
			Label partyName = new Label(a.getName(),ls);
			partyName.setWrap(true);
			if (table == dTable || table == dRetTable)
				partyName.setAlignment(Align.right);
			table.add(partyName).width(SidePanel.WIDTH/2 - PAD*2).padBottom(0);
			table.row();
			if (retreat) {
				Label retreating = new Label("Retreating: " + ((int) a.retreatCounter + 1), lsRetreat);
				retreating.setAlignment(0,0);
				table.add(retreating).padBottom(0);
				table.row();
			}
			
			
			// do consolidated view
			
			
//			for (Array<Soldier> type : types) {
//				Label name = new Label(type.first().name, lsSmall);
//				table.add(name).left();
//				Label count = new Label(type.size + "", lsSmall);
//				table.add(count).right();
//				table.row();
//			}
			
			Array<Array<Soldier>> consolHealthy = party.getConsolHealthy();
			
			for (Array<Soldier> as : consolHealthy) {
				Label name = new Label(as.first().name, lsSmall);
				
				name.setWrap(true);
//				name.addListener(new ClickListener() {
//					public void enter(InputEvent event, float x,
//							float y, int pointer, Actor fromActor) {
//						setStats(((SoldierLabel) event.getTarget()).soldier);
//					}
//					public void exit(InputEvent event, float x, float y,
//							int pointer, Actor fromActor) {
//						clearStats();
//					}
//				});
				table.add(name).width(table.getWidth()*3/4);
				Label count = new Label(as.size +"", lsSmall);
				table.add(count).right();
				table.row();
			}
			
			Array<Array<Soldier>> consolWounded = party.getConsolWounded();
			
			for (Array<Soldier> as : consolWounded) {
				Label name = new Label(as.first().name, lsSmallG);
				
				name.setWrap(true);
//				name.addListener(new ClickListener() {
//					public void enter(InputEvent event, float x,
//							float y, int pointer, Actor fromActor) {
//						setStats(((SoldierLabel) event.getTarget()).soldier);
//					}
//					public void exit(InputEvent event, float x, float y,
//							int pointer, Actor fromActor) {
//						clearStats();
//					}
//				});
				table.add(name).width(table.getWidth()*3/4);
				Label count = new Label(as.size +"", lsSmallG);
				table.add(count).right();
				table.row();
			}
			
//			Label woundedC;
//			if (party.getWounded().size > 0)
//				woundedC = new Label("Wounded", lsSmall);
//			else woundedC = new Label("",lsSmall);
//			woundedC.setAlignment(0,0);
//			table.add(woundedC).padTop(0);
//			table.row();
			
			
//			for (Soldier s : party.getWounded()) {
//				SoldierLabel name = new SoldierLabel(s.name, lsSmallG, s);
//				if (table == dTable)
//					name.setAlignment(Align.right);
//				name.setWrap(true);
////				name.addListener(new ClickListener() {
////					public void enter(InputEvent event, float x,
////							float y, int pointer, Actor fromActor) {
////						setStats(((SoldierLabel) event.getTarget()).soldier);
////					}
////					public void exit(InputEvent event, float x, float y,
////							int pointer, Actor fromActor) {
////						clearStats();
////					}
////				});
//				table.add(name).width(table.getWidth()*3/4);
//				Label count = new Label(s.level +"", lsSmall);
//				table.add(count).right();
//				table.row();
//			}
			table.add().padBottom(PAD);
			table.row();
		}
//		table.add().padTop(PAD*8);
		table.row(); // needed because scroll pane doesn't go all the way to the bottom... idk why
	}
	public void setStats(Soldier s) {
		stats.setVisible(true);
		nameS.setText(s.name + "");
		levelS.setText(s.level + "");
		expS.setText(s.exp + "");
		nextS.setText(s.next + "");
		if (s.bonusAtk >= 0)
			atkS.setText(s.getAtk() + " (" + s.baseAtk + "+" + s.bonusAtk + ")");
		else 
			atkS.setText(s.getAtk() + " (" + s.baseAtk + s.bonusAtk + ")");
		if (s.bonusDef >= 0)
			defS.setText(s.getDef() + " (" + s.baseDef + "+" + s.bonusDef + ")");
		else 
			defS.setText(s.getDef() + " (" + s.baseDef + s.bonusDef + ")");
		if (s.bonusSpd >= 0)
			spdS.setText(s.getSpd() + " (" + s.baseSpd + "+" + s.bonusSpd + ")");
		else 
			spdS.setText(s.getSpd() + " (" + s.baseSpd + s.bonusSpd + ")");
		weaponS.setText(s.weapon.name);
//		equipmentS.setText(s.equipmentList());
	}
	
	public void clearStats() {
		stats.setVisible(false);
	}
	
	@Override
	public void resize() { // problem with getting scroll bar to appear...
		Cell cell = text.getCell(soldierPane);
		cell.height(panel.getHeight() - DESC_HEIGHT).setWidget(null);
		soldierPane = new ScrollPane(soldierTable);
		soldierPane.setHeight(panel.getHeight() - DESC_HEIGHT);
		soldierPane.setScrollingDisabled(true, false);
		soldierPane.setFadeScrollBars(false);
		soldierPane.setScrollbarsOnTop(true);
		cell.setWidget(soldierPane);
		super.resize();
	}
	
	@Override
	public void button1() {
		//retreat button
		if (getButton(1).isVisible()) {
			battle.retreat(panel.getKingdom().getPlayer());
			getButton(1).setVisible(false);
		}
	}
	@Override
	public void button2() {
		BottomPanel.log("b2");
	}
	@Override
	public void button3() {
		
	}
	@Override
	public void button4() {
//		if (party == panel.getKingdom().getPlayer().getParty())
			panel.setDefault();
//		else 
//			panel.returnToPrevious();
	}
}
