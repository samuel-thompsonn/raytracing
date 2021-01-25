package raytracing;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import raytracing.linear_util.RayVector;
import raytracing.linear_util.SimpleRayVector;
import raytracing.rayshape.Sphere;
import raytracing.rayshape.TriPlane;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class TaskTest extends Application {

  public static final int STALL_CYCLES = 100;
  public static final int SIDE_LENGTH = 512;
  public static final int SECTOR_SIZE = 64;
  public static final double SCENE_SIZE = 512;
  private int numCompletedThreads = 0;
  private RayTraceModel myModel;
  private ImageView myImage;
  private WritableImage myWritableImage;
  private PixelWriter myPixelWriter;
  private GifWritingTest myGifWriter;
  private Sphere movableSphere;
  private Sphere largerSphere;
  private int numFrameIterations; //Having this thing is TERRIBLE design and suggests a need for a new class.
  private Scene myScene;
  private Group myImageViewGroup;

  @Override
  public void start(Stage primaryStage) throws Exception {

    myModel = initModel();

    myWritableImage = new WritableImage(SIDE_LENGTH, SIDE_LENGTH);
    myPixelWriter = myWritableImage.getPixelWriter();
    myImage = new ImageView(myWritableImage);
    myImage.setSmooth(false);
    myImage.setFitWidth(SCENE_SIZE);
    myImage.setFitHeight(SCENE_SIZE);

    myGifWriter = new GifWritingTest();

    myImageViewGroup = new Group();
    myImageViewGroup.getChildren().add(myImage);

    long before = System.currentTimeMillis();
    for (int i = 0; i < SIDE_LENGTH; i ++) {
      for (int j = 0; j < SIDE_LENGTH; j ++) {
        myPixelWriter.setColor(j,i, Color.BLACK);
      }
    }
    long elapsed = System.currentTimeMillis() - before;
    System.out.println("Clearing screen takes " + elapsed + " milliseconds.");

    myScene = new Scene(myImageViewGroup, SCENE_SIZE, SCENE_SIZE);

    myScene.setOnKeyPressed(event -> {
      if (event.getCode().equals(KeyCode.SPACE)) {
        System.out.println("System time: " + System.currentTimeMillis());
        for (int i = 0; i < SIDE_LENGTH; i ++) {
          for (int j = 0; j < SIDE_LENGTH; j ++) {
            myPixelWriter.setColor(j,i, Color.BLACK);
          }
        }
      }
      else if (event.getCode().equals(KeyCode.Q)) {
        writeSectorsConcurrently(myWritableImage, SIDE_LENGTH, SECTOR_SIZE, v->{});
        System.out.println("System time: " + System.currentTimeMillis());
      }
      else if (event.getCode().equals(KeyCode.W)) {
        System.out.println("System time before: " + System.currentTimeMillis());
        writeSectorsSequentially(myPixelWriter, SIDE_LENGTH);
        System.out.println("System time after: " + System.currentTimeMillis());
      }
      else if (event.getCode().equals(KeyCode.E)) {
        System.out.println("Writing image to raytraced_scene.png");

        BufferedImage outputImage = new BufferedImage(SIDE_LENGTH, SIDE_LENGTH, BufferedImage.TYPE_INT_RGB);

        WritableRaster raster = outputImage.getRaster();
        for (int i=0; i<SIDE_LENGTH; i++ ) {
          for (int  j=0; j<SIDE_LENGTH; j++ ) {
            Color pixelColor = myWritableImage.getPixelReader().getColor(i,j);
            int red = (int)Math.floor(pixelColor.getRed() * 255);
            int green = (int)Math.floor(pixelColor.getGreen() * 255);
            int blue = (int)Math.floor(pixelColor.getBlue() * 255);
            int[] colorArray = new int[]{red,green,blue};
            raster.setPixel(i, j, colorArray);
          }
        }

        try {
          ImageIO.write(outputImage, "png", new File("raytraced_scene.png"));
        } catch (IOException ioException) {
          ioException.printStackTrace();
        }
      }
      else if (event.getCode().equals(KeyCode.R)) {
        int numFrames = 50;
        numFrameIterations = 0;
        //Found the problem: I was passing in the side length as the sector size, so I wouldn't
        // expect any parallelism.
        double initialAngle = 0;
        movableSphere.setPosition(new SimpleRayVector(4+(2*Math.cos(initialAngle)),0.4+(2*Math.sin(initialAngle)),0.8+(2*Math.sin(initialAngle+Math.PI))));
        largerSphere.setPosition(new SimpleRayVector(4-(0.2*Math.cos(initialAngle)),0.4-(0.2*Math.sin(initialAngle)),0.0-(0.2*Math.sin(initialAngle+Math.PI))));
        writeFramesConcurrently(numFrames,myWritableImage,SIDE_LENGTH,SECTOR_SIZE,aVoid -> {
          numFrameIterations++;
          double angle = 2 * Math.PI * (numFrameIterations*1. / numFrames);
          movableSphere.setPosition(new SimpleRayVector(4+(2*Math.cos(angle)),0.4+(2*Math.sin(angle)),0.8+(2*Math.sin(angle+Math.PI))));
          largerSphere.setPosition(new SimpleRayVector(4-(0.2*Math.cos(angle)),0.4-(0.2*Math.sin(angle)),0.0-(0.2*Math.sin(angle+Math.PI))));
          myGifWriter.writeGifFrame(SIDE_LENGTH,SIDE_LENGTH,myWritableImage.getPixelReader());
        });
        System.out.println("Completed GIF of " + numFrames + " frames.");
      }

    });

    primaryStage.setTitle("JavaFX multithreading API test");
    primaryStage.setScene(myScene);
    primaryStage.show();
  }

  private void writeFramesConcurrently(int numFrames,WritableImage image, int sideLength, int sectorSize, Consumer<Void> perFrameComplete) {
    if (numFrames < 1) {
      System.out.println("Completed GIF with unknown frame count.");
      return;
    }
    writeSectorsConcurrently(image,sideLength,sectorSize,e -> {
      perFrameComplete.accept(null);
      writeFramesConcurrently(numFrames-1,image,sideLength,sectorSize,perFrameComplete);
    });
  }

  private RayTraceModel initModel() {
    RayTraceModel model = new RayTraceModel();

    model.setFov(65);
    model.setNumBounces(1);
    model.setImageSize(SIDE_LENGTH, SIDE_LENGTH);
    model.setCameraPos(new SimpleRayVector(0,0,0));
    model.setBackgroundColor(new double[]{0,0,0});

    largerSphere = new Sphere(new SimpleRayVector(4,0.4,0),1.2,new double[]{1.0,0.7,0.0});

    movableSphere = new Sphere(new SimpleRayVector(4,0.4,0.8),0.5,new double[]{0.8,0.1,0.1});
    movableSphere.setReflectivity(0.1);

    Sphere bottomSphere = new Sphere(new SimpleRayVector(3.5,0.0,-6),4,new double[]{0.5,0.5,0.5});
    bottomSphere.setReflectivity(0.4);

//    TriPlane plane = new TriPlane(List.of( //Produces an error where it fills a vertical bar onscreen.
//            new double[]{3,0,2},
//            new double[]{3,0,3.5},
//            new double[]{4,1,2}
//    ));

    TriPlane plane = new TriPlane(List.of(
            new SimpleRayVector(3,0,-1),
            new SimpleRayVector(3,0,2),
            new SimpleRayVector(4,6,2)
    ));

    TriPlane nearbyPlane = new TriPlane(List.of(
            new SimpleRayVector(2.8,0,-1),
            new SimpleRayVector(4,-6,2),
            new SimpleRayVector(2.8,0,2)
    ));

    RayVector earthCenter = new SimpleRayVector(3.7,0,0.0);
    Sphere earthSphere = new Sphere(earthCenter,1.0,new double[]{1.0,0,0});
    earthSphere.setTexture("raytracing/rayshape/resources/earthtexture_clouds.jpg");

//    TriPlane secondPlane = new TriPlane(List.of(
//            new double[]{1,0,1},
//            new double[]{1,1,1},
//            new double[]{2,1,1}
//    ));

//    model.addShapes(largerSphere, movableSphere, bottomSphere, plane);

//    Sphere lightSphere = new Sphere(new double[]{3,0,0},0.5,new double[]{1,1,1});
//    lightSphere.setTransparency(0.9);
//    Sphere lampshadeSphere = new Sphere(new double[]{3,0,0},1.2,new double[]{1.0,0.7,0}); //I lost like 45 minutes of my life because I put a 7 instead of a 0.7
//    lampshadeSphere.setTransparency(0.5);
//    Sphere lightedSphere = new Sphere(new double[]{4.3,2,2},0.7,new double[]{1,0,0});
//    model.addShapes(lightSphere,lightedSphere,lampshadeSphere);
//    model.addLight(new Light(new double[]{3,0,0},0.85));

//    model.addShapes(plane,nearbyPlane,frontSphere);
//
//    model.addShape(largerSphere);
      RayVector sunCenter = new SimpleRayVector(-100,100,0);
      double sunRadius = 1;
      int numLights = 1000;
      for (int i = 0; i < numLights; i ++) {
        RayVector offset = new SimpleRayVector(new Random().nextDouble() * sunRadius,0,new Random().nextDouble() * sunRadius);
        model.addLight(new Light(sunCenter.add(offset),0.9));
      }
//    model.addLight(new Light(LinearUtil.vectorAdd(sunCenter,new double[]{0,0,-1}),0.9));
//    model.addLight(new Light(LinearUtil.vectorAdd(sunCenter,new double[]{0,0,1}),0.9));

    System.out.println("Earth center: " + earthCenter.toString());
    RayVector earthToSun = sunCenter.subtract(earthCenter);
    System.out.println("Earth to sun: " + earthToSun.toString());
//    RayVector moonCenter = LinearUtil.vectorAdd(LinearUtil.vectorScale(1./4,earthToSun),earthCenter);
    RayVector moonCenter = earthToSun.normalized().scale(2.5).add(earthCenter);
    System.out.println("Moon center: " + moonCenter.toString());
    RayVector earthToMoon = moonCenter.subtract(earthCenter);
    System.out.println("Earth to moon: " + earthToMoon.toString());
    Sphere moonSphere = new Sphere(moonCenter,0.25,new double[]{0,0,0});
    moonSphere.setTexture("raytracing/rayshape/resources/moontexture.jpg");
    model.addShapes(earthSphere, moonSphere);

    return model;
  }

  private void writeSectorsSequentially(PixelWriter writer, int sideLength) {
    for (int i = 0; i < sideLength; i ++) {
      for (int j = 0; j < sideLength; j++) {
        for (int count = 0; count < STALL_CYCLES; count ++) {
              //do nothing
          writer.setColor(j,i,Color.BLACK);
        }
          writer.setColor(j,i, Color.YELLOW);
      }
    }
  }

  private void writeSector(PixelWriter writer, int startX, int startY, int sectorSize) {
    for (int y = 0; y < sectorSize; y ++) {
      for (int x = 0; x < sectorSize; x ++) {
        double[] pixelColor = myModel.generatePixelRgb(x+startX,y+startY);
        assert pixelColor != null;
        writer.setColor(x+startX,y+startY,Color.color(pixelColor[0],pixelColor[1],pixelColor[2]));
      }
    }
  }

  private void writeSectorsConcurrently(WritableImage image, int sideLength, int sectorSize, Consumer<Void> onComplete) {
    final long startTime = System.currentTimeMillis();
    numCompletedThreads = 0;
    List<Task<Void>> drawThreads = new ArrayList<>();
    for (int i = 0; i < sideLength; i += sectorSize) {
      for (int j = 0; j < sideLength; j+= sectorSize) {
        int finalJ = j;
        int finalI = i;
        Task<Void> sectorTask = new Task<>() {
          @Override
          protected Void call() throws Exception {
            writeSector(image.getPixelWriter(), finalJ, finalI, sectorSize);
            return null;
          }
        };
        sectorTask.setOnSucceeded(event -> {
          numCompletedThreads++;
          if (numCompletedThreads >= Math.pow(sideLength*1. / sectorSize,2)) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("All " + numCompletedThreads + " threads completed in " + elapsedTime / 1000. + " seconds.");
            System.out.println("my writeable image: " + myWritableImage);
            System.out.println("my image view: " + myImage);
            System.out.println("my pixel writer: " + myPixelWriter);
            System.out.println("my scene: " + myScene);
            System.out.println("my group: " + myImageViewGroup);
            numCompletedThreads = 0;
//            myGifWriter.writeGifFrame(SIDE_LENGTH,SIDE_LENGTH,image.getPixelReader());
            onComplete.accept(null);
          }
        });
        sectorTask.setOnFailed(event -> {
          System.out.println(event.toString());
          System.out.println(Arrays.toString(sectorTask.getException().getStackTrace()));
        });
        drawThreads.add(sectorTask);
      }
    }

    for (Task<Void> sectorTask : drawThreads) {
      new Thread(sectorTask).start();
    }
  }
}
