import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CalculateSamePath {

	public static void main(String args[]) throws IOException{
		int [][]flows = new int[10000][];
		String filePath = "input_flow_2500.txt";
		int [][]topo_path_inital = new int[26][26];
		int [][]topo_path_final = new int[26][26];
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
	     System.out.println(cnt);
	     for(int i=0;i<cnt;i=i+2){
	    	 for(int j=0;j<flows[i].length-1;j++){
	    		 if(flows[i][j]!=0&&flows[i][j+1]!=0){
	    			 topo_path_inital[flows[i][j]][flows[i][j+1]]++;
	    		 }
	    	 }
	     }
	     
	     for(int i=1;i<cnt;i=i+2){
	    	 for(int j=0;j<flows[i].length-1;j++){
	    		 if(flows[i][j]!=0&&flows[i][j+1]!=0){
	    			 topo_path_final[flows[i][j]][flows[i][j+1]]++;
	    		 }
	    	 }
	     }
	     int max = 0;
	     int maxu=0;
	     int maxv=0;
	     for(int i=0;i<topo_path_inital.length;i++){
	    	 for(int j=0;j<topo_path_inital[i].length;j++){
	    		 if(topo_path_inital[i][j]>max){
	    			 max = topo_path_inital[i][j];
	    			 maxu=i;
	    			 maxv=j;
	    		 }
	    	 }
	     }
	     
	     for(int i=0;i<topo_path_final.length;i++){
	    	 for(int j=0;j<topo_path_final[i].length;j++){
	    		 if(topo_path_final[i][j]>max){
	    			 max = topo_path_final[i][j];
	    			 maxu=i;
	    			 maxv=j;
	    		 }
	    	 }
	     }
	     System.out.println("max is: "+max+" "+maxu+" "+maxv);
	}
}
