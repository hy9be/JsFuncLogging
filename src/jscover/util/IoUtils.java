package jscover.util;

/**
 * Created by hyou on 8/12/15.
 */
import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IoUtils {
    private static final Logger logger = Logger.getLogger(IoUtils.class.getName());
    public static String CRLFx2 = "\r\n\r\n";
    public static String CRx2 = "\r\r";
    public static String LFx2 = "\n\n";
    private static IoUtils ioUtils = new IoUtils();

    public static IoUtils getInstance() {
        return ioUtils;
    }

    public Charset charSet = Charset.defaultCharset();

    public void closeQuietly(Closeable s) {
        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void flushQuietly(Flushable s) {
        if (s != null) {
            try {
                s.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeQuietly(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toString(InputStream is) {
        StringBuilder result = new StringBuilder();
        int bufSize = 1024;
        char buf[] = new char[bufSize];
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is, charSet));
            for (int read; (read = br.read(buf)) != -1; ) {
                result.append(buf, 0, read);
            }
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(br);
        }
    }

    public String toStringNoClose(InputStream is, int length) {
        byte bytes[] = new byte[length];
        try {
            int total = 0;
            for (int read; total < length && (read = is.read(bytes, total, length-total)) != -1; total += read);
            assert total == length;
            return new String(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String toString(File file) {
        try {
            return toString(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    public List<String> readLines(String source) {
        ByteArrayInputStream bais = new ByteArrayInputStream(source.getBytes(charSet));
        return readLines(new BufferedReader(new InputStreamReader(bais, charSet)));
    }
    private List<String> readLines(BufferedReader br) {
        List<String> result = new ArrayList<String>();
        try {
            for (String line; (line = br.readLine()) != null; ) {
                result.add(line);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(br);
        }
    }
    */

    public String loadFromClassPath(String dataFile) {
        InputStream is = null;
        try {
//            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(dataFile);
            is = IoUtils.class.getResourceAsStream(dataFile);
            return toString(is);
        } catch (Throwable e) {
            throw new RuntimeException(String.format("Problem loading file: '%s'",dataFile),e);
        } finally {
            closeQuietly(is);
        }
    }

    public String loadFromFileSystem(File dataFile) {
        InputStream is = null;
        try {
            is = new FileInputStream(dataFile);
            return toString(is);
        } catch (Throwable e) {
            throw new RuntimeException(String.format("Problem loading file: '%s'",dataFile),e);
        } finally {
            closeQuietly(is);
        }
    }

    public void copy(InputStream is, OutputStream os) {
        try {
            copyNoClose(is, os);
        } finally {
            closeQuietly(is);
            closeQuietly(os);
        }
    }

    public void copyNoClose(InputStream is, OutputStream os, int length) {
        int bufSize = Math.min(1024, length);
        byte buf[] = new byte[bufSize];
        try {
            for (int total = 0, read; total < length && (read = is.read(buf, 0, Math.min(bufSize, length - total))) != -1; total += read) {
                os.write(buf, 0, read);
                os.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void copyNoClose(InputStream is, OutputStream os) {
        int bufSize = 1024;
        byte buf[] = new byte[bufSize];
        try {
            for (int read; (read = is.read(buf)) != -1; ) {
                os.write(buf, 0, read);
                os.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void copyNoClose(File file, OutputStream os) {
        InputStream is = null;
        int bufSize = 1024;
        byte buf[] = new byte[bufSize];
        try {
            is = new FileInputStream(file);
            for (int read; (read = is.read(buf)) != -1; ) {
                os.write(buf, 0, read);
                os.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(is);
        }
    }

    public void copy(Reader reader, File dest) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(dest);
            copy(reader, os);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(reader);
            closeQuietly(os);
        }
    }

    void copy(Reader reader, OutputStream os) {
        int bufSize = 1024;
        char buf[] = new char[bufSize];
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(os, charSet));
            for (int read; (read = reader.read(buf)) != -1; ) {
                bw.write(buf, 0, read);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(reader);
            closeQuietly(bw);
        }

    }

    public void copy(String string, File dest) {
        dest.getParentFile().mkdirs();
        ByteArrayInputStream bais = new ByteArrayInputStream(string.getBytes(charSet));
        copy(new InputStreamReader(bais, charSet), dest);
    }

    public void copy(InputStream is, File dest) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(dest);
            copy(is, os);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(is);
            closeQuietly(os);
        }
    }

    public void copy(File src, File dest) {
        logger.log(Level.FINEST, "Copying ''{0}'' to ''{1}''", new Object[]{src.getPath(), dest.getPath()});
        dest.getParentFile().mkdirs();
        FileInputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(src);
            os = new FileOutputStream(dest);
            copy(is, os);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(is);
            closeQuietly(os);
        }
    }

    public String getRelativePath(File file1, File file2) {
        logger.log(Level.FINEST, "Get path ''{0}'' relative to ''{1}''", new Object[]{file1, file2});
        if (file1.equals(file2))
            return "";
        return file1.getAbsolutePath().substring(file2.getAbsolutePath().length()+File.separator.length()).replaceAll("\\\\","/");
    }

    /*
    public Reader getReader(String source) {
        ByteArrayInputStream bais = new ByteArrayInputStream(source.getBytes(charSet));
        BufferedReader reader = new BufferedReader(new InputStreamReader(bais, charSet));
        return reader;
    }
    */

    public boolean isSubDirectory(File file1, File file2) {
        try {
            return (file1.getCanonicalPath()+File.separator).startsWith(file2.getCanonicalPath()+File.separator);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void copyDir(File src, File dest) {
        if (src.isDirectory())
            for (String file : src.list())
                copyDir(new File(src, file), new File(dest, file));
        else
            ioUtils.copy(src, dest);
    }

    public String getCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadProperties(Properties properties, InputStream is) {
        try {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getDataIndex(byte[] bytes, Charset charset) {
        String firstBytes = new String(bytes, charset);
        String separator = CRLFx2;
        int index = firstBytes.indexOf(CRLFx2);
        int indexCR = firstBytes.indexOf(CRx2);
        int indexLF = firstBytes.indexOf(LFx2);
        if (indexCR != -1 && indexCR < index) {
            separator = CRx2;
            index = indexCR;
        }
        if (indexLF != -1 && indexLF < index)
            separator = LFx2;
        return getByteIndexIncludingSeparator(firstBytes, separator);
    }

    public int getByteIndex(String text, String separator) {
        String header = text.substring(0, text.indexOf(separator));
        return header.getBytes().length;
    }

    public int getByteIndexIncludingSeparator(String text, String separator) {
        String header = text.substring(0, text.indexOf(separator) + separator.length());
        return header.getBytes().length;
    }

    public int getNewLineIndex(byte[] bytes, Charset charset) {
        String firstBytes = new String(bytes, charset);
        String separator = "\r\n";
        int index = firstBytes.indexOf("\r\n");
        int indexCR = firstBytes.indexOf("\r");
        int indexLF = firstBytes.indexOf("\n");
        if (indexCR != -1 && indexCR < index) {
            separator = "\r";
            index = indexCR;
        }
        if (indexLF != -1 && indexLF < index)
            separator = "\n";
        return getByteIndex(firstBytes, separator);
    }
}
