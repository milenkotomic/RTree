import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luism on 28-04-15.
 */
public class Main {
    static public void main (String[]args) throws IOException {
        int[] nRect = new int[]{512, 4096, 32768, 262144, 524288, 1048576, 2097152};
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

            System.out.println("Inicio Busqueda");
            long accesosPorBusqueda = 0;
            int rectABuscar = r/10000;
            long tb1 = System.currentTimeMillis();
            for (int i=0; i<rectABuscar; i++) {
                if (i%100 == 0)
                    System.err.println("Buscando r="+i);
                long accesosAntes = tree.accessDisk;
                Rectangulo c = tree.generaRectangulo();
                ArrayList<Rectangulo> res = tree.buscar(c);
                long accesosDespues = tree.accessDisk;
                accesosPorBusqueda = ((accesosPorBusqueda * i) + (accesosDespues - accesosAntes)) / (i+1);
            }
            long tb2 = System.currentTimeMillis();
            long tb = (tb2-tb1) / 1000;
            System.out.println("Tiempo de busqueda: " + tb);
            System.out.println("Accesos a disco por busqueda: "+ accesosPorBusqueda);
            System.out.println("Fin\n");
            String nameFile = "rtree.bin";
            File f = new File(nameFile);
            if (f.exists())
                f.delete();
        }
    }
}
