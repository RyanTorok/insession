package gui;

import terminal.Address;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

public class ZipMap implements Serializable {

    static final long serialVersionUID = 44L;

    HashMap<Integer, LatLon> map;

    public LatLon get(Integer zip) {
        if (map == null)
            map = read().map;
        return map.get(zip);
    }

    public static void main(String args[]) {
        ZipMap z = new ZipMap();
        z.create();
        z.write();
    }

    private void write() {
        File f = new File(Address.root_addr + File.separator + "resources" + File.separator + "zip.ser");
        try {
            new ObjectOutputStream(new FileOutputStream(f)).writeObject(this);
        } catch (IOException e) {
        }
    }

    static ZipMap read() {
        File f = new File(Address.root_addr + File.separator + "resources" + File.separator + "zip.ser");
        try {
            return (ZipMap) new ObjectInputStream(new FileInputStream(f)).readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void create() {
       File f = new File(Address.root_addr + File.separator + "resources" + File.separator + "zip.txt");
        try {
            Scanner s = new Scanner(f);
            map = new HashMap<>();
            while (s.hasNextLine()) {
                String[] mapping = s.nextLine().split(",");
                map.put(Integer.parseInt(mapping[0]), new LatLon(Double.parseDouble(mapping[1]), Double.parseDouble(mapping[2])));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static class LatLon implements Serializable{

        static final long serialVersionUID = 45L;

        public LatLon(Double lat, Double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        Double lat;
        Double lon;

        public Double getLat() {
           return lat;
        }

        public Double getLon() {
            return lon;
        }

    }
}
