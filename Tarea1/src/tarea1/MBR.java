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

	public double getArea() {
		return sizes[0] * sizes[1];
	}

	public double getPoint(int i) {
		return points[i];
	}

	public double getSize(int i) {
		return sizes[i];
	}
}
