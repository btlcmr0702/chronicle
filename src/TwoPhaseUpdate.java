import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Array;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.w3c.dom.stylesheets.LinkStyle;

public class TwoPhaseUpdate {
	
	private static int numOfSwitch = 100;

	
	public static int getRandom(int min,int max){
		Random random = new Random();
		int s = random.nextInt(max)%(max-min+1) + min;
		return s;
	}
	
	public static int GetNumOfRules() {
		
		return 0;
	}
	
	public static boolean isLinkEqual(Link a, Link b) {
		if(a.getFrom()==-1||b.getFrom()==-1) return false;
		if(a.getLinkid()!=b.getLinkid()&&a.getFrom()==b.getFrom()&&a.getTo()==b.getTo()&&a.getStartTiming()==b.getStartTiming()&&a.getEndTiming()==b.getEndTiming()&&a.getFlowid()==b.getFlowid()&&a.isStart()==b.isStart()){
			return true;
		}
		else{	
			return false;
		}
	}
	//for some links across several timing, divide them for calculate load; such as fig2 in paper , v3 -> v4 from t0-t3,we should divide it into v3 -> v4 at t0-t1,t1-t2,t2-t3
	public static HashMap<Link,Integer> dividedLinks(HashMap<Link,Integer> links, int linkid){
		int size = links.size();
		HashMap<Link,Integer> newLinksMap = new HashMap<Link,Integer>();
		for(Map.Entry<Link,Integer> lEntry : links.entrySet()){
			Link key = lEntry.getKey();
			int value = lEntry.getValue();
			if((key.getEndTiming()-key.getStartTiming()==1)){
				newLinksMap.put(key, value);
			}
		}
		for(Map.Entry<Link,Integer> lEntry : links.entrySet()){
			Link link = lEntry.getKey();
			int index = lEntry.getValue();
			int startTiming = link.getStartTiming();
			int endTiming = link.getEndTiming();
			int diff = endTiming - startTiming;
			if(diff > 1){
				int from = link.getFrom();
				int to = link.getTo();
				int fid = link.getFlowid();
				boolean start = link.isStart();
				boolean end = link.isEnd();
				int interval = 1;
				while(diff > 0){
					Link newLink = new Link();
					newLink.setLink(from, to, startTiming, startTiming+interval, fid, linkid, start, end);
					if(newLinksMap.containsKey(newLink)){
						startTiming = startTiming + interval;
						diff--;
						size++;
						continue;
					}
					startTiming = startTiming + interval;
					diff--;
					size++;
					newLinksMap.put(newLink, size);
				}
				linkid++;
			}
		}
		return newLinksMap;
	}
	
