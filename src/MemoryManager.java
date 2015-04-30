import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedList;


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
	
	public MemoryManager(int numOfBuffers, int bufferSize) throws FileNotFoundException{
		file = new RandomAccessFile("archivo.bin", "rw");
		buffer = new byte[bufferSize];
		priority = new LinkedList<Long>();
		elements = new HashMap<Long, Nodo>();
		bufWasModified = new HashMap<Long, Boolean>();
		numOfElements = 0;
		position = 0;
	}
	
	/*-----------------------------------Administrador de memoria---------------------------*/

	/**
	 * Mejora la prioridad del elemento elmt
	 * */
	private void improvePriority(long elmt){
		priority.remove(elmt);
		priority.offerFirst(elmt);
	}
	/**
	 * Carga y retorna un Nodo, revisa si la informacion esta en memoria principal
	 * y de lo contrario lee el archivo en la posicion indicada y carga los
	 * datos a un buffer en memoria principal
	 * @throws IOException 
	 * */
	public Nodo loadNode(long filePos) throws IOException{
		if(elements.containsKey(filePos)){
			improvePriority(filePos);
			return elements.get(filePos);
		}
		if(numOfElements < numOfBuffers){
			//nunca deberia pasar, igual lo pongo porsiacaso
			readBlockFromFile(filePos, buffer);
			priority.addFirst(filePos);
			bufWasModified.put(filePos, false);
			numOfElements++;
			Nodo temp = new Nodo(buffer);
			elements.put(filePos, temp);
			return temp;
		}
		long exit = priority.pollLast();
		Nodo exitNodo = elements.get(exit);
		if(bufWasModified.get(exit)){
			exitNodo.writeToBuffer(buffer);
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
	 * Recibe un Nodo y lo escribe en el buffer, si no tengo espacio libre
	 * escribo uno de los buffers a disco (el de peor prioridad) y 
	 * sobreescribo el buffer con mi Nodo
	 * @param Nodo Nodon que quiero guardar
	 * @throws IOException 
	 * */
	public void saveNode(Nodo Nodo) throws IOException{
		if(numOfElements < numOfBuffers){
			elements.put(Nodo.getMyFilePosition(), Nodo);
			bufWasModified.put(Nodo.getMyFilePosition(), true);
			numOfElements++;
			improvePriority(Nodo.getMyFilePosition());
			return;
		}
		if(elements.containsKey(Nodo.getMyFilePosition())){
			elements.put(Nodo.getMyFilePosition(), Nodo);
			bufWasModified.put(Nodo.getMyFilePosition(), true);
			improvePriority(Nodo.getMyFilePosition());
		}
		else{
			long exit = priority.pollLast();
			Nodo temp = elements.get(exit);
			temp.writeToBuffer(buffer);
			writeBlockToFile(buffer, temp.getMyFilePosition());
			bufWasModified.remove(exit);
			elements.remove(exit);
			elements.put(Nodo.getMyFilePosition(), Nodo);
			priority.addFirst(Nodo.getMyFilePosition());
			bufWasModified.put(Nodo.getMyFilePosition(), true);
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
