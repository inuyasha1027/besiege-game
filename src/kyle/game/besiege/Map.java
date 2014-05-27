/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kyle.game.besiege;


import java.util.ArrayList;

import kyle.game.besiege.geom.PointH;
import kyle.game.besiege.geom.Rectangle;
import kyle.game.besiege.utils.MyRandom;
import kyle.game.besiege.voronoi.Center;
import kyle.game.besiege.voronoi.Corner;
import kyle.game.besiege.voronoi.Edge;
import kyle.game.besiege.voronoi.VoronoiGraph;
import kyle.game.besiege.voronoi.nodename.as3delaunay.Voronoi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
 
/*
 * TestDriver.java Function Date Jun 14, 2013
 *
 * @author Kyle
 */

public class Map extends Actor {
	public ShapeRenderer sr;

	public static final int WIDTH = 3000;
	public static final int HEIGHT = 3000;
	private static final int NUM_SITES = 1000;
//	public static boolean debug = true;
	public static boolean debug = false;
	public static boolean drawSpheres = false;
	public VoronoiGraph vg;

	private static final TextureRegion test = Assets.atlas.findRegion("crestRedCross");
	private static final TextureRegion test2 = Assets.atlas.findRegion("crestOrangeCross");

	public int testIndex;
	public int totalVisibilityLines;

	public Texture bg;
	public Array<Corner> cityCorners;
	public Array<Center> cityCenters;
//	public Array<PointH> availableLocationSites;
	public Array<Corner> availableCorners;
	public Array<Center> availableCenters;
	
	public Array<Corner> borderCorners;
	public Array<Edge> impassable;
	public Array<Edge> impBorders;
	public Array<Center> connected; // land centers connected to reference

	public Center reference; // center on main map
	public Point referencePoint;
	
	/** Borders between faction territory */
	public Array<Edge> borderEdges; 
	
	// testing
	public Array<Polygon> testPolygons = new Array<Polygon>();
	public static Array<Corner> testCorners = new Array<Corner>();
	private boolean toggle = true;
//	Array<Center> neighborAdj; // testing only
	
	// contains code written by connor
	public Map() {
		sr = new ShapeRenderer();
		testIndex = 1;
		totalVisibilityLines = 0;

		final ArrayList<PointH> pointHs = new ArrayList();
		final long seed = (long) (Math.random()*1000000);
		//final long see = System.nanoTime();
		final MyRandom r = new MyRandom(seed);
		cityCorners = new Array<Corner>();
		cityCenters = new Array<Center>();
//		availableLocationSites = new Array<PointH>();
		availableCorners = new Array<Corner>();
		availableCenters = new Array<Center>();
		
		borderCorners = new Array<Corner>();
		impassable = new Array<Edge>();
		impBorders = new Array<Edge>();
		connected = new Array<Center>();
		System.out.println("seed: " + seed);

		//let's create a bunch of random points
		for (int i = 0; i < NUM_SITES; i++)
			pointHs.add(new PointH(r.nextDouble(0, WIDTH), r.nextDouble(0, HEIGHT)));

		//now make the initial underlying voronoi structure
		final Voronoi v = new Voronoi(pointHs, null, new Rectangle(0, 0, WIDTH, HEIGHT));

		//assemble the voronoi structure into a usable graph object representing a map
		this.vg = new VoronoiGraph(v, 2, r);

		// paint background
		Pixmap pix = new Pixmap(WIDTH, HEIGHT, Pixmap.Format.RGB888);
		vg.paint(pix);
		this.bg = new Texture(pix);

		calcReference();
		calcReferencePoint();
		
		// testing: works
//		neighborAdj = new Array<Center>();
//		neighborAdj.add(reference);
//		for (Center c : reference.neighbors) {
//			neighborAdj.add(c);
//		}
		borderEdges = new Array<Edge>();
		calcConnected(reference, connected);
		System.out.println("Num connected polygons: " + connected.size);
		addPolygons();
		calcTriangles();
//		System.out.println("Adding polygons to centers");
		
		convertIslandsToWater();
		calcWaterBorders();
		calcVisibilityGraph();
		calcCitySpots();
	}
	
