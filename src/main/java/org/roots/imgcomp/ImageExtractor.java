package org.roots.imgcomp;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import javax.imageio.ImageIO;

/**
 *
 * @author User396
 */
public class ImageExtractor {

    private LinkedList<Rectangle> rectangles = new LinkedList<Rectangle>();
    private int distanceThreshold = 10;
    private double colorThreshold = 0.6;
    private final boolean diffMap;
    private BufferedImage imgExpected;
    private BufferedImage imgAtual;
    private BufferedImage differenceMap;

    public void setImgExpected(BufferedImage imgExpected) {
        this.imgExpected = imgExpected;
    }

    public ImageExtractor(String expected) {
        this.diffMap = false;
        this.imgExpected = this.loadImage(expected);
    }

    public ImageExtractor(String expected, boolean diffMap) {
        this.diffMap = diffMap;
        this.imgExpected = this.loadImage(expected);
    }

    public ImageExtractor(String expected, boolean diffMap, double colorThreshold, int distanceThreshold) {
        this.diffMap = diffMap;
        this.colorThreshold = colorThreshold;
        this.distanceThreshold = distanceThreshold;
        this.imgExpected = this.loadImage(expected);
    }

    public int searchDiff(String img_atual) {
        this.imgAtual = this.loadImage(img_atual);
        this.differenceMap = null;
        if (this.diffMap == true) {
            this.differenceMap = this.imgExpected;
        }
        for (int x = 0; x < Math.min(imgExpected.getWidth(), imgAtual.getWidth()); x++) {
            //System.out.println((double) x / differenceMap.getWidth() * 100 + "%");
            for (int y = 0; y < Math.min(imgExpected.getHeight(), imgAtual.getHeight()); y++) {
                Color color1 = new Color(imgExpected.getRGB(x, y));
                Color color2 = new Color(imgAtual.getRGB(x, y));

                int differenceRed = Math.abs(color1.getRed() - color2.getRed());
                int differenceGreen = Math.abs(color1.getGreen() - color2.getGreen());
                int differenceBlue = Math.abs(color1.getBlue() - color2.getBlue());
                int difference = differenceRed + differenceGreen + differenceBlue;
                double relativeDifference = (double) difference / (256 * 3);

                if (relativeDifference > colorThreshold) {
                    this.addToRectangles(x, y);
                }

                if (differenceMap != null) {
                    if (relativeDifference > colorThreshold) {
                        differenceMap.setRGB(x, y, new Color(0, 0, 0).getRGB());
                    } else {
                        differenceMap.setRGB(x, y, new Color(255, 255, 255).getRGB());
                    }
                }
            }
        }

        return rectangles.size();
    }

    public void saveImage(String fileName) {
        try {
            BufferedOutputStream bos;
            bos = new BufferedOutputStream(new FileOutputStream(fileName));
            ImageIO.write(imgAtual, "png", bos);
            bos.close();
            if (this.diffMap) {
                bos = new BufferedOutputStream(new FileOutputStream("map." + fileName));
                ImageIO.write(this.differenceMap, "png", bos);
                bos.close();
            }
        } catch (IOException e) {
            System.err.println("ERROR: could not write to " + fileName);
        }
    }

    private BufferedImage loadImage(String input) {
        String base = "C:\\Users\\User3\\ImageCompare\\";
        try {
            return ImageIO.read(new File(base + input));
        } catch (IOException e) {
            System.out.println(e);
        }
        return null;
    }

    private void addToRectangles(int x, int y) {
        Rectangle rectangle = findRectangleNearby(x, y);
        // if none found, create new
        if (rectangle == null) {
            rectangles.add(new Rectangle(x, y, 1, 1));
        } else {
            // if we did find a rectangle close by, enlarge it in order for it to to contain the given (x, y)
            if (x > rectangle.x + rectangle.width) {
                rectangle.width += x - rectangle.x - rectangle.width + 1;
            } else if (x < rectangle.x) {
                rectangle.width += Math.abs(rectangle.x - x) + 1;
                rectangle.x = x;
            }
            if (y > rectangle.y + rectangle.height) {
                rectangle.height += y - rectangle.y - rectangle.height + 1;
            } else if (y < rectangle.y) {
                rectangle.height += Math.abs(rectangle.y - y) + 1;
                rectangle.y = y;
            }
        }
    }

    private Rectangle findRectangleNearby(int x, int y) {
        for (int i = 0; i < this.rectangles.size(); i++) {
            Rectangle r = this.rectangles.get(i);
            if (x > r.x - distanceThreshold && y > r.y - distanceThreshold && x < r.x + r.width + distanceThreshold && y < r.y + r.height + distanceThreshold) {
                return r;
            }
        }
        return null;
    }

    private void drawRectanglesColor(Graphics2D graph) {
        this.rectangles.forEach((r) -> {
            graph.setColor(Color.RED);
            graph.drawRect(r.x, r.y, r.width, r.height);
        });
    }

    public void drawRectangles() {
        Graphics2D graph = imgAtual.createGraphics();
        drawRectanglesColor(graph);
        graph.dispose();
    }

    public void cropRectangles() {
        Rectangle r = this.rectangles.get(0);
        imgAtual = imgAtual.getSubimage(r.x, r.y, r.width, r.height);
    }
}
