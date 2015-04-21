import java.util.ArrayList;

/**
 * Created by luism on 17-04-15.
 */
public class RTree {
    /*
    Esta es la clase RTree que no usa reinsert, al momento de insertar un valor
     */
    private Nodo raiz;
    public RTree(Nodo raiz){

        this.raiz=raiz;
    }
    public boolean buscar(Rectangulo c){

        return buscar_aux(this.getRaiz(),c);
    }

    private boolean buscar_aux(Nodo nodo, Rectangulo c) {
        if (nodo.isLeaf()){
            ArrayList<Rectangulo> keys=nodo.getKeys();
            for (Rectangulo key : keys){
                if(key.equals(c)){
                    return true;
                }
            }
            return false;
        }else{
            ArrayList<Rectangulo> keys=nodo.getKeys();
            int i;
            for (i=0; i<keys.size(); i++){
                if(keys.get(i).equals(c)){
                    return true;
                }
                if(keys.get(i).contains(c)){
                    break;
                }
            }
            buscar_aux(nodo.getHijos().get(i),c);
        }
        return false;
    }

    public void insertar(Rectangulo c, int level, int m){
        Nodo nodo=ChooseSubTree(level);
        if(!nodo.isFull()){
            nodo.getKeys().add(c);
        }else{
            split(nodo, m);
        }

    }

    private Nodo ChooseSubTree(int level) {
        return null;
    }

    public void split(Nodo nodo, int m){
        int dimension=ChooseSplitAxis(nodo,m);
        int index=ChooseSplitIndex(dimension,nodo);
    }

    private int ChooseSplitIndex(int dimension, Nodo nodo) {

    }

    private ArrayList<Rectangulo> sortByAncho(ArrayList<Rectangulo> keys) {
        return null;
    }

    private ArrayList<Rectangulo> sortByAlto(ArrayList<Rectangulo> keys) {
        return keys;
    }

    private int ChooseSplitAxis(Nodo nodo, int m) {
        /*
        Se ordena por alto y ancho, luego x cadaarreglo, tomamos todas las permutaciones y calculamos su MBR, despues calculamos el permietro de cada uno
        calculo la suma y me quedo con la menor, repito el proceso para la otra dimension. finalmente me quedo con la menor
         */
        ArrayList<Rectangulo> keys = nodo.getKeys();
        ArrayList<Rectangulo> anchos=sortByAncho(keys);
        ArrayList<Rectangulo> altos= sortByAlto(keys);

        return 0;
    }

    /**
     * Genera un rectangulo con coordenadas al azar, pero cuya area este entre 1 y 100
     * @return un nuevo rectangulo, con area uniformente distriubida entre 1 y 100;
     */
    public Rectangulo generaRectangulo(){
        double x1=Math.random()*500000;
        double y1=Math.random()*500000;
        Punto p1=new Punto(x1,y1);
        double area=Math.random()*100+1;
        double x2=Math.random()*(x1+100);
        double y2=(area/(x2-x1))+y1;
        Punto p2=new Punto(x2,y2);
        return new Rectangulo(p1,p2);

    }

    /**
     * Crea un rectangulo al azar y lo inserta en el arbol
     * @param nRectangles, numero de rectangulos que queremos crear.
     */
    public void insertaRectangulos(int nRectangles){
        for (int i=0;i<nRectangles;i++){
            Rectangulo r=generaRectangulo();
            insertar(r);
        }
    }
    public Nodo getRaiz() {

        return raiz;
    }

    public void setRaiz(Nodo raiz) {

        this.raiz = raiz;
    }

}
