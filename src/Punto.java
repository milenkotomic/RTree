import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by milenkotomic on 17-04-15.
 */
public class Punto {
    private double x;
    private double y;

    public Punto(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean isValid(){
        return this.x >= 0 && this.x <=50000000 && this.y >= 0 && this.y <=50000000;
    }

    public double distance(Punto p){
        return sqrt(pow(this.getX() - p.getX(), 2) + pow(this.getY() - p.getY(), 2));
    }
}