	/** Updates list of edges that separate factions
	 * 
	 */
	public void calcBorderEdges() {
		borderEdges.clear();
		for (Edge e : vg.edges) {
			if (e.d0 != null && e.d1 != null && e.d0.faction != null && e.d1.faction != null) {
				if (e.d0.faction != e.d1.faction) {
					if (!borderEdges.contains(e, true)) borderEdges.add(e);
				}
			}
		}
	}
	
	private void calcReference() {
		double BOUND = .1; // *100 = percent range
		// think of a way to guarantee it's in the middle of the island
		for (Center c : vg.centers) {
			if (!c.water) {
				// check within a box in center of screen
				if (c.loc.x >= Map.WIDTH*(.5-BOUND) && c.loc.x <= Map.WIDTH*(.5+BOUND)){
					if (Map.HEIGHT-c.loc.y >= Map.HEIGHT*(.5-BOUND) && Map.HEIGHT-c.loc.y <= Map.WIDTH*(.5+BOUND)){
						reference = c;
						break;
					}
				}
			}
		}
	}
	
	private void calcReferencePoint() {
		referencePoint = new Point(reference.loc.x, Map.HEIGHT-reference.loc.y);
	}
	
	// calc connected components, recursively
	private void calcConnected(Center center, Array<Center> connected) {
		connected.add(center);
		for (Center neighbor : center.neighbors) {
			if (!neighbor.water && !connected.contains(neighbor, true)) {
				calcConnected(neighbor, connected);
			}
		}
	}
	
	private void addPolygons() {
		for (Center center : connected) {
			int indexInit = 0;
			Array<Corner> used = new Array<Corner>(); // stores corners that have been used;
			float[] vertices = new float[center.corners.size()*2];
			
			getNextVertex(center.corners.get(0), center, used, vertices, indexInit);
//			System.out.println("final vertex y " + vertices[vertices.length-1]);
			
			Polygon polygon = new Polygon(vertices);
			center.polygon = polygon;
		}
	}
	
	// recursively find and add adjacent vertex to vertices
	private void getNextVertex(Corner corner, Center center, Array<Corner> used, float[] vertices, int index) {
		for (Corner next : corner.adjacent) {
			if (center.corners.contains(next) && !used.contains(next, true)) {
				used.add(next);
				vertices[index] = (float) next.loc.x;
				index++;
				vertices[index] = (float) (Map.HEIGHT-next.loc.y);
				index++;
				getNextVertex(next, center, used, vertices, index);
				return;
			}
		}
	}
	
	/** reorganizes array disconnectedCenters into separate arrays of 
	 *  connected centers
	 * 
	 * @param disconnectedCenters
	 * @return
	 */
	public static Array<Array<Center>> calcConnectedCenters(Array<Center> original) {
		Array<Center> disconnectedCenters = new Array<Center>();
		for (Center c : original) {
			disconnectedCenters.add(c);
		}
		Array<Array<Center>> aaCenters = new Array<Array<Center>>();
		// start with random one, calc connected, remove all of those from disconnected, continue until disconnected is empty
		
		while (disconnectedCenters.size > 0) {
			Array<Center> connectedToStart = new Array<Center>();
			Center start = disconnectedCenters.random();
			calcConnectedContained(start, connectedToStart, original);
//			System.out.println("connected " + connectedToStart.size);
			aaCenters.add(connectedToStart);
			disconnectedCenters.removeAll(connectedToStart, true);
		}
//		System.out.println("number of separate polygons: " + aaCenters.size);
		return aaCenters;
	}
	
	// calc connected components, recursively
	private static void calcConnectedContained(Center center, Array<Center> connected, Array<Center> container) {
		connected.add(center);
		for (Center neighbor : center.neighbors) {
			if (container.contains(neighbor, true) && !connected.contains(neighbor, true)) {
				calcConnectedContained(neighbor, connected, container);
			}
		}
	}
	/**
	 * Converts array of centers to the largest polygon that can hold
	 * all of them. returns that polygon.
	 * 
	 * Problem when there is a different polygon in the middle of all of them.
	 *  
	 * @param polygonCenters
	 */
	public static Polygon centersToPolygon(Array<Center> polygonCenters) {
		return edgesToPolygon(getBordersOfCenters(polygonCenters));
	}
	
