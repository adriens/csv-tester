/**
 * This dummy maven project has been created to test that csv based resources
 * fit some quality requirements.
 *
 * This project only consists (for now) in a single test class that does the
 * job.
 *
 * In case you would like to enhance it, please fill free to make some PR
 * on the repo : https://github.com/adriens/csv-tester
 *
 * To use this code, just copy this test and put the required dependencies
 * with the scoe test as made on this project.
 *
 */
package com.github.adriens.csv.tester;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.BodyContentHandler;
import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;

public class CsvChecksTest {

    final static Logger logger = LoggerFactory.getLogger(CsvChecksTest.class);

    private static Collection<File> csvFiles;
    public static final String[] CSV_SUFFIX = {"csv", "CSV"};

    public CsvChecksTest() {
        loadCsvFiles();
    }

    public void loadCsvFiles() {
        File rootDir = new File("src");
        this.csvFiles = FileUtils.listFiles(rootDir, CsvChecksTest.CSV_SUFFIX, true);
    }

    @Test
    @Tag("csv")
    @Tag("extension")
    @DisplayName("Checks that all csv files extension strictly are csv")
    void checkCsvFileExtension() {
        Iterator<File> filesIter = this.csvFiles.iterator();
        while (filesIter.hasNext()) {
            File lFile = filesIter.next();
            String extension = FilenameUtils.getExtension(lFile.getName());
            Assert.assertEquals("Extension should only be <csv> not <CSV> or any other : ", extension.toLowerCase(), extension);
            logger.debug("Found <" + lFile.getName() + ">");
        }
    }

    @Test
    @DisplayName("Check that filename is lower case")
    void checkFileNameCase() {
        Iterator<File> filesIter = this.csvFiles.iterator();
        while (filesIter.hasNext()) {
            File lFile = filesIter.next();
            String fileName = FilenameUtils.getName(lFile.getName());
            Assert.assertEquals("Filename should only be lowercase : ", fileName.toLowerCase(), fileName);
            logger.debug("Found <" + lFile.getName() + ">");
        }
    }

    @Test
    @Tag("mime-type")
    @Tag("text/csv")
    @DisplayName("Detect csv mime-type from Tika and check that extension fits our needs.")
    void detectCsvByMimeType() throws Exception {

        // get all files (not only the csv pattern ;-p... stil in src folder
        File dir = new File("src");
        Iterator<File> filesIter = FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).iterator();

        Tika tika = new Tika();
        ContentHandler contenthandler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream is = null;

        while (filesIter.hasNext()) {
            File lFile = filesIter.next();
            logger.debug("Detecting mime-type for <" + lFile.getPath() + ">");
            String filetype = tika.detect(lFile);
            logger.debug("Detected mime-type : <" + filetype + "> for <" + lFile.getPath() + ">");
            if (filetype.equals("text/csv")) {
                logger.info("<" + filetype + "> mime-type detected on <" + lFile + ">");
                Assert.assertEquals("csv file name extension is not correct.", "csv", FilenameUtils.getExtension(lFile.getName()));
            }
        }
    }

    @Test
    @Tag("encoding")
    @Tag("charset")
    @Tag("UTF-8")
    void detectCsvCharacterEncoding() throws Exception {
        Iterator<File> filesIter = this.csvFiles.iterator();

        while (filesIter.hasNext()) {
            File lFile = filesIter.next();

            logger.debug("Detecting character encoding for <" + lFile.getPath() + ">");
            //
            byte[] buf = new byte[4096];
            FileInputStream fis = new FileInputStream(lFile);
            UniversalDetector detector = new UniversalDetector();
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            //
            detector.dataEnd();

            //
            String encoding = detector.getDetectedCharset();
            if (encoding != null) {
                logger.info("Detected encoding for file <" + lFile.getPath() + "> : <" + encoding + ">");
                Assert.assertEquals("File encoding of <" + lFile.getPath() + "> is not the expected one", "UTF-8", encoding);
            } else {
                logger.warn("No encoding could be detected on <" + lFile.getPath() + "> : do \"something\" if you can dude ;-p");
            }
            detector.reset();

        }
    }

    @Test
    @DisplayName("Test wether the number of columns in the same on each row.")
    void checkColumnNumber() throws Exception {
        Iterator<File> filesIter = this.csvFiles.iterator();
        while (filesIter.hasNext()) {
            File lFile = filesIter.next();
            String fileName = FilenameUtils.getName(lFile.getName());
            logger.debug("Found <" + lFile.getName() + ">");
            logger.debug("File path <" + lFile.getPath() + ">");

            boolean loadableCsv;
            String exMesg = null;
            Reader in = new FileReader(lFile.getPath());
            Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
            boolean compliant;
            int prevNbColumns = 0;
            int currentNbColumns = 0;

            int i = 0;
            for (CSVRecord record : records) {
                if (i == 0) {
                    //first row of the file
                    currentNbColumns = record.size();
                    prevNbColumns = record.size();
                } else {
                    prevNbColumns = currentNbColumns;
                    currentNbColumns = record.size();
                }
                Assert.assertEquals("All rows (see row <" + (i + 1) + "> of file <" + lFile.getPath() + "> : ) should have the same number of columns. ", prevNbColumns, currentNbColumns);
                i++;
            }

        }
    }

    @Test
    @Tag("RFC4180")
    @DisplayName("Test if csv are strictly compliant with RFC4180.")
    void wellFormatedCsvFilesForRFC4180() throws IOException {
        Iterator<File> filesIter = this.csvFiles.iterator();
        while (filesIter.hasNext()) {
            File lFile = filesIter.next();
            String fileName = FilenameUtils.getName(lFile.getName());
            logger.debug("Found <" + lFile.getName() + ">");
            logger.debug("File path <" + lFile.getPath() + ">");

            boolean loadableCsv;
            String exMesg = null;
            Reader in = new FileReader(lFile.getPath());
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            boolean compliant;
            String errMsg = null;
            String recordValue;

            // todo : get nb columns
            int j = 1;
            try {
                for (CSVRecord record : records) {
                    j++;
                    logger.info("<" + record.get(0) + ">");
                    for (int i = 0; i < record.size(); i++) {
                        recordValue = record.get(i);
                        Assert.assertEquals("Datas should be trimmed (see line <" + j + "> column <" + (i + 1) + "> on <" + recordValue + ">)", recordValue.trim(), recordValue);
                    }
                }
                compliant = true;
            } catch (Exception ex) {
                compliant = false;
                errMsg = ex.getMessage();
            }
            Assert.assertTrue("csv File <" + lFile.getPath() + "> is NOT compliant with rfc4180 (" + errMsg + ")", compliant);
        }
    }

}