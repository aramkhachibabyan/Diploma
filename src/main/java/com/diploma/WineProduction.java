package com.diploma;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Scanner;

public class WineProduction {

    private static final int    DEF_N_STANDARD = 4;
    private static final int    DEF_N_PREMIUM  = 3;

    private static final double[] DEF_A = { 3000, 2800, 3200, 2600, 4000, 5500, 7000 };

    private static final double[] DEF_B = { 30, 28, 32, 26, 60, 55, 70 };

    private static final double[] DEF_C = { 800, 750, 900, 700, 1000, 1800, 800 };

    private static final double[] DEF_F = { 18000, 15000, 50000 };

    private static final int    DEF_N_RESOURCES = 3;

    private static final double[] DEF_R = { 600, 500, 400 };

    private static final double[][] DEF_r = {
            { 5, 4, 6, 4, 8, 7, 9 },
            { 3, 3, 4, 2.5, 5, 5, 6 },
            { 2, 1.5, 2.5, 1.5, 4, 3.5, 5 }
    };

    private static final double DEF_M = 30;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=".repeat(70));
        System.out.println("ԳԻՆՈՒ ԱՐՏԱԴՐՈՒԹՅԱՆ ՕՊՏԻՄԱԼԱՑՈՒՄ");
        System.out.println("=".repeat(70));
        System.out.println();
        System.out.println("  1 — Օգտագործել առկա (default) տվյալները");
        System.out.println("  2 — Մուտքագրել նոր տվյալներ");
        System.out.println();
        System.out.print("Ընտրեք (1 կամ 2): ");

        int choice = 0;
        while (choice != 1 && choice != 2) {
            String line = scanner.nextLine().trim();
            if (line.equals("1") || line.equals("2")) {
                choice = Integer.parseInt(line);
            } else {
                System.out.print("Խնդրում ենք մուտքագրել 1 կամ 2: ");
            }
        }

        System.out.println();
        System.out.println("  1 — Քառակուսային ծրագրավորման խնդիր (B ≠ 0)");
        System.out.println("  2 — Գծային ծրագրավորման խնդիր      (B = 0)");
        System.out.println();
        System.out.print("Ընտրեք խնդրի տեսակը (1 կամ 2): ");

        int modelType = 0;
        while (modelType != 1 && modelType != 2) {
            String line = scanner.nextLine().trim();
            if (line.equals("1") || line.equals("2")) {
                modelType = Integer.parseInt(line);
            } else {
                System.out.print("Խնդրում ենք մուտքագրել 1 կամ 2: ");
            }
        }
        boolean isLinear = (modelType == 2);
        System.out.println(isLinear
                ? "\n  → Ընտրված է ԳԾԱՅԻՆ խնդիր (B[i] = 0 բոլոր գինիների համար)"
                : "\n  → Ընտրված է ՔԱՌԱԿՈՒՍԱՅԻՆ խնդիր (B[i] ≠ 0)");

        int      nStandard;
        int      nPremium;
        double[] A;
        double[] B;
        double[] C;
        double[] F;
        int      nResources;
        double[] R;
        double[][] r;
        double   M;

