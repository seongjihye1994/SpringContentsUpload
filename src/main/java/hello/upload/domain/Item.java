package hello.upload.domain;

import lombok.Data;

import java.util.List;

/**
 * 상품 도메인
 */
@Data
public class Item {

    private Long id;
    private String itemName;
    private UploadFile attachFile;
    private List<UploadFile> imageFiles; // 이미지 여러개 업로드 가능

}

