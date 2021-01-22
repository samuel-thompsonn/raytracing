package raytracing;

import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class GifWritingTest {

  private ImageWriter myGifWriter;
  private ImageWriteParam myWriteParam;
  private IIOMetadata myImageMetadata;

  public static final int TIME_BETWEEN_FRAMES_MS = 100;

  public GifWritingTest() throws IOException {
    int width = 200, height = 200;
    BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
    WritableRaster raster = bi.getRaster();

    Iterator imageWriters = ImageIO.getImageWritersByFormatName("GIF");
    myGifWriter = (ImageWriter) imageWriters.next();

    myWriteParam = myGifWriter.getDefaultWriteParam();
    ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(bi.getType());
    myImageMetadata = myGifWriter.getDefaultImageMetadata(imageTypeSpecifier,myWriteParam);

    String metaFormatName = myImageMetadata.getNativeMetadataFormatName();
    IIOMetadataNode root = (IIOMetadataNode)
            myImageMetadata.getAsTree(metaFormatName);

    IIOMetadataNode graphicsControlExtensionNode = getNode(
            root,
            "GraphicControlExtension");

    graphicsControlExtensionNode.setAttribute("disposalMethod", "restoreToBackgroundColor");
    graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
    graphicsControlExtensionNode.setAttribute(
            "transparentColorFlag",
            "FALSE");
    graphicsControlExtensionNode.setAttribute(
            "delayTime",
            Integer.toString(TIME_BETWEEN_FRAMES_MS / 10));
    graphicsControlExtensionNode.setAttribute(
            "transparentColorIndex",
            "0");

    IIOMetadataNode appEntensionsNode = getNode(root,"ApplicationExtensions");
    IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
    child.setAttribute("applicationID", "NETSCAPE");
    child.setAttribute("authenticationCode", "2.0");
    int looping = 0;
    child.setUserObject(new byte[]{0x1,(byte)(looping & 0xFF), (byte) ((looping >> 8) & 0xFF)});

    appEntensionsNode.appendChild(child);
    myImageMetadata.setFromTree(metaFormatName,root);

    File file = new File("GifTester.gif");
    ImageOutputStream ios = ImageIO.createImageOutputStream(file);
    myGifWriter.setOutput(ios);
    myGifWriter.prepareWriteSequence(null);
  }

//  public void createGif(String[] args) throws IOException {
//    int width = 200, height = 200;
//    BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
//    WritableRaster raster = bi.getRaster();
//
//    Iterator imageWriters = ImageIO.getImageWritersByFormatName("GIF");
//    myGifWriter = (ImageWriter) imageWriters.next();
//
//    myWriteParam = myGifWriter.getDefaultWriteParam();
//    ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(bi.getType());
//    myImageMetadata = myGifWriter.getDefaultImageMetadata(imageTypeSpecifier,myWriteParam);
//
//    String metaFormatName = myImageMetadata.getNativeMetadataFormatName();
//    IIOMetadataNode root = (IIOMetadataNode)
//            myImageMetadata.getAsTree(metaFormatName);
//
//    IIOMetadataNode graphicsControlExtensionNode = getNode(
//            root,
//            "GraphicControlExtension");
//
//    graphicsControlExtensionNode.setAttribute("disposalMethod", "restoreToBackgroundColor");
//    graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
//    graphicsControlExtensionNode.setAttribute(
//            "transparentColorFlag",
//            "FALSE");
//    graphicsControlExtensionNode.setAttribute(
//            "delayTime",
//            Integer.toString(TIME_BETWEEN_FRAMES_MS / 10));
//    graphicsControlExtensionNode.setAttribute(
//            "transparentColorIndex",
//            "0");
//
//    IIOMetadataNode appEntensionsNode = getNode(root,"ApplicationExtensions");
//    IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
//    child.setAttribute("applicationID", "NETSCAPE");
//    child.setAttribute("authenticationCode", "2.0");
//    int looping = 0;
//    child.setUserObject(new byte[]{0x1,(byte)(looping & 0xFF), (byte) ((looping >> 8) & 0xFF)});
//
//    appEntensionsNode.appendChild(child);
//    myImageMetadata.setFromTree(metaFormatName,root);
//
//    File file = new File("GifTester.gif");
//    ImageOutputStream ios = ImageIO.createImageOutputStream(file);
//    myGifWriter.setOutput(ios);
//    myGifWriter.prepareWriteSequence(null);
//    for (int i = 0; i < 10; i ++) {
//      for (int x = 0; x < 100; x ++) {
//        for (int y = 0; y < 100; y ++) {
//          raster.setPixel(x,y,new int[]{i*10,0,0});
//        }
//      }
//
//      myGifWriter.writeToSequence(
//              new IIOImage(
//                      bi,
//                      null,
//                      myImageMetadata),
//              myWriteParam
//      );
//    }
//
//  }

  public void writeGifFrame(int width, int height, PixelReader pixelReader) {
    BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
    WritableRaster raster = bi.getRaster();
    for (int i = 0; i < width; i ++) {
      for (int j = 0; j < height; j ++) {
        Color pixelColor = pixelReader.getColor(i,j);
        int red = (int)Math.floor(pixelColor.getRed() * 255);
        int green = (int)Math.floor(pixelColor.getGreen() * 255);
        int blue = (int)Math.floor(pixelColor.getBlue() * 255);
        int[] colorArray = new int[]{red,green,blue};
        raster.setPixel(i,j,colorArray);
      }
    }
    try {
      myGifWriter.writeToSequence(
              new IIOImage(
                      bi,
                      null,
                      myImageMetadata),
              myWriteParam
      );
    } catch (IOException e) {
      System.out.println("Couldn't put together GIF.");
    }
  }

  /**
   * @author Elliot Kroo (elliot[at]kroo[dot]net)
   * Returns an existing child node, or creates and returns a new child node (if
   * the requested node does not exist).
   *
   * @param rootNode the <tt>IIOMetadataNode</tt> to search for the child node.
   * @param nodeName the name of the child node.
   *
   * @return the child node, if found or a new node created with the given name.
   */
  private static IIOMetadataNode getNode(
          IIOMetadataNode rootNode,
          String nodeName) {
    int nNodes = rootNode.getLength();
    for (int i = 0; i < nNodes; i++) {
      if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName)
              == 0) {
        return((IIOMetadataNode) rootNode.item(i));
      }
    }
    IIOMetadataNode node = new IIOMetadataNode(nodeName);
    rootNode.appendChild(node);
    return(node);
  }
}
