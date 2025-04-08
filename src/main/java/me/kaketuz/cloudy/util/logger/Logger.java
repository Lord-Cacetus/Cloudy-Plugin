package me.kaketuz.cloudy.util.logger;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;



//so many russian words :O
//P.S its just from my lib, and im too lazy to delete info xd



/**
 * Этот класс предназначен для удобной работы с Консолью ({@link Bukkit#getLogger()})
 *<p>
 * Также класс работает с Enum: {@link ANSIValues}, {@link LoggerValues}
 * @author KaketuZ
 * @version 1.0
 * @since 1.0.0
 *
 * @see ANSIValues
 * @see LoggerValues
 * */
public final class Logger {

    /**
     * Этот метод отправляет в консоль сообщение с оформлением из предложенного
     * @param message сюда вы пишете то, что будет выведено в консоль
     * @param val enum который определяет тип оформления текста
     * @see LoggerValues
     * @since 1.0.0
     * */
    public static void sendToConsole(String message, @NotNull LoggerValues val) {
        switch (val) {
            case ERROR -> Bukkit.getLogger().severe(ANSIValues.RED.toANSI() + message + ANSIValues.RESET.toANSI());
            case WARNING -> Bukkit.getLogger().warning(ANSIValues.YELLOW.toANSI() + message + ANSIValues.RESET.toANSI());
            case GENERAL, CUSTOM -> Bukkit.getLogger().info(message);
            case SUCCESSFULLY -> Bukkit.getLogger().info(ANSIValues.GREEN.toANSI() + message + ANSIValues.RESET.toANSI());
            case INFO -> Bukkit.getLogger().info(ANSIValues.BLUE.toANSI() + message + ANSIValues.RESET.toANSI());
        }
    }
    /**
     * Этот метод отправляет в консоль сообщение с оформлением из предложенного
     * @param message сюда вы пишете то, что будет выведено в консоль
     * @param val enum который определяет тип оформления текста
     * @param ANSI_VAL если val = {@link LoggerValues#CUSTOM}, то этот аргумент нужен для выбора цвета текста из предложенного
     * @see LoggerValues
     * @see ANSIValues
     * @since 1.0.0
     * */
    public static void sendToConsole(String message, @NotNull LoggerValues val, @NotNull ANSIValues ANSI_VAL) {
        switch (val) {
            case ERROR -> Bukkit.getLogger().severe(ANSIValues.RED.toANSI() + message + ANSIValues.RESET.toANSI());
            case WARNING -> Bukkit.getLogger().warning(ANSIValues.YELLOW.toANSI() + message + ANSIValues.RESET.toANSI());
            case GENERAL -> Bukkit.getLogger().info(message);
            case SUCCESSFULLY -> Bukkit.getLogger().info(ANSIValues.GREEN.toANSI() + message + ANSIValues.RESET.toANSI());
            case INFO -> Bukkit.getLogger().info(ANSIValues.BLUE.toANSI() + message + ANSIValues.RESET.toANSI());
            case CUSTOM -> Bukkit.getLogger().info(ANSI_VAL + message + ANSIValues.RESET.toANSI());
        }
    }

    /**
     * Отправляет обычный текст в консоль
     * @param message текст, который отправится в консоль
     * @since 1.0.0
     * */
    public static void sendGeneral(String message) {
        Bukkit.getLogger().info(message);
    }
    /**
     * Отправляет предупреждение в консоль
     * @param message текст, который отправится в консоль
     * @since 1.0.0
     * */
    public static void sendWarning(String message) {
        Bukkit.getLogger().warning(ANSIValues.YELLOW.toANSI() + message + ANSIValues.RESET.toANSI());
    }
    /**
     * Отправляет ошибку в консоль
     * @param message текст, который отправится в консоль
     * @since 1.0.0
     * */
    public static void sendError(String message) {
        Bukkit.getLogger().severe(ANSIValues.RED.toANSI() + message + ANSIValues.RESET.toANSI());
    }
    /**
     * Отправляет текст для оповещения завершенности или успешности операции в консоль
     * @param message текст, который отправится в консоль
     * @since 1.0.0
     * */
    public static void sendSuccessfully(String message) {
        Bukkit.getLogger().info(ANSIValues.GREEN.toANSI() + message + ANSIValues.RESET.toANSI());
    }
    /**
     * Отправляет информацию в консоль
     * @param message текст, который отправится в консоль
     * @since 1.0.0
     * */
    public static void sendInfo(String message) {
        Bukkit.getLogger().info(ANSIValues.GREEN.toANSI() + message + ANSIValues.RESET.toANSI());
    }
    /**
     * Отправляет кастомно-оформленный текст в консоль
     * @param val тип цвета текста
     * @param message текст, который отправится в консоль
     * @since 1.0.0
     * */
    public static void sendCustom(String message, ANSIValues val) {
        Bukkit.getLogger().info(val.toANSI() + message + ANSIValues.RESET.toANSI());
    }



}
