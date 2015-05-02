import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luism on 17-04-15.
 *
 * Nodo para usarse en el R-Tree guarda el maximo y minimo numero de elementos
 * de cada nodo, la dimension del espacio, su MBR propio y los de sus hijos
 * asi como su posicion en el archivo donde se guarda el R-Tree y las de sus hijos

 * Ejemplo del formato de un Node con maximo de hijos 4 al ser guardado en disco
 *
 * --------------------------------------------------------------------------------------------------------------------------
 * | t |n_child| myMBR |MyRefFile|MBR1|MBR2|MBR3|MBR4|RefFile1|RefFile2|RefFile3|RefFile4|
 * --------------------------------------------------------------------------------------------------------------------------
 * |4B |  4B   | 32 B  |   8B    |      4 * 32  B    |   8B   |   8B   |   8B   |    8B  |
 *
 */
public class Nodo {
    private int t;
    private int nKeys;
    private int nChildren;
    private ArrayList<Rectangulo> keys;
    private Rectangulo myRectangulo;
    private long myFilePosition;
    private long[] childrenFilePosition;
    private int IsLeaf;

    public int getIsLeaf() {
        return IsLeaf;
    }

    public void setIsLeaf(int isLeaf) {
        this.IsLeaf = isLeaf;
    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }
    public int getnKeys() {
        return nKeys;
    }

    public void setnKeys(int nKeys) {
        this.nKeys = nKeys;
    }

