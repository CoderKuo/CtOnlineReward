package cn.ctcraft.ctonlinereward.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Arithmetic {

    public static boolean exc(String express) {
        List<String> split = split(express);
        for (int i = 0; i < split.size(); i++) {
            String s = split.get(i);
            if (!s.contains("&") && !s.contains("|")) {
                String transfer1 = transfer(initExpress(s));
                boolean b1 = calcRevPolishNotationPlus(transfer1);
                split.set(i, String.valueOf(b1));
            }
        }
        return judge(split);
    }

    public static Integer mathExc(String express) {
        String transfer = transfer(initExpress(express));
        return calcRevPolishNotation(transfer);
    }

    private static boolean judge(List<String> express) {
        boolean b = false;
        if (express.size() == 1) {
            return Boolean.parseBoolean(express.get(0));
        }
        for (int i = 0; i < express.size(); i++) {
            String s = express.get(i);
            if (s.equals("&&")) {
                b = Boolean.parseBoolean(express.get(i - 1)) && Boolean.parseBoolean(express.get(i + 1));
            } else if (s.equals("||")) {
                b = Boolean.parseBoolean(express.get(i - 1)) || Boolean.parseBoolean(express.get(i + 1));
            }
        }
        return b;
    }

    private static List<String> split(String express) {
        Stack<String> stack = new Stack<>();
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < express.length(); i++) {
            if (!(express.charAt(i) + "").matches("[\\&\\|]")) {
                stack.push(express.charAt(i) + "");
            } else if (!stack.lastElement().equals(express.charAt(i) + "")) {
                stack.push(express.charAt(i) + "");
            } else if (stack.lastElement().equals(express.charAt(i) + "")) {
                String k1 = stack.pop();
                Stack<String> stack1 = new Stack<>();
                while (!stack.isEmpty()) {
                    String pop = stack.pop();
                    stack1.push(pop);
                }
                StringBuffer sb = new StringBuffer();
                while (!stack1.isEmpty()) {
                    sb.append(stack1.pop());
                }
                strings.add(sb.toString());
                strings.add(k1 + express.charAt(i) + "");
            }
        }
        Stack<String> stack1 = new Stack<>();
        while (!stack.isEmpty()) {
            String pop = stack.pop();
            stack1.push(pop);
        }
        StringBuffer sb = new StringBuffer();
        while (!stack1.isEmpty()) {
            sb.append(stack1.pop());
        }
        strings.add(sb.toString());
        return strings;
    }


    private static String initExpress(String exp) {
        String reStr = null;
        reStr = exp.replaceAll("\\s", "");
        if (reStr.startsWith("-")) {
            reStr = "0" + reStr;
        }
        reStr = reStr.replaceAll("\\(\\-", "(0-");
        return reStr;
    }

    public static String transfer(String express) {
        Stack<String> stack = new Stack<>();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < express.length(); i++) {
            if ((express.charAt(i) + "").matches("\\d")) {
                list.add(express.charAt(i) + "");
            } else if ((express.charAt(i) + "").matches("[\\+\\-\\*\\/\\>\\<\\=\\!]")) {
                if (stack.isEmpty()) {
                    stack.push(express.charAt(i) + "");
                    continue;
                }
                while (!stack.isEmpty() && !stack.lastElement().equals("(") && !comparePriority(express.charAt(i) + "", stack.lastElement())) {
                    list.add(stack.pop());
                }
                stack.push(express.charAt(i) + "");
            } else if (express.charAt(i) == '(') {
                stack.push(express.charAt(i) + "");
            } else if (express.charAt(i) == ')') {
                while (!("(").equals(stack.lastElement())) {
                    list.add(stack.pop());
                }
                stack.pop();
            }
        }
        while (!stack.isEmpty()) {
            list.add(stack.pop());
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (String s : list) {
            stringBuffer.append(s);
        }
        return stringBuffer.toString();
    }


    public static boolean comparePriority(String o1, String o2) {
        return getPriorityValue(o1) > getPriorityValue(o2);
    }

    private static int getPriorityValue(String str) {
        switch (str) {
            case "+":
                return 5;
            case "-":
                return 5;
            case "*":
                return 10;
            case "/":
                return 10;
            case "=":
                return 4;
            case ">":
                return 3;
            case "<":
                return 3;
            case "!":
                return 3;
            default:
                throw new RuntimeException("没有该类型的运算符!");
        }
    }

    public static boolean calcRevPolishNotationPlus(String express) {
        boolean b = false;
        Stack<String> stack = new Stack<>();
        for (int i = 0; i < express.length(); i++) {
            if ((express.charAt(i) + "").matches("\\d")) {
                stack.push(express.charAt(i) + "");
            } else if ((express.charAt(i) + "").matches("[\\=]")) {
                if (!stack.lastElement().matches("[\\=]")) {
                    stack.push(express.charAt(i) + "");
                } else {
                    String k1 = stack.pop();
                    String k2 = stack.pop();
                    String k3 = stack.pop();
                    b = Double.parseDouble(k2) == Double.parseDouble(k3);
                }
            } else if ((express.charAt(i) + "").matches("[\\+\\-\\*\\/]")) {
                String k1 = stack.pop();
                String k2 = stack.pop();
                Integer res = calcValue(k1, k2, (express.charAt(i) + ""));
                stack.push(res + "");
            } else if ((express.charAt(i) + "").matches("[\\>\\<\\!]")) {
                if (stack.lastElement().matches("\\d")) {
                    String k1 = stack.pop();
                    String k2 = stack.pop();
                    b = calcValueBoolean(k2, k1, express.charAt(i) + "");
                } else if (stack.lastElement().matches("[\\=]")) {
                    String k1 = stack.pop();
                    String k2 = stack.pop();
                    String k3 = stack.pop();
                    b = calcValueBoolean(k3, k2, express.charAt(i) + "=");
                }
            }
        }
        return b;
    }

    public static boolean calcValueBoolean(String k1, String k2, String c) {
        switch (c) {
            case ">":
                return Double.parseDouble(k1) > Double.parseDouble(k2);
            case "<":
                return Double.parseDouble(k1) < Double.parseDouble(k2);
            case "<=":
                return Double.parseDouble(k1) <= Double.parseDouble(k2);
            case ">=":
                return Double.parseDouble(k1) >= Double.parseDouble(k2);
            case "!=":
                return Double.parseDouble(k1) != Double.parseDouble(k2);
            case "==":
                return Double.parseDouble(k1) == Double.parseDouble(k2);
            default:
                throw new RuntimeException("没有该类型的运算符!");
        }
    }

    public static Integer calcRevPolishNotation(String express) {
        Stack<String> stack = new Stack<>();
        for (int i = 0; i < express.length(); i++) {
            if ((express.charAt(i) + "").matches("\\d")) {
                stack.push(express.charAt(i) + "");
            } else if ((express.charAt(i) + "").matches("[\\+\\-\\*\\/]")) {
                String k1 = stack.pop();
                String k2 = stack.pop();
                Integer res = calcValue(k1, k2, (express.charAt(i) + ""));
                stack.push(res + "");
            }
        }
        return Integer.parseInt(stack.pop());
    }

    private static Integer calcValue(String k1, String k2, String c) {
        switch (c) {
            case "+":
                return Integer.parseInt(k1) + Integer.parseInt(k2);
            case "-":
                return Integer.parseInt(k2) - Integer.parseInt(k1);
            case "*":
                return Integer.parseInt(k1) * Integer.parseInt(k2);
            case "/":
                return Integer.parseInt(k2) / Integer.parseInt(k1);
            default:
                throw new RuntimeException("没有该类型的运算符!");
        }
    }
}
