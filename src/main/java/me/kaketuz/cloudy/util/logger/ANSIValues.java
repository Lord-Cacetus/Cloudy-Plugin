package me.kaketuz.cloudy.util.logger;



/**
 * Данный Enum содержит цвета, которые могут отображаться в консоли в формате ANSI
 * @author KaketuZ
 * @version 1.0
 * @since 1.0.0
 * */
public enum ANSIValues {
    /**
     * Отменяет Цвет, который был до него
     * @since 1.0.0
     * */
    RESET("\u001B[0m"),
    /**
     * Красный Цвет
     * @since 1.0.0
     * */
    RED("\u001B[31m"),
    /**
     * Зелёный Цвет
     * @since 1.0.0
     * */
    GREEN("\u001B[32m"),
    /**
     * Жёлтый Цвет
     * @since 1.0.0
     * */
    YELLOW("\u001B[33m"),
    /**
     * Синий Цвет
     * @since 1.0.0
     * */
    BLUE("\u001B[34m"),
    /**
     * Пурпурный (Фиолетовый) Цвет
     * @since 1.0.0
     * */
    PURPLE("\u001B[35m"),
    /**
     * Голубой Цвет
     * @since 1.0.0
     * */
    LIGHT_BLUE("\u001B[36m"),
    /**
     * Белый Цвет
     * @since 1.0.0
     * */
    WHITE("\u001B[37m");

    private final String code;

    ANSIValues(String code) {
        this.code = code;
    }

    /**
     * Этот метод возвращает код цвета. Если вы используете его на прямую (не в методах класса {@link Logger}),
     * то всегда вызывайте этот метод. Например {@link ANSIValues#YELLOW#toANSI()};
     * @since 1.0.0
     * */
    public String toANSI() {
        return code;
    }
}
