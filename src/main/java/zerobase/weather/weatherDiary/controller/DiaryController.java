package zerobase.weather.weatherDiary.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import zerobase.weather.weatherDiary.entity.Diary;
import zerobase.weather.weatherDiary.service.DiaryService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @ApiOperation(value = "일기 텍스트와 날씨를 이용해서 DB에 일기 저장", notes = "이것은 노트")
    @PostMapping("/create/diary")
    void createDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                     @RequestBody String text) {
        diaryService.createDiary(date, text);
    }
    @ApiOperation("선택한 날짜의 모든 일기 데이터를 가져옵니다")
    @GetMapping("/read/diary")
    List<Diary> readDiary (@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return diaryService.readDiary(date);
    }
    @ApiOperation("선택한 기간 중의 모든 일기 데이터를 가져옵니다.")
    @GetMapping("/read/diaries")
    List<Diary> readDiaries (@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam (value = "조회할 기간의 첫번째 날", example = "2020-02-04") LocalDate startDate,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam (value = "조회할 기간의 마지막 날", example = "2020-02-04") LocalDate endDate) {
        return diaryService.readDiaries(startDate, endDate);
    }
    @PutMapping("/update/diary")
    void updateDiary (@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                      @RequestBody String text) {
        diaryService.updateDiary(date, text);
    }
    @ApiOperation("선택한 날짜의 모든 일기 데이터를 삭제합니다")
    @DeleteMapping("/delete/diary")
    void deleteDiary (@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        diaryService.deleteDiary(date);
    }
}
