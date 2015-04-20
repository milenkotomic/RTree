import java.io.FileNotFoundException;
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


}
