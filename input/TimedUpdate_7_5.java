import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.SerializedLambda;
import java.nio.file.LinkPermission;
import java.nio.file.spi.FileSystemProvider;
import java.time.chrono.MinguoChronology;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.function.IntPredicate;

import javax.security.auth.kerberos.KerberosKey;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import org.omg.CORBA.ORBPackage.InconsistentTypeCode;
import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;
import org.w3c.dom.stylesheets.LinkStyle;

public class TimedUpdate {

	public static void findsegmentsCandidate(Link[] tempLinks,Link[] links,Link[] segmentCandi){
		//first find the links at t0, then according the paper to find the segments candidate
		int indexTemp = 1;
		for(int i=1;i<links.length;i++){
			if(links[i]!=null&&links[i].startTiming==0){  //find links at t0
			//	System.out.println("linkid: "+links[i].getLinkid()+" from: "+links[i].from+" to: "+links[i].to+" startT: "+links[i].startTiming+" endT: "+links[i].endTiming+" flowid: "+links[i].flowid +" isStart "+links[i].isStart());
				//System.out.println("----");
				tempLinks[indexTemp] = links[i];
				indexTemp++;
			}
		}
		
		int indexSC = 1;
		for(int i=1;i<indexTemp;i++){
			for(int j=i+1;j<indexTemp;j++){ 
				//find the two links start from same node, the two links belong to the same flow but one is initial and the other is final
				if(tempLinks[i].from == tempLinks[j].from && tempLinks[i].flowid == tempLinks[j].flowid && ((tempLinks[i].start == true&&tempLinks[j].end==true )||(tempLinks[i].end==true&&tempLinks[j].start==true))){					
					segmentCandi[indexSC] = tempLinks[i]; //j
					indexSC++;
					segmentCandi[indexSC] = tempLinks[j]; //i
					indexSC++;
				}
			}
		}
		
		/*for(int i=0;i<segmentCandi.length;i++){
			if(segmentCandi[i]!=null)
			System.out.println(segmentCandi[i].getFlowid());
		}*/
		//System.out.println("????? "+indexSC);

	}
	
	public static void findsegmentAtT0(HashSet<Link[]>segments,Link[] links,HashMap<Integer, Link[]> linkflows,Link[] segmentCandi){
		//find all the real segments start at t0 ( end at same node with different timing )
	/*	for(int i=0;i<segmentCandi.length;i++){
		if(segmentCandi[i]!=null)
		System.out.println(segmentCandi[i].getFrom());
		}*/
		for(int i=1;i<segmentCandi.length;i=i+2){
			if(segmentCandi[i]==null) continue;
			Link[] segment = new Link[10000];   //for segment at t0
			int indexSegment = 1;
			Link l1 = segmentCandi[i];    //get the candidate of segment
			Link l2 = segmentCandi[i+1];
			int l1id = l1.getLinkid();	  
			int l2id = l2.getLinkid();
			//System.out.println(l1id+" "+l2id+" "+segmentCandi.length);
			Map <Integer,Integer> l1map = new HashMap<Integer,Integer>();
			Map <Integer,Integer> l2map = new HashMap<Integer,Integer>();
			Link[] l1linkflow = linkflows.get(l1id);  //get the linkflow which contain these two links
			Link[] l2linkflow = linkflows.get(l2id);	
			
			int f=1;		
			
			for(int t=1;t<l1linkflow.length;t++){
				if(f==0) break;
				if(l1linkflow[t]!=null&&l1linkflow[t].getEndTiming()>0){
					//System.out.println(l1linkflow[t].getTo());
					int l1to = l1linkflow[t].getTo();
					int l1endTiming = l1linkflow[t].getEndTiming();
					for(int tt=1;tt<l2linkflow.length;tt++){
						if(l2linkflow[tt]!=null&&l2linkflow[tt].getEndTiming()>0){
							int l2to = l2linkflow[tt].getTo();
							int l2endTiming = l2linkflow[tt].getEndTiming();
							if(l2to == l1to){
								//System.out.println(l1to+"  "+l2to);
								for(int k=1;k<links.length;k++){
									if(links[k]==null) continue;
									if((links[k].getLinkid() == l1id && links[k].getEndTiming()<=l1endTiming) || (links[k].getLinkid() == l2id && links[k].getEndTiming()<=l2endTiming)){
										if(links[k].getStartTiming()<0 || links[k].getEndTiming()<=0) continue;   //segment should start from t0
										segment[indexSegment++] = links[k];
									//	System.out.println("linkid: "+links[k].getLinkid()+" from: "+links[k].from+" to: "+links[k].to+" startT: "+links[k].startTiming+" endT: "+links[k].endTiming+" flowid: "+links[k].flowid);
										}
									}
									segments.add(segment);
								f=0;
								break;
							}
						}
					}
				}
					
			}	
		/*	for(int xxx=0;xxx<l1linkflow.length;xxx++){
				if(l1linkflow[xxx]!=null)
				System.out.println("!!!!!!"+l1linkflow[xxx].flowid);
			}
			//System.out.println(l1linkflow.length+" "+l2linkflow.length);
			
			//record the destination and timing for each linkflow which has the candidate segment
			for(int x=1;x<l1linkflow.length;x++){
				//if(l1linkflow[x]!=null) System.out.println(l1linkflow[x].getEndTiming());
				if(l1linkflow[x]!=null&&l1linkflow[x].getEndTiming()>0){
					//System.out.println("dasdasdas");
					l1map.put(l1linkflow[x].getTo(),l1linkflow[x].getEndTiming());
					//System.out.println(l1linkflow[x].getLinkid()+" * "+l1linkflow[x].getTo()+" * "+l1linkflow[x].getEndTiming());
				}
			}
			
			for(int x=1;x<l2linkflow.length;x++){
				if(l2linkflow[x]!=null&&l2linkflow[x].getEndTiming()>0){
					l2map.put(l2linkflow[x].getTo(),l2linkflow[x].getEndTiming());
				//	System.out.println(l2linkflow[x].getLinkid()+" * "+l2linkflow[x].getTo()+" * "+l2linkflow[x].getEndTiming());
				}
			}
			//System.out.println(l1map.size()+" "+l2map.size());
			//find the real segment
			for(Map.Entry<Integer, Integer> l1entry: l1map.entrySet()){
				int endNode = l1entry.getKey();
				System.out.println(endNode+"***");
			}
			for(Map.Entry<Integer, Integer> l1entry: l1map.entrySet()){
				int endNode = l1entry.getKey();
				//System.out.println(endNode+"***");
				int l1endT = l1map.get(endNode);
				if(l2map.containsKey(endNode)){    //two flows experience the same node
				int l2endT = l2map.get(endNode);				
				//if(l1endT != l2endT || l1endT == l2endT ){   //if two flow experience the same node, it means they belong to one segment
					for(int k=1;k<links.length;k++){
					if(links[k]==null) continue;
					if((links[k].getLinkid() == l1id && links[k].getEndTiming()<=l1endT) || (links[k].getLinkid() == l2id && links[k].getEndTiming()<=l2endT)){
						if(links[k].getStartTiming()<0 || links[k].getEndTiming()<=0) continue;   //segment should start from t0
						segment[indexSegment++] = links[k];
					//	System.out.println("linkid: "+links[k].getLinkid()+" from: "+links[k].from+" to: "+links[k].to+" startT: "+links[k].startTiming+" endT: "+links[k].endTiming+" flowid: "+links[k].flowid);
						}
					}
					segments.add(segment);
					break; //find the first same ending node
				//}
			}
		  }*/	
			
		}
		//System.out.println("????? "+segments.size());

	}
	
