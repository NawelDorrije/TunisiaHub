package org.example.backend_tunisiahub.Services.Camping;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

/**
 * Generates QR code images encoded as Base64 PNG strings.
 *
 * <p>Add to pom.xml:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;com.google.zxing&lt;/groupId&gt;
 *   &lt;artifactId&gt;core&lt;/artifactId&gt;
 *   &lt;version&gt;3.5.2&lt;/version&gt;
 * &lt;/dependency&gt;
 * &lt;dependency&gt;
 *   &lt;groupId&gt;com.google.zxing&lt;/groupId&gt;
 *   &lt;artifactId&gt;javase&lt;/artifactId&gt;
 *   &lt;version&gt;3.5.2&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
@Service
public class QRCodeService {

    private static final int QR_SIZE = 350; // pixels

    /**
     * Generates a QR code from the given content string.
     *
     * @param content the data to encode (JSON payload, URL, etc.)
     * @return Base64-encoded PNG image (no data-URI prefix)
     */
    public String generateQRCodeBase64(String content) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}