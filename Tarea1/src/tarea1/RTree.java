package tarea1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RTree {
	
	private RNode root;
	private int grade;
	private boolean isQuadratic;
	private final int blockSize = 4096;
	private long nextPosition;
	private int nodeSizeInBlocks;
	
	private RandomAccessFile file;
	
	public RTree(int grade, boolean quadratic) throws FileNotFoundException{
		this.grade = grade;
		this.isQuadratic = quadratic;
		this.nextPosition = 0;
		
		/*Peso en byte nodo:
		 *  private int isLeaf  4
			private long position; 8
			private MBR myMBR; 32
				double[] points; 8 * 2
				double[] sizes; 8 * 2
			private MBR[] mbr; 32 * 2 * grade + 32
				double[] points; 8 * 2
				double[] sizes; 8 * 2
			private int grade; 4
			private long[] childrensPosition;  8 * 2 * grade + 8
			private int numberOfChildrens; 4
			private long parentPos; 8
		 */
		double nodeSizeInBytes = 12 + (2 * grade + 3) * 8 + (2 * grade + 2) * 32;
		this.nodeSizeInBlocks = (int)Math.ceil(nodeSizeInBytes/blockSize);
		this.root = new RNode(grade, position(), 1);
		
		this.file = new RandomAccessFile("RTree.obj", "rw");
				
	}
	
	private long position() {
		long i = nextPosition;
		nextPosition += nodeSizeInBlocks * blockSize;
		return i;
	}

	public void insert(MBR mbr) throws IOException{
		/*if(root.getNumberOfChildrens() != 0)
			System.out.println(root.toString());*/
		RNode leaf = leafToInsert(root, mbr);
		leaf.addMBR(mbr, -1);
		if(leaf.getNumberOfChildrens() > grade * 2){
			RNode[] nodes = makeSplit(leaf);
			check(nodes);
		}
		else{
			checkSizeOfMBR(leaf);
		}
		//System.out.println(root.toString());
	}
	
	private void checkSizeOfMBR(RNode leaf) throws IOException {
		if(leaf.getParentPos() != -1L){
			RNode node = loadNode(leaf.getParentPos());
			//System.out.println("NODO CARGADO3" + node.toString());
			//System.out.println(leaf.toString());
			node.replace(leaf);
			saveNode(leaf);
			//System.out.println("NODO GUARDADO" + leaf.toString());
			checkSizeOfMBR(node);
		}
		else {
			saveNode(leaf);
			//System.out.println("NODO GUARDADO" + leaf.toString());
			}
	}

	private void check(RNode[] nodes) throws IOException {
		long parentPos = nodes[0].getParentPos();
		if(parentPos == -1L){
			root = new RNode(grade, position(), 0);
			root.addMBR(nodes[0].getMyMBR(), nodes[0].getPosition());
			root.addMBR(nodes[1].getMyMBR(), nodes[1].getPosition());
			nodes[0].setParentPos(root.getPosition());
			nodes[1].setParentPos(root.getPosition());
			saveNode(nodes[0]);
			saveNode(nodes[1]);
			saveNode(root);
			//System.out.println("NODO GUARDADO" + nodes[0].toString());
			//System.out.println("NODO GUARDADO" + nodes[1].toString());
			//System.out.println("NODO GUARDADO" + root.toString());
			return;
		}
		RNode r = loadNode(parentPos);
		r.addMBR(nodes[1].getMyMBR(), nodes[1].getPosition());
		//System.out.println("NODO CARGADO1" + r.toString());
		//System.out.println(nodes[0].toString());
		r.replace(nodes[0]);
		saveNode(nodes[0]);
		saveNode(nodes[1]);
		//System.out.println("NODO GUARDADO" + nodes[0].toString());
		//System.out.println("NODO GUARDADO" + nodes[1].toString());
		if(r.getNumberOfChildrens() > grade * 2){
			RNode[] nodes2 = makeSplit(r);
			check(nodes2);
		}
		else{
			checkSizeOfMBR(r);
		}
	}

	private RNode[] makeSplit(RNode leaf) throws IOException {
		//System.out.println("Split");
		RNode[] nodes = new RNode[]{leaf, new RNode(leaf.isLeaf(), 
				leaf.getGrade(), leaf.getParentPos(), position())};
		MBR[] childrens = leaf.getMbr();
		long[] childrensPos = leaf.getChildrensPosition();
		leaf.setChildrensPosition(new long[2 * grade + 1]);
		leaf.setMbr(new MBR[2 * grade + 1]);
		leaf.setNumberOfChildrens(0);
		leaf.setMyMBR(null);
		int[] nodeGroup = isQuadratic ? makeQuadraticGroup(childrens) : makeLinearGroup(childrens); 
		//System.out.println(nodeGroup[0] + " " + nodeGroup[1]);
		nodes[0].addMBR(childrens[nodeGroup[0]], childrensPos[nodeGroup[0]]);
		nodes[1].addMBR(childrens[nodeGroup[1]], childrensPos[nodeGroup[1]]);
		refreshParent(childrensPos[nodeGroup[0]], nodes[0].getPosition());
		refreshParent(childrensPos[nodeGroup[1]], nodes[1].getPosition());
		childrens[nodeGroup[0]] = null;
		childrens[nodeGroup[1]] = null;
		while(notEmpty(childrens)){
			if(nodes[0].getNumberOfChildrens() > grade){
				for(int i=0; i < childrens.length; i++){
					if(childrens[i] != null){
						nodes[1].addMBR(childrens[i], childrensPos[i]);
						refreshParent(childrensPos[i], nodes[1].getPosition());
						childrens[i] = null;
					}
				}
				return nodes;
			}
			else if(nodes[1].getNumberOfChildrens() > grade){
				for(int i=0; i < childrens.length; i++){
					if(childrens[i] != null){
						nodes[0].addMBR(childrens[i], childrensPos[i]);
						refreshParent(childrensPos[i], nodes[0].getPosition());
						childrens[i] = null;
					}
				}
				return nodes;
			}
			int i = isQuadratic ? selectNextQ(childrens, nodes) : selectNextL(childrens, nodes);
			double area1 = incAddMBR(childrens[i], nodes[0]);
			double area2 = incAddMBR(childrens[i], nodes[1]);
			if(area1 > area2){
				nodes[1].addMBR(childrens[i], childrensPos[i]);
				refreshParent(childrensPos[i], nodes[1].getPosition());
				//System.out.println("Agregado a la DER");
			}
			else if(area2 > area1){
				nodes[0].addMBR(childrens[i], childrensPos[i]);
				refreshParent(childrensPos[i], nodes[0].getPosition());
				//System.out.println("Agregado a la IZQ");
			}
			else{
				if(nodes[0].getArea() > nodes[1].getArea()){
					nodes[1].addMBR(childrens[i], childrensPos[i]);
					refreshParent(childrensPos[i], nodes[1].getPosition());
				}
				else if(nodes[0].getArea() < nodes[1].getArea()){
					nodes[0].addMBR(childrens[i], childrensPos[i]);
					refreshParent(childrensPos[i], nodes[0].getPosition());
				}
				else{
					if(nodes[0].getNumberOfChildrens() < nodes[1].getNumberOfChildrens()){
						nodes[0].addMBR(childrens[i], childrensPos[i]);
						refreshParent(childrensPos[i], nodes[0].getPosition());
					}
					else{
						nodes[1].addMBR(childrens[i], childrensPos[i]);
						refreshParent(childrensPos[i], nodes[1].getPosition());
					}
				}
			}
			childrens[i] = null;
		}
		return nodes;
	}


	private void refreshParent(long l, long parentPos) throws IOException {
		if(l != -1){
			RNode node = loadNode(l);
			node.setParentPos(parentPos);
			saveNode(node);
		}
	}

	private int selectNextQ(MBR[] childrens, RNode[] nodes) {
		double maxArea = - Double.MAX_VALUE;
		int index = -1;
		for(int i=0; i < childrens.length; i++){
			if(childrens[i] != null){
				double area1 = incAddMBR(childrens[i], nodes[0]);
				double area2 = incAddMBR(childrens[i], nodes[1]);
				double diff = Math.abs(area1- area2);
				if(diff > maxArea){
					maxArea = diff;
					index = i;
				}
			}
		}
		//System.out.println(index);
		return index;
	}

	private double incAddMBR(MBR mbr, RNode rNode) {
		double nodeArea = rNode.getArea();
		double areaWithMBR = 1;
		for(int i=0; i < 2; i++){
			double min = Math.min(rNode.getPoint(i), mbr.getPoint(i));
			double max = Math.max(rNode.getPoint(i) + rNode.getSize(i), 
					mbr.getPoint(i) + mbr.getSize(i));
			areaWithMBR *= max - min;
		}
		return areaWithMBR - nodeArea;
	}

	private int selectNextL(MBR[] childrens, RNode[] nodes) {
		// TODO Auto-generated method stub
		return 0;
	}

	private boolean notEmpty(MBR[] childrens) {
		for(int i=0; i < childrens.length; i++){
			if(childrens[i] != null)
				return true;
		}
		return false;
	}

	private int[] makeLinearGroup(MBR[] children) {
		// TODO Auto-generated method stub
		return null;
	}

	private int[] makeQuadraticGroup(MBR[] children) {
		
		double maxInc = - Double.MAX_VALUE;
		int[] nodes = {-1, -1};
		for(int i=0; i < children.length; i++){
			for(int j=i+1; j < children.length; j++){
				double area = 1;
				for(int k=0; k < 2; k++){
					double min = Math.min(children[i].getPoint(k), children[j].getPoint(k));
					double max = Math.max(children[i].getPoint(k) + children[i].getSize(k), 
							children[j].getPoint(k) + children[j].getSize(k));
					area *= max - min;
				}
				double areaInc = Math.abs(area) - (children[i].getArea() + children[j].getArea());
				//System.out.println(areaInc);
				if(areaInc > maxInc){
					maxInc = areaInc;
					nodes[0] = i;
					nodes[1] = j;
				}
			}
		}
		
		return nodes;
	}

	private RNode leafToInsert(RNode r, MBR mbr) throws IOException {
		
		if(r.isLeaf() == 1){
			return r;
		}
		
		double minInc = Double.MAX_VALUE;
		int indexBestInc = -1;
		
		for(int i=0; i < r.getNumberOfChildrens(); i++){			
			double inc = calculateInc(r, mbr, i);
			if(inc < minInc){
				minInc = inc;
				indexBestInc = i;
			}
			else if(inc == minInc && indexBestInc != -1){
				double bestIncArea = r.getAreaMBRIndex(indexBestInc);
				double actualMBRArea = r.getAreaMBRIndex(i);
				if(actualMBRArea < bestIncArea)
					indexBestInc = i;
			}
		}
		
		long childrenPosition = r.getChildrenPositionIndex(indexBestInc);
		RNode node = loadNode(childrenPosition);
		//System.out.println("NODO CARGADO2" + node.toString());
		return leafToInsert(node, mbr);
	}
	
	private RNode loadNode(long childrenPosition) throws IOException {
		file.seek(childrenPosition);
		byte[] nodeBytes = new byte[(nodeSizeInBlocks * blockSize)];
		file.read(nodeBytes);
		return new RNode(nodeBytes);
	}
	
	private void saveNode(RNode leaf) throws IOException {
		file.seek(leaf.getPosition());
		byte[] nodeBytes = new byte[(nodeSizeInBlocks * blockSize)];
		leaf.toBytes(nodeBytes);
		file.write(nodeBytes);
	}

	private double calculateInc(RNode node, MBR mbr, int index) {		
		double area = node.getAreaMBRIndex(index);
		double areaWithNewMBR = 1;
		for(int i=0; i < 2; i++){
			double min = Math.min(node.getPointMBRIndex(i, index), mbr.getPoint(i));
			double max = Math.max(node.getPointMBRIndex(i, index) + node.getSizeMBRIndex(i, index), 
					mbr.getPoint(i) + mbr.getSize(i));
			areaWithNewMBR *= max - min;
		}
		return Math.abs(areaWithNewMBR) - area;
	}
	
	public static void main(String[] args) throws IOException {
		RTree rtree = new RTree(1, true);
		double[] point = new double[2];
		double[] size = new double[2];
		for (int i = 0; i < 1000; i++) {
			point[0] = Math.round(Math.random()*500000);
			point[1] = Math.round(Math.random()*500000);
			size[0] = Math.round(Math.random()*(100-1))+1;
			size[1] = Math.round(Math.random()*(100-1))+1;
			//System.out.println("Agregando : : : " + new MBR(point, size).toString());
			rtree.insert(new MBR(point, size));
			//System.out.println("add");
		}
		/*point[0]= 6.2;
		point[1]=6.3;
		size[0] = 1000;
		size[1] = 1000;
		rtree.insert(new MBR(point, size));*/
		System.out.println(rtree.root.getMyMBR().toString());
		RNode nod = rtree.loadNode(rtree.root.getChildrenPositionIndex(0));
		RNode nod1 = rtree.loadNode(rtree.root.getChildrenPositionIndex(1));
		System.out.println(nod.getMyMBR().toString());
		System.out.println(nod1.getMyMBR().toString());
		RNode n2 =rtree.loadNode(nod.getChildrenPositionIndex(0));
		RNode n3 =rtree.loadNode(nod.getChildrenPositionIndex(1));
		System.out.println(n2.getMyMBR().toString());
		System.out.println(n3.getMyMBR().toString());
	}
}