	//for some links across several timing, divide them for calculate load; such as fig2 in paper , v3 -> v4 from t0-t3,we should divide it into v3 -> v4 at t0-t1,t1-t2,t2-t3
/*	public static Link[] dividedLinks(Link[] links,int linkIndex,int linkid){  
		Link[] newLinks = new Link[links.length];
		int newIndex = 0;
		for(int i=0;i<links.length;i++){
			if(links[i]!=null){
				newLinks[newIndex] = links[i];
				newIndex++;
			}
		}
		int lid = linkid;
		for(int i=0;i<links.length;i++){
			if(links[i]!=null){
				int startTiming = links[i].getStartTiming();
				int endTiming = links[i].getEndTiming();
				int diff = endTiming - startTiming;
				if(diff>1){     //divide these links to small links
					int from = links[i].getFrom();
					int to = links[i].getTo();
					int fid = links[i].getFlowid();
					boolean start = links[i].isStart();
					boolean end = links[i].isEnd();
					//int lid = links[i].getLinkid();
					int interval = 1;
					//System.out.println(from+" "+to+" "+startTiming+" "+endTiming+" "+fid);
					while(diff>0){
						//System.out.println(linkIndex);
						newLinks[newIndex] = new Link();
						newLinks[newIndex].setLink(from, to, startTiming, startTiming+interval, fid, lid, start, end);
						startTiming = startTiming + interval;
						newIndex++;
						diff--;
					}
					lid++;
				}
			}
		}
		//System.out.println("@@ "+ newLinks.length);
		
		//for removing the reduplicated link we divide above, the reduplicated link are all belong to initial flow and at same time from same node to same node
		for(int i=0;i<newLinks.length;i++){
			if(newLinks[i]!=null&&newLinks[i].getFrom()>0){
				for(int j=i;j<newLinks.length;j++){
					if(newLinks[j]!=null&&newLinks[j].getFrom()>0){
						if(isLinkEqual(newLinks[i],newLinks[j])){
							//System.out.println("111");
							newLinks[j].setFrom(-1);
							newLinks[j].setTo(-1);
							newLinks[j].setStartTiming(-1);
							newLinks[j].setEndTiming(-1);
							newLinks[j].setFlowid(-1);
						}
					}
				}
			}
		}
		
		for(int i=0;i<newLinks.length;i++){
			if(newLinks[i]!=null&&newLinks[i].getFrom()<0){
				newLinks[i] = null;
			}
		}
		//System.out.println("*******************");

		return newLinks;
	}*/
	/*public static int[][] calRules(HashMap<Link,Integer> linksMap,int endTiming){
		int[][] rules = new int[endTiming+1][26];
		for(int t=0;t<endTiming;t++){
			//int x=0;
			for(int i=1;i<26;i++){
				HashMap<Integer, Integer> tmpFlow = new HashMap<Integer,Integer>();
				for(Map.Entry<Link,Integer> lEntry : linksMap.entrySet()){
					Link link = lEntry.getKey();
					if(link.startTiming==t-1 && link.getEndTiming()==t && link.getTo() == i){
						int fid = link.getFlowid();
						if(!tmpFlow.containsKey(fid)){
							//System.out.println("fid "+ fid+" "+i);
							tmpFlow.put(fid, i);
							rules[t][i]++;
						}
					}
				}
				//x+=tmpFlow.size();
				//System.out.println("tfs "+i+" "+tmpFlow.size());
			}
			//System.out.println("flow sum:is "+x);
		}
		return rules;
	}*/
	public static int[][] calRules(HashMap<Link,Integer> linksMap,int endTiming){
		int[][] rules = new int[endTiming+1][numOfSwitch+1];
		for(int t=-1;t<endTiming-1;t++){
			for(Map.Entry<Link,Integer> lEntry : linksMap.entrySet()){
				Link link = lEntry.getKey();
				if(link.startTiming==t){
					int to = link.getTo();
					rules[t+1][to]++;
				}
			}
		}
		return rules;
	}
	
	//calculate the df(throughput) / bandwidth at each timing
	public static double[] CalLinkUtlization(HashMap<Link,Integer> linksMap, int endT,double[] throughput,double [][] bandwidth,int[] congestedLinks,int[] congestedFlows){
		double[] maxCapa = new double[endT];
			for(int t=0;t<endT;t++){
				double tmpLoad = 0;
				double [][] load = new double[numOfSwitch+1][numOfSwitch+1];
				HashMap<Integer,Integer> congestedFlowsMap = new HashMap<Integer,Integer>();
				for(Map.Entry<Link,Integer> lEntry : linksMap.entrySet()){
					Link link = lEntry.getKey();
					if(link.getStartTiming()==t&&link.getEndTiming()==t+1){
						int from = link.getFrom();
						int to = link.getTo();
						int fid = link.getFlowid();
						double df = throughput[fid];
						load[from][to] +=df;		 //for get sum of df of all the flows at same link

					}
				}		
				for(int i=0;i<load.length;i++){
					for(int j=0;j<load[i].length;j++){
						if(load[i][j] > bandwidth[i][j]){  //congested links
							congestedLinks[t]++;
							for(Map.Entry<Link,Integer> lEntry : linksMap.entrySet()){
								Link link = lEntry.getKey();
								if(link.getStartTiming()==t&&link.getEndTiming()==t+1&&((link.getFrom()==i&&link.getTo()==j)||(link.getFrom()==j&&link.getTo()==i))){
									congestedFlowsMap.put(link.getFlowid(),t);
								}
							}
							congestedFlows[t] = congestedFlowsMap.size();  //congestion flows
						}
						
						tmpLoad = load[i][j] / bandwidth[i][j];
						if (tmpLoad > maxCapa[t]) {
							maxCapa[t] = tmpLoad;
						}
					}
				}
			}
		return maxCapa;
	}
	
	
	
