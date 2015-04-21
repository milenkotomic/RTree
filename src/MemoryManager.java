import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by milenkotomic on 19-04-15.
 */
public class MemoryManager {
    private final int blockSize = 4096;
    private final int numOfBuffers = 10;
    private RandomAccessFile file;
    private byte[] buffer;
    private HashMap<Long, Nodo> elements;
    private HashMap<Long, Boolean> bufWasModified;//indica si cada buffer a sido modificado desde que se leyo de disco
    private LinkedList<Long> priority;//indica que buffer va a ser sobreescrito
    private long position;
    private int numOfElements;

    public MemoryManager(int numOfBuffers, int bufferSize) throws FileNotFoundException {
        file = new RandomAccessFile("rtree.bin", "rw");
        buffer = new byte[bufferSize];
        priority = new LinkedList<Long>();
        elements = new HashMap<Long, Nodo>();
        bufWasModified = new HashMap<Long, Boolean>();
        numOfElements = 0;
        position = 0;
    }

    private void improvePriority(long elmt){
        priority.remove(elmt);
        priority.offerFirst(elmt);
    }

    /**
     * Carga y retorna un Node, revisa si la informacion esta en memoria principal
     * y de lo contrario lee el archivo en la posicion indicada y carga los
     * datos a un buffer en memoria principal
     * @throws IOException
     * */
    public Nodo loadNode(long filePos) throws IOException {
        if(elements.containsKey(filePos)){
            improvePriority(filePos);
            return elements.get(filePos);
        }
        if(numOfElements < numOfBuffers){
            readBlockFromFile(filePos, buffer);
            priority.addFirst(filePos);
            bufWasModified.put(filePos, false);
            numOfElements++;
            Nodo temp = new Nodo(buffer);
            elements.put(filePos, temp);
            return temp;
        }
        long exit = priority.pollLast();
        Nodo exitNode = elements.get(exit);
        if(bufWasModified.get(exit)){
            exitNode.writeToBuffer(buffer);
            writeBlockToFile(buffer, exit);
        }
        elements.remove(exit);
        bufWasModified.remove(exit);
        readBlockFromFile(filePos, buffer);
        Nodo temp = new Nodo(buffer);
        priority.addFirst(filePos);
        bufWasModified.put(filePos, true);
        elements.put(filePos, temp);
        return temp;
    }

    /**
     * Recibe un Node y lo escribe en el buffer, si no tengo espacio libre
     * escribo uno de los buffers a disco (el de peor prioridad) y
     * sobreescribo el buffer con mi Node
     * @param nodo Nodo que quiero guardar
     * @throws IOException
     * */
    public void saveNode(Nodo nodo) throws IOException{
        if(numOfElements < numOfBuffers){
            elements.put(nodo.getMyFilePosition(), nodo);
            bufWasModified.put(nodo.getMyFilePosition(), true);
            numOfElements++;
            improvePriority(nodo.getMyFilePosition());
            return;
        }
        if(elements.containsKey(nodo.getMyFilePosition())){
            elements.put(nodo.getMyFilePosition(), nodo);
            bufWasModified.put(nodo.getMyFilePosition(), true);
            improvePriority(nodo.getMyFilePosition());
        }
        else{
            long exit = priority.pollLast();
            Nodo temp = elements.get(exit);
            temp.writeToBuffer(buffer);
            writeBlockToFile(buffer, temp.getMyFilePosition());
            bufWasModified.remove(exit);
            elements.remove(exit);
            elements.put(nodo.getMyFilePosition(), nodo);
            priority.addFirst(nodo.getMyFilePosition());
            bufWasModified.put(nodo.getMyFilePosition(), true);
        }

    }

    /**
     * Escribe un bloque (buffer de bytes) en disco en la posicion indicada
     * @throws IOException
     * */
    public void writeBlockToFile(byte[] block, long pos) throws IOException{
        file.seek(pos);
        file.write(block);
    }

    /**
     * Lee un bloque de disco partiendo de la posicion pos y
     * lo escribe en el buffer
     * @param pos posicion del archivo para empezar a leer
     * @param buffer buffer donde escribir
     * @throws IOException
     * */
    public void readBlockFromFile(long pos, byte[] buffer) throws IOException{
        file.seek(pos);
        file.read(buffer);
    }

    public long getNewPosition(){
        long temp = position;
        position += buffer.length;
        return temp;
    }
}
