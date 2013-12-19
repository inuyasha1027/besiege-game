/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hoten.voronoi;

import hoten.geom.PointH;

import java.util.ArrayList;

import kyle.game.besiege.Map;

import com.badlogic.gdx.math.Vector2;

/**
 * Corner.java Function Date Jun 6, 2013
 *
 * @author Connor
 * modified by Kyle
 */
public class Corner {
	private final float PUSH_DIST = 15; //distance corners will be pushed away from convex corners

	public ArrayList<Center> touches = new ArrayList(); //good
	public ArrayList<Corner> adjacent = new ArrayList(); //good
	public ArrayList<Edge> protrudes = new ArrayList();
	public ArrayList<Corner> visibleCorners;
	// distance to bordercorner at index, if non-null/0 then it's visible.
	//    public ArrayList<Double> visibleDistance;
	public PointH loc;
	public PointH locPushed; // for use in visibility
	public int index;
	public boolean border;
	public boolean waterBorder;
	public int waterTouches; // num water centers it touches
	//    public boolean blocked;

	public double elevation;
	public boolean water, ocean, coast;
	public Corner downslope;
	public int river;
	public double moisture; 
	
	public void init() {
		locPushed = new PointH(this.loc.x, this.loc.y);
	}

	public void calcWaterTouches() {
		init();
		waterTouches = 0;
		for (Center center: touches)
			if (center.water) waterTouches++;
		pushOut();
	}
	public void pushOut() {
		if (waterTouches == 1 && touches.size() == 3) {
			for (Center center : touches) {
				if (center.water) {
					// create vector pointing towards center of water
					Vector2 vector = new Vector2((float)(center.loc.x-loc.x), (float)(center.loc.y-loc.y));// don't worry about "upside down map"
					vector.scl(1/vector.len()); // unitize
					vector.rotate(180); // rotate 180
					vector.scl(PUSH_DIST);
					this.locPushed.x += vector.x;
					this.locPushed.y += vector.y;
					break;
				}
			}
		}
		if (waterTouches == 2 && touches.size() == 3) {
			for (Center center : touches) {
				if (!center.water) {
					// create vector pointing towards center of land
					Vector2 vector = new Vector2((float)(center.loc.x-loc.x), (float)(center.loc.y-loc.y));// don't worry about "upside down map"
					vector.scl(1/vector.len()); // unitize
//					vector.rotate(180); // rotate 180
					vector.scl(PUSH_DIST);
					this.locPushed.x += vector.x;
					this.locPushed.y += vector.y;
					break;
				}
			}
		}
	}
}
