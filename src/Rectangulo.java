import java.nio.ByteBuffer;
import java.util.Comparator;

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

    public Rectangulo(Punto p1, Punto p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Rectangulo(byte[] buffer, int start){
        double xP1 = ByteBuffer.wrap(buffer, start, 8).getDouble();
        start+=8;
        double yP1 = ByteBuffer.wrap(buffer, start, 8).getDouble();
        start+=8;
        double xP2 = ByteBuffer.wrap(buffer, start, 8).getDouble();
        start+=8;
        double yP2 = ByteBuffer.wrap(buffer, start, 8).getDouble();
        start+=8;
        p1 = new Punto(xP1, yP1);
        p2 = new Punto(xP2, yP2);
    }

    public Rectangulo(Rectangulo r1, Rectangulo r2) {
        double p1X = Math.min(r1.p1.getX(), r2.p1.getX());
        double p1Y = Math.min(r1.p1.getY(), r2.p1.getY());
        double p2X = Math.max(r1.p2.getX(), r2.p2.getX());
        double p2Y = Math.max(r1.p2.getY(), r2.p2.getY());
        p1 = new Punto(p1X, p1Y);
        p1 = new Punto(p2X, p2Y);


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

    public void writeToBuffer(byte[] buffer, int ini) {
        ini = 0;
        ByteBuffer.wrap(buffer, ini, 8).putDouble(p1.getX());
        ini+=8;
        ByteBuffer.wrap(buffer, ini, 8).putDouble(p1.getY());
        ini+=8;
        ByteBuffer.wrap(buffer, ini, 8).putDouble(p2.getX());
        ini+=8;
        ByteBuffer.wrap(buffer, ini, 8).putDouble(p2.getY());
        ini+=8;
    }

    /*public boolean contains(Rectangulo r){
        Rectangulo expandido = new Rectangulo(this, r);
        return this.area() == expandido.area();
    }*/

    private Punto puntoMedio(){
        double x = (p1.getX() + p2.getX())/2.0;
        double y = (p1.getY() + p2.getY())/2.0;
        return new Punto(x, y);
    }

    public double distanceTo(Rectangulo r){
        return distance(this.puntoMedio(), r.puntoMedio());
    }

    private double distance(Punto punto1, Punto punto2) {
        double distance = (p1.getX() - p2.getX())*(p1.getX() - p2.getX());
        distance += (p1.getY() - p2.getY())*(p1.getY() - p2.getY());
        return Math.sqrt(distance);
    }

    /**
     * Retorna el area inutil que se crearia de insertar el nuevo mbr
     * */
    public double uselessArea(Rectangulo r){
        return (new Rectangulo(this, r)).area() - this.area() - r.area();
    }
    /**
     * Retorna cuanto aumentaria el area si se inserta el nuevo MBR
     * */
    public double extraArea(Rectangulo r){
        return (new Rectangulo(this, r)).area() - this.area();
    }

    public boolean intersect (Rectangulo r){
        return this.areaInterseccion(r) > 0;
    }

}
