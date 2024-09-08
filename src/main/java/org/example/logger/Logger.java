package org.example.logger;

import org.example.Color;

public class Logger extends Exception {
    Logger(String message) {
        System.out.println(Color.RED.getCode() + message);
    }

    public static void log(String message) {
        System.out.println(Color.BLUE.getCode() + message);
    }

    public static void log(String message, String body, String end) {
        System.out.println(Color.BLUE.getCode() + message + Color.RED.getCode() + body + Color.CYAN.getCode() + end);
    }

    public static void message(String message) {
        System.out.println(Color.BLUE.getCode() + message);
    }

    public static void message(String message, String body) {
        System.out.println(Color.BLUE.getCode() + message + Color.YELLOW.getCode() + body);
    }

    public static void warning(String message) {
        System.out.println(Color.PURPLE.getCode() + message);
    }

    public static void error(String message) {
        System.out.println(Color.RESET.getCode() + message + Color.RED.getCode());
    }

    public static void error(String message, String exception) {
        System.out.println(Color.RESET.getCode() + message + Color.RED.getCode() + exception);
    }

}