	public static void findPastsegment(HashSet<Link[]>segments,Link[] links, HashMap<Integer, Link[]> linkflows,double[][] bandWidth,double[] throughput){
		//find the segments in the past(<t0) that have effects on the segments at t0
		HashSet<Link[]>tmpSet = new HashSet<Link[]>();
		for(Link[] l:segments){   //find the segment in the past which have effects on segment start at t0
			for(int i=0;i<l.length;i++){
				if(l[i]!=null){
					int f1id = l[i].getFlowid();
			//	System.out.println("linkid: "+l[i].getLinkid()+" from: "+l[i].from+" to: "+l[i].to+" startT: "+l[i].startTiming+" endT: "+l[i].endTiming+" flowid: "+l[i].flowid);
				for(int j=1;j<links.length;j++){
					if(links[j]!=null&&links[j].getLinkid()!=l[i].getLinkid()&&links[j].getFrom()==l[i].getFrom()&&links[j].getTo()==l[i].getTo()&&links[j].getStartTiming()==l[i].getStartTiming()&&links[j].getEndTiming()==l[i].getEndTiming()&&links[j].isStart()==true){
						int f2id = links[j].getFlowid();
						if(bandWidth[links[j].getFrom()][links[j].getTo()]<(throughput[f1id]+throughput[f2id])){ //the bandwidth of the link can't afford of the two flows
						Link[] tmp = linkflows.get(links[j].getLinkid());
						if(tmp[1].startTiming>=0) continue;
						/*System.out.println("linkid: "+l[i].getLinkid()+" from: "+l[i].from+" to: "+l[i].to+" startT: "+l[i].startTiming+" endT: "+l[i].endTiming+" flowid: "+l[i].flowid + " isStart "+l[i].isStart());
						System.out.println("----");
						System.out.println("linkid: "+links[j].getLinkid()+" from: "+links[j].from+" to: "+links[j].to+" startT: "+links[j].startTiming+" endT: "+links[j].endTiming+" flowid: "+links[j].flowid+" isStart "+links[j].isStart());
						System.out.println("************");*/
						int tmplid=0;
						for(int k=1;k<links.length;k++){
							if(links[k]!=null&&links[k].getStartTiming()==tmp[1].getStartTiming()&&links[k].isEnd()==true&&links[k].getFrom()==tmp[1].getFrom()&&links[k].getFlowid()==tmp[1].getFlowid()){
								//System.out.println(links[k].getStartTiming()+" MMM "+tmp[1].getStartTiming());
								tmplid=links[k].getLinkid();
								break;
							}
						}
						Link[] tLink = linkflows.get(tmplid);
						Link[] tmpSeg = new Link [20];
						int tmpindex=1;
						for(int x=1;x<tmp.length;x++){
							if(tmp[x]==null) continue;
							tmpSeg[tmpindex]=tmp[x];
							tmpindex++;
						}
						for(int x=1;x<tLink.length;x++){
							if(tLink[x]==null) continue;
							tmpSeg[tmpindex]=tLink[x];
							tmpindex++;
						}
						//System.out.println("dasdadas");
						//segments.add(tmpSeg);
						tmpSet.add(tmpSeg);
						//System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
						}
					}
				}
			}
		  }
			//System.out.println("*****");
		}
		if(tmpSet.size()>0){
			for(Link[] l : tmpSet){
				segments.add(l);
			}
		}
	}
	
