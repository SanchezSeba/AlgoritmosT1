package tarea1;

import java.nio.ByteBuffer;

public class MBR {
	
	double[] points;
	double[] sizes;
	
	public MBR(byte[] nodeBytes, int pointer) {
		this.points = new double[2];
		this.sizes = new double[2];
		for(int i=0; i < 2; i++){
			this.points[i] = ByteBuffer.wrap(nodeBytes, pointer, 8).getDouble();
			pointer += 8;
		}
		for(int i=0; i < 2; i++){
			this.sizes[i] = ByteBuffer.wrap(nodeBytes, pointer, 8).getDouble();
			pointer += 8;
		}
	}

	public MBR(MBR mbr, MBR mbr2) {
		this.points = new double[2];
		this.sizes = new double[2];
		for(int i=0; i < 2; i++){
			this.points[i] = Math.min(mbr.getPoint(i), mbr2.getPoint(i));
			this.sizes[i] = Math.max(mbr.getPoint(i) + mbr.getSize(i), 
					mbr2.getPoint(i) + mbr2.getSize(i)) - this.points[i];
		}
	}
	
	public MBR(double[] point, double[] size) {
		this.points = new double[2];
		this.sizes = new double[2];
		this.points[0] = point[0];
		this.points[1] = point[1];
		this.sizes[0] = size[0];
		this.sizes[1] = size[1];
	}

	public MBR(double px, double py, double sx, double sy) {
		this.points = new double[2];
		this.sizes = new double[2];
		this.points[0] = px;
		this.points[1] = py;
		this.sizes[0] = sx;
		this.sizes[1] = sy;
	}

	public void toBytes(byte[] nodeBytes, int pointer) {
		for(int i=0; i < 2; i++){
			ByteBuffer.wrap(nodeBytes, pointer, 8).putDouble(this.points[i]);
			pointer += 8;
		}
		for(int i=0; i < 2; i++){
			ByteBuffer.wrap(nodeBytes, pointer, 8).putDouble(this.sizes[i]);
			pointer += 8;
		}
		
	}

	public double getArea() {
		return sizes[0] * sizes[1];
	}

	public double getPoint(int i) {
		return points[i];
	}

	public double getSize(int i) {
		return sizes[i];
	}

	public MBR addMBR(MBR mbr2) {
		return new MBR(this, mbr2);
	}
	
	public String toString(){
		return "points:" + points[0] + "," + points[1] + " size:" + sizes[0] + "," + sizes[1];
	}
}
