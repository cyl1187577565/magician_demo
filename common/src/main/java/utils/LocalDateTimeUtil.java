package utils;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class LocalDateTimeUtil {
    /**
     * 获取到时间戳
     * @param time
     * @return
     */
    public static Long toMillisecond(LocalDateTime time) {
        if (time == null) {
            return 0L;
        }
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

}