	public static void printLinks(HashMap<Link,Integer> linksMap) {
		List <Map.Entry<Link,Integer>> list = new ArrayList <Map.Entry<Link,Integer>>(linksMap.entrySet());
		
		Collections.sort(list, new Comparator<Map.Entry<Link, Integer>>() {
		    public int compare(Map.Entry<Link, Integer> o1,
		            Map.Entry<Link, Integer> o2) {
		        return (o1.getValue() - o2.getValue());
		    }
		});
		
		 for (int i = 0; i < list.size(); i++) {  
			 Map.Entry<Link,Integer> tmp = list.get(i);
			 Link link = tmp.getKey();
			 int index = tmp.getValue();
			 if(link.getStartTiming()==0)
			 System.out.println("link:"+index+" from:"+link.getFrom()+" to:"+link.getTo()+" at:"+link.getStartTiming()+" to:"+link.getEndTiming()+" flow:"+link.getFlowid()+" isStart:"+link.isStart()+" linkid:"+link.getLinkid());
	        }  
		 
		 System.out.println("***************************");
		 
	}	
	//calculate the df(throughput) / bandwidth at each timing
	/*public static double[] CalLinkUtlization(Link[] links,int endT,double[] throughput,double[][]bandwidth){
		double[] maxCapa = new double[endT];
		for(int t=0;t<endT;t++){		
			double [][] load = new double[26][26];  //for get sum of df of all the flows at same link
			for(int i=0;i<links.length;i++){
				if(links[i]!=null && links[i].getStartTiming()==t&&links[i].getEndTiming()==t+1){
				int from = links[i].getFrom();
				int to = links[i].getTo();
				int fid =links[i].getFlowid();
				double df = throughput[fid];
				//double bw = bandwidth[from][to];
				load[from][to] +=df;
				//System.out.println("flow:"+links[i].getFlowid()+" from:"+links[i].getFrom()+" to:"+links[i].getTo()+" at:"+links[i].getStartTiming()+" to:"+links[i].getEndTiming()+" is start:"+links[i].isStart()+" linkid:"+links[i].getLinkid());
				}
			}		
			for(int i=0;i<load.length;i++){
				for(int j=0;j<load[i].length;j++){
					load[i][j] = load[i][j] / bandwidth[i][j];
					if (load[i][j] > maxCapa[t]) {
						maxCapa[t] = load[i][j];
					}
				}
			}
		}
		
		return maxCapa;
	}*/
	
	/*public static void printLinks(Link[] links) {
		for(int i=1;i<links.length;i++){
			if(links[i]!=null){
				System.out.println("flow:"+links[i].getFlowid()+" from:"+links[i].getFrom()+" to:"+links[i].getTo()+" at:"+links[i].getStartTiming()+" to:"+links[i].getEndTiming()+" is start:"+links[i].isStart()+" linkid:"+links[i].getLinkid());
			}
		}
	}*/
	
