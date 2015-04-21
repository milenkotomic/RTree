import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

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
    private int nChildren;
    private ArrayList<Rectangulo> keys;
    private Rectangulo myRectangulo;
    private long myFilePosition;
    private long[] childrenFilePosition;

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }

    public int getnChildren() {
        return nChildren;
    }

    public void setnChildren(int nChildren) {
        this.nChildren = nChildren;
    }

    public ArrayList<Rectangulo> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<Rectangulo> keys) {
        this.keys = keys;
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

    public boolean isFull(){
        return (keys.size()>2*getT());
    }

    public boolean isEmpty(){
        return (keys.size()<t);
    }

    public boolean isLeaf(){

        return childrenFilePosition.length == 0;
    }

    public Nodo(int t, long filePos){
        setT(t);
        setnChildren(0);
        setKeys(new ArrayList<Rectangulo>());
        setMyFilePosition(filePos);
        setChildrenFilePosition(new long[(2*t)+1]);
    }

    public Nodo(byte[] buffer){
        int ini=0;
        t = ByteBuffer.wrap(buffer, ini, 4).getInt();
        ini+=4;
        nChildren = ByteBuffer.wrap(buffer, ini, 4).getInt();
        ini+=4;
        myRectangulo = new Rectangulo(buffer, ini);
        ini+=32;
        myFilePosition = ByteBuffer.wrap(buffer, ini, 8).getLong();
        ini+=8;

        keys = new ArrayList<Rectangulo>();
        for (int i=0; i < nChildren; i++){
            keys.add(i, new Rectangulo(buffer, ini));
            ini+=32;
        }
        ini = ini + ((2*t)+1-nChildren)*32;

        childrenFilePosition = new long[(2*t)+1];
        for (int i=0; i < nChildren; i++){
            childrenFilePosition[i] = ByteBuffer.wrap(buffer, ini, 8).getLong();
            ini+=8;
        }
        ini = ini + ((2*t)+1-nChildren)*8;
    }

    public Rectangulo getKeys(int i){
        return keys.get(i);
    }

    public long getChildFilePosition(int i){
        return childrenFilePosition[i];
    }

    public void addRectangulo(Rectangulo r, long newFilePosition){
        childrenFilePosition[nChildren] = newFilePosition;
        expandRectangulo(r);
    }

    public void expandRectangulo(Rectangulo r){
        if (this.isEmpty()){
            myRectangulo = r;
        }
        else{
            myRectangulo = new Rectangulo(myRectangulo, r);
        }
        keys.add(nChildren++, r);
    }

    public void writeToBuffer(byte [] buffer) throws IOException {
        int ini= 0;
        ByteBuffer.wrap(buffer, ini, 4).putInt(t);
        ini= ini+4;
        ByteBuffer.wrap(buffer, ini, 4).putInt(nChildren);
        ini= ini+4;
        myRectangulo.writeToBuffer(buffer, ini);
        ini += 32;
        ByteBuffer.wrap(buffer, ini, 8).putLong(myFilePosition);
        ini= ini + 8;
        for (int i=0; i< nChildren; i++){
            keys.get(i).writeToBuffer(buffer, ini);
            ini = ini + 32;
        }
        ini = ini + ((2*t)+1-nChildren)*32;
        for (int i=0; i<nChildren; i++){
            ByteBuffer.wrap(buffer, ini, 8).putLong(childrenFilePosition[i]);
            ini= ini+8;
        }        ini = ini + ((2*t)+1-nChildren)*8;

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
        nChildren = 0;
    }

    public boolean equals(Object o){
        if(!(o instanceof Nodo)){
            return false;
        }
        Nodo n = (Nodo) o;
        boolean isEqual =
                this.t == n.t &&
                        this.nChildren == n.nChildren &&
                        this.myRectangulo.equals(n.myRectangulo) &&
                        this.myFilePosition == n.myFilePosition;
        for (int i = 0; isEqual && i < nChildren; i++) {
            isEqual = isEqual && this.keys.get(i).equals(n.keys.get(i));
        }
        for (int i = 0; isEqual && i < nChildren; i++) {
            isEqual = isEqual && this.childrenFilePosition[i] == n.childrenFilePosition[i];
        }
        return isEqual;
    }
}