	/** Doesn't work! problem is when there is only one shared edge 
	 * and it doesn't know which corner to choose. Workaround: use
	 * all edges of all centers, but don't use ones that are shared!
	 * Won't yield 'Polygon' result but I'm sure there's a way to 
	 * convert it... pretty easy way actually
	 * How to paint a polygon? Paint it's triangles. Should I make a
	 * method for painting triangles of a center or a polygon? Yes.
	 * 
	 * @param corner
	 * @param outsideCorners
	 * @param used
	 * @param vertices
	 * @param index
	 */
	// recursively find and add adjacent vertex to vertices
	private static void getNextVertex(Corner corner, Array<Corner> outsideCorners, Array<Corner> used, float[] vertices, int index) {
		for (Corner next : corner.adjacent) {
			if (outsideCorners.contains(next, true) && !used.contains(next, true)) {
				used.add(next);
				vertices[index] = (float) next.loc.x;
				index++;
				vertices[index] = (float) (Map.HEIGHT-next.loc.y);
				index++;
				getNextVertex(next, outsideCorners, used, vertices, index);
				return;
			}
		}
	}
	
	private static Array<Edge> getBordersOfCenters(Array<Center> centers) {
		Array<Edge> usedMoreThanOnce = new Array<Edge>();
		Array<Edge> usedOnce = new Array<Edge>();
		for (Center center : centers) {
			for (Edge edge : center.borders) {
				if (usedOnce.contains(edge, true)) {
					usedOnce.removeValue(edge, true);
					usedMoreThanOnce.add(edge);
				}
				else if (!usedMoreThanOnce.contains(edge, true))
					usedOnce.add(edge);
			}
		}
		return usedOnce;
	}
	
	/** Converts Array of connected edges into a Libgdx polygon
	 * 
	 */
	private static Polygon edgesToPolygon(Array<Edge> edges) {
		Array<Edge> used = new Array<Edge>();
		int index = 0;
		float[] vertices = new float[edges.size*2];
		
		vertices[index] = (float) edges.first().v0.loc.x;
		index++;
		vertices[index] = (float) (Map.HEIGHT- edges.first().v0.loc.y);
		index++;
		adjEdge(edges.first().v0, edges.first(), edges, used, index, vertices);
		return new Polygon(vertices);
	}
	
	/** Recursively finds adjacent edges in a polygon adding their corners
	 *  to vertices.
	 * 
	 * @param startC
	 * @param startE
	 * @param allEdges
	 * @param used
	 * @param index
	 * @param vertices
	 */
	private static void adjEdge(Corner startC, Edge startE, Array<Edge> allEdges, Array<Edge> used, int index, float[] vertices) {
		used.add(startE);
		for (Edge adj : startC.protrudes) {
			if (allEdges.contains(adj, true) && !used.contains(adj, true)) {
				if (adj.v0 != startC) {
					vertices[index] = (float) adj.v0.loc.x;
					index++;
					vertices[index] = (float) (Map.HEIGHT - adj.v0.loc.y);
					index++;
					adjEdge(adj.v0, adj, allEdges, used, index, vertices);
					return;
				}
				else if (adj.v1 != startC) {
					vertices[index] = (float) adj.v1.loc.x;
					index++;
					vertices[index] = (float) (Map.HEIGHT - adj.v1.loc.y);
					index++;
					adjEdge(adj.v1, adj, allEdges, used, index, vertices);
					return;
				}
				else System.out.println("adjEdge not following proper path");
			}
		}
	}
	
	/** naive approach to finding outside corners
	 *  works :D
	 *  
	 * @param polygonCenters
	 * @return 
	 */
	private static Array<Corner> findOutsideCorners(Array<Center> polygonCenters) {
		Array<Corner> outside = new Array<Corner>();
		for (Center center : polygonCenters) {
			for (Corner corner : center.corners) {
				if (!outside.contains(corner, true)) {
					int containedCenters = 0;
					for (int i = 0; i < corner.touches.size(); i++) {
						if (polygonCenters.contains(corner.touches.get(i), true))
							containedCenters++;
					}
					if (containedCenters <= 2) outside.add(corner);
				}
			}
		}
		testCorners.addAll(outside);
		return outside;
	}
	
