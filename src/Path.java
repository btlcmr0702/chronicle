
public class Path {
	int u;
	int v;
	double capacity;
	boolean start;
	boolean end;
	
	public void setPath(int u, int v, double capacity,boolean start, boolean end){
		this.u = u;
		this.v = v;
		this.capacity = capacity;
		this.start = start;
		this.end = end;
	}
	
	public int getU() {
		return u;
	}
	public void setU(int u) {
		this.u = u;
	}
	public int getV() {
		return v;
	}
	public void setV(int v) {
		this.v = v;
	}
	public double getCapacity() {
		return capacity;
	}
	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}

	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

	public boolean isEnd() {
		return end;
	}

	public void setEnd(boolean end) {
		this.end = end;
	}
	
	
}
