package cn.edu.whu.indoorscene.util;

/**
 * @auther tanjiajie
 */
public class AccessResourceException extends Exception {

    public AccessResourceException(String message) {
        super(message);
    }

    public AccessResourceException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public AccessResourceException(Throwable throwable) {
        super(throwable);
    }

}
