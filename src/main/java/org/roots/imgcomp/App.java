package org.roots.imgcomp;

public class App {
    App(){}

    public static void main(String[] args) {
        ImageExtractor img = new ImageExtractor("expected.png", true);
        
        int searchDiff = img.searchDiff("atual.png");
        System.out.println(searchDiff);
        img.drawRectangles();
        
        img.saveImage("result.png");
    }
}
