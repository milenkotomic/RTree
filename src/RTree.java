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

    public void insertar(Rectangulo c){

    }
    public void split(){

    }

    public Nodo getRaiz() {
        return raiz;
    }

    public void setRaiz(Nodo raiz) {
        this.raiz = raiz;
    }
}
