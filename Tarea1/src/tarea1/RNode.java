package tarea1;

import java.util.LinkedList;

public class RNode {
	
	private boolean isLeaf;
	private MBR mbr;
	private LinkedList<RNode> childrens;
	private RNode parent;
	
	public RNode(MBR mbr2, boolean isLeaf2, RNode parent2) {
		this.mbr = mbr2;
		this.isLeaf = isLeaf2;
		this.parent = parent2;
		this.childrens = null;
	}

	public double getArea() {
		return mbr.getArea();
	}

	public double getPoint(int i) {		
		return mbr.getPoint(i);
	}
	
	public MBR getMBR(){
		return mbr;
	}

	public double getSize(int i) {
		return mbr.getSize(i);
	}

	public LinkedList<RNode> getChildrens() {
		return childrens;
	}

	public boolean isLeaf() {
		return isLeaf;
	}

	public RNode getParent() {
		return parent;
	}
	
	public void setParent(RNode leaf) {
		parent = leaf;		
	}
}
