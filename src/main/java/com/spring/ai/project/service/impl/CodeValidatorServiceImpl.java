package com.spring.ai.project.service.impl;

import com.spring.ai.project.service.CodeValidatorService;
import org.springframework.stereotype.Service;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;

@Service
public class CodeValidatorServiceImpl implements CodeValidatorService {

    @Override
    public boolean isValidJavaCode(String code) {
        File file = null;

        try {
            // Extract class name dynamically
            String className = extractClassName(code);

            // Create file with correct class name
            file = new File(className + ".java");

            FileWriter writer = new FileWriter(file);
            writer.write(code);
            writer.close();

            // Compile
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            if (compiler == null) {
                throw new RuntimeException("JDK required (not JRE)");
            }

            int result = compiler.run(null, null, null, file.getPath());

            return result == 0;

        } catch (Exception e) {
            return false;

        } finally {
            // Always clean file
            if (file != null && file.exists()) {
                file.delete();
            }
        }
    }

    // Extract class name from AI code
    private String extractClassName(String code) {

        String[] tokens = code.split("\\s+");

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals("class") && i + 1 < tokens.length) {

                return tokens[i + 1]
                        .replaceAll("[^a-zA-Z0-9]", ""); // clean symbols
            }
        }

        // fallback
        return "Temp";
    }
}