	/** treat islands as water (aka can't build there can't travel there)
	 */
	private void convertIslandsToWater() {
		for (Center center : vg.centers) {
			if (!connected.contains(center, true))
				center.water = true;
		}
//		// also recalculate corners.water and edges.water
//		for (Corner c : vg.corners) {
//			if (c.water) {
//				int landTouches = 0;
//				for (Center center : c.touches) {
//					if (!center.water) landTouches++;
//				}
//				if (landTouches == 0) c.water = false;
//			}
//		}
	}

	private void calcCitySpots() {
		// populate available city and village positions
		for (Center center : connected) {
			if (!cityCenters.contains(center, true)) {
				addToAvailableCenters(center);
				cityCenters.add(center);
				for (Corner corner : center.corners) {
					if (!cityCorners.contains(corner, true)) {
						cityCorners.add(corner);
						addToAvailableCorners(corner);
					}
				}
			}
			for (Edge edge : center.borders) {
				addToAvailableCorners(edge.v0);	
				addToAvailableCorners(edge.v1);
			}
		}
	}
	
//	private void addToLocSites(PointH loc) {
//		if (!availableLocationSites.contains(loc, true))
//			availableLocationSites.add(loc);
//	}
	
	private void addToAvailableCorners(Corner c) {
		if (!availableCorners.contains(c, true))
			availableCorners.add(c);
	}
	
	private void addToAvailableCenters(Center c) {
		if (!availableCenters.contains(c, true))
			availableCenters.add(c);
	}
	
	public boolean isInWater(Destination d) {
		return !pathExists(d, reference.loc.x, HEIGHT-reference.loc.y);
	}

	// TODO don't have so many news here
	public boolean pathExists(Destination start, double px, double py) {
		// ray casting
		Corner c1 = new Corner();
		c1.loc = new PointH(px, Map.HEIGHT-py);
		c1.init();
		Corner c2 = new Corner();
		c2.loc = new PointH(start.getCenterX(), Map.HEIGHT-start.getCenterY());
		c2.init();
		
		if (numIntersections(c1, c2) % 2 == 1) return false;
		return true;
	}
	
	public int numIntersections(Corner c1, Corner c2) {
		ArrayList<Edge> intersections = new ArrayList<Edge>();
		for (Edge edge : impBorders) {
			if (!c1.protrudes.contains(edge) && !c2.protrudes.contains(edge)) {
				if (intersect(c1, c2, edge))
					intersections.add(edge);
			}
		}
		return intersections.size();
	}
	private void calcWaterBorders() {
		for (Edge edge : vg.edges) {
    		if (edge.d0.water != edge.d1.water) {
    			edge.v0.waterBorder = true;
    			edge.v1.waterBorder = true;
    		}
    	}
	}
	
	public void addCorner(Corner otherCorner) {
		borderCorners.add(otherCorner);
		otherCorner.visibleCorners = new ArrayList<Corner>();
		for (Corner currentCorner : borderCorners) {
			if (otherCorner.waterTouches != 2) {
				for (Edge touching : currentCorner.protrudes) {
					if ((!touching.d0.water || !touching.d1.water) && (touching.v0 == otherCorner || touching.v1 == otherCorner)) {
						currentCorner.visibleCorners.add(otherCorner);
						totalVisibilityLines++;
						if (otherCorner.visibleCorners != null && !otherCorner.visibleCorners.contains(currentCorner))
							otherCorner.visibleCorners.add(currentCorner);
						continue;
					}
				}

				if (otherCorner != null && otherCorner != currentCorner && openPathInit(currentCorner, otherCorner) == null) {
					boolean shouldAdd = true;
					// make sure not same water polygon
					for (Center center : currentCorner.touches) {
						if (otherCorner.touches.contains(center)) {
							if (center.water) shouldAdd = false;
						}
					}
					if (shouldAdd) {
						if (!currentCorner.visibleCorners.contains(otherCorner))
							currentCorner.visibleCorners.add(otherCorner);
						totalVisibilityLines++;
						if (otherCorner.visibleCorners != null && !otherCorner.visibleCorners.contains(currentCorner))
							otherCorner.visibleCorners.add(currentCorner);
						// can optimize a little later
					}
				}
			}
		}
	}
	
