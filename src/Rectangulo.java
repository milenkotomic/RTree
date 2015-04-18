import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by milenkotomic on 17-04-15.
 */
public class Rectangulo {
    public Punto p1, p2;

   /*  ___________p2
       |          |
       |          |
      p1___________*/

    public Rectangulo(Punto p1, Punto p2, Punto p3, Punto p4) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public double area(){
        return abs(this.p1.getX() - this.p2.getX()) * abs(this.p1.getY() - this.p2.getY());
    }

    public double perimetro(){
        return 2*((this.p2.getX() - this.p1.getX()) + (this.p2.getY() - this.p1.getY()));
    }

    public double areaInterseccion(Rectangulo r){
        double maxAncho = min(this.p2.getX(), r.p2.getX());
        double minAncho = max(this.p1.getX(), r.p1.getX());
        double ancho = maxAncho - minAncho;

        double maxAlto = min(this.p2.getY(), r.p2.getY());
        double minAlto = max(this.p1.getY(), r.p1.getY());
        double alto = maxAlto - minAlto;

        if (ancho > 0 && alto > 0)
            return ancho * alto;
        return 0;
    }
    public boolean contains(Rectangulo r){
        /*
        Indica si r esta dentro de este rectangulo o no
         */
        boolean p1_dentro=(this.p1.getX()>r.p1.getX() && this.p1.getY()>r.p1.getY());
        boolean p2_dentro=(this.p2.getX()>r.p2.getX() && this.p2.getY()>r.p2.getY());
        return p1_dentro && p2_dentro;
    }
    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof Rectangulo)){
            return false;
        }
        else{
            Rectangulo r = (Rectangulo) obj;
            return (this.p1.equals(r.p1) && this.p2.equals(r.p2));
        }
    }
}
