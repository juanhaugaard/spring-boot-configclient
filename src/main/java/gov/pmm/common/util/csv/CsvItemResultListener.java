/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.common.util.csv;

/**
 * Created by juan.haugaard on 1/18/2017.
 */
public interface CsvItemResultListener {
    void onNext(CsvItemResult csvItemResult);

    void onCompleted();

    void onError(Throwable throwable);
}
