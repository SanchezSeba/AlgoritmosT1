package tarea1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;

import com.vividsolutions.jts.geom.MultiLineString;

public class RTree {
	
	private RNode root;
	private int grade;
	private boolean isQuadratic;
	private final int blockSize = 4096;
	private long nextPosition;
	private int nodeSizeInBlocks;	
	private RandomAccessFile file;
	
	int diskAccess;
	
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
		this.diskAccess = 0;
				
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
		this.root = loadNode(root.getPosition());
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
		for(int i=0; i < childrens.length; i++){
			if(childrens[i] != null)
				return i;
		}
		//no deberia llegar hasta aqui
		return -1;
	}

	private boolean notEmpty(MBR[] childrens) {
		for(int i=0; i < childrens.length; i++){
			if(childrens[i] != null)
				return true;
		}
		return false;
	}

	private int[] makeLinearGroup(MBR[] children) {
		int[] nodes = {0, 0};
		double best = 0;
		for(int i=0; i < 2; i++){
			double minPoint = Double.MAX_VALUE;
			double maxPoint = - Double.MAX_VALUE;
			double maxMinPoint = - Double.MAX_VALUE;
			double minMaxPoint = Double.MAX_VALUE;
			int[] index = {-1, -1};
			for(int j=0; j < children.length; j++){
				if(children[j].getPoint(i) < minPoint)
					minPoint = children[j].getPoint(i);
				if(children[j].getPoint(i) + children[j].getSize(i) > maxPoint)
					maxPoint = children[j].getPoint(i) + children[j].getSize(i);
				if(children[j].getPoint(i) > maxMinPoint){
					maxMinPoint = children[j].getPoint(i);
					index[0] = j; 
				}
				if(children[j].getPoint(i) + children[j].getSize(i) < minMaxPoint){
					minMaxPoint = children[j].getPoint(i) + children[j].getSize(i);
					index[1] = j;
				}					
			}
			double best2 = Math.abs((minMaxPoint - maxMinPoint)/(maxPoint - minPoint));
			if(best2 > best){
				nodes[0] = index[0];
				nodes[1] = index[1];
				best = best2;
			}
		}
		return nodes;
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
		diskAccess++; //para contar accesos a disco por busqueda
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
	
	public LinkedList<MBR> search(MBR mbr) throws IOException{
		LinkedList<MBR> list = new LinkedList<MBR>();
		search(mbr, root, list);
		return list;
	}
	

	private void search(MBR mbr, RNode node, LinkedList<MBR> list) throws IOException {
		for(int i=0; i < node.getNumberOfChildrens(); i++){
			if(intersection(mbr, node.getMBRWithIndex(i))){
				if(node.isLeaf() == 1){
					list.add(node.getMBRWithIndex(i));
				}
				else{
					RNode node2 = loadNode(node.getChildrenPositionIndex(i));
					search(mbr, node2, list);
				}
			}
		}		
	}

	private boolean intersection(MBR mbr, MBR nodeMBR) {
		for(int i=0; i < 2; i++){
			boolean intersection = false;
			if(mbr.getPoint(i) <= nodeMBR.getPoint(i)){
				if(mbr.getPoint(i) + mbr.getSize(i) >= nodeMBR.getPoint(i)){
					intersection = true;
				}
			}
			else if(mbr.getPoint(i) >= nodeMBR.getPoint(i)){
				if(mbr.getPoint(i) <= nodeMBR.getPoint(i) + nodeMBR.getSize(i)){
					intersection = true;
				}
			}
			if(!intersection)
				return false;
		}
		return true;
	}
	
	//generador de puntos uniformemente distribuidos
	public static LinkedList<MBR> randomGenerator(double size){
		LinkedList<MBR> mbrs = new LinkedList<MBR>();
		for(int i=0; i < size; i++) {
			double px = getHaltonNumber(i, 2, 499900); //499900 para no tener rectangulos fuera
			double py = getHaltonNumber(i, 3, 499900);
			double area = getHaltonNumber(i, 2, 100);
			
			double sx = getHaltonNumber(i, 2, area); 
			double sy = area / sx;
			mbrs.add(new MBR(px, py, sx, sy));
		
		}
		return mbrs;
	}
	
	//para generar puntos uniformemente distribuidos
	static double getHaltonNumber(int index, int base, double size) {
		index++;

		double x = 0;
		double factor = 1.0/base;
		while(index > 0) {
			x += (index % base) * factor;
			factor /= base;
			index /= base;
		}
		return x*size;
	}	
	
	//genera rectangulos para consultar
	static public LinkedList<MBR> totalRandom(double size){
		LinkedList<MBR> mbrs = new LinkedList<MBR>();
		Random rand = new Random();
		int max = 500000;
		for(int i=0; i < size; i++) {
			double px = rand.nextInt(max + 1);
			double py = rand.nextInt(max + 1);
			
			double sx = rand.nextInt(max + 1);
			double sy = rand.nextInt(max + 1);
			mbrs.add(new MBR(px, py, sx, sy));
		
		}
		return mbrs;
	}
	
	static public LinkedList<MBR> getRealData() throws IOException{
		LinkedList<MBR> list = new LinkedList<MBR>();
		File f = JFileDataStoreChooser.showOpenFile("shp", null);
	    if (f == null) {
	    	return list;
	    }        

	    Map<String, Serializable> map = new HashMap<>();
	    map.put( "url", f.toURI().toURL() );

	    DataStore dataStore = DataStoreFinder.getDataStore( map );
	    String typeName = dataStore.getTypeNames()[0];

	    FeatureSource source = dataStore.getFeatureSource( typeName );

	    FeatureCollection collection =  source.getFeatures();
	    FeatureIterator<SimpleFeature> results = collection.features();
	    
	    try {
            while (results.hasNext()) {
                SimpleFeature feature = (SimpleFeature) results.next();
                BoundingBox b = feature.getBounds();
                list.add(new MBR(b.getMinX(), b.getMinY(),b.getWidth() , b.getHeight()));
            }
        } finally {
            results.close();
        }
        dataStore.dispose();
        return list;
	}

	public static void main(String[] args) throws IOException {
		
		//Para datos random
		double sizeTest = Math.pow(2, 4);//tamaño del conjunto
		LinkedList<MBR> rectangles = randomGenerator(sizeTest); 
		
		//desmarcar para obtener datos reales
		//LinkedList<MBR> rectangles = getRealData();
		//double sizeTest = 10482;
		//para datos reales al momento de ejecutar el programa se pedira que selecciones el archivo
		// .shp con los datos correspondientes. Se debe tener GeoTools instalado
		
		//desde aqui medir tiempo
		RTree rtree = new RTree(24, false);
		while(!rectangles.isEmpty()) {
			rtree.insert(rectangles.pop());
		}
		//Hasta aqui medimos tiempo
		
		System.out.println(rtree.diskAccess); //numero de accesos a disco total para insertar
		
		//reseteamos accesos a disco para saber cuantos hacen las busquedas
		rtree.diskAccess = 0;
		
		LinkedList<MBR> toSearch = totalRandom(sizeTest/10);
		
		//desde aqui medir tiempo
		while(!toSearch.isEmpty()) {
			rtree.search(toSearch.pop());
		}
		//Hasta aqui medimos tiempo
		
		//numero de accesos a disco total para todas las busquedas
		System.out.println(rtree.diskAccess);
		
	}
}
