package test.staxtest;

import com.ctc.wstx.stax.WstxInputFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

class StripInvalidCharReader extends InputStreamReader {

    private static Logger logger = LoggerFactory.getLogger(StripInvalidCharReader.class);

    protected StripInvalidCharReader(InputStream in, String encoding) throws UnsupportedEncodingException {
        super(in, encoding);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int read = super.read(cbuf, off, len);
        if (read == -1) {
            return -1;
        }

        int pos = off - 1;
        for (int readPos = off; readPos < off + read; readPos++) {
            // ignore invalid XML 1.0 chars
            if (XMLUtil.isInValid(cbuf[readPos])) {
                logger.info("found control character: " + cbuf[readPos]);
                continue;
            } else {
                pos++;
            }

            if (pos < readPos) {
                cbuf[pos] = cbuf[readPos];
            }
        }
        return pos - off + 1;
    }

}

// The FilterInputStream won't work for encodings like UTF-16
/*
class StripInvalidCharInputStream extends FilterInputStream {

    private static Logger logger = LoggerFactory.getLogger(StripInvalidCharInputStream.class);

    protected StripInvalidCharInputStream(InputStream in, String encoding) {
        super(in);
    }

    @Override
    public int read(byte[] cbuf, int off, int len) throws IOException {
        int read = super.read(cbuf, off, len);
        if (read == -1) {
            return -1;
        }

        int pos = off - 1;
        for (int readPos = off; readPos < off + read; readPos++) {
            // ignore invalid XML 1.0 chars
            if (XMLUtil.isInvalid(cbuf[readPos])) {
                logger.info("found control character: " + cbuf[readPos]);
                continue;
            } else {
                pos++;
            }

            if (pos < readPos) {
                cbuf[pos] = cbuf[readPos];
            }
        }
        return pos - off + 1;
    }
}
*/

public class StripInvalidCharsTest {

    private static Logger logger = LoggerFactory.getLogger(StripInvalidCharsTest.class);

    private static TransformerFactory tf = TransformerFactory.newInstance();

    private static String getEncoding(String fileName) throws XMLStreamException {
        String encoding = null;
        XMLStreamReader xmlStreamReader = null;
        try {
            InputStream inputStream = StripInvalidCharsTest.class.getResourceAsStream(fileName);
            xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            encoding = xmlStreamReader.getEncoding();
        } finally{
            if (xmlStreamReader != null) {
                xmlStreamReader.close();
            }
        }
        return encoding;
    }

    public static void main(String[] args) throws Exception {
        XMLInputFactory xmlInputFactory = new WstxInputFactory();

        String fileName = "/example-8.xml";

        String encoding = getEncoding(fileName);
        System.out.println("encoding=" + encoding);

        InputStream inputStream = StripInvalidCharsTest.class.getResourceAsStream(fileName);
        Reader filterInputStream = new StripInvalidCharReader(inputStream, encoding);
        XMLStreamReader xmlStreamReader =  xmlInputFactory.createXMLStreamReader(filterInputStream);

        while (xmlStreamReader.hasNext()) {
            int eventType = xmlStreamReader.getEventType();
            if (eventType == XMLEvent.START_ELEMENT) {
                String elementName = xmlStreamReader.getName().getLocalPart();
                if (elementName.equals("Employee")) {
                    Transformer t = tf.newTransformer();
                    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    t.transform(new StAXSource(xmlStreamReader), new StreamResult(outputStream));
                    String employeeStr = outputStream.toString();
                    logger.info("employee={}", employeeStr);
                }

            }
            xmlStreamReader.next();
        }
    }

}
