

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by luism on 17-04-15.
 */
public class RTree {
    /*
    Esta es la clase RTree que no usa reinsert, al momento de insertar un valor
     */
    private Nodo raiz;
    protected int t;
    protected MemoryManager mem;
    protected int m;
    protected int splitCounter;
    protected int visitCount;
    public RTree(int t) throws FileNotFoundException {
        this.raiz=new Nodo(t, mem.getNewPosition());
        this.t = t;
        this.m=(int)(t*0.4);
        mem = new MemoryManager(10, 4096);
        splitCounter = 0;
        visitCount = 0;

    }
    public ArrayList<Rectangulo> buscar(Rectangulo c) throws IOException {
        return buscar_aux(raiz, c);

    }

    private ArrayList<Rectangulo> buscar_aux(Nodo nodo, Rectangulo c) throws IOException {
        ArrayList <Rectangulo> result = new ArrayList<Rectangulo>();
        if (nodo.isLeaf()){
            ArrayList<Rectangulo> keys=nodo.getKeys();
            for (Rectangulo key : keys){
                if(key.intersect(c)){
                    result.add(key);
                }
            }
            return result;
        }
        else{
            ArrayList<Rectangulo> keys=nodo.getKeys();
            int i;
            for (i=0; i<keys.size(); i++){
                if(keys.get(i).intersect(c)){
                    Nodo child = mem.loadNode(nodo.getChildFilePosition(i));
                    long filePosNodo = nodo.getMyFilePosition();
                    mem.saveNode(nodo);
                    result.addAll(buscar_aux(child, c));
                    mem.loadNode(filePosNodo);
                }
            }
        }
        return result;
    }

    public void insertar_aux(Rectangulo c,long ref,Nodo nodo) throws IOException {
        Nodo parent;
        if (!nodo.isLeaf()){//no es hoja
            Rectangulo minMBR=null;
            Nodo hijo=mem.loadNode(0);
            if (!hijo.isLeaf()) {//si su hijo no es hoja se debe usar el incremento de area como criterio
                double minArea = 0;
                int index = 0;
                for (int i = 0; i < nodo.getnKeys(); i++) {
                    double newArea = nodo.getkey(i).extraArea(c);//recorro los MBR y veo el que tiene menor aumento de area
                    if(newArea < minArea){
                        minArea = newArea;
                        minMBR = nodo.getkey(i);
                        index = i;
                    }else if(newArea == minArea && nodo.getkey(i).area() < minMBR.area()){
                        minArea = newArea;
                        minMBR = nodo.getkey(i);
                        index = i;
                    }
                }
                insertar_aux(c, nodo.getMyFilePosition(), mem.loadNode(nodo.getChildFilePos(index)));
                parent = mem.loadNode(ref);
                //revisar si hay overflow
                if (nodo.isFull()){
                    if(nodo.equals(parent)){//si el nodo es la raiz
                        splitRoot();
                    }
                    else{
                        Nodo newNode = split(nodo);
                        parent.addRectangulo(newNode.getMyRectangulo(), newNode.getMyFilePosition());
                        mem.saveNode(newNode);
                        mem.saveNode(nodo);
                        mem.saveNode(parent);
                    }

                }
            }else {//Si los hijos son hojas, se debe utilizar el incremento de Overlap como criterio.
                double minOverlap = 0;
                int index = 0;
                for (int i = 0; i <nodo.getnKeys() ; i++) {
                    double overlap=nodo.calculateOverlap(c,nodo.getkey(i));
                    if(overlap < minOverlap){
                        minOverlap = overlap;
                        minMBR = nodo.getkey(i);
                        index = i;
                    }else if(overlap == minOverlap && nodo.getkey(i).area() < minMBR.area()){
                        minOverlap = overlap;
                        minMBR = nodo.getkey(i);
                        index = i;
                    }
                }
                insertar_aux(c, nodo.getMyFilePosition(), mem.loadNode(nodo.getChildFilePos(index)));
                parent = mem.loadNode(ref);
                //revisar si hay overflow
                if (nodo.isFull()){
                    if(nodo.equals(parent)){//si el nodo es la raiz
                        splitRoot();
                    }
                    else{
                        Nodo newNode = split(nodo);
                        parent.addRectangulo(newNode.getMyRectangulo(), newNode.getMyFilePosition());
                        mem.saveNode(newNode);
                        mem.saveNode(nodo);
                        mem.saveNode(parent);
                    }

                }
            }
        }else{
            //Estoy en una hoja, inserto y acomodo, luego reviso si hay overflow
            nodo.addRectangulo(c, -1);
            mem.saveNode(nodo);
            if(nodo.isFull()){
                parent = mem.loadNode(ref);
                if(nodo.equals(parent)){//soy la raiz, caso especial
                    splitRoot();
                }
                else{
                    Nodo newNode = split(nodo);
                    parent.addRectangulo(newNode.getMyRectangulo(), newNode.getMyFilePosition());
                    mem.saveNode(newNode);
                    mem.saveNode(nodo);
                    mem.saveNode(parent);
                    updateRoot();
                }
            }
        }
    }


