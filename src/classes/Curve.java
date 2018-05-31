package classes;

/**
 * Created by S507098 on 4/13/2017.
 */

public class Curve {
    private Expression curve;
    private boolean adds; //true if result is added, false if result replaces original grade.

    public Curve(Expression c){
        curve = c;
    }

    public double curveGrade(double orig){
        double fin = curve.evaluate(orig);
        return (adds)? orig+fin : fin;
    }
}
