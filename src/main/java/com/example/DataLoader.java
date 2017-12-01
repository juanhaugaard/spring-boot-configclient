package com.example;

import com.example.entities.ActionImportBean;
import com.example.entities.AssignmentImportBean;
import com.example.entities.DelegationImportBean;
import com.example.entities.ObjectImportBean;
import com.example.entities.PrivilegeImportBean;
import com.example.entities.RoleImportBean;
import com.example.entities.ScopeGroupImportBean;
import com.example.entities.ScopeTypeImportBean;
import com.example.entities.SubjectImportBean;
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
import java.util.List;

@Slf4j
@Order(1)
@Component
public class DataLoader implements ApplicationRunner {

    public static final String PARAM = "csv-dir";

    private ApplicationContext context;

    @Autowired
    public DataLoader(final ApplicationContext context) {
        this.context = context;
    }

    /**
     * Callback used to run the bean.
     *
     * @param args incoming application arguments
     * @throws Exception on error
     */
    @Override
    public void run(final ApplicationArguments args) {
        if (!args.containsOption(PARAM)) {
            return;
        }

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

    private void processCsvDir(final String csvDir) throws IOException {
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
        String name = csvFile.getName();
        if (name.toLowerCase().startsWith("subjects")) {
            bean = context.getBean(SubjectImportBean.class);
        } else if (name.toLowerCase().startsWith("objects")) {
            bean = context.getBean(ObjectImportBean.class);
        } else if (name.toLowerCase().startsWith("actions")) {
            bean = context.getBean(ActionImportBean.class);
        } else if (name.toLowerCase().startsWith("scopetypes")) {
            bean = context.getBean(ScopeTypeImportBean.class);
        } else if (name.toLowerCase().startsWith("scopegroups")) {
            bean = context.getBean(ScopeGroupImportBean.class);
        } else if (name.toLowerCase().startsWith("privileges")) {
            bean = context.getBean(PrivilegeImportBean.class);
        } else if (name.toLowerCase().startsWith("roles")) {
            bean = context.getBean(RoleImportBean.class);
        } else if (name.toLowerCase().startsWith("assignments")) {
            bean = context.getBean(AssignmentImportBean.class);
        } else if (name.toLowerCase().startsWith("delegations")) {
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
