package kyle.game.besiege.battle;

import kyle.game.besiege.party.RangedWeapon;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.Weapon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;

public class Unit extends Group {
	final int FRAME_COLS = 2;
	final int FRAME_ROWS = 1;

	final int DEFENSE_DISTANCE = 3;
	final int ATTACK_EVERY = 1;

	public BattleStage stage;	
	private TextureRegion white;
	private Unit attacking;

	Soldier soldier;
	Weapon weapon;
	RangedWeapon rangedWeapon;

	public int baseAtk;
	public int baseDef;
	public int baseSpd;

	public int atk;
	public int def;
	public int spd;

	float timer = 0;
	float reloading = 0;
	int hp = 10;
	
	public float speed = 1f;
	public int team;
	public Array<Unit> enemyArray;
	boolean isHit; // is unit hit

	public int pos_x;
	public int pos_y;

	public int prev_x; 
	public int prev_y;
	public boolean moving;
	public float percentComplete; // between 0 and 1, used for moving?
	public enum Orientation {LEFT, UP, RIGHT, DOWN};
	public enum Stance {AGGRESSIVE, DEFENSIVE};
	public Orientation orientation;
	public Stance stance;
	public Unit nearestEnemy;
	
	public WeaponDraw weaponDraw;

	public float rotation;

	float stateTime;
	Animation knightWalk;
	Animation knightAttack;
	Animation knightIdle;
	Animation swordWalk;
	Animation swordAttack;

	public Unit(BattleStage parent, int pos_x, int pos_y, int team, Soldier soldier) {
		stage = parent;

		white = new TextureRegion(new Texture("whitepixel.png"));
//		texture = new TextureRegion(new Texture("red.png"));

		this.team = team;
		if (this.team == 0) enemyArray = stage.enemies;
		else enemyArray = stage.allies;

		this.setX(pos_x);
		this.setY(pos_y);

		this.setWidth(stage.scale*stage.unit_width);
		this.setHeight(stage.scale*stage.unit_height);
//		this.setWidth(texture.getRegionWidth());
//		this.setHeight(texture.getRegionHeight());

		this.setOriginX(this.getWidth()/2);
		this.setOriginY(this.getHeight()/2);

		//		this.setScale(texture.getRegionWidth()/map.unit_width);

		//		this.setScale(1);

		this.pos_x = pos_x;
		this.pos_y = pos_y;

		// TODO check if position already occupied before creating
		stage.map[pos_y][pos_x] = this;

		this.orientation = Orientation.DOWN;

		if (team == 0)
			this.weapon = Weapon.GLAIVE;
		if (team == 1)
			this.rangedWeapon = RangedWeapon.ADV_CROSSBOW;

		this.soldier = soldier;
		this.weapon = soldier.weapon;

		this.atk = soldier.getAtk();
		this.def = soldier.getDef();
		this.spd = Math.max(1, soldier.getSpd());

		if (this.spd == 0) System.out.println("speed is 0");
		
		this.stance = Stance.AGGRESSIVE;
		if (this.team == 1)  stance = Stance.DEFENSIVE;

		Texture walkSheet = new Texture(Gdx.files.internal("knightWalk.png")); 
		TextureRegion[][] textureArray = TextureRegion.split(walkSheet, walkSheet.getWidth()/FRAME_COLS, walkSheet.getHeight()/FRAME_ROWS);
		knightWalk = new Animation(0.25f, textureArray[0]);

		Texture walkSheet2 = new Texture(Gdx.files.internal("knightAttack.png")); 
		TextureRegion[][] textureArray2 = TextureRegion.split(walkSheet2, walkSheet2.getWidth()/FRAME_COLS, walkSheet2.getHeight()/FRAME_ROWS);
		knightAttack = new Animation(0.25f, textureArray2[0]);

//		Texture walkSheet3 = new Texture(Gdx.files.internal("swordAttack.png")); 
//		TextureRegion[][] textureArray3 = TextureRegion.split(walkSheet3, walkSheet2.getWidth()/FRAME_COLS, walkSheet2.getHeight()/FRAME_ROWS);
//		swordAttack = new Animation(0.25f, textureArray3[0]);
//
//		Texture walkSheet4 = new Texture(Gdx.files.internal("swordWalk.png")); 
//		TextureRegion[][] textureArray4 = TextureRegion.split(walkSheet4, walkSheet2.getWidth()/FRAME_COLS, walkSheet2.getHeight()/FRAME_ROWS);
//		swordWalk = new Animation(0.25f, textureArray4[0]);

		stateTime = 0f;
		
		this.weaponDraw = new WeaponDraw(this);
		this.addActor(weaponDraw);
	}

