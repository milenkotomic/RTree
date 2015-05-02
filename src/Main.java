import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luism on 28-04-15.
 */
public class Main {
    static public void main (String[]args) throws IOException {
        int[] nRect = new int[]{512, 4096, 32768, 262144, 2097152};
        for (int r: nRect) {
            RTree tree = new RTree(50);
            long t1 = System.currentTimeMillis();
            tree.insertaRectangulos(r);
            long t2 = System.currentTimeMillis();
            long t = (t2 - t1) / 1000;
            System.out.println("Prueba con "+ r +" rectangulos");
            System.out.println("Tiempo: " + t);
            System.out.println("Accesos a disco: " + tree.accessDisk);
            System.out.println("Splits: " + tree.splitCounter);
            //System.out.println(tree.m);
            //Nodo hijo1=tree.mem.loadNode(tree.getRaiz().getChildFilePosition(0));
            //Nodo hijo2=tree.mem.loadNode(tree.getRaiz().getChildFilePosition(1));
            //System.out.println(tree.m);
            System.out.println("Inicio Busqueda");
            int accesosAntes = tree.accessDisk;
            int rectABuscar = r/10;
            long tb1 = System.currentTimeMillis();
            for (int i=0; i<rectABuscar; i++) {
                Rectangulo c = tree.generaRectangulo();
                ArrayList<Rectangulo> res = tree.buscar(c);
            }
            long tb2 = System.currentTimeMillis();
            long tb = (tb2-tb1) / 1000;
            int accesosDespues = tree.accessDisk;
            int accesosBusqueda = accesosDespues - accesosAntes;
            System.out.println("Tiempo de busqueda: " + tb);
            System.out.println("Accesos a disco durante busqueda: "+ accesosBusqueda);
            System.out.println("Fin\n");
        }
    }
}
