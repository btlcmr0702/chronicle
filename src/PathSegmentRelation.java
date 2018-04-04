
public class PathSegmentRelation {
	int pathId;
	int segmentId;
	int relation; // 0 represent path ---> segment; 1 represent segment ---> path
	
	public void setParameter(int pid,int sid, int relation){
		this.pathId = pid;
		this.segmentId = sid;
		this.relation = relation;
	}
	
	public int getPathId() {
		return pathId;
	}
	public void setPathId(int pathId) {
		this.pathId = pathId;
	}
	public int getSegmentId() {
		return segmentId;
	}
	public void setSegmentId(int segmentId) {
		this.segmentId = segmentId;
	}
	public int getRelation() {
		return relation;
	}
	public void setRelation(int relation) {
		this.relation = relation;
	}
					
	
}
