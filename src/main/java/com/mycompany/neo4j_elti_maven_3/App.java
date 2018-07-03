package com.mycompany.neo4j_elti_maven_3;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

public class App {

    HashSet<String[]> ziyaretEdilenler;
    HashSet<String> iliskiler;
    Double max;
    String etiketDeger;
    double toplamEastAracSayisi;

    static final File databaseDirectory = new File("D:\\Neo4j_Store\\neo4jDatabases\\database-42a4c9a7-851a-4405-9506-b7b0402114a4\\installation-3.4.1\\data\\databases\\graph.db");
    GraphDatabaseService db;

    public App(GraphDatabaseService db) {
        this.db = db;
        ziyaretEdilenler = new HashSet<String[]>();
        iliskiler = new HashSet<String>();
        etiketDeger = null;
        max = 0.0;
    }

    public static void main(String[] args) throws SQLException {
        GraphDatabaseSettings.BoltConnector bolt = GraphDatabaseSettings.boltConnector("0");
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(databaseDirectory)
                .setConfig(bolt.type, "BOLT").setConfig(bolt.enabled, "true").setConfig(bolt.address, "localhost:7678")
                .newGraphDatabase();

        App op = new App(db);
        double modelEastAracSayisi = 0.0;
        double toplamArac = 1.0;

        db.execute("MATCH (n) OPTIONAL MATCH (n)-[r]-() WITH n,r DELETE n,r");
        op.createGraph();

        // ----------------------
        
        
        System.out.println(op.iliskiler.size());

        int aracSayisiCounter = 0;

        while (toplamArac != 0.0) {
            op.ziyaretEdilenler.clear();
            String etiket = null;
            op.Liste(etiket);
            for (int i = 0; i < op.iliskiler.size(); i++) {
                op.listeEtiketSil(etiket);
                for (String liste : op.iliskiler) {
                    System.out.println(liste);
                }
                etiket = op.calculations();
                modelEastAracSayisi = op.calcSupport();
                System.out.println("Model East Arac: " + modelEastAracSayisi);
                System.out.println("Toplam East Arac: " + op.toplamEastAracSayisi);
                if (op.max == 0.0 || modelEastAracSayisi < op.toplamEastAracSayisi / 10) {
                    break;
                }
                String[] deger = new String[2];
                deger[0] = etiket;
                deger[1] = op.etiketDeger;
                op.ziyaretEdilenler.add(deger);
                op.ziyaretEdilenler.remove("");
                op.Liste(etiket);
                if (op.max < 0.6) {
                    break;
                }
                System.out.println("Bulunan etiket: " + etiket);
            }
            if (aracSayisiCounter == 0) {
                toplamArac = op.toplamEastAracSayisi;
            } else {
                toplamArac -= modelEastAracSayisi;
            }
            op.bulunanlariSil();
        }
        // ------------------------
        op.shutdownGraph();
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
                    + " MERGE (a:Car {model: line.name})");
            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\closed.csv' AS line"
                    + " MATCH (b:Car {model: line.name}) MERGE (a:Closed {value: 'Closed'})"
                    + " MERGE (b)-[:Closed {name: 'Closed'}]->(a)");
            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\has_car.csv' AS line"
                    + " MATCH (a:Car {model: line.name2}) MERGE (b:Direction {value: line.name1})"
                    + " MERGE (a)-[:Direction {name: 'Direction'}]->(b)");
            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\jagged.csv' AS line"
                    + " MATCH (a:Car {model: line.name}) MERGE (b:Jagged {value: 'Jagged'})"
                    + " MERGE (a)-[:Jagged {name: 'Jagged'}]->(b)");
            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\load.csv' AS line"
                    + " MATCH (a:Car {model: line.name1}) MERGE (b:Load {value: line.name2})"
                    + " MERGE (a)-[:Load {name: 'Load'}]->(b)");
            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\open_car.csv' AS line"
                    + " MATCH (a:Car {model: line.name}) MERGE (b:Open_Car {value: 'Open_Car'})"
                    + " MERGE (a)-[:Open_Car {name: 'Open_Car'}]-(b)");
            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\shape_car.csv' AS line"
                    + " MATCH (a:Car {model: line.name1}) MERGE (b:Shape {value: line.name2})"
                    + " MERGE (a)-[:Shape {name: 'Shape'}]->(b)");
            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\tr_double.csv' AS line"
                    + " MATCH (a:Car {model: line.name}) MERGE (b:tr_double {value: 'tr_double'})"
                    + " MERGE (a)-[:tr_double {name: 'tr_double'}]->(b)");
            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\tr_long.csv' AS line"
                    + " MATCH (a:Car {model: line.name}) MERGE (b:Leght {value: 'tr_long'})"
                    + " MERGE (a)-[:Leght {name: 'Leght'}]->(b)");
            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\tr_short.csv' AS line"
                    + " MATCH (a:Car {model: line.name}) MERGE (b:Leght {value: 'tr_short'})"
                    + " MERGE (a)-[:Leght {name: 'Leght'}]->(b)");
            db.execute("LOAD CSV WITH HEADERS FROM 'file:///D:\\\\eastbound_csv\\\\wheels.csv' AS line"
                    + " MATCH (a:Car {model: line.name1}) MERGE (b:Wheels {value: line.name2})"
                    + " MERGE (a)-[:Wheels {name: 'Wheels'}]->(b)");
            tx.success();
        }
    }

    public void listeEtiketSil(String etiket) {
        if (etiket != null) {
            iliskiler.remove(etiket);
            System.out.println(etiket + "elemaný Silindi.");
        }

        for (String[] liste : ziyaretEdilenler) {
            if (iliskiler.contains(liste[0])) {
                iliskiler.remove(liste[0]);
                System.out.println(liste[0] + "elemanı Silindi.");
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
                    System.out.println("--" + liste[0] + " deger:" + liste[1]);
                }
                query += " MATCH (a)-[c]-() RETURN distinct(c.name) as rel";
                System.out.println("Liste sorgu: " + query);
                rsRel = stmt.executeQuery(query);
            }

            while (rsRel.next()) {
                iliskiler.add(rsRel.getString("rel"));
            }
        }
        con.close();
    }

    public String[] parcala(String path) {
        String[] pathArray = path.split(",");
        return pathArray;
    }
    int calcCounter = 0;

    private double calcSupport() throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:neo4j:bolt://localhost:7678");
        Double modelEastAracSayisi = 0.0;
        try (Statement stmt = con.createStatement()) {
            String query = "";
            for (String[] liste : ziyaretEdilenler) {
                query += " MATCH (a:Car)-[:" + liste[0] + "{name:'" + liste[0] + "'}]-(:" + liste[0] + " {value: '" + liste[1] + "'})";
                System.out.println("--" + liste[0] + " deger:" + liste[1]);
            }

            query += " MATCH (a)-[c]-() MATCH (a)-[:Direction]-(f:Direction {value: 'east'}) RETURN distinct(a.model) as model";

            ResultSet sonuc = stmt.executeQuery(query);
            String queryDelete = "";
            while (sonuc.next()) {
                modelEastAracSayisi++;
            }
        }
        return modelEastAracSayisi;
    }

    private void bulunanlariSil() throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:neo4j:bolt://localhost:7678");
        System.out.println("////////////////////////////////*");
        try (Statement stmt = con.createStatement()) {
            String query = "";
            System.out.println("?????????????????????????????");
            for (String[] silinecekAracSorgu : ziyaretEdilenler) {
                query += " MATCH (a:Car)-[:" + silinecekAracSorgu[0] + "{name:'" + silinecekAracSorgu[0] + "'}]-(:" + silinecekAracSorgu[0] + " {value: '" + silinecekAracSorgu[1] + "'})";
            }
            query += " RETURN distinct(a.model) as model";
            System.out.println("bulunanları sil : " + query);
            if (ziyaretEdilenler.size() != 0) {
                ResultSet silinecekAraclar = stmt.executeQuery(query);
                while (silinecekAraclar.next()) {
                    String aracSilStr = "MATCH (a:Car {model: '" + silinecekAraclar.getString("model") + "'})-[b]-(c) DELETE b";
                    stmt.executeQuery(aracSilStr);
                    System.out.println(aracSilStr);
                }
            }
        }
        con.close();
    }

    int calcSayac = 0;

    private String calculations() throws SQLException, ArithmeticException {
        Connection con = DriverManager.getConnection("jdbc:neo4j:bolt://localhost:7678");
        Double eastCurrentNumber = 0.0;
        Double westCurrentNumber = 0.0;
        Double eastSumNumber = 0.0;
        Double westSumNumber = 0.0;
        String etiket = "";

        max = 0.0;
        // Double currentSum = 0.0;
        Double sumOfCar = 0.0;
        ArrayList<Double> eastCurrentNumberArray = new ArrayList();
        ArrayList<Double> currentSum = new ArrayList();
        ArrayList<String> relNodes = new ArrayList();
        // Querying
        try (Statement stmt = con.createStatement()) {
            // ResultSet rsRel = stmt.executeQuery("MATCH ()-[r]-() RETURN
            // distinct(r.name) as rel");
            String toplamAracSorgu = "";
            toplamAracSorgu = "MATCH (a:Car)-[]-() ";
            for (String str[] : ziyaretEdilenler) {
                toplamAracSorgu += " MATCH (a:Car)-[:" + str[0] + "{name:'" + str[0] + "'}]-() ";
            }
            toplamAracSorgu += "RETURN count(distinct(a)) as toplam";
            System.out.println(toplamAracSorgu);
            ResultSet toplamArac = stmt.executeQuery(toplamAracSorgu);

            while (toplamArac.next()) {
//                stmt.executeQuery("MATCH (a:Car {model: '"+toplamArac.getString("cars")+"'})-[b]-() DELETE b");
//                sumOfCar++;
                sumOfCar = Double.parseDouble(toplamArac.getString("toplam"));
            }
            ResultSet toplamEast = stmt.executeQuery("MATCH (a:Car)-[:Direction]-(b:Direction {value: 'east'}) RETURN count(distinct(a)) as veri");
            while (toplamEast.next()) {
                toplamEastAracSayisi = Double.parseDouble(toplamEast.getString("veri"));
            }

            for (String liste : iliskiler) {
                if (!(liste.equalsIgnoreCase("direction"))) {
                    ResultSet rsRelValues = stmt
                            .executeQuery("MATCH (n:Car)-[r:" + liste + "]->(b) return distinct(b.value) as value");
                    eastSumNumber = 0.0;
                    while (rsRelValues.next()) {
                        String westSorgu = "MATCH (n:Car)-[:" + liste + "]-(a:" + liste + " {value: '"
                                + rsRelValues.getString("value")
                                + "'}) MATCH (n)-[:Direction]-(f:Direction {value: 'west'})";
                        String eastSorgu = "MATCH (n:Car)-[:" + liste + "]-(a:" + liste + " {value: '"
                                + rsRelValues.getString("value")
                                + "'}) MATCH (n)-[:Direction]-(f:Direction {value: 'east'})";
                        for (String[] ziyaretStr : ziyaretEdilenler) {
                            westSorgu += " MATCH (n)-[:" + ziyaretStr[0] + "]-(d:" + ziyaretStr[0] + " {value: '" + ziyaretStr[1] + "'})";
                            eastSorgu += " MATCH (n)-[:" + ziyaretStr[0] + "]-(d:" + ziyaretStr[0] + " {value: '" + ziyaretStr[1] + "'})";
                        }
                        eastSorgu += " RETURN Count(n) as c";
                        westSorgu += " RETURN Count(n) as c";

                        ResultSet rsWest = stmt.executeQuery(westSorgu);
                        ResultSet rsEast = stmt.executeQuery(eastSorgu);
                        System.out.println("---------------------------");

                        System.out.println("West sorgu: " + westSorgu);
                        System.out.println("East sorgu: " + eastSorgu);
                        // ResultSet rsEast = stmt.executeQuery("MATCH
                        // (n:Car)-[:Leght]-(a:Legth {value: 'tr_long'}) MATCH
                        // (n)-[:Direction]-(c:Direction {value: 'east'}) return
                        // count(n) as c");
                        while (rsWest.next()) {
                            System.out.println("Yön: west " + liste + "--" + rsRelValues.getString("value") + "---"
                                    + rsWest.getString("c"));
                            westCurrentNumber = Double.parseDouble(rsWest.getString("c"));
                        }

                        while (rsEast.next()) {
                            System.out.println("Yön: east " + liste + "--" + rsRelValues.getString("value") + "---"
                                    + rsEast.getString("c"));
                            eastCurrentNumberArray.add(Double.parseDouble(rsEast.getString("c")));
                            eastCurrentNumber = Double.parseDouble(rsEast.getString("c"));
                            eastSumNumber += Double.parseDouble(rsEast.getString("c"));
                            relNodes.add(liste + "," + rsRelValues.getString("value") + "," + rsEast.getString("c"));
                        }
                        currentSum.add(westCurrentNumber + eastCurrentNumber);
                    }
                    /*
                    * for (int i = 0; i < currentSum.size(); i++) {
                    * sumOfCar += currentSum.get(i); } if (calcSayac == 0)
                    * { sumOfCar = 30.0; }
                     */
                    double islem = 0;
                    for (int i = 0; i < currentSum.size(); i++) {
                        System.out.println("\n");
                        System.out.println("Path: " + relNodes.get(i));
                        System.out.println("East current: " + eastCurrentNumberArray.get(i));
                        System.out.println("East sum number: " + eastSumNumber);
                        System.out.println("Current Sum: " + currentSum.get(i));
                        System.out.println("Sum of car: " + sumOfCar);
                        try {
                            islem = (eastCurrentNumberArray.get(i) / eastSumNumber) / (currentSum.get(i) / sumOfCar);
                            System.out.println("Y?lem sonucu: " + islem);
                            if (max < islem) {
                                String[] path = parcala(relNodes.get(i));
                                max = islem;
                                etiket = liste;
                                etiketDeger = path[1];
                                // etiketDegerler.add(liste)   
                            }
                        } catch (ArithmeticException MatematikselHata) {
                            System.out.println(
                                    "Islem yapilirken matematiksel bir hata olustu :" + MatematikselHata.getMessage());
                        }
                        System.out.println("\n");
                    }
                    // sumOfCar = 0.0;
                    relNodes.clear();
                    eastCurrentNumberArray.clear();
                    currentSum.clear();
                    System.out.println("**************************************");

                    // System.out.println("East current: " + westCurrentNumber);
                    // System.out.println("East sum number: " + eastSumNumber);
                    // System.out.println("Current Sum: " + currentSum);
                    // System.out.println("Sum of car: " + sumOfCar);
                    // try {
                    // islem = (eastCurrentNumber / eastSumNumber) / (currentSum
                    // / sumOfCar);
                    // System.out.println("Y?lem: " + Double.toString(islem));
                    // } catch (ArithmeticException MatematikselHata) {
                    // System.out.println(" Y?lem yapylyrken matematiksel bir
                    // hata olu?tu :" + MatematikselHata.getMessage());
                    // }
                }
            }/*
            toplamArac = stmt.executeQuery(toplamAracSorgu);
            while(toplamArac.next()){
                System.out.println(toplamArac.getString("cars"));
            }*/
            calcSayac++;
        }
        con.close();
        return etiket;
    }
}
