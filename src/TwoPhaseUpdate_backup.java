import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TwoPhaseUpdate_backup {
	public static int getRandom(int min,int max){
		Random random = new Random();
		int s = random.nextInt(max)%(max-min+1) + min;
		return s;
	}
	
	public static int GetNumOfRules() {
		
		return 0;
	}
	
	public static HashMap<Integer,Path> getAllPath(int[][] flows,double [][]bandWidth,double[] throughput,int cnt){
		//get all the path that the flows go through(include final flow)
		HashMap<Integer,Path> paths = new HashMap<Integer,Path>();
		int index = 1;
		for(int i = 0;i<cnt;i=i+2){  //find the path belong to initial flow first
			boolean start = true;
			boolean end = false;
			for(int j = 0;j<flows[i].length-1;j++){
				int u = flows[i][j];
				int v = flows[i][j+1];
				int f = 1;
				for(Map.Entry<Integer,Path> pEntry : paths.entrySet()){
					Path tmpP = pEntry.getValue();
					//if(tmpP.getU()==u&&tmpP.getV()==v&&tmpP.isStart()==start&&tmpP.isEnd()==end){
					if(tmpP.getU()==u&&tmpP.getV()==v){
						f = 0;
						tmpP.setCapacity(tmpP.getCapacity() - throughput[i%2+1]);  //if different flow has same path, minus throughput of the flow
						/*if(tmpP.getCapacity()<0){
							System.out.println("capacity < 0 , offload > bandwidth, exit...");
							System.exit(0);
						}*/
						//	paths.replace(pEntry.getKey(),tmpP);
						
					}
				}
				if(f==1){  //first time to find the path
					Path path = new Path();
					double capa = bandWidth[u][v] - throughput[i%2+1];
					path.setPath(u, v, capa, start, end);
					paths.put(index, path);
					index++;
				}
			}
			checkPath(paths,bandWidth,0);
		}
		for(int i = 1;i<cnt;i=i+2){  //then, find the path belong to final flow 
			boolean start = false;
			boolean end = true;
			for(int j = 0;j<flows[i].length-1;j++){
				int u = flows[i][j];
				int v = flows[i][j+1];
				int f = 1;
				for(Map.Entry<Integer,Path> pEntry : paths.entrySet()){
					Path tmpP = pEntry.getValue();
					if(tmpP.getU()==u&&tmpP.getV()==v){
						f = 0;
					//	tmpP.setCapacity(tmpP.getCapacity() - throughput[i%2+1]);  //if different flow has same path, minus throughput of the flow
					}
				}
				if(f==1){
					Path path = new Path();
					double capa = bandWidth[u][v];
					path.setPath(u, v, capa, start, end);
					paths.put(index, path);
					index++;
				}
			
	    	}
		}
		return paths;
	}
	
	public static int findPathId(HashMap<Integer,Path> paths, int u, int v){
		for(Map.Entry<Integer,Path> pEntry : paths.entrySet()){
			Path path = pEntry.getValue();
			if(path.getU() == u && path.getV() == v){
				return pEntry.getKey();
			}
		}
		System.out.println("can't find the path, exit...");
		System.exit(0);
		return -1;
	}
	
	public static void updateCapacity(HashMap<Integer,Path> paths, int fid,int [][]flows,double [] throughput){
		//System.out.println("fid:"+fid);
		int []initFlow = flows[fid*2];
		int []finalFlow = flows[fid*2+1];  //final flow
		for(int i=0;i<initFlow.length-1;i++){
			if(initFlow[i]!=0&&initFlow[i+1]!=0){
				int u = initFlow[i];
				int v = initFlow[i+1];
				//System.out.println(u+" "+v);
				int pid = findPathId(paths, u, v);
				Path path = paths.get(pid);
				
				if(path.isStart())
				path.setCapacity(path.getCapacity() + throughput[fid+1]);  //initial flow disappear, so should plus the throughput
			}
		}
		
		for(int i=0;i<finalFlow.length-1;i++){
			if(finalFlow[i]!=0&&finalFlow[i+1]!=0){
				int u = finalFlow[i];
				int v = finalFlow[i+1];
				int pid = findPathId(paths, u, v);
				Path path = paths.get(pid);
				path.setCapacity(path.getCapacity() - throughput[fid+1]);  // final flow appear, minus the throughput
			}
		}
	}
	public static void  checkPath(HashMap<Integer,Path> paths, double [][] bandwidth, int endFlag) {
			for(Map.Entry<Integer,Path> pEntry:paths.entrySet()){
				Path path = pEntry.getValue();
				double capa = path.getCapacity();
				if(capa < 0){
					System.out.println("capacity < 0 , offload > bandwidth, exit...");
					System.exit(0);
				}
				if(capa > bandwidth[path.getU()][path.getV()]){
					if(endFlag == 1){
						System.out.println("capacity > bandwidth, error...");
						System.exit(0);
					}else{
						System.out.println("congestion!!!!!!!!!!!!!!!!");
					}
				}
			}
	}
	
	public static void printPaths(HashMap<Integer,Path> paths) {
		for(Map.Entry<Integer,Path> pEntry : paths.entrySet()){
			Path p = pEntry.getValue();
			if(p.getCapacity()<0){
				System.out.println("path:"+pEntry.getKey()+" has negative capacity!");
				System.exit(0);
			}
			System.out.println("pathId: "+pEntry.getKey()+" from: "+p.getU()+" to: "+p.getV()+" capa: "+p.getCapacity()+" isStart: "+p.isStart()+" isEnd: "+p.isEnd());
		}
		System.out.println("***********************************");
	}
	
	public static void main(String[] args) throws IOException {
	/*	int [] redStartFlow = {1,3,4,5}; 
		int [] greStartFlow = {1,2,5};
		int [] redEndFlow = {1,2,5};
		int [] greEndFlow = {1,3,2,4,5};*/
		
		//int [][] flows = {{1,3,4,5},{1,2,5},{1,2,5},{1,3,2,4,5}}; //start flow , end flow, start flow ,end flow ....
		
		int [][]flows = new int[1000][];
		String filePath = "E:\\eclipseworkspace\\TimedUpdate\\input_flow.txt";

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));  
		
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
	     
		//double [] throughput={0,1,1};  // 0,throughput of flow1, throughput of flow2.....
		double [] throughput = new double[cnt];
		for(int i=1;i<=cnt/2;i++){
			//throughput[i] = (double)getRandom(1, 5);
			throughput[i] = 1;
		}
	
		Topology topology = new Topology();
		topology.init(25,40,1);
		double [][] delay = topology.getDelay();
		double [][] bandWidth = topology.getBandWidth();
		
		HashMap<Integer, Path> paths = getAllPath(flows, bandWidth,throughput,cnt);

		printPaths(paths);
		HashMap<Double,Integer> latencyMap = new HashMap<Double,Integer>();
		double []latency = new double[cnt/2];
		for(int i= 0;i<latency.length;i++){
			latency[i] =  (double)getRandom(20, 40);
			latencyMap.put(latency[i],i);
		}
		Arrays.sort(latency);   //sort according the latency bwt controller and switch
		int index=0;
		double startTiming = latency[0];
		
		while(latencyMap.size()>0){   //update all the final flows
			int numOFflow = 0;
			int [] updateFlow = new int[cnt/2];
			for(Map.Entry<Double,Integer> lEntry : latencyMap.entrySet()){
				double key = lEntry.getKey();
				int val = lEntry.getValue();
				if(key == latency[index]){    //for some flows have same update timing
					updateFlow[numOFflow] = val;
					numOFflow++;
				}
				//latencyMap.remove(key,val);  
			}
			latencyMap.remove(latency[index]);
			if(numOFflow>0){
				for(int i=0;i<numOFflow;i++){
					System.out.println("updatefid: "+updateFlow[i]+" ");
					updateCapacity(paths,updateFlow[i],flows,throughput);
				}
				printPaths(paths);
			}
			index++;
		}
		
		System.out.println("final path capacity:");
		printPaths(paths);
		
		checkPath(paths, bandWidth, 1);

	}
}
