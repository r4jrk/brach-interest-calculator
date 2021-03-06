package pl.net.brach;

import java.io.*;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rates {

    private static final String COMMA_DELIMITER = ",";

    private static final String HEADER_START_DATE_NAME = "okresOd";
    private static final String HEADER_END_DATE_NAME = "okresDo";
    private static final String HEADER_RATE_NAME = "stopa";

    private static final String RATES_FILE_DATE_FORMAT = "yyyy-MM-dd";

    public List<List<String>> ratesFromFile = new ArrayList<>();

    public Rates() {
            FileReader ratesFileReader = validateRatesFileExists();
            if (ratesFileReader != null) {
                ratesFromFileFileContents(ratesFileReader);
                validateRatesFileStructure();
                validateRatesFileDateFormat();
                validateRatesFileChronology();
            } else {
                System.out.println("Program nie będzie działał prawidłowo");
            }
    }

    private FileReader validateRatesFileExists() {
        File ratesFile = null;
        try {
            ratesFile = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent() + "\\stopy.csv");
        } catch (URISyntaxException e) {
            System.out.println("Ścieżka wskazująca plik stopy.csv jest wadliwa");
        }

        FileReader fileReader = null;
        try {
            fileReader = new FileReader(ratesFile);
        } catch (FileNotFoundException e) {
            System.out.println("Nie znaleziono pliku stopy.csv");
        }

        return fileReader;
    }

    private void ratesFromFileFileContents(FileReader fileReader) {
        BufferedReader br = new BufferedReader(fileReader);
        String line = "";
        while (true) {
            try {
                if ((line = br.readLine()) != null) {
                    String[] values = line.split(COMMA_DELIMITER);
                    ratesFromFile.add(Arrays.asList(values));
                } else {
                    break;
                }
            } catch (IOException e) {
                System.out.println("Wystąpił błąd podczas odczytywania pliku stopy.csv");
            }
        }
    }

    private void validateRatesFileStructure() {
        try {
            if (!ratesFromFile.get(0).contains(HEADER_START_DATE_NAME)) throw new Exception();
            if (!ratesFromFile.get(0).contains(HEADER_END_DATE_NAME)) throw new Exception();
            if (!ratesFromFile.get(0).contains(HEADER_RATE_NAME)) throw new Exception();
        } catch (Exception e) {
            System.out.println(String.format("Plik stopy.csv ma nieprawidłową strukturę. Spodziewane nagłówki: %1$s, %2$s, %3$s. " +
                            "Odczytane nagłówki: " + ratesFromFile.get(0).get(0) + ", " + ratesFromFile.get(0).get(1) + ", " + ratesFromFile.get(0).get(2),
                    HEADER_START_DATE_NAME, HEADER_END_DATE_NAME, HEADER_RATE_NAME));
        }
    }

    private void validateRatesFileDateFormat() {
        for (int i = 1; i < ratesFromFile.size(); i++) { //Start from int = 1 since 0 is a header
            List<String> ratesFromFileRow = ratesFromFile.get(i);
            for (int j = 0; j < ratesFromFileRow.size() - 1; j++) { //Do not parse last record since there are rate values there
                String readDateString = ratesFromFileRow.get(j);
                if (readDateString != null && !readDateString.equals("")) {
                    LocalDate parsedDate = LocalDate.parse(readDateString, DateTimeFormatter.ofPattern(RATES_FILE_DATE_FORMAT));
                }
            }
        }
    }

    private void validateRatesFileChronology() {
        for (int i = 1; i < ratesFromFile.size(); i++) { //Start from int = 1 since 0 is a header
            List<String> ratesFromFileRow = ratesFromFile.get(i);
            LocalDate endDate;
            LocalDate startDate = null;

            for (int j = 0; j < ratesFromFileRow.size() - 1; j++) { //Do not parse last record since there are rate values there
                String readDateString = ratesFromFileRow.get(j);
                if (readDateString != null && !readDateString.equals("")) {
                    LocalDate parsedDate = LocalDate.parse(readDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    if (j % 2 == 0) {
                        startDate = parsedDate;
                    } else {
                        endDate = parsedDate;
                        if (startDate.isAfter(endDate)) {
                            try {
                                throw new Exception();
                            } catch (Exception e) {
                                System.out.println("Nieprawidłowa chronologia dat. Początkowa data okresu jest późniejsza od końcowej daty okresu");
                            }
                        } else {
                            if (!endDate.isAfter(startDate)) {
                                try {
                                    throw new Exception();
                                } catch (Exception e) {
                                    System.out.println("Nieprawidłowa chronologia dat. Początkowa data okresu jest taka sama jak końcowa data okresu");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
