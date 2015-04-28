import java.io.FileNotFoundException;

/**
 * Created by luism on 28-04-15.
 */
public class Main {
    static public void main (String[]args) throws FileNotFoundException {
        RTree tree=new RTree(40);
        tree.insertaRectangulos(25);
    }
}