	private void calcTriangles() {
    	for (Center c : vg.centers) c.calcTriangles(); 
	}

	public void removeCorner(Corner toRemove) {
		borderCorners.removeValue(toRemove, true);
		for (Corner existing : borderCorners) {
			// TODO very slow please improve
			//			if (existing.visibleCorners.contains(toRemove)) {
			// can improve by starting from end... should
			//			existing.visibleCorners.trimToSize();
			if (existing.visibleCorners.contains(toRemove)) {
				existing.visibleCorners.remove(toRemove);
			}
		}
	}

	public void calcVisibilityGraph() {
		for (Corner corner : vg.corners) {
			corner.calcWaterTouches();
			if (corner.waterBorder) {
				borderCorners.add(corner);
			}
		}
		for (Edge edge : vg.edges) {
			if (edge.isImpassable())
				impassable.add(edge);
			if (edge.isBorder())
				impBorders.add(edge);
		}

		System.out.println("Border corners: " + borderCorners.size);
		System.out.println("Impassable edges: " + impassable.size);
		System.out.println("Impassable borders: " + impBorders.size);
		for (Corner c : borderCorners) {
			calcVisible(c);
		}
		System.out.println("total visibility graph lines: " + totalVisibilityLines);
	}

	public void calcVisible(Corner currentCorner) {
		if (currentCorner.visibleCorners == null)
			currentCorner.visibleCorners = new ArrayList<Corner>();
		if (currentCorner.waterTouches != 2) {
			borderCorners.shrink();
			for (int j = 0; j < borderCorners.size; j++) {
				Corner otherCorner = (Corner) borderCorners.get(j);
				if (otherCorner.waterTouches != 2 && lineNeeded(currentCorner, otherCorner)) {
					for (Edge touching : currentCorner.protrudes) {
						if ((!touching.d0.water || !touching.d1.water) && (touching.v0 == otherCorner || touching.v1 == otherCorner)) {
							currentCorner.visibleCorners.add(otherCorner);
							totalVisibilityLines++;
							if (otherCorner.visibleCorners != null && !otherCorner.visibleCorners.contains(currentCorner))
								otherCorner.visibleCorners.add(currentCorner);
							continue;
						}
					}

					if (otherCorner != null && otherCorner != currentCorner && openPathInit(currentCorner, otherCorner) == null) {
						boolean shouldAdd = true;
						// make sure not same water polygon
						for (Center center : currentCorner.touches) {
							if (borderCorners.get(j).touches.contains(center)) {
								if (center.water) shouldAdd = false;
							}
						}
						if (shouldAdd) {
							if (!currentCorner.visibleCorners.contains(otherCorner))
								currentCorner.visibleCorners.add(otherCorner);
							totalVisibilityLines++;
							if (otherCorner.visibleCorners != null && !otherCorner.visibleCorners.contains(currentCorner))
								otherCorner.visibleCorners.add(currentCorner);
							// can optimize a little later
						}
					}
				}
			}
		}
	}

	/** checks if there is a direct path between two corners, returns
	 * null if path exists, or the edge blocking it if it doesn't.
	 * Looks at all possible blocking edges (slower)
	 * 
	 * @param c1
	 * @param c2
	 * @return
	 */
	public Edge openPathInit(Corner c1, Corner c2) {
		for (Edge edge : impassable) {
			if (!c1.protrudes.contains(edge) && !c2.protrudes.contains(edge)) {
				if (intersect(c1, c2, edge))
					return edge;
			}
			//			else System.out.println("touching edge");
		}
		return null;
	}
	
