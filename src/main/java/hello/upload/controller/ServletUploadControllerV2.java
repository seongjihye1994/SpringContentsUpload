package hello.upload.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

@Slf4j
@Controller
@RequestMapping("/servlet/v2")
public class ServletUploadControllerV2 {

    @Value("${file.dir}")
    private String fileDir;

    @GetMapping("/upload")
    public String newFile() {
        return "upload-form";
    }

    @PostMapping("/upload")
    public String saveFileV2(HttpServletRequest request) throws ServletException, IOException {

        log.info("request={}", request);

        String itemName = request.getParameter("itemName");
        log.info("itemName={}", itemName);

        // 클라이언트가 multipart-formdata로 요청하면
        // http request 요청 메세지 바디에 part로 쪼개져서 요청됨
        Collection<Part> parts = request.getParts();
        log.info("parts={}", parts);

        // parts 분해
        for (Part part : parts) {
            log.info("=== PART ===");

            log.info("name={}", part.getName());

            // parts 도 헤더와 바디로 구분된다.
            Collection<String> headerNames = part.getHeaderNames();

            // parts 헤더
            for (String headerName : headerNames) {
                log.info("header {} : {}", headerName, part.getHeader(headerName));
            }

            // 편의 메서드
            // form 에 파일을 넘기면 content-disposition; 에 filename 이 들어간다.
            log.info("submittedFilename={}", part.getSubmittedFileName());

            log.info("size={}", part.getSize()); // part body size

            // 데이터 읽기
            InputStream inputStream = part.getInputStream(); // http 요청 바디값 읽기
            String body = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8); // 읽은 바디값 바이너리를 String 화
            log.info("body={}", body);

            // 파일에 저장하기
            if (StringUtils.hasText(part.getSubmittedFileName())) {

                // 디렉토리명 + 파일명 합치기기
               String fullPath = fileDir + part.getSubmittedFileName();
               log.info("파일 저장 fullPath={}", fullPath);

               // 실제로 저장하기
               part.write(fullPath);
            }
        }

        return "upload-form";
    }
}
