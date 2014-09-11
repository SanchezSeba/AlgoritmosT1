package tarea1;

import java.util.LinkedList;

public class RNode {
	
	private int isLeaf;
	private double position;
	private MBR myMBR;
	private MBR[] mbr;
	private RNode parent;
	private int grade;
	private double[] childrens;
	/*
	public RNode(MBR mbr2, boolean isLeaf2, RNode parent2) {
		this.myMBR = mbr2;
		this.isLeaf = isLeaf2;
		this.parent = parent2;
		this.childrens = null;
	}*/

	public RNode(int grade, double position, int isLeaf) {
		this.grade = grade;
		this.position = position;
		this.isLeaf = isLeaf;
	}

	public double getArea() {
		return myMBR.getArea();
	}

	public double getPoint(int i) {		
		return myMBR.getPoint(i);
	}
	
	public MBR getMBR(){
		return myMBR;
	}

	public double getSize(int i) {
		return myMBR.getSize(i);
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
