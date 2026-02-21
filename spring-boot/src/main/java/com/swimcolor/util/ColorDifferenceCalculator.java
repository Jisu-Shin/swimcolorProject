package com.swimcolor.util;

/**
 * CIEDE2000 색상 거리 계산기
 * RGB → XYZ → CIELAB 변환 후 CIEDE2000 공식으로 색상 유사도를 계산합니다.
 *
 * 참고: Sharma et al. (2005), "The CIEDE2000 Color-Difference Formula"
 */
public class ColorDifferenceCalculator {

    // -------------------------------------------------------
    // Public API
    // -------------------------------------------------------

    /**
     * 두 RGB 색상 간의 CIEDE2000 거리를 반환합니다.
     *
     * @param r1, g1, b1  첫 번째 색상 (0~255)
     * @param r2, g2, b2  두 번째 색상 (0~255)
     * @return CIEDE2000 거리 (0 = 동일, 일반적으로 0~100 범위)
     */
    public static double deltaE2000(int r1, int g1, int b1,
                                    int r2, int g2, int b2) {
        double[] lab1 = rgbToLab(r1, g1, b1);
        double[] lab2 = rgbToLab(r2, g2, b2);
        return ciede2000(lab1[0], lab1[1], lab1[2],
                lab2[0], lab2[1], lab2[2]);
    }

    /**
     * 두 HEX 색상 문자열 간의 CIEDE2000 거리를 반환합니다.
     *
     * @param hex1  "#RRGGBB" 또는 "RRGGBB" 형식
     * @param hex2  "#RRGGBB" 또는 "RRGGBB" 형식
     */
    public static double deltaE2000(String hex1, String hex2) {
        int[] rgb1 = hexToRgb(hex1);
        int[] rgb2 = hexToRgb(hex2);
        return deltaE2000(rgb1[0], rgb1[1], rgb1[2],
                rgb2[0], rgb2[1], rgb2[2]);
    }

    // -------------------------------------------------------
    // RGB → XYZ → CIELAB 변환
    // -------------------------------------------------------

    /**
     * sRGB(0~255) → CIELAB [L*, a*, b*]
     * D65 광원, 2도 관찰자 기준
     */
    public static double[] rgbToLab(int r, int g, int b) {
        double[] xyz = rgbToXyz(r, g, b);
        return xyzToLab(xyz[0], xyz[1], xyz[2]);
    }

    private static double[] rgbToXyz(int r, int g, int b) {
        // sRGB 선형화 (gamma 제거)
        double rLin = linearize(r / 255.0);
        double gLin = linearize(g / 255.0);
        double bLin = linearize(b / 255.0);

        // sRGB → XYZ (D65, IEC 61966-2-1)
        double x = rLin * 0.4124564 + gLin * 0.3575761 + bLin * 0.1804375;
        double y = rLin * 0.2126729 + gLin * 0.7151522 + bLin * 0.0721750;
        double z = rLin * 0.0193339 + gLin * 0.1191920 + bLin * 0.9503041;

        return new double[]{x, y, z};
    }

    private static double linearize(double v) {
        return (v <= 0.04045) ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }

    private static double[] xyzToLab(double x, double y, double z) {
        // D65 기준 백색점
        final double Xn = 0.95047;
        final double Yn = 1.00000;
        final double Zn = 1.08883;

        double fx = labF(x / Xn);
        double fy = labF(y / Yn);
        double fz = labF(z / Zn);

        double L = 116.0 * fy - 16.0;
        double a = 500.0 * (fx - fy);
        double bVal = 200.0 * (fy - fz);

        return new double[]{L, a, bVal};
    }

    private static double labF(double t) {
        final double delta = 6.0 / 29.0;
        return (t > delta * delta * delta)
                ? Math.cbrt(t)
                : t / (3.0 * delta * delta) + 4.0 / 29.0;
    }

    // -------------------------------------------------------
    // CIEDE2000 핵심 공식
    // -------------------------------------------------------

