package com.crimemanagement.util;

import com.formdev.flatlaf.FlatClientProperties;

public class PropertyDiagnostic {
    public static void main(String[] args) {
        try {
            System.out.println("STYLE key: " + FlatClientProperties.STYLE);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
