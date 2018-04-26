import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import javax.swing.InputMap;

public class matlabInput {
	public static int getRandom(int min,int max){
		Random random = new Random();
		int s = random.nextInt(max)%(max-min+1) + min;
		return s;
	}
	public static void main(String[] args) throws IOException {
		 FileWriter fw1 = new FileWriter("E:\\matlabInput.txt");
		  //fw1.write(pausedTime+" "+t+"\n");
		 // fw1.flush();
		 
		/* int max=64;
	     int min=62;
	     Random random = new Random();*/

	     //int s = random.nextInt(max)%(max-min+1) + min;
	        
	    int cnt=0;
		int [][] input = new int[101][101];
		for(int i=1;i<input.length;i++){
			input[i][i] = getRandom(63, 64);
			cnt+=input[i][i];
		}
		
		for(int x=1;x<=8;x=x+2){
			int t1 = getRandom(1, 100);
			int t2 = getRandom(1, 100);
			if(t1 == t2){
				t1 = getRandom(1, 100);
			}
			int diff = getRandom(15, 25);
			input[t1][t1] += diff;
			input[t2][t2] -= diff;
		}
		
		double acc = cnt*1.0/100;
		System.out.println("cnt:"+cnt+" accuracy: "+acc);
		
		for(int i=1;i<input.length;i++){
			int dia = input[i][i];
			int left = 100 - dia;
			//int num = getRandom(2, 3);
			int tmpLeft = left;
			System.out.println("i:"+i+" left:"+left);
			while(tmpLeft>0){
				int tmpIndex = getRandom(1,100);
				while(input[i][tmpIndex]!=0){
					tmpIndex = getRandom(1,100);
				}
				int x = getRandom(10, 30);
				if(tmpLeft - x <0){
					input[i][tmpIndex] = tmpLeft;
					break;
				}else{
					input[i][tmpIndex] = x;
					tmpLeft -= x;
				}
			}
		}
		
	

		for(int i=1;i<input.length;i++){
			int t=0;
			for(int j=1;j<input[i].length;j++){
				t+=input[i][j];
			}	
			if(t!=100){
				System.out.println("! i:"+i+" t:"+t);
				for(int k=1;k<input[i].length;k++){
					if(input[i][k]!=0){
						System.out.println("k:"+k+" value:"+input[i][k]);
					}
				}
			}
		}
		
		
		for(int i=1;i<input.length;i++){
			for(int j=1;j<input[i].length;j++){
				fw1.write(input[i][j]+" ");
				fw1.flush();
			}
			fw1.write("\n");
			fw1.flush();
		}
		fw1.close();
		System.out.println("hi");
	}
}
