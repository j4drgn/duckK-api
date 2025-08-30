package com.duckchat.api.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class OpenSmileService {
    private final String openSmileExecPath;

    public OpenSmileService() {
        this.openSmileExecPath = "/Users/ryugi62/Desktop/해커톤/opensmile/build/progsrc/smilextract/SMILExtract";
    }

    // openSMILE 실행 및 주요 감정 특성 추출
    public Map<String, String> analyzeEmotionWithOpenSmile(String wavFilePath, String openSmileConfigPath) {
        Map<String, String> result = new HashMap<>();
        try {
            // openSMILE 작업 디렉토리 설정
            java.io.File openSmileDir = new java.io.File("/Users/ryugi62/Desktop/해커톤/opensmile");
            java.io.File outputFile = new java.io.File(openSmileDir, "output.csv");
            
            // openSMILE 명령어 구성 (절대경로 사용)
            String[] command = {
                openSmileExecPath,
                "-C", openSmileConfigPath,
                "-I", wavFilePath,
                "-O", outputFile.getAbsolutePath()
            };
            
            System.out.println("[openSMILE] 실행 명령어: " + String.join(" ", command));
            System.out.println("[openSMILE] 작업 디렉토리: " + openSmileDir.getAbsolutePath());
            System.out.println("[openSMILE] 출력 파일: " + outputFile.getAbsolutePath());
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(openSmileDir);  // 작업 디렉토리 설정
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                System.out.println("[openSMILE] " + line);
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            System.out.println("[openSMILE] 프로세스 종료 코드: " + exitCode);
            
            if (exitCode == 0) {
                // output.csv에서 주요 감정 특성만 추출
                if (outputFile.exists()) {
                    java.util.List<String> lines = java.nio.file.Files.readAllLines(outputFile.toPath());
                    System.out.println("[openSMILE] CSV 파일 라인 수: " + lines.size());
                    
                    if (lines.size() > 1) {
                        String[] headers = lines.get(0).split(",");
                        String[] values = lines.get(1).split(",");
                        System.out.println("[openSMILE] 헤더 수: " + headers.length + ", 값 수: " + values.length);
                        
                        // 대표 피처: F0final_sma(평균 pitch), pcm_RMSenergy_sma(에너지), voicingFinalUnclipped_sma(voice prob)
                        String[] mainFeatures = {"F0final_sma", "pcm_RMSenergy_sma", "voicingFinalUnclipped_sma"};
                        for (String feat : mainFeatures) {
                            for (int i = 0; i < headers.length; i++) {
                                if (headers[i].equals(feat) && i < values.length) {
                                    result.put(feat, values[i]);
                                    System.out.println("[openSMILE] 추출된 피처 " + feat + ": " + values[i]);
                                }
                            }
                        }
                    } else {
                        result.put("error", "CSV 파일에 데이터가 부족합니다");
                    }
                } else {
                    result.put("error", "output.csv 파일이 생성되지 않았습니다");
                }
            } else {
                result.put("error", "openSMILE 실행 실패 (종료 코드: " + exitCode + ")\n출력: " + output.toString());
            }
        } catch (Exception e) {
            System.err.println("[openSMILE] 예외 발생: " + e.getMessage());
            e.printStackTrace();
            result.put("error", "openSMILE 실행 중 예외: " + e.getMessage());
        }
        return result;
    }
}
