package py.com.kuaa.s3_backup_service.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import py.com.kuaa.s3_backup_service.definition.BackupDefinitionDto;
import py.com.kuaa.s3_backup_service.s3.BucketObject;
import py.com.kuaa.s3_backup_service.s3.S3OperationInterface;

@Slf4j
@Component
public class ProcesadorArchivoService {

    private List<BackupDefinitionDto> definicionList;

    @Autowired
    S3OperationInterface bucketOperation;

    public ProcesadorArchivoService() {
        log.info("\n##########\nProcesadorArchivoService init\n##########");
    }

    public void leer(String filePath) {

        File file = new File(filePath);

        if (!file.exists()) {
            String mensajeError = "Archivo de definición no existe [" + filePath + "] ";
            log.error(mensajeError);
            System.exit(1);
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Leer el archivo JSON y convertirlo a un objeto BackupDefinitionDto
            this.definicionList = objectMapper.readValue(file,
                    new TypeReference<List<BackupDefinitionDto>>() {
                    });
            // Imprimir para probar
            // for (BackupDefinitionDto definicion : definicionList) {
            // System.out.println(definicion);
            // }

            log.info(filePath + " leído exitosamente");

        } catch (Exception e) {
            String mensajeError = "Error al leer archivo de definiciones json [" + filePath + "] ";
            log.error(mensajeError, e);
            System.exit(1);
        }

    }

    public void procesar() {

        for (BackupDefinitionDto definicionActual : this.definicionList) {

            recorrerFile(definicionActual);

        }

    }

    private void subirArchivoAws(String directorioDestino, boolean reemplazar, String filePath) {

        File file = new File(filePath);

        if (!file.exists()) {
            String mensajeError = "Archivo o directorio no existe [" + filePath + "] ";
            log.error(mensajeError);
            System.exit(1);
        }
        String nombreArchivo = "";

        if (file.isFile()) {
            nombreArchivo = file.getName();
        }

        try {
            BucketObject bucketObject = bucketOperation.uploadFile(
                    nombreArchivo,
                    directorioDestino+"/"+filePath,
                    file);
            log.info("Subido exitosamente: " + bucketObject.getObjectKey());

        } catch (Exception e) {
            String mensajeError = "Error al subir a aws [" + filePath + "] ";
            log.error(mensajeError, e);
            System.exit(1);
        }
    }

    private void recorrerFile(BackupDefinitionDto definicion) {

        File file = new File(definicion.getDirectorio());

        if (!file.exists()) {
            String mensajeError = "Archivo o directorio no existe [" + definicion.getDirectorio() + "] ";
            log.error(mensajeError);
            System.exit(1);
        }


        if (definicion.getTipo().equalsIgnoreCase("original")) {
            if (file.isFile()) {
                // subir directamente
                subirArchivoAws(definicion.getDestino(), definicion.isReemplazar(),
                        definicion.getDirectorio());
            }

            if (file.isDirectory()) {
                subirDirectorioOriginal(definicion);
            }

        }

    }

    private void subirDirectorioOriginal(BackupDefinitionDto definicion){
            // Define la ruta inicial del directorio a recorrer
            Path startPath = Paths.get(definicion.getDirectorio());

            try {
                Files.walkFileTree(startPath, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                        new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                // Procesa cada archivo encontrado
                                System.out.println("subirDirectorioOriginal Archivo: " + file);
                                // Aquí puedes agregar código para leer y procesar el archivo si es necesario
                                subirArchivoAws(definicion.getDestino(), definicion.isReemplazar(),
                                        file.toString());
                                return FileVisitResult.CONTINUE;
                            }
    
                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                                    throws IOException {
                                // Procesa cada directorio encontrado
                                System.out.println("subirDirectorioOriginal Directorio: " + dir);

                                //crea el directorio
                                bucketOperation
                                        .createDirectory(definicion.getDestino() + "/" + definicion.getDirectorio());

                                return FileVisitResult.CONTINUE;
                            }
    
                            @Override
                            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                                System.err.println("subirDirectorioOriginal Error visitando el archivo: " + file + " (" + exc.getMessage() + ")");
                                return FileVisitResult.CONTINUE;
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }

        

    }



}
