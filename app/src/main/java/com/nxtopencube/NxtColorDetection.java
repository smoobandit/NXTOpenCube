package com.nxtopencube;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;

/* loaded from: classes.dex */
public class NxtColorDetection {
    private int[] xstart = {535, 770, 1075, 820, 1125, 1500, 1150, 1530, 1980};
    private int[] ystart = {1370, 940, 425, 1825, 1430, 900, 2320, 1990, 1480};
    //Colours is simple.  Each side(6)/cubie(9) has RGB(3) values.
    private int[][][] colors = (int[][][]) Array.newInstance(Integer.TYPE, 6, 9, 3);
    //Distance is not as simple.  I THINK that each side/cubie has a distance from the centre colour of each side's centre cubie(6)
    private int[][][] distance = (int[][][]) Array.newInstance(Integer.TYPE, 6, 9, 6);
    private char[] sides = {'U', 'R', 'F', 'D', 'L', 'B'};
    //private int[][] order = {new int[]{8, 7, 6, 5, 4, 3, 2, 1}, new int[]{6, 3, 0, 7, 4, 1, 8, 5, 2}, new int[]{6, 3, 0, 7, 4, 1, 8, 5, 2}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, new int[]{6, 3, 0, 7, 4, 1, 8, 5, 2}, new int[]{6, 3, 0, 7, 4, 1, 8, 5, 2}};
    private int[][] order = {
            {8, 7, 6, 5, 4, 3, 2, 1, 0}, //U
            {6, 3, 0, 7, 4, 1, 8, 5, 2}, //R
            {6, 3, 0, 7, 4, 1, 8, 5, 2}, //F
            {0, 1, 2, 3, 4, 5, 6, 7, 8}, //D
            {6, 3, 0, 7, 4, 1, 8, 5, 2}, //L
            {6, 3, 0, 7, 4, 1, 8, 5, 2}  //B
    };
    private char[][] cubetemp = (char[][]) Array.newInstance(Character.TYPE, 6, 9);
    private String[] colour = {"Yellow", "Orange", "Green", "White", "Red", "Blue"};
    static final int RED=0;
    static final int GREEN=1;
    static final int BLUE=2;
    static final int CENTRE_CUBIE_INDEX=4;

