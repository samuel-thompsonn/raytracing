package raytracing;

import raytracing.linear_util.RayVector;

public interface RayShape {

  public static final double TOLERANCE_MARGIN = 0.0001;

  void changePosition(RayVector positionChange);

  void setPosition(RayVector position);

  double[] getColor(RayVector surfacePoint);

  double getReflectivity();

  void setReflectivity(double reflectivity);

  void setTransparency(double transparency);

  double getTransparency();

  RayVector rayIntersection(RayVector eye, RayVector direction);

  RayVector getNormal(RayVector surfacePoint);
}
