package nl.abcbank.identity.helper;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class XmlHelper {

    public static <T> byte[] unmarshal(T data) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(data.getClass());
        Marshaller marshaller = context.createMarshaller();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        marshaller.marshal(data, outputStream);
        return outputStream.toByteArray();
    }

}
