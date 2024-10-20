package ngo.nabarun.app.api.config;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import java.util.Map;

public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
   
    
    
//    @Override
//    public <T> Callable<T> decorate(Callable<T> callable) {
//        Map<String, String> contextMap = MDC.getCopyOfContextMap();
//        return () -> {
//            try {
//                if (contextMap != null) {
//                    MDC.setContextMap(contextMap);
//                }
//                return callable.call();
//            } finally {
//                MDC.clear();
//            }
//        };
//    }
}

