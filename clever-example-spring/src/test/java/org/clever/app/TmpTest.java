package org.clever.app;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.clever.data.jdbc.support.SqlUtils;
import org.clever.task.core.cron.CronExpressionUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        corn = "* * * ? * 1,2 *";
        List<String> res = CronExpressionUtil.getNextTimeStrList(corn, 10);
        log.info("res=\n{}", StringUtils.join(res, "\n"));
    }

    @Test
    public void test03() {
        Class<?> clazz = SqlUtils.class;
        java.security.ProtectionDomain pd = clazz.getProtectionDomain();
        java.security.CodeSource cs = pd.getCodeSource();
        java.net.URL location = cs.getLocation();
        if (location != null) {
            log.info("-> {}", location.getPath());
        }
    }

    @SneakyThrows
    @Test
    public void test04() {
        Set<String> strSet = new HashSet<>();
        List<String> result = new ArrayList<>();
        Collection<File> files = FileUtils.listFiles(new File("D:\\SourceCode\\jztd-new\\uiFramework\\wms-ui"), new String[]{"js", "vue"}, true);
        for (File file : files) {
            if (file.getAbsolutePath().endsWith(".design.js") || file.getAbsolutePath().contains("\\widget\\") || true) {
                log.info("--> {}", file.getAbsolutePath());
                String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                String regex = "[\\u4e00-\\u9fff][^\\r\\n]*[\\u4e00-\\u9fff]";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    String word = StringUtils.trim(matcher.group()).replaceAll("", "").replaceAll("[^\\u4e00-\\u9fff]+", "{?}");
                    if (strSet.add(word)) {
                        result.add(word);
                    }
                }
            }
        }
        log.info("\n{} \n", StringUtils.join(result, ",\n"));
        FileUtils.writeStringToFile(new File("D:\\翻译.csv"), StringUtils.join(result, "\n"), StandardCharsets.UTF_8);
    }
}
