import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class NodeOrdering {
	private static int numOfSwitch = 100;

	public static int getRandom(int min,int max){
		Random random = new Random();
		int s = random.nextInt(max)%(max-min+1) + min;
		return s;
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
						//size++;
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
	public static double[] CalLinkUtlization(HashMap<Link,Integer> linksMap, int endT,double[] throughput,double [][] bandwidth,int[] congestedLinks,int[] congestedFlows) {
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
						load[from][to] +=df;			 //for get sum of df of all the flows at same link
					}
					
				}		
				for(int i=0;i<load.length;i++){
					for(int j=0;j<load[i].length;j++){
						if(load[i][j] > bandwidth[i][j]){  //congested links
							//System.out.println(i+" "+j+" "+load[i][j]+" "+t);
							//System.out.println(i+" "+j+" "+t);
							congestedLinks[t]++;
							for(Map.Entry<Link,Integer> lEntry : linksMap.entrySet()){
								Link link = lEntry.getKey();
								if(link.getStartTiming()==t&&link.getEndTiming()==t+1&&((link.getFrom()==i&&link.getTo()==j)||(link.getFrom()==j&&link.getTo()==i))){
									congestedFlowsMap.put(link.getFlowid(),t);
								}
							}
							congestedFlows[t] = congestedFlowsMap.size();  //congested flows
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
			 //if(link.getStartTiming()==1)
			 //System.out.println("link:"+link.getLinkid()+" from:"+link.getFrom()+" to:"+link.getTo()+" at:"+link.getStartTiming()+" to:"+link.getEndTiming()+" flow:"+link.getFlowid()+" isStart:"+link.isStart()+" isEnd:"+link.isEnd()+" linkid:"+link.getLinkid());
	        }  
		 //System.out.println("***************************");
	}
	
	public double getAverage(double[] array){
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
   
    public double getStandardDevition(double[] array){
        double sum = 0;
        int cnt=0;
        double avg = getAverage(array);
        for(int i = 0;i < array.length;i++){
        	if(array[i]>0){
        		cnt++;
                sum += Math.sqrt(((double)array[i] - avg) * (array[i] -avg));

        	}
        }
        return (sum / cnt);
    }
	
	public static void main(String args[]) throws NumberFormatException, IOException {
				
		
		int Tcase=0;
		while(Tcase<1) {
			long start_Time=System.currentTimeMillis();//记录开始时间  

		int t = 200;
		double[] linkUtilz = new double[120];
		double[] congestedflows = new double[120];
		double[][] congestedSTDFlows = new double[120][120];
		double[] ForRules = new double[120];
		int flagT = 0;
		//while(flagT<20){

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
				topology.init(numOfSwitch,6.3,1);
				//double [][] delay = topology.getDelay();
				double [][] bandWidth = topology.getBandWidth();
	
				HashMap<Integer,Double> latencyMap = new HashMap<Integer,Double>();  
				double []latency = new double[cnt/2];
				int ttt = 10;
				for(int i=0;i<latency.length;i++){
					latency[i] =  (double)getRandom(20, 100);   // the latency decides when to update the switch
					//System.out.println("i:"+" "+latency[i]);
					//latency[i] = 1;
					//latency[i] = ttt++;
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

			/*	for(Map.Entry<Integer,Integer> upEntry : updateTiming.entrySet()){
					System.out.println(upEntry.getKey()+" "+upEntry.getValue());
				}*/
				
				int endTime = 0;
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
							startTime = -110;   //initial flow all start from the past
							endTime = updateTiming.get(flowid-1);  //initial flow all end before updating
							//endTime = 0;
						}else{	         	    	  //final flow
							startFlag = false;
							endFlag = true;		
							startTime = updateTiming.get(flowid-1);  //final flow start from update timing 
							//startTime = 0;
							endTime=110;   //final flow end at set time
						}
						for(int i=startTime;i<endTime;i++){
							 startTiming = i;
							 linkid++;
							for(int k=0;k<flows[j].length-1;k++){
								int from = flows[j][k];
								int to = flows[j][k+1];
								int endTiming = startTiming + (int)delay[from][to];
								
								int f=0;								
								if(endTiming >= endTime && j%2==0 && startTiming <= endTime){   //initial flows
									//System.out.println("!!!: "+from+" "+to+" "+startTiming+" "+endTiming+" "+flowid+" "+startFlag+" "+endTime );
									//System.out.println("++++:"+startTiming+" "+endTime);							
									int startTimingTmp=endTime;
									int tmpindex = 0;
									for(int x=0;x<flows[j+1].length-1;x++){
										if(to == flows[j+1][x]){
											f=1;
											tmpindex=x;
											//System.out.println("!!!: "+from+" "+to+" "+startTiming+" "+endTiming+" "+flowid+" "+startFlag+" "+endTime );
											break;
										}
									}
										if(f==1){
											Link tlink = new Link();
											tlink.setLink(from, to, startTiming, endTime, flowid,linkid,startFlag,endFlag);	
											linksMap.put(tlink,linkIndex);
											linkIndex++;
											
											//System.out.println("tmpidex: "+flows[j+1][tmpindex]+" "+startTimingTmp+" "+flowid);
											for(int y=tmpindex;y<flows[j+1].length-1;y++){
												int fromTmp = flows[j+1][y];
												int toTmp = flows[j+1][y+1];
												int endTimingTmp = startTimingTmp + (int)delay[fromTmp][toTmp];
												Link link = new Link();
												link.setLink(fromTmp, toTmp, startTimingTmp, endTimingTmp, flowid,linkid,false,true);	
												linksMap.put(link,linkIndex);
												linkIndex++;
												startTimingTmp = endTimingTmp;
											}
										}
									}
								
								if(f==1) break;
								
								Link link = new Link();
								link.setLink(from, to, startTiming, endTiming, flowid,linkid,startFlag,endFlag);	
								linksMap.put(link,linkIndex);
								linkIndex++;
								startTiming = endTiming;			
							}
					}
				}
					
					//printLinks(linksMap);
				//	System.out.println("######");
					HashMap<Link, Integer> newLinkMap = dividedLinks(linksMap,linkid);
					//System.out.println("****");
				//	printLinks(newLinkMap);
					int[] congestedLinks = new int[endTime+100];
					int[] congestedFlows = new int[endTime+100];
					double []maxCapa = CalLinkUtlization(newLinkMap,endTime,throughput,bandWidth,congestedLinks,congestedFlows);
					int maxNum =0;
					for(int i=0;i<maxCapa.length;i++){
						//System.out.println(i+" "+maxCapa[i]);
						if(maxCapa[i] >= 1.0){
							maxNum++;
						}
					}
					/*System.out.println("********************");
					for(int i=0;i<congestedLinks.length;i++){
						if(congestedLinks[i]>0)
						System.out.println("congestedlinks: "+i+" "+congestedLinks[i]);
					}*/
					int c=0;
					//System.out.println("*********************");
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
						for(int j=0;j<rules[i].length;j++){
							if(rules[i][j]!=0&& rules[i][j]>max){
								max = rules[i][j];
							}
						}
						//if(max>0)
						//System.out.println(i+" "+max);
					}
					
					if(c>=0){
						System.out.println("NODE find a solution"+" cur num of solution is:"+flagT+" turn is:"+(200-t));
						flagT++;
						for(int i=0;i<maxCapa.length;i++){
							linkUtilz[i]+=maxCapa[i];
						}
						for(int i=0;i<congestedFlows.length;i++){
							if(congestedFlows[i]>0){
								congestedflows[i]+=congestedFlows[i];
								congestedSTDFlows[flagT][i] = congestedFlows[i];							}
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
		//	System.out.println(linkUtilz[i]/flagT);
		}
		System.out.println("##########################");
		for(int i=0;i<congestedflows.length;i++){
			//if(congestedflows[i]>0)
			//System.out.println(congestedflows[i]/flagT);
		}
		double[] max = new double[120];
		for(int tt=0;tt<flagT;tt++){
			for(int j=0;j<congestedSTDFlows[tt].length;j++){
				if(congestedSTDFlows[tt][j]>max[tt]){
				//	max[tt] = congestedSTDFlows[tt][j];
				}
			}
		}
		for(int i=0;i<max.length;i++){
			if(max[i]>0){
				System.out.println("!!!"+max[i]);
			}
		}
		
		
		System.out.println("##########################");
		for(int i=0;i<ForRules.length;i++){
		//	System.out.println(ForRules[i]/flagT);
		}
		
		
		long end_Time=System.currentTimeMillis();//记录结束时间  
		float excTime=(float)(end_Time-start_Time)/1000;  
		System.out.println("执行时间："+excTime+"s");  

		String filepath = "nop_runningtime.txt";
		FileWriter fw1 = new FileWriter(filepath,true);
		fw1.write(excTime+"\n");
		fw1.flush();
		
		System.out.println("end");	
		Tcase++;
		}
		
	}

}
