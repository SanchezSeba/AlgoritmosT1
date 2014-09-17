package tarea1;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class RNode {
	
	private int isLeaf; // 1 = leaf   ; 0 = node
	private long position;
	private MBR myMBR;
	private MBR[] mbr;
	private int grade;
	private long[] childrensPosition;
	private int numberOfChildrens;
	private long parentPos;
	/*
	public RNode(MBR mbr2, boolean isLeaf2, RNode parent2) {
		this.myMBR = mbr2;
		this.isLeaf = isLeaf2;
		this.parent = parent2;
		this.childrens = null;
	}*/

	public RNode(int grade, long position, int isLeaf) {
		this.myMBR = null;
		this.grade = grade;
		this.position = position;
		this.isLeaf = isLeaf;
		this.parentPos = -1L;
		this.numberOfChildrens = 0;
		this.mbr = new MBR[2 * grade + 1];
		this.childrensPosition = new long[2 * grade + 1];
	}
	
	public RNode(int leaf, int grade, long parentPos, long position) {
		this(grade, position, leaf);
		this.myMBR = null;
		this.parentPos = parentPos;
	}

	public RNode(byte[] nodeBytes) {
		int pointer = 0;
		this.isLeaf = ByteBuffer.wrap(nodeBytes, pointer, 4).getInt();
		pointer += 4;
		this.position = ByteBuffer.wrap(nodeBytes, pointer, 8).getLong();
		pointer += 8;
		this.parentPos = ByteBuffer.wrap(nodeBytes, pointer, 8).getLong();
		pointer += 8;
		this.grade = ByteBuffer.wrap(nodeBytes, pointer, 4).getInt();
		pointer += 4;
		this.numberOfChildrens = ByteBuffer.wrap(nodeBytes, pointer, 4).getInt();
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
	
	public void toBytes(byte[] nodeBytes) {
		int pointer = 0;
		ByteBuffer.wrap(nodeBytes, pointer, 4).putInt(this.isLeaf);
		pointer += 4;
		ByteBuffer.wrap(nodeBytes, pointer, 8).putLong(this.position);
		pointer += 8;
		ByteBuffer.wrap(nodeBytes, pointer, 8).putLong(this.parentPos);
		pointer += 8;
		ByteBuffer.wrap(nodeBytes, pointer, 4).putInt(this.grade);
		pointer += 4;
		ByteBuffer.wrap(nodeBytes, pointer, 4).putInt(this.numberOfChildrens);
		pointer += 4;
		for(int i=0; i < numberOfChildrens; i++){
			ByteBuffer.wrap(nodeBytes, pointer, 8).putLong(this.childrensPosition[i]);
			pointer += 8;
		}
		this.myMBR.toBytes(nodeBytes, pointer);
		pointer += 2 * 2 * 8;
		for(int i=0; i < numberOfChildrens; i++){
			this.mbr[i].toBytes(nodeBytes, pointer);
			pointer += 2 * 2 * 8;
		}
		
	}
	
	public void addMBR(MBR mbr2, long i) {
		this.mbr[this.numberOfChildrens] = mbr2;
		this.childrensPosition[this.numberOfChildrens] = i;
		this.numberOfChildrens ++;
		if(this.myMBR == null)
			this.myMBR = mbr2;
		else
			this.myMBR = this.myMBR.addMBR(mbr2);
		
	}
	
	public void addListMBR(MBR[] childrens, long[] childrensPos) {
		for(int i=0; i < childrens.length; i++){
			if(childrens[i] != null){
				addMBR(childrens[i], childrensPos[i]);
				childrens[i] = null;
			}
		}
		
	}
	
	public void replace(RNode rNode) {
		int index = -1;
		for(int i=0; i < this.numberOfChildrens; i++){
			if(rNode.getPosition() == this.childrensPosition[i])
				index = i;
		}
		this.mbr[index] = rNode.getMyMBR();
		this.myMBR = this.myMBR.addMBR(rNode.getMyMBR());
	}

	public double getArea() {
		return myMBR.getArea();
	}

	public double getPoint(int i) {		
		return myMBR.getPoint(i);
	}
	
	public MBR getMyMBR(){
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

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

	public long getParentPos() {
		return parentPos;
	}

	public void setParentPos(long parentPos) {
		this.parentPos = parentPos;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}
	
	public int getNumberOfChildrens() {
		return numberOfChildrens;
	}

	public void setNumberOfChildrens(int numberOfChildrens) {
		this.numberOfChildrens = numberOfChildrens;
	}
	
	public String toString(){
		return "Nodo: leaf:" + isLeaf + " position:" + position + " mymbr:" + myMBR.toString() + " mbrss:" + Arrays.toString(mbr)
				+ " grade:" + grade + " Childrenspos:" + Arrays.toString(childrensPosition) + " numberChildrens" +
				 numberOfChildrens + " Parentpos" + parentPos;
	}

	public void setMyMBR(MBR myMBR) {
		this.myMBR = myMBR;
	}

}