	@Override
	public void act(float delta) {
		if (this.attacking != null) {
			timer += delta;
			if (timer > ATTACK_EVERY) 
			{
				attack();
				timer = 0;
			}
		}
		else if (this.moving) {
			//System.out.println("moving");
			this.percentComplete += speed * spd * delta;
			if (percentComplete > 1) {
				moving = false;
				percentComplete = 1;
			}
		}
		else {
			//			System.out.println("moving2");
			//			getRandomDirection(delta);

			if (stance == Stance.AGGRESSIVE)
				moveToEnemy();
			else if (stance == Stance.DEFENSIVE) {
				faceEnemy();
				// if enemy is within one unit and fighting, can move to them.
				if (nearestEnemy != null && (nearestEnemy.distanceTo(this) < DEFENSE_DISTANCE && nearestEnemy.attacking != null))
					moveToEnemy();
				else if (this.reloading > 0) {
					reloading -= delta;
				}
				// if ranged, fire weapon
				else if (this.isRanged() && nearestEnemy != null) {
					if (nearestEnemy.distanceTo(this) < this.rangedWeapon.range)
						fireAtEnemy();
				}
			}
		}
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {		
		stateTime += Gdx.graphics.getDeltaTime();

		this.toBack();
		Color c = new Color(batch.getColor());
		if (team == 0)
			batch.setColor(1, 0, 0, .5f); 
		else batch.setColor(0, 0, 1, .5f);
		batch.draw(white, stage.scale*stage.unit_width*pos_x, stage.scale*stage.unit_height*pos_y, stage.scale*stage.unit_width, stage.scale*stage.unit_height);
		batch.setColor(c);
		
		this.toFront();

		updateRotation();

		if (moving) {
			setX(stage.scale * currentX() * stage.unit_width);
			setY(stage.scale * currentY() * stage.unit_height);
		}
		else {
			setX(stage.scale * pos_x * stage.unit_width);
			setY(stage.scale * pos_y * stage.unit_height);
		}

		if (this.isHit)
			batch.setColor(1, 0, 0, 1); 

		if (attacking != null) {
			drawAnimation(batch, knightAttack, stateTime);
//			drawAnimation(batch, swordAttack, stateTime);
		}
		else if (moving) {
			drawAnimation(batch, knightWalk, stateTime);
//			drawAnimation(batch, swordWalk, stateTime);
		}	
		else {
			drawAnimation(batch, knightWalk, 0);
//			drawAnimation(batch, swordWalk, 0);
		}

		//		if (team == 0) 
		//			batch.draw(texture, pos_x_world, pos_y_world, getWidth(), getHeight());

		if (this.isHit){
			batch.setColor(c);
			this.isHit = false;
		}
		
		super.draw(batch, parentAlpha);
	}
	private void moveToEnemy() {
		if (enemyArray.size == 0) return;
		this.faceEnemy();
		if (!this.moveForward()) {
			//System.out.println("random move");
			// move in a different direction
			faceEnemyAlt();
			this.moveForward();
		}
	}

	private void fireAtEnemy() {
		faceEnemy();
		this.reloading = rangedWeapon.rate;
		Unit enemy = nearestEnemy;
		Arrow arrow = new Arrow(this, enemy);

		stage.addActor(arrow);
	}

	private void faceEnemy() {
		Unit nearest = this.getNearestEnemy();
		if (nearest == null) return;
		this.face(nearest);
	}

	private void faceEnemyAlt() {
		Unit nearest = this.getNearestEnemy();
		if (nearest == null) return;
		this.faceAlt(nearest);
	}

	private Unit getNearestEnemy() {
		Unit closest = null;
		double closestDistance = 99999;

		for (Unit that : enemyArray) {
			if (that.team == this.team) System.out.println("TEAM ERROR!!!");
			double dist = this.distanceTo(that);
			if (dist < closestDistance) {
				closest = that;
				closestDistance = dist;
			}
		}
		nearestEnemy = closest;
		return closest;
	}

	public double distanceTo(Unit that) {
		return Math.sqrt((that.pos_x-this.pos_x)*(that.pos_x-this.pos_x) + (that.pos_y-this.pos_y)*(that.pos_y-this.pos_y));
	}

	private void attack() {
		if (!this.attacking.isAdjacent(this) || this.attacking.hp <= 0){
			this.attacking = null;
			return;
		}
		//System.out.println("attack phase: " + attacking.hp);
		this.face(attacking);

		attacking.hurt(Math.max(0, Math.random()*atk-Math.random()*def), this);
	}

	public void hurt(double damage, Unit attacker) {
		this.hp -= (int) (damage + .5);

		this.isHit = true;
		if (this.hp <= 0) {
			this.destroy();
			if (attacker != null)
				attacker.attacking = null;
		}
	}


	public static Orientation getRandomDirection() {
		double random = Math.random();
		if (random < .25)
			return Orientation.LEFT;
		else if (random < .5)
			return Orientation.UP;
		else if (random < .75)
			return Orientation.RIGHT;
		else
			return Orientation.DOWN;
	}


	public void updateRotation() {
		rotation = 0;
		if (orientation == Orientation.DOWN) rotation = 180;
		if (orientation == Orientation.LEFT) rotation = 90;
		if (orientation == Orientation.RIGHT) rotation = 270;
		this.setRotation(rotation);
	}

	public void drawAnimation(SpriteBatch batch, Animation animation, float stateTime) {
		TextureRegion region = animation.getKeyFrame(stateTime, true);
		batch.draw(region, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());		
	}


	public float currentX() {
		return (prev_x + percentComplete*(pos_x-prev_x));
	}

	public float currentY() {
		return (prev_y + percentComplete*(pos_y-prev_y));
	}

	public boolean moveForward() {
		return startMove(this.orientation);
	}

	// returns false if move failed, true otherwise
	// TODO collisions!
	public boolean startMove(Orientation direction) {
		if (this.hp < 0) return false;

		prev_x = pos_x;
		prev_y = pos_y;
		if (direction == Orientation.DOWN) {
			if (pos_y == 0 || stage.closed[pos_y-1][0]) return false;
			if (stage.map[pos_y-1][pos_x] != null) {
				this.orientation = direction;
				return collision(stage.map[pos_y-1][pos_x]);
			}
			stage.map[pos_y][pos_x] = null;
			pos_y -= 1;
		}
		else if (direction == Orientation.UP) {
			if (pos_y == stage.size_y-1 || stage.closed[pos_y+1][0]) return false;
			if (stage.map[pos_y+1][pos_x] != null) {
				this.orientation = direction;
				return collision(stage.map[pos_y+1][pos_x]);
			}
			stage.map[pos_y][pos_x] = null;
			pos_y += 1;
		}
		else if (direction == Orientation.LEFT) {
			if (pos_x == 0 || stage.closed[0][pos_x-1]) return false;
			if (stage.map[pos_y][pos_x-1] != null) {
				this.orientation = direction;
				return collision(stage.map[pos_y][pos_x-1]);
			}
			stage.map[pos_y][pos_x] = null;
			pos_x -= 1;
		}
		else if (direction == Orientation.RIGHT) {
			if (pos_x == stage.size_x-1 || stage.closed[0][pos_x+1]) return false;
			if (stage.map[pos_y][pos_x+1] != null) { 
				this.orientation = direction;
				return collision(stage.map[pos_y][pos_x+1]);
			}
			stage.map[pos_y][pos_x] = null;
			pos_x += 1;
		}

		stage.map[pos_y][pos_x] = this;
		moving = true;
		percentComplete = 0;

		this.orientation = direction;
		return true;
	}

	// return true if enemy, false if friend
	public boolean collision(Unit that) {
		if (this.team != that.team) {
			if (!that.moving) 
				attack(that);
			return true;
		}
		return false;
	}

	public void attack(Unit that) {
		// if (should fight back / not already attacking)
		// 		fight back
		this.attacking = that;

		// change this later
		that.attacking = this;
	}

	public boolean isAdjacent(Unit that) {
		int distance_x = Math.abs(that.pos_x - this.pos_x);
		int distance_y = Math.abs(that.pos_y - this.pos_y);

		if (distance_x > 1 || distance_y > 1) return false;
		if (distance_x == 1 && distance_y == 1) return false;
		return true;
	}

	public void destroy() {
		stage.map[pos_y][pos_x] = null;
		this.pos_x = -100;
		this.pos_y = -100;
		//		System.out.println("DESTROYED");
		stage.removeUnit(this);
	}

	public void face(Unit that) {
		int x_dif = that.pos_x - this.pos_x;
		int y_dif = that.pos_y - this.pos_y;

		if (Math.abs(x_dif) > Math.abs(y_dif)) {
			if (x_dif > 0) this.orientation = Orientation.RIGHT;
			else this.orientation = Orientation.LEFT;
		}
		else if (Math.abs(x_dif) < Math.abs(y_dif)) {
			if (y_dif > 0) this.orientation = Orientation.UP;
			else this.orientation = Orientation.DOWN;
		}
		else if (y_dif > 0) this.orientation = Orientation.UP;
		else this.orientation = Orientation.DOWN;
	}

	// still tries to face the enemy, but in none of the same ways as the other face method
	public void faceAlt(Unit that) {
		int x_dif = that.pos_x - this.pos_x;
		int y_dif = that.pos_y - this.pos_y;

		if (Math.abs(x_dif) < Math.abs(y_dif)) {
			if (x_dif > 0) this.orientation = Orientation.RIGHT;
			else if (x_dif < 0) this.orientation = Orientation.LEFT;
			else if (Math.random() < .5) this.orientation = Orientation.RIGHT;
			else this.orientation = Orientation.LEFT;
		}
		else if (Math.abs(x_dif) > Math.abs(y_dif)) {
			if (y_dif > 0) this.orientation = Orientation.UP;
			else if (y_dif < 0) this.orientation = Orientation.DOWN;
			else if (Math.random() < .5) this.orientation = Orientation.UP;
			else this.orientation = Orientation.UP;
		}
		else if (x_dif > 0) this.orientation = Orientation.RIGHT;
		else this.orientation = Orientation.LEFT;
	}

	public boolean isRanged() {
		return this.rangedWeapon != null;
	}
	
	public float getCenterX() {
		return getX() + getOriginX();
	}
	public float getCenterY() {
		return getY() + getOriginY();
	}
}