    /**
     * CIEDE2000 색상 거리 계산
     *
     * @param L1, a1, b1  첫 번째 CIELAB 값
     * @param L2, a2, b2  두 번째 CIELAB 값
     */
    public static double ciede2000(double L1, double a1, double b1,
                                   double L2, double a2, double b2) {
        // Step 1: C'ab, h'ab 계산
        double C1 = Math.sqrt(a1 * a1 + b1 * b1);
        double C2 = Math.sqrt(a2 * a2 + b2 * b2);
        double Cbar = (C1 + C2) / 2.0;
        double Cbar7 = Math.pow(Cbar, 7);

        double G = 0.5 * (1.0 - Math.sqrt(Cbar7 / (Cbar7 + Math.pow(25.0, 7))));

        double a1p = a1 * (1.0 + G);
        double a2p = a2 * (1.0 + G);

        double C1p = Math.sqrt(a1p * a1p + b1 * b1);
        double C2p = Math.sqrt(a2p * a2p + b2 * b2);

        double h1p = hpAngle(a1p, b1);
        double h2p = hpAngle(a2p, b2);

        // Step 2: ΔL', ΔC', ΔH'
        double dLp = L2 - L1;
        double dCp = C2p - C1p;

        double dhp;
        if (C1p * C2p == 0.0) {
            dhp = 0.0;
        } else if (Math.abs(h2p - h1p) <= 180.0) {
            dhp = h2p - h1p;
        } else if (h2p - h1p > 180.0) {
            dhp = h2p - h1p - 360.0;
        } else {
            dhp = h2p - h1p + 360.0;
        }

        double dHp = 2.0 * Math.sqrt(C1p * C2p) * Math.sin(Math.toRadians(dhp / 2.0));

        // Step 3: 가중치 함수
        double Lbar = (L1 + L2) / 2.0;
        double Cbarp = (C1p + C2p) / 2.0;

        double hbarp;
        if (C1p * C2p == 0.0) {
            hbarp = h1p + h2p;
        } else if (Math.abs(h1p - h2p) <= 180.0) {
            hbarp = (h1p + h2p) / 2.0;
        } else if (h1p + h2p < 360.0) {
            hbarp = (h1p + h2p + 360.0) / 2.0;
        } else {
            hbarp = (h1p + h2p - 360.0) / 2.0;
        }

        double T = 1.0
                - 0.17 * Math.cos(Math.toRadians(hbarp - 30.0))
                + 0.24 * Math.cos(Math.toRadians(2.0 * hbarp))
                + 0.32 * Math.cos(Math.toRadians(3.0 * hbarp + 6.0))
                - 0.20 * Math.cos(Math.toRadians(4.0 * hbarp - 63.0));

        double SL = 1.0 + 0.015 * Math.pow(Lbar - 50.0, 2)
                / Math.sqrt(20.0 + Math.pow(Lbar - 50.0, 2));
        double SC = 1.0 + 0.045 * Cbarp;
        double SH = 1.0 + 0.015 * Cbarp * T;

        double Cbarp7 = Math.pow(Cbarp, 7);
        double RC = 2.0 * Math.sqrt(Cbarp7 / (Cbarp7 + Math.pow(25.0, 7)));

        double dTheta = 30.0 * Math.exp(-Math.pow((hbarp - 275.0) / 25.0, 2));
        double RT = -Math.sin(Math.toRadians(2.0 * dTheta)) * RC;

        // Step 4: 최종 거리
        double term1 = dLp / SL;
        double term2 = dCp / SC;
        double term3 = dHp / SH;

        return Math.sqrt(term1 * term1 + term2 * term2 + term3 * term3
                + RT * term2 * term3);
    }

    // -------------------------------------------------------
    // 유틸리티
    // -------------------------------------------------------

    private static double hpAngle(double ap, double b) {
        if (ap == 0.0 && b == 0.0) return 0.0;
        double angle = Math.toDegrees(Math.atan2(b, ap));
        return (angle < 0.0) ? angle + 360.0 : angle;
    }

    private static int[] hexToRgb(String hex) {
        hex = hex.replace("#", "").trim();
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new int[]{r, g, b};
    }

    // -------------------------------------------------------
    // 간단한 테스트 (main)
    // -------------------------------------------------------

    public static void main(String[] args) {
        // 동일한 색상 → 0
        System.out.printf("Same color (red):      %.6f%n",
                deltaE2000("#FF0000", "#FF0000"));

        // 거의 비슷한 색상
        System.out.printf("Similar reds:          %.6f%n",
                deltaE2000("#FF0000", "#FE0000"));

        // 빨강 vs 초록
        System.out.printf("Red vs Green:          %.6f%n",
                deltaE2000("#FF0000", "#00FF00"));

        // 흰색 vs 검정
        System.out.printf("White vs Black:        %.6f%n",
                deltaE2000("#FFFFFF", "#000000"));

        // 유명한 테스트 케이스 (Sharma 2005 논문 첫 번째 쌍)
        double[] lab1 = {50.0000, 2.6772, -79.7751};
        double[] lab2 = {50.0000, 0.0000, -82.7485};
        System.out.printf("Sharma pair 1:         %.4f (expected: 2.0425)%n",
                ciede2000(lab1[0], lab1[1], lab1[2],
                        lab2[0], lab2[1], lab2[2]));
    }
}