        if (choice == 1) {
            nStandard  = DEF_N_STANDARD;
            nPremium   = DEF_N_PREMIUM;
            A          = DEF_A.clone();
            B          = isLinear ? new double[DEF_A.length] : DEF_B.clone();
            C          = DEF_C.clone();
            F          = DEF_F.clone();
            nResources = DEF_N_RESOURCES;
            R          = DEF_R.clone();
            r          = new double[nResources][nStandard + nPremium];
            for (int k = 0; k < nResources; k++)
                r[k] = DEF_r[k].clone();
            M = DEF_M;

            System.out.println();
            System.out.println("Օգտագործվում են առկա տվյալները:");
            System.out.printf("  Ստանդարտ գինիներ: %d,  Պրեմիում գինիներ: %d%n", nStandard, nPremium);
            System.out.printf("  Ռեսուրսների տեսակներ: %d,  M = %.0f%n", nResources, M);

        } else {
            System.out.println();
            System.out.print("Ստանդարտ գինիների քանակը: ");
            nStandard = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Պրեմիում գինիների քանակը: ");
            nPremium = Integer.parseInt(scanner.nextLine().trim());

            int nWines = nStandard + nPremium;
            System.out.printf("%nԳինիների ընդհանուր քանակը: %d (%d ստանդ. + %d պրեմ.)%n",
                    nWines, nStandard, nPremium);

            A = new double[nWines];
            System.out.printf("%nՄուտքագրեք A գործակիցները %d գինու համար:%n", nWines);
            for (int i = 0; i < nWines; i++) {
                System.out.printf("  A[%d]: ", i + 1);
                A[i] = Double.parseDouble(scanner.nextLine().trim());
            }

            B = new double[nWines];
            if (isLinear) {
                System.out.println("\nԳծային ռեժիմ. B[i] = 0 բոլոր գինիների համար (ավտոմատ):");
            } else {
                System.out.printf("%nՄուտքագրեք B գործակիցները %d գինու համար:%n", nWines);
                for (int i = 0; i < nWines; i++) {
                    System.out.printf("  B[%d]: ", i + 1);
                    B[i] = Double.parseDouble(scanner.nextLine().trim());
                }
            }

            C = new double[nWines];
            System.out.printf("%nՄուտքագրեք C փոփոխական ծախսերի գործակիցները %d գինու համար:%n", nWines);
            for (int i = 0; i < nWines; i++) {
                System.out.printf("  C[%d]: ", i + 1);
                C[i] = Double.parseDouble(scanner.nextLine().trim());
            }

            F = new double[nPremium];
            System.out.printf("%nՄուտքագրեք F ֆիքսված ծախսերի գործակիցները %d պրեմիում գինու համար:%n", nPremium);
            for (int j = 0; j < nPremium; j++) {
                System.out.printf("  F[%d]: ", j + 1);
                F[j] = Double.parseDouble(scanner.nextLine().trim());
            }

            System.out.print("\nՌեսուրսների տեսակների քանակը: ");
            nResources = Integer.parseInt(scanner.nextLine().trim());

            R = new double[nResources];
            System.out.println("\nՄուտքագրեք յուրաքանչյուր ռեսուրսի ընդհանուր հասանելիությունը:");
            for (int k = 0; k < nResources; k++) {
                System.out.printf("  R[%d]: ", k + 1);
                R[k] = Double.parseDouble(scanner.nextLine().trim());
            }

            r = new double[nResources][nWines];
            System.out.printf("%nՄուտքագրեք ռեսուրսների սպառումը - %d ռեսուրս x %d գինի:%n",
                    nResources, nWines);
            for (int k = 0; k < nResources; k++) {
                System.out.printf("Ռեսուրս %d:%n", k + 1);
                for (int i = 0; i < nWines; i++) {
                    System.out.printf("  r[%d][%d]: ", k + 1, i + 1);
                    r[k][i] = Double.parseDouble(scanner.nextLine().trim());
                }
            }

            System.out.print("\nՄուտքագրեք M հաստատունը: ");
            M = Double.parseDouble(scanner.nextLine().trim());
        }

        int nWines = nStandard + nPremium;

        System.out.println("\n" + "=".repeat(70));
        System.out.println("ԼՈՒԾՈՒՄ ojAlgo-ի ՄԻՋՈՑՈՎ...");
        System.out.println("Խնդրի տեսակ: " + (isLinear ? "ԳԾԱՅԻՆ (B = 0)" : "ՔԱՌԱԿՈՒՍԱՅԻՆ (B ≠ 0)"));
        System.out.println("=".repeat(70));

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable[] X = new Variable[nWines];
        for (int i = 0; i < nWines; i++)
            X[i] = model.addVariable("X" + (i + 1)).integer(true).lower(0);

        Variable[] Y = new Variable[nPremium];
        for (int j = 0; j < nPremium; j++)
            Y[j] = model.addVariable("Y" + (j + 1)).binary();

        Expression objLinear = model.addExpression("objLinear").weight(1);
        for (int i = 0; i < nWines; i++)
            objLinear.set(X[i], A[i] - C[i]);
        for (int j = 0; j < nPremium; j++)
            objLinear.set(Y[j], -F[j]);

        Expression objQuad = model.addExpression("objQuad").weight(1);
        for (int i = 0; i < nWines; i++)
            objQuad.set(X[i], X[i], -B[i]);

        for (int k = 0; k < nResources; k++) {
            Expression res = model.addExpression("resource" + (k + 1)).upper(R[k]);
            for (int i = 0; i < nWines; i++)
                res.set(X[i], r[k][i]);
        }

