import java.awt.image.Raster;

public class tcCmdScript {
	
	public static void main(String args[]) {
		
			int classID=2;
			String rate="10mbit";
			int dport = 4000;
			int flowId = 2;
			int num = 120;
			
		/*	for(int i=0;i<num;i++){
				System.out.println("sudo tc class add dev em3 parent 1:1 classid 1:"+classID+" htb rate "+rate+" ceil "+rate+" prio 2");
				classID++;
			}*/
			
			for(int i=0;i<num;i++){
				System.out.println("sudo tc filter add dev em3 protocol ip parent 1: prio 2 u32 match ip dport "+dport+" 0xffff flowid 1:"+flowId);
				dport++;
				flowId++;
			}
	}
}
