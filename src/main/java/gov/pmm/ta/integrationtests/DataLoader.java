/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.ta.integrationtests;

import gov.pmm.common.util.csv.CsvImportBean;
import gov.pmm.common.util.csv.CsvItemResult;
import gov.pmm.common.util.csv.CsvResult;
import gov.pmm.ta.integrationtests.domain.ActionImportBean;
import gov.pmm.ta.integrationtests.domain.DelegationImportBean;
import gov.pmm.ta.integrationtests.domain.FileOrder;
import gov.pmm.ta.integrationtests.domain.LocalAssignmentImportBean;
import gov.pmm.ta.integrationtests.domain.ObjectImportBean;
import gov.pmm.ta.integrationtests.domain.PrivilegeImportBean;
import gov.pmm.ta.integrationtests.domain.RoleImportBean;
import gov.pmm.ta.integrationtests.domain.ScopeGroupImportBean;
import gov.pmm.ta.integrationtests.domain.ScopeImportBean;
import gov.pmm.ta.integrationtests.domain.ScopeTypeImportBean;
import gov.pmm.ta.integrationtests.domain.SubjectImportBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class DataLoader implements ApplicationRunner {
    public static final String PARAM = "csv-dir";
    private ApplicationContext context;

    @Autowired
    public DataLoader(ApplicationContext context) {
        this.context = context;
    }

    public void run(ApplicationArguments args) {
        if (!args.containsOption(PARAM)) {
            log.info("Skiping DataLoader, argument '{}' not found", PARAM);
            return;
        }
        log.info("DataLoader started with option names : {}", args.getOptionNames());
        log.info("DataLoader started with command-line arguments: {}", Arrays.toString(args.getSourceArgs()));
        log.info("NonOptionArgs: {}", args.getNonOptionArgs());
        log.info("OptionNames: {}", args.getOptionNames());

        for (String optionValue : args.getOptionValues(PARAM)) {
            String[] csvDirs = optionValue.split(",");
            for (int i = 0; i < csvDirs.length; i++) {
                try {
                    processCsvDir(csvDirs[i].trim());
                } catch (Exception e) {
                    log.error("Failed to process directory {}, {}", csvDirs[i], e.getMessage());
                }
            }
        }
    }

    private void processCsvDir(final String csvDir) {
        if (StringUtils.isEmpty(csvDir)) {
            throw new IllegalArgumentException("Can not process empty CSV directory name");
        }
        log.debug("CSV Processing directory: {}", csvDir);
        File file = new File(csvDir);
        if (!file.isDirectory()) {
            throw new IllegalArgumentException(csvDir + " is not a directory");
        }
        List<String> orderedFiles = FileOrder.sortAndFilter(file.list());
        orderedFiles.forEach(filename -> {
                    String fullFilename = file.getName() + File.separator + filename;
                    File csvFile = new File(fullFilename);
                    if (!csvFile.isFile()) {
                        log.warn("{} is not a file", fullFilename);
                    } else {
                        processCsvFile(csvFile);
                    }
                }
        );
    }

    private void processCsvFile(final File csvFile) {
        CsvImportBean bean = null;
        String[] names = (String[]) FileOrder.FILE_ORDER.toArray();
        String name = csvFile.getName();
        if (name.toLowerCase().startsWith(names[0])) {
            bean = context.getBean(SubjectImportBean.class);
        } else if (name.toLowerCase().startsWith(names[1])) {
            bean = context.getBean(ObjectImportBean.class);
        } else if (name.toLowerCase().startsWith(names[2])) {
            bean = context.getBean(ActionImportBean.class);
        } else if (name.toLowerCase().startsWith(names[3])) {
            bean = context.getBean(ScopeTypeImportBean.class);
        } else if (name.toLowerCase().startsWith(names[4])) {
            bean = context.getBean(ScopeImportBean.class);
        } else if (name.toLowerCase().startsWith(names[5])) {
            bean = context.getBean(ScopeGroupImportBean.class);
        } else if (name.toLowerCase().startsWith(names[6])) {
            bean = context.getBean(PrivilegeImportBean.class);
        } else if (name.toLowerCase().startsWith(names[7])) {
            bean = context.getBean(RoleImportBean.class);
        } else if (name.toLowerCase().startsWith(names[8])) {
            bean = context.getBean(LocalAssignmentImportBean.class);
        } else if (name.toLowerCase().startsWith(names[9])) {
            bean = context.getBean(DelegationImportBean.class);
        }
        if (bean != null) {
            try {
                CsvResult result = bean.readInputStream(new FileInputStream(csvFile));
                log.debug("Result of reading {}: total {}, errors {}", name, result.getTotalCount(), result.getErrorCount());
                for (CsvItemResult itemResult : result.getItems()) {
                    log.debug("\t{}\t{}", itemResult.getStatus(), itemResult.getDescription());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}