	/** checks if there is a direct path between two corners, returns
	 * null if path exists, or the edge blocking it if it doesn't.
	 * Looks only at border edges (faster)
	 * 
	 * @param c1
	 * @param c2
	 * @return
	 */
	public Edge openPath(Corner c1, Corner c2) {
		for (Edge edge : impBorders) {
			if (!c1.protrudes.contains(edge) && !c2.protrudes.contains(edge)) {
				if (intersect(c1, c2, edge))
					return edge;
			}
			//			else System.out.println("touching edge");
		}
		return null;
	}
	/** checks if line between corners intersects edge
	 *  probably the most frequently called method in this game
	 * 
	 * @param c1
	 * @param c2
	 * @param edge
	 * @return
	 */
	public boolean intersect(Corner c1, Corner c2, Edge edge) {
		if (edge.v0 == null || edge.v1 == null) return false;

//		double ax = c1.loc.x;
//		double ay = Map.HEIGHT-c1.loc.y;
//		double bx = c2.loc.x;
//		double by = Map.HEIGHT-c2.loc.y;
		double ax = c1.getLoc().x;
		double ay = Map.HEIGHT-c1.getLoc().y;
		double bx = c2.getLoc().x;
		double by = Map.HEIGHT-c2.getLoc().y;
		double cx = edge.v0.loc.x;
		double cy = Map.HEIGHT-edge.v0.loc.y;
		double dx = edge.v1.loc.x;
		double dy = Map.HEIGHT-edge.v1.loc.y;

		return ((ccw(ax, ay, cx, cy, dx, dy) != ccw(bx, by, cx, cy, dx, dy))
				&& (ccw(ax, ay, bx, by, cx, cy) != ccw(ax, ay, bx, by, dx, dy)));
	}
	/** checks if there is a direct path between two points (on natural coordinates), returns
	 * null if path exists, or the edge blocking it if it doesn't.
	 * 
	 * @return
	 */
	public Edge openPath(float x1, float y1, float x2, float y2) {
		for (Edge edge : impBorders) {
			if (intersect(x1, y1, x2, y2, edge))
				return edge;
			//			else System.out.println("touching edge");
		}
		return null;
	}
	/** checks intersection between two points and an edge
	 */
	public boolean intersect(float x1, float y1, float x2, float y2, Edge edge) {
		if (edge.v0 == null || edge.v1 == null) return false;

		double ax = x1;
		double ay = Map.HEIGHT-y1;
		double bx = x2;
		double by = Map.HEIGHT-y2;
		double cx = edge.v0.loc.x;
		double cy = Map.HEIGHT-edge.v0.loc.y;
		double dx = edge.v1.loc.x;
		double dy = Map.HEIGHT-edge.v1.loc.y;

		return ((ccw(ax, ay, cx, cy, dx, dy) != ccw(bx, by, cx, cy, dx, dy))
				&& (ccw(ax, ay, bx, by, cx, cy) != ccw(ax, ay, bx, by, dx, dy)));
	}
	/* are these points in counter clockwise order? */
	private boolean ccw(double ax, double ay, double bx, double by, double cx, double cy) {
		return (cy-ay)*(bx-ax) > (by-ay)*(cx-ax);
	}
	
