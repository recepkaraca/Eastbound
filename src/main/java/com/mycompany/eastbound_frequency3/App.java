/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.eastbound_frequency3;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

/**
 *
 * @author haticezeren
 */
public class App {

    static final File databaseDirectory = new File("D:\\\\Neo4j_Store\\\\neo4jDatabases\\\\database-42a4c9a7-851a-4405-9506-b7b0402114a4\\\\installation-3.4.1\\\\data\\\\databases\\\\graph.db");
    GraphDatabaseService db;
    ArrayList<String> iliskiler;
//    ArrayList<String> karaListe;
    ArrayList<String[]> gidilenYol;
    HashSet<String> randomIliskiler;
    ArrayList<String[]> ziyaretEdilenler;
    HashSet<String> ArabaListesi;
    HashSet<ArrayList<String[]>> karaListe;
    ArrayList<ArrayList<String[]>> yollar;
    HashSet<String> kuralArabaListesi;
    int toplamDenenenYol = 0;

    public App(GraphDatabaseService db) {
        this.db = db;
        iliskiler = new ArrayList<String>();
//        karaListe = new ArrayList<>();
        gidilenYol = new ArrayList<>();
        ziyaretEdilenler = new ArrayList<String[]>();
        ArabaListesi = new HashSet<String>();
        randomIliskiler = new HashSet<>();
        kuralArabaListesi = new HashSet<String>();
        karaListe = new HashSet<>();
        yollar = new ArrayList<>();
        yollar.add(new ArrayList<>());
        yollar.add(new ArrayList<>());
        yollar.add(new ArrayList<>());
    }

