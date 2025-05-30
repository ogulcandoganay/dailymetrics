package com.ogulcan.dailymetrics; // Kendi paket yapına göre ayarla

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application.properties dosyasındaki app.upload.dir değerini alıyoruz.
    // Varsayılan olarak "src/main/resources/static/uploads" kullanılıyor.
    @Value("${app.upload.dir:src/main/resources/static/uploads}")
    private String uploadDirSourcePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Geliştirme ortamında, /uploads/** istekleri için
        // doğrudan src/main/resources/static/uploads dizininden dosyaları sun.
        // Bu, uygulamanın yeniden başlatılmasına gerek kalmadan yeni yüklenen resimlerin
        // hemen görünür olmasını sağlar.
        // "file:" prefix'i dosya sisteminden bir yola işaret ettiğini belirtir.
        // uploadDirSourcePath'in sonuna "/" ekliyoruz ki doğru şekilde eşleşsin.
        // Örneğin, src/main/resources/static/uploads/profiles/resim.jpg için
        // istek /uploads/profiles/resim.jpg şeklinde gelecek.

        // Eğer app.upload.dir = src/main/resources/static/uploads ise,
        // URL path /uploads/** olmalı ve resource location file:src/main/resources/static/uploads/ olmalı.
        // Ancak, Spring Boot zaten src/main/resources/static altını classpath'ten sunduğu için
        // ve SecurityConfig'de /uploads/** izni olduğu için,
        // burada önemli olan, uploadDirSourcePath'in doğru bir şekilde dosya sistemindeki
        // src klasörünü göstermesi.

        // Eğer uploadDirSourcePath "src/main/resources/static/uploads" ise,
        // ve frontend "/uploads/profiles/image.jpg" istiyorsa,
        // bu handler "/uploads/**" path'ini "file:src/main/resources/static/uploads/" ile eşleştirmeli.
        // Spring Boot'un varsayılan statik kaynak sunumu (classpath'ten) zaten / altından çalışır.
        // Bu ek handler, özellikle geliştirme sırasında doğrudan src klasöründen sunum yapar.

        String resourceLocation = "file:" + uploadDirSourcePath + "/"; // Örn: "file:src/main/resources/static/uploads/"

        // "/uploads/**" URL path'ini, dosya sistemindeki uploadDirSourcePath'e map et.
        // Bu, Spring Boot'un varsayılan classpath tabanlı kaynak sunucusuna ek olarak çalışır.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);

        // Eğer başka statik kaynaklarınız varsa (örn: /images/** için src/main/resources/static/images/)
        // ve benzer bir sorun yaşıyorsanız, onlar için de benzer handler'lar ekleyebilirsiniz.
        // Ancak genellikle /uploads gibi dinamik olarak dosya eklenen yerler için bu gereklidir.
        // registry.addResourceHandler("/images/**")
        //        .addResourceLocations("file:src/main/resources/static/images/");

        System.out.println("Development resource handler configured for /uploads/** pointing to " + resourceLocation);
    }
}