	// return true if a connection line is needed between two corners (vectors on same side of connecting line)
	private boolean lineNeeded(Corner a, Corner b) {
		Vector2 connector = new Vector2((float) (a.getLoc().x-b.getLoc().x), (float) (HEIGHT-a.getLoc().y-(HEIGHT-b.getLoc().y)));
//		Vector2 connector = new Vector2((float) (a.loc.x-b.loc.x), (float) (HEIGHT-a.loc.y-(HEIGHT-b.loc.y)));
		
		// if two corners are adjacent, return true
		if (a.adjacent.contains(b) || b.adjacent.contains(a)) return true;
		
		if (a.adjacent.size() != 0) { 
			Corner[] aC = new Corner[2];
			int index = 0;
			for (Corner corner : a.adjacent) {
				// can optimize
				if (borderCorners.contains(corner, true) && index <= 1) {
					aC[index] = corner;
					index++;
				}
			}

			Vector2 a1 = new Vector2((float) (aC[0].getLoc().x-a.getLoc().x),(float) (HEIGHT-aC[0].getLoc().y-(HEIGHT-a.getLoc().y)));
			Vector2 a2 = new Vector2((float) (aC[1].getLoc().x-a.getLoc().x),(float) (HEIGHT-aC[1].getLoc().y-(HEIGHT-a.getLoc().y)));
			
//			Vector2 a1 = new Vector2((float) (aC[0].loc.x-a.loc.x),(float) (HEIGHT-aC[0].loc.y-(HEIGHT-a.loc.y)));
//			Vector2 a2 = new Vector2((float) (aC[1].loc.x-a.loc.x),(float) (HEIGHT-aC[1].loc.y-(HEIGHT-a.loc.y)));
			
			a1.rotate(-1*connector.angle()+90);
			a2.rotate(-1*connector.angle()+90);

			// should be able to use this other version, improves speed by 5x
//			if (sameSide(a1, a2)) return true;
			if (!sameSide(a1, a2)) return false;
		}
		else return true;
		
		if (b.adjacent.size() != 0) { 
			Corner[] bC = new Corner[2];
			int index = 0;
			for (Corner corner : b.adjacent) {
				if (borderCorners.contains(corner, true) && index <= 1) {
					bC[index] = corner;
					index++;
				}
			}

//			Vector2 b1 = new Vector2((float) (bC[0].getLoc().x-b.getLoc().x),(float) (HEIGHT-bC[0].getLoc().y-(HEIGHT-b.getLoc().y)));
//			Vector2 b2 = new Vector2((float) (bC[1].getLoc().x-b.getLoc().x),(float) (HEIGHT-bC[1].getLoc().y-(HEIGHT-b.getLoc().y)));
			
			Vector2 b1 = new Vector2((float) (bC[0].loc.x-b.loc.x),(float) (HEIGHT-bC[0].loc.y-(HEIGHT-b.loc.y)));
			Vector2 b2 = new Vector2((float) (bC[1].loc.x-b.loc.x),(float) (HEIGHT-bC[1].loc.y-(HEIGHT-b.loc.y)));
			
			b1.rotate(-1*connector.angle()+90);
			b2.rotate(-1*connector.angle()+90);

			if (!sameSide(b1, b2)) return false;
		}
		else return true;

		return true;
	}
	
