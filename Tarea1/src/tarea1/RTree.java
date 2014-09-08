package tarea1;

public class RTree {
	
	private RNode root;
	private int grade;
	
	public void insert(REntry n){
		
		RNode l = leafToInsert(root, n);
	}
	
	/**
	 * Selecciona el nodo o subnodo de r donde se debe insertar n
	 * @param r subarbol donde se busca el nodo donde se debe realizar la insercion
	 * @param n nodo a insertar
	 * @return nodo donde se debe insertar n
	 */
	private RNode leafToInsert(RNode r, REntry n) {
		
		if(r.isLeaf){
			return r;
		}
		
		double minInc = Double.MAX_VALUE;
		RNode node = null;
		
		for(RNode rn : r.children){
			
			double inc = calculateInc(rn, n);
			if(inc < minInc){
				minInc = inc;
				node = rn;
			}
			else if(inc == minInc){
				double nodeArea = calculateArea(node);
				double rnArea = calculateArea(rn);
				if(rnArea < nodeArea)
					node = rn;
			}
		}
		
		return leafToInsert(node, n);
	}
	
	/**
	 * Calcula el incremento de area al agregar n al nodo rn
	 * @param rn nodo
	 * @param n nodo a agregar
	 * @return incremento de area
	 */
	private double calculateInc(RNode rn, REntry n) {
		
		double area = calculateArea(rn);
		double[] diff = {0,0};
		MBR nodeMBR = rn.mbr;
		MBR entryMBR = n.mbr;
		for(int i = 0; i < 2; i++){
			
			if(nodeMBR.point[i] + nodeMBR.size[i] < entryMBR.point[i] + entryMBR.size[i]){
				diff[i] =  entryMBR.point[i] + entryMBR.size[i] - nodeMBR.point[i] + nodeMBR.size[i];
			}
			if(nodeMBR.point[i] > entryMBR.point[i]){
				diff[i] += nodeMBR.point[i] - entryMBR.point[i];
			}
		}
		
		double areaWithEntry = 1;
		for(int i = 0; i < 2; i++){
			
			areaWithEntry *= nodeMBR.size[i] + diff[i];
			
		}
		return areaWithEntry - area;
	}
	
	/**
	 * Calcula area del MBR del nodo rn
	 * @param rn nodo
	 * @return area del MBR del nodo
	 */
	private double calculateArea(RNode rn) {
		
		MBR mbr = rn.mbr;
		double area = mbr.size[0] * mbr.size[1];
		
		return area;
	}
}