    public String getCube(String path, boolean detectionfile) {
        FileOutputStream outStream;
        int[] iArr = new int[3];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        for (int side_index = 0; side_index < 6; side_index++) {
            Bitmap bitmap = BitmapFactory.decodeFile(String.valueOf(path) + this.sides[side_index] + ".jpg", options);
            for (int cubie_index = 0; cubie_index < 9; cubie_index++) {
                int count = 0;
                int r = 0;
                int g = 0;
                int b = 0;
                //Scan through a 25x25 pixel square around the points defined in xstart/ystart
                for (int x = (this.xstart[cubie_index] - 50) / 4; x < (this.xstart[cubie_index] + 50) / 4; x++) {
                    for (int y = (this.ystart[cubie_index] - 50) / 4; y < (this.ystart[cubie_index] + 50) / 4; y++) {
                        //Keep a running tally of how many pixels we have scanned
                        count++;
                        int pixel = bitmap.getPixel(x, y);
                        //Add the RGB values of each pixel to running totals of each.
                        r += Color.red(pixel);
                        g += Color.green(pixel);
                        b += Color.blue(pixel);
                    }
                }
                //After the scan, divide the RGB values by the number of pixels scanned, to get an average.
                //Drop that averaged value into the normalize function (to get 0-100, not 0-255).
                //Slightly more complex than that - the higest colour is set to 100, and the others
                //scaled to fit.  So one colour should ALWAYS be 100.
                int[] rgb = normalize(r / count, g / count, b / count);
                //Then stick the rbg values into the colors array.
                this.colors[side_index][cubie_index][RED] = rgb[RED];
                this.colors[side_index][cubie_index][GREEN] = rgb[GREEN];
                this.colors[side_index][cubie_index][BLUE] = rgb[BLUE];
            }
            //Memory management stuff?
            if (bitmap != null) {
                bitmap.recycle();
            }
            //Finish the loop for this side, get ready to load another jpg.
        }
        //Do the distance calculation.
        //Loop through all cubies, and compare them to the centre of each side.
        //Each cubie should then have a 'distance' to each of the 6 centre cubies.
        for (int side_index = 0; side_index < 6; side_index++) {
            for (int cubie_index = 0; cubie_index < 9; cubie_index++) {
                for (int side_centre_index = 0; side_centre_index < 6; side_centre_index++) {
                    this.distance[side_index][cubie_index][side_centre_index] = getDistance(this.colors[side_index][cubie_index][RED], this.colors[side_index][cubie_index][GREEN], this.colors[side_index][cubie_index][BLUE], this.colors[side_centre_index][CENTRE_CUBIE_INDEX][RED], this.colors[side_centre_index][CENTRE_CUBIE_INDEX][GREEN], this.colors[side_centre_index][CENTRE_CUBIE_INDEX][BLUE]);
                }
            }
        }
        //Grab the centre cubies colours to set the type
        //Log.d("COLOUR:", "starting colour setting");
        for (int side_index = 0; side_index < 6; side_index++) {
            this.colour[side_index] = getColor(this.colors[side_index][CENTRE_CUBIE_INDEX][RED], this.colors[side_index][CENTRE_CUBIE_INDEX][GREEN], this.colors[side_index][CENTRE_CUBIE_INDEX][BLUE]);
            Log.d("COLOUR:", this.colour[side_index]);
        }
        //Log.d("COLOUR:", "completed colour setting");
        //Start the report
        String interim_report = "First Iteration:\r\n";
        //The first iteration just fixes the centre cubies, and then tries to match
        //each cubie to one of the centres.
        //Loop the sides
        for (int side_index = 0; side_index < 6; side_index++) {
            //Add a new line to the report with the side's character
            String text2 = String.valueOf(interim_report) + this.sides[side_index] + "\r\n";
            //Loop through all the cubies on the side.
            for (int cubie_index = 0; cubie_index < 9; cubie_index++) {
                //Add a line to the report each time with the RGB values and an equals sign
                String text3 = String.valueOf(text2) + cubie_index + ": R: " + String.format("%03d", Integer.valueOf(this.colors[side_index][cubie_index][RED])) + ", G: " + String.format("%03d", Integer.valueOf(this.colors[side_index][cubie_index][GREEN])) + ", B: " + String.format("%03d", Integer.valueOf(this.colors[side_index][cubie_index][BLUE])) + " = ";
                //Try to guess what the colour/face is from the data.
                int dist = 300;
                int index = 0;
                //Loop through the distance array for the current cubie.
                for (int side_centre_index = 0; side_centre_index < 6; side_centre_index++) {
                    if (this.distance[side_index][cubie_index][side_centre_index] < dist) {
                        //If there is a better fit (i.e. smaller distance),
                        //then set that as our best guess.
                        dist = this.distance[side_index][cubie_index][side_centre_index];
                        index = side_centre_index;
                    }
                }
                //Name the lowest distanced colour
                text2 = String.valueOf(text3) + this.sides[index] + " (" + this.colour[index] + ")";//\r\n";
                //Add the new line separately to deal with crashes
                text2 = String.valueOf(text2) + "\r\n";
                //Add the face name to the temp cube object.
                this.cubetemp[side_index][cubie_index] = this.sides[index];
            }
            interim_report = String.valueOf(text2) + "\r\n";
        }
        // Count the numbers of each cubie colours
        int ucnt = 0;
        int rcnt = 0;
        int fcnt = 0;
        int dcnt = 0;
        int lcnt = 0;
        int bcnt = 0;
        for (int side_index = 0; side_index < 6; side_index++) {
            for (int cubie_index = 0; cubie_index < 9; cubie_index++) {
                switch (this.cubetemp[side_index][cubie_index]) {
                    case 'B':
                        bcnt++;
                        break;
                    case 'D':
                        dcnt++;
                        break;
                    case 'F':
                        fcnt++;
                        break;
                    case 'L':
                        lcnt++;
                        break;
                    case 'R':
                        rcnt++;
                        break;
                    case 'U':
                        ucnt++;
                        break;
                }
            }
        }
        Log.d("COLOURS:","U:" + ucnt);
        Log.d("COLOURS:","R:" + rcnt);
        Log.d("COLOURS:","F:" + fcnt);
        Log.d("COLOURS:","D:" + dcnt);
        Log.d("COLOURS:","L:" + lcnt);
        Log.d("COLOURS:","B:" + bcnt);
        //if (ucnt != 9 || rcnt != 9 || fcnt != 9 || dcnt != 9 || lcnt != 9 || bcnt != 9) {
            interim_report = String.valueOf(interim_report) + "Second Iteration:\r\n";
            //The second iteration is going to run every cubie through the color detection.
            for (int side_index = 0; side_index < 6; side_index++) {
                String text4 = String.valueOf(interim_report) + this.sides[side_index] + "\r\n";
                for (int cubie_index = 0; cubie_index < 9; cubie_index++) {
                    text4 = String.valueOf(text4) + cubie_index + ": R: " + String.format("%03d", Integer.valueOf(this.colors[side_index][cubie_index][RED])) + ", G: " + String.format("%03d", Integer.valueOf(this.colors[side_index][cubie_index][GREEN])) + ", B: " + String.format("%03d", Integer.valueOf(this.colors[side_index][cubie_index][BLUE])) + " = ";
                    //
                    String guessed_colour = "blank";
                    try{
                        guessed_colour = getColor(this.colors[side_index][cubie_index][RED], this.colors[side_index][cubie_index][GREEN], this.colors[side_index][cubie_index][BLUE]);
                    } catch (Exception e) {
                        Log.d("COLOUR:","getColor failed");
                    }

                    //
                    Boolean matched_centre_cubie = false;
                    for (int side_centre_index = 0; side_centre_index < 6; side_centre_index++) {
                        if (guessed_colour == this.colour[side_centre_index]) {
                            this.cubetemp[side_index][cubie_index] = this.sides[side_centre_index];
                            text4 = String.valueOf(text4) + this.sides[side_centre_index] + " (" + this.colour[side_centre_index] + ")\r\n";
                            matched_centre_cubie = true;
                        }
                    }
                    if (matched_centre_cubie == false) {
                        text4 = String.valueOf(text4) + "\r\n";
                        Log.d("COLOUR:","no colour match found:" + side_index + " - " + cubie_index + " - " + guessed_colour);
                    }
                }
                interim_report = String.valueOf(text4) + "\r\n";
            }
        //}
        //
        // Count the numbers of each cubie colours
        ucnt = 0;
        rcnt = 0;
        fcnt = 0;
        dcnt = 0;
        lcnt = 0;
        bcnt = 0;
        for (int side_index = 0; side_index < 6; side_index++) {
            for (int cubie_index = 0; cubie_index < 9; cubie_index++) {
                switch (this.cubetemp[side_index][cubie_index]) {
                    case 'B':
                        bcnt++;
                        break;
                    case 'D':
                        dcnt++;
                        break;
                    case 'F':
                        fcnt++;
                        break;
                    case 'L':
                        lcnt++;
                        break;
                    case 'R':
                        rcnt++;
                        break;
                    case 'U':
                        ucnt++;
                        break;
                }
            }
        }
        Log.d("COLOURS:","U:" + ucnt);
        Log.d("COLOURS:","R:" + rcnt);
        Log.d("COLOURS:","F:" + fcnt);
        Log.d("COLOURS:","D:" + dcnt);
        Log.d("COLOURS:","L:" + lcnt);
        Log.d("COLOURS:","B:" + bcnt);
        // All colours should now be found.
        String retval = "";
        for (int i7 = 0; i7 < 6; i7++) {
            for (int j6 = 0; j6 < 9; j6++) {
                //appendLog(Integer.toString(i7) + " " + Integer.toString(j6) + " " + retval);
                //Log.d("COLOR", Integer.toString(i7) + " " + Integer.toString(j6) + " " + retval);
                retval = String.valueOf(retval) + this.cubetemp[i7][this.order[i7][j6]];
                /*    java.lang.ArrayIndexOutOfBoundsException: length=8; index=8
        at com.nxtopencube.NxtColorDetection.getCube(NxtColorDetection.java:131)*/
            }
        }
        String final_report = String.valueOf(interim_report) + "Cube Definition String:\r\n";
        for (int i8 = 0; i8 < 6; i8++) {
            final_report = String.valueOf(final_report) + this.sides[i8] + ": " + retval.substring(i8 * 9, (i8 * 9) + 9) + "\r\n";
        }
        if (detectionfile) {
            FileNotFoundException fnf_error;
            IOException io_error;
            try {
                Log.d("COLOR",  final_report);
                outStream = new FileOutputStream(String.valueOf(path) + "detectionfile.txt");
                outStream.write(final_report.getBytes());
                outStream.close();
            } catch (FileNotFoundException e) {
                fnf_error = e;
                fnf_error.printStackTrace();
                return retval;
            } catch (IOException e2) {
                io_error = e2;
                io_error.printStackTrace();
                return retval;
            }
            /*try {

            } catch (FileNotFoundException e3) {
                fnf_error = e3;
                fnf_error.printStackTrace();
                return retval;
            } catch (IOException e4) {
                io_error = e4;
                io_error.printStackTrace();
                return retval;
            }*/
        }
        return retval;
    }

