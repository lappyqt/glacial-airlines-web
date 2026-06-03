package com.lappyqt.glacialairlines.controllers;

import com.lappyqt.glacialairlines.entities.booking.BookingOrder;
import com.lappyqt.glacialairlines.enums.OrderStatus;
import com.lappyqt.glacialairlines.services.BookingService;
import com.lappyqt.glacialairlines.services.PdfGenerationService;
import com.lappyqt.glacialairlines.services.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

// Контроллер для формирования и отдачи PDF-документов через HTTP-поток
@Controller
@RequestMapping("/pdf")
@RequiredArgsConstructor
public class PdfController {
    private final PdfGenerationService pdfGenerationService;
    private final BookingService bookingService;

    // Метод генерации и скачивания PDF-файла маршрутной квитанции по идентификатору заказа
    @GetMapping("/order/{id}")
    public ResponseEntity<byte[]> downloadItinerary(@PathVariable("id") Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        BookingOrder bookingOrder = bookingService.getOrder(id);

        // Проверка прав доступа: скачать билет может только тот пользователь, который его оформил
        if (!Objects.equals(bookingOrder.getUserAccount().getId(), currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Проверка бизнес-логики: маршрутная квитанция доступна только для полностью оплаченных заказов
        if (bookingOrder.getStatus() != OrderStatus.PAID) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Определение фамилии владельца для формирования имени файла
        String ownerLastName = "Customer";
        if (bookingOrder.getUserAccount().getLastName() != null) {
            ownerLastName = bookingOrder.getUserAccount().getLastName();
        }

        // Формирование резервного ASCII-имени файла для старых браузеров
        String asciiFileName = String.format("Itinerary_%s_%d.pdf", ownerLastName, bookingOrder.getId());

        // Формирование и кодирование кириллического имени файла
        String utf8FileName = String.format("МаршрутнаяКвитанция_%s_%d.pdf", ownerLastName, bookingOrder.getId());
        String encodedUtf8FileName = UriUtils.encode(utf8FileName, StandardCharsets.UTF_8);

        // Генерации PDF-байтов сервисным слоем
        byte[] pdfBytes = pdfGenerationService.generatePdfFromHtml("itinerary", bookingOrder);

        // Сборка и отправка HTTP-ответа с бинарным содержимым файла
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + asciiFileName + "\"; filename*=UTF-8''" + encodedUtf8FileName)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }
}
