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

    // 오디오 파일을 WAV로 변환
    private String convertToWav(String inputFilePath) {
        try {
            // ffmpeg를 사용해서 WAV로 변환 (16kHz, mono)
            String outputWavPath = inputFilePath.replace(".wav", "_converted.wav");
            
            String[] convertCommand = {
                "ffmpeg", "-y", "-i", inputFilePath, 
                "-acodec", "pcm_s16le", "-ar", "16000", "-ac", "1", 
                outputWavPath
            };
            
            ProcessBuilder convertPb = new ProcessBuilder(convertCommand);
            convertPb.redirectErrorStream(true);
            Process convertProcess = convertPb.start();
            
            BufferedReader convertReader = new BufferedReader(new InputStreamReader(convertProcess.getInputStream()));
            String convertLine;
            while ((convertLine = convertReader.readLine()) != null) {
                System.out.println("[FFmpeg] " + convertLine);
            }
            
            int convertExitCode = convertProcess.waitFor();
            if (convertExitCode == 0) {
                System.out.println("오디오 변환 성공: " + outputWavPath);
                return outputWavPath;
            } else {
                System.err.println("오디오 변환 실패");
                return inputFilePath; // 변환 실패시 원본 파일 사용
            }
        } catch (Exception e) {
            System.err.println("오디오 변환 중 오류: " + e.getMessage());
            return inputFilePath; // 변환 실패시 원본 파일 사용
        }
    }

    // openSMILE 실행 및 주요 감정 특성 추출
    public Map<String, String> analyzeEmotionWithOpenSmile(String wavFilePath, String openSmileConfigPath) {
        Map<String, String> result = new HashMap<>();
        try {
            // 먼저 오디오 파일을 WAV로 변환 시도
            String processedWavPath = convertToWav(wavFilePath);
            
            // openSMILE 작업 디렉토리 설정
            java.io.File openSmileDir = new java.io.File("/Users/ryugi62/Desktop/해커톤/opensmile");
            java.io.File outputFile = new java.io.File(openSmileDir, "output.csv");
            
            // openSMILE 명령어 구성 (절대경로 사용)
            String[] command = {
                openSmileExecPath,
                "-C", openSmileConfigPath,
                "-I", processedWavPath,
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
                // ARFF 포맷의 CSV 파일에서 주요 감정 특성만 추출
                if (outputFile.exists()) {
                    java.util.List<String> lines = java.nio.file.Files.readAllLines(outputFile.toPath());
                    System.out.println("[openSMILE] CSV 파일 라인 수: " + lines.size());
                    
                    // ARFF 포맷 파싱
                    java.util.List<String> headers = new java.util.ArrayList<>();
                    java.util.List<String> dataValues = new java.util.ArrayList<>();
                    
                    boolean inDataSection = false;
                    for (String dataLine : lines) {
                        dataLine = dataLine.trim();
                        if (dataLine.startsWith("@attribute")) {
                            // @attribute name string 또는 @attribute feature_name numeric
                            String[] parts = dataLine.split("\\s+");
                            if (parts.length >= 3) {
                                String attrName = parts[1];
                                headers.add(attrName);
                            }
                        } else if (dataLine.startsWith("@data")) {
                            inDataSection = true;
                        } else if (inDataSection && !dataLine.isEmpty() && !dataLine.startsWith("%")) {
                            // 실제 데이터 라인
                            dataValues.add(dataLine);
                        }
                    }
                    
                    System.out.println("[openSMILE] 헤더 수: " + headers.size() + ", 데이터 라인 수: " + dataValues.size());
                    
                    if (!dataValues.isEmpty()) {
                        // 마지막 데이터 라인을 사용 (가장 최근 분석 결과)
                        String lastDataLine = dataValues.get(dataValues.size() - 1);
                        String[] values = lastDataLine.split(",");
                        
                        System.out.println("[openSMILE] 값 수: " + values.length);
                        
                        // 대표 피처: F0final_sma(평균 pitch), pcm_RMSenergy_sma(에너지), voicingFinalUnclipped_sma(voice prob)
                        String[] mainFeatures = {"F0final_sma", "pcm_RMSenergy_sma", "voicingFinalUnclipped_sma"};
                        for (String feat : mainFeatures) {
                            int headerIndex = headers.indexOf(feat);
                            if (headerIndex >= 0 && headerIndex < values.length) {
                                String value = values[headerIndex].trim();
                                if (!value.equals("?") && !value.isEmpty()) {
                                    result.put(feat, value);
                                    System.out.println("[openSMILE] 추출된 피처 " + feat + ": " + value);
                                }
                            }
                        }
                    } else {
                        result.put("error", "ARFF 파일에 데이터가 없습니다");
                    }
                } else {
                    result.put("error", "output.csv 파일이 생성되지 않았습니다");
                }
            } else {
                result.put("error", "openSMILE 실행 실패 (종료 코드: " + exitCode + ")\n출력: " + output.toString());
            }
            
            // 변환된 파일이 있으면 삭제
            if (!processedWavPath.equals(wavFilePath)) {
                try {
                    java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(processedWavPath));
                } catch (Exception e) {
                    System.err.println("변환된 파일 삭제 실패: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[openSMILE] 예외 발생: " + e.getMessage());
            e.printStackTrace();
            result.put("error", "openSMILE 실행 중 예외: " + e.getMessage());
        }
        return result;
    }
}
