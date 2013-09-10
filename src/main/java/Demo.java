import com.jcraft.jzlib.GZIPInputStream;
import com.jcraft.jzlib.GZIPOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
public class Demo {
    public static void main(final String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: Demo /file/name");
        }
        PipedOutputStream pos = new PipedOutputStream();
        InputStream pis = new PipedInputStream(pos);
        Checksum csOut = new CRC32();
        OutputStream gos = new GZIPOutputStream(pos);
        final OutputStream cos = new CheckedOutputStream(gos, csOut);
        Thread t = new Thread() {
            @Override public void run() {
                try {
                    InputStream fis = new FileInputStream(args[0]);
                    try {
                        int c;
                        while ((c = fis.read()) != -1) {
                            cos.write(c);
                        }
                    } finally {
                        fis.close();
                    }
                    cos.close();
                } catch (IOException x) {
                    x.printStackTrace();
                }
            }
        };
        t.start();
        InputStream gis = new GZIPInputStream(pis);
        Checksum csIn = new CRC32();
        InputStream cis = new CheckedInputStream(gis, csIn);
        while (cis.read() != -1) {/* discard */}
        t.join();
        if (csOut.getValue() == csIn.getValue()) {
            System.out.println(args[0] + ": OK");
        } else {
            System.out.println(args[0] + ": checksum mismatch");
        }
    }

}
