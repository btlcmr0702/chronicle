
public class Link {
	int from;
	int to;
	int startTiming;
	int endTiming;
	int flowid;
	int linkid;
	boolean start;
	boolean end;
	
	public void setLink(int from, int to, int startTiming, int endTiming, int flowid, int linkid,boolean start, boolean end){
		this.from = from;
		this.to = to;
		this.startTiming = startTiming;
		this.endTiming = endTiming;
		this.flowid = flowid;
		this.linkid = linkid;
		this.start = start;
		this.end = end;
	}
	
	 public boolean equals(Object obj){  
	        if(this == obj)//判断是否是本类的一个引用  
	            return true;  
	        if(obj == null)//  
	            return false;             
	        Link link = (Link)obj;  
	        if(this.from != link.from)  
	            return false;  
	        if(this.to != link.to)  
	            return false;  
	        if(this.startTiming != link.startTiming)  
	            return false;  
	        if(this.endTiming != link.endTiming)  
	            return false;  
	        if(this.flowid != link.flowid)
	            return false;
	        if(this.start != link.start)  
	            return false;  
	        if(this.end != link.end)  
	            return false;  
	        return true;  
	 }
	 
	 public int hashCode(){  
	        int result = 17;  
	        result = result * 31 + from;  
	        result = result * 31 + to;
	        result = result * 31 + startTiming;
	        result = result * 31 + endTiming;
	        result = result * 31 + flowid;
	        if(start){
		        result = result * 31 + 1;
	        }else{
		        result = result * 31 + 0;
	        }
	        return result;  
	    }  
	
	public int getFrom() {
		return from;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	public int getTo() {
		return to;
	}
	public void setTo(int to) {
		this.to = to;
	}
	public int getStartTiming() {
		return startTiming;
	}
	public void setStartTiming(int startTiming) {
		this.startTiming = startTiming;
	}
	public int getEndTiming() {
		return endTiming;
	}
	public void setEndTiming(int endTiming) {
		this.endTiming = endTiming;
	}
	public int getFlowid() {
		return flowid;
	}
	public void setFlowid(int flowid) {
		this.flowid = flowid;
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


	public int getLinkid() {
		return linkid;
	}


	public void setLinkid(int linkid) {
		this.linkid = linkid;
	}
	
	
	
	
}