        for (int j = 0; j < nPremium; j++) {
            int wineIdx = nStandard + j;
            Expression bigM = model.addExpression("bigM" + (j + 1)).upper(0);
            bigM.set(X[wineIdx], 1.0);
            bigM.set(Y[j], -M);
        }

        Optimisation.Result result = model.maximise();

        if (result.getState() != Optimisation.State.OPTIMAL
                && result.getState() != Optimisation.State.FEASIBLE) {
            System.out.println("\nԼուծիչի կարգավիճակ: " + result.getState());
            System.out.println("Օպտիմալ լուծում չի գտնվել։");
            return;
        }

        double[] XOpt = new double[nWines];
        double[] YOpt = new double[nPremium];
        for (int i = 0; i < nWines; i++)   XOpt[i] = result.get(i).doubleValue();
        for (int j = 0; j < nPremium; j++) YOpt[j] = result.get(nWines + j).doubleValue();

        double totalRevenue   = 0;
        double totalVarCost   = 0;
        double totalFixedCost = 0;

        System.out.println("\n" + "=".repeat(70));
        System.out.println("ՕՊՏԻՄԱԼ ԼՈՒԾՈՒՄ ԳՏՆՎԵԼ Է");
        System.out.println("=".repeat(70));
        System.out.println("\nԼուծիչի կարգավիճակ: " + result.getState());

        System.out.println("\n" + "-".repeat(70));
        System.out.println("ԱՐՏԱԴՐՈՒԹՅԱՆ ՔԱՆԱԿՆԵՐ:");
        System.out.println("-".repeat(70));

        for (int i = 0; i < nWines; i++) {
            String wineType = (i < nStandard) ? "Ստանդարտ" : "Պրեմիում";
            int xVal = (int) Math.round(XOpt[i]);
            double rev     = A[i] * xVal - B[i] * xVal * xVal;
            double varCost = C[i] * xVal;
            double net     = rev - varCost;
            totalRevenue  += rev;
            totalVarCost  += varCost;

            System.out.printf("%nԳինի %d (%s):%n", i + 1, wineType);
            System.out.printf("  Արտադրության քանակը: %d միավոր%n", xVal);
            System.out.printf("  Եկամուտ:          %10.2f AMD%n", rev);
            System.out.printf("  Փոփոխական ծախսեր: %10.2f AMD%n", varCost);
            System.out.printf("  Շահույթ:          %10.2f AMD%n", net);
        }

        System.out.println("\n" + "-".repeat(70));
        System.out.println("ՊՐԵՄԻՈՒՄ ԳԻՆԻՆԵՐԻ ՈՐՈՇՈՒՄՆԵՐ:");
        System.out.println("-".repeat(70));

        for (int j = 0; j < nPremium; j++) {
            int wineIdx  = nStandard + j;
            int yVal     = (int) Math.round(YOpt[j]);
            int xVal     = (int) Math.round(XOpt[wineIdx]);
            String status = (yVal == 1) ? "ԱՐՏԱԴՐՎՈՒՄ Է" : "ՉԻ ԱՐՏԱԴՐՎՈՒՄ";
            double fixedCost = (yVal == 1) ? F[j] : 0.0;
            totalFixedCost  += fixedCost;

            System.out.printf("%nՊրեմիում Գինի %d:%n", wineIdx + 1);
            System.out.printf("  Որոշում Y[%d] = %d (%s)%n", j + 1, yVal, status);
            System.out.printf("  Քանակ X[%d] = %d միավոր%n", wineIdx + 1, xVal);
            if (yVal == 1)
                System.out.printf("  Ֆիքսված ծախս (F*Y): %10.2f AMD%n", fixedCost);
        }

        double netProfit = totalRevenue - totalVarCost - totalFixedCost;

        System.out.println("\n" + "=".repeat(70));
        System.out.println("ՇԱՀՈՒՅԹԻ ԿԱՌՈՒՑՎԱԾՔ:");
        System.out.println("=".repeat(70));
        System.out.printf("Ընդհանուր եկամուտ (A*X - B*X²):    %12.2f AMD%n", totalRevenue);
        System.out.printf("  - Փոփոխական ծախսեր (C*X):        %12.2f AMD%n", totalVarCost);
        System.out.printf("  - Ֆիքսված ծախսեր (F*Y):         %12.2f AMD%n", totalFixedCost);
        System.out.println("  " + "=".repeat(42));
        System.out.printf("  = ՇԱՀՈՒՅԹ:                      %12.2f AMD%n", netProfit);
        System.out.println("=".repeat(70));

