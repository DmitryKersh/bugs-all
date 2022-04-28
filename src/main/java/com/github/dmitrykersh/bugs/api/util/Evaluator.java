package com.github.dmitrykersh.bugs.api.util;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains methods to work with arithmetic equations.
 * Note: All arithmetic signs except unary minus should be prefixed with '$' and surrounded with spaces
 *
 * i.e.    evaluateSimpleEquation("2 $+ 2 $* 2") -> "6"
 *         evaluateSimpleEquation(2$+2) -> "2$+2"
 */

public class Evaluator {
    public static final String ARITH_SIGN_PREFIX = "\\$";
    public static final String DIGIT = "[\\d\\-]";
    public static final String DECIMAL = "-?\\d+(\\.\\d+)?";
    public static final Pattern ARITH_PRIOR_1 = Pattern.compile(DECIMAL + "\\s\\$\\^\\s" + DECIMAL);
    public static final Pattern ARITH_PRIOR_2 = Pattern.compile(DECIMAL + "\\s\\$[*/]\\s" + DECIMAL);
    public static final Pattern ARITH_PRIOR_3 = Pattern.compile(DECIMAL + "\\s\\$[+-]\\s" + DECIMAL);

    public static String evaluateSimpleEquation(final String str) {
        String s = str;

        Matcher prior1Matcher = ARITH_PRIOR_1.matcher(s);
        while (prior1Matcher.find()) {
            int start = prior1Matcher.start();
            int end = prior1Matcher.end();

            String operation = s.substring(start, end);
            String[] values = operation.split(ARITH_SIGN_PREFIX);

            double op1 = Double.parseDouble(values[0]);
            double op2 = Double.parseDouble(values[1].substring(1));

            Double res = Math.pow(op1, op2);

            String decimalFormatPattern = String.valueOf(res).replaceAll(DIGIT, "#");
            DecimalFormat decimalFormat = new DecimalFormat(decimalFormatPattern);

            s = prior1Matcher.replaceFirst(decimalFormat.format(res));
            prior1Matcher.reset(s);
        }

        Matcher prior2Matcher = ARITH_PRIOR_2.matcher(s);
        while (prior2Matcher.find()) {
            int start = prior2Matcher.start();
            int end = prior2Matcher.end();

            String operation = s.substring(start, end);
            String[] values = operation.split(ARITH_SIGN_PREFIX);


            char operator = values[1].charAt(0);
            values[1] = values[1].substring(1);
            double op1 = Double.parseDouble(values[0]);
            double op2 = Double.parseDouble(values[1]);

            Double res = 0.0;

            switch (operator) {
                case '*': {
                    res = op1 * op2;
                    break;
                }
                case '/': {
                    res = op1 / op2;
                    break;
                }
            }
            String decimalFormatPattern = String.valueOf(res).replaceAll(DIGIT, "#");
            DecimalFormat decimalFormat = new DecimalFormat(decimalFormatPattern);

            s = prior2Matcher.replaceFirst(decimalFormat.format(res));
            prior2Matcher.reset(s);
        }

        Matcher prior3Matcher = ARITH_PRIOR_3.matcher(s);
        while (prior3Matcher.find()) {
            int start = prior3Matcher.start();
            int end = prior3Matcher.end();

            String operation = s.substring(start, end);
            String[] values = operation.split(ARITH_SIGN_PREFIX);


            char operator = values[1].charAt(0);
            values[1] = values[1].substring(1);
            double op1 = Double.parseDouble(values[0]);
            double op2 = Double.parseDouble(values[1]);

            Double res = 0.0;

            switch (operator) {
                case '+': {
                    res = op1 + op2;
                    break;
                }
                case '-': {
                    res = op1 - op2;
                    break;
                }
            }
            String decimalFormatPattern = String.valueOf(res).replaceAll(DIGIT, "#");
            DecimalFormat decimalFormat = new DecimalFormat(decimalFormatPattern);

            s = prior3Matcher.replaceFirst(decimalFormat.format(res));
            prior3Matcher.reset(s);
        }

        return s;
    }

    public static int evaluateSimpleEquationAsInt(String s) {
        Double value = Double.parseDouble(evaluateSimpleEquation(s));
        return value.intValue();
    }
}
