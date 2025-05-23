package me.kaketuz.cloudy.util.logger.logger;



//YAMETEKYDASAIII


/**
 * Этот enum нужен для подразделения типов вывода в консоль в классе {@link Logger}
 * @see Logger
 * @author KaketuZ
 * @version 1.0
 * @since 1.0.0
 * */
public enum LoggerValues {
    /**
     * Тип - Ошибка
     * @since 1.0.0
     * */
    ERROR,
    /**
     * Тип - Обычный
     * @since 1.0.0
     * */
    GENERAL,
    /**
     * Тип - Успешный
     * @since 1.0.0
     * */
    SUCCESSFULLY,
    /**
     * Тип - Предупреждение
     * @since 1.0.0
     * */
    WARNING,
    /**
     * Тип - Информация
     * @since 1.0.0
     * */
    INFO,
    /**
     * Тип - Кастом
     * @see ANSIValues
     * @since 1.0.0
     * */
    CUSTOM
}
