## s3-backup-service
Es una app springboot para realizar copias de seguridad de direcctorios o archivos definidos al servicio de almacenamiento de aws s3

### Definición de backup
Al programa se le pasa un archivo json donde está definido de que y como se realizarán las copias. El formato del archivo es
```json
[
    {
        "tipo": "",
        "directorio": "",
        "destinoBase": "",
        "destinoForzado": "",
        "reemplazar": false,
        "nota": ""
    }
]
```
- tipo:
- - original: se sube el archivo o directorio tal cual como está.
- - zipdirectory : comprime todo el directorio y lo sube como archivo único, si o si debe ser directorio.
- - zipfile: recorre el dirctorio, comprime cada archivo en forma individual y lo sube en el mismo directorio.
- directorio:
- - El directorio o archivo que se subirá o recorrerá para subir todo lo que está dentro
- destinoBase:
- - El directorio donde se subirá el archivo, dejar vacio para que use la raiz.
- destinoForzado:
- - El `directorio` donde se subirá será reemplazado por `destinoForzado`, solo funciona para archivos y no para directorios. Dejar vacio para no activarlo
- reemplazar: 
- - Si se reemplazará o no el archivo o los archivos.
- nota:
- - texto adicional para describir o algo que se quiera poner


