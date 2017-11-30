package com.example;

import com.example.subjects.SubjectImportBean;
import gov.pmm.common.util.csv.CsvImportBean;
import gov.pmm.common.util.csv.CsvItemResult;
import gov.pmm.common.util.csv.CsvResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Order(1)
@Component
public class DataLoader implements ApplicationRunner {

    public static final String PARAM = "csv-dir";

    private ApplicationContext context;

    @Autowired
    public DataLoader(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Callback used to run the bean.
     *
     * @param args incoming application arguments
     * @throws Exception on error
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!args.containsOption(PARAM)) {
            return;
        }

        for (String optionValue : args.getOptionValues(PARAM)) {
            String[] csvDirs = optionValue.split(",");
            for (int i = 0; i < csvDirs.length; i++)
                try {
                    processCsvDir(csvDirs[i].trim());
                } catch (Exception e) {
                    log.error("Failed to process directory {}, {}", csvDirs[i], e.getMessage());
                }
        }
    }

    private void processCsvDir(String csvDir) throws IOException {
        if (StringUtils.isEmpty(csvDir)) throw new IllegalArgumentException("Can not process empty CSV directory name");
        log.debug("CSV Processing directory: {}", csvDir);
        File file = new File(csvDir);
        if (!file.isDirectory()) throw new IllegalArgumentException(csvDir + " is not a directory");
        for (String filename : file.list()) {
            if (filename.toLowerCase().endsWith(".csv")) {
                String fullFilename = file.getName() + File.separator + filename;
                File csvFile = new File(fullFilename);
                if (csvFile.isFile()) {
                    processCsvFile(csvFile);
                } else
                    log.warn("{} is not a file", fullFilename);
            }
        }
    }

    private void processCsvFile(File csvFile) throws IOException {
        CsvImportBean bean = null;
        String name = csvFile.getName();
        if (name.toLowerCase().startsWith("subjects")) {
            bean = context.getBean(SubjectImportBean.class);
        }
        if (bean != null) {
            CsvResult result = bean.readInputStream(new FileInputStream(csvFile));
            log.debug("Result of reading {}: total {}, errors {}", name, result.getTotalCount(), result.getErrorCount());
            for (CsvItemResult itemResult : result.getItems()){
                log.debug("\t{}\t{}",itemResult.getStatus(),itemResult.getDescription());
            }
        }
    }
}
