
/*validacion de formulario*/ 
document.addEventListener('DOMContentLoaded', function () {
    var modalSolicitud = document.getElementById('modalSolicitud');
    var formulario = document.getElementById('formSolicitudServicio');

    // 1. Inyectar título y descripción breve al abrir el modal
    if (modalSolicitud != null) {
        modalSolicitud.addEventListener('show.bs.modal', function (evento) {
            var boton = evento.relatedTarget;
            var nombreServicio = boton.getAttribute('data-servicio');
            var descServicio = boton.getAttribute('data-desc');
            
            document.getElementById('nombreServicioSeleccionado').innerHTML = nombreServicio;
            document.getElementById('descripcionServicioModal').innerHTML = descServicio;
        });
    }

    // 2. Validación clásica del formulario
    if (formulario != null) {
        formulario.addEventListener('submit', function (evento) {
            evento.preventDefault();

            var inputNombre = document.getElementById('nombreCliente');
            var inputTelefono = document.getElementById('telefonoCliente');
            
            var errorNombre = document.getElementById('errorNombre');
            var errorTelefono = document.getElementById('errorTelefono');
            var mensajeExito = document.getElementById('mensajeExitoSolicitud');

            // Limpiar errores previos
            errorNombre.style.display = 'none';
            errorTelefono.style.display = 'none';
            mensajeExito.style.display = 'none';
            
            inputNombre.classList.remove('is-invalid');
            inputTelefono.classList.remove('is-invalid');

            var esValido = true;

            // Validación de Nombre
            if (inputNombre.value.trim() === "") {
                errorNombre.innerHTML = "Por favor, ingresa tu nombre completo.";
                errorNombre.style.display = "block";
                inputNombre.classList.add('is-invalid');
                esValido = false;
            }

            // Expresión regular tradicional para comprobar si solo contiene números
            var soloNumeros = /^[0-9]+$/;

            // Validación de Teléfono (Obligatorio, sin letras, longitud mínima)
            if (inputTelefono.value.trim() === "") {
                errorTelefono.innerHTML = "Por favor, ingresa tu número de teléfono.";
                errorTelefono.style.display = "block";
                inputTelefono.classList.add('is-invalid');
                esValido = false;
            } else if (!soloNumeros.test(inputTelefono.value.trim())) {
                errorTelefono.innerHTML = "El teléfono solo debe contener números (sin letras ni espacios).";
                errorTelefono.style.display = "block";
                inputTelefono.classList.add('is-invalid');
                esValido = false;
            } else if (inputTelefono.value.trim().length < 7) {
                errorTelefono.innerHTML = "El número de teléfono es demasiado corto.";
                errorTelefono.style.display = "block";
                inputTelefono.classList.add('is-invalid');
                esValido = false;
            }

            // Si pasa todas las validaciones
            if (esValido === true) {
                mensajeExito.innerHTML = "¡Solicitud registrada con éxito! Te contactaremos pronto.";
                mensajeExito.style.display = "block";

                // Limpiar campos
                inputNombre.value = "";
                inputTelefono.value = "";

                // Cerrar modal tras 2 segundos
                setTimeout(function() {
                    var modalInstance = bootstrap.Modal.getInstance(modalSolicitud);
                    if (modalInstance != null) {
                        modalInstance.hide();
                    }
                    mensajeExito.style.display = "none";
                }, 2000);
            }
        });
    }
});