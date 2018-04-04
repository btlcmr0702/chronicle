
public class rulesScript {

		public static void main(String args[]) {
			String IP = "192.168.1.66";                                                                                                              
			int bid = 24;
			int outport = 3 ;
			int udpsrc = 3000;
			int udpdst = 4000;
			int vid = 202;
			for(int i=0;i<50;i++){
				int srcport = udpsrc+i;
				int dstport = udpdst+i;
				System.out.println("sudo dpctl tcp:"+IP+":6635 flow-mod -b "+bid+" table=0,cmd=add, eth_type=0x800,ip_proto=17,udp_src="+srcport+",udp_dst="+dstport+", apply:output="+outport);
				//System.out.println("sudo dpctl tcp:"+IP+":6635 flow-mod -b "+bid+" table=0,cmd=add, eth_type=0x800,ip_proto=17,udp_src="+srcport+",udp_dst="+dstport+", apply:push_vlan=0x8100,set_field=vlan_vid:202,output="+outport);
				//System.out.println("sudo dpctl tcp:"+IP+":6635 flow-mod -b "+bid+" table=0,cmd=add, eth_type=0x800,ip_proto=17,udp_src="+srcport+",udp_dst="+dstport+",vlan_vid="+vid+", apply:output="+outport);
				//System.out.println("sudo dpctl tcp:"+IP+":6635 flow-mod -b "+bid+" table=0,cmd=add, eth_type=0x800,ip_proto=17,udp_src="+srcport+",udp_dst="+dstport+",vlan_vid="+vid+", apply:pop_vlan,output="+outport);

			}
			
			
			
		}
}