    private void splitRoot() {

    }
    private void updateRoot(){

    }

    public Nodo split(Nodo nodo){
        ArrayList<Rectangulo> keys_ancho = nodo.getKeys();
        ArrayList<Rectangulo> keys_alto = nodo.getKeys();
        Collections.sort(keys_ancho,Rectangulo.compareAlto());
        Collections.sort(keys_alto,Rectangulo.compareAncho());
        int dimension=ChooseSplitAxis(nodo,keys_ancho, keys_alto);
        ArrayList<Rectangulo> keys;
        if (dimension==0){
            keys=keys_ancho;
        }else {
            keys=keys_alto;
        }
        int index=ChooseSplitIndex(nodo,keys);

        return nodo;
    }

    /**
     *
     * @param nodo, el nodo sobre el cual se hace el split
     * @param keys, el conjunto de llaves del nodo, ordenadas x alguna dimension
     * @return indice correspondiente a la division entre las 2 distribuciones
     */
    private int ChooseSplitIndex(Nodo nodo, ArrayList<Rectangulo> keys) {
        int splitDistribution = 2 * nodo.getT() - 2 * m + 2;
        ArrayList<Double> inter = new ArrayList<Double>();
        ArrayList<Double> areas = new ArrayList<Double>();
        for (int i = 0; i < splitDistribution; i++) {
            List<Rectangulo> part1 = keys.subList(0, m + i);//m-1+i+1, sublist no considera el ultimo
            List<Rectangulo> part2 = keys.subList(m + i, keys.size());
            Rectangulo mbr1 = nodo.generarMbr(part1);
            Rectangulo mbr2 = nodo.generarMbr(part2);
            inter.add(mbr1.areaInterseccion(mbr2));
            areas.add(mbr1.area() + mbr2.area());
        }
        double min = inter.get(0);
        int indice = 0;
        for (double d : inter) {
            if (d <= min) {
                if (areas.get(areas.indexOf(d)) <= areas.get(indice)) {
                    min = d;
                    indice = inter.indexOf(d);
                }
            }
        }
        return indice + m;
    }

    /**
     * Escoge la dimension sobre la cual hacer el corte
     * @param nodo, el nodo que se quiere dividir
     * @param keys_ancho
     * @param keys_alto
     * @return 1 si la dimension es alto, 0 si es ancho
     */
    private int ChooseSplitAxis(Nodo nodo, ArrayList<Rectangulo> keys_ancho, ArrayList<Rectangulo> keys_alto) {
        /*
        Se ordena por alto y ancho, luego x cadaarreglo, tomamos todas las permutaciones y calculamos su MBR, despues calculamos el permietro de cada uno
        calculo la suma y me quedo con la menor, repito el proceso para la otra dimension. finalmente me quedo con la menor
         */

        double sum_ancho=calculateDistributions(nodo,keys_ancho);
        double sum_alto=calculateDistributions(nodo,keys_alto);
        if (sum_alto>sum_ancho){
            return 1;
        }else{
            return 0;
        }
    }

    private double calculateDistributions(Nodo nodo,ArrayList<Rectangulo> keys) {
        int splitDistribution=2*nodo.getT()-2*m+2;
        ArrayList<Double> dist1=new ArrayList<Double>();
        ArrayList<Double> dist2=new ArrayList<Double>();
        for (int i = 0; i < splitDistribution; i++) {
            List<Rectangulo> part1 = keys.subList(0, m + i);//m-1+i+1, sublist no considera el ultimo
            List<Rectangulo> part2 = keys.subList(m + i, keys.size());
            Rectangulo mbr1=nodo.generarMbr(part1);
            Rectangulo mbr2=nodo.generarMbr(part2);
            double margen1=mbr1.perimetro();
            double margen2=mbr2.perimetro();
            dist1.add(margen1);
            dist2.add(margen2);
        }
        double sum1 = 0;
        for(Double d : dist1)
            sum1 += d;
        double sum2 = 0;
        for(Double d : dist2)
            sum2 += d;
        return  sum1+sum2;
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

    public void insertar(Rectangulo r) {
        try {
            insertar_aux(r,getRaiz().getMyFilePosition(),getRaiz());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Nodo getRaiz() {

        return raiz;
    }

    public void setRaiz(Nodo raiz) {

        this.raiz = raiz;
    }

}
