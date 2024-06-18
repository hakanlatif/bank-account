package nl.abcbank.dbmigrator.helper;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class XmlHelper {

    @SuppressWarnings("unchecked")
    public static <T> T unmarshal(byte[] message, Class<T> clazz) throws JAXBException {
        ByteArrayInputStream input = new ByteArrayInputStream(message);
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object object = unmarshaller.unmarshal(input);
        return (T) (object);
    }

}