    public ArrayList<Rectangulo> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<Rectangulo> keys) {
        this.keys = keys;
        this.nKeys = keys.size();
    }

    public Rectangulo getMyRectangulo() {
        return myRectangulo;
    }

    public void setMyRectangulo(Rectangulo myRectangulo) {
        this.myRectangulo = myRectangulo;
    }

    public Long getMyFilePosition() {
        return myFilePosition;
    }

    public void setMyFilePosition(long myFilePosition) {
        this.myFilePosition = myFilePosition;
    }

    public long[] getChildrenFilePosition() {
        return childrenFilePosition;
    }

    public void setChildrenFilePosition(long[] childrenFilePosition) {
        this.childrenFilePosition = childrenFilePosition;
    }

    public int getnChildren() {
        return nChildren;
    }

    public void setnChildren(int nChildren) {
        this.nChildren = nChildren;
    }

    public boolean isFull(){

        return (keys.size()>2*getT());
    }
    public long getChildFilePos(int i){
        return childrenFilePosition[i];
    }

    public boolean isEmpty(){
        return (keys.size()<t);
    }

    public boolean isLeaf(){
        return this.IsLeaf==1;
        //return nChildren==0 || keys.get(0).isAPoint();
        //return childrenFilePosition.length == 0;
    }

    public Nodo(int t, long filePos){
        setT(t);
        setnKeys(0);
        setKeys(new ArrayList<Rectangulo>());
        setMyFilePosition(filePos);
        setChildrenFilePosition(new long[(2 * t) + 1]);
        nChildren = 0;
        setIsLeaf(0);
    }

    public Nodo(byte[] buffer){
        int ini=4;
        t = ByteBuffer.wrap(buffer, ini, 4).getInt();
        ini+=4;
        nChildren = ByteBuffer.wrap(buffer, ini, 4).getInt();
        ini+=4;
        nKeys = ByteBuffer.wrap(buffer, ini, 4).getInt();
        ini+=4;
        IsLeaf =ByteBuffer.wrap(buffer, ini, 4).getInt();
        ini+=4;
        myRectangulo = new Rectangulo(buffer, ini);
        ini+=32;
        myFilePosition = ByteBuffer.wrap(buffer, ini, 8).getLong();
        ini+=8;

        keys = new ArrayList<Rectangulo>();
        for (int i=0; i < nKeys; i++){
            keys.add(i, new Rectangulo(buffer, ini));
            ini+=32;
        }
        ini = ini + ((2*t)+1- nKeys)*32;

        childrenFilePosition = new long[(2*t)+1];
        for (int i=0; i < nKeys; i++){
            childrenFilePosition[i] = ByteBuffer.wrap(buffer, ini, 8).getLong();
            ini+=8;
        }
        ini = ini + ((2*t)+1- nKeys)*8;
    }


    public Rectangulo getkey(int i){

        return keys.get(i);
    }

    public long getChildFilePosition(int i){
        return childrenFilePosition[i];
    }

    public void addRectangulo(Rectangulo r, long newFilePosition){
        childrenFilePosition[nKeys] = newFilePosition;
        expandRectangulo(r);
    }

    public void expandRectangulo(Rectangulo r){
        if (this.isEmpty()){
            myRectangulo = r;
        }
        else{
            myRectangulo = new Rectangulo(myRectangulo, r);
        }
        keys.add(nKeys++, r);
    }

    public void writeToBuffer(byte [] buffer) throws IOException {
        int ini= 0;
        ByteBuffer.wrap(buffer, ini, 4).putInt(0);
        ini+=4;
        ByteBuffer.wrap(buffer, ini, 4).putInt(t);
        ini= ini+4;
        ByteBuffer.wrap(buffer, ini, 4).putInt(nChildren);
        ini= ini+4;
        ByteBuffer.wrap(buffer, ini, 4).putInt(nKeys);
        ini= ini+4;
        ByteBuffer.wrap(buffer, ini, 4).putInt(IsLeaf);
        ini= ini+4;
        myRectangulo.writeToBuffer(buffer, ini);
        ini += 32;
        ByteBuffer.wrap(buffer, ini, 8).putLong(myFilePosition);
        ini= ini + 8;

        for (int i=0; i< nKeys; i++){
            keys.get(i).writeToBuffer(buffer, ini);
            ini = ini + 32;
        }
        ini = ini + ((2*t)+1- nKeys)*32;
        for (int i=0; i< nKeys; i++){
            ByteBuffer.wrap(buffer, ini, 8).putLong(childrenFilePosition[i]);
            ini= ini+8;
        }
        ini = ini + ((2*t)+1- nKeys)*8;

    }

    public boolean contains(Rectangulo r){
        for (Rectangulo hijo : keys){
            if (hijo.contains(r))
                return true;
        }
        return false;
    }

    public void clear() throws Exception{
        childrenFilePosition = new long[(2*t)+1];
        keys = new ArrayList<Rectangulo>();
        myRectangulo = null;
        nKeys = 0;
        nChildren = 0;
    }

    public boolean equals(Object o){
        if(!(o instanceof Nodo)){
            return false;
        }
        Nodo n = (Nodo) o;
        boolean isEqual =
                this.t == n.t &&
                        this.nKeys == n.nKeys &&
                        this.myRectangulo.equals(n.myRectangulo) &&
                        this.myFilePosition == n.myFilePosition &&
                        this.nChildren == n.nChildren;
        for (int i = 0; isEqual && i < nKeys; i++) {
            isEqual = isEqual && this.keys.get(i).equals(n.keys.get(i));
        }
        for (int i = 0; isEqual && i < nKeys; i++) {
            isEqual = isEqual && this.childrenFilePosition[i] == n.childrenFilePosition[i];
        }
        return isEqual;
    }

    public Rectangulo generarMbr(List<Rectangulo> part1) {
        Rectangulo mbr=part1.get(0);
        for(Rectangulo r : part1){
            mbr=new Rectangulo(mbr,r);
        }
        return mbr;
    }

    /**
     * Calcula la diferencia de overlap, al momento de hacer una insercion
     * @param r, el MBR sobre el cual se esta calculando el overlap
     * @param r2, el rectangulo que se inserta.
     * @return la diferencia de overlap
     */
    public double calculateOverlap(Rectangulo r, Rectangulo r2){
        double overlap_init=0;
        double overlap_final=0;
        for (Rectangulo rect : keys) {
            if (r.equals(rect))
                continue;
            overlap_init=+r.areaInterseccion(rect);
        }
        ArrayList<Rectangulo> copy=new ArrayList<Rectangulo>();
        for (int i = 0; i < keys.size(); i++) {
            copy.add(keys.get(i));
        }
        copy.add(r2);
        for(Rectangulo rect : copy){
            if (r.equals(rect))
                continue;
            overlap_final=+r.areaInterseccion(rect);
        }
        return  overlap_final-overlap_init;
    }
}
