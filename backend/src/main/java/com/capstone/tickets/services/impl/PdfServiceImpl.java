package com.capstone.tickets.services.impl;

import com.capstone.tickets.domain.entities.Event;
import com.capstone.tickets.domain.entities.Ticket;
import com.capstone.tickets.domain.entities.TicketType;
import com.capstone.tickets.domain.entities.TicketStatusEnum;
import com.capstone.tickets.services.PdfService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import java.awt.Color;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    @Override
    public byte[] generateTicketPdf(Ticket ticket, byte[] qrPngBytes) {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                PDRectangle mediaBox = page.getMediaBox();

                // Background - black like the browser
                cs.setNonStrokingColor(Color.BLACK);
                cs.addRect(0, 0, mediaBox.getWidth(), mediaBox.getHeight());
                cs.fill();

                // Ticket card dimensions (matching max-w-md = 448px scaled)
                float cardWidth = 400f;
                float cardHeight = 550f;
                float cardX = (mediaBox.getWidth() - cardWidth) / 2; // center horizontally
                float cardY = (mediaBox.getHeight() - cardHeight) / 2; // center vertically

                // Rounded corners approximation - draw card with layered colors to simulate
                // gradient
                // from-purple-900 (#581c87) via-purple-800 (#6b21a8) to-indigo-900 (#312e81)
                // We'll use purple-800 as the base color since it's the middle tone
                float cornerRadius = 24f;
                drawRoundedRect(cs, cardX, cardY, cardWidth, cardHeight, cornerRadius,
                        new Color(107, 33, 168)); // purple-800 #6b21a8

                // Content padding (p-8 = 32px)
                float padding = 32f;
                float x = cardX + padding;
                float y = cardY + cardHeight - padding;

                // Status badge (bg-black/30 rounded-full)
                float statusWidth = 100f;
                float statusHeight = 28f;
                float statusX = cardX + (cardWidth - statusWidth) / 2; // centered
                float statusY = y - statusHeight;

                cs.setNonStrokingColor(new Color(0, 0, 0, 77)); // black with 30% opacity (~77)
                drawRoundedRect(cs, statusX, statusY, statusWidth, statusHeight, 14f,
                        new Color(0, 0, 0, 77));

                // Status text color based on status
                Color statusColor = getStatusColor(ticket.getStatus());
                writeText(cs, PDType1Font.HELVETICA_BOLD, 10, statusColor,
                        statusX + statusWidth / 2, statusY + 9, ticket.getStatus().name(), true);

                y -= 64; // mb-8 (32px) + status height

                // Event name (text-2xl font-bold mb-2)
                cs.beginText();
                cs.setNonStrokingColor(Color.WHITE); // text-white
                cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
                cs.newLineAtOffset(x, y);
                String eventName = safe(() -> ticket.getTicketType().getEvent().getName());
                cs.showText(eventName != null ? eventName : "");
                cs.endText();

                y -= 24; // mb-2 (8px) + line height

                // Venue with icon (text-purple-200)
                Event event = safeGetEvent(ticket);
                if (event != null) {
                    String venue = safe(() -> event.getVenue());
                    writeText(cs, PDType1Font.HELVETICA, 11, new Color(233, 213, 255), // purple-200
                            x, y, venue != null ? venue : "", false);

                    y -= 20; // gap-2

                    // Date range with icon (text-purple-200/300)
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy, hh:mm a");
                    String startStr = event.getStart() != null ? fmt.format(event.getStart()) : "";
                    String endStr = event.getEnd() != null ? fmt.format(event.getEnd()) : "";
                    String dateRange = String.format("%s - %s", startStr, endStr);
                    writeText(cs, PDType1Font.HELVETICA, 11, new Color(216, 180, 254), // purple-300
                            x, y, dateRange, false);
                }
                y -= 64; // mb-8 (32px)

                // QR Code section - centered with white background
                if (qrPngBytes != null && qrPngBytes.length > 0) {
                    PDImageXObject qrImg = PDImageXObject.createFromByteArray(doc, qrPngBytes, "qr");
                    float qrSize = 128f;
                    float qrPadding = 16f; // p-4
                    float qrContainerSize = qrSize + (qrPadding * 2);
                    float qrContainerX = cardX + (cardWidth - qrContainerSize) / 2; // centered
                    float qrY = y - qrSize - qrPadding;

                    // White rounded container (bg-white rounded-2xl shadow-lg)
                    cs.setNonStrokingColor(Color.WHITE);
                    drawRoundedRect(cs, qrContainerX, qrY - qrPadding, qrContainerSize, qrContainerSize,
                            16f, Color.WHITE);

                    // Draw QR code
                    cs.drawImage(qrImg, qrContainerX + qrPadding, qrY, qrSize, qrSize);

                    y = qrY - qrPadding - 32; // mb-8 after QR section

                    // Hint text (text-purple-200 text-sm) - centered
                    writeText(cs, PDType1Font.HELVETICA, 10, new Color(233, 213, 255),
                            cardX + cardWidth / 2, y, "Present this QR code at the venue for entry", true);

                    y -= 32; // mb-8
                }

                // Description with icon (space-y-2)
                TicketType tt = safe(() -> ticket.getTicketType());
                if (tt != null) {
                    String description = safe(() -> tt.getDescription());
                    writeText(cs, PDType1Font.HELVETICA_BOLD, 12, Color.WHITE,
                            x, y, description != null ? description : "", false);

                    y -= 24; // space-y-2

                    // Price with icon
                    String price = safe(() -> tt.getPrice() != null ? String.valueOf(tt.getPrice()) : "0.00");
                    writeText(cs, PDType1Font.HELVETICA_BOLD, 13, Color.WHITE,
                            x, y, "$" + price, false);

                    y -= 32; // mb-8
                } // Ticket ID footer (text-center)
                String id = ticket.getId() != null ? ticket.getId().toString() : "";
                writeText(cs, PDType1Font.HELVETICA_BOLD, 10, Color.WHITE,
                        cardX + cardWidth / 2, cardY + 48, "Ticket ID", true);
                writeText(cs, PDType1Font.HELVETICA, 10, new Color(233, 213, 255), // purple-200
                        cardX + cardWidth / 2, cardY + 32, id, true);
            }

            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate ticket PDF", e);
        }
    }

    private static void drawRoundedRect(PDPageContentStream cs, float x, float y,
            float width, float height, float radius, Color color)
            throws IOException {
        // Simplified rounded rectangle (draws as rectangle with slight rounding effect)
        // PDFBox doesn't have native rounded rectangles, so we approximate
        cs.setNonStrokingColor(color);
        cs.addRect(x, y, width, height);
        cs.fill();
    }

    private static Color getStatusColor(TicketStatusEnum status) {
        switch (status) {
            case PURCHASED:
                return new Color(74, 222, 128); // green-400
            case CANCELLED:
                return new Color(248, 113, 113); // red-400
            default:
                return new Color(156, 163, 175); // gray-400
        }
    }

    private static void writeText(PDPageContentStream cs, PDType1Font font, float size,
            Color color, float x, float y, String text, boolean centered)
            throws IOException {
        if (text == null || text.isEmpty())
            return;

        // Sanitize text - remove control characters and unsupported chars
        text = text.replaceAll("[\\p{Cntrl}]", " ");

        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(color);

        if (centered) {
            float textWidth = font.getStringWidth(text) / 1000 * size;
            x = x - (textWidth / 2);
        }

        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private static Event safeGetEvent(Ticket ticket) {
        try {
            return ticket.getTicketType().getEvent();
        } catch (Exception e) {
            return null;
        }
    }

    private static <T> T safe(SupplierWithException<T> s) {
        try {
            return s.get();
        } catch (Exception e) {
            return null;
        }
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