    private String getColor (int r, int g, int b) {
        String retval = "---";
        if (r < 10 && g > 15 && g < 95 && b > 99) { retval = "Blue"; }
        if (r > 50 && g > 99 && b < 30) { retval = "Yellow"; }  //orig b: 10
        if (r < 10 && g > 99 && b < 80) { retval = "Green"; }
        if (r > 99 && g > 10 && b < 33) { retval = "Orange"; }  //orig b: 10
        if (r > 99 && g < 10 && b < 90) { retval = "Red"; }  //orig g: 10
        if (r > 20 && g > 20 && b > 90) { retval = "White"; } //orig b: 99
        return retval;
    }
    /*private String getColor(int r, int g, int b) {
        String retval = "---";
        if (r < 10 && g > 15 && g < 95 && b > 99) {
            retval = "Blue";
        }
        if (r > 50 && g > 99 && b < 30) { //Orig b < 10
            retval = "Yellow";
        }
        if (r < 10 && g > 99 && b < 80) {
            retval = "Green";
        }
        if (r > 99 && g > 10 && b < 30) { //Orig b < 10
            retval = "Orange";
        }
        if (r > 99 && g < 15 && b < 90) { //Orig g < 10
            retval = "Red";
        }
        return (r <= 20 || g <= 20 || b <= 90) ? retval : "White"; //Orig b < 99
    }*/

