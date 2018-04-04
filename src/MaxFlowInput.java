import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class MaxFlowInput {
	public static int [][] topology = new int[26][26];
	  
	  
	public static int getRandom(int min,int max){
		Random random = new Random();
		int s = random.nextInt(max)%(max-min+1) + min;
		return s;
	}
	
	
/*	public static int DFS(int begin,int end,int[]visited,int[][]topo,int[]route,int index){
		if(visited[begin] == 1) return 0;
		visited[begin] = 1;
		route[index] = begin;
		index++;
		if(begin == end){
			return 1;
		}
		for(int i=0;i<topo[begin].length;i++){
			if(topo[begin][i]==1){
				//System.out.println("begin: "+begin+" to: "+i);
				if(DFS(i,end,visited,topo,route,index)==1){
					return 1;
				}
			}
		}
		route[--index] = 0;
		return 0;
	}
	
	public static void showPath(int [] route){
		for(int i=1;i<route.length;i++){
			if(route[i]!=0)
			System.out.print(+route[i]+" ");
		}
		System.out.println();
	}
	
	public static void printPath(int [] route) throws IOException{
		FileWriter fw1 = new FileWriter("E:\\eclipseworkspace\\TimedUpdate\\input_flow.txt",true);
			
		for(int i=1;i<route.length;i++){
			if(route[i]!=0)
			fw1.append(route[i]+" ");
			fw1.flush();
		}
		fw1.append("\n");
		fw1.flush();
		//fw1.close();
	}
	*/
	public static double getAverage(double[] array){
        double sum = 0;
        int cnt=0;
        for(int i = 0;i < array.length;i++){
        	if(array[i]>0){
                sum += array[i];
                cnt++;
        	}
        }
        return (double)(sum / cnt);
    }
   
    public static double getStandardDevition(double[] array){
        double sum = 0;
        int cnt=0;
        double avg = getAverage(array);
        System.out.println(avg);
        for(int i = 0;i < array.length;i++){
        	if(array[i]>0){
        		cnt++;
                sum += Math.sqrt(((double)array[i] - avg) * (array[i] -avg));
        		//sum += ((array[i] - avg)*(array[i]-avg));
        	}
        }
       // sum = sum / cnt;
       // sum = Math.sqrt(sum);
        sum = (double)sum/(cnt*1.0-1);
        return sum ;
    }

	public static boolean BFS(int src,int dst,int [][]topo,int[] pre){
        boolean flag = false;
		Queue<Integer> queue = new LinkedList<Integer>();  
        int [] visited = new int[26];
        queue.add(src);
        visited[src] = 1;
        pre[src] = -1;
        while(!queue.isEmpty()){
        	int node = queue.poll();       	
        	if(node == dst){
        		flag = true;
        		break;
        	}

        	for(int i=0;i<topo[node].length;i++){
        		if(topo[node][i]==1 && visited[i]!=1){
        			queue.add(i);
        			visited[i]=1;
        			pre[i] = node;
        		}
        	}
        	
        }    
        return flag;
	}
	
	public static void showPath(int[] pre,int dst,int[] route) {
		int index = 1;
		route[index++] = dst;
		int node = 0;
		while(true){
			node = pre[dst];
			if(node==-1){
				break;
			}
			route[index++] = node;
			dst = node;
		}
		
		for(int i=index-1;i>=1;i--){
			System.out.print(route[i]+"->");
		}
		System.out.println("end.");
	}
	
	public static void printThroughput(int flowNum) throws IOException {
		String filepath = "E:\\eclipseworkspace\\TimedUpdate\\flows_input\\throughput_"+flowNum+".txt";
		FileWriter fw1 = new FileWriter(filepath,true);
		for(int i=0;i<flowNum;i++){
			double t = getRandom(1, 5)*1.0/10;
			fw1.append(t+"\n");
			fw1.flush();
		}
	}
	
	public static void printPath(int [] route,int flowNum) throws IOException{
		String filepath = "E:\\eclipseworkspace\\TimedUpdate\\flows_input\\input_flow_"+flowNum+".txt";
		FileWriter fw1 = new FileWriter(filepath,true);
		for(int i=route.length-1;i>=1;i--){
			if(route[i]!=0)
			fw1.append(route[i]+" ");
			fw1.flush();
		}
		fw1.append("\n");
		fw1.flush();
		//fw1.close();
	}
	
	public static void findPath(int src,int dst,int [][]topo,int flownum) throws IOException {
		int [][]topology = new int[26][26];
		for(int i = 0;i<topo.length;i++){
			for(int j=0;j<topo[i].length;j++){
				topology[i][j] = topo[i][j];
			}
		}
		int[] pre = new int[26];
		int[] route = new int[26];
		boolean result = BFS(src,dst,topology,pre);
		if(result){
			System.out.println("inital flow:");
			showPath(pre,dst,route);
			printPath(route,flownum);
		}else{
			System.out.println("no inital flow!");
		}
		for(int i=1;i<route.length;i++){
			if(route[i]!=0&&route[i+1]!=0){
				//System.out.println(route[i]+" "+route[i+1]);
				topology[route[i]][route[i+1]]=0;
				topology[route[i+1]][route[i]]=0;
			}
		}
		
		int[] pre2 = new int[26];
		int[] route2 = new int[26];
		result = BFS(src,dst,topology,pre2);
		if(result){
			System.out.println("final flow:");
			showPath(pre2,dst,route2);
			printPath(route2,flownum);
		}else{
			System.out.println("no final flow!");
		}
		
	}
	
	
	
/*	public static void findPath(int src,int dst,int[][]topo) throws IOException {
		int [][]topology = new int[26][26];
		for(int i = 0;i<topo.length;i++){
			for(int j=0;j<topo[i].length;j++){
				topology[i][j] = topo[i][j];
			}
		}
		int [] visited = new int[26];
		int [] route = new int[26];
		int index = 1;
		int result = DFS(src,dst,visited,topology,route,index);
		if(result==1){
			System.out.println("inital flow:");
			showPath(route);
			printPath(route);
		}
		else{
			System.out.println("no inital flow!!!!!!!!!!!!!!!!!");
		}
		for(int i=1;i<route.length;i=i+2){
			if(route[i]!=0&&route[i+1]!=0){
				//System.out.println(route[i]+" "+route[i+1]);
				topology[route[i]][route[i+1]]=0;
				topology[route[i+1]][route[i]]=0;
			}
		}
		int [] visited2 = new int [26];
		int [] route2 = new int [26];
		int index2 = 1;
		result = DFS(src,dst,visited2,topology,route2,index2);
		if(result==1){
			System.out.println("final flow:");
			showPath(route2);
			printPath(route2);
		}else{
			System.out.println("no final flow!!!!!!!!!!!!!!!!!");
		}
	}*/
	
	private static double rad(double d) {    
        return d * Math.PI / 180.0;    
    } 
	
	public static double getDistance(double lat1, double lng1, double lat2,double lng2) {    
	    double EARTH_RADIUS = 6378.137;    
		double radLat1 = rad(lat1);    
		double radLat2 = rad(lat2);    
		double a = radLat1 - radLat2;    
		double b = rad(lng1) - rad(lng2);    
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)    
				+ Math.cos(radLat1) * Math.cos(radLat2)    
				* Math.pow(Math.sin(b / 2), 2)));    
		s = s * EARTH_RADIUS;    
		s = Math.round(s * 10000d) / 10000d;    
		s = s*1000;    
		return s;    
	} 
	
	public static void calDelay(int[][]topo,double[][]coordinate) throws IOException {
		double[][]delay = new double[26][26];
		double speed = (299792458/3)*2;
		for(int i=1;i<26;i++){
			for(int j=1;j<26;j++){
				if(topo[i][j]==1){
					double dis = getDistance(coordinate[i][0],coordinate[i][1], coordinate[j][0], coordinate[j][1]);
					delay[i][j] = dis/speed;
					delay[j][i] = dis/speed;
				}
			}
		}
		double min = 1000;
		for(int i=1;i<26;i++){
			for(int j=1;j<26;j++){
				if(topo[i][j]==1){
					//System.out.println("i:"+i+" j:"+j+" "+delay[i][j]);
					if(delay[i][j]<min){
						min = delay[i][j];
					}
				}
			}
		}
		for(int i=1;i<26;i++){
			for(int j=1;j<26;j++){
				if(topo[i][j]==1){
					//System.out.println("i:"+i+" j:"+j+" "+delay[i][j]);
					delay[i][j] = Math.round(delay[i][j] / min);
				}
			}
		}
		
		/*for(int i=1;i<26;i++){
			for(int j=1;j<26;j++){
				if(topo[i][j]==1){
					System.out.println("i:"+i+" j:"+j+" "+delay[i][j]);
				}
			}
		}*/
		
		FileWriter fw1 = new FileWriter("delay.txt",true);
		
		for(int i=1;i<26;i++){
			for(int j=1;j<26;j++){
				if(topo[i][j]==1){
					fw1.append(i+" "+j+" "+(int)delay[i][j]+"\n");
					fw1.flush();
				}
			}
		}
		//fw1.append("\n");
		fw1.flush();
		//return delay;
	}
	
	public static void gengerateFlow(int num) throws IOException{
		for(int i = 0;i<num;i++){
	    	 int src = getRandom(1, 25);
		     int dst = getRandom(1, 25);
		     while(dst == src){
		    	 dst = getRandom(1, 25);
		     }
		     System.out.println("find path btw src:"+src+" and dst:"+dst);
		     findPath(src, dst, topology,num);
		     System.out.println();
	     }
	}
	 public static void main(String args[]) throws IOException {
		 String filePath = "topology.txt";

		 BufferedReader br = new BufferedReader(new InputStreamReader(  
	             new FileInputStream(filePath)));  

	     for (String line = br.readLine(); line != null; line = br.readLine()) {  
	         String[] s = line.split(" ");
	         if(s.length == 1){
	        	 System.out.println("number of nodes: "+s[0]);
	         }else{
	        	 int src = Integer.parseInt(s[0]) + 1;
	        	 int dst = Integer.parseInt(s[1]) + 1;
	        	 //System.out.println("src: "+src+" to dst: "+ dst);
	        	 topology[src][dst] = 1;
	        	 topology[dst][src] = 1;
	         }    
	     }
	     
	     filePath = "coordinate.txt";
	     br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
	     double[][] coordinate   = new double[26][2];
	     int tIndex = 1;
	     for (String line = br.readLine(); line != null; line = br.readLine()) {  
	         String[] s = line.split(" ");
	         if(s.length == 1){
	        	 System.out.println("number of nodes: "+s[0]);
	         }else if(s.length==2){
	        	double longitude = Double.valueOf(s[0]);
	        	double latitude = Double.valueOf(s[1]);
	        	 //System.out.println("src: "+src+" to dst: "+ dst);
	        	coordinate[tIndex][0] = longitude;
	        	coordinate[tIndex][1] = latitude;
	        	tIndex++;
	         }    
	     }
	     
	 	/*double dis = getDistance(coordinate[1][1], coordinate[1][0],coordinate[2][1], coordinate[2][0]);
		System.out.println(dis);*/
	    // System.out.println(tIndex);
	    // calDelay(topology, coordinate);
	     
	   //findPath(21, 1, topology);
	     
	    int flowNum = 2500;
	     
	    gengerateFlow(flowNum);  //改这里的数字，表示生成的流的数目，放在文件里
	     
	   printThroughput(flowNum);
	     
	   
	}
	 
}
