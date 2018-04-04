import java.util.Random;

import org.omg.CORBA.ORBPackage.InconsistentTypeCode;

public class Topology {

	int n; //num of nodes
	int [][] topo;
	double [][] bandWidth;
	double [][] delay;
	
	public void init(int num,double bw, double d){
		this.n = num + 1;
		this.topo = new int [n][n];
		this.bandWidth = new double [n][n];
		this.delay = new double [n][n];
		//setTopo();
		setDelay(n,d);
		setBandWidth(n, bw);
	}
	public void setTopo(){
		this.topo[1][2] = 1;
		this.topo[1][3] = 1;
		this.topo[2][3] = 1;
		this.topo[2][4] = 1;
		this.topo[2][5] = 1;
		this.topo[3][4] = 1;
		this.topo[4][5] = 1;
	}
	
	public void setDelay(int num, double d){
		/*this.delay[1][2] = 1;
		this.delay[1][3] = 1;
		this.delay[2][3] = 1;
		this.delay[2][4] = 1;
		this.delay[2][5] = 1;
		this.delay[3][4] = 3;
		this.delay[4][5] = 1;
		this.delay[2][1] = 1;
		this.delay[3][1] = 1;
		this.delay[3][2] = 1;
		this.delay[4][2] = 1;
		this.delay[5][2] = 1;
		this.delay[4][3] = 1;
		this.delay[5][4] = 1;*/
		
		if(d == -1){
			for(int i=1;i<num;i++){
				for(int j=1;j<num;j++){
					this.delay[i][j] = (double)getRandom(1, 5);
				}
			}	
		}else{
			for(int i=1;i<num;i++){
				for(int j=1;j<num;j++){
					this.delay[i][j] = d;
				}
			}	
		}
		this.delay[3][4] = 3;
		this.delay[4][3] = 3;
	}
	
	public void setBandWidth(int num, double bw){
		/*this.bandWidth[1][2] = 1;
		this.bandWidth[1][3] = 1;
		this.bandWidth[2][3] = 1;
		this.bandWidth[2][4] = 2;
		this.bandWidth[2][5] = 2;
		this.bandWidth[3][4] = 1;
		this.bandWidth[4][5] = 1;
		this.bandWidth[2][1] = 1;
		this.bandWidth[3][1] = 1;
		this.bandWidth[3][2] = 1;
		this.bandWidth[4][2] = 2;
		this.bandWidth[5][2] = 2;
		this.bandWidth[4][3] = 1;
		this.bandWidth[5][4] = 1;*/
		if(bw == -1){
			for(int i=1;i<num;i++){
				for(int j=1;j<num;j++){
					this.bandWidth[i][j] = (double)getRandom(30,50);
				}
			}
		}else{
			for(int i=1;i<num;i++){
				for(int j=1;j<num;j++){
					this.bandWidth[i][j] = bw;
				}
			}
		}
		/*this.bandWidth[2][5] = 2;
		this.bandWidth[5][2] = 2;*/
		/*this.bandWidth[1][3] = 100;
		this.bandWidth[3][1] = 100;
		this.bandWidth[24][23] = 100;
		this.bandWidth[23][24] = 100;
		this.bandWidth[14][13] = 100;
		this.bandWidth[13][14] = 100;
		this.bandWidth[24][25] = 100;
		this.bandWidth[25][24] = 100;
		this.bandWidth[1][2] = 100;
		this.bandWidth[2][1] = 100;
		this.bandWidth[7][2] = 100;
		this.bandWidth[2][7] = 100;*/
		/*this.bandWidth[2][1]=50;
		this.bandWidth[1][2]=35;
		this.bandWidth[3][10] = 30;
		this.bandWidth[10][3] = 30;*/
	}
	
	public static int getRandom(int min,int max){
		Random random = new Random();
		int s = random.nextInt(max)%(max-min+1) + min;
		return s;
	}
	
	
	public int getN() {
		return n;
	}
	public int[][] getTopo() {
		return topo;
	}
	public double[][] getBandWidth() {
		return bandWidth;
	}
	public double[][] getDelay() {
		return delay;
	}
	
	
}
