package test.staxtest;

import com.ctc.wstx.stax.WstxInputFactory;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.sun.org.apache.xml.internal.utils.XMLChar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

class StripInvalidCharInputStream extends FilterInputStream {

    private static Logger logger = LoggerFactory.getLogger(StripInvalidCharInputStream.class);

    protected StripInvalidCharInputStream(InputStream in) {
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
            if (XMLChar.isInvalid(cbuf[readPos])) {
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

public class StripInvalidCharsTest {

    private static Logger logger = LoggerFactory.getLogger(StripInvalidCharsTest.class);

    private static TransformerFactory tf = new TransformerFactoryImpl();

    public static void main(String[] args) throws Exception {
        XMLInputFactory xmlInputFactory = new WstxInputFactory();
        InputStream inputStream = StripInvalidCharsTest.class.getResourceAsStream("/example.xml");
        InputStream filterInputStream = new StripInvalidCharInputStream(inputStream);
//        XMLStreamReader xmlStreamReader =  xmlInputFactory.createXMLStreamReader(inputStream);
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
