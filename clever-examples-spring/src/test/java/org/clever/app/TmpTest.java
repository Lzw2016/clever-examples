package org.clever.app;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.clever.task.core.cron.CronExpressionUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

/**
 * 作者：lizw <br/>
 * 创建时间：2025/01/09 15:25 <br/>
 */
@Slf4j
public class TmpTest {

    @SneakyThrows
    @Test
    public void test01() {
        Collection<File> files = FileUtils.listFiles(new File("D:\\SourceCode\\clever\\ease-forge\\packages\\components\\src\\components\\grid"), new String[]{"vue"}, true);
        int sum = 0;
        for (File file : files) {
            List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
            for (String line : lines) {
                sum++;
                if (line.matches(".+\\(params\\.on\\w+ \\?\\? reallyProps\\.on\\w+\\) as .+")) {
                    String[] codes = StringUtils.split(line, " ");
                    int count = StringUtils.countMatches(line, codes[1]);
                    if (count != 4) {
                        log.info("file-> {}", file.getAbsolutePath());
                        log.info("line -> {}", line);
                        log.info("codes -> {}", codes[1]);
                    }
                }
            }
        }
        log.info("sum={}", sum);
    }

    @SneakyThrows
    @Test
    public void test02() {
        String corn = "10 * * * * ?";
        // corn = "10 * * * * *";
        List<String> res = CronExpressionUtil.getNextTimeStrList(corn, 10);
        log.info("res=\n{}", StringUtils.join(res, "\n"));
    }
}