	public static void findDependencyRelation(HashSet<HashMap<Integer, Integer>> dependRelationSet, HashMap<Integer, Link[]>segmentsMap,double [][] bandWidth,double[] throughput) {
		//according the segments to find the dependency relation of them, a ---> b means a should happen earlier than b or congestion will happen
		for(Map.Entry<Integer,Link[]>segEntry:segmentsMap.entrySet()){  //find dependency relation
			Link[] l = segEntry.getValue();
			int segId = segEntry.getKey();
			int startT = l[1].getStartTiming();
			int f1id = l[1].getFlowid();
			for(int i=1;i<l.length;i++){
				if(l[i]!=null){
			//	System.out.println("linkid: "+l[i].getLinkid()+" from: "+l[i].from+" to: "+l[i].to+" startT: "+l[i].startTiming+" endT: "+l[i].endTiming+" flowid: "+l[i].flowid);
				for(Map.Entry<Integer,Link[]> segTmp:segmentsMap.entrySet()){
					int segTmpId =segTmp.getKey();
					if(segTmpId == segId) continue; //for except the same segment
					Link[] tmpl = segTmp.getValue();
					int startTmpT = tmpl[1].getStartTiming();
					int f2id = tmpl[1].getFlowid();
					for(int j=1;j<tmpl.length;j++){
						if(tmpl[j]==null) continue;
						if(l[i].getFrom()==tmpl[j].getFrom()&&l[i].getTo()==tmpl[j].getTo()&&l[i].getStartTiming()==tmpl[j].getStartTiming()&&l[i].getEndTiming()==tmpl[j].getEndTiming()){
							//find two segments has same link (the bandwidth of the link can't afford two links), the early segment --> the later segment
							if(bandWidth[l[i].getFrom()][l[i].getTo()]<(throughput[f1id]+throughput[f2id])&&startT<=startTmpT){ 
								HashMap<Integer, Integer> dependRelation = new HashMap<Integer,Integer>();
								dependRelation.put(segId, segTmpId);
								dependRelationSet.add(dependRelation);
								//System.out.println(segId+" "+segTmpId);
								break;
							}
						}
					}	
			    	}			
				}
			}
		}
	}
	
	public static void turnSet2Map(HashSet<Link[]> segments, HashMap<Integer,Link[]>segmentsMap){
		//just turn the hashset to hashmap for get the key
		int segmentId=1;
		for(Link[] l:segments){   
			segmentsMap.put(segmentId++, l);
			for(int i=0;i<l.length;i++){
				if(l[i]!=null){
				//System.out.println("linkid: "+l[i].getLinkid()+" from: "+l[i].from+" to: "+l[i].to+" startT: "+l[i].startTiming+" endT: "+l[i].endTiming+" flowid: "+l[i].flowid);
				}
			}
		}
		
	}
	
	public static void adjustUpdateTime(HashSet<HashMap<Integer, Integer>> dependRelationSet, HashMap<Integer, Link[]>segmentsMap){ 
		//adjust the update time make all segments > t0
		for(HashMap<Integer,Integer> dependRelation : dependRelationSet){
			for(Map.Entry<Integer,Integer>deprelEntry:dependRelation.entrySet()){
				Link[] l1 = segmentsMap.get(deprelEntry.getKey()); //early segment
				Link[] l2 = segmentsMap.get(deprelEntry.getValue()); // later segment
				if(l1[1].getStartTiming()<0){
					int increment = 0 - l1[1].getStartTiming();
					for(int i=1;i<l1.length;i++){
						if(l1[i]!=null){
							l1[i].setStartTiming(l1[i].getStartTiming()+increment);
							l1[i].setEndTiming(l1[i].getEndTiming()+increment);
						}
					}
					for(int i=1;i<l2.length;i++){
						if(l2[i]!=null){
							l2[i].setStartTiming(l2[i].getStartTiming()+increment);
							l2[i].setEndTiming(l2[i].getEndTiming()+increment);
						}
					}
					//segmentsMap.put(deprelEntry.getKey(), l1);
					//segmentsMap.replace(deprelEntry.getKey(),l1);
					//segmentsMap.replace(deprelEntry.getValue(),l2);
				}
			}
		}
	}
	
