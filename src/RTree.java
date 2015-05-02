

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

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
    protected int accessDisk;

    public RTree(int t) throws FileNotFoundException {
        mem = new MemoryManager(10, 4096);
        this.raiz = new Nodo(t, mem.getNewPosition());
        raiz.setIsLeaf(1);
        this.t = t;
        this.m=(int)(t*0.4);
        splitCounter = 0;
        visitCount = 0;
        accessDisk =0;

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
                    accessDisk++;
                    result.addAll(buscar_aux(child, c));
                    mem.loadNode(filePosNodo);
                    accessDisk++;
                }
            }
        }
        return result;
    }

    public void insertar_aux(Rectangulo c, long ref, Nodo nodo) throws IOException {
        Nodo parent;
        if (!nodo.isLeaf()){//no es hoja
            Rectangulo minMBR=new Rectangulo(new Punto(0,0),new Punto(0,0));
            Nodo hijo=mem.loadNode(nodo.getChildFilePos(0));
            accessDisk++;
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
                nodo = mem.loadNode(nodo.getMyFilePosition());
                //accessDisk++;
                parent = mem.loadNode(ref);
                accessDisk++;
                //revisar si hay overflow
                if (nodo.isFull()){
                    if(nodo.equals(parent)){//si el nodo es la raiz
                        splitRoot();
                        splitCounter++;
                    }
                    else{
                        Nodo newNode = split(nodo);
                        splitCounter++;
                        parent.addRectangulo(newNode.getMyRectangulo(), newNode.getMyFilePosition());
                        mem.saveNode(newNode);
                        accessDisk++;
                        mem.saveNode(nodo);
                        accessDisk++;
                        mem.saveNode(parent);
                        accessDisk++;
                    }
                    parent.setnChildren(parent.getnChildren() + 1);

                }
            }else {//Si los hijos son hojas, se debe utilizar el incremento de Overlap como criterio.
                double minOverlap = Double.MAX_VALUE;
                int index = 0;
                double incrementoArea=0;
                for (int i = 0; i <nodo.getnKeys() ; i++) {
                    double overlap=nodo.calculateOverlap(c,nodo.getkey(i));
                    //System.out.println(c);
                    if(overlap < minOverlap){
                        minOverlap = overlap;
                        minMBR = nodo.getkey(i);
                        incrementoArea=minMBR.extraArea(c);
                        index = i;
                    }else if (overlap==minOverlap && incrementoArea>minMBR.extraArea(c)){
                        minOverlap = overlap;
                        minMBR = nodo.getkey(i);
                        index = i;
                        incrementoArea=minMBR.extraArea(c);
                    }else if(overlap == minOverlap && incrementoArea==minMBR.extraArea(c) && nodo.getkey(i).area() < minMBR.area()){
                        minOverlap = overlap;
                        minMBR = nodo.getkey(i);
                        index = i;
                    }
                }
                insertar_aux(c, nodo.getMyFilePosition(), mem.loadNode(nodo.getChildFilePos(index)));
                nodo = mem.loadNode(nodo.getMyFilePosition());
                accessDisk++;
                parent = mem.loadNode(ref);
                accessDisk++;
                //revisar si hay overflow
                if (nodo.isFull()){
                    if(nodo.equals(parent)){//si el nodo es la raiz
                        splitRoot();
                        splitCounter++;
                    }
                    else{
                        Nodo newNode = split(nodo);
                        splitCounter++;
                        parent.addRectangulo(newNode.getMyRectangulo(), newNode.getMyFilePosition());
                        mem.saveNode(newNode);
                        accessDisk++;
                        mem.saveNode(nodo);
                        accessDisk++;
                        mem.saveNode(parent);
                        accessDisk++;
                    }
                    parent.setnChildren(parent.getnChildren() + 1);

                }
            }
        }else{
            //Estoy en una hoja, inserto y acomodo, luego reviso si hay overflow
            nodo.addRectangulo(c, -1);
            //accessDisk++;
            mem.saveNode(nodo);
            accessDisk++;
            //System.out.println("SOY HOJA " + c);
            if(nodo.isFull()){
                //System.out.println("Nodo lleno");
                parent = mem.loadNode(ref);
                accessDisk++;
                if(nodo.equals(parent)){//soy la raiz, caso especial
                    splitRoot();
                    splitCounter++;
                    getRaiz().setnChildren(2);
                }
                else{
                    Nodo newNode = split(nodo);
                    splitCounter++;
                    //System.out.println("nChild "+newNode.getnChildren());
                    parent.addRectangulo(newNode.getMyRectangulo(), newNode.getMyFilePosition());
                    mem.saveNode(newNode);
                    accessDisk++;
                    mem.saveNode(nodo);
                    accessDisk++;
                    mem.saveNode(parent);
                    accessDisk++;
                    updateRoot();
                    parent.setnChildren(parent.getnChildren() + 1);
                }

            }
        }
    }


    private void splitRoot() {
        Nodo newNode = split(getRaiz());
        if (getRaiz().isLeaf()){
            newNode.setIsLeaf(1);
        }
        Nodo newRoot = new Nodo(t, mem.getNewPosition());
        //accessDisk++;
        newRoot.addRectangulo(getRaiz().getMyRectangulo(), getRaiz().getMyFilePosition());
        newRoot.addRectangulo(newNode.getMyRectangulo(), newNode.getMyFilePosition());

        try {
            mem.saveNode(newNode);
            accessDisk++;
            mem.saveNode(getRaiz());
            accessDisk++;
            mem.saveNode(newRoot);
            accessDisk++;
        } catch (IOException e) {
            e.printStackTrace();
        }
        setRaiz(newRoot);
    }
    private void updateRoot(){
        try {
            setRaiz(mem.loadNode(getRaiz().getMyFilePosition()));
            accessDisk++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Nodo split(Nodo nodo) {
        ArrayList<Rectangulo> keys = nodo.getKeys();
        long[] filePositions = nodo.getChildrenFilePosition();
        ArrayList<Rectangulo> axis_keys;
        HashMap<Rectangulo, Long> RF = new HashMap<Rectangulo, Long>();
        for (int i = 0; i < keys.size(); i++) {
            RF.put(keys.get(i), filePositions[i]);
        }
        Collections.sort(keys, new CompareX1());
        double sX1 = calculateDistributions(nodo, keys);
        double min = sX1;
        axis_keys = keys;
        Collections.sort(keys, new CompareX2());
        double sX2 = calculateDistributions(nodo, keys);
        if (sX2 < min) {
            axis_keys = keys;
            min = sX2;
        }
        Collections.sort(keys, new CompareY1());
        double sY1 = calculateDistributions(nodo, keys);
        if (sY1 < min) {
            axis_keys = keys;
            min = sY1;
        }
        Collections.sort(keys, new CompareY2());
        double sY2 = calculateDistributions(nodo, keys);
        if (sY2 < min) {
            axis_keys = keys;
            min = sY2;
        }
        long[] newFilePos = new long[RF.size()];
        for (int i = 0; i < RF.size(); i++) {
            Rectangulo key = keys.get(i);
            long filepos = RF.get(key);
            newFilePos[i] = filepos;
        }
        nodo.setChildrenFilePosition(newFilePos);
        int index = ChooseSplitIndex(nodo, axis_keys);
        //falta generar el nuevo nodo y retornarlo
        Nodo newnodo = new Nodo(t, mem.getNewPosition());
        accessDisk++;
        ArrayList<Rectangulo> copy_keys = nodo.getKeys();
        long[] child_copy = nodo.getChildrenFilePosition();

        ArrayList<Rectangulo> part1 = new ArrayList<Rectangulo>();
        part1.addAll(copy_keys.subList(0, index + 1));


        ArrayList<Rectangulo> part2 = new ArrayList<Rectangulo>();
        part2.addAll(copy_keys.subList(index + 1, copy_keys.size()));

        long[] childPart1 = Arrays.copyOfRange(child_copy, 0, index + 1);
        long[] childPart2 = Arrays.copyOfRange(child_copy, index + 1, child_copy.length);
        for (int i = 0; i < part2.size(); i++) {
            newnodo.addRectangulo(part2.get(i), childPart2[i]);
        }
        if (nodo.isLeaf()){
            newnodo.setIsLeaf(1);
        }
        try {
            nodo.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i=0; i < part1.size(); i++){
            nodo.addRectangulo(part1.get(i), childPart1[i]);
        }
        //nodo.setKeys(part1);
        //nodo.setChildrenFilePosition(childPart1);
        splitCounter++;

        return newnodo;
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
                if (areas.get(inter.indexOf(d)) <= areas.get(indice)) {
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
        //System.out.println("SPLIT DIST!! "+splitDistribution);
        //System.out.println("KEYS:SIZE "+keys.size());
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
        double r1 = Math.random();
        double r2 = Math.random();
        double x1=r1*500000;
        double y1=r2*500000;
        Punto p1=new Punto(x1,y1);
        double area=Math.random()*100+1;
        double x2=x1+(100*r1);
        double y2=(area/(x2-x1))+y1;
        if (y2 > 500000)
            y2 = 500000;
        Punto p2=new Punto(x2,y2);
        return new Rectangulo(p1,p2);

    }

    /**
     * Crea un rectangulo al azar y lo inserta en el arbol
     * @param nRectangles, numero de rectangulos que queremos crear.
     */
    public void insertaRectangulos(int nRectangles){
        for (int i=0;i<nRectangles;i++){
            if (i%10000 == 0)
                System.out.println("i="+i);
            Rectangulo r=generaRectangulo();
            insertar(r);

        }
    }

    public void insertar(Rectangulo r) {
        try {
            insertar_aux(r, getRaiz().getMyFilePosition(), getRaiz());
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

    public int getSplitCounter() {
        return splitCounter;
    }

    public void setSplitCounter(int splitCounter) {
        this.splitCounter = splitCounter;
    }

    private class CompareX1 implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            Rectangulo r1 = (Rectangulo) o1;
            Rectangulo r2 = (Rectangulo) o2;
            if (r1.p1.getX() > r2.p1.getX())
                return 1;
            else if (r1.p1.getX() == r2.p1.getX())
                return 0;
            else
                return -1;
        }
    }

    private class CompareX2 implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            Rectangulo r1 = (Rectangulo) o1;
            Rectangulo r2 = (Rectangulo) o2;
            if (r1.p2.getX() > r2.p2.getX())
                return 1;
            else if (r1.p2.getX() == r2.p2.getX())
                return 0;
            else
                return -1;
        }
    }

    private class CompareY1 implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            Rectangulo r1 = (Rectangulo) o1;
            Rectangulo r2 = (Rectangulo) o2;
            if (r1.p1.getY() > r2.p1.getY())
                return 1;
            else if (r1.p1.getY() == r2.p1.getY())
                return 0;
            else
                return -1;
        }
    }

    private class CompareY2 implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            Rectangulo r1 = (Rectangulo) o1;
            Rectangulo r2 = (Rectangulo) o2;
            if (r1.p2.getY() > r2.p2.getY())
                return 1;
            else if (r1.p2.getY() == r2.p2.getY())
                return 0;
            else
                return -1;
        }
    }


}