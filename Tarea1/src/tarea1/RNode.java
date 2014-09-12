package tarea1;

import java.nio.ByteBuffer;
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
		int pointer = 0;
		this.isLeaf = ByteBuffer.wrap(nodeBytes, pointer, 4).getInt();
		pointer += 4;
		this.position = ByteBuffer.wrap(nodeBytes, pointer, 8).getLong();
		pointer += 8;
		this.grade = ByteBuffer.wrap(nodeBytes, pointer, 4).getInt();
		pointer += 4;
		this.numberOfChildrens =ByteBuffer.wrap(nodeBytes, pointer, 4).getInt();
		pointer += 4;
		this.childrensPosition = new long[2 * grade + 1];
		for(int i=0; i < numberOfChildrens; i++){
			this.childrensPosition[i] = ByteBuffer.wrap(nodeBytes, pointer, 8).getLong();
			pointer += 8;
		}
		this.myMBR = new MBR(nodeBytes, pointer);
		pointer += 2 * 2 * 8;
		this.mbr = new MBR[2 * grade + 1];
		for(int i=0; i < numberOfChildrens; i++){
			this.mbr[i] = new MBR(nodeBytes, pointer);
			pointer += 2 * 2 * 8;
		}
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
