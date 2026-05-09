package org.example.backend_tunisiahub.Services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.ReservationRestaurant;
import org.example.backend_tunisiahub.Entities.Restaurant.RestaurantTable;
import org.example.backend_tunisiahub.Entities.User.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReservationRestaurantPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Value("${app.base-url:http://localhost:8089}")
    private String baseUrl;

    public byte[] generateReservationPdf(ReservationRestaurant reservation) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            Paragraph title = new Paragraph("Reservation Confirmation", titleFont);
            title.setSpacingAfter(18f);
            document.add(title);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingAfter(16f);
            table.setWidths(new float[]{2f, 5f});

            addRow(table, "Reservation ID", valueOf(reservation.getId()), bodyFont);
            addRow(table, "Client name", extractClientName(reservation.getUser()), bodyFont);
            addRow(table, "Restaurant name", reservation.getRestaurant() != null ? valueOf(reservation.getRestaurant().getName()) : "-", bodyFont);
            addRow(table, "Number of people", reservation.getPartySize() != null ? reservation.getPartySize().toString() : "-", bodyFont);
            addRow(table, "Date and time", reservation.getDateTime() != null ? reservation.getDateTime().format(DATE_TIME_FORMATTER) : "-", bodyFont);
            addRow(table, "Table number(s)", extractTableNumbers(reservation.getTables()), bodyFont);

            document.add(table);
            document.add(new Paragraph("Please present this document at the restaurant", bodyFont));

            String checkInUrl = buildCheckInUrl(reservation);
            if (StringUtils.hasText(checkInUrl)) {
                Paragraph checkInParagraph = new Paragraph("Check-in URL: " + checkInUrl, bodyFont);
                checkInParagraph.setSpacingBefore(8f);
                document.add(checkInParagraph);
            }

            Image qrCodeImage = buildQrCodeImage(checkInUrl);
            if (qrCodeImage != null) {
                qrCodeImage.setSpacingBefore(18f);
                qrCodeImage.scaleToFit(120f, 120f);
                document.add(qrCodeImage);
            }

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException | IOException ex) {
            throw new IllegalStateException("Failed to generate reservation confirmation PDF", ex);
        }
    }

    private void addRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        labelCell.setPadding(8f);
        valueCell.setPadding(8f);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String extractClientName(User user) {
        if (user == null) {
            return "-";
        }
        String fullName = (valueOf(user.getPrenom()) + " " + valueOf(user.getNom())).trim();
        return StringUtils.hasText(fullName) ? fullName : valueOf(user.getEmail());
    }

    private String extractTableNumbers(List<RestaurantTable> tables) {
        if (tables == null || tables.isEmpty()) {
            return "-";
        }
        return tables.stream()
                .map(RestaurantTable::getTableNumber)
                .map(number -> number == null ? "-" : number.toString())
                .collect(Collectors.joining(", "));
    }

    private String valueOf(Object value) {
        if (value == null) {
            return "-";
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? "-" : text;
    }

    private String buildCheckInUrl(ReservationRestaurant reservation) {
        if (reservation == null || !StringUtils.hasText(reservation.getCheckInToken())) {
            return null;
        }
        String normalizedBaseUrl = StringUtils.trimTrailingCharacter(valueOf(baseUrl), '/');
        if (!StringUtils.hasText(normalizedBaseUrl) || "-".equals(normalizedBaseUrl)) {
            return null;
        }
        return normalizedBaseUrl + "/api/reservations/checkin-public?token=" + reservation.getCheckInToken().trim();
    }

    private Image buildQrCodeImage(String qrContent) {
        if (!StringUtils.hasText(qrContent)) {
            return null;
        }

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        try (ByteArrayOutputStream qrOutput = new ByteArrayOutputStream()) {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    qrContent,
                    BarcodeFormat.QR_CODE,
                    180,
                    180,
                    hints
            );
            MatrixToImageWriter.writeToStream(matrix, "PNG", qrOutput);
            return Image.getInstance(qrOutput.toByteArray());
        } catch (WriterException | IOException | DocumentException ex) {
            log.warn("Failed to generate reservation QR code for content {}", qrContent, ex);
            return null;
        }
    }
}
