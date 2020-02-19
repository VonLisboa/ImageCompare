import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

public class ImageExtractor {

    private LinkedList<Rectangle> rectangles = new LinkedList<Rectangle>();
    private int distanceThreshold = 10;
    private double colorThreshold = 0.6;
    private final boolean diffMap;
    private Bitmap imgExpected;
    private Bitmap imgAtual;
    private Bitmap differenceMap;

    public void setImgExpected(String img) {
        if(this.imgExpected!=null){
            imgExpected.recycle();
        }
        this.imgExpected = this.loadImage(img);
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
        if(this.imgAtual!=null){
            imgAtual.recycle();
        }
        this.imgAtual = this.loadImage(img_atual);
        this.differenceMap = null;
        if (this.diffMap == true) {
            this.differenceMap = this.imgExpected.copy(Bitmap.Config.ARGB_8888, true);
        }
        for (int x = 0; x < Math.min(imgExpected.getWidth(), imgAtual.getWidth()); x++) {
            for (int y = 0; y < Math.min(imgExpected.getHeight(), imgAtual.getHeight()); y++) {
                int pixel1 = imgExpected.getPixel(x, y);
                int pixel2 = imgAtual.getPixel(x, y);
                int differenceRed = Math.abs(Color.red(pixel1) - Color.red(pixel2));
                int differenceGreen = Math.abs(Color.green(pixel1) - Color.green(pixel2));
                int differenceBlue = Math.abs(Color.blue(pixel1) - Color.blue(pixel2));
                int difference = differenceRed + differenceGreen + differenceBlue;
                double relativeDifference = (double) difference / (256 * 3);

                if (relativeDifference > colorThreshold) {
                    this.addToRectangles(x, y);
                }

                if (differenceMap != null) {
                    if (relativeDifference > colorThreshold) {
                        differenceMap.setPixel(x, y, Color.WHITE);
                    } else {
                        differenceMap.setPixel(x, y, Color.BLACK);
                    }
                }
            }
        }

        return rectangles.size();
    }

    public void saveImage(String fileName) {
        try {
            String base = Environment.getExternalStorageDirectory().toString();
            FileOutputStream out = new FileOutputStream(base+ "/" +fileName);
            imgAtual.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (this.diffMap) {
            try {
                FileOutputStream out = new FileOutputStream(fileName);
                differenceMap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private Bitmap loadImage(String input) {
        String base = Environment.getExternalStorageDirectory().toString();
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inMutable = true;
        return BitmapFactory.decodeFile(base +"/"+ input, opt);
    }

    private void addToRectangles(int x, int y) {
        Rectangle rectangle = findRectangleNearby(x, y);
        // if none found, create a new one
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

    public void drawRectangles() {
        imgAtual.prepareToDraw();
        Canvas graph = new Canvas(imgAtual);
        Paint myPaint = new Paint();
        myPaint.setColor(Color.RED);
        myPaint.setStrokeWidth(10);
        myPaint.setStyle(Paint.Style.STROKE);
        for(int i=0; i< this.rectangles.size(); i++) {
            Rectangle r = rectangles.get(i);
            //coordinates incorrect, fix with translate
            //graph.drawRect(r.x, r.y, 2, 2, myPaint);
            graph.drawLine(r.x, r.y, r.x, r.y+r.width, myPaint);
        }
    }

    public void cropRectangles() {
        Rectangle r = this.rectangles.get(0);
        Bitmap crop = Bitmap.createBitmap(imgAtual, r.x, r.y, r.width, r.height).copy(Bitmap.Config.ARGB_8888, false);
        imgAtual.recycle();
        imgAtual = null;
        imgAtual = crop;
    }
}
