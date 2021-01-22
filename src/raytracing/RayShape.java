package raytracing;

public interface RayShape {

  public static final double TOLERANCE_MARGIN = 0.0001;

  void changePosition(double[] positionChange);

  void setPosition(double[] position);

  double[] getColor();

  double getReflectivity();

  void setReflectivity(double reflectivity);

  void setTransparency(double transparency);

  double getTransparency();

  double[] rayIntersection(double[] eye, double[] direction);

  double[] getNormal(double[] surfacePoint);
}
