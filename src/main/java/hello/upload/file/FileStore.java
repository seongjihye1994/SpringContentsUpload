package hello.upload.file;

import hello.upload.domain.UploadFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FileStore {

    @Value("${file.dir}")
    private String fileDir;

    /**
     * 파일 이름 받아서 저장할 디렉토리가 합쳐진 fullPath 반환하는 메소드
     * @param filename
     * @return
     */
    public String getFullPath(String filename) {
        return fileDir + filename;
    }

    /**
     * 클라이언트는 여러개의 이미지를 업로드할 수 있다.
     * @param multipartFiles
     * @return
     */
    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<UploadFile> storeFileResult = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                // 업로드 파일 정보 보관을 위한 UploadFile 객체 리턴 후 최종 저장
                storeFileResult.add(storeFile(multipartFile));
            }
        }

        return storeFileResult;
    }


    /**
     * 파일 저장 후 UploadFile 객체로 반환
     * @param multipartFile
     * @return
     */
    public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }

        // 1. 고객이 업로드한 파일명 가져오기
        String originalFilename = multipartFile.getOriginalFilename();

        // 5. 서버에 저장할 최종 업로드 파일 이름 완성
        String storeFileName = createStoreFileName(originalFilename);

        // 6. 최종 저장할 디렉토리 경로와 서버에 저장할 완성된 이름 합치기
        multipartFile.transferTo(new File(getFullPath(storeFileName)));

        // 7. 업로드 파일 정보 보관을 위한 UploadFile 객체 생성
        return new UploadFile(originalFilename, storeFileName);

    }

    private String createStoreFileName(String originalFilename) {

        // 2. 고객이 업로드한 파일명에서 확장자 꺼내기
        String ext = extractExt(originalFilename);

        // 3. 서버에 저장하는 파일명 생성
        String uuid = UUID.randomUUID().toString();

        // 4. 서버에 저장할 파일명과 꺼낸 확장자 이어 붙이기
        return uuid + "." + ext;
    }

    /**
     * 고객이 업로드한 파일의 확장자 가져오기
     * @param originalFilename
     */
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");// . 가 있는 마지막 위치 인덱스 찾기
        return originalFilename.substring(pos + 1);// . 뒤(확장자)부터 다 가져옴
    }

}
