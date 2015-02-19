package net.toften.docmaker.postprocessors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplyKeyValue {
    private static final String REGEX = "\\$\\{(.*)\\}";
    private final Pattern p = Pattern.compile(REGEX);
    private Properties keyValue = new Properties();

    public ApplyKeyValue(final Properties keyValue) {
        this.keyValue.putAll(keyValue);

        initKeys();
    }

    public ApplyKeyValue(final Properties keyValue[]) {
        for (Properties p : keyValue) {
            this.keyValue.putAll(p);
        }

        initKeys();
    }

    public ApplyKeyValue(final File propertyFile[]) throws IOException {
        for (File f : propertyFile) {
            Properties p = new Properties();
            InputStream in = new FileInputStream(f);
            p.load(in);
            in.close();

            this.keyValue.putAll(p);
        }

        initKeys();
    }

    public ApplyKeyValue(final File propertyFile) throws IOException {
        InputStream in = new FileInputStream(propertyFile);
        this.keyValue.load(in);
        in.close();

        initKeys();
    }

    private void initKeys() {
        for (Object k : this.keyValue.keySet()) {
            String v = this.keyValue.getProperty(k.toString());

            boolean matchFound = true;

            while (matchFound) {
                StringBuffer out = new StringBuffer();
                matchFound = processFragment(v, out);

                v = out.toString();
            }

            this.keyValue.put(k, v);
        }
    }

    public boolean processFragment(final String fragmentAsHtml, final StringBuffer out) {
        boolean matchFound = false;
        Matcher m = this.p.matcher(fragmentAsHtml);

        while (m.find()) {
            m.appendReplacement(out, this.keyValue.getProperty(m.group(1)));
            matchFound = true;
        }

        m.appendTail(out);

        return matchFound;
    }

    public String processFragment(final String fragmentAsHtml) {
        StringBuffer out = new StringBuffer();

        processFragment(fragmentAsHtml, out);

        return out.toString();
    }
}
