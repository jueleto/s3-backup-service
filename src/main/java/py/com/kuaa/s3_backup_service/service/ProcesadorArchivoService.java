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

    @Autowired
    ZipFileService zipFile;

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
            

            log.info(filePath + " leído exitosamente");
            this.verificar();
            log.info(filePath + " verificado exitosamente");

        } catch (Exception e) {
            String mensajeError = "Error al leer archivo de definiciones json [" + filePath + "] ";
            log.error(mensajeError, e);
            System.exit(1);
        }

    }

    private void verificar() {

        for (BackupDefinitionDto definicion : this.definicionList) {
            // System.out.println(definicion);
            // verificar tipo
            if (!(",original,zip,zipfile,").contains("," + definicion.getTipo() + ",")) {
                String mensajeError = "Archivo de definición incorrecto, tipo [" + definicion.getTipo() + "] ";
                log.error(mensajeError);
                System.exit(1);
            }

            // verificar directorio
            if (definicion.getDirectorio().endsWith("/")) {
                String mensajeError = "Archivo de definición incorrecto, directorio no puede terminar en / [" + definicion.getDirectorio() + "] ";
                log.error(mensajeError);
                System.exit(1);
            }

            // verificar destino
            if (definicion.getDestino().endsWith("/")) {
                String mensajeError = "Archivo de definición incorrecto, destino no puede terminar en / [" + definicion.getDestino() + "] ";
                log.error(mensajeError);
                System.exit(1);
            }
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
            //extraemos el nombre del archivo del path
            filePath = filePath.substring(0, filePath.length() - nombreArchivo.length()-1);
        }

        try {
            BucketObject bucketObject = bucketOperation.uploadFile(
                    reemplazar,
                    nombreArchivo,
                    directorioDestino+filePath,
                    file);
            
        } catch (Exception e) {
            String mensajeError = "Error al subir a aws [" + filePath + "] ";
            log.error(mensajeError, e);
            System.exit(1);
        }
    }

    private void subirArchivoZipAws(String directorioDestino, boolean reemplazar,
            String filePath, File fileZip) {

        if (!fileZip.exists()) {
            String mensajeError = "Archivo zip no existe [" + fileZip.getAbsolutePath() + "] ";
            log.error(mensajeError);
            System.exit(1);
        }

        String nombreArchivoZip = fileZip.getName();

        File file = new File(filePath);
        String nombreArchivo = "";

        if (file.isFile()) {
            nombreArchivo = file.getName();
            //extraemos el nombre del archivo del path
            filePath = filePath.substring(0, filePath.length() - nombreArchivo.length()-1);
        }

        try {
            BucketObject bucketObject = bucketOperation.uploadFile(
                    reemplazar,
                    nombreArchivoZip,
                    directorioDestino+filePath,
                    fileZip);
            
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


        if (definicion.getTipo().equalsIgnoreCase("zipfile")) {
            if (file.isFile()) {
                // subir directamente
                File fileComprimido = zipFile.compressFile(file);
                subirArchivoZipAws(definicion.getDestino(), definicion.isReemplazar(),
                        definicion.getDirectorio(), fileComprimido);
            }

            if (file.isDirectory()) {
                subirDirectorioZipOne(definicion);
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
                                System.out.println("- original Archivo: " + file);
                                // Aquí puedes agregar código para leer y procesar el archivo si es necesario
                                subirArchivoAws(definicion.getDestino(), definicion.isReemplazar(),
                                        file.toString());
                                return FileVisitResult.CONTINUE;
                            }
    
                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                                    throws IOException {
                                // Procesa cada directorio encontrado
                                System.out.println("- original Directorio: " + dir);

                                //crea el directorio
                                bucketOperation
                                        .createDirectory(definicion.isReemplazar(), definicion.getDestino() + "/" + definicion.getDirectorio());

                                return FileVisitResult.CONTINUE;
                            }
    
                            @Override
                            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                                System.err.println("- original Error visitando el archivo: " + file + " (" + exc.getMessage() + ")");
                                return FileVisitResult.CONTINUE;
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }

        

    }




    private void subirDirectorioZipOne(BackupDefinitionDto definicion){
        // Define la ruta inicial del directorio a recorrer
        Path startPath = Paths.get(definicion.getDirectorio());

        try {
            Files.walkFileTree(startPath, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                    new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            // Procesa cada archivo encontrado
                            System.out.println("- zipfile Archivo: " + file);
                            // Aquí puedes agregar código para leer y procesar el archivo si es necesario
                            File fileComprimido = zipFile.compressFile(new File(file.toString()));
                            subirArchivoZipAws(definicion.getDestino(), definicion.isReemplazar(),
                            file.toString(), fileComprimido);

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                                throws IOException {
                            // Procesa cada directorio encontrado
                            System.out.println("- zipfile Directorio: " + dir);

                            //crea el directorio
                            bucketOperation
                                    .createDirectory(definicion.isReemplazar(), definicion.getDestino() + "/" + definicion.getDirectorio());

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                            System.err.println("- zipfile Error visitando el archivo: " + file + " (" + exc.getMessage() + ")");
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

    

}


}
