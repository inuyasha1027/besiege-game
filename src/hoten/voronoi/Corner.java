/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hoten.voronoi;

import hoten.geom.PointH;
import java.util.ArrayList;

/**
 * Corner.java Function Date Jun 6, 2013
 *
 * @author Connor
 * modified by Kyle
 */
public class Corner {

    public ArrayList<Center> touches = new ArrayList(); //good
    public ArrayList<Corner> adjacent = new ArrayList(); //good
    public ArrayList<Edge> protrudes = new ArrayList();
    public ArrayList<Corner> visibleCorners;
    // distance to bordercorner at index, if non-null/0 then it's visible.
//    public ArrayList<Double> visibleDistance;
    public PointH loc;
    public int index;
    public boolean border;
    public boolean waterBorder;
//    public boolean blocked;
    
    public double elevation;
    public boolean water, ocean, coast;
    public Corner downslope;
    public int river;
    public double moisture; 
}
