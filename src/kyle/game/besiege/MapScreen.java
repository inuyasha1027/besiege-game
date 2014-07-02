/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
//contains camera, controls and stage for main actor: Kingdom

package kyle.game.besiege;


import java.io.PrintWriter;

import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.voronoi.VoronoiGraph;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class MapScreen implements Screen {
	private final float SCROLL_SPEED = 3f;
	private final float FAST_FORWARD_FACTOR = 3f;
	private final float ZOOM_MAX = 10;
	private final float ZOOM_MIN = .05f;
//	private final Color background = new Color(0, 109.0f/255, 185.0f/255, 1);
//	private final Color background = new Color(VoronoiGraph.OCEAN);
	private final Color background = new Color(0x44447aff);
	private float speedFactor;
	private OrthographicCamera camera;
	private OrthographicCamera minimap;
	public float rotation;
	private Stage kingdomStage;
	private Stage uiStage;
	private Kingdom kingdom;
	private Fog fog;
	private MapControllerDesktop mapControllerDesktop;
	private MapControllerAndroid mapControllerAndroid;
	private Vector2 mousePos;
	
	private Character character;
	
	private SidePanel sidePanel;
	private boolean mouseOverPanel;
	
	public boolean fastForward;
	public boolean shouldFastForward;
	public boolean superFastForward;
	public boolean shouldSuperFastForward;
	public boolean shouldLetRun; // for "waiting" and "resting", in location
	public boolean shouldCenter;

	private int keydown;
	public boolean fogOn;
	public boolean losOn;
	public boolean editOn; // "map editor" for placing cities
	private boolean fogToggle;
	private boolean losToggle;
	private boolean editToggle;
	private boolean nightToggle;
	
	private boolean territoryToggle;
	private boolean crestsToggle;
	private boolean armyCrestsToggle;
	private boolean debugToggle;
	
//	public PrintWriter out; // accessed by kingdom
	
	public MapScreen() {
		camera = new OrthographicCamera(BesiegeMain.WIDTH, BesiegeMain.HEIGHT);
		
		SpriteBatch kingdomBatch = new SpriteBatch(5460); // optimizes swapBuffers
		kingdomStage = new Stage();
		kingdomStage = new Stage(0, 0, false, kingdomBatch);
		kingdomStage.setCamera(camera);
		uiStage = new Stage();
		uiStage.addListener(new InputListener());
		sidePanel = new SidePanel(this);

		kingdom = new Kingdom(this);
		sidePanel.setKingdom(kingdom);
//		fog = new Fog(this);
		
		mapControllerAndroid = new MapControllerAndroid(camera, this);
		mapControllerDesktop = new MapControllerDesktop(camera, this);
		kingdomStage.addActor(kingdom);
//		kingdomStage.addActor(fog); // test to see if this is slowing things down
		mousePos = new Vector2(0,0);
		rotation = 0;
		speedFactor = 1;
		
		// if commented out, no more leaks!
		// leak is in SidePanel.act
		uiStage.addActor(sidePanel);
		mouseOverPanel = false;
		keydown = 0;
		
		character = new Character(this, "Kyle");
		kingdom.addPlayer();
		sidePanel.initializePanels();
		
		shouldCenter = false;
		shouldFastForward = false;
		shouldLetRun = false;
		
		fogOn = false;
		losOn = true;
		fogToggle = false;
		losToggle = true;
		
		startLog();
	}
	
	private void startLog() {
		BottomPanel.log("Welcome to Besiege! This is the pre-alpha release, mostly for testing the game engine. Enjoy!", "green");
		BottomPanel.log("Controls: ", "orange");
		BottomPanel.log("Move: left-click       Pan camera: WASD       Rotate camera: Q,E       Zoom: mouse wheel       Wait: hold space", "orange");
		BottomPanel.log("View map info: g       View factions: t         8x Speed: hold f          Toggle Line of Sight: l", "orange");
		BottomPanel.log("Check out the source code at github.com/kdhillon/besiege-game", "yellow");
	}

	@Override
	public void resize(int width, int height) {
		BesiegeMain.HEIGHT = height;
		BesiegeMain.WIDTH = width;
		kingdomStage.setViewport(BesiegeMain.WIDTH, BesiegeMain.HEIGHT, false);	
		uiStage.setViewport(BesiegeMain.WIDTH, BesiegeMain.HEIGHT, false);
//		BottomPanel.log("resizing to " + width + ", " + height, "white");
//		Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
//        Gdx.gl.glEnable(GL10.GL_BLEND);
        center();
	}
	
	@Override
	public void render(float delta) {
		handleInput();
		
		// keys for preventing slow display with SwapBuffers taking a 
		// long time: disable vsync. maybe a better solution somewhere.
		Gdx.gl.glClearColor(background.r*kingdom.currentDarkness, background.g*kingdom.currentDarkness, 
				background.b*kingdom.currentDarkness, background.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if (superFastForward) { // experimental 16x speed!
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
		}
		if (fastForward) { // x8 speed (naive approach)!!
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			kingdomStage.act(delta);
			uiStage.act(delta);
			
			// doesn't work
//			kingdomStage.act(8*delta);
//			uiStage.act(8*delta);
		}
		else {
			
			kingdomStage.act(delta);
			uiStage.act(delta);
		}
		
		
		kingdomStage.draw();
		uiStage.draw();
		
		//if (shouldCenter && !kingdom.isPaused()) center(); // maybe should be in kingdom
		if (shouldCenter) center(); // maybe should be in kingdom
	}
	
	public void moveUp() {
		camera.translate(camera.up.scale(SCROLL_SPEED*speedFactor*camera.zoom, SCROLL_SPEED*speedFactor*camera.zoom, 0));
		camera.up.scale(1/(SCROLL_SPEED*speedFactor)/camera.zoom, 1/(SCROLL_SPEED*speedFactor)/camera.zoom, 0);
	}
	public void moveDown() {
		camera.translate(camera.up.scale(-SCROLL_SPEED*speedFactor*camera.zoom, -SCROLL_SPEED*speedFactor*camera.zoom, 0));
		camera.up.scale(-1/(SCROLL_SPEED*speedFactor)/camera.zoom, -1/(SCROLL_SPEED*speedFactor)/camera.zoom, 0);
	}
	public void moveLeft() {
		camera.translate(camera.up.rotate(90, 0, 0, 1).scale(SCROLL_SPEED*speedFactor*camera.zoom, SCROLL_SPEED*speedFactor*camera.zoom,0));
		camera.up.scale(1/(SCROLL_SPEED*speedFactor)/camera.zoom, 1/(SCROLL_SPEED*speedFactor)/camera.zoom, 0).rotate(-90, 0, 0, 1);
	}
	public void moveRight() {
		camera.translate(camera.up.rotate(-90, 0, 0, 1).scale(SCROLL_SPEED*speedFactor*camera.zoom,SCROLL_SPEED*speedFactor*camera.zoom,0));
		camera.up.scale(1/(SCROLL_SPEED*speedFactor)/camera.zoom, 1/(SCROLL_SPEED*speedFactor)/camera.zoom, 0).rotate(90, 0, 0, 1);
	}
	public void rotate(float factor) {
		rotation += factor;
		camera.rotate(factor, 0, 0, 1);
	}
	public void zoom(float factor) {
		if ((factor > 0) && (camera.zoom < ZOOM_MAX) || (factor < 0) && (camera.zoom > ZOOM_MIN))
			camera.zoom += factor;
	}
	public void letRun() {
		kingdom.setPaused(false);
		kingdom.getPlayer().setWaiting(true);	
	}
	public void endRun() {
		kingdom.getPlayer().setWaiting(false);
		kingdom.setPaused(true);
	}
	public void click(int pointer) {
		kingdom.click(pointer);
	}
	public void center() {
		camera.translate(new Vector2(kingdom.getPlayer().getCenterX()-camera.position.x, kingdom.getPlayer().getCenterY()-camera.position.y));
	}
	public void handleInput() {
		mousePos.x = Gdx.input.getX();
		mousePos.y = Gdx.input.getY();
//		BottomPanel.log("mouse at " + mousePos.x + ", " + mousePos.y);
		
		if (mousePos.x > BesiegeMain.WIDTH-SidePanel.WIDTH || mousePos.y > BesiegeMain.HEIGHT - BottomPanel.HEIGHT) {
			mouseOverPanel = true;
			Gdx.input.setInputProcessor(uiStage);
		}
		else {
			if (Gdx.app.getType() == ApplicationType.Desktop)
				Gdx.input.setInputProcessor(mapControllerDesktop); // formerly mapControllerDesktop
			else if (Gdx.app.getType() == ApplicationType.WebGL)
				Gdx.input.setInputProcessor(mapControllerDesktop);
			else if (Gdx.app.getType() == ApplicationType.Android && Gdx.input.getInputProcessor() == uiStage)
				Gdx.input.setInputProcessor(new GestureDetector(mapControllerAndroid));
			mouseOverPanel = false;
		}
		kingdom.setMouseOver(!mouseOverPanel);
		
		kingdomStage.screenToStageCoordinates(mousePos);
		kingdom.setMouse(mousePos);
		
		if (Gdx.app.getType() == ApplicationType.Desktop || Gdx.app.getType() == ApplicationType.WebGL) {
			// this will update camera position on desktop!
			if (Gdx.input.isKeyPressed(Keys.W)) {
				moveUp();
				shouldCenter = false;
			}
			if (Gdx.input.isKeyPressed(Keys.S)) {
				moveDown();
				shouldCenter = false;
			}
			if (Gdx.input.isKeyPressed(Keys.D)) {
				moveRight();
				shouldCenter = false;
			}
			if (Gdx.input.isKeyPressed(Keys.A)) {
				moveLeft();
				shouldCenter = false;
			}
			
			if (Gdx.input.isKeyPressed(Keys.Q)) 
				rotate(.5f);
			if (Gdx.input.isKeyPressed(Keys.E)) 
				rotate(-.5f);
			
			if (Gdx.input.isKeyPressed(Keys.M))
				zoom(.03f);
			if (Gdx.input.isKeyPressed(Keys.N))
				zoom(-.03f);
			
			if (Gdx.input.isKeyPressed(Keys.SPACE) || (kingdom.getPlayer().isWaiting() && kingdom.getPlayer().forceWait) || kingdom.getPlayer().isInBattle() || (shouldLetRun && (kingdom.getPlayer().isGarrisoned() || kingdom.getPlayer().isInSiege()))) {
				letRun();
//				System.out.println(kingdom.getPlayer().isWaiting());
			}
			else if (!kingdom.getPlayer().forceWait && kingdom.getPlayer().isWaiting()) {
//				System.out.println("ending run");
				endRun();
			}
			if ((Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)|| Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)) && speedFactor != FAST_FORWARD_FACTOR)
				speedFactor = FAST_FORWARD_FACTOR;
			else if (speedFactor != 1)
				speedFactor = 1;
			if (Gdx.input.isKeyPressed(Keys.C)) {
				shouldCenter = true;
			}
			
//			if (!editOn) {
//				if (Gdx.input.isButtonPressed(0) && !kingdom.getPlayer().isGarrisoned()) // allows holding down mouse.
//					click(0);
//			}
				
			
			if (Gdx.input.justTouched()) {
				if (!editOn) {
					if (Gdx.input.isButtonPressed(0)) {
						click(0);
					}
					else if (Gdx.input.isButtonPressed(1)) {
						click(1);
					}
				}
				else {
					click(4); // arbitrary number to places cities
				}
			}
			
			if (Gdx.input.isKeyPressed(Keys.F) || shouldFastForward)
				fastForward = true;
			else if (fastForward)
				fastForward = false;
			
			if (Gdx.input.isKeyPressed(Keys.P) || shouldSuperFastForward)
				superFastForward = true;
			else if (superFastForward)
				superFastForward = false;
			
			if (Gdx.input.isKeyPressed(Keys.O))
				fogToggle = true;
			else if (fogToggle) {
				toggleFog();
				fogToggle = false;
			}
			if (Gdx.input.isKeyPressed(Keys.L))
				losToggle = true;
			else if (losToggle) {
				toggleLOS();
				losToggle = false;
			}
			if (Gdx.input.isKeyPressed(Keys.E))
				editToggle = true;
			else if (editToggle) {
				toggleEdit();
				editToggle = false;
			}
			if (Gdx.input.isKeyPressed(Keys.B))
				nightToggle = true;
			else if (nightToggle) {
				kingdom.toggleNight();
				nightToggle = false;
			}
			if (Gdx.input.isKeyPressed(Keys.T))
				territoryToggle = true;
			else if (territoryToggle) {
				Map.drawSpheres = !Map.drawSpheres;
				territoryToggle = false;
			}
			if (Gdx.input.isKeyPressed(Keys.G))
				debugToggle = true;
			else if (debugToggle) {
				Map.debug = !Map.debug;
				debugToggle = false;
			}
			
			if (Gdx.input.isKeyPressed(Keys.Y))
				crestsToggle = true;
			else if (crestsToggle) {
				Kingdom.drawCrests = !Kingdom.drawCrests;
				crestsToggle = false;
			}
			if (Gdx.input.isKeyPressed(Keys.U))
				armyCrestsToggle = true;
			else if (armyCrestsToggle) {
				Kingdom.drawArmyCrests = !Kingdom.drawArmyCrests;
				armyCrestsToggle = false;
			}
			
//			if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
//				sidePanel.setActiveArmy(kingdom.getPlayer());
//			}
			if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
				Gdx.app.exit();
			}
			
			if (keydown != 0) {
				if (keydown == 1) {
					if (!Gdx.input.isKeyPressed(Keys.NUM_1)) {
						sidePanel.press(1);
						keydown = 0;
					}
				}
				else if (keydown == 2) {
					if (!Gdx.input.isKeyPressed(Keys.NUM_2)) {
						sidePanel.press(2);
						keydown = 0;
					}
				}
				else if (keydown == 3) {
					if (!Gdx.input.isKeyPressed(Keys.NUM_3)) {
						sidePanel.press(3);
						keydown = 0;
					}
				}
				else if (keydown == 4) {
					if (!Gdx.input.isKeyPressed(Keys.NUM_4) && !Gdx.input.isKeyPressed(Keys.BACKSPACE)) {
						sidePanel.press(4);
						keydown = 0;
					}
				}
			}
			else {
				//bootleg key-up
				if (Gdx.input.isKeyPressed(Keys.NUM_1)) {
					keydown = 1;
				}
				else if (Gdx.input.isKeyPressed(Keys.NUM_2)) {
					keydown = 2;
				}
				else if(Gdx.input.isKeyPressed(Keys.NUM_3)) {
					keydown = 3;
				}
				else if(Gdx.input.isKeyPressed(Keys.NUM_4) || Gdx.input.isKeyPressed(Keys.BACKSPACE)) {
					keydown = 4;
				}
				else keydown = 0;
			}
		}
	}

	@Override
	public void show() {
		//BottomPanel.log("gdx apptype: " + Gdx.app.getType(), "orange");

		if (Gdx.app.getType() == ApplicationType.Desktop) {
			//BottomPanel.log("setting controller to desktop");
//			may not register, may be unnecessary
			Gdx.input.setInputProcessor(mapControllerDesktop);
		}
		else if (Gdx.app.getType() == ApplicationType.Android) {
			//BottomPanel.log("setting controller to android");
			Gdx.input.setInputProcessor(new GestureDetector(mapControllerAndroid));		
		}
		else if (Gdx.app.getType() == ApplicationType.WebGL){
			Gdx.input.setInputProcessor(mapControllerDesktop);
		}
		else System.out.println("invalid app type!");
	}
	
	public void toggleLOS() {
		this.losOn = !losOn;
	}
	public void toggleFog() {
		this.fogOn = !fogOn;
	}
	public void toggleEdit() {
		this.editOn = !editOn;
//		if (editOn == true) {
//			BottomPanel.log("Editor mode enabled", "magenta");
//			out = new PrintWriter((Gdx.files.absolute("data/map.txt").writer(false))); // boolean append, must be absolute, goes to Desktop data directory
//
//			out.print("Hello "); 
//			out.println("world");
//		}
//		else {
//			BottomPanel.log("Editor mode disabled", "magenta");
//			out.close(); 
//		}
	}
	
	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);	
		kingdom.setPaused(true);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	    // SAVE/Pause
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		// RESUME
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
	public Stage getKingdomStage() {
		return kingdomStage;
	}
	
	public Stage getUIStage() {
		return uiStage;
	}
	
	public Camera getUICamera() {
		return uiStage.getCamera();
	}

	public float getRotation() {
		return rotation;
	}
	public void setRotation(float rotation) {
		this.rotation = rotation;
	}
	public OrthographicCamera getCamera() {
		return camera;
	}
	public Kingdom getKingdom() {
		return kingdom;
	}
	public Fog getFog() {
		return fog;
	}
	public Character getCharacter() {
		return character;
	}
	public SidePanel getSidePanel() {
		return sidePanel;
	}
}
