import java.util.ArrayList;

/**
 * Created by luism on 17-04-15.
 */
public class Nodo {
    private int t;
    private ArrayList<Rectangulo> keys;
    private ArrayList<Nodo> hijos;
    public Nodo(int t){
        this.setT(t);
        setKeys(new ArrayList<Rectangulo>());
        setHijos(new ArrayList<Nodo>());
    }
    public boolean isFull(){

        return (keys.size()>2*getT());
    }
    public boolean isEmpty(){

        return (keys.size()<t);
    }
    public boolean isLeaf(){

        return getHijos().isEmpty();
    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }

    public ArrayList<Rectangulo> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<Rectangulo> keys) {
        this.keys = keys;
    }

    public ArrayList<Nodo> getHijos() {
        return hijos;
    }

    public void setHijos(ArrayList<Nodo> hijos) {
        this.hijos = hijos;
    }
}
