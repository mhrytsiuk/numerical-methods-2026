package lab9;

@FunctionalInterface
public interface ObjectiveFunction {
    double evaluate(double[] x);
}
