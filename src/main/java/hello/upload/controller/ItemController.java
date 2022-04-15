package hello.upload.controller;

import hello.upload.domain.Item;
import hello.upload.domain.ItemRepository;
import hello.upload.domain.UploadFile;
import hello.upload.file.FileStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;
    private final FileStore fileStore;

    /**
     * 등록 폼을 보여준다.
     *
     * @param itemForm
     * @return
     */
    @GetMapping("/items/new")
    public String newItem(@ModelAttribute ItemForm itemForm) {
        return "item-form";
    }

    @PostMapping("/items/new")
    public String saveItem(@ModelAttribute ItemForm itemForm, RedirectAttributes redirectAttributes) throws IOException {

        // 디렉토리에 저장
        UploadFile attachFile = fileStore.storeFile(itemForm.getAttachFile());
        List<UploadFile> storeImageFiles = fileStore.storeFiles(itemForm.getImageFiles());

        // 데이터베이스에 저장 (보통 DB 에는 용량상의 이유로 이미지 자체를 저장하지 않음)
        Item item = new Item();
        item.setItemName(itemForm.getItemName());
        item.setAttachFile(attachFile);
        item.setImageFiles(storeImageFiles);
        itemRepository.save(item);

        // 리다이렉트시 url 에 itemId를 이어붙여서 리다이렉트 가능
        redirectAttributes.addAttribute("itemId", item.getId());

        return "redirect:/items/{itemId}";
    }

    @GetMapping("/items/{id}")
    public String item(@PathVariable Long id, Model model) {
        Item item = itemRepository.findById(id);

        model.addAttribute("item", item);
        return "item-view";
    }

    @ResponseBody
    @GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {
        // "file:\Users\..\..\cd20284f-6e68-4127-b4ca-fae5160b6934.jpg"
        return new UrlResource("file:" + fileStore.getFullPath(filename));
        // getFullPath 경로에 있는 파일에 접근해서 파일을 가져온 후 스트림으로 변환해서 Resource 객체로 반환한다.
    }

    /**
     * 다운로드
     */
    @GetMapping("/attach/{itemId}")
    public ResponseEntity<Resource> downloadAttach(@PathVariable Long itemId) throws MalformedURLException {

        // 아무나 다운로드 받을 수 없게 하기 위해 itemId 로 클라이언트 신원 조회
        Item item = itemRepository.findById(itemId);
        String storeFileName = item.getAttachFile().getStoreFileName();
        // 다운로드 할 때는 클라이언트가 업로드 한 파일 이름으로 보이게끔 처리
        String uploadFileName = item.getAttachFile().getUploadFileName();

        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(storeFileName));

        log.info("uploadFileName={}", uploadFileName);

        // 한글 및 특수문자 깨짐을 방지하기 위한 인코딩 처리
        String encodedUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8);

        // 파일 다운로드를 위한 http 헤더 정의
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        // 브라우저는 HttpHeaders.CONTENT_DISPOSITION 응답 헤더를 보고 다운로드 여부를 결정한다.
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}
