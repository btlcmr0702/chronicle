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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.IntPredicate;

import javax.security.auth.kerberos.KerberosKey;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import org.omg.CORBA.ORBPackage.InconsistentTypeCode;
import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;
import org.w3c.dom.stylesheets.LinkStyle;

public class TimedUpdate {
	private static int numOfSwitch = 100;

	public static void findsegmentsCandidate(Link[] tempLinks,HashMap<Link,Integer> linksMap,Link[] segmentCandi){
		//first find the links at t0, then according the paper to find the segments candidate
		int indexTemp = 1;
		for(Map.Entry<Link,Integer> lEntry : linksMap.entrySet()){
			Link link = lEntry.getKey();
			if(link.getStartTiming()==0){
				tempLinks[indexTemp] = link;
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

	}
	
	public static void findsegmentAtT0(HashSet<Link[]>segments,HashMap<Link,Integer> linksMap,HashMap<Integer, Link[]> linkflows,Link[] segmentCandi){
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
			//for prove this is a segment, we need to ensure these two links end at different timing
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
								for(Map.Entry<Link,Integer> lEntry : linksMap.entrySet()){
									Link link = lEntry.getKey();
									if((link.getLinkid() == l1id && link.getEndTiming()<=l1endTiming) || (link.getLinkid() == l2id && link.getEndTiming()<=l2endTiming)){
										if(link.getStartTiming()<0 || link.getEndTiming()<=0) continue;   //segment should start from t0
										segment[indexSegment++] = link;
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
		
		}

	}
	//for some links across several timing, divide them for calculate load; such as fig2 in paper , v3 -> v4 from t0-t3,we should divide it into v3 -> v4 at t0-t1,t1-t2,t2-t3
		public static HashMap<Link,Integer> dividedLinks(HashMap<Link,Integer> links,HashSet<Link[]> segments){
			int size = links.size();
			HashMap<Link,Integer> newLinksMap = new HashMap<Link,Integer>();
			for(Map.Entry<Link,Integer> lEntry : links.entrySet()){
				Link key = lEntry.getKey();
				int value = lEntry.getValue();
				if((key.getEndTiming()-key.getStartTiming()==1)){
					newLinksMap.put(key, value);
				}
			}
			for(Link[] tlinks : segments){
				for(int i=0;i<tlinks.length;i++){
					if(tlinks[i]==null) continue;
					Link link = tlinks[i];
					int startTiming = link.getStartTiming();
					int endTiming = link.getEndTiming();
					int diff = endTiming - startTiming;
					if(diff > 1){
						int from = link.getFrom();
						int to = link.getTo();
						int fid = link.getFlowid();
						int linkid = link.getLinkid();
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
						//linkid++;
					}
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
					int linkid = link.getLinkid();
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
					//linkid++;
				}
			}
			return newLinksMap;
		}
		
		
		
	/*	public static void fff(HashSet<Link[]>segments,HashMap<Link,Integer>linksMap, HashMap<Integer, Link[]> linkflows,double[][] bandWidth,double[] throughput,int endTime){
			
			HashMap<Integer,Integer> linksLidMap = new HashMap<Integer,Integer>();
			for(Link[] s : segments){
				for(int i=0;i<s.length;i++){
					if(s[i]!=null){
						linksLidMap.put(s[i].linkid,0);
					}
				}
			}		
			HashMap<Integer,Link[]> tmpSegMap = new HashMap<Integer, Link[]>();
			HashMap<Integer,Integer> lid_inc = new HashMap<Integer,Integer>();
			HashMap<Integer,Integer> lid_sid = new HashMap<Integer,Integer>(); 

			turnSet2Map(segments, tmpSegMap);
			for(Map.Entry<Integer,Link[]> tsm : tmpSegMap.entrySet()){
				//HashSet<Integer> tmpSeg = new HashSet<Integer>();
				int sid = tsm.getKey();
				System.out.println("sid:"+sid);
				Link[] seg = tsm.getValue();
				for(int i=0;i<seg.length;i++){
					if(seg[i]==null) continue;
					int from = seg[i].getFrom();
					int to = seg[i].getTo();
					int startT = seg[i].getStartTiming();
					int endT = seg[i].getEndTiming();					
					int f2=0;
					HashMap<Integer,Integer> tmp_lid_inc = new HashMap<Integer,Integer>();
					HashMap<Integer,Integer> tmp_lid_sid = new HashMap<Integer,Integer>();

					int minStartT = 1000;
					double df=0;
					int pastFlag = 0;
					
					for(Map.Entry<Link,Integer> lEntry: linksMap.entrySet()){
						Link link = lEntry.getKey();
						int lid = link.getLinkid();
						Link[] tf = linkflows.get(link.getLinkid());
						if(tf[1].getStartTiming()>0) continue;  //just consider the linkflows belong to segment at t0 or belong to past
					
						if(link.getFrom()==from&&link.getTo()==to&&link.getStartTiming()==startT&&link.getEndTiming()==endT){
							if(!linksLidMap.containsKey(lid)&&tf[1].isStart()==false){
								continue;
							}
							df += throughput[link.getFlowid()];
							tmp_lid_inc.put(lid, 0);
							tmp_lid_sid.put(lid,sid);
							if(!linksLidMap.containsKey(lid)&&tf[1].getStartTiming()<minStartT){
								pastFlag = 1;
								minStartT = tf[1].getStartTiming();
								//System.out.println("min:"+minStartT);
							}
						}
					}
					
					if(df>bandWidth[from][to]) f2=1;

					if(f2==1 && pastFlag==1){
						for(Map.Entry<Integer, Integer> ttEntry : tmp_lid_sid.entrySet()){
							int key = ttEntry.getKey();
							int val = ttEntry.getValue();
							if(lid_sid.containsKey(key)){   //the past segment correspond different segments at t0, infeasible
								int val2 = lid_sid.get(key);
								if(val!=val2){
									System.out.println("infessssssssssssss");
									System.exit(0);
								}
							}
							lid_sid.put(key, val);
						}
						
						for(Map.Entry<Integer,Integer> tEntry : tmp_lid_inc.entrySet()){
							int lid = tEntry.getKey();
							//System.out.println("@@ "+lid+" "+(0-minStartT));
							lid_inc.put(lid, 0-minStartT);
						}
					}else if(f2==0){
						tmp_lid_inc.clear();
						tmp_lid_sid.clear();
					}					
				}		
			}
			
			for(Map.Entry<Integer,Integer> tEntry : lid_inc.entrySet()){
				int lid = tEntry.getKey();
				int inc = tEntry.getValue();
				//System.out.println(inc+"inc");
				if(linksLidMap.containsKey(lid)){
					for(Link[] l : segments){
						int f=0;
						for(int x=0;x<l.length;x++){
							if(l[x]!=null&&l[x].getLinkid()==lid){
								f=1;
								break;
							}
						}
						if(f==1){
							for(int x=0;x<l.length;x++){
								if(l[x]!=null){
									l[x].setStartTiming(l[x].getStartTiming()+inc);
									l[x].setEndTiming(l[x].getEndTiming()+inc);
								}
							}
						}
					}
					continue;
				}
				
				Link[] pastInitSeg = linkflows.get(lid); 
				//System.out.println("aaaaaaa"+pastInitSeg[1].isStart());
				int pastFinalLId = 0;
				//then we get the initial links,we need to get the final links to construct a segment
				for(Map.Entry<Link,Integer> lEntry : linksMap.entrySet()){
					Link link = lEntry.getKey();
					if(link.getFrom()==pastInitSeg[1].getFrom()&&link.getStartTiming()==pastInitSeg[1].getStartTiming()&&link.getFlowid()==pastInitSeg[1].getFlowid()&&link.isEnd()==true){
						pastFinalLId = link.getLinkid();
						break;
					}
				}
				//System.out.println("pafsa:"+pastFinalLId+" "+lid);
				Link[] pastFinalSeg = linkflows.get(pastFinalLId);
				Link[] pastSeg = new Link[1000];
				int tIndex = 1;
				//combine initial and final links to our segments
				for(int x=0;x<pastInitSeg.length;x++){
					if(pastInitSeg[x]!=null){
						pastSeg[tIndex] = pastInitSeg[x];
						tIndex++;
					}
				}
				for(int x=0;x<pastFinalSeg.length;x++){
					if(pastFinalSeg[x]!=null){
						pastSeg[tIndex] = pastFinalSeg[x];
						//System.out.println("wc "+pastSeg[tIndex].getFrom()+" "+pastSeg[tIndex].getTo()+" "+pastSeg[tIndex].getStartTiming()+" "+pastSeg[tIndex].getEndTiming()+" "+pastSeg[tIndex].getFlowid()+" "+pastSeg[tIndex].isStart());
						tIndex++;
					}
				}
				for(int x=0;x<pastSeg.length;x++){
					if(pastSeg[x]!=null){
						pastSeg[x].setStartTiming(pastSeg[x].getStartTiming()+inc);
						pastSeg[x].setEndTiming(pastSeg[x].getEndTiming()+inc);
					}
				}
				System.out.println("find past segments!!!!!!");
				segments.add(pastSeg);
			}
			
		}*/
		
	public static HashMap<Integer,Integer> getSegmentsLinkIdMap(HashSet<Link[]>segments) {
		HashMap<Integer,Integer> linksLidMap = new HashMap<Integer,Integer>();
		for(Link[] s : segments){
			for(int i=0;i<s.length;i++){
				if(s[i]!=null){
					if(linksLidMap.containsKey(s[i].getLinkid())){
						int et = linksLidMap.get(s[i].getLinkid());
						if(s[i].getEndTiming() > et ){
							linksLidMap.put(s[i].linkid,s[i].getEndTiming());
						}
					}else{
						linksLidMap.put(s[i].linkid,s[i].getEndTiming());
					}
				}
			}
		}
		return linksLidMap;
	}
		
	
	public static void findPastsegment(HashSet<Link[]>segments,HashMap<Link,Integer>linksMap, HashMap<Integer, Link[]> linkflows,double[][] bandWidth,double[] throughput,int endTime,HashMap<Integer,Integer> linksLidMap,HashMap<Link,Integer> oldLinksMap){
	/*	for(Map.Entry<Integer,Integer> aEntry : linksLidMap.entrySet()){
			System.out.println(aEntry.getKey()+" "+aEntry.getValue());
		}
		*/
		HashMap<Integer,Link[]> tmpSegMap = new HashMap<Integer, Link[]>();
		turnSet2Map(segments,tmpSegMap);
		HashMap<Integer,Integer> lid_inc = new HashMap<Integer,Integer>();
		for(int t=0;t<endTime-1;t++){
			double [][] load = new double[numOfSwitch+1][numOfSwitch+1];
			int[][] inc = new int[numOfSwitch+1][numOfSwitch+1];
			int[][] flag = new int[numOfSwitch+1][numOfSwitch+1];
			HashMap<int[], Integer> path_lid = new HashMap<int[],Integer>();
			for(Map.Entry<Link,Integer> lEntry : linksMap.entrySet()){
				Link link = lEntry.getKey();
				int[] path = new int[2];
				if(link.getStartTiming()==t&&link.getEndTiming()==t+1){
					int lid =link.getLinkid();
					Link[] tLF = linkflows.get(lid);
					if(tLF[1].getStartTiming()>0) continue;
					if(!linksLidMap.containsKey(lid)&&link.isStart()==false) continue;  //exclude the final links in the past
					if(linksLidMap.containsKey(lid) && t>=linksLidMap.get(lid)) continue; //exclude the links belong to segments at to while > endTiming of segment
					int from = link.getFrom();
					int to = link.getTo();
					int fid = link.getFlowid();
					double df = throughput[fid];
					load[from][to] +=df;			 //for get sum of df of all the flows at same link
					//System.out.println(from+" "+to+" "+load[from][to]+" "+t);
					path[0]=from;
					path[1]=to;
					path_lid.put(path,lid);
					if(!linksLidMap.containsKey(lid)) flag[from][to]=1;	

					if(!linksLidMap.containsKey(lid)&&tLF[1].getStartTiming()<inc[from][to]){
						//System.out.println(from+" "+to+" "+tLF[1].getStartTiming()+" ?? "+inc[from][to]+" "+t);
						inc[from][to] = tLF[1].getStartTiming();
						//System.out.println("??? "+inc[from][to]);
						//flag=1;
					}
					/*if(flag==1)
						System.out.println("load "+load[from][to]+" "+ from+" "+to+" "+t+" "+tLF[1].getFlowid()+" "+inc[from][to]);*/
					
					if(load[from][to] > bandWidth[from][to] && flag[from][to]==1){
						//System.out.println("find congested links!");
						for(Map.Entry<int[],Integer> pEntry : path_lid.entrySet()){
							int[] p = pEntry.getKey();
							if(p[0]==from&&p[1]==to){
								int lID = pEntry.getValue();
								//System.out.println("load> "+ from+" "+to+" "+t+" "+lID+" "+(0-inc[from][to]));
								if(lid_inc.containsKey(lID)&&lid_inc.get(lID)<(0-inc[from][to])){
									lid_inc.put(lID,0-inc[from][to]);
								}else{
									lid_inc.put(lID,0-inc[from][to]);
								}
							}
						}
					}
					
				}
			}
		}
			
			for(Map.Entry<Integer,Integer> tEntry : lid_inc.entrySet()){
				int lid = tEntry.getKey();
				int inc = tEntry.getValue();
				//System.out.println(inc+"inc");
				if(linksLidMap.containsKey(lid)){
					for(Link[] l : segments){
						int f=0;
						for(int x=0;x<l.length;x++){
							if(l[x]!=null&&l[x].getLinkid()==lid){
								f=1;
								break;
							}
						}
						if(f==1){
							for(int x=0;x<l.length;x++){
								if(l[x]!=null){
									l[x].setStartTiming(l[x].getStartTiming()+inc);
									l[x].setEndTiming(l[x].getEndTiming()+inc);
								}
							}
							break;
						}
					}
					continue;
				}
				
				Link[] pastInitSeg = linkflows.get(lid); 
				//System.out.println("aaaaaaa "+pastInitSeg[1].isStart());
				int pastFinalLId = 0;
				//then we get the initial links,we need to get the final links to construct a segment
				for(Map.Entry<Link,Integer> lEntry : oldLinksMap.entrySet()){
					Link link = lEntry.getKey();
					if(link.getFrom()==pastInitSeg[1].getFrom()&&link.getStartTiming()==pastInitSeg[1].getStartTiming()&&link.getFlowid()==pastInitSeg[1].getFlowid()&&link.isEnd()==true){
						pastFinalLId = link.getLinkid();
						break;
					}
				}
				//System.out.println("pafsa:"+pastFinalLId+" "+lid);
				Link[] pastFinalSeg = linkflows.get(pastFinalLId);
				Link[] pastSeg = new Link[1000];
				int tIndex = 1;
				//combine initial and final links to our segments
				for(int x=0;x<pastInitSeg.length;x++){
					if(pastInitSeg[x]!=null){
						pastSeg[tIndex] = pastInitSeg[x];
						tIndex++;
					}
				}
				for(int x=0;x<pastFinalSeg.length;x++){
					if(pastFinalSeg[x]!=null){
						pastSeg[tIndex] = pastFinalSeg[x];
						//System.out.println("wc "+pastSeg[tIndex].getFrom()+" "+pastSeg[tIndex].getTo()+" "+pastSeg[tIndex].getStartTiming()+" "+pastSeg[tIndex].getEndTiming()+" "+pastSeg[tIndex].getFlowid()+" "+pastSeg[tIndex].isStart());
						tIndex++;
					}
				}
				for(int x=0;x<pastSeg.length;x++){
					if(pastSeg[x]!=null){
						pastSeg[x].setStartTiming(pastSeg[x].getStartTiming()+inc);
						pastSeg[x].setEndTiming(pastSeg[x].getEndTiming()+inc);
					}
				}
				//System.out.println("find past segments!!!!!!");
				segments.add(pastSeg);
			}
			
		}
		
		/*	//HashMap<Link[],Integer> congestedSegments = new HashMap<Link[],Integer>();
			for(int i=0;i<load.length;i++){
				for(int j=0;j<load[i].length;j++){
					int flag1 = 0;
					int flag2 = 0;
					HashSet<Integer> tmpSeg = new HashSet<Integer>();
					if(load[i][j] > bandWidth[i][j]){      //congested links
						for(Map.Entry<Link,Integer> lEntry : linksMap.entrySet()){
							Link link = lEntry.getKey();
							if(link.getStartTiming()==t&&link.getEndTiming()==t+1&&link.getFrom()==i&&link.getTo()==j){  //find the links go through this link
								int lid = link.getLinkid();
								Link[] tLinlFlow = linkflows.get(lid);
								if(tLinlFlow[1].getStartTiming()>0) break;
								int flag = 0; //flag == 1, the link is belong to segment at t0; flag==2, the link is belong to past segment
								for(Link[] tseg : segments){
									int fx = 0;
									for(int x=0;x<tseg.length;x++){
										if(tseg[x]!=null&&tseg[x].getLinkid()==lid&&tseg[x].getFrom()==i&&tseg[x].getTo()==j){ //there should exist one link belong segments at t0 at least
											System.out.println("btst0 "+i+" "+j+" "+t+" "+lid);
											flag2 = 1;
											fx=1;
											flag = 1;
											break;
										}
									}
									if(fx==1){
										tmpSeg.add(lid);  //record all link id go through this link
										break;
									}
								}
								
								if(tLinlFlow[1].getStartTiming()<0&&tLinlFlow[1].isStart()==true&&flag==0){   //there should exist one link start before t0 at least
									flag1 = 1;
									tmpSeg.add(lid);
									System.out.println("past "+i+" "+j+" "+t+" "+lid);
								}
								
							}
						}
						
						if(flag1==1&&flag2==1){  
							int minStartT = 1000;
							for(int lid : tmpSeg){
								int f=1;
								for(Link[] seg: segments){   //except the link belong to segments at t0
									for(int a=0;a<seg.length;a++){
										if(seg[a]!=null&&seg[a].getLinkid()==lid){
											f=0;
											break;
										}
									}
									if(f==0) break;
								}
								if(f==1){
									Link[] pastInitSeg = linkflows.get(lid); 
									if(pastInitSeg[1].getStartTiming()<minStartT){
										minStartT = pastInitSeg[1].getStartTiming();
									}
									int pastFinalLId = 0;
									//then we get the initial links,we need to get the final links to construct a segment
									for(Map.Entry<Link,Integer> lEntry : linksMap.entrySet()){
										Link link = lEntry.getKey();
										if(link.getFrom()==pastInitSeg[1].getFrom()&&link.getStartTiming()==pastInitSeg[1].getStartTiming()&&link.getFlowid()==pastInitSeg[1].getFlowid()&&link.isEnd()==true){
											pastFinalLId = link.getLinkid();
										}
									}
									Link[] pastFinalSeg = linkflows.get(pastFinalLId);
									Link[] pastSeg = new Link[1000];
									int tIndex = 1;
									//combine initial and final links to our segments
									for(int x=0;x<pastInitSeg.length;x++){
										if(pastInitSeg[x]!=null){
											pastSeg[tIndex] = pastInitSeg[x];
											tIndex++;
										}
									}
									for(int x=0;x<pastFinalSeg.length;x++){
										if(pastFinalSeg[x]!=null){
											pastSeg[tIndex] = pastFinalSeg[x];
											//System.out.println("wc "+pastSeg[tIndex].getFrom()+" "+pastSeg[tIndex].getTo()+" "+pastSeg[tIndex].getStartTiming()+" "+pastSeg[tIndex].getEndTiming()+" "+pastSeg[tIndex].getFlowid()+" "+pastSeg[tIndex].isStart());
											tIndex++;
										}
									}
									//add the segments
									pastSegSet.add(pastSeg);
									int a=0;int b=0;
									for(int y=0;y<pastSeg.length;y++){
										if(pastSeg[y]!=null){
											if(pastSeg[y].isStart()==true) a=1;
											
											if(pastSeg[y].isEnd()==true) b=1;
										}
									}
									if(a+b!=2){
										System.out.println("misssss:"+pastSeg[1].getLinkid());
									}
									//segments.add(pastSeg);
								}
							}
							int increment = 0 - minStartT;
							segID_Incre.put(tmpSeg,increment);
							System.out.println("find congested links "+"from: "+i+" to: "+j+" at t"+t+"  num of links: "+tmpSeg.size()+" incre: "+ increment);
							for(int lID : tmpSeg){
								System.out.println("LID: "+lID);
							}
							
						}
					}
				}
			}
				
		}

		for(Link[] tl : pastSegSet){
			if(tl[1].getLinkid()==4717||tl[1].getLinkid()==4798){
				System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
				System.out.println(tl[1].getFrom()+" "+tl[1].getTo()+" "+tl[1].getStartTiming()+" "+tl[1].getEndTiming()+" "+tl[1].getFlowid()+" "+tl[1].isStart() );
			}
			segments.add(tl);
		}
		//adjust start timing for avoiding congestion
		for(Map.Entry<HashSet<Integer>, Integer> sEntry : segID_Incre.entrySet()){
			HashSet<Integer> tSeg = sEntry.getKey();
			int inc = sEntry.getValue();
			for(int lid : tSeg){
				//System.out.println(lid+" & "+inc);
				for(Link[] seg: segments){   //find segments that related to congestion
					for(int a=0;a<seg.length;a++){
						if(seg[a]!=null&&seg[a].getLinkid()==lid){
							for(int tt=0;tt<seg.length;tt++){
								if(seg[tt]!=null){
									//delay their start timing
									seg[tt].setStartTiming(seg[tt].getStartTiming()+inc);
									seg[tt].setEndTiming(seg[tt].getEndTiming()+inc);
								}
							}
							break;
						}
					}
				}
			}
		}*/
		
	//}
	
	
	public static void excludeRepeatedSegments(HashMap<Integer, Link[]>segmentsMap,HashMap<Integer,Integer> linksLidMap) {
		int[] remKey = new int[segmentsMap.size()*10];
		int rindex=0;
		HashMap<Integer,Integer> tmpRem = new HashMap<Integer, Integer>();
		HashMap<Integer,Integer> tmpEndT = new HashMap<Integer, Integer>();

		for(Map.Entry<Integer,Link[]> s1Entry : segmentsMap.entrySet()){
			int s1id = s1Entry.getKey();
			Link[] links1 = s1Entry.getValue();
			for(Map.Entry<Integer,Link[] > s2Entry : segmentsMap.entrySet()){
				int s2id = s2Entry.getKey();
				Link[] links2 = s2Entry.getValue();
				if(s2id == s1id ) continue;
				//System.out.println(s1id+"&"+s2id);
				if(isLinkEqual(s1Entry.getValue(), s2Entry.getValue(),3,linksLidMap)==1){
				//	System.out.println("exclu:"+s1id+" "+s2id);
					int min1=1000;
					int min2=1000;
					for(int a=0;a<links1.length;a++){
						if(links1[a]!=null&&links1[a].getStartTiming()<min1) min1 = links1[a].getStartTiming();
					}
					for(int a=0;a<links2.length;a++){
						if(links2[a]!=null&&links2[a].getStartTiming()<min1) min2 = links2[a].getStartTiming();
					}
					if(min1>min2){
					//	System.out.println("remmmm1:"+s1id);
						remKey[rindex] = s1id;
						rindex++;
					}else if(min1<min2){
					//	System.out.println("remmmm2:"+s2id);
						remKey[rindex] = s2id;
						rindex++;
					}
				}
			}
		}
		
		for(int i=0;i<remKey.length;i++){
			if(remKey[i]!=0){
				segmentsMap.remove(remKey[i]);
				//System.out.println("excluuuuu!");
			}
		}
		
	}
	
	public static void mergeSegments(HashMap<Integer, Link[]>segmentsMap,HashMap<Integer,Integer> linksLidMap) {
		
		int[] remKey = new int[segmentsMap.size()*10];
		int rindex=0;
		for(Map.Entry<Integer,Link[]> s1Entry : segmentsMap.entrySet()){
			int s1id = s1Entry.getKey();
			for(Map.Entry<Integer,Link[] > s2Entry : segmentsMap.entrySet()){
				int s2id = s2Entry.getKey();
				if(s2id == s1id ) continue;
				//System.out.println(s1id+"&"+s2id);
				if(isLinkEqual(s1Entry.getValue(), s2Entry.getValue(), 1,linksLidMap)==1){
				//	System.out.println("merge:"+s1id+" "+s2id);
					int rem = Math.max(s1id,s2id);
					remKey[rindex] = rem;
					rindex++;
				}
			}
		}
		
		for(int i=0;i<remKey.length;i++){
			if(remKey[i]!=0){
				segmentsMap.remove(remKey[i]);
			//	System.out.println("mergeeeeeee!");
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
	
	
	public static int isLinkEqual(Link[]l1, Link[]l2, int choose,HashMap<Integer,Integer> linksLidMap){
		//compare whether two links are same
		//judeg whether two links are  same
		// choose == 1  --> totally same (include timing)
		// choose == 2  --> exclude timing
		int flag = 1;
		int l1Num=0;int l2Num=0;
		int same = 0;
		for(int i=0;i<l1.length;i++){
			if(l1[i]==null) continue;
			l1Num++;
			l2Num=0;
			for(int j=0;j<l2.length;j++){
				if(l2[j]==null) continue;
				l2Num++;
				if(choose == 1){
					if(l1[i].getFrom()==l2[j].getFrom()&&l1[i].getTo()==l2[j].getTo()&&l1[i].getStartTiming()==l2[j].getStartTiming()&&l1[i].getEndTiming()==l2[j].getEndTiming()&&l1[i].getFlowid()==l2[j].getFlowid()&&l1[i].isStart()==l2[j].isStart()){
						if((linksLidMap.containsKey(l1[i].getLinkid())&&linksLidMap.containsKey(l2[j].getLinkid()))){
							break;
						}	
						same++;
					}
				}
				if(choose == 2){
					if(l1[i].getFrom()==l2[j].getFrom()&&l1[i].getTo()==l2[j].getTo()&&l1[i].getFlowid()==l2[j].getFlowid()&&l1[i].getStartTiming()!=l2[j].getStartTiming()&&l1[i].isStart()==l2[j].isStart()){
						if((linksLidMap.containsKey(l1[i].getLinkid())&&linksLidMap.containsKey(l2[j].getLinkid()))){
							break;
						}	
							same++;
					}
				}
				if(choose == 3){
					if(l1[i].getFrom()==l2[j].getFrom()&&l1[i].getTo()==l2[j].getTo()&&l1[i].getFlowid()==l2[j].getFlowid()&&l1[i].getStartTiming()!=l2[j].getStartTiming()&&l1[i].isStart()==l2[j].isStart()){
						//if((linksLidMap.containsKey(l1[i].getLinkid())&&linksLidMap.containsKey(l2[j].getLinkid()))||(!linksLidMap.containsKey(l1[i].getLinkid())&&!linksLidMap.containsKey(l2[j].getLinkid()))){
						if((linksLidMap.containsKey(l1[i].getLinkid())&&linksLidMap.containsKey(l2[j].getLinkid()))){
						same++;
						}
					}
				}
			}
		}
		
		if(same==l1Num&&same==l2Num){
		//	System.out.println("true"+same+" "+l1Num+" "+l2Num);
			flag = 1;
		}else{
		//	System.out.println("false:"+same+" "+l1Num+" "+l2Num);
			flag = 0;
		}
		return flag;
	}
	
	
	public static boolean checkFeasible(HashMap<Integer,Link[]> segmentsMap,HashMap<Integer,Integer> linksLidMap ){
		// check whether exist a makes O(t-a) = O(a),if exist then exit the program
		for(Map.Entry<Integer,Link[]> s1entry:segmentsMap.entrySet()){
			Link[] l1 = s1entry.getValue();
			int l1key = s1entry.getKey();
			for(Map.Entry<Integer, Link[]> s2entry:segmentsMap.entrySet()){
				if(s2entry.getKey() == l1key) continue;
				Link[]l2 = s2entry.getValue();
				if(isLinkEqual(l1, l2, 2,linksLidMap)==1){   // exist a makes O(t-a) = O(a), exit the program
					//System.out.println("exit!:"+s1entry.getKey()+" "+s2entry.getKey());
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
			if(path.getCapacity()>bandwidth[path.getU()][path.getV()]+0.5){
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
			// System.out.println("link:"+index+" from:"+link.getFrom()+" to:"+link.getTo()+" at:"+link.getStartTiming()+" to:"+link.getEndTiming()+" flow:"+link.getFlowid()+" isStart:"+link.isStart()+" linkid:"+link.getLinkid());
	        }  
		 
		 System.out.println("***************************");
		 
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
			//	System.out.println("segmentId: "+key+" linkid: "+l[i].getLinkid()+" from: "+l[i].from+" to: "+l[i].to+" startT: "+l[i].startTiming+" endT: "+l[i].endTiming+" flowid: "+l[i].flowid+" isStart: "+l[i].isStart()+" isEnd: "+l[i].isEnd());
				}
		}
			//System.out.println("#######");
		}
	}
	
	public static void printDependRelation(HashSet<HashMap<Integer,Integer>> dependRelationSet){  
		//print the dependency relation of segments
		for(HashMap<Integer,Integer> tmpDep:dependRelationSet){
			for(Map.Entry<Integer,Integer> depRelEntry : tmpDep.entrySet()){
				//System.out.println("sgement: "+depRelEntry.getKey()+" ---> segment: "+depRelEntry.getValue());
			}
		}
		
	}
	
	public static void printPaths(HashMap<Integer,Path> paths) {
		for(Map.Entry<Integer,Path> pEntry : paths.entrySet()){
			Path p = pEntry.getValue();
		//	System.out.println("pathId: "+pEntry.getKey()+" from: "+p.getU()+" to: "+p.getV()+" capa: "+p.getCapacity()+" isStart: "+p.isStart()+" isEnd: "+p.isEnd());
		}
	}
	
	public static void printDependecnyGraph(HashSet<PathSegmentRelation> dependencyG){
		for(PathSegmentRelation psr : dependencyG){
			if(psr.getRelation() == 0){
				//System.out.println("pid: "+psr.getPathId()+" ---> sid: "+psr.getSegmentId());
			}else{
				//System.out.println("sid: "+psr.getSegmentId()+" ---> pid: "+psr.getPathId());
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
				//System.out.println(pid);
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
	
	@SuppressWarnings("resource")
	public static void main(String args[]) throws IOException{
		int Tcase=0;
		while(Tcase<1) {
		long startTime=System.currentTimeMillis();//记录开始时间  
		
	//	int [][] flows = {{1,3,4,5},{1,2,5},{1,2,5},{1,3,2,4,5}}; //initial flow1 , final flow1, initial flow2 , final flow2 ....
		
		//int [][] flows = {{2,3},{2,4,3},{2,1},{2,3,1},{2,5},{2,4,5}};
		//int [][] flows = new int[1000][25];
		
		int [][]flows = new int[10000][];
		//String filePath = "input_flow_100_useful.txt";
		String filePath = "input_flow_1000.txt";
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
			 
			 //br.close();
		/*for(int i=1;i<=cnt/2;i++){
			throughput[i] = (double)getRandom(1, 20)*1.0/10;
			//System.out.println("throughput of flow"+i+" is:"+throughput[i]);
			//throughput[i] = 1;
		}
	     */
		Topology topology = new Topology();
		topology.init(100,6.3,1);
		//int [][] topo = topology.getTopo();
		//double [][] delay = topology.getDelay();
		double [][] bandWidth = topology.getBandWidth();
		int linkNumber = 10000000;
		HashMap<Link, Integer> linksMap = new HashMap<Link, Integer>();
		//Link [] links = new Link[linkNumber];  //for all links
		int time = 80;
		int index = 1;
		int flowid = 0;
		int linkid = 0;
		int startTmp = -80;  // startTmp ... t0 ... time
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
				// System.out.println("linkid:"+linkid);
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
					//links[index] = new Link();
					//links[index].setLink(from, to, startTiming, endTiming, flowid,linkid,start, end);	
					Link link = new Link();
					link.setLink(from, to, startTiming, endTiming, flowid, linkid, start, end);
					index++;
					linksMap.put(link,index);
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
	
		findsegmentsCandidate(tempLinks, linksMap, segmentCandi);
		System.out.println("1");
	/*	for(int i=0;i<segmentCandi.length;i++){
		if(segmentCandi[i]!=null)
		System.out.println(segmentCandi[i].getFrom());
		}*/

		HashSet<Link[]> segments = new HashSet<Link[]>();  //set of segment
		findsegmentAtT0(segments, linksMap, linkflows, segmentCandi);
		System.out.println("2");
		

		HashMap<Integer,Link[]> segmentsMap = new HashMap<Integer,Link[]>(); // segments map 
		turnSet2Map(segments, segmentsMap);
		printsegments(segmentsMap);
		System.out.println("----------");
		
		HashMap<Link,Integer> newLinksMap = new HashMap<Link,Integer>();
		newLinksMap = dividedLinks(linksMap,segments);
		
		/*HashMap<Link,Integer> newLinksMap = new HashMap<Link,Integer>();
		newLinksMap = dividedLinks(linksMap);*/
		//printLinks(newLinksMap);
		HashMap<Integer,Integer> linksLidMap = getSegmentsLinkIdMap(segments);
		
		findPastsegment(segments, newLinksMap, linkflows, bandWidth,throughput,time,linksLidMap,linksMap);
		//fff(segments, newLinksMap, linkflows, bandWidth,throughput,time);
		System.out.println("3");

		//HashMap<Integer,Link[]> segmentsMap = new HashMap<Integer,Link[]>(); // segments map 
		
		turnSet2Map(segments, segmentsMap);
		printsegments(segmentsMap);
		System.out.println("----------");
		//HashMap<Integer, Integer> dependRelation = new HashMap<Integer,Integer>();  // dependency relation map
		//HashSet<HashMap<Integer,Integer>> dependRelationSet = new HashSet<HashMap<Integer,Integer>>();
		//findDependencyRelation(dependRelationSet, segmentsMap, bandWidth,throughput);
		
		System.out.println("4");

		//System.out.println(dependRelationSet.size()+"!!!");
		
		//adjustUpdateTime(dependRelationSet,segmentsMap);
		System.out.println("5");

		excludeRepeatedSegments(segmentsMap, linksLidMap);
		//printsegments(segmentsMap);
		mergeSegments(segmentsMap,linksLidMap);
		System.out.println("6");

		printsegments(segmentsMap);	
		
		//printDependRelation(dependRelationSet);
		
		if(checkFeasible(segmentsMap,linksLidMap)==false){
			System.out.println("exist a makes O(t-a) = O(a),exits...");
			System.exit(0);
		}
		
		//HashMap<Integer, Path> paths = getAllPath(segmentsMap,flows, bandWidth,throughput);
		HashMap<Integer, Path> paths = getAllPath(flows, bandWidth,throughput,cnt);
		System.out.println("7");

		printPaths(paths);
		
		double maxUt = calLinkUtlization(paths, bandWidth);
		System.out.println("the max utiliztion of inital flow is : +"+maxUt);
		
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
				int minT = 1000;  //for getting the minimal start time of a segment
				for(int a =0;a<l.length;a++){
					if(l[a]!=null&&l[a].getStartTiming()<minT) minT = l[a].getStartTiming();
				}
				int sid = sEntry.getKey();
				int fid = l[1].getFlowid();
				double df = throughput[fid];
				//System.out.println("&&&&& "+t+" "+minT);
				if(minT == t){
					 f = IsIndependentSegments(dependencyGraph, sid, df, paths);
					 if(f==true){
							independentSegments[tmpCnt] = sid;
							tmpCnt++;
					//		System.out.println("the segment: "+sid+" is a independent segment at:t"+t);
						}else{
							dependentSegments[tmpCnt2] = sid;
							tmpCnt2++;
					//		System.out.println("the segment: "+sid+" is a dependent segment at:t"+t);
						}			 
				}		
			}
			
			int maxDelay = getDelayOfIndependentSegment(independentSegments, segmentsMap);
			//if(maxDelay>0)
			//System.out.println("maxDelay: "+maxDelay+" at t"+t);
			boolean continueFlag = true; 
			continueFlag = updateDependencyGraph(dependencyGraph, independentSegments, paths, segmentsMap, throughput,bandWidth);
			if(continueFlag == false){
				System.out.println("program ends cause the update of independent segments is failed although it's impossible! ");
				System.exit(0);
			}else if(independentSegments.length>1&&independentSegments[1]!=0){
				//	System.out.println("the segments after updating independent segments at t"+t);
					updateSegments(segmentsMap,independentSegments);
					printsegments(segmentsMap);
				//	System.out.println("the dependentGraph after updating independent segments at t"+t);
					printPaths(paths);
					maxUt = calLinkUtlization(paths, bandWidth);
				//	System.out.println("the max utiliztion at t"+t+" is: "+maxUt);
				}
			
		
			deferOtherSegments(independentSegments, segmentsMap, maxDelay);
			if(maxDelay>0)
			//System.out.println("defer");
			//printsegments(segmentsMap);
			
			//exclude the dependentSgements which are not at t
			for(int i=1;i<dependentSegments.length;i++){
				int minT=100;
				if(dependentSegments[i]!=0){
					Link[] l = segmentsMap.get(dependentSegments[i]);
					for(int a=0;a<l.length;a++){
						if(l[a]!=null&&l[a].getStartTiming()<minT) minT = l[a].getStartTiming();
					}
					if(minT>t){
						//System.out.println("dsada:"+dependentSegments[i]);
						dependentSegments[i]=0;
					}
				}
			}
			
			continueFlag = updateDependencyGraph(dependencyGraph, dependentSegments, paths, segmentsMap, throughput,bandWidth);
			if(continueFlag == false){
				//System.out.println("program ends cause the update of dependent segments is failed, so the solution doesn't exist!");
				System.exit(0);
			}else{
				if(dependentSegments.length>1&&dependentSegments[1]!=0){
					//System.out.println("the segments after updating dependent segments at t"+t);
					updateSegments(segmentsMap,dependentSegments);
					printsegments(segmentsMap);
				//	System.out.println("the dependentGraph after updating dependent segments at t"+t);
					printPaths(paths);
					maxUt = calLinkUtlization(paths, bandWidth);
				//	System.out.println("the max utiliztion at t"+t+" is: "+maxUt);
				}
			}
		}
		if(segmentsMap.size()==0){
			System.out.println("program has found a solution, congratulation :):):)");
		}
		
		long endTime=System.currentTimeMillis();//记录结束时间  
		float excTime=(float)(endTime-startTime)/1000;  
		System.out.println("执行时间："+excTime+"s");  

		String filepath = "chronicle_runningtime.txt";
		FileWriter fw1 = new FileWriter(filepath,true);
		fw1.write(excTime+"\n");
		fw1.flush();
		
		System.out.println("end");	
		Tcase++;
		}
	}    
}