        System.out.println("\n" + "-".repeat(70));
        System.out.println("ՌԵՍՈՒՐՍՆԵՐԻ ՕԳՏԱԳՈՐԾՈՒՄ:");
        System.out.println("-".repeat(70));

        for (int k = 0; k < nResources; k++) {
            double used = 0;
            for (int i = 0; i < nWines; i++) used += r[k][i] * Math.round(XOpt[i]);
            double remaining = R[k] - used;
            double percent   = (R[k] > 0) ? (used / R[k] * 100.0) : 0.0;

            System.out.printf("%nՌեսուրս %d:%n", k + 1);
            System.out.printf("  Օգտագործված:  %10.2f / %.2f (%5.1f%%)%n", used, R[k], percent);
            System.out.printf("  Մնացածը:      %10.2f%n", remaining);
        }

        System.out.println("\n" + "=".repeat(70));

        drawProductionQuantitiesChart(XOpt, nStandard, nPremium);
        drawProfitCurves(A, B, C, XOpt, nWines);
        drawBinaryDecisionTable(XOpt, YOpt, A, B, C, F, nStandard, nPremium,
                totalRevenue, totalVarCost, totalFixedCost, netProfit);
        drawResourceStackedByWine(XOpt, r, nWines, nResources);

        scanner.close();
    }

    static void drawProductionQuantitiesChart(double[] XOpt, int nStandard, int nPremium) {
        int nWines = nStandard + nPremium;
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < nWines; i++) {
            String type  = (i < nStandard) ? "Ստանդարտ" : "Պրեմիում";
            String label = "Գինի " + (i + 1);
            dataset.addValue(Math.round(XOpt[i]), type, label);
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "Արտադրության Քանակներ ըստ Գինու",
                "Գինի", "Արտադրված Միավորներ",
                dataset, PlotOrientation.VERTICAL, true, true, false);
        styleChart(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(70, 130, 180));
        renderer.setSeriesPaint(1, new Color(178, 34, 34));
        showChart(chart, "Գծապատկեր 1 – Արտադրության Քանակներ", 700, 450);
    }

    static void drawProfitCurves(double[] A, double[] B, double[] C,
                                 double[] XOpt, int nWines) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (int i = 0; i < nWines; i++) {
            XYSeries series = new XYSeries("Գինի " + (i + 1));
            int maxX = (int) Math.max(Math.round(XOpt[i]) * 2 + 10, 30);
            for (int x = 0; x <= maxX; x++) {
                double profit = A[i] * x - B[i] * x * x - C[i] * x;
                series.add(x, profit);
            }
            dataset.addSeries(series);
        }
        for (int i = 0; i < nWines; i++) {
            int    xVal = (int) Math.round(XOpt[i]);
            double yVal = A[i] * xVal - B[i] * xVal * xVal - C[i] * xVal;
            XYSeries marker = new XYSeries("Օպտ. Գ" + (i + 1));
            marker.add(xVal, yVal);
            dataset.addSeries(marker);
        }
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Շահույթի Կոր ըստ Գինու  (A·x − B·x² − C·x)",
                "Արտադրության Քանակ", "Շահույթ (AMD)",
                dataset, PlotOrientation.VERTICAL, true, true, false);
        styleChart(chart);
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        Color[] palette = {
                new Color(70,130,180), new Color(178,34,34),
                new Color(60,179,113), new Color(255,165,0),
                new Color(148,0,211),  new Color(0,206,209)
        };
        for (int i = 0; i < nWines; i++) {
            renderer.setSeriesPaint(i, palette[i % palette.length]);
            renderer.setSeriesLinesVisible(i, true);
            renderer.setSeriesShapesVisible(i, false);
            renderer.setSeriesStroke(i, new BasicStroke(2f));
        }
        for (int i = 0; i < nWines; i++) {
            int idx = nWines + i;
            renderer.setSeriesPaint(idx, palette[i % palette.length]);
            renderer.setSeriesLinesVisible(idx, false);
            renderer.setSeriesShapesVisible(idx, true);
            renderer.setSeriesShape(idx, new java.awt.geom.Ellipse2D.Double(-6, -6, 12, 12));
        }
        plot.setRenderer(renderer);
        showChart(chart, "Գծապատկեր 2 – Շահույթի Կորեր", 800, 500);
    }

    static void drawBinaryDecisionTable(double[] XOpt, double[] YOpt,
                                        double[] A, double[] B, double[] C, double[] F,
                                        int nStandard, int nPremium,
                                        double totalRevenue, double totalVarCost,
                                        double totalFixedCost, double netProfit) {
        int nWines = nStandard + nPremium;

        String[] colsTop = {
                "Գինի", "Տեսակ", "Որոշում (Y)", "Քանակ (X)",
                "Եկամուտ (AMD)", "Փոփ. Ծախս (AMD)", "Ֆիքս. Ծախս (AMD)",
                "Շահույթ (AMD)", "Կարգավիճակ"
        };
        Object[][] dataTop = new Object[nWines][9];

        for (int i = 0; i < nWines; i++) {
            int    xVal       = (int) Math.round(XOpt[i]);
            double rev        = A[i] * xVal - B[i] * xVal * xVal;
            double varCost    = C[i] * xVal;
            double wineProfit = rev - varCost;
            dataTop[i][0] = "Գինի " + (i + 1);
            if (i < nStandard) {
                dataTop[i][1] = "Ստանդարտ";
                dataTop[i][2] = "Կ/Չ";
                dataTop[i][3] = xVal;
                dataTop[i][4] = String.format("%.2f", rev);
                dataTop[i][5] = String.format("%.2f", varCost);
                dataTop[i][6] = "0.00";
                dataTop[i][7] = String.format("%.2f", wineProfit);
                dataTop[i][8] = xVal > 0 ? "✔ ԱՐՏԱԴՐՎՈՒՄ Է" : "✘ ՉԻ ԱՐՏԱԴՐՎՈՒՄ";
            } else {
                int    j            = i - nStandard;
                int    yVal         = (int) Math.round(YOpt[j]);
                double fixedCost    = yVal == 1 ? F[j] : 0.0;
                double wineProfitNet = wineProfit - fixedCost;
                dataTop[i][1] = "Պրեմիում";
                dataTop[i][2] = yVal;
                dataTop[i][3] = xVal;
                dataTop[i][4] = String.format("%.2f", rev);
                dataTop[i][5] = String.format("%.2f", varCost);
                dataTop[i][6] = String.format("%.2f", fixedCost);
                dataTop[i][7] = String.format("%.2f", wineProfitNet);
                dataTop[i][8] = yVal == 1 ? "✔ ԱՐՏԱԴՐՎՈՒՄ Է" : "✘ ՉԻ ԱՐՏԱԴՐՎՈՒՄ";
            }
        }

        JTable tableTop = new JTable(dataTop, colsTop) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        styleMainTable(tableTop);

        String[] colsSum = {
                "Ընդ. Եկամուտ (AMD)", "Ընդ. Փոփ. Ծախս (AMD)",
                "Ընդ. Ֆիքս. Ծախս (AMD)", "Ընդ. Շահույթ (AMD)"
        };
        Object[][] dataSum = {{
                String.format("%.2f", totalRevenue),
                String.format("%.2f", totalVarCost),
                String.format("%.2f", totalFixedCost),
                String.format("%.2f", netProfit)
        }};
        JTable tableSum = new JTable(dataSum, colsSum) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableSum.setRowHeight(32);
        tableSum.setFont(new Font("SansSerif", Font.BOLD, 13));
        tableSum.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        tableSum.setBackground(new Color(30, 50, 30));
        tableSum.setForeground(new Color(120, 255, 120));
        tableSum.setGridColor(new Color(80, 120, 80));
        tableSum.getTableHeader().setBackground(new Color(20, 70, 20));
        tableSum.getTableHeader().setForeground(Color.WHITE);
        tableSum.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (col == 3) {
                    setBackground(new Color(80, 70, 0));
                    setForeground(new Color(255, 220, 50));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setBackground(new Color(30, 50, 30));
                    setForeground(new Color(120, 255, 120));
                }
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        JFrame frame = new JFrame("Գծապատկեր 3 – Գինիների Արտադրության Աղյուսակ");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel titleLabel = new JLabel("  Գինիների Արտադրության Մանրամասն Աղյուսակ", SwingConstants.LEFT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(30, 30, 30));
        titleLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollTop = new JScrollPane(tableTop);
        scrollTop.setPreferredSize(new Dimension(1100, 50 + nWines * 30));
        scrollTop.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80)), "Գինիների Ցանկ",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12), new Color(180, 180, 180)));
        scrollTop.getViewport().setBackground(new Color(40, 40, 40));

        JScrollPane scrollSum = new JScrollPane(tableSum);
        scrollSum.setPreferredSize(new Dimension(1100, 80));
        scrollSum.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(80, 120, 80)), "Ընդհանուր",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12), new Color(120, 220, 120)));
        scrollSum.getViewport().setBackground(new Color(30, 50, 30));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(30, 30, 30));
        content.setBorder(new EmptyBorder(0, 8, 8, 8));
        content.add(scrollTop);
        content.add(Box.createVerticalStrut(10));
        content.add(scrollSum);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(30, 30, 30));
        root.add(titleLabel, BorderLayout.NORTH);
        root.add(content,    BorderLayout.CENTER);

        frame.setContentPane(root);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void styleMainTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setBackground(new Color(40, 40, 40));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(80, 80, 80));
        table.getTableHeader().setBackground(new Color(60, 60, 60));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                String status = String.valueOf(t.getValueAt(row, 8));
                setBackground(status.startsWith("✔") ? new Color(0, 70, 0) : new Color(70, 0, 0));
                setForeground(Color.WHITE);
                if (col == 4) setForeground(new Color(100, 210, 255));
                if (col == 7) { setForeground(new Color(100, 255, 160)); setFont(getFont().deriveFont(Font.BOLD)); }
                return this;
            }
        });
    }

    static void drawResourceStackedByWine(double[] XOpt, double[][] r,
                                          int nWines, int nResources) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < nWines; i++) {
            int xVal = (int) Math.round(XOpt[i]);
            for (int k = 0; k < nResources; k++)
                dataset.addValue(r[k][i] * xVal, "Գինի " + (i + 1), "Ռեսուրս " + (k + 1));
        }
        JFreeChart chart = ChartFactory.createStackedBarChart(
                "Ռեսուրսների Սպառում – Բաշխում ըստ Գինու",
                "Ռեսուրս", "Սպառված Քանակ",
                dataset, PlotOrientation.VERTICAL, true, true, false);
        styleChart(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
        Color[] palette = {
                new Color(70,130,180), new Color(178,34,34),
                new Color(60,179,113), new Color(255,165,0),
                new Color(148,0,211),  new Color(0,206,209)
        };
        for (int i = 0; i < nWines; i++) renderer.setSeriesPaint(i, palette[i % palette.length]);
        showChart(chart, "Գծապատկեր 4 – Ռեսուրսների Սպառում ըստ Գինու", 700, 450);
    }

    private static void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(new Color(30, 30, 30));
        chart.getTitle().setPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 15));
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(new Color(40, 40, 40));
            chart.getLegend().setItemPaint(Color.WHITE);
        }
        if (chart.getPlot() instanceof CategoryPlot cp) {
            cp.setBackgroundPaint(new Color(45, 45, 45));
            cp.setDomainGridlinePaint(new Color(80, 80, 80));
            cp.setRangeGridlinePaint(new Color(80, 80, 80));
            styleCategoryAxes(cp);
        } else if (chart.getPlot() instanceof XYPlot xp) {
            xp.setBackgroundPaint(new Color(45, 45, 45));
            xp.setDomainGridlinePaint(new Color(80, 80, 80));
            xp.setRangeGridlinePaint(new Color(80, 80, 80));
            styleXYAxes(xp);
        }
    }

    private static void styleCategoryAxes(CategoryPlot plot) {
        CategoryAxis domain = plot.getDomainAxis();
        domain.setTickLabelPaint(Color.WHITE);
        domain.setLabelPaint(Color.WHITE);
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setTickLabelPaint(Color.WHITE);
        range.setLabelPaint(Color.WHITE);
    }

    private static void styleXYAxes(XYPlot plot) {
        plot.getDomainAxis().setTickLabelPaint(Color.WHITE);
        plot.getDomainAxis().setLabelPaint(Color.WHITE);
        plot.getRangeAxis().setTickLabelPaint(Color.WHITE);
        plot.getRangeAxis().setLabelPaint(Color.WHITE);
    }

    private static void showChart(JFreeChart chart, String title, int w, int h) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(w, h));
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}