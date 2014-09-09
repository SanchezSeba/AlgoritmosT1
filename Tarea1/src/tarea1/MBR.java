package tarea1;

public class MBR {
	
	double[] points;
	double[] sizes;
	
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
