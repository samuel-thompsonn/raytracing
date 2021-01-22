//package raytracing.graveyard;
//
//import javafx.animation.KeyFrame;
//import javafx.animation.Timeline;
//import javafx.application.Application;
//import javafx.scene.Group;
//import javafx.scene.Scene;
//import javafx.scene.image.ImageView;
//import javafx.scene.image.PixelWriter;
//import javafx.scene.image.WritableImage;
//import javafx.scene.input.KeyCode;
//import javafx.scene.paint.Color;
//import javafx.stage.Stage;
//import javafx.util.Duration;
//import raytracing.Light;
//import raytracing.RayTraceModel;
//import raytracing.rayshape.Sphere;
//
//public class Main extends Application {
//
//  public static final int WIDTH_PIXELS = 1024;
//  public static final int HEIGHT_PIXELS = 1024;
//  public static final int SCENE_WIDTH = 1024;
//  public static final int SCENE_HEIGHT = 1024;
//  public static final double FOV = 65.;
//  public static final int NUM_BOUNCES = 8;
//
//  @Override
//  public void start(Stage primaryStage) throws Exception {
//    double[] cameraPos = {0,0,0};
//    double[] cameraDirection = {1,0,0};
//    double[] cameraRight = {0,1,0};
//
//    RayTraceModel model = new RayTraceModel();
//    model.addSphere(new Sphere(new double[]{3,0.4,0},1.2,new double[]{1.0,0.7,0.0},0.8));
//    Sphere secondSphere = new Sphere(new double[]{1,-0.9,-0.4},0.6,new double[]{0.7,0.2,0.7});
//    secondSphere.setTransparency(0.7);
//    secondSphere.setReflectivity(0.3);
//    model.addSphere(secondSphere);
//    Sphere fourthSphere = new Sphere(new double[]{2,1.2,1.7},0.5,new double[]{0.8,0.1,0.1});
//    fourthSphere.setTransparency(0.1);
//    fourthSphere.setReflectivity(0.1);
//    model.addSphere(new Sphere(new double[]{2.4,-1.6,0.2},0.7,new double[]{0.1,0.3,1.0}));
//    model.addSphere(new Sphere(new double[]{5.1,0.5,2},0.6,new double[]{0,0,0}));
//    Sphere bottomSphere = new Sphere(new double[]{3.5,0.0,-6},4,new double[]{0.5,0.5,0.5});
//    bottomSphere.setReflectivity(0.4);
//    model.addSpheres(fourthSphere,bottomSphere);
////    model.addLight(new raytracing.Light(new double[]{0,-.5,1},0.8));
////    model.addLight(new raytracing.Light(new double[]{0,0.6,-3},0.4));
//    model.addLight(new Light(new double[]{0,0.6,10},1.0));
//    model.addLight(new Light(new double[]{0,2.,0},1.0));
//
//    Group pixelGroup = new Group();
//
////    WritableImage img = new WritableImage(WIDTH_PIXELS,HEIGHT_PIXELS);
////    PixelWriter pw = img.getPixelWriter();
////    for (int i = 0; i < WIDTH_PIXELS; i ++) {
////      for (int j = 0; j < HEIGHT_PIXELS; j ++) {
////        pw.setColor(i,j,Color.color(0,0,0));
////      }
////    }
//
//
//    double[][][] colorBuffer = new double[WIDTH_PIXELS][HEIGHT_PIXELS][3];
//
//
//    WritableImage img = new WritableImage(WIDTH_PIXELS,HEIGHT_PIXELS);
//    PixelWriter pw = img.getPixelWriter();
//    ImageView viewer = new ImageView();
//    viewer.setFitWidth(SCENE_WIDTH);
//    viewer.setFitHeight(SCENE_HEIGHT);
//    pixelGroup.getChildren().add(viewer);
//
//    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1/1.),event -> {
//      System.out.println("Printing current image status...");
//      long currentTime = System.currentTimeMillis();
//      System.out.println(currentTime);
//      if (!model.isDirty()) {
//        return;
//      }
//      model.resetDirty();
//      for (int i = 0; i < WIDTH_PIXELS; i ++) {
//        for (int j = 0; j < HEIGHT_PIXELS; j ++) {
//          double[] color = colorBuffer[i][j];
//          pw.setColor(i,j,Color.color(color[0],color[1],color[2]));
//        }
//      }
//
//      viewer.setImage(img);
//    }));
//    timeline.setCycleCount(Timeline.INDEFINITE);
//    timeline.play();
//
////    for (int i = 0; i < WIDTH_PIXELS; i ++) {
////      double horizontalAngle = (-FOV + ((i * (1.0/ WIDTH_PIXELS)) * FOV * 2)) * ((2*Math.PI)/360);
////      for (int j = 0; j < HEIGHT_PIXELS; j ++) {
////        double verticalAngle = ((90-FOV) + ((j * (1.0/ HEIGHT_PIXELS)) * FOV * 2)) * ((2*Math.PI)/360);
////
////        double x = Math.sin(verticalAngle) * Math.cos(horizontalAngle);
////        double y = Math.sin(verticalAngle) * Math.sin(horizontalAngle);
////        double z = Math.cos(verticalAngle);
////        double[] direction = {x,y,z};
////
////        double[] rayColor = model.getRayColor(cameraPos,direction,4);
////        pixels[i][j].setFill(Color.color(rayColor[0],rayColor[1],rayColor[2]));
////      }
////    }
//
//    Scene scene = new Scene(pixelGroup,SCENE_WIDTH,SCENE_HEIGHT);
//
//    scene.setOnKeyPressed(event -> {
//      if (event.getCode().equals(KeyCode.SPACE)) {
//        for (int i = 1024; i <= 1024; i *= 2) {
//          long before = System.currentTimeMillis();
//          model.generateImageCallback(colorBuffer, i, i, cameraPos, cameraDirection, FOV, NUM_BOUNCES,doubles -> {
//            handleCompletedImage(doubles,pw,viewer,img);
//          });
//          long after = System.currentTimeMillis();
//          long totalTime = after - before;
//          double framesPerSecond = 1000. / (totalTime);
//          System.out.printf("Time using a %d x %d render: %d milliseconds (%f frames per second)\n",
//                  i, i, totalTime, framesPerSecond);
//        }
//
//      }
//    });
//
//
//    primaryStage.setScene(scene);
//    primaryStage.setTitle("Raytracing test");
//    primaryStage.show();
//
//    Thread renderThread =
//    new Thread(new Runnable() {
//      @Override
//      public void run() {
//        for (int i = 1024; i <= 1024; i *=2) {
//          long before = System.currentTimeMillis();
//          model.generateImage(colorBuffer,i,i,cameraPos,cameraDirection,FOV, NUM_BOUNCES);
//          long after = System.currentTimeMillis();
//          long totalTime = after - before;
//          double framesPerSecond = 1000./(totalTime);
//          System.out.printf("Time using a %d x %d render: %d milliseconds (%f frames per second)\n",
//                  i,i,totalTime,framesPerSecond);
//        }
//      }
//    });
//    renderThread.setPriority(Thread.MIN_PRIORITY);
//    renderThread.start();
//
//  }
//
//  private void handleCompletedImage(double[][][] colorBuffer, PixelWriter pw, ImageView viewer, WritableImage img) {
//    long before = System.currentTimeMillis();
//    long after = System.currentTimeMillis();
//    for (int i = 0; i < WIDTH_PIXELS; i++) {
//      for (int j = 0; j < HEIGHT_PIXELS; j++) {
//        double[] color = colorBuffer[i][j];
//        pw.setColor(i, j, Color.color(color[0], color[1], color[2]));
//      }
//    }
//    viewer.setImage(img);
//    long totalTime = after - before;
//    double framesPerSecond = 1000. / (totalTime);
//    System.out.printf("Time putting image onscreen: %d milliseconds (%f frames per second)\n",
//            totalTime,framesPerSecond);
//  }
//
//
//}
