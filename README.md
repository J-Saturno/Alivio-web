# Alivio Web

Aplicación académica de gestión de cuidados domiciliarios construida con Spring Boot, Spring Web, Thymeleaf y Bootstrap.

## Ejecutar

```powershell
.\mvnw.cmd spring-boot:run
```

Abrir `http://localhost:8080`.

## Acceso administrativo de demostración

Desde la portada, seleccionar `Acceso administrativo - Demo`. Esta etapa no implementa autenticación real.

## Datos

Las solicitudes y profesionales se guardan temporalmente en memoria y se reinician al detener la aplicación. MySQL y Spring Security pertenecen a unidades posteriores del curso.

## Verificar

Requiere Java 21 y Node.js para la prueba de JavaScript.

```powershell
.\mvnw.cmd test
node --test src/test/js/admin-focus.test.cjs
```

El formulario público y el servidor limitan los nombres a 100 caracteres, el distrito a 80 y la descripción a 500. Las pruebas comprueban los límites y que los registros inválidos no se guarden.

La verificación visual en celular, tableta y escritorio se realiza por separado; las pruebas automáticas no la sustituyen.