	// given a pair of vectors, return true if both lie on same side of y axis
	private boolean sameSide(Vector2 a1, Vector2 a2) {
		if (a1.angle() <= 90 || a1.angle() > 270) {
			if (a2.angle() > 270 || a2.angle() <= 90) return true;
			return false;
		} 
		else {
			if (a2.angle() > 90 && a2.angle() <= 270) return true;
			return false;
		}
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		batch.disableBlending();
		batch.draw(bg, 0, 0);
		batch.enableBlending(); // should speed up
		
//		 for copying and pasting
//		batch.end();
//		sr.begin(ShapeType.Line);
//		sr.setProjectionMatrix(batch.getProjectionMatrix());
//		sr.setColor(0, 0, 0, 1);
//		sr.end();
//		batch.begin();
		
		if (debug) {
			batch.end();
			sr.begin(ShapeType.Line);
			sr.setProjectionMatrix(batch.getProjectionMatrix());
			sr.setColor(0, 0, 0, 1);
			
			// draw available city locations (Fix this next)
//			for (Corner c : this.availableCorners) {
//				sr.circle((float) c.getLoc().x, (float) (Map.HEIGHT-c.getLoc().y), 4);
//			}
//			for (Center c : this.availableCenters) {
//				sr.circle((float) c.loc.x, (float) (Map.HEIGHT-c.loc.y), 4);
//			}
			
			Gdx.gl20.glLineWidth(1);

//			// draw visibility graph
			for (Corner c : borderCorners) {
				for (Corner c2 : c.visibleCorners){
					sr.line((float) c.loc.x,(float)( HEIGHT-c.loc.y),(float) c2.loc.x, (float)(HEIGHT-c2.loc.y));
				}
			}
			
//			// draw s
//			sr.setColor(1, 0, 0, 1);
//			for (int i = 0; i < impassable.size; i++){
//				Edge toDraw = impassable.get(i);
//				if (toDraw != null && toDraw.v0 != null && toDraw.v1 != null)
//					sr.line((float) toDraw.v0.loc.x, (float)(HEIGHT-toDraw.v0.loc.y), (float) toDraw.v1.loc.x, (float) (HEIGHT-toDraw.v1.loc.y));
//			}
			// draw impassable borders
			sr.setColor(.5f,1,0,1);
			for (int i = 0; i < impBorders.size; i++) {
				Edge toDraw = impBorders.get(i);
				if (toDraw != null && toDraw.v0 != null && toDraw.v1 != null)
					sr.line((float) toDraw.v0.loc.x, (float)(HEIGHT-toDraw.v0.loc.y), (float) toDraw.v1.loc.x, (float) (HEIGHT-toDraw.v1.loc.y));
			}
//			// draw impassable edges
			sr.setColor(1, 1, 0, 1);
			if (impassable.get(testIndex) != null && impassable.get(testIndex).midpoint != null)
				sr.line((float) impassable.get(testIndex).v0.loc.x, (float)(HEIGHT-impassable.get(testIndex).v0.loc.y), (float) impassable.get(testIndex).v1.loc.x, (float) (HEIGHT-impassable.get(testIndex).v1.loc.y));
//			
			// draw centers that contain armies
			for (Center c : connected) {
				// 1 = blue, 2 = green, 3+ = red
				if (c.armies.size >= 1) {
//					System.out.println("size: " + c.armies.size);
					if (c.armies.size == 1)
						sr.setColor(0, 0, 1, 1);
					else if (c.armies.size == 2)
						sr.setColor(0, 1, 0, 1);
					else
						sr.setColor(1, 0, 0, 1);
					sr.polygon(c.polygon.getVertices());
				}
			}
//			
			
//			// test centersToPolygon
////			sr.end();
////			sr.begin(ShapeType.Filled);
////			if (toggle) {
////				toggle = false;
////				Array<Center> disconnected = new Array<Center>();
////				for (Center center : Faction.factions.get(3).centers) 
////					disconnected.add(center);
////				Array<Array<Center>> connected = calcConnectedCenters(disconnected);
////				for (Array<Center> array : connected)
////					testPolygons.add(centersToPolygon(array));
////			}
////			for (Polygon p : testPolygons) {
////				sr.polygon(p.getVertices());
////			}

//			
//			testPolygons.clear();
//			sr.setColor(Faction.factions.get(3).color);
//			Array<Array<Center>> aaCenters = calcConnectedCenters(Faction.factions.get(3).centers);				
//			for (Array<Center> centers : aaCenters) {
//				testPolygons.add(centersToPolygon(centers));
//			}

//			for (Polygon p : testPolygons)
//				sr.polygon(p.getVertices());
//			System.out.println("outside corner size " + findOutsideCorners(calcConnectedCenters(Faction.factions.get(3).centers).first()).size);

//			for (Corner c : testCorners) 
//				sr.circle((float) c.loc.x, (float)(Map.HEIGHT-c.loc.y), 10);
			
			// draw reference pointer
			sr.end();
			sr.begin(ShapeType.Line);
			sr.setColor(.5f,.5f,0,1);
			sr.line(0f, 0f, (float) reference.loc.x, (float)(HEIGHT-reference.loc.y));
			sr.end();
			batch.begin();
		}	
		
		if (drawSpheres) {
			batch.end();
			sr.begin(ShapeType.Filled);
			sr.setProjectionMatrix(batch.getProjectionMatrix());
			Gdx.gl.glEnable(GL20.GL_BLEND);			
	
			// draw spheres of influence
			for (Faction f : Faction.factions) {
				sr.setColor(f.color.r, f.color.g, f.color.b, .5f);
				for (Center c : f.centers) {
					for (float[] vertices : c.triangles) {
						sr.triangle(vertices[0], vertices[1], vertices[2], 
								vertices[3], vertices[4], vertices[5]);
					}
				}
			}
			
			// draw thick black borders
			sr.end();
			sr.begin(ShapeType.Line);
			Gdx.gl20.glLineWidth(3);
			sr.setColor(Color.BLACK);
			for (Edge e : borderEdges) {
				sr.line((float) (e.v0.loc.x), (float) (HEIGHT-e.v0.loc.y), (float) (e.v1.loc.x), (float) (HEIGHT-e.v1.loc.y));
				// to smooth out line
//				sr.x((float) (e.v0.loc.x), (float) (HEIGHT-e.v0.loc.y), 0);
//				sr.x((float) (e.v1.loc.x), (float) (HEIGHT-e.v1.loc.y), 0);
			}
//			for (Faction f : Faction.factions) {
//				sr.setColor(Color.BLACK);
//				for (Polygon p : f.territory)
//					sr.polygon(p.getVertices());
//			}
			sr.end();
			batch.begin();
		}
	}
}
