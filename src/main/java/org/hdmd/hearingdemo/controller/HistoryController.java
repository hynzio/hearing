package org.hdmd.hearingdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hdmd.hearingdemo.dto.HistoryDTO;
import org.hdmd.hearingdemo.exception.HistoryNotFoundException;
import org.hdmd.hearingdemo.service.HistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Tag(name = "기록 관리", description = "기록으로 저장된 위험상황 데이터 관련 API")
@RestController
@RequestMapping("/api1/histories") // 수정: RequestMapping을 사용하여 기본 경로 설정
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("/{historyId}")
    @Operation(
            summary = "특정 기록 조회",
            description = "ID로 특정 기록을 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 조회됨"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 데이터"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")})
    public ResponseEntity<HistoryDTO> getHistory(@PathVariable Long historyId) {
        HistoryDTO historyDTO = historyService.getHistoryById(historyId);
        return ResponseEntity.ok(historyDTO);


    }   
    @GetMapping("/device/{deviceId}")
    @Operation(
            summary = "특정 단말기의 기록 모두 조회",
            description = "해당 단말기에 저장된 모든 기록을 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 기록을 조회함"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 단말기 ID"),
                    @ApiResponse(responseCode = "500", description = "서버 오류 발생")
            })

    public ResponseEntity<List<Map<String, Object>>> getAllHistoriesByDeviceId(@PathVariable Long deviceId) {
        try {
            // 서비스에서 List<Map<String, Object>> 형태로 데이터를 받음
            List<Map<String, Object>> historyResponses = historyService.getAllHistoriesByDeviceId(deviceId);

            if (historyResponses.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonList(Map.of("message", "해당 단말기에 저장된 기록이 없습니다.")));
            }

            return ResponseEntity.ok(historyResponses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(Map.of("message", "서버 오류가 발생했습니다.")));
        }
    }


    // 예외 처리 메서드
    @ExceptionHandler(HistoryNotFoundException.class)
    public ResponseEntity<String> handleHistoryNotFoundException(HistoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}