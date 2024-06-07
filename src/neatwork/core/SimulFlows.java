package neatwork.core;

/* Ce programme calcule les débits stationnaires dans un réseau arborescent dont certains des
robinets peuvent être fermés. Le programme simule des configurations de robinets ouverts par
tirage Monte-Carlo ; les ouverture des robinets sont identiquement distiribuées et indépendantes
suivant un probabilité donnée p.
Les débits sont calculés par une méthode de direction de Newton réduite, proche de la méthode
de Bertsekas. Les robinets ouverts définissent un ensemble actif dont l'entrée et la sortie des
éléments se fait en ligne. Le pas est projeté sur l'orthant positif.
Une recherche linéaire à partir du pas de Newton complet est effectuée
pour garantir une décroissance suffisante de la fonction objectif suivant
un crière simple adapté de la méthode de Armijo.
Auteur. Jean-Philippe Vial 15 octobre 2022.
D. P. Bertsekas, “Projected Newton methods for optimization problems with simple constraints,”
SIAM J. Control and Optimization, vol. 20, pp. 221–246, March 1982. */

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.crypto.Data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import org.apache.poi.ss.usermodel.CellType;

public class SimulFlows {

    private static final String length = null;

    public static void run () {
        
        String excelFilePath = "src/neatwork/core/SimuJava/ressource/dat.xlsx";

        try (InputStream inputStream = new FileInputStream(excelFilePath);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.iterator();
            String key = "";
            int nB = 0;
            int nT = 0;
            double[] length = new double[0];
            double[] h = new double[0];
            double invest = 0;
            double cible = 0;
            double lbd = 0;
            int[][] S = new int[0][0];
            double[] nbouvert = new double[0];
            double[] alpha = new double[0];
            double[] beta = new double[0];
            int maxiter = 0;
            double tolr = 0;
            double tolx = 0;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                Cell cell = row.getCell(0);
                if (cell != null && cell.getCellType() == CellType.STRING && !cell.getStringCellValue().matches(".*e-?[0-9]+")) {
                    key = cell.getStringCellValue().trim();
                    continue;
                }

                if (!key.isEmpty()) {
                    switch (key) {
                        case "nB":
                            nB = (int)getNumValueFromCell(row.getCell(0));
                            key = "";
                            break;
                        case "nT":
                            nT = (int)getNumValueFromCell(row.getCell(0));
                            key = "";
                            break;
                        case "length":
                            length = new double[nB + nT];
                            for (int i = 0; i < nB + nT; i++) {
                                length[i] = getNumValueFromCell(row.getCell(i));
                            }
                            key = "";
                            break;
                        case "h":
                            h = new double[nB + nT];
                            for (int i = 0; i < nB + nT; i++) {
                                h[i] = getNumValueFromCell(row.getCell(i));
                            }
                            key = "";
                            break;
                        case "beta":
                            beta = new double[nB + nT];
                            for (int i = 0; i < nB + nT; i++) {
                                beta[i] = getNumValueFromCell(row.getCell(i));
                            }
                            key = "";
                            break;
                        case "alpha":
                            alpha = new double[nT];
                            for (int i = 0; i < nT; i++) {
                                alpha[i] = getNumValueFromCell(row.getCell(i));
                            }
                            key = "";
                            break;
                        case "S":
                            S = new int[nB][nT];
                            for (int i = 0; i < nB; i++) {
                                for (int j = 0; j < nT; j++) {
                                    S[i][j] = (int)getNumValueFromCell(row.getCell(j));
                                }
                                if (i < nB - 1) {
                                    row = rowIterator.next();
                                }
                            }
                            key = "";
                            break; 
                        case "invest":
                            invest = getNumValueFromCell(row.getCell(0));
                            key = "";
                            break;
                        case "maxiter":
                            maxiter = (int)getNumValueFromCell(row.getCell(0));
                            key = "";
                            break;   
                        case "tolr":
                            tolr = getNumValueFromCell(row.getCell(0));
                            key = "";
                            break;   
                        case "tolx":
                            tolx = getNumValueFromCell(row.getCell(0));
                            key = "";
                            break;      
                        case "cible":
                            cible = getNumValueFromCell(row.getCell(0));
                            key = "";
                            break;    
                        case "lbd":
                            lbd = getNumValueFromCell(row.getCell(0));
                            key = "";
                            break;
                        case "nbouvert":
                            ArrayList<Double> nbouvertArrayList = new ArrayList<>();
                            int columns = row.getLastCellNum();
                            for (int i = 0; i < columns; i++) {
                                if (row.getCell(i) != null && row.getCell(i).getCellType() == CellType.NUMERIC) {
                                    nbouvertArrayList.add(getNumValueFromCell(row.getCell(i)));
                                }
                            }
                            nbouvert = nbouvertArrayList.stream().mapToDouble(Double::doubleValue).toArray();
                            key = "";
                            break;                                                                                                                                                 
                }
            }
        }
        int simulmax = 1000;
        SimulFlows.run(nB,  nT, length, h, invest, cible, lbd, S, nbouvert, alpha, beta, maxiter, tolr, tolx, simulmax);
        } catch (IOException e) {
                e.printStackTrace();
        }
    }

    public static void run (int nB, int nT, double[] length, double[] h, double invest,
    double cible, double lbd, int[][] S, double[] nbouvert, double[] alpha, double[] beta,
    int maxiter, double tolr, double tolx, int simulmax) {

        /* Paramètres pour le calcul des débits via l'optimisation
        - maxiter limite le nombre d'itérations dans le calcul des débits
        - tolr définit la convergence en terme de gradient actif.  Cette borne
        doit être en relation avec l'incertitude sur la partie fixe du gradient qu'est
        la dénivellation. Cette dernière est au mieux de l'ordre du décimètre
        de colonne d'eau (le mètre est l'unité de mesure de la pression)
        Un débit inférieur à tolx sera considéré nul.
        - tolx définit le seuil à partir duquel un débit est considéré nul.
        Les valeurs proposées ci-dessous sont en relation avec les cas traités
        par APLV. */

        Dat dat = new Dat(nB, nT, length, h, invest, cible, lbd, S, nbouvert, alpha, beta, maxiter, tolr, tolx);

        // Dat dat = new Dat(
        //         3,
        //         4,
        //         new double[]{200, 500, 70, 120, 180, 90, 110},
        //         new double[]{-10, -15, -20, -12, -9, -18, -50},
        //         455.6179,
        //         1.2000e-4,
        //         1.7810,
        //         new int[][]{
        //                 {1, 1, 1, 1},
        //                 {1, 1, 0, 0},
        //                 {1, 0, 1, 0}
        //         },
        //         new double[]{},
        //         new double[]{2.6837e+8, 1.0695e+8, 8.6923e+8, 3.0646e+9},
        //         new double[]{1.0972e+7, 2.7429e+7, 1.3032e+7, 2.2341e+7, 1.5844e+7, 1.6756e+7, 2.0479e+7},
        //         20,
        //         1e-2,
        //         1e-6
        // );


        // Dat dat = new Dat(
        // 74, // dat.nB
        // 61, // dat.nT
        // new double[]{ // dat.long
        //     69, 35, 65, 95, 35, 61, 82, 49, 45, 14, 40, 65, 106, 10, 44, 32, 56, 
        //     62, 84, 26, 17, 21, 41, 28, 39, 66, 26, 87, 41, 65, 57, 33, 28, 29, 
        //     32, 89, 22, 137, 84, 66, 83, 59, 136, 84, 48, 108, 66, 13, 39, 23, 
        //     33, 18, 75, 142, 32, 100, 30, 78, 21, 23, 74, 38, 67, 51, 52, 35, 
        //     35, 50, 27, 58, 21, 47, 12, 69, 29, 41, 16, 24, 63, 5, 13, 41, 41, 
        //     20, 19, 24, 8, 31, 31, 34, 17, 37, 42, 52, 32, 194, 114, 17, 17, 54, 
        //     5, 40, 6, 24, 20, 27, 35, 9, 11, 28, 146, 121, 13, 203, 30, 14, 151, 
        //     11, 25, 14, 20, 35, 35, 56, 19, 17, 20, 60, 70, 64, 10, 111, 48, 40, 66}, 

        // new double[]{ // dat.h
        //     -16.8, -26.4, -40, -52.9, -56.6, -67, -76.7, -83.9, -91.9, -84.7, 
        //     -94.53198389, -110.7, -155.2, -155, -162.1, -157.6, -160.1, -162.4, 
        //     -165.1, -164.4, -165.5, -171.8, -165.5, -166.3, -171.5, -177.1, -180.8, 
        //     -187.5, -202.2, -171.6, -170.6, -178.8, -184, -186.5, -173.1, -104.8, 
        //     -89.8, -125, -119, -114.8, -105.1, -105.1, -110.4, -118.7, -122.3, -136.8, 
        //     -120.8, -121.7, -127.2, -105.6, -103.8, -105.2, -141.7, -154.8, -155.9, -153.1, 
        //     -152.8, -161.5, -162.8, -168, -176.1, -163.8, -171.7, -172.6, -175.8, -172.7, 
        //     -176.6, -182.4, -177.6, -187.4, -192.1, -200.2, -197, -198, -32.41863503, 
        //     -44.21508682, -60.46302678, -81.68672287, -80.18299053, -159.4, -163.4, 
        //     -167.3, -178.5, -161.9, -207.1, -207.3, -181.1, -193.5, -193.5, -177.3, 
        //     -90.27289951, -93.70888464, -118.9, -126.7, -125.2, -145.2, -136.7, -101.8, 
        //     -103.4, -103.5, -115.4, -117.3, -116.6, -119.2, -121, -121.6, -127.4, -118.5, 
        //     -120.3, -131.8, -164.6, -151.6, -101.6, -157.7, -137.2, -152.8, -219.6, -171.3, 
        //     -166.8, -170.5, -167.3, -150.6, -150.6, -155.1, -176.1, -179.7, -170.7, -177.9, 
        //     -172.3, -174.3, -185.4, -172.8, -206.2, -44.12735787, -162.7}, 

        // 2398.5155, // dat.invest
        // 0.00012, // dat.cible
        // 1.781, // dat.lbd

        // new int[][]{ // dat.S
        //     {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        //     {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        //     {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        //     {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0}
        
        // },

        // new double[]{},
        // new double[]{ // dat.alpha
        //     198588537700000.0, 2788948850.30862, 3752967330.36513, 4561355627.24402, 
        //     3977610083.08795, 7013537837.41436, 6892417990.13253, 7093977813.68743, 
        //     7843520097.96555, 6450793362.89711, 9181746756.46675, 9188912908.8081, 
        //     8912274210.21867, 9688510182.13732, 9688510182.13732, 8771882444.56977, 
        //     5000132900.20402, 5211852032.4861, 6137595992.94651, 6665817186.5181, 
        //     6588541466.04158, 7759613690.79026, 7276899697.77308, 3901894665.9509, 
        //     3735920105.03293, 3693116299.02559, 3675444556.44693, 3760329845.05861, 
        //     3757433342.47075, 3913787046.45517, 3892667353.52202, 3924922189.02213, 
        //     4299464473.30025, 3481055992.01851, 3603366897.3995, 4283644591.96028, 
        //     6402765787.21618, 5533601692.17608, 2929174093.87998, 6545341586.83575, 
        //     4800013674.43753, 5412603534.71692, 9867289442.20328, 6315947979.43307, 
        //     5984624317.09997, 6045666387.62255, 5688943843.301, 4753150837.15949, 
        //     4753150837.15949, 5037415343.65983, 6301399501.72162, 6456125724.20348, 
        //     5445958256.65874, 5855873586.92174, 5076185195.53579, 5223141368.28173, 
        //     6066580256.32846, 4906951871.65775, 7218329032.24515, 2784201109.42369, 
        //     5434083260.8615},        
        
        // new double[]{// dat.beta
        //     164195.660014824, 83287.6536307077, 154677.071028457, 
        //     226066.488426207, 83287.6536307077, 145158.482042091, 195131.074220515, 
        //     445311.395996728, 720961.373443254, 2606422.50315073, 7446921.43757352, 
        //     12101247.336057, 19734341.8095698, 1861730.35939338, 8191613.58133087, 
        //     5957537.15005881, 10425690.0126029, 11542728.228239, 15638535.0189044, 
        //     4840498.93442279, 3164941.61096875, 3909633.7547261, 7633094.47351286, 
        //     5212845.00630146, 7260748.40163418, 12287420.3719963, 4840498.93442279, 
        //     16197054.1267224, 7633094.47351286, 12101247.336057, 10611863.0485423, 
        //     6143710.18599815, 5212845.00630146, 5399018.0422408, 5957537.15005882, 
        //     1425901.38303221, 4095806.79066544, 2194926.84803835, 15638535.0189044, 
        //     1057410.01438344, 1329773.19990645, 945260.467403378, 2178905.48418406, 
        //     1345794.56376074, 769025.465006137, 1730307.29626381, 12287420.3719963, 
        //     2420249.46721139, 7260748.40163418, 4281979.82660477, 6143710.18599815, 
        //     3351114.64690808, 1201602.28907209, 2275033.66730982, 512683.643337425, 
        //     1602136.38542945, 5585191.07818014, 1249666.38063497, 336448.640940185, 
        //     368491.368648774, 1185580.9252178, 7074575.36569484, 3675508.57263056, 
        //     2797775.18215162, 2852633.51905656, 6516056.25787683, 6442702.12385988, 
        //     9308651.7969669, 5026671.97036213, 10798036.0844816, 3909633.7547261, 
        //     8750132.68914888, 2234076.43127206, 12845939.4798143, 5399018.0422408, 
        //     7633094.47351286, 2978768.57502941, 4468152.86254411, 11728901.2641783, 
        //     930865.17969669, 2420249.46721139, 7633094.47351286, 7633094.47351286, 
        //     3723460.71878676, 3537287.68284742, 4468152.86254411, 1489384.2875147, 
        //     5771364.11411948, 5771364.11411948, 6329883.22193749, 3164941.61096875, 
        //     6888402.32975551, 7819267.50945219, 9680997.86884557, 5957537.15005882, 
        //     36117568.9722316, 21223726.0970845, 3164941.61096875, 3164941.61096875, 
        //     10053343.9407243, 930865.17969669, 7446921.43757352, 1117038.21563603, 
        //     4468152.86254411, 3723460.71878676, 5026671.97036212, 6516056.25787683, 
        //     1675557.32345404, 2047903.39533272, 5212845.00630146, 27181263.2471433, 
        //     22526937.3486599, 2420249.46721139, 37793126.2956856, 5585191.07818014, 
        //     2606422.50315073, 28112128.42684, 2047903.39533272, 4654325.89848345, 
        //     2606422.50315073, 3723460.71878676, 6516056.25787683, 6516056.25787683, 
        //     10425690.0126029, 3537287.68284742, 3164941.61096875, 3723460.71878676, 
        //     11170382.1563603, 13032112.5157537, 11915074.3001176, 1861730.35939338, 
        //     20665206.9892665, 8936305.72508822, 7446921.43757352, 12287420.3719963},

        // 20,
        // 1e-2,
        // 1e-6

        // );

        // Variables pour stocker les temps cumulés
        long totalHessienTime = 0;
        long totalNdirTime = 0;
        long totalAmaxTime = 0;
        long totalGradientTime = 0;
        long pressionDuration = 0;

        // Début de la mesure du temps de prétraitement
        long preTraitementStartTime = System.currentTimeMillis();



        /* Récolte des données et initialisation de base
        Le nom du fichier est le nom du projet + Dsg, suivi de
        _poids, _proba ouverture, _budget ; poids = kappa */

        System.out.printf("Réseau %s;   coût : %4.2f;  Diamètres : %s%n", "design_name", dat.invest, "base_name");
        
        // datsave archive la structure dat qui elle peut changer dans les simulations.
        Dat datsave = new Dat(dat);
        
        // Paramètres concernant les simulations
        double opentaps = 1;
        System.out.printf("Nb de simulations %d ;   proba  %10.2f%n", simulmax, opentaps);
        
        // Sécurité sur les boucles de simulation
        if (opentaps < 0.001) {
            throw new IllegalArgumentException("opentaps doit être supérieur à 0.0001");
        }

        int k = 0; // Compteur du nb de simulations
        int alerte = 0; // Compteur des défaillances de convergence

        /* Initialisation du post-traitement
        iter  garde trace du nombre d'itérations par simulation
        X : stocke les débits par simulation
        Fail : stocke le nombre d'échecs par simulation (rob ne coulant pas)
        T  matrice triangulaire inférieure de 1 pour calculer les pressions.
        P : Table des sommes des charges aux nœuds intermédiaires
        Tab : Table du nombre d'échecs par simulation
        iter vecteur du nombre d'itérations par simulation */

        int[] iter = new int[simulmax];
        double[][] X = new double[dat.nT][simulmax];
        int[][] T = Utils.createLowerTriangularMatrix(dat.nB);
        double[][] P = new double[dat.nB][simulmax];
        long startTime = System.currentTimeMillis();


        // Fin de la mesure du temps de prétraitement
        long preTraitementEndTime = System.currentTimeMillis();

        // Calcul et affichage du temps de prétraitement
        long preTraitementDuration = preTraitementEndTime - preTraitementStartTime;


        // Boucle de simulation des scénarios
        while (k < simulmax) {
            k = k + 1;

            /* La structure dat des dat.paramètres est sauvegardée, car elle évolue à chaque simulation
            et doit être réactualisée pour la simulation suivante. Saugegarde de la sturcture */
            dat = new Dat(datsave);
            
            // Sélection des robinets fermés.
            // On répète le tirage pour avoir au moins un robinet ouvert (length(K) < dat.nT)
            List<Integer> K = new ArrayList<>();
            if (opentaps < 1) {
                while (K.size() == 0 || K.size() == dat.nT) {
                    K.clear();
        
                    double[] randomVector = new double[dat.nT];
                    for (int i = 0; i < dat.nT; i++) {
                        randomVector[i] = Math.random();
                    }
        
                    for (int i = 0; i < dat.nT; i++) {
                        if (randomVector[i] > opentaps) {
                            K.add(i);
                        }
                    }
                }
            }


            // // TEST pour éviter la génération aléatoire
            // K.clear();
            // K.add(0);            
            // K.add(1);
            // K.add(2);
            // // FIN TEST
            
            /* Mise à niveau des paramètres du réseau pour la simulation en cours
            tout ce qui a trait aux robinets fermés n'est pas pertinent dans le calcul
            des débits */
            dat.nT -= K.size();

            dat.h = Utils.removeElements(dat.h, K, dat.nB);
            dat.S = Utils.removeColumns(dat.S, K);
            dat.alpha = Utils.removeElements(dat.alpha, K);
            dat.beta = Utils.removeElements(dat.beta, K, dat.nB);

            dat.nbouvert = new double[dat.nT];
            Arrays.fill(dat.nbouvert, 1.0);

            // iterations de la méthode de newton reduite
            // initialisation
            double[] x = new double[dat.nT];
            Arrays.fill(x, dat.cible);

            List<Integer> Izero = new ArrayList<>();

            double convergence = 10000;

            int i = 0;
            // Calcul du gradient et du Hessien au point de départ
            double[] y = new double[dat.S.length];
            for (int j = 0; j < dat.S.length; j++) {
                for (int l = 0; l < dat.S[j].length; l++) {
                    y[j] += dat.S[j][l] * x[l];
                }
            }

            // Timer pour Hessien
            long hessien0StartTime = System.currentTimeMillis();
            double[] gradf = Gradient.gradient(x, y, dat);
            long hessien0EndTime = System.currentTimeMillis();
            totalHessienTime += hessien0EndTime - hessien0StartTime;

            long gradient0StartTime = System.currentTimeMillis();
            double[][] H = Hessien.hessien(x, y, dat);
            long gradient0EndTime = System.currentTimeMillis();
            totalGradientTime += gradient0EndTime - gradient0StartTime;

            
            while (i < dat.maxiter && convergence > dat.tolr) {
                // iterations de la méthode de Newton projetée-réduite
                i = i + 1;

                // Timer pour Hessien
                long hessienStartTime = System.currentTimeMillis();
                H = Hessien.hessien(x, y, dat);
                long hessienEndTime = System.currentTimeMillis();
                totalHessienTime += hessienEndTime - hessienStartTime;

                // Timer pour Ndir
                long ndirStartTime = System.currentTimeMillis();
                Ndir.ResultNdir resultNdir = Ndir.ndir(H, gradf, Izero);
                long ndirEndTime = System.currentTimeMillis();
                totalNdirTime += ndirEndTime - ndirStartTime;

                // Timer pour Amax
                long amaxStartTime = System.currentTimeMillis();
                Amax.ResultAmax resultAmax = Amax.amax(x, y, resultNdir.dx_red, gradf, resultNdir.Hred, dat);
                x = resultAmax.x;
                long amaxEndTime = System.currentTimeMillis();
                totalAmaxTime += amaxEndTime - amaxStartTime;
            
                // Test de convergence et préparation de l'itération suivante
                // Calculer le gradient au nouvel itéré
                y = new double[dat.S.length];
                for (int j = 0; j < dat.S.length; j++) {
                    for (int l = 0; l < dat.S[j].length; l++) {
                        y[j] += dat.S[j][l] * x[l];
                    }
                }
                
                // Timer pour le calcul du gradient
                long gradientStartTime = System.currentTimeMillis();
                gradf = Gradient.gradient(x, y, dat);
            
                long gradientEndTime = System.currentTimeMillis();
                totalGradientTime += gradientEndTime - gradientStartTime;
            
                List<Integer> tempIzero = new ArrayList<>();
                for (int j = 0; j < x.length; j++) {
                    if (x[j] < dat.tolx && gradf[j] > 1e-3) {
                        tempIzero.add(j);
                    }
                }
            
                Izero = tempIzero; // Mise à jour de Izero avec les éléments filtrés
            
                // Calcul du résidu et de la convergence
                double[] residu = Arrays.copyOf(gradf, gradf.length);
                for (Integer index : Izero) {
                    residu[index] = Math.min(residu[index], 0);
                }
                double maxAbsResidu = Arrays.stream(residu).map(Math::abs).max().orElse(0.0);
                convergence = maxAbsResidu;
            
            }

            
            // Archivage des résultats pour traitement statistique ultérieur
            if (i >= dat.maxiter) {
                alerte++;
                k--;
            }
            else {

                // Début de la mesure du temps des pressions
                long pressionStartTime = System.currentTimeMillis();


                nT = datsave.nT;
                nB = datsave.nB;
                double[] xstore = new double[nT];
                Arrays.fill(xstore, -1e-4); // pour permettre l'identification des robinets fermés.

                List<Integer> ensembleTotal = new ArrayList<>();
                for (int j = 0; j < nT; j++) {
                    ensembleTotal.add(j);
                }

                // ensemble des robinets ouverts
                List<Integer> Ko = new ArrayList<>(ensembleTotal);
                List<Integer> listK = new ArrayList<>();
                //listK.addAll(K);
                for (int value : K) {
                    listK.add(value);
                }
                Ko.removeAll(listK);

                for (int j = 0; j < Ko.size(); j++) {
                    xstore[Ko.get(j)] = x[j];
                }

                for (int j = 0; j < nT; j++) {
                    X[j][k - 1] = xstore[j];
                }

                iter[k - 1] = i;

                // Calcul des pressions aux nœuds intermédiaires
                double[] pb = new double[nB];
                for (int j = 0; j < nB; j++) {
                    pb[j] = dat.beta[j] * Math.pow(y[j], dat.lbd);
                }

                double[][] product = Utils.multiplyElementwise(pb, dat.S);

                double[][] result = Utils.multiplyMatrices(T, product);
        
                double[] ch = Utils.findMaxValuesOfEachRow(result);

                for (int j = 0; j < nB; j++) {
                    P[j][k - 1] = ch[j];
                }

                // Fin de la mesure du temps des pressions
                long pressionEndTime = System.currentTimeMillis();

                // Calcul et affichage du temps des pressions
                pressionDuration += pressionEndTime - pressionStartTime;


            }

            

        }

        long TempsCalcul = System.currentTimeMillis() - startTime;

        System.out.printf("Temps de calcul %2.2f secondes, divergences %d%n", TempsCalcul / 1000.0, alerte);

        // Affichage des temps cumulés
        System.out.println("Temps total d'exécution de Hessien: " + totalHessienTime + " ms");
        System.out.println("Temps total d'exécution de Ndir: " + totalNdirTime + " ms");
        System.out.println("Temps total d'exécution de Amax: " + totalAmaxTime + " ms");
        System.out.println("Temps total d'exécution du calcul du gradient: " + totalGradientTime + " ms");
        System.out.println("Temps de prétraitement : " + preTraitementDuration + " ms");
        System.out.println("pressionDuration : " + pressionDuration + " ms");



        dat = new Dat(datsave);

        // Post-traitement
        double[][] Tab = new double[dat.nT][5];

        for (int j = 0; j < dat.nT; j++) {
            List<Integer> jo = new ArrayList<>();

            for (int l = 0; l < X[j].length; l++) {
                if (X[j][l] >= 0) {
                    jo.add(l);
                }
            }

            if (!jo.isEmpty()) {
                List<Integer> jwork = new ArrayList<>();
                for (int index : jo) {
                    if (X[j][index] > 0) {
                        jwork.add(index);
                    }
                }

                if (!jwork.isEmpty()) {
                    List<Integer> var5 = new ArrayList<>();
                    List<Integer> var95 = new ArrayList<>();

                    for (int index : jo) {
                        if (X[j][index] <= Utils.computePercentile(Utils.keepColumns(X, jo)[j], 5)) {
                            var5.add(index);
                        }
                        if (X[j][index] >= Utils.computePercentile(Utils.keepColumns(X, jo)[j], 95)) {
                            var95.add(index);
                        }
                    }

                    double cVar5 = Utils.computeMean(X[j], var5);
                    double cVar95 = Utils.computeMean(X[j], var95);

                    double[] tab = {
                            (double) jo.size() / simulmax,
                            cVar5,
                            Utils.computeMean(X[j], jo),
                            cVar95,
                            Utils.computeStd(X[j], jo) / Utils.computeMean(X[j], jo)
                    };

                    for (int i = 1; i <= 3; i++) {
                        tab[i] *= 1000;
                    }

                    Tab[j] = tab;
                } else {
                    Tab[j] = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
                }
            }
        }

        int[] nbnodes = new int[dat.S[0].length];
        for (int i = 0; i < dat.S.length; i++) {
            for (int j = 0; j < dat.S[0].length; j++) {
                nbnodes[j] += dat.S[i][j];
            }
        }

        double[][] longtotmat = Utils.multiplyElementwise(Arrays.copyOfRange(dat.length, 0, dat.nB), dat.S); 
        double[] longtot = Utils.addArrays(Utils.calculateColumnSums(longtotmat), Arrays.copyOfRange(dat.length, dat.nB, dat.length.length));

        double[] ratiocritique = new double[dat.S[0].length];
        for (int i = 0; i < dat.S[0].length; i++) {
            ratiocritique[i] = longtot[i] / (-dat.h[i + dat.nB]);
        }

        System.out.println("Résultats");
        System.out.println("   noeud    fréquence   cVar5%%      moy       cVar95%%    coefvar     noeuds   long/1000");

        for (int i = dat.nB; i < dat.nB + dat.nT; i++) {
            System.out.printf("%6d%12.4f%12.4f%12.4f%12.4f%12.4f%9d%11.4f%n",
                    i + 1, Tab[i - dat.nB][0], Tab[i - dat.nB][1], Tab[i - dat.nB][2],
                    Tab[i - dat.nB][3], Tab[i - dat.nB][4], nbnodes[i - dat.nB], longtot[i - dat.nB] / 1000);
        }

        System.out.printf("Statistiques sur les itérations    min = %d,   moyenne = %1.2f,   max = %d%n",
                Utils.findMin(iter), Utils.findMean(iter), Utils.findMax(iter));

    }

    public static void main(String[] args) {
        SimulFlows.run();
    }

    private static double getNumValueFromCell(Cell cell) {
        double value = 0;
         if (cell != null) {
            //Pattern pattern = Pattern.compile(".*e-?[0-9]");
            if (CellType.STRING.equals(cell.getCellType()) && cell.getStringCellValue().matches(".*e-?[0-9]+")) {
                String exponentialValue = cell.getStringCellValue();
                String valueAsStr = exponentialValue.split("e")[0];
                String expAsStr = exponentialValue.split("e")[1].replace("e", "");
                value = Double.parseDouble(valueAsStr) *  Math.pow(10, Double.parseDouble(expAsStr));
            }
            else if (CellType.NUMERIC.equals(cell.getCellType())){
               value = cell.getNumericCellValue();
            }
        }
        return value;
    }

}