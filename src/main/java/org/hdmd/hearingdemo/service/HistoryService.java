package org.hdmd.hearingdemo.service;

import lombok.RequiredArgsConstructor;
import org.hdmd.hearingdemo.dto.HistoryDTO;
import org.hdmd.hearingdemo.exception.HistoryNotFoundException;
import org.hdmd.hearingdemo.model.History;
import org.hdmd.hearingdemo.repository.HistoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class HistoryService {
    private final HistoryRepository historyRepository;

    //특정 기록 조회
    @Transactional(readOnly = true)
    public HistoryDTO getHistoryById(Long id) {
        History history = historyRepository.findById(id)
                .orElseThrow(() -> new HistoryNotFoundException("기록을 찾을 수 없습니다."));

        HistoryDTO historyDTO = new HistoryDTO();
        historyDTO.setHistoryId(history.getId());
        historyDTO.setTimestamp(history.getTimestamp());
        historyDTO.setFilepath(history.getFilepath());
        historyDTO.setLocation(history.getLocation());
        historyDTO.setText(history.getText());
        historyDTO.setDevice(history.getDevice().getDeviceName());

        return historyDTO;
    }

    //특정 단말기에 저장된 기록 전부 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllHistoriesByDeviceId(Long deviceId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
        List<History> histories = historyRepository.findByDeviceId(deviceId, sort);


        return histories.stream()
                .map(history -> {
                    Map<String, Object> historyMap = new HashMap<>();
                    historyMap.put("historyId", history.getId());
                    historyMap.put("timestamp", history.getTimestamp());
                    historyMap.put("location", history.getLocation());
                    historyMap.put("text", history.getText());
                    return historyMap;
                })
                .collect(Collectors.toList());
    }
}