    private int getDistance(int cubie_red, int cubie_green, int cubie_blue, int centre_red, int centre_green, int centre_blue) {
        //The base distance is simply the total of the absolute differences in each of the colour channels.
        int retval = Math.abs(cubie_red - centre_red) + Math.abs(cubie_green - centre_green) + Math.abs(cubie_blue - centre_blue);
        //if the red values match, half the distance
        if (cubie_red == centre_red) {
            retval /= 2;
        }
        //same for the greens
        if (cubie_green == centre_green) {
            retval /= 2;
        }
        //and the blues
        if (cubie_blue == centre_blue) {
            retval /= 2;
        }
        //If the centre values are Zero, and the cubie value is more than 5, then double the distance.
        if (centre_red == 0 && cubie_red > 5) {
            retval *= 2;
        }
        if (centre_green == 0 && cubie_green > 5) {
            retval *= 2;
        }
        if (centre_blue == 0 && cubie_blue > 5) {
            retval *= 2;
        }
        //If the centre values are 100, and the cubie value is less than 95, then double the distance.
        if (centre_red == 100 && cubie_red < 95) {
            retval *= 2;
        }
        if (centre_green == 100 && cubie_green < 95) {
            retval *= 2;
        }
        if (centre_blue != 100 || cubie_blue >= 95) {
            return retval;
        }
        return retval * 2;
        //return (centre_blue != 100 || cubie_blue >= 95) ? retval : retval * 2;
    }

    private int[] normalize(int r, int g, int b) {
        int[] retval = new int[3];
        //Work with doubles throughout for greater accuracy?
        double rd = 0.0d;
        double gd = 0.0d;
        double bd = 0.0d;
        //If all RGB are 0, then return zeros.
        if (r == 0 && g == 0 && b == 0) {
            rd = 0.0d;
            gd = 0.0d;
            bd = 0.0d;
        } else {
            //Set the highest colour to 100, and scale the other two to fit.
            if (r >= g && r >= b) {
                rd = 100.0d;
                gd = (g * 100) / r;
                bd = (b * 100) / r;
            }
            if (g >= r && g >= b) {
                gd = 100.0d;
                rd = (r * 100) / g;
                bd = (b * 100) / g;
            }
            if (b >= r && b >= g) {
                bd = 100.0d;
                rd = (r * 100) / b;
                gd = (g * 100) / b;
            }
        }
        //Return an array of the values converted back to INTs
        retval[0] = (int) rd;
        retval[1] = (int) gd;
        retval[2] = (int) bd;
        return retval;
    }

    public void appendLog(String text)
    {
        File logFile = new File("sdcard/log.txt");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
