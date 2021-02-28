import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class Main {
  /**
   * Entry point for PictureMosaic program
   *
   * @param args String sourceDirectory: Directory to a folder of images from which to generate the mosaic
   *             String templateImage: The template image to create the mosaic from
   *             String outputName: Output file name. If file already exists, appends " (n)" after
   *             it.
   *             int resolutionX: 
   *             int resolutionY: 
   *             int granularityX: 
   *             int granularityY: 
   *
   *
   */
  public static void main(String[] args) {
    if (args.length < 7) {
      System.out.println("Invalid arguments.");
    }
    String sourceImageDirName = args[0];
    String targetImageDirName = args[1];
    String outputImageDirName = args[2];
    String resolutionXString = args[3];
    String resolutionYString = args[4];
    String granularityXString = args[5];
    String granularityYString = args[6];

    System.out.println("Source Image Directory: " + sourceImageDirName);
    System.out.println("Target Image Directory: " + targetImageDirName);
    System.out.println("Output Image Directory: " + outputImageDirName);
    System.out.println("Resolution: " + resolutionXString + "x" + resolutionYString);
    System.out.println("Granularity: " + granularityXString + "x" + granularityYString);
    System.out.println("");

    ImageWrapper[] sourceImages = parseImagesInDirectory(sourceImageDirName);
    if (sourceImages == null) {
      return;
    }

    ImageWrapper[] targetImages = parseImagesInDirectory(targetImageDirName);
    if (targetImages == null) {
      return;
    }

    File outputFolder = new File(outputImageDirName);
    if (!outputFolder.isDirectory()) {
      System.out.println("Invalid output folder.");
      return;
    }

    // The number of pixels each subimage in the output image should have.
    int[] subImageResolution = parseIntPair(resolutionXString, resolutionYString);
    if (subImageResolution == null) {
      return;
    }
    // The number of 
    int[] subImageGranularity = parseIntPair(granularityXString, granularityYString);
    if (subImageGranularity == null) {
      return;
    }
    // The total size of the output image is (subImageResolution[0] * subImageGranularity[0]) x (subImageResolution[1] * subImageGranularity[1])

    INearestNeighborFinder<Pixel, ImageWrapper> nearestImageFinder = new NearestImageFinderList();
    for (int i = 0; i < sourceImages.length; i++) {
      nearestImageFinder.add(sourceImages[i].getAveragePixel(), sourceImages[i]);
    }

    for (int i = 0; i < targetImages.length; i++) {
      processTarget(nearestImageFinder, targetImages[i], outputFolder, subImageResolution, subImageGranularity);
    }
  }

  private static ImageWrapper[] parseImagesInDirectory(String folderName) {
    File folder = new File(folderName);
    if (!folder.exists() || !folder.isDirectory()) {
      System.out.println("Invalid directory: " + folderName);
      return null;
    }

    File[] files = folder.listFiles();

    if (files.length == 0) {
      System.out.println("No files found in folder: " + folderName);
      return null;
    }

    ImageWrapper[] images = new ImageWrapper[files.length];
    for (int i = 0; i < files.length; i++) {
      BufferedImage image;
      try {
        image = ImageIO.read(files[i]);
      } catch (IOException e) {
        System.out.println("Invalid image: " + files[i].getName());
        return null;
      }
      images[i] = new ImageWrapper(image, files[i].getName());
    }
    return images;
  }
  
  private static int[] parseIntPair(String xStr, String yStr) {
    int x = 0;
    int y = 0;

    try {
      x = Integer.parseInt(xStr);
    } catch (NumberFormatException e) {
      System.out.println("The argument " + xStr + " is not a number.");
      return null;
    };
    try {
      y = Integer.parseInt(yStr);
    } catch (NumberFormatException e) {
      System.out.println("The argument " + yStr + " is not a number.");
      return null;
    };

    return new int[] { x, y };
  }

  private static void processTarget(INearestNeighborFinder<Pixel, ImageWrapper> nearestImageFinder,
      ImageWrapper target, 
      File outputFolder, 
      int[] resolution, 
      int[] granularity) {
    File outputFile = new File(outputFolder, target.name);

    BufferedImage outputImage = new BufferedImage(resolution[0] * granularity[0], resolution[1] * granularity[1], BufferedImage.TYPE_INT_RGB);
    WritableRaster outputRaster = outputImage.getRaster();

    double targetSectionWidth = (double) target.width / granularity[0];
    double targetSectionHeight = (double) target.height / granularity[1];

    for (int outY = 0; outY < granularity[1]; outY++) {
      for (int outX = 0; outX < granularity[0]; outX++) {
        Pixel targetSectionAveragePixel = target.getAveragePixelInBounds(targetSectionWidth * outX, 
            targetSectionWidth * (outX + 1), 
            targetSectionHeight * outY, 
            targetSectionHeight * (outY + 1));

        ImageWrapper closestSource = nearestImageFinder.find(targetSectionAveragePixel);
        if (closestSource == null) {
          System.out.println("Unable to process image: " + target.name + " for unknown reasons. This is a bug.");
          return;
        }

        BufferedImage compressedSource = closestSource.getCompressedImage(resolution[0], resolution[1]);        
        outputRaster.setRect(resolution[0] * outX, resolution[1] * outY, compressedSource.getRaster());
      }
    }

    try {
      ImageIO.write(outputImage, "png", outputFile);
      System.out.println("Mosaic successfully created at path: " + outputFile.getAbsolutePath());
    } catch (IOException e) {
      System.out.println("Unable to write to file: " + outputFile.getName());
    }
  }
}
