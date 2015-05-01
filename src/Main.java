import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by luism on 28-04-15.
 */
public class Main {
    static public void main (String[]args) throws IOException {
        RTree tree=new RTree(50);
        tree.insertaRectangulos(800000);
        //System.out.println(tree.m);
        Nodo hijo1=tree.mem.loadNode(tree.getRaiz().getChildFilePosition(0));
        Nodo hijo2=tree.mem.loadNode(tree.getRaiz().getChildFilePosition(1));
        System.out.println(tree.m);
    }
}
