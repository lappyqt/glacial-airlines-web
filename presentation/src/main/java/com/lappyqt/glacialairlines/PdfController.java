package com.lappyqt.glacialairlines;

import com.lappyqt.glacialairlines.services.PdfGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pdf")
@RequiredArgsConstructor
public class PdfController {
    private final PdfGenerationService pdfGenerationService;

    @GetMapping("/itinerary")
    public ResponseEntity<byte[]> downloadItinerary() {
        byte[] pdfBytes = pdfGenerationService.generatePdfFromHtml("itinerary");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document.pdf\"")
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }
}
