package com.duckchat.api.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class OpenSmileService {
    private final String openSmileExecPath;

    public OpenSmileService(String openSmileExecPath) {
        this.openSmileExecPath = openSmileExecPath;
    }

    // openSMILE 실행 및 주요 감정 특성 추출
    public Map<String, String> analyzeEmotionWithOpenSmile(String wavFilePath, String openSmileConfigPath) {
        Map<String, String> result = new HashMap<>();
        try {
            // openSMILE 명령어 구성 (절대경로 사용)
            String[] command = {
                openSmileExecPath,
                "-C", openSmileConfigPath,
                "-I", wavFilePath,
                "-O", "output.csv"
            };
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[openSMILE] " + line);
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // output.csv에서 주요 감정 특성 추출(간단 예시)
                java.nio.file.Path csvPath = java.nio.file.Paths.get("output.csv");
                java.util.List<String> lines = java.nio.file.Files.readAllLines(csvPath);
                if (lines.size() > 1) {
                    String[] headers = lines.get(0).split(",");
                    String[] values = lines.get(1).split(",");
                    for (int i = 0; i < headers.length && i < values.length; i++) {
                        result.put(headers[i], values[i]);
                    }
                }
            } else {
                result.put("error", "openSMILE 실행 실패");
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }
}
