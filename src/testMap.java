import java.util.HashMap;

public class testMap {

	public static void main(String args[]) {
		HashMap<Link, Integer>  links = new HashMap<Link,Integer>();
		Link l1 = new Link();
		l1.setLink(1, 2, -1, 0,1, 0, true, false);
		links.put(l1,111);
		Link l2 = new Link();
		l2.setLink(1, 2, -1, 0,2, 0, true, false);
		links.put(l2,222);
		
        System.out.println(links.toString());
        System.out.println(links.get(l1));
        System.out.println(links.get(l2));
        
    	Link l3 = new Link();
		l3.setLink(1, 2, -1, 0,2, 0, true, false);
        if(links.containsKey(l3)){
        	System.out.println("@!#@!");
        }

	}
}