	public static int isLinkEqual(Link[]l1, Link[]l2,int choose){   
		//compare whether two links are same
		//judeg whether two links are  same
		// choose == 1  --> totally same (include timing)
		// choose == 2  --> exclude timing
		int cnt=0;
		int flag = 1;
		int index = 1;
		int le1=1; int le2=1;
		Link[] tmp1 = new Link[l1.length];
		Link[] tmp2 = new Link[l2.length];
		for(int i=0;i<l1.length;i++){
			if(l1[i]!=null){
				tmp1[le1] = l1[i];
				le1++;
			}
		}
		for(int i=0;i<l2.length;i++){
			if(l2[i]!=null) {
				tmp2[le2]=l2[i];
				le2++;
			}
		}
		//System.out.println(l1.length+" MMM "+le1);
		if(le1!=le2){
			flag = 0;
			return flag;
		}
		for(index=1;index<le1;index++){
			//if(l2[index]==null) System.out.println(index+" "+le1+" "+le2);
			if(choose == 1){
				if(tmp1[index].getFrom()!=tmp2[index].getFrom()||tmp1[index].getTo()!=tmp2[index].getTo()||tmp1[index].getStartTiming()!=tmp2[index].getStartTiming()||tmp1[index].getEndTiming()!=tmp2[index].getEndTiming()||tmp1[index].getFlowid()!=tmp2[index].getFlowid()||tmp1[index].isStart()!=tmp2[index].isStart()){
					flag = 0;
					break;
				}
			}
			else if(choose == 2){
				if(tmp1[index].getFrom()==tmp2[index].getFrom()&&tmp1[index].getTo()==tmp2[index].getTo()&&tmp1[index].getFlowid()==tmp2[index].getFlowid()&&(tmp1[index].getStartTiming()!=tmp2[index].getStartTiming())){
					cnt++;
				}
			}
			
		}
		if(choose==2&&cnt == le1){  // the two links are the same except they are at different timing
			flag = 1;
		}else if(choose==2){
			flag = 0;
		}
		return flag;
	}
	
	public static void mergesegments(HashMap<Integer,Link[]>segmentsMap, HashSet<HashMap<Integer, Integer>> dependRelationSet ){ 
		// if there are two segments are same totally, then merge them and update the dependency relation set
		int[] remKey =new int[100000000];
		int index=1;
		for(Map.Entry<Integer,Link[]> s1entry:segmentsMap.entrySet()){
			int l1key = s1entry.getKey();
			Link[] l1 = s1entry.getValue();
				for(Map.Entry<Integer, Link[]> s2entry:segmentsMap.entrySet()){
					if(s2entry.getKey() == l1key) continue;
					Link[]l2 = s2entry.getValue();
					if(isLinkEqual(l1, l2,1)==1){  // two segments are totally same
					//	System.out.println(l1key+" &&& "+s2entry.getKey());
						int replace = Math.min(l1key, s2entry.getKey());
						int rem = Math.max(l1key,s2entry.getKey());
						//System.out.println(replace+" "+rem);
						HashSet<HashMap<Integer,Integer>> tmpReplaceSet = new HashSet<HashMap<Integer,Integer>>();
						HashSet<HashMap<Integer,Integer>> tmpDeleteSet = new HashSet<HashMap<Integer,Integer>>();
						for(HashMap<Integer,Integer> tmpDep : dependRelationSet){
							if(tmpDep.containsKey(rem)){
								tmpDeleteSet.add(tmpDep);
								HashMap<Integer,Integer> tmp = new HashMap<Integer,Integer>();
								tmp.put(replace,tmpDep.get(rem));
								tmpReplaceSet.add(tmp);
								//dependRelationSet.remove(tmpDep);
								//dependRelationSet.add(tmp);
								//break; // 0703
							}
						}
						for(HashMap<Integer,Integer> t : tmpDeleteSet){
							dependRelationSet.remove(t);
						}
						for(HashMap<Integer,Integer> t : tmpReplaceSet){
							dependRelationSet.add(t);
						}
						remKey[index++] = Math.max(l1key, s2entry.getKey());
					}
				}
			}
			System.out.println("rem index:" + index);
		for(int i=1;i<remKey.length;i++){
			if(segmentsMap.containsKey(remKey[i])){
				//System.out.println(remKey[i]);
				segmentsMap.remove(remKey[i]);
			}
		}
		//return remKey;
	}
	
	public static boolean checkFeasible(HashMap<Integer,Link[]> segmentsMap){
		// check whether exist a makes O(t-a) = O(a),if exist then exit the program
		for(Map.Entry<Integer,Link[]> s1entry:segmentsMap.entrySet()){
			Link[] l1 = s1entry.getValue();
			int l1key = s1entry.getKey();
			for(Map.Entry<Integer, Link[]> s2entry:segmentsMap.entrySet()){
				if(s2entry.getKey() == l1key) continue;
				Link[]l2 = s2entry.getValue();
				if(isLinkEqual(l1, l2, 2)==1){   // exist a makes O(t-a) = O(a), exit the program
					return false;
				}
			}
			
		}
		return true;	
	}
	
	public static void updateSegments(HashMap<Integer,Link[]> segmentsMap,int[] segments){
		//after updating segments, we need update the segments set
		for(int i=1;i<segments.length;i++){
			int sid = segments[i];
			if(sid!=0){
				segmentsMap.remove(sid);
			}
		}
	}
	
