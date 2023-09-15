package com.github.dmitrykersh.bugs.engine.util;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains methods to work with arithmetic equations.
 * Note: All arithmetic signs except unary minus should be prefixed with '$' and surrounded with spaces
 *
 * i.e.    evaluateSimpleEquation("2 $+ 2 $* 2") -> "6" (not 8 LOL)
 *         evaluateSimpleEquation(2$+2) -> "2$+2"
 */

public class Evaluator {
    public static final String ARITH_SIGN_PREFIX = "\\$";
    public static final String DIGIT = "[\\d\\-]";
    public static final String DECIMAL = "-?\\d+(\\.\\d+)?";

    public static final Pattern ARITH_PRIOR_1 = Pattern.compile(DECIMAL + "\\s\\$\\^\\s" + DECIMAL);
    public static final Pattern ARITH_PRIOR_2 = Pattern.compile(DECIMAL + "\\s\\$[*/]\\s" + DECIMAL);
    public static final Pattern ARITH_PRIOR_3 = Pattern.compile(DECIMAL + "\\s\\$[+-]\\s" + DECIMAL);
    public static final Pattern ARITH_BRACKETS_PATTERN = Pattern.compile(
            "\\(" + DECIMAL + "(\\s\\$[+\\-/*\\^]\\s" + DECIMAL + ")+\\)");

    /**
     * Evaluates arithmetic equation without brackets (resolves all arithmetic actions). Equation may contain other
     * symbols, they will be preserved.
     *
     * i.e. "foo21 $+ 12bar" ---> "foo33bar"
     * @param str equation
     * @return result
     */
    public static String evaluateSimpleEquation(final @NotNull String str) {
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

    public static int evaluateSimpleEquationAsInt(final @NotNull String str) {
        Double value = Double.parseDouble(evaluateSimpleEquation(str));
        return value.intValue();
    }

    /**
     * Evaluates arithmetic equation with or without brackets (resolves all arithmetic actions).
     * Equation may contain other symbols, they will be preserved.
     *
     * i.e. "(2 $+ 2) $* 2" ---> "8"
     * @param s equation
     * @return result
     */
    public static String evaluateComplexEquation(final @NotNull String s) {
        String equation = s;
        Matcher arithBracketsMatcher = ARITH_BRACKETS_PATTERN.matcher(equation);
        arithBracketsMatcher.reset(equation);
        // evaluate all brackets
        while (arithBracketsMatcher.find()){
            int start = arithBracketsMatcher.start();
            int end = arithBracketsMatcher.end();

            String bracket_part = equation.substring(start + 1, end - 1);

            equation = arithBracketsMatcher.replaceFirst(evaluateSimpleEquation(bracket_part));
            arithBracketsMatcher.reset(equation);
        }
        // evaluate remaining equation
        return evaluateSimpleEquation(equation);
    }

    public static int evaluateComplexEquationAsInt(String s) {
        Double value = Double.parseDouble(evaluateComplexEquation(s));
        return value.intValue();
    }
}
