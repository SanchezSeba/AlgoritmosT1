package tarea1;

import Nodo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;

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
			private MBR[] mbr; 32 * 2 * grade
				double[] points; 8 * 2
				double[] sizes; 8 * 2
			private int grade; 4
			private long[] childrensPosition;  8 * 2 * grade
			private int numberOfChildrens; 4
		 */
		double nodeSizeInBytes = 12 + (2 * grade + 1) * 8 + (2 * grade + 1) * 32;
		this.nodeSizeInBlocks = (int)Math.ceil(nodeSizeInBytes/blockSize);
		this.root = new RNode(grade, position(), 1);
		
		this.file = new RandomAccessFile("RTree.obj", "rw");
				
	}
	
	private long position() {
		long i = nextPosition;
		nextPosition += nodeSizeInBlocks * blockSize;
		return i;
	}

	public void insert(MBR mbr){
		
		RNode leaf = leafToInsert(root, mbr);
		leaf.getChildrens().add(n);
		n.setParent(leaf);
		
		if(leaf.getChildrens().size() >= grade * 2){
			RNode[] nodes = makeSplit(leaf);
		}
	}
	
	private RNode[] makeSplit(RNode leaf) {
		
		RNode[] nodes = new RNode[]{leaf, new RNode(leaf.getMBR(), leaf.isLeaf(), leaf.getParent())};
		if(leaf.getParent() != null){
			leaf.getParent().getChildrens().add(nodes[1]);
		}
		LinkedList<RNode> childrens = leaf.getChildrens();
		leaf.getChildrens().clear();
		RNode[] nodeGroup = isQuadratic ? makeQuadraticGroup(childrens) : makeLinearGroup(childrens); 
		
		return null;
	}


	private RNode[] makeLinearGroup(LinkedList<RNode> children) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Crea 2 grupos iniciales con los rectangulos R1 y R2 cuyo incremento  de area es maximo si es que
	 * fueran puestos en el mismo grupo
	 * 
	 * @param children lista de nodos
	 * @return RNode[] con 2 grupos, uno con R1 y otro con R2
	 */
	private RNode[] makeQuadraticGroup(LinkedList<RNode> children) {
		
		double maxInc = Double.MIN_VALUE;
		RNode[] nodes = new RNode[2];
		for(int i=0; i < children.size(); i++){
			for(int j=i+1; j < children.size(); j++){
				RNode node1 = children.get(i);
				RNode node2 = children.get(j);
				double area = 1;
				for(int k=0; k < 2; k++){
					double min = Math.min(node1.getPoint(i), node2.getPoint(i));
					double max = Math.max(node1.getPoint(i) + node1.getSize(i), node2.getPoint(i) + node2.getSize(i));
					area *= max - min;
				}
				double areaInc = Math.abs(area) - (node1.getArea() + node2.getArea());
				if(areaInc > maxInc){
					maxInc = areaInc;
					nodes[0] = node1;
					nodes[1] = node2;
				}
			}
		}
		children.remove(nodes[0]);
		children.remove(nodes[1]);
		
		return nodes;
	}

	private RNode leafToInsert(RNode r, MBR mbr) {
		
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
		
		return leafToInsert(node, n);
	}
	
	private RNode loadNode(long childrenPosition) throws IOException {
		file.seek(childrenPosition);
		byte[] nodeBytes = new byte[(nodeSizeInBlocks * blockSize)];
		file.read(nodeBytes);
		return new RNode(nodeBytes);
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
}