    public int toplamDoguAracSayisi(HashSet<String[]> ziyaretEdilenler) throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:neo4j:bolt://localhost:7678");
        int toplamDoguAracSayisi = 0;
        String sql = "MATCH (a:Car)-[:Direction]-(b:Direction {value: 'east'}) RETURN count(distinct(a)) as veri";
        try (Statement stmt = con.createStatement()) {
            ResultSet toplamEast = stmt.executeQuery(sql);
            while (toplamEast.next()) {
                toplamDoguAracSayisi = Integer.parseInt(toplamEast.getString("veri"));
            }
        } catch (SQLException exp) {
            exp.printStackTrace();

        }
        // System.out.println("toplamdoguarac:");
        // System.out.println("toplamDoguAracSayisi :" + sql);
        return toplamDoguAracSayisi;
    }

    private int eastWestAracBul(HashSet<String[]> ziyaretEdilenler) throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:neo4j:bolt://localhost:7678");
        // System.out.println("eastwestAracBul");
        // yazZiyaretEdilenler(ziyaretEdilenler);
        String query = "";
        int eastWestArac = 0;
        try (Statement stmt = con.createStatement()) {
            for (String[] silinecekAracSorgu : ziyaretEdilenler) {
                query += " MATCH (a:Car)-[:" + silinecekAracSorgu[0] + "{name:'" + silinecekAracSorgu[0] + "'}]-(:"
                        + silinecekAracSorgu[0] + " {value: '" + silinecekAracSorgu[1] + "'})";
            }
            query += " RETURN distinct(a.model) as model";
            if (ziyaretEdilenler.size() != 0) {
                ResultSet silinecekAraclar = stmt.executeQuery(query);
                while (silinecekAraclar.next()) {
                    eastWestArac++;
                }
            }
        } catch (SQLException exp) {
            exp.printStackTrace();

        }
        // System.out.println("eastWestSayi: " + query);
        return eastWestArac;
    }

    public boolean kural1(int modelDoguAracSayisi, int toplamDoguAracSayisi, int doguBatıAracSayisi) {
        if ((double) modelDoguAracSayisi / (double) toplamDoguAracSayisi > 0.1
                && (double) modelDoguAracSayisi / (double) doguBatıAracSayisi > 0.6) {
            return true;
        }

        return false;
    }

    public void yazZiyaretEdilenler(HashSet<String[]> deneme) {
        // System.out.println("Ziyaret Edilenler:");
        for (String[] liste : deneme) {
            System.out.print("(" + liste[0] + "){" + liste[1] + "}->");
        }
        System.out.print("...");
    }

    public void yazListe() {
        System.out.println("Etiketler:");
        for (String liste : iliskiler) {
            System.out.println(liste);
        }
    }

    public HashSet<String> kuralArabaListesi(ArrayList<String[]> ziyaretEdilenler) throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:neo4j:bolt://localhost:7678");
        HashSet<String> kuralArabaListesi = new HashSet<>();
        String query = "";

        try (Statement stmt = con.createStatement()) {

            for (String[] liste : ziyaretEdilenler) {
                query += " MATCH (a:Car)-[:" + liste[0] + "{name:'" + liste[0] + "'}]-(:" + liste[0] + " {value: '"
                        + liste[1] + "'})";
            }
            query += " MATCH (a)-[:Direction]-(b:Direction {value: 'east'}) RETURN distinct(a.model) as model";

            // System.out.println(query);
            ResultSet Araclar = stmt.executeQuery(query);
//            System.out.println("Kuralın açıkladığı arabalar: ");
            while (Araclar.next()) {
//                System.out.println(Araclar.getString("model"));
                kuralArabaListesi.add(Araclar.getString("model"));
            }

        }

        con.close();
        return kuralArabaListesi;

    }

    public boolean arabaListesiAyniMi(HashSet<String> kuralArabaListesi) {

        System.out.println("kuralArabaListesi:" + kuralArabaListesi.size());
        System.out.println("ArabaListesi:" + ArabaListesi.size());
        if (ArabaListesi.isEmpty()) {
            return false;
        }
        for (String liste : kuralArabaListesi) {
            if (!ArabaListesi.contains(liste)) {
                return false;
            }
        }

        return true;
    }

    public void arabaListesineEkle(HashSet<String> kuralArabaListesi) {

        for (String liste : kuralArabaListesi) {
            ArabaListesi.add(liste);
        }
    }

    public void diziKaristir() {

        Collections.shuffle(iliskiler);

        for (int i = 0; i < iliskiler.size(); i++) {
            System.out.println(iliskiler.get(i));
        }

    }

    public String[] etiketVeDegerDondur() throws SQLException {
        String[] iliskiDegerler = new String[2];
        Connection con = DriverManager.getConnection("jdbc:neo4j:bolt://localhost:7678");
        ResultSet rsDeger;
        String query = "";
        double max = 0.0;
        double eastCurrentNumber = 0.0;
        double currentSum = 0.0;
        double eastSumNumber = 0.0;
        double sumOfCar = 30.0;
        try (Statement stmt = con.createStatement()) {
            for (String iliski : iliskiler) {
                if (gidilenYol.contains(iliski)) {
                    continue;
                }
                query = "";
                for (String[] gidilen : gidilenYol) {
                    query += "MATCH (a:Car)-[b:" + gidilen[0] + "]-(c:" + gidilen[0] + " {value: '" + gidilen[1] + "'}) ";
                }
                query += "MATCH (a:Car)-[b:" + iliski + "]-(c) return distinct(c.value) as deger, count(a) as currentSum";
                rsDeger = stmt.executeQuery(query);
                while (rsDeger.next()) {
                    String[] gecici = new String[2];
                    gecici[0] = iliski;
                    gecici[1] = rsDeger.getString("deger");
                    query = "";
                    for (String[] gidilen : gidilenYol) {
                        query += "MATCH (a:Car)-[b:" + gidilen[0] + "]-(c:" + gidilen[0] + " {value: '" + gidilen[1] + "'}) ";
                    }
                    query += "MATCH (a:Car)-[b:" + iliski + "]-(c:" + iliski + " {value: '" + rsDeger.getString("deger") + "'})"
                            + " MATCH (a)-[:Direction]-(:Direction {value: 'east'}) return distinct(count (a))";
                    ResultSet toplamlar = stmt.executeQuery(query);
                    while (toplamlar.next()) {
                        eastCurrentNumber++;
                    }
                    query = "";
                    for (String[] gidilen : gidilenYol) {
                        query += "MATCH (a:Car)-[b:" + gidilen[0] + "]-(c:" + gidilen[0] + " {value: '" + gidilen[1] + "'}) ";
                    }
                    query += "MATCH (a:Car)-[b:" + iliski + "]-(c)"
                            + " MATCH (a)-[:Direction]-(:Direction {value: 'east'}) return distinct(count (a))";
                    toplamlar = stmt.executeQuery(query);
                    while (toplamlar.next()) {
                        eastSumNumber++;
                    }
                    System.out.println("East current: " + eastCurrentNumber);
                    System.out.println("East sum number: " + eastSumNumber);
                    System.out.println("Current Sum: " + currentSum);
                    System.out.println("Sum of car: " + sumOfCar);
                    currentSum = Double.parseDouble(rsDeger.getString("currentSum"));
                    if ((eastCurrentNumber / eastSumNumber) / (currentSum / sumOfCar) > max) {
                        max = (eastCurrentNumber / eastSumNumber) / (currentSum / sumOfCar);
                        iliskiDegerler[0] = iliski;
                        iliskiDegerler[1] = rsDeger.getString("deger");
                    }
                }
            }
        }
        con.close();
        System.out.println(iliskiDegerler[0] + "****************" + iliskiDegerler[1]);
        return iliskiDegerler;
    }

    public ArrayList<String[]> etiketDegerDondur(ArrayList<String[]> gelenYol) throws SQLException {
        HashSet<String> kuralArabaListesi = new HashSet<String>();

        Connection con = DriverManager.getConnection("jdbc:neo4j:bolt://localhost:7678");
        ResultSet rsDeger;
        String[] iliskiDegerler = new String[3];
        String query = "";
        double max = 0.0;
        double eastCurrentNumber = 0.0;
        double currentSum = 0.0;
        double eastSumNumber = 0.0;
        double sumOfCar = 30.0;
        double supportValue = 0.0;
        double confidenceValue = 0.0;
        double modelEast = 0.0;
        double eastWestArac = 0.0;
        double toplamEastArac = 0.0;

        for (String deneme : iliskiler) {
//            System.out.println("İlişkiler: " + deneme);
        }
        try (Statement stmt = con.createStatement()) {
            iliskiler.remove("Direction");
            for (String iliski : iliskiler) {

//                System.out.println("Şuanki ilişki etiket: " + iliski);
                query = "";
                for (String[] gidilen : ziyaretEdilenler) {
//                    System.out.println("ZiyaretEdilenler[0] : " + gidilen[0] + "," + gidilen[1]);
                    query += "MATCH (a:Car)-[:" + gidilen[0] + "]-(:" + gidilen[0] + " {value: '" + gidilen[1] + "'}) ";
                }
                query += "MATCH (a:Car)-[b:" + iliski + "]-(c) RETURN distinct(c.value) as deger, count(a) as currentSum";
//                System.out.println("İlişki değer sorgu" + query);
                ResultSet rsValue = stmt.executeQuery(query);
                while (rsValue.next()) {
                    boolean atlaFlag = false;
                    if (!gelenYol.isEmpty()) {
//                        System.out.println("Gelen Yol: *******************-----------" + gelenYol.get(0)[0]);
                    }
                    for (String[] gelenYolVarMi : gelenYol) {
                        if (gelenYolVarMi[0].equals(iliski) && gelenYolVarMi[1].equals(rsValue.getString("deger"))) {
                            atlaFlag = true;
                        }
                    }
                    for (String[] ziyaretEdilenVarMi : ziyaretEdilenler) {
                        if (ziyaretEdilenVarMi[0].equals(iliski) && ziyaretEdilenVarMi[1].equals(rsValue.getString("deger"))) {
                            atlaFlag = true;
                        }
                    }
                    if (atlaFlag) {
                        continue;
                    }
//                    System.out.println("Şuanki ilişki değer: " + rsValue.getString("deger"));
                    query = "";
                    for (String[] gidilen : ziyaretEdilenler) {
                        query += "MATCH (a:Car)-[:" + gidilen[0] + "]-(:" + gidilen[0] + " {value: '" + gidilen[1] + "'}) ";
                    }
                    query += "MATCH (a:Car)-[b:" + iliski + "]-(c:" + iliski + " {value: '" + rsValue.getString("deger") + "'})"
                            + " MATCH (a)-[:Direction]-(:Direction {value: 'east'}) return distinct(count (a)) as eastCurrentNumber";
//                    System.out.println(query);
                    ResultSet toplamlar = stmt.executeQuery(query);
                    while (toplamlar.next()) {
                        eastCurrentNumber = Double.parseDouble(toplamlar.getString("eastCurrentNumber"));
                    }
                    query = "";
                    for (String[] gidilen : ziyaretEdilenler) {
                        query += "MATCH (a:Car)-[:" + gidilen[0] + "]-(:" + gidilen[0] + ")";
                    }
                    query += "MATCH (a:Car)-[b:" + iliski + "]-(c:" + iliski + ")"
                            + " MATCH (a)-[:Direction]-(:Direction {value: 'east'}) return distinct(count (a)) as eastSumNumber";
//                    System.out.println(query);
                    toplamlar = stmt.executeQuery(query);
                    while (toplamlar.next()) {
                        eastSumNumber = Double.parseDouble(toplamlar.getString("eastSumNumber"));
                    }
                    currentSum = Double.parseDouble(rsValue.getString("currentSum"));
                    query = "";
                    for (String[] gidilen : ziyaretEdilenler) {
                        query += "MATCH (a:Car)-[:" + gidilen[0] + "]-(:" + gidilen[0] + " {value: '" + gidilen[1] + "'}) ";
                    }
                    query += " MATCH (a)-[:" + iliski + "]-(:" + iliski + " {value: '" + rsValue.getString("deger") + "'})"
                            + " MATCH (a)-[:Direction]-(:Direction {value: 'east'}) return distinct(count(a)) as modelEast";
//                    System.out.println(query);
                    ResultSet confidenceSupport = stmt.executeQuery(query);
                    while (confidenceSupport.next()) {
                        modelEast = Double.parseDouble(confidenceSupport.getString("modelEast"));
                    }
                    query = "";
                    for (String[] gidilen : ziyaretEdilenler) {
                        query += "MATCH (a:Car)-[:" + gidilen[0] + "]-(:" + gidilen[0] + " {value: '" + gidilen[1] + "'}) ";
                    }
                    query += " MATCH (a)-[:" + iliski + "]-(:" + iliski + " {value: '" + rsValue.getString("deger") + "'})"
                            + "MATCH (a)-[:Direction]-(:Direction) return distinct(count(a)) as eastWestArac";
//                    System.out.println(query);
                    confidenceSupport = stmt.executeQuery(query);
                    while (confidenceSupport.next()) {
                        eastWestArac = Double.parseDouble(confidenceSupport.getString("eastWestArac"));
                    }
                    query = "MATCH (a)-[:Direction]-(:Direction {value: 'east'}) return distinct(count(a)) as toplamEast";
//                    System.out.println(query);
                    confidenceSupport = stmt.executeQuery(query);
                    while (confidenceSupport.next()) {
                        toplamEastArac = Double.parseDouble(confidenceSupport.getString("toplamEast"));
                    }
//                    System.out.println("East Current Number: " + eastCurrentNumber);
//                    System.out.println("East Sum Number: " + eastSumNumber);
//                    System.out.println("Current Sum: " + currentSum);
//                    System.out.println("Model East " + modelEast);
//                    System.out.println("Toplam East Arac " + toplamEastArac);
//                    System.out.println("East West Arac " + eastWestArac);
                    if ((double) modelEast / toplamEastArac >= 0.1) {
//                        System.out.println("Güven değeri: " + (double) modelEast / eastWestArac);
//                        System.out.println("Support: " + modelEast / toplamEastArac);

                        if ((eastCurrentNumber / eastSumNumber) / (currentSum / sumOfCar) > max) {
                            max = (eastCurrentNumber / eastSumNumber) / (currentSum / sumOfCar);
                            iliskiDegerler[0] = iliski;
                            iliskiDegerler[1] = rsValue.getString("deger");
                            iliskiDegerler[2] = Double.toString(max);
//                            System.out.println("Max: " + max);
                        }
                    }
                }
            }
//            System.out.println(" ---------------- BURASI");
            gelenYol.add(iliskiDegerler);

            if (max != 0.0 && iliskiDegerler[0] != null && iliskiDegerler[1] != null) {
//                System.out.println("Alınan ******************** : " + iliskiDegerler[0] + "," + iliskiDegerler[1]);
                etiketDegerDondur(gelenYol);
            }

            sayac++;
        }
        con.close();
        return gelenYol;
    }

    public boolean kuralMi(ArrayList<String[]> etiketDeger) throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:neo4j:bolt://localhost:7678");
        double modelEast = 0.0;
        double eastWestArac = 0.0;
        double toplamEastArac = 0.0;
        boolean kuralMi = false;
        HashSet<String> suankiArabaListesi = new HashSet<>();
        try (Statement stmt = con.createStatement()) {
            String query = "";
            for (String[] gidilen : etiketDeger) {
                query += "MATCH (a:Car)-[:" + gidilen[0] + "]-(:" + gidilen[0] + " {value: '" + gidilen[1] + "'}) ";
            }
            query += "MATCH (a)-[:Direction]-(:Direction {value: 'east'}) return distinct(count(a)) as modelEast";
//            System.out.println(query);
            ResultSet confidenceSupport = stmt.executeQuery(query);
            while (confidenceSupport.next()) {
                modelEast = Double.parseDouble(confidenceSupport.getString("modelEast"));
            }
            query = "";
            for (String[] gidilen : etiketDeger) {
                query += "MATCH (a:Car)-[:" + gidilen[0] + "]-(:" + gidilen[0] + " {value: '" + gidilen[1] + "'}) ";
            }
            query += "MATCH (a)-[:Direction]-(:Direction) return distinct(count(a)) as eastWestArac";
//            System.out.println(query);
            confidenceSupport = stmt.executeQuery(query);
            while (confidenceSupport.next()) {
                eastWestArac = Double.parseDouble(confidenceSupport.getString("eastWestArac"));
            }
            query = "MATCH (a)-[:Direction]-(:Direction {value: 'east'}) return distinct(count(a)) as toplamEast";
//            System.out.println(query);
            confidenceSupport = stmt.executeQuery(query);
            while (confidenceSupport.next()) {
                toplamEastArac = Double.parseDouble(confidenceSupport.getString("toplamEast"));
            }

//            System.out.println("Model East " + modelEast);
//            System.out.println("Toplam East Arac " + toplamEastArac);
//            System.out.println("East West Arac " + eastWestArac);
            if ((double) modelEast / toplamEastArac >= 0.3 && (double) modelEast / eastWestArac >= 0.6) {
                suankiArabaListesi = kuralArabaListesi(ziyaretEdilenler);
                if (!arabaListesiAyniMi(suankiArabaListesi)) {
                    System.out.println("Güven değeri: " + (double) modelEast / eastWestArac);
                    System.out.println("Support: " + modelEast / toplamEastArac);
                    kuralMi = true;
                }
            } else {
                kuralMi = false;
            }
        }
        for (String[] gidilen : etiketDeger) {
//            System.out.print("=========Denenen etiket: " + gidilen[0] + ", Değeri: " + gidilen[1]);
        }
        if (kuralMi) {
//            System.out.println("Geçti.");
        } else {
//            System.out.println("Kaldı.");
        }
        con.close();
        return kuralMi;
    }

    int sayac = 0;

    public void kuralBul() throws SQLException {
        // ziyaretEdilenler.clear();
        // System.out.println("kural uzunluğu: " + kuralUzunlugu +
        // "////////////////////////////");

        ArrayList<String[]> seviye0 = new ArrayList<>();

        Connection con = DriverManager.getConnection("jdbc:neo4j:bolt://localhost:7678");
        seviye0 = etiketDegerDondur(seviye0);
        for (String[] seviye1Alinan : seviye0) {
//            System.out.println("Seviye1: " + seviye1Alinan[0] + ", " + seviye1Alinan[1]);
        }
        for (String[] seviye0Alinan : seviye0) {
            toplamDenenenYol++;
            if (ArabaListesi.size() != 17) {
                ziyaretEdilenler.clear();
                ziyaretEdilenler.add(seviye0Alinan);
                if (!kuralMi(ziyaretEdilenler)) {
//                for(String[] aktarilacak : seviye0){
//                    seviye1.add(aktarilacak);
//                }
////                seviye1.remove(seviye0Alinan);
                    System.out.println("++++++++++++++++++++Kural olmayan: " + ziyaretEdilenler.get(0)[0] + "," + ziyaretEdilenler.get(0)[1]);
                    ArrayList<String[]> seviye1 = new ArrayList<>();
                    seviye1 = etiketDegerDondur(seviye1);
                    for (String[] seviye1Alinan : seviye1) {
//                        System.out.println("Seviye1: " + seviye1Alinan[0] + ", " + seviye1Alinan[1]);
                    }
                    for (String[] seviye1Alinan : seviye1) {
                        toplamDenenenYol++;
                        ziyaretEdilenler.add(seviye1Alinan);
                        if (!kuralMi(ziyaretEdilenler)) {
//                            ziyaretEdilenler.remove(ziyaretEdilenler.size()-1);
                            ArrayList<String[]> seviye2 = new ArrayList<>();
                            seviye2 = etiketDegerDondur(seviye2);
                            for (String[] seviye2Alinan : seviye2) {
                                toplamDenenenYol++;
                                ziyaretEdilenler.add(seviye2Alinan);
                                if (!kuralMi(ziyaretEdilenler)) {
                                    ziyaretEdilenler.remove(ziyaretEdilenler.size() - 1);
                                } else {
                                    for (String[] kural : ziyaretEdilenler) {
                                        System.out.print("///////////////////Kural3 : " + kural[0] + " - Değer : " + kural[1] + " - Frequency " + kural[2] + ",");
                                    }
                                    System.out.println("");
                                    kuralArabaListesi = kuralArabaListesi(ziyaretEdilenler);
                                    arabaListesineEkle(kuralArabaListesi);
                                    break;
                                }
                            }
                            ziyaretEdilenler.remove(ziyaretEdilenler.size()-1);
                        } else {
                            for (String[] kural : ziyaretEdilenler) {
                                System.out.print("///////////////////Kural2 : " + kural[0] + " - Değer : " + kural[1] + " - Frequency " + kural[2] + ",");
                            }
                            System.out.println("");
                            kuralArabaListesi = kuralArabaListesi(ziyaretEdilenler);
                            arabaListesineEkle(kuralArabaListesi);
                            break;
                        }
                    }
                } else {
                    for (String[] kural : ziyaretEdilenler) {
                        kuralArabaListesi = kuralArabaListesi(ziyaretEdilenler);
                        arabaListesineEkle(kuralArabaListesi);
                        System.out.println("///////////////////Kural1 : " + kural[0] + " - Değer : " + kural[1] + " - Frequency " + kural[2]);
                    }

                }
            }
        }
    }

    public static void main(String[] Args) throws SQLException {
        
        GraphDatabaseSettings.BoltConnector bolt = GraphDatabaseSettings.boltConnector("0");
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(databaseDirectory)
                .setConfig(bolt.type, "BOLT").setConfig(bolt.enabled, "true").setConfig(bolt.address, "localhost:7678")
                .newGraphDatabase();
        final long startTime = System.currentTimeMillis();
        App op = new App(db);

        db.execute("MATCH (n) OPTIONAL MATCH (n)-[r]-() WITH n,r DELETE n,r");
        op.createGraph();

        HashSet<String> kuralIlkEtiketler = new HashSet<>();
        boolean aracKaldiMi = true;
        int aracSayisi = 0;
        op.ziyaretEdilenler.clear();
        op.Liste(null);
        op.listeEtiketSil();
        System.out.println("Başladı.");
        int sayac = 0;
        op.yazListe();
        System.out.println("................................................");
        System.out.println("................................................");
        while (op.ArabaListesi.size() != 17) {
            op.kuralBul();
        }

        System.out.println("*************************************************************************");
        System.out.println("Bulunan Kuralların Açıkladığı Arabaların Listesi");
        for (String liste : op.ArabaListesi) {
            System.out.println(liste);
            aracSayisi++;
        }
        System.out.println("Açıklanan arac sayısı:" + aracSayisi);
        aracSayisi = 0;
        System.out.println("*************************************************************************");
        op.shutdownGraph();
        final long endTime = System.currentTimeMillis();
        System.out.println("Toplam denenen kurallar: " + op.toplamDenenenYol);
        System.out.println("Çalışma zamanı (saniye) : " + (endTime - startTime)/1000);
    }

    private int modelDoguAracSayisi(HashSet<String[]> ziyaretEdilenler) throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:neo4j:bolt://localhost:7678");
        int modelDoguAracSayisi = 0;
        String query = "";

        try (Statement stmt = con.createStatement()) {
            for (String[] liste : ziyaretEdilenler) {
                query += " MATCH (a:Car)-[:" + liste[0] + "{name:'" + liste[0] + "'}]-(:" + liste[0] + " {value: '"
                        + liste[1] + "'})";
                // System.out.println("--" + liste[0] + " deger:" + liste[1]);
            }

            query += " MATCH (a)-[c]-() MATCH (a)-[:Direction]-(f:Direction {value: 'east'}) RETURN distinct(a.model) as model";

            ResultSet sonuc = stmt.executeQuery(query);
            String queryDelete = "";
            while (sonuc.next()) {
                modelDoguAracSayisi++;
            }
        } catch (SQLException exp) {
            exp.printStackTrace();

        }
        // System.out.println("modelDoguSayi: " + query);
        return modelDoguAracSayisi;
    }

    public String rastgeleEtiketOzellikDegeri(String etiketDeger) throws SQLException {
        ArrayList<String> etiketDegerleriList = new ArrayList<String>();
        // System.out.println("etiket:" + etiketDeger);
        if (!etiketDeger.equalsIgnoreCase("direction")) {
            Connection con = DriverManager.getConnection("jdbc:neo4j:bolt://localhost:7678");
            try (Statement stmt = con.createStatement()) {
                String sql = "MATCH (n:Car)-[r:" + etiketDeger + "]->(b) return distinct(b.value) as value";
                ResultSet rsRelValues = stmt.executeQuery(sql);
                // System.out.println("sql: " + sql);
                while (rsRelValues.next()) {
                    String deger = rsRelValues.getString("value");
                    if (!etiketDegerleriList.contains(deger)) {
                        etiketDegerleriList.add(deger);
                        // System.out.println(deger);
                    }

                }
            } catch (SQLException exp) {
                exp.printStackTrace();
            }
            con.close();

        }

        // for (int i = 0; i < etiketDegerleriList.size(); i++) {
        // System.out.println(etiketDegerleriList.get(i));
        // }
        // System.out.println("-------");
        Random rand = new Random();
        // System.out.println("dizi Liste boyut:" + etiketDegerleriList.size());

        int n = rand.nextInt(etiketDegerleriList.size()) + 0;
        if (n == etiketDegerleriList.size()) {
            n = n - 1;
        }
        // System.out.println("random sayı:" + (n));
        // System.out.println(etiketDegerleriList.get(n));
        return etiketDegerleriList.get(n);

    }

    public void listeEtiketSil() {

        for (String[] liste : ziyaretEdilenler) {
            if (iliskiler.contains(liste[0])) {
                iliskiler.remove(liste[0]); //
                // System.out.println(liste[0] + "elemani Silindi.");
            }
        }
        iliskiler.remove("Direction");

    }

    public void Liste(String etiket) throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:neo4j:bolt://localhost:7678");
        ResultSet rsRel;
        String query = "";
        iliskiler.clear();
        try (Statement stmt = con.createStatement()) {
            if (etiket == null) {
                rsRel = stmt.executeQuery("MATCH (:Car)-[r]-() RETURN distinct(r.name) as rel");
            } else {
                for (String[] liste : ziyaretEdilenler) {
                    query += " MATCH (a:Car)-[:" + liste[0] + "{name:'" + liste[0] + "'}]-()";
                    // System.out.println("--" + liste[0] + " deger:" +
                    // liste[1]);
                }
                query += " MATCH (a)-[c]-() RETURN distinct(c.name) as rel";
                // System.out.println("Liste sorgu: " + query);
                rsRel = stmt.executeQuery(query);
            }

            while (rsRel.next()) {
                if (!iliskiler.contains(rsRel.getString("rel"))) {
                    iliskiler.add(rsRel.getString("rel"));
                }

            }
        } catch (SQLException exp) {
            exp.printStackTrace();

        }
        iliskiler.remove("Direction");
        con.close();
    }

    public void shutdownGraph() {
        try {
            if (db != null) {
                db.shutdown();
            }
        } finally {
            db = null;
        }
    }

    private void createGraph() {

        try (Transaction tx = db.beginTx()) {
            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\car.csv' AS line"
                    + " MERGE (a:Car {model: line.name}) ");

            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\closed.csv' AS line"
                    + " MATCH (b:Car {model: line.name}) MERGE (a:Closed {value:'Closed'})"
                    + " MERGE (b)-[:Closed{name:'Closed'}]->(a)");

            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\has_car.csv' AS line"
                    + " MATCH (a:Car {model: line.name2}) MERGE (b:Direction {value: line.name1})"
                    + " MERGE (a)-[:Direction{name:'Direction'}]->(b)");

            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\jagged.csv' AS line"
                    + " MATCH (a:Car {model: line.name}) MERGE (b:Jagged {value:'Jagged'})"
                    + " MERGE (a)-[:Jagged{name:'Jagged'}]->(b)");

            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\load.csv' AS line"
                    + " MATCH (a:Car {model: line.name1}) MERGE (b:Load {value: line.name2})"
                    + " MERGE (a)-[:Load{name:'Load'}]->(b)");

            db.execute("LOAD CSV WITH HEADERS FROM  'file:///D:\\\\eastbound_csv\\\\open_car.csv' AS line"
                    + " MATCH (a:Car {model: line.name}) MERGE (b:Open_Car {value:'Open_Car'})"
                    + " MERGE (a)-[:Open_Car{name:'Open_Car'}]-(b)");

            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\shape_car.csv' AS line"
                    + " MATCH (a:Car {model: line.name1}) MERGE (b:Shape {value:line.name2})"
                    + " MERGE (a)-[:Shape{name:'Shape'}]->(b)");

            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\tr_double.csv' AS line"
                    + " MATCH (a:Car {model: line.name}) MERGE (b:tr_double {value:'tr_double'})"
                    + " MERGE (a)-[:tr_double{name:'tr_double'}]->(b)");

            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\tr_long.csv' AS line"
                    + " MATCH (a:Car {model: line.name}) MERGE (b:Lenght {value:'tr_long'})"
                    + " MERGE (a)-[:Lenght{name:'Lenght'}]->(b)");
            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\tr_short.csv' AS line"
                    + " MATCH (a:Car {model: line.name}) MERGE (b:Lenght {value:'tr_short'})"
                    + " MERGE (a)-[:Lenght{name:'Lenght'}]->(b)");
            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\wheels.csv' AS line"
                    + " MATCH (a:Car {model: line.name1}) MERGE (b:Wheels {value:line.name2})"
                    + " MERGE (a)-[:Wheels{name:'Wheels'}]->(b)");

            tx.success();
        }
    }

}
