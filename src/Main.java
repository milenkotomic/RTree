import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by luism on 28-04-15.
 */
public class Main {
    static public void main (String[]args) throws IOException {
        RTree tree=new RTree(10);
        long t1 = System.currentTimeMillis();
        tree.insertaRectangulos(100000);
        long t2 = System.currentTimeMillis();
        long t = (t2 - t1)/1000;
        System.out.println("Tiempo: " + t);
        //System.out.println(tree.m);
        //Nodo hijo1=tree.mem.loadNode(tree.getRaiz().getChildFilePosition(0));
        //Nodo hijo2=tree.mem.loadNode(tree.getRaiz().getChildFilePosition(1));
        //System.out.println(tree.m);
        Rectangulo r = tree.generaRectangulo();
        ArrayList<Rectangulo> res = tree.buscar(r);
        System.out.println("Fin");
    }
}
