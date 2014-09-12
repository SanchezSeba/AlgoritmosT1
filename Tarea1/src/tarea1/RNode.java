package tarea1;

import java.util.LinkedList;

public class RNode {
	
	private int isLeaf;
	private long position;
	private MBR myMBR;
	private MBR[] mbr;
	private int grade;
	private long[] childrensPosition;
	private int numberOfChildrens;
	/*
	public RNode(MBR mbr2, boolean isLeaf2, RNode parent2) {
		this.myMBR = mbr2;
		this.isLeaf = isLeaf2;
		this.parent = parent2;
		this.childrens = null;
	}*/

	public int getNumberOfChildrens() {
		return numberOfChildrens;
	}

	public void setNumberOfChildrens(int numberOfChildrens) {
		this.numberOfChildrens = numberOfChildrens;
	}

	public RNode(int grade, long position, int isLeaf) {
		this.grade = grade;
		this.position = position;
		this.isLeaf = isLeaf;
	}

	public RNode(byte[] nodeBytes) {
		
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


	public int isLeaf() {
		return isLeaf;
	}


	public MBR[] getMbr() {
		return mbr;
	}

	public void setMbr(MBR[] mbr) {
		this.mbr = mbr;
	}
	
	public MBR getMBRWithIndex(int index){
		return mbr[index];
	}

	public double getAreaMBRIndex(int index) {		
		return mbr[index].getArea() ;
	}

	public double getPointMBRIndex(int i, int index) {
		return mbr[index].getPoint(i);
	}

	public double getSizeMBRIndex(int i, int index) {
		return mbr[index].getSize(i);
	}

	public long[] getChildrensPosition() {
		return childrensPosition;
	}

	public void setChildrensPosition(long[] childrensPosition) {
		this.childrensPosition = childrensPosition;
	}

	public long getChildrenPositionIndex(int indexBestInc) {
		return childrensPosition[indexBestInc];
	}
}
