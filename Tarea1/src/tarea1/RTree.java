package tarea1;

public class RTree {
	
	private RNode root;
	private int grade;
	
	public void insert(REntry n){
		
		RNode l = leafToInsert(root, n);
	}

	private RNode leafToInsert(RNode r, REntry n) {
		
		if(r.isLeaf){
			return r;
		}
		
		double minInc = Double.MAX_VALUE;
		RNode node = null;
		
		for(RNode rn : r.children){
			
			double inc = calculateInc(rn, n);
		}
		return null;
	}

	private double calculateInc(RNode rn, REntry n) {
		
		double area = calculateArea(rn);
		double[] diff = {0,0};
		MBR nodeMBR = rn.mbr;
		MBR entryMBR = n.mbr;
		for(int i=0; i<2; i++){
			
			if(nodeMBR.point[i] + nodeMBR.size[i] < entryMBR.point[i] + entryMBR.size[i]){
				diff[i] =  entryMBR.point[i] + entryMBR.size[i] - nodeMBR.point[i] + nodeMBR.size[i];
			}
			if(nodeMBR.point[i] > entryMBR.point[i]){
				diff[i] += nodeMBR.point[i] - entryMBR.point[i];
			}
		}
		return 0;
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