	public static HashMap<Integer,Path> getAllPath(int[][] flows,double [][]bandWidth,double[] throughput,int cnt){
		/*for(int i=1;i<throughput.length;i++){
			System.out.println("flow"+i+" : "+throughput[i]);
		}*/
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
						tmpP.setCapacity(tmpP.getCapacity() - throughput[i/2+1]);  //if different flow has same path, minus throughput of the flow
						if(tmpP.getCapacity()<0){
							System.out.println("capacity < 0 , offload > bandwidth, exit...");
							System.exit(0);
						}
						//	paths.replace(pEntry.getKey(),tmpP);
						
					}
				}
				if(f==1){  //first time to find the path
					Path path = new Path();
					double capa = bandWidth[u][v] - throughput[i/2+1];
					//System.out.println(u+" -> "+v+" "+bandWidth[u][v]+" "+throughput[i%2+1]);
					if(capa < 0){
						System.out.println("capacity < 0 , offload > bandwidth, exit...");
						System.exit(0);
					}
					path.setPath(u, v, capa, start, end);
					paths.put(index, path);
					index++;
				}
			}
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
	
/*	public static HashMap<Integer,Path> getAllPath(HashMap<Integer,Link[]> segments,int[][] flows,double [][]bandWidth,double[] throughput){
		//get all the path that the flows go through(include final flow)
		HashMap<Integer,Path> paths = new HashMap<Integer,Path>();
		int index = 1;
		for(Map.Entry<Integer,Link[]> sEntry : segments.entrySet()){
			int sid = sEntry.getKey();
			Link[] l = sEntry.getValue();
			int fid = l[1].getFlowid();
			for(int i = 0;i<l.length;i++){
				if(l[i]!=null){
					int u = l[i].getFrom();
					int v = l[i].getTo();
					int f = 1;
					if(l[i].isStart() == true){
						for(Map.Entry<Integer,Path> pEntry : paths.entrySet()){
							Path tmpP = pEntry.getValue();
							//if(tmpP.getU()==u&&tmpP.getV()==v&&tmpP.isStart()==start&&tmpP.isEnd()==end){
							if(tmpP.getU()==u&&tmpP.getV()==v){
								f = 0;
								tmpP.setCapacity(tmpP.getCapacity() - throughput[fid]);  //if different flow has same path, minus throughput of the flow
							//	paths.replace(pEntry.getKey(),tmpP);
							}
						}
						if(f==1){  //first time to find the path
							Path path = new Path();
							double capa = bandWidth[u][v] - throughput[fid];
							path.setPath(u, v, capa, true, false);
							paths.put(index, path);
							index++;
						}
					  }
					else if(l[i].isEnd()==true){
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
							path.setPath(u, v, capa, false,true);
							paths.put(index, path);
							index++;
						}
						
					}
				}
			}
			
		}
		return paths;
	}*/
	
	public static int findPathID(Link l,HashMap<Integer,Path> paths){
		//find the corresponding path id of the link l 
		int rtn=0;
		for(Map.Entry<Integer,Path> pEntry : paths.entrySet()){
			int pid = pEntry.getKey();
			Path path = pEntry.getValue();
			if(path.getU()==l.getFrom()&&path.getV()==l.getTo()){
				rtn = pid;
				break;
			}
		}
		return rtn;
	}
	
	public static HashSet<PathSegmentRelation> buildDependencyGraph(HashMap<Integer, Link[]> segments,HashMap<Integer,Path> paths) {
		//build dependency graph
		HashSet<PathSegmentRelation> dependencyG = new HashSet<PathSegmentRelation>();
		for(Map.Entry<Integer,Link[]> sEntry:segments.entrySet()){
			int sid = sEntry.getKey();
			Link[] seg = sEntry.getValue();
				for(Link l : seg){
					if(l!=null){
						int pid = findPathID(l, paths);
						PathSegmentRelation psr = new PathSegmentRelation();
						// 0 represent path ---> segment; 1 represent segment ---> path
						if(l.isStart() == true){
							psr.setParameter(pid, sid, 0);
						}else if(l.isEnd() == true){
							psr.setParameter(pid, sid, 1);
						}
						dependencyG.add(psr);
					}
				}
		}
		return dependencyG;
	}
	
	public static boolean IsIndependentSegments(HashSet<PathSegmentRelation> dependencyG,int sid,double throughput,HashMap<Integer, Path> paths){
		//judge whether a segment is a independent segment
		boolean rtn = true;
		for(PathSegmentRelation psr : dependencyG){
			if(psr.getSegmentId() == sid && psr.getRelation() == 1){  //find the outgoing link of this segment
				int pid = psr.getPathId();
				Path path = paths.get(pid);
				if(path.getCapacity() < throughput){  //if the capacity of the outgoing link < df, it's not independent segments
					rtn = false;
					return rtn;
				}
			}
		}
		return rtn;
	}
	
