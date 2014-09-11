package tarea1;

import java.util.LinkedList;

public class RTree {
	
	private RNode root;
	private int grade;
	private boolean quadratic;
	private final int blockSize = 4096;
	private double nextPosition;
	private double nodeInBlockSize;
	
	public RTree(int grade, boolean quadratic){
		this.grade = grade;
		this.quadratic = quadratic;
		this.nextPosition = 0;
		double nodeInByteSize = 0; //CALCULAR PESO DE UN NODO
		this.nodeInBlockSize = Math.ceil(nodeInByteSize/blockSize);
		this.root = new RNode(grade, nextPosition(), 1);
				
	}
	
	private double nextPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void insert(REntry n){
		
		RNode leaf = leafToInsert(root, n);
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
		RNode[] nodeGroup = quadratic ? makeQuadraticGroup(childrens) : makeLinearGroup(childrens); 
		
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

	/**
	 * Selecciona el nodo de r donde se debe insertar n
	 * 
	 * @param r subarbol donde se busca el nodo donde se debe realizar la insercion
	 * @param n nodo a insertar
	 * @return nodo donde se debe insertar n
	 */
	private RNode leafToInsert(RNode r, REntry n) {
		
		if(r.isLeaf()){
			return r;
		}
		
		double minInc = Double.MAX_VALUE;
		RNode node = null;
		
		for(RNode rn : r.getChildrens()){
			
			double inc = calculateInc(rn, n);
			if(inc < minInc){
				minInc = inc;
				node = rn;
			}
			else if(inc == minInc){
				double nodeArea = node.getArea();
				double rnArea = rn.getArea();
				if(rnArea < nodeArea)
					node = rn;
			}
		}
		
		return leafToInsert(node, n);
	}
	
	/**
	 * Calcula el incremento de area al agregar n al nodo rn
	 * 
	 * @param rn nodo
	 * @param n nodo a agregar
	 * @return incremento de area
	 */
	private double calculateInc(RNode rn, REntry n) {
		
		double area = rn.getArea();
		double areaWithEntry = 1;
		for(int i=0; i < 2; i++){
			double min = Math.min(rn.getPoint(i), n.getPoint(i));
			double max = Math.max(rn.getPoint(i) + rn.getSize(i), n.getPoint(i) + n.getSize(i));
			areaWithEntry *= max - min;
		}
		return Math.abs(areaWithEntry) - area;
	}
}