	public static void main(String[] args) throws IOException {
		int Tcase=0;
		while(Tcase<1) {
			long start_Time=System.currentTimeMillis();//记录开始时间 
			
		int t = 200;
		double[] linkUtilz = new double[81];
		double[] congestedflows = new double[81];
		double[][] congestedSTDFlows = new double[81][81];
		double[] ForRules = new double[81];
		int flagT = 0;
		//while(flagT<11){
		
	/*	int [] redStartFlow = {1,3,4,5}; 
		int [] greStartFlow = {1,2,5};
		int [] redEndFlow = {1,2,5};
		int [] greEndFlow = {1,3,2,4,5};*/
		
		//int [][] flows = {{1,3,4,5},{1,2,5},{1,2,5},{1,3,2,4,5}}; //start flow , end flow, start flow ,end flow ....
		
		int [][]flows = new int[10000][];
		String filePath = "input_flow_1000.txt";

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
	     
		//double [] throughput={0,1,1};  // 0,throughput of flow1, throughput of flow2.....
		double [] throughput = new double[cnt/2+1];
		int throughtIndex = 1;
		filePath = "throughput_1000.txt";
		br = new BufferedReader(new InputStreamReader(  
	             new FileInputStream(filePath)));  
		 for (String line = br.readLine(); line != null; line = br.readLine()) {  
		    	throughput[throughtIndex] = Double.valueOf(line);
		    	//System.out.println(throughtIndex+" "+Double.valueOf(line));
		    	throughtIndex++;
		     }
		 
		 double[][] delay = new double[numOfSwitch+1][numOfSwitch+1];
		 filePath = "delay_100.txt";
			br = new BufferedReader(new InputStreamReader(  
		             new FileInputStream(filePath)));  
			 for (String line = br.readLine(); line != null; line = br.readLine()) {  
				 String[] s = line.split(" ");
				 int i = Integer.valueOf(s[0]);
				 int j = Integer.valueOf(s[1]);
				 double d = Double.valueOf(s[2]);
				 delay[i][j] = d;
				// System.out.println(i+" "+j+" "+d);
			     }
	/*	for(int i=1;i<=cnt/2;i++){
			//throughput[i] = (double)getRandom(1, 5);
			throughput[i] = 1;
		}*/
	
		Topology topology = new Topology();
		topology.init(numOfSwitch,14.9,1);
		//double [][] delay = topology.getDelay();
		double [][] bandWidth = topology.getBandWidth();
		
		HashMap<Integer,Double> latencyMap = new HashMap<Integer,Double>();  
		double []latency = new double[cnt/2];
		for(int i= 0;i<latency.length;i++){
			latency[i] =  (double)getRandom(20, 100);   // the latency decides when to update the switch
			//latency[i] = 1;
			//System.out.println(latency[i]);
		}
		
		for(int i=0;i<cnt;i=i+2){		// if some flows have the same src, they should be updated at same time
			int src = flows[i][0];
			double lat = latency[i/2];
			for(int j=0;j<cnt;j=j+2){
				if(j!=i && flows[j][0] == src){  // if two flows have same src
					latency[j/2] = lat;
				}
			}
		}
		
		for(int i=0;i<latency.length;i++){
			latencyMap.put(i,latency[i]);  //latency map: <flow_id,latency>
			//System.out.println(i+"***"+latency[i]);
		}
		Arrays.sort(latency);   //sort according the latency bwt controller and switch
		
		
		//updateTiming:<flow_id,update time>, record the time to update each switch
		HashMap<Integer,Integer> updateTiming = new HashMap<Integer,Integer>();
		int updateT = 1;  //the first switch should update from t1
		for(int i=0;i<latency.length;i++){
			double lat = latency[i];
			if(i<latency.length-1 && latency[i+1] == lat) continue;  //if some flows should be updated at same time 
			for(Map.Entry<Integer,Double> lEntry : latencyMap.entrySet()){
				int f = lEntry.getKey();
				double la = lEntry.getValue();
				if(la == lat){    //for some flows have same update timing
					updateTiming.put(f,updateT);
					//System.out.println(f+" "+updateCnt+" "+la);
				}
			}
			updateT++; //update time for each switch
		}
			
		//int linkNumber = 10000000;
		//Link [] links = new Link[linkNumber];  //for all links
		HashMap<Link,Integer> linksMap = new HashMap<Link,Integer>();
		
		int endTime=0;
		int linkIndex = 1;
		int flowid = 0;
		int linkid = 0;
		int startTime=0;  // startTmp ... t0 ... time
		boolean startFlag = true;
		boolean endFlag = false;
		int startTiming;
		
		//HashMap<Integer, Link[]> linkflows = new HashMap<Integer,Link[]>(); // for the links belong to one flow
			for(int j=0;j<cnt;j++){
				if(j%2==0){        			  //initial flow
					flowid++;   
					startFlag = true;
					endFlag = false;
					startTime = -80;   //initial flow all start from the past
					endTime = updateTiming.get(flowid-1);  //initial flow all end before updating
				}else{	         	    	  //final flow
					startFlag = false;
					endFlag = true;		
					startTime = updateTiming.get(flowid-1);  //final flow start from update timing 
					endTime= 80;   //final flow end at set time
				}
				for(int i=startTime;i<endTime;i++){
					 startTiming = i;
					 linkid++;
					for(int k=0;k<flows[j].length-1;k++){
						int from = flows[j][k];
						int to = flows[j][k+1];
						int endTiming = startTiming + (int)delay[from][to];
					/*	if(startTiming >= 5)
							break;*/
						/*if(endTiming > endTime || startTiming >= endTime)
							break;*/
						Link link  = new Link();
						link.setLink(from, to, startTiming, endTiming, flowid, linkid, startFlag, endFlag);
						startTiming = endTiming;			
						linksMap.put(link, linkIndex);
						linkIndex++;
					
					}
			}
		}
		//System.out.println(linkIndex);
	//	printLinks(linksMap);
	//	System.out.println("######");
		HashMap<Link, Integer> newLinkMap = dividedLinks(linksMap,linkid);
		//System.out.println("****");
	//	printLinks(newLinkMap);
		int[] congestedLinks = new int[endTime+1];
		int[] congestedFlows = new int[endTime+1];
		double []maxCapa = CalLinkUtlization(newLinkMap,endTime,throughput,bandWidth,congestedLinks,congestedFlows);
		int maxNum =0;
		for(int i=0;i<maxCapa.length;i++){
			if(maxCapa[i] > 1.12){
				maxNum++;
			}
			//System.out.println(maxCapa[i]);
		}
		
		//System.out.println("********************");
		/*for(int i=0;i<congestedLinks.length;i++){
			if(congestedLinks[i]>0)
			System.out.println(congestedLinks[i]);
		}
		System.out.println("*********************");*/
		int c=0;
		for(int i=0;i<congestedFlows.length;i++){
			if(congestedFlows[i]>0){
				//System.out.println(i+" "+congestedFlows[i]);
				c++;
			}
		}
		//System.out.println("*********************");
		int[][]rules = calRules(newLinkMap, endTime);
		for(int i=0;i<rules.length;i++){
			int max = 0;
			int xxx=0;
			for(int j=0;j<rules[i].length;j++){
				xxx +=rules[i][j];
				if(rules[i][j]!=0&& rules[i][j]>max){
					max = rules[i][j];
				}
			}
			//System.out.println("xxx:"+xxx);
			//if(max>0)
			//System.out.println(max);
		}
		
		if(c>=0 ){
			System.out.println("find a solution"+" cur num of solution is:"+flagT+" turn is:"+(200-t));
			flagT++;
			for(int i=0;i<maxCapa.length;i++){
				linkUtilz[i]+=maxCapa[i];
			}
			for(int i=0;i<congestedFlows.length;i++){
				if(congestedFlows[i]>0){
					congestedflows[i]+=congestedFlows[i];
					congestedSTDFlows[flagT][i] = congestedFlows[i];
				}
			}
			for(int i=0;i<rules.length;i++){
				int max = 0;
				for(int j=0;j<rules[i].length;j++){
					if(rules[i][j]!=0&& rules[i][j]>max){
						max = rules[i][j];
					}
				}
				if(max>0)
				ForRules[i]+=max;
			}
				
		}
		
		t--;
	//}
	
		
		for(int i=0;i<linkUtilz.length;i++){
			System.out.println(linkUtilz[i]/flagT);
		}
		System.out.println("##########################");
		/*for(int i=0;i<congestedflows.length;i++){
			if(congestedflows[i]>0)
			System.out.println(i+" "+congestedflows[i]/flagT);
		}*/
		
		double[] max = new double[81];
		for(int tt=0;tt<flagT;tt++){
			for(int j=0;j<congestedSTDFlows[tt].length;j++){
				if(congestedSTDFlows[tt][j]>max[tt]){
					//System.out.println("#@!?");
					max[tt] = congestedSTDFlows[tt][j];
				}
			}
		}
		for(int i=0;i<max.length;i++){
			if(max[i]>0){
				System.out.println(max[i]);
			}
		}
		
		System.out.println("##########################");
		for(int i=0;i<ForRules.length;i++){
			//System.out.println(ForRules[i]/flagT);
		}
		long end_Time=System.currentTimeMillis();//记录结束时间  
		float excTime=(float)(end_Time-start_Time)/1000;  
		System.out.println("执行时间："+excTime+"s");  

		String filepath = "tpp_runningtime.txt";
		FileWriter fw1 = new FileWriter(filepath,true);
		fw1.write(excTime+"\n");
		fw1.flush();
		
		System.out.println("end");	
		Tcase++;
		
		
		}
		
	}
}
