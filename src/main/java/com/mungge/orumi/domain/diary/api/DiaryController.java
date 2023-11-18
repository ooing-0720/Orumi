package com.mungge.orumi.domain.diary.api;

import com.mungge.orumi.domain.Image.application.ImageService;
import com.mungge.orumi.domain.Image.domain.Image;
import com.mungge.orumi.domain.diary.application.DiaryService;
import com.mungge.orumi.domain.diary.domain.Diary;
import com.mungge.orumi.domain.diary.dto.DiaryRequestDto;
import com.mungge.orumi.domain.diary.dto.DiaryResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/diary")
public class DiaryController {

    private final DiaryService diaryService;
    private final ImageService imageService;
    private static String id = "orumi";

    @Operation(summary = "감정구름 고르기", tags = "Diary Controller")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = String.class)))
    @GetMapping("/cloud")
    public ResponseEntity<?> selectCloud() {
        return ResponseEntity.ok("nickname");
    }

    @Operation(summary = "구름일기 작성 + 사진 첨부", tags = "Diary Controller")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Diary.class)))
    @ApiResponse(responseCode = "400", description = "BAD REQUEST")
    @ApiResponse(responseCode = "404", description = "NOT FOUND")
    @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    @PostMapping(value = "/new", consumes = "multipart/form-data")
    public ResponseEntity<?> createDiary(@RequestPart DiaryRequestDto diaryDto, @RequestPart(required = false) MultipartFile requestImage) throws IOException {
        Long imageId = null;
        if (!requestImage.isEmpty()) {
            imageId = imageService.save(requestImage);
        }
        Diary diary = new Diary(id, diaryDto.getEmotion(), diaryDto.getText(), imageId);
        diaryService.createDiary(diary);
        return ResponseEntity.ok(diary);
    }

    @Operation(summary = "나의 구름 + 사진 보기", tags = "Diary Controller")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = DiaryResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "BAD REQUEST")
    @ApiResponse(responseCode = "404", description = "NOT FOUND")
    @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    @GetMapping("/{date}")
    public ResponseEntity<?> getDiary(@PathVariable String date) {
        LocalDate formattedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        Diary diary = diaryService.getDiary(id, formattedDate);
        Image image = imageService.getImage(diary.getImageId());
        DiaryResponseDto responseDto = new DiaryResponseDto(diary.getEmotion(), diary.getText(), diary.getDate());;
        if (image != null) {
            String imageUrl = image.getFileUrl() + image.getFileName();
            responseDto.setImageUrl(imageUrl);
        }

        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "나의 구름 수정 + 사진 첨부", tags = "Diary Controller")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Diary.class)))
    @ApiResponse(responseCode = "400", description = "BAD REQUEST")
    @ApiResponse(responseCode = "404", description = "NOT FOUND")
    @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    @PutMapping(value = "/{date}/update", consumes = "multipart/form-data")
    public ResponseEntity<?> updateDiary(@RequestPart DiaryRequestDto diaryDto, @RequestPart(required = false)MultipartFile requestImage, @PathVariable String date) throws IOException {
        LocalDate formattedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        Diary diary = diaryService.getDiary(id, formattedDate);
        diary.setText(diaryDto.getText());
        diary.setEmotion(diaryDto.getEmotion());

        Long imageId = diary.getImageId();
        if (!requestImage.isEmpty()) {
            imageId = imageService.save(requestImage);
        }
        diaryService.updateDiary(diary);

        return ResponseEntity.ok(diary);
    }
}
