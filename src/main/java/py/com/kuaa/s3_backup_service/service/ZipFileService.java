package py.com.kuaa.s3_backup_service.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

@Slf4j
@Service
public class ZipFileService {

    public File compressFile(File fileToCompress) {

        try {
            // Obtener el directorio temporal del sistema
            String tempDir = System.getProperty("java.io.tmpdir");
            Path s3TempBackupDir = Paths.get(tempDir, "s3tempBack");

            // Crear el directorio s3tempBack si no existe
            if (!Files.exists(s3TempBackupDir)) {

                Files.createDirectory(s3TempBackupDir);

            }

            // Definir el nombre del archivo ZIP resultante en el directorio s3tempBack
            String zipFileName = fileToCompress.getName() + ".zip";
            Path zipFilePath = s3TempBackupDir.resolve(zipFileName);

            // Determinar el comando del SO
            String command = getZipCommand(fileToCompress, zipFilePath);

            // Ejecutar el comando del SO
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            if(fileToCompress.isFile()){
                // cuando es archivo
                processBuilder.directory(fileToCompress.getParentFile()); //comprime con directorio padre y todo
            }else{
                // cuando es directorio
                processBuilder.directory(fileToCompress); // Cambiar al directorio que contiene el contenido a comprimir
            }

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IOException("Error al comprimir el archivo, código de salida: " + exitCode);
            }

            // Retornar el archivo ZIP resultante
            return zipFilePath.toFile();
        } catch (IOException e) {

            String mensajeError = "IO Error al comprimir [" + fileToCompress.getAbsolutePath() + "] ";
            log.error(mensajeError, e);
            System.exit(1);
        } catch (InterruptedException e) {
            String mensajeError = "IE Error al comprimir [" + fileToCompress.getAbsolutePath() + "] ";
            log.error(mensajeError, e);
            System.exit(1);
        }

        return null;
    }

    private String getZipCommand(File fileToCompress, Path zipFilePath) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            if (fileToCompress.isDirectory()) {
                // comprime todo el predirectorio
                // return String.format("powershell Compress-Archive -Path %s -DestinationPath
                // %s",
                // fileToCompress.getAbsolutePath(), zipFilePath.toAbsolutePath().toString());

                // comprime solo el contenido
                return String.format("powershell Compress-Archive -Path %s\\* -DestinationPath %s",
                        fileToCompress.getAbsolutePath(), zipFilePath.toAbsolutePath().toString());

            }
            // Comando para Windows (requiere que zip esté instalado y en el PATH)
            return String.format("powershell Compress-Archive -Path %s -DestinationPath %s",
                    fileToCompress.getAbsolutePath(), zipFilePath.toAbsolutePath().toString());
        } else {

            if (fileToCompress.isDirectory()) {
                // if(fileToCompress.getName().equalsIgnoreCase(".DS_Store")){

                // }
                // comprime todo el predirectorio
                // return String.format("zip -r %s %s",
                // zipFilePath.toAbsolutePath().toString(), fileToCompress.getAbsolutePath());

                // comprime dentro con una carpeta padre adentro
                // return String.format("zip -r %s %s",
                // zipFilePath.toAbsolutePath().toString(), fileToCompress.getName());

                // comprime solo el contenido
                return String.format("zip -r %s .",
                        zipFilePath.toAbsolutePath().toString());
            }

            // Comando para Unix-like (Linux/Mac)
            return String.format("zip -j %s %s",
                    zipFilePath.toAbsolutePath().toString(), fileToCompress.getAbsolutePath());
        }
    }
}