/*	public static void findDependencyCircle(HashSet<PathSegmentRelation> dependencyG, int sid){
		
	}*/
	public static boolean updateDependencyGraph(HashSet<PathSegmentRelation> dependencyG,int[]segments,HashMap<Integer,Path> paths,HashMap<Integer,Link[]> segmentsMap,double[] throughput,double[][]bandwidth){
		//update the independent segments
		boolean success = true;
		for(int i=1;i<segments.length;i++){
			int sid = segments[i];
			if(sid == 0) continue;
			//System.out.println("sid is : "+sid);
			Link[] l = segmentsMap.get(sid);
			int fid = l[1].getFlowid();
				//System.out.println(independentSegment[i]);
				for(PathSegmentRelation psr : dependencyG){
					if(psr.getSegmentId()==sid&&psr.getRelation()==1){  // outgoing links, capacity minus df
						int pid = psr.getPathId();
						Path path = paths.get(pid);
						path.setCapacity(path.getCapacity() - throughput[fid]);
						//dependencyG.remove(psr);
					//	paths.replace(pid, path);
					//	paths.put(pid, path);
					}
					if(psr.getSegmentId()==sid&&psr.getRelation()==0){  // incoming links, capacity plus df
						int pid = psr.getPathId();
						Path path = paths.get(pid);
						path.setCapacity(path.getCapacity() + throughput[fid]);
					//	paths.replace(pid, path);
					//	paths.put(sid, path);
					}
				}
		}
		success = checkDependencyGraph(paths,bandwidth);
		return success;
	}
	
	public  static boolean checkDependencyGraph(HashMap<Integer,Path> paths,double[][]bandwidth){
		//check whether all links'capacity > 0 in dependency graph 
		boolean rtn = true;
		for(Map.Entry<Integer,Path> pEntry : paths.entrySet()){
			Path path = pEntry.getValue();
			if(path.getCapacity()<0 ){
				System.out.println("warning: path:"+pEntry.getKey()+" capacity is < 0");
				rtn = false;
				break;
			}
			if(path.getCapacity()>bandwidth[path.getU()][path.getV()]){
				System.out.println("warning: path:"+pEntry.getKey()+" capacity is > throughput, impossible");
				System.out.println(path.getU()+" -> "+path.getV()+" "+path.getCapacity());
				rtn = false;
				break;
			}
		}
		return rtn;
	}
	
	public static int getDelayOfIndependentSegment(int[] independentSgements,HashMap<Integer,Link[]>segmentsMap){
		//get the max endtiming of the independent segments updated at t0
		int maxDelay=0;
		for(Map.Entry<Integer, Link[]> sEntry : segmentsMap.entrySet()){
			int sid = sEntry.getKey();
			//System.out.println("sssid:"+sid);
			Link[] link = sEntry.getValue();
			for(int i=1;i<independentSgements.length;i++){ //find the independent segments 
				if(independentSgements[i]!=0&&sid==independentSgements[i]){
					for(int j=1;j<link.length;j++){
						if(link[j]!=null){
							if(link[j].isStart()==true&&link[j].getEndTiming()>maxDelay){ //just find the initial flows cause the final flows will be updated
								//System.out.println("(*^^%*&^%$(&*^*&");
								maxDelay = link[j].getEndTiming();
							}
						}
					}
				}
			}
		}
		return maxDelay;
	}
	
	public  static void deferOtherSegments(int[] independentSegments, HashMap<Integer,Link[]> segmentsMap,int delay) {
		for(Map.Entry<Integer,Link[]> sEntry : segmentsMap.entrySet()){
		int sid = sEntry.getKey();
		Link[] link = sEntry.getValue();
		for(int j=1;j<link.length;j++){
			if(link[j]!=null){
				link[j].setStartTiming(link[j].getStartTiming() + delay);  //defer other segments's statrt timing
				link[j].setEndTiming(link[j].getEndTiming() + delay);
				//System.out.println("!@#:"+link[j].getStartTiming()+"  "+link[j].getEndTiming());
		       }
	        }
	    }	
	 }
	
	public  static void printsegments(HashMap<Integer,Link[]> segmentsMap) {  
		//print the info of all segments
		if(segmentsMap.size()==0){
			System.out.println("there are no segments!");
		}
		for(Map.Entry<Integer,Link[]>showEntry:segmentsMap.entrySet() ){
			Link[] l = showEntry.getValue();
			int key = showEntry.getKey();
			for(int i=1;i<l.length;i++){
				if(l[i]!=null){
				System.out.println("segmentId: "+key+" linkid: "+l[i].getLinkid()+" from: "+l[i].from+" to: "+l[i].to+" startT: "+l[i].startTiming+" endT: "+l[i].endTiming+" flowid: "+l[i].flowid+" isStart: "+l[i].isStart()+" isEnd: "+l[i].isEnd());
				}
		}
			System.out.println("#######");
		}
	}
	
	public static void printDependRelation(HashSet<HashMap<Integer,Integer>> dependRelationSet){  
		//print the dependency relation of segments
		for(HashMap<Integer,Integer> tmpDep:dependRelationSet){
			for(Map.Entry<Integer,Integer> depRelEntry : tmpDep.entrySet()){
				System.out.println("sgement: "+depRelEntry.getKey()+" ---> segment: "+depRelEntry.getValue());
			}
		}
		
	}
	
	public static void printPaths(HashMap<Integer,Path> paths) {
		for(Map.Entry<Integer,Path> pEntry : paths.entrySet()){
			Path p = pEntry.getValue();
			System.out.println("pathId: "+pEntry.getKey()+" from: "+p.getU()+" to: "+p.getV()+" capa: "+p.getCapacity()+" isStart: "+p.isStart()+" isEnd: "+p.isEnd());
		}
	}
	
	public static void printDependecnyGraph(HashSet<PathSegmentRelation> dependencyG){
		for(PathSegmentRelation psr : dependencyG){
			if(psr.getRelation() == 0){
				System.out.println("pid: "+psr.getPathId()+" ---> sid: "+psr.getSegmentId());
			}else{
				System.out.println("sid: "+psr.getSegmentId()+" ---> pid: "+psr.getPathId());
			}
		}
	}
	
	public static void checkDependecnyGraph(HashSet<PathSegmentRelation> dependencyG){
		for(PathSegmentRelation psr : dependencyG){
			int pid = psr.getPathId();
			int sid = psr.getSegmentId();
			int r = psr.getRelation();
			int x = 1;
			for(PathSegmentRelation psr2:dependencyG){
				if(psr2.getPathId()==pid&&psr2.getRelation()==r){
					x++;
				}
			}
			if(x>2){
				System.out.println(pid);
				System.out.println("**************************");
			}
		}
	}
	
	public static double calLinkUtlization(HashMap<Integer,Path> paths, double[][]bandwidth){
		double max = -1;
		for(Map.Entry<Integer,Path> pEntry : paths.entrySet()){
			Path p = pEntry.getValue();
			int from = p.getU();
			int to = p.getV();
			double utlization = 1-(p.getCapacity()/bandwidth[from][to]);
			if(utlization > max ){
				max = utlization;
			}
		}
		return max;
	}
	
	public static int getRandom(int min,int max){
		Random random = new Random();
		int s = random.nextInt(max)%(max-min+1) + min;
		return s;
	}
	
	public static void main(String args[]) throws IOException{
		
	//	int [][] flows = {{1,3,4,5},{1,2,5},{1,2,5},{1,3,2,4,5}}; //initial flow1 , final flow1, initial flow2 , final flow2 ....
		
		//int [][] flows = {{2,3},{2,4,3},{2,1},{2,3,1},{2,5},{2,4,5}};
		//int [][] flows = new int[1000][25];
		
		int [][]flows = new int[3000][];
		String filePath = "E:\\eclipseworkspace\\TimedUpdate\\input_flow.txt";

		BufferedReader br = new BufferedReader(new InputStreamReader(  
	             new FileInputStream(filePath)));  
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
	    // System.out.println("ccc"+cnt+flows.length);
	/*	for(int i=0;i<cnt;i++){
			System.out.println(i);
			for(int j=0;j<flows[i].length;j++){
				if(flows[i][j]!=0)
				System.out.print(flows[i][j]+" ");
			}
			System.out.println();
		}*/
	     
		//double [] throughput={0,1,1,1};  // 0,throughput of flow1, throughput of flow2.....
		double [] throughput = new double[cnt/2+1];
		for(int i=1;i<=cnt/2;i++){
			throughput[i] = (double)getRandom(1, 3);
			//System.out.println("throughput of flow"+i+" is:"+throughput[i]);
			//throughput[i] = 1;
		}
	     
		Topology topology = new Topology();
		topology.init(25,60,-1);
		//int [][] topo = topology.getTopo();
		double [][] delay = topology.getDelay();
		double [][] bandWidth = topology.getBandWidth();
		int linkNumber = 10000000;
		HashMap<Link[], Integer> LinksMap = new HashMap<Link[], Integer>();
		Link [] links = new Link[linkNumber];  //for all links
		int time = 30;
		int index = 1;
		int flowid = 0;
		int linkid = 0;
		int startTmp = -20;;  // startTmp ... t0 ... time
		boolean start;
		boolean end;
		int startTiming;
		
		HashMap<Integer, Link[]> linkflows = new HashMap<Integer,Link[]>(); // for the links belong to one flow
		//the rule of flow components are : initial flow of flow1, final flow of flow1, initial flow of flow2, final flow of flow2 ....
		for(int t=0;t<cnt;t++){
			if(t%2==0){         //initial flow
				flowid++;   
				start = true;
				end = false;
			}else{	           //final flow
				start = false;
				end = true;				
			}
			//for each timing, find all the links of the flow start at this timing first
			for(int i=startTmp;i<=time;i++){   
				 startTiming = i;
				 linkid++;
				 Link[] linkflow = new Link[1000];
				 int indexLF = 1;
				for(int j=0;j<flows[t].length-1;j++){
					//if(flows[t][j]==0 || flows[t][j+1]==0) break;
					int from = flows[t][j];
					int to = flows[t][j+1];
					int endTiming = startTiming + (int)delay[from][to];
					//System.out.println(endTiming);
					if(endTiming > time || startTiming >= time)
						break;
					links[index] = new Link();
					links[index].setLink(from, to, startTiming, endTiming, flowid,linkid,start, end);	
					index++;
					linkflow[indexLF] = new Link();
					linkflow[indexLF].setLink(from, to, startTiming, endTiming, flowid, linkid, start, end);  //record the links belong to one flow
					//System.out.println(linkflow[indexLF].getFrom());
					indexLF++;
					startTiming = endTiming;
				}
				linkflows.put(linkid, linkflow);
			}
		}
		
		/*for(Map.Entry<Integer,Link[]> lEntry : linkflows.entrySet()){
			Link[] t = lEntry.getValue();
			for(int i=1;i<t.length;i++){
				if(t[i]!=null)
				System.out.println(t[i].getFrom());
			}
		}*/
		
		Link [] tempLinks = new Link[linkNumber];    //all links start from t0
		Link [] segmentCandi = new Link[linkNumber]; // segments candidate
	
		findsegmentsCandidate(tempLinks, links, segmentCandi);
		System.out.println("1");
	/*	for(int i=0;i<segmentCandi.length;i++){
		if(segmentCandi[i]!=null)
		System.out.println(segmentCandi[i].getFrom());
		}*/

		HashSet<Link[]> segments = new HashSet<Link[]>();  //set of segment
		findsegmentAtT0(segments, links, linkflows, segmentCandi);
		System.out.println("2");

		//findPastsegment(segments, links, linkflows, bandWidth,throughput);
		System.out.println("3");

		HashMap<Integer,Link[]> segmentsMap = new HashMap<Integer,Link[]>(); // segments map 
		
		turnSet2Map(segments, segmentsMap);
		//printsegments(segmentsMap);
		//System.out.println("----------");
		//HashMap<Integer, Integer> dependRelation = new HashMap<Integer,Integer>();  // dependency relation map
		HashSet<HashMap<Integer,Integer>> dependRelationSet = new HashSet<HashMap<Integer,Integer>>();
		
		findDependencyRelation(dependRelationSet, segmentsMap, bandWidth,throughput);
		
		System.out.println("4");

		System.out.println(dependRelationSet.size()+"!!!");
		
		adjustUpdateTime(dependRelationSet,segmentsMap);
		System.out.println("5");

		//printsegments(segmentsMap);
		mergesegments(segmentsMap,dependRelationSet);
		System.out.println("6");

		printsegments(segmentsMap);	
		
		//printDependRelation(dependRelationSet);
		
		if(checkFeasible(segmentsMap)==false){
			System.out.println("exist a makes O(t-a) = O(a),exits...");
			System.exit(0);
		}
		
		//HashMap<Integer, Path> paths = getAllPath(segmentsMap,flows, bandWidth,throughput);
		HashMap<Integer, Path> paths = getAllPath(flows, bandWidth,throughput,cnt);
		System.out.println("7");

		printPaths(paths);
		
		HashSet<PathSegmentRelation> dependencyGraph = buildDependencyGraph(segmentsMap, paths);
		System.out.println("8");

		//printDependecnyGraph(dependencyGraph);
		
		//checkDependecnyGraph(dependencyGraph);
		
		//sortSegmentWithDF();
		
		for(int t=0;t<time;t++){
			//find the independent segments
			int []independentSegments = new int[segmentsMap.size()+1];
			int tmpCnt = 1;
			int []dependentSegments = new int[segmentsMap.size()+1];
			int tmpCnt2 = 1;
			for(Map.Entry<Integer,Link[]> sEntry : segmentsMap.entrySet()){   
				boolean f = false;
				Link[] l = sEntry.getValue();
				int sid = sEntry.getKey();
				int fid = l[1].getFlowid();
				double df = throughput[fid];
				if(l[1].getStartTiming() == t){
					 f = IsIndependentSegments(dependencyGraph, sid, df, paths);
					 if(f==true){
							independentSegments[tmpCnt] = sid;
							tmpCnt++;
							System.out.println("the segment: "+sid+" is a independent segment at:t"+t);
						}else{
							dependentSegments[tmpCnt2] = sid;
							tmpCnt2++;
							System.out.println("the segment: "+sid+" is a dependent segment at:t"+t);
						}			 
				}		
			}
			
			int maxDelay = getDelayOfIndependentSegment(independentSegments, segmentsMap);
			//System.out.println("maxDelay: "+maxDelay+" at t"+t);
			
			boolean continueFlag = true; 
			continueFlag = updateDependencyGraph(dependencyGraph, independentSegments, paths, segmentsMap, throughput,bandWidth);
			if(continueFlag == false){
				System.out.println("program ends cause the update of independent segments is failed although it's impossible! ");
				System.exit(0);
			}else if(independentSegments.length>1&&independentSegments[1]!=0){
					System.out.println("the segments after updating independent segments at t"+t);
					updateSegments(segmentsMap,independentSegments);
					printsegments(segmentsMap);
					System.out.println("the dependentGraph after updating independent segments at t"+t);
					printPaths(paths);
					double maxUt = calLinkUtlization(paths, bandWidth);
					System.out.println("the max utiliztion at t"+t+" is: "+maxUt);
				}
			
		
			deferOtherSegments(independentSegments, segmentsMap, maxDelay);
			//printsegments(segmentsMap);
			
			//exclude the dependentSgements which are not at t
			for(int i=1;i<dependentSegments.length;i++){
				if(dependentSegments[i]!=0){
					Link[] l = segmentsMap.get(dependentSegments[i]);
					if(l[1].getStartTiming()!=t){
						dependentSegments[i]=0;
					}
				}
			}
			
			continueFlag = updateDependencyGraph(dependencyGraph, dependentSegments, paths, segmentsMap, throughput,bandWidth);
			if(continueFlag == false){
				System.out.println("program ends cause the update of dependent segments is failed, so the solution doesn't exist!");
				System.exit(0);
			}else{
				if(dependentSegments.length>1&&dependentSegments[1]!=0){
					System.out.println("the segments after updating dependent segments at t"+t);
					updateSegments(segmentsMap,dependentSegments);
					printsegments(segmentsMap);
					System.out.println("the dependentGraph after updating dependent segments at t"+t);
					printPaths(paths);
					double maxUt = calLinkUtlization(paths, bandWidth);
					System.out.println("the max utiliztion at t"+t+" is: "+maxUt);
				}
			}
		}
		if(segmentsMap.size()==0){
			System.out.println("program has found a solution, congratulation :):):)");
		}
		
		System.out.println("end");	
	}    
}