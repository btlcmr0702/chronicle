import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ControlOverhead {

	public static int[] calControlOverheadNOP(int[][]flows,int cnt){
		//flag: 0 - timed; 1 - nop; 2 - tpp
		int[] flowControlOverhead = new int[cnt];
		for(int i=0;i<cnt;i=i+2){
			if(flows[i].length==0) continue;
			HashMap<Integer, Integer> initalFlow = new HashMap<Integer,Integer>();
			for(int j=0;j<flows[i].length-1;j++){
				initalFlow.put(flows[i][j],1);
			}

			for(int j=0;j<flows[i+1].length-1;j++){
				if(initalFlow.containsKey(flows[i+1][j])){
					initalFlow.remove(flows[i+1][j]);
				}
				flowControlOverhead[i/2]++;
			}
			flowControlOverhead[i/2]+=initalFlow.size();
		}
	
		return flowControlOverhead;
	}
	
	public static int[] calControlOverheadTPP(int[][]flows,int cnt){
		//flag: 0 - timed; 1 - nop; 2 - tpp
		int[] flowControlOverhead = new int[cnt];
		for(int i=0;i<cnt;i=i+2){
			int initalNodes = flows[i].length;
			int finalNodes = flows[i+1].length;
			flowControlOverhead[i/2]+=initalNodes+finalNodes;
		}
	
		return flowControlOverhead;
	}
	public static void main(String args[]) throws IOException{
		
		int [][]flows = new int[10000][];
		String filePath = "E:\\eclipseworkspace\\TimedUpdate\\flows_input\\input_flow_2500.txt";

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));  
		
		//read flows from file
		 int cnt=0;
	     for (String line = br.readLine(); line != null; line = br.readLine()) {  
	    	String []s = line.split(" ");
	    	flows[cnt] = new int[s.length];
	    	for(int i=0;i<s.length;i++){
	    		if(s[i]!=" "){
	    			flows[cnt][i] = Integer.parseInt(s[i]);
	    		}
	    	}
	    	cnt++;
	     }
	     
	     int[] flowControlOverheadNOP =  calControlOverheadNOP(flows,cnt); 
	     int[] flowControlOverheadTPP =  calControlOverheadTPP(flows,cnt);
	     double avgNop=0;
	     double avgTpp=0;
	     for(int i=0;i<flowControlOverheadNOP.length;i++){
	    	 if(flowControlOverheadNOP[i]!=0){
	    		// System.out.println(i+" "+ flowControlOverheadNOP[i]);
	    		 avgNop+=flowControlOverheadNOP[i];
	    		// System.out.println(i+" "+flowControlOverheadTPP[i]);
	    	 }
	     }
	    // System.out.println("****************************");
	     for(int i=0;i<flowControlOverheadTPP.length;i++){
	    	 if(flowControlOverheadTPP[i]!=0){
	    		// System.out.println(i+" "+flowControlOverheadTPP[i]);
	    		 avgTpp+=flowControlOverheadTPP[i];
	    	 }
	     }
	     int flowNum = cnt/2;
	     System.out.println("flowNum:"+flowNum+" NOP: "+(avgNop));
	     System.out.println("flowNum:"+flowNum+" TPP: "+(avgTpp));

	}
}
