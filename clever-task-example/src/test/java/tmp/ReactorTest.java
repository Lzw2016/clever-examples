package tmp;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/01/02 22:46 <br/>
 */
@Slf4j
public class ReactorTest {
    @SneakyThrows
    @Test
    public void test01() {
        Disposable disposable = Flux.just(1, 2, 3, 4)
            .delayElements(Duration.ofSeconds(1))
            .flatMap(integer -> Mono.just(integer * 2))
            .subscribe(integer -> log.info("-> {}", integer));
        while (!disposable.isDisposed()) {
            //noinspection BusyWait
            Thread.sleep(100);
        }
        log.info("完成");
    }
}
