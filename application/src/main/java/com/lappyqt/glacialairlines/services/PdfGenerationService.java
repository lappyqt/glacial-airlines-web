package com.lappyqt.glacialairlines.services;

import com.lappyqt.glacialairlines.entities.booking.BookingOrder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;

// Сервисный класс для динамической генерации PDF-документов (маршрутных квитанций/билетов) на основе HTML-шаблонов
@Service
@RequiredArgsConstructor
public class PdfGenerationService {
    private final TemplateEngine templateEngine;

    // Метод для преобразования HTML-шаблона Thymeleaf с данными заказа в байтовый массив PDF-файла
    public byte[] generatePdfFromHtml(String templateName, BookingOrder order) {
        // Создание контекста Thymeleaf для передачи объекта заказа в HTML-шаблон
        Context context = new Context();
        context.setVariable("order", order);

        // Рендеринг HTML-кода путем подстановки данных заказа в выбранный шаблон
        String htmlContent = templateEngine.process(templateName, context);

        // Инициализация потока вывода в массив байтов
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // Добавляем собственные шрифты
            builder.useFont(new ClassPathResource("fonts/Raleway-Regular.ttf").getFile(), "Raleway", 400, PdfRendererBuilder.FontStyle.NORMAL, true);
            builder.useFont(new ClassPathResource("fonts/Raleway-Medium.ttf").getFile(), "Raleway", 500, PdfRendererBuilder.FontStyle.NORMAL, true);
            builder.useFont(new ClassPathResource("fonts/Raleway-Bold.ttf").getFile(), "Raleway", 700, PdfRendererBuilder.FontStyle.NORMAL, true);

            builder.withHtmlContent(htmlContent, "file:///");
            builder.toStream(os);
            builder.run();

            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при генерации PDF", e);
        }
    }
}
